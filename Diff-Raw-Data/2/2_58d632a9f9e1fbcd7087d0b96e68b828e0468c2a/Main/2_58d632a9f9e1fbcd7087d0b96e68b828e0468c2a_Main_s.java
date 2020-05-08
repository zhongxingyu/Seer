 package com.radonsky.monkeysched;
 
 import static org.joda.time.DateTime.now;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 public class Main {
     
     public static void main(String[] args) {
         TimeRule timeRule = RuleEngine.getDefault().getTimeRule();
         DateTime currentTime = now();
         DateTimeFormatter fmt = DateTimeFormat.fullDateTime();
         System.out.println("The current time is " + fmt.print(currentTime));
         boolean canRun = timeRule.apply(currentTime);
         if (canRun) {
             System.out.println("Monkeys can cause havoc now!");
         } else {
            System.out.println("Monkeys should sleep now.");
         }
     }
 
 }
