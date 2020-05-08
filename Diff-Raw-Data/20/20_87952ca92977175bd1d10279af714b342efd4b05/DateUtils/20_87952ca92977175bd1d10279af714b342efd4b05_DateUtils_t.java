 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile: DateUtils.java,v $ 
	Created on $Date: 2003-06-17 23:05:41 $ 
 */
 package net.sf.statcvs.util;
 
 import net.sf.statcvs.Messages;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 /** 
  * Utility functions for date handling
  * @author Lukasz Pekacki
 * @version $Id: DateUtils.java,v 1.2 2003-06-17 23:05:41 squig Exp $
  */
 public class DateUtils {
 	private static final String LOG_TIMESTAMP_FORMAT =
 		"yyyy/MM/dd HH:mm:ss zzz";
 	private static final Locale LOG_TIMESTAMP_LOCALE = Locale.US;
 
 	private static SimpleDateFormat logTimeFormat =
 		new SimpleDateFormat(LOG_TIMESTAMP_FORMAT, LOG_TIMESTAMP_LOCALE);

	// the commented lines caused the maven junit test to throw
	// weird exceptions

 	private static SimpleDateFormat outputDateFormat =
		new SimpleDateFormat();//Messages.getString("DATE_FORMAT"));
 	private static SimpleDateFormat outputDateTimeFormat =
		new SimpleDateFormat();//Messages.getString("DATE_TIME_FORMAT"));
 
 	/**
 	 * Method currentDate.
 	 * @return String of the today date
 	 */
 	public static String currentDate() {
 		Calendar cal = Calendar.getInstance();
 		return outputDateFormat.format(cal.getTime());
 	}
 
 	/**
 	 * Method getShortDate.
 	 * @param date input
 	 * @return String short representation of the given Date
 	 */
 	public static String formatDate(Date date) {
 		return outputDateFormat.format(date);
 	}
 
 	/**
 	 * Method getDateAndTime.
 	 * @param date input Date
 	 * @return String returns date format compliant date and time string
 	 */
 	public static String formatDateAndTime(Date date) {
 		return outputDateTimeFormat.format(date);
 	}
 
 	/**
 	 * Returns a date from a given modTime String of a cvs logfile
 	 * @param modTime modTime String of a cvs logfile
 	 * @return Date date from a given modTime String of a cvs logfile
 	 */
 	public static Date convertFromLogTime(String modTime) {
 		try {
 			return logTimeFormat.parse(modTime);
 		} catch (ParseException e) {
 			// fallback is to return null
 			return null;
 		}
 	}
}
