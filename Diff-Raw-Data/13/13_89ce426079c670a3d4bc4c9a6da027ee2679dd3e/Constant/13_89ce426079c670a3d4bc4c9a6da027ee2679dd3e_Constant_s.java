 package org.kaoriha.flowerflower.compile;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.joda.time.LocalTime;
 import org.joda.time.Period;
 import org.joda.time.format.PeriodFormat;
 
 public class Constant {
 	private static final ResourceBundle RESOURCE = ResourceBundle
 			.getBundle("flowerflower");
 
 	public static final String PROJECT_NAME = RESOURCE.getString("projectName");
 
 	public static final LocalTime RELEASE_TIME = LocalTime.parse(RESOURCE
 			.getString("releaseTime"));
 	public static final Period TEST_RELEASE_PERIOD;
 	static {
 		Period p;
 		try {
 			p = Period.parse(RESOURCE.getString("testReleasePeriod"), PeriodFormat.getDefault());
 		} catch (MissingResourceException e) {
 			p = null;
 		}
 		TEST_RELEASE_PERIOD = p;
 	}
 
 	public static final String DATE_DIR_NAME_FORMAT = "yyyyMMdd_HHmm";
 
 	public static final String CATALOGUE_FILENAME = "catalogue.json";
 	public static final String CATALOGUE_LOCAL_KEY = "local";
 	public static final String CATALOGUE_EXPRESS_KEY = "express";
 	public static final String CATALOGUE_NEXT_RELEASE_SCHEDULE_KEY = "next_release";
 	public static final String CATALOGUE_PUSH_MESSAGE = "push_message";
 
 	public static final String AUTH_DIR_NAME = "Auth";
 	public static final String PUBLIC_DIR_NAME = "Public";
 	public static final String LATEST_SEPARATION_HTML_FILENAME = "latest.html";
 
 	private static final ResourceBundle PROJECT_RESOURCE = ResourceBundle
 			.getBundle(RESOURCE.getString("projectProperties"));
 
 	public static final String CHARACTER_NOTE_INITIAL_KEY = "character_note";
 	public static final String CHARACTER_NOTE_NAME = PROJECT_RESOURCE.getString("character_note_name");
 	public static final String LATEST_SEPARATION_NAME = PROJECT_RESOURCE.getString("latest_separation_name");
 
 	public static class Html {
 		public static final String FORMAT = PROJECT_RESOURCE.getString("html");
 		public static final String TITLE = "title";
 		public static final String INDEX = "index";
 		public static final String CONTENT = "content";
 	}
 
 	public static class LatestSeparationHtml {
 		public static final String FORMAT = PROJECT_RESOURCE.getString("latest_separation_html");
 		public static final String TITLE = "title";
 		public static final String INDEX = "index";
 		public static final String CONTENT = "content";
 		public static final String yyyy = "yyyy";
 		public static final String MM = "MM";
 		public static final String dd = "dd";
 	}
 }
