 
 package eionet.meta.exports.pdf;
 
 import eionet.meta.*;
 import eionet.util.Util;
 
 import java.sql.*;
 import java.util.*;
 import java.io.*;
 import java.awt.Color;
 
 import com.lowagie.text.*;
 import com.lowagie.text.pdf.*;
 
 public class DstPdfGuideline extends PdfHandout {
     
     private int vsTableIndex = -1;
     private int elmCount = 0;
     
     private String dsName = "";
 	private String dsVersion = "";
 	private Hashtable tblElms = new Hashtable();
 	private Hashtable tblNames = new Hashtable();
 	private Hashtable submitOrg = new Hashtable();
 	
 //	private Chapter chapter = null;
     
     public DstPdfGuideline(Connection conn, OutputStream os){
         searchEngine = new DDSearchEngine(conn);
         this.os = os;
 		setShowedAttributes();
     }
     
     public void write(String dsID) throws Exception {
         
         if (Util.voidStr(dsID))
             throw new Exception("Dataset ID not specified");
         
         Dataset ds = searchEngine.getDataset(dsID);
         if (ds == null)
             throw new Exception("Dataset not found!");
             
         Vector v = searchEngine.getSimpleAttributes(dsID, "DS");
         ds.setSimpleAttributes(v);
         v = searchEngine.getComplexAttributes(dsID, "DS");
         ds.setComplexAttributes(v);
         v = searchEngine.getDatasetTables(dsID);
         DsTable tbl = null;
         for (int i=0; v!=null && i<v.size(); i++){
         	tbl = (DsTable)v.get(i);
         	tbl.setSimpleAttributes(
         			searchEngine.getSimpleAttributes(tbl.getID(), "T"));
         }
         ds.setTables(v);
         
         addParameter("dstID", dsID);
         
         write(ds);
     }
     
     private void write(Dataset ds) throws Exception {
         
         if (ds == null)
             throw new Exception("Dataset object is null!");
         
         String s = ds.getAttributeValueByShortName("Name");
         dsName = Util.voidStr(s) ? ds.getShortName() : s;
         
 		s = ds.getAttributeValueByShortName("ETCVersion");
 		dsVersion = Util.voidStr(s) ? ds.getVersion() : s;
         
         String title = "General information for " + dsName + " dataset";
 		String nr = sect.level(title, 1);
 		nr = nr==null ? "" : nr + " ";
 		        
         Paragraph prg = new Paragraph();
 		prg.add(new Chunk(nr + "General information for " + dsName,
 											Fonts.get(Fonts.HEADING_1)));  
         prg.add(new Chunk(" dataset",
         			FontFactory.getFont(FontFactory.HELVETICA, 16)));
         
         //chapter = new Chapter(prg, 1);
         
         // add the dataset chapter to the document
         // elmCount = super.addElement(chapter);
 		elmCount = addElement(prg);
         
         addElement(new Paragraph("\n"));
         
         // write simple attributes
         addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));
         
         Vector attrs = ds.getSimpleAttributes();
 
         Hashtable hash = new Hashtable();
         hash.put("name", "Short name");
         hash.put("value", ds.getShortName());
 		attrs.add(0, hash);
         
         String version = ds.getVersion();
         if (!Util.voidStr(version)){
             hash = new Hashtable();
             hash.put("name", "Version");
             hash.put("value", version);
 			attrs.add(1, hash);
         }
         
 		showedAttrs.remove("Name");
 		showedAttrs.add(0, "Short name");
         addElement(PdfUtil.simpleAttributesTable(attrs, showedAttrs));
         addElement(new Phrase("\n"));
         
         // write complex attributes, one table for each
 		Vector v = ds.getComplexAttributes();
 		if (v!=null && v.size()>0){
             
 			DElemAttribute attr = null;
 			for (int i=0; i<v.size(); i++){
 				attr = (DElemAttribute)v.get(i);
 				attr.setFields(searchEngine.getAttrFields(attr.getID()));
 			}
             
 			/*for (int i=0; i<v.size(); i++){
 				addElement(PdfUtil.complexAttributeTable((DElemAttribute)v.get(i)));
 				addElement(new Phrase("\n"));
 			}*/
 		}
 		
 		this.submitOrg = ds.getCAttrByShortName("SubmitOrganisation");
 		
 		/* write image attributes
 		Element imgAttrs = PdfUtil.imgAttributes(attrs, vsPath);
 		if (imgAttrs!=null){
 			addElement(new Phrase("\n"));
 			addElement(imgAttrs);
 		}*/
         
         // write tables list
         title = "Overview of " + dsName + " dataset tables";
 		nr = sect.level(title, 1);
 		nr = nr==null ? "" : nr + " ";
 		prg = new Paragraph();
 		prg.add(new Chunk(nr,
 					 FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
 		prg.add(new Chunk("Overview of ",
 					 FontFactory.getFont(FontFactory.HELVETICA, 16)));
 		prg.add(new Chunk(dsName,
 					 FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
 		prg.add(new Chunk(" dataset tables",
 					 FontFactory.getFont(FontFactory.HELVETICA, 16)));
 		addElement(prg);
         //addElement(new Phrase("Tables in this dataset:\n", Fonts.get(Fonts.HEADING_0)));
         
         Vector tables = ds.getTables();
         addElement(PdfUtil.tablesList(tables));
         addElement(new Phrase("\n"));
         
         // add dataset visual structure image
         if (ds.getVisual() != null){
             String fullPath = vsPath + ds.getVisual();
             File file = new File(fullPath);
             if (file.exists()){
                 
                 try {
                     PdfPTable table = PdfUtil.vsTable(fullPath, "Datamodel for this dataset");
                     if (table != null){
                         //insertPageBreak();
                         int size = addElement(table);
                         vsTableIndex = size-1;
                     }
                 }
                 catch (IOException e){
                     
                     // If there was an IOException, it very probably means that
                     // the file was not an image. But we nevertheless tell the
                     // user where the file can be seen.
                     
                     StringBuffer buf = new StringBuffer("\n\n");
                     buf.append("Visual structure of this dataset ");
                     buf.append("can be downloaded from its detailed view ");
                     buf.append("in the Data Dictionary website.");
                             
                     addElement(new Phrase(buf.toString()));
                 }
             }
         }
         
 		pageToLandscape();
 		if (tables!=null && tables.size()>0){
 			title = "Tables";
 			nr = sect.level(title, 1);
 			nr = nr==null ? "" : nr + " ";
 			prg = new Paragraph(nr + title, Fonts.get(Fonts.HEADING_1));
 			addElement(prg);
 		}
         
         // add full guidlines of tables
         for (int i=0; tables!=null && i<tables.size(); i++){
             DsTable dsTable = (DsTable)tables.get(i);
             // the tables guidelines will be added to the current chapter
             addElement(new Paragraph("\n"));
             TblPdfGuideline tblGuideln =
             	new TblPdfGuideline(searchEngine, this);//, (Section)chapter);
 			tblGuideln.setVsPath(vsPath);
             tblGuideln.write(dsTable.getID(), ds.getID());
             insertPageBreak();
         }
         
 		pageToPortrait();
         
         // add codelists
         addCodelists(tables);
 		insertPageBreak();
         
         // add img attrs
         addImgAttrs(tables);
         
         // set header & footer
         setHeader("");
 		setFooter();
     }
     
     private void addCodelists(Vector tables) throws Exception{
     	
 		String nr = null;
 		Paragraph prg = null;
 		String title = null;
 		String s = null;
 		boolean lv1added = false;
 		
 		for (int i=0; tables!=null && i<tables.size(); i++){
 			boolean lv2added = false;
 			DsTable tbl = (DsTable)tables.get(i);
 			Vector elms = (Vector)tblElms.get(tbl.getID());
 			for (int j=0; elms!=null && j<elms.size(); j++){
 				
 				DataElement elm = (DataElement)elms.get(j);
 				
 				PdfPTable codelist = PdfUtil.codelist(elm.getFixedValues());
 				if (codelist==null || codelist.size()==0) continue;
 				
 				// add 'Codelists' title
 				if (!lv1added){
 					nr = sect.level("Codelists", 1);
 					nr = nr==null ? "" : nr + " ";
 					prg = new Paragraph(nr +
 							"Codelists", Fonts.get(Fonts.HEADING_1));
 					addElement(prg);
 					addElement(new Paragraph("\n"));
 					lv1added = true;
 				}
 				
 				// add table title
 				if (!lv2added){
 					s = (String)tblNames.get(tbl.getID());
 					String tblName = Util.voidStr(s) ? tbl.getShortName() : s;
 					title = "Codelists for " + tblName + " table";
 					nr = sect.level(title, 2, false);
 					nr = nr==null ? "" : nr + " ";
 					
 					prg = new Paragraph();
 					prg.add(new Chunk(nr,
 						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
 					prg.add(new Chunk("Codelists for ",
 						FontFactory.getFont(FontFactory.HELVETICA, 14)));
 					prg.add(new Chunk(tblName,
 						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
 					prg.add(new Chunk(" table",
 						FontFactory.getFont(FontFactory.HELVETICA, 14)));
 					
 					addElement(prg);
 					addElement(new Paragraph("\n"));
 					lv2added = true;
 				}
 				
 				// add element title
 				s = elm.getAttributeValueByShortName("Name");
 				String elmName = Util.voidStr(s) ? elm.getShortName() : s;
 				title = elmName + " codelist";
 				nr = sect.level(title, 3, false);
 				nr = nr==null ? "" : nr + " ";
 				
 				prg = new Paragraph();
 				prg.add(new Chunk(nr + elmName,
 					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
 				prg.add(new Chunk(" codelist",
 					FontFactory.getFont(FontFactory.HELVETICA, 14)));
 
 				addElement(prg);
 				addElement(new Paragraph("\n"));
 				
 				// add codelist
 				addElement(codelist);
 				addElement(new Paragraph("\n"));
 			}
 		}
     }
 
 	private void addImgAttrs(Vector tables) throws Exception{
 		
 		String nr = null;
 		Paragraph prg = null;
 		String title = null;
 		String s = null;
 		boolean lv1added = false;
 		
 		for (int i=0; tables!=null && i<tables.size(); i++){
 			boolean lv2added = false;
 			DsTable tbl = (DsTable)tables.get(i);
 			Vector elms = (Vector)tblElms.get(tbl.getID());
 			for (int j=0; elms!=null && j<elms.size(); j++){
 				
 				DataElement elm = (DataElement)elms.get(j);
 				//PdfPTable imgTable =
 				//PdfUtil.imgAttributes(elm.getAttributes(), vsPath);
 				//if (imgTable==null || imgTable.size()==0) continue;
 				Vector imgVector =
 					PdfUtil.imgAttributes(elm.getAttributes(), vsPath);
 				if (imgVector==null || imgVector.size()==0) continue; 				
 				
 				// add 'Images' title
 				if (!lv1added){
 					nr = sect.level("Illustrations", 1);
 					nr = nr==null ? "" : nr + " ";
 					prg = new Paragraph(nr +
							"Images", Fonts.get(Fonts.HEADING_1));
 					addElement(prg);
 					addElement(new Paragraph("\n"));
 					lv1added = true;
 				}
 				
 				// add table title
 				if (!lv2added){
 					s = (String)tblNames.get(tbl.getID());
 					String tblName = Util.voidStr(s) ? tbl.getShortName() : s;
 					title = "Illustrations for " + tblName + " table";
 					nr = sect.level(title, 2, false);
 					nr = nr==null ? "" : nr + " ";
 					
 					prg = new Paragraph();
 					prg.add(new Chunk(nr,
 						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
 					prg.add(new Chunk("Illustrations for ",
 						FontFactory.getFont(FontFactory.HELVETICA, 14)));
 					prg.add(new Chunk(tblName,
 						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
 					prg.add(new Chunk(" table",
 						FontFactory.getFont(FontFactory.HELVETICA, 14)));
 					
 					addElement(prg);
 					addElement(new Paragraph("\n"));
 					lv2added = true;
 				}
 				
 				// add element title
 				s = elm.getAttributeValueByShortName("Name");
 				String elmName = Util.voidStr(s) ? elm.getShortName() : s;
 				title = elmName + " illustrations";
 				nr = sect.level(title, 3, false);
 				nr = nr==null ? "" : nr + " ";
 				
 				prg = new Paragraph();
 				prg.add(new Chunk(nr + elmName,
 					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
 				prg.add(new Chunk(" illustrations",
 					FontFactory.getFont(FontFactory.HELVETICA, 14)));
 
 				addElement(prg);
 				
 				// add images
 				for (int u=0; u<imgVector.size(); u++){
 					com.lowagie.text.Image img =
 						(com.lowagie.text.Image)imgVector.get(u); 
 					addElement(img);
 					//addElement(imgTable);
 					//addElement(new Paragraph("\n"));
 				}
 			}
 		}
 	}
     
     /*protected int addElement(Element elm){
         
         if (elm == null || chapter == null)
 		if (elm == null)
             return elmCount;
         
         chapter.add(elm);
         elmCount = elmCount + 1;
         return elmCount;
     }*/
     
     protected boolean keepOnOnePage(int index){
         if (index == vsTableIndex)
             return true;
         else
             return false;
     }
     
     /**
     * Override of the method for adding a title page
     */
     protected void addTitlePage(Document doc) throws Exception {
         
 		doc.add(new Paragraph("\n\n\n\n"));			
         // data dictionary
         Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
 		Paragraph prg = new Paragraph("Data Dictionary", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
         
         doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n"));
         
         // full definition
         font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
         prg = new Paragraph("Definition of", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
         
         // dataset name
         font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26);
         prg = new Paragraph(dsName, font);
 		prg.setAlignment(Element.ALIGN_CENTER);
 		doc.add(prg);
 		
 		// dataset word
         font = FontFactory.getFont(FontFactory.HELVETICA, 14);
 		prg = new Paragraph("dataset", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
         
 		doc.add(new Paragraph("\n\n"));
 		
 		// version
 		prg = new Paragraph();
 		prg.add(new Chunk("Version: ", font));
 		prg.add(new Chunk(dsVersion, font));
 		prg.setAlignment(Element.ALIGN_CENTER);
 		doc.add(prg);
 		
 		// date
 		//prg = new Paragraph(getTitlePageDate());
 		//prg.setAlignment(Element.ALIGN_CENTER);
 		//doc.add(prg);
         
         doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n"));
         
         // European Environment Agency
         font = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
         prg = new Paragraph("European Environment Agency", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         
         if (!Util.voidStr(logo)){
             Image img = Image.getInstance(logo);
             img.setAlignment(Image.LEFT);
                     
             prg.add(new Chunk(img, 0, 0));
         }
         
         doc.add(prg);
     }
     
     /**
     * Override of the method indicating if title page needed
     */
     protected boolean titlePageNeeded(){
         return true;
     }
     
     private String getTitlePageDate(){
     	
     	String[] months = {"January", "February", "March", "April", "May",
     					   "June", "July", "August", "September", "October",
     					   "November", "December"};
     	
 		Calendar cal = Calendar.getInstance();
 		int month = cal.get(Calendar.MONTH);
 		int year = cal.get(Calendar.YEAR);
 		
 		return months[month] + " " + String.valueOf(year);
     }
     
 	protected void setHeader(String title) throws Exception {
         
 		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
 		font.setColor(Color.gray);
 	
 		Paragraph prg = new Paragraph();		
 		prg.add(new Chunk("Data Dictionary\n", font));
 		prg.setLeading(10*1.2f);
 		font = FontFactory.getFont(FontFactory.HELVETICA, 9);
 		font.setColor(Color.lightGray);
 		prg.add(new Chunk("Dataset specification for " + dsName +
 								" * Version " + dsVersion, font));
 
 		this.header = new HeaderFooter(prg, false);
 		header.setBorder(com.lowagie.text.Rectangle.BOTTOM);
 	}
 
 	/**
 	 * Default implementation for adding index based on sectioning
 	 */
 	public Vector getIndexPage() throws Exception{
 		
 		Vector elems = new Vector();
 		
 		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
 		Paragraph prg = new Paragraph("About this document", font);
 		elems.add(prg);
 		elems.add(new Paragraph("\n"));
 		
 		String about = 
 		"This document holds the technical specifications for a dataflow " +
 		"based on automatically generated output from the Data Dictionary " +
 		"application. The Data Dictionary is a central service for storing " +
 		"technical specifications for information requested in reporting " +
 		"obligations. The purpose of this document is to support countries " +
 		"in reporting good quality data. This document contains detailed " +
 		"specifications in a structured format for the data requested in a " +
 		"dataflow. Suggestions from users on how to improve the document " +
 		"are welcome.";
 		
 		font = FontFactory.getFont(FontFactory.HELVETICA, 10);
 		prg = new Paragraph(about, font);
 		elems.add(prg);
 		
 		if (sect==null)
 			return elems;
 		
 		Vector toc = sect.getTOCformatted("\t\t\t\t");
 		if (toc==null || toc.size()==0)
 			return elems;
 		
 		elems.add(new Paragraph("\n\n\n"));
 		
 		font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
 		prg = new Paragraph("Index", font);
 		elems.add(prg);
 		
 		elems.add(new Paragraph("\n"));
 		
 		font = FontFactory.getFont(FontFactory.HELVETICA, 10);
 		for (int i=0; i<toc.size(); i++){
 			String line = (String)toc.get(i);
 			elems.add(new Chunk(line + "\n", font));
 		}
 		
 		return elems;
 	}
 	
 	public void addTblElms(String tblID, Vector elms){
 		tblElms.put(tblID, elms);
 	}
 	
 	public void addTblNames(String tblID, String name){
 		tblNames.put(tblID, name);
 	}
 
 	protected void setFooter() throws Exception {
     	
 		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
 		font.setColor(Color.gray);
 		
 		Phrase phr = new Phrase();
 		phr.add(new Chunk("European Environment Agency  *  ", font));
 
 		font = FontFactory.getFont(FontFactory.HELVETICA, 9);
 		font.setColor(Color.lightGray);
 		phr.add(new Chunk("http://www.eea.eu.int     ", font));
 		phr.setLeading(10*1.2f);
 		
 		String submOrgName = (String)submitOrg.get("name");
 		if (!Util.voidStr(submOrgName) &&
 			!submOrgName.trim().equals("European Environment Agency")){
 			if (!Util.voidStr(submOrgName)){
 				font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
 				font.setColor(Color.gray);
 				phr.add(new Chunk("\n" + submOrgName, font));
 			}
 			
 			String submOrgUrl = (String)submitOrg.get("url");
 			if (!Util.voidStr(submOrgUrl)){
 				font = FontFactory.getFont(FontFactory.HELVETICA, 9);
 				font.setColor(Color.lightGray);
 				phr.add(new Chunk("  *  " + submOrgUrl, font));
 			}
 		}
 		
 		phr.add(new Chunk("   ", font));
 		
 		footer = new HeaderFooter(phr, true);
 		//footer.setAlignment(Element.ALIGN_LEFT);
 		footer.setAlignment(Element.ALIGN_RIGHT);
 		footer.setBorder(com.lowagie.text.Rectangle.TOP);
 	}
   
     public static void main(String[] args){
         try{
             Class.forName("org.gjt.mm.mysql.Driver");
             Connection conn =
             //DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
             DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
 
             String fileName = "x:\\projects\\datadict\\tmp\\ds_test_guideline.pdf";
             DstPdfGuideline guideline = new DstPdfGuideline(conn, new FileOutputStream(fileName));
             guideline.setVsPath("x:\\projects\\datadict\\visuals");
             guideline.setLogo("x:\\projects\\datadict\\images\\pdf_logo_small.png");
             guideline.write("1259");
             guideline.flush();
         }
         catch (Exception e){
             System.out.println(e.toString());
         }
     }
 }
