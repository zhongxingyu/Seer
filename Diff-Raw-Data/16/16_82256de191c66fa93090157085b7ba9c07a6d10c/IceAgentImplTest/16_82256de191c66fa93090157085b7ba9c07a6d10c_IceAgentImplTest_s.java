 package org.lastbamboo.common.ice;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.mina.common.ByteBuffer;
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.ExecutorThreadModel;
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoHandlerAdapter;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.DatagramConnector;
 import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
 import org.junit.Assert;
 import org.junit.Test;
 import org.lastbamboo.common.ice.candidate.IceCandidatePair;
 import org.lastbamboo.common.ice.stubs.StunClientStub;
 import org.lastbamboo.common.offer.answer.MediaOfferAnswer;
 import org.lastbamboo.common.offer.answer.OfferAnswerListener;
 import org.lastbamboo.common.offer.answer.OfferAnswerMediaListener;
 import org.lastbamboo.common.stun.client.StunClient;
 import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
 import org.lastbamboo.common.stun.stack.StunIoHandler;
 import org.lastbamboo.common.stun.stack.StunProtocolCodecFactory;
 import org.lastbamboo.common.stun.stack.message.IcmpErrorStunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
 import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Test connections between ICE agents.
  */
 public class IceAgentImplTest
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     
     private final AtomicBoolean m_gotIcmpError = new AtomicBoolean(false);
     
     /**
      * Tests creating a local UDP connection using ICE.
      * 
      * @throws Exception If any unexpected error occurs.
      */
     @Test
     public void testLocalUdpConnection() throws Exception
         {
         final IceMediaStreamDesc desc = 
             new IceMediaStreamDesc(false, true, "message", "http", 1);
 
        final StunClient turnClient = new StunClientStub();
         
         final IceMediaStreamFactory mediaStreamFactory1 = 
             new IceMediaStreamFactory()
             {
             public IceMediaStream newStream(final IceAgent iceAgent, 
                 final StunClient tcpTurnClient) 
                 {
                 final int hostPort = 65044;
                 final InetSocketAddress serverReflexive =
                     new InetSocketAddress("53.43.90.1", 2452);
                 final StunClient udpStunClient = 
                     StunClientStub.newClient(serverReflexive, hostPort);
                 final DemuxableProtocolCodecFactory stunCodecFactory =
                     new StunDemuxableProtocolCodecFactory();
                 final DemuxableProtocolCodecFactory otherCodecFactory =
                     new StunDemuxableProtocolCodecFactory();
                 final ProtocolCodecFactory codecFactory =
                     new DemuxingProtocolCodecFactory(stunCodecFactory, 
                         otherCodecFactory);
                 final IoHandler clientIoHandler = new IoHandlerAdapter();
                 final IoHandler serverIoHandler = new IoHandlerAdapter();
                 return new IceMediaStreamImpl(iceAgent, desc, tcpTurnClient, 
                     udpStunClient, codecFactory, Void.class, clientIoHandler, 
                     serverIoHandler);
                 }
             };
         
         final IceMediaStreamFactory mediaStreamFactory2 = 
             new IceMediaStreamFactory()
             {
             public IceMediaStream newStream(final IceAgent iceAgent, 
                 final StunClient tcpTurnClient)
                 {
                 final int hostPort = 48290;
                 final InetSocketAddress serverReflexive =
                     new InetSocketAddress("21.9.90.1", 9852);
                 final StunClient udpStunClient = 
                     StunClientStub.newClient(serverReflexive, hostPort);
                 final DemuxableProtocolCodecFactory stunCodecFactory =
                     new StunDemuxableProtocolCodecFactory();
                 final DemuxableProtocolCodecFactory otherCodecFactory =
                     new StunDemuxableProtocolCodecFactory();
                 final ProtocolCodecFactory codecFactory =
                     new DemuxingProtocolCodecFactory(stunCodecFactory, 
                         otherCodecFactory);
                 final IoHandler clientIoHandler = new IoHandlerAdapter();
                 final IoHandler serverIoHandler = new IoHandlerAdapter();
                 return new IceMediaStreamImpl(iceAgent, desc, tcpTurnClient, 
                     udpStunClient, codecFactory, Void.class, clientIoHandler, 
                     serverIoHandler);
                 }
             };
         final IceMediaFactory iceMediaFactory = new IceMediaFactory()
             {
 
             public void newMedia(IceCandidatePair pair, boolean client, OfferAnswerMediaListener mediaListener)
                 {
                 // TODO Auto-generated method stub
                 
                 }
             
             };
        final IceAgent offerer = new IceAgentImpl(turnClient, 
             mediaStreamFactory1, true, iceMediaFactory);
         final byte[] offer = offerer.generateOffer();
 
         m_log.debug("Telling answerer to process offer: {}", new String(offer));
         
         
        final IceAgent answerer = new IceAgentImpl(turnClient, 
             mediaStreamFactory2, false, iceMediaFactory);
         
         Assert.assertFalse(answerer.isControlling());
         
         m_log.debug("About to generate answer...");
         final byte[] answer = answerer.generateAnswer();
         
         m_log.debug("Generated answer: {}", new String(answer));
         
         final AtomicBoolean answererCompleted = new AtomicBoolean(false);
         final AtomicBoolean offererCompleted = new AtomicBoolean(false);
         final OfferAnswerListener offererStateListener =
             new OfferAnswerListener()
             {
             public void onOfferAnswerComplete(final MediaOfferAnswer offerAnswer)
                 {
                 offererCompleted.set(true);
                 }
             };
         final OfferAnswerListener answererStateListener =
             new OfferAnswerListener()
             {
             public void onOfferAnswerComplete(final MediaOfferAnswer offerAnswer)
                 {
                 answererCompleted.set(true);
                 }
             };
 
         final AtomicBoolean threadFailed = new AtomicBoolean(false);
         final Thread answerThread = new Thread(new Runnable()
             {
 
             public void run()
                 {
                 try
                     {
                     answerer.processOffer(ByteBuffer.wrap(offer), answererStateListener);
                     }
                 catch (final IOException e)
                     {
                     threadFailed.set(true);
                     }
                 }
             
             });
         
         answerThread.setDaemon(true);
         answerThread.start();
         Thread.yield();
         
         final Collection<IceMediaStream> streams = answerer.getMediaStreams();
         Assert.assertEquals(1, streams.size());
         
         offerer.processAnswer(ByteBuffer.wrap(answer), offererStateListener);
         
         //final Socket sock = offerer.createSocket();
         
         Assert.assertFalse(threadFailed.get());
         
         synchronized (offererCompleted)
             {
             if (!offererCompleted.get())
                 offererCompleted.wait(4000);
             }
         
         synchronized (answererCompleted)
             {
             if (!answererCompleted.get())
                 answererCompleted.wait(4000);
             }
         
         Assert.assertTrue("Did not complete offer", offererCompleted.get());
         Assert.assertTrue("Did not complete answer", answererCompleted.get());
         }
 
     /*
      NOTE: The address used in this test only worked on a specific network --
      one where we could consistently generate ICMP errors.
     public void testPortUnreachable() throws Exception
         {
         
         final InetSocketAddress localAddress = 
             new InetSocketAddress(NetworkUtils.getLocalHost(), 44252);
         final InetSocketAddress remoteAddress =
             new InetSocketAddress("141.157.201.230", 54911);
         final IoSession session = 
             createClientSession(localAddress, remoteAddress);
         
         assertTrue(session.isConnected());
         
         m_gotIcmpError.set(false);
         session.write(new BindingRequest());
         
         synchronized (this.m_gotIcmpError)
             {
             this.m_gotIcmpError.wait(2000);
             }
         assertTrue("Did not get ICMP error", m_gotIcmpError.get());
         }
         */
     
     private IoSession createClientSession(final InetSocketAddress localAddress, 
         final InetSocketAddress remoteAddress) 
         {
         final DatagramConnector connector = new DatagramConnector();
         
         final DatagramConnectorConfig cfg = connector.getDefaultConfig();
         cfg.getSessionConfig().setReuseAddress(true);
 
         final String controlling = "Whatever";
         
         cfg.setThreadModel(
             ExecutorThreadModel.getInstance(
                 "IceUdpStunChecker-"+controlling));
         final ProtocolCodecFactory codecFactory = 
             new StunProtocolCodecFactory();
         final ProtocolCodecFilter stunFilter = 
             new ProtocolCodecFilter(codecFactory);
         
         connector.getFilterChain().addLast("stunFilter", stunFilter);
         
         final StunMessageVisitorFactory<StunMessage> visitorFactory =
             new StunMessageVisitorFactory<StunMessage>()
             {
 
             public StunMessageVisitor<StunMessage> createVisitor(IoSession session)
                 {
                 final StunMessageVisitor<StunMessage> visitor = 
                     new StunMessageVisitorAdapter<StunMessage>()
                     {
 
                     public StunMessage visitIcmpErrorMesssage(
                         final IcmpErrorStunMessage message)
                         {
                         m_log.debug("Got ICMP error!!");
                         m_gotIcmpError.set(true);
                         synchronized (m_gotIcmpError)
                             {
                             m_gotIcmpError.notify();
                             }
                         return message;
                         }
                     };
                 return visitor;
                 }
             
             };
         final IoHandler ioHandler = 
             new StunIoHandler<StunMessage>(visitorFactory);
         m_log.debug("Connecting from "+localAddress+" to "+remoteAddress);
         final ConnectFuture cf = 
             connector.connect(remoteAddress, localAddress, ioHandler);
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
     }
