 /**
  * 
  */
 package de.aidger.utils.pdf;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.Color;
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import com.itextpdf.text.BaseColor;
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
 import de.aidger.model.budgets.BudgetCreator;
 import de.aidger.model.budgets.BudgetFilter;
 import de.aidger.model.budgets.CourseBudget;
 import de.aidger.model.models.Course;
 import de.aidger.view.UI;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * This class converts budget reports to a format for itext and exports it.
  * 
  * @author aidGer Team
  */
 public class BudgetReportConverter {
 
     /**
      * The PDF-document which will be created.
      */
     private Document document = null;
 
     /**
      * The PdfWriter used to write the document.
      */
     private PdfWriter writer = null;
 
     /**
      * The filters used for this budget report.
      */
     private BudgetFilter filters = null;
 
     /**
      * Whether the file was created successfully.
      */
     private boolean fileCreated = false;
 
     /**
      * The name of this report.
      */
     private static String name;
 
     /**
      * Initializes this BalanceReportConverter with a given file and filters for
      * the courses.
      * 
      * @param file
      *            The file to create the pdf in.
      * @param filters
      *            The filters with which to filter the courses.
      */
     public BudgetReportConverter(File file, BudgetFilter filters) {
         document = new Document(PageSize.A4);
         document.setMargins(document.leftMargin(), document.rightMargin(),
             document.topMargin() + 40, document.bottomMargin());
         this.filters = filters;
         file = checkExtension(file);
         name = _("Budget report");
         makeNewDocument(file);
         if (fileCreated) {
             createCourses();
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
      *            The file.
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
                 table.setWidths(new int[] { 28, 20, 2 });
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
      * Creates all the course budgets and writes them into a table.
      */
     private void createCourses() {
         try {
             PdfPTable contentTable = new PdfPTable(1);
             PdfPCell contentCell = new PdfPCell(createTableHeader());
 
             contentCell.setBorder(0);
             contentTable.addCell(contentCell);
 
             BudgetCreator budgetCreator = new BudgetCreator();
             List<ICourse> courses = (new Course()).getAll();
             /*
              * Get course budgets for all courses along with the filter
              * criteria.
              */
             for (ICourse course : courses) {
                 budgetCreator.addCourseBudget(new Course(course), filters);
             }
             ArrayList<CourseBudget> courseBudgets = budgetCreator
                 .getCourseBudgets();
             /*
              * Add a new row for every course that passed the filters.
              */
             for (CourseBudget courseBudget : courseBudgets) {
                 contentCell = new PdfPCell(addRow(budgetCreator
                     .getObjectArray(courseBudget)));
                 contentCell.setBorder(0);
                 contentTable.addCell(contentCell);
             }
             document.add(contentTable);
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     /**
      * Creates the titles of the table.
      * 
      * @return The table with the titles.
      */
     private PdfPTable createTableHeader() {
         try {
             Font tableTitleFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED), 9);
             PdfPTable tableHeader = new PdfPTable(new float[] { 0.3f, 0.15f,
                     0.15f, 0.15f, 0.15f, 0.10f });
             String[] titles = { _("Course"), _("Semester"), _("Lecturer"),
                     _("Booked budgets"), _("Available budgets"),
                     _("Total Budgets") };
             /*
              * Add the title of every column.
              */
             for (int i = 0; i < titles.length; i++) {
                 PdfPCell cell = new PdfPCell(new Phrase(titles[i],
                     tableTitleFont));
                 tableHeader.addCell(cell);
             }
             PdfPTable tableHeaderTable = new PdfPTable(1);
             PdfPCell cell = new PdfPCell(tableHeader);
             cell.setPaddingBottom(1);
             cell.setBorder(0);
             tableHeaderTable.addCell(cell);
             return tableHeaderTable;
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Adds a row, containing a course budget, to the table.
      * 
      * @param objectArray
      *            The course budget.
      * @return The table containing the row.
      */
     private PdfPTable addRow(Object[] objectArray) {
         try {
             Font tableContentFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 9);
             PdfPTable tableContent = new PdfPTable(new float[] { 0.3f, 0.15f,
                     0.15f, 0.15f, 0.15f, 0.10f });
             Color cellColor = new Color(255, 255, 255);
             if (Double.parseDouble(objectArray[4].toString()) == 0) {
                 cellColor = new Color(252, 200, 150);
             }
             for (int i = 0; i < objectArray.length; i++) {
                 PdfPCell cell = new PdfPCell(new Phrase(objectArray[i]
                     .toString(), tableContentFont));
                 cell.setBackgroundColor(new BaseColor(cellColor));
                 cell.setPaddingBottom(5);
                 tableContent.addCell(cell);
             }
             return tableContent;
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
 }
