 package ch.bli.mez.controller;
 
 import java.awt.Desktop;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import ch.bli.mez.model.Mission;
 import ch.bli.mez.model.Position;
 import ch.bli.mez.util.Parser;
 import ch.bli.mez.util.TimeEntriesPerEmployee;
 import ch.bli.mez.util.TimeEntriesPerMission;
 import ch.bli.mez.util.TimeEntriesPerPosition;
 
 public class FormatMissionReportController {
 
   public void formatTimeEntries(List<TimeEntriesPerMission> timeEntriesPerMissions,
       List<TimeEntriesPerPosition> summarizedTimeEntries, HashMap<String, Object> formData) {
     showReport(createReport(timeEntriesPerMissions, summarizedTimeEntries, formData));
   }
 
   private String createReport(List<TimeEntriesPerMission> timeEntriesPerMissions,
       List<TimeEntriesPerPosition> summarizedTimeEntries, HashMap<String, Object> formData) {
     Calendar endDate = (Calendar) formData.get("endDate");
    if (endDate == null) {
      endDate = Calendar.getInstance();
      int year = endDate.get(Calendar.YEAR);
      endDate.clear();
      endDate.set(year, 11, 31);
    }
     Calendar startDate = (Calendar) formData.get("startDate");
    if (startDate == null) {
      startDate = Calendar.getInstance();
      int year = startDate.get(Calendar.YEAR);
      startDate.clear();
      startDate.set(year, 0, 1);
    }
 
     Date date = new Date();
     SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 
     String report = "<!Doctype html>";
     report += "<div style='margin: 0 auto; width: 1000px;'>";
    report += "<div style='height: 100px; border-bottom: 1px dotted black;'>" + "<h2>Metzler Orgelbau AG - Auftragsreport</h2>";
    if ((Integer) formData.get("selectedMission") != 2){
    report += "<span>Zeitperiode: " + Parser.parseDateCalendarToString(startDate) + " - "
        + Parser.parseDateCalendarToString(endDate) + "</span>";
    }
    report += "<span style='float: right'>" + f.format(date) + "</span>" + "</div><br>";
     report += "<div style='font-weight: bold; width: 300px; text-align: left; display: inline-block;'>Auftrag</div>";
     report += "<div style='font-weight: bold; width: 350px; text-align: left; display: inline-block;'>Position</div>";
     report += "<div style='font-weight: bold; width: 350px; text-align: left; display: inline-block;'>Person</div>";
     report += "<br>";
 
     for (TimeEntriesPerMission timeEntryPerMission : timeEntriesPerMissions) {
       report += "<div style='display: inline-block; width: 300px;'>";
       report += "<span>" + timeEntryPerMission.getMission().getMissionName() + "</span>";
       report += "<div style='display: inline-block; float: right; margin-right: 50px; text-align: right; font-weight: bold;'>"
           + Parser.parseMinutesIntegerToString(timeEntryPerMission.getTotalTime()) + "</div>";
       report += "</div>";
       report += "<br>";
 
       for (TimeEntriesPerPosition timeEntryPerPosition : timeEntryPerMission.getTimeEntriesPerPositions()) {
         report += "<div style='display: inline-block; width: 350px; margin-left: 300px;'>";
         report += "<span style='padding-right: 25px;'>" + timeEntryPerPosition.getPosition().getCode() + "</span>";
         report += "<span>" + timeEntryPerPosition.getPosition().getPositionName() + "</span>";
         report += "<div style='display: inline-block; float: right; margin-right: 50px; text-align: right; font-weight: bold;'>"
             + Parser.parseMinutesIntegerToString(timeEntryPerPosition.getTotalTime()) + "</div>";
         report += "</div>";
         report += "<br>";
 
         if (timeEntryPerPosition.getShowEmployees()) {
           for (TimeEntriesPerEmployee timeEntriesPerEmployee : timeEntryPerPosition.getTimeEntriesPerEmployees()) {
             report += "<div style='display: inline-block; width: 350px; margin-left: 650px;'>";
             report += "<span style='padding-right: 10px;'>"
                 + Parser.encodeHTML(timeEntriesPerEmployee.getEmployee().getFirstName()) + "</span>";
             report += "<span style='padding-right: 25px;'>"
                 + Parser.encodeHTML(timeEntriesPerEmployee.getEmployee().getLastName()) + "</span>";
             report += "<div style='display: inline-block; text-align: right; float: right; font-weight: bold;'>"
                 + Parser.parseMinutesIntegerToString(timeEntriesPerEmployee.getTotalTime()) + "</div>";
             report += "</div>";
             report += "<br>";
           }
         } else {
           report += "<br>";
         }
       }
       report += "<hr>";
     }
     if (!summarizedTimeEntries.isEmpty()) {
       report += "<div style='display: inline-block; width: 300px;'>";
       report += "<span style='padding-right: 25px;'>Zusammengefasste Positionen:</span>";
       report += "</div>";
       report += "<br>";
 
       for (TimeEntriesPerPosition timeEntryPerPosition : summarizedTimeEntries) {
         report += "<div style='display: inline-block; width: 350px; margin-left: 300px;'>";
         report += "<span style='padding-right: 25px;'>" + timeEntryPerPosition.getPosition().getCode() + "</span>";
         report += "<span>" + timeEntryPerPosition.getPosition().getPositionName() + "</span>";
         report += "<div style='display: inline-block; float: right; margin-right: 50px; text-align: right; font-weight: bold;'>"
             + Parser.parseMinutesIntegerToString(timeEntryPerPosition.getTotalTime()) + "</div>";
         report += "</div>";
         report += "<br>";
         if (timeEntryPerPosition.getShowEmployees()) {
           for (TimeEntriesPerEmployee timeEntriesPerEmployee : timeEntryPerPosition.getTimeEntriesPerEmployees()) {
             report += "<div style='display: inline-block; width: 350px; margin-left: 650px;'>";
             report += "<span style='padding-right: 10px;'>"
                 + Parser.encodeHTML(timeEntriesPerEmployee.getEmployee().getFirstName()) + "</span>";
             report += "<span style='padding-right: 25px;'>"
                 + Parser.encodeHTML(timeEntriesPerEmployee.getEmployee().getLastName()) + "</span>";
             report += "<div style='display: inline-block; text-align: right; float: right; font-weight: bold;'>"
                 + Parser.parseMinutesIntegerToString(timeEntriesPerEmployee.getTotalTime()) + "</div>";
             report += "</div>";
             report += "<br>";
           }
         }
         report += "<hr>";
       }
     }
     report += "</div></html>";
     return report;
   }
 
   private void showReport(String report) {
     File reportFile;
     try {
       reportFile = File.createTempFile("report", ".html");
       BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile), "UTF-8"));
       out.write(report);
       out.close();
       if (Desktop.isDesktopSupported()) {
         Desktop desktop = Desktop.getDesktop();
         try {
           desktop.browse(reportFile.toURI());
         } catch (IOException e) {
           e.printStackTrace();
         }
       } else {
         Runtime runtime = Runtime.getRuntime();
         try {
           runtime.exec("xdg-open " + reportFile.toURI());
         } catch (IOException e) {
           e.printStackTrace();
         }
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 }
