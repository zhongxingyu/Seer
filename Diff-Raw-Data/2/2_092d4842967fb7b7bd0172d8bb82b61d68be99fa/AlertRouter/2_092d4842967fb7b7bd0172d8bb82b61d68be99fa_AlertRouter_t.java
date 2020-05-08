 package org.motechproject.care.service.router;
 
 import org.apache.log4j.Logger;
 import org.motechproject.care.schedule.vaccinations.ChildVaccinationSchedule;
 import org.motechproject.care.schedule.vaccinations.ExpirySchedule;
 import org.motechproject.care.schedule.vaccinations.MotherVaccinationSchedule;
 import org.motechproject.care.service.router.action.*;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.scheduletracking.api.domain.MilestoneAlert;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.events.MilestoneEvent;
 import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.motechproject.care.service.router.Matcher.*;
 
 @Component
 public class AlertRouter {
     private List<Route> routes;
     Logger logger = Logger.getLogger(AlertRouter.class);
 
     @Autowired
     public AlertRouter(AlertChildAction alertChildAction
             , AlertMotherAction alertMotherAction
             , ClientExpiryAction expiryAction, Hep0ExpiryAction hep0ExpiryAction, Opv0ExpiryAction opv0ExpiryAction, BcgExpiryAction bcgExpiryAction) {
         routes = new ArrayList<Route>();
         routes.add(new Route(eq(ChildVaccinationSchedule.Hepatitis0.getName()), any(), eq(WindowName.late.name()), hep0ExpiryAction));
         routes.add(new Route(eq(ChildVaccinationSchedule.OPV0.getName()), any(), eq(WindowName.late.name()), opv0ExpiryAction));
         routes.add(new Route(eq(ChildVaccinationSchedule.Bcg.getName()), any(), eq(WindowName.late.name()), bcgExpiryAction));
         routes.add(new Route(childSchedules(), any(), any(), alertChildAction));
         routes.add(new Route(motherSchedules(), any(), any(), alertMotherAction));
         routes.add(new Route(expirySchedules(), any(), any(), expiryAction));
     }
 
     @MotechListener(subjects = {EventSubjects.MILESTONE_ALERT})
     public void handle(MotechEvent realEvent) {
         MilestoneEvent event = new MilestoneEvent(realEvent);
         MilestoneAlert milestoneAlert = event.getMilestoneAlert();
        logger.info( String.format("Received alert -- ScheduleName: %s, MilestoneName: %s, WindowName: %s, ExternalId: %s", event.getScheduleName(), milestoneAlert.getMilestoneName(), event.getWindowName(), event.getExternalId()));
 
         for (Route route : routes) {
             if (route.isSatisfiedBy(event.getScheduleName(), milestoneAlert.getMilestoneName(), event.getWindowName())) {
                 route.invokeAction(event);
                 return;
             }
         }
         throw new NoRoutesMatchException();
     }
 
     private Matcher childSchedules() {
         ArrayList<String> childVaccines = new ArrayList<String>();
         for (ChildVaccinationSchedule b : ChildVaccinationSchedule.values()) {
             childVaccines.add(b.getName());
         }
         return anyOf(childVaccines);
     }
 
     private Matcher motherSchedules() {
         ArrayList<String> motherVaccines = new ArrayList<String>();
         for (MotherVaccinationSchedule b : MotherVaccinationSchedule.values()) {
             motherVaccines.add(b.getName());
         }
         return anyOf(motherVaccines);
     }
 
     private Matcher expirySchedules() {
         ArrayList<String> expiryVaccines = new ArrayList<String>();
         for (ExpirySchedule b : ExpirySchedule.values()) {
             expiryVaccines.add(b.getName());
         }
         return anyOf(expiryVaccines);
     }
 
     private class Route {
         private final Matcher scheduleMatcher;
         private final Matcher milestoneMatcher;
         private final Matcher windowMatcher;
         private final Action action;
 
         public Route(Matcher scheduleMatcher, Matcher milestoneMatcher, Matcher windowMatcher, Action action) {
             this.scheduleMatcher = scheduleMatcher;
             this.milestoneMatcher = milestoneMatcher;
             this.windowMatcher = windowMatcher;
             this.action = action;
         }
 
         public boolean isSatisfiedBy(String scheduleName, String milestoneName, String windowName) {
             return scheduleMatcher.matches(scheduleName) && milestoneMatcher.matches(milestoneName) && windowMatcher.matches(windowName);
         }
 
         public void invokeAction(MilestoneEvent event) {
             action.invoke(event);
         }
     }
 }
