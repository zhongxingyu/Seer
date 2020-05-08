 package by.fksis.schedule;
 
 import java.util.Calendar;
 import java.util.Date;
 
 @SuppressWarnings("deprecation")
 public final class Util {
     public static int getWeek(Date date) {
         Date januaryFirst = new Date(date.getYear(), 0, 1);
         return (int) Math.round(Math.ceil((((date.getTime() - januaryFirst.getTime()) / 86400000) + januaryFirst.getDay() + 1) / 7));
     }
 
     public static int getLastEnteringYear() {
         int year = new Date().getYear();
         if (new Date().getMonth() < 8)
             year--;
         return year;
     }
 
     public static int getScheduleWeek(Date date) {
         int y = getLastEnteringYear();
         Date sep1 = new Date(y, 8, 1);
         long wsd = sep1.getTime() / 86400000 - sep1.getDay();
         return (int) Math.round(Math.floor((date.getTime() / 86400000 - wsd) / 7) % 4) + 1;
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
