 package org.drools.planner.examples.ras2012.move;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 
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
 
 public class RouteReassignmentUndoMove implements Move {
 
     private ItineraryAssignment       assignment;
     private final Train               train;
     private final Map<Node, WaitTime> originalWaitTimes;
     private final Route               originalRoute, routeToUndo;
 
     public RouteReassignmentUndoMove(final Train train, final Route routeToUndo,
             final Route originalRoute, final Map<Node, WaitTime> originalWaitTimes) {
         this.train = train;
         this.originalRoute = originalRoute;
         this.routeToUndo = routeToUndo;
         this.originalWaitTimes = originalWaitTimes;
     }
 
     @Override
     public Move createUndoMove(final ScoreDirector scoreDirector) {
         // this is just an undo move; no need to undo an undo
         return null;
     }
 
     @Override
     public void doMove(final ScoreDirector scoreDirector) {
         this.initializeMove(scoreDirector);
         this.assignment.setRoute(this.originalRoute);
         for (final Map.Entry<Node, WaitTime> entry : this.originalWaitTimes.entrySet()) {
            this.assignment.getItinerary().setWaitTime(entry.getKey(), entry.getValue());
         }
         scoreDirector.afterEntityAdded(this.assignment);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof RouteReassignmentUndoMove)) {
             return false;
         }
         final RouteReassignmentUndoMove other = (RouteReassignmentUndoMove) obj;
         return new EqualsBuilder().append(this.routeToUndo, other.routeToUndo)
                 .append(this.train, other.train).isEquals();
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
         return Collections.singletonList(this.routeToUndo);
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(this.routeToUndo).append(this.train).build();
     }
 
     private ItineraryAssignment initializeMove(final ScoreDirector scoreDirector) {
         this.assignment = ((ProblemSolution) scoreDirector.getWorkingSolution())
                 .getAssignment(this.train);
         return this.assignment;
     }
 
     @Override
     public boolean isMoveDoable(final ScoreDirector scoreDirector) {
         // this is just an undo move; it should always be doable
         return true;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("RouteReassignmentUndoMove [train=");
         builder.append(this.train.getName());
         builder.append(", ");
         builder.append(this.routeToUndo.getId());
         builder.append(" -> ");
         builder.append(this.originalRoute.getId());
         builder.append("]");
         return builder.toString();
     }
 
 }
