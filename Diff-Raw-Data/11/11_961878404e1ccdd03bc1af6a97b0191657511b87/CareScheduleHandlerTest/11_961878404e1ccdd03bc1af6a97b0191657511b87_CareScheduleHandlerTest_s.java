 package org.motechproject.ghana.national.handler;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.motechproject.ghana.national.vo.Pregnancy;
 import org.motechproject.scheduletracking.api.domain.Milestone;
 import org.motechproject.scheduletracking.api.domain.MilestoneAlert;
 import org.motechproject.scheduletracking.api.events.MilestoneEvent;
 import org.motechproject.util.DateUtil;
import org.motechproject.valueobjects.WallTime;
 
 import static org.mockito.Mockito.*;
import static org.motechproject.ghana.national.handler.CareScheduleHandler.*;
import static org.motechproject.valueobjects.WallTimeUnit.Week;
 
 public class CareScheduleHandlerTest {
 
     private CareScheduleHandler careScheduleHandlerSpy;
 
     @Before
     public void setUp() throws Exception {
         careScheduleHandlerSpy = spy(new CareScheduleHandler());
     }
 
     @Test
     public void handlePregnancyAlert() {
         doNothing().when(careScheduleHandlerSpy).sendSMSToFacility(Matchers.<String>any(), Matchers.<MilestoneEvent>any(), Matchers.<LocalDate>any());
 
         LocalDate conceptionDate = DateUtil.newDate(2000, 11, 11);
         final MilestoneEvent milestoneEvent = new MilestoneEvent(null, null, null, null, conceptionDate);
 
         careScheduleHandlerSpy.handlePregnancyAlert(milestoneEvent);
 
         verify(careScheduleHandlerSpy).sendSMSToFacility(PREGNANCY_ALERT_SMS_KEY, milestoneEvent, Pregnancy.basedOnConceptionDate(conceptionDate).dateOfDelivery());
     }
 
     @Test
     public void handleTTVaccinationAlert() {
         doNothing().when(careScheduleHandlerSpy).sendSMSToFacility(Matchers.<String>any(), Matchers.<MilestoneEvent>any(), Matchers.<LocalDate>any());
 
         LocalDate referenceDate = DateUtil.newDate(2012, 2, 1);
        Milestone milestone = new Milestone("M1", new WallTime(0, Week), new WallTime(3, Week), new WallTime(6, Week), new WallTime(7, Week));
         final MilestoneEvent milestoneEvent = new MilestoneEvent(null, null, MilestoneAlert.fromMilestone(milestone, referenceDate), null, referenceDate);
 
         careScheduleHandlerSpy.handleTTVaccinationAlert(milestoneEvent);
 
         verify(careScheduleHandlerSpy).sendSMSToFacility(TT_VACCINATION_SMS_KEY, milestoneEvent, DateUtil.newDate(2012, 2, 29));
     }
 }
