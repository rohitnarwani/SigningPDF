import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.qoppa.pdf.SignatureAppearance;
import com.qoppa.pdf.SigningInformation;
import com.qoppa.pdf.form.SignatureField;
import com.qoppa.pdfSecure.PDFSecure;

/**
 * This is a PDF util class to merge two PDF and sign a PDF
 * 
 * @author rohit
 *
 */
public class PDFUtil {

	public static void main(String[] args) {
		try {
			List<InputStream> inputPdfList = new ArrayList<InputStream>();
			inputPdfList.add(new FileInputStream(
					"C:\\Users\\admin\\Downloads\\pdf1.pdf"));
			inputPdfList.add(new FileInputStream(
					"C:\\Users\\admin\\Downloads\\pdf2.pdf"));

			// Prepare output stream for merged pdf file.
			OutputStream outputStream = new FileOutputStream(
					"C:\\Users\\admin\\Downloads\\MergeFile_1234.pdf");

			// merge pdf files.
			mergePdfFiles(inputPdfList, outputStream);
			// sign PDF files
			signPDF("E:\\rohit\\MergeFile_1234.pdf",
					"E:\\rohit\\keystore1.pkx", "mykey", "password",
					"E:\\rohit\\output.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will take list of pdf and merge it and produce a final PDF
	 * 
	 * @param inputPdfList
	 * @param outputStream
	 * @throws Exception
	 */
	public static void mergePdfFiles(List<InputStream> inputPdfList,
			OutputStream outputStream) throws Exception {
		// Create document and pdfReader objects.
		Document document = new Document();
		List<PdfReader> readers = new ArrayList<PdfReader>();
		int totalPages = 0;

		// Create pdf Iterator object using inputPdfList.
		Iterator<InputStream> pdfIterator = inputPdfList.iterator();

		// Create reader list for the input pdf files.
		while (pdfIterator.hasNext()) {
			InputStream pdf = pdfIterator.next();
			PdfReader pdfReader = new PdfReader(pdf);
			readers.add(pdfReader);
			totalPages = totalPages + pdfReader.getNumberOfPages();
		}

		// Create writer for the outputStream
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);

		// Open document.
		document.open();

		// Contain the pdf data.
		PdfContentByte pageContentByte = writer.getDirectContent();

		PdfImportedPage pdfImportedPage;
		int currentPdfReaderPage = 1;
		Iterator<PdfReader> iteratorPDFReader = readers.iterator();

		// Iterate and process the reader list.
		while (iteratorPDFReader.hasNext()) {
			PdfReader pdfReader = iteratorPDFReader.next();
			// Create page and add content.
			while (currentPdfReaderPage <= pdfReader.getNumberOfPages()) {
				document.newPage();
				pdfImportedPage = writer.getImportedPage(pdfReader,
						currentPdfReaderPage);
				pageContentByte.addTemplate(pdfImportedPage, 0, 0);
				currentPdfReaderPage++;
			}
			currentPdfReaderPage = 1;
		}

		// Close document and outputStream.
		outputStream.flush();
		document.close();
		outputStream.close();

		System.out.println("Pdf files merged successfully.");
	}

	/**
	 * This method will sign the input pdf using the input
	 * keystore/password/keyname and produce the final PDF
	 * 
	 * @param pdfToSignPath
	 * @param keyStorePath
	 * @param keyName
	 * @param keyStorePasswd
	 * @param finalPDFPath
	 * @throws Exception
	 */
	public static void signPDF(String pdfToSignPath, String keyStorePath,
			String keyName, String keyStorePasswd, String finalPDFPath)
			throws Exception {

		PDFSecure pdfDoc = new PDFSecure(pdfToSignPath, null);
		FileInputStream pkcs12Stream = new FileInputStream(keyStorePath);
		KeyStore store = KeyStore.getInstance("PKCS12");
		store.load(pkcs12Stream, keyStorePasswd.toCharArray());
		pkcs12Stream.close();
		SigningInformation signInfo = new SigningInformation(store, keyName,
				keyStorePasswd);

		// Customize signature appearance
		SignatureAppearance signAppear = signInfo.getSignatureAppearance();

		signAppear.setVisibleName(false);
		// signAppear.setImagePosition(SwingConstants.LEFT);
		// signAppear.setImageFile("C:\\test\\image.png");

		// Only show the signer's name and date on the right side of the
		// signature field
		signAppear.setVisibleCommonName(false);
		signAppear.setVisibleOrgUnit(false);
		signAppear.setVisibleOrgName(false);
		signAppear.setVisibleLocal(false);
		signAppear.setVisibleState(false);
		signAppear.setVisibleCountry(false);
		signAppear.setVisibleEmail(false);

		// Create signature field on the first page
		java.awt.geom.Rectangle2D signBounds = new java.awt.geom.Rectangle2D.Double(
				36, 36, 144, 48);
		SignatureField signField = pdfDoc.addSignatureField(0, "signature",
				signBounds);

		// Apply digital signature
		pdfDoc.signDocument(signField, signInfo);

		// Save the document
		pdfDoc.saveDocument(finalPDFPath);
		System.out.println("Pdf file signed successfully.");
	}
}
