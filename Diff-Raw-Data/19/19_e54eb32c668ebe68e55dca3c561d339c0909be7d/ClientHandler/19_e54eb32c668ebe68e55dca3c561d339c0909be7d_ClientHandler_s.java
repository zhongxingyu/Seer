 /**
  * ClientHandler.java
  *
  * Copyright 2012 Niolex, Inc.
  *
  * Niolex licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.niolex.network.server;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 
 import org.apache.niolex.network.BasePacketWriter;
 import org.apache.niolex.network.Config;
 import org.apache.niolex.network.IPacketHandler;
 import org.apache.niolex.network.PacketData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The basic ClientHandler reads and writes Packet.
  *
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0
  * @Date: 2012-6-11
  */
 public class ClientHandler  extends BasePacketWriter {
 	private static final Logger LOG = LoggerFactory.getLogger(ClientHandler.class);
 
 	/* 缓冲区大小*/
 	private static final int BLOCK = Config.SERVER_NIO_BUFFER_SIZE;
 
 
     /**
      * Internal used in ClientHandler. Please ignore.
      *
      * @author Xie, Jiyun
      *
      */
     public static enum Status {
         HEADER, BODY, NONE, DATA, SEND
     }
 
     /**
      * The packet handler.
      */
     private IPacketHandler packetHandler;
 
     /**
      * The server selector holding this handler.
      */
     private Selector selector;
 
     /**
      * The socket channel this client handler is handling
      */
     private SocketChannel socketChannel;
 
     /**
      * The interval to send heart beat if no packet sent between this time.
      */
     private int heartBeatInterval = Config.SERVER_HEARTBEAT_INTERVAL;
 
     /* 发送数据缓冲区*/
     private ByteBuffer sendBuffer = ByteBuffer.allocate(BLOCK);
     private Status sendStatus;
     private PacketData sendPacket;
     private int sendPos;
 
     /**
      * The latest packet send time.
      */
     private long lastSentTime;
 
     /**
      * Current write status. attached to the selector or not.
      */
     private Boolean writeAttached = Boolean.FALSE;
 
     /**
      * The name of this client socket.
      */
     private String remoteName;
 
     /* 接收数据缓冲区*/
     private ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);
     private Status receiveStatus;
     private PacketData receivePacket;
 
     /**
      * Constructor of ClientHandler, manage a SocketChannel inside.
      *
      * @param packetHandler
      * @param selector
      * @param client
      */
     public ClientHandler(IPacketHandler packetHandler, Selector selector, SocketChannel client) {
 		super();
 		this.packetHandler = packetHandler;
 		this.selector = selector;
 		this.socketChannel = client;
 
 		// Initialize local variables.
         sendStatus = Status.NONE;
         receiveStatus = Status.HEADER;
         remoteName = client.socket().getRemoteSocketAddress().toString();
         lastSentTime = System.currentTimeMillis();
         StringBuilder sb = new StringBuilder();
         sb.append("Remote Client [").append(remoteName);
         sb.append("] connected to local Port [").append(client.socket().getLocalPort());
         sb.append("].");
         LOG.info(sb.toString());
     }
 
     @Override
     public String getRemoteName() {
         return remoteName;
     }
 
     @Override
 	public void handleWrite(PacketData sc) {
 		super.handleWrite(sc);
 		// Signal the selector there is data to write.
 		synchronized (writeAttached) {
 			if (!writeAttached) {
 				try {
 					socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);
 				} catch (Exception e) {
 					LOG.warn("Failed to attach write to selector, client will stop. " , e.toString());
 					handleClose();
 				}
 				writeAttached = Boolean.TRUE;
 			}
 		}
 	}
 
 	/**
      * handle read request. called by NIO selector.
      * This method will read packet over and over again. Never stop.
      *
      * Read status change summary:
      * HEADER -> Need read header, means nothing is read by now
      * BODY -> Header is read, need to read body now
      *
      */
     public boolean handleRead() {
         try {
             int k = socketChannel.read(receiveBuffer);
             if (k < 0) {
             	// This socket is closed now.
             	handleClose();
             	return false;
             }
             if (receiveBuffer.position() > 0) {
                 if (receiveStatus == Status.HEADER && receiveBuffer.position() < 8) {
                     return false;
                 }
                 receiveBuffer.flip();
 
                 if (receiveStatus == Status.HEADER) {
                     receivePacket = new PacketData();
                     if (receivePacket.parseHeader(receiveBuffer)) {
                     	LOG.debug("Packet received. desc {}, size {}.", receivePacket.descriptor(), receivePacket.getLength());
                         packetHandler.handleRead(receivePacket, this);
                         receiveBuffer.compact();
                         return true;
                     } else {
                         receiveBuffer.clear();
                         receiveStatus = Status.BODY;
                     }
                 } else {
                     if (receivePacket.parseBody(receiveBuffer)) {
                     	LOG.debug("Packet received. desc {}, size {}.", receivePacket.descriptor(), receivePacket.getLength());
                         packetHandler.handleRead(receivePacket, this);
                         receiveStatus = Status.HEADER;
                         receiveBuffer.compact();
                         return true;
                     } else {
                         receiveBuffer.clear();
                     }
                 }
             }
         } catch (Exception e) {
             LOG.info("Failed to read data from client socket: {}", e.toString());
             handleClose();
         }
         return false;
     }
 
     /**
      * Handle write request. called by NIO selector.
      * Send packets to client when there is any.
      *
      * Status change summary:
      * NONE -> Nothing is sending now
      * DATA -> Sending a packet, not finished yet, need to fill buffer again
      * SEND -> Sending a packet, all in the buffer. If buffer sent, can start new
      *
      */
     public boolean handleWrite() {
         try {
             if (sendStatus == Status.NONE) {
                 return sendNewPacket();
             } else {
                 if (sendPos != sendBuffer.limit()) {
                     sendPos += socketChannel.write(sendBuffer);
                     return sendPos == sendBuffer.limit();
                 } else {
                     if (sendStatus == Status.SEND) {
                     	sendStatus = Status.NONE;
                     	LOG.debug("Packet sent. desc {}, size {}.", sendPacket.descriptor(), sendPacket.getLength());
                         return sendNewPacket();
                     } else {
                     	sendBuffer.clear();
                         if (sendPacket.generateData(sendBuffer)) {
                             sendStatus = Status.SEND;
                         }
                         sendBuffer.flip();
                         sendPos = socketChannel.write(sendBuffer);
                         return sendPos == sendBuffer.limit();
                     }
                 }
             }
         } catch (Exception e) {
             LOG.info("Failed to send data to client socket: {}", e.getMessage());
             handleClose();
         }
         return false;
     }
 
     /**
      * Start to send a new packet, or send the heart beat packet.
      * @throws IOException
      */
     private boolean sendNewPacket() throws IOException {
     	sendPacket = super.handleNext();
 
         if (sendPacket == null) {
             if (lastSentTime + heartBeatInterval < System.currentTimeMillis()) {
             	sendPacket = PacketData.getHeartBeatPacket();
             	return doSendNewPacket();
             } else {
             	// Nothing to send, remove the OP_WRITE from selector.
             	synchronized (writeAttached) {
             		if (writeAttached) {
             			// The second time try to find a packet. If still nothing, then do the remove.
             			sendPacket = super.handleNext();
             			if (sendPacket != null) {
             				return doSendNewPacket();
             			}
             			try {
         					socketChannel.register(selector, SelectionKey.OP_READ, this);
         				} catch (Exception e) {
         					LOG.warn("Failed to dettach write from selector, client will stop. " , e.toString());
         					handleClose();
         				}
         				writeAttached = Boolean.FALSE;
             		}
             	}
             }
             return false;
         }
         return doSendNewPacket();
     }
 
     /**
      * Do really send the packet.
      * @return
      * @throws IOException
      */
     private boolean doSendNewPacket() throws IOException {
         lastSentTime = System.currentTimeMillis();
         sendStatus = Status.DATA;
         sendBuffer.clear();
         if (sendPacket.generateData(sendBuffer)) {
             sendStatus = Status.SEND;
         }
         sendBuffer.flip();
         sendPos = socketChannel.write(sendBuffer);
         return sendPos == sendBuffer.limit();
     }
 
     /**
      * Error occurred when read or write.
      * Anyway, socket is closed.
      *
      * Need to close socket and clean Map.
      *
      * Call IPacketHandler.handleClose internally.
      */
     private void handleClose() {
     	try {
             socketChannel.close();
             StringBuilder sb = new StringBuilder();
             sb.append("Remote Client [").append(remoteName);
             sb.append("] disconnected.");
             LOG.info(sb.toString());
         } catch (IOException e) {
             LOG.info("Failed to close client socket: {}", e.getMessage());
         } finally {
         	packetHandler.handleClose(this);
         }
     }
 
 
     /**
 	 * Return The heartBeatInterval
 	 * @see org.apache.niolex.network.IServer#getHeartBeatInterval()
 	 */
 	public int getHeartBeatInterval() {
         return heartBeatInterval;
     }
 
     /**
 	 * The heartBeatInterval to set
 	 * @see org.apache.niolex.network.IServer#setHeartBeatInterval(int)
 	 */
 	public void setHeartBeatInterval(int heartBeatInterval) {
         this.heartBeatInterval = heartBeatInterval;
     }
 }
