 package org.motechproject.ghana.national.service;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.configuration.ScheduleNames;
 import org.motechproject.ghana.national.domain.*;
 import org.motechproject.ghana.national.repository.AllEncounters;
 import org.motechproject.ghana.national.repository.AllSchedules;
 import org.motechproject.ghana.national.service.request.PNCBabyRequest;
 import org.motechproject.ghana.national.vo.CWCVisit;
 import org.motechproject.model.Time;
 import org.motechproject.mrs.model.*;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 
 import java.util.Date;
 import java.util.Set;
 
 import static ch.lambdaj.Lambda.*;
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.ArgumentCaptor.forClass;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 import static org.motechproject.ghana.national.domain.Concept.*;
 import static org.motechproject.ghana.national.domain.PNCChildVisit.*;
 import static org.motechproject.util.DateUtil.*;
 
 public class ChildVisitServiceTest extends BaseUnitTest {
     private ChildVisitService service;
     @Mock
     AllEncounters mockAllEncounters;
     @Mock
     private AllSchedules mockAllSchedules;
 
     @Before
     public void setUp() {
         initMocks(this);
         service = new ChildVisitService(mockAllEncounters, mockAllSchedules);
     }
 
     @Test
     public void shouldCreateEncounterForCWCVisitWithAllInfo() {
         MRSUser staff = mock(MRSUser.class);
         Facility facility = mock(Facility.class);
         Patient patient = mock(Patient.class);
         when(patient.pentaPatientCare()).thenReturn(new PatientCare(CWC_PENTA, DateUtil.today()));
         CWCVisit cwcVisit = createTestCWCVisit(new Date(), staff, facility, patient);
 
         ChildVisitService spyService = spy(service);
         spyService.save(cwcVisit);
 
         ArgumentCaptor<Encounter> encounterCaptor = ArgumentCaptor.forClass(Encounter.class);
         verify(mockAllEncounters).persistEncounter(encounterCaptor.capture());
         verify(spyService).updatePentaSchedule(cwcVisit);
         verify(spyService).updateYellowFeverSchedule(cwcVisit);
         verify(spyService).updateIPTSchedule(cwcVisit);
        verify(spyService).updateBCGSchedule(cwcVisit);
     }
 
     @Test
     public void shouldCreateEncounterForPNCBabyWithAllInfo() {
         String patientId = "mrsPatientId";
         MRSUser staff = mock(MRSUser.class);
         Facility facility = mock(Facility.class);
         Patient patient = new Patient(new MRSPatient(patientId, "motechId", new MRSPerson().dateOfBirth(newDate(2011, 3, 30).toDate()), new MRSFacility(null)));
         DateTime visitDate = newDateTime(2012, 2, 2, 9, 8, 8);
         Time visitTime = time(visitDate);
         PNCBabyRequest pncBabyRequest = createTestPNCBabyForm(visitDate, staff, facility, patient).visit(PNC2.visitNumber());
 
         DateTime today = visitDate.plusDays(1);
         mockCurrentDate(today);
         ChildVisitService spyService = spy(service);
         spyService.save(pncBabyRequest);
 
         ArgumentCaptor<Encounter> encounterCaptor = ArgumentCaptor.forClass(Encounter.class);
         verify(mockAllEncounters).persistEncounter(encounterCaptor.capture());
 
         ArgumentCaptor<EnrollmentRequest> requestCaptor = ArgumentCaptor.forClass(EnrollmentRequest.class);
         verify(mockAllSchedules).enrollOrFulfill(requestCaptor.capture(), eq(visitDate.toLocalDate()), eq(visitTime));
         assertEnrollmentRequest(new EnrollmentRequest(patientId, PNC2.scheduleName(), time(today), visitDate.toLocalDate(), visitTime, visitDate.toLocalDate(), visitTime, null)
                 , requestCaptor.getValue());
     }
   
     private PNCBabyRequest createTestPNCBabyForm(DateTime date, MRSUser staff, Facility facility, Patient patient) {
         PNCBabyRequest pncBabyRequest = new PNCBabyRequest();
         return pncBabyRequest.staff(staff).
                 facility(facility).
                 patient(patient).
                 date(date).
                 weight(65.67d).
                 comments("comments").
                 location("34").
                 house("house").
                 community("community").
                 maleInvolved(false).
                 babyConditionGood(Boolean.TRUE).
                 cordConditionNormal(Boolean.TRUE).
                 referred(false).
                 bcg(false).opv0(false).
                 respiration(12).
                 temperature(32.2d).
                 visit(1);
     }
 
     @Test
     public void shouldFulfillPentaScheduleIfAlreadyEnrolled() {
         String mrsPatientId = "mrsPatientId";
         Patient patient = mock(Patient.class);
         when(patient.getMRSPatientId()).thenReturn(mrsPatientId);
         EnrollmentRecord enrollmentRecord = mock(EnrollmentRecord.class);
         when(mockAllSchedules.enrollment(any(EnrollmentRequest.class))).thenReturn(enrollmentRecord);
 
         service.updatePentaSchedule(createTestCWCVisit(new Date(), mock(MRSUser.class), mock(Facility.class), patient));
 
         ArgumentCaptor<EnrollmentRequest> enrollmentRequestCaptor = ArgumentCaptor.forClass(EnrollmentRequest.class);
         verify(mockAllSchedules, never()).enroll(enrollmentRequestCaptor.capture());
         verify(mockAllSchedules).fulfilCurrentMilestone(eq(mrsPatientId), eq(ScheduleNames.CWC_PENTA), any(LocalDate.class));
     }
 
     @Test
     public void shouldNotFulfillPentaScheduleWhenPentaNotChosenInImmunization() {
         String mrsPatientId = "mrsPatientId";
         Patient patient = mock(Patient.class);
         when(patient.getMRSPatientId()).thenReturn(mrsPatientId);
         EnrollmentRecord enrollmentRecord = mock(EnrollmentRecord.class);
         when(mockAllSchedules.enrollment(any(EnrollmentRequest.class))).thenReturn(enrollmentRecord);
 
         CWCVisit cwcVisit = createTestCWCVisit(new Date(), mock(MRSUser.class), mock(Facility.class), patient);
         cwcVisit.pentadose("");
         service.updatePentaSchedule(cwcVisit);
 
         ArgumentCaptor<EnrollmentRequest> enrollmentRequestCaptor = ArgumentCaptor.forClass(EnrollmentRequest.class);
         verify(mockAllSchedules, never()).enroll(enrollmentRequestCaptor.capture());
         verify(mockAllSchedules, never()).fulfilCurrentMilestone(eq(mrsPatientId), eq(ScheduleNames.CWC_PENTA), any(LocalDate.class));
     }
 
     @Test
     public void shouldEnrollAndFulfillPentaScheduleIfNotEnrolledEarlier() {
         String mrsPatientId = "mrsPatientId";
         Patient patient = mock(Patient.class);
         when(patient.pentaPatientCare()).thenReturn(new PatientCare(CWC_PENTA, DateUtil.today()));
         when(patient.getMRSPatientId()).thenReturn(mrsPatientId);
         when(mockAllSchedules.enrollment(any(EnrollmentRequest.class))).thenReturn(null);
 
         service.updatePentaSchedule(createTestCWCVisit(new Date(), mock(MRSUser.class), mock(Facility.class), patient));
 
         ArgumentCaptor<EnrollmentRequest> enrollmentRequestCaptor = ArgumentCaptor.forClass(EnrollmentRequest.class);
         verify(mockAllSchedules).enroll(enrollmentRequestCaptor.capture());
         verify(mockAllSchedules).fulfilCurrentMilestone(eq(mrsPatientId), eq(ScheduleNames.CWC_PENTA), any(LocalDate.class));
     }
 
     @Test
     // 2 scenarios - in both case IPT1 is created and fulfilled and send alerts for remaining weeks
     // i) User enrolled after 14th week
     // ii) User enrolled for SP2, taking IPT2 directly based on some pre-history
     public void shouldEnrollOrFulfilIPTiScheduleBasedOnVisitDate_AndRecordObservations() {
 
         Time deliveryTime = new Time(20, 2);
         DateTime today = new DateTime(2012, 2, 1, deliveryTime.getHour(), deliveryTime.getMinute());
         LocalDate visitDate = today.minusDays(10).toLocalDate();
         IPTDose dose = IPTDose.SP2;
         String mrsPatientId = "1234";
         CWCVisit cwcVisit = createTestCWCVisit(visitDate.toDate(), new MRSUser(), mock(Facility.class),
                 new Patient(new MRSPatient(mrsPatientId, "", new MRSPerson().dateOfBirth(newDate(2011, 1, 1).toDate()), null)))
                 .iptidose(dose.value().toString());
 
         mockCurrentDate(today);
         service.save(cwcVisit);
 
         ArgumentCaptor<Encounter> encounterCaptor = forClass(Encounter.class);
         verify(mockAllEncounters).persistEncounter(encounterCaptor.capture());
         assertIfObservationsAvailableForConcepts(true, encounterCaptor.getValue().getObservations(), IPTI.getName());
 
         ArgumentCaptor<EnrollmentRequest> captor = forClass(EnrollmentRequest.class);
         verify(mockAllSchedules).enrollOrFulfill(captor.capture(), eq(visitDate));
         assertEnrollmentRequest(new EnrollmentRequest(mrsPatientId, CWC_IPT_VACCINE, deliveryTime, visitDate, null, visitDate, null, dose.milestone()), captor.getAllValues().get(0));
     }
 
     @Test
     public void shouldNotCreateIPTSchedule_IfIPTReadingsAreNotCaptured() {
         Time deliveryTime = new Time(20, 2);
         DateTime today = new DateTime(2012, 2, 1, deliveryTime.getHour(), deliveryTime.getMinute());
         mockCurrentDate(today);
         LocalDate visitDate = today.minusDays(10).toLocalDate();
         String mrsPatientId = "99999";
         CWCVisit cwcVisit = createTestCWCVisit(visitDate.toDate(), new MRSUser(), mock(Facility.class),
                 new Patient(new MRSPatient(mrsPatientId, "", new MRSPerson().dateOfBirth(newDate(2011, 1, 1).toDate()), null)))
                 .iptidose(null);
 
         service.save(cwcVisit);
         ArgumentCaptor<Encounter> encounterCaptor = forClass(Encounter.class);
         verify(mockAllEncounters).persistEncounter(encounterCaptor.capture());
         assertIfObservationsAvailableForConcepts(false, encounterCaptor.getValue().getObservations(), IPTI.getName());
         verify(mockAllSchedules, never()).fulfilCurrentMilestone(mrsPatientId, CWC_IPT_VACCINE, visitDate);
     }
 
     @Test
     public void shouldFulfillYellowFeverScheduleIfYellowFeverIsChosen() {
         String mrsPatientId = "1234";
         CWCVisit testCWCVisit = createTestCWCVisit(new Date(), new MRSUser(), mock(Facility.class), new Patient(new MRSPatient(mrsPatientId)));
         testCWCVisit.immunizations(asList(Concept.YF.name()));
 
         service.updateYellowFeverSchedule(testCWCVisit);
 
         verify(mockAllSchedules).fulfilCurrentMilestone(mrsPatientId, CWC_YELLOW_FEVER, DateUtil.newDate(testCWCVisit.getDate()));
     }
 
     @Test
    public void shouldFulfillBCGScheduleIfChosen() {
        String mrsPatientId = "1234";
        CWCVisit testCWCVisit = createTestCWCVisit(new Date(), new MRSUser(), mock(Facility.class), new Patient(new MRSPatient(mrsPatientId)));
        testCWCVisit.immunizations(asList(Concept.BCG.name()));

        service.updateBCGSchedule(testCWCVisit);

        verify(mockAllSchedules).fulfilCurrentMilestone(mrsPatientId, CWC_BCG, DateUtil.newDate(testCWCVisit.getDate()));
    }

    @Test
     public void shouldFulfilMilestoneIfEnrolledAndTakenMeaslesVaccineOnVisit() {
 
         String mrsPatientId = "mrsPatientId";
         Patient patient = new Patient(new MRSPatient(mrsPatientId, "motechId", new MRSPerson().dateOfBirth(newDate(2011, 3, 30).toDate()), new MRSFacility(null)));
         LocalDate visitDate = newDate(2012, 2, 3);
         CWCVisit cwcVisit = createTestCWCVisit(visitDate.toDate(), mock(MRSUser.class), mock(Facility.class), patient)
                 .immunizations(asList(MEASLES.name()));
         EnrollmentRecord mockEnrollmentRecord = mock(EnrollmentRecord.class);
         when(mockAllSchedules.enrollment(Matchers.<EnrollmentRequest>any())).thenReturn(mockEnrollmentRecord);
         service.save(cwcVisit);
 
         verify(mockAllSchedules).fulfilCurrentMilestone(mrsPatientId, CWC_MEASLES_VACCINE, visitDate);
     }
 
     @Test
     public void shouldNonFulfilMilestoneIfHaveNotTakenMeaslesVaccineOnVisit() {
 
         Patient patient = new Patient(new MRSPatient("mrsPatientId", "motechId", new MRSPerson().dateOfBirth(newDate(2011, 3, 30).toDate()), new MRSFacility(null)));
         CWCVisit cwcVisit = createTestCWCVisit(today().toDate(), mock(MRSUser.class), mock(Facility.class), patient).immunizations(asList(Concept.PENTA.name()));
         EnrollmentRecord mockEnrollmentRecord = mock(EnrollmentRecord.class);
         when(mockAllSchedules.enrollment(Matchers.<EnrollmentRequest>any())).thenReturn(mockEnrollmentRecord);
         service.save(cwcVisit);
         verify(mockAllSchedules, never()).fulfilCurrentMilestone(eq("mrsPatientId"), eq(CWC_MEASLES_VACCINE), Matchers.<LocalDate>any());
     }
 
     @Test
     public void shouldNonFulfilMilestoneIfNotEnrolledAndHaveEnteredTakenMeaslesVaccineMistakenlyOnVisit() {
 
         Patient patient = new Patient(new MRSPatient("mrsPatientId", "motechId", new MRSPerson().dateOfBirth(newDate(2011, 3, 30).toDate()), new MRSFacility(null)));
         CWCVisit cwcVisit = createTestCWCVisit(today().toDate(), mock(MRSUser.class), mock(Facility.class), patient).immunizations(asList(MEASLES.name()));
         when(mockAllSchedules.enrollment(Matchers.<EnrollmentRequest>any())).thenReturn(null);
         service.save(cwcVisit);
         verify(mockAllSchedules, never()).fulfilCurrentMilestone(eq("mrsPatientId"), eq(CWC_MEASLES_VACCINE), Matchers.<LocalDate>any());
     }
 
     private CWCVisit createTestCWCVisit(Date registrationDate, MRSUser staff, Facility facility, Patient patient) {
         CWCVisit cwcVisit = new CWCVisit();
         return cwcVisit.staff(staff).facility(facility).patient(patient).date(registrationDate).serialNumber("4ds65").pentadose("2")
                 .weight(65.67d).comments("comments").cwcLocation("34").house("house").community("community").maleInvolved(false);
     }
 
     private void assertEnrollmentReqWithoutDeliveryTime(EnrollmentRequest expected, EnrollmentRequest actual) {
         assertThat(actual.getScheduleName(), is(equalTo(expected.getScheduleName())));
         assertThat(actual.getReferenceDate(), is(equalTo(expected.getReferenceDate())));
         assertThat(actual.getExternalId(), is(equalTo(expected.getExternalId())));
         assertThat(actual.getStartingMilestoneName(), is(equalTo(expected.getStartingMilestoneName())));
     }
 
     private void assertEnrollmentRequest(EnrollmentRequest expected, EnrollmentRequest actual) {
         assertEnrollmentReqWithoutDeliveryTime(expected, actual);
         assertThat(actual.getPreferredAlertTime(), is(equalTo(expected.getPreferredAlertTime())));
     }
 
     private void assertIfObservationsAvailableForConcepts(Boolean present, Set<MRSObservation> observations, String... conceptNames) {
         for (String conceptName : conceptNames)
             if (present)
                 assertNotNull("concept not present:" + conceptName, selectFirst(observations, having(on(MRSObservation.class).getConceptName(), equalTo(conceptName))));
             else
                 assertNull("concept present:" + conceptName, selectFirst(observations, having(on(MRSObservation.class).getConceptName(), equalTo(conceptName))));
     }
 }
