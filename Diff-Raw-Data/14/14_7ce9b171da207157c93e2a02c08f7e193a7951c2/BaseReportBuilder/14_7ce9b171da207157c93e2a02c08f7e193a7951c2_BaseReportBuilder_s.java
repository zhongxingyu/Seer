 package export;
 
 
 import com.google.common.collect.Lists;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfPageEventHelper;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 import models.JCra;
 import models.JUser;
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.DateTime;
 import utils.time.TimeUtils;
 
 import java.util.List;
 import java.util.Locale;
 
 /**
  * @author f.patin
  */
 
 public abstract class BaseReportBuilder extends PdfPageEventHelper {
	private final DateTime generationDate = DateTime.now();
 	protected BaseFont baseFont = new Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL).getCalculatedBaseFont(false);
 	protected JUser user;
 	protected JCra cra;
 	private PdfTemplate centerPages;
 	private PdfTemplate rightPages;
 	private PdfTemplate leftPages;
 	private float footerTextSize = 8f;
 
 	public BaseReportBuilder() {
 		super();
 	}
 
 	@Override
 	public void onOpenDocument(PdfWriter writer, Document document) {
 		centerPages = writer.getDirectContent().createTemplate(100, 100);
 		centerPages.setBoundingBox(new Rectangle(-20, -20, 100, 100));
 		rightPages = writer.getDirectContent().createTemplate(100, 100);
 		rightPages.setBoundingBox(new Rectangle(-20, -20, 100, 100));
 		leftPages = writer.getDirectContent().createTemplate(100, 100);
 		leftPages.setBoundingBox(new Rectangle(-20, -20, 100, 100));
 	}
 
 	@Override
 	public void onEndPage(PdfWriter writer, Document document) {
 		PdfContentByte cb = writer.getDirectContent();
 		cb.saveState();
 		float textBase = document.bottom() - 20;
 
 
 		// Page numbers
 		cb.beginText();
 		cb.setFontAndSize(baseFont, footerTextSize);
 		String totalText = String.format("Page %s / ", writer.getPageNumber());
 		float totalTextSize = baseFont.getWidthPoint(totalText, footerTextSize);
 
 		cb.setTextMatrix((document.right() / 2), textBase);
 		cb.showText(totalText);
 		cb.endText();
 		cb.addTemplate(centerPages, (document.right() / 2) + totalTextSize, textBase);
 
 
 		// User name
 		List<String> right = Lists.newArrayList();
 		if(user != null) {
 			right.add(user.fullName());
 		}
 		if(cra != null) {
 			right.add(String.format("CRA %s", StringUtils.capitalize(TimeUtils.firstDateOfMonth(cra.year, cra.month).toString("MMMM yyyy", Locale.FRANCE))));
 		}
 		if(!right.isEmpty()){
 			final String rightText = StringUtils.join(right, " - ");
 			if(StringUtils.isNotBlank(rightText)) {
 				cb.beginText();
 				float adjust = baseFont.getWidthPoint("0", footerTextSize);
 
 				float userTextSize = baseFont.getWidthPoint(rightText, footerTextSize);
 				cb.setTextMatrix(document.right() - userTextSize - adjust, textBase);
 				cb.showText(rightText);
 				cb.endText();
 				cb.addTemplate(rightPages, document.right() - adjust, textBase);
 			}
 		}
 
 
 
		final String leftText = "Généré le " + generationDate.toString("dd/MM/yyyy à HH:mm:ss", Locale.FRANCE);
 			cb.beginText();
 			cb.setTextMatrix(document.left(), textBase);
 
 			float yearMonthTextSize = baseFont.getWidthPoint(leftText, footerTextSize);
 			cb.showText(leftText);
 			cb.endText();
 			cb.addTemplate(leftPages, document.left() + yearMonthTextSize, textBase);
 		cb.restoreState();
 	}
 
 	@Override
 	public void onCloseDocument(PdfWriter writer, Document document) {
 		centerPages.beginText();
 		centerPages.setFontAndSize(baseFont, footerTextSize);
 		centerPages.setTextMatrix(0, 0);
 		centerPages.showText(String.valueOf(writer.getPageNumber() - 1));
 		centerPages.endText();
 	}
 }
