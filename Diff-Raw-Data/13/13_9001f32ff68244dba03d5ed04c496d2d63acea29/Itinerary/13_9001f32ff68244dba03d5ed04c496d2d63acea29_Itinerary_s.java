 package org.drools.planner.examples.ras2012.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.drools.planner.examples.ras2012.RAS2012Solution;
 import org.drools.planner.examples.ras2012.model.original.Arc;
 import org.drools.planner.examples.ras2012.model.original.MaintenanceWindow;
 import org.drools.planner.examples.ras2012.model.original.Node;
 import org.drools.planner.examples.ras2012.model.original.ScheduleAdherenceRequirement;
 import org.drools.planner.examples.ras2012.model.original.Train;
 import org.drools.planner.examples.ras2012.model.original.WaitTime;
 import org.drools.planner.examples.ras2012.util.ItineraryVisualizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class Itinerary {
 
     private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
 
     private static final Logger   logger            = LoggerFactory.getLogger(Itinerary.class);
 
     private static BigDecimal getDistanceInMilesFromSpeedAndTime(final int speedInMPH,
             final long timeInMilliseconds) {
         final BigDecimal timeInSeconds = BigDecimal.valueOf(timeInMilliseconds).divide(
                 BigDecimal.valueOf(1000), 10, BigDecimal.ROUND_HALF_EVEN);
         final BigDecimal timeInMinutes = timeInSeconds.divide(BigDecimal.valueOf(60), 10,
                 BigDecimal.ROUND_HALF_EVEN);
         final BigDecimal milesPerHour = BigDecimal.valueOf(speedInMPH);
         final BigDecimal milesPerMinute = milesPerHour.divide(BigDecimal.valueOf(60), 10,
                 BigDecimal.ROUND_HALF_EVEN);
         return milesPerMinute.multiply(timeInMinutes);
     }
 
     private static long getTimeFromSpeedAndDistance(final int speedInMPH, final BigDecimal distance) {
         return distance.divide(BigDecimal.valueOf(speedInMPH), 10, BigDecimal.ROUND_HALF_EVEN)
                 .multiply(BigDecimal.valueOf(3600000)).longValue();
     }
 
     private final Route                        route;
 
     private final Train                        train;
 
     private final AtomicBoolean                scheduleCacheValid    = new AtomicBoolean(false);
     private final Collection<Node>             nodesEnRoute;
     private final SortedMap<Long, Node>        scheduleCache         = new TreeMap<Long, Node>();
     private final SortedMap<Long, Arc>         scheduleCacheWithArcs = new TreeMap<Long, Arc>();
     private final long                         trainEntryTime;
     private final Map<Node, WaitTime>          nodeWaitTimes         = new HashMap<Node, WaitTime>();
 
     // FIXME only one window per node; multiple different windows with same node will get lost
     private final Map<Node, MaintenanceWindow> maintenances          = new HashMap<Node, MaintenanceWindow>();
 
     private final Map<Long, Collection<Arc>>   occupiedArcsCache     = new HashMap<Long, Collection<Arc>>();
 
     public Itinerary(final Route r, final Train t,
             final Collection<MaintenanceWindow> maintenanceWindows) {
         if (!r.isPossibleForTrain(t)) {
             throw new IllegalArgumentException("Route " + r.getId() + " impossible for train "
                     + t.getName() + ".");
         }
         this.route = r;
         this.train = t;
         this.trainEntryTime = t.getEntryTime(Itinerary.DEFAULT_TIME_UNIT);
         this.nodesEnRoute = this.getRoute().getProgression().tail(this.getTrain().getOrigin())
                 .getNodes();
         // initialize the maintenance windows
         for (final MaintenanceWindow mow : maintenanceWindows) {
             final Node n = mow.getOrigin(t);
             this.maintenances.put(n, mow);
         }
     }
 
     private void cacheSchedule() {
         int i = 0;
         long previousTime = 0;
         Arc previousArc = null;
         boolean seekingStart = true;
         for (final Arc currentArc : this.getRoute().getProgression().getArcs()) {
             // arc progression begins with the start of the route; the train doesn't necessarily start there
             if (seekingStart) {
                 if (currentArc.getOrigin(this.getTrain()) != this.getTrain().getOrigin()) {
                     continue;
                 } else {
                     seekingStart = false;
                 }
             }
             long time = 0;
             if (i == 0) {
                 // first item needs to be augmented by the train entry time
                 time += this.trainEntryTime;
             } else {
                 // otherwise we need to convert a relative time to an absolute time by adding the previous node's time
                 time = this.getTrain().getArcTravellingTime(previousArc,
                         Itinerary.DEFAULT_TIME_UNIT);
                 time += previousTime;
             }
             // now adjust for node wait time, should there be any
             final Node n = currentArc.getOrigin(this.getTrain());
             final WaitTime wt = this.nodeWaitTimes.get(n);
             if (wt != null) {
                 time += wt.getWaitFor(Itinerary.DEFAULT_TIME_UNIT);
             }
             // check for maintenance windows
             if (this.maintenances.containsKey(n)) {
                 // there is a maintenance registered for the next node
                 final MaintenanceWindow w = this.maintenances.get(n);
                 if (w.isInside(time, Itinerary.DEFAULT_TIME_UNIT)) { // the maintenance is ongoing, we have to wait
                     // and adjust total node entry time
                     time = w.getEnd(Itinerary.DEFAULT_TIME_UNIT);
                 }
             }
             // and store
             this.scheduleCache.put(time, n);
             this.scheduleCacheWithArcs.put(time, currentArc);
             previousTime = time;
             previousArc = currentArc;
             i++;
         }
         if (previousArc == null) {
             throw new IllegalStateException("previousArc == null. That shouldn't have happened!");
         }
         final long time = previousTime
                 + this.getTrain().getArcTravellingTime(previousArc, Itinerary.DEFAULT_TIME_UNIT);
         this.scheduleCache.put(time, previousArc.getDestination(this.getTrain()));
        this.scheduleCacheWithArcs.put(time, previousArc);
         this.scheduleCacheValid.set(true);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Itinerary)) {
             return false;
         }
         final Itinerary other = (Itinerary) obj;
         if (this.nodeWaitTimes == null) {
             if (other.nodeWaitTimes != null) {
                 return false;
             }
         } else if (!this.nodeWaitTimes.equals(other.nodeWaitTimes)) {
             return false;
         }
         if (this.route == null) {
             if (other.route != null) {
                 return false;
             }
         } else if (!this.route.equals(other.route)) {
             return false;
         }
         if (this.train == null) {
             if (other.train != null) {
                 return false;
             }
         } else if (!this.train.equals(other.train)) {
             return false;
         }
         return true;
     }
 
     public Map<Node, WaitTime> getAllWaitTimes() {
         return Collections.unmodifiableMap(this.nodeWaitTimes);
     }
 
     public Collection<Arc> getCurrentlyOccupiedArcs(final long time) {
         if (!this.occupiedArcsCache.containsKey(time)) {
             final Collection<Arc> a = this.getCurrentlyOccupiedArcsUncached(time);
             this.occupiedArcsCache.put(time, a);
             return a;
         }
         return this.occupiedArcsCache.get(time);
     }
 
     private Collection<Arc> getCurrentlyOccupiedArcsUncached(final long time) {
         final Arc leadingArc = this.getLeadingArc(time);
         if (leadingArc == null) {
             // train not in the network
             // FIXME train should leave the network gradually, not at once when it reaches destination
             return Collections.emptySet();
         }
         final Collection<Arc> occupiedArcs = new LinkedHashSet<Arc>();
         occupiedArcs.add(leadingArc);
         // calculate how far are we into the leading arc
         final SortedMap<Long, Arc> nodeEntryTimes = this.getScheduleWithArcs();
         long timeArcEntered = -1;
         for (final SortedMap.Entry<Long, Arc> entry : nodeEntryTimes.entrySet()) {
             if (entry.getValue() == leadingArc) {
                 timeArcEntered = entry.getKey();
                 break;
             }
         }
         if (timeArcEntered < this.trainEntryTime) {
             throw new IllegalStateException(
                     "Proper arc cannot be found! Possibly a bug in the algoritm.");
         }
         final long timeTravelledInLeadingArc = time - timeArcEntered;
         final BigDecimal travelledInLeadingArc = Itinerary.getDistanceInMilesFromSpeedAndTime(this
                 .getTrain().getMaximumSpeed(leadingArc.getTrack()), timeTravelledInLeadingArc);
         BigDecimal remainingLengthOfTrain = this.getTrain().getLength()
                 .subtract(travelledInLeadingArc);
         // and now add any preceding arcs for as long as the remaining train length > 0
         Arc currentlyProcessedArc = leadingArc;
         while ((currentlyProcessedArc = this.getRoute().getProgression()
                 .getPrevious(currentlyProcessedArc)) != null) {
             occupiedArcs.add(currentlyProcessedArc);
             remainingLengthOfTrain = remainingLengthOfTrain.subtract(currentlyProcessedArc
                     .getLengthInMiles());
             if (remainingLengthOfTrain.compareTo(BigDecimal.ZERO) < 0) {
                 break;
             }
         }
         return occupiedArcs;
     }
 
     public long getDelay() {
         return this.getDelay(RAS2012Solution.getPlanningHorizon(Itinerary.DEFAULT_TIME_UNIT));
     }
 
     private long getDelay(final long horizon) {
         if (this.trainEntryTime > horizon) {
             // train not en route yet, delay must be zero
             return 0;
         }
         final long originalDelay = this.getTrain().getOriginalDelay(Itinerary.DEFAULT_TIME_UNIT);
         final SortedMap<Long, Node> schedule = this.getSchedule();
         final SortedMap<Long, Node> scheduleInHorizon = schedule.headMap(horizon);
         if (scheduleInHorizon.size() == 0) {
             // train is not in the network at the time
             if (this.trainEntryTime < horizon) {
                 // however, it should be; origin node possibly has some wait time
                 return horizon - this.trainEntryTime + originalDelay;
             } else {
                 // otherwise it's alright, there is only the original delay
                 return originalDelay;
             }
         }
         final long actualArrivalTime = scheduleInHorizon.lastKey();
         final long optimalArrivalTime = this.trainEntryTime
                 + Itinerary.getTimeFromSpeedAndDistance(this.getTrain().getMaximumSpeed(),
                         this.getTravellingDistance(this.getTrain().getDestination()));
         return actualArrivalTime - optimalArrivalTime;
     }
 
     protected Arc getLeadingArc(final long time) {
         if (time < this.trainEntryTime) {
             return null;
         }
         Node previousNode = null;
         for (final SortedMap.Entry<Long, Node> entry : this.getSchedule().entrySet()) {
             final long timeOfArrival = entry.getKey();
             final Node currentNode = entry.getValue();
             if (time > timeOfArrival) {
                 previousNode = currentNode;
                 continue;
             } else if (time == timeOfArrival) {
                 return this.getRoute().getProgression().getWithOriginNode(currentNode);
             } else {
                 return this.getRoute().getProgression().getWithOriginNode(previousNode);
             }
         }
         return null;
     }
 
     public Map<Node, MaintenanceWindow> getMaintenances() {
         return this.maintenances;
     }
 
     public Route getRoute() {
         return this.route;
     }
 
     public synchronized SortedMap<Long, Node> getSchedule() {
         if (!this.scheduleCacheValid.get() || this.scheduleCache.size() == 0) {
             this.cacheSchedule();
         }
         return Collections.unmodifiableSortedMap(this.scheduleCache);
     }
 
     public Map<Node, Long> getScheduleAdherenceStatus() {
         final long horizon = RAS2012Solution.getPlanningHorizon(Itinerary.DEFAULT_TIME_UNIT);
         final SortedMap<Long, Node> schedule = this.getSchedule();
         final SortedMap<Long, Node> scheduleInteresting = schedule.headMap(horizon);
         final Map<Node, Long> result = new HashMap<Node, Long>();
         for (final ScheduleAdherenceRequirement sa : this.getTrain()
                 .getScheduleAdherenceRequirements()) {
             final long expectedArrival = sa.getTimeSinceStartOfWorld(Itinerary.DEFAULT_TIME_UNIT);
             final Node expectedDestination = sa.getDestination();
             if (expectedArrival <= horizon) {
                 // arrival expected inside the planning horizon
                 if (scheduleInteresting.values().contains(expectedDestination)) {
                     final int numResults = result.size();
                     // arrival actually inside the planning horizon; we know the exact delay
                     for (final SortedMap.Entry<Long, Node> entry : scheduleInteresting.entrySet()) {
                         if (entry.getValue() == expectedDestination) {
                             result.put(expectedDestination, expectedArrival - entry.getKey());
                         }
                     }
                     if (result.size() != numResults + 1) {
                         throw new IllegalStateException(
                                 "Cannot find scheduled node. This must be a bug in the algorithm.");
                     }
                 } else {
                     // arrival unfortunately outside the planning horizon
                     result.put(expectedDestination, this.getDelay());
                 }
             } else {
                 // arrival expected outside the horizon, only count the delay before the horizon
                 result.put(expectedDestination, this.getDelay());
             }
         }
         return Collections.unmodifiableMap(result);
     }
 
     public synchronized SortedMap<Long, Arc> getScheduleWithArcs() {
         if (!this.scheduleCacheValid.get() || this.scheduleCache.size() == 0) {
             this.cacheSchedule();
         }
         return Collections.unmodifiableSortedMap(this.scheduleCacheWithArcs);
     }
 
     public long getTimeSpentOnUnpreferredTracks(final long time) {
         final SortedMap<Long, Arc> arcEntryTimes = this.getScheduleWithArcs();
         long spentTime = 0;
         final Arc leadingArc = this.getLeadingArc(time);
         /*
          * the time spent in between the nodes is calculated as a difference of their entry times; if we calculated just the
          * time spent traversing the arc, we would have missed wait times and MOWs.
          */
         long previousTimeOfEntry = 0;
         Arc previousArc = null;
         for (final SortedMap.Entry<Long, Arc> entry : arcEntryTimes.headMap(time).entrySet()) {
             final long currentTimeOfEntry = entry.getKey();
             final Arc a = entry.getValue();
             if (previousArc != null && !this.getRoute().isArcPreferred(previousArc)) {
                 if (previousArc == leadingArc) {
                     // include the time spent on this track so far
                     spentTime += time - currentTimeOfEntry;
                 }
                 // include the whole time spent on previous
                 spentTime += currentTimeOfEntry - previousTimeOfEntry;
             }
             previousTimeOfEntry = currentTimeOfEntry;
             previousArc = a;
         }
         return spentTime;
     }
 
     public Train getTrain() {
         return this.train;
     }
 
     private BigDecimal getTravellingDistance(final Node target) {
         BigDecimal distance = BigDecimal.ZERO;
         for (final Arc a : this.getRoute().getProgression().getArcs()) {
             distance = distance.add(a.getLengthInMiles());
             if (a.getDestination(this.getTrain()) == target) {
                 break;
             }
         }
         return distance;
     }
 
     public synchronized WaitTime getWaitTime(final Node n) {
         return this.nodeWaitTimes.get(n);
     }
 
     public Map<Long, Long> getWantTimeDifference() {
         final Map<Long, Long> result = new HashMap<Long, Long>();
         for (final SortedMap.Entry<Long, Node> entry : this.getSchedule().entrySet()) {
             if (entry.getValue() == this.getTrain().getDestination()) {
                 // make sure we only include the time within the planning horizon
                 final long actualTime = Math.min(entry.getKey(),
                         RAS2012Solution.getPlanningHorizon(Itinerary.DEFAULT_TIME_UNIT));
                 final long difference = actualTime
                         - this.getTrain().getWantTime(Itinerary.DEFAULT_TIME_UNIT);
                 result.put(entry.getKey(), difference);
             }
         }
         return result;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + (this.nodeWaitTimes == null ? 0 : this.nodeWaitTimes.hashCode());
         result = prime * result + (this.route == null ? 0 : this.route.hashCode());
         result = prime * result + (this.train == null ? 0 : this.train.hashCode());
         return result;
     }
 
     private synchronized void invalidateCaches() {
         this.occupiedArcsCache.clear();
         this.scheduleCache.clear();
         this.scheduleCacheWithArcs.clear();
     }
 
     public boolean isNodeOnRoute(final Node n) {
         return this.nodesEnRoute.contains(n);
     }
 
     public void removeAllWaitTimes() {
         if (this.nodeWaitTimes.size() > 0) {
             this.invalidateCaches();
         }
         this.nodeWaitTimes.clear();
     }
 
     public WaitTime removeWaitTime(final Node n) {
         if (this.nodeWaitTimes.containsKey(n)) {
             this.invalidateCaches();
             return this.nodeWaitTimes.remove(n);
         } else {
             return null;
         }
     }
 
     public synchronized WaitTime setWaitTime(final WaitTime w, final Node n) {
         if (w == null) {
             return this.removeWaitTime(n);
         }
         if (!this.isNodeOnRoute(n)) {
             throw new IllegalArgumentException(n + " not in the itinerary: " + this);
         }
         if (this.getRoute().getWaitPoints().contains(n)) {
             this.invalidateCaches();
             final WaitTime previous = this.nodeWaitTimes.get(n);
             this.nodeWaitTimes.put(n, w);
             return previous;
         } else {
             return null;
         }
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("Itinerary: Train ").append(this.getTrain().getName()).append(", Route ")
                 .append(this.getRoute().getId()).append(" [");
         for (final SortedMap.Entry<Long, Node> a : this.getSchedule().entrySet()) {
             sb.append(a.getValue().getId()).append("@").append(a.getKey()).append(" ");
         }
         sb.append("]");
         return sb.toString();
     }
 
     public boolean visualize(final File target) {
         OutputStream os = null;
         try {
             os = new FileOutputStream(target);
             Itinerary.logger.info("Starting visualizing itinerary: " + this);
             new ItineraryVisualizer(this).visualize(os);
             Itinerary.logger.info("Itinerary visualization finished: " + this);
             return true;
         } catch (final Exception ex) {
             Itinerary.logger.error("Visualizing itinerary " + this + " failed.", ex);
             return false;
         } finally {
             if (os != null) {
                 try {
                     os.close();
                 } catch (final IOException e) {
                     // nothing to do here
                 }
             }
         }
     }
 }
