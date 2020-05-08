 /**
  * Client component for generating load for the KeyValue store.
  * This is also used by the Master server to reach the slave nodes.
  *
  * @author Mosharaf Chowdhury (http://www.mosharaf.com)
  * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
  *
  * Copyright (c) 2012, University of California at Berkeley
  * All rights reserved.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *  * Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of University of California, Berkeley nor the
  *    names of its contributors may be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package edu.berkeley.cs162;
 
 import java.net.Socket;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.UnknownHostException;
 
 /**
  * This class is used to communicate with (appropriately marshalling and unmarshalling)
  * objects implementing the {@link KeyValueInterface}.
  *
  * @param <K> Java Generic type for the Key
  * @param <V> Java Generic type for the Value
  */
 public class KVClient implements KeyValueInterface {
 
     private String server = null;
     private int port = 0;
 
     /**
      * @param server is the DNS reference to the Key-Value server
      * @param port is the port on which the Key-Value server is listening
      */
     public KVClient(String server, int port) {
         this.server = server;
         this.port = port;
     }
 
     private Socket connectHost() throws KVException {
         Socket socket = null;
         try {
             socket = new Socket(this.server, this.port);
         } catch(UnknownHostException u) {
             throw new KVException(new KVMessage("resp", "Network Error: Could not connect"));
         } catch(IOException io) {
             throw new KVException(new KVMessage("resp", "Network Error: Could not create socket"));
         }
         return socket;
     }
 
     private void closeHost(Socket sock) throws KVException {
         try {
             sock.close();
         } catch(IOException io) {
             throw new KVException(new KVMessage("resp", "Unknown Error: Couldn’t close connection"));
         }
     }
 
     private void shutdownOut(Socket socket) throws KVException {
         try {
             socket.shutdownOutput();
         } catch(IOException io) {
             throw new KVException(new KVMessage("resp", "Unknown Error: Couldn’t shut down output"));
         }
     }
 
     private InputStream setupInput(Socket socket) throws KVException {
         InputStream in;
         try {
             in = socket.getInputStream();
         } catch(IOException io) {
             throw new KVException(new KVMessage("resp", "Network Error: Could not receive data"));
         }
 
         return in;
     }
 
     public void put(String key, String value) throws KVException {
         Socket socket = connectHost();
         KVMessage kvReq = new KVMessage("putreq");
         kvReq.setKey(key);
         kvReq.setValue(value);
         kvReq.sendMessage(socket);
         System.out.println("Request: " + kvReq.toXML());
         shutdownOut(socket);
         InputStream in = setupInput(socket);
 
         KVMessage kvResp = new KVMessage(in);
         if(!kvResp.getMessage().equals("Success")) {
             throw new KVException(kvResp);
         }
         System.out.println("Response: " + kvResp.toXML());
         closeHost(socket);
     }
 
     public String get(String key) throws KVException {
         Socket socket = connectHost();
         KVMessage kvReq = new KVMessage("getreq");
         kvReq.setKey(key);
         kvReq.sendMessage(socket);
         System.out.println("Request: " + kvReq.toXML());
         String result = "";
 
         shutdownOut(socket);
         InputStream in = setupInput(socket);
 
         KVMessage kvResp = new KVMessage(in);
        if(!kvResp.getMessage().equals("Does not exist") && kvResp.getValue() == null) {
             throw new KVException(kvResp);
         }
         result = kvResp.getValue();
         System.out.println("Response:" + kvResp.toXML());
         closeHost(socket);
         return result;
     }
 
     public void del(String key) throws KVException {
         Socket socket = connectHost();
         KVMessage kvReq = new KVMessage("delreq");
         kvReq.setKey(key);
         kvReq.sendMessage(socket);
         System.out.println("Request: " + kvReq.toXML());
 
         shutdownOut(socket);
         InputStream in = setupInput(socket);
 
 		KVMessage kvResp = new KVMessage(in);
         if(!kvResp.getMessage().equals("Success"))
             throw new KVException(kvResp);
         System.out.println("Response:" + kvResp.toXML());
         closeHost(socket);
     }
 
     // public boolean connectTest(){
     //     try {
     //         KVServer kvserver = new KVServer(100, 10);
     //         SocketServer sserver = new SocketServer("localhost", 8080);
     //         Thread server = new Thread(new runServer(kvserver, sserver));
     //         server.start();
     //         Socket socket = this.connectHost();
     //         sserver.stop();
     //         server.stop();
     //         while(server.isAlive()){
     //             Thread.currentThread().yield();
     //         }
     //         return socket.isConnected();
     //     } catch (Exception e){
     //         System.out.println(e.getMessage());
     //         return false;
     //     }
     // }
     
     // public boolean closeTest(){
     //     try {
     //         KVServer kvserver = new KVServer(100, 10);
     //         SocketServer sserver = new SocketServer("localhost", 8080);
     //         Thread server = new Thread(new runServer(kvserver, sserver));
     //         server.start(); 
     //         Socket socket = this.connectHost();
     //         this.closeHost(socket);
     //         sserver.stop();
     //         server.stop();
     //         while(server.isAlive()){
     //             Thread.currentThread().yield();
     //         }
     //         return socket.isClosed();
     //     } catch (Exception e){
     //         System.out.println(e.getMessage());
     //         return false;
     //     }
     // }
 
     // private class runServer implements Runnable{
     //     KVServer server;
     //     SocketServer ss;
     //     public runServer(KVServer s, SocketServer socketServer){
     //         kvclient.ss = socketServer;
     //         kvclient.server = s;
     //     }
     //     public void run(){
     //         try {
     //             System.out.println("Binding Server:");
     //             NetworkHandler handler = new KVClientHandler(kvclient.server);
     //             ss.addHandler(handler);
     //             ss.connect();
     //             System.out.println("Starting Server");
     //             ss.run();
     //         } catch (Exception e){
     //             ;
     //         }
     //     }
     // }
 }
