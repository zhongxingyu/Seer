 package utils;
 
 import play.Logger;
 
 import java.text.DecimalFormat;
 
 public class Currency {
 
     public static String prettyDouble(String doubleString) {
         try{
         return prettyDouble(Double.parseDouble(doubleString));
         } catch (NumberFormatException e)
         {
             Logger.error(e.getMessage(), e);
 
         }
         return "undefined";
     }
 
     public static String prettyDoubleOld(double doubleString) {
         DecimalFormat df2 = new DecimalFormat("#####0.0");
         double dd2dec = new Double(df2.format(doubleString)).doubleValue();
 
         String newValue = dd2dec + "";
 
         return newValue.replaceAll("\\.0", "");
 
     }
 
 
    public static String prettyDouble(double doubleString)
     {
         DecimalFormat df2 = new DecimalFormat("#####0.0");
         double dd2dec = new Double(df2.format(doubleString)).doubleValue();
         doubleString = dd2dec;
 
          String resultString = doubleString + "";
 
         if(doubleString > 1000)
         {
             int result = (int) Math.round(doubleString/100)*100;
             resultString = result + "";
         }
         else if(doubleString > 100)
         {
             int result = (int) Math.round(doubleString/10)*10;
             resultString = result + "";
         }
         else if(doubleString > 10)
         {
             int result = (int) Math.round(doubleString);
             resultString = result + "";
         }
 
 
         return resultString.replaceAll("\\.0", "");
 
     }
 
 
 }
