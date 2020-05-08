 /**
  * 
  */
 package de.eahapp;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Hilfsklasse fr den Umgang mit den Datentyp Date
  * 
  * @author Christoph Dieck
  * @see ava.util.Date
  */
 public class DateHelper {
 	/**
 	 * Funktion korrigiert den Typ Date auf 0 Uhr
 	 * 
 	 * @param times
 	 *            The value is the number of milliseconds since Jan. 1, 1970
 	 *            GMT.
 	 * @return long Rckgabe von Date als millisecond Wert ab 0 Uhr.
 	 */
 	public static Long getMorning(long times) {
 		Date time = new Date(times);
 		time.setHours(0);
 		time.setMinutes(0);
 		time.setSeconds(0);
 		return time.getTime();
 	}
 
 	/**
 	 * Funktion korrigiert den Typ Date auf 24 Uhr
 	 * 
 	 * @param times
 	 *            The value is the number of milliseconds since Jan. 1, 1970
 	 *            GMT.
 	 * @return long Rckgabe von Date als millisecond Wert ab 24 Uhr.
 	 */
 	public static Long getEvening(long times) {
 		Date time = new Date(times);
 		time.setHours(24);
 		time.setMinutes(0);
 		time.setSeconds(0);
 		return time.getTime();
 	}
 
 	/**
 	 * Funktion erzeugt aus einem milliseconds Wert die Minuten
 	 * 
 	 * @param diff
 	 *            The value is the number of milliseconds since Jan. 1, 1970
 	 *            GMT.
 	 * @return int Minuten des milliseconds Werts
 	 */
 	public static int getMinutes(long diff) {
 		return (int) (diff / (60 * 1000) % 60);
 	}
 
 	/**
 	 * Funktion erzeugt aus einem milliseconds Wert die Sekunden
 	 * 
 	 * @param diff
 	 *            The value is the number of milliseconds since Jan. 1, 1970
 	 *            GMT.
 	 * @return int Sekunden des milliseconds Werts
 	 */
 	public static int getSeconds(long diff) {
 		return (int) (diff / 1000 % 60);
 	}
 
 	/**
 	 * Funktion erzeugt aus einem milliseconds Wert die Stunden
 	 * 
 	 * @param diff
 	 *            The value is the number of milliseconds since Jan. 1, 1970
 	 *            GMT.
 	 * @return int Stunden des milliseconds Werts
 	 */
 	public static int getHouers(long diff) {
 		return (int) (diff / (60 * 60 * 1000));
 	}
 
 	/**
 	 * Die Funktion generiert aus einem Date type ein Datum String
 	 * 
 	 * @param time
 	 *            Datum fr die Datumsausgabe
 	 * @return Der Date String fr das Layout terminplaner
 	 * @see terminplaner.xml
 	 * 
 	 */
 	public static String getDateString(Date time) {
 		// Tag als kurzform -> "EEEE" erzeugt lange Form
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 		return time == null ? null : dateFormat.format(time);
 	}
 
 	/**
 	 * Die Funktion generiert aus einem Date type ein Namenstag als String
 	 * 
 	 * @param time
 	 *            Datum fr die Datumsausgabe
 	 * @return String Der Namenstag
 	 * 
 	 */
 	public static String getDayString(Date time) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
 		return time == null ? null : dateFormat.format(time);
 	}
 
 	/**
 	 * Die Funktion generiert aus einem Date type die Uhrzeit ohne Sekunden als
 	 * String
 	 * 
 	 * @param time
 	 *            Datum fr die Datumsausgabe
 	 * @return String Die Uhrzeit
 	 * 
 	 */
 	public static String getTimeString(Date time) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
 		return time == null ? null : dateFormat.format(time);
 	}
 
 	/**
 	 * Die Funktion generiert aus einem Date type die Uhrzeit mit Sekunden als
 	 * String
 	 * 
 	 * @param time
 	 *            Datum fr die Datumsausgabe
 	 * @return String Die Uhrzeit
 	 * 
 	 */
 	public static String getTimeWitheSecondsString(Date time) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
 		return time == null ? null : dateFormat.format(time);
 	}
 
 	/**
 	 * Funktion erzeugt aus einem iCal Datum ein Datentype Date
 	 * 
 	 * @param dateStr
 	 *            iCal Datum
 	 * @return Date
 	 */
 	public static Date getDateByIcal(String dateStr) {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 		try {
 			return dateFormat.parse(dateStr);
 		} catch (ParseException e) {
 
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Die Funktion setzt das Datum auf den Anfang einer Woche
 	 * 
 	 * @param calender
 	 *            Kalender der zum Anfang der Woche zurckgesetzt werden soll
 	 * @return calender der zurckgesetzte Kalender
 	 * 
 	 */
 	public static Calendar setToMonday(Calendar calender) {
 		while (calender.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
 			calender.add(Calendar.DATE, -1);
 		}
 		return calender;
 	}
 
 	/**
 	 * Funktion gibt die aktuelle Zeit zurck die mit einem offset um x Stunden
 	 * beeinflusst werden kann
 	 * 
 	 * @param offSet
 	 *            Der offset in Stunden
 	 * @return Long Computes the time from the fields if required and returns
 	 *         the time.
 	 */
 	public static long getTime(int offSet) {
 		Date date = new Date();
 		date.setHours(date.getHours() + offSet);
 		return date.getTime();
 	}
 	/**
 	 * @return String Day of Year as String
 	 */
 	public static String getDayOfYear(){
         GregorianCalendar gc = new GregorianCalendar();
         return String.valueOf(gc.get(GregorianCalendar.DAY_OF_YEAR)+1);
 	}
 	/**
 	 * @return String Year as String
 	 */
 	public static String getYear() {
         GregorianCalendar gc = new GregorianCalendar();
         return String.valueOf(gc.get(GregorianCalendar.YEAR));
 	}
 	/**
 	 * Funktion erzeugt anhand von einen Wochentag das Datum was in der Woche
 	 * oder kommenden Woche liegt
 	 * 
 	 * @param text
 	 *            Mit den string Datum
 	 * @return
 	 */
 	public static long getDateByWeekDay(String day) {
 		Date date1 = new Date();
 		Calendar c = Calendar.getInstance();
 		c.setTime(date1);
 		if (day.equals("@string/montag")) {
 			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 		} else if (day.equals("@string/dienstag")) {
 			c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
 		} else if (day.equals("@string/mittwoch")) {
 			c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
 		} else if (day.equals("@string/donnerstag")) {
 			c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
 		} else if (day.equals("@string/freitag")) {
 			c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
 		} else if (day.equals("@string/samstag")) {
 			c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
 		} else if (day.equals("@string/sonntag")) {
 			c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
 		}
 		return date1.getTime();
 	}
 
 	/**
 	 * @param text
 	 *            Mit den string Datum
 	 * @return
 	 */
 	public static long getDateLongByText(String text) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h 'mm'min 'ss's'");
 		try {
 			Date date1 = dateFormat.parse(text);
 			// Calendar c = Calendar.getInstance();
 			// c.set(Calendar.HOUR_OF_DAY, date1.getHours());
 			// c.set(Calendar.MINUTE, date1.getMinutes());
 			// c.set(Calendar.SECOND, date1.getSeconds());
 			return date1.getTime();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	public static String getTimeShort(long diff) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("HH':'mm' '");
 		return dateFormat.format(new Date(diff));
 	}
 	public static String getTimeDiff(long time, long timeInMillis) {
 		long diff = time - timeInMillis;
 		if (diff < 0) {
 			return "Sofort";
 		}
 		long diffSeconds = diff / 1000 % 60;
 		long diffMinutes = diff / (60 * 1000) % 60;
 		long diffHours = diff / (60 * 60 * 1000);
 		String zeit = null;
 		if (diffHours == 0) {
 			zeit = fixValue(String.valueOf(diffMinutes)) + "min " + fixValue(String.valueOf(diffSeconds)) + "s";
 		} else {
 			zeit = fixValue(String.valueOf(diffHours)) + "h " + fixValue(String.valueOf(diffMinutes)) + "min "
 					+ fixValue(String.valueOf(diffSeconds)) + "s";
 		}
 		return zeit;
 	}
 
 	private static String fixValue(String valueOf) {
 		if (valueOf.length() == 0) {
 			return "00";
 		}
 		if (valueOf.length() == 1) {
 			return "0" + valueOf;
 		}
 		return valueOf;
 	}
 
 
 }
