 package models;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Parser {
 	
	private static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd.MM.yy kk.mm");
 	
 	public static Date parseStringToDate(String strDate) {
 		Date date = null;
 		try {
 			date = simpleDateFormatter.parse(strDate);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		return date;
 	}
 	
 	public static String parseDateToString(Date date) {
 		return simpleDateFormatter.format(date);
 	}
 }
