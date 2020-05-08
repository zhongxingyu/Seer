 package org.dancres.peers;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicReference;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.google.gson.Gson;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ConsistentHashRing {
     private static final String RING_MEMBERSHIP = "org.dancres.peers.consistentHashRing.ringMembership";
 
     private static final Logger _logger = LoggerFactory.getLogger(ConsistentHashRing.class);
 
     public static class RingPosition implements Comparable {
         private final String _peerName;
         private final Integer _position;
         private final long _birthDate;
 
         RingPosition(Peer aPeer, Integer aPosition) {
             this(aPeer, aPosition, System.currentTimeMillis());
         }
 
         RingPosition(Peer aPeer, Integer aPosition, long aBirthDate) {
             _peerName = aPeer.getAddress();
             _position = aPosition;
             _birthDate = aBirthDate;
         }
 
         public Integer getPosition() {
             return _position;
         }
 
         boolean bounces(RingPosition anotherPosn) {
             return _birthDate < anotherPosn._birthDate;
         }
 
         boolean isLocal(Peer aPeer) {
             return _peerName.equals(aPeer.getAddress());
         }
 
         public int compareTo(Object anObject) {
             RingPosition myOther = (RingPosition) anObject;
 
             return (_position - myOther._position);
         }
 
         public int hashCode() {
             return _peerName.hashCode() ^ _position.hashCode();
         }
 
         public boolean equals(Object anObject) {
             if (anObject instanceof RingPosition) {
                 RingPosition myOther = (RingPosition) anObject;
 
                 if (_peerName.equals(myOther._peerName))
                     return (compareTo(anObject) == 0);
             }
 
             return false;
         }
 
         public String toString() {
             return "RingPosn: " + _position + " @ " + _peerName + " born: " + _birthDate;
         }
     }
 
     public static class RingPositions {
         private final Long _generation;
         private final Set<RingPosition> _positions;
 
         RingPositions() {
             _generation = 0L;
             _positions = new HashSet<RingPosition>();
         }
 
         public RingPositions(long aGeneration, HashSet<RingPosition> aPositions) {
             _generation = aGeneration;
             _positions = aPositions;
         }
 
         boolean supercedes(RingPositions aPositions) {
             return _generation > aPositions._generation;
         }
 
         RingPositions add(Collection<RingPosition> aPositions) {
             HashSet<RingPosition> myPositions = new HashSet<RingPosition>(_positions);
             myPositions.addAll(aPositions);
 
             return new RingPositions(_generation + 1, myPositions);
         }
 
         RingPositions remove(Collection<RingPosition> aPositions) {
             HashSet<RingPosition> myPositions = new HashSet<RingPosition>(_positions);
             myPositions.removeAll(aPositions);
 
             return new RingPositions(_generation + 1, myPositions);
         }
 
         public Set<RingPosition> getPositions() {
             return Collections.unmodifiableSet(_positions);
         }
 
         public String toString() {
             return "RingPosns: " + _generation + " => " + _positions;
         }
 
         public boolean equals(Object anObject) {
             if (anObject instanceof RingPositions) {
                 RingPositions myPositions = (RingPositions) anObject;
 
                 return ((_generation.equals(myPositions._generation)) &&
                         (Sets.symmetricDifference(_positions, myPositions._positions).size() == 0));
             }
 
             return false;
         }
     }
 
     public static class NeighbourRelation {
         private final RingPosition _neighbour;
         private final RingPosition _owned;
 
         NeighbourRelation(RingPosition aNeighbour, RingPosition aLocal) {
             _neighbour = aNeighbour;
             _owned = aLocal;
         }
 
         public RingPosition getNeighbour() {
             return _neighbour;
         }
 
         public RingPosition getOwned() {
             return _owned;
         }
 
         public boolean equals(Object anObject) {
             if (anObject instanceof NeighbourRelation) {
                 NeighbourRelation myOther = (NeighbourRelation) anObject;
 
                 return ((_neighbour.equals(myOther._neighbour)) & (_owned.equals(myOther._owned)));
             }
 
             return false;
         }
 
         public int hashCode() {
             return _neighbour.hashCode() ^ _owned.hashCode();
         }
 
         public String toString() {
             return "NRel: " + _neighbour + ", " + _owned;
         }
     }
 
     private final Peer _peer;
     private final Directory _dir;
     private final Random _rng = new Random();
     private final List<Listener> _listeners = new CopyOnWriteArrayList<Listener>();
 
     /**
      * The positions held by each node identified by address
      */
     private final ConcurrentMap<String, RingPositions> _ringPositions = new ConcurrentHashMap<String, RingPositions>();
 
     /**
      * The neighbour relations - which positions are closest whilst still less than our own
      */
     private final AtomicReference<HashSet<NeighbourRelation>> _neighbours =
             new AtomicReference<HashSet<NeighbourRelation>>(new HashSet<NeighbourRelation>());
 
     public ConsistentHashRing(Peer aPeer) {
         _peer = aPeer;
         _dir = (Directory) aPeer.find(Directory.class);
 
         if (_dir == null)
             throw new RuntimeException("ConsistentHashRing couldn't locate a Directory service in peer");
 
         _ringPositions.put(_peer.getAddress(), new RingPositions());
 
         _dir.add(new AttrProducerImpl());
         _dir.add(new DirListenerImpl());
     }
 
     private class AttrProducerImpl implements Directory.AttributeProducer {
         public Map<String, String> produce() {
             Map<String, String> myFlattenedRingPosns = new HashMap<String, String>();
 
             myFlattenedRingPosns.put(RING_MEMBERSHIP, flattenRingPositions(_ringPositions.get(_peer.getAddress())));
 
             return myFlattenedRingPosns;
         }
     }
 
     private String flattenRingPositions(RingPositions aPositions) {
        return new Gson().toJson(_ringPositions.get(_peer.getAddress()));
     }
 
     private RingPositions extractRingPositions(Directory.Entry anEntry) {
         return new Gson().fromJson(anEntry.getAttributes().get(RING_MEMBERSHIP), RingPositions.class);
     }
 
     private class DirListenerImpl implements Directory.Listener {
         /**
          * A locally inserted position will be communicated to other nodes as we gossip. Other nodes though may
          * introduce no changes to the ring. <code>updated</code> is called for each gossip and sweep through the
          * directory whether there are changes or not. We rely on this to run a conflict resolution sweep
          * to do conflict resolution on our locally inserted positions. Thus at this moment we cannot avoid doing
          * conflict resolution in absence of ring changes.
          *
          * @todo Modify new/insertPosition to do a sweep for conflict resolution so that we can implement a no
          * gossip'd updates, no sweep optimisation.
          *
          * @param aDirectory
          * @param aNewPeers
          * @param anUpdatedPeers
          */
         public void updated(Directory aDirectory, List<Directory.Entry> aNewPeers,
                             List<Directory.Entry> anUpdatedPeers) {
 
             _logger.debug("Ring Update");
 
             for (Directory.Entry aNewEntry : Iterables.filter(aNewPeers, new Predicate<Directory.Entry>() {
                 public boolean apply(Directory.Entry entry) {
                     return entry.getAttributes().containsKey(RING_MEMBERSHIP);
                 }
             })) {
                 RingPositions myPeerPositions = extractRingPositions(aNewEntry);
 
                 _logger.debug("New positions from new: " + aNewEntry.getPeerName(), myPeerPositions);
 
                 /*
                  * Slightly naughty as there may be a more up to date version kicking around but that will get
                  * worked out over time
                  */
                 _ringPositions.put(aNewEntry.getPeerName(), myPeerPositions);
             }
 
             for (Directory.Entry anUpdatedEntry : Iterables.filter(anUpdatedPeers, new Predicate<Directory.Entry>() {
                 public boolean apply(Directory.Entry entry) {
                     return entry.getAttributes().containsKey(RING_MEMBERSHIP);
                 }
             })) {
                 RingPositions myPeerPositions = extractRingPositions(anUpdatedEntry);
                 RingPositions myPrevious = _ringPositions.get(anUpdatedEntry.getPeerName());
 
                 // Was the positions list updated?
                 //
                 if (myPrevious == null) {
                     _logger.debug("New positions from: " + anUpdatedEntry.getPeerName(), myPeerPositions);
 
                     /*
                      * Slightly naughty as there may be a more up to date version kicking around but that will get
                      * worked out over time
                      */
                     _ringPositions.put(anUpdatedEntry.getPeerName(), myPeerPositions);
                 } else {
                     if (myPeerPositions.supercedes(myPrevious)) {
                         _logger.debug("Updated positions from: " + anUpdatedEntry.getPeerName(), myPeerPositions);
 
                         _ringPositions.replace(anUpdatedEntry.getPeerName(), myPrevious, myPeerPositions);
                     }
                 }
             }
 
             RingRebuild myRingRebuild = rebuildRing(_ringPositions);
 
             if (! myRingRebuild._rejected.isEmpty()) {
                 RingPositions myOldPosns = _ringPositions.get(_peer.getAddress());
                 _ringPositions.replace(_peer.getAddress(), myOldPosns, myOldPosns.remove(myRingRebuild._rejected));
 
                 for (RingPosition myPosn : myRingRebuild._rejected) {
                     for (Listener anL : _listeners) {
                         anL.rejected(myPosn);
                     }
                 }
             }
 
             // No point in a diff if we're empty
             //
             if (myRingRebuild._newRing.isEmpty())
                 return;
 
             /*
              * JVM Bug! Seemingly if rebuildNeighbours does not return two completely independent sets, the following
              * clear and addAll will cause _changes to become empty in spite of the fact that it's possible duplicate
              * myNeighbourRebuild._neighbours is not empty. Another possibility is that a second final field in a simple
              * return object, as done with ring and neighbour computations, causes problems. Notably both methods
              * have required the same treatment to prevent loss of set contents and thus the latter seems more likely.
              * Perhaps something to do with stack scope/corruption?
 
             _logger.debug(Thread.currentThread() + " " + this + " Rebuild: " + myNeighbourRebuild._neighbours +
                     " " + System.identityHashCode(myNeighbourRebuild._neighbours));
             _logger.debug(Thread.currentThread() + " " + this + " Changes before: " + myNeighbourRebuild._changes +
                     " " + System.identityHashCode(myNeighbourRebuild._changes));
             _logger.debug(Thread.currentThread() + " " + this + " Neighbours before: " + _neighbours +
                     " " + System.identityHashCode(_neighbours));
 
             _neighbours.clear();
 
             _logger.debug(Thread.currentThread() + " " + this + " Rebuild after 1: " + myNeighbourRebuild._neighbours +
                     " " + System.identityHashCode(myNeighbourRebuild._neighbours));
             _logger.debug(Thread.currentThread() + " " + this + " Changes after 1: " + myNeighbourRebuild._changes +
                     " " + System.identityHashCode(myNeighbourRebuild._changes));
             _logger.debug(Thread.currentThread() + " " + this + " Neighbours after 1: " + _neighbours +
                     " " + System.identityHashCode(_neighbours));
 
             _neighbours.addAll(myNeighbourRebuild._neighbours);
 
             _logger.debug(Thread.currentThread() + " " + this + " Rebuild after 2: " + myNeighbourRebuild._neighbours +
                     " " + System.identityHashCode(myNeighbourRebuild._neighbours));
             _logger.debug(Thread.currentThread() + " " + this + " Changes after 2: " + myNeighbourRebuild._changes +
                     " " + System.identityHashCode(myNeighbourRebuild._changes));
             _logger.debug(Thread.currentThread() + " " + this + " Neighbours after 2: " + _neighbours +
                     " " + System.identityHashCode(_neighbours));
              */
 
             NeighboursRebuild myNeighbourRebuild =
                     rebuildNeighbours(myRingRebuild._newRing.values(), _neighbours.get(), _peer);
 
             _neighbours.set(myNeighbourRebuild._neighbours);
 
             if (! myNeighbourRebuild._changes.isEmpty())
                 for (Listener myL : _listeners)
                     for (NeighbourRelation myChange : myNeighbourRebuild._changes)
                         myL.newNeighbour(myChange._owned, myChange._neighbour);
         }
     }
 
     private static class RingRebuild {
         final Map<Integer, RingPosition> _newRing;
         final List<RingPosition> _rejected;
 
         RingRebuild(Map<Integer, RingPosition> aNewRing, List<RingPosition> aRejected) {
             _newRing = aNewRing;
             _rejected = aRejected;
         }
     }
 
     private RingRebuild rebuildRing(Map<String, RingPositions> aRingPositions) {
 
         /*
          * Re-build the ring from _ringPositions
          *
          * Doing collision resolution as we go. In the case where one of our positions is the loser, remove it
          * and report it to listeners.
          */
         Map<Integer, RingPosition> myNewRing = new HashMap<Integer, RingPosition>();
         List<RingPosition> myLocalRejections = new LinkedList<RingPosition>();
 
         for (RingPositions myRingPositions : aRingPositions.values()) {
             for (RingPosition myRingPosn : myRingPositions.getPositions()) {
                 RingPosition myConflict = myNewRing.get(myRingPosn.getPosition());
 
                 if (myConflict == null) {
                     myNewRing.put(myRingPosn.getPosition(), myRingPosn);
                 } else {
                     RingPosition myLoser;
 
                     _logger.debug("Got position conflict: " + myConflict + ", " + myRingPosn);
 
                     if (myConflict.bounces(myRingPosn)) {
                         _logger.debug("Loser in conflict (new posn): " + myRingPosn);
 
                         myLoser = myRingPosn;
 
                     } else {
                         _logger.debug("Loser in conflict (conflict): " + myConflict);
 
                         myLoser = myConflict;
                         myNewRing.put(myRingPosn.getPosition(), myRingPosn);
                     }
 
                     // Are we the losing peer?
                     //
                     if (myLoser.isLocal(_peer)) {
                         _logger.debug("We are the losing peer");
 
                         myLocalRejections.add(myLoser);
                     }
                 }
             }
         }
 
         // JVM workaround for clear and addAll
         // return new RingRebuild(myNewRing, new LinkedList<RingPosition>(myLocalRejections));
 
         return new RingRebuild(myNewRing, myLocalRejections);
     }
 
     private static class NeighboursRebuild {
         final HashSet<NeighbourRelation> _neighbours;
         final Set<NeighbourRelation> _changes;
 
         NeighboursRebuild(HashSet<NeighbourRelation> aNeighbours, Set<NeighbourRelation> aChanges) {
             _neighbours = aNeighbours;
             _changes = aChanges;
         }
     }
 
     private NeighboursRebuild rebuildNeighbours(Collection<RingPosition> aRing,
                                                          HashSet<NeighbourRelation> anOldNeighbours,
                                                          Peer aLocal) {
         HashSet<NeighbourRelation> myNeighbours = new HashSet<NeighbourRelation>();
         SortedSet<RingPosition> myRing = new TreeSet<RingPosition>(aRing);
         RingPosition myLast = myRing.last();
 
         for (RingPosition myPosn : myRing) {
             if (myPosn.isLocal(aLocal) && (! myPosn.equals(myLast))) {
                 myNeighbours.add(new NeighbourRelation(myLast, myPosn));
             }
 
             myLast = myPosn;
         }
 
         _logger.debug("Neighbour sets: " + anOldNeighbours + " vs\n" + myNeighbours);
 
         for (NeighbourRelation myNR : anOldNeighbours) {
             _logger.debug("Same: " + myNeighbours.contains(myNR));
         }
 
         /*
          * JVM Workaround - if this result is not wrapped in a new hashset, the clearAll/addAll in
          * DirectoryListenerImpl.update will cause the changes set to be empty!
          */
         Set<NeighbourRelation> myChanges = Sets.difference(myNeighbours, anOldNeighbours);
 
         _logger.debug("Neighbour diff: " + myChanges + " " + myChanges.equals(myNeighbours) + " " +
                 myChanges.equals(anOldNeighbours));
 
         // JVM workaround for clear and addAll
         // return new NeighboursRebuild(myNeighbours, new HashSet<NeighbourRelation>(myChanges));
 
         return new NeighboursRebuild(myNeighbours, myChanges);
     }
 
     public Set<NeighbourRelation> getNeighbours() {
         return Collections.unmodifiableSet(_neighbours.get());
     }
 
     public Collection<RingPosition> getCurrentRing() {
         return Collections.unmodifiableCollection(rebuildRing(_ringPositions)._newRing.values());
     }
 
     public RingPositions getCurrentPositions() {
         return _ringPositions.get(_peer.getAddress());
     }
 
     RingPosition insertPosition(RingPosition aPosn) {
         RingPositions myOldPosns = _ringPositions.get(_peer.getAddress());
         _ringPositions.replace(_peer.getAddress(), myOldPosns, myOldPosns.add(Collections.singletonList(aPosn)));
 
         return aPosn;
     }
 
     public RingPosition newPosition() {
         // Simply flatten _ringPositions to get a view of current ring, don't care about peers or collision detection
         //
         HashSet<Integer> myOccupiedPositions = new HashSet<Integer>();
 
         for (RingPositions myRPs : _ringPositions.values())
             for (RingPosition myRP : myRPs.getPositions())
                 myOccupiedPositions.add(myRP.getPosition());
 
         int myNewPos;
 
         do {
             myNewPos = _rng.nextInt();
         } while (myOccupiedPositions.contains(myNewPos));
 
         return insertPosition(new RingPosition(_peer, myNewPos));
     }
 
     public void add(Listener aListener) {
         _listeners.add(aListener);
     }
 
     /**
      * Takes a hashCode and returns the container to allocate it to.
      *
      * @param aHashCode
      * @return
      */
     public RingPosition allocate(Integer aHashCode) {
         throw new UnsupportedOperationException();
     }
 
     public static interface Listener {
         public void newNeighbour(RingPosition anOwnedPosition, RingPosition aNeighbourPosition);
 
         public void rejected(RingPosition anOwnedPosition);
     }
 }
