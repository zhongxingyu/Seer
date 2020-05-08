 package com.chaman.model;
 
 //import java.util.Calendar;
 import java.util.List;
 //import java.util.TimeZone;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.beoui.geocell.LocationCapableRepositorySearch;
 import com.googlecode.objectify.Objectify;
 
 public class OfyEntityLocationCapableRepositorySearchImpl implements
 		LocationCapableRepositorySearch<EventLocationCapable> {
 
 	private Objectify ofy;
 	private long timeStampLimit;
 	
 	public OfyEntityLocationCapableRepositorySearchImpl(Objectify ofy, String timeZone, int timeFrame) {
 		
 		this.ofy = ofy;
 		
		/*Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.add(Calendar.MINUTE, Integer.parseInt(timeZone));
		c.add(Calendar.HOUR, timeFrame);
		timeStampLimit = c.getTimeInMillis() / 1000L;*/
		
 		int timeZoneInMinutes	= Integer.parseInt(timeZone);		
 		DateTimeZone TZ = DateTimeZone.forOffsetMillis(timeZoneInMinutes*60*1000);
 		DateTime now = DateTime.now(TZ).plusMinutes(timeFrame * 60);	
 		long timeStampNow = now.getMillis();
 		long timeStampToday = timeStampNow - (timeZoneInMinutes * 60000) - now.getMillisOfDay();
 		timeStampLimit = timeStampToday / 1000L;
		
		/*DateTimeZone T = DateTimeZone.forID("America/Los_Angeles");
		DateTime now = DateTime.now(T).plusMinutes(timeZoneInMinutes + (timeFrame * 60));
		long timeStampNow = now.getMillis();
		long timeStampToday = timeStampNow + (86400000 - now.getMillisOfDay());
		timeStampLimit = timeStampToday / 1000;*/
 	}
 	
 	@Override
 	public List<EventLocationCapable> search(List<String> geocells) {
 		
 		return ofy.query(EventLocationCapable.class).filter("geocells in", geocells).filter("timeStampStart <=", timeStampLimit).list();
 	}
 
 }
