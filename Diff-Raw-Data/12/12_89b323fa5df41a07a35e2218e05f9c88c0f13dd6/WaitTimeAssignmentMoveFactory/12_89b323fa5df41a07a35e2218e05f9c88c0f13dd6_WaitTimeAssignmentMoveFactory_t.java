 package org.drools.planner.examples.ras2012.move;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.drools.planner.core.move.Move;
 import org.drools.planner.core.move.factory.AbstractMoveFactory;
 import org.drools.planner.core.solution.Solution;
 import org.drools.planner.examples.ras2012.RAS2012Solution;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.WaitTime;
 import org.drools.planner.examples.ras2012.model.planner.ItineraryAssignment;
 
 public class WaitTimeAssignmentMoveFactory extends AbstractMoveFactory {
 
     @Override
     public List<Move> createMoveList(@SuppressWarnings("rawtypes") final Solution solution) {
         // TODO estimate maximum necessary wait time from the longest arc and slowest train
         final List<Move> moves = new ArrayList<Move>();
         final RAS2012Solution sol = (RAS2012Solution) solution;
         for (final ItineraryAssignment ia : sol.getAssignments()) {
             for (final Node waitPoint : ia.getRoute().getWaitPoints()) {
                 moves.add(new WaitTimeAssignmentMove(ia, waitPoint, null));
                int i = 1;
                for (; i < 10; i++) { // plan to the minute
                     moves.add(new WaitTimeAssignmentMove(ia, waitPoint, WaitTime.getWaitTime(i)));
                 }
                for (; i < (RAS2012Solution.PLANNING_HORIZON_MINUTES / 4); i += 5) {
                    moves.add(new WaitTimeAssignmentMove(ia, waitPoint, WaitTime.getWaitTime(i)));
                }
                for (; i < (2 * (RAS2012Solution.PLANNING_HORIZON_MINUTES / 2)); i += 10) {
                    moves.add(new WaitTimeAssignmentMove(ia, waitPoint, WaitTime.getWaitTime(i)));
                }
                for (; i <= (RAS2012Solution.PLANNING_HORIZON_MINUTES); i += 25) {
                     moves.add(new WaitTimeAssignmentMove(ia, waitPoint, WaitTime.getWaitTime(i)));
                 }
             }
         }
         return moves;
     }
 
 }
