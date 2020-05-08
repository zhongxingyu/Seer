 package edu.northwestern.bioinformatics.studycalendar.service.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
 import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  * @author Rhett Sutphin
  */
 public class AddPeriodMutator extends CollectionAddMutator {
     private ParticipantService participantService;
 
     public AddPeriodMutator(Add change, PeriodDao dao, ParticipantService participantService) {
         super(change, dao);
         this.participantService = participantService;
     }
 
     @Override
     public boolean appliesToExistingSchedules() {
         return true;
     }
 
     @Override
     public void apply(ScheduledCalendar calendar) {
         Period newPeriod = (Period) findChild();
         for (ScheduledArm scheduledArm : findMatchingArms(calendar)) {
             participantService.schedulePeriod(newPeriod,
                 // TODO: make this cast unnecessary
                 (Amendment) change.getDelta().getRevision(), scheduledArm);
         }
     }
 
     private Collection<ScheduledArm> findMatchingArms(ScheduledCalendar cal) {
        return cal.getScheduledArmsFor((Arm) change.getDelta().getNode());
     }
 }
