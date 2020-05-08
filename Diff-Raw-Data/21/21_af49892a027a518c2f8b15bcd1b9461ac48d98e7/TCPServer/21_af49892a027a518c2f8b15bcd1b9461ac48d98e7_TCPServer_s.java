 /*
  * Copyright (c) 2000
  *      Jon Schewe.  All rights reserved
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
  */
 package net.mtu.eggplant.util.network;
 
 import net.mtu.eggplant.util.Functions;
 
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.io.InputStreamReader;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import java.io.IOException;
 
 /**
  * Simple TCP server class
  * 
 * @version $Revision: 1.4 $
  */
 public class TCPServer extends Object implements Runnable, Cloneable {
   public final static int DEFAULT_PORT = 6789;
   
   protected int _port;
   protected ServerSocket _listenSocket;
   
   private Socket _clientSocket = null;
   private BufferedReader _input = null;
   private PrintWriter _output = null;
 
   /**
      default constructor.  Uses port 6789 as the port to listen on.
   **/
   public TCPServer() {
     this(DEFAULT_PORT);
   }
                  
   /**
      Create a ServerSocket to listen for connections on
      If port < 0 or port > 65535 port defaults to DEFAULT_PORT.
     @param the port to listen on
   **/
   public TCPServer(int port) {
     if (port < 0 || port > 65535) {
       port = DEFAULT_PORT;
     }
     this._port = port;
     
     try {
       _listenSocket = new ServerSocket(port);
     }
     catch (IOException e) {
       Functions.fail(e, "Exception creating server socket");
     }
     System.out.println("Server: listening on port " + _port);
     
   }
 
   /**
      The body of the server thread.  Loop forever, listening for and
      accepting connections from clients.  For each connection, 
      create a Connection object to handle communication through the
      new Socket.
   **/
   public void run() {
     // check if we're starting fresh or already have a connection
     if(_clientSocket != null) {
       initializeConnection();
     }
     
     try {
       while(true) {
         Socket client_socket = _listenSocket.accept();
         try {
           TCPServer clone = (TCPServer)this.clone();
           clone.setSocket(client_socket);
           ThreadGroup tg = new
             ThreadGroup(client_socket.getInetAddress().getHostName());
           Thread t = new Thread(tg, clone);
           t.start();
         }
         catch(CloneNotSupportedException cnse) {
           System.err.println("Clone Not supported?!");
         }
       }
     }
     catch (IOException e) { 
       Functions.fail(e, "Exception while listening for connections");
     }
   }
 
   /**
     actually make the connection and wait for data.
     @param socket the socket we just connected to
  **/
   public void initializeConnection() {
     try { 
       _input = new BufferedReader(new
                                   InputStreamReader(getSocket().getInputStream())); 
       
       _output = new PrintWriter(getSocket().getOutputStream(), true);
     }
     catch (IOException e) {
       try {
         getSocket().close();
       }
       catch (IOException e2) { /* nothing */ }
       System.err.println("Exception while getting socket streams: " + e);
       return;
     }
 
     // wait for data
     String line;
     StringBuffer revline;
     int len;
     try {
       while(true) {
         // read in a line
         line = readLine();
         if(processData(line)) {
           break;
         }
       }
     }
     catch (IOException e) { /* nothing */ }
     finally {
       try {
         getSocket().close();
       }
       catch (IOException e2) { /* nothing */ }
     }
     
   }
 
   /**
      do something with a line of input.  Just print the line back out on the
      socket.
      Override this to do more with the data.
      @param line the input
      @return true if we should quit, false otherwise
   **/
   protected boolean processData(String line) {
     if(line.equals("Quit")) {
       return true;
     }
     else {
       print(line);
     }
     return false;
   }
 
   /**
      print something to the socket.
      @param message the message, if not connected nothing happens
   **/
   public void print(String message) {
     if(_output != null) {
       _output.println(getClass().getName() + ": " + message);
     }
   }
 
   /**
      read a line from the socket
      @return the line, null if not connected
   **/
   public String readLine() throws IOException {
     if(_input != null) {
       return _input.readLine();
     }
     else {
       return null;
     }
   }
 
   /**
      get the actual socket
      @return the socket, null if not connected
   **/
   protected Socket getSocket() {
     return _clientSocket;
   }
 
   /**
      set the actual socket.
      @param s the socket
   **/
   protected void setSocket(Socket s) {
     _clientSocket = s;
   }
     
   public static void main(String[] args) {
     TCPServer server;
     if (args.length == 1) {
       try {
         server = new TCPServer(Integer.parseInt(args[0]));
       }
       catch (NumberFormatException e) {
         server = new TCPServer();
       }
     }
     else {
       server = new TCPServer();
     }
     Thread t = new Thread(server);
     t.start();
   }
 }
 
   
   
