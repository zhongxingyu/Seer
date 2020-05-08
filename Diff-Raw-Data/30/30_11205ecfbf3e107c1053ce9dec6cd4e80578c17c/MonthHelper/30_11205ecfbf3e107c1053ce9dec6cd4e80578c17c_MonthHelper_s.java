 package war.webapp.util;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.ResourceBundle;
 
 public final class MonthHelper {
 
     public static String getMonthString(int month, ResourceBundle bundle) {
         if (month == 0) {
             return bundle.getString("month.jan");
         }
         if (month == 1) {
             return bundle.getString("month.feb");
         }
         if (month == 2) {
             return bundle.getString("month.mar");
         }
         if (month == 3) {
             return bundle.getString("month.apr");
         }
         if (month == 4) {
             return bundle.getString("month.may");
         }
         if (month == 5) {
             return bundle.getString("month.jun");
         }
         if (month == 6) {
             return bundle.getString("month.jul");
         }
         if (month == 7) {
             return bundle.getString("month.aug");
         }
         if (month == 8) {
             return bundle.getString("month.sep");
         }
         if (month == 9) {
             return bundle.getString("month.oct");
         }
         if (month == 10) {
             return bundle.getString("month.nov");
         }
         if (month == 11) {
             return bundle.getString("month.dec");
         }
         return null;
     }
 
     public static String getShortMonthString(int month, ResourceBundle bundle) {
         if (month == 0) {
             return bundle.getString("month.jan.short");
         }
         if (month == 1) {
             return bundle.getString("month.feb.short");
         }
         if (month == 2) {
             return bundle.getString("month.mar.short");
         }
         if (month == 3) {
             return bundle.getString("month.apr.short");
         }
         if (month == 4) {
             return bundle.getString("month.may.short");
         }
         if (month == 5) {
             return bundle.getString("month.jun.short");
         }
         if (month == 6) {
             return bundle.getString("month.jul.short");
         }
         if (month == 7) {
             return bundle.getString("month.aug.short");
         }
         if (month == 8) {
             return bundle.getString("month.sep.short");
         }
         if (month == 9) {
             return bundle.getString("month.oct.short");
         }
         if (month == 10) {
             return bundle.getString("month.nov.short");
         }
         if (month == 11) {
             return bundle.getString("month.dec.short");
         }
         return null;
     }
 
 
     public static int getDaysNumInMonth(int month) {
         switch (month) {
         case 0:
             return 31;
         case 1:
             if (Calendar.getInstance().get(Calendar.YEAR) % 4 == 0) {
                 return 29;
             }
             return 28;
         case 2:
             return 31;
         case 3:
             return 30;
         case 4:
             return 31;
         case 5:
             return 30;
         case 6:
             return 31;
         case 7:
             return 31;
         case 8:
             return 30;
         case 9:
             return 31;
         case 10:
             return 30;
         case 11:
             return 31;
         }
         return 0;
     }
 
     public static int getMonth(String monthString, ResourceBundle bundle) {
         if (monthString.equals(bundle.getString("month.jan"))) {
             return 0;
         }
         if (monthString.equals(bundle.getString("month.feb"))) {
             return 1;
         }
         if (monthString.equals(bundle.getString("month.mar"))) {
             return 2;
         }
         if (monthString.equals(bundle.getString("month.apr"))) {
             return 3;
         }
         if (monthString.equals(bundle.getString("month.may"))) {
             return 4;
         }
         if (monthString.equals(bundle.getString("month.jun"))) {
             return 5;
         }
         if (monthString.equals(bundle.getString("month.jul"))) {
             return 6;
         }
         if (monthString.equals(bundle.getString("month.aug"))) {
             return 7;
         }
         if (monthString.equals(bundle.getString("month.sep"))) {
             return 8;
         }
         if (monthString.equals(bundle.getString("month.oct"))) {
             return 9;
         }
         if (monthString.equals(bundle.getString("month.nov"))) {
             return 10;
         }
         if (monthString.equals(bundle.getString("month.dec"))) {
             return 11;
         }
         return 0;
     }
 
     public static String[] getMonths(ResourceBundle bundle) {
        return new String[] { bundle.getString("month.jan"), bundle.getString("month.feb"),
                 bundle.getString("month.mar"), bundle.getString("month.apr"), bundle.getString("month.may"),
                 bundle.getString("month.jun"), bundle.getString("month.jul"), bundle.getString("month.aug"),
                bundle.getString("month.sep"), bundle.getString("month.oct"), bundle.getString("month.nov"),
                 bundle.getString("month.dec"), };
     }
 
     /**
      * Return year number -5 from current, current and +5 from current year
      * 
      * @return list of years as {@link String}
      */
     public static List<String> getYears() {
         Calendar currentDate = Calendar.getInstance();
         List<String> years = new ArrayList<String>();
         
         int currentYear = currentDate.get(Calendar.YEAR);
         
         for (int year = currentYear - 5; year <= currentYear + 5; year++) {
             years.add(Integer.toString(year));
         }
         
         return years;
     }
 
 }
