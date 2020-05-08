 package org.optaplanner.examples.projectscheduling.solver.move.gapremover;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.math.IntRange;
 import org.optaplanner.core.impl.move.Move;
 import org.optaplanner.core.impl.score.director.ScoreDirector;
 import org.optaplanner.examples.projectscheduling.domain.Allocation;
 import org.optaplanner.examples.projectscheduling.domain.ProjectSchedule;
 import org.optaplanner.examples.projectscheduling.solver.move.StartDateUndoMove;
 
 public class GapRemovingMove implements Move {
 
     private final IntRange gap;
     private final Map<Allocation, Integer> newDates = new LinkedHashMap<Allocation, Integer>();
 
     @Override
     public String toString() {
         return "GapRemovingMove [gap=" + this.gap + "]";
     }
     
     public GapRemovingMove() {
         this.gap = null;
     }
 
     public GapRemovingMove(final ProjectSchedule schedule, final IntRange gap) {
         this.gap = gap;
        final int gapSize = gap.getMaximumInteger() - gap.getMinimumInteger();
         if (gapSize < 1) {
             return;
         }
         for (final Allocation a : schedule.getAllocations()) {
             if (a.getStartDate() > gap.getMaximumInteger()) {
                 this.newDates.put(a, a.getStartDate() - gapSize);
             }
         }
     }
 
     @Override
     public boolean isMoveDoable(final ScoreDirector scoreDirector) {
         return this.newDates.size() > 0;
     }
 
     @Override
     public Move createUndoMove(final ScoreDirector scoreDirector) {
         final Map<Allocation, Integer> oldDates = new LinkedHashMap<Allocation, Integer>();
         for (final Map.Entry<Allocation, Integer> entry : this.newDates.entrySet()) {
             oldDates.put(entry.getKey(), entry.getKey().getStartDate());
         }
         return new StartDateUndoMove(Collections.unmodifiableMap(oldDates));
     }
 
     @Override
     public void doMove(final ScoreDirector scoreDirector) {
         for (final Allocation a : this.newDates.keySet()) {
             scoreDirector.beforeVariableChanged(a, "startDate");
             a.setStartDate(this.newDates.get(a));
             scoreDirector.afterVariableChanged(a, "startDate");
         }
     }
 
     @Override
     public Collection<? extends Object> getPlanningEntities() {
         return Collections.unmodifiableSet(this.newDates.keySet());
     }
 
     @Override
     public Collection<? extends Object> getPlanningValues() {
         return Collections.unmodifiableCollection(this.newDates.values());
     }
 
 }
