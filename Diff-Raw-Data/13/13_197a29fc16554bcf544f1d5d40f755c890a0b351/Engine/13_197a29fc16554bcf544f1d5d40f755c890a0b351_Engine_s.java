 /*
  * The MIT License
  *
  * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package me.entityreborn.socbot.core;
 
 import me.entityreborn.socbot.events.EventManager;
 import me.entityreborn.socbot.api.Connection;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import javax.net.SocketFactory;
 import me.entityreborn.socbot.api.events.ConnectedEvent;
 import me.entityreborn.socbot.api.events.ConnectingEvent;
 import me.entityreborn.socbot.api.events.LineSendEvent;
 
 /**
  * Provide low level connection and lifetime management for an IRC session. <p>
  * Some of the /ideas/ behind how this works taken from PircBotX. No actual
  * PircBotX code used.
  *
  * @author Jason Unger <entityreborn@gmail.com>
  */
 public abstract class Engine implements Connection {
 
     private InputThread in;
     private OutputThread out;
     private Socket sock;
     private SocketFactory sockfactory;
     private String nickname;
     private String username = "SocPuppet";
     private String realname = "SocPuppet";
     private boolean isConnected = false;
     private boolean isConnecting = false;
     private static int enginecount = 1;
 
     /**
      * Increment and return a counter for use in naming threads.
      *
      * @return id number
      */
     protected static int getCountAndIncrement() {
         return enginecount++;
     }
 
     public void disconnect() {
         isConnected = false;
 
         try {
             in.interrupt();
             out.interrupt();
         } catch (Exception e) {
             handleException(e);
         }
 
        if (!sock.isClosed()) {
             try {
                 sock.shutdownInput();
                 sock.shutdownOutput();
                 sock.close();
             } catch (IOException e) {
                 handleException(e);
             }
         }
     }
 
     public void connect(String srvr) throws IOException {
         connect(srvr, 6667, "", null);
     }
 
     public void connect(String server, int port) throws IOException {
         connect(server, port, "", null);
     }
 
     public void connect(String server, int port, String password) throws IOException {
         connect(server, port, password, sockfactory);
     }
 
     public void connect(String server, int port, String password, SocketFactory factory) throws UnknownHostException, IOException {
         sockfactory = factory;
 
         isConnecting = true;
         sock = null;
 
         EventManager.callEvent(new ConnectingEvent(server, port), this);
 
         if (factory != null) {
             for (InetAddress addr : InetAddress.getAllByName(server)) {
                 sock = sockfactory.createSocket(server, port, addr, 0);
                 break;
             }
         } else {
             sock = new Socket(server, port, null, 0);
         }
 
         if (sock == null) {
             throw new RuntimeException("Could not connect to a server.");
         }
 
         in = new InputThread(sock.getInputStream(), this);
         out = new OutputThread(sock.getOutputStream(), this);
 
         int count = getCountAndIncrement();
         in.setName("input-" + count);
         out.setName("output-" + count);
 
         out.start();
 
         EventManager.callEvent(new ConnectedEvent(server, port), this);
 
         if (password != null && !password.trim().equals("")) {
             sendLine("PASS " + password.trim());
         }
 
         sendLine("NICK " + getNickname());
         sendLine("USER " + getUsername() + " 8 * :" + getRealname());
 
         in.start();
 
         isConnecting = false;
         isConnected = true;
         
     }
 
     /**
      * Exception callback for when something bad happens in the core.
      * <p>
      * This doesn't pertain to error numerics or the ERROR IRC message
      * sent from the server.
      * 
      * @param t the exception that was thrown.
      */
     public abstract void handleException(Throwable t);
 
     /**
      * Handle any input the bot receives.
      * <p>
      * This method will fire off appropriate events,
      * depending on the type of line.
      * <p>
      * Numeric lines will be sent to handleNumeric.
      * 
      * @param line the line the bot received.
      */
     public abstract void handleLine(String line);
 
 
     public void sendLine(String line) {
         EventManager.callEvent(new LineSendEvent(line), this);
         out.send(line);
     }
 
     public void sendLineNow(String line) {
         EventManager.callEvent(new LineSendEvent(line, true), this);
         out.sendNow(line);
     }
 
 
     public void sendPing() {
         sendLine("PING " + (System.currentTimeMillis() / 1000));
     }
 
     public boolean isConnected() {
         return isConnected;
     }
 
     public boolean isConnecting() {
         return isConnecting;
     }
 
     public String getNickname() {
         return nickname;
     }
 
 
     public void setNickname(String nickname) {
         if (isConnected() || isConnecting()) {
             sendLine("NICK " + nickname);
         }
         
         this.nickname = nickname;
     }
 
     public String getUsername() {
         return username;
     }
 
 
     public void setUsername(String username) {
         if (!isConnected()) {
             this.username = username;
         }
     }
 
     public String getRealname() {
         return realname;
     }
 
     public void setRealname(String realname) {
         if (!isConnected()) {
             this.realname = realname;
         }
     }
 }
