 package edu.northwestern.bioinformatics.studycalendar.service.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 
 /**
  * @author Rhett Sutphin
  */
 public class AddPlannedEventMutator extends ListAddMutator {
     private ParticipantService participantService;
     private TemplateService templateService;
 
     public AddPlannedEventMutator(
         Add change, PlannedEventDao dao,
         ParticipantService participantService, TemplateService templateService
     ) {
         super(change, dao);
         this.participantService = participantService;
         this.templateService = templateService;
     }
 
     @Override
     public boolean appliesToExistingSchedules() {
         return true;
     }
 
     @Override
     public void apply(ScheduledCalendar calendar) {
         PlannedEvent event = (PlannedEvent) findChild();
        // Second cast works around a dumb javac bug
        Period period = (Period) (PlanTreeNode) change.getDelta().getNode();
         Arm arm = templateService.findParent(period);
 
         for (ScheduledArm scheduledArm : calendar.getScheduledArmsFor(arm)) {
             participantService.schedulePlannedEvent(event, period, (Amendment) change.getDelta().getRevision(), scheduledArm);
         }
     }
 }
