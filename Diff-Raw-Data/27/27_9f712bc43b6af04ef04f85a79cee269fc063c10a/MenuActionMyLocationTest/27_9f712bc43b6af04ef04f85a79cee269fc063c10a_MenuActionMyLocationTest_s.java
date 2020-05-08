 
 package com.google.code.geobeagle.actions;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.GeoFix;
 import com.google.code.geobeagle.GeoFixProvider;
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.GeoFixProviderLive;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.activity.cachelist.actions.MenuActionMyLocation;
 
 import org.easymock.classextension.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.Locale;
 import java.util.TimeZone;
 
 @RunWith(PowerMockRunner.class)
 public class MenuActionMyLocationTest {
 
     @Test
     public void testCreate() {
         GeoFixProvider geoFixProvider = PowerMock
                 .createMock(GeoFixProviderLive.class);
         GeoFix location = PowerMock.createMock(GeoFix.class);
         GeocacheFactory geocacheFactory = PowerMock.createMock(GeocacheFactory.class);
         Geocache geocache = PowerMock.createMock(Geocache.class);
         Locale.setDefault(Locale.ENGLISH);
         TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
 
         EasyMock.expect(geoFixProvider.getLocation()).andReturn(location);
         EasyMock.expect(location.getTime()).andReturn(1000000L);
         EasyMock.expect(location.getLatitude()).andReturn(37.0);
         EasyMock.expect(location.getLongitude()).andReturn(-122.0);
         EasyMock.expect(
                 geocacheFactory.create("ML161640", "[16:16] My Location", 37.0, -122.0,
                         Source.MY_LOCATION, null, CacheType.MY_LOCATION, 0, 0, 0)).andReturn(
                 geocache);

         PowerMock.replayAll();
        new MenuActionMyLocation(null, geocacheFactory, geoFixProvider, null, null, null).act();
         PowerMock.verifyAll();
     }
 
 
 }
