 package com.chaman.model;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import com.beoui.geocell.LocationCapableRepositorySearch;
 import com.googlecode.objectify.Objectify;
 
 public class OfyEntityLocationCapableRepositorySearchImpl implements
 		LocationCapableRepositorySearch<EventLocationCapable> {
 
 	private Objectify ofy;
 	private long timeStampLimit;
 	
 	public OfyEntityLocationCapableRepositorySearchImpl(Objectify ofy, String timeZone, int timeFrame) {
 		
 		this.ofy = ofy;
 		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //TODO: What is this? Should it be PST?
 		c.add(Calendar.MINUTE, Integer.parseInt(timeZone));
 		c.add(Calendar.HOUR, timeFrame);
 		timeStampLimit = c.getTimeInMillis() / 1000L;
 	}
 	
 	@Override
 	public List<EventLocationCapable> search(List<String> geocells) {
 		
 		return ofy.query(EventLocationCapable.class).filter("timeStampStart <", timeStampLimit).filter("geocells in", geocells).list();
 	}
 
 }
