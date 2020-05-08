 package is.us.util;
 
 import java.util.*;
 
 /**
  * Various utility methods for handling dates.
  * 
  * @author Hugi Þórðarson
  */
 
 public class USDateUtilities {
 
 	public static final Long MILLISECONDS_IN_DAY = 86400000L;
 
 	/**
 	 * Compares the two dates and calculates the number of days from one date to another( both dates are included ).
 	 * Time of day is irrelevant.
 	 * If the dates are the same( day, month & year are the same in the both dates ), 0 is returned.
 	 * Order of the dates is irrelevant.
 	 * 
 	 * @param date {@link Date} date to compare
 	 * @param anotherDate
 	 * @return number of days between dates, where 0 is returned when the dates are the same day
 	 * @throws USDateException if either date is null
 	 */
 	public static Long numberOfDaysBetweenDates( Date date, Date anotherDate ) {
 
 		if( date == null || anotherDate == null ) {
 			throw new IllegalArgumentException( "Unable to determine number of days between dates when both dates are not set" );
 		}
 
 		long dayDifference = 0L;
 
 		if( date.after( anotherDate ) ) {
 			dayDifference = date.getTime() - anotherDate.getTime();
 		}
 		else {
 			dayDifference = anotherDate.getTime() - date.getTime();
 		}
 
 		if( dayDifference > 0 ) {
 			dayDifference = dayDifference / MILLISECONDS_IN_DAY;
 		}
 
 		return dayDifference;
 	}
 
 	/**
 	 * Returns the number of days from date to today( both days included )
 	 * Returns 0 if date is today
 	 * 
 	 * @param date the date to compare with todays date
 	 * @return number of days from date to today
 	 * @throws USDateException if either date is null
 	 */
 	public static Long numberOfDaysFromToday( Date date ) {
 		return numberOfDaysBetweenDates( date, new Date() );
 	}
 
 	/**
 	 * @param the date to normalized
 	 * @return the given date normalized to midnight
 	 * @reviewedby Logi Helgu at Jun 24, 2009( see JIRA issue INN-622 )
 	 */
 	public static Date normalizeToMidnight( Date date ) {
 		GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.setTime( date );
 		c = normalizeToMidnight( c );
 		return c.getTime();
 	}
 
 	/**
 	 * @param calendar the calendar to normalize
 	 * @return the given calendar normalized to midnight
 	 */
 	public static GregorianCalendar normalizeToMidnight( GregorianCalendar calendar ) {
 		calendar.set( Calendar.HOUR_OF_DAY, 0 );
 		calendar.set( Calendar.MINUTE, 0 );
 		calendar.set( Calendar.SECOND, 0 );
 		calendar.set( Calendar.MILLISECOND, 0 );
 		return calendar;
 	}
 
 	/**
 	 * @param calendar the calendar to normalize
 	 * @return the given calendar normalized to the 1. of the month
 	 */
 	public static GregorianCalendar normalizeMonth( GregorianCalendar calendar ) {
 		calendar.set( Calendar.DAY_OF_MONTH, 1 );
 		calendar = normalizeToMidnight( calendar );
 		return calendar;
 	}
 
	// TODO A simple unit test would look good for these utility functions ;)
 	/**
 	* @param the date to normalized
 	* @return the given date normalized to one millisecond before next date
 	* @author BjarniS
 	* @reviewedby Logi Helgu at Oct 13, 2009( see JIRA issue INN-739 )
 	*/
 	public static Date normalizeToEndOfDay( Date date ) {
 		GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.setTime( date );
 		c = normalizeToEndOfDay( c );
 		return c.getTime();
 	}
 
 	/**
 	 * @param calendar the calendar to normalize
 	 * @return the given calendar normalized to one millisecond before next calendar date
 	 * @author BjarniS
 	 * @reviewedby Logi Helgu at Oct 13, 2009( see JIRA issue INN-739 )
 	 */
 	public static GregorianCalendar normalizeToEndOfDay( GregorianCalendar calendar ) {
 		calendar.set( Calendar.HOUR_OF_DAY, 23 );
 		calendar.set( Calendar.MINUTE, 59 );
 		calendar.set( Calendar.SECOND, 59 );
 		calendar.set( Calendar.MILLISECOND, 999 );
 		return calendar;
 	}
 
 	/**
 	 * Adds the given number of days to the given date
 	 * 
 	 * @param date the date to add days to
 	 * @param days the number of days to add, use a negative number for subtraction
 	 * @return the given date with the given days added
 	 */
 	public static Date addDays( Date date, int days ) {
 		GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.setTime( date );
 		c.add( GregorianCalendar.DAY_OF_MONTH, days );
 		date = c.getTime();
 		return date;
 	}
 
 	/**
 	 * @return true if the given date is not older than 7 days, otherwise false
 	 * @param date the date to check
 	 */
 	public static boolean isNotOlderThanSevenDays( Date date ) {
 		Date sevenDaysAgo = addDays( normalizeToMidnight( new Date() ), -7 );
 
 		if( date.before( sevenDaysAgo ) ) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * @return true if the given date is today, otherwise false
 	 */
 	public static boolean isToday( Date date ) {
 		GregorianCalendar today = (GregorianCalendar)GregorianCalendar.getInstance();
 		GregorianCalendar candidate = (GregorianCalendar)GregorianCalendar.getInstance();
 		candidate.setTime( date );
 
 		if( today.get( GregorianCalendar.YEAR ) != candidate.get( GregorianCalendar.YEAR ) ) {
 			return false;
 		}
 
 		if( today.get( GregorianCalendar.DAY_OF_YEAR ) != candidate.get( GregorianCalendar.DAY_OF_YEAR ) ) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * @return a new instance of GregorianCalendar with date normalized to midnight
 	 */
 	public static GregorianCalendar cal() {
 		GregorianCalendar cal = (GregorianCalendar)GregorianCalendar.getInstance();
 		cal = USDateUtilities.normalizeToMidnight( cal );
 		return cal;
 	}
 
 	/**
 	 * Set the calendar date to a specific weekday
 	 * Usage example: <code>calendar = setToNextDayOfWeek(calendar, GregorianCalendar.MONDAY);</code> sets the calendar with the next Monday date.
 	 * 
 	 * @param calendar to set the date to the next specified weekday
 	 * @param weekday the day of week to set to
 	 * @return GregorianCalendar where the date as been set to the desired weekday
 	 */
 	public static GregorianCalendar setToNextDayOfWeek( GregorianCalendar calendar, int weekday ) {
 		int currentDayOfWeek = calendar.get( GregorianCalendar.DAY_OF_WEEK );
 		if( currentDayOfWeek > weekday ) {
 			calendar.add( GregorianCalendar.DAY_OF_MONTH, -currentDayOfWeek + weekday + 7 );
 		}
 		else {
 			calendar.add( GregorianCalendar.DAY_OF_MONTH, -currentDayOfWeek + weekday );
 		}
 		return calendar;
 	}
 
 	/**
 	 * Indicates if the given date is between monday and friday, inclusice.
 	 * Does not take into account holidays.
 	 * 
 	 * @param date the date to check
 	 * @return true if and only if the given date is not a saturday or a sunday.
 	 */
 	public static boolean isWorkday( Date date ) {
 		GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
 		c.setTime( date );
 		int dayOfWeek = c.get( GregorianCalendar.DAY_OF_WEEK );
 		if( dayOfWeek == GregorianCalendar.SATURDAY || dayOfWeek == GregorianCalendar.SUNDAY )
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Calculates the age of a person given a birthdate and another date.
 	 * 
 	 * @param birthDate The birthdate of the person to check.
 	 * @param date The date at which we want to know the person's age.
 	 * @return The person's age at [date]
 	 */
 	public static Integer ageAtDate( Date birthDate, Date date ) {
 
 		if( birthDate == null || date == null ) {
 			return null;
 		}
 
 		GregorianCalendar dateCalendar = (GregorianCalendar)GregorianCalendar.getInstance();
 		dateCalendar.setTime( date );
 
 		GregorianCalendar birthdateCalendar = (GregorianCalendar)GregorianCalendar.getInstance();
 		birthdateCalendar.setTime( birthDate );
 
 		int ageInYears = dateCalendar.get( Calendar.YEAR ) - birthdateCalendar.get( Calendar.YEAR );
 
 		if( (birthdateCalendar.get( Calendar.MONTH ) == dateCalendar.get( Calendar.MONTH )) && (birthdateCalendar.get( Calendar.DAY_OF_MONTH ) <= dateCalendar.get( Calendar.DAY_OF_MONTH )) ) {
 			return ageInYears;
 		}
 		else if( birthdateCalendar.get( Calendar.MONTH ) < dateCalendar.get( Calendar.MONTH ) ) {
 			return ageInYears;
 		}
 
 		return ageInYears - 1;
 	}
 
 	/**
 	 * Constructs a date at midnight on the given day.
 	 * 
 	 * Please note that months are not zero-based! That means January = 1.
 	 */
 	public static Date date( int year, int month, int day ) {
 		GregorianCalendar calendar = (GregorianCalendar)GregorianCalendar.getInstance();
 		calendar.clear();
 		calendar.set( Calendar.YEAR, year );
 		calendar.set( Calendar.MONTH, month - 1 );
 		calendar.set( Calendar.DAY_OF_MONTH, day );
 		calendar.set( Calendar.HOUR_OF_DAY, 0 );
 		calendar.set( Calendar.MINUTE, 0 );
 		calendar.set( Calendar.SECOND, 0 );
 		calendar.set( Calendar.MILLISECOND, 0 );
 
 		return calendar.getTime();
 	}
 }
