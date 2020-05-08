 package net.animeimports.android;
 
 import com.google.api.client.googleapis.GoogleUrl;
 import com.google.api.client.util.DateTime;
 import com.google.api.client.util.Key;
 
 public class CustomCalendarURL extends GoogleUrl {
 	private static final String CALENDAR_URL = "https://www.google.com/calendar/feeds/anime.imports.com%40gmail.com/public/full";
 	
 	@Key("start-min")
 	public DateTime startMin;
 	
 	@Key("start-max")
 	public DateTime startMax;
 	
	@Key("order-by")
	public String orderBy = "startTime";
	
 	public CustomCalendarURL(String encodeUrl) {
 		super(encodeUrl);
 	}
 	
 	private static CustomCalendarURL root() {
 		return new CustomCalendarURL(CALENDAR_URL);
 	}
 	
 	public static CustomCalendarURL getUrl() {
 		CustomCalendarURL result = root();
 		return result;
 	}
 }
