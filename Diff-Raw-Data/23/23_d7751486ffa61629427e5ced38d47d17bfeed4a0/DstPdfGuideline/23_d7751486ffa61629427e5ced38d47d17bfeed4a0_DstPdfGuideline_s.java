 package eionet.meta.exports.pdf;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedHashMap;
 import java.util.Vector;
 
 import com.lowagie.text.Chunk;
 import com.lowagie.text.Document;
 import com.lowagie.text.Element;
 import com.lowagie.text.Font;
 import com.lowagie.text.FontFactory;
 import com.lowagie.text.HeaderFooter;
 import com.lowagie.text.Image;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.Phrase;
 import com.lowagie.text.pdf.PdfPTable;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.DElemAttribute;
 import eionet.meta.DataElement;
 import eionet.meta.Dataset;
 import eionet.meta.DsTable;
 import eionet.meta.exports.CachableIF;
 import eionet.util.Util;
 import eionet.util.sql.INParameters;
 import eionet.util.sql.SQL;
 
 /**
  * PDF guidelines generator for a dataset.
  */
 public class DstPdfGuideline extends PdfHandout implements CachableIF {
 
     protected static final String FILE_EXT = ".pdf";
 
     private int vsTableIndex = -1;
     protected int levelOffset = 0;
 
     private String dsName = "";
     private String dsVersion = "";
     @SuppressWarnings("rawtypes")
     private Hashtable tblElms = new Hashtable();
     @SuppressWarnings("rawtypes")
     private Hashtable tblNames = new Hashtable();
     @SuppressWarnings("rawtypes")
     private Hashtable submitOrg = new Hashtable();
     @SuppressWarnings("rawtypes")
     private Hashtable respOrg = new Hashtable();
 
     private String cachePath = null;
     private String cacheFileName = null;
 
     /** DB connection for storing cache entries. */
     private Connection conn = null;
 
     /**
      * Constructor.
      * 
      * @param conn
      */
     public DstPdfGuideline(Connection conn) {
         this.conn = conn;
         this.levelOffset = 0;
         searchEngine = new DDSearchEngine(conn);
         setShowedAttributes();
     }
 
     /**
      * Constructor.
      * 
      * @param conn
      * @param os
      */
     public DstPdfGuideline(Connection conn, OutputStream os) {
         this(conn);
         this.os = os;
     }
 
     /**
      * 
      * @param dsID
      * @throws Exception
      */
     private void cache(String dsID) throws Exception {
         write(dsID, true);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.PdfHandout#write(java.lang.String)
      */
     @Override
     public void write(String dsID) throws Exception {
         write(dsID, false);
     }
 
     /**
      * 
      * @param dsID
      * @param caching
      * @throws Exception
      */
     @SuppressWarnings("rawtypes")
     protected void write(String dsID, boolean caching) throws Exception {
 
         if (Util.isEmpty(dsID)) {
             throw new Exception("Dataset ID not specified");
         }
 
         // See if this output has been cached.
         // If so, write from cache and exit
         if (!caching && isCached(dsID)) {
             writeFromCache();
             return;
         }
         Dataset datasetInstance = initializeDataset(dsID);
         write(datasetInstance);
     }
 
     /**
      * Method to initialize dataset instance
      * 
      * @param dstId
      * @return
      */
     protected Dataset initializeDataset(String dstId) throws Exception {
         Dataset datasetInstance = searchEngine.getDataset(dstId);
         if (datasetInstance == null) {
             throw new Exception("Dataset not found!");
         }
 
         fileName = datasetInstance.getIdentifier() + FILE_EXT;
 
         Vector v = searchEngine.getSimpleAttributes(dstId, "DS");
         datasetInstance.setSimpleAttributes(v);
         v = searchEngine.getComplexAttributes(dstId, "DS");
         datasetInstance.setComplexAttributes(v);
         v = searchEngine.getDatasetTables(dstId, true);
         DsTable tbl = null;
         for (int i = 0; v != null && i < v.size(); i++) {
             tbl = (DsTable) v.get(i);
             tbl.setSimpleAttributes(searchEngine.getSimpleAttributes(tbl.getID(), "T"));
         }
         datasetInstance.setTables(v);
 
         addParameter("dstID", dstId);
 
         String s = datasetInstance.getAttributeValueByShortName("Name");
         dsName = Util.isEmpty(s) ? datasetInstance.getShortName() : s;
 
         dsVersion = datasetInstance.getAttributeValueByShortName("Version");
 
         return datasetInstance;
     }// end of method initializeDataset
 
     /**
      * 
      * @param ds
      * @throws Exception
      */
     @SuppressWarnings({"rawtypes", "unchecked"})
     protected void write(Dataset ds) throws Exception {
 
         // defensive check
         if (ds == null) {
             throw new Exception("Dataset object is null!");
         }
 
         String title = "General information for " + dsName + " dataset";
         String nr = sect.level(title, getLevelFor(1));
         nr = nr == null ? "" : nr + " ";
 
         String localAddress = PdfHandout.getLocalDestinationAddressFor(nr + title);
         Paragraph prg = new Paragraph();
         prg.add(new Chunk(nr + "General information for ", Fonts.getUnicode(16)).setLocalDestination(localAddress));
         prg.add(new Chunk(dsName, Fonts.getUnicode(16, Font.BOLD)));
         prg.add(new Chunk(" dataset", Fonts.getUnicode(16)));
 
         // add the dataset chapter to the document
         addElement(prg);
 
         addElement(new Paragraph("\n"));
 
         // set up complex attributes to retrieve SubmitOrg, RespOrg and Contact information
         Vector cattrs = ds.getComplexAttributes();
         if (cattrs != null && cattrs.size() > 0) {
             DElemAttribute attr = null;
             for (int i = 0; i < cattrs.size(); i++) {
                 attr = (DElemAttribute) cattrs.get(i);
                 attr.setFields(searchEngine.getAttrFields(attr.getID()));
             }
         }
 
         this.submitOrg = ds.getCAttrByShortName("SubmitOrganisation");
         this.respOrg = ds.getCAttrByShortName("RespOrganisation");
 
         // write simple attributes
         addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));
 
         Vector attrs = ds.getSimpleAttributes();
 
         Hashtable hash = new Hashtable();
         hash.put("name", "Short name");
         hash.put("value", ds.getShortName());
         attrs.add(hash);
 
         if (!Util.isEmpty(dsVersion)) {
             hash = new Hashtable();
             hash.put("name", "Version");
             hash.put("value", dsVersion);
             attrs.add(hash);
         }
 
         String contactInfo = getContactInfo();
         if (contactInfo != null) {
             hash = new Hashtable();
             hash.put("name", "Contact information");
             hash.put("value", contactInfo);
             attrs.add(hash);
         }
 
         addElement(PdfUtil.simpleAttributesTable(attrs, showedAttrs));
         addElement(new Phrase("\n"));
 
         // write tables list
         title = "Overview of " + dsName + " dataset tables";
         nr = sect.level(title, getLevelFor(1));
         nr = nr == null ? "" : nr + " ";
 
         localAddress = PdfHandout.getLocalDestinationAddressFor(nr + title);
         prg = new Paragraph();
         prg.add(new Chunk(nr, Fonts.getUnicode(16, Font.BOLD)).setLocalDestination(localAddress));
         prg.add(new Chunk("Overview of ", Fonts.getUnicode(16)));
         prg.add(new Chunk(dsName, Fonts.getUnicode(16, Font.BOLD)));
         prg.add(new Chunk(" dataset tables", Fonts.getUnicode(16)));
         addElement(prg);
 
         Vector tables = ds.getTables();
         addElement(PdfUtil.tablesList(tables));
         addElement(new Phrase("\n"));
 
         // add dataset visual structure image
         if (ds.getVisual() != null) {
             String fullPath = visualsPath + ds.getVisual();
             File file = new File(fullPath);
             if (file.exists()) {
 
                 try {
                     PdfPTable table = PdfUtil.vsTable(fullPath, "Datamodel for this dataset");
                     if (table != null) {
                         // insertPageBreak();
                         int size = addElement(table);
                         vsTableIndex = size - 1;
                     }
                 } catch (IOException e) {
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
         if (tables != null && tables.size() > 0) {
             title = "Tables";
             nr = sect.level(title, getLevelFor(1));
             nr = nr == null ? "" : nr + " ";
             localAddress = PdfHandout.getLocalDestinationAddressFor(nr + title);
             prg = new Paragraph(new Chunk(nr + title, Fonts.get(Fonts.HEADING_1)).setLocalDestination(localAddress));
             addElement(prg);
         }
 
         // add full guidlines of tables
         for (int i = 0; tables != null && i < tables.size(); i++) {
             DsTable dsTable = (DsTable) tables.get(i);
 
             // the tables guidelines will be added to the current chapter
             addElement(new Paragraph("\n"));
             TblPdfGuideline tblGuideln = new TblPdfGuideline(searchEngine, this);
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
 
     private void addCodelists(Vector<DsTable> tables) throws Exception {
         String localAddress = null;
         String nr = null;
         Paragraph prg = null;
         String title = null;
         String temp = null;
         boolean lv1added = false;
 
         HashMap<String, DataElement> commonElements = new HashMap<String, DataElement>();
         HashMap<String, ArrayList<String>> commonElementsToTables = new HashMap<String, ArrayList<String>>();
 
         // iterate on all tables and all elements of tables
         // add non-common code lists and find common code lists
         for (int i = 0; tables != null && i < tables.size(); i++) {
             boolean lv3added = false;
             DsTable tbl = tables.get(i);
             Vector elms = (Vector) tblElms.get(tbl.getID());
             for (int j = 0; elms != null && j < elms.size(); j++) {
 
                 DataElement elm = (DataElement) elms.get(j);
 
                 if (elm.isCommon()) {
                     String elementId = elm.getID();
                     if (commonElements.containsKey(elementId)) {
                         ArrayList<String> commonElemTables = commonElementsToTables.get(elementId);
                         if (!commonElemTables.contains(elm.getTableID())) {
                             commonElemTables.add(elm.getTableID());
                         }
                     } else {
                         commonElements.put(elementId, elm);
                         ArrayList<String> commonElemTables = new ArrayList<String>();
                         commonElemTables.add(elm.getTableID());
                         commonElementsToTables.put(elementId, commonElemTables);
                     }
                     continue;
                 }
 
                 PdfPTable codelist = PdfUtil.codelist(elm.getFixedValues());
                 if (codelist == null || codelist.size() == 0) {
                     continue;
                 }
 
                 // add 'Codelists' title
                 if (!lv1added) {
                     temp = "Codelists";
                     nr = sect.level(temp, getLevelFor(1));
                     nr = nr == null ? "" : nr + " ";
                     localAddress = PdfHandout.getLocalDestinationAddressFor(nr + temp);
                     prg = new Paragraph(new Chunk(nr + temp, Fonts.get(Fonts.HEADING_1)).setLocalDestination(localAddress));
                     addElement(prg);
                     addElement(new Paragraph("\n"));
                     lv1added = true;
 
                     temp = "Non-common Elements Codelists";
                     nr = sect.level(temp, getLevelFor(2));
                     nr = nr == null ? "" : nr + " ";
                     localAddress = PdfHandout.getLocalDestinationAddressFor(nr + temp);
                     prg = new Paragraph(new Chunk(nr + temp, Fonts.get(Fonts.HEADING_2)).setLocalDestination(localAddress));
                     addElement(prg);
                     addElement(new Paragraph("\n"));
                 }
 
                 // add table title
                 if (!lv3added) {
                     temp = (String) tblNames.get(tbl.getID());
                     String tblName = Util.isEmpty(temp) ? tbl.getShortName() : temp;
                     title = "Codelists for " + tblName + " table";
                     nr = sect.level(title, getLevelFor(3), false);
                     nr = nr == null ? "" : nr + " ";
 
                     prg = new Paragraph();
                     prg.add(new Chunk(nr, Fonts.getUnicode(14, Font.BOLD)));
                     prg.add(new Chunk("Codelists for ", Fonts.getUnicode(14)));
                     prg.add(new Chunk(tblName, Fonts.getUnicode(14, Font.BOLD)));
                     prg.add(new Chunk(" table", Fonts.getUnicode(14)));
 
                     addElement(prg);
                     addElement(new Paragraph("\n"));
                     lv3added = true;
                 }
 
                 // add element title
                 temp = elm.getAttributeValueByShortName("Name");
                 String elmName = Util.isEmpty(temp) ? elm.getShortName() : temp;
 
                 String releasedDate = "";
                 if (!Util.isEmpty(elm.getDate())) {
                     long parsed = Long.parseLong(elm.getDate());
                     if (parsed > 0) {
                         releasedDate = " (Released at " + eionet.util.Util.releasedDateShort(parsed) + ")";
                     }
                 }
                 title = elmName + releasedDate + " codelist";
                 nr = sect.level(title, getLevelFor(4), false);
                 nr = nr == null ? "" : nr + " ";
 
                 localAddress = PdfHandout.getLocalDestinationAddressFor(elm.getID());
                 prg = new Paragraph();
                 prg.add(new Chunk(nr + elmName + releasedDate, Fonts.getUnicode(14, Font.BOLD)).setLocalDestination(localAddress));
                 prg.add(new Chunk(" codelist", Fonts.getUnicode(14)));
 
                 addElement(prg);
                 addElement(new Paragraph("\n"));
 
                 // add codelist
                 addElement(codelist);
                 addElement(new Paragraph("\n"));
             }// end of for iterating on table elements
         }// end of for iterating on tables
 
         // add common elements code lists
         if (commonElements.size() > 0) {
             // add 'Codelists' title
             if (!lv1added) {
                 temp = "Codelists";
                 nr = sect.level(temp, getLevelFor(1));
                 nr = nr == null ? "" : nr + " ";
                 localAddress = PdfHandout.getLocalDestinationAddressFor(nr + temp);
                 prg = new Paragraph(new Chunk(nr + temp, Fonts.get(Fonts.HEADING_1)).setLocalDestination(localAddress));
                 addElement(prg);
                 addElement(new Paragraph("\n"));
             }
 
             temp = "Common Elements Codelists";
             nr = sect.level(temp, getLevelFor(2));
             nr = nr == null ? "" : nr + " ";
             localAddress = PdfHandout.getLocalDestinationAddressFor(nr + temp);
             prg = new Paragraph(new Chunk(nr + temp, Fonts.get(Fonts.HEADING_2)).setLocalDestination(localAddress));
             addElement(prg);
             addElement(new Paragraph("\n"));
 
             for (String elementId : commonElements.keySet()) {
                 DataElement elm = commonElements.get(elementId);
 
                 PdfPTable codelist = PdfUtil.codelist(elm.getFixedValues());
                 if (codelist == null || codelist.size() == 0) {
                     continue;
                 }
 
                 // add element title
                 temp = elm.getAttributeValueByShortName("Name");
                 String elmName = Util.isEmpty(temp) ? elm.getShortName() : temp;
                 title = elmName;
                 if (!Util.isEmpty(elm.getDate())) {
                     long parsed = Long.parseLong(elm.getDate());
                     if (parsed > 0) {
                         title += " (Released at " + eionet.util.Util.releasedDateShort(parsed) + ")";
                     }
                 }
 
                 nr = sect.level(title, getLevelFor(3), false);
                 nr = nr == null ? "" : nr + " ";
 
                 localAddress = PdfHandout.getLocalDestinationAddressFor(elm.getID());
                 prg = new Paragraph();
                 prg.add(new Chunk(nr + title, Fonts.getUnicode(14, Font.BOLD)).setLocalDestination(localAddress));
 
                 addElement(prg);
                 addElement(new Paragraph("\n"));
 
                 // add Codelist title
                 title = "Codelist";
                 nr = sect.level(title, getLevelFor(4), false);
                 nr = nr == null ? "" : nr + " ";
 
                 prg = new Paragraph();
                 prg.add(new Chunk(nr + title, Fonts.getUnicode(14)));
                 addElement(prg);
                 addElement(new Paragraph("\n"));
 
                 // add codelist
                 addElement(codelist);
                 addElement(new Paragraph("\n"));
 
                 // add reference tables title
                 title = "Referencing Tables";
                 nr = sect.level(title, getLevelFor(4), false);
                 nr = nr == null ? "" : nr + " ";
                 prg = new Paragraph();
                 prg.add(new Chunk(nr + title, Fonts.getUnicode(14)));
                 addElement(prg);
                 addElement(new Paragraph("\n"));
 
                 // add tables here
                 ArrayList<ArrayList<String>> tableContent = new ArrayList<ArrayList<String>>();
                 for (String name : commonElementsToTables.get(elementId)) {
                     ArrayList<String> row = new ArrayList<String>();
                     row.add((String) tblNames.get(name));// there is only one column
                     tableContent.add(row);
                 }
 
                 ArrayList<String> header = new ArrayList<String>();
                 header.add("Table Name"); // also it is good idea to add column name
                 PdfPTable tableForTablesOfCommonElement = PdfUtil.giveMeTableOfThisContents(header, tableContent, null);
 
                 // add table matrix
                 addElement(tableForTablesOfCommonElement);
                 addElement(new Paragraph("\n"));
             }
         }// end of block to add common elements
     }// end of method addCodelists
 
     @SuppressWarnings("rawtypes")
     private void addImgAttrs(Vector tables) throws Exception {
 
         String nr = null;
         Paragraph prg = null;
         String title = null;
         String s = null;
         boolean lv1added = false;
 
         for (int i = 0; tables != null && i < tables.size(); i++) {
 
             boolean lv2added = false;
             DsTable tbl = (DsTable) tables.get(i);
             Vector tblImgVector = PdfUtil.imgAttributes(tbl.getSimpleAttributes(), visualsPath);
             if (tblImgVector != null && tblImgVector.size() != 0) {
 
                 // add level 1 title
                 if (!lv1added) {
                     nr = sect.level("Illustrations", getLevelFor(1));
                     nr = nr == null ? "" : nr + " ";
                     prg = new Paragraph(nr + "Illustrations", Fonts.get(Fonts.HEADING_1));
                     addElement(prg);
                     addElement(new Paragraph("\n"));
                     lv1added = true;
                 }
 
                 // add level 2 title
                 s = (String) tblNames.get(tbl.getID());
                 String tblName = Util.isEmpty(s) ? tbl.getShortName() : s;
                 title = "Illustrations for " + tblName + " table";
                 nr = sect.level(title, getLevelFor(2), false);
                 nr = nr == null ? "" : nr + " ";
 
                 prg = new Paragraph();
                 prg.add(new Chunk(nr, Fonts.getUnicode(14, Font.BOLD)));
                 prg.add(new Chunk("Illustrations for ", Fonts.getUnicode(14)));
                 prg.add(new Chunk(tblName, Fonts.getUnicode(14, Font.BOLD)));
                 prg.add(new Chunk(" table", Fonts.getUnicode(14)));
 
                 addElement(prg);
                 addElement(new Paragraph("\n"));
                 lv2added = true;
 
                 // add images vector
                 for (int u = 0; u < tblImgVector.size(); u++) {
                     com.lowagie.text.Image img = (com.lowagie.text.Image) tblImgVector.get(u);
                     addElement(img);
                 }
 
                 addElement(new Paragraph("\n"));
             }
 
             Vector elms = (Vector) tblElms.get(tbl.getID());
             for (int j = 0; elms != null && j < elms.size(); j++) {
 
                 DataElement elm = (DataElement) elms.get(j);
 
                 Vector elmImgVector = PdfUtil.imgAttributes(elm.getAttributes(), visualsPath);
                 if (elmImgVector == null || elmImgVector.size() == 0) {
                     continue;
                 }
 
                 // add 'Images' title
                 if (!lv1added) {
                     nr = sect.level("Illustrations", getLevelFor(1));
                     nr = nr == null ? "" : nr + " ";
                     prg = new Paragraph(nr + "Illustrations", Fonts.get(Fonts.HEADING_1));
                     addElement(prg);
                     addElement(new Paragraph("\n"));
                     lv1added = true;
                 }
 
                 // add table title
                 if (!lv2added) {
                     s = (String) tblNames.get(tbl.getID());
                     String tblName = Util.isEmpty(s) ? tbl.getShortName() : s;
                     title = "Illustrations for " + tblName + " table";
                     nr = sect.level(title, getLevelFor(2), false);
                     nr = nr == null ? "" : nr + " ";
 
                     prg = new Paragraph();
                     prg.add(new Chunk(nr, Fonts.getUnicode(14, Font.BOLD)));
                     prg.add(new Chunk("Illustrations for ", Fonts.getUnicode(14)));
                     prg.add(new Chunk(tblName, Fonts.getUnicode(14, Font.BOLD)));
                     prg.add(new Chunk(" table", Fonts.getUnicode(14)));
 
                     addElement(prg);
                     addElement(new Paragraph("\n"));
                     lv2added = true;
                 }
 
                 // add element title
                 s = elm.getAttributeValueByShortName("Name");
                 String elmName = Util.isEmpty(s) ? elm.getShortName() : s;
                 title = elmName + " illustrations";
                 nr = sect.level(title, getLevelFor(3), false);
                 nr = nr == null ? "" : nr + " ";
 
                 prg = new Paragraph();
                 prg.add(new Chunk(nr + elmName, Fonts.getUnicode(14, Font.BOLD)));
                 prg.add(new Chunk(" illustrations", Fonts.getUnicode(14)));
 
                 addElement(prg);
 
                 // add images
                 for (int u = 0; u < elmImgVector.size(); u++) {
                     com.lowagie.text.Image img = (com.lowagie.text.Image) elmImgVector.get(u);
                     addElement(img);
                 }
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.PdfHandout#keepOnOnePage(int)
      */
     @Override
     protected boolean keepOnOnePage(int index) {
         if (index == vsTableIndex) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Override of the method for adding a title page
      */
     @Override
     protected void addTitlePage(Document doc) throws Exception {
         ArrayList<Paragraph> titlePageContents = getTitlePageContents();
 
         for (Paragraph prg : titlePageContents) {
             doc.add(prg);
         }
     }
 
     protected void addTitlePageForCombined() throws Exception {
         // String s = ds.getAttributeValueByShortName("Name");
         // dsName = Util.isEmpty(s) ? ds.getShortName() : s;
 
         String nr = sect.level(dsName, 1);
         nr = nr == null ? "" : nr + " ";
         String localAddress = PdfHandout.getLocalDestinationAddressFor(nr + dsName);
         Paragraph prg = new Paragraph();
         prg.add(new Chunk(" ", Fonts.getUnicode(16, Font.BOLD)).setLocalDestination(localAddress));
         addElement(prg);
 
         ArrayList<Paragraph> titlePageContents = getTitlePageContents();
         for (Paragraph content : titlePageContents) {
             addElement(content);
         }
         insertPageBreak();
         // prg = new Paragraph();
         // prg.add(new Chunk(nr + dsName, Fonts.getUnicode(16, Font.BOLD)));
         // addElement(prg);
     }// end of method addTitlePageForCombined
 
     private ArrayList<Paragraph> getTitlePageContents() throws Exception {
         ArrayList<Paragraph> doc = new ArrayList<Paragraph>();
 
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
         font = Fonts.getUnicode(26, Font.BOLD);
         prg = new Paragraph(dsName, font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
 
         // dataset word
         font = Fonts.getUnicode(14);
         prg = new Paragraph("dataset", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
 
         doc.add(new Paragraph("\n\n"));
 
         // version
         if (dsVersion != null) {
             prg = new Paragraph();
             prg.add(new Chunk("Version: ", font));
             prg.add(new Chunk(dsVersion, font));
             prg.setAlignment(Element.ALIGN_CENTER);
             doc.add(prg);
         }
 
         doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n"));
 
         // European Environment Agency
         font = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
         prg = new Paragraph("European Environment Agency", font);
         prg.setAlignment(Element.ALIGN_CENTER);
 
         if (!Util.isEmpty(logo)) {
             Image img = Image.getInstance(logo);
             img.setAlignment(Image.LEFT);
 
             prg.add(new Chunk(img, 0, 0));
         }
 
         doc.add(prg);
 
         return doc;
     }// end of method titlePageContents
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.PdfHandout#titlePageNeeded()
      */
     @Override
     protected boolean titlePageNeeded() {
         return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.PdfHandout#setHeader(java.lang.String)
      */
     @Override
     protected void setHeader(String title) throws Exception {
         Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
         font.setColor(Color.gray);
 
         Paragraph prg = new Paragraph();
         prg.add(new Chunk("Data Dictionary\n", font));
         prg.setLeading(10 * 1.2f);
 
         font = Fonts.getUnicode(9);
         font.setColor(Color.lightGray);
         prg.add(new Chunk("Dataset specification for " + dsName + " * Version " + dsVersion, font));
         prg.add(new Chunk(" * created " + Util.pdfDate(System.currentTimeMillis()), font));
 
         this.header = new HeaderFooter(prg, false);
        header.setBorder(com.lowagie.text.Rectangle.BOTTOM);
     }// end of method setHeader
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.PdfHandout#getIndexPage()
      */
     @SuppressWarnings({"rawtypes", "unchecked"})
     @Override
     public Vector getIndexPage() throws Exception {
         Vector elems = new Vector();
 
         Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
         Paragraph prg = new Paragraph("About this document", font);
         elems.add(prg);
         elems.add(new Paragraph("\n"));
 
         String about =
                 "This document holds the technical specifications for a dataflow "
                         + "based on automatically generated output from the Data Dictionary "
                         + "application. The Data Dictionary is a central service for storing "
                         + "technical specifications for information requested in reporting "
                         + "obligations. The purpose of this document is to support countries "
                         + "in reporting good quality data. This document contains detailed "
                         + "specifications in a structured format for the data requested in a "
                         + "dataflow. Suggestions from users on how to improve the document " + "are welcome.";
 
         font = FontFactory.getFont(FontFactory.HELVETICA, 10);
         prg = new Paragraph(about, font);
         elems.add(prg);
 
         if (sect == null) {
             return elems;
         }
 
         Vector toc = sect.getTOCformatted("    ");
         if (toc == null || toc.size() == 0) {
             return elems;
         }
 
         elems.add(new Paragraph("\n\n\n"));
 
         font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
         prg = new Paragraph("Index", font);
         elems.add(prg);
 
         elems.add(new Paragraph("\n"));
 
         font = Fonts.getUnicode(10);
         for (int i = 0; i < toc.size(); i++) {
             String line = (String) toc.get(i);
             String goToAddress = PdfHandout.getLocalDestinationAddressFor(line);
             elems.add(new Chunk(line + "\n", font).setLocalGoto(goToAddress));
         }
 
         return elems;
     }
 
     @SuppressWarnings({"rawtypes", "unchecked"})
     public void addTblElms(String tblID, Vector elms) {
         tblElms.put(tblID, elms);
     }
 
     @SuppressWarnings("unchecked")
     public void addTblNames(String tblID, String name) {
         tblNames.put(tblID, name);
     }
 
     public int getLevelFor(int lvl) {
         return this.levelOffset + lvl;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.PdfHandout#setFooter()
      */
     @Override
     protected void setFooter() throws Exception {
 
         Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
         font.setColor(Color.gray);
 
         Phrase phr = new Phrase();
 
         String submOrgName = (String) submitOrg.get("name");
         if (!Util.isEmpty(submOrgName)) {
             font = Fonts.getUnicode(9, Font.BOLD);
             font.setColor(Color.gray);
             phr.add(new Chunk(submOrgName, font));
 
             String submOrgUrl = (String) submitOrg.get("url");
             if (!Util.isEmpty(submOrgUrl)) {
                 font = Fonts.getUnicode(9);
                 font.setColor(Color.lightGray);
                 phr.add(new Chunk("  *  " + submOrgUrl, font));
             }
 
             phr.add(new Chunk("     ", font));
             phr.setLeading(10 * 1.2f);
         } else {
             submOrgName = "";
         }
 
         if (respOrg != null) {
             String respOrgName = (String) respOrg.get("name");
             if (!Util.isEmpty(respOrgName) && !respOrgName.equals(submOrgName)) {
                 font = Fonts.getUnicode(9, Font.BOLD);
                 font.setColor(Color.gray);
                 phr.add(new Chunk("\n" + respOrgName, font));
 
                 String respOrgUrl = (String) respOrg.get("url");
                 if (!Util.isEmpty(respOrgUrl)) {
                     font = Fonts.getUnicode(9);
                     font.setColor(Color.lightGray);
                     phr.add(new Chunk("  *  " + respOrgUrl, font));
                 }
             }
         }
 
         phr.add(new Chunk("   ", font));
 
         footer = new HeaderFooter(phr, true);
         footer.setAlignment(Element.ALIGN_RIGHT);
         footer.setBorder(com.lowagie.text.Rectangle.TOP);
     }
 
     public String getDsName() {
         return dsName;
     }
 
     public String getDsVersion() {
         return dsVersion;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.PdfHandout#setShowedAttributes()
      */
     @SuppressWarnings("unchecked")
     @Override
     protected void setShowedAttributes() {
 
         showedAttrs.add("Short name");
         showedAttrs.add("Version");
         showedAttrs.add("Definition");
         showedAttrs.add("ShortDescription");
         showedAttrs.add("Contact information");
         showedAttrs.add("PlannedUpdFreq");
         showedAttrs.add("Methodology");
     }
 
     /**
      * 
      * @return
      */
     @SuppressWarnings("rawtypes")
     private String getContactInfo() {
 
         if (submitOrg == null) {
             return null;
         }
 
         Vector flds = (Vector) submitOrg.get("fields");
         if (flds == null || flds.size() == 0) {
             return null;
         }
 
         StringBuffer buf = null;
         for (int i = 0; i < flds.size(); i++) {
             Hashtable fld = (Hashtable) flds.get(i);
             String fldName = (String) fld.get("name");
             if (fldName != null) {
                 String fldValue = (String) submitOrg.get(fldName);
                 if (!Util.isEmpty(fldValue)) {
                     if (buf == null) {
                         buf = new StringBuffer();
                     }
                     if (buf.length() > 0) {
                         buf.append("\n");
                     }
                     buf.append(fldValue);
                 }
             }
         }
 
         return buf == null ? null : buf.toString();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.CachableIF#updateCache(java.lang.String)
      */
     @Override
     public void updateCache(String id) throws Exception {
 
         cache(id);
         if (cachePath != null && fileName != null) {
             String fn = cachePath + fileName;
             try {
                 os = new FileOutputStream(fn);
                 flush();
                 os.flush();
                 storeCacheEntry(id, fileName, conn);
             } catch (Exception e) {
                 try {
                     File file = new File(fn);
                     if (file.exists()) {
                         file.delete();
                     }
                 } catch (Exception ee) {
                 }
             } finally {
                 if (os != null) {
                     os.close();
                 }
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.CachableIF#clearCache(java.lang.String)
      */
     @Override
     public void clearCache(String id) throws Exception {
 
         String fn = deleteCacheEntry(id, conn);
         File file = new File(cachePath + fn);
         if (file.exists() && file.isFile()) {
             file.delete();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.CachableIF#setCachePath(java.lang.String)
      */
     @Override
     public void setCachePath(String path) throws Exception {
         cachePath = path;
         if (cachePath != null) {
             cachePath.trim();
             if (!cachePath.endsWith(File.separator)) {
                 cachePath = cachePath + File.separator;
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.CachableIF#isCached(java.lang.String)
      */
     @Override
     public boolean isCached(String id) throws Exception {
         if (searchEngine == null) {
             throw new Exception("DstPdfGuideline.isCached(): missing searchEngine!");
         }
 
         cacheFileName = searchEngine.getCacheFileName(id, "dst", "pdf");
         if (Util.isEmpty(cacheFileName)) {
             return false;
         }
 
         // if the file is referenced in CACHE table, but does not actually exist, we say false
         File file = new File(cachePath + cacheFileName);
         if (!file.exists()) {
             cacheFileName = null;
             return false;
         }
 
         return true;
     }
 
     /*
      * Called when the output is present in cache. Writes the cached document into the output stream.
      */
     public void writeFromCache() throws Exception {
 
         if (Util.isEmpty(cachePath)) {
             throw new Exception("Cache path is missing!");
         }
         if (Util.isEmpty(cacheFileName)) {
             throw new Exception("Cache file name is missing!");
         }
 
         fileName = cacheFileName;
 
         String fullName = cachePath + cacheFileName;
         File file = new File(fullName);
         if (!file.exists()) {
             throw new Exception("Cache file <" + fullName + "> does not exist!");
         }
 
         int i = 0;
         byte[] buf = new byte[1024];
         FileInputStream in = null;
         try {
             in = new FileInputStream(file);
             while ((i = in.read(buf, 0, buf.length)) != -1) {
                 os.write(buf, 0, i);
             }
         } finally {
             if (in != null) {
                 in.close();
             }
         }
     }
 
     /*
      * Overriding flush() to check if content has been written from cache
      */
     @Override
     public void flush() throws Exception {
         if (cacheFileName != null) {
             os.flush();
         } else {
             super.flush();
         }
     }
 
     /**
      * 
      * @param id
      * @param fn
      * @throws SQLException
      */
     @SuppressWarnings({"rawtypes", "unchecked"})
     protected static int storeCacheEntry(String id, String fn, Connection conn) throws SQLException {
 
         if (id == null || fn == null || conn == null) {
             return -1;
         }
 
         INParameters inParams = new INParameters();
 
         PreparedStatement stmt = null;
         try {
             // first delete the old entry
             StringBuffer buf =
                     new StringBuffer().append("delete from CACHE where OBJ_TYPE='dst' and ARTICLE='pdf' and OBJ_ID=").append(
                             inParams.add(id, Types.INTEGER));
             stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
             stmt.executeUpdate();
             stmt.close();
 
             // now create the new entry
             inParams = new INParameters();
             LinkedHashMap map = new LinkedHashMap();
             map.put("OBJ_ID", inParams.add(id, Types.INTEGER));
             map.put("OBJ_TYPE", SQL.surroundWithApostrophes("dst"));
             map.put("ARTICLE", SQL.surroundWithApostrophes("pdf"));
             map.put("FILENAME", SQL.surroundWithApostrophes(fn));
             map.put("CREATED", inParams.add(String.valueOf(System.currentTimeMillis()), Types.BIGINT));
 
             stmt = SQL.preparedStatement(SQL.insertStatement("CACHE", map), inParams, conn);
             return stmt.executeUpdate();
         } finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
             } catch (SQLException e) {
             }
         }
     }
 
     /**
      * 
      * @param id
      * @return
      * @throws SQLException
      */
     protected static String deleteCacheEntry(String id, Connection conn) throws SQLException {
 
         if (id == null || conn == null) {
             return null;
         }
 
         INParameters inParams = new INParameters();
         StringBuffer buf =
                 new StringBuffer("select FILENAME from CACHE where ").append("OBJ_TYPE='dst' and ARTICLE='pdf' and OBJ_ID=")
                         .append(inParams.add(id, Types.INTEGER));
 
         String fn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
             rs = stmt.executeQuery();
             if (rs.next()) {
                 fn = rs.getString(1);
                 inParams = new INParameters();
                 buf =
                         new StringBuffer("delete from CACHE where ").append("OBJ_TYPE='dst' and ARTICLE='pdf' and OBJ_ID=")
                                 .append(inParams.add(id, Types.INTEGER));
                 stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
                 stmt.executeUpdate();
             }
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
             } catch (SQLException e) {
             }
         }
 
         return fn;
     }
 }
