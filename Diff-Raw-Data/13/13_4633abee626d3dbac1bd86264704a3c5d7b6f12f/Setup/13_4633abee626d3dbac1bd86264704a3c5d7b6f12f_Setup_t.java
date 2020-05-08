 package com.kupriyanov.android.apps.gddsched;
 
 import gdd11.app.R.xml;
 
 import java.util.Locale;
 import java.util.TimeZone;
 
 
 public class Setup implements ISetup {
 
 	/*
 	 * update it on the blocks and Rooms update
 	 */
	public static final int VERSION_CURRENT = 26;
 
 	public static final String CONFERENCE_STREAM = "http://gddstream.appspot.com/?event=";
 
 	public static String BLOCK_TITLE_BREAKOUT_SESSIONS = "Breakout sessions";
 
 	public static int BLOCKS_XML = xml.blocks;
 	public static int ROOMS_XML = xml.rooms;
 
 	public static String EVENT_ID_SELECTED = null;
 
 	public enum EventId {
 		BR, AR, RU, CZ, JP, AU, IL, DE
 	}
 
 	public static String WORKSHEETS_URL = "";// "http://spreadsheets.google.com/feeds/worksheets/0Akgh73WhU1qHdHJmSUlSb0JIckowX1ZiQkhsYmdkdkE/public/basic";
 
 	public static TimeZone CONFERENCE_TIME_ZONE = TimeZone.getTimeZone("Europe/Berlin");
 
 	// http://snipplr.com/view/23131/timezone-enum/
 	public static String TIME_ZONE = "-0300"; // RFC 822 - time zone
 	public static String TIME_ZONE_SPLITTED = "-03:00"; // RFC 822 - time
 														// zone
 
 	public static int CONFERENCE_DAYS = 1;
 	public static long CONFERENCE_START_MILLIS_DAY1 = 0;
 	public static long CONFERENCE_END_MILLIS_DAY1 = 0;
 	public static long CONFERENCE_START_MILLIS_DAY2 = 0;
 	public static long CONFERENCE_END_MILLIS_DAY2 = 0;
 
 	public static Locale LOCALE = Locale.US;
 
 	public static String EVENT_YEAR = "2011";
 
 	public static String DATETIME_FORMAT = "dd.MM.yyyy HH:mm Z";
 	public static String CONFERNCE_URL = "http://www.google.com/events/developerday/2011/";
 
 	public static String CONFERENCE_HASHTAG = "#gdd11";
 
 	public static boolean FEATURE_VENDORS_ON = false;
 	public static boolean FEATURE_OFFICEHOURS_ON = false;
 	public static boolean FEATURE_MAP_ON = false;
 	public static boolean FEATURE_ANNOUNCEMENTS_ON = true;
 
 	public static String ANNOUNCEMENT_URL = "http://www.google.com/intl/pt-BR/events/developerday/2011/";
 
 	public static String EVENT_PREFIX = "";
 
 //	public static String CONTENT_AUTHORITY = "gdd11";// com.kupriyanov.android.apps.gddsched";
 
 	public static String ANALYTICS_CODE = "UA-25535360-1"; // INSERT_YOUR_ANALYTICS_UA_CODE_HERE
 
 }
