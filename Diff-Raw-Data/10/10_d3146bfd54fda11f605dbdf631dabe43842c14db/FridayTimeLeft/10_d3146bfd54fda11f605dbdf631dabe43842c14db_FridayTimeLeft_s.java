 package com.friday_countdown.andriod;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.content.Context;
 
 public class FridayTimeLeft {
 
 	private final int MINUTE = 60 * 1000;
 	private final int HOUR = 60 * MINUTE;
 	private final int DAY = 24 * HOUR;
 
 	private Date mCurDate;
 	private Calendar mCal;
 	private Context mContext;
 
 	public boolean isFridayHasCome, isFridayStart, isLeftOne;
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
 		mCurDate = mCal.getTime();
 		
 		mCal.set(GregorianCalendar.DAY_OF_WEEK, Calendar.FRIDAY);
 		mCal.set(GregorianCalendar.HOUR_OF_DAY, goalHour);
 		mCal.set(GregorianCalendar.MINUTE, goalMinute);
 		mCal.set(GregorianCalendar.SECOND, 0);
 
 		Date startFriday = mCal.getTime();
 
 // time to start Friday		
 		if ( mCurDate.getDay() == startFriday.getDay() && 
 				mCurDate.getHours() == startFriday.getHours() &&
 				mCurDate.getMinutes() == startFriday.getMinutes() )
 			isFridayStart = true;
 		
 		mCal.set(GregorianCalendar.DAY_OF_WEEK, Calendar.SATURDAY);
 		mCal.set(GregorianCalendar.HOUR_OF_DAY, 0);
 		mCal.set(GregorianCalendar.MINUTE, 0);
 		
 		Date endFriday = mCal.getTime();
 		
 		isFridayHasCome = isFridayStart || 
 				( mCurDate.after(startFriday) && mCurDate.before(endFriday) );
 		
 // count next Friday		
 		mCal.setTime(startFriday);
 		if ( mCurDate.after( mCal.getTime() ) )
 			mCal.add( GregorianCalendar.WEEK_OF_YEAR, 1 );
 		
 		Date startNextFriday = mCal.getTime();
 		
 		long left = startNextFriday.getTime() - mCurDate.getTime();
 
 		days = (int) Math.ceil(left / DAY);
         left = left - days * DAY;
         hours = (int) Math.ceil(left / HOUR);
         left = left - hours * HOUR;
         mins = (int) Math.ceil(left / MINUTE);
 	}
 
 	public String getMessage() {
 		String msg = "";
 		
 		if ( isFridayHasCome ) {
 			if ( mContext == null )
 				return "пятница пришла!";
 			else
 				return mContext.getString(R.string.friday_has_come);
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
		final DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date curDate = dfDate.parse("2012-12-16 17:44");
 		
 		FridayTimeLeft fr = new FridayTimeLeft(curDate);
 		fr.setContext(null);
 		fr.calc(19, 0);
 		
 		System.out.println(fr.mCurDate);
 		System.out.println(fr.isFridayStart);
 		System.out.println(fr.isFridayHasCome);
 		System.out.println(fr.getLeftMessage());
 		System.out.println(fr.getMessage());
 	}
 
 }
