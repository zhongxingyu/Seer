 package org.drools.planner.examples.ras2012.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.drools.planner.examples.ras2012.interfaces.Visualizable;
 import org.drools.planner.examples.ras2012.model.Arc.TrackType;
 import org.drools.planner.examples.ras2012.util.RouteVisualizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Note: this class has a natural ordering that is inconsistent with equals.
  */
 public class Route implements Comparable<Route>, Visualizable {
 
     public static enum Direction {
         EASTBOUND, WESTBOUND
     }
 
     private static final Logger        logger      = LoggerFactory.getLogger(Route.class);
 
     private static final AtomicInteger idGenerator = new AtomicInteger(0);
 
     protected static int resetRouteCounter() {
         return Route.idGenerator.getAndSet(0);
     }
 
     private final List<Arc>           arcs                    = new LinkedList<Arc>();
 
     private final Direction           direction;
 
     private final int                 id                      = Route.idGenerator.getAndIncrement();
 
     private int                       numberOfPreferredTracks = -1;
 
     private BigDecimal                travellingTimeInMinutes = null;
 
     private final Map<Train, Boolean> routePossibilitiesCache = new HashMap<Train, Boolean>();
 
     public Route(final Direction d) {
         this.direction = d;
     }
 
     private Route(final Direction d, final Arc... e) {
         this(d);
         for (final Arc a : e) {
             this.arcs.add(a);
         }
     }
 
     @Override
     public int compareTo(final Route o) {
         if (this.direction == o.direction) { // less tracks = better
             final int comparison = o.getLengthInArcs() - this.getLengthInArcs();
             if (comparison == 0) { // shorter = better
                 final int comparison2 = o.getTravellingTimeInMinutes().compareTo(
                         this.getTravellingTimeInMinutes());
                 if (comparison2 == 0) { // more preferred tracks = better
                     return this.getNumberOfPreferredTracks() - o.getNumberOfPreferredTracks();
                 } else {
                     return comparison2;
                 }
             } else {
                 return comparison;
             }
         } else {
             if (this.direction == Direction.EASTBOUND) {
                 return -1;
             } else {
                 return 1;
             }
         }
     }
 
     public boolean contains(final Arc e) {
         return this.arcs.contains(e);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (this.getClass() != obj.getClass()) {
             return false;
         }
         final Route other = (Route) obj;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
     public Route extend(final Arc add) {
         if (add == null) {
             throw new IllegalArgumentException("Cannot extend route with a null arc!");
         }
         if (this.arcs.contains(add)) {
             throw new IllegalArgumentException("Cannot extend route with the same arc twice!");
         }
         final Collection<Arc> newParts = new ArrayList<Arc>(this.arcs);
         newParts.add(add);
         final Arc[] result = newParts.toArray(new Arc[newParts.size()]);
         return new Route(this.getDirection(), result);
     }
 
     public List<Arc> getArcs() {
         return this.arcs;
     }
 
     public Direction getDirection() {
         return this.direction;
     }
 
     public int getId() {
         return this.id;
     }
 
     public Arc getInitialArc() {
         /*
          * get the first and last arc inserted; some arc should have the west node == 0, it is the west-most arc; the other is
          * by definition the east-most arc
          */
         final Arc one = this.arcs.get(0);
         final Arc two = this.arcs.get(this.arcs.size() - 1);
         if (one.getWestNode().getId() == 0) {
             // one is west-most
             return this.direction == Direction.WESTBOUND ? two : one;
         } else {
             // one is east-most
             return this.direction == Direction.WESTBOUND ? one : two;
         }
     }
 
     public int getLengthInArcs() {
         return this.arcs.size();
     }
 
     public BigDecimal getLengthInMiles() {
         BigDecimal result = BigDecimal.ZERO;
         for (final Arc a : this.arcs) {
             result = result.add(a.getLengthInMiles());
         }
         return result;
     }
 
     public Arc getNextArc(final Arc a) {
         if (this.arcs.isEmpty()) {
             throw new IllegalArgumentException("There is no next arc in an empty route.");
         }
         if (a == null) {
             return this.getInitialArc();
         }
         if (!this.contains(a)) {
             throw new IllegalArgumentException("The route doesn't contain the arc.");
         }
         final Node n = a.getEndingNode(this);
         for (final Arc a2 : this.arcs) {
             if (a2.getStartingNode(this) == n) {
                 return a2;
             }
         }
         return null;
     }
 
     private int getNumberOfPreferredTracks() {
         if (this.numberOfPreferredTracks == -1) {
             int i = 0;
             for (final Arc a : this.arcs) {
                 if (this.isArcPreferred(a)) {
                     i++;
                 }
             }
             this.numberOfPreferredTracks = i;
         }
         return this.numberOfPreferredTracks;
     }
 
     public Arc getPreviousArc(final Arc a) {
         if (this.arcs.isEmpty()) {
             throw new IllegalArgumentException("There is no previous arc in an empty route.");
         }
         if (a == null) {
             return this.getTerminalArc();
         }
         if (!this.contains(a)) {
             throw new IllegalArgumentException("The route doesn't contain the arc.");
         }
         final Node n = a.getStartingNode(this);
         for (final Arc a2 : this.arcs) {
             if (a2.getEndingNode(this) == n) {
                 return a2;
             }
         }
         return null;
     }
 
     public Arc getTerminalArc() {
         final Arc one = this.arcs.get(0);
         final Arc two = this.arcs.get(this.arcs.size() - 1);
         final Arc initial = this.getInitialArc();
         if (one == initial) {
             return two;
         } else {
             return one;
         }
     }
 
     private BigDecimal getTravellingTimeInMinutes() {
         if (this.travellingTimeInMinutes == null) {
             BigDecimal result = BigDecimal.ZERO;
             for (final Arc a : this.arcs) {
                 final BigDecimal length = a.getLengthInMiles();
                 final int speed = this.direction == Direction.EASTBOUND ? a.getTrackType()
                         .getSpeedEastbound() : a.getTrackType().getSpeedWestbound();
                 final BigDecimal timeInHours = length.divide(BigDecimal.valueOf(speed), 2,
                         BigDecimal.ROUND_HALF_DOWN);
                 result = result.add(timeInHours.multiply(BigDecimal.valueOf(60)));
             }
             this.travellingTimeInMinutes = result;
         }
         return this.travellingTimeInMinutes;
     }
 
     public Collection<Node> getWaitPoints() {
         final Collection<Node> waitPoints = new TreeSet<Node>();
         // we want to be able to hold the train before it enters the network
         final Arc firstArc = this.getInitialArc();
         if (this.direction == Direction.EASTBOUND) {
             waitPoints.add(firstArc.getWestNode());
         } else {
             waitPoints.add(firstArc.getEastNode());
         }
         // other wait points depend on the type of the track
         for (final Arc a : this.arcs) {
             if (a.getTrackType() == TrackType.SIDING) {
                 // on sidings, wait before leaving them through a switch
                 if (this.direction == Direction.EASTBOUND) {
                     waitPoints.add(a.getEastNode());
                 } else {
                     waitPoints.add(a.getWestNode());
                 }
             } else if (!a.getTrackType().isMainTrack()) {
                 // on crossovers and switches, wait before joining them
                 if (this.direction == Direction.EASTBOUND) {
                     waitPoints.add(a.getWestNode());
                 } else {
                     waitPoints.add(a.getEastNode());
                 }
             } else {
                 // on main tracks, never wait
             }
         }
         return Collections.unmodifiableCollection(waitPoints);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + this.id;
         return result;
     }
 
     public boolean isArcPreferred(final Arc a) {
         if (a.getTrackType() == TrackType.MAIN_0) {
             return true;
         } else if (a.getTrackType() == TrackType.MAIN_2) {
             return this.getDirection() == Direction.EASTBOUND;
         } else if (a.getTrackType() == TrackType.MAIN_1) {
             return this.getDirection() == Direction.WESTBOUND;
         } else {
             // preference of SIDING/SWITCH/CROSSOVER is based on which track are those coming off of
             final Arc previousArc = this.getPreviousArc(a);
             if (previousArc == null) {
                 return true;
             } else {
                 return this.isArcPreferred(previousArc);
             }
         }
     }
 
     public boolean isPossibleForTrain(final Train t) {
         if (!this.routePossibilitiesCache.containsKey(t)) {
             this.routePossibilitiesCache.put(t, this.isPossibleForTrainUncached(t));
         }
         return this.routePossibilitiesCache.get(t);
     }
 
     private boolean isPossibleForTrainUncached(final Train t) {
         // make sure both the route and the train are in the same direction
         final boolean bothEastbound = t.isEastbound() && this.getDirection() == Direction.EASTBOUND;
         final boolean bothWestbound = t.isWestbound() && this.getDirection() == Direction.WESTBOUND;
         if (!(bothEastbound || bothWestbound)) {
             return false;
         }
         boolean containsOrigin = false;
         boolean containsDestination = false;
         for (final Arc a : this.arcs) {
             containsOrigin = containsOrigin || a.getStartingNode(t) == t.getOrigin();
             containsDestination = containsDestination || a.getEndingNode(t) == t.getDestination();
             if (a.getTrackType() == TrackType.SIDING) {
                 if (t.isHeavy()) {
                     /*
                      * heavy trains must never use a siding when there is a meet-pass with another NSA train. this is the
                      * easiest way to fulfill the requirement.
                      */
                     return false;
                 }
                 // hazmat trains disallowed to take sidings
                 if (t.carriesHazardousMaterials()) {
                     return false;
                 }
                 // make sure the route doesn't contain a siding shorter than the train
                 final int result = a.getLengthInMiles().compareTo(t.getLength());
                 if (result < 0) {
                     return false;
                 }
             }
         }
         /*
          * some trains don't enter the world at depots. we must make sure that their origin/destination is actually part of this
          * route
          */
         if (!containsOrigin || !containsDestination) {
             return false;
         }
         // make sure that the train traverses through everywhere it's expected to
         for (final ScheduleAdherenceRequirement sar : t.getScheduleAdherenceRequirements()) {
             final Node requestedNode = sar.getDestination();
             boolean found = false;
             for (final Arc a : this.arcs) {
                if (a.getStartingNode(t) == requestedNode) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "Route [id=" + this.id + ", direction=" + this.direction + ", parts=" + this.arcs
                 + "]";
     }
 
     @Override
     public boolean visualize(final File target) {
         OutputStream os = null;
         try {
             os = new FileOutputStream(target);
             final Collection<Node> nodes = new HashSet<Node>();
             for (final Arc a : this.arcs) {
                 nodes.add(a.getStartingNode(this));
                 nodes.add(a.getEndingNode(this));
             }
             Route.logger.info("Starting visualizing route: " + this.getId());
             new RouteVisualizer(this).visualize(os);
             Route.logger.info("Route visualization finished: " + this.getId());
             return true;
         } catch (final Exception ex) {
             Route.logger.error("Visualizing route " + this.getId() + " failed.", ex);
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
