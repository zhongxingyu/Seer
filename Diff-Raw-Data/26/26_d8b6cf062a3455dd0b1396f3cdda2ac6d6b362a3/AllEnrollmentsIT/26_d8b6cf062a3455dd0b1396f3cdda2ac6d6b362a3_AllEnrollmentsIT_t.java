 package org.motechproject.scheduletracking.api.it;
 
import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.domain.Enrollment;
 import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;
 import org.motechproject.scheduletracking.api.domain.Milestone;
 import org.motechproject.scheduletracking.api.domain.Schedule;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static org.junit.Assert.*;
 import static org.motechproject.scheduletracking.api.utility.PeriodFactory.weeks;
 import static org.motechproject.util.DateUtil.now;
import static org.motechproject.util.DateUtil.today;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:testApplicationSchedulerTrackingAPI.xml")
 public class AllEnrollmentsIT {
     @Autowired
     private AllEnrollments allEnrollments;
 
     private Enrollment enrollment;
     private Milestone milestone;
     private Schedule schedule;
 
     @Before
     public void setUp() {
         milestone = new Milestone("first_milestone", weeks(13), weeks(1), weeks(2), weeks(0));
         schedule = new Schedule("schedule_name");
         schedule.addMilestones(milestone);
     }
 
     @After
     public void tearDown() {
         allEnrollments.remove(enrollment);
     }
 
     @Test
     public void shouldAddEnrollment() {
         enrollment = new Enrollment("externalId", "schedule_name", "first_milestone", now(), now(), new Time(now().toLocalTime()), EnrollmentStatus.Active);
         allEnrollments.add(enrollment);
 
         enrollment = allEnrollments.get(enrollment.getId());
         assertNotNull(enrollment);
         assertEquals(EnrollmentStatus.Active, enrollment.getStatus());
     }
 
     @Test
     public void shouldFindActiveEnrollmentByExternalIdAndScheduleName() {
         enrollment = new Enrollment("entity_1", "schedule_name", "first_milestone", now(), now(), new Time(DateUtil.now().toLocalTime()), EnrollmentStatus.Active);
         enrollment.setStatus(EnrollmentStatus.Unenrolled);
         allEnrollments.add(enrollment);
 
         assertNull(allEnrollments.getActiveEnrollment("entity_1", "schedule_name"));
     }
 
     @Test
    public void shouldConvertTheFulfillmentDateTimeIntoCorrectTimeZoneWhenRetrievingAnEnrollmentWithFulfilledMilestoneFromDatabase() {
        enrollment = new Enrollment("entity_1", "schedule_name", "first_milestone", now(), now(), new Time(DateUtil.now().toLocalTime()), EnrollmentStatus.Active);
        allEnrollments.add(enrollment);
        DateTime fulfillmentDateTime = DateTime.now();
        enrollment.fulfillCurrentMilestone(fulfillmentDateTime);
        allEnrollments.update(enrollment);

        Enrollment enrollmentFromDatabase = allEnrollments.getActiveEnrollment("entity_1", "schedule_name");
        assertEquals(fulfillmentDateTime, enrollmentFromDatabase.lastFulfilledDate());
    }

    @Test
     public void shouldUpdateEnrollmentIfAnActiveEnrollmentForTheScheduleAlreadyAvailable() {
         String externalId = "externalId";
         enrollment = new Enrollment(externalId, schedule.getName(), milestone.getName(), now(), now(), new Time(8, 10), EnrollmentStatus.Active);
         allEnrollments.add(enrollment);
 
         Enrollment enrollmentWithUpdates = new Enrollment(enrollment.getExternalId(), enrollment.getScheduleName(), milestone.getName(), enrollment.getReferenceDateTime().plusDays(1), enrollment.getEnrollmentDateTime().plusDays(1), new Time(2, 5), EnrollmentStatus.Active);
         allEnrollments.addOrReplace(enrollmentWithUpdates);
 
         enrollment = allEnrollments.getActiveEnrollment(enrollment.getExternalId(), schedule.getName());
         assertEquals(enrollmentWithUpdates.getCurrentMilestoneName(), enrollment.getCurrentMilestoneName());
         assertEquals(enrollmentWithUpdates.getReferenceDateTime().toDateTime(DateTimeZone.UTC), enrollment.getReferenceDateTime().toDateTime(DateTimeZone.UTC));
         assertEquals(enrollmentWithUpdates.getEnrollmentDateTime().toDateTime(DateTimeZone.UTC), enrollment.getEnrollmentDateTime().toDateTime(DateTimeZone.UTC));
         assertEquals(enrollmentWithUpdates.getPreferredAlertTime(), enrollment.getPreferredAlertTime());
     }
 
     @Test
     public void shouldCreateEnrollmentIfADefaultedEnrollmentForTheScheduleAlreadyExists() {
         String externalId = "externalId";
         enrollment = new Enrollment(externalId, schedule.getName(), milestone.getName(), now(), now(), new Time(8, 10), EnrollmentStatus.Active);
         enrollment.setStatus(EnrollmentStatus.Defaulted);
         allEnrollments.add(enrollment);
 
         Enrollment enrollmentWithUpdates = new Enrollment(enrollment.getExternalId(), enrollment.getScheduleName(), milestone.getName(), enrollment.getReferenceDateTime().plusDays(1), enrollment.getEnrollmentDateTime().plusDays(1), new Time(2, 5), EnrollmentStatus.Active);
         allEnrollments.addOrReplace(enrollmentWithUpdates);
 
         enrollment = allEnrollments.getActiveEnrollment(enrollment.getExternalId(), schedule.getName());
         assertEquals(enrollmentWithUpdates.getCurrentMilestoneName(), enrollment.getCurrentMilestoneName());
         assertEquals(enrollmentWithUpdates.getReferenceDateTime(), enrollment.getReferenceDateTime());
         assertEquals(enrollmentWithUpdates.getEnrollmentDateTime(), enrollment.getEnrollmentDateTime());
         assertEquals(enrollmentWithUpdates.getPreferredAlertTime(), enrollment.getPreferredAlertTime());
     }
 
     @Test
     public void shouldCreateEnrollmentIfAnUnenrolledEnrollmentForTheScheduleAlreadyExists() {
         String externalId = "externalId";
         enrollment = new Enrollment(externalId, schedule.getName(), milestone.getName(), now(), now(), new Time(8, 10), EnrollmentStatus.Active);
         enrollment.setStatus(EnrollmentStatus.Unenrolled);
         allEnrollments.add(enrollment);
 
         Enrollment enrollmentWithUpdates = new Enrollment(enrollment.getExternalId(), enrollment.getScheduleName(), milestone.getName(), enrollment.getReferenceDateTime().plusDays(1), enrollment.getEnrollmentDateTime().plusDays(1), new Time(2, 5), EnrollmentStatus.Active);
         allEnrollments.addOrReplace(enrollmentWithUpdates);
 
         enrollment = allEnrollments.getActiveEnrollment(enrollment.getExternalId(), schedule.getName());
         assertEquals(enrollmentWithUpdates.getCurrentMilestoneName(), enrollment.getCurrentMilestoneName());
         assertEquals(enrollmentWithUpdates.getReferenceDateTime(), enrollment.getReferenceDateTime());
         assertEquals(enrollmentWithUpdates.getEnrollmentDateTime(), enrollment.getEnrollmentDateTime());
         assertEquals(enrollmentWithUpdates.getPreferredAlertTime(), enrollment.getPreferredAlertTime());
     }
 }
