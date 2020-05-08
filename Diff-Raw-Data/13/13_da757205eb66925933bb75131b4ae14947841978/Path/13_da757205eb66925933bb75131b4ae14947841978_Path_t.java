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
 private String title;
 private long entryId;
 private Calendar calendar;
 
 protected boolean
 initialize(String aYear, String aMonth, String aDay, String anEntry)
 {
     try {
         calendar = Calendar.getInstance();
         int y = 0;
         int m = 0;
         int d = 1;
         if (aYear != null) {
             y = Integer.parseInt(aYear);
             if (y < 1998 || y > calendar.get(Calendar.YEAR) + 1) {
                 log.fine("Invalid year: " + y);
                 return false;
             }
             calendar.set(y, m, d, 0, 0, 0);
         }
         if (aMonth != null) {
             m = Integer.parseInt(aMonth) - 1;
             if (m < calendar.getActualMinimum(Calendar.MONTH) ||
                 m > calendar.getActualMaximum(Calendar.MONTH)) {
                 log.fine("Invalid month: " + m);
                 return false;
             }
             calendar.set(y, m, d, 0, 0, 0);
         }
         if (aDay != null) {
             d = Integer.parseInt(aDay);
             if (d < calendar.getActualMinimum(Calendar.DATE) ||
                 d > calendar.getActualMaximum(Calendar.DATE)) {
                 log.fine("Invalid day: " + d);
                 return false;
             }
             calendar.set(y, m, d, 0, 0, 0);
         }
         if (anEntry != null) {
             entryId = Integer.parseInt(anEntry);
         }
     } catch (NumberFormatException e) {
         log.fine("Invalid path.");
         return false;
     }
     year = aYear;
     month = aMonth;
     day = aDay;
     entry = anEntry;
     return true;
 }
 
 public
 Path(String aYear, String aMonth)
 {
 	this(aYear, aMonth, null, null);
 }
 
 public
 Path(String aYear, String aMonth, String aDay)
 {
 	this(aYear, aMonth, aDay, null);
 }
 
 public
 Path(String aYear, String aMonth, String aDay, String anEntry)
 {
 	initialize(aYear, aMonth, aDay, anEntry);
 }
 
 public
 Path(int year)
 {
     this(String.valueOf(year));
 }
 
 public
 Path(int year, int month)
 {
     this(String.valueOf(year), String.valueOf(month));
 }
 
 public
 Path(int year, int month, int day)
 {
     this(String.valueOf(year), String.valueOf(month), String.valueOf(day));
 }
 
 public
 Path(String path)
 {
     if (path == null) {
         return;
     }
     String[] parts = path.split("/");
     switch (parts.length) {
     default:
        title = parts[5];
         /* fall through */
     case 5:
     	initialize(parts[1], parts[2], parts[3], parts[4]);
     	break;
     case 4:
     	initialize(parts[1], parts[2], parts[3], null);
     	break;
     case 3:
     	initialize(parts[1], parts[2], null, null);
     	break;
     case 2:
     	initialize(parts[1], null, null, null);
     	break;
     case 1:
     	initialize(parts[0], null, null, null);
     	break;
     case 0:
         break;
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
         title = (String)entity.getProperty("title");
        if (title != null) {
            if (title.isEmpty()) {
                title = null;
            } else {
                title = title.replaceAll("[^\\w ]", "").replaceAll(" ", "-");
            }
        }
     }
 }
 
 public
 Path(Date date)
 {
     calendar = Calendar.getInstance();
     calendar.setTime(date);
     year = String.valueOf(calendar.get(Calendar.YEAR));
     month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
     day = String.valueOf(calendar.get(Calendar.DATE));
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
     if (title != null) {
         s.append("/");
        s.append(title);
     }
     return s.toString();
 }
 
 public boolean
 equals(Object obj)
 {
     return obj == null ? false : toString().equals(obj.toString());
 }
 
 }
