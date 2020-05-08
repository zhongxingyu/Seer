 package org.drools.planner.examples.ras2012;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.drools.planner.core.score.buildin.hardandsoft.DefaultHardAndSoftScore;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.core.score.director.incremental.AbstractIncrementalScoreCalculator;
 import org.drools.planner.examples.ras2012.model.Arc;
 import org.drools.planner.examples.ras2012.model.Itinerary;
 import org.drools.planner.examples.ras2012.model.ItineraryAssignment;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.Track;
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.util.Converter;
 import org.drools.planner.examples.ras2012.util.EntryRegistry;
 
 /**
  * <p>
  * A class that converts a solution into a score, in order to be able to rank various solutions based on their efficiency. For
  * performance reasons, this class implements incremental score calculation mechanisms. The important bits:
  * </p>
  * 
  * <ul>
  * <li>When {@link #resetWorkingSolution(ProblemSolution)} is called, this class is reset. No deltas are kept, we're starting
  * fresh.</li>
  * <li>Call to any of the <code>after...</code> or <code>before...</code> methods lets us know that some entity has changed.
  * Data associated with that entity need to be re-calculated. Cached data from other entities won't be touched.</li>
  * <li>When {@link #calculateScore()} is called, all that data is summed up and the result is the score for that particular
  * solution.</li>
  * </ul>
  * 
  * <p>
  * Each score has two parts, a hard score and a soft score. Hard score, if negative, means that there are some constraints
  * broken with which the solution doesn't make sense. These constraints are entry times (see
  * {@link #recalculateEntries(ItineraryAssignment)}) - the problem definition requires that train enters an arc no sooner than 5
  * minutes after it's been cleared by the previous train occupying it.
  * </p>
  * 
  * <p>
  * Soft constraints, those that only affect score quality and not its feasibility, are all defined by the problem. They are:
  * </p>
  * 
  * <ul>
  * <li>Train's delay on the route (see {@link #getDelayPenalty(Itinerary)}),</li>
  * <li>terminal want time (see {@link #getWantTimePenalty(Itinerary)}),</li>
  * <li>schedule adherence (see {@link #getScheduleAdherencePenalty(Itinerary)})</li>
  * <li>and time spent on unpreferred tracks (see {@link #getUnpreferredTracksPenalty(Itinerary)}).</li>
  * </ul>
  * 
  */
 public class ScoreCalculator extends AbstractIncrementalScoreCalculator<ProblemSolution> {
 
     private static final BigDecimal MILLIS_TO_HOURS = BigDecimal.valueOf(3600000);
 
     /**
      * Perform a one-time calculation on a given solution. This eliminates the possible side-effects of incremental score
      * calculation, resulting in a score that is guaranteed to be correct.
      * 
      * @param solution Solution to calculate the score for.
      * @return Score of the given solution.
      */
     public static HardAndSoftScore oneTimeCalculation(final ProblemSolution solution) {
         final ScoreCalculator calc = new ScoreCalculator();
         calc.resetWorkingSolution(solution);
         return calc.calculateScore();
     }
 
     private static BigDecimal roundMillisecondsToHours(final long milliseconds) {
         return BigDecimal.valueOf(milliseconds).divide(ScoreCalculator.MILLIS_TO_HOURS,
                 Converter.BIGDECIMAL_SCALE, Converter.BIGDECIMAL_ROUNDING);
     }
 
     private ProblemSolution           solution                   = null;
 
     private final Map<Train, Integer> wantTimePenalties          = new HashMap<Train, Integer>();
 
     private final Map<Train, Integer> delayPenalties             = new HashMap<Train, Integer>();
 
     private final Map<Train, Integer> scheduleAdherencePenalties = new HashMap<Train, Integer>();
 
     private final Map<Train, Integer> unpreferredTracksPenalties = new HashMap<Train, Integer>();
 
     private final Map<Train, Integer> uselessSidingsPenalties    = new HashMap<Train, Integer>();
     private EntryRegistry             entries;
 
     @Override
     public void afterAllVariablesChanged(final Object entity) {
         if (entity instanceof ItineraryAssignment) {
             this.modify((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void afterEntityAdded(final Object entity) {
         if (entity instanceof ItineraryAssignment) {
             this.modify((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void afterEntityRemoved(final Object entity) {
         throw new NotImplementedException();
     }
 
     @Override
     public void afterVariableChanged(final Object entity, final String variableName) {
         if (entity instanceof ItineraryAssignment) {
             this.modify((ItineraryAssignment) entity);
         }
     }
 
     @Override
     public void beforeAllVariablesChanged(final Object entity) {
         throw new NotImplementedException();
     }
 
     @Override
     public void beforeEntityAdded(final Object entity) {
         throw new NotImplementedException();
     }
 
     @Override
     public void beforeEntityRemoved(final Object entity) {
         throw new NotImplementedException();
     }
 
     @Override
     public void beforeVariableChanged(final Object entity, final String variableName) {
         throw new NotImplementedException();
     }
 
     /**
      * Calculate the score of the solution after the increments have been resolved. This methods only sums up those increments,
      * except in cases where we need to take into account the information from all the trains. These cases are:
      * 
      * <ul>
      * <li>Conflicts in occupied arcs are calculated in {@link ConflictRegistry}.</li>
      * <li>Conflicts in entry times are calculated in {@link EntryRegistry}.</li>
      * </ul>
      */
     @Override
     public HardAndSoftScore calculateScore() {
         int softPenalty = 0;
         int hardPenalty = this.entries.countConflicts();
         for (final Train t : this.solution.getTrains()) {
             softPenalty += this.wantTimePenalties.get(t);
             softPenalty += this.delayPenalties.get(t);
             softPenalty += this.scheduleAdherencePenalties.get(t);
             softPenalty += this.unpreferredTracksPenalties.get(t);
             hardPenalty += this.uselessSidingsPenalties.get(t);
         }
         return DefaultHardAndSoftScore.valueOf(-hardPenalty, -softPenalty);
     }
 
     /**
      * Calculate delay penalty for a given schedule. The rules for that are defined by the RAS 2012 problem statement.
      * 
      * @param i The schedule in question.
      * @return The penalty in dollars.
      */
     public int getDelayPenalty(final Itinerary i) {
         final long delay = i.getDelay(this.solution.getPlanningHorizon(TimeUnit.MILLISECONDS));
         if (delay <= 0) {
             return 0;
         }
         final BigDecimal hoursDelay = ScoreCalculator.roundMillisecondsToHours(delay);
         final BigDecimal maxHoursDelay = hoursDelay.max(BigDecimal.ZERO);
         return maxHoursDelay.multiply(BigDecimal.valueOf(i.getTrain().getType().getDelayPenalty()))
                 .intValue();
     }
 
     public int getPenaltyForNoMeetPassOnSidings(final Itinerary i) {
         int penalty = 0;
         for (final Arc a : i.getRoute().getProgression().getArcs()) {
             final Node destination = a.getDestination(i.getTrain());
             if (a.getTrack() != Track.SIDING) {
                 continue;
             }
            if (!i.hasNode(a.getOrigin(i.getTrain())) || !i.hasNode(destination)) {
                 continue;
             }
             if (!this.isInPlanningHorizon(i.getArrivalTime(destination))) {
                 continue;
             }
             if (i.getWaitTime(destination) != null) {
                 penalty++;
             }
         }
         return penalty;
     }
 
     /**
      * Calculate schedule adherence penalty for all nodes on a schedule. The rules for that are defined by the RAS 2012 problem
      * statement.
      * 
      * @param i The schedule in question.
      * @return The penalty in dollars.
      */
     public int getScheduleAdherencePenalty(final Itinerary i) {
         int penalty = 0;
         if (i.getTrain().getType().adhereToSchedule()) {
             for (final Node node : i.getTrain().getScheduleAdherenceRequirements().keySet()) {
                 penalty += this.getScheduleAdherencePenalty(i, node);
             }
         }
         return penalty;
     }
 
     /**
      * Calculate schedule adherence penalty for a given node on a schedule. The rules for that are defined by the RAS 2012
      * problem statement.
      * 
      * @param i The schedule in question.
      * @param node The node in question.
      * @return The penalty in dollars.
      */
     public int getScheduleAdherencePenalty(final Itinerary i, final Node node) {
         if (!i.getTrain().getType().adhereToSchedule()) {
             return 0;
         }
         final long arrival = i.getArrivalTime(node);
         if (!this.isInPlanningHorizon(arrival)) {
             return 0;
         }
         final long expectedArrival = i.getTrain().getScheduleAdherenceRequirements().get(node)
                 .getTimeSinceStartOfWorld(TimeUnit.MILLISECONDS);
         if (arrival <= expectedArrival) {
             return 0;
         }
         final long difference = arrival - expectedArrival;
         BigDecimal hourlyDifference = ScoreCalculator.roundMillisecondsToHours(difference);
         hourlyDifference = hourlyDifference.subtract(BigDecimal.valueOf(2));
         if (hourlyDifference.signum() > 0) {
             return hourlyDifference.multiply(BigDecimal.valueOf(200)).intValue();
         }
         return 0;
     }
 
     /**
      * Calculate a penalty for using unpreferred tracks in a given schedule. The rules for that are defined by the RAS 2012
      * problem statement.
      * 
      * @param i The schedule in question.
      * @return The penalty in dollars.
      */
     public int getUnpreferredTracksPenalty(final Itinerary i) {
         final BigDecimal hours = ScoreCalculator.roundMillisecondsToHours(i
                 .getTimeSpentOnUnpreferredTracks(this.solution
                         .getPlanningHorizon(TimeUnit.MILLISECONDS)));
         return hours.multiply(BigDecimal.valueOf(50)).intValue();
     }
 
     /**
      * Calculate a want time penalty for a given schedule. The rules for that are defined by the RAS 2012 problem statement.
      * 
      * @param i The schedule in question.
      * @return The penalty in dollars.
      */
     public int getWantTimePenalty(final Itinerary i) {
         final long actualTime = i.getArrivalTime();
         if (!this.isInPlanningHorizon(actualTime)) {
             // arrivals outside of the planning horizon aren't counted
             return 0;
         }
         final long delay = actualTime - i.getTrain().getWantTime(TimeUnit.MILLISECONDS);
         BigDecimal hours = ScoreCalculator.roundMillisecondsToHours(delay);
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
 
     /**
      * Whether or not a given time falls into the planning horizon.
      * 
      * @param time The time in question.
      * @return True if 0 <= time <= horizon, false otherwise.
      */
     private boolean isInPlanningHorizon(final long time) {
         if (time < 0) {
             return false;
         }
         return time <= this.solution.getPlanningHorizon(TimeUnit.MILLISECONDS);
     }
 
     /**
      * Re-calculate constraints for the changed entity.
      * 
      * @param ia The entity that's changed.
      */
     private void modify(final ItineraryAssignment ia) {
         final Train t = ia.getTrain();
         final Itinerary i = ia.getItinerary();
         this.unpreferredTracksPenalties.put(t, this.getUnpreferredTracksPenalty(i));
         this.scheduleAdherencePenalties.put(t, this.getScheduleAdherencePenalty(i));
         this.wantTimePenalties.put(t, this.getWantTimePenalty(i));
         this.delayPenalties.put(t, this.getDelayPenalty(i));
         this.uselessSidingsPenalties.put(t, this.getPenaltyForNoMeetPassOnSidings(i));
         this.recalculateEntries(ia);
     }
 
     /**
      * Retrieve and store all the entry/leave times for a particular train on a particular schedule. They will be used later to
      * make sure that no trains follows sooner than 5 minutes after another train. See {@link Itinerary#getArrivalTime(Node)}
      * and {@link Itinerary#getLeaveTime(Node)} for details on how these times are calculated.
      * 
      * @param ia The changed schedule for the train.
      */
     private void recalculateEntries(final ItineraryAssignment ia) {
         final Train t = ia.getTrain();
         final Itinerary i = ia.getItinerary();
         this.entries.resetTimes(t);
         for (final Arc a : ia.getRoute().getProgression().getArcs()) {
             if (!i.hasNode(a.getOrigin(t)) || !i.hasNode(a.getDestination(t))) {
                 continue;
             }
             final long arriveTime = i.getArrivalTime(a);
             if (this.isInPlanningHorizon(arriveTime)) {
                 final long leaveTime = i.getLeaveTime(a);
                 if (leaveTime == -1) {
                     // train reached the destination; make sure we properly account for its arrival time
                     this.entries.setTimes(a, t, arriveTime, i.getArrivalTime(a.getDestination(t)));
                 } else {
                     this.entries.setTimes(a, t, arriveTime, leaveTime);
 
                 }
             }
         }
     }
 
     /**
      * Prepare the calculator for working on a completely different solution. Resets all the caches.
      * 
      * @param workingSolution The solution to be used from now on.
      */
     @Override
     public void resetWorkingSolution(final ProblemSolution workingSolution) {
         this.solution = workingSolution;
         this.wantTimePenalties.clear();
         this.unpreferredTracksPenalties.clear();
         this.scheduleAdherencePenalties.clear();
         this.uselessSidingsPenalties.clear();
         this.delayPenalties.clear();
         this.entries = new EntryRegistry(Node.count());
         for (final ItineraryAssignment ia : this.solution.getAssignments()) {
             ia.getItinerary().resetLatestWaitTimeChange();
             this.modify(ia);
         }
     }
 
 }
