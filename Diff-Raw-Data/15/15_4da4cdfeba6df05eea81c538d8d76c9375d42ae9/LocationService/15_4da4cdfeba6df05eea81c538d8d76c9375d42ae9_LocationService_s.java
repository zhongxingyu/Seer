 package org.motechproject.ananya.referencedata.service;
 
 import org.motechproject.ananya.referencedata.domain.Location;
 import org.motechproject.ananya.referencedata.domain.LocationList;
 import org.motechproject.ananya.referencedata.mapper.LocationMapper;
 import org.motechproject.ananya.referencedata.repository.AllLocations;
 import org.motechproject.ananya.referencedata.request.LocationRequest;
 import org.motechproject.ananya.referencedata.response.FLWValidationResponse;
 import org.motechproject.ananya.referencedata.response.LocationCreationResponse;
 import org.motechproject.ananya.referencedata.validators.LocationValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Service
 public class LocationService {
     private AllLocations allLocations;
 
     public LocationService() {
     }
 
     @Autowired
     public LocationService(AllLocations allLocations) {
         this.allLocations = allLocations;
     }
 
     public List<Location> getAll() {
         return allLocations.getAll();
     }
 
     @Transactional
     public LocationCreationResponse add(LocationRequest locationRequest) {
         LocationCreationResponse response = new LocationCreationResponse();
 
         Location location = LocationMapper.mapFrom(locationRequest);
         LocationList locationList = new LocationList(this.allLocations.getAll());
 
         FLWValidationResponse FLWValidationResponse = new LocationValidator(locationList).validate(location);
         if(FLWValidationResponse.isValid()) {
             location = locationList.updateLocationCode(location);
             allLocations.add(location);
             return response.withCreated();
         }
 
         return response.withValidationResponse(FLWValidationResponse);
     }
 
     @Transactional
     public void addAllWithoutValidations(List<LocationRequest> locationRequests) {
         LocationList locationList = new LocationList(allLocations.getAll());
         List<Location> locations = new ArrayList<Location>();
         for(LocationRequest request : locationRequests) {
            Location location = LocationMapper.mapFrom(request);
            locationList.updateLocationCode(location);
            locationList.add(location);
            locations.add(location);
         }
         allLocations.addAll(locations);
     }
 }
