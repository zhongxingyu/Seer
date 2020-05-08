 package gatherer;
 
 import org.apache.commons.net.telnet.TelnetClient;
 import play.Logger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * synchronous write and read for initial commands like login
  */
 class TelnetUtil extends Thread {
 
     private TelnetClient telnet;
 
     private BufferedReader in;
 
     private PrintWriter out;
 
     private String server;
 
     private int port;
 
     private boolean shouldRead;
 
     private List<TelnetOutputListener> listener = new ArrayList<TelnetOutputListener>();
 
     public TelnetUtil() {
         telnet = new TelnetClient();
     }
 
     /**
      * factory method
      *
      * @param server
      * @param port
      * @return
      */
     public static TelnetUtil connect(String server, int port) {
         TelnetUtil util = new TelnetUtil();
         util.setServer(server);
         util.setPort(port);
         Logger.debug("create telnet connection with %s", util);
         util.connect();
         return util;
     }
 
     public synchronized String readUntil(String pattern) {
         Logger.debug("read until: %s", pattern);
         StringBuffer sb = new StringBuffer();
         char lastChar = pattern.charAt(pattern.length() - 1);
         char ch = readChar();
         while (true) {
             if (ch == 65535) { // connection closed
                 throw new IllegalArgumentException(sb.toString());
             }
             sb.append(ch);
             if (ch == lastChar && sb.toString().endsWith(pattern)) {
                 Logger.debug("read: %s", sb);
                 return sb.toString();
             }
             ch = readChar();
         }
     }
 
     private char readChar() {
         try {
             return (char) in.read();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public synchronized void send(String value) {
         Logger.debug("write: %s", value);
         out.println(value);
         out.flush();
     }
 
     public String getServer() {
         return server;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setServer(String server) {
         this.server = server;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     /**
      * open telnet connection and assign streams
      */
     public synchronized void connect() {
         Logger.debug("check whether already connected with %s", this);
         if (telnet.isConnected()) {
             throw new IllegalStateException("already connected");
         }
 
         try {
             Logger.info("try to connect with %s", this);
             telnet.connect(server, port);
             Logger.debug("connected with %s", this);
         } catch (Exception e) {
             throw new RuntimeException("telnet connection failed with " + this, e);
         }
 
         in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
         out = new PrintWriter(telnet.getOutputStream());
     }
 
     public synchronized boolean isConnected() {
         return telnet.isConnected();
     }
 
     public synchronized void disconnect() throws IOException {
         telnet.disconnect();
     }
 
     @Override
     public String toString() {
         return server + ":" + port;
     }
 
     public void addOutputListener(TelnetOutputListener handler) {
         listener.add(handler);
     }
 
     public synchronized void setShouldRead(boolean shouldRead) {
         this.shouldRead = shouldRead;
     }
 
     public synchronized boolean isShouldRead() {
         return shouldRead;
     }
 
     @Override
     public void run() {
         setShouldRead(true);
         while (isShouldRead()) {
             String line;
             try {
                 line = in.readLine();
                Logger.debug("read line: %s", line);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
 
             for (TelnetOutputListener l : listener) {
                 if (l.notify(line)) {
                     try {
                         while (l.notify(in.readLine())) {
                             // send next lines to the interested listener
                         }
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                     break;
                 }
             }
         }
         Logger.debug("telnet reading stopped");
     }
 
 }
