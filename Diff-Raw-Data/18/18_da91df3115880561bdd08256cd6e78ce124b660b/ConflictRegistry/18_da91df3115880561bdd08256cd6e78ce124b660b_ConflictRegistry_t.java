 package org.drools.planner.examples.ras2012.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.util.model.OccupationTracker;
 
 public class ConflictRegistry {
 
     private static class ConflictRegistryItem {
 
         private final Map<Train, OccupationTracker> occupiedArcsByTrain = new HashMap<Train, OccupationTracker>();
         private final List<OccupationTracker> occupiedArcs = new ArrayList<OccupationTracker>();
 
        public double getConflicts() {
            double conflicts = 0;
             int size = occupiedArcs.size();
             for (int position = 0; position < size; position++) {
                 OccupationTracker left = occupiedArcs.get(position);
                 for (int i = position + 1; i < size; i++) {
                     OccupationTracker right = occupiedArcs.get(i);
                    conflicts += left.getConflictingMileage(right).doubleValue();
                 }
             }
             return conflicts;
         }
 
         public void resetOccupiedArcs(final Train t) {
             OccupationTracker toRemove = occupiedArcsByTrain.get(t);
             if (toRemove != null) {
                 occupiedArcsByTrain.remove(t);
                 occupiedArcs.remove(toRemove);
             }
         }
 
         public void setOccupiedArcs(final Train t, final OccupationTracker arcs) {
             occupiedArcsByTrain.put(t, arcs);
             occupiedArcs.add(arcs);
         }
 
     }
 
     private final Map<Long, ConflictRegistryItem> items;
 
     public ConflictRegistry(final int numberOfItems) {
         items = new HashMap<Long, ConflictRegistryItem>(numberOfItems);
     }
 
     public int countConflicts() {
        double conflicts = 0;
         for (final ConflictRegistryItem item : items.values()) {
            conflicts += item.getConflicts();
         }
        return (int) Math.round(conflicts);
     }
 
     public void resetOccupiedArcs(final Train t) {
         for (final ConflictRegistryItem item : items.values()) {
             item.resetOccupiedArcs(t);
         }
     }
 
     public void setOccupiedArcs(final long time, final Train t, final OccupationTracker occupiedArcs) {
         if (!items.containsKey(time)) {
             items.put(time, new ConflictRegistryItem());
         }
         items.get(time).setOccupiedArcs(t, occupiedArcs);
     }
 
 }
