 // Copyright (C) 2003 Philip Aston
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
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.ServerSocket;
 
 
 /**
  * Active object that accepts connections on a ServerSocket.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class Acceptor {
 
   private final ServerSocket m_serverSocket;
   private final ResourcePool[] m_socketSets;
   private final ThreadPool m_threadPool;
 
   /**
    * Constructor.
    *
    * @param addressString The TCP address to listen on. Zero-length
    * string => listen on all interfaces.
    * @param port The TCP port to listen to. 0 => use any free port.
    * @param numberOfThreads Number of acceptor threads.
    * @throws CommunicationException If server socket could not be
    * bound.
    */
   public Acceptor(String addressString, int port, int numberOfThreads)
     throws CommunicationException {
 
     if (addressString.length() > 0) {
       try {
         m_serverSocket =
           new ServerSocket(port, 50, InetAddress.getByName(addressString));
       }
       catch (IOException e) {
         throw new CommunicationException(
           "Could not bind to address '" + addressString + ":" + port + "'", e);
       }
     }
     else {
       try {
         m_serverSocket = new ServerSocket(port, 50);
       }
       catch (IOException e) {
         throw new CommunicationException(
           "Could not bind to port '" + port + "' on local interfaces", e);
       }
     }
 
     m_socketSets = new ResourcePool[ConnectionType.NUMBER_OF_CONNECTION_TYPES];
 
     for (int i = 0; i < m_socketSets.length; ++i) {
       m_socketSets[i] = new ResourcePool();
     }
 
     final ThreadPool.RunnableFactory runnableFactory =
       new ThreadPool.RunnableFactory() {
         public Runnable create() {
           return new Runnable() {
               public void run() { process(); }
             };
         };
       };
 
     m_threadPool =
       new ThreadPool("Acceptor", numberOfThreads, runnableFactory);
 
     m_threadPool.start();
   }
 
   /**
    * Shut down this acceptor.
    *
    * @throws CommunicationException If an IO exception occurs.
    */
   public void shutdown() throws CommunicationException {
 
     try {
       m_serverSocket.close();
     }
     catch (IOException e) {
       throw new CommunicationException("Error closing socket", e);
     }
     finally {
       for (int i = 0; i < m_socketSets.length; ++i) {
         m_socketSets[i].close();
       }
 
       m_threadPool.stop();
     }
   }
 
   /**
    * Get the port this Acceptor is listening on.
    *
    * @return The port.
    */
   public int getPort() {
     return m_serverSocket.getLocalPort();
   }
 
   /**
    * Get a set of accepted connections.
    *
    * @param connectionType Identifies the set of connections to
    * return.
    * @return A set of sockets, each wrapped in a {@link
    * SocketResource}.
    */
   ResourcePool getSocketSet(ConnectionType connectionType) {
     return m_socketSets[connectionType.toInteger()];
   }
 
   /**
    * Return the thread group used for our threads. Package scope; used
    * by the unit tests.
    *
    * @return The thread group.
    */
   ThreadGroup getThreadGroup() {
     return m_threadPool.getThreadGroup();
   }
 
   private void process() {
     try {
       while (true) {
         final Socket localSocket = m_serverSocket.accept();
 
        final InputStream inputStream = localSocket.getInputStream();
 
        final int connectionType = inputStream.read();
 
         if (connectionType < 0 ||
             connectionType >= ConnectionType.NUMBER_OF_CONNECTION_TYPES) {
           System.err.println("Unknown connection type: " + connectionType);
         }
         else {
           m_socketSets[connectionType].add(new SocketResource(localSocket));
         }
       }
     }
     catch (IOException e) {
       // Treat accept socket errors as fatal - we've probably been
       // shutdown.
     }
     finally {
       // Best effort to ensure our server socket is closed.
       try {
         shutdown();
       }
       catch (CommunicationException e) {
         // Ignore.
       }
     }
   }
 
   /**
    * Wrapper for sockets that are returned by {@link getSocketSet}.
    */
   static final class SocketResource implements ResourcePool.Resource {
     private final Socket m_socket;
 
     private SocketResource(Socket socket) {
       m_socket = socket;
     }
 
     public InputStream getInputStream() throws IOException {
       return m_socket.getInputStream();
     }
 
     public OutputStream getOutputStream() throws IOException {
       return m_socket.getOutputStream();
     }
 
     public void close() {
       try {
         m_socket.close();
       }
       catch (IOException e) {
         // Ignore.
       }
     }
   }
 }
