 package ch.hsr.waktu.services;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import ch.hsr.waktu.domain.WorkSession;
 
 import com.trolltech.qt.core.QDateTime;
 
 public class IcsParser {
 
     private IcsParser() {
 
     }
 
     /**
      * Parses contents of an .ics file which is used in iCal, Outlook, etc.
      * <p>
      * 
      * <b>ICS FILE FORMAT:</b><br>
      * An .ics file consists of multiple lines, each line beginning with a tag.
      * 
      * 
      * <p>
      * <b>EXAMPLE EVENT:</b> <br>
      * BEGIN:VEVENT<br>
      * CREATED:20100809T062219Z<br>
      * UID:2DE909B3-96C6-4D7B-BB99-5CA7487C43A2<br>
      * DTEND;TZID=Europe/Zurich:20100810T115000<br>
      * TRANSP:OPAQUE<br>
      * SUMMARY:Prog2<br>
      * DTSTART;TZID=Europe/Zurich:20100810T101000<br>
      * DTSTAMP:20100809T062304Z<br>
      * SEQUENCE:4<br>
      * END:VEVENT
      * <p>
      * An WorkSession starts with the tag "BEGIN:VEVENT" and should be closed by
      * "END:VEVENT". A missing "END:VEVENT" could lead to missing WorkSessions
      * as startTime and endTime might be overwritten.
      * <p>
      * Within a WorkSession the following tags may occur in any order:
      * <ul>
      * <li>SUMMARY:* WorkSession Name
      * <li>DTEND;...:* End time
      * <li>DTSTART;...:* Start time
      * </ul>
      * *: Mandatory tags
      * <p>
      * All other tags are ignored.
      * <p>
      * If the Argument filePath is invalid (file non-existent) or null,
      * parseIcsFile() will return null
      * 
      * @param filePath
      *            path to the .ics file to parse.
      * @return a linked list containing all WorkSessions extracted from input
      *         file.
      * @throws WaktuException
      */
     public static ArrayList<WorkSession> parseIcsFile(final String filePath)
             throws WaktuException {
 
         ArrayList<WorkSession> calendar = new ArrayList<WorkSession>();
 
         BufferedReader br;
 
         try {
             br = new BufferedReader(new FileReader(new File(filePath)));
         } catch (Exception e) {
             throw new WaktuException("Error reading File: " + filePath);
         }
 
         String currentLine;
 
         try {
             while ((currentLine = br.readLine()) != null) {
                 WorkSession tmpWs = null;
 
                 if (currentLine.startsWith("BEGIN:VEVENT")) {
 
                     tmpWs = new WorkSession();
 
                     processEventLine(br, tmpWs);
 
                     if (isValidWorkSession(tmpWs)) {
                         calendar.add(tmpWs);
                     }
                 }
             }
         } catch (Exception e) {
            throw new WaktuException("A general problem occurred reading current line");
         }
         try {
             br.close();
         } catch (IOException e) {
             throw new WaktuException("IO problem");
         }
 
         return calendar;
     }
 
     private static void processEventLine(BufferedReader br, WorkSession tmpWs)
             throws IOException {
         String currentLine;
         while (!((currentLine = br.readLine()) == null || currentLine
                 .startsWith("END:VEVENT"))) {
             if (currentLine.contains("SUMMARY:")) {
 
                 tmpWs.setDescription(stripTag(currentLine));
 
             } else if (currentLine.startsWith("DTSTART")) {
 
                 QDateTime startTime = TimeUtil
                         .stringToQDateTime(stripTag(currentLine));
                 tmpWs.setStart(TimeUtil.convertQDateTimeToGregorian(startTime));
 
             } else if (currentLine.startsWith("DTEND")) {
 
                 QDateTime endTime = TimeUtil
                         .stringToQDateTime(stripTag(currentLine));
                 tmpWs.setEnd(TimeUtil.convertQDateTimeToGregorian(endTime));
 
             }
 
         }
     }
 
     private static boolean isValidWorkSession(final WorkSession workSession) {
         if (workSession == null) {
             return false;
         }
         return (workSession.getDescription() != null
                 && workSession.getStart() != null && workSession.getEnd() != null);
     }
 
     /**
      * Strips tags from parsed line.
      * 
      * A .ics tag is a sequence of upper-case letters followed by a colon. Every
      * parsed .ics line starts with a tag and may only contain a single tag.
      * (e.g. stripTag("SUMMARY:CN2:p3") would return "CN2:p3")
      * 
      * @param stringWithTag
      *            a string beginning with an .ics tag
      * @return string without the tag.
      */
     private static String stripTag(final String stringWithTag) {
         return stringWithTag.split(":", 2)[1];
     }
 
 }
