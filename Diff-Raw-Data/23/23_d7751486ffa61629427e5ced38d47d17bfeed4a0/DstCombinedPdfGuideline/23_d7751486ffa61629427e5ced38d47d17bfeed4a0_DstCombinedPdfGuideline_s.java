 package eionet.meta.exports.pdf;
 
 import java.awt.Color;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.util.ArrayList;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.lowagie.text.Chunk;
 import com.lowagie.text.Document;
 import com.lowagie.text.Element;
 import com.lowagie.text.Font;
 import com.lowagie.text.FontFactory;
 import com.lowagie.text.HeaderFooter;
 import com.lowagie.text.Image;
 import com.lowagie.text.List;
 import com.lowagie.text.ListItem;
 import com.lowagie.text.Paragraph;
 
 import eionet.meta.Dataset;
 import eionet.meta.savers.Parameters;
 import eionet.util.Util;
 
 //TODO there can be some refactorings for all pdf generation package, maybe more inheritance rather than owner or using owner more
 //genericly, or this class can include instance variable of DstPdfGuideline instead of inheriting. there should be common
 //methodology for pdf generation part.
 /**
  * PDF guidelines generator for more than a dataset.
  */
 public class DstCombinedPdfGuideline extends DstPdfGuideline {
 
     private ArrayList<String> dsNames = null;
     private ArrayList<String> dsVersions = null;
     private StringBuffer headerTitle = null;
 
     /**
      * Constructor.
      * 
      * @param conn
      * @param os
      */
     public DstCombinedPdfGuideline(Connection conn, OutputStream os) {
         super(conn, os);
         this.levelOffset = 1;
         super.levelOffset = 1;
     }
 
     @Override
     public void write(String objIDs) throws Exception {
         if (Util.isEmpty(objIDs)) {
             throw new Exception("No Dataset ID specified");
         }
 
         String[] datasetIds = objIDs.split("[:]");
         if (Util.isEmpty(datasetIds[0])) {
             throw new Exception("No Dataset ID specified");
         }
 
         this.dsNames = new ArrayList<String>();
         this.dsVersions = new ArrayList<String>();
         this.headerTitle = new StringBuffer();
         StringBuffer filenames = new StringBuffer();
         for (String dstId : datasetIds) {
             if (!Util.isEmpty(dstId)) {
                 // add a title
                 Dataset ds = super.initializeDataset(dstId);
                 addTitlePageForCombined();
 
                 this.dsNames.add(super.getDsName());
                 this.dsVersions.add(super.getDsVersion());
 
                 super.write(ds);
                 insertPageBreak();
 
                 filenames.append(ds.getIdentifier());
                 filenames.append("-");
                 this.headerTitle.append(ds.getIdentifier());
                this.headerTitle.append("&");
             }
         }
         filenames.deleteCharAt(filenames.length() - 1);
        this.headerTitle.deleteCharAt(this.headerTitle.length() - 1);
         filenames.append(DstPdfGuideline.FILE_EXT);
         this.fileName = filenames.toString();
 
         setHeader("");
         setFooter();
     }// end of method write
 
     @Override
     public void flush() throws Exception {
         super.flush();
     }
 
     @Override
     public void setLogo(String logo) {
     }
 
     @Override
     public void setVisualsPath(String visualsPath) {
     }
 
     @Override
     public void setParameters(Parameters params) {
     }
 
     @Override
     public String getFileName() {
         return this.fileName;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see eionet.meta.exports.pdf.DstPdfGuideline#setHeader(java.lang.String)
      */
     @Override
     protected void setHeader(String title) throws Exception {
         Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
         font.setColor(Color.gray);
 
         Paragraph prg = new Paragraph();
        prg.add(new Chunk("Data Dictionary\n", font));
         prg.setLeading(10 * 1.2f);
 
         int fontSize = 9 - this.dsNames.size() / 5;
         if (fontSize < 5) {
             fontSize = 5;
         }
 
         font = Fonts.getUnicode(fontSize);
         font.setColor(Color.lightGray);
        prg.add(new Chunk("Dataset specifications for " + headerTitle, font));
         prg.add(new Chunk(" * created " + Util.pdfDate(System.currentTimeMillis()), font));
 
         this.header = new HeaderFooter(prg, false);
        header.setBorder(com.lowagie.text.Rectangle.BOTTOM);
    }
 
     /**
      * Override of the method for adding a title page
      */
     @Override
     protected void addTitlePage(Document doc) throws Exception {
         doc.add(new Paragraph("\n\n"));
 
         // data dictionary
         Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
         Paragraph prg = new Paragraph("Data Dictionary", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
 
         int numberOfNewLines = 10 - (this.dsNames.size() / 2);
         if (numberOfNewLines < 2) {
             numberOfNewLines = 2;
         }
         String newLines = StringUtils.repeat("\n", numberOfNewLines);
         doc.add(new Paragraph(newLines));
 
         // full definition
         font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
         prg = new Paragraph("Definitions of", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
         doc.add(new Paragraph("\n"));
 
         int fontSizeForDatasets = 26 - (this.dsNames.size() * 2);
         if (fontSizeForDatasets < 10) {
             fontSizeForDatasets = 10;
         }
 
         // dataset name and version
         List unOrderedList = new List(false);
         font = Fonts.getUnicode(fontSizeForDatasets);
         for (int i = 0; i < this.dsNames.size(); i++) {
             String item = this.dsNames.get(i);
             String dsVersion = this.dsVersions.get(i);
             // version
             if (!Util.isEmpty(dsVersion)) {
                 item += " (Version: " + dsVersion + ")";
             }
             unOrderedList.add(new ListItem(item, font));
         }
 
         doc.add(unOrderedList);
         doc.add(new Paragraph("\n"));
 
         // dataset word
         font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
         prg = new Paragraph("datasets", font);
         prg.setAlignment(Element.ALIGN_CENTER);
         doc.add(prg);
 
         doc.add(new Paragraph("\n\n"));
 
         doc.add(new Paragraph(newLines));
 
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
 
     }// end of method addTitlePage
 }// end of class DstCombinedPdfGuideline
