 package com.enigmastation.yahrzeit;
 
 import java.util.Calendar;
 
 public class GregorianDate extends YahrzeitDate {
     public GregorianDate(Calendar c) {
        //To change body of created methods use File | Settings | File Templates.
     }
 
     @Override
     public String getMonthName() {
         String monthNames[] = {"January", "February", "March", "April", "May", "June",
                 "July", "August", "September", "October", "November", "December"};
         return monthNames[getMonth()];
     }
 
     @Override
     public int toAbsolute() {
         int N = day;        // days this month
         for (int m = month - 1; m > 0; m--)    // days in prior months this year
             N = N + lastDayOfGregorianMonth(m, year);
         return (N            // days this year
                 + 365 * (year - 1)    // days in previous years ignoring leap days
                 + (year - 1) / 4    // Julian leap days before this year...
                 - (year - 1) / 100    // ...minus prior century years...
                 + (year - 1) / 400);    // ...plus prior years divisible by 400
     }
 }
