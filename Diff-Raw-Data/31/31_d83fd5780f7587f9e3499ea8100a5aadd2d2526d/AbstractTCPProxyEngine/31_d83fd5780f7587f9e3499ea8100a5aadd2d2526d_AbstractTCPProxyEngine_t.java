 // Copyright (C) 2000 Phil Dawes
 // Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005 Philip Aston
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
 import java.net.SocketException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.grinder.common.Logger;
 import net.grinder.util.TerminalColour;
 
 
 /**
  * Base class for TCPProxyEngine implementations.
  *
  * @author Phil Dawes
  * @author Philip Aston
  * @author Bertrand Ave
  * @version $Revision$
  */
 public abstract class AbstractTCPProxyEngine implements TCPProxyEngine {
 
   private static final ThreadGroup s_streamThreadGroup =
     new ThreadGroup("TCPProxy Stream Handler");
 
   private final TCPProxyFilter m_requestFilter;
   private final TCPProxyFilter m_responseFilter;
   private final String m_requestColour;
   private final String m_responseColour;
   private final Logger m_logger;
   private final PrintWriter m_outputWriter;
   private final TCPProxySocketFactory m_socketFactory;
   private final ServerSocket m_serverSocket;
   private final List m_streamThreads = new LinkedList();
 
   /**
    * Allow unit tests to access the <code>ThreadGroup</code> to be
    * used for stream handling threads.
    *
    * @return The thread group.
    */
   static final ThreadGroup getStreamThreadGroup() {
     return s_streamThreadGroup;
   }
 
   /**
    * Constructor.
    *
    * @param socketFactory Socket factory for creating our server and
    * client sockets.
    * @param requestFilter Request filter.
    * @param responseFilter Response filter.
    * @param logger Logger.
    * @param localEndPoint Local host and port to listen on. If the
    * <code>EndPoint</code>'s port is 0, an arbitrary port will be
    * assigned.
    * @param useColour Whether to use colour.
    * @param timeout Timeout for server socket in milliseconds.
    *
    * @exception IOException If an I/O error occurs.
    */
   public AbstractTCPProxyEngine(TCPProxySocketFactory socketFactory,
                                 TCPProxyFilter requestFilter,
                                 TCPProxyFilter responseFilter,
                                 Logger logger,
                                 EndPoint localEndPoint,
                                 boolean useColour,
                                 int timeout)
     throws IOException {
 
     m_logger = logger;
     m_outputWriter = logger.getOutputLogWriter();
 
     m_socketFactory = socketFactory;
     m_requestFilter = requestFilter;
     m_responseFilter = responseFilter;
 
     if (useColour) {
       m_requestColour = TerminalColour.RED;
       m_responseColour = TerminalColour.BLUE;
     }
     else {
       m_requestColour = "";
       m_responseColour = "";
     }
 
     m_serverSocket =
       m_socketFactory.createServerSocket(localEndPoint, timeout);
   }
 
   /**
    * Stop the engine and flush filter buffer.
    */
   public void stop() {
 
     m_requestFilter.stop();
     m_responseFilter.stop();
 
     // Close socket to stop engine.
     try {
       m_serverSocket.close();
     }
     catch (IOException ioe) {
       // Be silent.
     }
 
     // Ensure all our threads are shut down.
     final Iterator iterator = m_streamThreads.iterator();
 
     while (iterator.hasNext()) {
       ((StreamThread)iterator.next()).stop();
     }
   }
 
  /**
   * Check whether this engine is stopped.
   *
   * @return <code>true</code> => the engine is stopped.
   */
   public boolean isStopped() {
     return m_serverSocket.isClosed();
   }
 
   /**
    * Main event loop.
    */
   public abstract void run();
 
   /**
    * <code>IOException</code> that indicates that an accept has timed
    * out on our server socket, and we have no active threads handling
    * connections.
    */
   private static final class NoActivityTimeOutException extends IOException {
   }
 
   /**
    * Accept a connection using our server socket. Blocks until a
    * connection is accepted.
    *
    * @return The client socket.
    * @throws IOException If an I/O error occurred.
    * @throws NoActivityTimeOutException If the accept timed out, and
    * we have no active threads handling connections.
    */
   protected Socket accept() throws IOException, NoActivityTimeOutException {
     while (true) {
       try {
         return m_serverSocket.accept();
       }
       catch (InterruptedIOException e) {
         if (getStreamThreadGroup().activeCount() == 0) {
           stop();
           throw new NoActivityTimeOutException();
         }
       }
     }
   }
 
   /**
    * Return the EndPoint we are listening on.
    *
    * @return The <code>EndPoint</code>.
    */
   protected EndPoint getListenEndPoint() {
     return EndPoint.serverEndPoint(m_serverSocket);
   }
 
   /**
    * Allow subclasses to access socket factory.
    *
    * @return The socket factory.
    */
   protected final TCPProxySocketFactory getSocketFactory() {
     return m_socketFactory;
   }
 
   /**
    * Allow subclasses to access request filter.
    *
    * @return The filter.
    */
   protected final TCPProxyFilter getRequestFilter() {
     return m_requestFilter;
   }
 
   /**
    * Allow subclasses to access response filter.
    *
    * @return The filter.
    */
   protected final TCPProxyFilter getResponseFilter() {
     return m_responseFilter;
   }
 
   /**
    * Allow subclasses to access colour terminal control code for
    * request streams.
    *
    * @return The filter.
    */
   protected final String getRequestColour() {
     return m_requestColour;
   }
 
   /**
    * Allow subclasses to access request terminal control code for
    * response streams.
    *
    * @return The filter.
    */
   protected final String getResponseColour() {
     return m_responseColour;
   }
 
  /**
   * Wrapper for handling stream threads. Thread is automatically started on
   * construction. The thread is stopped when the engine is stopped by closing
   * the associated input stream.
   */
   protected class StreamThread {
     private final Thread m_thread;
     private final InputStream m_inputStream;
 
    /**
     * Constructor. Starts a Thread to run the Runnable.
     *
     * @param runnable What to do.
     * @param name Thread name.
     * @param inputStream Stream to close when shutting down.
     */
     public StreamThread(Runnable runnable, String name,
                         InputStream inputStream) {
       m_thread = new Thread(getStreamThreadGroup(), runnable, name);
       m_inputStream = inputStream;
       m_streamThreads.add(this);
       m_thread.start();
     }
 
    /**
     * Close the associated input stream and thus stop the thread.
     */
     public void stop() {
       try {
         m_inputStream.close();
       }
       catch (IOException e) {
         // Ignore.
       }
 
       try {
         m_thread.join();
       }
       catch (InterruptedException e) {
         // Oh well.
       }
     }
   }
 
   /**
    * Launch a pair of threads to handle bi-directional stream
    * communication.
    *
    * @param localSocket Local socket.
    * @param remoteEndPoint ConnectionDetails. The remote
    * <code>EndPoint</code> to forward output to. This is also used in
    * the logging and filter output.
    * @param clientEndPoint The <code>EndPoint</code> to be used in the
    * logging and filter output. This may well differ from the
    * <code>localSocket</code> binding.
    * @param isSecure Whether the connection is secure.
    *
    * @exception IOException If an I/O error occurs.
    */
   protected final void launchThreadPair(Socket localSocket,
                                         EndPoint remoteEndPoint,
                                         EndPoint clientEndPoint,
                                         boolean isSecure)
     throws IOException {
 
 
     final Socket remoteSocket =
       m_socketFactory.createClientSocket(remoteEndPoint);
 
     final ConnectionDetails connectionDetails =
       new ConnectionDetails(clientEndPoint, remoteEndPoint, isSecure);
 
     new FilteredStreamThread(localSocket.getInputStream(),
                              new OutputStreamFilterTee(
                                connectionDetails,
                                remoteSocket.getOutputStream(),
                                m_requestFilter,
                                m_requestColour));
 
     new FilteredStreamThread(remoteSocket.getInputStream(),
                              new OutputStreamFilterTee(
                                connectionDetails.getOtherEnd(),
                                localSocket.getOutputStream(),
                                m_responseFilter,
                                m_responseColour));
   }
 
   /**
    * <code>Runnable</code> which actively reads an input stream and
    * writes to an output stream, passing the data through a filter.
    */
   protected final class FilteredStreamThread implements Runnable {
 
     // For simplicity, the filters take a buffer oriented approach.
     // This means that they all break at buffer boundaries. Our buffer
     // is huge, so we shouldn't practically cause a problem, but the
     // network clearly can by giving us message fragments. I consider
     // this a bug, we really ought to take a stream oriented approach.
     private static final int BUFFER_SIZE = 65536;
 
     private final InputStream m_in;
     private final OutputStreamFilterTee m_outputStreamFilterTee;
 
     /**
      * Constructor.
      */
     FilteredStreamThread(InputStream in,
                          OutputStreamFilterTee outputStreamFilterTee) {
 
       m_in = in;
       m_outputStreamFilterTee = outputStreamFilterTee;
 
       new StreamThread(
         this,
         "Filter thread for " + outputStreamFilterTee.getConnectionDetails(),
         m_in);
     }
 
     /**
      * Main event loop.
      */
     public void run() {
 
       m_outputStreamFilterTee.connectionOpened();
 
       final byte[] buffer = new byte[BUFFER_SIZE];
 
       try {
         while (true) {
           final int bytesRead = m_in.read(buffer, 0, BUFFER_SIZE);
 
           if (bytesRead == -1) {
             break;
           }
 
           m_outputStreamFilterTee.handle(buffer, bytesRead);
         }
       }
       catch (SocketException e) {
         // Ignore, assume closed.
       }
       catch (IOException e) {
         logIOException(e);
       }
       finally {
         m_outputStreamFilterTee.connectionClosed();
       }
 
       // Tidy up.
       try {
         m_in.close();
       }
       catch (IOException e) {
         // Ignore.
       }
     }
   }
 
   /**
    * Log IOExceptions.
    *
    * @param e The exception.
    */
   protected final void logIOException(IOException e) {
 
     final Class c = e.getClass();
     final String message = e.getMessage();
 
     if (NoActivityTimeOutException.class.equals(c)) {
       getLogger().error("Listen time out");
     }
     else if (IOException.class.equals(c) && "Stream closed".equals(message) ||
              SocketException.class.equals(c)) {
       // Ignore common exceptions that are due to connections being
       // closed.
     }
     else {
       e.printStackTrace(getLogger().getErrorLogWriter());
     }
   }
 
   /**
    * Accessor for the logger.
    *
    * @return The <code>Logger</code>.
    */
   protected final Logger getLogger() {
     return m_logger;
   }
 
   /**
    * Filter like class that delegates to a user filter and tees the
    * result to an output stream. It is constructed for a particular
    * connection. Also controls output of colour codes to the terminal.
    */
   protected final class OutputStreamFilterTee {
 
     private final ConnectionDetails m_connectionDetails;
     private final OutputStream m_out;
     private final TCPProxyFilter m_filter;
     private final String m_colour;
     private final String m_resetColour;
 
     /**
      * Constructor.
      *
      * @param connectionDetails Connection details.
      * @param out The output stream.
      * @param filter The user filter.
      * @param colourString Terminal control code which sets appropriate
      * colours for this stream.
      */
     public OutputStreamFilterTee(ConnectionDetails connectionDetails,
                                  OutputStream out, TCPProxyFilter filter,
                                  String colourString) {
 
       m_connectionDetails = connectionDetails;
       m_out = out;
       m_filter = filter;
       m_colour = colourString;
       m_resetColour = m_colour.length() > 0 ? TerminalColour.NONE : "";
     }
 
     /**
      * A new connection has been opened.
      */
     public void connectionOpened() {
 
       preOutput();
 
       try {
         m_filter.connectionOpened(m_connectionDetails);
       }
       catch (Exception e) {
         e.printStackTrace(getLogger().getErrorLogWriter());
       }
       finally {
         postOutput();
       }
     }
 
     /**
      * Handle a message fragment.
      *
      * @param buffer Contains the data.
      * @param bytesRead How many bytes of data in <code>buffer</code>.
      * @exception IOException If an I/O error occurs writing to the
      * output stream.
      */
     public void handle(byte[] buffer, int bytesRead) throws IOException {
 
       preOutput();
 
       byte[] newBytes = null;
 
       try {
         newBytes = m_filter.handle(m_connectionDetails, buffer, bytesRead);
       }
       catch (Exception e) {
         e.printStackTrace(getLogger().getErrorLogWriter());
       }
       finally {
         postOutput();
       }
 
       if (newBytes != null) {
         m_out.write(newBytes);
       }
       else {
         m_out.write(buffer, 0, bytesRead);
       }
     }
 
     /**
      * A connection has been closed.
      */
     public void connectionClosed() {
 
       preOutput();
 
       try {
         m_filter.connectionClosed(m_connectionDetails);
       }
       catch (Exception e) {
         e.printStackTrace(getLogger().getErrorLogWriter());
       }
       finally {
         postOutput();
       }
 
       // Close our output stream. This will cause any
       // FilteredStreamThread managing the paired stream to exit.
       try {
         m_out.close();
       }
       catch (IOException e) {
         // Ignore.
       }
     }
 
     /**
      * Accessor for connection details.
      *
      * @return The connection details.
      */
     public ConnectionDetails getConnectionDetails() {
       return m_connectionDetails;
     }
 
     private void preOutput() {
       m_outputWriter.print(m_colour);
     }
 
     private void postOutput() {
       m_outputWriter.print(m_resetColour);
       m_outputWriter.flush();
     }
   }
 }
 
