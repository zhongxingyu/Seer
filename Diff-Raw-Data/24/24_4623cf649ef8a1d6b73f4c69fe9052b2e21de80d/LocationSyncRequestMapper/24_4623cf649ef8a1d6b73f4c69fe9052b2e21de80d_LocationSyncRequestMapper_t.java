 package org.motechproject.ananya.referencedata.flw.mapper;
 
 import org.motechproject.ananya.referencedata.flw.domain.Location;
 import org.motechproject.ananya.referencedata.flw.service.request.LocationRequest;
 import org.motechproject.ananya.referencedata.flw.service.request.LocationSyncRequest;
 
 public class LocationSyncRequestMapper {
 
     public static LocationSyncRequest map(Location location) {
         Location alternateLocation = location.getAlternateLocation();
         LocationRequest existingLocation = new LocationRequest(location.getState(), location.getDistrict(), location.getBlock(), location.getPanchayat());
         LocationRequest newLocation = alternateLocation == null
                ? existingLocation : new LocationRequest(alternateLocation.getState(), alternateLocation.getDistrict(), alternateLocation.getBlock(), alternateLocation.getPanchayat());
 
         return new LocationSyncRequest(existingLocation, newLocation, location.getStatus(), location.getLastModified());
     }
 }
