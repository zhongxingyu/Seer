 package org.lastbamboo.common.ice;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
 import org.apache.mina.common.ThreadModel;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.SocketConnector;
 import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
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
     private SocketConnector m_connector;
 
     /**
      * Creates a new ICE connectivity checker over UDP.
      * 
      * @param localCandidate The local address.
      * @param remoteCandidate The remote address.
      * @param messageVisitorFactory The factory for creating visitors for 
      * incoming messages.
      * @param iceAgent The top-level ICE agent.
      * @param demuxingCodecFactory The {@link ProtocolCodecFactory} for 
      * demultiplexing between STUN and another protocol.
      * @param clazz The top-level message class the protocol other than STUN.
      * @param ioHandler The {@link IoHandler} to use for the other protocol.
      */
     public IceTcpStunChecker(final IceCandidate localCandidate, 
         final IceCandidate remoteCandidate, 
         final StunMessageVisitorFactory messageVisitorFactory, 
         final IceAgent iceAgent, 
         final ProtocolCodecFactory demuxingCodecFactory,
         final Class clazz, final IoHandler ioHandler)
         {
         super(localCandidate, remoteCandidate, messageVisitorFactory, 
             iceAgent, demuxingCodecFactory, clazz, ioHandler);
         }
     
     @Override
     protected void createConnector(
         final InetSocketAddress localAddress, 
         final InetSocketAddress remoteAddress,
         final ThreadModel threadModel, final ProtocolCodecFilter stunFilter, 
         final IoHandler demuxer)
         {
         this.m_connector = new SocketConnector();
         
         final SocketConnectorConfig cfg = m_connector.getDefaultConfig();
         cfg.getSessionConfig().setReuseAddress(true);
         cfg.setThreadModel(threadModel);
         
         m_connector.getFilterChain().addLast("stunFilter", stunFilter);
         }
 
     @Override
     protected boolean connect()
         {
         m_log.debug("Establishing TCP connection...");
         if (this.m_ioSession != null)
             {
             // We might be performing a second check to verify a nominated
             // pair, for example.
             return true;
             }
         
         final InetAddress address = this.m_remoteAddress.getAddress();
         
         final int connectTimeout;
 
 
         if (address.isSiteLocalAddress())
             {
 
             try
                 {
                 if (!address.isReachable(400))
                     {
                     return false;
                     }
                 }
             catch (final IOException e)
                 {
                 return false;
                 }
             m_log.debug("Address is reachable. Connecting:{}", address);
 
             // We should be able to connect to local, private addresses 
             // really, really quickly.  So don't wait around too long.
             connectTimeout = 3000;
             }
         else
             {
             connectTimeout = 12000;
             }
         
         
         // TODO: We don't currently support TCP-SO, so we don't bind to the 
         // local port.
         final ConnectFuture cf = 
             m_connector.connect(this.m_remoteAddress, this.m_demuxer);
         cf.join(connectTimeout);
        try
             {
            final IoSession session = cf.getSession();
            if (session == null)
                {
                return false;
                }
            this.m_ioSession = session;
            return true;
            }
        catch (final RuntimeIOException e)
            {
            // This happens when we can't connect.
            m_log.debug("Could not connect to host: {}", this.m_remoteAddress);
             return false;
             }
        

         }
     }
