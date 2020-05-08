 package org.lastbamboo.common.ice.transport;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.ExecutorThreadModel;
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoServiceListener;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.common.RuntimeIOException;
 import org.apache.mina.common.ThreadModel;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.SocketConnector;
 import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
 import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
 import org.lastbamboo.common.stun.stack.StunIoHandler;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.tcp.frame.TcpFrame;
 import org.lastbamboo.common.tcp.frame.TcpFrameCodecFactory;
 import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;
 import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
 import org.lastbamboo.common.util.mina.DemuxingIoHandler;
 import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for establishing TCP connections for ICE. 
  */
 public class IceTcpConnector implements IceConnector
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     private final boolean m_controlling;
     private final DemuxingIoHandler<StunMessage, TcpFrame> m_demuxingIoHandler;
     
     private final Collection<IoServiceListener> m_serviceListeners =
         new LinkedList<IoServiceListener>();
 
     /**
      * Creates a new connector for connecting to the remote address.
      * 
      * @param messageVisitorFactory The class for visiting received STUN
      * messages.
      * @param controlling Whether or not this agent is controlling.
      */
     public IceTcpConnector(
         final StunMessageVisitorFactory messageVisitorFactory, 
         final boolean controlling)
         {
         m_controlling = controlling;
         // TODO: We don't currently support TCP-SO, so we don't bind to the 
         // local port.
         final IoHandler stunIoHandler = 
             new StunIoHandler<StunMessage>(messageVisitorFactory);
 
         final TcpFrameIoHandler streamIoHandler = 
             new TcpFrameIoHandler();
         this.m_demuxingIoHandler = 
             new DemuxingIoHandler<StunMessage, TcpFrame>(
                 StunMessage.class, stunIoHandler, 
                 TcpFrame.class, streamIoHandler);
         }
 
     public IoSession connect(final InetSocketAddress localAddress,
         final InetSocketAddress remoteAddress)
         {
         final SocketConnector connector = new SocketConnector();
         synchronized (this.m_serviceListeners)
             {
             for (final IoServiceListener listener : this.m_serviceListeners)
                 {
                 connector.addListener(listener);
                 }
             }
         
         final SocketConnectorConfig cfg = connector.getDefaultConfig();
         cfg.getSessionConfig().setReuseAddress(true);
         
         final ThreadModel threadModel = ExecutorThreadModel.getInstance(
             getClass().getSimpleName() +
             (this.m_controlling ? "-Controlling" : "-Not-Controlling"));
         final DemuxableProtocolCodecFactory stunCodecFactory =
             new StunDemuxableProtocolCodecFactory();
         final DemuxableProtocolCodecFactory tcpFramingCodecFactory =
             new TcpFrameCodecFactory();
         final ProtocolCodecFactory demuxingCodecFactory = 
             new DemuxingProtocolCodecFactory(stunCodecFactory, 
                 tcpFramingCodecFactory);
         
         final ProtocolCodecFilter demuxingFilter = 
             new ProtocolCodecFilter(demuxingCodecFactory);
         
         cfg.setThreadModel(threadModel);
         
         connector.getFilterChain().addLast("demuxingFilter", demuxingFilter);
 
         m_log.debug("Establishing TCP connection to: {}", remoteAddress);
         final InetAddress address = remoteAddress.getAddress();
         final int connectTimeout;
         // If the address is on the local network, we should be able to 
         // connect more quickly.  If we can't, that likely indicates the 
         // address is just from a different local network.
         if (address.isSiteLocalAddress())
             {
             try
                 {
                 if (!address.isReachable(600))
                     {
                     m_log.debug("Address is not reachable: {}", remoteAddress);
                     return null;
                     }
                 }
             catch (final IOException e)
                 {
                 m_log.debug("Exception checking reachability", e);
                 return null;
                 }
             m_log.debug("Address is reachable. Connecting to: {}", 
                  remoteAddress);
 
             // We should be able to connect to local, private addresses 
             // really quickly.  So don't wait around too long.
             connectTimeout = 3000;
             }
         else
             {
             connectTimeout = 6000;
             }
         
         m_log.debug("Connecting with timeout: {}", connectTimeout);
         final ConnectFuture cf = 
             connector.connect(remoteAddress, localAddress, 
                  this.m_demuxingIoHandler);
         cf.join(connectTimeout);
         m_log.debug("Successfully joined...");
         try
             {
             final IoSession session = cf.getSession();
             if (session == null)
                 {
                 m_log.debug("Session is null!!");
                 return null;
                 }
             m_log.debug("TCP STUN checker connected on: {}",session);
             return session;
             }
         catch (final RuntimeIOException e)
             {
             // This happens when we can't connect.
             m_log.debug("Could not connect to host: {}", remoteAddress);
             m_log.debug("Reason for no connection: ", e);
            throw e;
             }
         }
     
     public void addIoServiceListener(final IoServiceListener serviceListener)
         {
         this.m_serviceListeners.add(serviceListener);
         }
     }
