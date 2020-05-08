 /*
  * Copyright 2011 SUSE Linux Products GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package com.suse.logkeeper.plugins;
 
 import de.suse.logkeeper.plugins.LogKeeperBackend;
 import de.suse.logkeeper.plugins.LogKeeperBackendException;
 import de.suse.logkeeper.service.entities.LogEntry;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author bo
  */
 public class Syslog implements LogKeeperBackend {
     public static final int DEFAULT_SYSLOG_PORT = 514;
     public static final String TCP_TYPE = "tcp";
     public static final String UDP_TYPE = "udp";
     public static final String LOCAL_TYPE = "local";
 
     private static final String SYSLOG_COMMAND = "/bin/logger";
 
     private Socket tcpSocket;
     private DatagramSocket udpSocket;
     private SocketAddress address;
     private String proto;
 
     private boolean fieldPreciseTime;
     private boolean fieldResolveIP;
     private boolean fieldExtmap;
     private String fieldSignature = "LK";
 
 
     public void setup(String definition, Properties setup) throws LogKeeperBackendException {
         this.proto = setup.getProperty("plugin." + definition + ".proto", Syslog.LOCAL_TYPE);
 
         if (this.proto.equals(Syslog.LOCAL_TYPE)) {
             File syslog = new File(Syslog.SYSLOG_COMMAND);
             if (!syslog.canExecute() || !syslog.exists()) {
                 throw new LogKeeperBackendException(String.format("Plugin %s configured as %s, but cannot call %s.",
                                                                   definition, Syslog.LOCAL_TYPE, SYSLOG_COMMAND));
             }
         }
 
         // Setup fields
         this.fieldPreciseTime = setup.getProperty("plugin." + definition + ".fields.precisetime", "on").toLowerCase().equals("on");
         this.fieldResolveIP = setup.getProperty("plugin." + definition + ".fields.resolveip", "on").toLowerCase().equals("on");
         this.fieldExtmap = setup.getProperty("plugin." + definition + ".fields.extmap", "on").toLowerCase().equals("on");
         this.fieldSignature = setup.getProperty("plugin." + definition + ".fields.signature", "LK").trim();
 
         try {
             String host = setup.getProperty("plugin." + definition + ".host", "");
             this.address = new InetSocketAddress(!host.equals("") && !host.toLowerCase().equals("localhost")
                                                 ? InetAddress.getByName(host) : InetAddress.getLocalHost(), Syslog.DEFAULT_SYSLOG_PORT);
         } catch (UnknownHostException ex) {
             Logger.getLogger(Syslog.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
 
     /**
      * Connect to the socket.
      */
     private void connect() {
         this.close();
         try {
             if (this.proto.equals(Syslog.UDP_TYPE)) {
                 this.udpSocket = new DatagramSocket();
                 this.udpSocket.connect(this.address);
                 this.udpSocket.setBroadcast(true);
             } else if (this.proto.equals(Syslog.TCP_TYPE)) {
                 this.tcpSocket = new Socket();
                 this.tcpSocket.setTcpNoDelay(false);
                 this.tcpSocket.connect(this.address);
             } // else local output
         } catch (IOException ex) {
             Logger.getLogger(Syslog.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
 
     /**
      * Close the socket.
      */
     private void close() {
         if (this.tcpSocket != null && this.tcpSocket.isConnected()) {
             try {
                 this.tcpSocket.close();
                 this.tcpSocket = null;
             } catch (IOException ex) {
                 Logger.getLogger(Syslog.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
 
     /**
      * Send a log message to the syslog.
     * 
      * @param entry
      * @throws LogKeeperBackendException
      */
     public synchronized void log(LogEntry entry) throws LogKeeperBackendException {
         try {
             if (this.proto.equals(Syslog.TCP_TYPE)) {
                 String[] lines = this.wrapToLines(this.formatMessage(entry), 0xff);
                 for (int i = 0; i < lines.length; i++) {
                     try {
                         this.connect();
                         PrintStream out = new PrintStream(this.tcpSocket.getOutputStream());
                         out.println(lines[i]);
                         out.close();
                     } finally {
                         this.close();
                     }
                 }
             } else if (this.proto.equals(Syslog.UDP_TYPE)) {
                 String[] lines = this.wrapToLines(this.formatMessage(entry), 0x400);
                 try {
                     this.connect();
                     for (int i = 0; i < lines.length; i++) {
                         this.udpSocket.send(new DatagramPacket(lines[i].getBytes(), lines[i].getBytes().length,
                                                                ((InetSocketAddress) this.address).getAddress(),
                                                                ((InetSocketAddress) this.address).getPort()));
                     }
                 } finally {
                     this.close();
                 }
             } else {
                 try {
                     int exitValue = this.callSyslog(entry);
                     if (exitValue != 0) {
                         throw new LogKeeperBackendException(String.format("Calling a local syslog failed. Exit value: %s", exitValue));
                     }
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Syslog.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         } catch (IOException ex) {
             Logger.getLogger(Syslog.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
 
     /**
      * Call a system /bin/logger and pipe a data into it.
      *
      * @param entry
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     private int callSyslog(LogEntry entry) throws IOException, InterruptedException {
         Process process = Runtime.getRuntime().exec(new String[]{Syslog.SYSLOG_COMMAND});
         PrintStream out = new PrintStream(process.getOutputStream());
         String[] lines = this.wrapToLines(this.formatMessage(entry), 0xff);
         for (int i = 0; i < lines.length; i++) {
             out.println(lines[i]);
             out.flush();
         }
         out.close();
         process.waitFor();
 
         return process.exitValue();
     }
 
 
     /**
      * Format message for the logger.
     * 
      * @param entry
      * @return
      */
     private String formatMessage(LogEntry entry) {
         StringBuilder message = new StringBuilder().append(" ");
         message.append(entry.getUserName()).append(" ");
         if (this.fieldPreciseTime) {
             message.append("at ")
                     .append(entry.getISO8601Timestamp())
                     .append(" ");
         }
        
         if (this.fieldResolveIP) {
             message.append(entry.getNode().getCanonicalHostName())
                     .append(" (")
                     .append(entry.getNode().getHostAddress())
                     .append(") ");
         }
 
         message.append(entry.getMessage());
        
         if (this.fieldExtmap) {
             message.append(" ").append(this.renderExtMapForSyslog(entry));
         }
 
         return message.toString();
     }
 
 
     /**
      * Tied up each extmap in a syslog to a particular message.
     * 
      * @param entry
      * @param id
      * @return
      */
     private String renderExtMapForSyslog(LogEntry entry) {
         String[] lines = entry.renderExtMap().split("\n");
         StringBuilder out = new StringBuilder().append("(");
         for (int i = 0; i < lines.length; i++) {
             out.append("'").append(lines[i].replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\'")).append("'");
             if (i < (lines.length - 1)) {
                 out.append(", ");
             }
         }
 
         return out.append(")").toString();
     }
 
 
     /**
      * Wrap the stuff to the lines, if exceeds the limits or is a multi-line.
      *
      * @param message
      * @return
      */
     private String[] wrapToLines(String message, int chars) {
         message = (message == null ? "" : message).trim();
         String prefix = this.fieldSignature + " " + new Date().getTime() + " ";
         if ((chars - prefix.length()) > prefix.length() + 0xf) {
             chars -= prefix.length();
         }
 
         if (message.length() <= chars) {
             return new String[] {this.fieldSignature + " " + message};
         }
 
         String[] words = message.split(" ");
 
         StringBuilder wrapped = new StringBuilder();
         StringBuilder line = new StringBuilder();
         for (int i = 0; i < words.length; i++) {
             String word = words[i];
             if (word.equals("")) {
                 continue;
             }
 
             if ((line.length() + word.length() + 1) < chars) {
                 line.append(word).append(" ");
             } else {
                 wrapped.append(prefix).append(line.toString()).append(" ").append(word).append("\n");
                 line = new StringBuilder();
             }
         }
 
         if (line.length() > 0) {
             wrapped.append(prefix).append(line.toString());
         }
 
         return wrapped.toString().split("\n");
     }
 
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args)
             throws UnknownHostException,
                    LogKeeperBackendException {
         Map<String, String> extmap = new HashMap<String, String>();
         extmap.put("custom.foo", "bar");
         extmap.put("custom.baz", "spam");
         extmap.put("custom.name", "Fred");
         extmap.put("custom.longstuff", "Something very long here that does not really makes a sense at all, but still present just for the testing purposes.");
 
         Syslog syslog = new Syslog();
         syslog.setup("syslog", new Properties());
        syslog.log(new LogEntry(1L, "audit", 1, "Hello, world! 1", System.getProperty("user.name"), InetAddress.getLocalHost(), new Date(), extmap));
        syslog.log(new LogEntry(1L, "audit", 1, "Hello, world! 2", System.getProperty("user.name"), InetAddress.getLocalHost(), new Date(), extmap));
        syslog.log(new LogEntry(1L, "audit", 1, "Hello, world! 3", System.getProperty("user.name"), InetAddress.getLocalHost(), new Date(), extmap));
     }
 }
