 package net.lshift.timetracking;
 
 import org.apache.log4j.Logger;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class TimeTrackingEntry {
 
     private static final Logger log = Logger.getLogger(TimeTrackingEntry.class);
 
     String user;
     String issue;
     Date date;
     Long timeSpent;
 
     public String getUser() {
         return user;
     }
 
     public String getIssue() {
         return issue;
     }
 
     public Date getDate() {
         return date;
     }
 
     public Long getTimeSpent() {
         return timeSpent;
     }
 
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 
     public TimeTrackingEntry(String line) {
         if (line != null) {
             String[] elements = line.split(",");
             assert elements.length == 4;
            user = elements[0].trim();
            issue = elements[1].trim();
             DateFormat formatter = new SimpleDateFormat("yyyyy-MM-dd");
             try {
                date = formatter.parse(elements[2].trim());
             } catch (ParseException e) {
                 throw new RuntimeException(e);
             }
            timeSpent = Long.parseLong(elements[3].trim());
         }
     }
 
 
     static List<TimeTrackingEntry> parse(InputStream in) throws Exception {
         return parse(new BufferedReader(new InputStreamReader(in)));
     }
 
     static List<TimeTrackingEntry> parse(String s) throws Exception {
         return parse(new BufferedReader(new StringReader(s)));        
     }
 
     static List<TimeTrackingEntry> parse(BufferedReader reader) throws Exception {
         ArrayList<TimeTrackingEntry> results = new ArrayList<TimeTrackingEntry>();
         String line = null;
         while ((line = reader.readLine()) != null) {
             log.debug(line);
             TimeTrackingEntry entry = new TimeTrackingEntry(line);
             results.add(entry);
         }
         return results;
     }
 }
