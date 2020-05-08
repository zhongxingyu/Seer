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
 import org.motechproject.care.service.schedule.DptService;
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
 
 public class DptIntegrationTest extends SpringIntegrationTest {
 
     @Autowired
     private DptService dptService;
     @Autowired
     private AllChildren allChildren;
     private String dptScheduleName = ChildVaccinationSchedule.DPT.getName();
     private String caseId;
     private ChildService childService;
 
     @Before
     public void setUp() {
         caseId = CaseUtils.getUniqueCaseId();
         List<VaccinationService> vaccinationServices = Arrays.asList((VaccinationService) dptService);
         ChildVaccinationProcessor childVaccinationProcessor = new ChildVaccinationProcessor(vaccinationServices);
         childService = new ChildService(allChildren, childVaccinationProcessor);
     }
 
     @After
     public void tearDown() {
         allChildren.removeAll();
     }
 
     @Test
     public void shouldVerifyDptScheduleCreationWhenChildIsRegistered() {
         LocalDate dob = DateUtil.today().plusMonths(4);
 
         CareCase careCase = new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(null).withDpt2Date(null).build();
         childService.process(careCase);
         markScheduleForUnEnrollment(caseId, dptScheduleName);
         EnrollmentRecord enrollment = getEnrollmentRecord(dptScheduleName, caseId, EnrollmentStatus.ACTIVE);
 
         assertEquals(MilestoneType.DPT1.toString(), enrollment.getCurrentMilestoneName());
         assertEquals(DateUtil.newDateTime(dob), enrollment.getReferenceDateTime());
         assertEquals(DateUtil.newDateTime(dob.plusWeeks(6)), enrollment.getStartOfDueWindow());
         assertEquals(DateUtil.newDateTime(dob.plusMonths(24)), enrollment.getStartOfLateWindow());
     }
 
     @Test
     public void shouldVerifyDpt1ScheduleFulfillmentWhenDpt1VaccineIsOver() {
         LocalDate dob = DateUtil.today().minusMonths(4);
         LocalDate dpt1Date = dob.plusMonths(2);
 
         CareCase careCase = new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(dpt1Date.toString()).withDpt2Date(null).build();
         childService.process(careCase);
 
         markScheduleForUnEnrollment(caseId, dptScheduleName);
         EnrollmentRecord enrollment = getEnrollmentRecord(dptScheduleName, caseId, EnrollmentStatus.ACTIVE);
 
         assertEquals(MilestoneType.DPT2.toString(), enrollment.getCurrentMilestoneName());
         assertEquals(DateUtil.newDateTime(dpt1Date.plusWeeks(4)), enrollment.getStartOfDueWindow());
         assertEquals(DateUtil.newDateTime(dpt1Date.plusMonths(24)), enrollment.getStartOfLateWindow());
     }
 
     @Test
     public void shouldVerifyDpt2ScheduleFulfillmentWhenDpt2VaccineIsOver() {
         LocalDate dob = DateUtil.today().minusMonths(6);
         LocalDate dpt1Date = dob.plusMonths(2);
         LocalDate dpt2Date = dob.plusMonths(3);
 
         CareCase careCase = new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(null).withDpt2Date(null).withDpt3Date(null).build();
         childService.process(careCase);
         careCase = new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(dpt1Date.toString()).withDpt2Date(null).withDpt3Date(null).build();
         childService.process(careCase);
         careCase = new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString()).withDpt1Date(dpt1Date.toString()).withDpt2Date(dpt2Date.toString()).withDpt3Date(null).build();
         childService.process(careCase);
 
         markScheduleForUnEnrollment(caseId, dptScheduleName);
         EnrollmentRecord enrollment = getEnrollmentRecord(dptScheduleName, caseId, EnrollmentStatus.ACTIVE);
 
         assertEquals(MilestoneType.DPT3.toString(), enrollment.getCurrentMilestoneName());
         assertEquals(DateUtil.newDateTime(dpt2Date.plusWeeks(4)), enrollment.getStartOfDueWindow());
         assertEquals(DateUtil.newDateTime(dpt2Date.plusMonths(24)), enrollment.getStartOfLateWindow());
     }
 
     @Test
     public void shouldVerifyDpt3ScheduleFulfillmentWhenDpt3VaccineIsOver() {
         LocalDate today = DateUtil.today();
         LocalDate dob = today.minusMonths(6);
         LocalDate dpt1Date = dob.plusMonths(2);
         LocalDate dpt2Date = dob.plusMonths(3);
         LocalDate dpt3Date = today;
 
         CareCase careCase = new ChildCareCaseBuilder().withCaseId(caseId).withDOB(dob.toString())
                 .withDpt1Date(dpt1Date.toString()).withDpt2Date(dpt2Date.toString()).withDpt3Date(dpt3Date.toString()).withDptBoosterDate(null).build();
         childService.process(careCase);
 
        markScheduleForUnEnrollment(caseId, dptScheduleName);
        EnrollmentRecord enrollment = getEnrollmentRecord(dptScheduleName, caseId, EnrollmentStatus.ACTIVE);
        assertEquals(MilestoneType.DPTBooster.toString(), enrollment.getCurrentMilestoneName());
        assertEquals(DateUtil.newDateTime(dpt3Date.plusDays(180)), enrollment.getStartOfDueWindow());
        assertEquals(DateUtil.newDateTime(dpt3Date.plusMonths(24)), enrollment.getStartOfLateWindow());
     }
 }
