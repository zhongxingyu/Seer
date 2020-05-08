 package com.orgsync.api;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.junit.Test;
 
 import com.ning.http.client.FluentStringsMap;
 import com.orgsync.api.model.events.EventQueryParams;
 
 public class EventsResourceImplTest extends BaseResourceTest {
 
     private final EventsResourceImpl events = new EventsResourceImpl(client);
 
     @Test
     public void testGetEvents() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         cal.set(2013, 0, 1, 10, 25, 35); // 0th month is Jan...
 
         Date startTime = cal.getTime();
         String keyword = "test";
 
         EventQueryParams params = new EventQueryParams().setStartDate(startTime).setKeyword(keyword);
 
         events.getEvents(params);
 
         FluentStringsMap queryParams = new FluentStringsMap()
                 .add("keyword", keyword)
                 .add("start_date", "2013-01-01T10:25:35Z");
 
         verifyRequest(RequestParams.get("/events", queryParams));
     }
 
     @Test
     public void testGetOrgEvents() throws Exception {
         events.getOrgEvents(321, new EventQueryParams());
 
         verifyRequest(RequestParams.get("/orgs/321/events"));
     }
 
     @Test(expected = NullPointerException.class)
     public void testGetEventsThrowsNPEForNoQuery() throws Exception {
         events.getEvents(null);
     }
 
     @Test(expected = NullPointerException.class)
     public void testGetOrgEventsThrowsNPEForNoQuery() throws Exception {
         events.getOrgEvents(123, null);
     }
 
 }
