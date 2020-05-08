 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edu.umich.lib.normalizers;
 
 import java.util.regex.*;
 
 /**
  *
  * @author BillDueber
  */
 public class ISBNLongifier {
 
     private static String  ISBNDelimiiterPattern = "[\\-\\.]";
     //    private static Pattern ISBNPattern = Pattern.compile("^.*?(\\d{9})[\\dXx].*$");
    private static Pattern ISBNPattern = Pattern.compile("^.*\\b(\\d{9})[\\dXx](?:\\Z|\\D).*$");
    private static Pattern LongISBNPattern = Pattern.compile("^.*\\b(\\d{13}).*$");
     //    private static Pattern ISBN13Pattern = Pattern.compile("^.*?\\d{13}.*$");
 
     public static Boolean matches(String isbn) throws IllegalArgumentException {
         isbn = isbn.replaceAll(ISBNDelimiiterPattern, "");
         Matcher m = ISBNPattern.matcher(isbn);
         return m.matches();
 	//        Matcher m2 = ISBN13Pattern.matcher(isbn);
 	//        return m.matches() && !m2.matches();
     }
 
     public static String longify(String isbn) {
         isbn = isbn.replaceAll(ISBNDelimiiterPattern, "");
         
         // Does it match a long one? If so, return it
         Matcher m = LongISBNPattern.matcher(isbn);
         if (m.matches()) {
          System.out.println(isbn + " is long");
           return m.group(1);
         }
 	//        System.out.println(isbn);
         m = ISBNPattern.matcher(isbn);
         if (!m.matches()) {
             throw new IllegalArgumentException(isbn + ": Not an ISBN");
         }
 	//        Matcher m2 = ISBN13Pattern.matcher(isbn);
 	//        if (m2.matches()) {
 	//	    throw new IllegalArgumentException(isbn + ": Already 13 digits");
 	//        }
 
         String longisbn = "978" + m.group(1);
         int[] digits = new int[12];
         for (int i=0;i<12;i++) {
             digits[i] =  new Integer(longisbn.substring(i, i+1));
         }
 
         Integer sum = 0;
         for (int i = 0; i < 12; i++) {
             sum = sum + digits[i] + (2 * digits[i] * (i % 2));
         }
 
         // Get the smallest multiple of ten > sum
         Integer top = sum + (10 - (sum % 10));
         Integer check = top - sum;
         if (check == 10) {
             return longisbn + "0";
         } else {
             return longisbn + check.toString();
         }
 
     }
 }
