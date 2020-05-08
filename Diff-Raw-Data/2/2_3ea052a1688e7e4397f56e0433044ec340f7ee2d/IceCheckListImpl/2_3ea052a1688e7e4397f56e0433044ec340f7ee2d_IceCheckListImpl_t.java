 package org.lastbamboo.common.ice;
 
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidatePair;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairFactory;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairPriorityCalculator;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
 import org.lastbamboo.common.ice.candidate.IceCandidateVisitor;
 import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceTcpPeerReflexiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceTcpServerReflexiveSoCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpRelayCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
 import org.lastbamboo.common.util.Closure;
 import org.lastbamboo.common.util.CollectionUtils;
 import org.lastbamboo.common.util.CollectionUtilsImpl;
 import org.lastbamboo.common.util.Pair;
 import org.lastbamboo.common.util.PairImpl;
 import org.lastbamboo.common.util.Predicate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class containing data and state for an ICE check list.<p>
  * 
  * See: http://tools.ietf.org/html/draft-ietf-mmusic-ice-17#section-5.7
  */
 public class IceCheckListImpl implements IceCheckList
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     
     /**
      * The triggered check queue.  This is a FIFO queue of checks that the
      * course of the connectivity check process "triggers", typically through
      * the discovery of new peer reflexive candidates.  
      */
     private final Queue<IceCandidatePair> m_triggeredQueue = 
         new ConcurrentLinkedQueue<IceCandidatePair>();
     
     private final List<IceCandidatePair> m_pairs =
         new LinkedList<IceCandidatePair>();
 
     private volatile IceCheckListState m_state = IceCheckListState.RUNNING;
 
     private final Collection<IceCandidate> m_localCandidates;
 
     private final IceCandidatePairFactory m_iceCandidatePairFactory;
 
     /**
      * Creates a new check list, starting with only local candidates.
      * 
      * @param candidatePairFactory Factory for creating candidate pairs.
      * @param localCandidates The local candidates to use in the check list.
      */
     public IceCheckListImpl(
         final IceCandidatePairFactory candidatePairFactory,
         final Collection<IceCandidate> localCandidates) 
         {
         this.m_iceCandidatePairFactory = candidatePairFactory;
         this.m_localCandidates = localCandidates;
         m_log.debug("Using local candidates: {}", localCandidates);
         }
     
     public IceCandidatePair removeTopTriggeredPair()
         {
         while (!this.m_triggeredQueue.isEmpty())
             {
             final IceCandidatePair pair = this.m_triggeredQueue.poll();
             
             // Don't re-check nominated pairs.
             if (!pair.isNominated())
                 {
                 return pair;
                 }
             }
         return null;
         }
 
     public void setState(final IceCheckListState state)
         {
         if (this.m_state != IceCheckListState.COMPLETED)
             {
             this.m_state = state;
             synchronized (this)
                 {
                 m_log.debug("State changed to: {}", state);
                 this.notifyAll();
                 }
             }
         }
 
     public IceCheckListState getState()
         {
         return this.m_state;
         }
 
     public void check()
         {
         synchronized (this)
             {
             while (this.m_state == IceCheckListState.RUNNING)
                 {
                 try
                     {
                     wait();
                     }
                 catch (final InterruptedException e)
                     {
                     m_log.error("Interrupted??", e);
                     }
                 }
             }
         m_log.debug("Returning from check");
         }
 
     public boolean isActive()
         {
         // TODO: I believe this should depend on the state of the check list.  
        // The active state is used in determining the value of N in timer
         // computations.
         return false;
         }
 
     public void addTriggeredPair(final IceCandidatePair pair)
         {
         synchronized (this)
             {
             if (!this.m_triggeredQueue.contains(pair))
                 {
                 m_log.debug("Adding triggered pair:{}", pair);
                 this.m_triggeredQueue.add(pair);
                 }
             else
                 {
                 m_log.debug("Triggered queue already has pair:{}", pair);
                 }
             }
         }
     
     public void addPair(final IceCandidatePair pair)
         {
         if (pair == null)
             {
             m_log.error("Null pair");
             throw new NullPointerException("Null pair");
             }
         synchronized (this)
             {
             this.m_pairs.add(pair);
             Collections.sort(this.m_pairs);
             }
         }
 
     public void recomputePairPriorities(final boolean controlling)
         {
         synchronized (this)
             {
             recompute(this.m_triggeredQueue, controlling);
             recompute(this.m_pairs, controlling);
             sortPairs(this.m_pairs);
             }
         }
 
     private void recompute(final Collection<IceCandidatePair> pairs, 
         final boolean controlling)
         {
         final Closure<IceCandidatePair> closure = 
             new Closure<IceCandidatePair>()
             {
             public void execute(final IceCandidatePair pair)
                 {
                 final IceCandidate local = pair.getLocalCandidate();
                 final IceCandidate remote = pair.getRemoteCandidate();
                 local.setControlling(controlling);
                 
                 // Note we also set the controlling status of the remote 
                 // candidate because there's nothing in the SDP specifying the
                 // controlling status -- it's just an externally configured
                 // property based on starting roles and any role conflicts that
                 // may emerge over the course of establishing a media session.
                 remote.setControlling(!controlling);
                 pair.recomputePriority();
                 }
             };
         executeOnPairs(pairs, closure);
         }
 
     public void formCheckList(final Collection<IceCandidate> remoteCandidates)
         {
 
         final Collection<Pair<IceCandidate, IceCandidate>> pairs = 
             new LinkedList<Pair<IceCandidate,IceCandidate>>();
         
         for (final IceCandidate localCandidate : m_localCandidates)
             {
             for (final IceCandidate remoteCandidate : remoteCandidates)
                 {
                 if (shouldPair(localCandidate, remoteCandidate))
                     {
                     final Pair<IceCandidate, IceCandidate> pair =
                         new PairImpl<IceCandidate, IceCandidate>(localCandidate, 
                             remoteCandidate);
                     pairs.add(pair);
                     }
                 }
             }
         
         m_log.debug("Pairs before conversion: {}", pairs.size());
         
         // Convert server reflexive local candidates to their base and remove
         // pairs with TCP passive local candidates.
         final List<Pair<IceCandidate, IceCandidate>> convertedPairs = 
             convertPairs(pairs);
         m_log.debug("Pairs after conversion:  {}", convertedPairs.size());
         
         final Comparator<Pair<IceCandidate, IceCandidate>> comparator =
             new Comparator<Pair<IceCandidate, IceCandidate>>()
                 {
 
                 public int compare(
                     final Pair<IceCandidate, IceCandidate> pair1, 
                     final Pair<IceCandidate, IceCandidate> pair2)
                     {
                     final long pair1Priority = calculatePriority(pair1);
                     final long pair2Priority = calculatePriority(pair2);
                     
                     if (pair1Priority > pair2Priority) return -1;
                     if (pair1Priority < pair2Priority) return 1;
                     return 0;
                     }
                 
                 private long calculatePriority(
                     final Pair<IceCandidate, IceCandidate> pair)
                     {
                     return IceCandidatePairPriorityCalculator.calculatePriority(
                         pair.getFirst(), pair.getSecond());
                     }
                 };
 
         Collections.sort(convertedPairs, comparator);
         
         m_log.debug(convertedPairs.size()+" converted");
         final List<IceCandidatePair> pruned = prunePairs(convertedPairs);
         m_log.debug(pruned.size()+" after pruned");
         final List<IceCandidatePair> sorted = sortPairs(pruned);
         synchronized (this)
             {
             this.m_pairs.addAll(sorted);
             m_log.debug("Created pairs:\n"+this.m_pairs);
             }
         
         final Closure<IceCandidatePair> tcpTurnClosure =
             new Closure<IceCandidatePair>()
             {
             private final Set<InetAddress> addedAddresses =
                 new HashSet<InetAddress>();
             public void execute(final IceCandidatePair pair)
                 {
                 final IceCandidate remote = pair.getRemoteCandidate();
                 final InetAddress remoteAddress = 
                     remote.getSocketAddress().getAddress();
                 if (addedAddresses.contains(remoteAddress))
                     {
                     return;
                     }
                 if (!remoteAddress.isSiteLocalAddress() &&
                     !remoteAddress.isLinkLocalAddress() &&
                     !remoteAddress.isAnyLocalAddress())
                     {
                     addedAddresses.add(remoteAddress);
                     }
                 }
             };
         executeOnPairs(tcpTurnClosure);
         }
     
 
     private List<IceCandidatePair> sortPairs(
         final List<IceCandidatePair> pairs)
         {
         synchronized (this)
             {
             Collections.sort(pairs);
             }
         return pairs;
         }
 
     /**
      * Removes any TCP passive local pairs and converts pairs with a local
      * UDP server reflexive candidate to the associated base candidate.
      * 
      * @param pairs The pairs to convert.
      * @return The {@link List} of pairs with TCP passive pairs removed and
      * server reflexive local candidates converted to their bases.  See
      * ICE section 5.7.3.
      */
     private static List<Pair<IceCandidate, IceCandidate>> convertPairs(
         final Collection<Pair<IceCandidate, IceCandidate>> pairs)
         {
         final List<Pair<IceCandidate, IceCandidate>> convertedPairs = 
             new LinkedList<Pair<IceCandidate,IceCandidate>>();
         
         for (final Pair<IceCandidate, IceCandidate> pair : pairs)
             {
             final Pair<IceCandidate, IceCandidate> converted = convertPair(pair);
             if (converted != null)
                 {
                 convertedPairs.add(converted);
                 }
             }
         
         return convertedPairs;
         }
         
     private static Pair<IceCandidate, IceCandidate> convertPair(
         final Pair<IceCandidate, IceCandidate> pair)
         {
         final IceCandidate localCandidate = pair.getFirst();
         final IceCandidate remoteCandidate = pair.getSecond();
         
         // We have to convert all local UDP server reflexive candidates to
         // their base and we have to ignore all TCP passive candidates.
         final IceCandidateVisitor<Pair<IceCandidate, IceCandidate>> visitor =
             new IceCandidateVisitor<Pair<IceCandidate, IceCandidate>>()
             {
             public void visitCandidates(Collection<IceCandidate> candidates)
                 {
                 // Not used here.
                 }
             
             public Pair<IceCandidate, IceCandidate> visitUdpServerReflexiveCandidate(
                 final IceUdpServerReflexiveCandidate candidate)
                 {
                 // Convert server reflexive candidates to their base.
                 final IceCandidate base = candidate.getBaseCandidate();
                 return new PairImpl<IceCandidate, IceCandidate>(base, 
                     remoteCandidate);
                 }
 
             public Pair<IceCandidate, IceCandidate> visitTcpActiveCandidate(
                 final IceTcpActiveCandidate candidate)
                 {
                 return pair;
                 }
 
             public Pair<IceCandidate, IceCandidate> visitTcpHostPassiveCandidate(
                 final IceTcpHostPassiveCandidate candidate)
                 {
                 // Ignore all TCP passive candidates.
                 return null;
                 }
 
             public Pair<IceCandidate, IceCandidate> visitTcpRelayPassiveCandidate(
                 final IceTcpRelayPassiveCandidate candidate)
                 {
                 // Ignore all TCP passive candidates.
                 return null;
                 }
 
             public Pair<IceCandidate, IceCandidate> visitTcpServerReflexiveSoCandidate(
                 final IceTcpServerReflexiveSoCandidate candidate)
                 {
                 // TODO: We don't currently support TCP SO.
                 return null;
                 }
 
             public Pair<IceCandidate, IceCandidate> visitTcpPeerReflexiveCandidate(
                 final IceTcpPeerReflexiveCandidate candidate)
                 {
                 // Should not visit peer reflexive in check lists.
                 return null;
                 }
 
             public Pair<IceCandidate, IceCandidate> visitUdpHostCandidate(
                 final IceUdpHostCandidate candidate)
                 {
                 return pair;
                 }
 
             public Pair<IceCandidate, IceCandidate> visitUdpPeerReflexiveCandidate(
                 final IceUdpPeerReflexiveCandidate candidate)
                 {
                 // This can happen when the answerer starts checks before the
                 // offerer has received the answer.  The offerer might get
                 // a peer reflexive candidate before it's had a chance to form
                 // its checklist.
                 return pair;
                 }
 
             public Pair<IceCandidate, IceCandidate> visitUdpRelayCandidate(
                 final IceUdpRelayCandidate candidate)
                 {
                 return pair;
                 }
             };
         
         return localCandidate.accept(visitor);
         }
 
     /**
      * Prunes pairs by converting any non-host local candidates to host 
      * candidates and removing any duplicates created.  The pairs should already
      * be ordered by priority when this method is called.
      * 
      * @param pairs The pairs to prune.  This {@link List} MUST already be
      * sorted by pair priority prior to this call.
      */
     private List<IceCandidatePair> prunePairs(
         final List<Pair<IceCandidate, IceCandidate>> pairs)
         {
         // Note the pairs override hashCode using the local and the remote
         // candidates.  We just use the map here to identify pairs with the
         // same address for the local and the remote candidates.  This is
         // possible because we just converted local server reflexive 
         // candidates to their associated bases, according to the algorithm.
         //
         // If we find a duplicate pair, we always take the one with the 
         // higher priority.
         final List<IceCandidatePair> prunedPairs = 
             new LinkedList<IceCandidatePair>();
         final Set<Pair<IceCandidate, IceCandidate>> seenPairs =
             new HashSet<Pair<IceCandidate, IceCandidate>>();
         
         for (final Pair<IceCandidate, IceCandidate> pair : pairs)
             {
             if (!seenPairs.contains(pair))
                 {
                 seenPairs.add(pair);
                 prunedPairs.add(createPair(pair));
                 }
             }
         
         // Limit attacks based on the number of pairs.
         if (prunedPairs.size() > 100)
             {
             return prunedPairs.subList(0, 100);
             }
         return prunedPairs;
         }
 
     private IceCandidatePair createPair(
         final Pair<IceCandidate, IceCandidate> pair)
         {
         final IceCandidate localCandidate = pair.getFirst();
         final IceCandidate remoteCandidate = pair.getSecond();
         return m_iceCandidatePairFactory.newPair(localCandidate, 
             remoteCandidate);
         }
     
 
     private static boolean shouldPair(final IceCandidate localCandidate, 
         final IceCandidate remoteCandidate)
         {
         // This is specified in ICE section 5.7.1
         return (
             (localCandidate.getComponentId() == 
             remoteCandidate.getComponentId()) &&
             addressTypesMatch(localCandidate, remoteCandidate) &&
             transportTypesMatch(localCandidate, remoteCandidate));
         }
 
     private static boolean addressTypesMatch(final IceCandidate localCandidate, 
         final IceCandidate remoteCandidate)
         {
         final InetAddress localAddress = 
             localCandidate.getSocketAddress().getAddress();
         final InetAddress remoteAddress =
             remoteCandidate.getSocketAddress().getAddress();
         
         final boolean localIsIpV4 = localAddress instanceof Inet4Address;
         final boolean remoteIsIpV4 = remoteAddress instanceof Inet4Address;
         
         if (localIsIpV4)
             {
             return remoteIsIpV4;
             }
         else
             {
             return !remoteIsIpV4;
             }
         }
     
     private static boolean transportTypesMatch(
         final IceCandidate localCandidate, final IceCandidate remoteCandidate)
         {
         final IceTransportProtocol localTransport = 
             localCandidate.getTransport();
         final IceTransportProtocol remoteTransport =
             remoteCandidate.getTransport();
         switch (localTransport)
             {
             case UDP:
                 return remoteTransport == IceTransportProtocol.UDP;
             case TCP_SO:
                 return remoteTransport == IceTransportProtocol.TCP_SO;
             case TCP_ACT:
                 return remoteTransport == IceTransportProtocol.TCP_PASS;
             case TCP_PASS:
                 return remoteTransport == IceTransportProtocol.TCP_ACT;
             }
         return false;
         }
 
     public boolean hasHigherPriorityPendingPair(final IceCandidatePair pair)
         {
         final long priority = pair.getPriority();
         final Predicate<IceCandidatePair> triggeredPred =
             new Predicate<IceCandidatePair>()
             {
             public boolean evaluate(final IceCandidatePair curPair)
                 {
                 if (curPair.getPriority() > priority) return true;
                 return false;
                 }
             };
         
         if (matchesAny(this.m_triggeredQueue, triggeredPred))
             {
             return true;
             }
         
         final Predicate<IceCandidatePair> pred =
             new Predicate<IceCandidatePair>()
             {
 
             public boolean evaluate(final IceCandidatePair curPair)
                 {
                 if (curPair.getPriority() > priority)
                     {
                     final IceCandidatePairState state = curPair.getState();
                     
                     switch (state)
                         {
                         case FROZEN:
                             // Fall through.
                         case WAITING:
                             // Fall through.
                         case IN_PROGRESS:
                             return true;
                         case SUCCEEDED:
                             // Fall through.
                         case FAILED:
                             return false;
                         }
                     }
                 return false;
                 }
             };
         
         return matchesAny(pred);
         }
 
     public void removeWaitingAndFrozenPairs(
         final IceCandidatePair nominatedPair)
         {
         m_log.debug("Removing waiting and frozen pairs...");
         final Predicate<IceCandidatePair> pred = 
             new Predicate<IceCandidatePair>()
             {
             public boolean evaluate(final IceCandidatePair curPair)
                 {
                 final IceCandidatePairState state = curPair.getState();
                 switch (state)
                     {
                     case FROZEN:
                         // Fall through.
                     case WAITING:
                         m_log.debug("Closing pair:\n{}", curPair);
                         curPair.close();
                         /*
                         if (curPair.isTcp())
                             {
                             curPair.close();
                             }
                         else 
                             {
                             final IceCandidate nominatedLocal = 
                                 pair.getLocalCandidate();
                             final IceCandidate local = 
                                 curPair.getLocalCandidate();
                             
                             // For UDP, we need to check that the local 
                             // candidate is not the same as the local 
                             // candidate for the nominated pair.  If it is, 
                             // this will inadvertently close the session for 
                             // the nominated pair, which would be bad!  
                             if (!local.getSocketAddress().equals(
                                 nominatedLocal.getSocketAddress()))
                                 {
                                 curPair.close();
                                 }
                             }
                             */
                         return true;
                     case IN_PROGRESS:
                         // The following is at SHOULD strength in 8.1.2.  We
                         // do this for both triggered checks and the check list
                         // because I see no reason not to cancel the triggered
                         // one here too, although the draft seems to indicate
                         // we should only do it for the normal check list.
                         if (curPair.getPriority() < nominatedPair.getPriority())
                             {
                             m_log.debug("Canceling IN-PROGRESS pair {}", 
                                 curPair);
                             curPair.cancelStunTransaction();
                             }
                         else
                             {
                             m_log.debug("Not canceling higher priority " +
                                 "IN-PROGRESS pair: {}", curPair);
                             }
                         break;
                     case SUCCEEDED:
                         // Do nothing.
                     case FAILED:
                         // Do nothing.
                     }
                 return false;
                 }
             };
             
         synchronized (this)
             {
             for (final Iterator<IceCandidatePair> iter = m_pairs.iterator(); 
                 iter.hasNext();)
                 {
                 final IceCandidatePair curPair = iter.next();
                 if (pred.evaluate(curPair))
                     {
                     iter.remove();
                     }
                 }
             for (final Iterator<IceCandidatePair> iter = m_triggeredQueue.iterator();
                 iter.hasNext();)
                 {
                 final IceCandidatePair curPair = iter.next();
                 if (pred.evaluate(curPair))
                     {
                     iter.remove();
                     }
                 }
             }
         }
 
     public void executeOnPairs(final Closure<IceCandidatePair> closure)
         {
         executeOnPairs(this.m_pairs, closure);
         }
 
     public IceCandidatePair selectPair(final Predicate<IceCandidatePair> pred)
         {
         synchronized (this)
             {
             final CollectionUtils utils = new CollectionUtilsImpl();
             return utils.selectFirst(this.m_pairs, pred);
             }
         }
     
     public IceCandidatePair selectAnyPair(final Predicate<IceCandidatePair> pred)
         {
         synchronized (this)
             {
             final CollectionUtils utils = new CollectionUtilsImpl();
             final IceCandidatePair pair = utils.selectFirst(this.m_pairs, pred);
             if (pair != null)
                 {
                 return pair;
                 }
             return utils.selectFirst(this.m_triggeredQueue, pred);
             }
         }
 
     public boolean matchesAny(final Predicate<IceCandidatePair> pred)
         {
         return matchesAny(this.m_pairs, pred);
         }
 
     private boolean matchesAny(final Collection<IceCandidatePair> pairs,
         final Predicate<IceCandidatePair> pred)
         {
         synchronized (this)
             {
             final CollectionUtils utils = new CollectionUtilsImpl();
             return utils.matchesAny(pairs, pred);
             }
         }
     
     private void executeOnPairs(final Collection<IceCandidatePair> pairs, 
         final Closure<IceCandidatePair> closure)
         {
         synchronized (this)
             {
             final CollectionUtils utils = new CollectionUtilsImpl();
             utils.forAllDo(pairs, closure);
             }
         }
 
     public void close()
         {
         m_log.debug("Closing check list...");
         final Closure<IceCandidatePair> close = new Closure<IceCandidatePair>()
             {
             public void execute(final IceCandidatePair pair)
                 {
                 pair.close();
                 }
             };
         
         executeOnPairs(close);
         executeOnPairs(this.m_triggeredQueue, close);
         }
     }
