 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.view.AbstractView;
 
 import com.itextpdf.text.BadElementException;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.ExceptionConverter;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.ColumnText;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPageEvent;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.CustomPdfPTableBuilder;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.ParagraphBuilder;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.PhraseBuilder;
 
 /**
  * This class generates PDF views and files in DIN A4 in Priebes-style.</br> 
  * Therefore it has all settings (e.g. {@link Font}) and methods to create letters.
  * 
  * @author Marek
  * 
  */
 @Component
 public abstract class PriebesIText5PdfView extends AbstractView implements
 		PdfPageEvent {
 
 	/** 
 	 * font settings
 	 */
 	private static final String ENCODING = BaseFont.CP1252;
 	public static final String FONT = "Arial";
 	public static final float FONT_SIZE = 10;
	public static final Font font = FontFactory.getFont(FONT, ENCODING, false, FONT_SIZE);
 	public static final Font boldFont = FontFactory.getFont(FONT, ENCODING, FONT_SIZE, Font.BOLD);
 	public static final Font twelveSizefont = FontFactory.getFont(FONT, ENCODING, 12, Font.BOLD);
 	
 
 	/**
 	 * format settings
 	 */
 	public final static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
 	public static final float BORDER_WIDTH = 0.15f;
 	public static final int PAGE_MARGIN_BOTTOM = /*bottom*/180;
 	public static final int PAGE_MARGIN_TOP = /*top*/80;
 	public static final int PAGE_MARGIN_RIGHT = /*right*/72;
 	public static final int PAGE_MARGIN_LEFT = /*left*/60;
 	public static final int FOOTER_MARGIN_BOTTOM = 30;
 	public static final float WIDTH = 464f;
 	
 	
 	/**
 	 * texts
 	 */
 	protected static final String UEBER_EMPFAENGERADRESSE = "Maxstrasse1, 71636 Ludwigsburg";
 	protected static final String HEADER_ZEILE1 = "Maxstrasse 1";
 	protected static final String HEADER_ZEILE2 = "71636 Ludwigsburg";
 	protected static final String HEADER_ZEILE3 = "www.priebes.eu";
 	
 	/**
 	 * other properties
 	 */
 	private static final boolean SHOW_PAGE_NUMBERS = false;
 
 	private String logoPath = null;
 
 	public PriebesIText5PdfView() {
 		setContentType("application/pdf");
 	}
 
 	@Override
 	protected boolean generatesDownloadContent() {
 		return true;
 	}
 
 	@Override
 	protected final void renderMergedOutputModel(Map<String, Object> model,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 
 		// IE workaround: write into byte array first.
 		ByteArrayOutputStream baos = createTemporaryOutputStream();
 
 		// Apply preferences and build metadata.
 
 		Document document = newDocument();
 		PdfWriter writer = newWriter(document, baos);
 		prepareWriter(model, writer, request);
 		buildPdfMetadata(model, document, request);
 
 		// Build PDF document.
 		document.open();
 		buildPdfDocument(model, document, writer, request, response);
 		document.close();
 
 		// Flush to HTTP response.
 		writeToResponse(response, baos);
 	}
 
 	/**
 	 * sets format and margins of document
 	 * @return
 	 */
 	protected Document newDocument() {
 		Document doc = new Document(PageSize.A4);
 		doc.setMargins(PAGE_MARGIN_LEFT, PAGE_MARGIN_RIGHT, PAGE_MARGIN_TOP, PAGE_MARGIN_BOTTOM);
 		return doc;
 	}
 
 	protected PdfWriter newWriter(Document document, OutputStream os)
 			throws DocumentException {
 		return PdfWriter.getInstance(document, os);
 	}
 
 	protected void prepareWriter(Map<String, Object> model, PdfWriter writer,
 			HttpServletRequest request) throws DocumentException {
 		writer.setViewerPreferences(getViewerPreferences());
 		writer.setPageEvent(this);
 	}
 
 	protected int getViewerPreferences() {
 		return PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage;
 	}
 
 	protected void buildPdfMetadata(Map<String, Object> model,
 			Document document, HttpServletRequest request) {
 	}
 
 	protected abstract void buildPdfDocument(Map<String, Object> model,
 			Document document, PdfWriter writer, HttpServletRequest request,
 			HttpServletResponse response) throws Exception;
 
 	/**
 	 * Einfuegen des Headers in ein PDF-Document
 	 * 
 	 * @param document
 	 * @throws BadElementException
 	 * @throws MalformedURLException
 	 * @throws DocumentException
 	 * @throws IOException
 	 */
 	public void insertHeader(Document document) throws BadElementException,
 			MalformedURLException, DocumentException, IOException {
 
 	}
 
 	private Image createLogo() throws BadElementException,
 	MalformedURLException, IOException {
 		if (logoPath != null)
 			return Image.getInstance(logoPath);
 		else
 		return Image.getInstance(this.getServletContext()
 				.getRealPath("/images").concat("/LogoGross.jpg"));
 	}
 	
 	/**
 	 * Einfgen eines Betreffs / Titels des Dokuments
 	 * 
 	 * @param doc
 	 * @param title
 	 *            Der Dokumententitel wie z.B. "Rechnung"
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws DocumentException
 	 */
 	public void insertSubject(Document doc, String title)
 			throws MalformedURLException, IOException, DocumentException {
 
 		doc.add(new ParagraphBuilder(title)
 			.withFont(FontFactory.getFont(FONT, 12, Font.BOLD))
 			.build());
 	}
 
 	public void insertSmallText(Document doc, String text)
 			throws MalformedURLException, IOException, DocumentException {
 
 		Paragraph p = new Paragraph();
 		p.setFont(FontFactory.getFont(FONT, 10, Font.NORMAL));
 		p.add(text);
 		doc.add(p);
 	}
 
 	/**
 	 * @deprecated
 	 * @param doc
 	 * @param info Der Dokumententitel wie z.B. "Rechnung"
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws DocumentException
 	 */
 	public void insertInfo(Document doc, String info)
 			throws MalformedURLException, IOException, DocumentException {
 		doc.add(new ParagraphBuilder(info).build());
 	}
 
 	@Override
 	public void onOpenDocument(PdfWriter writer, Document document) {
 		total = writer.getDirectContent().createTemplate(30, 16);
 		insertBigLogo(writer);
 	}
 
 	/**
 	 * Creates PDF-Footer
 	 */
 	@Override
 	public void onStartPage(PdfWriter writer, Document document) {
 
 		PdfPTable footer = new PdfPTable(1);
 		footer.setTotalWidth(527);
 		footer.setLockedWidth(true);
 		footer.setHorizontalAlignment(Element.ALIGN_CENTER);
 		footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 		Paragraph fPara = new ParagraphBuilder("priebes OHG / Maxstrasse 1 / 71636 Ludwigsburg")
 			.addTextLine("www.priebes.eu / info@priebes.eu / 0162 7014338 / 07141 - 9475640 (auch Fax)")
 			.addTextLine("KSK Ludwigsburg BLZ 60450050 - Kto 30055142 / HRA 725747 / Ust-IdNr.: DE275948390")
 			.addTextLine("IBAN: DE79604500500030055142 / BIC-/SWIFT-Code: SOLADES1LBG")
 			.withAlignment(Element.ALIGN_CENTER)
 			.withFont(FontFactory.getFont(FONT, 9, Font.NORMAL))
 			.withLineSpacing(12f)
 			.build();
 		PdfPCell footerCell = new PdfPCell();
 		footerCell.addElement(fPara);
 		//footerCell.setBorder(Rectangle.TOP);
 		footerCell.setBorder(Rectangle.NO_BORDER);
 		footer.addCell(footerCell);
 		footer.writeSelectedRows(0, -1, /*xPos*/34, /*yPos*/120, writer.getDirectContent());
 
 		if (SHOW_PAGE_NUMBERS) addPageNumber(writer, document);
 
 	}
 
 	public void insertBigLogo(PdfWriter writer) {
 		try {
 			Image img = Image.getInstance(createLogo());
 			img.setAlignment(Image.RIGHT);
 			img.scaleToFit(180, 75);
 
 			PdfPTable table = new PdfPTable(3);
 			// Adresse im Header
 			PdfPCell headerCell = new PdfPCell();
 			headerCell.setBorder(Rectangle.NO_BORDER);
 			headerCell.addElement(img);
 			headerCell.addElement(
 					new ParagraphBuilder("priebes OHG")
 					.withFont(FontFactory.getFont(FONT, 12, Font.NORMAL))
 					.withAlignment(Element.ALIGN_RIGHT)
 					.withLineSpacing(25f)
 					.build());
 			headerCell.addElement(new ParagraphBuilder(HEADER_ZEILE1).withAlignment(Element.ALIGN_RIGHT).build());
 			headerCell.addElement(new ParagraphBuilder(HEADER_ZEILE2).withAlignment(Element.ALIGN_RIGHT).build());
 			headerCell.addElement(new ParagraphBuilder(HEADER_ZEILE3).withAlignment(Element.ALIGN_RIGHT).build());
 
 			table.setWidths(new int[] { 10, 10, 30 });
 			table.setTotalWidth(527);
 			table.setLockedWidth(true);
 			table.getDefaultCell().setFixedHeight(20);
 			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
 			table.addCell("");
 			table.addCell("");
 			table.addCell(headerCell);
 			table.writeSelectedRows(0, -1, 0, 788, writer.getDirectContent());
 
 		} catch (DocumentException de) {
 			throw new ExceptionConverter(de);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	PdfTemplate total;
 
 	@Override
 	public void onEndPage(PdfWriter writer, Document document) {
 	}
 
 	private void addPageNumber(PdfWriter writer, Document document) {
 		Image img2;
 		int x = 550;
 		int y = 20;
 		try {
 			img2 = Image.getInstance(total);
 			img2.setAbsolutePosition(x, y);
 			document.add(img2);
 		} catch (BadElementException e) {
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		absText(writer, String.format("S. %d / ", writer.getPageNumber()),
 					x - 25, y + 2);
 
 	}
 
 	private static void absText(PdfWriter writer, String text, int x, int y) {
 		PdfContentByte cb = writer.getDirectContent();
 		try {
 			BaseFont bf = BaseFont.createFont(PriebesIText5PdfView.FONT,
 					ENCODING, BaseFont.NOT_EMBEDDED);
 			cb.saveState();
 			cb.beginText();
 			cb.moveText(x, y);
 			cb.setFontAndSize(bf, PriebesIText5PdfView.FONT_SIZE);
 			cb.showText(text);
 			cb.endText();
 			cb.restoreState();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onCloseDocument(PdfWriter writer, Document document) {
 		if ((writer.getPageNumber() - 1) > 1)
 		ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
 						new PhraseBuilder(String.valueOf(writer.getPageNumber() - 1)).build(),
 						2, 2, 0);
 
 	}
 
 	@Override
 	public void onParagraph(PdfWriter writer, Document document,
 			float paragraphPosition) {
 
 	}
 
 	@Override
 	public void onParagraphEnd(PdfWriter writer, Document document,
 			float paragraphPosition) {
 
 	}
 
 	@Override
 	public void onChapter(PdfWriter writer, Document document,
 			float paragraphPosition, Paragraph title) {
 
 	}
 
 	@Override
 	public void onChapterEnd(PdfWriter writer, Document document,
 			float paragraphPosition) {
 
 	}
 
 	@Override
 	public void onSection(PdfWriter writer, Document document,
 			float paragraphPosition, int depth, Paragraph title) {
 
 	}
 
 	@Override
 	public void onSectionEnd(PdfWriter writer, Document document,
 			float paragraphPosition) {
 
 	}
 
 	@Override
 	public void onGenericTag(PdfWriter writer, Document document,
 			Rectangle rect, String text) {
 
 	}
 
 	public String getLogoPath() {
 		return logoPath;
 	}
 
 	public void setLogoPath(String logoPath) {
 		this.logoPath = logoPath;
 	}
 	
 	public void insertInfoTable(Document document,
 			CustomPdfPTableBuilder infoTableBuilder) throws DocumentException {
 		
 	}
 	
 }
