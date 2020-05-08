 /**
  * Copyright 2013 Bjørn Remseth (la3lma@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package no.rmz.blobee.rpc;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.logging.Logger;
 import no.rmz.blobee.rpc.client.RpcClient;
 import no.rmz.blobee.rpc.peer.RpcMessageListener;
 import no.rmz.blobee.rpc.server.RpcServer;
 import no.rmz.blobee.serviceimpls.SampleServerImpl;
 import no.rmz.blobeeproto.api.proto.Rpc;
 import no.rmz.blobeetestproto.api.proto.Testservice;
 import no.rmz.testtools.Net;
 import no.rmz.testtools.Receiver;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public final class RpcPeerStartupAndShutdownTest {
 
     private static final Logger log = Logger.getLogger(
             no.rmz.blobee.rpc.RpcPeerStartupAndShutdownTest.class.getName());
     private final static String HOST = "localhost";
     private final static Rpc.RpcControl HEARTBEAT_MESSAGE =
             Rpc.RpcControl.newBuilder().setMessageType(Rpc.MessageType.HEARTBEAT).build();
     private final static Rpc.RpcControl SHUTDOWN_MESSAGE =
             Rpc.RpcControl.newBuilder().setMessageType(Rpc.MessageType.SHUTDOWN).build();
     private int port;
 
     @Before
     public void setUp() throws IOException {
         port = Net.getFreePort();
     }
     private RpcServer rpcServer;
     private RpcClient rpcclient;
 
     private void startClientAndServer(final RpcMessageListener ml) {
         rpcServer =
                 RpcSetup.newServer(
                 new InetSocketAddress(HOST, port),
                 ml)
                 .addImplementation(
                 new SampleServerImpl(),
                 Testservice.RpcService.Interface.class)
                 .start();
 
         rpcclient =
                 RpcSetup
                 .newClient(new InetSocketAddress(HOST, port))
                 .addInterface(Testservice.RpcService.class)
                 .start();
     }
 
     @After
     public void shutDown() {
         rpcclient.stop();
         rpcServer.stop();
     }
     @Mock
     Receiver<Rpc.RpcControl> heartbeatReceiver;
     @Mock
     Receiver<Rpc.RpcControl> shutdownReceiver;
 
     public void sleepHalfASec() {
         try {
             Thread.currentThread().sleep(500);
         }
         catch (InterruptedException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @Test(timeout = 10000)
     public void testTransmissionOfHeartbeatsAtStartup() {
 
         final RpcMessageListener ml = new RpcMessageListener() {
             @Override
             public void receiveMessage(
                     final Object message,
                     final ChannelHandlerContext ctx) {
                 if (message instanceof Rpc.RpcControl) {
                     final Rpc.RpcControl msg = (Rpc.RpcControl) message;
                     heartbeatReceiver.receive(msg);
                     ctx.getChannel().close();
                 }
             }
         };
         startClientAndServer(ml);
 


         sleepHalfASec();
         verify(heartbeatReceiver, times(1)).receive(HEARTBEAT_MESSAGE);
     }
 
     @Test(timeout = 10000)
     public void testReactionToShutdown() {
 
         final RpcMessageListener ml = new RpcMessageListener() {
             @Override
             public void receiveMessage(
                     final Object message,
                     final ChannelHandlerContext ctx) {
                 if (message instanceof Rpc.RpcControl) {
                     final Rpc.RpcControl msg = (Rpc.RpcControl) message;
 
 
                     if (msg.getMessageType() == Rpc.MessageType.HEARTBEAT) {
                         heartbeatReceiver.receive(msg);
                         ctx.getChannel().write(SHUTDOWN_MESSAGE);
                     } else if (msg.getMessageType() == Rpc.MessageType.SHUTDOWN) {
                         shutdownReceiver.receive(msg);
                     }
                 }
             }
         };
 
         startClientAndServer(ml);
 
 
         // Need some time to let the startup transient settle.
         // XXX Should perhaps used a lock instead?
         sleepHalfASec();
 
         verify(heartbeatReceiver).receive(HEARTBEAT_MESSAGE);
 
         // XXX  This test is bogus, since it does not have the right
         //      probes in the right places.  It needs to be fixed.
         // verify(shutdownReceiver).receive(SHUTDOWN_MESSAGE);
     }
 }
