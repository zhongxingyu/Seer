 // Copyright (C) 2000 Phil Dawes
 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
 // Copyright (C) 2003 Bertrand Ave
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
 
 package net.grinder.tools.tcpproxy;
 
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import net.grinder.util.TerminalColour;
 
 
 /**
  *
  * @author Phil Dawes
  * @author Philip Aston
  * @author Bertrand Ave
  * @version $Revision$
  */
 public class TCPProxyEngineImplementation implements TCPProxyEngine
 {
   private final TCPProxyFilter m_requestFilter;
   private final TCPProxyFilter m_responseFilter;
   private final ConnectionDetails m_connectionDetails;
   private final String m_requestColour;
   private final String m_responseColour;
 
   private final PrintWriter m_outputWriter;
 
   private final TCPProxySocketFactory m_socketFactory;
   private final ServerSocket m_serverSocket;
 
   public TCPProxyEngineImplementation(TCPProxySocketFactory socketFactory,
                                       TCPProxyFilter requestFilter,
                                       TCPProxyFilter responseFilter,
                                       PrintWriter outputWriter,
                                       ConnectionDetails connectionDetails,
                                       boolean useColour,
                                       int timeout)
     throws IOException
   {
     m_outputWriter = outputWriter;
 
     m_socketFactory = socketFactory;
     m_requestFilter = requestFilter;
     m_responseFilter = responseFilter;
     m_connectionDetails = connectionDetails;
 
     if (useColour) {
       m_requestColour = TerminalColour.RED;
       m_responseColour = TerminalColour.BLUE;
     }
     else {
       m_requestColour = "";
       m_responseColour = "";
     }
 
     m_serverSocket =
       m_socketFactory.createServerSocket(
         connectionDetails.getLocalHost(),
         connectionDetails.getLocalPort(),
         timeout);
   }
 
   /* Stop the engine and flush filter buffer. */
   public void stop()
   {
    m_requestFilter.stop();
     m_responseFilter.stop();
 
     // Close socket to stop engine.
     try {
       getServerSocket().close();
     }
     catch (java.io.IOException ioe) {
       // Be silent.
     }
   }
 
   public void run()
   {
     while (true) {
       final Socket localSocket;
 
       try {
         localSocket = m_serverSocket.accept();
       }
       catch (InterruptedIOException e) {
         System.err.println(ACCEPT_TIMEOUT_MESSAGE);
         return;
       }
       catch (IOException e) {
         e.printStackTrace(System.err);
         return;
       }
 
       try {
         launchThreadPair(localSocket,
                          localSocket.getInputStream(),
                          localSocket.getOutputStream(),
                          m_connectionDetails.getRemoteHost(),
                          m_connectionDetails.getRemotePort());
       }
       catch(IOException e) {
         e.printStackTrace(System.err);
       }
     }
   }
 
   public final ServerSocket getServerSocket()
   {
     return m_serverSocket;
   }
 
   protected final TCPProxySocketFactory getSocketFactory()
   {
     return m_socketFactory;
   }
 
   protected final ConnectionDetails getConnectionDetails()
   {
     return m_connectionDetails;
   }
 
   protected final void launchThreadPair(Socket localSocket,
                                         InputStream localInputStream,
                                         OutputStream localOutputStream,
                                         String remoteHost,
                                         int remotePort)
     throws IOException
   {
     final Socket remoteSocket =
       m_socketFactory.createClientSocket(remoteHost, remotePort);
 
     final ConnectionDetails connectionDetails =
       new ConnectionDetails(m_connectionDetails.getLocalHost(),
                             localSocket.getPort(),
                             remoteHost,
                             remoteSocket.getPort(),
                             m_connectionDetails.isSecure());
 
     new StreamThread(connectionDetails,
                      localInputStream,
                      remoteSocket.getOutputStream(),
                      m_requestFilter,
                      m_outputWriter,
                      m_requestColour);
 
     new StreamThread(connectionDetails.getOtherEnd(),
                      remoteSocket.getInputStream(),
                      localOutputStream,
                      m_responseFilter,
                      m_outputWriter,
                      m_responseColour);
   }
 }
 
