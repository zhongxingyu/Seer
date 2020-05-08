 package no.kantega.android.afp.utils;
 
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 public class FmtUtil {
 
     /**
      * Convert the given date to string using the given format
      *
      * @param format Date format to use when converting
      * @param date   Date to convert
      * @return The date
      */
     public static String dateToString(String format, Date date) {
         final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
         return simpleDateFormat.format(date);
     }
 
     /**
      * Format currency according to the default locale
      *
      * @param number Number to format
      * @return Formatted currency
      */
     public static String currency(double number) {
         return currency(number, Locale.getDefault());
     }
 
     /**
      * Format currency according to the given locale
      *
      * @param number Number to format
      * @param locale Locale to use when formatting
      * @return Formatted currency
      */
     public static String currency(double number, Locale locale) {
         final NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
         nf.setMinimumFractionDigits(2);
         nf.setMaximumFractionDigits(2);
         return nf.format(number);
     }
 
     /**
      * Trim useless data from the given transaction text
      *
      * @param text Text to trim
      * @return Trimmed text
      */
     public static String trimTransactionText(String text) {
         final String pattern = "(\\d{2,}\\**\\s)?(\\d{2}\\.\\d{2}\\s)?([A-Z]{3}\\s\\d+,\\d+\\s)?(TIL\\:\\s)?";
         return text == null ? "" : text.replaceAll(pattern, "");
     }
 
     /**
      * Convert the given string to date using the given format
      *
      * @param format Format to use when converting
      * @param date   Date to convert
      * @return The date
      */
     public static Date stringToDate(String format, String date) {
         final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
         try {
             return simpleDateFormat.parse(date);
         } catch (ParseException e) {
             return null;
         }
     }
 
     /**
      * Check if given string is a number with optional decimals
      *
      * @param s String to check
      * @return True if string contains one or more numbers
      */
     public static boolean isNumber(String s) {
         return s != null && s.matches("^\\d+([,\\.]\\d+)?$");
     }
 }
