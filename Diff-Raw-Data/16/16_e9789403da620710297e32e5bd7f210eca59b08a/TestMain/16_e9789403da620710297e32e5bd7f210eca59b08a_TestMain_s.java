 package com.github.ryancwilliams.Starways;
 
 import com.github.ryancwilliams.Starways.engine.math.ValuesOfQuantities.QuantityOfLength;
 import com.github.ryancwilliams.Starways.engine.math.ValuesOfQuantities.ValueOfLength;
 
 /**
  * This is a main class for testing the application
  * @author ryancwilliams
  */
 public class TestMain {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         com.github.ryancwilliams.Starways.ui.LaunchWindow.createWindow();
         
         //Test Values of Quanties
         ValueOfLength test = new ValueOfLength(1, QuantityOfLength.LIGHT_YEAR);
         System.out.println(test);
         System.out.println(test.asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.METRE).asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.KILOMETRE).asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.ASTRONOMICAL_UNIT).asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.LIGHT_YEAR).asString());
        test = new ValueOfLength(0.1, QuantityOfLength.ASTRONOMICAL_UNIT);
         System.out.println(test);
         System.out.println(test.asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.METRE).asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.KILOMETRE).asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.ASTRONOMICAL_UNIT).asString());
         System.out.println(test.getConvertedValueOfLength(QuantityOfLength.LIGHT_YEAR).asString());
     }
 }
