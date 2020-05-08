 package org.motechproject.ananya.referencedata.csv.validator;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.ananya.referencedata.csv.request.LocationImportRequest;
 import org.motechproject.ananya.referencedata.csv.response.LocationValidationResponse;
 import org.motechproject.ananya.referencedata.flw.domain.Location;
 import org.motechproject.ananya.referencedata.flw.domain.LocationStatus;
 import org.motechproject.ananya.referencedata.flw.repository.AllLocations;
 
 import java.util.ArrayList;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class LocationImportValidatorTest {
     @Mock
     AllLocations allLocations;
     private LocationImportValidator locationImportValidator;
 
     @Before
     public void setUp() {
         locationImportValidator = new LocationImportValidator(allLocations);
     }
 
     @Test
     public void shouldFailValidationIfLocationFieldsAreBlank() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         LocationImportRequest locationRequest = new LocationImportRequest("", "B1", "P1", "");
 
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequests);
 
         assertFalse(locationValidationResponse.isValid());
         assertTrue(locationValidationResponse.getMessage().contains("Blank district, block or panchayat"));
         assertTrue(locationValidationResponse.getMessage().contains("Blank or Invalid status"));
     }
 
     @Test
     public void shouldFailValidationIfLocationStatusIsInvalid() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         LocationImportRequest locationRequest = new LocationImportRequest("D1", "B1", "P1", "RandomStatus");
 
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequests);
 
         assertFalse(locationValidationResponse.isValid());
         assertTrue(locationValidationResponse.getMessage().contains("Blank or Invalid status"));
     }
 
     @Test
     public void shouldFailValidationIfStatusIsNotVerified() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         LocationImportRequest locationRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.NOT_VERIFIED.toString());
 
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequests);
 
         assertFalse(locationValidationResponse.isValid());
         assertTrue(locationValidationResponse.getMessage().contains("Blank or Invalid status"));
     }
 
     @Test
     public void shouldPassValidationIfAllFieldsArePresent() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         when(allLocations.getFor("D1", "B1", "P1")).thenReturn(null);
 
         LocationImportRequest locationRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.NEW.toString());
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequests);
 
         assertTrue(locationValidationResponse.isValid());
         assertTrue(locationValidationResponse.getMessage().isEmpty());
     }
 
     @Test
     public void shouldFailValidationForABlankAlternateLocationForAnInvalidLocation() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         LocationImportRequest locationRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.INVALID.toString());
 
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequests);
 
         assertFalse(locationValidationResponse.isValid());
        assertTrue(locationValidationResponse.getMessage().contains("Location is invalid and does not have an alternate location"));
     }
 
     @Test
     public void shouldValidateAlternateLocationForLocationsWithInvalidStatusOnly() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         Location location = new Location("D1", "B1", "P1", LocationStatus.NOT_VERIFIED, null);
         when(allLocations.getFor("D1", "B1", "P1")).thenReturn(location);
         LocationImportRequest locationRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.VALID.toString());
 
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequests);
 
         assertTrue(locationValidationResponse.isValid());
         assertTrue(locationValidationResponse.getMessage().isEmpty());
     }
 
     @Test
     public void shouldValidateForValidAlternateLocationInDBForAnInvalidLocation() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         Location location = new Location("D1", "B1", "P1", LocationStatus.NOT_VERIFIED, null);
         Location alternateLocation = new Location("D2", "B2", "P2", LocationStatus.VALID, null);
         when(allLocations.getFor("D1", "B1", "P1")).thenReturn(location);
         when(allLocations.getFor("D2", "B2", "P2")).thenReturn(alternateLocation);
         LocationImportRequest locationRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.INVALID.toString(), "D2", "B2", "P2");
 
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequests);
 
         assertTrue(locationValidationResponse.isValid());
         assertTrue(locationValidationResponse.getMessage().isEmpty());
     }
 
     @Test
     public void shouldValidateForInvalidAlternateLocationInDBForAnInvalidLocation() {
         ArrayList<LocationImportRequest> locationImportRequestsFromCsv = new ArrayList<>();
         locationImportRequestsFromCsv.add(new LocationImportRequest("D1", "B1", "P1", LocationStatus.INVALID.toString(), "D2", "B2", "P2"));
         locationImportRequestsFromCsv.add(new LocationImportRequest("D3", "B3", "P3", LocationStatus.VALID.toString()));
         when(allLocations.getFor("D2", "B2", "P2")).thenReturn(new Location("D2", "B2", "P2", LocationStatus.NEW, null));
 
         LocationImportRequest locationRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.INVALID.toString(), "D2", "B2", "P2");
         LocationValidationResponse locationValidationResponse = locationImportValidator.validate(locationRequest, locationImportRequestsFromCsv);
 
         assertFalse(locationValidationResponse.isValid());
        assertTrue(locationValidationResponse.getMessage().contains("Location is invalid and has an invalid alternate location"));
     }
 
     @Test
     public void shouldPassValidationIfAlternateLocationIsPresentInCSVAsValidAndNotInDb() {
         String district = "D2";
         String block = "B2";
         String panchayat = "P2";
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         locationImportRequests.add(new LocationImportRequest("D1", "B1", "P1", LocationStatus.NEW.toString()));
         LocationImportRequest locationImportRequest = new LocationImportRequest(district, block, panchayat, LocationStatus.INVALID.toString(), "D1", "B1", "P1");
         locationImportRequests.add(locationImportRequest);
         when(allLocations.getFor("D1", "B1", "P1")).thenReturn(null);
         when(allLocations.getFor(district, block, panchayat)).thenReturn(new Location(district, block, panchayat, LocationStatus.NOT_VERIFIED, null));
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertTrue(response.isValid());
         assertTrue(response.getMessage().isEmpty());
     }
 
     @Test
     public void shouldPassValidationIfAlternateLocationIsPresentInCSVAsNewAndNotInDb() {
         String district = "D3";
         String block = "B3";
         String panchayat = "P3";
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         locationImportRequests.add(new LocationImportRequest("D4", "B4", "P4", LocationStatus.NEW.toString()));
         LocationImportRequest locationImportRequest = new LocationImportRequest(district, block, panchayat, LocationStatus.INVALID.toString(), "D4", "B4", "P4");
         locationImportRequests.add(locationImportRequest);
         when(allLocations.getFor("D4", "B4", "P4")).thenReturn(null);
         when(allLocations.getFor(district, block, panchayat)).thenReturn(new Location(district, block, panchayat, LocationStatus.NOT_VERIFIED, null));
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertTrue(response.isValid());
         assertTrue(response.getMessage().isEmpty());
     }
 
     @Test
     public void shouldValidateForDuplicateEntries() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         locationImportRequests.add(new LocationImportRequest("D1", "B1", "P1", LocationStatus.VALID.toString()));
         locationImportRequests.add(new LocationImportRequest("d1", "B1", "P1", LocationStatus.NEW.toString()));
         LocationImportRequest locationImportRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.INVALID.toString(), "D4", "B4", "P4");
         locationImportRequests.add(locationImportRequest);
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertFalse(response.isValid());
         assertTrue(response.getMessage().contains("Duplicate location in CSV file"));
     }
 
     @Test
     public void shouldFailIfALocationAlreadyExistsInDBForRequestWithNewStatus() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         String district = "D3";
         String block = "B3";
         String panchayat = "P3";
         LocationImportRequest locationImportRequest = new LocationImportRequest(district, block, panchayat, LocationStatus.NEW.toString());
         locationImportRequests.add(locationImportRequest);
         when(allLocations.getFor(district, block, panchayat)).thenReturn(new Location(district, block, panchayat, LocationStatus.VALID, null));
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertFalse(response.isValid());
         assertTrue(response.getMessage().contains("Location is already present in DB"));
     }
 
     @Test
     public void shouldFailIfALocationDoesExistInDBForRequestWithAnyStatusOtherThanNew() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         String district = "D3";
         String block = "B3";
         String panchayat = "P3";
         LocationImportRequest locationImportRequest = new LocationImportRequest(district, block, panchayat, LocationStatus.VALID.toString());
         locationImportRequests.add(locationImportRequest);
         when(allLocations.getFor(district, block, panchayat)).thenReturn(null);
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertFalse(response.isValid());
         assertTrue(response.getMessage().contains("Location is not present in DB"));
     }
     
     @Test
     public void shouldInvalidateLocationRequestIfStatusIsNotInvalidAndItHasAlternateLocation() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         LocationImportRequest locationImportRequest = new LocationImportRequest("D1", "B1", "P1", LocationStatus.VALID.toString(), "D2", "B2", "P2");
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertFalse(response.isValid());
         assertTrue(response.getMessage().contains("Alternate location provided when not required"));
     }
 
     @Test
     public void shouldUpdateDBForAStatusOtherThanNewOnlyIfTransitionIsApplicable() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         String district = "D1";
         String block = "B1";
         String panchayat = "P1";
         LocationImportRequest locationImportRequest = new LocationImportRequest(district, block, panchayat, LocationStatus.VALID.toString());
         when(allLocations.getFor(district,block,panchayat)).thenReturn(new Location(district,block,panchayat,LocationStatus.VALID, null));
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertTrue(response.isValid());
     }
 
     @Test
     public void shouldFailValidationForAStatusOtherThanNewOnlyIfTransitionIsNotApplicable() {
         ArrayList<LocationImportRequest> locationImportRequests = new ArrayList<>();
         String district = "D1";
         String block = "B1";
         String panchayat = "P1";
         LocationImportRequest locationImportRequest = new LocationImportRequest(district, block, panchayat, LocationStatus.IN_REVIEW.toString());
         when(allLocations.getFor(district,block,panchayat)).thenReturn(new Location(district,block,panchayat,LocationStatus.VALID, null));
 
         LocationValidationResponse response = locationImportValidator.validate(locationImportRequest, locationImportRequests);
 
         assertFalse(response.isValid());
         assertTrue(response.getMessage().contains("Location cannot be updated to the specified status"));
     }
 }
