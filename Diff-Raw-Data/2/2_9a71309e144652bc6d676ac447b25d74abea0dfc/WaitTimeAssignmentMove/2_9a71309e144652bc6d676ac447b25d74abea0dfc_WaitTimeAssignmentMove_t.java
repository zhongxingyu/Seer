 package org.drools.planner.examples.ras2012.move;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.drools.planner.core.move.Move;
 import org.drools.planner.core.score.director.ScoreDirector;
 import org.drools.planner.examples.ras2012.ProblemSolution;
 import org.drools.planner.examples.ras2012.model.ItineraryAssignment;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.Route;
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.model.WaitTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WaitTimeAssignmentMove implements Move {
 
     private static final Logger logger = LoggerFactory.getLogger(WaitTimeAssignmentMove.class);
     private ItineraryAssignment assignment;
     private final Train         train;
     private final Route         route;
     private final Node          node;
     private final WaitTime      waitTime;
     private WaitTime            previousWaitTime;
 
     public WaitTimeAssignmentMove(final Train t, final Route r, final Node n, final WaitTime wt) {
         this.train = t;
         this.route = r;
         this.node = n;
         this.waitTime = wt;
     }
 
     private WaitTimeAssignmentMove(final Train t, final Route r, final Node n, final WaitTime wt,
             final WaitTime previousWaitTime) {
         this.train = t;
         this.route = r;
         this.node = n;
         this.waitTime = wt;
        this.previousWaitTime = previousWaitTime;
     }
 
     private boolean assignmentExists(final ScoreDirector scoreDirector) {
         return this.getAssignment(scoreDirector) != null;
     }
 
     @Override
     public Move createUndoMove(final ScoreDirector scoreDirector) {
         this.initializeMove(scoreDirector);
         final Move undo = new WaitTimeAssignmentMove(this.train, this.route, this.node,
                 this.previousWaitTime, this.waitTime);
         WaitTimeAssignmentMove.logger.debug("Undo move for {} is {}.", new Object[] { this, undo });
         return undo;
     }
 
     @Override
     public void doMove(final ScoreDirector scoreDirector) {
         this.initializeMove(scoreDirector);
         final ItineraryAssignment ia = this.getAssignment(scoreDirector);
         ia.getItinerary().setWaitTime(this.node, this.waitTime);
         scoreDirector.afterVariableChanged(ia, "waitTime");
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof WaitTimeAssignmentMove)) {
             return false;
         }
         final WaitTimeAssignmentMove rhs = (WaitTimeAssignmentMove) obj;
         return new EqualsBuilder().append(this.route, rhs.route).append(this.train, rhs.train)
                 .append(this.node, rhs.node).append(this.waitTime, rhs.waitTime).isEquals();
     }
 
     private ItineraryAssignment getAssignment(final ScoreDirector scoreDirector) {
         return this.getSolution(scoreDirector).getAssignment(this.train);
     }
 
     @Override
     public Collection<? extends Object> getPlanningEntities() {
         if (this.assignment == null) {
             throw new IllegalStateException("Move not yet initialized!");
         }
         return Collections.singletonList(this.assignment);
     }
 
     @Override
     public Collection<? extends Object> getPlanningValues() {
         return Collections.singletonList(this.waitTime);
     }
 
     private ProblemSolution getSolution(final ScoreDirector scoreDirector) {
         return (ProblemSolution) scoreDirector.getWorkingSolution();
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(this.route).append(this.train).append(this.node)
                 .append(this.waitTime).build();
     }
 
     private ItineraryAssignment initializeMove(final ScoreDirector scoreDirector) {
         this.assignment = this.getAssignment(scoreDirector);
         if (this.previousWaitTime == null) { // if we don't know the previous wait time
             this.previousWaitTime = this.assignment.getItinerary().getWaitTime(this.node);
         }
         return this.assignment;
     }
 
     @Override
     public boolean isMoveDoable(final ScoreDirector scoreDirector) {
         if (!this.assignmentExists(scoreDirector)) {
             return false;
         }
         this.initializeMove(scoreDirector);
         if (this.assignment.getRoute() != this.route) {
             return false;
         }
         if (this.assignment.getItinerary().isNodeOnRoute(this.node)) {
             return this.waitTime != this.previousWaitTime;
         } else {
             return false;
         }
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("WaitTimeAssignmentMove [");
         builder.append(this.train.getName());
         builder.append("@");
         builder.append(this.route.getId());
         builder.append("-");
         builder.append(this.node.getId());
         builder.append(", ");
         if (this.previousWaitTime == null) {
             builder.append("X");
         } else {
             builder.append(this.previousWaitTime.getWaitFor(TimeUnit.MINUTES));
         }
         builder.append("->");
         if (this.waitTime == null) {
             builder.append("X");
         } else {
             builder.append(this.waitTime.getWaitFor(TimeUnit.MINUTES));
         }
         builder.append("]");
         return builder.toString();
     }
 
 }
