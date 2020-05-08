 package org.lastbamboo.common.ice;
 
 import java.net.InetSocketAddress;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.id.uuid.UUID;
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.ExecutorThreadModel;
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.DatagramConnector;
 import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.stun.client.StunClientMessageVisitor;
 import org.lastbamboo.common.stun.stack.StunDemuxingIoHandler;
 import org.lastbamboo.common.stun.stack.StunIoHandler;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.CanceledStunMessage;
 import org.lastbamboo.common.stun.stack.message.IcmpErrorStunMessage;
 import org.lastbamboo.common.stun.stack.message.NullStunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionListener;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class that performs STUN connectivity checks for ICE over UDP.  Each 
  * ICE candidate pair has its own connectivity checker. 
  */
 public class IceUdpStunChecker implements IceStunChecker, 
     StunTransactionListener
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     
     private final IoSession m_ioSession;
 
     /**
      * TODO: Review if this works!!
      */
     private volatile boolean m_transactionCancelled = false;
 
     private final IceAgent m_iceAgent;
     
     private volatile int m_writeCallsForPair = 0;
     
     private final Map<UUID, StunMessage> m_idsToResponses =
         new ConcurrentHashMap<UUID, StunMessage>();
 
     private final StunTransactionTracker m_transactionTracker;
 
    private volatile boolean m_icmpError;
     
     private final Object m_requestLock = new Object();
 
     private final IoHandler m_protocolIoHandler;
 
     private final Class m_clazz;
 
     private final ProtocolCodecFactory m_codecFactory;
 
     private final StunMessageVisitorFactory m_stunServerCheckerFactory;
 
     /**
      * Creates a new ICE connectivity checker over UDP.
      * 
      * @param localCandidate The local address.
      * @param remoteCandidate The remote address.
      * @param iceAgent The top-level ICE agent.
      */
     public IceUdpStunChecker(final IceCandidate localCandidate, 
         final IceCandidate remoteCandidate, 
         final StunMessageVisitorFactory messageVisitorFactory,
         final IceAgent iceAgent, final ProtocolCodecFactory codecFactory,
         final Class clazz, final IoHandler ioHandler)
         {
         this.m_stunServerCheckerFactory = messageVisitorFactory;
         this.m_iceAgent = iceAgent;
         this.m_codecFactory = codecFactory;
         this.m_clazz = clazz;
         this.m_protocolIoHandler = ioHandler;
         this.m_ioSession = createClientSession(
             localCandidate.getBaseCandidate().getSocketAddress(), 
             remoteCandidate.getSocketAddress());
         
         this.m_transactionTracker = new StunTransactionTrackerImpl();
         }
     
     private IoSession createClientSession(final InetSocketAddress localAddress, 
         final InetSocketAddress remoteAddress) 
         {
         final DatagramConnector connector = new DatagramConnector();
         
         final DatagramConnectorConfig cfg = connector.getDefaultConfig();
         cfg.getSessionConfig().setReuseAddress(true);
 
         final String controlling;
         if (this.m_iceAgent.isControlling())
             {
             controlling = "Controlling";
             }
         else
             {
             controlling = "Not-Controlling";
             }
         
         cfg.setThreadModel(
             ExecutorThreadModel.getInstance(
                 "IceUdpStunChecker-"+controlling));
         final ProtocolCodecFilter stunFilter = 
             new ProtocolCodecFilter(this.m_codecFactory);
         
         connector.getFilterChain().addLast("stunFilter", stunFilter);
         
         final StunMessageVisitorFactory<StunMessage> visitorFactory =
             new IceConnectivityStunMessageVisitorFactory();
         final IoHandler ioHandler = 
             new StunIoHandler<StunMessage>(visitorFactory);
         
         final IoHandler demuxer = new StunDemuxingIoHandler(this.m_clazz, 
             this.m_protocolIoHandler, ioHandler);
         m_log.debug("Connecting from "+localAddress+" to "+remoteAddress);
         final ConnectFuture cf = 
             connector.connect(remoteAddress, localAddress, demuxer);
         cf.join();
         final IoSession session = cf.getSession();
         
         if (session == null)
             {
             m_log.error("Could not create session from "+
                 localAddress +" to "+remoteAddress);
             throw new NullPointerException("Could not create session!!");
             }
         return session;
         }
     
     private class IceConnectivityStunMessageVisitorFactory 
         implements StunMessageVisitorFactory<StunMessage>
         {
 
         public StunMessageVisitor<StunMessage> createVisitor(
             final IoSession session)
             {
             final StunMessageVisitor serverChecker = 
                 m_stunServerCheckerFactory.createVisitor(session);
             return new IceConnectivityStunMessageVisitor(m_transactionTracker,
                 serverChecker);
             }
         }
     
     private class IceConnectivityStunMessageVisitor 
         extends StunClientMessageVisitor<StunMessage>
         {
         
         private final StunMessageVisitor m_serverVisitor;
 
 
         private IceConnectivityStunMessageVisitor(
             final StunTransactionTracker transactionTracker, 
             final StunMessageVisitor serverVisitor)
             {
             super(transactionTracker);
             m_serverVisitor = serverVisitor;
             }
 
         public StunMessage visitBindingRequest(final BindingRequest binding)
             {
             m_log.debug("Handling Binding Request on: {}", m_ioSession);
             this.m_serverVisitor.visitBindingRequest(binding);
             return null;
             }
         
         
         public StunMessage visitIcmpErrorMesssage(
             final IcmpErrorStunMessage message)
             {
             if (m_log.isDebugEnabled())
                 {
                 m_log.debug("Received ICMP error: "+message);
                 }
             m_icmpError = true;
             synchronized (m_requestLock)
                 {
                 m_requestLock.notify();
                 }
             return null;
             }
         }
     
     public StunMessage write(final BindingRequest bindingRequest, 
         final long rto)
         {
         m_log.debug("Writing Binding Request...");
         this.m_writeCallsForPair++;
         try
             {
             return writeInternal(bindingRequest, rto);
             }
         catch (final Throwable t)
             {
             m_log.error("Could not write Binding Request", t);
             return new NullStunMessage();
             }
         }
     
     private StunMessage writeInternal(final BindingRequest bindingRequest, 
         final long rto)
         {
         if (bindingRequest == null)
             {
             throw new NullPointerException("Null Binding Request");
             }
         
         // This method will retransmit the same request multiple times because
         // it's being sent unreliably.  All of these requests will be 
         // identical, using the same transaction ID.
         final UUID id = bindingRequest.getTransactionId();
         final InetSocketAddress localAddress = 
             (InetSocketAddress) this.m_ioSession.getLocalAddress();
         final InetSocketAddress remoteAddress =
             (InetSocketAddress) this.m_ioSession.getRemoteAddress();
         
         this.m_transactionTracker.addTransaction(bindingRequest, this, 
             localAddress, remoteAddress);
         
         synchronized (m_requestLock)
             {   
             this.m_transactionCancelled = false;
             this.m_icmpError = false;
             int requests = 0;
             
             long waitTime = 0L;
 
             while (!m_idsToResponses.containsKey(id) && requests < 7 &&
                 !this.m_transactionCancelled && !this.m_icmpError)
                 {
                 waitIfNoResponse(bindingRequest, waitTime);
                 if (this.m_icmpError)
                     {
                     m_log.debug("ICMP error -- breaking");
                     break;
                     }
                 
                 // See draft-ietf-behave-rfc3489bis-06.txt section 7.1.  We
                 // continually send the same request until we receive a 
                 // response, never sending more that 7 requests and using
                 // an expanding interval between requests based on the 
                 // estimated round-trip-time to the server.  This is because
                 // some requests can be lost with UDP.
                 this.m_ioSession.write(bindingRequest);
                 
                 // Wait a little longer with each send.
                 waitTime = (2 * waitTime) + rto;
                 
                 requests++;
                 }
             
             // Now we wait for 1.6 seconds after the last request was sent.
             // If we still don't receive a response, then the transaction 
             // has failed.  
             if (!this.m_transactionCancelled && !this.m_icmpError)
                 {
                 waitIfNoResponse(bindingRequest, 1600);
                 }
             
             if (this.m_icmpError)
                 {
                 m_log.debug("Got ICMP error");
                 return new IcmpErrorStunMessage();
                 }
 
             // Even if the transaction was cancelled, we still may have 
             // received a successful response.  If we did, we process it as
             // usual.  This is specified in 7.2.1.4.
             if (m_idsToResponses.containsKey(id))
                 {
                 final StunMessage response = this.m_idsToResponses.get(id);
                 m_log.debug("Returning some sort of response...");
                 return response;
                 }
             
             if (this.m_transactionCancelled)
                 {
                 m_log.debug("The transaction was cancelled!");
                 return new CanceledStunMessage();
                 }
             
             else
                 {
                 m_log.warn("Did not get response on: {}", this.m_ioSession);
                 return new NullStunMessage();
                 }
             }
         }
 
     public void cancelTransaction()
         {
         m_log.debug("Cancelling transaction!!");
         this.m_transactionCancelled = true;
         }
     
     private void waitIfNoResponse(final BindingRequest request, 
         final long waitTime)
         {
         if (waitTime == 0L) return;
         if (!m_idsToResponses.containsKey(request.getTransactionId()))
             {
             try
                 {
                 m_requestLock.wait(waitTime);
                 }
             catch (final InterruptedException e)
                 {
                 m_log.error("Unexpected interrupt", e);
                 }
             }
         }
 
     public Object onTransactionFailed(final StunMessage request,
         final StunMessage response)
         {
         m_log.warn("Transaction failed");
         return notifyWaiters(request, response);
         }
     
     public Object onTransactionSucceeded(final StunMessage request, 
         final StunMessage response)
         {
         return notifyWaiters(request, response);
         }
 
     private Object notifyWaiters(final StunMessage request, 
         final StunMessage response)
         {
         synchronized (m_requestLock)
             {
             this.m_idsToResponses.put(request.getTransactionId(), response);
             m_requestLock.notify();
             }
         return null;
         }
 
     public IoSession getIoSession()
         {
         return this.m_ioSession;
         }
     }
