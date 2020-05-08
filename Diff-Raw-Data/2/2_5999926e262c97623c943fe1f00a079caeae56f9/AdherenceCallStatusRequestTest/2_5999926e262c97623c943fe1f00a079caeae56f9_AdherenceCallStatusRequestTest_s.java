 package org.motechproject.whp.reports.contract;
 
 import org.hibernate.validator.HibernateValidator;
 import org.joda.time.format.DateTimeFormatter;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
 
 import javax.validation.ConstraintViolation;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.extract;
 import static ch.lambdaj.Lambda.on;
 import static javax.xml.bind.JAXBContext.newInstance;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNull;
 import static junit.framework.Assert.assertTrue;
 import static org.joda.time.format.DateTimeFormat.forPattern;
 
 public class AdherenceCallStatusRequestTest {
 
     private LocalValidatorFactoryBean localValidatorFactory;
     private Marshaller marshaller;
 
     @Before
     public void setup() throws JAXBException {
         localValidatorFactory = new LocalValidatorFactoryBean();
         localValidatorFactory.setProviderClass(HibernateValidator.class);
         localValidatorFactory.afterPropertiesSet();
         initializeJaxb();
     }
 
     private void initializeJaxb() throws JAXBException {
         JAXBContext context = newInstance(AdherenceCallStatusRequest.class);
         marshaller = context.createMarshaller();
     }
 
     @Test
     public void shouldBeInvalidWhenStartTimeHasInvalidFormat() {
         AdherenceCallStatusRequest request = validRequest();
         request.setStartTime("invalidTime");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("invalid date format"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("startTime"));
     }
 
     @Test
     public void shouldBeValidIfStartTimeIsNull() {
         AdherenceCallStatusRequest request = validRequest();
         request.setStartTime(null);
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldBeInvalidWhenEndTimeHasInvalidFormat() {
         AdherenceCallStatusRequest request = validRequest();
         request.setEndTime("invalidTime");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("invalid date format"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("endTime"));
     }
 
     @Test
     public void shouldBeValidWhenEndTimeIsNull() {
         AdherenceCallStatusRequest request = validRequest();
         request.setEndTime(null);
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldBeInvalidWhenAttemptTimeHasInvalidFormat() {
         AdherenceCallStatusRequest request = validRequest();
         request.setAttemptTime("invalidTime");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("invalid date format"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("attemptTime"));
     }
 
     @Test
     public void shouldBeValidWhenAttemptTimeIsNull() {
         AdherenceCallStatusRequest request = validRequest();
         request.setAttemptTime(null);
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldBeValidIfCallStatusIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setCallStatus("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldBeValidIfCallStatusIsNull() {
         AdherenceCallStatusRequest request = validRequest();
         request.setCallStatus(null);
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldBeInvalidIfProviderIdIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setProviderId("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("may not be empty"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("providerId"));
     }
 
     @Test
     public void shouldBeInvalidIfAdherenceCapturedCountIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setAdherenceCaptured("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("may not be empty"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("adherenceCaptured"));
     }
 
     @Test
     public void shouldBeValidIfDisconnectionTypeIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setDisconnectionType("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldBeValidIfDisconnectionTypeIsNull() {
         AdherenceCallStatusRequest request = validRequest();
         request.setDisconnectionType(null);
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldBeInvalidWhenFlashingCallIdIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setFlashingCallId("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("may not be empty"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("flashingCallId"));
     }
 
     @Test
     public void shouldBeInvalidWhenCallAnsweredIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setCallAnswered("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("may not be empty"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("callAnswered"));
     }
 
     @Test
     public void shouldBeInvalidWhenAdherenceNotCapturedCountIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setAdherenceNotCaptured("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("may not be empty"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("adherenceNotCaptured"));
     }
 
     @Test
     public void shouldBeInvalidIfPatientCountIsEmpty() {
         AdherenceCallStatusRequest request = validRequest();
         request.setTotalPatients("");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("may not be empty"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("totalPatients"));
     }
 
     @Test
     public void shouldBeInvalidIfPatientCountIsNotANumber() {
         AdherenceCallStatusRequest request = validRequest();
         request.setTotalPatients("a");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("should be a number"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("totalPatients"));
     }
 
     @Test
     public void shouldBeInvalidIfAdherenceCapturedCountIsNotANumber() {
         AdherenceCallStatusRequest request = validRequest();
         request.setAdherenceCaptured("a");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("should be a number"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("adherenceCaptured"));
     }
 
     @Test
     public void shouldBeInvalidIfAdherenceNotCapturedCountIsNotANumber() {
         AdherenceCallStatusRequest request = validRequest();
         request.setAdherenceNotCaptured("a");
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getMessage()).contains("should be a number"));
         assertTrue(extract(constraintViolations, on(ConstraintViolation.class).getPropertyPath().toString()).contains("adherenceNotCaptured"));
     }
 
     @Test
     public void shouldBeValidWhenAllTheFieldsAreValid() {
         AdherenceCallStatusRequest request = validRequest();
 
         List<ConstraintViolation<AdherenceCallStatusRequest>> constraintViolations = new ArrayList<>(localValidatorFactory.validate(request));
         assertTrue(constraintViolations.isEmpty());
     }
 
     @Test
     public void shouldConvertAdherenceCallStatusRequestToAdherenceCallLogRequest() {
         AdherenceCallStatusRequest adherenceCallStatusRequest = validRequest();
 
         AdherenceCallLogRequest adherenceCallLogRequest = adherenceCallStatusRequest.toCallLogRequest();
 
         assertEquals(adherenceCallStatusRequest.getAdherenceCaptured(),adherenceCallLogRequest.getAdherenceCaptured().toString());
         assertEquals(adherenceCallStatusRequest.getAdherenceNotCaptured(),adherenceCallLogRequest.getAdherenceNotCaptured().toString());
         assertEquals(adherenceCallStatusRequest.getTotalPatients(),adherenceCallLogRequest.getTotalPatients().toString());
         assertEquals(asDate(adherenceCallStatusRequest.getAttemptTime()),adherenceCallLogRequest.getAttemptTime());
         assertEquals(asDate(adherenceCallStatusRequest.getStartTime()),adherenceCallLogRequest.getStartTime());
         assertEquals(asDate(adherenceCallStatusRequest.getEndTime()),adherenceCallLogRequest.getEndTime());
         assertEquals(adherenceCallStatusRequest.getCallAnswered(),adherenceCallLogRequest.getCallAnswered());
         assertEquals(adherenceCallStatusRequest.getCallId(),adherenceCallLogRequest.getCallId());
         assertEquals(adherenceCallStatusRequest.getCallStatus(),adherenceCallLogRequest.getCallStatus());
         assertEquals(adherenceCallStatusRequest.getDisconnectionType(),adherenceCallLogRequest.getDisconnectionType());
         assertEquals(adherenceCallStatusRequest.getFlashingCallId(),adherenceCallLogRequest.getFlashingCallId());
         assertEquals(adherenceCallStatusRequest.getProviderId(),adherenceCallLogRequest.getProviderId());
     }
 
     @Test
     public void shouldConvertAdherenceCallStatusRequestToAdherenceCallLogRequestForBlankStartAndEndTime() {
         AdherenceCallStatusRequest adherenceCallStatusRequest = requestWithBlankStartAndEndTime();
 
         AdherenceCallLogRequest adherenceCallLogRequest = adherenceCallStatusRequest.toCallLogRequest();
 
         assertEquals(adherenceCallStatusRequest.getAdherenceCaptured(), adherenceCallLogRequest.getAdherenceCaptured().toString());
         assertEquals(adherenceCallStatusRequest.getAdherenceNotCaptured(),adherenceCallLogRequest.getAdherenceNotCaptured().toString());
         assertEquals(adherenceCallStatusRequest.getTotalPatients(),adherenceCallLogRequest.getTotalPatients().toString());
         assertEquals(asDate(adherenceCallStatusRequest.getAttemptTime()),adherenceCallLogRequest.getAttemptTime());
         assertNull(adherenceCallLogRequest.getStartTime());
         assertNull(adherenceCallLogRequest.getEndTime());
         assertEquals(adherenceCallStatusRequest.getCallAnswered(),adherenceCallLogRequest.getCallAnswered());
         assertEquals(adherenceCallStatusRequest.getCallId(),adherenceCallLogRequest.getCallId());
         assertEquals(adherenceCallStatusRequest.getCallStatus(),adherenceCallLogRequest.getCallStatus());
         assertEquals(adherenceCallStatusRequest.getDisconnectionType(),adherenceCallLogRequest.getDisconnectionType());
         assertEquals(adherenceCallStatusRequest.getFlashingCallId(),adherenceCallLogRequest.getFlashingCallId());
         assertEquals(adherenceCallStatusRequest.getProviderId(),adherenceCallLogRequest.getProviderId());
     }
 
     private AdherenceCallStatusRequest validRequest() {
         AdherenceCallStatusRequest request = new AdherenceCallStatusRequest();
         request.setCallId("callId");
         request.setStartTime("12/12/2011 11:11:11");
         request.setEndTime("12/12/2011 11:11:11");
         request.setAdherenceCaptured("1");
         request.setAttemptTime("12/12/2011 11:11:11");
         request.setCallAnswered("YES");
         request.setCallStatus("SUCCESS");
        request.setDisconnectionType("PROVIDER_HANGUP");
         request.setFlashingCallId("flashingCallId");
         request.setAdherenceNotCaptured("0");
         request.setTotalPatients("1");
         request.setProviderId("providerId");
         return request;
     }
 
     private AdherenceCallStatusRequest requestWithBlankStartAndEndTime() {
         AdherenceCallStatusRequest request = validRequest();
         request.setStartTime("");
         request.setEndTime("");
         return request;
     }
 
     private Date asDate(String startTime) {
         DateTimeFormatter formatter = forPattern("dd/MM/YYYY HH:mm:ss");
         return formatter.parseDateTime(startTime).toDate();
     }
 }
