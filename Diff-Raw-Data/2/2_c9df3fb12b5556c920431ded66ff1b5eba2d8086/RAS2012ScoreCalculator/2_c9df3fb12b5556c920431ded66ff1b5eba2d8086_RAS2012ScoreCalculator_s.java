 package org.drools.planner.examples.ras2012;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeSet;
 
 import org.drools.planner.core.score.buildin.hardandsoft.DefaultHardAndSoftScore;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.core.score.director.simple.SimpleScoreCalculator;
 import org.drools.planner.examples.ras2012.interfaces.ScheduleProducer;
 import org.drools.planner.examples.ras2012.model.Arc;
 import org.drools.planner.examples.ras2012.model.Itinerary;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.Route;
 import org.drools.planner.examples.ras2012.model.planner.ItineraryAssignment;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RAS2012ScoreCalculator implements SimpleScoreCalculator<RAS2012Solution> {
 
     private static final Logger logger = LoggerFactory.getLogger(RAS2012ScoreCalculator.class);
 
     @Override
     public HardAndSoftScore calculateScore(final RAS2012Solution solution) {
         // count the number of conflicts
         int cost = 0;
         for (final ItineraryAssignment ia : solution.getAssignments()) {
             cost += this.getCostPerItinerary(ia.getItinerary(), solution);
         }
         final HardAndSoftScore score = DefaultHardAndSoftScore.valueOf(
                 -this.getConflicts(solution), -cost);
         return score;
     }
 
     private int getConflicts(final RAS2012Solution solution) {
         int conflicts = 0;
         // insert the number of conflicts for the given assignments
         for (double minutes = 0; minutes <= RAS2012Solution.PLANNING_HORIZON_MINUTES; minutes += RAS2012Solution.PLANNING_TIME_DIVISION_MINUTES) {
             final BigDecimal time = new BigDecimal(minutes);
             // for each point in time...
             final Map<Arc, Integer> arcConflicts = new HashMap<Arc, Integer>();
             for (final ItineraryAssignment ia : solution.getAssignments()) {
                 // ... and each assignment...
                 final ScheduleProducer i = ia.getItinerary();
                 for (final Arc a : i.getCurrentlyOccupiedArcs(time)) {
                     // ... find out how many times an arc has been used
                     if (arcConflicts.containsKey(a)) {
                         arcConflicts.put(a, arcConflicts.get(a) + 1);
                     } else {
                        arcConflicts.put(a, 1);
                     }
                 }
             }
             // when an arc has been used more than once, it is a conflict of two itineraries
             for (final Map.Entry<Arc, Integer> entry : arcConflicts.entrySet()) {
                 conflicts += entry.getValue();
             }
         }
         return conflicts;
     }
 
     private int getCostPerItinerary(final ScheduleProducer i, final RAS2012Solution solution) {
         int penalty = 0;
         /*
          * want time penalties are only counted when the train arrives on hour before or three hours after the want time
          */
         penalty += this.getWantTimePenalty(i, solution);
         /*
          * calculate hot schedule adherence penalties, given that the train needs to adhere and that the delay is more than 2
          * hours
          */
         penalty += this.getScheduleAdherencePenalty(i, solution);
         /*
          * calculate time spent on unpreferred tracks
          */
         penalty += this.roundMinutesToWholeHours(i.getTimeSpentOnUnpreferredTracks(BigDecimal
                 .valueOf(RAS2012Solution.PLANNING_HORIZON_MINUTES))) * 50;
         /*
          * calculate penalty for delays on the route
          */
         penalty += this.getDelayPenalty(i, solution);
         return penalty;
     }
 
     private int getDelayPenalty(final ScheduleProducer i, final RAS2012Solution solution) {
         final Route bestRoute = solution.getNetwork().getBestRoute(i.getTrain());
         final SortedMap<BigDecimal, Node> idealSchedule = new Itinerary(bestRoute, i.getTrain(),
                 solution.getMaintenances()).getSchedule();
         final SortedMap<BigDecimal, Node> actualSchedule = i.getSchedule();
         final BigDecimal idealArrival = new TreeSet<BigDecimal>(idealSchedule.keySet()).last();
         final BigDecimal actualArrival = new TreeSet<BigDecimal>(actualSchedule.keySet()).last();
         final int hoursDelay = this.roundMinutesToWholeHours(actualArrival.subtract(idealArrival));
         if (hoursDelay < 0) {
             // FIXME fuck!
             RAS2012ScoreCalculator.logger
                     .debug("Delay for "
                             + i.getTrain().getName()
                             + " on route "
                             + i.getRoute().getId()
                             + " was negative! The optimal route probably hit a maintenance window that the actual route avoided.");
         }
         return Math.max(0, hoursDelay) * i.getTrain().getType().getDelayPenalty();
     }
 
     private int getScheduleAdherencePenalty(final ScheduleProducer i, final RAS2012Solution solution) {
         int penalty = 0;
         if (i.getTrain().getType().adhereToSchedule()) {
             final Map<BigDecimal, BigDecimal> sa = i.getScheduleAdherenceStatus();
             for (final Map.Entry<BigDecimal, BigDecimal> entry : sa.entrySet()) {
                 if (!this.isInPlanningHorizon(entry.getKey())) {
                     // difference occured past the planning horizon; we don't care about it
                     continue;
                 }
                 final BigDecimal difference = entry.getValue();
                 if (difference.signum() < 1) {
                     continue;
                 }
                 final int hourlyDifference = this.roundMinutesToWholeHours(difference);
                 if (hourlyDifference > 2) {
                     penalty += (hourlyDifference - 2) * 200;
                 }
             }
         }
         return penalty;
     }
 
     private int getWantTimePenalty(final ScheduleProducer i, final RAS2012Solution solution) {
         int penalty = 0;
         int hourlyDifference = 0;
         final Map<BigDecimal, BigDecimal> wantTimeDifferences = i.getWantTimeDifference();
         for (final Map.Entry<BigDecimal, BigDecimal> entry : wantTimeDifferences.entrySet()) {
             if (!this.isInPlanningHorizon(entry.getKey())) {
                 // difference occured past the planning horizon; we don't care about it
                 continue;
             }
             final BigDecimal wantTimeDifference = entry.getValue();
             if (wantTimeDifference.signum() > 0) {
                 final int hours = this.roundMinutesToWholeHours(wantTimeDifference);
                 if (hours > 3) {
                     hourlyDifference = hours - 3;
                 }
             } else if (wantTimeDifference.signum() < 0) {
                 final int hours = this.roundMinutesToWholeHours(wantTimeDifference);
                 if (hours < -1) {
                     hourlyDifference = Math.abs(hours + 1);
                 }
             }
             penalty += hourlyDifference * 75;
         }
         return penalty;
     }
 
     private boolean isInPlanningHorizon(final BigDecimal time) {
         return time.compareTo(BigDecimal.valueOf(RAS2012Solution.PLANNING_HORIZON_MINUTES)) <= 0;
     }
 
     private int roundMinutesToWholeHours(final BigDecimal minutes) {
         final BigDecimal hours = minutes.divide(BigDecimal.valueOf(60), 10,
                 BigDecimal.ROUND_HALF_EVEN);
         final int result = hours.setScale(0, BigDecimal.ROUND_UP).intValue();
         return result;
     }
 
 }
