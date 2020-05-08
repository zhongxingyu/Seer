 package be.artesis.timelog.clock;
 
 import be.artesis.timelog.controle.DataControle;
 import java.sql.Timestamp;
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
//elke pauze apart bijhouden of gewoon totaal pauze in seconden?
 public class Clock {
 
     private boolean running, pauzed;
     private static DecimalFormat df = new DecimalFormat("00");
     long startTime, stopTime;
 
     public Clock(){
         running = false;
         pauzed = false;
     }
     
     public long start() throws ClockException {
         if (!running) {
             running = true;
 
             startTime = generateUnixTimestamp();
             return startTime;
 
         } else {
             throw new ClockException("Clock is already running");
         }
     }
 
     public long pauseToggle() throws ClockException {
         if (running) {
             pauzed = !pauzed;
             long pauseTime = generateUnixTimestamp();
             return pauseTime;
         } else {
             throw new ClockException("Clock is not running");
         }
     }
 
     public long stop() throws ClockException {
         if (running && !pauzed) {
             running = false;
             stopTime = generateUnixTimestamp();
             return stopTime;
         } else {
             throw new ClockException("Clock is not running or pauzed");
         }
     }
 
     public long getStartTime() {
         return startTime;
     }
 
     public long getStopTime() {
         return stopTime;
     }
 
     public boolean isRunning() {
         return running;
     }
     
     public boolean isPaused(){
         return pauzed;
     }
 
     public long getRuntime() {
        return generateUnixTimestamp() - startTime;
     }
 
     //indien enkel start en stoptijd nodig zijn om tijdspanne te berekenen
     public static long generateUnixTimestamp() {
         return System.currentTimeMillis() / 1000;
     }
 
     //formaat yyyy/mm/dd hh:mm:ss
     public static String timestampToString(long unixTimeStamp) {
         Timestamp t = new Timestamp(unixTimeStamp * 1000);
         Calendar c = GregorianCalendar.getInstance();
         c.setTime(t);
         
         int day = c.get(Calendar.DAY_OF_MONTH);
         int month = c.get(Calendar.MONTH) +1;
         int hour = c.get(Calendar.HOUR_OF_DAY);
         int min = c.get(Calendar.MINUTE);
         
         String date = String.format("%02d/%02d", day, month);
         String time = String.format("%02d:%02d", hour, min);
         
         String s = date + " - " + time;
         return s;
     }
 
     //formaat dd/mm/yyyy
     public static String timestampToDateString(long unixTimeStamp) {
         Timestamp t = new Timestamp(unixTimeStamp * 1000);
         return df.format(t.getDate()) + "/" + df.format(t.getMonth() + 1) + "/" + (t.getYear() + 1900);
     }
 
     //input formaat dd/mm/yyyy
     public static long StringToTimestamp(String date){
         try {
             Date d = DataControle.sdf.parse(date);
             return d.getTime() / 1000;
         } catch (Exception ex) {
             return -1l;
         }
     }
 
     public static String longTimeToHMS(long time) {
         int h = (int) time / 3600;
         int remainder = (int) time - h * 3600;
         int m = remainder / 60;
         remainder = remainder - m * 60;
         int s = remainder;
 
         return df.format(h) + " : " + df.format(m) + " : " + df.format(s);
     }
 
     public static String longTimeToString(long time, boolean longFormat) {
         String output = "";
 
         int h = (int) time / 3600;
         int remainder = (int) time - h * 3600;
         int m = remainder / 60;
         remainder = remainder - m * 60;
         int s = remainder;
 
         if (longFormat) {
             if (h != 0) {
                 output += h + " hour(s), ";
             }
             if (m != 0) {
                 output += m + " minute(s), ";
             }
             if (s != 0) {
                 output += s + " second(s)";
             }
         } else {
             if (h != 0) {
                 output += h + " h, ";
             }
             if (m != 0) {
                 output += m + " m, ";
             }
             if (s != 0) {
                 output += s + " s";
             }
         }
 
         if (output.equals("")) {
             output = "0 seconds";
         }
 
         return output;
     }
 
     @Override
     public String toString() {
         if (running) {
             return longTimeToHMS(generateUnixTimestamp() - startTime);
         } else {
             return longTimeToHMS(stopTime - startTime);
         }
     }
 }
