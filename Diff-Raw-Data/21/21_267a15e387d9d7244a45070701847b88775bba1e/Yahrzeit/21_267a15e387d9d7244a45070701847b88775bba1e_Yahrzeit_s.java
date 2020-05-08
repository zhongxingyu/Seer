 package com.enigmastation.yahrzeit;
 
 import com.beust.jcommander.Parameter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Scanner;
 
 public class Yahrzeit {
     @Parameter(names = {"-s", "--since"})
     private boolean since = false;
 
     @Parameter(names = {"-o", "--output"})
     private boolean outputToday = true;
 
     @Parameter(names = {"-h", "--hebrew"})
     private boolean showHebrew = true;
 
     @Parameter(names = {"-i", "--islamic"})
     private boolean showIslamic = false;
 
     @Parameter(names = {"-g", "--gregorian"})
     private boolean showGregorian = false;
 
     public void run() {
         Calendar c = Calendar.getInstance();
         GregorianDate gregorian = new GregorianDate(c);
         HebrewDate hebrew = new HebrewDate(c);
         IslamicDate islamic = new IslamicDate(c);
 
         List<String> dateMatches = new ArrayList<>();
 
         dateMatches.add(toString(hebrew));
         dateMatches.add(toString(islamic));
         dateMatches.add(toString(gregorian));
 
         if (outputToday) {
             show(showHebrew, hebrew);
             show(showIslamic, islamic);
         }
         File calendarFile = new File(System.getProperty("user.home") +
                 System.getProperty("file.separator") + ".calendar");
         try {
             FileReader fr = new FileReader(calendarFile);
             BufferedReader br = new BufferedReader(fr);
             Scanner scanner = new Scanner(br);
             String input;
             while (scanner.hasNextLine()) {
                 input = scanner.nextLine();
                 for (String date : dateMatches) {
                     if (input.trim().startsWith(date)) {
                         System.out.println(input.substring(date.length() + 1).trim());
                     }
                 }
             }
         } catch (FileNotFoundException ignored) {
             // this is okay, we just don't output anything if the file doesn't exist
         }
 
     }
 
     private String toString(YahrzeitDate date) {
         return date.getMonthName() + " " + date.getDay();
     }
 
     private void show(boolean showDate, YahrzeitDate date) {
         if (showDate) {
             System.out.println(date.getMonthName() + " " + date.getDay() + ", " + date.getYear());
         }
     }
 
     public static void main(String[] args) {
         Yahrzeit y = new Yahrzeit();
         y.run();
     }
 }
