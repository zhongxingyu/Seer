 // Copyright (C) 2006 Philip Aston
 // All rights reserved.
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
 
 package net.grinder.plugin.http.tcpproxyfilter;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.picocontainer.Disposable;
 
 import net.grinder.tools.tcpproxy.ConnectionDetails;
 
 
 /**
  * Map of {@link ConnectionDetails} to handlers.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class ConnectionCache
   implements HTTPFilterEventListener, Disposable {
 
   private final ConnectionHandlerFactory m_connectionHandlerFactory;
   private final Map m_handlers = Collections.synchronizedMap(new HashMap());
 
   /**
    * Constructor.
    *
    * @param connectionHandlerFactory Factory for connection handlers.
    */
   public ConnectionCache(ConnectionHandlerFactory connectionHandlerFactory) {
     m_connectionHandlerFactory = connectionHandlerFactory;
   }
 
   /**
    * A connection has been opened, create a handler.
    *
    * @param connectionDetails Details.
    */
   public void open(ConnectionDetails connectionDetails) {
     synchronized (m_handlers) {
       if (m_handlers.containsKey(connectionDetails)) {
         throw new IllegalArgumentException(
           "Connection " + connectionDetails + " already opened");
       }
 
       m_handlers.put(connectionDetails,
                      m_connectionHandlerFactory.create(connectionDetails));
     }
   }
 
   /**
    * A request message has been received.
    *
    * @param connectionDetails Connection details.
    * @param buffer Buffer containing message.
    * @param bytesRead Length of message.
    */
   public void request(
     ConnectionDetails connectionDetails, byte[] buffer, int bytesRead) {
 
     final ConnectionHandler handler =
       (ConnectionHandler)m_handlers.get(connectionDetails);
 
     if (handler == null) {
       throw new IllegalArgumentException(
         "Unknown connection " + connectionDetails);
     }
 
     handler.handleRequest(buffer, bytesRead);
   }
 
   /**
    * Part of a response message has been received.
    *
    * @param connectionDetails Connection details.
    * @param buffer Buffer containing message.
    * @param bytesRead Length of message.
    */
   public void response(
     ConnectionDetails connectionDetails, byte[] buffer, int bytesRead) {
 
     final ConnectionHandler handler =
       (ConnectionHandler)m_handlers.get(connectionDetails.getOtherEnd());
 
     if (handler == null) {
       throw new IllegalArgumentException(
         "Unknown connection " + connectionDetails);
     }
 
     handler.handleResponse(buffer, bytesRead);
   }
 
   /**
   * A connection has been closed, remove the handler.
    *
    * @param connectionDetails Details.
    */
   public void close(ConnectionDetails connectionDetails) {
 
     final ConnectionHandler handler =
       (ConnectionHandler)m_handlers.remove(connectionDetails);
 
     if (handler == null) {
       throw new IllegalArgumentException(
         "Unknown connection " + connectionDetails);
     }
 
     handler.newRequestMessage();
   }
 
   /**
    * Called after the filter has been stopped.
    */
   public void dispose() {
     // Close all handlers.
     synchronized (m_handlers) {
       final Iterator iterator = m_handlers.values().iterator();
 
       while (iterator.hasNext()) {
         final ConnectionHandler handler = (ConnectionHandler)iterator.next();
         handler.newRequestMessage();
       }
     }
   }
 }
