 /**
  * 
  */
 package de.aidger.utils.pdf;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.ColumnText;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPageEventHelper;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.aidger.model.Runtime;
 import de.aidger.view.UI;
 
 /**
  * This class converts controlling reports to a format for itext and exports
  * them
  * 
  * @author aidGer Team
  */
 public class ControllingConverter {
 
     /**
      * The PDF-document which will be created.
      */
     private Document document = null;
 
     /**
      * The PdfWriter used to write the document.
      */
     private PdfWriter writer = null;
 
     /**
      * Whether the file was created successfully.
      */
     private boolean fileCreated = false;
 
     /**
      * The name of this report.
      */
     private static String name;
 
     /**
      * The table rows that this report should contain.
      */
     private final ArrayList<String[]> tableRows;
 
     /**
      * Initializes a new ControllingConverter and creates the .pdf file.
      * 
      * @param file
      *            The file for the report.
      */
     public ControllingConverter(File file, ArrayList<String[]> tableRows) {
         this.tableRows = tableRows;
         document = new Document(PageSize.A4);
         document.setMargins(document.leftMargin(), document.rightMargin(),
             document.topMargin() + 40, document.bottomMargin());
         file = checkExtension(file);
         name = _("Controlling report");
         makeNewDocument(file);
         if (fileCreated) {
             createTable();
             document.close();
             /*
              * Open the created document if the setting is enabled with the
              * specified pdf viewer.
              */
             if (Runtime.getInstance().getOption("auto-open").equals("true")) {
                 try {
                     java.lang.Runtime.getRuntime().exec(
                         new String[] {
                                 Runtime.getInstance().getOption("pdf-viewer"),
                                 file.getAbsolutePath() });
                 } catch (IOException e) {
                     try {
                         Desktop.getDesktop().open(file);
                     } catch (IOException e1) {
                         UI.displayError(_("No pdf viewer could be found!"));
                     }
                 }
             }
         }
     }
 
     /**
      * Checks if the extension of the file is in fact .pdf. If not, it adds the
      * .pdf extension to the file name.
      * 
      * @param file
      *            The file to check.
      * @return The file name with the correct extension.
      */
     private File checkExtension(File file) {
         String fileName = file.getName();
         int fileExtensionStart = fileName.lastIndexOf('.');
         String fileExtension = fileName.substring(fileExtensionStart + 1);
         if (!fileExtension.equals("pdf")) {
             return new File(file.getPath() + ".pdf");
         }
         return file;
     }
 
     /**
      * Creates a new document.
      * 
      * @param file
      *            The file to create.
      */
     private void makeNewDocument(File file) {
         FileOutputStream outStream = null;
         try {
             outStream = new FileOutputStream(file.getPath());
             writer = PdfWriter.getInstance(document, outStream);
             HeaderFooter event = new HeaderFooter();
             writer.setPageEvent(event);
             document.open();
             fileCreated = true;
         } catch (FileNotFoundException e1) {
             UI.displayError(_("File could not be created.") + " "
                     + _("Please close all processes that are using the file."));
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     /**
      * This class is used to write the headers and footers of every page.
      * 
      * @author aidGer Team
      */
     static class HeaderFooter extends PdfPageEventHelper {
 
         /**
          * The template containing the total number of pages.
          */
         PdfTemplate total;
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(com.itextpdf
          * .text.pdf.PdfWriter, com.itextpdf.text.Document)
          */
         @Override
         public void onOpenDocument(PdfWriter writer, Document document) {
             total = writer.getDirectContent().createTemplate(30, 16);
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(com.itextpdf
          * .text.pdf.PdfWriter, com.itextpdf.text.Document)
          */
         @Override
         public void onStartPage(PdfWriter writer, Document document) {
             PdfPTable table = new PdfPTable(3);
             table.setTotalWidth(writer.getPageSize().getRight()
                     - document.rightMargin() - document.leftMargin());
             try {
                 Font pageTitleFont = new Font(BaseFont
                     .createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252,
                         BaseFont.EMBEDDED), 18);
                 Font authorNameFont = new Font(BaseFont.createFont(
                     BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 8);
 
                 PdfPCell left = new PdfPCell();
                 left.setHorizontalAlignment(Element.ALIGN_LEFT);
                 left.setVerticalAlignment(Element.ALIGN_BOTTOM);
                 left.setBorder(Rectangle.BOTTOM);
                 Image img = Image.getInstance(getClass().getResource(
                     "/de/aidger/pdf/UniLogo.png"));
                 img.scaleAbsoluteWidth(150.0f);
                 left.addElement(img);
 
                 PdfPCell center;
                 if (writer.getCurrentPageNumber() == 1) {
                     center = new PdfPCell(new Phrase(name, pageTitleFont));
                 } else {
                     center = new PdfPCell(new Phrase(""));
                 }
                 center.setHorizontalAlignment(Element.ALIGN_CENTER);
                 center.setVerticalAlignment(Element.ALIGN_BOTTOM);
                 center.setBorder(Rectangle.BOTTOM);
 
                 PdfPCell right = new PdfPCell(new Phrase(_("Author") + ": "
                         + Runtime.getInstance().getOption("name"),
                     authorNameFont));
                 right.setVerticalAlignment(Element.ALIGN_BOTTOM);
                 right.setHorizontalAlignment(Element.ALIGN_RIGHT);
                 right.setBorder(Rectangle.BOTTOM);
 
                 left.setPaddingBottom(10);
                 center.setPaddingBottom(10);
                 right.setPaddingBottom(10);
                 table.addCell(left);
                 table.addCell(center);
                 table.addCell(right);
                 table.writeSelectedRows(0, -1, document.leftMargin(), document
                     .getPageSize().getTop() - 15, writer.getDirectContent());
             } catch (DocumentException e1) {
             } catch (IOException e1) {
                 // TODO Auto-generated catch block
                 e1.printStackTrace();
             }
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(com.itextpdf.text
          * .pdf.PdfWriter, com.itextpdf.text.Document)
          */
         @Override
         public void onEndPage(PdfWriter writer, Document document) {
             PdfPTable table = new PdfPTable(3);
             try {
                 Font pageFont = new Font(BaseFont.createFont(
                     BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 12);
                 table.setWidths(new int[] { 20, 28, 2 });
                 table.setTotalWidth(writer.getPageSize().getRight()
                         - document.rightMargin() - document.leftMargin());
                 table.setLockedWidth(true);
                 table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                 table.getDefaultCell().setFixedHeight(20);
                 table.getDefaultCell().setHorizontalAlignment(
                     Element.ALIGN_RIGHT);
                 Calendar calendar = Calendar.getInstance();
                 SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                 PdfPCell dateCell = new PdfPCell(new Phrase(dateFormat
                     .format(calendar.getTime())));
                 dateCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 dateCell.setBorder(Rectangle.BOTTOM);
                 table.addCell(dateCell);
                 table.addCell(new Phrase(_("Page") + ": "
                        + writer.getCurrentPageNumber() + " " + _("of") + " ",
                    pageFont));
                 PdfPCell cell = new PdfPCell(Image.getInstance(total));
                 cell.setBorder(Rectangle.BOTTOM);
                 table.addCell(cell);
                 table.writeSelectedRows(0, -1, document.leftMargin(), 50,
                     writer.getDirectContent());
             } catch (DocumentException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(com.itextpdf
          * .text.pdf.PdfWriter, com.itextpdf.text.Document)
          */
         @Override
         public void onCloseDocument(PdfWriter writer, Document document) {
             try {
                 Font pageFont = new Font(BaseFont.createFont(
                     BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 12);
                 ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                     new Phrase(String.valueOf(writer.getPageNumber() - 1),
                         pageFont), 2, 2, 0);
             } catch (DocumentException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Creates the table of assistants.
      */
     private void createTable() {
         try {
             Font tableTitleFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED), 9);
             String[] courseTitles = { _("Assistant"),
                     _("Planned costs(pre-tax)"), _("Actual costs(pre-tax)"),
                     _("Remark") };
             PdfPTable contentTable = new PdfPTable(1);
             PdfPTable titleTable = new PdfPTable(4);
             /*
              * Create the titles of the table entries.
              */
             for (int i = 0; i < courseTitles.length; i++) {
                 PdfPCell cell = new PdfPCell(new Phrase(courseTitles[i],
                     tableTitleFont));
                 titleTable.addCell(cell);
             }
             PdfPCell cell = new PdfPCell(titleTable);
             cell.setPaddingTop(10.0f);
             cell.setPaddingBottom(2.0f);
             cell.setBorder(0);
             contentTable.addCell(cell);
             document.add(contentTable);
             addRows();
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     /**
      * Adds the rows of assistants to the table.
      */
     private void addRows() {
         try {
             Font tableContentFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 9);
             PdfPTable contentTable = new PdfPTable(4);
             for (String[] row : tableRows) {
                 for (int i = 0; i < row.length; i++) {
                     PdfPCell cell = new PdfPCell(new Phrase(row[i],
                         tableContentFont));
                     cell.setPaddingBottom(5);
                     contentTable.addCell(cell);
                 }
             }
             document.add(contentTable);
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
 }
