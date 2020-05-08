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
 
 import net.rim.device.api.io.NoCopyByteArrayOutputStream;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Arrays;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.SocketConnection;
 
 
 /**
  * This class serves as a facade for socket connections used by the various
  * protocols supported by this application.  It handles the details of sending
  * and receiving data in whole lines, with customizable logic to meet protocol
  * specific needs.
  */
 public class Connection {
     private static final byte CR = (byte)0x0D;
     private static final byte LF = (byte)0x0A;
     private static final byte[] CRLF = new byte[] { CR, LF };
     private static String strCRLF = "\r\n";
 
     private SocketConnection socket;
     private String localAddress;
     private GlobalConfig globalConfig;
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
      * @param socket Socket representing the connection
      * @throws IOException Thrown if an I/O error occurs
      */
     protected Connection(SocketConnection socket) throws IOException {
         this.globalConfig = MailSettings.getInstance().getGlobalConfig();
         
         this.socket = socket;
         this.input = socket.openDataInputStream();
         this.output = socket.openDataOutputStream();
         this.localAddress = socket.getLocalAddress();
         this.bytesSent = 0;
         this.bytesReceived = 0;
     }
     
     /**
      * Closes a connection.
      */
     public void close() {
         synchronized(socketLock) {
             try {
                 input.close();
                 output.close();
                 socket.close();
             } catch (Exception exp) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("Error closing connection: " + exp.getMessage()).getBytes(),
                         EventLogger.WARNING);
             } finally {
                 input = null;
                 output = null;
                 socket = null;
             }
         }
         
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
      * Gets the socket used by this connection instance.
      * This method should only be called by <code>NetworkConnector</code>
      * for the purpose of creating new wrapped sockets.
      *
      * @return the connection socket
      */
     SocketConnection getSocket() {
         return socket;
     }
     
     /**
      * Gets the input stream used by this connection instance.
      * This method should only be called by <code>NetworkConnector</code>
      * for the purpose of creating new wrapped sockets.
      *
      * @return the connection socket's input stream
      */
     InputStream getInput() {
         return input;
     }
     
     /**
      * Gets the output stream used by this connection instance.
      * This method should only be called by <code>NetworkConnector</code>
      * for the purpose of creating new wrapped sockets.
      *
      * @return the connection socket's output stream
      */
     OutputStream getOutput() {
         return output;
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
         synchronized(socketLock) {
             if (fakeAvailable == -1) {
                 return input.available();
             } else {
                 return fakeAvailable;
             }
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
                     int len = input.read(readBuffer, 0, Math.min(bytesAvailable, readBuffer.length));
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
                
                // Check for any final data
                if(byteStream.size() > 0) {
                    line = checkForLine(responseTester);
                    if(line != null) {
                        return line;
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
     
                 close();
     
                 throw new IOException("Connection closed");
             }
         }
         // This should never normally happen
        System.err.println("-->Blargh!");
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
         private int lastLength = 0;
         
         public int checkForCompleteResponse(byte[] buf, int len) {
             trimCount = 0;
             int p = StringArrays.indexOf(buf, LF, lastLength);
             
             if(p != -1 && p < len) {
                 // Specific test for responses that use a double LF in
                 // the middle, to separate things that look like separate
                 // responses but really are not.
                 while(p != -1 && p + 1 < len && buf[p + 1] == LF) {
                     if(p + 2 == len) {
                         lastLength = len;
                         return -1;
                     }
                     else {
                         p = StringArrays.indexOf(buf, LF, p + 2);
                     }
                 }
                 
                 if(p > 0 && buf[p - 1] == CR) {
                     trimCount = 2;
                 }
                 else {
                     trimCount = 1;
                 }
                 lastLength = 0;
                 return ++p;
             }
             else {
                 lastLength = len;
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
 }
