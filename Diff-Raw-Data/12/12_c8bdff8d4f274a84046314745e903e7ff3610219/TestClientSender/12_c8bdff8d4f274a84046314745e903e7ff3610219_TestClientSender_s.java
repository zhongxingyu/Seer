// Copyright (C) 2003, 2004, 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.communication;
 
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.net.Socket;
 
 import net.grinder.util.StreamCopier;
 
 import junit.framework.TestCase;
 
 
 /**
  *  Unit test case for <code>ClientSender</code>.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestClientSender extends TestCase {
 
   public TestClientSender(String name) {
     super(name);
   }
 
   public void testSend() throws Exception {
 
     final SocketAcceptorThread socketAcceptor = new SocketAcceptorThread();
 
     final Connector connector =
       new Connector(socketAcceptor.getHostName(), socketAcceptor.getPort(),
                     ConnectionType.AGENT);
 
     final Sender clientSender = ClientSender.connect(connector);
 
     socketAcceptor.join();
 
     final SimpleMessage message1 = new SimpleMessage();
     final SimpleMessage message2 = new SimpleMessage();
 
     clientSender.send(message1);
     clientSender.send(message2);
 
     final InputStream socketInput =
       socketAcceptor.getAcceptedSocket().getInputStream();
 
     assertEquals(ConnectionType.AGENT, ConnectionType.read(socketInput));
 
     // Need an ObjectInputStream for every message. See note in
     // ClientSender.writeMessage.
     final ObjectInputStream inputStream1 = new ObjectInputStream(socketInput);
     final Object o1 = inputStream1.readObject();
 
     final ObjectInputStream inputStream2 = new ObjectInputStream(socketInput);
     final Object o2 = inputStream2.readObject();
 
     assertEquals(message1, o1);
     assertEquals(message2, o2);
 
     assertEquals(0, socketInput.available());
 
     socketAcceptor.close();
 
     try {
       ClientReceiver.connect(connector);
       fail("Expected CommunicationException");
     }
     catch (CommunicationException e) {
     }
   }
 
   public void testShutdown() throws Exception {
 
     final SocketAcceptorThread socketAcceptor = new SocketAcceptorThread();
 
     final Connector connector =
       new Connector(socketAcceptor.getHostName(), socketAcceptor.getPort(),
                     ConnectionType.AGENT);
 
     final Sender clientSender = ClientSender.connect(connector);
 
     socketAcceptor.join();
 
     final Message message = new SimpleMessage();
 
     clientSender.send(message);
 
     clientSender.shutdown();
 
     try {
       clientSender.send(message);
       fail("Expected CommunicationException");
     }
     catch (CommunicationException e) {
     }
 
     final InputStream socketInput =
       socketAcceptor.getAcceptedSocket().getInputStream();
 
     assertEquals(ConnectionType.AGENT, ConnectionType.read(socketInput));
 
     final ObjectInputStream inputStream1 = new ObjectInputStream(socketInput);
     final Object o1 = inputStream1.readObject();
     assertNotNull(o1);
 
     final ObjectInputStream inputStream2 = new ObjectInputStream(socketInput);
     final Object o2 = inputStream2.readObject();
 
     assertTrue(o2 instanceof CloseCommunicationMessage);
 
     socketAcceptor.close();
   }
 
   public void testIsPeerShutdown() throws Exception {
     final SocketAcceptorThread socketAcceptor = new SocketAcceptorThread();
 
     final Connector connector =
       new Connector(socketAcceptor.getHostName(), socketAcceptor.getPort(),
                     ConnectionType.AGENT);
 
     final ClientSender clientSender = ClientSender.connect(connector);
 
     socketAcceptor.join();
 
     assertTrue(!clientSender.isPeerShutdown());
 
     new SocketWrapper(socketAcceptor.getAcceptedSocket()).close();
 
     assertTrue(clientSender.isPeerShutdown());
 
     socketAcceptor.close();
   }
 
   public void testWithPairedClientReceiver() throws Exception {
     final SocketAcceptorThread socketAcceptor = new SocketAcceptorThread();
 
     final Connector connector =
       new Connector(socketAcceptor.getHostName(), socketAcceptor.getPort(),
                     ConnectionType.AGENT);
 
     final ClientReceiver clientReceiver = ClientReceiver.connect(connector);
     final ClientSender clientSender = ClientSender.connect(clientReceiver);
 
     socketAcceptor.join();
 
     // Wire up the remote end to simply copy the bytes back to us.
     final Socket remoteSocket = socketAcceptor.getAcceptedSocket();
     assertEquals(ConnectionType.AGENT,
                  ConnectionType.read(remoteSocket.getInputStream()));
 
     new Thread(
       new StreamCopier(1000, true).getRunnable(remoteSocket.getInputStream(),
                                                remoteSocket.getOutputStream()),
       "Echo stream").start();
 
     final Message message = new SimpleMessage();
     clientSender.send(message);
     final Message receivedMessage = clientReceiver.waitForMessage();
 
     assertEquals(receivedMessage, message);
 
     assertFalse(clientSender.isPeerShutdown());
 
     clientReceiver.shutdown();
     assertTrue(clientSender.isPeerShutdown());
 
   }
 
  public void testWithBadPairedClientSender() throws Exception {
     final SocketAcceptorThread socketAcceptor = new SocketAcceptorThread();
 
     final Connector connector =
       new Connector(socketAcceptor.getHostName(), socketAcceptor.getPort(),
                     ConnectionType.AGENT);
 
     final ClientReceiver clientReceiver = ClientReceiver.connect(connector);
     clientReceiver.shutdown();
 
     try {
      ClientSender.connect(clientReceiver);
       fail("Expected CommunicationException");
     }
     catch (CommunicationException e) {
     }
   }
 }
