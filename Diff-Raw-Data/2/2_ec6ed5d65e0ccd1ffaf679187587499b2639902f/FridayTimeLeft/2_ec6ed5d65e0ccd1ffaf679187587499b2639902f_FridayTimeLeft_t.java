 package com.friday_countdown.andriod;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 import android.content.Context;
 
 public class FridayTimeLeft {
 
 	private final int MINUTE = 60 * 1000;
 	private final int HOUR = 60 * MINUTE;
 	private final int DAY = 24 * HOUR;
 
 	private Calendar mCal;
 	private Context mContext;
 
 	public boolean isFridayHasCome, isFridayStart, isSaturdayHasCome, isSundayHasCome;
 	private int leftMessageId = R.string.friday_time_left;
 	private int days, hours, mins;
 	
 
 	public FridayTimeLeft() {
 		mCal = GregorianCalendar.getInstance();
 	}
 
 	public FridayTimeLeft(Date today) {
 		mCal = GregorianCalendar.getInstance();
 		mCal.setTime(today);
 	}
 
 	public void setContext(Context context) {
 		mContext = context;
 	}
 	
 	public void calc(int goalHour, int goalMinute) {
 		Calendar curDate = (Calendar) mCal.clone();
 		
 		mCal.setFirstDayOfWeek(Calendar.MONDAY);
 		mCal.set(GregorianCalendar.DAY_OF_WEEK, Calendar.FRIDAY);
 		mCal.set(GregorianCalendar.HOUR_OF_DAY, goalHour);
 		mCal.set(GregorianCalendar.MINUTE, goalMinute);
 		mCal.set(GregorianCalendar.SECOND, 0);
 
 		Date startFriday = mCal.getTime();
 		Calendar startFridayCal = (Calendar) mCal.clone();
 
 // time to start Friday		
 		if ( curDate.get(GregorianCalendar.DAY_OF_MONTH) == startFridayCal.get(GregorianCalendar.DAY_OF_MONTH) && 
				curDate.get(GregorianCalendar.HOUR_OF_DAY) == startFridayCal.get(GregorianCalendar.HOUR_OF_DAY) &&
 				curDate.get(GregorianCalendar.MINUTE) == startFridayCal.get(GregorianCalendar.MINUTE) )
 			isFridayStart = true;
 		
 		mCal.set(GregorianCalendar.HOUR_OF_DAY, 23);
 		mCal.set(GregorianCalendar.MINUTE, 59);
 		mCal.set(GregorianCalendar.SECOND, 59);
 		
 		Calendar endFriday = (Calendar) mCal.clone();
 		
 		isFridayHasCome = isFridayStart || 
 				( curDate.after(startFridayCal) && curDate.before(endFriday) );
 		
 // count the next weekend day 		
 		mCal.set(GregorianCalendar.DAY_OF_WEEK, Calendar.SATURDAY);
 		mCal.set(GregorianCalendar.HOUR_OF_DAY, 23);
 		mCal.set(GregorianCalendar.MINUTE, 59);
 		mCal.set(GregorianCalendar.SECOND, 59);
 		
 		Calendar endSaturday = (Calendar) mCal.clone();
 
 		isSaturdayHasCome = curDate.after(endFriday) && curDate.before(endSaturday);
 
 		mCal.set(GregorianCalendar.DAY_OF_WEEK, Calendar.SUNDAY);
 				
 		Calendar endSunday = (Calendar) mCal.clone();
 				
 		isSundayHasCome = curDate.after(endSaturday) && curDate.before(endSunday);
 		
 		
 // count the next Friday		
 		mCal.setTime(startFriday);
 		if ( curDate.after( mCal ) )
 			mCal.add( GregorianCalendar.WEEK_OF_YEAR, 1 );
 		
 		Calendar startNextFriday = (Calendar) mCal.clone();
 		
 		long left = startNextFriday.getTimeInMillis() - curDate.getTimeInMillis();
 
 		days = (int) Math.ceil(left / DAY);
         left = left - days * DAY;
         hours = (int) Math.ceil(left / HOUR);
         left = left - hours * HOUR;
         mins = (int) Math.ceil(left / MINUTE);
         left = left - mins * MINUTE;
         
 // if left some seconds - increase minutes        
         if ( left > 0 )
         	mins++;
 	}
 
 	public String getMessage() {
 		String msg = "";
 		
 		if (isFridayHasCome) {
 			return (mContext == null) ? 
 					"пятница пришла!" : 
 					mContext.getString(R.string.friday_has_come);
 		}
 		if (isSaturdayHasCome) {
 			return (mContext == null) ? 
 					"уже суббота" : 
 					mContext.getString(R.string.saturday_has_come);
 		}
 		if (isSundayHasCome) {
 			return (mContext == null) ? 
 					"все еще воскресенье" : 
 					mContext.getString(R.string.sunday_has_come);
 		}
 
 		else {
 // left days
 			if ( days > 0 )
 				return getPluralDays();
 // left hours
 			else if ( hours > 0 )
 				msg = getPluralHours();
 // left minutes
 			else if ( mins >= 0 )
 				msg = getPluralMinutes();
 		}
 		
 		return msg;
 	}
 	
 	private static boolean checkLeftPluralOne(int count) {
 		int last_digit = count % 10;
 		int last_two_digits = count % 100;
 
 		return last_digit == 1 && last_two_digits != 11;
 	}	
 	
 	private String getPluralDays() {
 		if ( mContext == null )
 			return getPluralNumber(days, "д", "ень", "ня", "ней");
 		else
 			return getPluralNumber(days, 
 					mContext.getString(R.string.plural_day0), 
 					mContext.getString(R.string.plural_day1), 
 					mContext.getString(R.string.plural_day2), 
 					mContext.getString(R.string.plural_day3));
 	}
 
 	private String getPluralHours() {
 		if ( mContext == null )
 			return getPluralNumber(hours, "час", "", "а", "ов");		
 		else
 			return getPluralNumber(hours, 
 					mContext.getString(R.string.plural_hour0), 
 					mContext.getString(R.string.plural_hour1), 
 					mContext.getString(R.string.plural_hour2), 
 					mContext.getString(R.string.plural_hour3));
 	}
 
 	private String getPluralMinutes() {
 		if ( mContext == null )
 			return getPluralNumber(mins, "минут", "а", "ы", "");		
 		else
 			return getPluralNumber(mins, 
 					mContext.getString(R.string.plural_min0), 
 					mContext.getString(R.string.plural_min1), 
 					mContext.getString(R.string.plural_min2), 
 					mContext.getString(R.string.plural_min3));
 	}
 	
 	private static String getPluralNumber(int count, String arg0, String arg1, String arg2, String arg3) {
 		StringBuffer result = new StringBuffer();
 
 		result.append(count);
 		result.append(" ");
 		result.append(arg0);
 		
 		int last_digit = count % 10;
 		int last_two_digits = count % 100;
 
 		if (last_digit == 1 && last_two_digits != 11)
 			result.append(arg1);
 		else if ((last_digit == 2 && last_two_digits != 12) || 
 				(last_digit == 3 && last_two_digits != 13) || 
 				(last_digit == 4 && last_two_digits != 14))
 			result.append(arg2);
 		else
 			result.append(arg3);
 
 		return result.toString();
 	}	
 
 // select "left" phrase	
 	public String getLeftMessage() {
 		if ( days > 0 ) {
 			if ( checkLeftPluralOne(days) )
 				leftMessageId = R.string.friday_time_left_day1;
 		}
 		else if ( hours > 0 ) {
 			if ( hours > 0 && checkLeftPluralOne(hours) )
 				leftMessageId = R.string.friday_time_left_hour1;
 		}
 		else if ( mins > 0 ) {
 			if ( checkLeftPluralOne(mins) )
 				leftMessageId = R.string.friday_time_left_min1;
 		}
 
 		
 		if ( mContext != null )
 			return mContext.getString(leftMessageId);
 		else
 			switch (leftMessageId) {
 				case R.string.friday_time_left_day1:
 					return "остался";
 				case R.string.friday_time_left_hour1:
 					return "остался";
 				case R.string.friday_time_left_min1:
 					return "осталась";
 
 			default:
 				return "осталось";
 			}
 	}
 	
 	public static void main(String[] args) throws ParseException {
 		final DateFormat dfDate = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss", Locale.getDefault() );
 		Date curDate = dfDate.parse("2013-03-31 23:58:00");
 		
 		FridayTimeLeft fr = new FridayTimeLeft(curDate);
 		fr.setContext(null);
 		fr.calc(19, 0);
 		
 		System.out.println(fr.mCal);
 		System.out.println(fr.isFridayStart);
 		System.out.println(fr.isFridayHasCome);
 		System.out.println(fr.getLeftMessage());
 		System.out.println(fr.getMessage());
 	}
 
 }
