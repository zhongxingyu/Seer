 package by.fksis.schedule;
 
 import java.util.Calendar;
 import java.util.Date;
import java.util.GregorianCalendar;
 
 @SuppressWarnings("deprecation")
 public final class Util {
     public static int getWeek(Date date) {
         Date januaryFirst = new Date(date.getYear(), 0, 1);
         return (int) Math.round(Math.ceil((((date.getTime() - januaryFirst.getTime()) / 86400000) + januaryFirst.getDay() + 1) / 7));
     }
 
     public static int getLastEnteringYear() {
        int year = new GregorianCalendar().get(GregorianCalendar.YEAR);
         if (new Date().getMonth() < 8)
             year--;
         return year;
     }
 
     public static int getScheduleWeek(Date date) {
        Calendar sep1 = new GregorianCalendar(getLastEnteringYear(), GregorianCalendar.SEPTEMBER, 1);
        if (sep1.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY) {
            sep1.add(GregorianCalendar.DAY_OF_YEAR, -7);
        }
        sep1.add(GregorianCalendar.DAY_OF_YEAR, -sep1.get(GregorianCalendar.DAY_OF_WEEK) + GregorianCalendar.MONDAY);
        Calendar now = new GregorianCalendar();
        now.setTime(date);

        int dWeeks = (int) Math.floor((now.getTime().getTime() - sep1.getTime().getTime()) / 86400000 / 7);
        return dWeeks % 4 + 1;
     }
 
     public static int getDayOfWeekIndex(Calendar date) {
         switch (date.get(Calendar.DAY_OF_WEEK)) {
             case Calendar.SUNDAY:
                 return 6;
             case Calendar.MONDAY:
                 return 0;
             case Calendar.TUESDAY:
                 return 1;
             case Calendar.WEDNESDAY:
                 return 2;
             case Calendar.THURSDAY:
                 return 3;
             case Calendar.FRIDAY:
                 return 4;
             case Calendar.SATURDAY:
                 return 5;
         }
         return 0;
     }
 
     public static <T> T defaultValue(T obj, T def) {
         return (obj == null) ? def : obj;
     }
 }
