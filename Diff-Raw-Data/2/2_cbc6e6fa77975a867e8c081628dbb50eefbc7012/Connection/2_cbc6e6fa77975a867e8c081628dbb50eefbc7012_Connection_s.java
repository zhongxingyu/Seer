 /*-
  * Copyright (c) 2010, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.util;
 
 import net.rim.device.api.crypto.tls.tls10.TLS10Connection;
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.io.NoCopyByteArrayOutputStream;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Arrays;
 import net.rim.device.cldc.io.ssl.TLSException;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.SocketConnection;
 import javax.microedition.io.StreamConnection;
 
 
 /**
  * This is the abstract base class for socket connections used inside the SMTP,
  * POP3, and IMAP protocols of the mail package.  It handles the details of
  * sending data in what ever manner is appropriate to the mail protocol, and
  * receiving data in whole lines.  The specifics of opening network connections
  * on the BlackBerry platform is delegated to subclasses.
  */
 public abstract class Connection {
     protected static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
     
     /** Select everything except WiFi */
     protected static final int TRANSPORT_AUTO = 0xFE;
     /** Select WiFi */
     protected static final int TRANSPORT_WIFI = 0x01;
     /** Select Direct TCP */
     protected static final int TRANSPORT_DIRECT_TCP = 0x02;
     /** Select MDS */
     protected static final int TRANSPORT_MDS = 0x04;
     /** Select WAP 2.0 */
     protected static final int TRANSPORT_WAP2 = 0x08;
 
     private static final byte CR = (byte)0x0D;
     private static final byte LF = (byte)0x0A;
     private static final byte[] CRLF = new byte[] { CR, LF };
     private static String strCRLF = "\r\n";
 
     private UtilFactory utilFactory;
     protected String serverName;
     protected int serverPort;
     protected boolean useSSL;
     protected int transports;
     private StreamConnection socket;
     private String localAddress;
     private String connectionUrl;
     protected GlobalConfig globalConfig;
     protected boolean useWiFi;
     private InputStream input;
     private OutputStream output;
     private int fakeAvailable = -1;
     private int bytesSent = 0;
     private int bytesReceived = 0;
     private final Object socketLock = new Object();
     
     /**
      * Byte stream used to hold received data before it is passed back to
      * the rest of the application.
      */
     private final ByteArrayOutputStream byteStream = new NoCopyByteArrayOutputStream(1024);
     
     /**
      * Temporary read buffer used as an intermediary between the socket and
      * the byteStream.
      */
     private final byte[] readBuffer = new byte[1024];
     
     /**
      * Initializes a new connection object.
      * 
      * @param connectionConfig Configuration data for the connection
      */
     protected Connection(ConnectionConfig connectionConfig) {
         this.globalConfig = MailSettings.getInstance().getGlobalConfig();
         this.utilFactory = UtilFactory.getInstance();
         
         this.serverName = connectionConfig.getServerName();
         this.serverPort = connectionConfig.getServerPort();
         this.useSSL = (connectionConfig.getServerSecurity() == ConnectionConfig.SECURITY_SSL);
         
         int transportType;
         boolean enableWiFi;
         if(connectionConfig.getTransportType() == ConnectionConfig.TRANSPORT_GLOBAL) {
             transportType = globalConfig.getTransportType();
             enableWiFi = globalConfig.getEnableWiFi();
         }
         else {
             transportType = connectionConfig.getTransportType();
             enableWiFi = connectionConfig.getEnableWiFi();
         }
 
         // Populate the bit-flags for the selected transport types
         // based on the configuration parameters.
         switch(transportType) {
         case ConnectionConfig.TRANSPORT_WIFI_ONLY:
             transports = Connection.TRANSPORT_WIFI;
             break;
         case ConnectionConfig.TRANSPORT_AUTO:
             transports = Connection.TRANSPORT_AUTO;
             break;
         case ConnectionConfig.TRANSPORT_DIRECT_TCP:
             transports = Connection.TRANSPORT_DIRECT_TCP;
             break;
         case ConnectionConfig.TRANSPORT_MDS:
             transports = Connection.TRANSPORT_MDS;
             break;
         case ConnectionConfig.TRANSPORT_WAP2:
             transports = Connection.TRANSPORT_WAP2;
             break;
         default:
             // Should only get here in rare cases of invalid configuration
             // data, so we select full automatic with WiFi.
             transports = Connection.TRANSPORT_AUTO;
             enableWiFi = true;
             break;
         }
         if(enableWiFi) { transports |= Connection.TRANSPORT_WIFI; }
         
         this.input = null;
         this.output = null;
         this.socket = null;
     }
     
     /**
      * Opens a connection.
      */
     public void open() throws IOException {
         if ((input != null) || (output != null) || (socket != null)) {
             close();
         }
         
         utilFactory.addOpenConnection(this);
 
         synchronized(socketLock) {
             socket = openStreamConnection();
             if(socket == null) {
                 throw new IOException(resources.getString(LogicMailResource.ERROR_UNABLE_TO_OPEN_CONNECTION));
             }
             
             input = socket.openDataInputStream();
             output = socket.openDataOutputStream();
             localAddress = ((SocketConnection) socket).getLocalAddress();
             bytesSent = 0;
             bytesReceived = 0;
         }
         
         if (EventLogger.getMinimumLevel() >= EventLogger.INFORMATION) {
             String msg = "Connection established:\r\n" + "Socket: " +
             socket.getClass().toString() + strCRLF + "Local address: " +
             localAddress + strCRLF;
             EventLogger.logEvent(AppInfo.GUID, msg.getBytes(),
                     EventLogger.INFORMATION);
         }
     }
     
     /**
      * Open a stream connection.
      * This method should encapsulate all platform-specific logic for opening
      * network connections.  If the connection is successfully opened, this
      * method should also call {@link #setConnectionUrl(String)} to set the
      * chosen connection string.
      * 
      * @return the stream connection
      * 
      * @throws IOException Signals that an I/O exception has occurred.
      */
     protected abstract StreamConnection openStreamConnection() throws IOException;
 
     /**
      * Sets the connection string used to open the current connection.
      *
      * @param connectionUrl the new connection string
      */
     protected void setConnectionUrl(String connectionUrl) {
         this.connectionUrl = connectionUrl;
     }
     
     /**
      * Gets the connection string used to open the current connection.
      *
      * @return the connection string
      */
     public String getConnectionUrl() {
         return connectionUrl;
     }
     
     /**
      * Closes a connection.
      */
     public void close() throws IOException {
         synchronized(socketLock) {
             try {
                 if (input != null) {
                     input.close();
                     input = null;
                 }
             } catch (Exception exp) {
                 input = null;
             }
     
             try {
                 if (output != null) {
                     output.close();
                     output = null;
                 }
             } catch (Exception exp) {
                 output = null;
             }
     
             try {
                 if (socket != null) {
                     socket.close();
                     socket = null;
                 }
             } catch (Exception exp) {
                 socket = null;
             }
             setConnectionUrl(null);
         }
         
         utilFactory.removeOpenConnection(this);
         
         EventLogger.logEvent(AppInfo.GUID, "Connection closed".getBytes(),
                 EventLogger.INFORMATION);
     }
 
     /**
      * Determine whether we are currently connected
      * @return True if connected
      */
     public boolean isConnected() {
         if (socket != null) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Get the local address to which this connection is bound
      * @return Local address
      */
     public String getLocalAddress() {
         return localAddress;
     }
 
     /**
      * Get the server name used when this connection was created
      * @return Server name
      */
     public String getServerName() {
         return serverName;
     }
 
     /**
      * Gets the number of bytes that have been sent since the
      * connection was opened.
      * <p>
      * The counter is not synchronized, so it should only be
      * called from the same thread as the send and receive
      * methods.
      * </p>
      * @return bytes sent
      */
     public int getBytesSent() {
         return bytesSent;
     }
 
     /**
      * Gets the number of bytes that have been received since the
      * connection was opened.
      * <p>
      * The counter is not synchronized, so it should only be
      * called from the same thread as the send and receive
      * methods.
      * </p>
      * @return bytes received
      */
     public int getBytesReceived() {
         return bytesReceived;
     }
 
     /**
      * Sends a string to the server, terminating it with a CRLF.
      * No cleanup is performed, as it is expected that the string
      * is a prepared protocol command.
      */
     public void sendCommand(String s) throws IOException {
         if (globalConfig.getConnDebug()) {
             EventLogger.logEvent(AppInfo.GUID, ("[SEND CMD] " + s).getBytes(),
                     EventLogger.DEBUG_INFO);
         }
 
         synchronized(socketLock) {
             if (s == null) {
                 output.write(CRLF, 0, 2);
                 bytesSent += 2;
             } else {
                 byte[] buf = (s + strCRLF).getBytes();
                 output.write(buf);
                 bytesSent += buf.length;
             }
     
             output.flush();
         }
     }
 
     /**
      * Sends a string to the server. This method is used to bypass all
      * the processing done by the normal send method, and is most useful
      * for bulk transmissions.  It writes the provided string to the socket
      * in a single command, followed by a flush.
      *
      * @see #send
      */
     public void sendRaw(String s) throws IOException {
         byte[] buf = s.getBytes();
 
         if (globalConfig.getConnDebug()) {
             EventLogger.logEvent(AppInfo.GUID,
                     ("[SEND RAW]\r\n" + s).getBytes(), EventLogger.DEBUG_INFO);
         }
 
         synchronized(socketLock) {
             output.write(buf, 0, buf.length);
             bytesSent += buf.length;
     
             output.flush();
         }
     }
 
     /**
      * Returns the number of bytes available for reading.
      * Used to poll the connection without blocking.
      *
      * @see InputStream#available()
      */
     public int available() throws IOException {
         if (fakeAvailable == -1) {
             return input.available();
         } else {
             return fakeAvailable;
         }
     }
 
     /**
      * Receives a string from the server. This method is used internally for
      * incoming communication from the server. The main thing it does is
      * ensure that only complete lines are returned to the application, that is,
      * lines that were terminated at least by a LF.  Neither CRs nor LFs are
      * returned as part of the result.
      *
      * @return the complete line, minus the CRLF, as a byte array
      */
     public byte[] receive() throws IOException {
         return receive(lineResponseTester);
     }
     
     
     /**
      * Receives a string from the server. This method is used internally for
      * incoming communication from the server.
      *
      * @param responseTester class to determine when a complete response has
      *   been read from the network, and whether to trim it prior to returning
      * @return the complete response, as a byte array
      */
     public byte[] receive(ConnectionResponseTester responseTester) throws IOException {
         byte[] result = receiveImpl(responseTester);
         
         if(result != null && globalConfig.getConnDebug()) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("[RECV] " + responseTester.logString(result)).getBytes(),
                         EventLogger.DEBUG_INFO);
         }
         
         return result;
     }
     
     private byte[] receiveImpl(ConnectionResponseTester responseTester) throws IOException {
         synchronized(socketLock) {
             // Check existing data for a usable line
             byte[] line = checkForLine(responseTester);
             if(line != null) {
                 return line;
             }
     
             // Read from the socket
             int firstByte = input.read();
             if(firstByte != -1) {
                 byteStream.write((byte)firstByte);
                 bytesReceived++;
                 int bytesAvailable = input.available();
                 while(bytesAvailable > 0) {
                    int len = input.read(readBuffer);
                     byteStream.write(readBuffer, 0, len);
                     bytesReceived += len;
                     
                     // Check read data for a usable line
                     line = checkForLine(responseTester);
                     if(line != null) {
                         return line;
                     }
                     
                     bytesAvailable = input.available();
     
                     // If no bytes are reported as being available, but we have
                     // not yet received a full line, then we need to attempt
                     // another single-byte blocking read.
                     if(bytesAvailable == 0) {
                         firstByte = input.read();
                         byteStream.write((byte)firstByte);
                         bytesReceived++;
                         bytesAvailable = input.available();
                     }
                 }
             }
             else {
                 // If we got here, that means that the InputStream is either closed
                 // or we are in some otherwise unrecoverable state.  This means we
                 // will try to close the connection, ignore any errors from the
                 // close operation, and throw an IOException.
                 
                 EventLogger.logEvent(AppInfo.GUID,
                         "Unable to read from socket, closing connection".getBytes(),
                         EventLogger.INFORMATION);
     
                 try {
                     close();
                 } catch (IOException e) { }
     
                 throw new IOException("Connection closed");
             }
         }
         // This should never normally happen
         return null;
     }
     
     /**
      * Checks the byte stream buffer for a usable line of returnable data.
      * If a line is returned, the buffer will be updated to only contain data
      * following that line.
      *
      * @return the trimmed string which ended in a CRLF in the source data
      */
     private byte[] checkForLine(ConnectionResponseTester responseTester) throws IOException {
         byte[] result;
         
         byte[] buf = byteStream.toByteArray();
         int size = byteStream.size();
         
         int p = responseTester.checkForCompleteResponse(buf, size);
         
         if(p != -1) {
             int trimCount = responseTester.trimCount();
             
             result = Arrays.copy(buf, 0, p - trimCount);
             
             if(p < size) {
                 buf = Arrays.copy(buf, p, size - p);
                 byteStream.reset();
                 byteStream.write(buf);
                 fakeAvailable = buf.length;
             }
             else {
                 byteStream.reset();
                 fakeAvailable = -1;
             }
         }
         else {
             fakeAvailable = size;
             result = null;
         }
         return result;
     }
 
     private static ConnectionResponseTester lineResponseTester = new ConnectionResponseTester() {
         private int trimCount;
         
         public int checkForCompleteResponse(byte[] buf, int len) {
             trimCount = 0;
             int p = Arrays.getIndex(buf, LF);
             if(p != -1 && p < len) {
                 if(p > 0 && buf[p - 1] == CR) {
                     trimCount = 2;
                 }
                 else {
                     trimCount = 1;
                 }
                 return ++p;
             }
             else {
                 return -1;
             }
         }
 
         public int trimCount() {
             return trimCount;
         }
         
         public String logString(byte[] result) {
             return new String(result);
         };
     };
     
     /**
      * Switches the underlying connection to SSL mode, as commonly done after
      * sending a <tt>STARTTLS</tt> command to the server.
      * 
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public void startTLS() throws IOException {
         synchronized(socketLock) {
             // Shortcut the method if we're already in SSL mode
             if(socket instanceof TLS10Connection) { return; }
             
             if(socket == null || connectionUrl == null) {
                 throw new IOException("Connection has not been opened");
             }
     
             try {
                 TLS10Connection tlsSocket = new TLS10Connection(
                         new StreamConnectionWrapper(
                                 socket,
                                 (DataInputStream)input,
                                 (DataOutputStream)output),
                                 connectionUrl,
                                 true);
     
                 socket = tlsSocket;
                 input = socket.openDataInputStream();
                 output = socket.openDataOutputStream();
             } catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("Unable to switch to TLS mode: " + e.getMessage()).getBytes(), EventLogger.ERROR);
                 throw new IOException("Unable to switch to TLS mode");
             } catch (TLSException e) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("Unable to switch to TLS mode: " + e.getMessage()).getBytes(), EventLogger.ERROR);
                 throw new IOException("Unable to switch to TLS mode");
             }
         }
     }
 
     /**
      * Decorator to wrap an existing stream connection so its I/O streams
      * can be reopened without throwing exceptions.
      */
     private static class StreamConnectionWrapper implements StreamConnection {
         private StreamConnection stream;
         private DataInputStream dataInputStream;
         private DataOutputStream dataOutputStream;
 
         public StreamConnectionWrapper(StreamConnection stream, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
             this.stream = stream;
             this.dataInputStream = dataInputStream;
             this.dataOutputStream = dataOutputStream;
         }
 
         public DataInputStream openDataInputStream() throws IOException {
             return dataInputStream;
         }
         public InputStream openInputStream() throws IOException {
             return dataInputStream;
         }
         public void close() throws IOException {
             stream.close();
         }
         public DataOutputStream openDataOutputStream() throws IOException {
             return dataOutputStream;
         }
         public OutputStream openOutputStream() throws IOException {
             return dataOutputStream;
         }
     }
 }
