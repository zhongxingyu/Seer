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
 
 import com.github.escortkeel.circle.event.IRCConnectionEstablishedEvent;
 import com.github.escortkeel.circle.event.IRCWelcomeEvent;
 import com.github.escortkeel.circle.event.IRCConnectionClosedEvent;
 import com.github.escortkeel.circle.event.IRCErrorEvent;
 import com.github.escortkeel.circle.event.IRCChannelJoinEvent;
 import com.github.escortkeel.circle.event.IRCChannelPartEvent;
 import com.github.escortkeel.circle.event.IRCEvent;
 import com.github.escortkeel.circle.event.IRCMotdEvent;
 import com.github.escortkeel.circle.event.IRCNicknameChangeEvent;
 import com.github.escortkeel.circle.event.IRCNicknameInUseEvent;
 import com.github.escortkeel.circle.event.IRCPrivateMessageEvent;
 import com.github.escortkeel.circle.event.IRCRawMessageEvent;
 import java.io.Closeable;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.AsynchronousChannelGroup;
 import java.nio.channels.AsynchronousSocketChannel;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.CompletionHandler;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Objects;
 import java.util.Queue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This class implements an IRC client connection to an IRC server.
  *
  * @author Keeley Hoek (escortkeel@live.com)
  */
 public class IRCClient implements Closeable {
 
     private static final SecureRandom random = new SecureRandom();
     private final AsynchronousChannelGroup group;
     private final AsynchronousSocketChannel socket;
     private final String nickname;
     private final String password;
     private final String username;
     private final String realname;
     private final boolean invisible;
     private final IRCAdapter adapter;
     private final ByteBuffer buff = ByteBuffer.allocateDirect(512);
     private final Queue<ByteBuffer> outQueue = new LinkedBlockingQueue<>();
     private final ArrayList<String> channels = new ArrayList<>();
     private final StringBuilder motd = new StringBuilder();
     private String part = "";
     private volatile boolean welcomed = false;
     private volatile boolean connected = false;
     private volatile boolean asynchWriting = false;
     private final ArrayList<String> welcomeWaiters = new ArrayList<>();
 
     /**
      * Constructs a new
      * <code>IRCClient</code> with the specified nickname and
      * <code>IRCAdapter</code>. A call to
      * <code>connect()</code> must be made in order to resolve and connect this
      * <code>IRCClient</code>.
      *
      * @param nickname the nickname.
      * @param adapter the adapter to be associated with this
      * <code>IRCClient</code>.
      *
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public IRCClient(String nickname, IRCAdapter adapter) throws IOException {
         this(nickname, nickname, nickname, false, adapter);
     }
 
     /**
      * Constructs a new
      * <code>IRCClient</code> with the specified nickname, username, real name,
      * invisibility flag and
      * <code>IRCAdapter</code>. A call to
      * <code>connect()</code> must be made in order to resolve and connect this
      * <code>IRCClient</code>.
      *
      * @param nickname the nickname.
      * @param username the username.
      * @param realname the real name.
      * @param invisible whether the client should be invisible to other clients.
      * @param adapter the adapter to be associated with this
      * <code>IRCClient</code>.
      *
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public IRCClient(String nickname, String username, String realname, boolean invisible, IRCAdapter adapter) throws IOException {
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
 
         if (nickname.length() > 16) {
             throw new IllegalArgumentException("Nickname must be no more than 16 characters");
         }
 
         this.group = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(1));
         this.socket = AsynchronousSocketChannel.open(group);
 
         this.nickname = nickname;
         this.password = Long.toString(random.nextLong(), 36);
         this.username = username;
         this.realname = realname;
         this.invisible = invisible;
 
         this.adapter = adapter;
     }
 
     /**
      * Connects this
      * <code>IRCClient</code> to port 6667 (the default IRC port) of the
      * specified host.
      *
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @throws IOException
      */
     public void connect(String address) throws IOException {
         connect(address, 6667);
     }
 
     /**
      * Connects this
      * <code>IRCClient</code> to the specified port of the specified host.
      *
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @param port the port number.
      * @throws IOException if an I/O error occurs
      * @throws ConnectionPendingException if a connection attempt is in progress
      *
      */
     public void connect(String address, int port) throws IOException {
         queueWrite("PASS " + password);
         queueWrite("USER " + username + " " + (invisible ? "8" : "0") + " * :" + realname);
         queueWrite("NICK " + nickname);
 
         final IRCClient me = this;
         socket.connect(new InetSocketAddress(address, port), this, new CompletionHandler<Void, IRCClient>() {
             @Override
             public void completed(Void result, IRCClient attachment) {
                 synchronized (outQueue) {
                     connected = true;
 
                     fire(new IRCConnectionEstablishedEvent(me));
                 }
 
                 readLoop();
                 writeLoop();
             }
 
             @Override
             public void failed(Throwable exc, IRCClient attachment) {
                 fire(new IRCConnectionClosedEvent(me));
             }
         });
     }
 
     /**
      * Attempts to join the specified channel.
      *
      * @param channel the channel to join.
      */
     public void join(String channel) {
         sendMessage("JOIN " + channel);
     }
 
     /**
      * Attempts to leave the specified channel.
      *
      * @param channel the channel to leave.
      */
     public void part(String channel) {
         sendMessage("PART " + channel);
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
 
         sendMessage("PRIVMSG " + target + " :" + message);
     }
 
     /**
      * Attempts to change the nickname of this
      * <code>IRCClient</code>.
      *
      * @param nickname.
      *
      * @throws IllegalArgumentException is the nickname is longer than 16
      * characters
      */
     public void nick(String nickname) {
         if (nickname.length() > 16) {
             throw new IllegalArgumentException("Nickname must be no more than 16 characters");
         }
 
         sendMessage("NICK " + nickname);
     }
 
     /**
      * Closes this
      * <code>IRCClient</code> gracefully. If the connection is already closed
      * then invoking this method has no effect.
      */
     public void quit() {
         sendMessage("QUIT");
     }
 
     /**
      * Closes this
      * <code>IRCClient</code> gracefully with the specified reason. If the
      * connection is already closed then invoking this method has no effect.
      *
      * @param reason the reason for closing the connection
      */
     public void quit(String reason) {
         sendMessage("QUIT :" + reason);
     }
 
     /**
      * Returns the address of the remote host which this
      * <code>IRCClient</code> instance is connected to.
      *
      * @return the address of the remote host.
      * @throws ClosedChannelException if the underlying channel has already been
      * closed
      */
     public String getRemoteAddress() throws IOException, ClosedChannelException {
         if (!connected) {
             throw new IllegalStateException("Client not yet connected");
         }
 
         return ((InetSocketAddress) socket.getRemoteAddress()).getHostName();
     }
 
     /**
      * Returns the port on the port number which this
      * <code>IRCClient</code> instance is connected via.
      *
      * @return the port on the remote host, or -1.
      * @throws ClosedChannelException if the underlying channel has already been
      * closed
      */
     public int getRemotePort() throws IOException, ClosedChannelException {
         if (!connected) {
             throw new IllegalStateException("Client not yet connected");
         }
 
         return ((InetSocketAddress) socket.getRemoteAddress()).getPort();
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
         return !socket.isOpen();
     }
 
     /**
      * Waits until this
      * <code>IRCClient</code> is closed.
      *
      * @throws InterruptedException if the waiting thread is interrupted.
      */
     public void waitFor() throws InterruptedException {
         group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
     }
 
     /**
      * Waits until this
      * <code>IRCClient</code> is closed, or until the specified number of
      * milliseconds elapse.
      *
      * @param millis the maximum number of milliseconds to wait for
      *
      * @throws InterruptedException if the waiting thread is interrupted.
      */
     public void waitFor(int millis) throws InterruptedException {
         group.awaitTermination(millis, TimeUnit.MILLISECONDS);
     }
 
     /**
      * Closes the socket underlying this
      * <code>IRCClient</code> abruptly. If the connection is already closed then
      * invoking this method has no effect.
      *
      * @throws IOException if an I/O error occurs
      */
     @Override
     public void close() throws IOException {
         group.shutdownNow();
 
         fire(new IRCConnectionClosedEvent(this));
     }
 
     private void handleMessage(String raw) {
         String source = null;
         int split = raw.indexOf(' ');
         if (raw.startsWith(":")) {
             source = raw.substring(1, split);
             raw = raw.substring(split + 1);
             split = raw.indexOf(' ');
         }
 
         fire(new IRCRawMessageEvent(this, source, raw));
 
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
             if (source != null) {
                 split = source.indexOf('!');
 
                 if (split != -1) {
                    String nick = source.substring(0, split);

                    if (!nick.equals(nickname)) {
                        return;
                    }
                 }
             }
 
             switch (keyword) {
                 case "PING": {
                     queueWrite("PONG " + args);
                     break;
                 }
                 case "JOIN": {
                     channels.add(args);
                     fire(new IRCChannelJoinEvent(this, args));
                     break;
                 }
                 case "PART": {
                     channels.remove(args);
                     fire(new IRCChannelPartEvent(this, args, false));
                     break;
                 }
                 case "KICK": {
                     int space = args.indexOf(' ');
                     if (args.substring(space + 1).equals(getNickname())) {
                         channels.remove(args);
                         fire(new IRCChannelPartEvent(this, args.substring(0, space), true));
                     }
                     break;
                 }
                 case "PRIVMSG": {
                     fire(new IRCPrivateMessageEvent(this, source, args));
                     break;
                 }
                 case "QUIT": {
                    try {
                        close();
                    } catch (IOException ex) {
                     }
                     break;
                 }
                 case "ERROR": {
                    fire(new IRCErrorEvent(this, source, args));
                     break;
                 }
                 default: {
                     break;
                 }
             }
         } else {
             switch (IRCReply.toEnum(reply)) {
                 case WELCOME: {
                     fire(new IRCWelcomeEvent(this));
                     fire(new IRCNicknameChangeEvent(this, nickname));
 
                     wasWelcomed();
                     break;
                 }
                 case NICKNAMEINUSE: {
                     fire(new IRCNicknameInUseEvent(this, args.substring(args.indexOf(" ")).substring(args.indexOf(" ") + 1)));
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
                     fire(new IRCMotdEvent(this, motd.toString()));
                     break;
                 }
                 default: {
                     break;
                 }
             }
         }
     }
     private static final HashMap<Class, Method> eventMethods = new HashMap<>();
 
     static {
         for (Method m : IRCAdapter.class.getMethods()) {
             if (m.getParameterTypes().length == 1 && IRCEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                 eventMethods.put(m.getParameterTypes()[0], m);
             }
         }
     }
 
     private void fire(IRCEvent e) {
         try {
             Method m = eventMethods.get(e.getClass());
             if (m == null) {
                 throw new IllegalArgumentException("No handler in IRCAdapter class for: " + e.getClass().getName());
             }
 
             m.invoke(adapter, e);
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
             Logger.getLogger(IRCClient.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void readLoop() {
         socket.read(buff, this, new CompletionHandler<Integer, IRCClient>() {
             @Override
             public void completed(Integer result, IRCClient attachment) {
                 byte[] bytes = new byte[buff.position()];
 
                 buff.flip();
                 buff.get(bytes);
                 buff.rewind();
 
                 String raw = part + new String(bytes);
                 String[] split = raw.split("\r\n");
                 for (int i = 0; i < split.length - 1; i++) {
                     handleMessage(split[i]);
                 }
                 part = "";
 
                 if (raw.endsWith("\r\n")) {
                     handleMessage(split[split.length - 1]);
                 } else {
                     part += split[split.length - 1];
                 }
 
                 readLoop();
             }
 
             @Override
             public void failed(Throwable exc, IRCClient attachment) {
                 try {
                     close();
                 } catch (IOException ex) {
                 }
             }
         });
     }
 
     private void sendMessage(String message) {
         synchronized (outQueue) {
             if (welcomed) {
                 queueWrite(message);
             } else {
                 welcomeWaiters.add(message);
             }
         }
     }
 
     private void wasWelcomed() {
         synchronized (outQueue) {
             welcomed = true;
 
             for (String message : welcomeWaiters) {
                 queueWrite(message);
             }
         }
     }
 
     private void queueWrite(String raw) {
         synchronized (outQueue) {
             outQueue.add(ByteBuffer.wrap((raw + "\r\n").getBytes()));
             if (!asynchWriting && connected) {
                 writeLoop();
             }
         }
     }
 
     private void writeLoop() {
         synchronized (outQueue) {
             asynchWriting = true;
             socket.write(outQueue.poll(), this, new CompletionHandler<Integer, IRCClient>() {
                 @Override
                 public void completed(Integer result, IRCClient attachment) {
                     synchronized (outQueue) {
                         asynchWriting = false;
                         if (!outQueue.isEmpty()) {
                             writeLoop();
                         }
                     }
                 }
 
                 @Override
                 public void failed(Throwable exc, IRCClient attachment) {
                     try {
                         close();
                     } catch (IOException ex) {
                     }
                 }
             });
 
         }
     }
 }
