 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber * : Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
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
 		TITLE, CHARTS, RESULTS, CREATEDAT, DATEFORMAT, SCENARIODATA, RESULT_MAP, DISTRIBUTION_MAP, PERIODDATA, SHAREHOLDER_VALUE, DISTRIBUTION_CHART, MONEY_UNIT, ALL, TOTALCOST, DIRECT_INPUT, COSTOFSALES, WIENERPROCESS, RANDOMWALK;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	/**
 	 * Creates a new document for the given scenario
 	 * 
 	 * @param path
 	 * @param scenario
 	 */
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
 
 	/**
 	 * finishes the pdffile creation
 	 */
 	void closeDocument() {
 		try {
 			doc.add(report);
 			doc.close();
 		} catch (DocumentException e) {
 			log.error(e);
 		}
 	}
 
 	/**
 	 * builds the result data part of the pdf for deterministical dcf scenarios
 	 * 
 	 * @param resultMap
 	 * @param charts
 	 */
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
 			int j = 0;
 			for (Entry<String, Calculable[]> e : resultMap.entrySet()) {
 				Calculable[] val = e.getValue();
 				if (val.length >= 1) {
 					t.addCell(trans.translate(e.getKey()));
 					if (val[0] != null) {
						if(j == 0 || j == 1 || j == 2 || j == 8 || j == 10) {
 							Float value = (Float.parseFloat(val[0].toString().replace(',','.')))*100;
 							t.addCell(value + " %");	
 						}
 						else
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
 				j++;
 			}
 			resultMapSection.add(t);
 			resultMapSection.add(new Paragraph("\n\n"));
 
 			buildChartsSection(results, charts);
 		}
 	}
 
 	/**
 	 * builds the chart section of the pdf
 	 * 
 	 * @param results
 	 * @param charts
 	 */
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
 
 	/**
 	 * builds the result data part of the pdf for stochastic scenarios
 	 * 
 	 * @param distMap
 	 * @param charts
 	 */
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
 		t.addCell(trans.translate(Keys.SHAREHOLDER_VALUE)+" (in "+ trans.translate(Keys.MONEY_UNIT)+")");
 		t.addCell(trans.translate(Keys.DISTRIBUTION_CHART));
 		for (Iterator<Entry<Double, Integer>> i = distMap.iterator(); i
 				.hasNext();) {
 			Entry<Double, Integer> val = i.next();
 			Float value = Float.parseFloat(val.getValue().toString());
 			t.addCell(""+value.intValue());
 			t.addCell(val.getKey().toString());
 		}
 		distMapSection.add(t);
 
 		buildChartsSection(results, charts);
 	}
 
 	/**
 	 * builds the scenario input section
 	 * 
 	 * @return
 	 */
 	Section buildResultHead() {
 		Paragraph title;
 		report.newPage();
 		title = new Paragraph(trans.translate(Keys.RESULTS), SECTION1_FONT);
 		return report.addSection(title, 1);
 	}
 
 	/**
 	 * builds the scenario input section
 	 * 
 	 * @return
 	 */
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
 
 			
 			//Scenario input data Export
 			data.add(new Paragraph("\n"));
 			t = new PdfPTable(2);
 			int j = 0;
 			for (Iterator<Entry<String, IValue>> i = scenario.iterator(); i
 					.hasNext(); j++) {
 				Map.Entry<String, IValue> val = i.next();
 				
 				//Adjustments for DCF_METHOD
 				if(val.getKey().equals("org.bh.data.DTOScenario$Key.DCF_METHOD")) {
 					t.addCell(trans.translate(val.getKey()));
 					if(val.getValue().toString().equals("all")) {
 						t.addCell(trans.translate(Keys.ALL));		
 					}
 					else {
 						t.addCell(val.getValue().toString().toUpperCase());
 					}
 				}
 				//Adjustment for percentage values RFK CTAX BTAX REK
 				else if(val.getKey().equals("org.bh.data.DTOScenario$Key.RFK") || val.getKey().equals("org.bh.data.DTOScenario$Key.CTAX") || val.getKey().equals("org.bh.data.DTOScenario$Key.BTAX") || val.getKey().equals("org.bh.data.DTOScenario$Key.REK")) {
 					t.addCell(trans.translate(val.getKey()));
 					Float value = (Float.parseFloat(val.getValue().toString().replace(',','.')))*100;
 					//int value2 = value.intValue();
 					t.addCell(value + " %");	
 				}
 				//Adjustment for PERIOD_TYPE
 				else if (val.getKey().equals("org.bh.data.DTOScenario$Key.PERIOD_TYPE")) {
 					t.addCell(trans.translate(val.getKey()));
 					if(val.getValue().toString().equals("Direct_Input")) {
 						t.addCell(trans.translate(Keys.DIRECT_INPUT));		
 					}
 					else if (val.getValue().toString().equals("gcc_input_costofsales"))  {
 						t.addCell(trans.translate(Keys.COSTOFSALES));
 					}
 					else if (val.getValue().toString().equals("gcc_input_totalcost"))  {
 						t.addCell(trans.translate(Keys.TOTALCOST));
 					}
 				}
 				//Adjustment for STOCHASTIC_KEYS
 				else if (val.getKey().equals("org.bh.data.DTOScenario$Key.STOCHASTIC_KEYS")) {
 					t.addCell(trans.translate(val.getKey()));
 					String stochasticKeys = val.getValue().toString();
 					stochasticKeys = stochasticKeys.substring(1, stochasticKeys.length()-1);
 					t.addCell(stochasticKeys);										
 				}
 				//Adjustment for STOCHASTIC_PROCESS
 				else if (val.getKey().equals("org.bh.data.DTOScenario$Key.STOCHASTIC_PROCESS")) {
 					t.addCell(trans.translate(val.getKey()));
 					if(val.getValue().toString().equals("wienerProcess")) {
 						t.addCell(trans.translate(Keys.WIENERPROCESS));		
 					}
 					else if (val.getValue().toString().equals("randomWalk"))  {
 						t.addCell(trans.translate(Keys.RANDOMWALK));
 					}
 				}
 				else {
 					t.addCell(trans.translate(val.getKey()));
 					t.addCell(val.getValue().toString());
 				}
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
 
 	/**
 	 * builds the pdf section with scenario information 
 	 * 
 	 * @param scenario
 	 */
 	@SuppressWarnings("unchecked")
 	void buildScenarioData(DTOScenario scenario) {
 		Paragraph title;
 		Section input;
 		Section period;
 		PdfPTable t;
 
 		report.newPage();
 		title = new Paragraph(trans.translate(Keys.PERIODDATA), SECTION1_FONT);
 		input = report.addSection(title, 1);
 
 		
 		//Period Data Export
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
 					t.addCell(val.getValue().toString() + " " + trans.translate(Keys.MONEY_UNIT));
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
