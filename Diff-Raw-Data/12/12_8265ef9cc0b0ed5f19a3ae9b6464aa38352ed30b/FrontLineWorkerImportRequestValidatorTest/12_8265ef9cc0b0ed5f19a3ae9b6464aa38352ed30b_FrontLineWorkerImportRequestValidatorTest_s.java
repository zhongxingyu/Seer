 package org.motechproject.ananya.referencedata.csv.validator;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.motechproject.ananya.referencedata.csv.request.FrontLineWorkerImportRequest;
 import org.motechproject.ananya.referencedata.csv.response.FrontLineWorkerImportValidationResponse;
 import org.motechproject.ananya.referencedata.flw.domain.Designation;
 import org.motechproject.ananya.referencedata.flw.domain.FrontLineWorker;
 import org.motechproject.ananya.referencedata.flw.domain.VerificationStatus;
 import org.motechproject.ananya.referencedata.flw.repository.AllFrontLineWorkers;
 import org.motechproject.ananya.referencedata.flw.request.LocationRequest;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import static java.util.UUID.randomUUID;
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.motechproject.ananya.referencedata.flw.utils.PhoneNumber.formatPhoneNumber;
 
 public class FrontLineWorkerImportRequestValidatorTest {
 
     private FrontLineWorkerImportRequestValidator validator;
 
     private AllFrontLineWorkers allFrontLineWorkers;
     private FrontLineWorkerImportValidationResponse response;
 
     @Before
     public void setUp() throws Exception {
         allFrontLineWorkers = mock(AllFrontLineWorkers.class);
         validator = new FrontLineWorkerImportRequestValidator(allFrontLineWorkers);
         when(allFrontLineWorkers.getByMsisdnWithStatus(anyLong())).thenReturn(new ArrayList<FrontLineWorker>());
         when(allFrontLineWorkers.getByFlwId(Matchers.<UUID>any())).thenReturn(null);
     }
 
     @Test
     public void shouldValidateMSISDN() {
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateMsisdn(new FrontLineWorkerImportRequest(randomUUID().toString(), "msisdn", "1234567891", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid msisdn]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateMsisdn(new FrontLineWorkerImportRequest(randomUUID().toString(), "12345", "1234567891", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid msisdn]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateMsisdn(new FrontLineWorkerImportRequest(randomUUID().toString(), "", "1234567891", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid msisdn]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateMsisdn(new FrontLineWorkerImportRequest(randomUUID().toString(), "123456789012", "1234567891", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid msisdn]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateMsisdn(new FrontLineWorkerImportRequest(randomUUID().toString(), "911234567890", "1234567891", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateMsisdn(new FrontLineWorkerImportRequest(randomUUID().toString(), "001234567890", "1234567891", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateMsisdn(new FrontLineWorkerImportRequest(randomUUID().toString(), "1256789031", "1234567891", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
     }
 
     @Test
     public void shouldValidateName() {
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateName(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567890", "1234567891", "Mr. Valid", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateName(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567890", "1234567891", "Valid 1234", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateName(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567890", "1234567891", "Invalid-Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid name]", response.getMessage().toString());
     }
 
     @Test
     public void shouldValidateLocation() {
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateLocation(null, response);
 
         assertFalse(response.isValid());
         assertEquals("[Invalid location]", response.getMessage().toString());
     }
 
     @Test
     public void shouldValidateId() {
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateId(new FrontLineWorkerImportRequest(null, "1234567890", "1234567891", "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateId(new FrontLineWorkerImportRequest("", "1234567890", "1234567891", "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateId(new FrontLineWorkerImportRequest("11111111-1111-1111-1111-111111111111", "1234567890", "1234567891", "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         UUID uuid = randomUUID();
         when(allFrontLineWorkers.getByFlwId(uuid)).thenReturn(new FrontLineWorker(formatPhoneNumber("1234567890"), "Valid. Name", Designation.ANM, null, null));
         validator.validateId(new FrontLineWorkerImportRequest(uuid.toString(), "1234567890", "1234567891", "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateId(new FrontLineWorkerImportRequest("11111111-1111-1111-1111-1111", "1234567890", "1234567891", "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid id]", response.getMessage().toString());
     }
 
     @Test
     public void shouldValidateIdWhenMsisdnAreNotMatching() {
         response = new FrontLineWorkerImportValidationResponse();
         UUID uuid = randomUUID();
         when(allFrontLineWorkers.getByFlwId(uuid)).thenReturn(new FrontLineWorker(formatPhoneNumber("1234567891"), "Valid. Name", Designation.ANM, null, null));
         validator.validateId(new FrontLineWorkerImportRequest(uuid.toString(), "1234567890", null, "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Msisdn is not matching with the record in DB for the given FLW ID]", response.getMessage().toString());
     }
 
     @Test
     public void shouldValidateIdWhenFLWIsNotPresentInDB() {
         response = new FrontLineWorkerImportValidationResponse();
         UUID uuid = randomUUID();
         when(allFrontLineWorkers.getByFlwId(uuid)).thenReturn(null);
         validator.validateId(new FrontLineWorkerImportRequest(uuid.toString(), "1234567890", null, "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[FLW with given id not found in DB]", response.getMessage().toString());
     }
 
     @Test
     public void shouldValidateIdWhenDuplicateFLWIsPresentInDB() {
         response = new FrontLineWorkerImportValidationResponse();
         UUID uuid = randomUUID();
         String msisdn = "1234567890";
         String name = "name";
         Long number = formatPhoneNumber(msisdn);
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         FrontLineWorker matchingFLWWithBlankVerificationStatus = new FrontLineWorker(number, name, Designation.ANM, null, null);
         FrontLineWorker matchingFLWWithNonBlankVerificationStatus = new FrontLineWorker(number, name, Designation.ANM, null, "SUCCESS");
         frontLineWorkers.add(matchingFLWWithNonBlankVerificationStatus);
         when(allFrontLineWorkers.getByFlwId(uuid)).thenReturn(matchingFLWWithBlankVerificationStatus);
         when(allFrontLineWorkers.getByMsisdnWithStatus(formatPhoneNumber("1234567890"))).thenReturn(frontLineWorkers);
 
         validator.validateId(new FrontLineWorkerImportRequest(uuid.toString(), "1234567890", null, "Valid. Name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
 
         assertFalse(response.isValid());
         assertEquals("[Flw with same Msisdn having non blank verification status already present]", response.getMessage().toString());
     }
 
     @Test
     public void shouldValidateAlternateContactNumber() {
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", "msisdn", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid alternate contact number]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", "12345", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid alternate contact number]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", "123456789012", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid alternate contact number]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", "911234567890", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", "001234567890", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", "1256789031", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", "", "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateAlternateContactNumber(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", null, "name", "ANM", VerificationStatus.SUCCESS.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
     }
 
     @Test
     public void shouldValidateVerificationStatus() {
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", null, "name", "ANM", "Foo", new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid verification status]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(randomUUID().toString(), "1asdf67890", null, "name", "ANM", null, new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Invalid msisdn]", response.getMessage().toString());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", null, "name", "ANM", VerificationStatus.INVALID.name(), new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", null, "name", "ANM", "", new LocationRequest()), response);
         assertTrue(response.isValid());
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(randomUUID().toString(), "1234567891", null, "name", "ANM", null, new LocationRequest()), response);
         assertTrue(response.isValid());
     }
 
     @Test
     public void canUpdateAnUnverifiedFlwByIdInThePresenceOfVerifiedFLW() {
         String msisdn = "1234567891";
         String name = "name";
         Long number = formatPhoneNumber(msisdn);
 
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
        FrontLineWorker worker = new FrontLineWorker(number, name, Designation.ANM, null, "SUCCESS", UUID.randomUUID(),"");
         frontLineWorkers.add(worker);
         when(allFrontLineWorkers.getByMsisdnWithStatus(number)).thenReturn(frontLineWorkers);
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(randomUUID().toString(), msisdn, null, name, "ANM", null, new LocationRequest()), response);
         assertTrue(response.isValid());
     }
 
     @Test
     public void shouldAddErrorIfRequestHasBlankVerificationStatusAndDefaultIdAndVerifiedFlwIsInDB() {
         String msisdn = "1234567891";
         String name = "name";
         Long number = formatPhoneNumber(msisdn);
 
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         FrontLineWorker worker = new FrontLineWorker(number, name, Designation.ANM, null, "SUCCESS");
         frontLineWorkers.add(worker);
         when(allFrontLineWorkers.getByMsisdnWithStatus(number)).thenReturn(frontLineWorkers);
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(FrontLineWorker.DEFAULT_UUID_STRING, msisdn, null, name, "ANM", null, new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Cannot update non blank verification status to blank]", response.getMessage().toString());
     }
 
     @Test
     public void shouldAddErrorIfRequestHasBlankVerificationStatusAndIdAndVerifiedFlwIsInDB() {
         String msisdn = "1234567891";
         String name = "name";
         Long number = formatPhoneNumber(msisdn);
 
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         FrontLineWorker worker = new FrontLineWorker(number, name, Designation.ANM, null, "SUCCESS");
         frontLineWorkers.add(worker);
         when(allFrontLineWorkers.getByMsisdnWithStatus(number)).thenReturn(frontLineWorkers);
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest("", msisdn, null, name, "ANM", null, new LocationRequest()), response);
         assertFalse(response.isValid());
         assertEquals("[Cannot update non blank verification status to blank]", response.getMessage().toString());
     }
 
     @Test
     public void shouldNotUpdateAVerifiedFlwToUnVerifiedWhenUpdatedByFlwId() {
         String msisdn = "1234567891";
         String name = "name";
         Long number = formatPhoneNumber(msisdn);
 
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         UUID flwId = randomUUID();
        FrontLineWorker worker = new FrontLineWorker(number, name, Designation.ANM, null, "SUCCESS", flwId, "");
         frontLineWorkers.add(worker);
         when(allFrontLineWorkers.getByMsisdnWithStatus(number)).thenReturn(frontLineWorkers);
 
         response = new FrontLineWorkerImportValidationResponse();
         validator.validateVerificationStatus(new FrontLineWorkerImportRequest(flwId.toString(), msisdn, null, name, "ANM", "", new LocationRequest()), response);
         verify(allFrontLineWorkers).getByMsisdnWithStatus(number);
         assertFalse(response.isValid());
         assertEquals("[Cannot update non blank verification status to blank]", response.getMessage().toString());
     }
 }
