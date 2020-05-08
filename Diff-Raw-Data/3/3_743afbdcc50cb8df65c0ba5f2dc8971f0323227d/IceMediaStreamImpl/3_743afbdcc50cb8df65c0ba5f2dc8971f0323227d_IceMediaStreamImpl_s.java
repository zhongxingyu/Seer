 package org.lastbamboo.common.ice;
 
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.math.RandomUtils;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidateGatherer;
 import org.lastbamboo.common.ice.candidate.IceCandidateGathererImpl;
 import org.lastbamboo.common.ice.candidate.IceCandidatePair;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
 import org.lastbamboo.common.ice.candidate.IceTcpPeerReflexiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpPeerReflexiveCandidate;
 import org.lastbamboo.common.ice.sdp.IceCandidateSdpEncoder;
 import org.lastbamboo.common.stun.client.StunClient;
 import org.lastbamboo.common.stun.client.StunClientFactory;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.stun.stack.message.attributes.StunAttribute;
 import org.lastbamboo.common.stun.stack.message.attributes.StunAttributeType;
 import org.lastbamboo.common.stun.stack.message.attributes.ice.IcePriorityAttribute;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
import org.lastbamboo.common.turn.client.TcpFrameTurnClientListener;
import org.lastbamboo.common.turn.client.TurnClientListener;
import org.lastbamboo.common.turn.http.server.ServerDataFeeder;
 import org.lastbamboo.common.util.Closure;
 import org.lastbamboo.common.util.Predicate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class containing an ICE media stream.  Each media stream contains a single
  * ICE check list, as described in ICE section 5.7.
  */
 public class IceMediaStreamImpl<T> implements IceMediaStream
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     private final IceCheckList m_checkList;
     
     private final Queue<IceCandidatePair> m_validPairs =
         new PriorityQueue<IceCandidatePair>();
     
     /**
      * {@link Queue} of nominated pairs for this check list.
      */
     private final Queue<IceCandidatePair> m_nominatedPairs = 
         new PriorityQueue<IceCandidatePair>();
 
     private final Collection<IceCandidate> m_localCandidates;
     private final IceAgent m_iceAgent;
     private final IceMediaStreamDesc m_desc;
     private final Collection<IceCandidate> m_remoteCandidates = 
         new LinkedList<IceCandidate>();
     
     public IceMediaStreamImpl(final StunClientFactory<T> stunClientFactory,
         final IceAgent iceAgent, final IceMediaStreamDesc desc, 
         final IceStunCheckerFactory checkerFactory,
         final StunTransactionTracker<StunMessage> transactionTracker)
         {
         this(stunClientFactory, iceAgent, desc, null, checkerFactory, 
             transactionTracker);
         }
 
     public IceMediaStreamImpl(final StunClientFactory<T> stunClientFactory,
         final IceAgent iceAgent, 
         final IceMediaStreamDesc desc, final StunClient udpStunClient, 
         final IceStunCheckerFactory checkerFactory, 
         final StunTransactionTracker<StunMessage> transactionTracker)
         {
         m_iceAgent = iceAgent;
         m_desc = desc;
 
         final StunMessageVisitorFactory<T> messageVisitorFactory =
             new IceStunConnectivityCheckerFactory(iceAgent, 
                 this, transactionTracker, checkerFactory);
         final StunClient udpStunPeer;
         if (udpStunClient == null && desc.isUdp())
             {
             udpStunPeer = 
                 new IceStunUdpPeer(messageVisitorFactory, 
                     iceAgent.isControlling());
             }
         else
             {
             udpStunPeer = udpStunClient;
             }
         
         final StunClient tcpTurnClient = 
             stunClientFactory.newStunClient(messageVisitorFactory);
         final StunClient tcpIceClient = 
             new IceStunTcpPeer(tcpTurnClient, messageVisitorFactory, 
                 iceAgent.isControlling());
         final IceCandidateGatherer gatherer =
             new IceCandidateGathererImpl(tcpIceClient, udpStunPeer, 
                 iceAgent.isControlling(), desc);
         this.m_localCandidates = gatherer.gatherCandidates();
         this.m_checkList = 
             new IceCheckListImpl(checkerFactory, this.m_localCandidates, 
                 messageVisitorFactory);
         }
 
     public byte[] encodeCandidates()
         {
         final IceCandidateSdpEncoder encoder = 
             new IceCandidateSdpEncoder(m_desc.getMimeContentType(), 
                 m_desc.getMimeContentSubtype());
         encoder.visitCandidates(m_localCandidates);
         return encoder.getSdp();
         }
     
     public void establishStream(final Collection<IceCandidate> remoteCandidates)
         {
         synchronized (this.m_remoteCandidates)
             {
             synchronized (remoteCandidates)
                 {
                 this.m_remoteCandidates.addAll(remoteCandidates);
                 }
             }
         
         m_checkList.formCheckList(remoteCandidates);
         
         // TODO: We currently skip this step because it eliminates our extra
         // UDP candidates for trying to cross NATs with port and address
         // dependent mapping.
         //processPairGroups();
         
         final IceCheckScheduler scheduler = 
             new IceCheckSchedulerImpl(this.m_iceAgent, this, m_checkList);
         scheduler.scheduleChecks();
 
         m_checkList.check();
         }
     
     public IceCandidate addRemotePeerReflexive(final BindingRequest request,
         final InetSocketAddress localAddress, 
         final InetSocketAddress remoteAddress, final boolean isUdp)
         {
         // See ICE section 7.2.1.3.
         final Map<StunAttributeType, StunAttribute> attributes = 
             request.getAttributes();
         final IcePriorityAttribute priorityAttribute = 
             (IcePriorityAttribute) attributes.get(
                 StunAttributeType.ICE_PRIORITY);
         final long priority = priorityAttribute.getPriority();
         
         // We set the foundation to a random value.
         final String foundation = String.valueOf(RandomUtils.nextLong());
         
         // Find the local candidate for the address the request was sent to.
         // We use that to determine the component ID of the new peer
         // reflexive candidate.
         final IceCandidate localCandidate = 
             getLocalCandidate(localAddress, isUdp);
         if (localCandidate == null)
             {
             m_log.warn("Could not find local candidate.  Aborting: "+
                 localAddress);
             return null;
             }
         final int componentId = localCandidate.getComponentId();
         
         m_log.debug("Creating new peer reflexive candidate");
         final IceCandidate prc;
         if (isUdp)
             {
             prc = new IceUdpPeerReflexiveCandidate(remoteAddress, foundation, 
                 componentId, this.m_iceAgent.isControlling(), priority);
             }
         else
             {
             prc = new IceTcpPeerReflexiveCandidate(remoteAddress, foundation, 
                 componentId, this.m_iceAgent.isControlling(), priority);
             }
 
         this.m_remoteCandidates.add(prc);
         return prc;
         }
 
     /**
      * Groups the pairs as specified in ICE section 5.7.4. The purpose of this
      * grouping appears to be just to set the establish the waiting pair for
      * each foundation prior to running connectivity checks.
      * 
      * @param pairs The pairs to form into foundation-based groups for setting 
      * the state of the pair with the lowest component ID to waiting.
      */
     private void processPairGroups()
         {
         final Map<String, List<IceCandidatePair>> groupsMap = 
             new HashMap<String, List<IceCandidatePair>>();
         
         // Group together pairs with the same foundation.
         final Closure<IceCandidatePair> groupClosure = 
             new Closure<IceCandidatePair>()
             {
             public void execute(final IceCandidatePair pair)
                 {
                 final String foundation = pair.getFoundation();
                 final List<IceCandidatePair> foundationPairs;
                 if (groupsMap.containsKey(foundation))
                     {
                     foundationPairs = groupsMap.get(foundation);
                     }
                 else
                     {
                     foundationPairs = new LinkedList<IceCandidatePair>();
                     groupsMap.put(foundation, foundationPairs);
                     }
                 foundationPairs.add(pair);
                 }
             };
         this.m_checkList.executeOnPairs(groupClosure);
         
         final Collection<List<IceCandidatePair>> groups = 
             groupsMap.values();
         
         m_log.debug(groups.size()+ " before sorting...");
         for (final List<IceCandidatePair> group : groups)
             {
             setLowestComponentIdToWaiting(group);
             }
         }
     
     private void setLowestComponentIdToWaiting(
         final List<IceCandidatePair> pairs)
         {
         IceCandidatePair pairToSet = null;
         for (final IceCandidatePair pair : pairs)
             {
             if (pairToSet == null)
                 {
                 pairToSet = pair;
                 continue;
                 }
             
             // Always use the lowest component ID.
             if (pair.getComponentId() < pairToSet.getComponentId())
                 {
                 pairToSet = pair;
                 }
             
             // If the component IDs match, use the one with the highest
             // priority.  See ICE section 5.7.4
             else if (pair.getComponentId() == pairToSet.getComponentId())
                 {
                 if (pair.getPriority() > pairToSet.getPriority())
                     {
                     pairToSet = pair;
                     }
                 }
             }
         
         if (pairToSet != null)
             {
             pairToSet.setState(IceCandidatePairState.WAITING);
             }
         else
             {
             m_log.warn("No pair to set!!!");
             }
         }
 
     public Queue<IceCandidatePair> getValidPairs()
         {
         return m_validPairs;
         }
 
     public void addLocalCandidate(final IceCandidate localCandidate)
         {
         this.m_localCandidates.add(localCandidate);
         }
 
     public IceCandidate getLocalCandidate(final InetSocketAddress localAddress,
         final boolean isUdp)
         {
         return getCandidate(this.m_localCandidates, localAddress, isUdp);
         }
 
     public IceCandidate getRemoteCandidate(
         final InetSocketAddress remoteAddress, final boolean isUdp)
         {
         return getCandidate(this.m_remoteCandidates, remoteAddress, isUdp);
         }
     
     public boolean hasRemoteCandidate(final InetSocketAddress remoteAddress,
         final boolean isUdp)
         {
         final IceCandidate remoteCandidate = 
             getCandidate(this.m_remoteCandidates, remoteAddress, isUdp);
         
         return remoteCandidate != null;
         }
     
     private IceCandidate getCandidate(final Collection<IceCandidate> candidates,
         final InetSocketAddress address, final boolean isUdp)
         {
         // A little inefficient here, but we're not talking about a lot of
         // candidates.
         synchronized (candidates)
             {
             for (final IceCandidate candidate : candidates)
                 {
                 if (candidate.isUdp())
                     {
                     if (!isUdp) continue;
                     }
                 else 
                     {
                     if (isUdp) continue;
                     }
                 if (address.equals(candidate.getSocketAddress()))
                     {
                     return candidate;
                     }
                 }
             m_log.debug(address+" with transport UDP: "+isUdp+
                 " not found in "+candidates);
             }
         
         return null;
         }
     
 
     /**
      * Checks if the new pair matches any pair the media stream already 
      * knows about.
      * 
      * @param localAddress The local address of the new pair.
      * @param remoteAddress The remote address of the new pair.
      * @return The matching pair if it exists, otherwise <code>null</code>.
      */
     public IceCandidatePair getPair(final InetSocketAddress localAddress, 
         final InetSocketAddress remoteAddress)
         {
         // The check list might not exist yet if the offerer receives incoming
         // requests before it has received an answer.
         if (this.m_checkList == null)
             {
             return null;
             }
 
         final Predicate<IceCandidatePair> pred = 
             new Predicate<IceCandidatePair>()
             {
             public boolean evaluate(final IceCandidatePair pair)
                 {
                 if (pair.getLocalCandidate().getSocketAddress().equals(localAddress) &&
                     pair.getRemoteCandidate().getSocketAddress().equals(remoteAddress))
                     {
                     return true;
                     }
                 return false;
                 }
             };
         
         return this.m_checkList.selectPair(pred);
         }
 
     public void updatePairStates(final IceCandidatePair validPair, 
         final IceCandidatePair generatingPair, final boolean useCandidate)
         {
         m_log.debug("Updating pair states...");
         // Set the state of the pair that *generated* the check to succeeded.
         generatingPair.setState(IceCandidatePairState.SUCCEEDED);
         
         // Now set FROZEN pairs with the same foundation as the pair that 
         // *generated* the check for this media stream to waiting.
         updateToWaiting(generatingPair);
         }
     
     public void updateCheckListAndTimerStates()
         {
         // Update check list and timer states.  See section 7.1.2.3.
         if (allFailedOrSucceeded())
             {
             // 1) Set the check list to failed if there is not a pair in the 
             // valid list for all componenents.
             
             // TODO: We only currently have one component!!
             if (this.m_validPairs.isEmpty())
                 {
                 // The check list is definitely created at this point, as
                 // we're updating pair state for a pair that had to have
                 // been on the check list.
                 this.m_checkList.setState(IceCheckListState.FAILED);
                 }
             
             // 2) Agent changes state of pairs in frozen check lists.
             this.m_iceAgent.onUnfreezeCheckLists(this);
             }
         
         // The final part of this section states the following:
         //
         // If none of the pairs in the check list are in the Waiting or Frozen
         // state, the check list is no longer considered active, and will not
         // count towards the value of N in the computation of timers for
         // ordinary checks as described in Section 5.8.
         
         // NOTE:  This requires no action on our part.  The definition of 
         // and "active" check list is "a check list with at least one pair 
         // that is Waiting" from 5.7.4.  When computing the value of N, that's
         // the definition that's used, and the active state is determined
         // dynamically at that time.        
         }
 
     /**
      * Checks to see if all pairs are in either the SUCCEEDED or the FIALED
      * state.
      * 
      * @return <code>true</code> if all pairs are in either the SUCCEEDED or
      * the FAILED state, otherwise <code>false</code>.
      */
     private boolean allFailedOrSucceeded()
         {
         final Predicate<IceCandidatePair> pred = 
             new Predicate<IceCandidatePair>()
             {
 
             public boolean evaluate(final IceCandidatePair pair)
                 {
                 if (pair.getState() != IceCandidatePairState.SUCCEEDED &&
                     pair.getState() != IceCandidatePairState.FAILED)
                     {
                     return false;
                     }
                 return true;
                 }
             };
         
         return this.m_checkList.matchesAny(pred);
         }
 
     public void addValidPair(final IceCandidatePair pair)
         {
         synchronized (this.m_validPairs)
             {
             this.m_validPairs.add(pair);
             }
         }
     
     /**
      * Implements part 1 of "7.1.2.2.3. Updating Pair States."  All pairs with
      * the same foundation as the successful pair that are in the FROZEN state
      * are switched to the WAITING state.
      *  
      * @param successfulPair The pair that succeeded.
      */
     private void updateToWaiting(final IceCandidatePair successfulPair)
         {
         // TODO: This should happen for ALL components.  We only currently
         // support one component.  See:
         // http://tools.ietf.org/html/draft-ietf-mmusic-ice-17#section-7.1.2.2.3
         final Closure<IceCandidatePair> closure =
             new Closure<IceCandidatePair>()
             {
             public void execute(final IceCandidatePair pair)
                 {
                 // We just update pairs with the same foundation that are in
                 // the frozen state to the waiting state.
                 if (pair.getFoundation().equals(successfulPair.getFoundation()) &&
                     pair.getState() == IceCandidatePairState.FROZEN)
                     {
                     pair.setState(IceCandidatePairState.WAITING);
                     }
                 }
             };
         this.m_checkList.executeOnPairs(closure);
         
         
         // TODO:  We only currently have a single component, so we call this
         // each time.  We need to implement ICE section 7.1.2.2.3, part 2.
         }
 
     public void addTriggeredCheck(final IceCandidatePair pair)
         {
         this.m_checkList.addTriggeredPair(pair);
         }
 
     public void recomputePairPriorities(final boolean controlling)
         {
         this.m_checkList.recomputePairPriorities(controlling);
         }
 
     public void addPair(final IceCandidatePair pair)
         {
         m_log.debug("Adding pair to media stream: {}", this);
         this.m_checkList.addPair(pair);
         }
     
     public IceCheckListState getCheckListState()
         {
         return this.m_checkList.getState();
         }
     
     public void setCheckListState(final IceCheckListState state)
         {
         this.m_checkList.setState(state);
         }
     
     public boolean hasHigherPriorityPendingPair(final IceCandidatePair pair)
         {
         return this.m_checkList.hasHigherPriorityPendingPair(pair);
         }
 
     public void onNominated(final IceCandidatePair pair)
         {
         // First, remove all Waiting and Frozen pairs on the check list and
         // triggered check queue.
         this.m_checkList.removeWaitingAndFrozenPairs(pair);
         synchronized (this.m_nominatedPairs)
             {
             this.m_nominatedPairs.add(pair);
             }
         }
 
     @Override
     public String toString()
         {
         return ClassUtils.getShortClassName(getClass())+ " controlling: "+
             this.m_iceAgent.isControlling();
         }
 
     public Queue<IceCandidatePair> getNominatedPairs()
         {
         return this.m_nominatedPairs;
         }
     }
