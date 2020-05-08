 package org.drools.planner.examples.ras2012.move;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import org.drools.planner.core.move.Move;
 import org.drools.planner.core.score.director.ScoreDirector;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.WaitTime;
 import org.drools.planner.examples.ras2012.model.planner.ItineraryAssignment;
 
 public class WaitTimeAssignmentMove implements Move {
 
     private final ItineraryAssignment assignment;
 
     private final Node                node;
 
     private final WaitTime            waitTime, previousWaitTime;
 
     public WaitTimeAssignmentMove(final ItineraryAssignment ia, final Node n, final WaitTime wt) {
         this.assignment = ia;
         this.node = n;
         this.waitTime = wt;
         this.previousWaitTime = ia.getItinerary().getWaitTime(n);
     }
 
     @Override
     public Move createUndoMove(final ScoreDirector scoreDirector) {
         return new WaitTimeAssignmentMove(this.assignment, this.node, this.previousWaitTime);
     }
 
     @Override
     public void doMove(final ScoreDirector scoreDirector) {
         this.assignment.getItinerary().setWaitTime(this.waitTime, this.node);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (this.getClass() != obj.getClass()) {
             return false;
         }
         final WaitTimeAssignmentMove other = (WaitTimeAssignmentMove) obj;
         if (this.assignment == null) {
             if (other.assignment != null) {
                 return false;
             }
         } else if (!this.assignment.equals(other.assignment)) {
             return false;
         }
         if (this.node == null) {
             if (other.node != null) {
                 return false;
             }
         } else if (!this.node.equals(other.node)) {
             return false;
         }
         if (this.waitTime == null) {
             if (other.waitTime != null) {
                 return false;
             }
         } else if (!this.waitTime.equals(other.waitTime)) {
             return false;
         }
         return true;
     }
 
     @Override
     public Collection<? extends Object> getPlanningEntities() {
         return Collections.singletonList(this.assignment);
     }
 
     @Override
     public Collection<? extends Object> getPlanningValues() {
         return Collections.singletonList(this.waitTime);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + (this.assignment == null ? 0 : this.assignment.hashCode());
         result = prime * result + (this.node == null ? 0 : this.node.hashCode());
         result = prime * result + (this.waitTime == null ? 0 : this.waitTime.hashCode());
         return result;
     }
 
     @Override
     public boolean isMoveDoable(final ScoreDirector scoreDirector) {
         if (this.waitTime == null) {
             if (this.previousWaitTime == null) {
                 return false;
             } else {
                 return true;
             }
         } else {
             return !this.waitTime.equals(this.previousWaitTime);
         }
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("WaitTimeAssignmentMove [");
         builder.append(this.assignment.getTrain().getName());
         builder.append("@");
        builder.append(this.assignment.getRoute().getId());
        builder.append("-");
         builder.append(this.node.getId());
         builder.append(", ");
         if (this.previousWaitTime == null) {
             builder.append(0);
         } else {
             builder.append(this.previousWaitTime.getMinutesWaitFor());
         }
         builder.append("->");
         if (this.waitTime == null) {
             builder.append(0);
         } else {
             builder.append(this.waitTime.getMinutesWaitFor());
         }
         builder.append("]");
         return builder.toString();
     }
 
 }
