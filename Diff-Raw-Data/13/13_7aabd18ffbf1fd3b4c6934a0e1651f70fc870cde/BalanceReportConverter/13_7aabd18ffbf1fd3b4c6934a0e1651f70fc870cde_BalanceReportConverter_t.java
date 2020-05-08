 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.utils.pdf;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
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
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfImportedPage;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPageEventHelper;
 import com.itextpdf.text.pdf.PdfReader;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.aidger.model.Runtime;
 import de.aidger.model.models.Course;
 import de.aidger.model.reports.BalanceCourse;
 import de.aidger.model.reports.BalanceFilter;
 import de.aidger.model.reports.BalanceReportGroupCreator;
 import de.aidger.model.reports.BalanceCourse.BudgetCost;
 import de.aidger.utils.DateUtils;
 import de.aidger.utils.Logger;
 import de.aidger.utils.reports.BalanceHelper;
 import de.aidger.view.UI;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * This class converts balance reports to a format for iText and exports it to a
  * .pdf file.
  * 
  * @author aidGer Team
  */
 public class BalanceReportConverter {
 
     /**
      * The calculation method to be used for this balance report.
      */
     private int calculationMethod = 0;
 
     /**
      * The PDF-document which will be created.
      */
     private Document document = null;
 
     /**
      * Contains the group tables and their names.
      */
     @SuppressWarnings("unchecked")
     private ArrayList balanceReportGroups = null;
 
     /**
      * The PdfWriter used to write the document.
      */
     private PdfWriter writer = null;
 
     /**
      * The filters to use for this converter.
      */
     private BalanceFilter filters = null;
 
     /**
      * The balance helper used to filter the courses for this converter.
      */
     private BalanceHelper balanceHelper = null;
 
     /**
      * The group creators of this balance report.
      */
     private BalanceReportGroupCreator balanceReportGroupCreator = null;
 
     /**
      * Whether the file was created successfully.
      */
     private boolean fileCreated = false;
 
     /**
      * The name of this report
      */
     private static String name;
 
     /**
      * The sums of this report.
      */
     private ArrayList<Object> sums;
 
     /**
      * Initializes this BalanceReportConverter with a given path and a name.
      * 
      * @param file
      *            The file of this report.
      * @param index
      *            What kind of report this is.
      * @param semester
      *            The semester of the report.
      * @param filters
      *            The filters for the report.
      * @param calculationMethod
      *            The calculation method of this report.
      */
     @SuppressWarnings("unchecked")
     public BalanceReportConverter(File file, int index, Object semester,
             BalanceFilter filters, int calculationMethod) {
         document = new Document(PageSize.A4.rotate());
         document.setMargins(document.leftMargin(), document.rightMargin(),
             document.topMargin() + 25, document.bottomMargin());
         this.filters = filters;
         balanceHelper = new BalanceHelper();
         this.calculationMethod = calculationMethod;
         file = checkExtension(file);
         switch (index) {
         case 1:
             name = _("Full balance report");
             break;
         case 2:
             name = _("Annual balance report") + " - " + semester;
             break;
         case 3:
             name = _("Semester balance report") + " - " + semester;
             break;
         }
         File preTemplateFile = null;
         try {
             preTemplateFile = File.createTempFile("BalanceReport", ".pdf");
         } catch (IOException e2) {
             e2.printStackTrace();
         }
         makeNewDocument(preTemplateFile);
         if (fileCreated) {
             switch (index) {
             case 1:
                 ArrayList semesters = new BalanceHelper().getSemesters();
                 for (int i = 0; i < semesters.size(); i++) {
                     if (new BalanceHelper().courseExists((String) semesters
                         .get(i), filters)) {
                         createSemester((String) semesters.get(i));
                     }
                 }
                 break;
             case 2:
                 createYear((Integer) semester);
                 break;
             case 3:
                 createSemester("" + semester);
                 break;
             }
             document.close();
             applyTemplate(file, preTemplateFile);
             preTemplateFile.delete();
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
      * Places the created document onto the template for this report.
      * 
      * @param file
      *            The file to which this report will be saved.
      * @param preTemplateFile
      *            The report to be used.
      */
     private void applyTemplate(File file, File preTemplateFile) {
         FileOutputStream outStream = null;
         FileInputStream inStream = null;
         PdfContentByte contentByte = null;
         PdfReader reader = null, templateReader = null;
         try {
             /*
              * Use the template located in the configuration path first, if it
              * exists.
              */
             File template = new File(Runtime.getInstance().getConfigPath()
                     + "/templates/BalanceReportTemplate.pdf");
             URL templateURL = null;
             if (template.exists()) {
                 templateURL = template.toURI().toURL();
             } else {
                 templateURL = getClass().getResource(
                     "/de/aidger/res/pdf/BalanceReportTemplate.pdf");
             }
             Document document = new Document(PageSize.A4.rotate());
             outStream = new FileOutputStream(file.getPath());
             inStream = new FileInputStream(preTemplateFile);
             writer = PdfWriter.getInstance(document, outStream);
             document.open();
             contentByte = writer.getDirectContent();
             reader = new PdfReader(inStream);
             templateReader = new PdfReader(templateURL);
             /*
              * Add the template pdf to the document and place the finished
              * report on top of it.
              */
             for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                 document.newPage();
                 PdfImportedPage page = writer
                     .getImportedPage(templateReader, 1);
                 int rotation = templateReader.getPageRotation(1);
                 if (rotation == 90 || rotation == 270) {
                     //landscape mode
                     contentByte.addTemplate(page, 0, -1f, 1f, 0, 0, reader
                         .getPageSizeWithRotation(1).getHeight());
                 } else {
                     //portrait mode
                     contentByte.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                 }
                 page = writer.getImportedPage(reader, i);
                 rotation = reader.getPageRotation(i);
                 if (rotation == 90 || rotation == 270) {
                     //landscape mode
                     contentByte.addTemplate(page, 0, -1f, 1f, 0, 0, reader
                         .getPageSizeWithRotation(1).getHeight());
                 } else {
                     //portrait mode
                     contentByte.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                 }
             }
             document.close();
         } catch (DocumentException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Creates a new document.
      * 
      * @param path
      *            The path at which to create the PDF-document.
      * @param name
      *            The desired name of the PDF-file.
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
                 right.setVerticalAlignment(Element.ALIGN_TOP);
                 right.setHorizontalAlignment(Element.ALIGN_RIGHT);
 
                 left.setBorder(0);
                 center.setBorder(0);
                 right.setBorder(0);
 
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
                 Font generatedByFont = new Font(BaseFont.createFont(
                     BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 8);
                 table.setWidths(new int[] { 28, 20, 2 });
                 table.setTotalWidth(writer.getPageSize().getRight()
                         - document.rightMargin() - document.leftMargin());
                 table.setLockedWidth(true);
                 table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                 table.getDefaultCell().setFixedHeight(20);
                 table.getDefaultCell().setHorizontalAlignment(
                     Element.ALIGN_RIGHT);
                Calendar calendar = Calendar.getInstance();
                PdfPCell dateCell = new PdfPCell(new Phrase(DateUtils
                    .formatDate(calendar.getTime())));
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
                 Image aidger = Image.getInstance(getClass().getResource(
                     "/de/aidger/res/pdf/AidgerLogo.png"));
                 aidger.scaleAbsolute(80.0f, 20.0f);
                 table = new PdfPTable(2);
                 table.setTotalWidth(writer.getPageSize().getRight()
                         - document.rightMargin() - document.leftMargin());
                 cell = new PdfPCell(new Phrase(_("Generated by: "),
                     generatedByFont));
                 cell.setBorder(0);
                 cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                 cell.setPaddingBottom(5);
                 cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                 table.addCell(cell);
                 cell = new PdfPCell(Image.getInstance(aidger));
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setBorder(0);
                 table.addCell(cell);
                 table.writeSelectedRows(0, -1, document.leftMargin(), 25,
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
      * Writes a semester table and adds the groups of that semester to it.
      * 
      * @param semester
      *            The name of the semester to be added.
      */
     @SuppressWarnings("unchecked")
     private void createSemester(String semester) {
        balanceReportGroups = new ArrayList<ArrayList>();
         try {
             Font semesterTitleFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED),
                 13);
             Font semesterNameFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 13);
 
             PdfPTable finalSemesterTable = new PdfPTable(1);
 
             PdfPTable semesterTitleTable = new PdfPTable(new float[] { 0.2f,
                     0.8f });
 
             PdfPCell semesterCell = new PdfPCell(new Phrase(_("Semester"),
                 semesterTitleFont));
             semesterCell.setBorder(0);
             semesterTitleTable.addCell(semesterCell);
 
             semesterCell = new PdfPCell(new Phrase(semester, semesterNameFont));
             semesterCell.setBorder(0);
             semesterTitleTable.addCell(semesterCell);
 
             semesterCell = new PdfPCell(semesterTitleTable);
             semesterCell.setBorder(0);
             semesterCell.setPaddingTop(7.0f);
             finalSemesterTable.addCell(semesterCell);
 
             PdfPTable contentTable = new PdfPTable(1);
 
             /*
              * As long as there are groups for this Balance report, create new
              * group tables.
              */
             List<Course> courses = null;
             courses = (new Course()).getCoursesBySemester(semester);
             List<Course> filteredCourses = balanceHelper.filterCourses(courses,
                 filters);
             for (ICourse course : filteredCourses) {
                 PdfPTable groupTable = null;
                 if (balanceReportGroups.isEmpty()) {
                     groupTable = createGroup(course);
                 } else {
                     boolean foundGroup = false;
                     for (int i = 0; i <= balanceReportGroups.size() - 1; i++) {
                         if (((ArrayList) balanceReportGroups.get(i)).get(1)
                             .equals(course.getGroup())) {
                             foundGroup = true;
                             break;
                         }
                     }
                     if (!foundGroup) {
                         groupTable = createGroup(course);
                     }
                 }
             }
             for (int i = 0; i <= balanceReportGroups.size() - 1; i++) {
                 PdfPCell cell = new PdfPCell(
                     (PdfPTable) ((ArrayList) balanceReportGroups.get(i)).get(0));
                 cell.setBorder(0);
                 cell.setPaddingBottom(8.0f);
                 contentTable.addCell(cell);
             }
             PdfPCell contentCell = new PdfPCell(contentTable);
             contentCell.setPaddingLeft(10.0f);
             contentCell.setBorder(1);
             finalSemesterTable.addCell(contentCell);
             finalSemesterTable.setKeepTogether(true);
             document.add(finalSemesterTable);
         } catch (Exception e) {
             Logger.error(e.getMessage());
         }
     }
 
     /**
      * Creates a row with the data of one course and returns it.
      * 
      * @param course
      *            The course of which the data shall be written to a row.
      * @return The row as a PdfPCell
      */
     private PdfPCell addRow(BalanceCourse balanceCourse, float[] columnwidths,
             ArrayList<Integer> costUnits) {
         PdfPTable groupContentTable = new PdfPTable(columnwidths);
         ArrayList<Object> rowObjectVector = new ArrayList<Object>();
         Font tableContentFont;
         try {
             tableContentFont = new Font(BaseFont.createFont(BaseFont.HELVETICA,
                 BaseFont.CP1252, BaseFont.EMBEDDED), 9);
             for (int i = 0; i < 6; i++) {
                 rowObjectVector.add((balanceCourse).getCourseObject()[i]);
             }
 
             sums.set(4, (Double) sums.get(4) + (Double) rowObjectVector.get(4));
             sums.set(5, (Double) sums.get(5) + (Double) rowObjectVector.get(5));
 
             int i = 0;
             for (Integer costUnit : costUnits) {
                 boolean foundCostUnit = false;
                 for (BudgetCost budgetCost : balanceCourse.getBudgetCosts()) {
                     int budgetCostId = budgetCost.getId();
                     /*
                      * Set the budget cost of the cost unit to the one
                      * specified.
                      */
                     if (budgetCostId == costUnit) {
                         rowObjectVector.add(budgetCost.getValue());
                         foundCostUnit = true;
                     }
                 }
                 if (!foundCostUnit) {
                     rowObjectVector.add(0.0);
                 }
                 if (sums.size() < rowObjectVector.size()) {
                     sums.add(rowObjectVector.get(rowObjectVector.size() - 1));
                 } else {
                     sums.set(rowObjectVector.size() - 1, (Double) sums
                         .get(rowObjectVector.size() - 1)
                             + (Double) rowObjectVector.get(rowObjectVector
                                 .size() - 1));
                 }
             }
             for (Object courseObject : rowObjectVector.toArray()) {
                 PdfPCell cell = null;
                 if (courseObject.getClass().equals(Double.class)) {
                     /*
                      * Round to the configured precision.
                      */
                     int decimalPlace = Integer.parseInt(Runtime.getInstance()
                         .getOption("rounding", "2"));
                     double rounded = new BigDecimal((Double) courseObject)
                         .setScale(decimalPlace, BigDecimal.ROUND_HALF_EVEN)
                         .doubleValue();
                     cell = new PdfPCell(new Phrase(new BigDecimal(rounded)
                         .setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(),
                         tableContentFont));
                 } else {
                     cell = new PdfPCell(new Phrase(courseObject.toString(),
                         tableContentFont));
                 }
                 if (i != 0) {
                     cell.setBorder(4 + Rectangle.BOTTOM);
                 } else {
                     cell.setBorder(0 + Rectangle.BOTTOM);
                     i++;
                 }
                 cell.setPaddingBottom(5);
                 groupContentTable.addCell(cell);
             }
             PdfPCell cell = new PdfPCell(groupContentTable);
             cell.setBorder(0);
             return cell;
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Creates a new group table with the title of the group. Adds the table and
      * its title to the group table vector.
      * 
      * @param course
      *            The course for which a group shall be created.
      * @return The PdfPTable of the group.
      */
     @SuppressWarnings("unchecked")
     private PdfPTable createGroup(ICourse course) {
         balanceReportGroupCreator = new BalanceReportGroupCreator(course,
             calculationMethod);
         List<Course> courses = null;
         sums = new ArrayList<Object>();
         try {
             courses = (new Course()).getCoursesBySemester(course.getSemester());
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         }
         List<Course> filteredCourses = balanceHelper.filterCourses(courses,
             filters);
         ArrayList<Integer> addedCourses = new ArrayList<Integer>();
         addedCourses.add(course.getId());
         for (Course filteredCourse : filteredCourses) {
             if (!addedCourses.contains(filteredCourse.getId())
                     && filteredCourse.getGroup().equals(course.getGroup())) {
                 balanceReportGroupCreator.addCourse(filteredCourse);
                 addedCourses.add(filteredCourse.getId());
             }
         }
         ArrayList<Integer> costUnits = new ArrayList<Integer>();
         ArrayList<BalanceCourse> balanceCourses = balanceReportGroupCreator
             .getBalanceCourses();
         ArrayList<String> titles = new ArrayList<String>();
         String[] courseTitles = { _("Title"), _("Part"), _("Lecturer"),
                 _("Target Audience"), _("Planned AWS"), _("Basic needed AWS") };
         for (int i = 0; i < courseTitles.length; i++) {
             titles.add(courseTitles[i]);
         }
         for (Object balanceCourse : balanceCourses) {
             for (BudgetCost budgetCost : ((BalanceCourse) balanceCourse)
                 .getBudgetCosts()) {
                 int budgetCostId = budgetCost.getId();
                 String budgetCostName = budgetCost.getName();
                 if (!costUnits.contains(budgetCostId)) {
                     costUnits.add(budgetCostId);
                     titles.add(_("Budget costs from") + " " + budgetCostName);
                 }
             }
         }
         int columnCount = titles.size();
         Font tableTitleFont;
         Font tableContentFont;
         try {
             tableContentFont = new Font(BaseFont.createFont(BaseFont.HELVETICA,
                 BaseFont.CP1252, BaseFont.EMBEDDED), 9);
             tableTitleFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED), 9);
 
             Font groupTitleFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED),
                 11);
             Font groupNameFont = new Font(BaseFont.createFont(
                 BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 11);
 
             PdfPTable groupTable = new PdfPTable(1);
             PdfPTable groupNameTable = new PdfPTable(new float[] { 0.2f, 0.8f });
             PdfPCell groupTitle = new PdfPCell(new Phrase(_("Group"),
                 groupTitleFont));
             groupTitle.setBorder(2);
             groupNameTable.addCell(groupTitle);
             PdfPCell groupName = new PdfPCell(new Phrase(course.getGroup(),
                 groupNameFont));
             groupName.setBorder(2);
             groupNameTable.addCell(groupName);
             PdfPCell groupContent = new PdfPCell(groupNameTable);
             groupContent.setBorder(0);
             groupContent.setPaddingTop(3.0f);
             groupContent.setPaddingBottom(2.0f);
             groupTable.addCell(groupContent);
             float[] columnWidths = new float[columnCount];
             columnWidths[0] = (200 / columnCount);
             columnWidths[1] = (50 / columnCount);
             columnWidths[2] = (100 / columnCount);
             columnWidths[3] = (150 / columnCount);
             columnWidths[4] = (100 / columnCount);
             for (int i = 5; i < columnCount; i++) {
                 columnWidths[i] = (100 / columnCount);
             }
             PdfPTable groupContentTable = new PdfPTable(columnWidths);
             /*
              * Create the titles of the table entries.
              */
             for (int i = 0; i < titles.size(); i++) {
                 PdfPCell cell = new PdfPCell(new Phrase(titles.get(i),
                     tableTitleFont));
                 if (i != 0) {
                     cell.setBorder(6);
                 } else {
                     cell.setBorder(2);
                 }
                 groupContentTable.addCell(cell);
             }
 
             PdfPCell cell = new PdfPCell(groupContentTable);
             cell.setBorder(0);
             groupTable.addCell(cell);
             sums.add(_("Sum"));
             sums.add("");
             sums.add("");
             sums.add("");
             sums.add(0.0);
             sums.add(0.0);
             for (Object balanceCourse : balanceCourses) {
                 groupTable.addCell(addRow((BalanceCourse) balanceCourse,
                     columnWidths, costUnits));
             }
             PdfPTable emptyRow = new PdfPTable(columnWidths);
             PdfPTable sumRow = new PdfPTable(columnWidths);
             for (int i = 0; i < sums.size(); i++) {
                 cell = new PdfPCell(new Phrase(""));
                 if (i != 0) {
                     cell.setBorder(Rectangle.TOP + Rectangle.LEFT);
                 } else {
                     cell.setBorder(Rectangle.TOP);
                 }
                 emptyRow.addCell(cell);
                 if (i < 4) {
                     cell = new PdfPCell(new Phrase(new Phrase(sums.get(i)
                         .toString(), tableContentFont)));
                 } else {
                     /*
                      * Round to the configured precision.
                      */
                     int decimalPlace = Integer.parseInt(Runtime.getInstance()
                         .getOption("rounding", "2"));
                     double rounded = new BigDecimal((Double) sums.get(i))
                         .setScale(decimalPlace, BigDecimal.ROUND_HALF_EVEN)
                         .doubleValue();
                     cell = new PdfPCell(new Phrase(new BigDecimal(rounded)
                         .setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(),
                         tableContentFont));
                 }
                 if (i != 0) {
                     cell.setBorder(Rectangle.TOP + Rectangle.LEFT);
                 } else {
                     cell.setBorder(Rectangle.TOP);
                 }
                 sumRow.addCell(cell);
             }
             cell = new PdfPCell(emptyRow);
             cell.setBorder(0);
             groupTable.addCell(cell);
             cell = new PdfPCell(sumRow);
             cell.setBorder(0);
             groupTable.addCell(cell);
             groupTable.setKeepTogether(true);
 
             balanceReportGroups.add(new ArrayList<Object>());
             int i = balanceReportGroups.size() - 1;
             ((ArrayList) balanceReportGroups.get(i)).add(groupTable);
             ((ArrayList) balanceReportGroups.get(i)).add(course.getGroup());
 
             return groupTable;
         } catch (DocumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Creates the balance reports of the year by calling the functions to
      * create all semesters of the year.
      * 
      * @param year
      *            The year to be added.
      */
     private void createYear(int year) {
         // Lose the first two numbers of the year
         int semester = year % 100;
         /*
          * Contains the year in YYYY form, the previous, current and next
          * semester in that order.
          */
         String[] semesters = new String[4];
         semesters[0] = Integer.toString(year);
         switch (semester) {
         /*
          * If the given year is 2001-2008, (year % 100) will give a single
          * number below 9. Therefore, the previous, current and next semester
          * all need a leading 0 added.
          */
         case 0:
             semesters[1] = "WS" + "99" + "00";
             semesters[2] = "SS" + "00";
             semesters[3] = "WS" + "00" + "01";
             break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
             semesters[1] = "WS0" + (semester - 1) + "0" + semester;
             semesters[2] = "SS0" + semester;
             semesters[3] = "WS0" + semester + "0" + (semester + 1);
             break;
         /*
          * If the given year is 2009, the previous and current semester will
          * both be a single number and therefore need a leading 0 added. The
          * next semester will be 10 and thus needs no adjustments.
          */
         case 9:
             semesters[1] = "WS0" + (semester - 1) + "0" + semester;
             semesters[2] = "SS0" + semester;
             semesters[3] = "WS0" + semester + (semester + 1);
             break;
         /*
          * If the given year is 2010, the current and next semesters will be 10
          * and 11 and therefore don't need a leading 0. The previous semester
          * will be 9 though.
          */
         case 10:
             semesters[1] = "WS0" + (semester - 1) + semester;
             semesters[2] = "SS" + semester;
             semesters[3] = "WS" + semester + (semester + 1);
             break;
         case 99:
             semesters[1] = "WS" + (semester - 1) + semester;
             semesters[2] = "SS" + semester;
             semesters[3] = "WS" + semester + "00";
             break;
         /*
          * In all other relevant cases (11 and higher), the semesters can be
          * used the way (year % 100) returns them.
          */
         default:
             semesters[1] = "WS" + (semester - 1) + semester;
             semesters[2] = "SS" + semester;
             semesters[3] = "WS" + semester + (semester + 1);
             break;
         }
         // Check if the semester has a course and add it if it does.
         for (String currentSemester : semesters) {
             if (courseExists(currentSemester)) {
                 createSemester(currentSemester);
             }
         }
     }
 
     /**
      * Checks if the given semester contains any courses.
      * 
      * @param semester
      *            The semester to check
      * @return true if the semester contains one or more courses.
      */
     private boolean courseExists(String semester) {
         List<ICourse> courses = null;
         try {
             courses = (new Course()).getAll();
         } catch (Exception e) {
             e.printStackTrace();
         }
         // Check for every course, if it belongs to the given semester.
         for (ICourse course : courses) {
             if (course.getSemester().equals(semester)) {
                 return true;
             }
         }
         return false;
     }
 }
