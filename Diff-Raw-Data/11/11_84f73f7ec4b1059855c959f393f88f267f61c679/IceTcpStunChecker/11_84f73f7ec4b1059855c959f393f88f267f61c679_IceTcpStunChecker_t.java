 package org.lastbamboo.common.ice.transport;
 
 import java.net.InetSocketAddress;
 
 import org.apache.commons.id.uuid.UUID;
 import org.apache.mina.common.IoSession;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.CanceledStunMessage;
 import org.lastbamboo.common.stun.stack.message.NullStunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class that performs STUN connectivity checks for ICE over TCP.  Each 
  * ICE candidate pair has its own connectivity checker. 
  */
 public class IceTcpStunChecker extends AbstractIceStunChecker
     {
 
     private final Logger m_log = 
         LoggerFactory.getLogger(IceTcpStunChecker.class);
 
     /**
      * Creates a new ICE connectivity checker over TCP.  If the 
      * {@link IoSession} is <code>null</code>, this will connect to create a
      * new session.
      * @param session The {@link IoSession} to write messages over.
      * @param transactionTracker The class that keeps track of STUN 
      * transactions.
      */
     public IceTcpStunChecker(final IoSession session,
         final StunTransactionTracker<StunMessage> transactionTracker)
         {
         super(session, transactionTracker);
         }
 
     @Override
     protected StunMessage writeInternal(
         final BindingRequest bindingRequest, final long rto)
         {
         if (bindingRequest == null)
             {
             throw new NullPointerException("Null Binding Request");
             }
 
         if (this.m_closed || this.m_ioSession.isClosing())
             {
             m_log.debug("Already closed");
             return new CanceledStunMessage();
             }

         final UUID id = bindingRequest.getTransactionId();
         final InetSocketAddress localAddress = 
             (InetSocketAddress) this.m_ioSession.getLocalAddress();
         final InetSocketAddress remoteAddress =
             (InetSocketAddress) this.m_ioSession.getRemoteAddress();
         
         this.m_transactionTracker.addTransaction(bindingRequest, this, 
             localAddress, remoteAddress);
         
         m_log.debug("Waiting for lock...");
         synchronized (m_requestLock)
             {   
             this.m_transactionCanceled = false;
             m_log.debug("Sending Binding Request...");
             this.m_ioSession.write(bindingRequest);
             
             if (!this.m_transactionCanceled)
                 {
                 waitIfNoResponse(bindingRequest, 7900);
                 }
 
             // Even if the transaction was canceled, we still may have 
             // received a successful response.  If we did, we process it as
             // usual.  This is specified in 7.2.1.4.
             if (m_idsToResponses.containsKey(id))
                 {
                 final StunMessage response = this.m_idsToResponses.remove(id);
                 m_log.debug("Read STUN response for check: {}", response);
                 return response;
                 }
             
             if (this.m_transactionCanceled)
                 {
                 m_log.debug("The transaction was cancelled!");
                 return new CanceledStunMessage();
                 }
             
             else
                 {
                 // This will happen quite often, such as when we haven't
                 // yet successfully punched a hole in the firewall.
                 m_log.debug("Did not get response on: {}", this.m_ioSession);
                 return new NullStunMessage();
                 }
             }
         }
     }
