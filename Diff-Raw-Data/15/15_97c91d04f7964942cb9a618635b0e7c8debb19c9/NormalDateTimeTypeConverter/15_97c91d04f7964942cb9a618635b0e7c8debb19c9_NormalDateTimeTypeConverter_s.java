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
  private static final String[] TIME_FORMAT = { "HH:mm","yyyy/MM/dd HH:mm" };
   @Override
   protected String[] getFormatStrings() {
    String sup[]=super.getFormatStrings();
    String result[]=Arrays.copyOf(sup, sup.length+TIME_FORMAT.length );
    System.arraycopy(TIME_FORMAT, 0, result, sup.length, TIME_FORMAT.length);
    return result;
   }
 }
