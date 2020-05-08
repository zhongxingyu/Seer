 package br.com.flexait.gateway.util;
 
 import java.text.DecimalFormat;
 
 public class NumberUtil {
 
 	public static String format(double value) {
 		DecimalFormat df = new DecimalFormat("#.00");
 		
 		String format = df.format(value);
		return format.replace(".", "").replace(",", "").replaceFirst("^0+", "");
 	}
 
 	public static double parse(String value) {
 		Double d = Double.valueOf(value);
 		return d / 100.0;
 	}
 
 }
