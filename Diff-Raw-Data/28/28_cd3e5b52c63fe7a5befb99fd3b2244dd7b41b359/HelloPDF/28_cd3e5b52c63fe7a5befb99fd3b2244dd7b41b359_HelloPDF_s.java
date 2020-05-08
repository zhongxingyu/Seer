 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ksi;
 
 import java.io.FileOutputStream;
 import java.util.Date;
 
 import com.itextpdf.text.Anchor;
 import com.itextpdf.text.BadElementException;
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Chapter;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.List;
 import com.itextpdf.text.ListItem;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Section;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class HelloPDF {
 
     private static String matchFile = "C:/temp/Match.pdf";
     private static String teamFile = "C:/temp/Team.pdf";
     private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
             Font.BOLD);
     private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
             Font.NORMAL, BaseColor.RED);
     private static Font blueFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
             Font.NORMAL, BaseColor.BLUE);
 
    HelloPDF(String directory) {
        this.matchFile = directory + "/Match.pdf";
        this.teamFile = directory + "/Team.pdf";
     }
 
     public static void createMatchPDF(TeamSheet blue1, TeamSheet blue2, TeamSheet blue3, TeamSheet red1, TeamSheet red2, TeamSheet red3)
             throws DocumentException {
         //Create a document
         Document document = new Document();
         try {
             //Make the PDF writer
             PdfWriter.getInstance(document, new FileOutputStream(matchFile));
         } catch (FileNotFoundException ex) {
         }
 
         document.open();
         //Create a paragraph
         Paragraph content = new Paragraph();
         //Create the table
         PdfPTable table = new PdfPTable(4);
 
         //Add the headers
         PdfPCell c1 = new PdfPCell(new Phrase("Field"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
 
         c1 = new PdfPCell(new Phrase(blue1.getTeamNum()));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
 
         c1 = new PdfPCell(new Phrase(blue2.getTeamNum()));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
 
         c1 = new PdfPCell(new Phrase(blue3.getTeamNum()));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
 
         //Add the cells for number of matches
         table.addCell("# of Matches");
         table.addCell(blue1.getNumMatches() + "");
         table.addCell(blue2.getNumMatches() + "");
         table.addCell(blue3.getNumMatches() + "");
 
         //Add the cells for the starting positions
         table.addCell("Starts Auto");
         table.addCell(blue1.getAutoStart() + "");
         table.addCell(blue2.getAutoStart() + "");
         table.addCell(blue3.getAutoStart() + "");
 
         //Add the cells for auto points
         table.addCell("Auto Pts");
         table.addCell(blue1.getAutoPoints() + "");
         table.addCell(blue2.getAutoPoints() + "");
         table.addCell(blue3.getAutoPoints() + "");
 
         //Add the cells for teleop scoring percentage
         table.addCell("Teleop Scoring %");
         table.addCell(blue1.getTeleScoringPercentage() + "");
         table.addCell(blue2.getTeleScoringPercentage() + "");
         table.addCell(blue3.getTeleScoringPercentage() + "");
 
         //Add the cells for the average points in teleop
         table.addCell("Teleop Avg Pts");
         table.addCell(blue1.getTelePoints() + "");
         table.addCell(blue2.getTelePoints() + "");
         table.addCell(blue3.getTelePoints() + "");
 
         //Add the cells for defensive rating
         table.addCell("Defensive Rating");
         table.addCell(blue1.getDefenseRating() + "");
         table.addCell(blue2.getDefenseRating() + "");
         table.addCell(blue3.getDefenseRating() + "");
 
         //Add the cells for the time required to hang
         table.addCell("Time Hang");
         table.addCell(blue1.getHangTime() + "");
         table.addCell(blue2.getHangTime() + "");
         table.addCell(blue3.getHangTime() + "");
 
         //Add the cells for the level the robot can hang at
         table.addCell("Hang Level");
         table.addCell(blue1.getHangLevel() + "");
         table.addCell(blue2.getHangLevel() + "");
         table.addCell(blue3.getHangLevel() + "");
 
         //Add the cells for the average KPR
         table.addCell("Avg KPR");
         table.addCell(blue1.getKPR() + "");
         table.addCell(blue2.getKPR() + "");
         table.addCell(blue3.getKPR() + "");
 
 
 
         PdfPTable table1 = new PdfPTable(4);
 
         c1 = new PdfPCell(new Phrase("Field"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table1.addCell(c1);
 
         c1 = new PdfPCell(new Phrase(red1.getTeamNum()));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table1.addCell(c1);
 
         c1 = new PdfPCell(new Phrase(red2.getTeamNum()));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table1.addCell(c1);
 
         c1 = new PdfPCell(new Phrase(red3.getTeamNum()));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table1.addCell(c1);
 
 
         //Add the cells for number of matches
         table1.addCell("# of Matches");
         table1.addCell(red1.getNumMatches() + "");
         table1.addCell(red2.getNumMatches() + "");
         table1.addCell(red3.getNumMatches() + "");
 
         //Add the cells for the starting positions
         table1.addCell("Starts Auto");
         table1.addCell(red1.getAutoStart() + "");
         table1.addCell(red2.getAutoStart() + "");
         table1.addCell(red3.getAutoStart() + "");
 
         //Add the cells for auto points
         table1.addCell("Auto Pts");
         table1.addCell(red1.getAutoPoints() + "");
         table1.addCell(red2.getAutoPoints() + "");
         table1.addCell(red3.getAutoPoints() + "");
 
         //Add the cells for teleop scoring percentage
         table1.addCell("Teleop Scoring %");
         table1.addCell(red1.getTeleScoringPercentage() + "");
         table1.addCell(red2.getTeleScoringPercentage() + "");
         table1.addCell(red3.getTeleScoringPercentage() + "");
 
         //Add the cells for the average points in teleop
         table1.addCell("Teleop Avg Pts");
         table1.addCell(red1.getTelePoints() + "");
         table1.addCell(red2.getTelePoints() + "");
         table1.addCell(red3.getTelePoints() + "");
 
         //Add the cells for defensive rating
         table1.addCell("Defensive Rating");
         table1.addCell(red1.getDefenseRating() + "");
         table1.addCell(red2.getDefenseRating() + "");
         table1.addCell(red3.getDefenseRating() + "");
 
         //Add the cells for the time required to hang
         table1.addCell("Time Hang");
         table1.addCell(red1.getHangTime() + "");
         table1.addCell(red2.getHangTime() + "");
         table1.addCell(red3.getHangTime() + "");
 
         //Add the cells for the level the robot can hang at
         table1.addCell("Hang Level");
         table1.addCell(red1.getHangLevel() + "");
         table1.addCell(red2.getHangLevel() + "");
         table1.addCell(red3.getHangLevel() + "");
 
         //Add the cells for the average KPR
         table1.addCell("Avg KPR");
         table1.addCell(red1.getKPR() + "");
         table1.addCell(red2.getKPR() + "");
         table1.addCell(red3.getKPR() + "");
 
         Paragraph paragraph = new Paragraph("Blue Alliance", blueFont);
         paragraph.setAlignment(Element.ALIGN_CENTER);
         content.add(paragraph);
         addEmptyLine(content, 1);
         content.add(table);
         paragraph = new Paragraph("Red Alliance", redFont);
         paragraph.setAlignment(Element.ALIGN_CENTER);
         content.add(paragraph);
         addEmptyLine(content, 1);
         content.add(table1);
         document.add(content);
 
         document.close();
     }
 
     public static void createTeamPDF(ArrayList<MatchSheet> matches)
             throws DocumentException {
         Document document = new Document();
         try {
             PdfWriter.getInstance(document, new FileOutputStream(teamFile));
         } catch (FileNotFoundException ex) {
         }
         document.open();
 
         Paragraph content = new Paragraph();
 
 
         PdfPTable table = new PdfPTable(matches.size() + 2);
 
         PdfPCell c1 = new PdfPCell(new Phrase("Match #"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 
         table.addCell(c1);
 
         for (int i = 0; i < matches.size(); i++) {
             c1 = new PdfPCell(new Phrase(matches.get(i).getMatchNum() + ""));
             c1.setHorizontalAlignment(Element.ALIGN_CENTER);
             table.addCell(c1);
         }
 
         c1 = new PdfPCell(new Phrase("Average"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
 
         
         table.addCell("Starting Position");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getStartingPos());
         }
         table.addCell("N/A");
 
         table.addCell("Auto Points");
         double total = 0;
         for (int i = 0; i < matches.size(); i++) {
             total+=matches.get(i).getAutonPointsScored();
             table.addCell(matches.get(i).getAutonPointsScored() + "");
         }
         table.addCell(total/matches.size() + "");
         
         total = 0;
         table.addCell("Teleop Low Attempted");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsAttemptedLow() + "");
         }
         table.addCell(total/matches.size() + "");
         
         total = 0;
         table.addCell("Teleop Low Scored");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsScoredLow() + "");
         }
         table.addCell(total/matches.size() + "");
 
         total = 0;
         table.addCell("Teleop Middle Attempted");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsAttemptedMiddle() + "");
         }
         table.addCell(total/matches.size() + "");
         
         
         total = 0;
         table.addCell("Teleop Middle Scored");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsScoredMiddle() + "");
         }
         table.addCell(total/matches.size() + "");
 
         total = 0;
         table.addCell("Teleop High Attempted");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsAttemptedHigh() + "");
         }
         table.addCell(total/matches.size() + "");
         
         total = 0;
         table.addCell("Teleop High Scored");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsScoredHigh() + "");
         }
         table.addCell(total/matches.size() + "");
 
         total = 0;
         table.addCell("Teleop Pyramid Attempted");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsAttemptedPyramid() + "");
         }
         table.addCell(total/matches.size() + "");
         
         total = 0;
         table.addCell("Teleop Pyramid Scored");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getShotsScoredPyramid() + "");
         }
         table.addCell(total/matches.size() + "");
 
         total = 0;
         table.addCell("Defense Rating");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getDefense() + "");
         }
         table.addCell(total/matches.size() + "");
 
         total = 0;
         table.addCell("Hang Level");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getHangLevel() + "");
         }
         table.addCell(total/matches.size() + "");
 
         total = 0;
         table.addCell("Hang Time");
         for (int i = 0; i < matches.size(); i++) {
             table.addCell(matches.get(i).getHangTime() + "");
         }
         table.addCell(total/matches.size() + "");
 
         Paragraph paragraph = new Paragraph(matches.get(0).getTeamNum(), catFont);
         paragraph.setAlignment(Element.ALIGN_CENTER);
         content.add(paragraph);
         addEmptyLine(content, 1);
         content.add(table);
 
         document.add(content);
         document.close();
     }
 
     private static void addEmptyLine(Paragraph paragraph, int number) {
         for (int i = 0; i < number; i++) {
             paragraph.add(new Paragraph(" "));
         }
     }
 }
