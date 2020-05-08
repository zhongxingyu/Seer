 package org.mcguppy.eventplaner.reporting.controller;
 
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletResponse;
 import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
 import org.mcguppy.eventplaner.jpa.entities.Shift;
 import org.mcguppy.eventplaner.jpa.entities.StaffMember;
 
 /**
  *
  * @author stefan meichtry
  */
 @Named
 @RequestScoped
 public class ReportingController {
 
     public ReportingController() {
         facesContext = FacesContext.getCurrentInstance();
         jpaController = (StaffMemberJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "staffMemberJpa");
     }
     private StaffMemberJpaController jpaController = null;
     private FacesContext facesContext = null;
     private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
     private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
     private static Font tableHeadFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
     private static Font smallNormal = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
 
     // TODO: exception handling
     public String createShiftPlan() throws DocumentException, FileNotFoundException, IOException {
 
         Document document = new Document();
 
         HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
         response.setContentType("application/pdf");
         response.setHeader("Content-Disposition", "attachment; filename=\"schichtplan.pdf\"");
         PdfWriter.getInstance(document, response.getOutputStream());
 
         document.open();
         addMetaData(document);
         addContent(document);
         document.close();
         
         facesContext.responseComplete();
         return "schiftPlanCreated";
     }
 
     private void addMetaData(Document document) {
         document.addTitle("Schichtplan");
         document.addSubject("Schichtplan für den Event");
         document.addKeywords("Event, PDF, Plan");
         document.addAuthor("Stefan Meichtry");
         document.addCreator("Stefan Meichtry");
     }
 
     private void addContent(Document document) throws DocumentException {
 
         List<StaffMember> staffMembers = jpaController.findStaffMemberEntities();
         Collections.sort(staffMembers);
         PdfPTable staffMemberTable = null;
         for (StaffMember staffMember : staffMembers) {
             if (staffMember.getShifts().isEmpty()) {
                 continue;
             }
 
             // preface
             Paragraph preface = new Paragraph();
             addEmptyLine(preface, 1);
             preface.add(new Paragraph("eventplaner", catFont));
             addEmptyLine(preface, 1);
             document.add(preface);
 
             // staffMember Data
             Paragraph staffMemberDataSection = new Paragraph();
             addEmptyLine(staffMemberDataSection, 1);
             staffMemberDataSection.add(new Paragraph("Personen-Daten:", subFont));
             addEmptyLine(staffMemberDataSection, 1);
             document.add(staffMemberDataSection);
 
             staffMemberTable = new PdfPTable(new float[]{15, 40});
             staffMemberTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 
             staffMemberTable.addCell(new Paragraph("Anrede:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getTitle().toString(), smallNormal));
             staffMemberTable.addCell(new Paragraph("Vorname:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getFirstName(), smallNormal));
             staffMemberTable.addCell(new Paragraph("Name:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getLastName(), smallNormal));
             staffMemberTable.addCell(new Paragraph("Strasse:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getStreet(), smallNormal));
             staffMemberTable.addCell(new Paragraph("PLZ:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getZip(), smallNormal));
             staffMemberTable.addCell(new Paragraph("Ort:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getCity(), smallNormal));
             staffMemberTable.addCell(new Paragraph("Mailadresse:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getMailAddress(), smallNormal));
             staffMemberTable.addCell(new Paragraph("Telefon Nummer:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getPhoneNr(), smallNormal));
             staffMemberTable.addCell(new Paragraph("Natel Nummer:", smallNormal));
             staffMemberTable.addCell(new Paragraph(staffMember.getCellPhoneNr(), smallNormal));
 
 
             document.add(staffMemberTable);
 
             // shift Data
             Paragraph shiftDataSection = new Paragraph();
             addEmptyLine(shiftDataSection, 1);
             shiftDataSection.add(new Paragraph("Schicht-Daten:", subFont));
             addEmptyLine(shiftDataSection, 1);
             document.add(shiftDataSection);
 
             PdfPTable shiftTable = new PdfPTable(new float[]{20, 25, 18, 18, 25});
 
             PdfPCell c1 = new PdfPCell(new Phrase("Standort", tableHeadFont));
             c1.setHorizontalAlignment(Element.ALIGN_CENTER);
             c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
             shiftTable.addCell(c1);
 
             PdfPCell c2 = new PdfPCell(new Phrase("Beschreibung", tableHeadFont));
             c2.setHorizontalAlignment(Element.ALIGN_CENTER);
             c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
             shiftTable.addCell(c2);
 
             PdfPCell c3 = new PdfPCell(new Phrase("Start", tableHeadFont));
             c3.setHorizontalAlignment(Element.ALIGN_CENTER);
             c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
             shiftTable.addCell(c3);
 
             PdfPCell c4 = new PdfPCell(new Phrase("Ende", tableHeadFont));
             c4.setHorizontalAlignment(Element.ALIGN_CENTER);
             c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
             shiftTable.addCell(c4);
 
             PdfPCell c5 = new PdfPCell(new Phrase("Schicht-Verantwortung", tableHeadFont));
             c5.setHorizontalAlignment(Element.ALIGN_CENTER);
             c5.setBackgroundColor(BaseColor.LIGHT_GRAY);
             shiftTable.addCell(c5);
 
             for (Shift shift : staffMember.getShifts()) {
                 shiftTable.addCell(new Paragraph(shift.getLocation().getLocationName(), smallNormal));
                 shiftTable.addCell(new Paragraph(shift.getLocation().getDescription(), smallNormal));
 
                 SimpleDateFormat formatter = new SimpleDateFormat("E. dd.MM.yyyy HH:mm", Locale.GERMAN);
 
                 Date startTime = shift.getStartTime();
                 Date endTime = shift.getEndTime();
 
                 String startTimeString = formatter.format(startTime);
                 String endTimeString = formatter.format(endTime);
 
 
                 shiftTable.addCell(new Paragraph(startTimeString, smallNormal));
                 shiftTable.addCell(new Paragraph(endTimeString, smallNormal));
                 if (shift.getResponsible() == null) {
                     shiftTable.addCell(new Paragraph("", smallNormal));
                 } else {
                     shiftTable.addCell(new Paragraph(shift.getResponsible().toString(), smallNormal));
                 }
             }
 
             document.add(shiftTable);
 
             document.newPage();
 
 
 
         }
     }
 
     private static void addEmptyLine(Paragraph paragraph, int number) {
         for (int i = 0; i < number; i++) {
             paragraph.add(new Paragraph(" "));
         }
     }
 }
