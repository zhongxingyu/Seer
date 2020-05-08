 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import org.springframework.transaction.annotation.Transactional;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
 
 /**
  * Provides methods for calculating deltas and for applying them to PlannedCalendars.
  * Should also provide the methods for applying them to schedules, though I'm not sure
  * about that yet.
  *
  * @author Rhett Sutphin
  */
 @Transactional
 public class DeltaService {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private MutatorFactory mutatorFactory;
 
     /**
      * Amend the given PlannedCalendar according to the deltas contained in the given
      * revision.  This means:
      * <ul>
      *   <li>Apply the deltas to the calendar</li>
      *   <li>Create a new {@link edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment}
      *       containing reversed versions of all the deltas in the revision</li>
      *   <li>Register the amendment in the planned calendar</li>
      *   <li>Save it all</li>
      * </ul>
      */
     public void amend(String amendmentName, PlannedCalendar source, Revision rev) {
         throw new UnsupportedOperationException("TODO");
     }
 
     /**
      * Takes the provided source calendar and rolls it back to the amendment
      */
     public PlannedCalendar getAmendedCalendar(PlannedCalendar source, Amendment target) {
         if (!(source.getAmendment().equals(target) || source.getAmendment().hasPreviousAmendment(target))) {
             throw new StudyCalendarSystemException(
                 "Amendment %s (%s) does not apply to the template for %s (%s)",
                 target.getName(), target.getGridId(), source.getName(), source.getGridId());
         }
 
         PlannedCalendar amended = source.transientClone();
         while (!target.equals(amended.getAmendment())) {
             log.debug("Rolling {} back to {}", source, amended.getAmendment().getPreviousAmendment().getName());
             for (Delta<?> delta : amended.getAmendment().getDeltas()) {
                 PlanTreeNode<?> affected = findEquivalentChild(amended, delta.getNode());
                 if (affected == null) {
                     throw new StudyCalendarSystemException(
                         "Could not find a node in the cloned tree matching the node in delta: %s", delta);
                 }
                 for (Change change : delta.getChanges()) {
                     log.debug("Rolling back change {} on {}", change, affected);
                     mutatorFactory.createMutator(affected, change).revert(affected);
                 }
             }
             amended.setAmendment(amended.getAmendment().getPreviousAmendment());
         }
 
         return amended;
     }
 
     /**
      * Applies all the deltas in the given revision to source calendar,
      * returning a new, transient PlannedCalendar.  The revision might be
      * and in-progress amendment or a customization.
      */
     public PlannedCalendar revise(PlannedCalendar source, Revision revision) {
         PlannedCalendar revised = source.transientClone();
         for (Delta<?> delta : revision.getDeltas()) {
             PlanTreeNode<?> affected = findEquivalentChild(revised, delta.getNode());
             if (affected == null) {
                 throw new StudyCalendarSystemException(
                     "Could not find a node in the cloned tree matching the node in delta: %s", delta);
             }
             for (Change change : delta.getChanges()) {
                log.debug("Rolling back change {} on {}", change, affected);
                 mutatorFactory.createMutator(affected, change).apply(affected);
             }
         }
         return revised;
     }
 
     private PlanTreeNode<?> findEquivalentChild(PlanTreeNode<?> node, PlanTreeNode<?> toMatch) {
         if (isEquivalent(node, toMatch)) return node;
         if (node instanceof PlanTreeInnerNode) {
             for (PlanTreeNode<?> child : ((PlanTreeInnerNode<?, PlanTreeNode<?>, ?>) node).getChildren()) {
                 PlanTreeNode<?> match = findEquivalentChild(child, toMatch);
                 if (match != null) return match;
             }
         }
         return null;
     }
 
     private boolean isEquivalent(PlanTreeNode<?> node, PlanTreeNode<?> toMatch) {
         return toMatch.getClass().equals(node.getClass())
             && toMatch.getId().equals(node.getId());
     }
 
     ////// CONFIGURATION
 
     public void setMutatorFactory(MutatorFactory mutatorFactory) {
         this.mutatorFactory = mutatorFactory;
     }
 }
