 package org.motechproject.ananya.reports.kilkari.service;
 
 import org.apache.log4j.Logger;
 import org.motechproject.ananya.reports.kilkari.contract.request.LocationRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.LocationSyncRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriberLocation;
 import org.motechproject.ananya.reports.kilkari.domain.LocationStatus;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.LocationDimension;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.Subscriber;
 import org.motechproject.ananya.reports.kilkari.repository.AllLocationDimensions;
 import org.motechproject.ananya.reports.kilkari.repository.AllSubscribers;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.sql.Timestamp;
 import java.util.List;
 
 @Service
 public class LocationService {
 
     private AllLocationDimensions allLocationDimensions;
     private AllSubscribers allSubscribers;
     private static Logger logger = Logger.getLogger(LocationService.class);
    private static final Object SYNC_LOCK = new Object();
 
 
     public LocationService() {
     }
 
     @Autowired
     public LocationService(AllLocationDimensions allLocationDimensions, AllSubscribers allSubscribers) {
         this.allLocationDimensions = allLocationDimensions;
         this.allSubscribers = allSubscribers;
     }
 
     @Transactional
     public LocationDimension digDeepAndFetchFor(String state, String district, String block, String panchayat) {
         LocationDimension locationDimension = allLocationDimensions.fetchFor(state, district, block, panchayat);
         return locationDimension != null ? getValidLocation(locationDimension) : null;
     }
 
     @Transactional
     public void addOrUpdate(LocationSyncRequest locationSyncRequest) {
        synchronized (SYNC_LOCK) {
             if (isNotLatestRequest(locationSyncRequest)) {
                 logger.info("Not syncing " + locationSyncRequest + " since it is not the latest request.");
                 return;
             }
 
             LocationStatus locationStatus = LocationStatus.getFor(locationSyncRequest.getLocationStatus());
             LocationRequest existingLocationRequest = locationSyncRequest.getExistingLocation();
             LocationDimension existingLocation = allLocationDimensions.fetchFor(existingLocationRequest.getState(), existingLocationRequest.getDistrict(), existingLocationRequest.getBlock(), existingLocationRequest.getPanchayat());
             LocationDimension newLocation = null;
             if (locationStatus.equals(LocationStatus.INVALID)) {
                 newLocation = createNewLocation(locationSyncRequest, existingLocation);
             }
             createOrUpdateExistingLocation(existingLocation, newLocation, locationSyncRequest);
         }
     }
 
     //Don't put @Transactional on me. The callers are already @Transactional
     public LocationDimension createAndFetch(SubscriberLocation location) {
         if (location == null) return null;
 
         LocationDimension locationDimension = digDeepAndFetchFor(location.getState(), location.getDistrict(), location.getBlock(), location.getPanchayat());
         if (locationDimension != null)
             return locationDimension;
 
         locationDimension = new LocationDimension(location.getState(), location.getDistrict(), location.getBlock(), location.getPanchayat(), LocationStatus.NOT_VERIFIED.name());
         allLocationDimensions.createOrUpdate(locationDimension);
         return locationDimension;
     }
 
     private boolean isNotLatestRequest(LocationSyncRequest locationSyncRequest) {
         LocationRequest existingLocation = locationSyncRequest.getExistingLocation();
         LocationDimension locationDimension = allLocationDimensions.fetchFor(existingLocation.getState(), existingLocation.getDistrict(), existingLocation.getBlock(), existingLocation.getPanchayat());
         return locationDimension != null && locationDimension.getLastModified() != null && locationDimension.getLastModified().after(locationSyncRequest.getLastModifiedTime().toDate());
     }
 
     private LocationDimension getValidLocation(LocationDimension locationDimension) {
         return locationDimension.isInvalidLocation() ? getValidLocation(locationDimension.getAlternateLocation()) : locationDimension;
     }
 
     private void createOrUpdateExistingLocation(LocationDimension existingLocation, LocationDimension newLocation, LocationSyncRequest locationSyncRequest) {
         LocationRequest existingLocationRequest = locationSyncRequest.getExistingLocation();
         String locationStatus = locationSyncRequest.getLocationStatus();
         existingLocation = existingLocation == null ?
                 new LocationDimension(existingLocationRequest.getState(), existingLocationRequest.getDistrict(), existingLocationRequest.getBlock(), existingLocationRequest.getPanchayat(), locationStatus)
                 : existingLocation;
         existingLocation.setStatus(locationStatus);
         existingLocation.setAlternateLocation(newLocation);
         existingLocation.setLastModified(new Timestamp(locationSyncRequest.getLastModifiedTime().getMillis()));
         logger.info("Updating existing location : " + existingLocation + "with status : " + locationStatus);
         allLocationDimensions.createOrUpdate(existingLocation);
     }
 
     private LocationDimension createNewLocation(LocationSyncRequest locationSyncRequest, LocationDimension existingLocation) {
         LocationRequest newLocationRequest = locationSyncRequest.getNewLocation();
         LocationDimension newLocation = allLocationDimensions.fetchFor(newLocationRequest.getState(), newLocationRequest.getDistrict(), newLocationRequest.getBlock(), newLocationRequest.getPanchayat());
         if (newLocation == null) {
             newLocation = new LocationDimension(newLocationRequest.getState(), newLocationRequest.getDistrict(), newLocationRequest.getBlock(), newLocationRequest.getPanchayat(), LocationStatus.VALID.name());
             allLocationDimensions.createOrUpdate(newLocation);
             logger.info("Added new location : " + newLocation);
         }
         logger.info("Remapping subscribers from " + existingLocation + " to new location : " + newLocation);
         remapSubscribers(existingLocation, newLocation);
         return newLocation;
     }
 
     private void remapSubscribers(LocationDimension oldLocation, LocationDimension newLocation) {
         if (oldLocationIsNotPresent(oldLocation)) return;
         List<Subscriber> subscriberList = allSubscribers.findByLocation(oldLocation);
         for (Subscriber subscriber : subscriberList) {
             subscriber.setLocationDimension(newLocation);
         }
         allSubscribers.saveOrUpdateAll(subscriberList);
     }
 
     private boolean oldLocationIsNotPresent(LocationDimension oldLocation) {
         return oldLocation == null;
     }
 }
