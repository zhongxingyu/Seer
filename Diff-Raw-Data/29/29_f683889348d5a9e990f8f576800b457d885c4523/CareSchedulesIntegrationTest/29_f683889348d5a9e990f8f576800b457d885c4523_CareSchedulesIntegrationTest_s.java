 package org.motechproject.care.integration.schedule;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.Period;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.motechproject.care.schedule.vaccinations.ChildVaccinationSchedule;
 import org.motechproject.care.schedule.vaccinations.ExpirySchedule;
 import org.motechproject.care.schedule.vaccinations.MotherVaccinationSchedule;
 import org.motechproject.care.service.util.PeriodUtil;
 import org.motechproject.care.utils.SpringIntegrationTest;
 import org.motechproject.delivery.schedule.util.FakeSchedule;
 import org.motechproject.delivery.schedule.util.ScheduleVisualization;
 import org.motechproject.delivery.schedule.util.ScheduleWithCapture;
 import org.motechproject.delivery.schedule.util.SetDateAction;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 
 import java.io.File;
 import java.util.Date;
 
 import static org.motechproject.scheduletracking.api.domain.WindowName.*;
 import static org.motechproject.util.DateUtil.newDate;
 
 public class CareSchedulesIntegrationTest extends SpringIntegrationTest {
     private static final int JANUARY = 1;
     private static final int FEBRUARY = 2;
     private static final int MARCH = 3;
     private static final int APRIL = 4;
     private static final int MAY = 5;
     private static final int JUNE = 6;
     private static final int JULY = 7;
     private static final int AUGUST = 8;
     private static final int SEPTEMBER = 9;
     private static final int OCTOBER = 10;
     private static final int NOVEMBER = 11;
     private static final int DECEMBER = 12;
 
     @Autowired
     private ScheduleTrackingService trackingService;
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     private ScheduleWithCapture schedule;
     private ScheduleVisualization visualization;
     private Period scheduleOffset ;
 
     @BeforeClass
     public static void turnOffSpringLogging() {
         Logger logger = Logger.getLogger("org.springframework");
         logger.setLevel(Level.FATAL);
     }
 
     @Before
     public void setUp() throws Exception {
         FakeSchedule fakeSchedule = new FakeSchedule(trackingService, schedulerFactoryBean, new SetDateAction() {
             @Override
             public void setTheDateTo(LocalDate date) {
                 mockCurrentDate(date);
             }
         });
 
         String outputDir = null;
         if (new File("ananya-care-scheduling").exists()) {
             outputDir = "ananya-care-scheduling/doc/schedules/";
         }
         else if (new File("doc").exists()) {
             outputDir = "doc/schedules/";
         }
 
         scheduleOffset = periodUtil.getScheduleOffset();
         visualization = new ScheduleVisualization(fakeSchedule, outputDir);
         schedule = new ScheduleWithCapture(fakeSchedule, visualization);
     }
 
     @Test
     public void shouldProvideAlertsForTetanusToxoidVaccinationAtTheRightTimes() throws Exception {
         Date tt1TakenDate = date(25, JANUARY);
         Date tt2AlertDate = DateUtil.newDate(tt1TakenDate).plusWeeks(4).plus(scheduleOffset).toDate();
 
         schedule.withFulfillmentDates(tt1TakenDate).enrollFor("TT Vaccination", newDate(2012, 1, 1), null);
 
         schedule.assertNoAlerts("TT 1", earliest);
         schedule.assertAlertsStartWith("TT 1", due, date(1, JANUARY));
         schedule.assertNoAlerts("TT 1", late);
         schedule.assertNoAlerts("TT 1", max);
 
         schedule.assertNoAlerts("TT 2", earliest);
         schedule.assertAlerts("TT 2", due, tt2AlertDate);
         schedule.assertNoAlerts("TT 2", late);
         schedule.assertNoAlerts("TT 2", max);
 
         visualization.outputTo("mother-tetanus.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForMeaslesVaccinationAtTheRightTimes() throws Exception {
         LocalDate dob = newDate(2011, 12, 1);
         LocalDate measlesAlertDate = dob.plusMonths(9).plus(scheduleOffset);
         
         schedule.enrollFor(ChildVaccinationSchedule.Measles.getName(), dob, null);
 
         schedule.assertNoAlerts("Measles", earliest);
         schedule.assertAlertsStartWith("Measles", due, measlesAlertDate.toDate());
         schedule.assertNoAlerts("Measles", late);
         schedule.assertNoAlerts("Measles", max);
         visualization.outputTo("child-measles.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForBcgVaccinationAtTheRightTimes() throws Exception {
         LocalDate dob = newDate(2011, 12, 1);
 
         schedule.enrollFor(ChildVaccinationSchedule.Bcg.getName(), dob, null);
 
         schedule.assertNoAlerts("Bcg", earliest);
         schedule.assertAlertsStartWith("Bcg", due, dob.toDate());
         schedule.assertAlertsStartWith("Bcg", late, dob.plusYears(1).toDate());
         schedule.assertNoAlerts("Bcg", max);
         visualization.outputTo("child-bcg.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForVitaVaccinationAtTheRightTimes() throws Exception {
         LocalDate dob = newDate(2011, 12, 1);
         LocalDate vitaAlertDate = dob.plusMonths(9).plus(scheduleOffset);
 
         schedule.enrollFor(ChildVaccinationSchedule.Vita.getName(), dob, null);
 
         schedule.assertNoAlerts("Vita", earliest);
         schedule.assertAlertsStartWith("Vita", due, vitaAlertDate.toDate()); 
         schedule.assertNoAlerts("Vita", late);
         schedule.assertNoAlerts("Vita", max);
         visualization.outputTo("child-vita.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForAncVisitsAtTheRightTimes() throws Exception {
         LocalDate lmp = newDate(2012, 1, 1);
         LocalDate anc1TakenDate = new LocalDate(date(25, JANUARY));
 
         LocalDate anc2TakenDate = new LocalDate(date(11, MARCH));
         LocalDate anc2AlertDate = anc1TakenDate.plusDays(30).plus(scheduleOffset);
 
         LocalDate anc3TakenDate = new LocalDate(date(30, APRIL));
         LocalDate anc3AlertDate = anc2TakenDate.plusDays(30).plus(scheduleOffset);
         schedule.withFulfillmentDates(anc1TakenDate.toDate(), anc2TakenDate.toDate(), anc3TakenDate.toDate()).enrollFor(MotherVaccinationSchedule.Anc.getName(), lmp, null);
 
         schedule.assertNoAlerts("Anc 1", earliest);
         schedule.assertAlertsStartWith("Anc 1", due, lmp.toDate());
         schedule.assertNoAlerts("Anc 1", late);
         schedule.assertNoAlerts("Anc 1", max);
 
         schedule.assertNoAlerts("Anc 2", earliest);
         schedule.assertAlerts("Anc 2", due, anc2AlertDate.toDate());
         schedule.assertNoAlerts("Anc 2", late);
         schedule.assertNoAlerts("Anc 2", max);
 
         schedule.assertNoAlerts("Anc 3", earliest);
         schedule.assertAlerts("Anc 3", due, anc3AlertDate.toDate());
         schedule.assertNoAlerts("Anc 3", late);
         schedule.assertNoAlerts("Anc 3", max);
 
         visualization.outputTo("mother-anc.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForAnc4VisitsAtTheRightTimes() throws Exception {
         schedule.withFulfillmentDates(date(25, FEBRUARY)).enrollFor(MotherVaccinationSchedule.Anc4.getName(), newDate(2012, 2, 1), null);
 
         schedule.assertNoAlerts("Anc 4", earliest);
         schedule.assertAlerts("Anc 4", due, date(1, FEBRUARY));
         schedule.assertNoAlerts("Anc 4", late);
         schedule.assertNoAlerts("Anc 4", max);
         visualization.outputTo("mother-anc4.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForHepatitis0VaccinationOnlyOnTheBabyDOB() throws Exception {
         LocalDate dob = DateUtil.today();
 
         schedule.enrollFor(ChildVaccinationSchedule.Hepatitis0.getName(), dob, null);
 
         schedule.assertNoAlerts("Hep 0", earliest);
         schedule.assertAlertsStartWith("Hep 0", due, dob.toDate());
         schedule.assertAlertsStartWith("Hep 0", late, dob.plusDays(1).toDate());
         schedule.assertNoAlerts("Hep 0", max);
         visualization.outputTo("child-hepatitis0.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForHepVisitsAtTheRightTimes() throws Exception {
         LocalDate dob = newDate(2012, 1, 1);
 
         LocalDate hep1AlertDate = dob.plusWeeks(6).plus(scheduleOffset);
         LocalDate hep1TakenDate = new LocalDate(date(22, FEBRUARY));
 
         LocalDate hep2AlertDate = hep1TakenDate.plusWeeks(4).plus(scheduleOffset);
         LocalDate hep2TakenDate = new LocalDate(date(18, APRIL));
 
         LocalDate hep3AlertDate = hep2TakenDate.plusWeeks(4).plus(scheduleOffset);
         LocalDate hep3TakenDate = new LocalDate(date(30, APRIL));
 
         schedule.withFulfillmentDates(hep1TakenDate.toDate(), hep2TakenDate.toDate(), hep3TakenDate.toDate()).enrollFor(ChildVaccinationSchedule.Hepatitis.getName(), dob, null);
 
         schedule.assertNoAlerts("Hep 1", earliest);
         schedule.assertAlertsStartWith("Hep 1", due, hep1AlertDate.toDate());
         schedule.assertNoAlerts("Hep 1", late);
         schedule.assertNoAlerts("Hep 1", max);
 
         schedule.assertNoAlerts("Hep 2", earliest);
         schedule.assertAlerts("Hep 2", due, hep2AlertDate.toDate());
         schedule.assertNoAlerts("Hep 2", late);
         schedule.assertNoAlerts("Hep 2", max);
 
         schedule.assertNoAlerts("Hep 3", earliest);
         schedule.assertAlerts("Hep 3", due, hep3AlertDate.toDate());
         schedule.assertNoAlerts("Hep 3", late);
         schedule.assertNoAlerts("Hep 3", max);
 
         visualization.outputTo("child-hepatitis.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForDptVisitsAtTheRightTimes() throws Exception {
         LocalDate dob = newDate(2012, 1, 1);
 
         LocalDate dpt1AlertDate = dob.plusWeeks(6).plus(scheduleOffset);
         LocalDate dpt1TakenDate = new LocalDate(date(12, FEBRUARY));
 
         LocalDate dpt2AlertDate = dpt1TakenDate.plusWeeks(4).plus(scheduleOffset);
         LocalDate dpt2TakenDate = new LocalDate(date(11, MARCH));
 
         LocalDate dpt3AlertDate = dpt2TakenDate.plusWeeks(4).plus(scheduleOffset);
         LocalDate dpt3TakenDate = new LocalDate(date(8, APRIL));
 
         schedule.withFulfillmentDates(dpt1TakenDate.toDate(), dpt2TakenDate.toDate(), dpt3TakenDate.toDate()).enrollFor(ChildVaccinationSchedule.DPT.getName(), dob, null);
 
         schedule.assertNoAlerts("DPT 1", earliest);
         schedule.assertAlertsStartWith("DPT 1", due, dpt1AlertDate.toDate());
         schedule.assertNoAlerts("DPT 1", late);
         schedule.assertNoAlerts("DPT 1", max);
 
         schedule.assertNoAlerts("DPT 2", earliest);
         schedule.assertAlerts("DPT 2", due, dpt2AlertDate.toDate());
         schedule.assertNoAlerts("DPT 2", late);
         schedule.assertNoAlerts("DPT 2", max);
 
         schedule.assertNoAlerts("DPT 3", earliest);
         schedule.assertAlerts("DPT 3", due, dpt3AlertDate.toDate());
         schedule.assertNoAlerts("DPT 3", late);
         schedule.assertNoAlerts("DPT 3", max);
        
         schedule.assertNoAlerts("DPT Booster", earliest);
        schedule.assertAlerts("DPT Booster", due, date(21, SEPTEMBER));
         schedule.assertNoAlerts("DPT Booster", late);
         schedule.assertNoAlerts("DPT Booster", max);

        visualization.outputTo("child-dpt.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForOPV0VaccinationOnlyOnTheBabyDOBAndExpiryAlert() throws Exception {
         LocalDate dob = DateUtil.today();
 
         schedule.enrollFor(ChildVaccinationSchedule.OPV0.getName(), dob, null);
 
         schedule.assertNoAlerts("OPV 0", earliest);
         schedule.assertAlertsStartWith("OPV 0", due, dob.toDate());
         schedule.assertAlertsStartWith("OPV 0", late, dob.plusDays(15).toDate());
         schedule.assertNoAlerts("OPV 0", max);
         visualization.outputTo("child-opv0.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForOPVVaccinationsAtTheRightTimes() throws Exception {
         LocalDate dob = newDate(2012, 1, 1);
 
         LocalDate opv1AlertDate = dob.plusWeeks(6).plus(scheduleOffset);
         LocalDate opv1TakenDate = new LocalDate(date(22, FEBRUARY));
 
         LocalDate opv2AlertDate = opv1TakenDate.plusWeeks(4).plus(scheduleOffset);
         LocalDate opv2TakenDate = new LocalDate(date(18, APRIL));
 
         LocalDate opv3AlertDate = opv2TakenDate.plusWeeks(4).plus(scheduleOffset);
         LocalDate opv3TakenDate = new LocalDate(date(30, APRIL));
 
         schedule.withFulfillmentDates(opv1TakenDate.toDate(), opv2TakenDate.toDate(), opv3TakenDate.toDate()).enrollFor(ChildVaccinationSchedule.OPV.getName(), dob, null);
 
         schedule.assertNoAlerts("OPV 1", earliest);
         schedule.assertAlertsStartWith("OPV 1", due, opv1AlertDate.toDate());
         schedule.assertNoAlerts("OPV 1", late);
         schedule.assertNoAlerts("OPV 1", max);
 
         schedule.assertNoAlerts("OPV 2", earliest);
         schedule.assertAlerts("OPV 2", due, opv2AlertDate.toDate());
         schedule.assertNoAlerts("OPV 2", late);
         schedule.assertNoAlerts("OPV 2", max);
 
         schedule.assertNoAlerts("OPV 3", earliest);
         schedule.assertAlerts("OPV 3", due, opv3AlertDate.toDate());
         schedule.assertNoAlerts("OPV 3", late);
         schedule.assertNoAlerts("OPV 3", max);
 
         visualization.outputTo("child-opv.html", 2);
     }
 
     @Test
     public void shouldProvideAlertsForOPVBoosterVisitsAtTheRightTimes() throws Exception {
         schedule.enrollFor(ChildVaccinationSchedule.OPVBooster.getName(), newDate(2012, 2, 1), null);
 
         schedule.assertNoAlerts("OPV Booster", earliest);
         schedule.assertAlerts("OPV Booster", due, date(1, FEBRUARY));
         schedule.assertNoAlerts("OPV Booster", late);
         schedule.assertNoAlerts("OPV Booster", max);
         visualization.outputTo("child-opvbooster.html", 2);
     }
 
 
     @Test
     public void shouldProvideAlertsForTTBoosterVaccinationForMother() throws Exception {
         LocalDate today = DateUtil.today();
 
         schedule.enrollFor(MotherVaccinationSchedule.TTBooster.getName(), today, null);
 
         schedule.assertNoAlerts("TT Booster", earliest);
         schedule.assertAlertsStartWith("TT Booster", due, today.toDate());
         schedule.assertNoAlerts("TT Booster", late);
         schedule.assertNoAlerts("TT Booster", max);
         visualization.outputTo("child-tt-booster.html", 2);
     }
 
     @Test
     public void shouldProvideAnAlertWhenChildTurns2Years() throws Exception {
         LocalDate dob = DateUtil.today();
 
         schedule.enrollFor(ExpirySchedule.ChildCare.getName(), dob, null);
 
         schedule.assertNoAlerts("Child Care", earliest);
         schedule.assertNoAlerts("Child Care", due);
         schedule.assertAlertsStartWith("Child Care", late, dob.plusMonths(24).toDate());
         schedule.assertNoAlerts("Child Care", max);
         visualization.outputTo("child-care.html", 2);
     }
 
     @Test
     public void shouldProvideAnAlertWhenMothersEddIsComplete() throws Exception {
         LocalDate lmp = DateUtil.today();
 
         schedule.enrollFor(ExpirySchedule.MotherCare.getName(), lmp, null);
 
         schedule.assertNoAlerts("Mother Care", earliest);
         schedule.assertNoAlerts("Mother Care", due);
         schedule.assertAlertsStartWith("Mother Care", late, lmp.plusDays(PeriodUtil.DAYS_IN_9_MONTHS).toDate());
         schedule.assertNoAlerts("Mother Care", max);
         visualization.outputTo("mother-care.html", 2);
     }
 
     private Date date(int day, int month) {
         return dateWithYear(day, month, 2012);
     }
 
     private Date dateWithYear(int day, int month, int year) {
         return new DateTime(year, month, day, 0, 0).toDate();
     }
 }
