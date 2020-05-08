 package com.oppian.oikos;
 
 import java.sql.SQLException;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Locale;
 
 public class OikosParser {
     
     private static NumberFormat DEFAULT_NUMBERFORMAT = NumberFormat.getNumberInstance(Locale.US);
     
     public static final Boolean parseEntry(String entryText, OikosManager manager) throws SQLException {
         // get number format
         NumberFormat cf = NumberFormat.getCurrencyInstance();
         NumberFormat nf = NumberFormat.getNumberInstance();
         NumberFormat[] numberFormats = new NumberFormat[] { cf, nf, DEFAULT_NUMBERFORMAT };
         StringBuilder description = new StringBuilder();
         Number amount = null;
         // tokenize string on whitespace
         String[] tokens = entryText.split("\\s");
         String token = null;
         for (int x = 0; x < tokens.length; x++) {
             token = tokens[x];
             // look for currency
             if (amount == null) {
                 amount = parseAmount(numberFormats, token);
                 if (amount != null) {
                     continue;
                 }
             }
             if (description.length() > 0) {
                 description.append(" ");
             }
             description.append(token);
         }
 
         if (amount != null) {
            int a = Math.round(amount.floatValue() * -100);
             manager.addEntry(a, description.toString());
             // clear text field
             return true;
         }
         return false;
     }
     
     private static Number parseAmount(NumberFormat[] numberFormats, String token) {
         // try parse using currency
         for (NumberFormat numberFormat : numberFormats) {
             try {
                 return numberFormat.parse(token);
             } catch (ParseException e) {
                 // ignore exception
             }
         }
         return null;
     }
 }
