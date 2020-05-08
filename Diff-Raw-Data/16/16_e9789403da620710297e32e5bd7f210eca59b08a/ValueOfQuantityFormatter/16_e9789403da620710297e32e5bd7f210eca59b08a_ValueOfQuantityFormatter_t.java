 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.ryancwilliams.Starways.engine.math.ValuesOfQuantities;
 
 import java.text.DecimalFormat;
 
 /**
  * This class provides static methods for formating Value of Quantities to strings.
  * This should be used in the implementation classes of ValueOfQuantity interface.
  * @author ryancwilliams
  */
 public class ValueOfQuantityFormatter {
     
     /**
      * Gets a DecimalFormat that can be used to create decimal strings with the 
      * specified number of decimal places.
      * @param decimalPlaces the number of decimal places to generate a DecimalFormat for. 
      * @return The DecimalFormat.
      */
     private static DecimalFormat getFormat(int decimalPlaces) {
         //Create a digit and a decimal point with leading digits and thousands separator 
        String pattern = "###,##0.";
         
         //For <decimalPlaces> of times
         for(int i = 0;i < decimalPlaces;i++) {
             //add a digit
             pattern += "0";
         }
         
         //Create the Decimal format and return it
         return new DecimalFormat(pattern);
     }
     
     /**
      * Creates a formated string from the provided ValueOfQuantity. 
      * This method is called by asString methods in the implementation 
      * classes of ValueOfQuantity interface. That asString method is 
      * preferred over this method.
      * @return a formated string based on the provided ValueOfQuantity.
      */
     // Using no access modifier because this method should only be accessed from this package.
     static String formatValueOfQuantity(ValueOfQuantity value, int decimalPlaces) {
         //Create a empty soutput tring 
         String formatedString = "";
         
         //Add the formated number to the string
         formatedString += ValueOfQuantityFormatter.getFormat(decimalPlaces)
                 .format(value.getValue());
         
         //Add a space to the string
         formatedString += " ";
         
         //Add the quantity symbol to the string
         formatedString += value.getQuantity().getSymbol();
         
         //return the formated string
         return formatedString;
     }
 }
