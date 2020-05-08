 package org.drools.planner.examples.ras2012;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.concurrent.TimeUnit;
 
 import org.drools.planner.core.score.buildin.hardandsoft.DefaultHardAndSoftScore;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.core.score.director.incremental.AbstractIncrementalScoreCalculator;
 import org.drools.planner.examples.ras2012.model.Itinerary;
 import org.drools.planner.examples.ras2012.model.ItineraryAssignment;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.util.ConflictRegistry;
 import org.drools.planner.examples.ras2012.util.Converter;
 
 public class RAS2012ScoreCalculator extends AbstractIncrementalScoreCalculator<RAS2012Solution> {
 
     private static final int        OCCUPATION_CHECKS_PER_MINUTE = 2;
 
     private static final BigDecimal MILLIS_TO_HOURS              = BigDecimal.valueOf(3600000);
 
     private static BigDecimal roundMillisecondsToHours(final long milliseconds) {
         return BigDecimal.valueOf(milliseconds).divide(RAS2012ScoreCalculator.MILLIS_TO_HOURS,
                 Converter.BIGDECIMAL_SCALE, Converter.BIGDECIMAL_ROUNDING);
     }
 
     private RAS2012Solution           solution                   = null;
 
     private final Map<Train, Boolean> didTrainArrive             = new HashMap<Train, Boolean>();
 
     private final Map<Train, Integer> wantTimePenalties          = new HashMap<Train, Integer>();
 
     private final Map<Train, Integer> delayPenalties             = new HashMap<Train, Integer>();
 
     private final Map<Train, Integer> scheduleAdherencePenalties = new HashMap<Train, Integer>();
 
     private final Map<Train, Integer> unpreferredTracksPenalties = new HashMap<Train, Integer>();
 
     private ConflictRegistry          conflicts;
 
     @Override
     public void afterAllVariablesChanged(final Object entity) {
         if (entity instanceof ItineraryAssignment) {
             this.insert((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void afterEntityAdded(final Object entity) {
         if (entity instanceof ItineraryAssignment) {
             this.insert((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void afterEntityRemoved(final Object entity) {
         // do nothing
     }
 
     @Override
     public void afterVariableChanged(final Object entity, final String variableName) {
         if (entity instanceof ItineraryAssignment) {
             this.insert((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void beforeAllVariablesChanged(final Object entity) {
         if (entity instanceof ItineraryAssignment) {
             this.retract((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void beforeEntityAdded(final Object entity) {
         // do nothing
     }
 
     @Override
     public void beforeEntityRemoved(final Object entity) {
         if (entity instanceof ItineraryAssignment) {
             this.retract((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void beforeVariableChanged(final Object entity, final String variableName) {
         if (entity instanceof ItineraryAssignment) {
             this.retract((ItineraryAssignment) entity);
         }
     }
 
     @Override
    public HardAndSoftScore calculateScore() {
         int penalty = 0;
         for (final Train t : this.solution.getTrains()) {
             penalty += this.wantTimePenalties.containsKey(t) ? this.wantTimePenalties.get(t) : 0;
             penalty += this.delayPenalties.containsKey(t) ? this.delayPenalties.get(t) : 0;
             penalty += this.scheduleAdherencePenalties.containsKey(t) ? this.scheduleAdherencePenalties
                     .get(t) : 0;
             penalty += this.unpreferredTracksPenalties.containsKey(t) ? this.unpreferredTracksPenalties
                     .get(t) : 0;
         }
         final int conflicts = this.conflicts.countConflicts();
         if (conflicts > 0) {
             return DefaultHardAndSoftScore.valueOf(-conflicts, -penalty);
         } else {
             return DefaultHardAndSoftScore.valueOf(this.getTrainArrivalMetrics(), -penalty);
         }
     }
 
     private void clearEveryCache() {
         this.wantTimePenalties.clear();
         this.unpreferredTracksPenalties.clear();
         this.scheduleAdherencePenalties.clear();
         this.delayPenalties.clear();
         this.didTrainArrive.clear();
         this.conflicts = new ConflictRegistry(
                 (int) this.solution.getPlanningHorizon(TimeUnit.MINUTES)
                         * RAS2012ScoreCalculator.OCCUPATION_CHECKS_PER_MINUTE);
     }
 
     private boolean didTrainArrive(final Itinerary producer) {
         final SortedMap<Long, Node> nodes = producer.getSchedule().headMap(
                 this.solution.getPlanningHorizon(TimeUnit.MILLISECONDS) + 1);
         return nodes.values().contains(producer.getTrain().getDestination());
     }
 
     private int getDelayPenalty(final Itinerary i, final RAS2012Solution solution) {
         final long delay = i.getDelay();
         if (delay <= 0) {
             return 0;
         }
         final BigDecimal hoursDelay = RAS2012ScoreCalculator.roundMillisecondsToHours(delay);
         final BigDecimal maxHoursDelay = hoursDelay.max(BigDecimal.ZERO);
         return maxHoursDelay.multiply(BigDecimal.valueOf(i.getTrain().getType().getDelayPenalty()))
                 .intValue();
     }
 
     private int getScheduleAdherencePenalty(final Itinerary i) {
         int penalty = 0;
         if (i.getTrain().getType().adhereToSchedule()) {
             for (final Map.Entry<Node, Long> arrivals : i.getArrivalsAtSANodes().entrySet()) {
                 final Node node = arrivals.getKey();
                 final long arrival = arrivals.getValue();
                 if (!this.isInPlanningHorizon(arrival)) {
                     // doesn't count when it's outside the horizon
                     continue;
                 }
                 final long expectedArrival = i.getTrain().getScheduleAdherenceRequirements()
                         .get(node).getTimeSinceStartOfWorld(TimeUnit.MILLISECONDS);
                 if (arrival <= expectedArrival) {
                     // doesn't count when we're ahead
                     continue;
                 }
                 final long difference = arrival - expectedArrival;
                 BigDecimal hourlyDifference = RAS2012ScoreCalculator
                         .roundMillisecondsToHours(difference);
                 hourlyDifference = hourlyDifference.subtract(BigDecimal.valueOf(2));
                 if (hourlyDifference.signum() > 0) {
                     penalty += hourlyDifference.multiply(BigDecimal.valueOf(200)).intValue();
                 }
             }
         }
         return penalty;
     }
 
     private int getTrainArrivalMetrics() {
         int actual = 0, full = 0;
         for (final Map.Entry<Train, Boolean> entry : this.didTrainArrive.entrySet()) {
             final int trainValue = (int) Math
                     .ceil(entry.getKey().getType().getDelayPenalty() / 100.0);
             full += trainValue;
             if (entry.getValue()) {
                 actual += trainValue;
             }
         }
         return actual * 100 / full;
     }
 
     private int getUnpreferredTracksPenalty(final Itinerary i) {
         final BigDecimal hours = RAS2012ScoreCalculator.roundMillisecondsToHours(i
                 .getTimeSpentOnUnpreferredTracks(this.solution
                         .getPlanningHorizon(TimeUnit.MILLISECONDS)));
         return hours.multiply(BigDecimal.valueOf(50)).intValue();
     }
 
     private int getWantTimePenalty(final Itinerary i) {
         final long actualTime = i.getArrivalTime();
         if (!this.isInPlanningHorizon(actualTime)) {
             // arrivals outside of the planning horizon aren't counted
             return 0;
         }
         final long delay = actualTime - i.getTrain().getWantTime(TimeUnit.MILLISECONDS);
         BigDecimal hours = RAS2012ScoreCalculator.roundMillisecondsToHours(delay);
         final BigDecimal penalty = BigDecimal.valueOf(75);
         if (delay > 0) {
             hours = hours.subtract(BigDecimal.valueOf(3));
             if (hours.signum() > 0) {
                 return hours.multiply(penalty).intValue();
             }
         } else if (delay < 0) {
             hours = hours.add(BigDecimal.valueOf(1));
             if (hours.signum() < 0) {
                 return -hours.multiply(penalty).intValue();
             }
         }
         return 0;
     }
 
     private void insert(final ItineraryAssignment ia) {
         final Train t = ia.getTrain();
         final Itinerary i = ia.getItinerary();
         this.unpreferredTracksPenalties.put(t, this.getUnpreferredTracksPenalty(i));
         this.scheduleAdherencePenalties.put(t, this.getScheduleAdherencePenalty(i));
         this.didTrainArrive.put(t, this.didTrainArrive(i));
         this.wantTimePenalties.put(t, this.getWantTimePenalty(i));
         this.delayPenalties.put(t, this.getDelayPenalty(i, this.solution));
         this.recalculateOccupiedArcs(ia);
     }
 
     private boolean isInPlanningHorizon(final long time) {
         return time <= this.solution.getPlanningHorizon(TimeUnit.MILLISECONDS);
     }
 
     private void recalculateOccupiedArcs(final ItineraryAssignment ia) {
         final int scanEveryXMillis = 60000 / RAS2012ScoreCalculator.OCCUPATION_CHECKS_PER_MINUTE;
         // insert the number of conflicts for the given assignments
         for (long time = 0; this.isInPlanningHorizon(time); time += scanEveryXMillis) {
             this.conflicts.setOccupiedArcs(time, ia.getTrain(),
                     ia.getItinerary().getOccupiedArcs(time));
         }
     }
 
     @Override
     public void resetWorkingSolution(final RAS2012Solution workingSolution) {
         this.solution = workingSolution;
         this.clearEveryCache();
         for (final ItineraryAssignment ia : this.solution.getAssignments()) {
             this.insert(ia);
         }
     }
 
     private void retract(final ItineraryAssignment ia) {
         final Train t = ia.getTrain();
         this.wantTimePenalties.remove(t);
         this.unpreferredTracksPenalties.remove(t);
         this.scheduleAdherencePenalties.remove(t);
         this.delayPenalties.remove(t);
         this.didTrainArrive.remove(t);
         this.conflicts.resetOccupiedArcs(t);
     }
 }
