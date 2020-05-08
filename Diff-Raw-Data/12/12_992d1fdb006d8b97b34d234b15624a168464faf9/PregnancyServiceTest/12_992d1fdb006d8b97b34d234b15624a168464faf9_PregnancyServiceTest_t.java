 package org.motechproject.ghana.national.service;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.configuration.ScheduleNames;
 import org.motechproject.ghana.national.domain.*;
 import org.motechproject.ghana.national.factory.PregnancyEncounterFactory;
 import org.motechproject.ghana.national.repository.*;
 import org.motechproject.ghana.national.service.request.DeliveredChildRequest;
 import org.motechproject.ghana.national.service.request.PregnancyDeliveryRequest;
 import org.motechproject.ghana.national.service.request.PregnancyTerminationRequest;
 import org.motechproject.ghana.national.vo.CwcVO;
 import org.motechproject.mrs.model.*;
 import org.motechproject.util.DateUtil;
 
 import java.util.*;
 
 import static junit.framework.Assert.assertFalse;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.FIRST_NAME;
 import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.LAST_NAME;
 import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.MOTECH_ID;
 import static org.motechproject.ghana.national.domain.Constants.OTHER_CAUSE_OF_DEATH;
 import static org.motechproject.ghana.national.domain.Constants.PREGNANCY_TERMINATION;
 import static org.motechproject.ghana.national.domain.SmsTemplateKeys.REGISTER_SUCCESS_SMS_KEY;
 import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
 
 public class PregnancyServiceTest {
     private PregnancyService pregnancyService;
     @Mock
     private AllPatients mockAllPatients;
     @Mock
     private AllEncounters mockAllEncounters;
     @Mock
     private AllSchedules mockAllSchedules;
     @Mock
     private AllAppointments mockAllAppointments;
     @Mock
     private IdentifierGenerator mockIdentifierGenerator;
     @Mock
     private SMSGateway mockSmsGateway;
 
     @Mock
     private  AllObservations mockAllObservations;
     @Mock
     private CareService mockCareService;
 
     @Mock
     private PatientService mockPatientService;
 
     @Mock
     private MobileMidwifeService mockMobileMidwifeService;
 
     @Before
     public void setUp() {
         initMocks(this);
         pregnancyService = new PregnancyService(mockAllPatients, mockAllEncounters, mockAllSchedules, mockAllAppointments,
                 mockIdentifierGenerator, mockAllObservations, mockCareService, mockSmsGateway,mockPatientService, mockMobileMidwifeService);
     }
 
     @Test
     public void shouldDeceasePatientIfDeadDuringPregnancyTermination() {
         String mrsFacilityId = "mrsFacilityId";
         String patientMRSId = "patientMRSId";
         Patient mockPatient = mock(Patient.class);
         MRSPatient mockMRSPatient = mock(MRSPatient.class);
         when(mockMRSPatient.getId()).thenReturn("mrsPatientId");
         Facility mockFacility = mock(Facility.class);
         MRSUser mockStaff = mock(MRSUser.class);
         List<String> schedules = Arrays.asList("schedule1", "schedule2");
         PregnancyTerminationRequest request = pregnancyTermination(mockPatient, mockStaff, mockFacility);
         request.setDead(Boolean.TRUE);
 
         when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
         when(mockPatient.getMRSPatientId()).thenReturn(patientMRSId);
         when(mockPatient.ancCareProgramsToUnEnroll()).thenReturn(schedules);
         when(mockFacility.getMrsFacilityId()).thenReturn(mrsFacilityId);
 
         pregnancyService.terminatePregnancy(request);
 
         verify(mockPatientService).deceasePatient(request.getTerminationDate(), request.getPatient().getMotechId(), OTHER_CAUSE_OF_DEATH, PREGNANCY_TERMINATION);
         verify(mockMobileMidwifeService).unRegister(request.getPatient().getMotechId());

     }
 
     @Test
     public void shouldCreateEncounterOnPregnancyTermination() {
         String patientMRSId = "patientMRSId";
         Patient mockPatient = mock(Patient.class);
         Facility mockFacility = mock(Facility.class);
         MRSUser mockStaff = mock(MRSUser.class);
         List<String> schedules = Arrays.asList("schedule1", "schedule2");
 
         when(mockPatient.getMRSPatientId()).thenReturn(patientMRSId);
         when(mockPatient.ancCareProgramsToUnEnroll()).thenReturn(schedules);
         final Date date = DateUtil.newDate(2000, 12, 12).toDate();
         PregnancyTerminationRequest request = pregnancyTermination(mockPatient, mockStaff, mockFacility);
         request.setTerminationDate(date);
         request.setDead(Boolean.FALSE);
 
         pregnancyService.terminatePregnancy(request);
 
         Encounter expectedEncounter = new PregnancyEncounterFactory().createTerminationEncounter(request, null);
         ArgumentCaptor<Encounter> argumentCaptor = ArgumentCaptor.forClass(Encounter.class);
 
         verify(mockAllSchedules).unEnroll(patientMRSId, mockPatient.ancCareProgramsToUnEnroll());
         verify(mockAllAppointments).remove(mockPatient);
         verify(mockAllEncounters).persistEncounter(argumentCaptor.capture());
        verify(mockMobileMidwifeService).unRegister(request.getPatient().getMotechId());
         assertReflectionEquals(expectedEncounter, argumentCaptor.getValue());
     }
 
     @Test
     public void shouldCreateEncounterForPregnancyDelivery_AndForBirth() {
         String facilityId = "12";
         MRSFacility mrsFacility = new MRSFacility(facilityId);
         Facility mockFacility = new Facility(mrsFacility).mrsFacilityId(facilityId);
         MRSUser mockStaff = new MRSUser().id("staff-id");
         String parentMotechId = "121";
         String mrsPatientId = "mrsPatientId";
         Patient mockPatient = mock(Patient.class);
         List<String> schedules = Arrays.asList("schedule1", "schedule2");
 
         final String childMotechId = "child-motech-id";
         final String childWeight = "1.1";
         final String childSex = "?";
         final String childFirstName = "Jo";
 
         DeliveredChildRequest deliveredChildRequest = new DeliveredChildRequest()
                 .childBirthOutcome(BirthOutcome.ALIVE)
                 .childRegistrationType(RegistrationType.USE_PREPRINTED_ID).childFirstName(childFirstName)
                 .childWeight(childWeight).childMotechId(childMotechId).childSex(childSex);
 
         DateTime deliveryDate = DateTime.now();
         String sender = "0987654321";
         PregnancyDeliveryRequest deliveryRequest = pregnancyDelivery(mockPatient, mockStaff, mockFacility, deliveredChildRequest, deliveryDate, sender);
 
         final Date birthDate = deliveryRequest.getDeliveryDateTime().toDate();
 
         final String childDefaultLastName = "Baby";
         final MRSPerson mrsPerson = new MRSPerson().firstName(childFirstName).lastName(childDefaultLastName).dateOfBirth(birthDate).gender("?").dead(false);
         MRSPatient childMRSPatient = new MRSPatient(childMotechId, mrsPerson, mrsFacility);
 
         final MRSObservation activePregnancyObservation = new MRSObservation(new Date(), "PREG", "Value");
 
         when(mockPatient.getMRSPatientId()).thenReturn(mrsPatientId);
         when(mockPatient.getMotechId()).thenReturn(parentMotechId);
         when(mockAllObservations.activePregnancyObservation(parentMotechId)).thenReturn(activePregnancyObservation);
         when(mockPatient.ancCareProgramsToUnEnroll()).thenReturn(schedules);
         Patient child = new Patient(childMRSPatient);
         when(mockAllPatients.save(Matchers.<Patient>any())).thenReturn(child);
         pregnancyService.handleDelivery(deliveryRequest);
 
         final PregnancyEncounterFactory factory = new PregnancyEncounterFactory();
         Encounter expectedDeliveryEncounter = factory.createDeliveryEncounter(deliveryRequest, activePregnancyObservation);
         Encounter expectedBirthEncounter = factory.createBirthEncounter(deliveredChildRequest, childMRSPatient, mockStaff, mockFacility, birthDate);
 
         ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
         ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
         ArgumentCaptor<CwcVO> cwcVOArgumentCaptor = ArgumentCaptor.forClass(CwcVO.class);
         verify(mockAllEncounters,times(2)).persistEncounter(encounterArgumentCaptor.capture());
         verify(mockAllPatients).save(patientArgumentCaptor.capture());
         verify(mockCareService).enroll(cwcVOArgumentCaptor.capture());
         verify(mockCareService).enrollChildForPNCOnDelivery(child);
         verify(mockAllSchedules).unEnroll(mrsPatientId,schedules);
 
         List<Encounter> encounters = encounterArgumentCaptor.getAllValues();
         assertThat(encounters.size(), is(equalTo(2)));
         assertReflectionEquals(expectedBirthEncounter, encounters.get(0));
         assertReflectionEquals(expectedDeliveryEncounter, encounters.get(1));
 
         Patient actualChild = patientArgumentCaptor.getValue();
         assertThat(actualChild.getMotechId(), is(deliveryRequest.getDeliveredChildRequests().get(0).getChildMotechId()));
         assertThat(actualChild.getParentId(), is(parentMotechId));
         assertThat(actualChild.getFirstName(), is(childFirstName));
         assertThat(actualChild.getLastName(), is(childDefaultLastName));
         assertFalse(actualChild.getMrsPatient().getPerson().getBirthDateEstimated());
         assertThat(actualChild.getMrsPatient().getFacility(), is(mrsFacility));
 
         CwcVO actualCWCVO = cwcVOArgumentCaptor.getValue();
         assertThat(actualCWCVO.getSerialNumber(), is(childMotechId));
         assertThat(actualCWCVO.getRegistrationDate(), is(birthDate));
         assertThat(actualCWCVO.getPatientMotechId(), is(childMotechId));
         assertThat(actualCWCVO.getFacilityId(), is(facilityId));
 
         ArgumentCaptor<Map> smsTemplateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
         verify(mockSmsGateway).dispatchSMS(eq(REGISTER_SUCCESS_SMS_KEY), smsTemplateValuesArgCaptor.capture(), eq(sender));
 
         SMSTemplateTest.assertContainsTemplateValues(new HashMap<String, String>() {{
             put(MOTECH_ID, childMotechId);
             put(FIRST_NAME, childFirstName);
             put(LAST_NAME, childDefaultLastName);
         }}, smsTemplateValuesArgCaptor.getValue());
 
         verify(mockAllSchedules).fulfilCurrentMilestone(mrsPatientId, ScheduleNames.ANC_DELIVERY, deliveryDate.toLocalDate());
         verify(mockAllAppointments).remove(mockPatient);
     }
 
     private PregnancyTerminationRequest pregnancyTermination(Patient mockPatient, MRSUser mockUser, Facility mockFacility) {
         PregnancyTerminationRequest request = new PregnancyTerminationRequest();
         request.setFacility(mockFacility);
         request.setPatient(mockPatient);
         request.setStaff(mockUser);
         request.setDead(Boolean.FALSE);
         request.setReferred(Boolean.FALSE);
         request.setTerminationProcedure("1");
         request.setTerminationType("2");
         request.setComments("Patient lost lot of blood");
         request.addComplication("1");
         request.addComplication("2");
         request.setPostAbortionFPAccepted(Boolean.TRUE);
         request.setPostAbortionFPCounselling(Boolean.FALSE);
         return request;
     }
 
     private PregnancyDeliveryRequest pregnancyDelivery(Patient mockPatient, MRSUser mockUser, Facility mockFacility, DeliveredChildRequest deliveredChildRequest, DateTime deliveryDate, String sender) {
         PregnancyDeliveryRequest request = new PregnancyDeliveryRequest();
         request.facility(mockFacility);
         request.patient(mockPatient);
         request.staff(mockUser);
         request.deliveryDateTime(deliveryDate);
         request.childDeliveryOutcome(ChildDeliveryOutcome.SINGLETON);
         request.addDeliveredChildRequest(deliveredChildRequest);
         request.deliveryComplications(Arrays.asList(DeliveryComplications.OTHER));
         request.sender(sender);
 
         return request;
     }
 }
