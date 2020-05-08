 /**
  * Logback: the reliable, generic, fast and flexible logging framework.
  * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
  *
  * This program and the accompanying materials are dual-licensed under
  * either the terms of the Eclipse Public License v1.0 as published by
  * the Eclipse Foundation
  *
  *   or (per the licensee's choosing)
  *
  * under the terms of the GNU Lesser General Public License version 2.1
  * as published by the Free Software Foundation.
  */
 package com.janrain.logback;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 /**
  * SyslogTCPOutputStream is a wrapper around the {@link DatagramSocket} class so that it
  * behaves like an {@link OutputStream}.
  */
 public class SyslogTCPOutputStream extends OutputStream {
 
   /**
    * The maximum length after which we discard the existing string buffer and
    * start anew.
    */
   private static final int MAX_LEN = 1024;
 
   private InetAddress address;
   private Socket sock;
   private ByteArrayOutputStream baos = new ByteArrayOutputStream();
   final private int port;
 
   public SyslogTCPOutputStream(String syslogHost, int port) throws UnknownHostException,
       SocketException {
     this.address = InetAddress.getByName(syslogHost);
     this.port = port;
     try {
         this.sock = new Socket(this.address, this.port);
     } catch(Exception e) {
         throw new SocketException(e.getMessage());
     }
   }
 
   public void write(byte[] byteArray, int offset, int len) throws IOException {
     baos.write(byteArray, offset, len);
   }
 
   public void flush() throws IOException {
     byte[] bytes = baos.toByteArray();
 
     // clean up for next round
     if (baos.size() > MAX_LEN) {
       baos = new ByteArrayOutputStream();
     } else {
       baos.reset();
     }
     
     // after a failure, it can happen that bytes.length is zero
     // in that case, there is no point in sending out an empty message/
     if(bytes.length == 0) {
       return;
     }
     if(this.sock != null) {
       try {
         OutputStream outputStream = sock.getOutputStream();
         outputStream.write(bytes);
         outputStream.write('\n');
       } catch(SocketException e) {
        System.out.println("Caught exception: " + e);
         throw new IOException(e);
       }
     }
   }
 
   public void close() {
     address = null;
     sock = null;
   }
 
   public int getPort() {
     return port;
   }
 
   @Override
   public void write(int b) throws IOException {
     baos.write(b);
   }
 
 }
