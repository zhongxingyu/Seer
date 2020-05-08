 package org.bh.plugin.pdfexport;
 
 import java.awt.Color;
 import java.awt.Transparency;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.IPeriodicalValuesDTO;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.IValue;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 import org.jfree.chart.JFreeChart;
 
 import com.itextpdf.text.BadElementException;
 import com.itextpdf.text.Chapter;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.Section;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPageEvent;
 import com.itextpdf.text.pdf.PdfWriter;
 
 /**
  * Builder class for pdf's
  * 
  * @author Norman
  * @version 1.0, 15.01.2010
  * 
  */
 public class ITextDocumentBuilder implements PdfPageEvent {
 
 	static Logger log = Logger.getLogger(ITextDocumentBuilder.class);
 
 	private static ITranslator trans = BHTranslator.getInstance();
 
 	private static final Font TITLE_FONT = FontFactory.getFont(
 			FontFactory.HELVETICA, 20, Font.BOLD);
 
 	private static final Font SECTION1_FONT = FontFactory.getFont(
 			FontFactory.HELVETICA, 16, Font.BOLD);
 
 	private static final Font SECTION2_FONT = FontFactory.getFont(
 			FontFactory.HELVETICA, 14, Font.BOLD);
 
 	SimpleDateFormat s;
 	Document doc;
 	Chapter report;
 	PdfWriter pdfWriter;
 
 	public enum Keys {
 		TITLE, CHARTS, RESULTS, CREATEDAT, DATEFORMAT, SCENARIODATA, RESULT_MAP, DISTRIBUTION_MAP, PERIODDATA;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	void newDocument(String path, DTOScenario scenario) {
 		try {
 
 			s = new SimpleDateFormat(trans.translate(Keys.DATEFORMAT));
 			doc = new Document(PageSize.A4, 50, 50, 50, 50);
 
 			pdfWriter = PdfWriter.getInstance(doc, new FileOutputStream(path));
 			pdfWriter.setPageEvent(this);
 
 			doc.addAuthor(trans.translate("title") + trans.translate("version")
 					+ trans.translate("version_long"));
 			doc.addSubject(trans.translate(Keys.TITLE));
 			doc.addCreationDate();
 			doc.addTitle(trans.translate(Keys.TITLE) + " - "
 					+ scenario.get(DTOScenario.Key.IDENTIFIER));
 			doc.open();
 
 		} catch (FileNotFoundException e) {
 			log.error(e);
 		} catch (DocumentException e) {
 			log.error(e);
 		}
 	}
 
 	void closeDocument() {
 		try {
 			doc.add(report);
 			doc.close();
 		} catch (DocumentException e) {
 			log.error(e);
 		}
 	}
 
 	void buildResultDataDet(Map<String, Calculable[]> resultMap,
 			List<JFreeChart> charts) {
 		Paragraph title;
 		Section results;
 		Section resultMapSection;
 		PdfPTable t;
 
 		results = buildResultHead();
 
 		if (resultMap != null && resultMap.size() > 0) {
 			title = new Paragraph(trans.translate(Keys.RESULT_MAP),
 					SECTION2_FONT);
 			resultMapSection = results.addSection(title, 2);
 			resultMapSection.add(new Paragraph("\n"));
 			t = new PdfPTable(2);
 			for (Entry<String, Calculable[]> e : resultMap.entrySet()) {
 				Calculable[] val = e.getValue();
 				if (val.length >= 1) {
 					t.addCell(trans.translate(e.getKey()));
 					if (val[0] != null) {
 						t.addCell(val[0].toString());
 					} else {
 						t.addCell(" ");
 					}
 				}
 				if (val.length > 1) {
 					for (int i = 1; i < val.length; i++) {
 						t.addCell(" ");
 						if (val[i] != null) {
 							t.addCell(val[i].toString());
 						} else {
 							t.addCell(" ");
 						}
 					}
 				}
 			}
 			resultMapSection.add(t);
 			resultMapSection.add(new Paragraph("\n\n"));
 
 			buildChartsSection(results, charts);
 		}
 	}
 
 	private void buildChartsSection(Section results, List<JFreeChart> charts) {
 		Paragraph title;
 		Image chartImage;
 		Section chartSection;
 		try {
 			results.newPage();
 			title = new Paragraph(trans.translate(Keys.CHARTS), SECTION2_FONT);
 			chartSection = results.addSection(title, 2);
 			chartSection.add(new Paragraph("\n"));
 
 			for (JFreeChart c : charts) {
 
 				chartImage = Image.getInstance(c.createBufferedImage(500, 350),
 						new Color(Transparency.OPAQUE));
 				chartSection.add(chartImage);
 			}
 
 		} catch (BadElementException e) {
 			log.error(e);
 		} catch (IOException e) {
 			log.error(e);
 		}
 	}
 
 	void buildResultDataStoch(DistributionMap distMap, List<JFreeChart> charts) {
 		Paragraph title;
 		Section results;
 		Section distMapSection;
 		PdfPTable t;
 
 		results = buildResultHead();
 
 		title = new Paragraph(trans.translate(Keys.DISTRIBUTION_MAP),
 				SECTION2_FONT);
 		distMapSection = results.addSection(title, 2);
		distMapSection.add(new Paragraph("\n"));
 		t = new PdfPTable(2);
 		for (Iterator<Entry<Double, Integer>> i = distMap.iterator(); i
 				.hasNext();) {
 			Entry<Double, Integer> val = i.next();
 			t.addCell(val.getKey().toString());
 			t.addCell(val.getValue().toString());
 		}
 		distMapSection.add(t);
 
 		buildChartsSection(results, charts);
 	}
 
 	Section buildResultHead() {
 		Paragraph title;
 		report.newPage();
 		title = new Paragraph(trans.translate(Keys.RESULTS), SECTION1_FONT);
 		return report.addSection(title, 1);
 	}
 
 	void buildHeadData(DTOScenario scenario) {
 		Section data;
 		PdfPTable t;
 
 		try {
 			Image img = Image.getInstance(getClass().getResource(
 					"/org/bh/images/background.jpg"));
 			img.scalePercent(70f);
 			img.setAlignment(Image.MIDDLE);
 			doc.add(img);
 			
 			Paragraph title = new Paragraph(trans.translate(Keys.TITLE) + " - "
 					+ scenario.get(DTOScenario.Key.NAME), TITLE_FONT);
 			doc.add(new Paragraph("\n\n"));
 			doc.add(title);
 			doc.add(new Paragraph(trans.translate(Keys.CREATEDAT) + ": "
 					+ s.format(new Date()) + "\n\n"));
 			
 			report = new Chapter(title, 1);
 			report.setNumberDepth(0);
 			title = new Paragraph(trans.translate(Keys.SCENARIODATA),
 					SECTION1_FONT);
 			data = report.addSection(title, 1);
 
 			data.add(new Paragraph("\n"));
 			t = new PdfPTable(2);
 			for (Iterator<Entry<String, IValue>> i = scenario.iterator(); i
 					.hasNext();) {
 				Map.Entry<String, IValue> val = i.next();
 				t.addCell(trans.translate(val.getKey()));
 				t.addCell(val.getValue().toString());
 			}
 			data.add(t);
 			data.add(new Paragraph("\n\n"));
 		} catch (BadElementException e) {
 			log.error(e);
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (IOException e) {
 			log.error(e);
 		} catch (DocumentException e) {
 			log.debug(e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	void buildScenarioData(DTOScenario scenario) {
 		Paragraph title;
 		Section input;
 		Section period;
 		PdfPTable t;
 
 		report.newPage();
 		title = new Paragraph(trans.translate(Keys.PERIODDATA), SECTION1_FONT);
 		input = report.addSection(title, 1);
 
 		for (DTOPeriod d : scenario.getChildren()) {
 			title = new Paragraph(d.get(DTOPeriod.Key.NAME).toString(),
 					SECTION2_FONT);
 			period = input.addSection(title, 2);
 			period.add(new Paragraph("\n"));
 			for (IPeriodicalValuesDTO pv : d.getChildren()) {
 				t = new PdfPTable(2);
 				for (Iterator<Entry<String, IValue>> i = pv.iterator(); i
 						.hasNext();) {
 					Map.Entry<String, IValue> val = i.next();
 					t.addCell(trans.translate(val.getKey()));
 					t.addCell(val.getValue().toString());
 				}
 				period.add(t);
 				period.add(new Paragraph("\n\n"));
 			}
 		}
 	}
 
 	@Override
 	public void onChapter(PdfWriter arg0, Document arg1, float arg2,
 			Paragraph arg3) {
 	}
 
 	@Override
 	public void onChapterEnd(PdfWriter arg0, Document arg1, float arg2) {
 	}
 
 	@Override
 	public void onCloseDocument(PdfWriter arg0, Document arg1) {
 	}
 
 	@Override
 	public void onEndPage(PdfWriter writer, Document document) {
 		// we will print the footer using the code below
 		try {
 			Rectangle page = document.getPageSize();
 			PdfPTable footTable = getFooterSignatures();
 			footTable.setTotalWidth(page.getWidth() - document.leftMargin()
 					- document.rightMargin());
 			footTable.writeSelectedRows(0, -1, document.leftMargin(), document
 					.bottomMargin() + 20, writer.getDirectContent());
 		} catch (DocumentException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private PdfPTable getFooterSignatures() throws DocumentException {
 		Font fontStyleFooters = FontFactory.getFont(FontFactory.HELVETICA, 9,
 				Font.BOLD);
 		// the following code will create a table with 2 columns
 		PdfPTable footTable = new PdfPTable(2);
 		// now we set the widths of each of the columns
 		footTable.setWidths(new int[] { 50, 50 });
 		// set the width of the table
 		footTable.setWidthPercentage(100);
 		// set the padding
 		footTable.getDefaultCell().setPadding(2);
 		// since we are using 0 border width, the border wont apppear on this
 		// particular table
 		footTable.getDefaultCell().setBorderWidth(0);
 		footTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
 		// possible text
 		footTable.addCell(new Phrase("", fontStyleFooters));
 		footTable.addCell("");
 
 		footTable.addCell("\n");
 		footTable.addCell("\n");
 
 		// possible text
 		footTable.addCell(new Phrase(" ", fontStyleFooters));
 
 		footTable.addCell(new Phrase("- " + pdfWriter.getPageNumber() + " -",
 				fontStyleFooters));
 
 		return footTable;
 	}
 
 	@Override
 	public void onGenericTag(PdfWriter arg0, Document arg1, Rectangle arg2,
 			String arg3) {
 	}
 
 	@Override
 	public void onOpenDocument(PdfWriter arg0, Document arg1) {
 	}
 
 	@Override
 	public void onParagraph(PdfWriter arg0, Document arg1, float arg2) {
 	}
 
 	@Override
 	public void onParagraphEnd(PdfWriter arg0, Document arg1, float arg2) {
 	}
 
 	@Override
 	public void onSection(PdfWriter arg0, Document arg1, float arg2, int arg3,
 			Paragraph arg4) {
 	}
 
 	@Override
 	public void onSectionEnd(PdfWriter arg0, Document arg1, float arg2) {
 	}
 
 	@Override
 	public void onStartPage(PdfWriter arg0, Document arg1) {
 	}
 
 }
