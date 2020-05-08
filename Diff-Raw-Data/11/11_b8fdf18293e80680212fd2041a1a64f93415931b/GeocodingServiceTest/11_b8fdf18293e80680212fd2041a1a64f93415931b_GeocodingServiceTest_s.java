 package org.klab.geocoding.service;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.klab.geocoding.model.Coordinates;
 import org.klab.geocoding.service.impl.GoogleGeocodingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
 
 public class GeocodingServiceTest {
     
    @Autowired
    private GeocodingService geocodingService;
    
     @Test
     public void testGeocodeOK() throws GeocodingServiceException {
         GeocodingService geocodingService = new GoogleGeocodingServiceImpl();
         
         String address = "12 rue Tiquetonne, Paris, France";
         Coordinates coordinates = geocodingService.geocode(address);
         
         Assert.assertNotNull(coordinates);
         Assert.assertTrue(coordinates.getLatitude() != 0);
         Assert.assertTrue(coordinates.getLongitude() != 0);
     }
     
     @Test
     public void testGeocodeKO() throws GeocodingServiceException {
         try {
             geocodingService.geocode("rue du aaa, aaa");
             Assert.fail();
         } catch (GeocodingServiceException e) {
         }
     }
 }
