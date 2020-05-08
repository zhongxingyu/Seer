 package org.motechproject.ananya.referencedata.csv.importer;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.ananya.referencedata.csv.request.FrontLineWorkerImportRequest;
 import org.motechproject.ananya.referencedata.csv.service.FrontLineWorkerImportService;
 import org.motechproject.ananya.referencedata.csv.service.LocationImportService;
 import org.motechproject.ananya.referencedata.csv.validator.FrontLineWorkerImportRequestValidator;
 import org.motechproject.ananya.referencedata.flw.domain.*;
 import org.motechproject.ananya.referencedata.flw.repository.AllFrontLineWorkers;
 import org.motechproject.ananya.referencedata.flw.request.LocationRequest;
 import org.motechproject.importer.domain.ValidationResponse;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.UUID;
 
 import static java.lang.Long.parseLong;
 import static java.util.Arrays.asList;
 import static java.util.Collections.EMPTY_LIST;
 import static junit.framework.Assert.*;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.motechproject.ananya.referencedata.flw.utils.PhoneNumber.formatPhoneNumber;
 
 @RunWith(MockitoJUnitRunner.class)
 public class FrontLineWorkerImporterTest {
     @Mock
     private LocationImportService locationImportService;
     @Mock
     private FrontLineWorkerImportService frontLineWorkerImportService;
     @Mock
     private Properties clientServicesProperties;
     @Mock
     private AllFrontLineWorkers allFrontLineWorkers;
     private FrontLineWorkerImporter frontLineWorkerImporter;
     private Location location;
     private LocationRequest locationRequest = new LocationRequest("D1", "B1", "P1", "state");
     private String msisdn = "1234567890";
     private UUID flwId = UUID.randomUUID();
 
     @Before
     public void setUp() {
         location = new Location("D1", "B1", "P1", "state", LocationStatus.VALID, null);
         when(locationImportService.getFor("state", "D1", "B1", "P1")).thenReturn(location);
         FrontLineWorkerImportRequestValidator frontLineWorkerValidator = new FrontLineWorkerImportRequestValidator(allFrontLineWorkers);
         frontLineWorkerImporter = new FrontLineWorkerImporter(frontLineWorkerImportService, locationImportService, frontLineWorkerValidator);
     }
 
     @Test
     public void shouldValidateFLWRequests() {
         ArrayList<FrontLineWorkerImportRequest> frontLineWorkerWebRequests = new ArrayList<>();
         when(allFrontLineWorkers.getByFlwId(flwId)).thenReturn(new FrontLineWorker(formatPhoneNumber("1234567890"), "name", Designation.ANM, null, null));
         frontLineWorkerWebRequests.add(new FrontLineWorkerImportRequest(flwId.toString(), "1234567890", "1234567891", "name", Designation.ANM.name(), VerificationStatus.SUCCESS.name(), new LocationRequest("D1", "B1", "P1", "state", "VALID")));
 
         ValidationResponse validationResponse = frontLineWorkerImporter.validate(frontLineWorkerWebRequests);
 
         assertTrue(validationResponse.isValid());
         assertEquals(2, validationResponse.getErrors().size());
         assertEquals("id,msisdn,alternate_contact_number,name,designation,verification_status,state,district,block,panchayat,error", validationResponse.getErrors().get(0).getMessage());
         verify(locationImportService).invalidateCache();
     }
 
     @Test
     public void shouldFailValidationIfFLWDoesNotHaveAllTheDetails() {
         ArrayList<FrontLineWorkerImportRequest> frontLineWorkerWebRequests = new ArrayList<>();
         when(allFrontLineWorkers.getByFlwId(flwId)).thenReturn(new FrontLineWorker(formatPhoneNumber("1234567890"), "name", Designation.ANM, null, null));
 
         String fwlId = flwId.toString();
         frontLineWorkerWebRequests.add(new FrontLineWorkerImportRequest(fwlId, "1asdf67890", "1234567891", "name", Designation.ANM.name(), VerificationStatus.SUCCESS.name(), new LocationRequest("D1", "B1", "P1", "state", "VALID")));
 
         ValidationResponse validationResponse = frontLineWorkerImporter.validate(frontLineWorkerWebRequests);
 
         assertFalse(validationResponse.isValid());
         assertEquals(2, validationResponse.getErrors().size());
         String fwlIdInQuotes = String.format("\"%s\",", flwId);
         assertEquals(fwlIdInQuotes + "\"1asdf67890\",\"name\",\"ANM\",\"SUCCESS\",\"state\",\"D1\",\"B1\",\"P1\",\"[Invalid msisdn]\"", validationResponse.getErrors().get(1).getMessage());
         verify(locationImportService).invalidateCache();
     }
 
     @Test
     public void shouldSaveFLW() throws IOException {
         ArrayList<FrontLineWorkerImportRequest> frontLineWorkerWebRequests = new ArrayList<>();
         frontLineWorkerWebRequests.add(new FrontLineWorkerImportRequest(UUID.randomUUID().toString(), msisdn, "1234567891", "name", Designation.ANM.name(), VerificationStatus.SUCCESS.name(), new LocationRequest("D1", "B1", "P1", "state", "VALID")));
 
         frontLineWorkerImporter.postData(frontLineWorkerWebRequests);
 
         verify(frontLineWorkerImportService).addAllWithoutValidations(frontLineWorkerWebRequests);
     }
 
     @Test
     public void nonBlankVerificationStatusInDBCannotBeBlanked() {
        FrontLineWorker flwInDB = new FrontLineWorker(parseLong(msisdn), null, null, location, VerificationStatus.SUCCESS.name(), UUID.randomUUID(), "");
         when(allFrontLineWorkers.getByMsisdnWithStatus(formatPhoneNumber(msisdn))).thenReturn(asList(flwInDB));
         FrontLineWorkerImportRequest frontLineWorkerImportRequest = new FrontLineWorkerImportRequest("", msisdn, null, null, null, null, locationRequest);
 
         ValidationResponse result = frontLineWorkerImporter.validate(asList(frontLineWorkerImportRequest));
 
         verify(allFrontLineWorkers).getByMsisdnWithStatus(formatPhoneNumber(msisdn));
         assertFalse(result.isValid());
         assertTrue(result.getErrors().get(1).getMessage().contains("[Cannot update non blank verification status to blank]"));
     }
 
     @Test
     public void multipleFlwInDBWithBlankVerificationIsValid() {
         when(allFrontLineWorkers.getByMsisdnWithStatus(formatPhoneNumber(msisdn))).thenReturn(EMPTY_LIST);
         FrontLineWorkerImportRequest frontLineWorkerImportRequest = new FrontLineWorkerImportRequest("", msisdn, null, null, null, null, locationRequest);
 
         ValidationResponse result = frontLineWorkerImporter.validate(asList(frontLineWorkerImportRequest));
 
         verify(allFrontLineWorkers).getByMsisdnWithStatus(formatPhoneNumber(msisdn));
         assertTrue(result.isValid());
         assertTrue(result.getErrors().get(1).getMessage().contains("[]"));
     }
 
     @Test
     public void shouldHaveFlwWithGivenIdInDB() {
         when(allFrontLineWorkers.getByFlwId(flwId)).thenReturn(null);
         FrontLineWorkerImportRequest frontLineWorkerImportRequest = new FrontLineWorkerImportRequest(flwId.toString(), msisdn, null, null, null, VerificationStatus.SUCCESS.name(), locationRequest);
 
         ValidationResponse result = frontLineWorkerImporter.validate(asList(frontLineWorkerImportRequest));
 
         verify(allFrontLineWorkers).getByFlwId(flwId);
         assertFalse(result.isValid());
         assertTrue(result.getErrors().get(1).getMessage().contains("[FLW with given id not found in DB]"));
     }
 
     @Test
     public void shouldFailIfUnverifiedFlwIsPresentInDBWithDuplicateVerifiedFlw() {
         when(allFrontLineWorkers.getByFlwId(flwId)).thenReturn(new FrontLineWorker(formatPhoneNumber(msisdn), null, null, null, null, null, flwId, null));
         FrontLineWorker flwInDB = new FrontLineWorker(parseLong(msisdn), null, null, location, VerificationStatus.SUCCESS.name());
         when(allFrontLineWorkers.getByMsisdnWithStatus(formatPhoneNumber(msisdn))).thenReturn(Arrays.asList(flwInDB));
         FrontLineWorkerImportRequest frontLineWorkerImportRequest = new FrontLineWorkerImportRequest(flwId.toString(), msisdn, null, null, null, VerificationStatus.SUCCESS.name(), locationRequest);
 
         ValidationResponse result = frontLineWorkerImporter.validate(asList(frontLineWorkerImportRequest));
 
         verify(allFrontLineWorkers).getByFlwId(flwId);
         verify(allFrontLineWorkers).getByMsisdnWithStatus(formatPhoneNumber(msisdn));
         assertFalse(result.isValid());
         assertTrue(result.getErrors().get(1).getMessage().contains("[Flw with same Msisdn having non blank verification status already present]"));
     }
 
     @Test
     public void shouldFailWhenGUIDIsPresentAndVerificationStatusIsBlank() {
         FrontLineWorkerImportRequest frontLineWorkerImportRequest = new FrontLineWorkerImportRequest(flwId.toString(), msisdn, null, null, null, null, locationRequest);
 
         ValidationResponse result = frontLineWorkerImporter.validate(asList(frontLineWorkerImportRequest));
 
         assertFalse(result.isValid());
         assertTrue(result.getErrors().get(1).getMessage().contains("[Verification Status cannot be blank when FLW ID is given]"));
     }
 
     @Test
     public void csvShouldNotHaveDuplicateFlw() {
         FrontLineWorkerImportRequest request1 = new FrontLineWorkerImportRequest(null, msisdn, null, null, null, null, locationRequest);
         FrontLineWorkerImportRequest request2 = new FrontLineWorkerImportRequest("", msisdn, null, null, null, VerificationStatus.SUCCESS.name(), locationRequest);
 
         ValidationResponse result = frontLineWorkerImporter.validate(asList(request1, request2));
 
         assertTrue(result.getErrors().get(1).getMessage().contains("[Duplicate with verification status found in CSV]"));
     }
 
     @Test
     public void csvCanHaveMultipleFlwRecords() {
         FrontLineWorkerImportRequest request1 = new FrontLineWorkerImportRequest(null, msisdn, null, null, null, null, locationRequest);
         FrontLineWorkerImportRequest request2 = new FrontLineWorkerImportRequest("", "1234567891", null, null, null, VerificationStatus.SUCCESS.name(), locationRequest);
 
         ValidationResponse result = frontLineWorkerImporter.validate(asList(request1, request2));
 
         assertFalse(result.getErrors().get(1).getMessage().contains("[Duplicate with verification status found in CSV]"));
     }
 
 }
