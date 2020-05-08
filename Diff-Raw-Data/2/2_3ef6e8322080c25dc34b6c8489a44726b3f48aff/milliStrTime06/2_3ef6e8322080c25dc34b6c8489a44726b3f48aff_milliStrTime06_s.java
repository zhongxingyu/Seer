 import java.util.Date;
 import java.util.Calendar;
 
 /**
  * Convert milliseconds to readable colon-delimeted hour, minute, second.
  * - course:     COMP-171-801RL
  * - assignment: pg 167; que. #5.25; due: 11/09  (lab# 06)
  *
  * @author Jonathan Zacsh <jzacsh@gmail.com>
  */
 public class milliStrTime06 {
   /**
    * Converts input millisecond-representation of current time into human
    * readable format.
    *
    * @param  long   epoch in milliseconds
    * @return string hours, minutes, seconds
   * - character delimeted hours, minutes, seconds
   * - "ERROR: [...]" if something is wrong with input
    */
   public static String convertMillis(long millis) {
     //convert epoch to current time
     long oneday = 86400000;
     millis %= oneday;
 
     long seconds = millis / 1000; // rational base of measure
 
     long hrs = seconds / (60 * 60); // 60 seconds, 60 minutes per hour.
     long min = (seconds % (60 * 60)) / 60; // just minutes
     long sec = (seconds % (60 * 60)) % 60; //remainder
 
     //concatenate hrs, min, sec with ':' colon.
     String delim = ":";
     String time = hrs + delim + min + delim + sec;
 
     return time;
   }
 
   public static void main(String[] args) {
     //printed documentation
     String intro = "\nJonathan Zacsh <jzacsh@gmail.com>; COMP-171-801RL\n";
     intro += "assignment: pg 167; que. #5.25; due: 11/09  (lab# 06)\n";
     intro += "Converts milliseconds to readable colon-delimeted hour, minute, second.\n";
     intro += "Below is a test run of the method convertMillis(), using the current time.\n";
     System.out.printf("%s", intro);
 
     //codez
     long epoch = System.currentTimeMillis();
     System.out.printf("%d milliseconds is %s\n", epoch, convertMillis(epoch));
   }
 }
