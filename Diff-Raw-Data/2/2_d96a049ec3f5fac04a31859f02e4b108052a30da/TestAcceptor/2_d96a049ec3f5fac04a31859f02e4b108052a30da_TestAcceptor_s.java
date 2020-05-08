// Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
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
 
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 
 /**
  *  Unit tests for <code>Acceptor</code>.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestAcceptor extends TestCase {
 
   public TestAcceptor(String name) {
     super(name);
   }
 
   public void testConstructor() throws Exception {
 
     final InetAddress[] localAddresses =
       InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
 
     final String[] testAddresses = new String[localAddresses.length + 2];
 
     // Loop back.
     testAddresses[0] = InetAddress.getByName(null).getHostName();
 
     // All addresses.
     testAddresses[1] = "";
 
     for (int i=0; i<localAddresses.length; ++i) {
       testAddresses[i + 2] = localAddresses[i].getHostName();
     }
 
     // Figure out a free local port.
     final ServerSocket serverSocket = new ServerSocket(0);
     final int port = serverSocket.getLocalPort();
     serverSocket.close();
 
     for (int i=0; i<testAddresses.length; ++i) {
       final Acceptor acceptor = new Acceptor(testAddresses[i], port);
       acceptor.shutdown();
 
       // Should also be able to use a Random port.
       final Acceptor acceptor2 = new Acceptor(testAddresses[i], 0);
       acceptor2.shutdown();
     }
   }
 
   public void testGetSocketSet() throws Exception {
 
     final ServerSocket serverSocket = new ServerSocket(0);
     final int port = serverSocket.getLocalPort();
     serverSocket.close();
 
     final Acceptor acceptor = new Acceptor("", port);
 
     final SocketSet socketSet = acceptor.getSocketSet();
     assertNotNull(socketSet);
     assertTrue(socketSet.reserveNextHandle().isSentinel());
 
     final Socket socket1 = new Socket(InetAddress.getByName(null), port);
     final Socket socket2 = new Socket(InetAddress.getByName(null), port);
 
     // Sleep until we've accepted both connections. Give up after a
     // few seconds.
     for (int i=0; i<10; ++i) {
       Thread.sleep(i * i * 10);
       final List handles = socketSet.reserveAllHandles();
 
       final Iterator iterator = handles.iterator();
 
       while (iterator.hasNext()) {
         ((SocketSet.Handle)iterator.next()).free();
       }
 
       if (handles.size() == 2) {
         break;
       }
     }
     
     assertSame(socketSet, acceptor.getSocketSet());
     assertEquals(2, socketSet.reserveAllHandles().size());
 
     acceptor.shutdown();
   }
 
   private Acceptor createAcceptor() throws Exception {
     // Figure out a free local port.
     final ServerSocket serverSocket = new ServerSocket(0);
     final int port = serverSocket.getLocalPort();
     serverSocket.close();
 
     return new Acceptor("", port);
   }
 
   public void testGetThreadGroup() throws Exception {
 
     final Acceptor acceptor1 = createAcceptor();
     final Acceptor acceptor2 = createAcceptor();
 
     final ThreadGroup threadGroup = acceptor1.getThreadGroup();
 
     assertTrue(!threadGroup.equals(acceptor2.getThreadGroup()));
 
     assertEquals(1, acceptor1.getThreadGroup().activeCount());
 
     acceptor1.shutdown();
     acceptor2.shutdown();
 
     assertEquals(threadGroup, acceptor1.getThreadGroup());
     assertTrue(threadGroup.isDestroyed());
   } 
 
   public void testShutdown() throws Exception {
 
     final ServerSocket serverSocket = new ServerSocket(0);
     final int port = serverSocket.getLocalPort();
     serverSocket.close();
 
     final Acceptor acceptor = new Acceptor("", port);
 
     final SocketSet socketSet = acceptor.getSocketSet();
 
     final Socket socket = new Socket(InetAddress.getByName(null), port);
 
     // Sleep until we've accepted the connection. Give up after a few
     // seconds.
     SocketSet.Handle handle = socketSet.reserveNextHandle();
 
     for (int i=0; handle.isSentinel() && i<10; ++i) {
 
       Thread.sleep(i * i * 10);
 
       handle = socketSet.reserveNextHandle();
     }
 
     assertTrue(!handle.isSentinel());
     assertTrue(socketSet.reserveNextHandle().isSentinel());
     handle.free();
 
     acceptor.shutdown();
 
     assertTrue(acceptor.getThreadGroup().isDestroyed());
 
     assertTrue(socketSet.reserveNextHandle().isSentinel());
   } 
 }
