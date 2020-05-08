 /**
 * Copyright (c) 2012-2014, Ken Anderson <caffeinatedrat at gmail dot com>
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.caffeinatedrat.WebSocketServicesBridge.Server;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.text.MessageFormat;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import com.caffeinatedrat.SimpleWebSockets.Handshake;
 import com.caffeinatedrat.SimpleWebSockets.Exceptions.InvalidFrameException;
 import com.caffeinatedrat.SimpleWebSockets.Frames.Frame;
 import com.caffeinatedrat.SimpleWebSockets.Frames.FullFrameReader;
 import com.caffeinatedrat.SimpleWebSockets.Util.Logger;
 
 /**
  * Handles a single threaded connection to a targeted server.
  *
  * @version 1.0.0.0
  * @author CaffeinatedRat
  */
 public class ProxyConnection extends Thread {
 
     public enum StateInfo {
         
         UNINITIALIZED,
         INITIALIZED,
         CONNECTING,
         CONNECTED,
         CLOSED,
         BADSTATE
         
     }
     
     // ----------------------------------------------
     // Member Vars (fields)
     // ----------------------------------------------
     private ConfiguredServer configuredServer = null;
     private Socket socket = null;
     private ProxyFrameWriter writer = null;
     
     //Reuse the handshake again...
     private Handshake handshake = null;
     
     //Blocking queue.
     private Queue<Frame> framesFromClient = new LinkedList<Frame>();
     
     //State management of the connection.
     private volatile StateInfo state = StateInfo.UNINITIALIZED;
 
     
     // ----------------------------------------------
     // Properties
     // ----------------------------------------------
     
     /**
      * Returns true if the connection is closed.
      * @return true if the connection is closed.
      */
     public synchronized boolean isClosed() {
 
         return (this.state == StateInfo.CLOSED);
         
     }
     
     
     // ----------------------------------------------
     // Constructors
     // ----------------------------------------------
     
     public ProxyConnection(ConfiguredServer configuredServer, ProxyFrameWriter writer, Handshake handshake) {
         
         if (configuredServer == null) {
             throw new IllegalArgumentException("The configuredServer is invalid (null).");
         }
         
         this.configuredServer = configuredServer;
         this.state = StateInfo.INITIALIZED;
         this.handshake = handshake;
         this.writer = writer;
         
     }
 
     // ----------------------------------------------
     // Thread Entry Point
     // ----------------------------------------------
     @Override
     public void run() {
         
         //Open a connection to the server for the first time.
         if (open()) {
             
             //A handshake must be successful before we can proceed.
             if (performHandshake(this.handshake)) {
                 
                 boolean continueListening = true;
                 while ( (!socket.isClosed()) && (continueListening) ) {
                     
                     try {
                         
                         //If the frames are not fragmented then write a single frame.
                         if (framesFromClient.size() == 1) {
                             
                             writeToServer(framesFromClient.poll());
                             
                         }
                         //If frames are fragmented we have to go into a O(n) operation where n is the number of fragments.
                         else {
                             
                             //Iterate through each frame from the client.
                             while(!framesFromClient.isEmpty()) {
                                 
                                 writeToServer(framesFromClient.poll());
                             }
                             
                         }
                         //END OF if (framesFromClient.size() == 1) {...
 
                         FullFrameReader reader = new FullFrameReader(this.socket, null, 15000, this.configuredServer.getMaximumFragmentationSize());
                         
                         //Read frames from the server.
                         if(reader.read()) {
                             
                             List<Frame> framesFromServer = reader.getFrames();
                             
                             if (reader.getFrameType() != Frame.OPCODE.CONNECTION_CLOSE_CONTROL_FRAME) {
                                 writeToClient(framesFromServer);
                             }
                             else {
                                 continueListening = false;
                             }
                             
                         }
                         //END OF if(reader.read()) {...
                         
                     }
                     catch (InvalidFrameException e) {
                         break;
                     }
                 }
                 //END OF while ( (!socket.isClosed()) && (continueListening) ) {...
                 
                 close();
             }
             //END OF if (performHandshake(handshake)) {...
         }
         //END OF if (open()) {...
     }
     
     // ----------------------------------------------
     // Public Methods
     // ----------------------------------------------
     
     /**
      * Adds frames to the connection to be passed onto the configured server.
      * @params frames if the connection was successful.
      */
     public synchronized void addFrames(List<Frame> frames) {
         
         if (frames == null) {
             throw new IllegalArgumentException("The argument frames cannot be null.");
         }
         
         this.framesFromClient.addAll(frames);
         
     }
     
     // ----------------------------------------------
     // Internal Methods
     // ----------------------------------------------
     
     /**
      * Attempts to open a connection to the configured server.
      * @return true if the connection was successful.
      */
     protected boolean open() {
         
         //We are already connected or connecting, ignore this.
         if (this.state == StateInfo.CONNECTED || this.state == StateInfo.CONNECTING) {
             return true;
         }
         
         if (this.state == StateInfo.INITIALIZED) {
         
             String address = this.configuredServer.getAddress();
             
             try {
                 
                 //If a socket exists, see if we can reuse it if it is closed.
                 if (this.socket != null) {
                 
                     if ( this.socket.isClosed() ) {
                         this.socket = new Socket(address, this.configuredServer.getPort());
                     }
                     
                 }
                 else {
                     
                     this.socket = new Socket(address, this.configuredServer.getPort());
                     
                 }
                 
                 Logger.verboseDebug(MessageFormat.format("A connection to the configured server {0} has been opened.", this.configuredServer.getServerName()));
                 this.state = StateInfo.CONNECTING;
                 return true;
             }
             catch (UnknownHostException e) {
     
                 this.state = StateInfo.CLOSED;
                 Logger.info(MessageFormat.format("The host {0} is invalid or could not be resolved.", address));
                 Logger.verboseDebug(MessageFormat.format("Details: {0}", e.getMessage()));
                 
             }
             catch (IOException e) {
                 
                 this.state = StateInfo.CLOSED;
                 Logger.verboseDebug(MessageFormat.format("Unknown IOException.\r\n Details: {0}", e.getMessage()));
                 
             }
         }
         else {
             Logger.verboseDebug(MessageFormat.format("The connection cannot be opened during this state {0}", this.state.toString()));
         }
         
         return false;
     }
     
     /**
      * Attempts to close a connection to the configured server.
      */
     protected synchronized void close() {
         
         if (this.socket != null) {
             
             try {
                 
                 Logger.verboseDebug(MessageFormat.format("The connection to the configured server {0} has been closed.", this.configuredServer.getServerName()));
                 
                 this.state = StateInfo.CLOSED;
                 socket.close();
                 
             } catch (IOException e) {
                 //Do nothing...
             }
             
         }
         
     }
     
     /**
      * Attempt to handshake with the configured server.
      * @param handshakeRequest An already established handshake from the original client.
      * @return true if the handshake was successful
      */
     protected boolean performHandshake(Handshake handshakeRequest) {
         
         //We are already connected ignore this.
         if (this.state == StateInfo.CONNECTED) {
             return true;
         }
         
         if (this.state == StateInfo.CONNECTING) {
             
             this.handshake = handshakeRequest.cloneHandshake(this.socket);
             this.handshake.forwardRequest();
             if(this.handshake.negotiateResponse()) {
                 
                 this.state = StateInfo.CONNECTED;
                 return true;
                 
             }
             else {
                 Logger.verboseDebug("The handshake has failed.");
             }
             
         }
         else {
             Logger.verboseDebug(MessageFormat.format("The handshake cannot be performed during this state {0}", this.state.toString()));
         }
         
         //Close the socket.
         close();
         return false;
     }
     
     /**
      * Attempt to write a read frame to the configured server.
      * @param frame The frame to write to the configured server.
      * @throws InvalidFrameException if a frame is invalid.
      */    
     protected void writeToServer(Frame frame) throws InvalidFrameException {
         
         //For now, do nothing on null frames.
         if (frame == null) {
             return;
         }
         
         //We can only write if the connection is still opened.
         if (this.state == StateInfo.CONNECTED) {
         
             try {
                 
                 //TODO: --Performance Improvement
                 frame.write(this.socket);
             
             } catch (InvalidFrameException e) {
                  
                 close();
                 throw e;
 
             }
         }
         else {
             Logger.verboseDebug(MessageFormat.format("The server write cannot be performed during this state {0}", this.state.toString()));
         }
         //END OF if (this.state == StateInfo.CONNECTED) {...
     }
     
     /**
      * Attempt to write a read frame to the client.
      * @param frame The frames to write to the client.
      * @throws InvalidFrameException if a frame is invalid.
      */
     protected void writeToClient(List<Frame> frames) throws InvalidFrameException {
         
         //For now, do nothing on null frames.
         if (frames == null) {
             return;
         }
         
         //We can only write if the connection is still opened.
         if (this.state == StateInfo.CONNECTED) {
             //this.writer.Write(frames, this.configuredServer.getServerName());
            this.writer.Write(frames, this.configuredServer.getServerName());
         }
         else {
             Logger.verboseDebug(MessageFormat.format("The client write cannot be performed during this state {0}", this.state.toString()));
         }
         //END OF if (this.state == StateInfo.CONNECTED) {...
     }    
     
 }
