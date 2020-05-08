 package br.com.bluesoft.commons.lang;
 
 import java.sql.Date;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  * @author AndreFaria
  */
 public class DateUtil {
 
 	private static String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
 
 	/**
 	 * Converte uma String em Data
 	 * @param date
 	 * @return java.sql.Date
 	 */
 	public static java.sql.Date toDate(final String date) {
		if (date == null ) { 
 			return null;
		}else if (date.equalsIgnoreCase("")) { 
 			return null;
		}	
 		try {
 			return toDate(new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(date));
 		} catch (final ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Converte um java.util.Date em um java.sql.Date
 	 * @param date
 	 * @return
 	 */
 	public static java.sql.Date toDate(final java.util.Date date) {
 		if (date == null) {
 			return null;
 		}
 		return new java.sql.Date(date.getTime());
 	}
 
 	/**
 	 * Obtem a data Atual
 	 * @return Date
 	 */
 	public static Date getDate() {
 		return new java.sql.Date(new java.util.Date().getTime());
 	}
 
 	public static Date getDate(final int dias) {
 		final Calendar calendar = Calendar.getInstance();
 		calendar.add(Calendar.DATE, dias);
 		return new java.sql.Date(calendar.getTimeInMillis());
 	}
 
 	public static String formatDate(final java.util.Date date) {
 		return DateFormat.getDateInstance().format(date);
 	}
 
 	public static String formatDate(final Date dataInicial, final String pattern) {
 		return new SimpleDateFormat(pattern).format(dataInicial);
 	}
 
 }
