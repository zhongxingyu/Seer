 package com.country.common;
 
 import java.text.Format;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class DateFormater {
 
 	public static Date convertStringToDate (String d){
 		
 		SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
 		
 		Date returnDate = null;
 
 		try {
 			returnDate = sdf1.parse(d);
 		} catch (ParseException e) {
 			e.printStackTrace();
			System.out.println("(DateFormater.java) Error en el parseo de formato ");
 		}
 
 		return returnDate;
 		
 	}
 	
 	public static String convertDateToString (Date d){
 		
 		Format formatter = new SimpleDateFormat("dd-MM-yyyy");
 		String resultDate = formatter.format(d);
 
 		return resultDate;
 		
 	}
 
 }
