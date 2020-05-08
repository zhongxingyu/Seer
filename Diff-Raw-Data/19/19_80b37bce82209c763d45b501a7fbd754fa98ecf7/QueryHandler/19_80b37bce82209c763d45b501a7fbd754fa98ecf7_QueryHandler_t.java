 package org.kjkoster.zapcat.zabbix;
 
 /* This file is part of Zapcat.
  *
  * Zapcat is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Zapcat is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Zapcat.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceNotFoundException;
 
 import org.apache.log4j.Logger;
 
 /**
  * A JMX query handler for Zabbix. The query handler reads the query from the
  * socket, parses the request and constructs and sends a response.
  * <p>
  * You can configure the protocol version to use and set it to either
  * &quot;1.1&quot; or &quot;1.4&quot;.
  * 
  * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
  */
 final class QueryHandler implements Runnable {
     private static final Logger log = Logger.getLogger(QueryHandler.class);
 
     private final Socket socket;
 
     private final StringBuilder hexdump = new StringBuilder();
 
     /**
     * The return value that Zabbix interprets as the agent not supporting the item.
     */
    private static final String NOTSUPPORTED = "ZBX_NOTSUPPORTED";

    /**
      * Create a new query handler.
      * 
      * @param socket
      *            The socket that was accepted.
      */
     public QueryHandler(final Socket socket) {
         this.socket = socket;
     }
 
     /**
      * @see java.lang.Runnable#run()
      */
     public void run() {
         try {
             log.debug("started worker");
             try {
                 do {
                     handleQuery();
                 } while (socket.getInputStream().available() > 0);
             } finally {
                 if (socket != null) {
                     socket.close();
                 }
             }
             log.debug("worker is done");
         } catch (Exception e) {
             log.error("dropping exception", e);
         }
     }
 
     private void handleQuery() throws Exception {
         String request = receive(socket.getInputStream());
         log.debug("received '" + request + "'");
 
         String response = response(request);
         // make sure we can send
         if (response == null) {
             response = "";
         }
 
         log.debug("sending '" + response + "'");
         send(response, socket.getOutputStream());
     }
 
     private String receive(final InputStream in) throws IOException {
         String line = "";
         int b = in.read();
         while (b != -1 && b != 0x0a) {
             line += (char) b;
 
             b = in.read();
         }
 
         return line;
     }
 
     private String response(final String query) throws Exception {
         final int lastOpen = query.lastIndexOf('[');
         final int lastClose = query.lastIndexOf(']');
         final String attribute = query.substring(lastOpen + 1, lastClose);
 
         if (query.startsWith("jmx")) {
             final int firstClose = query.lastIndexOf(']', lastOpen);
             final int firstOpen = query.indexOf('[');
             final String objectName = query
                     .substring(firstOpen + 1, firstClose);
 
             try {
                 return JMXHelper.query(objectName, attribute);
             } catch (InstanceNotFoundException e) {
                 log.debug("no bean named " + objectName, e);
                return NOTSUPPORTED;
             } catch (AttributeNotFoundException e) {
                 log.debug("no attribute named " + attribute + " on bean named "
                         + objectName, e);
                return NOTSUPPORTED;
             }
         } else if (query.startsWith("system.property")) {
             return querySystemProperty(attribute);
         } else if (query.startsWith("system.env")) {
             return queryEnvironment(attribute);
        } else if (query.equals("agent.ping")) {
            return "1";
        } else if (query.equals("agent.version")) {
            return "zapcat 1.2-beta";
         }
 
        return NOTSUPPORTED;
     }
 
     /*
      * This method will go away once I have added collection support to the
      * query handler.
      */
     private String querySystemProperty(final String key) {
         log.debug("System property[" + key + "]");
         return System.getProperty(key);
     }
 
     private String queryEnvironment(final String key) {
         log.debug("Environment[" + key + "]");
         return System.getenv(key);
     }
 
     private void send(final String response, final OutputStream outputStream)
             throws IOException {
         final BufferedOutputStream out = new BufferedOutputStream(outputStream);
 
         if (isProtocol14()) {
             // write magic marker
             write(out, (byte) 'Z');
             write(out, (byte) 'B');
             write(out, (byte) 'X');
             write(out, (byte) 'D');
 
             // write protocol version
             write(out, (byte) 0x01);
 
             // length as 64 bit integer, little endian format
             long length = response.length();
             for (int i = 0; i < 8; i++) {
                 write(out, (byte) (length & 0xff));
 
                 length >>= 8;
             }
         }
 
         // response itself
         for (int i = 0; i < response.length(); i++) {
             write(out, (byte) response.charAt(i));
         }
 
         out.flush();
         log.debug("sent bytes " + hexdump);
     }
 
     private boolean isProtocol14() {
         final String protocolProperty = System
                 .getProperty(ZabbixAgent.PROTOCOL_PROPERTY);
         if (protocolProperty == null || "1.4".equals(protocolProperty)) {
             return true;
         }
         if ("1.1".equals(protocolProperty)) {
             return false;
         }
 
         log.warn("Unsupported protocol '" + protocolProperty + "', using 1.4");
         return true;
     }
 
     private void write(final BufferedOutputStream out, final byte b)
             throws IOException {
         final String hex = Integer.toHexString(b);
         if (hex.length() < 2) {
             hexdump.append("0");
         }
         hexdump.append(hex).append(" ");
 
         out.write(b);
     }
 }
