 package org.motechproject.care.integration.schedule;
 
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.care.repository.AllChildren;
 import org.motechproject.care.request.CareCase;
 import org.motechproject.care.schedule.service.MilestoneType;
 import org.motechproject.care.schedule.vaccinations.ChildVaccinationSchedule;
 import org.motechproject.care.service.ChildService;
 import org.motechproject.care.service.ChildVaccinationProcessor;
 import org.motechproject.care.service.builder.ChildCareCaseBuilder;
 import org.motechproject.care.service.schedule.OpvBoosterService;
 import org.motechproject.care.service.schedule.VaccinationService;
 import org.motechproject.care.utils.CaseUtils;
 import org.motechproject.care.utils.SpringIntegrationTest;
 import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;
 import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
 import org.motechproject.scheduletracking.api.service.EnrollmentsQuery;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 
 public class OpvBoosterIntegrationTest extends SpringIntegrationTest {
 
     @Autowired
     private OpvBoosterService opvBoosterService;
     @Autowired
     private ScheduleTrackingService scheduleTrackingService;
     @Autowired
     private AllChildren allChilden;
     private final String caseId = CaseUtils.getUniqueCaseId();
     private ChildService childService;
     private String scheduleName = ChildVaccinationSchedule.OPVBooster.getName();
 
     @After
     public void tearDown() {
         allChilden.removeAll();
     }
 
     @Before
     public void setUp(){
         List<VaccinationService> ancServices = Arrays.asList((VaccinationService) opvBoosterService);
         ChildVaccinationProcessor childVaccinationProcessor = new ChildVaccinationProcessor(ancServices);
         childService = new ChildService(allChilden, childVaccinationProcessor);
     }
     
     @Test
     public void shouldVerifyOPVBoosterScheduleWithStartDateAs16MonthsAgeIfOPV3IsFulfilledMuchBefore() {
         LocalDate dob = DateUtil.today();
         LocalDate opv1Date = dob.plusWeeks(7);
         LocalDate opv2Date = dob.plusWeeks(11);
         LocalDate opv3Date = dob.plusWeeks(15);
         LocalDate expectedReferenceDate = dob.plusMonths(16).minusWeeks(2);
         LocalDate expectedStartDueDate = dob.plusMonths(16);
 
         CareCase careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withOPV1Date(null).withOPV2Date(null).withOPV3Date(null).withOPVBoosterDate(null).build();
         childService.process(careCase);
         markScheduleForUnEnrollment(caseId, scheduleName);
         EnrollmentRecord enrollment;
         assertNull(getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE));
 
 
         careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withOPV1Date(opv1Date.toString()).withOPV2Date(opv2Date.toString()).withOPV3Date(opv3Date.toString()).withOPVBoosterDate(null).build();
         childService.process(careCase);
         enrollment = getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE);
         assertEquals(MilestoneType.OPVBooster.toString(), enrollment.getCurrentMilestoneName());
         assertEquals(DateUtil.newDateTime(expectedReferenceDate), enrollment.getReferenceDateTime());
         assertEquals(DateUtil.newDateTime(expectedStartDueDate), enrollment.getStartOfDueWindow());
        assertEquals(DateUtil.newDateTime(expectedReferenceDate.plusMonths(8).plusWeeks(2)), enrollment.getStartOfLateWindow());
     }
 
     @Test
     public void shouldVerifyOPVBoosterScheduleWithStartDateAfter16MonthsAgeIfOPV3IsNotFulfilledMuchBefore() {
         LocalDate dob = DateUtil.today();
         LocalDate opv1Date = dob.plusWeeks(7);
         LocalDate opv2Date = dob.plusWeeks(11);
         LocalDate opv3Date = dob.plusMonths(15);
         LocalDate expectedReferenceDate = opv3Date.plusDays(180).minusWeeks(2);
         LocalDate expectedStartDueDate = opv3Date.plusDays(180);
         LocalDate expectedStartLateDate = opv3Date.plusDays(180).plusMonths(8);
 
         CareCase careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withOPV1Date(null).withOPV2Date(null).withOPV3Date(null).withOPVBoosterDate(null).build();
         childService.process(careCase);
         markScheduleForUnEnrollment(caseId, scheduleName);
         EnrollmentRecord enrollment;
         assertNull(getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE));
 
 
         careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withOPV1Date(opv1Date.toString()).withOPV2Date(opv2Date.toString()).withOPV3Date(opv3Date.toString()).withOPVBoosterDate(null).build();
         childService.process(careCase);
         enrollment = getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE);
         assertEquals(MilestoneType.OPVBooster.toString(), enrollment.getCurrentMilestoneName());
         assertEquals(DateUtil.newDateTime(expectedReferenceDate), enrollment.getReferenceDateTime());
         assertEquals(DateUtil.newDateTime(expectedStartDueDate), enrollment.getStartOfDueWindow());
         assertEquals(DateUtil.newDateTime(expectedStartLateDate), enrollment.getStartOfLateWindow());
     }
 
     @Test
     public void shouldVerifyAnc4ScheduleFulfillmentWhenAnc4VisitIsOver() {
         LocalDate dob = DateUtil.today();
         LocalDate opv1Date = dob.plusWeeks(7);
         LocalDate opv2Date = dob.plusWeeks(11);
         LocalDate opv3Date = dob.plusMonths(15);
         LocalDate opvBoosterDate = dob.plusMonths(21);
 
         CareCase careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withOPV1Date(null).withOPV2Date(null).withOPV3Date(null).withOPVBoosterDate(null).build();
         childService.process(careCase);
         markScheduleForUnEnrollment(caseId, scheduleName);
         EnrollmentRecord enrollment;
         assertNull(getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE));
 
 
         careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withOPV1Date(opv1Date.toString()).withOPV2Date(opv2Date.toString()).withOPV3Date(opv3Date.toString()).withOPVBoosterDate(opvBoosterDate.toString()).build();
         childService.process(careCase);
 
         assertNull(scheduleTrackingService.getEnrollment(caseId, scheduleName));
     }
 
     private EnrollmentRecord getEnrollmentRecord(String ttScheduleName, String externalId, EnrollmentStatus status) {
         EnrollmentsQuery query = new EnrollmentsQuery()
                 .havingExternalId(externalId)
                 .havingState(status)
                 .havingSchedule(ttScheduleName);
 
         List<EnrollmentRecord> enrollmentRecords = scheduleTrackingService.searchWithWindowDates(query);
         return enrollmentRecords.isEmpty()? null : enrollmentRecords.get(0);
     }
 }
