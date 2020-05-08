 package net._87k.blog;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import com.google.appengine.api.datastore.Entity;
 
 public class Path
 {
 
 private static final Logger log = Logger.getLogger(Path.class.getName());
 
 private String year;
 private String month;
 private String day;
 private String entry;
 private long entryId;
 private Calendar calendar;
 
 public
 Path(String path)
 {
     if (path == null) {
         return;
     }
     String[] parts = path.split("/");
     try {
         calendar = Calendar.getInstance();
         int y = 0;
         int m = 0;
         int d = 1;
         if (parts.length > 1) {
             y = Integer.parseInt(parts[1]);
            if (y < 1999 || y > calendar.get(Calendar.YEAR) + 1) {
                 log.fine("Invalid year: " + y);
                 return;
             }
             calendar.set(y, m, d, 0, 0, 0);
         }
         if (parts.length > 2) {
             m = Integer.parseInt(parts[2]) - 1;
             if (m < calendar.getActualMinimum(Calendar.MONTH) ||
                 m > calendar.getActualMaximum(Calendar.MONTH)) {
                 log.fine("Invalid month: " + m);
                 return;
             }
             calendar.set(y, m, d, 0, 0, 0);
         }
         if (parts.length > 3) {
             d = Integer.parseInt(parts[3]);
             if (d < calendar.getActualMinimum(Calendar.DATE) ||
                 d > calendar.getActualMaximum(Calendar.DATE)) {
                 log.fine("Invalid day: " + d);
                 return;
             }
             calendar.set(y, m, d, 0, 0, 0);
         }
         if (parts.length > 4) {
             entryId = Integer.parseInt(parts[4]);
         }
     } catch (NumberFormatException e) {
         log.fine("Invalid path: " + path);
         return;
     }
     
     switch (parts.length) {
     case 5:
         entry = parts[4];
     case 4:
         day = parts[3];
     case 3:
         month = parts[2];
     case 2:
         year = parts[1];
     }
 }
 
 public
 Path(Entity entity)
 {
     if (entity.getKind() == "Entry") {
         calendar = Calendar.getInstance();
         calendar.setTime((Date)entity.getProperty("date"));
         entryId = entity.getKey().getId();
         entry = String.valueOf(entryId);
         year = String.valueOf(calendar.get(Calendar.YEAR));
         month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
         day = String.valueOf(calendar.get(Calendar.DATE));
     }
 }
 
 public boolean
 hasYear()
 {
     return year != null;
 }
 
 public String
 getYear()
 {
     return year;
 }
 
 public boolean
 hasMonth()
 {
     return month != null;
 }
 
 public String
 getMonth()
 {
     return month;
 }
 
 public boolean
 hasDay()
 {
     return day != null;
 }
 
 public String
 getDay()
 {
     return day;
 }
 
 public boolean
 hasEntry()
 {
     return entry != null;
 }
 
 public String
 getEntry()
 {
     return entry;
 }
 
 public long
 getEntryId()
 {
     return entryId;
 }
 
 public Calendar
 getCalendar()
 {
     return calendar;
 }
 
 public String
 toString()
 {
     StringBuilder s = new StringBuilder();
     if (year != null) {
         s.append(year);
     }
     if (month != null) {
         s.append("/");
         s.append(month);
     }
     if (day != null) {
         s.append("/");
         s.append(day);
     }
     if (entry != null) {
         s.append("/");
         s.append(entry);
     }
     return s.toString();
 }
 
 }
