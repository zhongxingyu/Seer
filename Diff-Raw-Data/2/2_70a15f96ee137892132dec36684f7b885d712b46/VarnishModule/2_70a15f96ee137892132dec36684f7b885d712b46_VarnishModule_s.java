 /**
  * Mule Rest Module
  *
  * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * This software is protected under international copyright law. All use of this software is
  * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
  * separately entered into in writing between you and MuleSoft. If such an agreement is not
  * in place, you may not use the software.
  */
 
 package org.mule.modules.varnish;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.log4j.Logger;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.mule.api.ConnectionException;
 import org.mule.api.ConnectionExceptionCode;
 import org.mule.api.annotations.Connect;
 import org.mule.api.annotations.ConnectionIdentifier;
 import org.mule.api.annotations.Connector;
 import org.mule.api.annotations.Disconnect;
 import org.mule.api.annotations.InvalidateConnectionOn;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.ValidateConnection;
 import org.mule.api.annotations.param.ConnectionKey;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * A Mule module for connecting with Varnish administrative interface via
  * TCP/IP and sending administrative instructions such as cache purge or
  * URL banning.
  * <p/>
  * {@sample.config ../../../doc/mule-module-varnish.xml.sample varnish:config}
  *
  * @author MuleSoft, Inc.
  */
 @Connector(name = "varnish")
 public class VarnishModule extends SimpleChannelHandler {
     private static final Logger LOGGER = Logger.getLogger(VarnishModule.class);
 
     private Channel channel;
     private final Lock lock = new ReentrantLock();
     private final Queue<Callback> callbacks = new ConcurrentLinkedQueue<Callback>();
 
     /**
      * Connect to Varnish management port
      *
      * @param host Host of the instance running Varnish
      * @param port Port at which the management interface is located
      * @param secret Shared secret to be used when connecting to a secured management port
      * @throws ConnectionException if cannot connect
      */
     @Connect
     public void connect(@ConnectionKey String host, int port, String secret) throws ConnectionException {
         ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
 
         final SimpleChannelHandler simpleChannelHandler = this;
 
         ClientBootstrap bootstrap = new ClientBootstrap(factory);
         bootstrap.setPipelineFactory(new VarnishPipelineFactory(simpleChannelHandler));
         bootstrap.setOption("tcpNoDelay", true);
         bootstrap.setOption("keepAlive", true);
 
         // start the connection attempt.
         LOGGER.debug("Connecting to Varnish management port at " + host + ":" + Integer.toString(port));
         Callback callback = new Callback();
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
 
         lock.lock();
         try {
             callbacks.add(callback);
             // wait until the connection attempt succeeds or fails.
             channel = future.awaitUninterruptibly().getChannel();
             if (!future.isSuccess()) {
                 LOGGER.error("Connection failure to Varnish management port at " + host + ":" + Integer.toString(port) + ". Cause: " + future.getCause().getMessage());
                 channel = null;
                 throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", future.getCause().getMessage());
             }
         } finally {
             lock.unlock();
         }
 
         VarnishResponse response = null;
         try {
             response = callback.get(60, TimeUnit.SECONDS);
         } catch (TimeoutException e) {
             throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", "Timeout await connect ACK");
         }
 
         if (response.getStatusCode() != VarnishStatusCode.OK) {
             if (response.getStatusCode() == VarnishStatusCode.AUTHENTICATION_REQUIRED) {
                 String challenge = response.getMessage().substring(0, response.getMessage().indexOf('\n'));
                 authenticate(secret, challenge);
             } else {
                 throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", response.getMessage());
             }
         }
     }
 
     private void authenticate(String secret, String challenge) throws ConnectionException {
         LOGGER.debug("Received " + challenge + " as challenge. Calculating response...");
         if (secret == null) {
             throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", "Authentication is required but no secret has been provided");
         }
 
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-256");
            String messageToDigest = challenge + '\n' + secret + challenge + '\n';
             md.update(messageToDigest.getBytes("US-ASCII"));
 
             String sha256 = new String(Hex.encodeHex(md.digest()));
 
             LOGGER.debug("SHA256 calculated as " + sha256 + ". Sending auth command.");
 
             Callback callback = new Callback();
             lock.lock();
             try {
                 callbacks.add(callback);
                 // wait until the connection attempt succeeds or fails.
                 ChannelFuture future = channel.write("auth " + sha256 + "\n");
                 future.awaitUninterruptibly();
                 if (!future.isSuccess()) {
                     throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", "Unable to send auth command", future.getCause());
                 }
             } finally {
                 lock.unlock();
             }
 
             VarnishResponse response = null;
             try {
                 response = callback.get(60, TimeUnit.SECONDS);
             } catch (TimeoutException e) {
                 throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", "Timeout while waiting for auth response");
             }
 
             if (response.getStatusCode() != VarnishStatusCode.OK) {
                 throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", "Authentication unsuccessful");
             }
         } catch (NoSuchAlgorithmException e) {
             throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", "Unable to acquire a message digest generator for SHA-256", e);
         } catch (UnsupportedEncodingException e) {
             throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, "", "Unsupported encoding US-ASCII", e);
         }
     }
 
     @ConnectionIdentifier
     public String connectionId() {
         return Integer.toString(channel.getId());
     }
 
     @ValidateConnection
     public boolean validateConnection() {
         if (channel != null) {
             return channel.isConnected();
         } else {
             return false;
         }
     }
 
     @Disconnect
     public void disconnect() {
         channel.disconnect();
     }
 
     /**
      * Immediately invalidate all documents whose URL matches the specified regular expression. Please note that the
      * Host part of the URL is ignored, so if you have several virtual hosts all of them will be banned. Use ban to
      * specify a complete ban if you need to narrow it down.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-varnish.xml.sample varnish:ban-url}
      *
      * @param url URL to ban, it can be a regular expression
      * @throws VarnishChannelException if unable to send message
      * @throws VarnishException        if the response from server was not OK
      * @throws TimeoutException        if operation took longer than expected
      */
     @Processor
     @InvalidateConnectionOn(exception = TimeoutException.class)
     public void banUrl(String url) throws VarnishChannelException, VarnishException, TimeoutException {
         LOGGER.info("Banning URL " + url + " from Varnish cache located at " + channel.getRemoteAddress().toString());
 
         Callback callback = new Callback();
         lock.lock();
         try {
             callbacks.add(callback);
             ChannelFuture future = channel.write("ban.url " + url + "\n");
             future.awaitUninterruptibly();
             if (!future.isSuccess()) {
                 throw new VarnishChannelException("Unable to ban url", future.getCause());
             }
         } finally {
             lock.unlock();
         }
 
         VarnishResponse response = callback.get(60, TimeUnit.SECONDS);
 
         if (response.getStatusCode() != VarnishStatusCode.OK) {
             throw new VarnishException("An error occurred: " + response.getStatusCode().name() + " " + response.getMessage());
         }
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         callbacks.poll().handle((VarnishResponse) e.getMessage());
     }
 
     static class Callback {
         private final CountDownLatch latch = new CountDownLatch(1);
         private VarnishResponse response;
 
         VarnishResponse get(int timeout, TimeUnit unit) throws TimeoutException {
             try {
                 if (!latch.await(timeout, unit)) {
                     throw new TimeoutException("No response was received in a timely fashion");
                 }
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
             return response;
         }
 
         void handle(VarnishResponse response) {
             this.response = response;
             latch.countDown();
         }
     }
 }
