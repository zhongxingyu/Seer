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
 import org.motechproject.care.service.schedule.DptBoosterService;
 import org.motechproject.care.service.schedule.VaccinationService;
 import org.motechproject.care.utils.CaseUtils;
 import org.motechproject.care.utils.SpringIntegrationTest;
 import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;
 import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 
 public class DptBoosterIntegrationTest extends SpringIntegrationTest {
 
     @Autowired
     private DptBoosterService dptBoosterService;
     @Autowired
     private AllChildren allChilden;
 
     private String caseId;
     private ChildService childService;
     private String scheduleName = ChildVaccinationSchedule.DPTBooster.getName();
 
     @After
     public void tearDown() {
         allChilden.removeAll();
     }
 
     @Before
     public void setUp(){
         caseId = CaseUtils.getUniqueCaseId();
         List<VaccinationService> ancServices = Arrays.asList((VaccinationService) dptBoosterService);
         ChildVaccinationProcessor childVaccinationProcessor = new ChildVaccinationProcessor(ancServices);
         childService = new ChildService(allChilden, childVaccinationProcessor);
     }
     
     @Test
     public void shouldVerifyDPTBoosterScheduleWithStartDateAs16MonthsAgeIfDpt3IsFulfilledMuchBefore() {
         LocalDate dob = DateUtil.today();
         LocalDate dpt1Date = dob.plusWeeks(7);
         LocalDate dpt2Date = dob.plusWeeks(11);
         LocalDate dpt3Date = dob.plusWeeks(15);
         LocalDate expectedReferenceDate = dob.plusMonths(16).plus(periodUtil.getScheduleOffset());
         LocalDate expectedStartDueDate = dob.plusMonths(16);
 
         CareCase careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(null).withDpt2Date(null).withDpt3Date(null).withDptBoosterDate(null).build();
         childService.process(careCase);
         markScheduleForUnEnrollment(caseId, scheduleName);
         EnrollmentRecord enrollment;
         assertNull(getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE));
 
         careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(dpt1Date.toString()).withDpt2Date(dpt2Date.toString()).withDpt3Date(dpt3Date.toString()).withDptBoosterDate(null).build();
         childService.process(careCase);
         enrollment = getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE);
         assertEquals(MilestoneType.DPTBooster.toString(), enrollment.getCurrentMilestoneName());
         assertEquals(DateUtil.newDateTime(expectedReferenceDate), enrollment.getReferenceDateTime());
         assertEquals(DateUtil.newDateTime(expectedStartDueDate), enrollment.getStartOfDueWindow());
         assertEquals(DateUtil.newDateTime(expectedReferenceDate.plusMonths(8).plusWeeks(2)), enrollment.getStartOfLateWindow());
     }
 
     @Test
     public void shouldVerifyDPTBoosterScheduleWithStartDateAfter16MonthsAgeIfDpt3IsNotFulfilledMuchBefore() {
         LocalDate dob = DateUtil.today();
         LocalDate dpt1Date = dob.plusWeeks(7);
         LocalDate dpt2Date = dob.plusWeeks(11);
         LocalDate dpt3Date = dob.plusMonths(15);
         LocalDate expectedReferenceDate = dpt3Date.plusDays(180).plus(periodUtil.getScheduleOffset());
         LocalDate expectedStartDueDate = dpt3Date.plusDays(180);
        LocalDate expectedStartLateDate = dpt3Date.plusMonths(8).plusDays(180);
 
         CareCase careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(null).withDpt2Date(null).withDpt3Date(null).withDptBoosterDate(null).build();
         childService.process(careCase);
         markScheduleForUnEnrollment(caseId, scheduleName);
         EnrollmentRecord enrollment;
         assertNull(getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE));
 
         careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(dpt1Date.toString()).withDpt2Date(dpt2Date.toString()).withDpt3Date(dpt3Date.toString()).withDptBoosterDate(null).build();
         childService.process(careCase);
         enrollment = getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE);
         assertEquals(MilestoneType.DPTBooster.toString(), enrollment.getCurrentMilestoneName());
         assertEquals(DateUtil.newDateTime(expectedReferenceDate), enrollment.getReferenceDateTime());
         assertEquals(DateUtil.newDateTime(expectedStartDueDate), enrollment.getStartOfDueWindow());
         assertEquals(DateUtil.newDateTime(expectedStartLateDate), enrollment.getStartOfLateWindow());
     }
 
     @Test
     public void shouldVerifyDPTBoosterScheduleFulfillmentWhenDPT4VisitIsOver() {
         LocalDate dob = DateUtil.today();
         LocalDate dpt1Date = dob.plusWeeks(7);
         LocalDate dpt2Date = dob.plusWeeks(11);
         LocalDate dpt3Date = dob.plusMonths(15);
         LocalDate dptBoosterDate = dob.plusMonths(21);
 
         CareCase careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(null).withDpt2Date(null).withDpt3Date(null).withDptBoosterDate(null).build();
         childService.process(careCase);
         markScheduleForUnEnrollment(caseId, scheduleName);
         assertNull(getEnrollmentRecord(scheduleName, caseId, EnrollmentStatus.ACTIVE));
 
         careCase=new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(dpt1Date.toString()).withDpt2Date(dpt2Date.toString()).withDpt3Date(dpt3Date.toString()).withDptBoosterDate(dptBoosterDate.toString()).build();
         childService.process(careCase);
 
         assertNull(trackingService.getEnrollment(caseId, scheduleName));
     }
 }
