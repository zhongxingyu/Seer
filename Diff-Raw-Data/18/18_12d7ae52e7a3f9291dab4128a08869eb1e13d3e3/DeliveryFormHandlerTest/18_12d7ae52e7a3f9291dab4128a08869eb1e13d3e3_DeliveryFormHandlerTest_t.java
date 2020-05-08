 package org.motechproject.ghana.national.handlers;
 
import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.bean.DeliveryForm;
 import org.motechproject.ghana.national.domain.*;
 import org.motechproject.ghana.national.exception.XFormHandlerException;
 import org.motechproject.ghana.national.service.*;
 import org.motechproject.ghana.national.service.request.DeliveredChildRequest;
 import org.motechproject.ghana.national.service.request.PregnancyDeliveryRequest;
 import org.motechproject.mrs.exception.PatientNotFoundException;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.mrs.model.MRSUser;
 import org.motechproject.util.DateUtil;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import static junit.framework.Assert.fail;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class DeliveryFormHandlerTest {
 
     private DeliveryFormHandler handler;
 
     @Mock
     PregnancyService mockPregnancyService;
 
     @Mock
     MobileMidwifeService mockMobileMidwifeService;
 
     @Mock
     PatientService mockPatientService;
 
     @Mock
     FacilityService mockFacilityService;
 
     @Mock
     StaffService mockStaffService;
     @Mock
     private CareService mockCareService;
 
     @Before
     public void setUp() {
         initMocks(this);
         handler = new DeliveryFormHandler();
         ReflectionTestUtils.setField(handler, "pregnancyService", mockPregnancyService);
         ReflectionTestUtils.setField(handler, "mobileMidwifeService", mockMobileMidwifeService);
         ReflectionTestUtils.setField(handler, "patientService", mockPatientService);
         ReflectionTestUtils.setField(handler, "facilityService", mockFacilityService);
         ReflectionTestUtils.setField(handler, "staffService", mockStaffService);
         ReflectionTestUtils.setField(handler, "careService", mockCareService);
     }
 
     @Test
     public void shouldRethrowException() {
         doThrow(new RuntimeException()).when(mockPregnancyService).handleDelivery(Matchers.<PregnancyDeliveryRequest>any());
         try {
             handler.handleFormEvent(new DeliveryForm());
             fail("Should handle exception");
         } catch (XFormHandlerException e) {
             assertThat(e.getMessage(), is("Encountered error while processing delivery form"));
         }
     }
 
     @Test
     public void shouldCreateADeliveryRequestAndUnEnrollMotherFromMM_IfMotherIsAlive() {
         String motechFacilityId = "232465";
         String staffId = "465";
         String motechId = "2321465";
 
         final DeliveryForm deliveryForm = new DeliveryForm();
         deliveryForm.setStaffId(staffId);
         deliveryForm.setFacilityId(motechFacilityId);
         deliveryForm.setMotechId(motechId);
         deliveryForm.setDate(DateUtil.now());
         deliveryForm.setComments("comments");
         deliveryForm.setMaleInvolved(false);
 
 
         deliveryForm.setMode(ChildDeliveryMode.C_SECTION);
         deliveryForm.setOutcome(ChildDeliveryOutcome.TRIPLETS);
         deliveryForm.setDeliveryLocation(ChildDeliveryLocation.GOVERNMENT_HOSPITAL);
         deliveryForm.setDeliveredBy(ChildDeliveredBy.DOCTOR);
         deliveryForm.setComplications(DeliveryComplications.OTHER.name());
         deliveryForm.setVvf(VVF.REFERRED);
         deliveryForm.setMaternalDeath(false);
 
         deliveryForm.setChild1Outcome(BirthOutcome.ALIVE);
         deliveryForm.setChild1RegistrationType(RegistrationType.USE_PREPRINTED_ID);
         deliveryForm.setChild1MotechId("1234544");
         deliveryForm.setChild1Sex("male");
         deliveryForm.setChild1FirstName("baby1");
         deliveryForm.setChild1Weight("1");
 
         deliveryForm.setChild2Outcome(BirthOutcome.ALIVE);
         deliveryForm.setChild2RegistrationType(RegistrationType.AUTO_GENERATE_ID);
         deliveryForm.setChild2Sex("female");
         deliveryForm.setChild2FirstName("baby2");
         deliveryForm.setChild2Weight("1.2");
 
         deliveryForm.setChild3Outcome(BirthOutcome.FRESH_STILL_BIRTH);
         deliveryForm.setChild3RegistrationType(RegistrationType.AUTO_GENERATE_ID);
         deliveryForm.setChild3Sex("?");
         deliveryForm.setChild3FirstName("baby3");
         deliveryForm.setChild3Weight("1.1");
 
         deliveryForm.setSender("sender");
 
         String facilityId = "111";
         MRSFacility mrsFacility = new MRSFacility(facilityId);
         Patient patient = new Patient(new MRSPatient("patientId", motechId, new MRSPerson(), mrsFacility));
         MRSUser staff = new MRSUser();
         staff.id(staffId);
         Facility facility = new Facility(mrsFacility);
         facility.motechId(motechFacilityId);
 
         when(mockFacilityService.getFacilityByMotechId(motechFacilityId)).thenReturn(facility);
         when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(staff);
 
         handler.handleFormEvent(deliveryForm);
 
         verify(mockMobileMidwifeService).unRegister(deliveryForm.getMotechId());
 
         ArgumentCaptor<PregnancyDeliveryRequest> deliveryRequestCaptor = ArgumentCaptor.forClass(PregnancyDeliveryRequest.class);
         verify(mockPregnancyService).handleDelivery(deliveryRequestCaptor.capture());
 
         PregnancyDeliveryRequest deliveryRequest = deliveryRequestCaptor.getValue();
 
         assertEquals(staff, deliveryRequest.getStaff());
         assertEquals(facility, deliveryRequest.getFacility());
         assertEquals(patient, deliveryRequest.getPatient());
         assertEquals(deliveryForm.getDate(), deliveryRequest.getDeliveryDateTime());
         assertEquals(deliveryForm.getComments(), deliveryRequest.getComments());
         assertEquals(deliveryForm.getMaleInvolved(), deliveryRequest.getMaleInvolved());
         assertEquals(deliveryForm.getMode(), deliveryRequest.getChildDeliveryMode());
         assertEquals(deliveryForm.getOutcome(), deliveryRequest.getChildDeliveryOutcome());
         assertEquals(deliveryForm.getDeliveryLocation(), deliveryRequest.getChildDeliveryLocation());
         assertEquals(deliveryForm.getDeliveredBy(), deliveryRequest.getChildDeliveredBy());
         assertEquals(deliveryForm.getDeliveryComplications(), deliveryRequest.getDeliveryComplications());
         assertEquals(deliveryForm.getVvf(), deliveryRequest.getVvf());
         assertEquals(deliveryForm.getMaternalDeath(), deliveryRequest.getMaternalDeath());
 
         assertEquals(3, deliveryRequest.getDeliveredChildRequests().size());
 
         DeliveredChildRequest deliveredChild1 = deliveryRequest.getDeliveredChildRequests().get(0);
         DeliveredChildRequest deliveredChild2 = deliveryRequest.getDeliveredChildRequests().get(1);
         DeliveredChildRequest deliveredChild3 = deliveryRequest.getDeliveredChildRequests().get(2);
 
         assertEquals(deliveryForm.getChild1Outcome(), deliveredChild1.getChildBirthOutcome());
         assertEquals(deliveryForm.getChild1RegistrationType(), deliveredChild1.getChildRegistrationType());
         assertEquals(deliveryForm.getChild1MotechId(), deliveredChild1.getChildMotechId());
         assertEquals(deliveryForm.getChild1Sex(), deliveredChild1.getChildSex());
         assertEquals(deliveryForm.getChild1FirstName(), deliveredChild1.getChildFirstName());
         assertEquals(deliveryForm.getChild1Weight(), deliveredChild1.getChildWeight());
 
         assertEquals(deliveryForm.getChild2Outcome(), deliveredChild2.getChildBirthOutcome());
         assertEquals(deliveryForm.getChild2RegistrationType(), deliveredChild2.getChildRegistrationType());
         assertEquals(deliveryForm.getChild2Sex(), deliveredChild2.getChildSex());
         assertEquals(deliveryForm.getChild2FirstName(), deliveredChild2.getChildFirstName());
         assertEquals(deliveryForm.getChild2Weight(), deliveredChild2.getChildWeight());
 
         assertEquals(deliveryForm.getChild3Outcome(), deliveredChild3.getChildBirthOutcome());
         assertEquals(deliveryForm.getChild3RegistrationType(), deliveredChild3.getChildRegistrationType());
         assertEquals(deliveryForm.getChild3Sex(), deliveredChild3.getChildSex());
         assertEquals(deliveryForm.getChild3FirstName(), deliveredChild3.getChildFirstName());
         assertEquals(deliveryForm.getChild3Weight(), deliveredChild3.getChildWeight());
 
         assertEquals(deliveryForm.getSender(), deliveryRequest.getSender());
     }
     
     @Test
     public void shouldCreateADeliveryRequestAndRolloverMotherFromMMPregnancyToChildCare_IfMotherAndAtleastOneChildIsAlive() {
         String motechFacilityId = "232465";
         String staffId = "465";
         String motechId = "2321465";
 
         final DeliveryForm deliveryForm = new DeliveryForm();
         deliveryForm.setStaffId(staffId);
         deliveryForm.setFacilityId(motechFacilityId);
         deliveryForm.setMotechId(motechId);
         deliveryForm.setDate(DateUtil.now());
         deliveryForm.setComments("comments");
         deliveryForm.setMaleInvolved(false);
 
 
         deliveryForm.setMode(ChildDeliveryMode.C_SECTION);
         deliveryForm.setOutcome(ChildDeliveryOutcome.TRIPLETS);
         deliveryForm.setDeliveryLocation(ChildDeliveryLocation.GOVERNMENT_HOSPITAL);
         deliveryForm.setDeliveredBy(ChildDeliveredBy.DOCTOR);
         deliveryForm.setComplications(DeliveryComplications.OTHER.name());
         deliveryForm.setVvf(VVF.REFERRED);
         deliveryForm.setMaternalDeath(false);
 
         deliveryForm.setChild1Outcome(BirthOutcome.ALIVE);
         deliveryForm.setChild1RegistrationType(RegistrationType.USE_PREPRINTED_ID);
         deliveryForm.setChild1MotechId("1234544");
         deliveryForm.setChild1Sex("male");
         deliveryForm.setChild1FirstName("baby1");
         deliveryForm.setChild1Weight("1");
 
         deliveryForm.setChild2Outcome(BirthOutcome.FRESH_STILL_BIRTH);
         deliveryForm.setChild2RegistrationType(RegistrationType.AUTO_GENERATE_ID);
         deliveryForm.setChild2Sex("female");
         deliveryForm.setChild2FirstName("baby2");
         deliveryForm.setChild2Weight("1.2");
 
         deliveryForm.setSender("sender");
 
 
         String facilityId = "111";
         MRSFacility mrsFacility = new MRSFacility(facilityId);
         Patient patient = new Patient(new MRSPatient("patientId", motechId, new MRSPerson(), mrsFacility));
         MRSUser staff = new MRSUser();
         staff.id(staffId);
         Facility facility = new Facility(mrsFacility);
         facility.motechId(motechFacilityId);
 
         when(mockFacilityService.getFacilityByMotechId(motechFacilityId)).thenReturn(facility);
         when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(staff);
         when(mockPregnancyService.isDeliverySuccessful(Matchers.<PregnancyDeliveryRequest>any())).thenReturn(true);
 
         handler.handleFormEvent(deliveryForm);
 
         verify(mockMobileMidwifeService).rollover(deliveryForm.getMotechId(), deliveryForm.getDate());
     }
 
     @Test
     public void shouldDeceaseMotherAndHandleDeliveryInCaseOfMaternalDeath() throws PatientNotFoundException {
         String motechFacilityId = "232465";
         String staffId = "465";
         String motechId = "2321465";
 
         final DeliveryForm deliveryForm = new DeliveryForm();
         deliveryForm.setStaffId(staffId);
         deliveryForm.setFacilityId(motechFacilityId);
         deliveryForm.setMotechId(motechId);
         deliveryForm.setDate(DateUtil.now());
         deliveryForm.setComments("comments");
         deliveryForm.setMaleInvolved(false);
         deliveryForm.setMaternalDeath(true);
 
         deliveryForm.setMode(ChildDeliveryMode.C_SECTION);
         deliveryForm.setOutcome(ChildDeliveryOutcome.TRIPLETS);
         deliveryForm.setDeliveryLocation(ChildDeliveryLocation.GOVERNMENT_HOSPITAL);
         deliveryForm.setDeliveredBy(ChildDeliveredBy.DOCTOR);
         deliveryForm.setComplications(DeliveryComplications.OTHER.name());
         deliveryForm.setVvf(VVF.REFERRED);
 
         deliveryForm.setChild1Outcome(BirthOutcome.ALIVE);
         deliveryForm.setChild1RegistrationType(RegistrationType.USE_PREPRINTED_ID);
         deliveryForm.setChild1MotechId("1234544");
         deliveryForm.setChild1Sex("male");
         deliveryForm.setChild1FirstName("baby1");
         deliveryForm.setChild1Weight("1");
 
         deliveryForm.setChild2Outcome(BirthOutcome.ALIVE);
         deliveryForm.setChild2RegistrationType(RegistrationType.AUTO_GENERATE_ID);
         deliveryForm.setChild2Sex("female");
         deliveryForm.setChild2FirstName("baby2");
         deliveryForm.setChild2Weight("1.2");
 
         deliveryForm.setChild3Outcome(BirthOutcome.FRESH_STILL_BIRTH);
         deliveryForm.setChild3RegistrationType(RegistrationType.AUTO_GENERATE_ID);
         deliveryForm.setChild3Sex("?");
         deliveryForm.setChild3FirstName("baby3");
         deliveryForm.setChild3Weight("1.1");
 
         deliveryForm.setSender("sender");
 
 
         String facilityId = "111";
         MRSFacility mrsFacility = new MRSFacility(facilityId);
         Patient patient = new Patient(new MRSPatient("patientId", motechId, new MRSPerson(), mrsFacility));
         MRSUser staff = new MRSUser();
         staff.id(staffId);
         Facility facility = new Facility(mrsFacility);
         facility.motechId(motechFacilityId);
 
         when(mockFacilityService.getFacilityByMotechId(motechFacilityId)).thenReturn(facility);
         when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(staff);
 
         handler.handleFormEvent(deliveryForm);
 
         verify(mockPatientService).deceasePatient(deliveryForm.getDate().toDate(), deliveryForm.getMotechId(), "OTHER", "Delivery");
 
         ArgumentCaptor<PregnancyDeliveryRequest> deliveryRequestCaptor = ArgumentCaptor.forClass(PregnancyDeliveryRequest.class);
         verify(mockPregnancyService).handleDelivery(deliveryRequestCaptor.capture());
 
         PregnancyDeliveryRequest deliveryRequest = deliveryRequestCaptor.getValue();
 
         assertEquals(staff, deliveryRequest.getStaff());
         assertEquals(facility, deliveryRequest.getFacility());
         assertEquals(patient, deliveryRequest.getPatient());
         assertEquals(deliveryForm.getDate(), deliveryRequest.getDeliveryDateTime());
         assertEquals(deliveryForm.getComments(), deliveryRequest.getComments());
         assertEquals(deliveryForm.getMaleInvolved(), deliveryRequest.getMaleInvolved());
         assertEquals(deliveryForm.getMode(), deliveryRequest.getChildDeliveryMode());
         assertEquals(deliveryForm.getOutcome(), deliveryRequest.getChildDeliveryOutcome());
         assertEquals(deliveryForm.getDeliveryLocation(), deliveryRequest.getChildDeliveryLocation());
         assertEquals(deliveryForm.getDeliveredBy(), deliveryRequest.getChildDeliveredBy());
         assertEquals(deliveryForm.getDeliveryComplications(), deliveryRequest.getDeliveryComplications());
         assertEquals(deliveryForm.getVvf(), deliveryRequest.getVvf());
         assertEquals(deliveryForm.getMaternalDeath(), deliveryRequest.getMaternalDeath());
 
         assertEquals(3, deliveryRequest.getDeliveredChildRequests().size());
 
         DeliveredChildRequest deliveredChild1 = deliveryRequest.getDeliveredChildRequests().get(0);
         DeliveredChildRequest deliveredChild2 = deliveryRequest.getDeliveredChildRequests().get(1);
         DeliveredChildRequest deliveredChild3 = deliveryRequest.getDeliveredChildRequests().get(2);
 
         assertEquals(deliveryForm.getChild1Outcome(), deliveredChild1.getChildBirthOutcome());
         assertEquals(deliveryForm.getChild1RegistrationType(), deliveredChild1.getChildRegistrationType());
         assertEquals(deliveryForm.getChild1MotechId(), deliveredChild1.getChildMotechId());
         assertEquals(deliveryForm.getChild1Sex(), deliveredChild1.getChildSex());
         assertEquals(deliveryForm.getChild1FirstName(), deliveredChild1.getChildFirstName());
         assertEquals(deliveryForm.getChild1Weight(), deliveredChild1.getChildWeight());
 
         assertEquals(deliveryForm.getChild2Outcome(), deliveredChild2.getChildBirthOutcome());
         assertEquals(deliveryForm.getChild2RegistrationType(), deliveredChild2.getChildRegistrationType());
         assertEquals(deliveryForm.getChild2Sex(), deliveredChild2.getChildSex());
         assertEquals(deliveryForm.getChild2FirstName(), deliveredChild2.getChildFirstName());
         assertEquals(deliveryForm.getChild2Weight(), deliveredChild2.getChildWeight());
 
         assertEquals(deliveryForm.getChild3Outcome(), deliveredChild3.getChildBirthOutcome());
         assertEquals(deliveryForm.getChild3RegistrationType(), deliveredChild3.getChildRegistrationType());
         assertEquals(deliveryForm.getChild3Sex(), deliveredChild3.getChildSex());
         assertEquals(deliveryForm.getChild3FirstName(), deliveredChild3.getChildFirstName());
         assertEquals(deliveryForm.getChild3Weight(), deliveredChild3.getChildWeight());
 
         assertEquals(deliveryForm.getSender(), deliveryRequest.getSender());

        verify(mockMobileMidwifeService,never()).rollover(anyString(),Matchers.<DateTime>any());
        verify(mockMobileMidwifeService,atLeastOnce()).unRegister(anyString());
     }
 }
