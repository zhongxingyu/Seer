 package org.drools.planner.examples.ras2012.model;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 import org.drools.planner.examples.ras2012.Visualizable;
 import org.drools.planner.examples.ras2012.util.Converter;
 import org.drools.planner.examples.ras2012.util.model.ArcProgression;
 import org.drools.planner.examples.ras2012.util.model.OccupationTracker;
 import org.drools.planner.examples.ras2012.util.visualizer.ItineraryVisualizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class Itinerary extends Visualizable {
 
     public static enum ChangeType {
 
         UNCHANGED, REMOVE_ALL_WAIT_TIMES, REMOVE_WAIT_TIME, SET_WAIT_TIME;
 
     }
 
     Pair<ChangeType, Node>                     lastChange            = ImmutablePair.of(
                                                                              ChangeType.UNCHANGED,
                                                                              null);
 
     private static final TimeUnit              DEFAULT_TIME_UNIT     = TimeUnit.MILLISECONDS;
 
     private final Route                        route;
 
     private final Train                        train;
     private long                               delay                 = 0;
 
     private final AtomicBoolean                scheduleCacheValid    = new AtomicBoolean(false);
     private final Collection<Node>             nodesEnRoute;
     private final Collection<Arc>              arcsEnRoute;
     private final SortedMap<Long, Node>        scheduleCache         = new TreeMap<Long, Node>();
     private final SortedMap<Long, Arc>         scheduleCacheWithArcs = new TreeMap<Long, Arc>();
     private final long                         trainEntryTime;
     private final Map<Node, WaitTime>          nodeWaitTimes         = new HashMap<Node, WaitTime>();
 
     // FIXME only one window per node; multiple different windows with same node will get lost
     private final Map<Node, MaintenanceWindow> maintenances          = new HashMap<Node, MaintenanceWindow>();
 
     private static final Logger                logger                = LoggerFactory
                                                                              .getLogger(Itinerary.class);
 
     public Itinerary(final Route r, final Train t) {
         this(r, t, null);
     }
 
     public Itinerary(final Route r, final Train t,
             final Collection<MaintenanceWindow> maintenanceWindows) {
         if (r == null || t == null) {
             throw new IllegalArgumentException("Neither route nor train may be null.");
         }
         if (!r.isPossibleForTrain(t)) {
             throw new IllegalArgumentException("Route " + r.getId() + " impossible for train "
                     + t.getName() + ".");
         }
         this.route = r;
         this.train = t;
         this.trainEntryTime = t.getEntryTime(Itinerary.DEFAULT_TIME_UNIT);
         final ArcProgression enRoute = this.getRoute().getProgression()
                 .tail(this.getTrain().getOrigin());
         this.nodesEnRoute = enRoute.getNodes();
         this.arcsEnRoute = enRoute.getArcs();
         // initialize the maintenance windows
         if (maintenanceWindows != null) {
             for (final MaintenanceWindow mow : maintenanceWindows) {
                 final Node origin = mow.getOrigin(t);
                 final Node destination = mow.getDestination(t);
                 if (this.isNodeOnRoute(origin) && this.isNodeOnRoute(destination)) {
                     this.maintenances.put(origin, mow);
                 }
             }
         }
     }
 
     private void cacheSchedule() {
         if (this.scheduleCacheValid.get()) {
             return;
         }
         this.delay = 0;
         this.scheduleCache.clear();
         this.scheduleCacheWithArcs.clear();
         int i = 0;
         long previousTime = 0;
         Arc previousArc = null;
         for (final Arc currentArc : this.arcsEnRoute) {
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
             final long preDelaying = time;
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
             this.delay += time - preDelaying; // difference between 'preDelaying' is a sum of MOWs and WTs
             // and store
             this.scheduleCache.put(time, n);
             this.scheduleCacheWithArcs.put(time, currentArc);
             previousTime = time;
             previousArc = currentArc;
             i++;
         }
         final long time = previousTime
                 + this.getTrain().getArcTravellingTime(previousArc, Itinerary.DEFAULT_TIME_UNIT);
         this.scheduleCache.put(time, previousArc.getDestination(this.getTrain()));
         this.scheduleCacheWithArcs.put(time + 1, null);
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
         return new EqualsBuilder().append(this.getTrain(), other.getTrain())
                 .append(this.getRoute(), other.getRoute())
                 .append(this.nodeWaitTimes, other.nodeWaitTimes).isEquals();
     }
 
     public long getArrivalTime() {
         return this.getSchedule().lastKey();
     }
 
     public long getArrivalTime(final Arc a) {
         return this.getArrivalTime(a.getOrigin(this.getTrain()));
     }
 
     public long getArrivalTime(final Node n) {
         if (n == null) {
             throw new IllegalArgumentException("Node cannot be null.");
         }
         final SortedMap<Long, Node> nodeEntryTimes = this.getSchedule();
         long timeEntered = -1;
         for (final SortedMap.Entry<Long, Node> entry : nodeEntryTimes.entrySet()) {
             if (entry.getValue() == n) {
                 timeEntered = entry.getKey();
                 break;
             }
         }
         if (timeEntered < this.trainEntryTime) {
             throw new IllegalStateException(
                     "Proper node cannot be found! Possibly a bug in the algoritm.");
         }
         return timeEntered;
     }
 
     public long getDelay() {
         this.cacheSchedule();
         return this.delay;
     }
 
     public Pair<ChangeType, Node> getLatestWaitTimeChange() {
         return this.lastChange;
     }
 
     protected Arc getLeadingArc(final long time) {
         if (time < this.trainEntryTime) {
             return null;
         }
         final SortedMap<Long, Arc> arcs = this.getScheduleWithArcs().tailMap(time);
         if (arcs.size() == 0) {
             return null;
         }
         final long arcTime = arcs.firstKey();
         final Arc arc = arcs.get(arcTime);
        if (time > arcTime) {
             return arc;
         } else {
             return this.getRoute().getProgression().getPreviousArc(arc);
         }
     }
 
     public long getLeaveTime(final Arc a) {
         Arc nextArc = this.route.getProgression().getNextArc(a);
         if (nextArc == null) {
             return -1;
         }
         return this.getArrivalTime(nextArc);
     }
 
     public long getLeaveTime(final Node n) {
         Node nextNode = this.route.getProgression().getNextNode(n);
         if (nextNode == null) {
             return -1;
         }
         return this.getArrivalTime(nextNode);
     }
 
     public Map<Node, MaintenanceWindow> getMaintenances() {
         return this.maintenances;
     }
 
     public OccupationTracker getOccupiedArcs(final long time) {
         final SortedMap<Long, Node> schedule = this.getSchedule();
         final ArcProgression progression = this.getRoute().getProgression();
         final boolean trainStarted = time <= schedule.firstKey();
         final boolean trainInOrigin = this.getTrain().getOrigin() == progression.getOrigin()
                 .getOrigin(this.getRoute());
         if (trainStarted && trainInOrigin) {
             // train not yet on the route
             return OccupationTracker.Builder.empty();
         }
         final Arc leadingArc = this.getLeadingArc(time);
         if (leadingArc == null) {
             // the train should gradually leave the territory through its destination
             final long timeTravelledInArc = time - schedule.lastKey();
             final BigDecimal travelledInArc = Converter.getDistanceFromSpeedAndTime(this.getTrain()
                     .getMaximumSpeed(Track.MAIN_0), timeTravelledInArc);
             if (travelledInArc.compareTo(this.getTrain().getLength()) >= 0) {
                 // the train is gone completely
                 return OccupationTracker.Builder.empty();
             } else {
                 // some part of the train is still in the territory
                 return progression.getOccupiedArcs(progression.getLength(), this.getTrain()
                         .getLength().subtract(travelledInArc));
             }
         } else if (!this.arcsEnRoute.contains(leadingArc)) {
             // the train didn't enter the territory yet
             return progression.getOccupiedArcs(progression.getDistance(leadingArc
                     .getDestination(progression)), this.getTrain().getLength());
         } else {
             // the train is in the territory
             final long timeTravelledInArc = time - this.getArrivalTime(leadingArc);
             final BigDecimal travelledInArc = Converter.getDistanceFromSpeedAndTime(
                     this.getTrain().getMaximumSpeed(leadingArc.getTrack()), timeTravelledInArc)
                     .min(leadingArc.getLength());
             return progression.getOccupiedArcs(
                     progression.getDistance(leadingArc.getOrigin(progression)).add(travelledInArc),
                     this.getTrain().getLength());
         }
     }
 
     public Route getRoute() {
         return this.route;
     }
 
     public SortedMap<Long, Node> getSchedule() {
         this.cacheSchedule();
         return Collections.unmodifiableSortedMap(this.scheduleCache);
     }
 
     public SortedMap<Long, Arc> getScheduleWithArcs() {
         this.cacheSchedule();
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
             if (previousArc != null && !this.getRoute().getProgression().isPreferred(previousArc)) {
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
 
     public WaitTime getWaitTime(final Node n) {
         return this.nodeWaitTimes.get(n);
     }
 
     public Map<Node, WaitTime> getWaitTimes() {
         return Collections.unmodifiableMap(this.nodeWaitTimes);
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(this.getTrain()).append(this.getRoute())
                 .append(this.nodeWaitTimes).build();
     }
 
     private void invalidateCaches() {
         this.scheduleCacheValid.set(false);
     }
 
     public boolean isNodeOnRoute(final Node n) {
         return this.nodesEnRoute.contains(n);
     }
 
     public WaitTime removeWaitTime(final Node n) {
         if (this.nodeWaitTimes.containsKey(n)) {
             Itinerary.logger.debug("Removing wait time for {} from {}.", new Object[] { n, this });
             this.invalidateCaches();
             this.lastChange = ImmutablePair.of(ChangeType.REMOVE_WAIT_TIME, n);
             return this.nodeWaitTimes.remove(n);
         } else {
             Itinerary.logger.debug("No wait time to remove for {} from {}.",
                     new Object[] { n, this });
             return null;
         }
     }
 
     public void removeWaitTimes() {
         if (this.nodeWaitTimes.size() > 0) {
             Itinerary.logger.debug("Removing all wait times from {}.", new Object[] { this });
             this.invalidateCaches();
         }
         this.nodeWaitTimes.clear();
         this.lastChange = ImmutablePair.of(ChangeType.REMOVE_ALL_WAIT_TIMES, null);
     }
 
     public void resetLatestWaitTimeChange() {
         this.lastChange = ImmutablePair.of(ChangeType.UNCHANGED, null);
     }
 
     public WaitTime setWaitTime(final Node n, final WaitTime w) {
         if (!this.getRoute().getProgression().getWaitPoints().contains(n)) {
             throw new IllegalArgumentException(n + " not a wait point: " + this);
         }
         if (w == null) {
             return this.removeWaitTime(n);
         }
         this.invalidateCaches();
         final WaitTime previous = this.nodeWaitTimes.put(n, w);
         this.lastChange = ImmutablePair.of(ChangeType.SET_WAIT_TIME, n);
         Itinerary.logger.debug("Set {} on {} in {}, replacing {}.", new Object[] { w, n, this,
                 previous });
         return previous;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("Itinerary [route=").append(this.route.getId()).append(", train=")
                 .append(this.train.getName()).append("]");
         return builder.toString();
     }
 
     @Override
     public boolean visualize(final File target) {
         return this.visualize(target, -1);
     }
 
     public boolean visualize(final File target, final long time) {
         return this.visualize(new ItineraryVisualizer(this, time), target);
     }
 }
