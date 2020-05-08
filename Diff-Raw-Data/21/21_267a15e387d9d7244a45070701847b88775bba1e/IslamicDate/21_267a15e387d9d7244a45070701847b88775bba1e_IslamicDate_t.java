 package com.enigmastation.yahrzeit;
 
 import java.util.Calendar;
 
 public class IslamicDate extends YahrzeitDate {
     private static final int epoch = 227014;
 
     static boolean isLeapYear(int year) {
         // True if year is an Islamic leap year
 
         return (((11 * year) + 14) % 30) < 11;
     }
 
     static int
     lastDayOfIslamicMonth(int month, int year) {
         // Last day in month during year on the Islamic calendar.
         if (((month % 2) == 1) || ((month == 12) && isLeapYear(year)))
             return 30;
         else
             return 29;
     }
 
     public IslamicDate(int month, int day, int year) {
         this.month = month;
         this.day = day;
         this.year = year;
     }
 
     public IslamicDate(Calendar c) {
         initFromAbsolute(toAbsolute(c));
     }
 
     public IslamicDate(int d) {
         initFromAbsolute(d);
     }
 
     private void initFromAbsolute(int d) {
         if (d <= epoch) {   // Date is pre-Islamic
             month = 0;
             day = 0;
             year = 0;
         } else {
             // Search forward year by year from approximate year
             year = (d - epoch) / 355;
             while (d >= new IslamicDate(1, 1, year + 1).toAbsolute())
                 year++;
             // Search forward month by month from Muharram
             month = 1;
             while (d > new IslamicDate(month, lastDayOfIslamicMonth(month, year),
                     year).toAbsolute())
                 month++;
             day = d - new IslamicDate(month, 1, year).toAbsolute() + 1;
         }
     }
 
     @Override
     public String getMonthName() {
        String[] monthNames = {"Muharram", "Safar", "Rabi I", "Rabi II",
                 "Jumada I", "Jumada II", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu'l Qa'dah", "Dhu'l-Hijja"};
         return monthNames[getMonth()];
     }
 
     @Override
     public int toAbsolute() {
         return (getDay()            // days so far this month
                 + 29 * (getMonth() - 1)    // days so far...
                 + getMonth() / 2        //            ...this year
                 + 354 * (getYear() - 1)    // non-leap days in prior years
                 + (3 + (11 * getYear())) / 30    // leap days in prior years
                 + epoch);    // days before start of calendar
     }
 }
