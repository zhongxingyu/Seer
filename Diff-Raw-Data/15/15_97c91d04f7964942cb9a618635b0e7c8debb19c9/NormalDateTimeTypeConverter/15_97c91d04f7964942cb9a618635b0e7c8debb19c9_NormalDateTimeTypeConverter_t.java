 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.lmb97.util;
 
 import java.util.Arrays;
 import net.sourceforge.stripes.validation.DateTypeConverter;
 
 /**
  *
  * @author javier
  */
 public class NormalDateTimeTypeConverter extends DateTypeConverter {
  private static final String[] TIME_FORMAT = { "HH:mm","yyyy/MM/dd-HH:mm" };
   @Override
   protected String[] getFormatStrings() {
      System.out.println("NormalDateTimeConverter is going to return patterns");
      return TIME_FORMAT;
   }
 }
