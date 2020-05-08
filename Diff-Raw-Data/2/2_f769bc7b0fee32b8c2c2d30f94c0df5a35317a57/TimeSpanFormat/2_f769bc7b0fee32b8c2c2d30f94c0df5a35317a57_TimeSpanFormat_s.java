 package polly.core.formatting;
 
 public class TimeSpanFormat {
     
     
     public static void main(String[] args) {
         long seconds = 60*60*24+60*15;
         TimeSpanFormat tsf = new TimeSpanFormat();
         System.out.println(tsf.format(seconds));
     }
     
     public final static long SECONDS = 1L;
     public final static long MINUTES = 60L;
     public final static long HOURS = 60L;
     public final static long DAYS = 24L;
     public final static long WEEKS = 7L;
     public final static long YEARS = 365L;
 
     public String format(long seconds) {
         StringBuilder result = new StringBuilder(20);
         
         long years = this.yearPart(seconds);
         long days = this.dayPart(seconds);
         long hours = this.hourPart(seconds);
         long minutes = this.minutePart(seconds);
         long s = this.secondPart(seconds);
 
         if (years > 0) {
             result.append(years);
             result.append("y ");
         }
         if (days > 0) {
             result.append(days);
             result.append("d ");
         }
         if (hours > 0) {
             result.append(hours);
             result.append("h ");
         }
         if (minutes > 0) {
             result.append(minutes);
             result.append("m ");
         }
         if (s > 0) {
             result.append(s);
             result.append("s");
         }
        return result.toString();
         
     }
     
     
     protected String secondsString(long seconds) {
         if (seconds == 1) {
             return "Sekunde";
         }
         return "Sekunden";
     }
     
     
     
     protected String minuteString(long minutes) {
         if (minutes == 1) {
             return "Minuten";
         }
         return "Minuten";
     }
     
     
     
     protected String hourString(long hours) {
         if (hours == 1) {
             return "Stunde";
         }
         return "Stunden";
     }
     
     
     
     protected String dayString(long days) {
         if (days == 1) {
             return "Tag";
         }
         return "Tage";
     }
     
     
     
     protected String yearString(long years) {
         if (years == 1) {
             return "Jahr";
         }
         return "Jahre";
     }
     
     
     
     protected long secondPart(long seconds) {
         return seconds >= MINUTES ? seconds % MINUTES : seconds;
     }
     
     
     protected long minutePart(long seconds) {
         long minutes = seconds / MINUTES;
         return minutes >= HOURS ? minutes % HOURS : minutes;
     }
     
     
     protected long hourPart(long seconds) {
         long hours = seconds / HOURS / MINUTES;
         return hours >= DAYS ? hours % DAYS : hours;
     }
     
     
     protected long dayPart(long seconds) {
         long days = seconds / DAYS / HOURS / MINUTES;
         return days >= YEARS ? days % YEARS : days;
     }
     
     
     protected long yearPart(long seconds) {
         long years = seconds / YEARS / DAYS / HOURS / MINUTES;
         return years;
     }
 }
