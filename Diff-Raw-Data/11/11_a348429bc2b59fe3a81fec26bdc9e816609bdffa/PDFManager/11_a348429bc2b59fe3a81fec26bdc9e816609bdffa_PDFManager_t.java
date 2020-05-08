 package at.epu.BusinessLayer;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.pdfbox.exceptions.COSVisitorException;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
 import org.apache.pdfbox.pdmodel.font.PDFont;
 import org.apache.pdfbox.pdmodel.font.PDType1Font;
 
 import com.ibm.icu.util.Calendar;
 
 import at.epu.PresentationLayer.DataModels.BackofficeTableModel;
 
 public class PDFManager {
 	
 	private String filename;
 	private SimpleDateFormat sdf;
 	
 	public void createAnnualPrognosis(BackofficeTableModel model, String path) {
 		sdf = new SimpleDateFormat("ddMMyyyy");
 		filename = path;
 		String year = new SimpleDateFormat("yyyy").format(new Date()).toString();
 		PDDocument doc = newDocument();
 		PDPage page = newPage();
 
 		try {
 			if(model.getDataObjectCollection().size() != 0) {
 				doc.addPage(page);
 		        PDFont font = PDType1Font.HELVETICA_BOLD;
 		
 		        PDPageContentStream content = new PDPageContentStream(doc, page);
 				
 		        content.beginText();
 		        content.setFont( font, 18 );
 		        content.moveTextPositionByAmount( 100, 700 );
 		        content.drawString("Jahresprognose der Angebote im Jahr " + year);
 		        content.moveTextPositionByAmount(10, -50);
 		        content.setFont(font, 16);
 		        content.drawString("Angebote");
 		        
 		        content.moveTextPositionByAmount(-10, -50);
 		        content.setFont(font, 12);
 		      
 		        content.drawString(model.getObjectAtRow(0).getFieldNames().get(1).toString().toUpperCase());
 		        content.moveTextPositionByAmount(100, 0);
 		        content.drawString(model.getObjectAtRow(0).getFieldNames().get(3).toString().toUpperCase());
 		        content.moveTextPositionByAmount(100, 0);
 		        content.drawString(model.getObjectAtRow(0).getFieldNames().get(4).toString().toUpperCase());
 		        content.moveTextPositionByAmount(100, 0);
 		        content.drawString(model.getObjectAtRow(0).getFieldNames().get(5).toString().toUpperCase());
 		        content.moveTextPositionByAmount(100, 0);
 		        content.drawString(model.getObjectAtRow(0).getFieldNames().get(6).toString().toUpperCase());
 		        content.moveTextPositionByAmount(-400, -20);
 		        
 		        font = PDType1Font.HELVETICA;
 		        content.setFont(font, 12);
 		        
 		        for(int x=0;x<model.getRowCount();x++) {
 		        	if(model.getObjectAtRow(x).getFieldValues().get(5).toString().contains(year)) {
 			        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(1).toString());
 			        	content.moveTextPositionByAmount(100, 0);
 			        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(3).toString());
 			        	content.moveTextPositionByAmount(100, 0);
 			        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(4).toString());
 			        	content.moveTextPositionByAmount(100, 0);
 			        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(5).toString());
 			        	content.moveTextPositionByAmount(100, 0);
 			        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(6).toString());
 			        	content.moveTextPositionByAmount(-400, -20);
 			        	
 		        	}
 		        }
 		        
 		        double summe = 0.0;
 		        double prognose = 0.0;
 		        
 		        for(int x=0;x<model.getRowCount();x++) {
 		        	summe += (Double) model.getObjectAtRow(x).getFieldValues().get(3);
 		        	prognose += (Double) model.getObjectAtRow(x).getFieldValues().get(3) * (Double) model.getObjectAtRow(x).getFieldValues().get(6);
 		        }
 		        
 		        font = PDType1Font.HELVETICA_BOLD;
 		        content.setFont(font, 14);
 		        content.moveTextPositionByAmount(0, -40);
 		        content.drawString("Summe: " + summe + " Euro");
 	  
 		        content.moveTextPositionByAmount(0, -40);
 		        content.drawString("Prognose: " + prognose + " Euro");
 		        
 		        int offeneProjekte = 0;
 		        
 		        for(int i=0;i<model.getRowCount();i++) {
 		        	String startdate = model.getObjectAtRow(i).getFieldValues().get(5).toString();
 		        	String enddate = null;
 		        	int numDays = (Integer) model.getObjectAtRow(i).getFieldValues().get(4);
 		        	sdf = new SimpleDateFormat("yyyyMMdd");
 		        	Calendar calendar = Calendar.getInstance();
 		        	try {
 						calendar.setTime(sdf.parse(startdate));
 						calendar.add(Calendar.DATE, numDays);
 						enddate = sdf.format(calendar.getTime());
 					} catch (ParseException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 		        	int end, now;
 		        	end = Integer.parseInt(enddate);
 		        	now = Integer.parseInt(sdf.format(new Date()).toString());
 		        	
 		        	if(end < now) {
 		        		offeneProjekte++;
 		        	}
 		        	
 		        }
 		        
 		        font = PDType1Font.HELVETICA_BOLD;
 		        content.setFont(font, 12);
 		        content.moveTextPositionByAmount(250, -20);
 		        content.drawString("Offene Projekte laut Angebote = " + offeneProjekte);
 		        
 		        content.endText();
 		        content.close();
 			}
 			
 			saveDocument(doc, filename);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void createBillReportPDF(BackofficeTableModel model_ausgRechnungen, String path) {
 		
 		ApplicationManager appManager = ApplicationManager.getInstance();
 		BackofficeTableModel model_eingRechnungen = appManager.getModelForTableName("Eingangsrechnungen");
 		BackofficeTableModel model_kunden = appManager.getModelForTableName("Kunden");
 		BackofficeTableModel model_kontakte = appManager.getModelForTableName("Kontakte");
 		BackofficeTableModel model_buchungszeilen = appManager.getModelForTableName("Buchungszeilen");

 		sdf = new SimpleDateFormat("ddMMyyyy");
 		filename = path;
 		String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date()).toString();
 		PDDocument doc = newDocument();
 		PDPage page = newPage();
 		
 		try {
 			if(model_ausgRechnungen.getDataObjectCollection().size() != 0) {
 				doc.addPage(page);
 				PDFont font = PDType1Font.HELVETICA_BOLD;
 					
 			    PDPageContentStream content = new PDPageContentStream(doc, page);
 			    
 				content.beginText();
 		        content.setFont( font, 18 );
 		        content.moveTextPositionByAmount( 100, 700 );
 		        content.drawString("Rechnungsreport " + date);
 		        content.moveTextPositionByAmount(10, -50);
 		        content.setFont(font, 16);
 		        content.drawString("Eingangsrechnungen");
 		        
 		        content.moveTextPositionByAmount(-10, -50);
 		        content.setFont(font, 10);
 		        
 		        content.drawString(model_eingRechnungen.getObjectAtRow(0).getFieldNames().get(1).toString().toUpperCase());
 		        content.moveTextPositionByAmount(120, 0);
 		        content.drawString("KONTAKT");
 		        content.moveTextPositionByAmount(60, 0);
 		        content.drawString(model_eingRechnungen.getObjectAtRow(0).getFieldNames().get(4).toString().toUpperCase());
 		        content.moveTextPositionByAmount(-180, -20);
 		        
 		        font = PDType1Font.HELVETICA;
 		        
 		        for(int x=0;x<model_eingRechnungen.getRowCount();x++) {
 			       	content.setFont(font, 10);
 			       	content.drawString(model_eingRechnungen.getObjectAtRow(x).getFieldValues().get(1).toString());
 			       	content.moveTextPositionByAmount(120, 0);
 			       	//resolve Foreign Key
 		        	for(int i=0;i<model_kontakte.getRowCount();i++) {
 		        		if(model_kontakte.getObjectAtRow(i).getFieldValues().get(0).toString().equals(model_eingRechnungen.getObjectAtRow(x).getFieldValues().get(2).toString())) {
 		    		        content.drawString(model_kontakte.getObjectAtRow(i).getFieldValues().get(1).toString()); 
 		        		}
 		        	}  	
 			      	content.moveTextPositionByAmount(60, 0);
 			       	content.drawString(model_eingRechnungen.getObjectAtRow(x).getFieldValues().get(4).toString());
 			       	content.moveTextPositionByAmount(-180, -15);
 			       	content.moveTextPositionByAmount(10, 0);
 			       	font = PDType1Font.HELVETICA_BOLD;
 			       	content.setFont(font, 8);
 			       	content.drawString("Buchungszeilen");	
 			       	font = PDType1Font.HELVETICA;
 			       	content.setFont(font, 8);
 			       	content.moveTextPositionByAmount(0, -10);
 			       	content.drawString("BETRAG");
 			       	content.moveTextPositionByAmount(80, 0);
 			       	content.drawString("UMSATZSTEUER");
 			       	content.moveTextPositionByAmount(100, 0);
 			       	content.drawString("BUCHUNGSDATUM");
 			       	content.moveTextPositionByAmount(-180, -10);
 			       	for(int f=0;f<model_buchungszeilen.getRowCount();f++) {
 			       		if(model_buchungszeilen.getObjectAtRow(f).getFieldValues().get(1).toString().equals(model_eingRechnungen.getObjectAtRow(x).getFieldValues().get(0).toString())) {
 			       			content.drawString(model_buchungszeilen.getObjectAtRow(f).getFieldValues().get(3).toString());
 			       			content.moveTextPositionByAmount(80, 0);
 			       			content.drawString(model_buchungszeilen.getObjectAtRow(f).getFieldValues().get(4).toString());
 			       			content.moveTextPositionByAmount(100, 0);
 			       			content.drawString(model_buchungszeilen.getObjectAtRow(f).getFieldValues().get(5).toString());
 			       			content.moveTextPositionByAmount(-180, -20);
 			       		}
 			       	}
 			       	content.moveTextPositionByAmount(-10, 0);  	
 		        }
 				
 				content.moveTextPositionByAmount(10, -50);
 				font = PDType1Font.HELVETICA_BOLD;
 				content.setFont(font, 16);
 				content.drawString("Ausgangsrechnungen");
 		        content.setFont(font, 10);
 		        content.moveTextPositionByAmount(-10, -50);
 		        
 		        content.drawString(model_ausgRechnungen.getObjectAtRow(0).getFieldNames().get(1).toString().toUpperCase());
 		        content.moveTextPositionByAmount(120, 0);
 		        content.drawString("KUNDE");
 		        content.moveTextPositionByAmount(60, 0);
 		        content.drawString(model_ausgRechnungen.getObjectAtRow(0).getFieldNames().get(5).toString().toUpperCase());
 		        content.moveTextPositionByAmount(-180, -20);
 		        
 		        font = PDType1Font.HELVETICA;
 		        content.setFont(font, 10);
 		        
 		        for(int x=0;x<model_ausgRechnungen.getRowCount();x++) {
 			        	content.drawString(model_ausgRechnungen.getObjectAtRow(x).getFieldValues().get(1).toString());
 			        	content.moveTextPositionByAmount(120, 0);
 			        	//resolve Foreign Key
 			        	for(int i=0;i<model_kunden.getRowCount();i++) {
 			        		if(model_kunden.getObjectAtRow(i).getFieldValues().get(0).toString().equals(model_ausgRechnungen.getObjectAtRow(x).getFieldValues().get(2).toString())) {
 			    		        content.drawString(model_kontakte.getObjectAtRow(i).getFieldValues().get(1).toString()); 
 			        		}
 			        	}  
 			        	content.moveTextPositionByAmount(60, 0);
 			        	content.drawString(model_ausgRechnungen.getObjectAtRow(x).getFieldValues().get(5).toString());
 			        	content.moveTextPositionByAmount(-180, -20);
 		        }
 				int offeneRechnungen = 0;
 				for(int i=0;i<model_eingRechnungen.getRowCount();i++) {
 					if(model_eingRechnungen.getObjectAtRow(i).getFieldValues().get(4).toString().equals("offen")) {
 						offeneRechnungen++;
 					}
 				}
 				for(int i=0;i<model_ausgRechnungen.getRowCount();i++) {
 					if(model_ausgRechnungen.getObjectAtRow(i).getFieldValues().get(5).toString().equals("offen")) {
 						offeneRechnungen++;
 					}
 				}
 				
 				font = PDType1Font.HELVETICA_BOLD;
 		        content.setFont(font, 12);
 		        content.moveTextPositionByAmount(300, -20);
 		        content.drawString("Offene Rechnungen = " + offeneRechnungen);
 		        content.endText();
 		        content.close();
 			}
			
 			saveDocument(doc, filename);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void createInOutBillReportPDF(BackofficeTableModel model, String path) {
 		if(model.getRowCount() != 0) {
 		sdf = new SimpleDateFormat("ddMMyyyy");
 		filename = path;
 		String year = new SimpleDateFormat("yyyy").format(new Date()).toString();
 		String month = new SimpleDateFormat("MMMM").format(new Date()).toString();
 		String mm = new SimpleDateFormat("MM").format(new Date()).toString();
 		
 		ApplicationManager appManager = ApplicationManager.getInstance();
 		
 		BackofficeTableModel modelFK1 = appManager.getModelForTableName("Ausgangsrechnungen");
 		BackofficeTableModel modelFK2 = appManager.getModelForTableName("Eingangsrechnungen");
 		
 		PDDocument doc = newDocument();
 		PDPage page = newPage();
 		
 		try {
 			doc.addPage(page);
 			PDFont font = PDType1Font.HELVETICA_BOLD;
 				
 		    PDPageContentStream content = new PDPageContentStream(doc, page);
 		    
 			content.beginText();
 	        content.setFont( font, 18 );
 	        content.moveTextPositionByAmount( 100, 700 );
 	        content.drawString("Einnahmen / Ausgaben Report " + month + " " + year);
 	        content.moveTextPositionByAmount(10, -50);
 	        content.setFont(font, 16);
 	        content.drawString("Einnahmen");
 	        
 	        content.moveTextPositionByAmount(-10, -50);
 	        content.setFont(font, 10);
 	        
 	        content.drawString("AUSGANGSRECHNUNG");
 	        content.moveTextPositionByAmount(150, 0);
 	        content.drawString(model.getObjectAtRow(0).getFieldNames().get(3).toString().toUpperCase());
 	        content.moveTextPositionByAmount(60, 0);
 	        content.drawString(model.getObjectAtRow(0).getFieldNames().get(5).toString().toUpperCase());
 	        content.moveTextPositionByAmount(-210, -20);
 	        
 	        font = PDType1Font.HELVETICA;
 	        content.setFont(font, 10);
 	        
 	        for(int x=0;x<model.getRowCount();x++) {
 	        	if(Integer.parseInt(model.getObjectAtRow(x).getFieldValues().get(2).toString()) > 0 && 
 	        			model.getObjectAtRow(x).getFieldValues().get(5).toString().contains(year +"-"+ mm)) {
 	        		//resolve Foreign Key
 		        	for(int i=0;i<modelFK1.getRowCount();i++) {
 		        		if(modelFK1.getObjectAtRow(i).getFieldValues().get(0).toString().equals(model.getObjectAtRow(x).getFieldValues().get(2).toString())) {
 		    		        content.drawString(modelFK1.getObjectAtRow(i).getFieldValues().get(1).toString()); 
 		        		}
 		        	}  	
 		        	content.moveTextPositionByAmount(150, 0);
 		        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(3).toString());
 		        	content.moveTextPositionByAmount(60, 0);
 		        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(5).toString());
 		        	content.moveTextPositionByAmount(-210, -20);
 	        	}
 	        }
 			
 			content.moveTextPositionByAmount(10, -50);
 			font = PDType1Font.HELVETICA_BOLD;
 			content.setFont(font, 16);
 			content.drawString("Ausgaben");
 	        content.setFont(font, 10);
 	        content.moveTextPositionByAmount(-10, -50);
 	        
 	        content.drawString("EINGANGSRECHNUNG");
 	        content.moveTextPositionByAmount(150, 0);
 	        content.drawString(model.getObjectAtRow(0).getFieldNames().get(3).toString().toUpperCase());
 	        content.moveTextPositionByAmount(60, 0);
 	        content.drawString(model.getObjectAtRow(0).getFieldNames().get(5).toString().toUpperCase());
 	        content.moveTextPositionByAmount(-210, -20);
 	        
 	        font = PDType1Font.HELVETICA;
 	        content.setFont(font, 10);
 	        
 	        for(int x=0;x<model.getRowCount();x++) {
 	        	if(Integer.parseInt(model.getObjectAtRow(x).getFieldValues().get(1).toString()) > 0 && 
 	        			model.getObjectAtRow(x).getFieldValues().get(5).toString().contains(year +"-"+ mm)) {
 	        		//resolve Foreign Key
 		        	for(int i=0;i<modelFK2.getRowCount();i++) {
 		        		if(modelFK2.getObjectAtRow(i).getFieldValues().get(0).toString().equals(model.getObjectAtRow(x).getFieldValues().get(1).toString())) {
 		    		        content.drawString(modelFK2.getObjectAtRow(i).getFieldValues().get(1).toString()); 
 		        		}
 		        	}
 		        	content.moveTextPositionByAmount(150, 0);
 		        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(3).toString());
 		        	content.moveTextPositionByAmount(60, 0);
 		        	content.drawString(model.getObjectAtRow(x).getFieldValues().get(5).toString());
 		        	content.moveTextPositionByAmount(-210, -20);
 	        	}
 	        }
 			
 	        content.endText();
 	        content.close();
 	        
 			saveDocument(doc, filename);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		}
 	}
 	
 	private PDDocument newDocument() {
 		PDDocument doc = null;
 		
 		try {
 			doc = new PDDocument();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return doc;
 	}
 	
 	private PDPage newPage() {
 		PDPage page = null;
 		
 		page = new PDPage();
 		
 		return page;
 	}
 	
 	private void saveDocument(PDDocument document, String filename) {
 		try {
 			document.save(filename);
 			document.close();
 		} catch (COSVisitorException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
