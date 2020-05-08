 package edu.northwestern.bioinformatics.studycalendar.service.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 
 /**
  * @author Rhett Sutphin
  */
 public class RemovePeriodMutator extends RemoveMutator {
     private TemplateService templateService;
 
     public RemovePeriodMutator(Remove remove, PeriodDao dao, TemplateService templateService) {
         super(remove, dao);
         this.templateService = templateService;
     }
 
     @Override
     public boolean appliesToExistingSchedules() {
         return true;
     }
 
     @Override
     public void apply(ScheduledCalendar calendar) {
        // second cast is for silly javac/generics bug
        Arm arm = (Arm) (PlanTreeNode) change.getDelta().getNode();
         Period removedPeriod = (Period) findChild();
         Revision rev = change.getDelta().getRevision();
         for (ScheduledArm scheduledArm : calendar.getScheduledArmsFor(arm)) {
             log.debug("Applying removal of {} to {}", removedPeriod, scheduledArm.getName());
             for (ScheduledEvent se : scheduledArm.getEvents()) {
                 Period period = templateService.findParent(se.getPlannedEvent());
                 if (period.equals(removedPeriod)) {
                     se.unscheduleIfOutstanding("Removed in revision " + rev.getDisplayName());
                 }
             }
         }
     }
 }
