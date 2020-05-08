 package org.drools.planner.examples.ras2012.util;
 
import java.math.BigDecimal;
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
 
        public BigDecimal getConflicts() {
            BigDecimal conflicts = BigDecimal.ZERO;
             int size = occupiedArcs.size();
             for (int position = 0; position < size; position++) {
                 OccupationTracker left = occupiedArcs.get(position);
                 for (int i = position + 1; i < size; i++) {
                     OccupationTracker right = occupiedArcs.get(i);
                    conflicts = conflicts.add(left.getConflictingMileage(right));
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
        BigDecimal conflicts = BigDecimal.ZERO;
         for (final ConflictRegistryItem item : items.values()) {
            conflicts = conflicts.add(item.getConflicts());
         }
        return conflicts.setScale(0, BigDecimal.ROUND_HALF_EVEN).intValue();
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
