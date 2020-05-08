 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 package net.FriendsUnited.NodeLevel;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.ClosedByInterruptException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.security.SecureRandom;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.FriendsUnited.FriendPacket;
 import net.FriendsUnited.NodeLevel.Packet.HelloPacket;
 import net.FriendsUnited.NodeLevel.Packet.ServerListPacket;
 import net.FriendsUnited.NodeLevel.Packet.ServerPacket;
 import net.FriendsUnited.NodeLevel.Packet.ServerRequestPacket;
 import net.FriendsUnited.Util.ByteConverter;
 import net.FriendsUnited.Util.Tool;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class RemoteNode
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
 
 
     /** Byte sequence to identify the data as FriendsUnited Data.
      */
     public static final byte MAGIC = (byte)0xf1;
     public static final byte VERSION = (byte)1;
     public static final int MIN_SIZE = 1;
     public static final int MAX_SIZE = 30 * 1024;
     public static final int MAX_SERVERS = 50;
     public static final int ENVELOPE = 4;
     public static final int CONNECT_TIMEOUT = 1000;
     public static final int HELLO_TIMEOUT = 300;
     public static final int SERVER_INFO_TIMEOUT = 500;
     public static final int IDLE_TIMEOUT = 300000;
 
     private SocketChannel socketChannel;
     private final InetAddress host;
     private final int port;
     private NodeId remNode;
     private final NodeId ownNode;
     private final ByteConverter bc = new ByteConverter();
     private final List<byte[]> queue  = new LinkedList<byte[]>();;
     private final List<byte[]> delayQueue = new LinkedList<byte[]>();
     private final PacketTransmitter pt;
     private final int cost;
     private int msUnitTimeOut;
     private final NodePacketHandler pktHndl;
 
     // The buffer into which we'll read data when it's available
     private final ByteBuffer readBuffer = ByteBuffer.allocate(32768);
     // The buffer we use to send data
     private final ByteBuffer writeBuffer = ByteBuffer.allocate(32768);
 
     private enum eState {NEW_AND_WAITING,            /** created no channel established */
                          CONNECTING,                 /** creating the TCP connection */
                          SENDED_HELLO,               /** TCP connection is established
                                                          hello checks if other side is the Node we seek */
                          SENDED_SERVER_INFO_REQUEST, /** requesting the servers that the other Node sees */
                          ESTABLISHED,                /** connection is established an can be used to send Packets */
                          CLOSED};                    /** connection closed connection needs to be reopened before data can be exchanged*/
     private eState State;
 
     /** creates a Remote Node and starts a connection to the node.
      *
      * Remote Node has just been detected, so we give the Node time to connect us.
      *
      * @param ownNodeId  Id of this Node.
      * @param remoteNodeId ID of Node on other end of the channel.
      * @param pt Packet Transmitter that manages the channel.
      * @param host IP Address of other Node.
      * @param port TCP Port of other Node.
      * @param cost cost of this channel.
      */
     public RemoteNode(NodeId ownNodeId,
                       NodeId remoteNodeId,
                       final PacketTransmitter pt,
                       InetAddress host,
                       int port,
                       int cost,
                       NodePacketHandler node)
     {
         this.host = host;
         this.port = port;
         this.cost = cost;
         this.remNode = remoteNodeId;
         this.ownNode = ownNodeId;
         this.pt = pt;
         this.pktHndl = node;
         State = eState.NEW_AND_WAITING;
         SecureRandom sr = new SecureRandom();
         int r = sr.nextInt(3); // only 3 bits
         msUnitTimeOut = 50 + r * 50; // Values: 50, 100, ..,400
     }
 
     /** creates a remote node.
      *
      * Someone connected to this Node but we don't know who.
      *
      * @param pt
      * @param host
      * @param port
      * @param cost
      */
     public RemoteNode(final NodeId ownNodeId,
                       final PacketTransmitter pt,
                       InetAddress host,
                       int port,
                       int cost,
                       NodePacketHandler node)
     {
         this.host = host;
         this.port = port;
         this.pt = pt;
         this.cost = cost;
         this.ownNode = ownNodeId;
         this.remNode = null;
         this.pktHndl = node;
         socketChannel = null;
         State = eState.NEW_AND_WAITING;
         SecureRandom sr = new SecureRandom();
         int r = sr.nextInt(3); // only 3 bits
         msUnitTimeOut = 50 + r * 50; // Values: 50, 100, ..,400
     }
 
     public final String toString()
     {
        return "Packet Queue to " + host + ":" + port + " in State " + State;
     }
 
     public synchronized void tick()
     {
         handleTimeOut();
     }
 
     public final synchronized void setChannel(final SocketChannel channel)
     {
         socketChannel = channel;
         State = eState.CONNECTING;
     }
 
     public final synchronized SocketChannel getChannel()
     {
         return socketChannel;
     }
 
     public final InetAddress getLocation()
     {
         return host;
     }
 
     public int getPort()
     {
         return port;
     }
 
     public final synchronized boolean isConnected()
     {
         if(eState.ESTABLISHED == State)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     public final int getCost()
     {
         return cost;
     }
 
     public final synchronized boolean hasNothingToSend()
     {
         return queue.isEmpty();
     }
 
     /** sends a packet containing the whole byte array.
      *
      * @param data the bytes to send.
      */
     public final void putPacket(final byte[] data)
     {
         putPacket(data, 0, data.length);
     }
 
     /** send the defined bytes out of the byte array.
      *
      * if the data is too much for one Packet then the data will be split up and more than one Packet will be send.
      *
      * @param data the byte Array
      * @param startOffset bytes to skip
      * @param numBytes number of bytes to send
      */
     public final void putPacket(final byte[] data, int startOffset, int numBytes)
     {
         // Check Input
         if(null == data)
         {
             log.error("No data for Packet");
             return;
         }
         if(data.length < (startOffset + numBytes))
         {
             log.error("invalid range definition !");
             return;
         }
         if(MIN_SIZE > numBytes)
         {
             log.error("to little Data for Packet");
             return;
         }
         if(MAX_SIZE < numBytes)
         {
             // Data is too big ! -> Split up into several Packets
             int sendData = 0;
             while(sendData < data.length)
             {
                 int packetBytes = 0;
                 if(sendData + MAX_SIZE < data.length)
                 {
                     packetBytes = MAX_SIZE;
                 }
                 else
                 {
                     // last Packet
                     packetBytes = data.length - sendData;
                 }
                 // send chunk
                 putPacket(data, sendData, packetBytes);
                 sendData = sendData + packetBytes;
             }
             return;
         }
         synchronized (this)
         {
             // Put Packet in Send Queue
             // In Established we can send all kinds of Packets
             // If not Established then we do not send Server Packets, but everything else
             if((eState.ESTABLISHED == State) || (ServerPacket.TYPE != data[0]))
             {
                 log.info("sending the Bytes : " + Tool.fromByteBufferToHexString(data));
                 sendPacket(data);
                 // reset the Idle Timeout
                 msUnitTimeOut = IDLE_TIMEOUT;
             }
             else
             {
                 log.info("Packet added to delay queue !");
                 log.info("Pakets Bytes are: " + Tool.fromByteBufferToHexString(data) + " !");
                 delayQueue.add(data);
                 // Packet is in Queue and will be processed as soon as the Connection is established
                 if((eState.NEW_AND_WAITING == State) || (eState.CLOSED == State))
                 {
                     startConnect();
                 }
             }
         }
         log.info("Added Packet to send Queue");
     }
 
     public final synchronized void setChannelStateToClosed()
     {
         log.info("Closing Channel !!!");
         if(null != socketChannel)
         {
             try
             {
                 socketChannel.close();
             }
             catch(final IOException e)
             {
                 // I don't care
             }
             socketChannel = null;
         }
         State = eState.CLOSED;
     }
 
     private synchronized void sendPacket(final byte[] data)
     {
         log.info("sendingPacket");
         // Put Packet in Send Queue
         log.info("sending privately the Bytes : " + Tool.fromByteBufferToHexString(data));
         queue.add(data);
         pt.registerWrite(socketChannel);
     }
 
     private final synchronized void handleTimeOut()
     {
         if(0 < msUnitTimeOut)
         {
             msUnitTimeOut--;
             if(1 > msUnitTimeOut)
             {
                 log.info("Timeout in state {}!", State);
                 switch(State)
                 {
                 case NEW_AND_WAITING:
                     // open connection to node
                     startConnect();
                     break;
 
                 case CLOSED:
                     // Timeout was started in some other state but then the connection got closed
                     break;
 
                 case CONNECTING:
                     // Connection could not be finished
                     log.error("Could not create connection to {}:{}", host, port);
                     setChannelStateToClosed();
                     break;
 
                 case SENDED_HELLO:
                     log.error("Got no Hello from {}:{}", host, port);
                     setChannelStateToClosed();
                     break;
 
                 case SENDED_SERVER_INFO_REQUEST:
                     log.error("Got no Server Information from {}:{}", host, port);
                     setChannelStateToClosed();
                     break;
 
                 case ESTABLISHED:
                     // Connection was idle to long - close it down until we need it again
                     setChannelStateToClosed();
                     break;
 
                 default:
                     log.error("Timeout in unexpected state " + State + " !");
                     setChannelStateToClosed();
                     break;
                 }
             }
             // else wait for timeout
         }
         // else 0 = timeout is deactivated
     }
 
     /** this is called to bring up the communication channel to another node.
      *
      */
     public final synchronized void startConnect()
     {
         if((eState.NEW_AND_WAITING != State) && (eState.CLOSED != State))
         {
             log.error("Starting to connect in invalid state of " + State);
             setChannelStateToClosed();
         }
         log.info(" Connect to location {}:{}", host, port);
         // Create a new non-blocking socket channel
         try
         {
             socketChannel = SocketChannel.open();
             socketChannel.configureBlocking(false);
             // Bind the server socket to the specified address and port
             final InetSocketAddress isa = new InetSocketAddress(host, port);
             pt.registerChannel(socketChannel, this);
             socketChannel.connect(isa);
             State = eState.CONNECTING;
             // TimeOut
             msUnitTimeOut = CONNECT_TIMEOUT;
         }
         catch (final IOException e)
         {
             log.error(Tool.fromExceptionToString(e));
             setChannelStateToClosed();
         }
     }
 
     public final synchronized void handleFinishedConnection(final SelectionKey key)
     {
         log.info("Finish Connection !");
         if(eState.CONNECTING == State)
         {
             // Finish the Outgoing connection. If the connection operation failed
             // this will raise an IOException.
             try
             {
                 socketChannel.finishConnect();
             }
             catch(final ClosedByInterruptException e)
             {
                 // Cancel the channel's registration with our selector
                 log.error(Tool.fromExceptionToString(e));
                 log.error("Could not connect to {}:{} !", host, port);
                 setChannelStateToClosed();
                 return;
             }
             catch (final IOException e)
             {
                 // Cancel the channel's registration with our selector
                 log.error(Tool.fromExceptionToString(e));
                 log.error("Could not connect to {}:{} !", host, port);
                 setChannelStateToClosed();
                 return;
             }
 
             // send the hello Packet
             log.info("Sending Hello Packet !");
             HelloPacket hp = new HelloPacket(ownNode);
             sendPacket(hp.toByteArray());
             State = eState.SENDED_HELLO;
             // TimeOut
             msUnitTimeOut = HELLO_TIMEOUT;
         }
         else
         {
             // Should not happen in this State !
             log.error("Finish Connect in State " + State);
             setChannelStateToClosed();
         }
     }
 
     public final synchronized void handleAccept(final SelectionKey key)
     {
         log.info("Handle Accept");
         // Send Hello Packet
         log.info("Sending Hello Packet !");
         HelloPacket hp = new HelloPacket(ownNode);
         sendPacket(hp.toByteArray());
         State = eState.SENDED_HELLO;
         // TimeOut
         msUnitTimeOut = HELLO_TIMEOUT;
     }
 
     public final synchronized void handleWrite(final SelectionKey key)
     {
         log.info("Handle Write");
         // Write until there's not more data ...
         // ... or the socket's buffer fills up
         while(false == queue.isEmpty())
         {
             // get the data to write
             writeBuffer.clear();
             final byte[] buf = queue.get(0);
             // log.info("Writing : " + Tool.fromByteBufferToHexString(buf));
             queue.remove(0);
             // wrap data in Packet
             writeBuffer.put(MAGIC);
             writeBuffer.put(VERSION);
             // Length in 16 Bit high byte first
             writeBuffer.put((byte) (0xff & (buf.length/256)%256));
             writeBuffer.put((byte) (0xff & buf.length%256));
             writeBuffer.put(buf);
             // write the data
             try
             {
                 log.debug("writing");
                 writeBuffer.flip();
                 socketChannel.write(writeBuffer);
             }
             catch(final IOException e)
             {
                 log.error(Tool.fromExceptionToString(e));
             }
             // clean up
             if(writeBuffer.remaining() > 0)
             {
                 // Store the not send bytes for next time
                 final int remainderSize = writeBuffer.remaining();
                 final byte[] remainder = new byte[remainderSize];
                 writeBuffer.get(remainder);
                 queue.add(0, remainder);
                 return;
             }
         }
 
         if(true == queue.isEmpty())
         {
               // We wrote away all data, so we're no longer interested
               // in writing on this socket. Switch back to waiting for
               // data.
               key.interestOps(SelectionKey.OP_READ);
               log.info("Switching from Writing to Reading");
         }
     }
 
     public final synchronized void handleRead(final SelectionKey key)
     {
         log.info("Handle Read");
         // Clear out our read buffer so it's ready for new data
         readBuffer.clear();
         // Attempt to read off the channel
         int numRead;
         try
         {
             numRead = socketChannel.read(readBuffer);
             log.info("read {} bytes", numRead);
         }
         catch (final IOException e)
         {
             // The remote forcedly closed the connection, cancel
             // the selection key and close the channel.
             log.error("The remote forcedly closed the connection");
             key.cancel();
             setChannelStateToClosed();
             return;
         }
         if (numRead == -1)
         {
             // Remote entity shut the socket down cleanly. Do the
             // same from our end and cancel the channel.
             log.info("Remote entity shut the socket down cleanly");
             key.cancel();
             setChannelStateToClosed();
             return;
         }
         byte[] bytes = readBuffer.array();
         log.info("Received the bytes: " + Tool.fromByteBufferToHexString(bytes, numRead));
         bc.add(bytes, numRead);
         checkIfWeHaveACompletePacketInTheBuffer(key);
     }
 
     private synchronized void checkIfWeHaveACompletePacketInTheBuffer(final SelectionKey key)
     {
         // Check if we have a complete Packet in the buffer
         boolean mightHaveAnotherPacket = true;
         while(true == mightHaveAnotherPacket) // There might be two or more packets in there already
         {
             if(ENVELOPE >= bc.size())
             {
                 // not enough bytes for another packet
                 // at least Envelope + one data byte is needed
                 log.info("not enough bytes for another packet");
                 mightHaveAnotherPacket = false;
             }
             else
             {
                 // Enough Bytes to be a complete packet
                 if(MAGIC == bc.readByte(0))
                 {
                     log.info("Magic ok !");
                     // Is a valid Friends United Packet
                     final int lengthOfPacket = ((0xff & bc.readByte(2)) * 256) + (0xff & bc.readByte(3));
                     log.info("length of Packet : {} bytes", lengthOfPacket);
                     if(lengthOfPacket < 0)
                     {
                         log.error("Packet Protocol Error (Invalid Length)closing connection !");
                         key.cancel();
                         setChannelStateToClosed();
                         mightHaveAnotherPacket = false;
                     }
                     else
                     {
                         if(bc.size() >= lengthOfPacket + ENVELOPE)
                         {
                             if(false == removePacketFromBuffer(bc, lengthOfPacket) )
                             {
                                 log.error("Packet Error closing connection !");
                                 key.cancel();
                                 setChannelStateToClosed();
                                 mightHaveAnotherPacket = false;
                             }
                             // there might be another packet still in the buffer, ...
                         }
                         else
                         {
                             log.info("Packet is incomplete");
                             // This packet isn't completed so no further packets in Buffer
                             mightHaveAnotherPacket = false;
                         }
                     }
                 }
                 else
                 {
                     log.error("Recieve Magic Error ! Close Channel");
                     log.error(Tool.fromByteBufferToHexString(bc.toByteArray()));
                     // drop bytes until Magic found ? - NO !
                     // Remote entity is not a Friends United Entity
                     // Close the channel !
                     key.cancel();
                     setChannelStateToClosed();
                     mightHaveAnotherPacket = false;
                     return;
                 }
             }
         }
     }
 
     /** remove Packet Bytes out of the Queue.
      *
      * @param pbc Queue
      * @param lengthOfPacket number of bytes that the packet consists of.
      * @return true = ok; false = error in packet communication -> close connection!
      */
     private final synchronized boolean removePacketFromBuffer(final ByteConverter pbc, final int lengthOfPacket)
     {
         // The packet is complete the buffer might have the start of the next packet
         log.info("Received a Packet");
         byte[] PacketsBytes;
         PacketsBytes = pbc.getByteArray(lengthOfPacket, ENVELOPE);
         if(null == PacketsBytes)
         {
             log.error("Packet not completely recieved !");
             return false;
         }
         else
         {
             if(PacketsBytes.length < 1)
             {
                 log.error("Problem with retrieving Packet data !");
                 return false;
             }
             System.out.println("Received the bytes: " + Tool.fromByteBufferToHexString(PacketsBytes));
         }
         pbc.removeBytes(0, lengthOfPacket + ENVELOPE);
 
         if(eState.ESTABLISHED == State)
         {
             // reset the Idle Timeout
             msUnitTimeOut = IDLE_TIMEOUT;
             // Handle Frame
             switch(PacketsBytes[0])
             {
             case ServerRequestPacket.TYPE:
                 return receiveServerRequest(PacketsBytes);
             case ServerListPacket.TYPE:
                 return receiveServerInfo(PacketsBytes);
             case ServerPacket.TYPE:
                 return receiveServerPacket(PacketsBytes);
             default:
                 log.error("Invalid Packet Type dropped Packet ! (" + PacketsBytes[0] + ",ESTABLISHED)");
                 return false;
             }
         }
         else
         {
             // Handle Frame
             switch(PacketsBytes[0])
             {
             case HelloPacket.TYPE:
                 return receiveHelloPacket(PacketsBytes);
             case ServerRequestPacket.TYPE:
                 return receiveServerRequest(PacketsBytes);
             case ServerListPacket.TYPE:
                 return receiveServerInfo(PacketsBytes);
             case ServerPacket.TYPE:
             default:
                 // not allowed !
                 log.error("Invalid Packet Type dropped Packet ! (" + PacketsBytes[0] + "," + State + ")");
                 return false;
             }
         }
     }
 
     /**
      *
      * @return true = ok; false = error in packet communication -> close connection!
      */
     private synchronized boolean receiveHelloPacket(final byte[] bytes)
     {
         HelloPacket hPacket = new HelloPacket(bytes);
 
         if(true == hPacket.isValid())
         {
             NodeId receivedNodeId = hPacket.getPacketsNodeId();
             if(null == remNode)
             {
                 // This is an incoming connection
                 remNode = receivedNodeId;
                 // Check if we already have a connection to that Node
                 RemoteNode con = pktHndl.getRemoteNodeFor(remNode);
                 if(null == con)
                 {
                     // new Node
                     pktHndl.addRemoteNode(remNode, this);
                 }
                 else
                 {
                     // we already have a connection
                     // Close the channel !
                     return false;
                 }
             }
             else
             {
                 if(false == remNode.equals(receivedNodeId))
                 {
                     log.error("Received Node ID({}) is not same as announced Node ID({}) !", receivedNodeId, remNode);
                 }
             }
             log.info("Recieved Hello Packet !");
             // Ask new Node about his Servers
             ServerRequestPacket srp = new ServerRequestPacket(0, MAX_SERVERS);
             sendPacket(srp.toByteArray());
             State = eState.SENDED_SERVER_INFO_REQUEST;
             // TimeOut
             msUnitTimeOut = SERVER_INFO_TIMEOUT;
             return true;
         }
         else
         {
             log.error("Recieved invalid Hello Packet !");
             return false;
         }
     }
 
     private synchronized boolean receiveServerRequest(final byte[] data)
     {
         log.info("Receiving Server Request");
         if(null == remNode)
         {
             log.error("Do not know where that Server Request came from ! No Hello Packet ?");
             return false;
         }
         ServerRequestPacket request = new ServerRequestPacket(data);
         if(true == request.isValid())
         {
             log.info("Recieved valid Server Request Packet !");
             pktHndl.processServerRequestFrom(remNode, request);
             return true;
         }
         else
         {
             log.error("Recieved invalid Server Request Packet !");
             return false;
         }
     }
 
     private synchronized boolean receiveServerInfo(final byte[] data)
     {
         log.info("Receive Server Info");
         ServerListPacket reply = new ServerListPacket(data);
         if(true == reply.isValid())
         {
             pktHndl.processServerInfoFrom(remNode, reply);
 
             log.info("Connection has been established");
             State = eState.ESTABLISHED;
 
             queue.addAll(delayQueue);
             delayQueue.clear();
             pt.registerWrite(socketChannel);
 
             // TimeOut
             msUnitTimeOut = IDLE_TIMEOUT;
             return true;
         }
         else
         {
             log.error("Recieved invalid Server Info Packet !");
             return false;
         }
     }
 
     private synchronized boolean receiveServerPacket(final byte[] data)
     {
         log.info("Receive Server Service Packet");
         ServerPacket sp = new ServerPacket(data);
         if(true == sp.isValid())
         {
             FriendPacket fp = sp.getFriendPacket();
             pktHndl.sendPacketIntoNetwork(new NodePacket(ownNode, fp));
             return true;
         }
         else
         {
             log.error("Received Invalid Server Packet !");
             return false;
         }
     }
 
 }
