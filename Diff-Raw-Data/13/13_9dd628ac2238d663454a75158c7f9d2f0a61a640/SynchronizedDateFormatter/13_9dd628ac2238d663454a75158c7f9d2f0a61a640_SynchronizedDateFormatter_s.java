 package com.luntsys.luntbuild.utility;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A synchronized wrapper around SimpleDateFormat.
  */
 public class SynchronizedDateFormatter {
 	
     private static List formats = new ArrayList();
     static {
     	formats.add(new SynchronizedDateFormatter("yyyy.MM.dd-hh:mm"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-dd hh:mm:ss zzz"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-dd'T'HH:mm:ssZ"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-ddTHH:mm:ssZ"));
     	formats.add(new SynchronizedDateFormatter("dd-MMMM-yyyy.HH:mm:ss"));
     	formats.add(new SynchronizedDateFormatter("yyyy/MM/dd HH:mm:ss"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-dd HH:mm:ss"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-dd'T'HH:mm:ss'Z'"));
     	formats.add(new SynchronizedDateFormatter("EEE, d MMM yyyy HH:mm:ss Z"));
     	formats.add(new SynchronizedDateFormatter("EEE, d MMM yyyy HH:mm:ss"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS Z"));
     	formats.add(new SynchronizedDateFormatter("M/dd/yy;h:mm:ssa"));
     	formats.add(new SynchronizedDateFormatter("yyyy-MM-dd HH:mm"));
     	
         formats.add(new SynchronizedDateFormatter(DateFormat.getDateInstance(SimpleDateFormat.FULL), "full"));
         formats.add(new SynchronizedDateFormatter(DateFormat.getDateInstance(SimpleDateFormat.LONG), "long"));
         formats.add(new SynchronizedDateFormatter(DateFormat.getDateInstance(SimpleDateFormat.MEDIUM), "medium"));
         formats.add(new SynchronizedDateFormatter(DateFormat.getDateInstance(SimpleDateFormat.SHORT), "short"));
         formats.add(new SynchronizedDateFormatter(DateFormat.getDateInstance(SimpleDateFormat.DEFAULT), "default"));
 
         formats.add(new SynchronizedDateFormatter("MM/dd/yyyy"));
         formats.add(new SynchronizedDateFormatter("MM-dd-yyyy"));
         formats.add(new SynchronizedDateFormatter("d MMM yyyy"));
         formats.add(new SynchronizedDateFormatter("d-MMM-yyyy"));
         formats.add(new SynchronizedDateFormatter("d MMM. yyyy"));
         formats.add(new SynchronizedDateFormatter("MMM d, yyyy"));
         formats.add(new SynchronizedDateFormatter("MMM. d, yyyy"));
         
         formats.add(new SynchronizedDateFormatter("hh:mm"));        
     }
 
 	private ThreadLocal delegate;
     private String pattern;
     
 	/**
 	 * Instantiates a new synchronized date formatter.
 	 * 
 	 * @param format the format
 	 */
 	public SynchronizedDateFormatter(final DateFormat format, String pattern) {
         delegate = new ThreadLocal() {
             protected synchronized Object initialValue() {
                 return format;
             }
         };
         this.pattern = pattern;
 	}
 	
     /**
      * Instantiates a new synchronized date formatter.
      * 
      * @param formatStr the format str
      */
     public SynchronizedDateFormatter(final String formatStr) {
         delegate = new ThreadLocal() {
             protected synchronized Object initialValue() {
                 return new SimpleDateFormat(formatStr);
             }
         };
         pattern = formatStr;
     }
     
     /**
      * Format the date.
      * 
      * @param date the date
      * 
      * @return the string
      */
     public synchronized String format(Date date) {
        return ((SimpleDateFormat)delegate.get()).format(date);
     }
     
     /**
      * Gets the simple date format.
      * 
      * @return the simple date format
      */
     public synchronized SimpleDateFormat get() {
     	return (SimpleDateFormat)delegate.get();
     }
     
     /**
      * Parses the date.
      * 
      * @param dateStr the date str
      * 
      * @return the date
      * 
      * @throws ParseException the parse exception
      */
     public synchronized Date parse(String dateStr) throws ParseException {
        return ((SimpleDateFormat)delegate.get()).parse(dateStr);
     }
     
     /**
      * Gets the format.
      * 
      * @param formatStr the format str
      * 
      * @return the format
      */
     public static synchronized SimpleDateFormat getFormat(String formatStr) {
     	return getFormatByName(formatStr).get();
     }
     
     /**
      * Format date.
      * 
      * @param date the date
      * 
      * @return the string
      */
     public static final String formatDate(Date date) {
     	return formatDate(date, null);
     }
     
     /**
      * Format date.
      * 
      * @param date the date
      * @param defaultFormat the default format
      * 
      * @return the string
      */
     public static synchronized final String formatDate(Date date, String defaultFormat) {
     	if (defaultFormat != null) {
 			SynchronizedDateFormatter formatter = getFormatByName(defaultFormat);
 			try {
 				String dateStr = formatter.format(date);
 				if (dateStr != null && dateStr.trim().length() > 0) return dateStr;
 			} catch (Exception e) {
 		           // try again with another formats
 			}
     	}
     	for (Iterator it = formats.iterator(); it.hasNext();) {
 			SynchronizedDateFormatter formatter = (SynchronizedDateFormatter)it.next();
 			try {
 				String dateStr = formatter.format(date);
 				if (dateStr != null && dateStr.trim().length() > 0) return dateStr;
 			} catch (Exception e) {
 		           // try again with another format
 			}
 		}
     	return null;
     }
     
     /**
      * Parses the date.
      * 
      * @param dateStr the date str
      * 
      * @return the date
      */
     public static final Date parseDate(String dateStr) {
     	return parseDate(dateStr, null);
     }
     
     /**
      * Parses the date.
      * 
      * @param dateStr the date str
      * @param defaultFormat the default format
      * 
      * @return the date
      */
     public static synchronized final Date parseDate(String dateStr, String defaultFormat) {
     	if (defaultFormat != null) {
 			SynchronizedDateFormatter formatter = getFormatByName(defaultFormat);
 			try {
 				Date date = formatter.parse(dateStr);
 				if (date != null) return date;
 			} catch (Exception e) {
 		           // try again with another format
 			}
     	}
     	for (Iterator it = formats.iterator(); it.hasNext();) {
 			SynchronizedDateFormatter formatter = (SynchronizedDateFormatter)it.next();
 			try {
 				Date date = formatter.parse(dateStr);
 				if (date != null) return date;
 			} catch (Exception e) {
 		           // try again with another format
 			}
 		}
     	return null;
     }
     
     private static SynchronizedDateFormatter getFormatByName(String defaultFormat) {
     	for (Iterator it = formats.iterator(); it.hasNext();) {
 			SynchronizedDateFormatter formatter = (SynchronizedDateFormatter)it.next();
 			if (formatter.pattern.equals(defaultFormat)) return formatter;
     	}
     	return new SynchronizedDateFormatter(DateFormat.getDateInstance(SimpleDateFormat.MEDIUM), "medium");
     }
 }
