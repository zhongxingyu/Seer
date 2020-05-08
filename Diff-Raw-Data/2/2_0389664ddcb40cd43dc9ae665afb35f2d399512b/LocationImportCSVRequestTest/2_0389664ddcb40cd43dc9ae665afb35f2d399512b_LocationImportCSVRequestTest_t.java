 package org.motechproject.ananya.referencedata.csv.request;
 
 import org.junit.Test;
 import org.motechproject.ananya.referencedata.csv.response.LocationValidationResponse;
 import org.motechproject.ananya.referencedata.csv.utils.LocationImportCSVRequestBuilder;
 import org.motechproject.ananya.referencedata.flw.domain.LocationStatus;
 
 import static org.junit.Assert.*;
 
 public class LocationImportCSVRequestTest {
 
     @Test
     public void shouldValidateExistenceOfAlternateLocation() {
         LocationImportCSVRequest CSVRequest = locationImportCSVRequest("d", "b", "p", LocationStatus.INVALID.getDescription());
         assertFalse(CSVRequest.hasAlternateLocation());
 
         CSVRequest = locationImportCSVRequest("d", "b", "p", LocationStatus.INVALID.name(), "d1", "b1", "p1");
         assertTrue(CSVRequest.hasAlternateLocation());
     }
 
     @Test
     public void shouldVerifyIfLocationMatchesWithRequestLocation() {
         String district = "d";
         String block = "b";
         String panchayat = "p";
 
         LocationImportCSVRequest CSVRequest = locationImportCSVRequest(district, block, panchayat, LocationStatus.INVALID.getDescription());
 
         assertTrue(CSVRequest.matchesLocation(district, block, panchayat));
         assertFalse(CSVRequest.matchesLocation("d1", "b1", "p1"));
     }
 
     @Test
     public void shouldChangeToUpperCase() {
         LocationImportCSVRequest CSVRequest1 = locationImportCSVRequest("district", "block", "panchayat", "status1", "newdistrict1", "newblock1", "newpanchayat1");
         LocationImportCSVRequest CSVRequest2 = locationImportCSVRequest("District", "BlocK", "PanchaYat", "status2", "newdistrict2", "newblock2", "newpanchayat2");
 
         assertTrue(CSVRequest1.equals(CSVRequest2));
     }
 
     @Test
     public void shouldValidateACorrectRequest() {
         LocationImportCSVRequest locationImportCSVRequest = locationImportCSVRequest("d", "b", "p", "valid");
         LocationValidationResponse locationValidationResponse = new LocationValidationResponse();
 
         locationImportCSVRequest.validate(locationValidationResponse);
 
         assertTrue(locationValidationResponse.isValid());
         assertTrue(locationValidationResponse.getMessage().isEmpty());
     }
 
     @Test
     public void shouldInvalidateAnIncorrectRequest() {
         LocationImportCSVRequest locationImportCSVRequest = new LocationImportCSVRequest();
         LocationValidationResponse locationValidationResponse = new LocationValidationResponse();
 
         locationImportCSVRequest.validate(locationValidationResponse);
 
         assertTrue(locationValidationResponse.isInValid());
         assertTrue(locationValidationResponse.getMessage().contains("Blank district, block or panchayat"));
         assertTrue(locationValidationResponse.getMessage().contains("Blank or Invalid status"));
     }
 
     @Test
     public void shouldReturnStatusEnum() {
         assertEquals(LocationStatus.VALID, locationImportCSVRequest(null, null, null, "valid").getStatusEnum());
         assertEquals(LocationStatus.IN_REVIEW, locationImportCSVRequest(null, null, null, " in revIEW   ").getStatusEnum());
         assertNull(locationImportCSVRequest(null, null, null, "invalid status").getStatusEnum());
         assertNull(locationImportCSVRequest(null, null, null, null).getStatusEnum());
     }
 
     @Test
     public void shouldReturnHeaderRowWithErrorsColumn() {
         String headerRowForErrors = new LocationImportCSVRequest().getHeaderRowForErrors();
 
        assertEquals("district,block,panchayat,status,newDistrict,newBlock,newPanchayat,error", headerRowForErrors);
     }
 
     private LocationImportCSVRequest locationImportCSVRequest(String district, String block, String panchayat, String status, String newDistrict, String newBlock, String newPanchayat) {
         return new LocationImportCSVRequestBuilder().withDefaults().buildWith(district, block, panchayat, status, newDistrict, newBlock, newPanchayat);
     }
 
     private LocationImportCSVRequest locationImportCSVRequest(String district, String block, String panchayat, String status) {
         return locationImportCSVRequest(district, block, panchayat, status, null, null, null);
     }
 }
