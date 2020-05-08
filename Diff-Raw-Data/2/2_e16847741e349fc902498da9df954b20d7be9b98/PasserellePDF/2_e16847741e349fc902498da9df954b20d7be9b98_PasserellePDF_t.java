 package pack;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import ui.Common;
 
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.List;
 import com.itextpdf.text.ListItem;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 
 /**
  * classe technique qui gre la gnration des document en format PDF
  * @author BERON Jean-Sebastien
  *
  */
 public class PasserellePDF {
 	
 	private static String expDir = Config.get("pdf.expDir");
 	private static Font font9   = new Font(Font.HELVETICA, 9);
 	private static Font font    = new Font(Font.HELVETICA, 10);
 	private static Font fontB09 = new Font(Font.HELVETICA,  9, Font.BOLD);
 	private static Font fontB10 = new Font(Font.HELVETICA, 10, Font.BOLD);
 	private static Font fontB12 = new Font(Font.HELVETICA, 12, Font.BOLD);
 	private static Font fontB14 = new Font(Font.HELVETICA, 14, Font.BOLD);
 	private static Font fontB15 = new Font(Font.HELVETICA, 15, Font.BOLD);
 	private static Font fontB22 = new Font(Font.HELVETICA, 22, Font.BOLD);
 	
 	/**
 	 * creer le pdf pour la liste des stagiaires pour le stage pass en parametre
 	 * @param leStage
 	 */
 	public static void creationListeStagiaire(Stage leStage){
 		
 		//creation du dossier
 		String cfgP="pdf.stagiaires.";
 		String date = leStage.getDateStr();
 		date = date.replace("/", ".");
 		String pathDossier = expDir+Config.get(cfgP+"dir")+date;
 		new File(pathDossier).mkdir();
 
 		Common.setStatus("Cration Liste Stagiaires "+leStage.getCodeI());
 		
 		try {
 			//creation du document et du fichier
 			Document doc = new Document(PageSize.A4,20,20,10,0);
 			FileOutputStream fichier = new FileOutputStream(pathDossier+"/"
 					+ Config.get(cfgP+"file")+" - "+leStage.getCode()+".pdf");
 			//ouverture du writer
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			//construction du header
 			PdfPTable header = new PdfPTable(1);
 			header.setWidthPercentage(100);
 			
 			//la date
 			PdfPCell cell = new PdfPCell(new Phrase(leStage.getDateStr(),fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			//le code du stage
 			Phrase par = new Phrase(leStage.getCode(),new Font(Font.HELVETICA, 28, Font.BOLD));
 			cell = new PdfPCell(par);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPadding(10);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			//le libelle du stage
 			par = new Phrase(leStage.getLibelle(),fontB14);
 			cell = new PdfPCell(par);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPadding(10);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			//construction du center
 			PdfPTable center = new PdfPTable(8);
 			center.setWidthPercentage(100);
 			
 			int i;
 			for (i = 0; i < leStage.getSizeStagiaireList(); i++) {
 				for (int j = 0; j < 5; j++) {
 					String info = leStage.getEltStagiaireList(i).getInfo(j);
 					Phrase phrs = new Phrase(info,new Font(Font.HELVETICA, 11));
 					cell = new PdfPCell(phrs);
 					cell.setPadding(5);
 					if(j != 3 && j != 2){
 						cell.setColspan(2);
 					}//finpour
 					cell.setBorder(0);
 					center.addCell(cell);
 				}//finpour
 			}//finpour
 			
 			if(i<24){
 				for (int j = i; j <= 24; j++) {
 					for (int k = 0; k < 5; k++) {
 						Phrase phrs = new Phrase(" ");
 						cell = new PdfPCell(phrs);
 						cell.setBorder(0);
 						if(j != 3 && j != 2){
 							cell.setColspan(2);
 						}//finpour
 						cell.setPadding(5);
 						center.addCell(cell);
 					}//finpour
 				}//finpour
 			}//finsi
 			
 			//construction du footer
 			PdfPTable footer = new PdfPTable(3);
 			footer.setWidthPercentage(95);
 			cell = new PdfPCell();
 			cell.setBorder(0);
 			footer.addCell(cell);
 			cell = new PdfPCell(new Phrase("Formateur : "+leStage.getLeader(),fontB12));
 			cell.setColspan(2);
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			footer.addCell(cell);
 			cell = new PdfPCell(new Phrase(leStage.getFirstModule().getSalle(),fontB22));
 			cell.setBorder(0);
 			footer.addCell(cell);
 			cell = new PdfPCell(new Phrase(leStage.getFirstModule().getLibelle(),new Font(Font.HELVETICA, 18, Font.BOLD)));
 			cell.setBorder(0);
 			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			footer.addCell(cell);
 			cell = new PdfPCell(new Phrase(leStage.getFirstModule().getHeureDebut(),fontB22));
 			cell.setBorder(0);
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			footer.addCell(cell);
 			
 			//ajout des composants
 			doc.add(header);
 			doc.add(center);
 			doc.add(footer);
 			
 			//fermeture du writer
 			doc.close();
 			
 		} catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "<html>erreur" +
 					"<br/>de fichier</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 		} catch (DocumentException e) {
 			JOptionPane.showMessageDialog(null, "<html>erreur" +
 					"<br/>d'ecriture</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
 		}
 		
 	}//fin 
 	
 	/**
 	 * creer le pdf pour la liste d'emargement pour le stage pass en parametre
 	 * @param leStage
 	 */
 	public static void creationListeEmargement(Stage leStage){
 		
 		if (Config.getB("pdf.dif")) {
 			if (leStage.getCode().matches(Config.get("pdf.dif.pattern"))) {
 				creationListeEmargementDIF(leStage);
 				return;
 			}
 		}
 		String cfgP="pdf.emargement.";
 		Integer colNum = Config.getI(cfgP+"colnum");
 		String date = leStage.getDateStr();
 		date = date.replace("/", ".");
 		String pathDossier = expDir+Config.get(cfgP+"dir")+date;
 		new File(pathDossier).mkdir();
 		
 		Common.setStatus("Cration Liste Emargement "+leStage.getCodeI());
 
 		try {
 			//creation du document
 			Document doc = new Document(PageSize.A4,10,10,10,0);
 			FileOutputStream fichier = new FileOutputStream(pathDossier+"/"
 					+Config.get(cfgP+"file")+" - "+leStage.getCode()+".pdf");
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			PdfPTable header = new PdfPTable(1);
 			header.setWidthPercentage(100);
 			
 			PdfPCell cell = new PdfPCell(new Phrase(leStage.getDateStr(),fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			header.addCell(cell);
 
 			Phrase par = new Phrase(leStage.getCode(),fontB22);
 			cell = new PdfPCell(par);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(Config.get(cfgP+"s1"), fontB10));
 			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 			cell.setPadding(0);
 			cell.setPaddingTop(5);
 			cell.setBorder(0);
 
 			//formation du center
 			PdfPTable center = new PdfPTable(colNum);
 			//center.setWidthPercentage(95);
 
 			float[] widths = new float[] { 1f, 8f, 1.5f, 4f, 1.5f };
 			for (int i=1; i<=colNum; i++) { widths[i-1]=Config.getF(cfgP+"col"+i+".w"); }
 			center.setWidths(widths);
 			
 			cell = new PdfPCell(new Phrase("N",fontB10)); cell.setBorder(0);
 
 			for (int i=1; i<=5; i++) {
 				cell.setPhrase(new Phrase(Config.get(cfgP+"col"+i+".str"),fontB10));
 				center.addCell(cell);
 			}
 
 			int i;
 			// TODO SG font constante
 			for (i = 0; i < leStage.getSizeStagiaireList(); i++) {
 					cell.setPhrase(new Phrase(""+(i+1), font));
 					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 					cell.setBorder(7);
 					center.addCell(cell);
 					// String str = "";
 					// TODO  SG liste statique
 					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 					PdfPTable stgCell = new PdfPTable(10);
 					center.setWidthPercentage(100);
 
 					//str = String.format("%-20s%-20s\n%25s%10s%10s", 
 					cell.setBorder(0);
 					cell.setColspan(5);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(0), fontB10));
 					stgCell.addCell(cell);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(1), font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(1);
 					cell.setPhrase(new Phrase(" ", font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(2);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(3), font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(5);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(4), font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(2);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(2), font));
 					stgCell.addCell(cell);
 					
 					cell.setColspan(1);
 					center.addCell(stgCell);
 					//presence
 					center.addCell(" ");
 					//emargement
 					center.addCell(" ");
 					//isvsmp
 					center.addCell(" ");
 			}
 			if(i<=20){
 				cell.setBorder(0);
 				cell.setPhrase(new Phrase(" ", font));
 				cell.setColspan(5);
 				for (int j = i; j < 20; j++) {
 						center.addCell(cell);
 				}//fin pour
 			}//fin si
 			
 			PdfPTable footer = new PdfPTable(3);
 			footer.setWidthPercentage(75);
 			cell = new PdfPCell(new Phrase("Nombre de stagiaires :",fontB14));
 			cell.setColspan(3);
 			cell.setBorder(0);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("Prvus",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(0);
 			cell.setBorderWidthBottom(1);
 			cell.setBorderWidthRight(1);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("Prsents",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthBottom(1);
 			cell.setBorderWidthRight(1);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("Maxi Prsents",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(0);
 			cell.setBorderWidthBottom(1);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(""+leStage.getSizeStagiaireList(),fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(0);
 			cell.setBorderWidthRight(1);
 			cell.setBorderWidthBottom(0);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(" ",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(1);
 			cell.setBorderWidthBottom(0);;
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(""+leStage.getMaxiPresent(),fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(0);
 			cell.setBorderWidthBottom(0);
 			footer.addCell(cell);
 			
 			//formateur
 			PdfPTable footer2 = new PdfPTable(1);
 			footer2.setWidthPercentage(80);
 			par = new Phrase("Formateurs : _ _ _ _ _ _ _ _ _ _ _ _ _ _ _/ "+leStage.getLeader(),fontB14);
 			cell = new PdfPCell(par);
 			cell.setBorder(0);
 			footer2.addCell(cell);
 			
 			//ajout des composants
 			doc.add(header);
 			doc.add(new Paragraph("        "));
 			doc.add(center);
 			doc.add(new Paragraph("        "));
 			doc.add(footer);
 			doc.add(footer2);
 			
 			doc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		
 	}//fin
 	
 	public static void creationListeEmargementDIF(Stage leStage)
 	{			
 		String cfgP="pdf.emargement.";
 		Integer colNum = Config.getI(cfgP+"dif.colnum");
 		String date = leStage.getDateStr();
 		date = date.replace("/", ".");
 		String pathDossier = expDir+Config.get(cfgP+"dir")+date;
 		new File(pathDossier).mkdir();
 	
 		Common.setStatus("Cration Liste Emargement "+leStage.getCodeI());
 
 		try {
 			
 			//creation du document
 			Document doc = new Document(PageSize.A4,10,10,10,0);
 			FileOutputStream fichier = new FileOutputStream(pathDossier+"/"
 					+Config.get(cfgP+"file")+" - "+leStage.getCode()+".pdf");
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			cfgP=cfgP+"dif.";
 			
 			PdfPTable header = new PdfPTable(3);
 			header.setWidthPercentage(100);
 			
 			PdfPCell cell = new PdfPCell(new Phrase(Config.get(cfgP+"s1"), fontB10));
 			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 			cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
 			cell.setPadding(0);
 			cell.setPaddingTop(5);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(leStage.getCode(),fontB22));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			header.addCell(cell);
 	
 			cell = new PdfPCell(new Phrase(leStage.getDateStr()+"   ",fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
 			cell.setBorder(0);
 			header.addCell(cell);
 
 			
 			//formation du center
 			PdfPTable center = new PdfPTable(7);
 			center.setWidthPercentage(98);
 
 			float[] widths = new float[] { 0.8f, 8f, 1.5f, 4f, 4f, 1f, 1.5f };
 			for (int i=1; i<=colNum; i++) { widths[i-1]=Config.getF(cfgP+"col"+i+".w"); }
 			center.setWidths(widths);
 			
 			cell.setPadding(0);
 			cell.setPaddingTop(0);
 			cell.setPaddingBottom(0);
 
 			cell = new PdfPCell(new Phrase("N",fontB10)); cell.setBorder(0);
 			cell.setColspan(3);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPhrase(new Phrase("",fontB10));				center.addCell(cell);		
 			cell.setColspan(1);
 			cell.setPhrase(new Phrase("Emargement",fontB10));	center.addCell(cell);
 			cell.setColspan(3);
 			cell.setPhrase(new Phrase("",fontB10));				center.addCell(cell);		
 			cell.setColspan(1);
 
 			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 			for (int i=1; i<=colNum; i++) {
 					cell.setPhrase(new Phrase(Config.get(cfgP+"col"+i+".str"),fontB10));
 					center.addCell(cell);				
 			}
 			/*
 			cell.setPhrase(new Phrase("",fontB10));				center.addCell(cell);		
 			cell.setPhrase(new Phrase("Stagiaire",fontB10));		center.addCell(cell);
 			cell.setPhrase(new Phrase("Pres.",fontB10));			center.addCell(cell);
 			cell.setPhrase(new Phrase("Matin",fontB10));		center.addCell(cell);
 			cell.setPhrase(new Phrase("Aprs-Midi",fontB10));		center.addCell(cell);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPhrase(new Phrase("DIF",fontB10));			center.addCell(cell);
 			cell.setPhrase(new Phrase("IS.VS",fontB10));			center.addCell(cell);
 			*/
 			
 			int i;
 			// TODO SG font constante
 			/*
 			if(leStage.getSizeStagiaireList()>=20 && leStage.getSizeStagiaireList() < 25){
 				font.setSize(11);
 			}
 			if(leStage.getSizeStagiaireList()>=25){
 				font.setSize(9);
 			}
 			*/
 			for (i = 0; i < leStage.getSizeStagiaireList(); i++) {
 					cell.setPhrase(new Phrase(""+(i+1), font));
 					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 					cell.setBorder(7);
 					center.addCell(cell);
 					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 					PdfPTable stgCell = new PdfPTable(10);
 					stgCell.setWidthPercentage(100);
 					float[] widths2 = new float[] { 1f, 2f, 2f, 2.5f, 2f, 2f, 2f, 2f, 2f, 0.5f};
 					stgCell.setWidths(widths2);
 
 					//str = String.format("%-20s%-20s\n%25s%10s%10s", 
 					cell.setBorder(0);
 					cell.setColspan(5);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(0), fontB10));
 					stgCell.addCell(cell);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(1), font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(1);
 					cell.setPhrase(new Phrase(" ", font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(2);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(3), font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(4);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(4), font));
 					stgCell.addCell(cell);
 
 					cell.setColspan(3);
 					cell.setPhrase(new Phrase(leStage.getEltStagiaireList(i).getInfo(2), font));
 					stgCell.addCell(cell);
 					
 					cell.setColspan(1);
 					center.addCell(stgCell);
 					
 					//presence
 					center.addCell(" ");
 					//emargement M
 					center.addCell(" ");
 					//emargement A-M
 					center.addCell(" ");
 					//DIF
 					center.addCell(" ");
 					//isvsmp
 					center.addCell(" ");
 			}
 			if(i<19){
 				cell.setBorder(0);
 				cell.setPhrase(new Phrase("", font));
 				cell.setColspan(5);
 				for (int j = i; j < 19; j++) {
 					//for (int k = 0; k < 5; k++) {
 						//Phrase phrs = new Phrase("- \n ", new Font(Font.TIMES_ROMAN, 10));
 						//cell = new PdfPCell(phrs);
 						//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 						//cell.setPadding(4);
 						center.addCell(cell);
 					//}
 				}//fin pour
 			}//fin si
 
 			// DIF
 			PdfPTable footer0 = new PdfPTable(1);
 			footer0.setWidthPercentage(95);
 			cell = new PdfPCell(new Phrase(Config.get(cfgP+"s2"), font9));
 			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 			cell.setPaddingBottom(5);
 			cell.setBorder(0);
 			footer0.addCell(cell);
 			
 			// Footer
 			PdfPTable footer = new PdfPTable(3);
 			footer.setWidthPercentage(75);
 			
 			cell = new PdfPCell(new Phrase("Nombre de stagiaires :", fontB12));
 			cell.setColspan(3);
 			cell.setBorder(0);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("Prvus", fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(0);
 			cell.setBorderWidthBottom(1);
 			cell.setBorderWidthRight(1);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("Prsents", fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthBottom(1);
 			cell.setBorderWidthRight(1);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("Maxi Prsents", fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(0);
 			cell.setBorderWidthBottom(1);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(""+leStage.getSizeStagiaireList(), fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(0);
 			cell.setBorderWidthRight(1);
 			cell.setBorderWidthBottom(0);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(" ", fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(1);
 			cell.setBorderWidthBottom(0);;
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(""+leStage.getMaxiPresent(),fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(0);
 			cell.setBorderWidthBottom(0);
 			footer.addCell(cell);
 			
 			//formateur
 			PdfPTable footer2 = new PdfPTable(1);
 			footer2.setWidthPercentage(80);
 			cell = new PdfPCell(new Phrase("Formateurs : _ _ _ _ _ _ _ _ _ _ _ _ _ _ _/ " +
 					leStage.getLeader(), fontB12));
 			cell.setBorder(0);
 			footer2.addCell(cell);
 						
 			// DIF
 			/*
 			PdfPTable footer3 = new PdfPTable(1);
 			footer3.setWidthPercentage(95);
 			cell = new PdfPCell(new Phrase(Config.get(cfgP+"s2"), font));
 			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 			cell.setPaddingBottom(5);
 			cell.setBorder(0);
 			footer3.addCell(cell);
 			*/
 			
 			//ajout des composants
 			doc.add(header);
 			//doc.add(new Paragraph("        "));
 			doc.add(center);
 			doc.add(footer0);
 			doc.add(new Paragraph("   ", font9));
 			doc.add(footer);
 			doc.add(footer2);
 			
 			doc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		
 	}//fin
 
 	/**
 	 * creer le pdf pour la liste d'emargement pour le stage pass en parametre
 	 * @param leStage
 	 */
 	public static void creationListeEmargementVide(Stage leStage){
 		
 		String date = leStage.getDateStr();
 		date = date.replace("/", ".");
 		String pathDossier = "Emargement du "+date;
 		new File(expDir+pathDossier).mkdir();
 		
 		Common.setStatus("Cration Liste Emargememt "+leStage.getCodeI());
 
 		try {
 			
 			//creation du document
 			Document doc = new Document(PageSize.A4,10,10,10,0);
 			FileOutputStream fichier = new FileOutputStream(expDir+pathDossier+"\\listeEmargement - "+leStage.getCode()+".pdf");
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			PdfPTable header = new PdfPTable(1);
 			header.setWidthPercentage(100);
 			
 			PdfPCell cell = new PdfPCell(new Phrase(leStage.getDateStr(),fontB12));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			header.addCell(cell);
 
 			Phrase par = new Phrase(leStage.getCode(),new Font(Font.HELVETICA, 24, Font.BOLD));
 			cell = new PdfPCell(par);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			PdfPTable center = new PdfPTable(34);
 			center.setWidthPercentage(95);
 			cell = new PdfPCell(new Phrase("",fontB10));
 			cell.setBorder(0);
 			cell.setColspan(2);
 			center.addCell(cell);
 			cell = new PdfPCell(new Phrase("Nom",fontB10));
 			cell.setBorder(0);
 			cell.setColspan(8);
 			center.addCell(cell);
 			cell = new PdfPCell(new Phrase("Prenom",fontB10));
 			cell.setBorder(0);
 			cell.setColspan(8);
 			center.addCell(cell);
 			cell = new PdfPCell(new Phrase("Matricule",fontB10));
 			cell.setBorder(0);
 			cell.setColspan(8);
 			center.addCell(cell);
 			cell = new PdfPCell(new Phrase("Emargement",fontB10));
 			cell.setColspan(8);
 			cell.setBorder(0);
 			center.addCell(cell);
 			
 			for (int j = 0; j < 20; j++) {
 					Phrase phrs = new Phrase(""+(j+1), fontB12);
 					cell = new PdfPCell(phrs);
 					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 					cell.setColspan(2);
 					center.addCell(cell);
 					//nom
 					phrs = new Phrase(" \n ", fontB12);
 					cell = new PdfPCell(phrs);
 					cell.setColspan(8);
 					center.addCell(cell);
 					//prenom
 					phrs = new Phrase("  ", fontB12);
 					cell = new PdfPCell(phrs);;
 					cell.setColspan(8);
 					center.addCell(cell);
 					//matricule
 					for (int k = 0; k < 8; k++) {
 						phrs = new Phrase(" ", fontB12);
 						cell = new PdfPCell(phrs);
 						center.addCell(cell);
 					}
 					//emargement
 					phrs = new Phrase("  ", fontB12);
 					cell = new PdfPCell(phrs);
 					cell.setPadding(4);
 					cell.setColspan(8);
 					center.addCell(cell);
 			}//fin pour
 			
 			//nombre de stagiares
 			PdfPTable footer = new PdfPTable(3);
 			footer.setWidthPercentage(75);
 			cell = new PdfPCell(new Phrase("Nombre de stagiaires :",fontB14));
 			cell.setColspan(3);
 			cell.setBorder(0);
 			footer.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("Prvus",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(0);
 			cell.setBorderWidthBottom(1);
 			cell.setBorderWidthRight(1);
 			footer.addCell(cell);
 			
 			//presents
 			cell = new PdfPCell(new Phrase("Prsents",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthBottom(1);
 			cell.setBorderWidthRight(1);
 			footer.addCell(cell);
 			
 			//maxipresent
 			cell = new PdfPCell(new Phrase("Maxi Prsents",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(0);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(0);
 			cell.setBorderWidthBottom(1);
 			footer.addCell(cell);
 			
 			//cellule vide
 			cell = new PdfPCell(new Phrase(" ",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(0);
 			cell.setBorderWidthRight(1);
 			cell.setBorderWidthBottom(0);
 			footer.addCell(cell);
 			
 			//cellule vide
 			cell = new PdfPCell(new Phrase(" ",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(1);
 			cell.setBorderWidthBottom(0);
 			footer.addCell(cell);
 			
 			//cellule vide
 			cell = new PdfPCell(new Phrase(" ",fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorderWidthTop(1);
 			cell.setBorderWidthLeft(1);
 			cell.setBorderWidthRight(0);
 			cell.setBorderWidthBottom(0);
 			footer.addCell(cell);
 			
 			//formateur
 			PdfPTable footer2 = new PdfPTable(1);
 			footer2.setWidthPercentage(80);
 			par = new Phrase("Formateurs : _ _ _ _ _ _ _ _ _ _ _ _ _ _ _/ "+leStage.getLeader(),fontB14);
 			cell = new PdfPCell(par);
 			cell.setBorder(0);
 			footer2.addCell(cell);
 			
 			//ajout des composants
 			doc.add(header);
 			doc.add(new Paragraph("        "));
 			doc.add(center);
 			doc.add(new Paragraph("        "));
 			doc.add(footer);
 			doc.add(footer2);
 			
 			//fermeture
 			doc.close();
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		
 	}//fin
 	
 	/**
 	 * creer les pdf des feuilles de routes FSS
 	 */
 	public static void creationAllFRFSS(ArrayList<Stage> stageList){
 		
 		ArrayList<String> fssList = new ArrayList<String>();
 		String date = stageList.get(0).getDateStr();
 		
 		for (Stage leStage : stageList) {
 			ArrayList<Module> moduleList = leStage.getModuleList();
 			for (Module module : moduleList) {
 				if( ! module.getNomLeader().equalsIgnoreCase("")){
 					if(! fssList.contains(module.getNomLeader())){
 						fssList.add(module.getNomLeader());
 					}
 				}//finsi
 				if( ! module.getNomAide().equalsIgnoreCase("")){
 					if(! fssList.contains(module.getNomAide())){
 						fssList.add(module.getNomAide());
 					}
 				}//finsi
 			}//finpour
 		}//finpour
 		
 		for (String nomFss : fssList) {
 			ArrayList<Module> moduleList = new ArrayList<Module>();
 			for (Stage leStage : stageList) {
 				for (Module module : leStage.getModuleList()) {
 					if(module.getNomLeader().equalsIgnoreCase(nomFss)){
 						moduleList.add(module);
 					}else{
 						if(module.getNomAide().equalsIgnoreCase(nomFss)){
 							moduleList.add(module);
 						}
 					}
 				}//finpour
 			}//finpour
 			if(moduleList.size()!=0){
 				creationFeuilRouteFSS(date, nomFss, moduleList);
 			}
 		}//finpour
 		
 	}//fin creationAllFRFSS()
 	
 	/**
 	 * creer le pdf pour la feuille de route pour les FSS
 	 * @param Nom
 	 * @param moduleList
 	 */
 	public static void creationFeuilRouteFSS(String date, String Nom, ArrayList<Module> moduleList){
 		
 		//creation du dossier
 		String ladate = date.replace("/", ".");
 		String pathDossier = "Feuilles de routes du "+ladate;
 		new File(expDir+pathDossier).mkdir();
 		
 		
 		// Suppression des feuilles inutiles :
 		if (Nom.startsWith("IFH PNC") || Nom.startsWith("IPNC") || Nom.startsWith("CADRE PNT")
 				|| Nom.startsWith("Infirmire EXT") || Nom.startsWith("SFI")) {
 			return;
 		}
 		
 		//tri
 		Module modTemps ;
 		boolean good = false;
 		//tant que le tri n'est pas bon
 		while (! good) {
 			good = true;
 			for (int i = 0; i < moduleList.size()-1; i++) {
 				if(moduleList.get(i).getnbMin() > moduleList.get(i+1).getnbMin()){
 					good = false;
 					//echange
 					modTemps = moduleList.get(i);
 					moduleList.set(i, moduleList.get(i+1));
 					moduleList.set(i+1, modTemps);
 				}//finsi
 			}//finpour
 		}//fin tant que
 		
 		Common.setStatus("Cration Feuille de Route FSS " + Nom);
 
 		try {
 			
 			Document doc = new Document(PageSize.A4);
 			FileOutputStream fichier = new FileOutputStream(expDir+pathDossier+"\\FeuilleRouteFSS - "+Nom+".pdf");
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			PdfPTable header = new PdfPTable(1);
 			Phrase phrs = new Phrase(date, fontB15);
 			PdfPCell cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			header.addCell(cell);
 			phrs = new Phrase(Nom,new Font(Font.HELVETICA, 20, Font.BOLD));
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			header.addCell(cell);
 			phrs = new Phrase("Stages du jour",fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			PdfPTable center = new PdfPTable(5);
 			center.setWidthPercentage(100);
 			float[] widths = new float[] { 1f, 1f, 1f, 1.2f, 1.2f };
 			center.setWidths(widths);
 
 			phrs = new Phrase("Code Stage",fontB12);
 			cell = new PdfPCell(phrs);
 			cell.setBorder(0);
 			cell.setPadding(10);
 			center.addCell(cell);
 			phrs = new Phrase("Leader/Aide",fontB12);
 			cell = new PdfPCell(phrs);
 			cell.setBorder(0);
 			cell.setPadding(10);
 			center.addCell(cell);
 			phrs = new Phrase("Horaire",fontB12);
 			cell = new PdfPCell(phrs);
 			cell.setBorder(0);
 			cell.setPadding(10);
 			center.addCell(cell);
 			phrs = new Phrase("Module",fontB12);
 			cell = new PdfPCell(phrs);
 			cell.setBorder(0);
 			cell.setPadding(10);
 			center.addCell(cell);
 			phrs = new Phrase("Salle",fontB12);
 			cell = new PdfPCell(phrs);
 			cell.setBorder(0);
 			cell.setPadding(10);
 			center.addCell(cell);
 			for (Module module : moduleList) {
 				phrs = new Phrase(module.getCodeStage(),new Font(Font.HELVETICA, 12));
 				cell = new PdfPCell(phrs);
 				cell.setBorder(0);
 				cell.setPadding(10);
 				center.addCell(cell);
 				String lead = "Leader";
 				String aide = module.getNomAide();
 				if(! module.getNomLeader().equalsIgnoreCase(Nom)){
 					lead = "Aide";
 					aide = module.getNomLeader();
 				}
 				phrs = new Phrase(lead,new Font(Font.HELVETICA, 12));
 				if (!aide.equals("")) {
 					phrs.add(new Phrase("\n"+aide,new Font(Font.HELVETICA, 9, Font.ITALIC)));
 
 				}
 				//phrs = new Phrase(lead,new Font(Font.HELVETICA, 10));
 				cell = new PdfPCell(phrs);
 				cell.setBorder(0);
 				cell.setPadding(10);
 				center.addCell(cell);
 				phrs = new Phrase(module.getHeureDebut()+" - "+module.getHeureFin(),new Font(Font.HELVETICA, 12));
 				cell = new PdfPCell(phrs);
 				cell.setBorder(0);
 				cell.setPadding(10);
 				center.addCell(cell);
 				phrs = new Phrase(module.getLibelle(),new Font(Font.HELVETICA, 12));
 				cell = new PdfPCell(phrs);
 				cell.setBorder(0);
 				cell.setPadding(10);
 				center.addCell(cell);
 				phrs = new Phrase(module.getSalle(),new Font(Font.HELVETICA, 12));
 				cell = new PdfPCell(phrs);
 				cell.setBorder(0);
 				cell.setPadding(10);
 				center.addCell(cell);
 			}
 			
 			doc.add(header);
 			doc.add(new Phrase("  "));
 			doc.add(center);
 			
 			doc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		
 	}//fin
 	
 	/**
 	 * creer le pdf pour le FREP du stage pass en parametre
 	 * @param leStage
 	 */
 	public static void creationFREP(Stage leStage){
 		
 		String date = leStage.getDateStr();
 		date = date.replace("/", ".");
 		String pathDossier = "FREP du "+date;
 		new File(expDir+pathDossier).mkdir();
 		
 		Common.setStatus("Cration FREP "+leStage.getCodeI());
 
 		try {
 			Document doc = new Document(PageSize.A4,60,60,10,10);
 			FileOutputStream fichier;
 			fichier = new FileOutputStream(expDir+pathDossier+"\\FREP - "+leStage.getCode()+".pdf");
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			//construction du header
 			PdfPTable header = new PdfPTable(1);
 			header.setWidthPercentage(100);
 			
 			//la date
 			PdfPCell cell = new PdfPCell(new Phrase(leStage.getDateStr(),fontB14));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			//le code du stage
 			Phrase par = new Phrase(leStage.getCode(),new Font(Font.HELVETICA, 28, Font.BOLD));
 			cell = new PdfPCell(par);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			//l'intitul
 			par = new Phrase("Fiche rcaputulative de fin de formation",new Font(Font.HELVETICA, 18, Font.BOLD));
 			cell = new PdfPCell(par);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			doc.add(header);
 			
 			par = new Phrase("\n");
 			doc.add(par);
 			
 			par = new Phrase("Conformment au programme, les exercices pratiques suivant ont t raliss :");
 			doc.add(par);
 			
 			ArrayList<String> list = new ArrayList<String>();
 			list .add("    Equipement");
 			list .add("    Feu - Fume");
 			list .add("    Manuvres en mode normal des portes");
 			list .add("    Manuvres en mode secours des portes");
 			list .add("    Trappe PSU");
 			list .add("    Manuvres en mode secours des issues d'aile");
 			list .add("    Evacuation par toboggan");
 			list .add("    Convertible");
 			list .add("    Evacuation par toboggan dgonfl");
 			list .add("    Evacuation par le dispositif associ  une issu d'aile");
 			list .add("    Vol simul en maquette ou simulatuer cabine");
 			list .add("    Vol simul en mode alternatif (en salle)");
 			
 			PdfPTable listTable = new PdfPTable(4);
 			listTable.setWidthPercentage(100);
 			for (String string : list) {
 				cell = new PdfPCell(new Phrase(string));
 				cell.setColspan(3);
 				cell.setPadding(3);
 				cell.setBorder(0);
 				listTable.addCell(cell);
 				cell = new PdfPCell(new Phrase("Oui / Sans objet *"));
 				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 				cell.setBorder(0);
 				cell.setPadding(3);
 				listTable.addCell(cell);
 			}//fin pour
 			
 			cell = new PdfPCell(new Phrase("  Visualisation sur avion"));
 			cell.setColspan(2);
 			cell.setPadding(3);
 			cell.setBorder(0);
 			listTable.addCell(cell);
 			cell = new PdfPCell(new Phrase("Immat. : _ _ _ _ _ _ / sans objet *"));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			cell.setColspan(2);
 			cell.setPadding(3);
 			listTable.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase("  Visualisation virtuelle"));
 			cell.setColspan(2);
 			cell.setPadding(3);
 			cell.setBorder(0);
 			listTable.addCell(cell);
 			cell = new PdfPCell(new Phrase("Type-avion : _ _ _ _ _ _ / sans objet *"));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			cell.setColspan(2);
 			cell.setPadding(3);
 			listTable.addCell(cell);
 			
 			listTable.addCell(new PdfPCell(new Phrase(" ")));
 			
 			doc.add(listTable);
 			
 			
 			PdfPTable question = new PdfPTable(5);
 			question.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("Des stagiaires ont-ils echou aux exercices pratiques ?"));
 			cell.setColspan(4);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			question.addCell(cell);
 			cell = new PdfPCell(new Phrase("Oui / Non *"));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			cell.setPadding(5);
 			question.addCell(cell);
 			doc.add(question);
 			
 			par = new Phrase("      Si oui, lesquels ? _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n");
 			doc.add(par);
 			
 			par = new Phrase("      Sur quel exercice ? _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n");
 			doc.add(par);
 			
 			question = new PdfPTable(5);
 			question.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("Cette information a-t-elle t transmise  IS.VS MP ?"));
 			cell.setColspan(4);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			question.addCell(cell);
 			cell = new PdfPCell(new Phrase("Oui / Non *"));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			cell.setPadding(5);
 			question.addCell(cell);
 			doc.add(question);
 			
 			question = new PdfPTable(5);
 			question.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("Des stagiaires sont-ils partis en cours de formation ?"));
 			cell.setColspan(4);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			question.addCell(cell);
 			cell = new PdfPCell(new Phrase("Oui / Non *"));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			cell.setPadding(5);
 			question.addCell(cell);
 			doc.add(question);
 			
 			par = new Phrase("      Si oui, lesquels ?  _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n");
 			doc.add(par);
 			
 			question = new PdfPTable(5);
 			question.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("Un des exercices a-t-il t ralis en mode drogatoire ?"));
 			cell.setColspan(4);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			question.addCell(cell);
 			cell = new PdfPCell(new Phrase("Oui / Non *"));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			cell.setPadding(5);
 			question.addCell(cell);
 			doc.add(question);
 			
 			par = new Phrase("      Si oui, lesquels ?  _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n");
 			doc.add(par);
 			
 			question = new PdfPTable(5);
 			question.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("La dure de formation s'est-elle carte de plus de 15mn du temps prvu ?"));
 			cell.setColspan(4);
 			cell.setPadding(5);
 			cell.setBorder(0);
 			question.addCell(cell);
 			cell = new PdfPCell(new Phrase("Oui / Non *"));
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			cell.setPadding(5);
 			question.addCell(cell);
 			doc.add(question);
 			
 			par = new Phrase("      Si oui, pourquoi ?  _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n");
 			doc.add(par);
 			
 			par = new Phrase("Voir Remarques / particularits du stage au verso (AT, problmes rencontrs, ...) \n \n");
 			doc.add(par);
 			
			par = new Phrase("Nom du FSS leader :  " +  leStage.getLeader() + "  / _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n");
 			par.setFont(new Font(Font.HELVETICA, 14));
 			doc.add(par);
 			
 			PdfPTable sign = new PdfPTable(3);
 			sign.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("Signature du FSS leader :"));
 			cell.setBorder(0);
 			cell.setColspan(2);
 			sign.addCell(cell);
 			cell = new PdfPCell(new Phrase("Visa du CPO :"));
 			cell.setBorder(0);
 			sign.addCell(cell);
 			doc.add(sign);
 			
 			par = new Phrase("\n\n\n* Rayer la mention inutile",new Font(Font.HELVETICA, 8));
 			doc.add(par);
 			
 			doc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (DocumentException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * creer le pdf pour le surbook du stage pass en parametre
 	 * @param leStage
 	 */
 	public static void creationSurbook(Stage leStage){
 		
 		String date = leStage.getDateStr();
 		date = date.replace("/", ".");
 		String pathDossier = "Surbook du "+date;
 		new File(expDir+pathDossier).mkdir();
 		
 		Common.setStatus("Cration Surbook "+leStage.getCodeI());
 
 		try {
 			FileOutputStream fichier;
 			fichier = new FileOutputStream(expDir+pathDossier+"\\Surbook - "+leStage.getCode()+".pdf");
 			Document doc = new Document(PageSize.A4,40,40,20,20);
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			PdfPTable header = new PdfPTable(3);
 			header.setWidthPercentage(95);
 			Phrase phrs = new Phrase("Direction Gnrale de la Qualit et des Oprations\n" +
 									"Centre de Formation du PN\n" +
 									"Formation Scurit Sauvetage du PN"
 									,fontB12);
 			PdfPCell cell = new PdfPCell(phrs);
 			cell.setBorder(0);
 			cell.setColspan(2);
 			header.addCell(cell);
 			Image img = Image.getInstance(Config.getRes("Airfrance.jpg"));
 			cell = new PdfPCell(img);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			doc.add(header);
 			
 			doc.add(new Phrase("\n"));
 			
 			PdfPTable title = new PdfPTable(1);
 			title.setWidthPercentage(75);
 			
 			phrs = new Phrase(leStage.getDateStr(),fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			title.addCell(cell);
 			phrs = new Phrase(leStage.getCode(),fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			title.addCell(cell);
 			phrs = new Phrase(leStage.getLibelle(),new Font(Font.HELVETICA,15,Font.BOLD));
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			title.addCell(cell);
 			phrs = new Phrase(" ");
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setBorder(0);
 			title.addCell(cell);
 			phrs = new Phrase("- Stagiaire  l'heure refus cause surbook -",new Font(Font.HELVETICA,18,Font.BOLD));
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			title.addCell(cell);
 			
 			doc.add(title);
 			
 			phrs = new Phrase("\n\nNom du stagiaire : _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"
 								,new Font(Font.HELVETICA,15));
 			doc.add(phrs);
 			
 			phrs = new Phrase("matricule : _   _   _   _   _   _   _   _\n\n"
 								,new Font(Font.HELVETICA, 15));
 			doc.add(phrs);
 			
 			phrs = new Phrase("Madame, Monsieur,\n\n\n" +
 					"Les programmes d'instruction dposs  la DGAC prvoient un nombre maximum de stagiaires admissibles" +
 					"en stage ( 16 sur les stages de spcialisation, 18 sur les stages de maintien des comptences).\n\n" +
 					"Afin d'optimiser l'utilisation des moyens d'instruction, les services dits de production" +
 					"PNC sont amens  grer des stages avec surbook.\n\n" +
 					"Le nombre de stagiaires prsents aujourd'hui sur le vtre a conduit votre formateur  en limiter l'accs." +
 					"Il n'existe en effet aucune tolrance sur le nombre maximum de stagiaires admissibles.\n\n" +
 					"le choix de la personne invite  se faire inscrire sur un autre stage se fait :"
 					,new Font(Font.HELVETICA,12));
 			doc.add(phrs);
 			
 			List list = new List(false);
 			list.add(new ListItem(new Phrase("conformment  l'interligne 04-079 du 4 octobre 2004",new Font(Font.HELVETICA,12))));
 			list.add(new ListItem(new Phrase("selon les indications des services de produstion PNC",new Font(Font.HELVETICA,12))));
 			doc.add(list);
 			
 			phrs = new Phrase("\nNous vous remercions de votre comprhension et votre prions de bien vouloir prendre contact" +
 					"avec la permanence oprationnelle situe Salle 003 au RDC du BEPN afin de faire enregistrer votre prsence et" +
 					"vous rendre ensuite au suivi planning muni du prsent document.\n\n\n"
 					,new Font(Font.HELVETICA,12));
 			doc.add(phrs);
 			
 			PdfPTable footer = new PdfPTable(2);
 			footer.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("Signature du FSS :",new Font(Font.HELVETICA, 13)));
 			cell.setBorder(0);
 			footer.addCell(cell);
 			cell = new PdfPCell(new Phrase("Visa du CPO :",new Font(Font.HELVETICA, 13)));
 			cell.setBorder(0);
 			footer.addCell(cell);
 			doc.add(footer);
 			
 			doc.close();
 			
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 			
 	}//fin creationSurbook(
 
 	/**
 	 * creer le pdf pour la checkListe pour le ple Administratif
 	 * @param date
 	 */
 	public static void creationCheckListAdm(String ladate, ArrayList<Stage> stageList){
 		
 		String date = ladate.replace("/", ".");
 		String pathDossier = "CheckListPoleAdm du "+date;
 		new File(expDir+pathDossier).mkdir();
 		
 		//tri des stages par ordre alphabetique
 		Stage stgTemps ;
 		boolean good = false;
 		//tant que le tri n'est pas bon
 		while (! good) {
 			good = true;
 			for (int i = 0; i < stageList.size()-1; i++) {
 				if(stageList.get(i).getCode().compareToIgnoreCase(stageList.get(i+1).getCode()) > 0){
 					good = false;
 					//echange
 					stgTemps = stageList.get(i);
 					stageList.set(i, stageList.get(i+1));
 					stageList.set(i+1, stgTemps);
 				}//finsi
 			}//finpour
 		}//fin tant que
 		
 		Common.setStatus("Cration C/L Admin du "+ladate);
 
 		try {
 			FileOutputStream fichier;
 			fichier = new FileOutputStream(expDir+pathDossier+"\\CheckListPleAdm - "+date+".pdf");
 			Document doc = new Document(PageSize.A4,20,20,10,10);
 			doc.setPageSize(PageSize.A4.rotate());
 			doc.newPage();
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			PdfPTable header = new PdfPTable(2);
 			header.setWidthPercentage(100);
 			
 			Phrase phrs = new Phrase("RETOUR Listes Stagiaires et Emargements", fontB15);
 			PdfPCell cell = new PdfPCell(phrs);
 			cell.setBorder(0);
 			header.addCell(cell);
 			phrs = new Phrase(ladate, fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			doc.add(header);
 			doc.add(new Phrase("\n"));
 					
 			BaseColor gray = new BaseColor(175, 175, 175);
 			BaseColor lightGray = new BaseColor(210, 210, 210);
 			
 			PdfPTable tableau = new PdfPTable(14);
 			tableau.setWidthPercentage(100);
 			
 			cell = new PdfPCell();
 			cell.setColspan(3);
 			cell.setBorder(0);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("ABSENCES",fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(gray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setColspan(3);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("TRAITEMENT AF",fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(gray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setColspan(2);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("TRAITEMENT TIERS",fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(gray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setColspan(2);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("TRAITEMENT DELIA",fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(gray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setColspan(2);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("FREP",fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(gray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setRowspan(2);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("STATS",fontB15);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(gray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setRowspan(2);
 			tableau.addCell(cell);
 			
 			//deuxieme ligne
 			
 			cell = new PdfPCell();
 			cell.setColspan(3);
 			cell.setBorder(0);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("ABS",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("R/Prod",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("Dbarqus",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("ITRI",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("SCAN",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("Attestations",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("SCAN",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("PREVUS",fontB09);;
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			phrs = new Phrase("PRESENTS",fontB09);
 			cell = new PdfPCell(phrs);
 			cell.setBackgroundColor(lightGray);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			tableau.addCell(cell);
 			
 			//lignes de stage
 			int index = 0;
 			for (Stage lestage : stageList) {
 				//premiere cellule
 				index++;
 				phrs = new Phrase(lestage.getCode(),fontB12);
 				cell = new PdfPCell(phrs);
 				cell.setColspan(3);
 				cell.setFixedHeight(480 / stageList.size());
 				cell.setBackgroundColor(gray);
 				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 				tableau.addCell(cell);
 				//autres cellules
 				for (int i = 3; i < tableau.getNumberOfColumns(); i++) {
 					phrs = new Phrase("");
 					cell = new PdfPCell(phrs);
 					if ((index % 2 ) == 0) {
 						cell.setBackgroundColor(lightGray);
 					}
 					tableau.addCell(cell);
 				}//fin pour
 			}//fin pour chaque
 			
 			doc.add(tableau);
 			
 			//frmeture du document
 			doc.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}//fin creationCheckListAdm()
 	
 
 	/**
 	 * creer le pdf pour le surbook du stage pass en parametre
 	 * @param leStage
 	 */
 	public static void creationAffichageSalle(Stage leStage){
 		
 		SimpleDateFormat fmt = new SimpleDateFormat("EEEE d MMMM yyyy");
 		String date = leStage.getDateStr();
 		date = date.replace("/", ".");
 		String pathDossier = "dataExport/Salles "+date;
 		new File(pathDossier).mkdir();
 		
 		String code = leStage.getCode();
 
 		Common.setStatus("Cration Affichage Salle "+leStage.getCodeI());
 
 		try {
 			FileOutputStream fichier;
 			fichier = new FileOutputStream(pathDossier+"/Salle - "+leStage.getCode()+".pdf");
 			Document doc = new Document(PageSize.A4.rotate(),40,40,20,20); 	
 			PdfWriter.getInstance(doc, fichier);
 			doc.open();
 			
 			PdfPTable header = new PdfPTable(3);
 			header.setWidthPercentage(95);
 			Font font  = new Font(Font.HELVETICA,  20, Font.BOLD);
 			Font font2 = new Font(Font.HELVETICA, 140, Font.BOLD);
 			Font font3 = new Font(Font.HELVETICA,  95, Font.BOLD);
 
 			PdfPCell cell;
 			try {
 				Image img = Image.getInstance(Config.get("data.logos")+"Orig/"+leStage.getCompagnie()+".jpg");
 				img.scaleAbsoluteWidth(30*img.getWidth()/img.getHeight());
 				img.scaleAbsoluteHeight(30);
 				cell = new PdfPCell(img); 
 			} catch (FileNotFoundException e) {
 				cell = new PdfPCell(new Phrase("")); 
 			}
 			//Chunk c = new Chunk(img,0,0);
 			//PdfPCell cell = new PdfPCell(new Phrase(""));
 			cell.setBorder(0);
 			header.addCell(cell);
 			
 			cell = new PdfPCell(new Phrase(fmt.format(leStage.getDateDt()), font));
 			cell.setBorder(0);
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			header.addCell(cell);
 
 			cell = new PdfPCell(new Phrase(""));
 			cell.setBorder(0);
 			header.addCell(cell);
 
 			doc.add(header);
 			
 			PdfPTable center = new PdfPTable(1);
 			center.setWidthPercentage(95);
 			
 			Phrase phrs1 = new Phrase("",font);
 			PdfPCell cell1 = new PdfPCell(phrs1);
 			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
 			cell1.setBorder(0);
 			cell1.setMinimumHeight(120);
 
 			Phrase phrs2 = new Phrase(leStage.getCode(),font2);
 			PdfPCell cell2 = new PdfPCell(phrs2);
 			cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
 			cell2.setBorder(0);
 			cell2.setMinimumHeight(200);
 			
 			Phrase phrs3 = new Phrase(leStage.getLibelle(), font);
 			PdfPCell cell3 = new PdfPCell(phrs3);
 			cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
 			cell3.setBorder(0);
 			cell3.setMinimumHeight(100);
 		
 			if (code.length() > 10) {
 				cell1.setMinimumHeight(50);
 				phrs2 = new Phrase(leStage.getCode(),font3); cell2.setPhrase(phrs2);
 				cell2.setMinimumHeight(300);
 				cell3.setMinimumHeight(50);
 			}
 
 			center.addCell(cell1);
 			center.addCell(cell2);
 			center.addCell(cell3);
 
 			doc.add(center);
 									
 			PdfPTable footer = new PdfPTable(1);
 			footer.setWidthPercentage(95);
 			cell = new PdfPCell(new Phrase(leStage.getFirstModule().getSalle(), font));
 			cell.setBorder(0);
 			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 			footer.addCell(cell);
 			doc.add(footer);
 			
 			doc.close();
 			
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 			
 	}//fin
 
 }//fin class
