 package pack;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.swing.JOptionPane;
 
 import ui.Common;
 
 import com.itextpdf.text.BadElementException;
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Rectangle;
 //import com.itextpdf.text.Font.FontFamily;
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
 	
 	public static class FSS_Modules {
 		public String nomFSS;
 		public ArrayList<Module> modules;
 		public FSS_Modules(String nomFss) {
 			this.nomFSS = nomFss;
 			this.modules = new ArrayList<Module>();
 		}
 	};
 
 	private static final class FontFamily {
 		//public static final int COURIER = 0;
 		public static final int HELVETICA = 1;
 		//public static final int TIMES_ROMAN = 2;
 		//public static final int SYMBOL = 3;
 		//public static final int ZAPFDINGBATS = 4;
 	}
 
 	private static String expDir = Config.get("pdf.expDir");
 	private static Font font9   = new Font(FontFamily.HELVETICA, 9);
 	private static Font font    = new Font(FontFamily.HELVETICA, 10);
 	private static Font font12  = new Font(FontFamily.HELVETICA, 12);
 	private static Font fontB9  = new Font(FontFamily.HELVETICA,  9, Font.BOLD);
 	private static Font fontB10 = new Font(FontFamily.HELVETICA, 10, Font.BOLD);
 	private static Font fontB12 = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
 	private static Font fontB14 = new Font(FontFamily.HELVETICA, 14, Font.BOLD);
 	private static Font fontB15 = new Font(FontFamily.HELVETICA, 15, Font.BOLD);
 	private static Font fontB20 = new Font(FontFamily.HELVETICA, 20, Font.BOLD);
 	private static Font fontB22 = new Font(FontFamily.HELVETICA, 22, Font.BOLD);
 	private static Font fontB28 = new Font(FontFamily.HELVETICA, 28, Font.BOLD);
 	private static Font fontB140= new Font(FontFamily.HELVETICA,140, Font.BOLD);
 	private static Font fontB95 = new Font(FontFamily.HELVETICA, 95, Font.BOLD);
 	private static Font fontI9 =  new Font(FontFamily.HELVETICA,  9, Font.ITALIC);
 	private static Font fontBI9  = new Font(FontFamily.HELVETICA,  9, Font.BOLD | Font.ITALIC);
 
 	/** 
 	 *  Liste des document ditables
 	 */
 	//public enum DocTypes {
 	/** Liste des stagiaires pour affichage */
 	public static final int	LISTE_STAGIAIRES = 0x0;
 	/** Liste des stagiaires pour margement */
 	public static final int	LISTE_EMARGEMENTS = 0x1;
 	/** Liste des stagiaires pour margement avec DIF */
 	public static final int	LISTE_EMARGEMENTS_DIF = 0x2;
 	/** Liste des stagiaires <b>vide</b> pour margement */
 	public static final int	LISTE_EMARGEMENT_VIDE = 0x3;
 	/** FREP */
 	public static final int	FREP = 0x4;
 	/** Feuille de route FSS */
 	public static final int	FEUILLE_ROUTE_FSS = 0x5;
 	/** Feuille Surbook stagiaire */
 	public static final int	SURBOOK = 0x6;
 	/** Affichage entre salle */
 	public static final int	AFFICHAGE_SALLE = 0x7;
 	/** C/L ple admin */
 	public static final int	CHECKLIST = 0x8;
 	/** Dossier complet FSS */
 	public static final int	DOSSIER_FSS = 0x9;
 	/** Liste des stagiaires pour affichage 4S */
 	public static final int	LISTE_STAGIAIRES_GROUPE = 0x10;
 	/** Liste des stagiaires pour margement 4S*/
 	public static final int	LISTE_EMARGEMENTS_GROUPE = 0x11;
 
 	//};
 		
 	@SuppressWarnings("unchecked")
 	public static void creationDoc(/*Stage leStage*/ Object param, int type) {
 		String cfgP="";
 		String fileName="";
 		boolean dif = false;
 		Stage leStage = null;
 		ArrayList<Stage> listeStage = null;
 		FSS_Modules fssModules = null;
 		
 		if (param.getClass().toString().equals("class pack.Stage")) {
 			leStage = (Stage)param;
 		}
 		else if (param.getClass().toString().equals("class java.util.ArrayList")) {
 			listeStage = (ArrayList<Stage>) param;
 			leStage = listeStage.get(0);
 		}
 		else if (param.getClass().toString().equals("class pack.PasserellePDF$FSS_Modules")) {
 			fssModules = (FSS_Modules) param;
 			leStage = fssModules.modules.get(0).getStage();
 		}
 
 		else {
 			System.out.println("[ERR] creationDoc bad arg "+param.getClass().toString());
 			return;
 		}
 
 		//System.out.println("creationDoc("+param.getClass()+" "+leStage.getCode()+", "+type+")");
 		// Type de doc
 		cfgP = Config.get("pdf.doc."+type);
 		if (cfgP == null) {
 			System.out.println("[ERR] creationDoc unknown type "+type);
 		}
 		else {
 			cfgP="pdf."+cfgP+".";
 		}
 		
 		// Nom du fichier
 		String date = leStage.getDateStr();
 		String dateP = date.replace("/", ".");
 		String naming = Config.get(cfgP+"naming");
 		if (naming.equalsIgnoreCase("stage")) {
 			fileName = Config.get(cfgP+"file")+" - "+leStage.getCode()+".pdf";
 		} else if (naming.equalsIgnoreCase("fss")) {
 			fileName = Config.get(cfgP+"file")+" - "+fssModules.nomFSS+".pdf";
 			if (Config.getB(cfgP+"filter") 
 					&& fssModules.nomFSS.matches(Config.get(cfgP+"filter.pat")))
 			{
 				return;
 			}
 		} else if (naming.equalsIgnoreCase("date")) {
 			fileName = Config.get(cfgP+"file")+" - "+dateP+".pdf";
 		} else {
 			System.out.println("[ERR] creationDoc bad naming "+naming);
 			return;
 		}
 		fileName = fileName.replace("/", "_");
 		
 		// Dif
 		if (Config.getB("pdf.dif")) {
 			dif = leStage.getCode().matches(Config.get("pdf.dif.pattern"));
 		}
 		// Dossier
 		ArrayList<Integer> l; 
 		if (type == DOSSIER_FSS) {
 			l = Config.getIL(cfgP+"docs");
 			//System.out.println(l);
 		}
 		else {
 			l = new ArrayList<Integer>();
 			l.add(type);
 		}
 
 		try {
 			String pathDossier = expDir+Config.get(cfgP+"dir")+dateP;
 			new File(pathDossier).mkdir();
 			
 			// Margin
 			//int vMargin = Config.getI(cfgP+"margin.v");
 			//int hMargin = Config.getI(cfgP+"margin.h");
 			//creation du document et du fichier
 			Document doc = new Document(PageSize.A4,0,0,0,0);
 			FileOutputStream fichier = new FileOutputStream(pathDossier+"/"+fileName);
 			//ouverture du writer
 			PdfWriter.getInstance(doc, fichier);			
 			doc.open();
 				
 			for (int mType : l) {
 				cfgP = "pdf."+Config.get("pdf.doc."+mType)+".";
 				// Margin
 				int vMargin = Config.getI(cfgP+"margin.v");
 				int hMargin = Config.getI(cfgP+"margin.h");
 				doc.setMargins(hMargin,hMargin,vMargin,vMargin);
 				// Rotate
 				if (Config.getB(cfgP+"rotate")) {
 					doc.setPageSize(PageSize.A4.rotate());
 				}
 				doc.newPage();
 				
 				// Gnration du doc
 				switch(mType) {
 				case LISTE_STAGIAIRES: 
 					creationListStagiaire(leStage, doc, cfgP, false);
 					// Cas 4S
 					if (Config.getB(cfgP+"s2.group") 
 							&& leStage.getCode().matches(Config.get(cfgP+"s2.pattern"))
 							&& leStage.getCoStage() == null) {
 						System.out.println("ListStagiaire 4S Group "+leStage.getCode());
 						doc.newPage();
 						creationListStagiaire(leStage, doc, cfgP, true);
 					}
 					break;
 				case LISTE_EMARGEMENTS:
 					if (leStage.getSizeStagiaireList() != 0) {
 						if (dif)
 							creationListeEmargementDIF(leStage, doc, cfgP);
 						else {
 							creationListeEmargement(leStage, doc, cfgP, false);
 							if (Config.getB(cfgP+"s2.group") 
 									&& leStage.getCode().matches(Config.get(cfgP+"s2.pattern"))
 									&& leStage.getCoStage() == null) {
 								System.out.println("ListStagiaire 4S Group "+leStage.getCode());
 								doc.newPage();
 								creationListeEmargement(leStage, doc, cfgP, true);
 							}
 						}
 					} else 
 						creationListeEmargementVide(leStage, doc, cfgP);
 					break;
 				case FREP:
 					creationFREP(leStage, doc, cfgP);
 					break;
 				case AFFICHAGE_SALLE:
 					creationAffichageSalle(leStage, doc, cfgP);
 					break;
 				case CHECKLIST:
 					creationCheckListAdm(listeStage, doc, cfgP);
 					break;
 				case SURBOOK:
 					creationSurbook(leStage, doc, cfgP);
 					break;
 				case FEUILLE_ROUTE_FSS:
 					creationFeuilRouteFSS(fssModules, doc, cfgP);
 					break;
 				default:
 					break;
 				}
 			}			
 			//fermeture du writer
 			doc.close();
 		} catch (FileNotFoundException e) {
 		JOptionPane.showMessageDialog(null, "erreur de fichier: " +fileName, "Erreur", JOptionPane.ERROR_MESSAGE);
 		} catch (DocumentException e) {
 			JOptionPane.showMessageDialog(null, "erreur de document: " +e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	/**
 	 * creer le pdf pour la liste des stagiaires pour le stage pass en parametre
 	 * @param leStage Classe Stage : le stage
 	 * @param doc Classe Document : le document pdf
 	 * @param cfg Classe String : config string
 	 */
 	private static void creationListStagiaire(Stage leStage,Document doc, String cfg,
 			boolean group) throws DocumentException {
 
 		Common.setStatus("Cration Liste Stagiaires "+leStage.getCodeI());
 		Integer colNum = Config.getI(cfg+"colnum");
 		Integer rowNum = Config.getI(cfg+"rownum");
 		Boolean adapt = leStage.getCode().matches(Config.get(cfg+"s2.pat"));
 		String code = leStage.getCode();
 		
 		ArrayList<Stagiaire> sl = new ArrayList<Stagiaire>();
 		sl.addAll(leStage.getStagiaireList());
 		if (group) {
 			code = leStage.getCodeI();
 			System.out.println("ListStagiaire " + code + " : ");
 			for (Stage s : leStage.getCoStageList()) {
 				System.out.println("   + "+s.getCode());
 				sl.addAll(s.getStagiaireList());
 			}
 			Collections.sort(sl);
 			System.out.println(sl);
 		}
 
 		//construction du header
 		PdfPTable header = new PdfPTable(3);
 		header.setWidthPercentage(100);
 		
 		float[] widths = new float[] { 1f, 8f, 1f };
 		header.setWidths(widths);
 
 		// Le logo
 		PdfPCell cell = new PdfPCell(new Phrase(getCieImage(leStage.getCompagnie(), cfg))); 
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setVerticalAlignment(Element.ALIGN_TOP);
 		cell.setPadding(0);
 		cell.setBorder(0);
 		cell.setColspan(1);
 		header.addCell(cell);
 
 		//la date
 		cell = new PdfPCell(new Phrase(leStage.getDateStr(),fontB14));
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setBorder(0);
 		cell.setPadding(10);
 		header.addCell(cell);
 		
 		// sp
 		cell = new PdfPCell(new Phrase(" "));
 		cell.setBorder(0);
 		cell.setPadding(0);
 		header.addCell(cell);
 
 		//le code du stage
 		Phrase par = new Phrase(code,new Font(FontFamily.HELVETICA, 28, Font.BOLD));
 		cell = new PdfPCell(par);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setPadding(10);
 		cell.setBorder(0);
 		cell.setColspan(3);
 		header.addCell(cell);
 		
 
 		//le libelle du stage
 		par = new Phrase(leStage.getLibelle(),fontB14);
 		cell = new PdfPCell(par);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setPadding(10);
 		cell.setBorder(0);
 		cell.setColspan(3);
 		header.addCell(cell);
 		
 		//construction du center
 		PdfPTable center = new PdfPTable(colNum);
 		center.setWidthPercentage(100);
 		
 		widths = new float[] { 2f, 2f, 1f, 1f, 2f };
 		for (int i=1; i<=colNum; i++) { widths[i-1]=Config.getF(cfg+"col"+i+".w"); }
 		center.setWidths(widths);
 
 		cell.setColspan(0);
 		cell.setPadding(5);
 		cell.setBorder(0);
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		
 		int i;
 		for (i = 0; i < sl.size(); i++) {
 			for (int j = 0; j < colNum; j++) {
 				String info = sl.get(i).getInfo(j);
 				cell.setPhrase(new Phrase(info,new Font(FontFamily.HELVETICA, 11)));
 				center.addCell(cell);
 			}
 		}
 		
 		if (adapt) {
 			rowNum = rowNum - 4;
 		}
 		if(i<rowNum){
 			cell.setColspan(colNum);
 			cell.setPhrase(new Phrase(" "));
 			for (int ii = i; ii <= rowNum; ii++) {
 				center.addCell(cell);
 			}
 		}
 				
 		//construction du footer
 		PdfPTable footer = new PdfPTable(3);
 		footer.setWidthPercentage(95);
 				
 		if (adapt) {
 			cell = new PdfPCell();
 			PdfPTable w = new PdfPTable(new float[] { 1f, 3f, 1f});
 			w.setWidthPercentage(100);
 			cell.setPaddingBottom(5);
 			cell.setBorder(0);
 
 			cell.setPhrase(new Phrase(getResImage("WARN", cfg+"s2.")));
 			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 			w.addCell(cell);
 			
 			cell.setPhrase(new Phrase(Config.get(cfg+"s2"), fontB15));
 			w.addCell(cell);
 			
 			cell.setPhrase(new Phrase(getResImage("DOC", cfg+"s2.")));
 			w.addCell(cell);
 			
 			cell.addElement(w);
 			cell.setColspan(3);
 			cell.setBorder(15);
 			cell.setBackgroundColor(new BaseColor(Config.getI(cfg+"s2.bg")));
 			footer.addCell(cell);
 			
 			cell.setPhrase(new Phrase(" "));
 			cell.setBackgroundColor(new BaseColor(0xFFFFFF));
 			cell.setBorder(0);
 			footer.addCell(cell);
 			footer.addCell(cell);
 		}
 		cell = new PdfPCell(new Phrase(" "));
 		cell.setColspan(3);
 		cell.setBorder(0);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase("Formateur : "+leStage.getLeader(),fontB12));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		footer.addCell(cell);
 		
 		cell.setColspan(1);
 		cell.setPhrase(new Phrase(leStage.getFirstModule().getSalle(),fontB22));
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase(leStage.getFirstModule().getLibelle(),new Font(FontFamily.HELVETICA, 18, Font.BOLD)));
 		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase(leStage.getFirstModule().getHeureDebut(),fontB22));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		footer.addCell(cell);
 		
 		
 		//ajout des composants
 		doc.add(header);
 		doc.add(center);
 		doc.add(footer);
 
 	}//fin 
 	
 	/**
 	 * creer le pdf pour la liste d'emargement pour le stage pass en parametre
 	 * @param leStage
 	 * @param group 
 	 * @throws DocumentException 
 	 */
 	private static void creationListeEmargement(Stage leStage, Document doc, String cfg, 
 			boolean group) throws DocumentException{
 		Integer colNum = Config.getI(cfg+"colnum");
 		Integer rowNum = Config.getI(cfg+"rownum");
 		float[] widths;
 		String code = leStage.getCode();
 		
 		ArrayList<Stagiaire> sl = new ArrayList<Stagiaire>();
 		sl.addAll(leStage.getStagiaireList());
 		if (group) {
 			code = leStage.getCodeI();
 			System.out.println("ListeEmargement " + code + " : ");
 			System.out.println(sl);
 			for (Stage s : leStage.getCoStageList()) {
 				System.out.println("   + "+s.getCode());
 				sl.addAll(s.getStagiaireList());
 			}
 			Collections.sort(sl);
 			System.out.println(sl);
 		}
 
 		Common.setStatus("Cration Liste Emargement "+code);
 			
 		PdfPTable header = new PdfPTable(3);
 		header.setWidthPercentage(100);
 		
 		if (code.length() > 15)
 			widths = new float[] { 2.4f, 5.2f, 2.4f };
 		else 
 			widths = new float[] { 3.3f, 3.3f, 3.3f };
 		header.setWidths(widths);
 
 		// Head 1
 		Chunk logo = getCieImage(leStage.getCompagnie(), cfg);
 		Phrase p = new Phrase("");
 		if (logo.getWidthPoint() > 20) {
 			p.add("    "); p.add(logo); p.add(new Phrase("\n"));
 		}
 		p.add(new Phrase(leStage.getLibelle(), fontB10));
 		PdfPCell cell = new PdfPCell(p);
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		//cell.setRowspan(2);
 		cell.setBorder(0);
 		header.addCell(cell);
 		
 		cell.setPhrase(new Phrase(code,fontB22));
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		cell.setPadding(6);
 		header.addCell(cell);
 		//cell.setRowspan(1);
 
 		cell.setPhrase(new Phrase(leStage.getDateStr()+"   ",fontB12));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		cell.setPadding(2);
 		header.addCell(cell);
 				
 		cell.setPhrase(new Phrase(Config.get(cfg+"s1"),fontB10));
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		header.addCell(cell);		
 		
 		cell.setPhrase(new Phrase(Config.get(cfg+"l1"),fontB10));
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		header.addCell(cell);
 		
 		cell.setPhrase(new Phrase(Config.get(cfg+"h1") + 
 				leStage.getFirstModule().getHeureDebut() + "  " +
 				leStage.getLastModule().getHeureFin(),
 				fontB10));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		header.addCell(cell);		
 		
 		cell.setPhrase(new Phrase(" "));
 		cell.setColspan(3);
 		header.addCell(cell);
 		cell.setColspan(1);
 		
 		
 		//formation du center
 		PdfPTable center = new PdfPTable(colNum);
 		//center.setWidthPercentage(95);
 
 		widths = new float[] { 1f, 8f, 1.5f, 4f, 1.5f };
 		for (int i=1; i<=colNum; i++) { widths[i-1]=Config.getF(cfg+"col"+i+".w"); }
 		center.setWidths(widths);
 		
 		//cell = new PdfPCell(new Phrase("N",fontB10)); cell.setBorder(0);
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 
 		for (int i=1; i<=colNum; i++) {
 			cell.setPhrase(new Phrase(Config.get(cfg+"col"+i+".str"),fontB10));
 			center.addCell(cell);
 		}
 
 		int i;
 		// TODO SG font constante
 		for (i = 0; i < sl.size(); i++) {
 				cell.setPhrase(new Phrase(""+(i+1), font));
 				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 				cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
 				center.addCell(cell);
 				cell.setPadding(1.5f);
 				// String str = "";
 				// TODO  SG liste statique
 				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 				PdfPTable stgCell = new PdfPTable(10);
 				center.setWidthPercentage(100);
 				
 				//str = String.format("%-20s%-20s\n%25s%10s%10s", 
 				cell.setBorder(0);
 				cell.setColspan(5);
 				cell.setPhrase(new Phrase(sl.get(i).getInfo(0), fontB10));
 				stgCell.addCell(cell);
 				cell.setPhrase(new Phrase(sl.get(i).getInfo(1), font));
 				stgCell.addCell(cell);
 
 				cell.setColspan(1);
 				cell.setPhrase(new Phrase(" ", font));
 				stgCell.addCell(cell);
 
 				cell.setColspan(2);
 				cell.setPhrase(new Phrase(sl.get(i).getInfo(3), font));
 				stgCell.addCell(cell);
 
 				cell.setColspan(5);
 				cell.setPhrase(new Phrase(sl.get(i).getInfo(4), font));
 				stgCell.addCell(cell);
 
 				cell.setColspan(2);
 				cell.setPhrase(new Phrase(sl.get(i).getInfo(2), font));
 				stgCell.addCell(cell);
 				
 				cell.setColspan(1);
 				PdfPCell c = new PdfPCell(stgCell);
 				c.setPadding(1.5f);
 				center.addCell(c);
 
 				//presence
 				center.addCell(" ");
 				//emargement
 				center.addCell(" ");
 				//isvsmp
 				center.addCell(" ");
 		}
 		if(i<=rowNum){
 			cell.setPhrase(new Phrase(" \n ", font));
 			cell.setColspan(colNum);
 			for (int j = i; j < rowNum; j++) {
 					center.addCell(cell);
 			}//fin pour
 		}//fin si
 		
 		// Footer
 		PdfPTable footer = getFooter(leStage);
 		
 		//ajout des composants
 		doc.add(header);
 		doc.add(center);
 		doc.add(footer);
 			
 	}//fin
 	
 	private static PdfPTable getFooter(Stage leStage) {
 		PdfPTable footer = new PdfPTable(4);
 		footer.setWidthPercentage(90);
 		
 		PdfPCell cell = new PdfPCell(new Phrase("Nombre de stagiaires :",fontB10));
 		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		cell.setPadding(3);
 		cell.setRowspan(2);
 		cell.setBorder(0);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase("Prvus",fontB10));
 		cell.setColspan(1); cell.setRowspan(1);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setBorder(Rectangle.RIGHT | Rectangle.BOTTOM);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase("Prsents",fontB10));
 		cell.setBorder(Rectangle.BOTTOM);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase("Maxi Prsents",fontB10));
 		cell.setBorder(Rectangle.LEFT | Rectangle.BOTTOM);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase(""+leStage.getSizeStagiaireList(),fontB10));
 		cell.setBorder(Rectangle.RIGHT | Rectangle.TOP);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase(" ", fontB10));
 		cell.setBorder(Rectangle.TOP);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase(""+leStage.getMaxiPresent(), fontB10));
 		cell.setBorder(Rectangle.LEFT | Rectangle.TOP);
 		footer.addCell(cell);
 		
 		//formateur
 		cell.setBorder(Rectangle.NO_BORDER);
 		cell.setPhrase(new Phrase("", font9));
 		cell.setColspan(4);
 		footer.addCell(cell);
 		
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		cell.setColspan(1);
 		cell.setPhrase(new Phrase("Formateurs :", fontB10));
 		footer.addCell(cell);
 		
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setPhrase(new Phrase(" "));
 		cell.setBorder(Rectangle.BOTTOM);
 		footer.addCell(cell);
 
 		cell.setPhrase(new Phrase(leStage.getLeader(), fontB10));
 		cell.setBorder(Rectangle.NO_BORDER);
 		footer.addCell(cell);
 		
 		cell.setPhrase(new Phrase(" "));
 		footer.addCell(cell);
 
 		return footer;
 	}
 	
 	private static void creationListeEmargementDIF(Stage leStage, Document doc, String cfg) throws DocumentException
 	{			
 		cfg = cfg+"dif.";
 		Integer colNum = Config.getI(cfg+"colnum");
 		Integer rowNum = Config.getI(cfg+"rownum");
 
 		Common.setStatus("Cration Liste Emargement DIF "+leStage.getCodeI());
 
 		//creation du document		
 		PdfPTable header = new PdfPTable(3);
 		header.setWidthPercentage(100);
 		
 		// Head 1
 		PdfPCell cell = new PdfPCell(new Phrase(leStage.getLibelle(), fontB10));
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		cell.setBorder(0);
 		header.addCell(cell);
 		
 		cell.setPhrase(new Phrase(leStage.getCode(),fontB22));
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setPadding(5);
 		header.addCell(cell);
 
 		cell.setPhrase(new Phrase(leStage.getDateStr()+"   ",fontB12));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		cell.setPadding(2);
 		header.addCell(cell);
 
 		// Dif + emargement + horaires
 		cell.setPhrase(new Phrase(Config.get(cfg+"s1"),fontB10));
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		header.addCell(cell);		
 		
 		cell.setPhrase(new Phrase(Config.get(cfg+"l1"),fontB10));
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		header.addCell(cell);
 
 		cell.setPhrase(new Phrase(Config.get(cfg+"h1") + 
 				leStage.getFirstModule().getHeureDebut() + "  " +
 				leStage.getLastModule().getHeureFin(),
 				fontB10));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		header.addCell(cell);		
 
 		cell.setPhrase(new Phrase(" "));
 		cell.setColspan(3);
 		header.addCell(cell);
 		cell.setColspan(1);
 
 		//formation du center
 		PdfPTable center = new PdfPTable(colNum);
 		center.setWidthPercentage(98);
 
 		float[] widths = new float[] { 0.8f, 8f, 1.5f, 4f, 4f, 1f, 1.5f };
 		for (int i=1; i<=colNum; i++) { widths[i-1]=Config.getF(cfg+"col"+i+".w"); }
 		center.setWidths(widths);
 		
 		//cell.setPaddingTop(0);
 		//cell.setPaddingBottom(0);
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		// Col Head
 		for (int i=1; i<=colNum; i++) {
 				cell.setPhrase(new Phrase(Config.get(cfg+"col"+i+".str"),fontB10));
 				center.addCell(cell);				
 		}
 
 		cell.setPadding(1.5f);
 		int i;
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
 				PdfPCell c = new PdfPCell(stgCell);
 				c.setPadding(1.5f);
 				center.addCell(c);
 				
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
 		if(i<rowNum){
 			cell.setBorder(0);
 			cell.setPhrase(new Phrase(" \n ", font));
 			cell.setColspan(colNum);
 			for (int j = i; j < rowNum; j++) {
 				center.addCell(cell);
 			}//fin pour
 		}//fin si
 
 		// DIF
 		PdfPTable footer0 = new PdfPTable(1);
 		footer0.setWidthPercentage(95);
 		cell = new PdfPCell(new Phrase(Config.get(cfg+"s2"), font9));
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		cell.setPadding(10);
 		cell.setBorder(0);
 		footer0.addCell(cell);
 		cell.setPhrase(new Phrase(""));
 		footer0.addCell(cell);
 		
 		// Footer
 		PdfPTable footer = getFooter(leStage);
 							
 		//ajout des composants
 		doc.add(header);
 		doc.add(center);
 		doc.add(footer0);
 		doc.add(footer);
 
 	}//fin
 
 	/**
 	 * creer le pdf pour la liste d'emargement pour le stage pass en parametre
 	 * @param leStage
 	 * @param doc 
 	 * @param cfg 
 	 * @throws DocumentException 
 	 */
 	private static void creationListeEmargementVide(Stage leStage, Document doc, String cfg) throws DocumentException{
 				
 		Common.setStatus("Cration Liste Emargememt "+leStage.getCodeI());
 
 		//creation du document
 		
 		PdfPTable header = new PdfPTable(2);
 		header.setWidthPercentage(100);
 
 		PdfPCell cell = new PdfPCell(new Phrase(getCieImage(leStage.getCompagnie(), cfg))); 
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setVerticalAlignment(Element.ALIGN_TOP);
 		cell.setBorder(0);
 		header.addCell(cell);
 	
 		cell = new PdfPCell(new Phrase(leStage.getDateStr(),fontB12));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		cell.setBorder(0);
 		header.addCell(cell);
 
 		Phrase par = new Phrase(leStage.getCode(),new Font(FontFamily.HELVETICA, 24, Font.BOLD));
 		cell = new PdfPCell(par);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setPadding(5);
 		cell.setBorder(0);
 		cell.setColspan(2);
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
 		
 		// Footer
 		PdfPTable footer = getFooter(leStage);
 		
 		//ajout des composants
 		doc.add(header);
 		doc.add(new Paragraph("        "));
 		doc.add(center);
 		doc.add(new Paragraph("        "));
 		doc.add(footer);
 				
 	}//fin
 	
 	/**
 	 * creer les pdf des feuilles de routes FSS
 	 */
 	public static ArrayList<FSS_Modules> getFSSModules(ArrayList<Stage> stageList){
 		
 		ArrayList<FSS_Modules> list= new ArrayList<FSS_Modules>();
 		ArrayList<String> fssList = new ArrayList<String>();
 		
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
 			FSS_Modules fss = new FSS_Modules(nomFss);
 			fss.modules = new ArrayList<Module>();
 			for (Stage leStage : stageList) {
 				for (Module module : leStage.getModuleList()) {
 					if(module.getNomLeader().equalsIgnoreCase(nomFss)){
 						fss.modules.add(module);
 					}else{
 						if(module.getNomAide().equalsIgnoreCase(nomFss)){
 							fss.modules.add(module);
 						}
 					}
 				}//finpour
 			}//finpour
 			if(fss.modules.size()!=0){
 				list.add(fss);
 			}
 		}//finpour
 		return list;
 		
 	}//fin creationAllFRFSS()
 	
 	/**
 	 * creer le pdf pour la feuille de route pour les FSS
 	 * @param cfg 
 	 * @param Nom
 	 * @param moduleList
 	 * @throws DocumentException 
 	 */
 	private static void creationFeuilRouteFSS(FSS_Modules fss, Document doc, String cfg) throws DocumentException{
 		
 		String Nom = fss.nomFSS;
 		ArrayList<Module> moduleList = fss.modules;
 		String date = moduleList.get(0).getDate();
 		
 		// tri selon l'heure de dbut
 		Collections.sort(moduleList, new ModuleStartComparator());
 
 		Common.setStatus("Cration Feuille de Route FSS " + Nom);
 
 		PdfPTable header = new PdfPTable(3);
 		header.setWidthPercentage(100);
 		PdfPCell cell = new PdfPCell(new Phrase("Stages du jour", fontB15));
 		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 		cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
 		cell.setBorder(0); cell.setPadding(10);
 		header.addCell(cell);
 		cell.setPhrase(new Phrase(Nom, fontB22));
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		header.addCell(cell);
 		cell.setPhrase(new Phrase(date, fontB15));
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		header.addCell(cell);
 		
 		PdfPTable center = new PdfPTable(5);
 		PdfPCell defCell = center.getDefaultCell();
 		defCell.setBorder(0);
 		defCell.setPadding(9);
 		
 		center.setWidthPercentage(100);
 		float[] widths = new float[] { 1f, 1f, 1f, 1.3f, 1.2f };
 		center.setWidths(widths);
 
 		center.addCell(new Phrase("Code Stage",fontB12));
 		center.addCell(new Phrase("Leader/Aide",fontB12));
 		center.addCell(new Phrase("Horaire",fontB12));
 		center.addCell(new Phrase("Module",fontB12));
 		center.addCell(new Phrase("Salle",fontB12));
 		
 		for (Module module : moduleList) {
 			center.addCell(new Phrase(module.getCodeStage(), font12));
 			String lead = "Leader";
 			String aide = module.getNomAide();
 			if(! module.getNomLeader().equalsIgnoreCase(Nom)){
 				lead = "Aide";
 				aide = module.getNomLeader();
 			}
 			Phrase phrs = new Phrase(lead, font12);
 			if (!aide.equals("")) {
 				phrs.add(new Phrase("\n"+aide, fontI9));
 			}
 			center.addCell(phrs);			
 			
 			center.addCell(new Phrase(module.getHeureDebut()+" - "+module.getHeureFin(),font12));
 			center.addCell(new Phrase(module.getLibelle(),font12));
 			center.addCell(new Phrase(module.getSalle(),font12));
 		}
 		
 		doc.add(header);
 		doc.add(new Phrase("  "));
 		doc.add(center);
 		
 		if (Config.getB(cfg+"circulation")) {
 			// Liste des stages
 			ArrayList<Stage> stageList = new ArrayList<Stage>();
 			for (Module module : moduleList) {
 				if (!stageList.contains(module.getStage())) {
 					stageList.add(module.getStage());
 				}
 			}
 
 			header = new PdfPTable(2);
 			header.setWidthPercentage(100);
 			cell = new PdfPCell(new Phrase("\n"));
 			cell.setPadding(2); cell.setBorder(2);
 			header.addCell(cell); header.addCell(cell);
 		
 			for (Stage stage : stageList) {
 				//System.out.println(stage.getCodeI());
 				cell = new PdfPCell(new Phrase("\n"));
 				cell.setPadding(2); cell.setBorder(0);
 				header.addCell(cell); header.addCell(cell);
 				
 				cell = new PdfPCell(new Phrase("Stage : "+stage.getCode(), fontB12));
 				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 				cell.setPadding(2); cell.setBorder(0); header.addCell(cell);
 				
 				cell = new PdfPCell(new Phrase("Date : "+ date, fontB12));
 				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 				cell.setPadding(2); cell.setBorder(0); header.addCell(cell);
 				
 				center = new PdfPTable(6);
 				center.setWidthPercentage(100);
 				widths = new float[] { 1f, 1f, 2.5f, 2.5f, 2.5f, 3f };
 				center.setWidths(widths);
 				defCell = center.getDefaultCell();
 				//defCell.setBorder(7);
 				defCell.setPadding(3);
 				
 				center.addCell(new Phrase("Dbut", fontB9));
 				center.addCell(new Phrase("Fin", fontB9));
 				center.addCell(new Phrase("Activit", fontB9));
 				center.addCell(new Phrase("Leader", fontB9));
 				center.addCell(new Phrase("Aide", fontB9));
 				center.addCell(new Phrase("Moyen", fontB9));
 
 				for (Module m : stage.getModuleList()) {
 					center.addCell(new Phrase(m.getHeureDebut(), font9));
 					center.addCell(new Phrase(m.getHeureFin(), font9));
 					center.addCell(new Phrase(m.getLibelle(), font9));
 					center.addCell(new Phrase(m.getNomLeader(), m.getNomLeader().equals(Nom) ? fontB9 : font9));
 					center.addCell(new Phrase(m.getNomAide(), m.getNomAide().equals(Nom) ? fontBI9 :fontI9));
 					center.addCell(new Phrase(m.getSalle(), font9));
 				}
 				
 				//doc.add(new Phrase(" "));
 				doc.add(header);
 				doc.add(center);
 
 				header = new PdfPTable(2);
 				header.setWidthPercentage(100);
 			}
 		}
 	}//fin
 	
 	/**
 	 * creer le pdf pour le FREP du stage pass en parametre
 	 * @param leStage
 	 * @param cfg 
 	 * @throws DocumentException 
 	 */
 	private static void creationFREP(Stage leStage, Document doc, String cfg) throws DocumentException{
 				
 		Common.setStatus("Cration FREP "+leStage.getCode());
 		
 		//construction du header
 		PdfPTable header = new PdfPTable(1);
 		header.setWidthPercentage(100);
 		PdfPCell defaultCell = header.getDefaultCell();
 		defaultCell.setBorder(0);
 		defaultCell.setPadding(5);
 		defaultCell.setHorizontalAlignment(Element.ALIGN_CENTER);
 	
 		//la date
 		// = new PdfPCell();
 		header.addCell(new Phrase(leStage.getDateStr(),fontB14));
 		//le code du stage
 		header.addCell(new Phrase(leStage.getCode(), fontB28));
 		//l'intitul
 		header.addCell(new Phrase("Fiche rcaputulative de fin de formation", fontB20));
 		
 
 		float[] widths = new float[] { 4f, 1f };
 		
 		PdfPTable center = new PdfPTable(2);
 		center.setWidths(widths);
 		center.setWidthPercentage(100);
 		defaultCell = center.getDefaultCell();
 		defaultCell.setBorder(0);
 		defaultCell.setPadding(3);
 		defaultCell.setHorizontalAlignment(Element.ALIGN_LEFT);
 
 		PdfPCell OSOcell = new PdfPCell(new Phrase("Oui / Sans objet *"));
 		OSOcell.setBorder(0); OSOcell.setPadding(3);
 		OSOcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 
 		PdfPCell lineCell = new PdfPCell(new Phrase("\n", font9));
 		lineCell.setBorder(0); lineCell.setPadding(0);
 
 		center.addCell(lineCell); center.addCell(lineCell);
 		center.addCell(new Phrase("Conformment au programme, les exercices pratiques suivant ont t raliss :")); 
 		center.addCell(lineCell);
 		center.addCell(lineCell);		center.addCell(lineCell);
 
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
 		
 		for (String string : list) {
 			center.addCell(new Phrase(string));
 			center.addCell(OSOcell);
 		}
 		
 		center.addCell(new Phrase("    Visualisation sur avion                                            Immat. : _ _ _ _ _ _ _"));
 		OSOcell.setPhrase(new Phrase("Sans Objet *"));
 		center.addCell(OSOcell);
 		
 		center.addCell(new Phrase("    Visualisation virtuelle                                        Type-avion : _ _ _ _ _ _ _"));
 		center.addCell(OSOcell);
 	
 		center.addCell(lineCell); center.addCell(lineCell);
 						
 		defaultCell.setPadding(5);
 
 		center.addCell(new Phrase("Des stagiaires ont-ils echou aux exercices pratiques ?"));
 		OSOcell.setPhrase(new Phrase("Oui / Non *"));
 		center.addCell(OSOcell);
 
 		center.addCell(new Phrase("      Si oui, lesquels : _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"));
 		center.addCell(lineCell);
 
 		center.addCell(new Phrase("      Sur quel exercice : _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"));
 		center.addCell(lineCell);
 
 		center.addCell(new Phrase("Cette information a-t-elle t transmise  IS.VS MP ?"));
 		center.addCell(OSOcell);
 		
 		center.addCell(lineCell); center.addCell(lineCell);
 
 		center.addCell(new Phrase("Des stagiaires sont-ils partis en cours de formation ?"));
 		center.addCell(OSOcell);
 
 		center.addCell(new Phrase("      Si oui, lesquels :  _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"));
 		center.addCell(lineCell);
 		
 		center.addCell(lineCell); center.addCell(lineCell);
 
 		center.addCell(new Phrase("Un des exercices a-t-il t ralis en mode drogatoire ?"));
 		center.addCell(OSOcell);
 
 		center.addCell(new Phrase("      Si oui, lesquels :  _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"));
 		center.addCell(lineCell);
 
 		center.addCell(lineCell); center.addCell(lineCell);
 
 		center.addCell(new Phrase("La dure de formation s'est-elle carte de plus de 15mn du temps prvu ?"));
 		center.addCell(OSOcell);
 
 		center.addCell(new Phrase("      Si oui, pourquoi :  _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"));
 		center.addCell(lineCell);
 
 		center.addCell(lineCell); center.addCell(lineCell);
 
 		center.addCell(new Phrase("Voir Remarques / particularits du stage au verso (AT, problmes rencontrs, ...) \n \n"));
 		center.addCell(lineCell);
 
 		center.addCell(lineCell); center.addCell(lineCell);
 		center.addCell(lineCell); center.addCell(lineCell);
 		center.addCell(lineCell); center.addCell(lineCell);
 
 		center.addCell(new Phrase("Nom du FSS leader :  " +  leStage.getLeader() + "  / _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"));
 		center.addCell(lineCell);
 		
 		center.addCell(new Phrase("Signature du FSS leader :"));
 		center.addCell(new Phrase("Visa du CPO :"));
 		
 		center.addCell(new Phrase("\n\n\n* Rayer la mention inutile", font9));
 		center.addCell(lineCell);
 		
 		doc.add(header);
 		doc.add(center);
 	} 
 
 	/**
 	 * creer le pdf pour le surbook du stage pass en parametre
 	 * @param leStage
 	 * @param cfg 
 	 * @throws DocumentException 
 	 */
 	private static void creationSurbook(Stage leStage, Document doc, String cfg) throws DocumentException{
 		
 		Common.setStatus("Cration Surbook "+leStage.getCodeI());
 			
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
 		Image img;
 		try {
 			img = Image.getInstance(Config.getRes("Airfrance.jpg"));
 			cell = new PdfPCell(img);
 		} catch (BadElementException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
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
 		phrs = new Phrase(leStage.getLibelle(),new Font(FontFamily.HELVETICA,15,Font.BOLD));
 		cell = new PdfPCell(phrs);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setBorder(0);
 		title.addCell(cell);
 		phrs = new Phrase(" ");
 		cell = new PdfPCell(phrs);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setBorder(0);
 		title.addCell(cell);
 		phrs = new Phrase("- Stagiaire  l'heure refus cause surbook -",new Font(FontFamily.HELVETICA,18,Font.BOLD));
 		cell = new PdfPCell(phrs);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		title.addCell(cell);
 		
 		doc.add(title);
 		
 		phrs = new Phrase("\n\nNom du stagiaire : _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"
 							,new Font(FontFamily.HELVETICA,15));
 		doc.add(phrs);
 		
 		phrs = new Phrase("matricule : _   _   _   _   _   _   _   _\n\n"
 							,new Font(FontFamily.HELVETICA, 15));
 		doc.add(phrs);
 		
 		phrs = new Phrase("Madame, Monsieur,\n\n\n" +
 				"Les programmes d'instruction dposs  la DGAC prvoient un nombre maximum de stagiaires admissibles" +
 				"en stage ( 16 sur les stages de spcialisation, 18 sur les stages de maintien des comptences).\n\n" +
 				"Afin d'optimiser l'utilisation des moyens d'instruction, les services dits de production" +
 				"PNC sont amens  grer des stages avec surbook.\n\n" +
 				"Le nombre de stagiaires prsents aujourd'hui sur le vtre a conduit votre formateur  en limiter l'accs." +
 				"Il n'existe en effet aucune tolrance sur le nombre maximum de stagiaires admissibles.\n\n" +
 				"le choix de la personne invite  se faire inscrire sur un autre stage se fait :"
 				,new Font(FontFamily.HELVETICA,12));
 		doc.add(phrs);
 		
 		List list = new List(false);
 		list.add(new ListItem(new Phrase("conformment  l'interligne 04-079 du 4 octobre 2004",new Font(FontFamily.HELVETICA,12))));
 		list.add(new ListItem(new Phrase("selon les indications des services de produstion PNC",new Font(FontFamily.HELVETICA,12))));
 		doc.add(list);
 		
 		phrs = new Phrase("\nNous vous remercions de votre comprhension et votre prions de bien vouloir prendre contact" +
 				"avec la permanence oprationnelle situe Salle 003 au RDC du BEPN afin de faire enregistrer votre prsence et" +
 				"vous rendre ensuite au suivi planning muni du prsent document.\n\n\n"
 				,new Font(FontFamily.HELVETICA,12));
 		doc.add(phrs);
 		
 		PdfPTable footer = new PdfPTable(2);
 		footer.setWidthPercentage(100);
 		cell = new PdfPCell(new Phrase("Signature du FSS :",new Font(FontFamily.HELVETICA, 13)));
 		cell.setBorder(0);
 		footer.addCell(cell);
 		cell = new PdfPCell(new Phrase("Visa du CPO :",new Font(FontFamily.HELVETICA, 13)));
 		cell.setBorder(0);
 		footer.addCell(cell);
 		doc.add(footer);
 						
 	}//fin creationSurbook(
 
 	/**
 	 * creer le pdf pour la checkListe pour le ple Administratif
 	 * @param cfg 
 	 * @param date
 	 * @throws DocumentException 
 	 */
 	private static void creationCheckListAdm(ArrayList<Stage> stageList, Document doc, String cfg) throws DocumentException{
 		
 		String date = stageList.get(0).getDateStr();
 		
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
 		
 		Common.setStatus("Cration C/L Admin du "+date);
 
 		//doc.newPage();
 		//PdfWriter.getInstance(doc, fichier);
 		//doc.open();
 		
 		PdfPTable header = new PdfPTable(2);
 		header.setWidthPercentage(100);
 		
 		Phrase phrs = new Phrase("RETOUR Listes Stagiaires et Emargements", fontB15);
 		PdfPCell cell = new PdfPCell(phrs);
 		cell.setBorder(0);
 		header.addCell(cell);
 		phrs = new Phrase(date, fontB15);
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
 		
 		phrs = new Phrase("ABS",fontB9);
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("R/Prod",fontB9);
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("Dbarqus",fontB9);
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("ITRI",fontB9);
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("SCAN",fontB9);
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("Attestations",fontB9);
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("SCAN",fontB9);
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("PREVUS",fontB9);;
 		cell = new PdfPCell(phrs);
 		cell.setBackgroundColor(lightGray);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		tableau.addCell(cell);
 		
 		phrs = new Phrase("PRESENTS",fontB9);
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
 		
 		
 	}//fin creationCheckListAdm()
 	
 
 	/**
 	 * creer le pdf pour le surbook du stage pass en parametre
 	 * @param leStage
 	 * @param cfg 
 	 * @throws DocumentException 
 	 */
 	private static void creationAffichageSalle(Stage leStage, Document doc, String cfg) throws DocumentException{
 		
 		SimpleDateFormat fmt = new SimpleDateFormat("EEEE d MMMM yyyy");		
 		String code = leStage.getCode();
 
 		Common.setStatus("Cration Affichage Salle "+leStage.getCodeI());
 
 		PdfPTable header = new PdfPTable(3);
 		float[] widths = new float[] { 3f, 4f, 3f };
 		header.setWidths(widths);
 		header.setWidthPercentage(95);
 
 		// Logo
 		PdfPCell cell = new PdfPCell(new Phrase(getCieImage(leStage.getCompagnie(), cfg))); 
 		cell.setBorder(0);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setVerticalAlignment(Element.ALIGN_TOP);
 		cell.setMinimumHeight(60);
 		header.addCell(cell);
 		
 		cell = new PdfPCell(new Phrase(fmt.format(leStage.getDateDt()), fontB20));
 		cell.setBorder(0);
 		cell.setPadding(5);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		header.addCell(cell);
 
 		cell = new PdfPCell(new Phrase(""));
 		cell.setBorder(0);
 		header.addCell(cell);
 
 		doc.add(header);
 		
 		PdfPTable center = new PdfPTable(1);
 		center.setWidthPercentage(95);
 		
 		Phrase phrs1 = new Phrase("", fontB20);
 		PdfPCell cell1 = new PdfPCell(phrs1);
 		cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		cell1.setBorder(0);
 		cell1.setMinimumHeight(120);
 
 		Phrase phrs2 = new Phrase(leStage.getCode(),fontB140);
 		PdfPCell cell2 = new PdfPCell(phrs2);
 		cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		cell2.setBorder(0);
 		cell2.setMinimumHeight(200);
 		
 		Phrase phrs3 = new Phrase(leStage.getLibelle(), fontB20);
 		PdfPCell cell3 = new PdfPCell(phrs3);
 		cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
 		cell3.setBorder(0);
 		cell3.setMinimumHeight(100);
 	
 		if (code.length() > 10) {
 			cell1.setMinimumHeight(50);
 			phrs2 = new Phrase(leStage.getCode(),fontB95); cell2.setPhrase(phrs2);
 			cell2.setMinimumHeight(300);
 			cell3.setMinimumHeight(50);
 		}
 
 		center.addCell(cell1);
 		center.addCell(cell2);
 		center.addCell(cell3);
 
 		doc.add(center);
 								
 		PdfPTable footer = new PdfPTable(1);
 		footer.setWidthPercentage(95);
 		cell = new PdfPCell(new Phrase(leStage.getFirstModule().getSalle(), fontB20));
 		cell.setBorder(0);
 		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
 		footer.addCell(cell);
 		doc.add(footer);
 						
 	}//fin
 
 	private static Chunk getCieImage(String cie, String cfg) throws BadElementException {
 		Chunk chunk = new Chunk("");
 		int maxHeight = Config.getI(cfg+"logo.h");
 		int maxWidth = Config.getI(cfg+"logo.w");
 		if (!Config.getB(cfg+"logo.filter") 
 				|| !cie.matches(Config.get(cfg+"logo.filter.pat"))) {
 			try {
 				Image img = Image.getInstance(Config.get("data.logos")+"Orig/"+cie+".jpg");
 				float r = img.getWidth()/img.getHeight();
 				int w = (int) Math.min(img.getWidth(), maxWidth);
 				int h = (int) Math.min(w/r, maxHeight);
 				w = (int) (h*r);
 				img.scaleAbsoluteWidth(w);
 				img.scaleAbsoluteHeight(h);
 				//img.scaleToFit(1000, height);
 				if (Config.getB(cfg+"logo.align"))
 					chunk = new Chunk(img,0,-h);
 				else
 					chunk = new Chunk(img,0,0);
 			} catch (FileNotFoundException e) {
 			} catch (MalformedURLException e) {
 			} catch (IOException e) {
 				System.out.println("[WRN] "+e.toString());
 			}
 		}
 		return chunk;
 	}
 	private static Chunk getResImage(String res, String cfg) throws BadElementException {
 		Chunk chunk = new Chunk("");
 		int maxHeight = Config.getI(cfg+"logo.h");
 		int maxWidth = Config.getI(cfg+"logo.w");
 		try {
 			Image img = Image.getInstance(Config.getRes(res+".png"));
 			img.scaleToFit(maxWidth, maxHeight);
 			chunk = new Chunk(img,0,0);
 		} catch (MalformedURLException e) {
 		} catch (IOException e) {
 			System.out.println("[WRN] "+e.toString());
 		}
 		return chunk;
 	}
 }//fin class
