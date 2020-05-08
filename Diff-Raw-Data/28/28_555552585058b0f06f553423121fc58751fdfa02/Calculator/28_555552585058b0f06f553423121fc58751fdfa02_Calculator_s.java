 package com.ninja_squad.codestory.calculator;
 
 import com.google.common.annotations.VisibleForTesting;
 import groovy.lang.GroovyRuntimeException;
 import groovy.lang.GroovyShell;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.Locale;
 
 public class Calculator {
 
     public String calculate(final String query) {
         String result = null;
 
         String preparedQuery = prepare(query);
 
         GroovyShell shell = new GroovyShell();
         try {
             Object value = shell.evaluate(preparedQuery);
             if (value != null) {
                 result = format(value);
             }
         } catch (GroovyRuntimeException e) {
             // Invalid expression
         }
         return result;
     }
 
 
     private String prepare(String query) {
         String preparedQuery = query;
 
         // query might be in French LOCALE
         preparedQuery = manageFrenchLocale(preparedQuery);
 
         return preparedQuery;
     }
 
     @VisibleForTesting
    protected String manageFrenchLocale(String query) {
         return query.replace(',', '.');
     }
 
     @VisibleForTesting
    protected String format(Object value) {
         return getNumberFormat().format(value);
     }
 
     private NumberFormat getNumberFormat() {
         return new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.FRENCH));
     }
 
 }
