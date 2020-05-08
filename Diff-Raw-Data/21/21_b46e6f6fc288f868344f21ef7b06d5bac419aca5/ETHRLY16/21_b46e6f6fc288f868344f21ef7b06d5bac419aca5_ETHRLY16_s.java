 package de.paluch.midi.relay.relay;
 
 import org.apache.log4j.Logger;
 
 import javax.sound.midi.MetaMessage;
 import javax.sound.midi.MidiMessage;
 import javax.sound.midi.Receiver;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
  * @since 08.11.12 19:59
  */
public class ETHRLY16{
 
     public static final int CONNECT_TIMEOUT_MS = 1000;
     public static final int READ_TIMEOUT_MS = 1000;
     public static final int SEND_BUFFER_SIZE = 8192;
 
     private Logger log = Logger.getLogger(getClass());
     private String hostname;
     private int port;
     private OutputStream outputStream;
     private Socket socket;
     private long lastSendTimestamp = -1;
     private long connectionKeepAlive = 30000;
     private long bytesSent = 0;
 
 
     public final static int ALL_ON = 100;
     public final static int ALL_OFF = 110;
 
     public ETHRLY16() {
 
     }
 
 

     private void checkOrInit() {
         if (socket == null) {
             socket = new Socket();
 
         }
 
         if (!socket.isConnected()) {
             try {
 
                 socket.connect(new InetSocketAddress(hostname, port), CONNECT_TIMEOUT_MS);
                 socket.setSoTimeout(READ_TIMEOUT_MS);
                 socket.setTcpNoDelay(true);
                 socket.setSendBufferSize(SEND_BUFFER_SIZE);
                 outputStream = socket.getOutputStream();
 
             } catch (IOException e) {
                 try {
                     socket.close();
                 } catch (IOException e1) {
                 }
                 log.warn(e.getMessage(), e);
                 outputStream = null;
             }
 
         }
     }
 
     public void close() {
 
         if (outputStream != null) {
             try {
                 outputStream.close();
             } catch (IOException e) {
                 log.warn(e.getMessage(), e);
             }
             outputStream = null;
         }
 
         if (socket != null) {
             if (socket.isConnected()) {
                 try {
                     socket.close();
                 } catch (IOException e) {
                     log.warn(e.getMessage(), e);
                 }
             }
             socket = null;
         }
     }
 
     public void keepaliveOrClose() {
         if (socket != null) {
            if (lastSendTimestamp + connectionKeepAlive < System.currentTimeMillis()) {
                 close();
             }
         }
     }
 
     public void on(int port) {
 
         write(ALL_ON + port);
 
     }
 
     private void write(int command) {
         checkOrInit();
         try {
             if (socket != null && socket.isConnected()) {
                 bytesSent++;
                 outputStream.write(command);
                 lastSendTimestamp = System.currentTimeMillis();
             }
         } catch (Exception e) {
             log.warn(e.getMessage());
         }
     }
 
     public void off(int port) {
         write(ALL_OFF + port);
     }
 
 
     public long getBytesSent() {
         return bytesSent;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public void setHostname(String hostname) {
         this.hostname = hostname;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 }
