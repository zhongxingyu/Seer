 package org.drools.planner.examples.ras2012.util.model;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.drools.planner.examples.ras2012.Directed;
 import org.drools.planner.examples.ras2012.model.Arc;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.Track;
 import org.drools.planner.examples.ras2012.util.model.OccupationTracker.Builder;
 
 public class ArcProgression implements Directed {
 
     private final LinkedList<Arc>            orderedArcs        = new LinkedList<Arc>();
     private final SortedMap<BigDecimal, Arc> milestones         = new TreeMap<BigDecimal, Arc>();
     private final Map<Node, Arc>             arcsPerOrigin      = new LinkedHashMap<Node, Arc>();
     private final Map<Node, Arc>             arcsPerDestination = new LinkedHashMap<Node, Arc>();
     private final Map<Arc, Boolean>          isArcPreferred     = new LinkedHashMap<Arc, Boolean>();
     private final List<Node>                 nodes              = new ArrayList<Node>();
     private final Collection<Node>           waitPoints;
     private final boolean                    isEastbound;
     private final BigDecimal                 length;
 
     public ArcProgression(final Directed directed, final Arc... arcs) {
         this(directed, Arrays.asList(arcs));
     }
 
     public ArcProgression(final Directed directed, final Collection<Arc> arcs) {
         this.isEastbound = directed.isEastbound();
         // put arcs in proper order
         Node startingNode = this.getStartingNode(directed, arcs);
         while (arcs.size() != this.orderedArcs.size()) {
             for (final Arc a : arcs) {
                 if (a.getOrigin(directed) == startingNode) {
                     this.orderedArcs.addLast(a);
                     startingNode = this.orderedArcs.peekLast().getDestination(directed);
                     break;
                 }
             }
         }
         // cache information about nodes related to arcs
         BigDecimal milestone = BigDecimal.ZERO;
         for (final Arc a : this.orderedArcs) {
             this.milestones.put(milestone, a);
             this.arcsPerOrigin.put(a.getOrigin(this), a);
             this.arcsPerDestination.put(a.getDestination(this), a);
             this.nodes.add(a.getOrigin(this));
             milestone = milestone.add(a.getLength());
         }
         this.length = milestone;
         if (this.orderedArcs.size() > 0) {
             this.nodes.add(this.getDestination().getDestination(this));
         }
         // determine whether a particular arc is preferred
         for (final Arc a : this.orderedArcs) {
             this.isArcPreferred.put(a, this.determineArcPreferrence(a));
         }
         // and finally cache the wait points
         this.waitPoints = this.assembleWaitPoints();
     }
 
     private Collection<Node> assembleWaitPoints() {
         final Collection<Node> points = new TreeSet<Node>();
         if (this.orderedArcs.size() == 0) {
             return Collections.unmodifiableCollection(points);
         }
         // we want to be able to hold the train before it enters the network
         final Arc firstArc = this.getOrigin();
         points.add(firstArc.getOrigin(this));
         // other wait points depend on the type of the track
         for (final Arc a : this.orderedArcs) {
             switch (a.getTrack()) {
                 case SIDING:
                     // on sidings, wait before leaving them through a switch
                     points.add(a.getDestination(this));
                     break;
                 case CROSSOVER:
                     // on crossovers, wait before joining them
                     points.add(a.getOrigin(this));
                     break;
                 default:
                     /*
                      * we don't wait on main tracks not to block them; also, we don't wait on switches, since then the train is
                      * reaching over to a main track and blocking it. sidings are close enough to not need to wait at switches.
                      */
             }
         }
         return Collections.unmodifiableCollection(points);
     }
 
     public boolean contains(final Arc a) {
         return this.orderedArcs.contains(a);
     }
 
     public int countArcs() {
         return this.orderedArcs.size();
     }
 
     private boolean determineArcPreferrence(final Arc a) {
         if (a.getTrack() == Track.MAIN_0) {
             return true;
         } else if (a.getTrack() == Track.MAIN_2) {
             return this.isEastbound();
         } else if (a.getTrack() == Track.MAIN_1) {
             return this.isWestbound();
         } else {
             // preference of SIDING/SWITCH/CROSSOVER is based on which track are those coming off of
             final Arc previousArc = this.getPrevious(a);
             if (previousArc == null) {
                 return true;
             } else {
                 return this.determineArcPreferrence(previousArc);
             }
         }
     }
 
     public List<Arc> getArcs() {
         return Collections.unmodifiableList(this.orderedArcs);
     }
 
     public Arc getDestination() {
         return this.orderedArcs.peekLast();
     }
 
     public BigDecimal getDistance(final Node end) {
         final Node start = this.getOrigin().getOrigin(this);
         return this.getDistance(start, end);
     }
 
     public BigDecimal getDistance(final Node start, final Node end) {
         // first solve some corner cases
         if (!this.nodes.contains(start)) {
             throw new IllegalArgumentException(start + " not in progression!");
         } else if (!this.nodes.contains(end)) {
             throw new IllegalArgumentException(end + " not in progression!");
         } else if (start == end) {
             return BigDecimal.ZERO;
         }
         // then make sure nodes are in a proper order
         final int startIndex = this.nodes.indexOf(start);
         final int endIndex = this.nodes.indexOf(end);
         // and then retrieve the actual distance
         final int startWith = Math.min(startIndex, endIndex);
         final int endWith = Math.max(startIndex, endIndex);
         BigDecimal result = BigDecimal.ZERO;
        for (int i = startWith; i < endWith; i++) {
             final Arc a = this.getWithOriginNode(this.nodes.get(i));
             result = result.add(a.getLength());
         }
         return result;
     }
 
     public BigDecimal getLength() {
         return this.length;
     }
 
     public Arc getNext(final Arc a) {
         if (this.orderedArcs.size() == 0) {
             throw new IllegalArgumentException("No next arc on an empty route.");
         } else if (a == null) {
             return this.getOrigin();
         }
         final int indexOf = this.orderedArcs.indexOf(a);
         if (indexOf < 0) {
             throw new IllegalArgumentException("Arc not in the progression!");
         } else if (indexOf == this.orderedArcs.size() - 1) {
             return null;
         } else {
             return this.orderedArcs.get(indexOf + 1);
         }
 
     }
 
     public Node getNext(final Node n) {
         if (this.nodes.size() == 0) {
             throw new IllegalArgumentException("No next node on an empty route.");
         } else if (n == null) {
             return this.getOrigin().getOrigin(this);
         }
         final int indexOf = this.nodes.indexOf(n);
         if (indexOf < 0) {
             throw new IllegalArgumentException("Node not in the progression!");
         } else if (indexOf == this.nodes.size() - 1) {
             return null;
         } else {
             return this.nodes.get(indexOf + 1);
         }
     }
 
     public List<Node> getNodes() {
         return Collections.unmodifiableList(this.nodes);
     }
 
     public OccupationTracker getOccupiedArcs(final BigDecimal endingMilestone,
             final BigDecimal backtrack) {
         if (endingMilestone.signum() < 0) {
             throw new IllegalArgumentException("Please provide a milestone >= 0.");
         }
         if (backtrack.signum() <= 0) {
             throw new IllegalArgumentException("Please provide a backtrack > 0.");
         }
         final BigDecimal startingMilestone = endingMilestone.subtract(backtrack);
         if (startingMilestone.compareTo(this.getLength()) > 0) {
             return OccupationTracker.Builder.empty();
         }
         final Builder b = new OccupationTracker.Builder(this);
         // find the farthest away occupied arc
         final SortedMap<BigDecimal, Arc> post = this.milestones.headMap(endingMilestone);
         final BigDecimal endingWith = post.size() == 0 ? this.milestones.lastKey() : post.lastKey();
         // determine how much must be occupied in other arcs
         final BigDecimal occupied = endingMilestone.subtract(endingWith);
         BigDecimal leftToOccupy = backtrack.subtract(occupied);
         Arc currentArc = this.milestones.get(endingWith);
         if (!leftToOccupy.equals(backtrack)) { // something has been occupied
             b.addTo(currentArc, occupied);
         }
         while (leftToOccupy.signum() > 0) {
             // now occupy every other arc for as long as necessary
             currentArc = this.getPrevious(currentArc);
             if (currentArc == null) {
                 break;
             }
             if (leftToOccupy.compareTo(currentArc.getLength()) < 0) {
                 // can no longer occupy the whole arc
                 b.addFrom(currentArc, currentArc.getLength().subtract(leftToOccupy));
                 break;
             } else {
                 // occupy the whole arc and continue to another
                 b.addWhole(currentArc);
             }
             leftToOccupy = leftToOccupy.subtract(currentArc.getLength());
         }
         return b.build();
     }
 
     public Arc getOrigin() {
         return this.orderedArcs.peekFirst();
     }
 
     public Arc getPrevious(final Arc a) {
         if (this.orderedArcs.size() == 0) {
             throw new IllegalArgumentException("No previous arc on an empty route.");
         } else if (a == null) {
             return this.getDestination();
         }
         final int indexOf = this.orderedArcs.indexOf(a);
         if (indexOf < 0) {
             throw new IllegalArgumentException("Arc not in the progression!");
         } else if (indexOf == 0) {
             return null;
         } else {
             return this.orderedArcs.get(indexOf - 1);
         }
     }
 
     public Node getPrevious(final Node n) {
         if (this.nodes.size() == 0) {
             throw new IllegalArgumentException("No previous node on an empty route.");
         } else if (n == null) {
             return this.getDestination().getDestination(this);
         }
         final int indexOf = this.nodes.indexOf(n);
         if (indexOf < 0) {
             throw new IllegalArgumentException("Node not in the progression!");
         } else if (indexOf == 0) {
             return null;
         } else {
             return this.nodes.get(indexOf - 1);
         }
     }
 
     private Node getStartingNode(final Directed r, final Collection<Arc> arcs) {
         final Set<Node> isDestination = new HashSet<Node>();
         final Set<Node> isOrigin = new HashSet<Node>();
         for (final Arc a : arcs) {
             isOrigin.add(a.getOrigin(r));
             isDestination.add(a.getDestination(r));
         }
         isOrigin.removeAll(isDestination);
         if (isOrigin.size() == 1) {
             return isOrigin.toArray(new Node[1])[0];
         } else {
             return null;
         }
     }
 
     public Collection<Node> getWaitPoints() {
         return this.waitPoints;
     }
 
     public Arc getWithDestinationNode(final Node n) {
         return this.arcsPerDestination.get(n);
     }
 
     public Arc getWithOriginNode(final Node n) {
         return this.arcsPerOrigin.get(n);
     }
 
     /**
      * Returns arc progression that is a subset of this arc progression. The subset starts at the beginning and ends with the
      * arc that has the specified node as its ending.
      * 
      * @param n Node to end the arc progression.
      * @return Subset from nodes 0 to n. If the node is the ending node, returns this. If the node is the starting node, returns
      *         empty progression.
      */
     public ArcProgression head(final Node n) {
         if (!this.nodes.contains(n)) {
             throw new IllegalArgumentException(n + " not in progression!");
         } else if (n == this.getDestination().getDestination(this)) {
             return this;
         } else if (n == this.getOrigin().getOrigin(this)) {
             return new ArcProgression(this, new Arc[0]);
         }
         final Arc a = this.getWithDestinationNode(n);
         assert a != null;
         final int indexOf = this.orderedArcs.indexOf(a);
         return new ArcProgression(this, this.orderedArcs.subList(0, indexOf + 1));
     }
 
     @Override
     public boolean isEastbound() {
         return this.isEastbound;
     }
 
     public boolean isPreferred(final Arc a) {
         return this.isArcPreferred.get(a);
     }
 
     @Override
     public boolean isWestbound() {
         return !this.isEastbound();
     }
 
     /**
      * Returns arc progression that is a subset of this arc progression. The subset starts with the arc that has the specified
      * node as its beginning, ends where this progression ends.
      * 
      * @param n Node to start the arc progression.
      * @return Subset from nodes n to end. If the node is the ending node, returns empty progression. If the node is the
      *         starting node, returns this.
      */
     public ArcProgression tail(final Node n) {
         if (!this.nodes.contains(n)) {
             throw new IllegalArgumentException(n + " not in progression!");
         } else if (n == this.getDestination().getDestination(this)) {
             return new ArcProgression(this, new Arc[0]);
         } else if (n == this.getOrigin().getOrigin(this)) {
             return this;
         }
         final Arc a = this.getWithOriginNode(n);
         assert a != null;
         final int indexOf = this.orderedArcs.indexOf(a);
         return new ArcProgression(this, this.orderedArcs.subList(indexOf, this.orderedArcs.size()));
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("ArcProgression [orderedArcs=").append(this.orderedArcs).append("]");
         return builder.toString();
     }
 }
