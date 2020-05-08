 /*
  * Copyright (c) 2012, Keeley Hoek
  * All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  * 
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.github.escortkeel.circle;
 
 import com.github.escortkeel.circle.event.IRCChannelJoinEvent;
 import com.github.escortkeel.circle.event.IRCChannelPartEvent;
 import com.github.escortkeel.circle.event.IRCConnectEvent;
 import com.github.escortkeel.circle.event.IRCEvent;
 import com.github.escortkeel.circle.event.IRCMotdEvent;
 import com.github.escortkeel.circle.event.IRCPrivateMessageEvent;
 import com.github.escortkeel.circle.event.IRCRawMessageEvent;
 import com.github.escortkeel.circle.exception.IRCNameException;
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.Socket;
 import java.nio.charset.Charset;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 import java.util.concurrent.Semaphore;
 
 /**
  * This class implements an IRC client connection to an IRC server.
  *
  * @author Keeley Hoek (escortkeel@live.com)
  */
 public class IRCClient implements Closeable {
 
     /**
      * Creates a new
      * <code>IRCClient</code> and synchronously connects it to port 6667 (the
      * default IRC port) on the named host, using the specified nickname and
      * <code>IRCAdapter</code>. A call to this method <b>will</b> block until
      * either a connection is established or an error occurred.
      * <p>
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @param nickname the nickname.
     * @param adapter the adapter to be associated with * * * * * *      * this <code>IRCClient</code>.
      *
      * @exception IRCNameException if the specified nickname is already in use.
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public static IRCClient create(String address, String nickname, IRCAdapter adapter) throws IRCNameException, IOException {
         return create(address, 6667, nickname, adapter);
     }
 
     /**
      * Creates a new
      * <code>IRCClient</code> and synchronously connects it to the specified
      * port number on the named host, using the specified nickname and
      * <code>IRCAdapter</code>. A call to this method <b>will</b> block until
      * either a connection is established or an error occurred.
      * <p>
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @param port the port number.
      * @param nickname the nickname.
      * @param adapter the adapter to be associated with this
      * <code>IRCClient</code>.
      *
      * @exception IRCNameException if the specified nickname is already in use.
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public static IRCClient create(String address, int port, String nickname, IRCAdapter adapter) throws IRCNameException, IOException {
         return create(address, port, nickname, nickname, nickname, false, adapter);
     }
 
     /**
      * Creates a new
      * <code>IRCClient</code> and synchronously connects it to the specified
      * port number on the named host, using the specified nickname, username,
      * real name, invisibility flag and
      * <code>IRCAdapter</code>. A call to this method <b>will</b> block until
      * either a connection is established or an error occurred.
      * <p>
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @param port the port number.
      * @param nickname the nickname.
      * @param username the username.
      * @param realname the real name.
      * @param invisible whether the client should be invisible to other clients.
      * @param adapter the adapter to be associated with this
      * <code>IRCClient</code>.
      *
      * @exception IRCNameException if the specified nickname is already in use.
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public static IRCClient create(String address, int port, String nickname, String username, String realname, boolean invisible, IRCAdapter adapter) throws IRCNameException, IOException {
         IRCClient c = new IRCClient(address, port, nickname, username, realname, invisible, adapter);
         c.worker.start();
         c.fireEvent(new IRCConnectEvent(c));
         return c;
     }
     private final Socket socket;
     private final BufferedReader in;
     private final PrintWriter out;
     private final String nickname;
     private final String password;
     private final String username;
     private final String realname;
     private final boolean invisible;
     private final IRCAdapter adapter;
     private final StringBuilder motd = new StringBuilder();
     private final ArrayList<String> channels = new ArrayList<>();
     private final Thread worker;
     private final Semaphore running = new Semaphore(1);
     private Status status = Status.WAITING;
 
     private IRCClient(String address, int port, String nickname, String username, String realname, boolean invisible, IRCAdapter adapter) throws IRCNameException, IOException {
         Objects.requireNonNull(nickname);
         Objects.requireNonNull(username);
         Objects.requireNonNull(realname);
         Objects.requireNonNull(adapter);
 
         if (nickname.contains(" ")) {
             throw new IllegalArgumentException("Nickname must not contain spaces");
         }
 
         if (username.contains(" ")) {
             throw new IllegalArgumentException("Username must not contain spaces");
         }
 
         this.socket = new Socket(address, port);
         this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
         this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
 
         this.nickname = nickname;
         this.password = Long.toString(new SecureRandom().nextLong(), 36);
         this.username = username;
         this.realname = realname;
         this.invisible = invisible;
 
         this.adapter = adapter;
 
         this.running.acquireUninterruptibly();
 
         send("PASS " + password);
         send("USER " + username + " " + (invisible ? "8" : "0") + " * :" + realname);
         send("NICK " + nickname);
 
         while (parseLine()) {
             if (status == Status.SUCCESS) {
                 break;
             } else if (status == Status.FAIL) {
                 throw new IRCNameException("Nickname taken");
             } else if (status == Status.ERROR) {
                 throw new IOException("IRC error");
             }
         }
 
         worker = new Thread("cIRCle Thread") {
             @Override
             @SuppressWarnings("empty-statement")
             public void run() {
                 while (parseLine());
             }
         };
 
         worker.setDaemon(true);
     }
 
     /**
      * Sends a private message to the specified target.
      *
      * @param target the target of the message.
      * @param message the message.
      */
     public void privmsg(String target, String message) {
         if (target.contains(" ")) {
             throw new IllegalArgumentException("Target must not contain spaces");
         }
 
         send("PRIVMSG " + target + " :" + message);
     }
 
     /**
      * Attempts to join the specified channel.
      *
      * @param channel the channel to join.
      */
     public void join(String channel) {
         send("JOIN " + channel);
     }
 
     /**
      * Attempts to leave the specified channel. If the client is not a member of
      * the specified channel, invoking this method has no effect.
      *
      * @param channel the channel to leave.
      */
     public void part(String channel) {
         send("PART " + channel);
     }
 
     /**
      * Closes this
      * <code>IRCClient</code> gracefully. If the connection is already closed
      * then invoking this method has no effect.
      */
     public void quit() {
         send("QUIT");
         status = Status.QUIT;
     }
 
     /**
      * Closes this
      * <code>IRCClient</code> gracefully with the specified reason. If the
      * connection is already closed then invoking this method has no effect.
      *
      * @param reason the reason for closing the connection
      */
     public void quit(String reason) {
         send("QUIT :" + reason);
         status = Status.QUIT;
     }
 
     /**
      * Returns the address of the remote host which this
      * <code>IRCClient</code> instance is connected to.
      *
      * @return the address of the remote host.
      */
     public String getRemoteAddress() {
         return socket.getInetAddress().getHostName();
     }
 
     /**
      * Returns the port on the remote host which this
      * <code>IRCClient</code> instance is connected to.
      *
      * @return the port on the remote host.
      */
     public int getRemotePort() {
         return socket.getPort();
     }
 
     /**
      * Returns the nickname associated with this
      * <code>IRCClient</code> instance.
      *
      * @return the nickname associated with this <code>IRCClient</code>
      * instance.
      */
     public String getNickname() {
         return nickname;
     }
 
     /**
      * Returns the username associated with this
      * <code>IRCClient</code> instance.
      *
      * @return the username associated with this <code>IRCClient</code>
      * instance.
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Returns the "real name" associated with this
      * <code>IRCClient</code> instance.
      *
      * @return the "real name" associated with this <code>IRCClient</code>
      * instance.
      */
     public String getRealname() {
         return realname;
     }
 
     /**
      * Returns the channels which this
      * <code>IRCClient</code> is a member of.
      *
      * This list is free to be manipulated or changed by the caller.
      *
      * @return a list of the channels which this <code>IRCClient</code> is a
      * member of
      */
     public List<String> getChannels() {
         return (List<String>) channels.clone();
     }
 
     /**
      * Returns whether this
      * <code>IRCClient</code> instance is invisible.
      *
      * @return whether this <code>IRCClient</code> instance is invisible.
      */
     public boolean isInvisible() {
         return invisible;
     }
 
     /**
      * Returns the closed state of the socket.
      *
      * @return true if the socket has been closed
      * @see #close
      */
     public boolean isClosed() {
         return socket.isClosed();
     }
 
     /**
      * Waits until this
      * <code>IRCClient</code> is closed.
      *
      * @throws InterruptedException if the waiting thread is interrupted.
      */
     public void waitFor() throws InterruptedException {
         running.acquire();
     }
 
     /**
      * Closes the socket underlying this
      * <code>IRCClient</code> abruptly. If the connection is already closed then
      * invoking this method has no effect.
      */
     @Override
     public void close() {
         try {
             socket.close();
         } catch (IOException ex) {
         }
 
         running.release();
     }
 
     private boolean parseLine() {
         if (isClosed()) {
             return false;
         }
 
         try {
             String s = in.readLine();
             if (s != null) {
                 handleMessage(s);
                 return true;
             }
         } catch (IOException ex) {
             close();
         }
 
         return false;
     }
 
     private void handleMessage(String raw) {
         String source = null;
         int split = raw.indexOf(' ');
         if (raw.startsWith(":")) {
             source = raw.substring(1, split);
             raw = raw.substring(split + 1);
             split = raw.indexOf(' ');
         }
 
         String keyword = raw.substring(0, split);
         String args = raw.substring(split + 1);
 
         int reply = -1;
         try {
             if (keyword.length() == 3) {
                 reply = Integer.parseInt(keyword);
             }
         } catch (NumberFormatException nfe) {
         }
 
         if (reply == -1) {
             switch (keyword) {
                 case "PING": {
                     send("PONG " + args);
                     break;
                 }
                 case "JOIN": {
                     channels.add(args);
                     fireEvent(new IRCChannelJoinEvent(this, args));
                     break;
                 }
                 case "PART": {
                     channels.remove(args);
                     fireEvent(new IRCChannelPartEvent(this, args, false));
                     break;
                 }
                 case "KICK": {
                    channels.remove(args);
                    fireEvent(new IRCChannelPartEvent(this, args, true));
                     break;
                 }
                 case "PRIVMSG": {
                     int space = args.indexOf(' ');
                     fireEvent(new IRCPrivateMessageEvent(this, source, args.substring(space + 1), args.substring(0, space)));
                     break;
                 }
                 case "ERROR": {
                     if (status == Status.QUIT) {
                         close();
                     } else if (status == Status.WAITING) {
                         status = Status.ERROR;
                     }
                     break;
                 }
                 default: {
                     break;
                 }
             }
         } else {
             switch (IRCReply.toEnum(reply)) {
                 case WELCOME: {
                     status = Status.SUCCESS;
                     break;
                 }
                 case NICKNAMEINUSE: {
                     if (status != Status.SUCCESS) {
                         status = Status.FAIL;
                     }
                     break;
                 }
                 case MOTDSTART: {
                     int motdStart = args.indexOf(':');
                     String sub = args.substring(motdStart);
                     if (motdStart != -1 && sub.length() != 1 && sub.length() > 3) {
                         motdStart = sub.indexOf(' ');
                         sub = sub.substring(motdStart + 1, sub.length() - 3);
 
                         if (motdStart != -1 && sub.length() != 1) {
                             motd.append(sub).append("\n");
                         }
                     }
                     break;
                 }
                 case MOTD: {
                     int motdStart = args.indexOf(':');
                     String sub = args.substring(motdStart);
                     if (motdStart != -1 && sub.length() != 1) {
                         motdStart = sub.indexOf(' ');
                         sub = sub.substring(motdStart + 1);
 
                         if (motdStart != -1 && sub.length() != 1) {
                             motd.append(sub).append("\n");
                         }
                     }
                     break;
                 }
                 case ENDOFMOTD: {
                     int motdStart = args.indexOf(':');
                     if (motdStart != -1 && args.substring(motdStart).length() != 1) {
                         motd.append(args.substring(motdStart + 1)).append("\n");
                     }
                     fireEvent(new IRCMotdEvent(this, motd.toString()));
                     break;
                 }
                 default: {
                     break;
                 }
             }
         }
 
         fireEvent(new IRCRawMessageEvent(this, source, raw));
     }
 
     private void fireEvent(IRCEvent e) {
         for (Method m : IRCAdapter.class.getMethods()) {
             if (m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(e.getClass())) {
                 try {
                     m.invoke(adapter, e);
                 } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                 }
             }
         }
     }
 
     private void send(String string) {
         out.print(string + "\r\n");
         out.flush();
     }
 
     private static enum Status {
 
         WAITING,
         SUCCESS,
         FAIL,
         ERROR,
         QUIT
     }
 }
