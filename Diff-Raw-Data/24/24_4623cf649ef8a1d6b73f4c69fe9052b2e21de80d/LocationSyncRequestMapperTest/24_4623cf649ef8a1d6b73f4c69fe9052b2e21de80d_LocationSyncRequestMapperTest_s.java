 package org.motechproject.ananya.referencedata.flw.mapper;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.motechproject.ananya.referencedata.flw.domain.Location;
 import org.motechproject.ananya.referencedata.flw.domain.LocationStatus;
 import org.motechproject.ananya.referencedata.flw.service.request.LocationRequest;
 import org.motechproject.ananya.referencedata.flw.service.request.LocationSyncRequest;
 
 import static org.junit.Assert.assertEquals;
 
 public class LocationSyncRequestMapperTest {
 
     @Test
     public void shouldMapLocationWithAlternateLocation() {
         Location alternateLocation = new Location("D1", "B1", "P1", "state", LocationStatus.VALID, null);
         Location location = new Location("D2", "B2", "P2", "state", LocationStatus.NOT_VERIFIED, alternateLocation);
         location.setLastModified(DateTime.now());
 
         LocationSyncRequest syncRequest = LocationSyncRequestMapper.map(location);
 
         assertEquals(new LocationRequest(location.getState(), location.getDistrict(), location.getBlock(), location.getPanchayat()), syncRequest.getExistingLocation());
        assertEquals(new LocationRequest(location.getState(), alternateLocation.getDistrict(), alternateLocation.getBlock(), alternateLocation.getPanchayat()), syncRequest.getNewLocation());
         assertEquals(location.getStatus(), syncRequest.getLocationStatus());
         assertEquals(location.getLastModified(), syncRequest.getLastModifiedTime());
     }
 
     @Test
     public void shouldMapLocationWithoutAlternateLocation() {
         Location location = new Location("D2", "B2", "P2", "state", LocationStatus.NOT_VERIFIED, null);
         location.setLastModified(DateTime.now());
 
         LocationSyncRequest syncRequest = LocationSyncRequestMapper.map(location);
 
         LocationRequest expectedLocationRequest = new LocationRequest(location.getState(), location.getDistrict(), location.getBlock(), location.getPanchayat());
         assertEquals(location.getStatus(), syncRequest.getLocationStatus());
         assertEquals(location.getLastModified(), syncRequest.getLastModifiedTime());
         assertEquals(expectedLocationRequest, syncRequest.getExistingLocation());
         assertEquals(expectedLocationRequest, syncRequest.getNewLocation());
     }
 }
