 package org.lastbamboo.common.ice;
 
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 import java.util.Map;
 
 import org.apache.commons.id.uuid.UUID;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidatePair;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
 import org.lastbamboo.common.stun.client.StunClientMessageVisitor;
 import org.littleshoot.stun.stack.message.BindingErrorResponse;
 import org.littleshoot.stun.stack.message.BindingRequest;
 import org.littleshoot.stun.stack.message.BindingSuccessResponse;
 import org.littleshoot.stun.stack.message.StunMessage;
 import org.littleshoot.stun.stack.message.attributes.StunAttribute;
 import org.littleshoot.stun.stack.message.attributes.StunAttributeType;
 import org.littleshoot.stun.stack.message.attributes.ice.IceControlledAttribute;
 import org.littleshoot.stun.stack.message.attributes.ice.IceControllingAttribute;
 import org.littleshoot.stun.stack.transaction.StunTransactionTracker;
 import org.littleshoot.mina.common.IoSession;
 import org.littleshoot.mina.common.TransportType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Processes STUN connectivity checks for ICE. See the 
  * "7.2.  STUN Server Procedures" section at:<p>
  * 
  * http://tools.ietf.org/html/rfc5245#section-7.2
  * 
  * @param <T> The type STUN message visitor methods return.
  */
 public final class IceStunConnectivityCheckerImpl<T>
     extends StunClientMessageVisitor<T>
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     
     private final IceAgent m_agent;
 
     private final IceMediaStream m_iceMediaStream;
 
     private final IoSession m_ioSession;
     
     private final ExistingSessionIceCandidatePairFactory m_candidatePairFactory;
 
     private final IceBindingRequestTracker m_bindingRequestTracker;
 
     /**
      * Creates a new message visitor for the specified session.
      * 
      * @param agent The top-level ICE agent.
      * @param session The IO session to perform the checks over.
      * @param transactionTracker The class that keeps track of STUN 
      * transactions for the checks.
      * @param checkerFactory The factory for creating new classes for 
      * performing connectivity checks.
      * @param bindingRequestTracker Tracks Binding Requests we've already
      * processed.
      */
     public IceStunConnectivityCheckerImpl(
         final IceAgent agent, final IoSession session, 
         final StunTransactionTracker<T> transactionTracker,
         final IceStunCheckerFactory checkerFactory, 
         final IceBindingRequestTracker bindingRequestTracker)
         {
         super (transactionTracker);
         m_agent = agent;
         m_iceMediaStream = (IceMediaStream) session.getAttribute(
             IceMediaStream.class.getSimpleName());
         m_ioSession = session;
         m_bindingRequestTracker = bindingRequestTracker;
         m_candidatePairFactory = 
             new ExistingSessionIceCandidatePairFactoryImpl(checkerFactory);
         }
 
     @Override
     public T visitBindingRequest(final BindingRequest request)
         {
         m_log.debug("Visiting Binding Request message: {}", request);
         // This is not standard.  Most STUN implementations will implement
         // transaction state machines and will therefore filter out duplicate
         // Binding Requests.  This is not required, however, so we add this
         // check to avoid more processing if we are sent duplicate requests
         // for transactions.
         if (this.m_bindingRequestTracker.recentlyProcessed(request))
             {
             m_log.debug("We've recently processed the request -- ignoring " +
                 "duplicate");
             final StunMessage response = 
                 this.m_bindingRequestTracker.getResponse(request);
             if (response != null) 
                 {
                 // We need to resend the response again for the same reason
                 // we send requests multiple times: UDP packets can get 
                 // dropped!!
                 m_log.info("Writing same response again");
                 this.m_ioSession.write(response);
                 }
             else 
                 {
                 m_log.warn("Received dup request before mapping response?");
                 }
             return null;
             }
         this.m_bindingRequestTracker.add(request);
         
         // We occasionally see requests from ourselves in tests where the
         // NAT has funky hairpinning support.  We check if the request is one
         // we just sent and ignore it if so.
         if (fromOurselves(this.m_agent, request))
             {
             m_log.error("Received a request from us on: {}", this.m_ioSession);
             return null;
             }
         m_log.debug("Not from ourselves...");
         // We need to check ICE controlling and controlled roles for conflicts.
         // This implements:
         // 7.2.1.1.  Detecting and Repairing Role Conflicts
         final IceRoleChecker checker = new IceRoleCheckerImpl();
         final BindingErrorResponse errorResponse = 
             checker.checkAndRepairRoles(request, this.m_agent, 
                 this.m_ioSession);
         m_log.debug("Checked role conflict...");
         
         if (errorResponse != null)
             {
             // This can happen in the rare case that there's a role conflict.
             this.m_log.debug("Sending error response...");
             this.m_ioSession.write(errorResponse);
             this.m_bindingRequestTracker.addResponse(request, errorResponse);
             }
         else
             {
             // We now implement the remaining sections 7.2.1 following 7.2.1.1 
             // since we're returning a success response.
             m_log.debug("Processing no role conflict...");
             processNoRoleConflict(request);
             }
         return null;
         }
 
     /**
      * Process the typical case where the {@link BindingRequest} did not 
      * create a role conflict.
      * 
      * @param binding The {@link BindingRequest}.
      */
     private void processNoRoleConflict(final BindingRequest binding)
         {
         if (binding.getAttributes().containsKey(
             StunAttributeType.ICE_USE_CANDIDATE))
             {
             m_log.debug("GOT BINDING REQUEST WITH USE CANDIDATE");
             }
         
         final InetSocketAddress localAddress = 
             (InetSocketAddress) this.m_ioSession.getLocalAddress();
         
         /*
         // We need to implement ICE section 7.2.1.2 here for TURN 
         // candidates.  We need to translate the remote address as seen on 
         // the IoSession (which will be the address of the TURN server) 
         // to the address of the remote host - the peer itself.  This is
         // available in the REMOTE-ADDRESS attribute of a Data Indication 
         // message.
         final InetSocketAddress remoteAddress;
         final TurnStunMessageMapper mapper =
             (TurnStunMessageMapper) this.m_ioSession.getAttribute(
                 "REMOTE_ADDRESS_MAP");
         if (mapper != null)
             {
             remoteAddress = mapper.get(binding);
             m_log.debug("Using stored remote address (TURN CONNECTION): {}", 
                 remoteAddress);
             }
         else
             {
             // If there's no remote address map, then it's not a TURN check. 
             // This is the "normal" case, so just keep going.
             remoteAddress = 
                 (InetSocketAddress) this.m_ioSession.getRemoteAddress();
             m_log.debug("Using normal remote address: {}", remoteAddress);
             }
             */
         
         final InetSocketAddress remoteAddress = 
             (InetSocketAddress) this.m_ioSession.getRemoteAddress();
         m_log.debug("Using normal remote address: {}", remoteAddress);
         
         // TODO: This should include other attributes!!
         final UUID transactionId = binding.getTransactionId();
         final StunMessage response = 
             new BindingSuccessResponse(transactionId.getRawBytes(), 
                 remoteAddress);
         
         // We write the response as soon as possible.
         m_log.debug("Writing success response...");
         this.m_ioSession.write(response);
         // We need to remember our response to use it for duplicate requests.
         this.m_bindingRequestTracker.addResponse(binding, response);
         
         // Check to see if the remote address matches the address of
         // any remote candidates we know about.  If it does not, it's a
         // new peer reflexive address.  See ICE section 7.2.1.3
         final TransportType type = this.m_ioSession.getTransportType();
         final boolean isUdp = type.isConnectionless();
         final IceCandidate remoteCandidate;
         if (!this.m_iceMediaStream.hasRemoteCandidate(remoteAddress, isUdp))
             {
             m_log.debug("New remote candidate...");
             remoteCandidate = this.m_iceMediaStream.addRemotePeerReflexive(
                 binding, localAddress, remoteAddress, isUdp);
             m_log.debug("Added peer reflexive remote candidate: {}", 
                 remoteCandidate);
             }
         else
             {
             remoteCandidate = 
                 this.m_iceMediaStream.getRemoteCandidate(remoteAddress, isUdp);
             }
         
         final IceCandidate localCandidate = 
             this.m_iceMediaStream.getLocalCandidate(localAddress, isUdp);
         
         m_log.debug("Using existing local candidate: {}", localCandidate);
         
         if (localCandidate == null)
             {
             m_log.warn("Could not create local candidate.");
             return;
             }
 
         if (remoteCandidate == null)
             {
             // There should always be a remote candidate at this point 
             // because the peer reflexive check above should have added it
             // if it wasn't already there.
             m_log.warn("Could not find remote candidate.");
             return;
             }
         
         // 7.2.1.4. Triggered Checks
         final IceCandidatePair existingPair = 
             this.m_iceMediaStream.getPair(localAddress, remoteAddress, 
                 localCandidate.isUdp());
         final IceCandidatePair computedPair;
         if (existingPair != null)
             {
             m_log.debug("Found existing pair");
             computedPair = existingPair;
             /*
             if (computedPair.getIoSession() == null)
                 {
                 m_log.error("Should have a session for pair: {}", computedPair);
                 throw new NullPointerException("Null session!!!");
                 }
                 */
             
             // This is the case where the new pair is already on the 
             // check list.  See ICE section 7.2.1.4. Triggered Checks
             final IceCandidatePairState state = computedPair.getState();
             switch (state)
                 {
                 case WAITING:
                     // Fall through.
                 case FROZEN:
                     m_log.debug("Adding triggered check for previously " +
                         "frozen or waiting pair:\n{}",existingPair);
                     this.m_iceMediaStream.addTriggeredPair(existingPair);
                     break;
                 case IN_PROGRESS:
                     // We need to cancel the in-progress transaction.  
                     // This just means we won't re-submit requests and will
                     // not treat the lack of response as a failure.
                     /*
                     m_log.debug("Canceling in progress transaction...");
                     existingPair.cancelStunTransaction();
                 
                     // Add the pair to the triggered check queue.
                     existingPair.setState(IceCandidatePairState.WAITING);
                     this.m_iceMediaStream.addTriggeredPair(existingPair);
                     */
                     break;
                 case FAILED:
                     existingPair.setState(IceCandidatePairState.WAITING);
                     this.m_iceMediaStream.addTriggeredPair(existingPair);
                     break;
                 case SUCCEEDED:
                     // Nothing more to do.
                     m_log.debug("Pair has already been checked and SUCCEEDED");
                     break;
                 }
             }
         else
             {
             m_log.debug("Creating new candidate pair.");
             computedPair = this.m_candidatePairFactory.newUdpPair(
                 localCandidate, remoteCandidate, this.m_ioSession); 
                 
             // Continue with the rest of ICE section 7.2.1.4, 
             // "Triggered Checks"
             
             // *  The pair is inserted into the check list based on its priority.
             // *  Its state is set to Waiting.
             // *  The pair is enqueued into the triggered check queue.
             //
             
             // TODO: The remote candidate needs a username fragment and
             // password.  We don't implement this yet.  This is the 
             // description of what we need to do:
             
             // "The username fragment for the remote candidate is equal to 
             // the part after the colon of the USERNAME in the Binding 
             // Request that was just received.  Using that username 
             // fragment, the agent can check the SDP messages received 
             // from its peer (there may be more than one in cases of 
             // forking), and find this username fragment.  The
             // corresponding password is then selected."
             
             
             // Add the pair the normal check list, set its state to waiting,
             // and add a triggered check.
             this.m_iceMediaStream.addPair(computedPair);
             computedPair.setState(IceCandidatePairState.WAITING);
             this.m_iceMediaStream.addTriggeredPair(computedPair);
             
             // TODO: We should be handling the username fragment and
             // password for the triggered check.
             }
         
         // 7.2.1.5. Updating the Nominated Flag
         
         // If the ICE USE CANDIDATE attribute is set, and we're in the 
         // controlled role, we need to deal with nominating the pair.
         if (binding.getAttributes().containsKey(
             StunAttributeType.ICE_USE_CANDIDATE) &&
             !this.m_agent.isControlling())
             {
             // Just record the fact that the controlling agent told us
             // to use this pair.
             computedPair.useCandidate();
             final IceCandidatePairState state = computedPair.getState();
             
             switch (state)
                 {
                 // This is an optimization that allows us to nominate now
                 // when we've already previously done a successful check
                 // on this pair. In that case, we didn't add a triggered
                 // check above, and we can just nominate now.
                 case SUCCEEDED:
                     m_log.debug("Nominating pair on controlled agent:\n{}",  
                         computedPair);
                     computedPair.nominate();
 
                     // Note this typically means the controlled agent will be
                     // the first to use a nominated pair for media, as the
                     // controlling agent won't use the nominated pair until
                     // it receives the successful binding response.
                     m_agent.onNominatedPair(computedPair, 
                         this.m_iceMediaStream);
                     break;
                 case IN_PROGRESS:
                     // Section 7.2.1.5:
                     /*
                       If the state of this pair is In-Progress, if its check produces a
                       successful result, the resulting valid pair has its nominated flag
                       set when the response arrives. This may end ICE processing for
                       this media stream when it arrives; see Section 8.
                      */
                     computedPair.nominateOnSuccess();
                     break;
                 case WAITING:
                     // No action.
                     break;
                 case FROZEN:
                     // No action.
                     break;
                 case FAILED:
                     // No action.
                     break;
                 }
             }
         }
 
     private boolean fromOurselves(final IceAgent agent, 
         final BindingRequest request)
         {
         final Map<StunAttributeType, StunAttribute> remoteAttributes = 
             request.getAttributes();
         final byte[] remoteTieBreaker;
         if (remoteAttributes.containsKey(StunAttributeType.ICE_CONTROLLED))
             {
             final IceControlledAttribute attribute = 
                 (IceControlledAttribute) remoteAttributes.get(
                     StunAttributeType.ICE_CONTROLLED);
             remoteTieBreaker = attribute.getTieBreaker();
             }
         else 
             {
             final IceControllingAttribute attribute = 
                 (IceControllingAttribute) remoteAttributes.get(
                     StunAttributeType.ICE_CONTROLLING);
             if (attribute == null)
                 {
                 // This can often happen during tests.  If it happens in
                 // production, though, this will get sent to our servers.
                 m_log.error("No controlling attribute");
                 return false;
                 }
             remoteTieBreaker = attribute.getTieBreaker();
             }
         
         final byte[] localTieBreaker = agent.getTieBreaker().toByteArray();
         
         return Arrays.equals(localTieBreaker, remoteTieBreaker);
         }
     
     }
