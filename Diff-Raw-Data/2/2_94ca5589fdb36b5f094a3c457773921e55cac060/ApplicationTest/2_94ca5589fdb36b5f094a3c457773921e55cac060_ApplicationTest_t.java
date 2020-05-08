 package org.atlasapi.application.v3;
 
 import static org.junit.Assert.*;
 
 import org.atlasapi.application.v3.Application;
 import org.atlasapi.application.v3.ApplicationCredentials;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.metabroadcast.common.time.DateTimeZones;
 
 
 public class ApplicationTest {
 
     @Test
     public void shouldCreateApplication() {
         assertNotNull(Application.application("test-slug").withCredentials(new ApplicationCredentials("apiKey")).build());
     }
     
     @Test
     public void testLastUpdated() {
         Application application = Application.application("test-slug").withCredentials(new ApplicationCredentials("apiKey")).build();
        assertNotNull(application.getLastUpdated());
         DateTime fixed = new DateTime(DateTimeZones.UTC).withDate(2013, 12, 13).withTime(9, 10, 20, 0);
         application = application.copy().withLastUpdated(fixed).build();
         assertEquals(fixed, application.getLastUpdated());
     }
 }
