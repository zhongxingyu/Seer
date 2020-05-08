 package PeerConnection;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: admin
  * Date: 8/2/13
  * Time: 4:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PeerConnection {
     boolean hosting;
     ArrayList<PeerConnection> activeConnections;
     final Queue<PeerAction> queuedActions;
 
     String peerIPAddress;
     int peerPort;
     int hostPort;
 
     boolean hostChokingPeer = true;
     boolean peerChokingHost = true;
     boolean hostInterestedPeer = false;
     boolean peerInterestedHost = false;
 
 
     boolean closed;
 
 
     public PeerConnection(String peerIPAddress, int peerPort, ArrayList<PeerConnection> activeConnections) {
         hostPort = 0;
 
         this.peerPort = peerPort;
         this.peerIPAddress = peerIPAddress;
         this.activeConnections = activeConnections;
 
         hosting = false;
         queuedActions = new LinkedList<PeerAction>();
     }
 
     public PeerConnection(int hostPort, ArrayList<PeerConnection> activeConnections) {
         this.hostPort = hostPort;
 
         this.peerPort = 0;
         this.peerIPAddress = "";
         this.activeConnections = activeConnections;
 
         hosting = true;
         queuedActions = new LinkedList<PeerAction>();
     }
 
     public void run() {
         SocketChannel socketChannel = connectToPeer();
         if (socketChannel == null) {
             return;
         }
         //if(handshakeWithPeer(socketChannel))
 
         DataReader reader = new DataReader();
         DataWriter writer = new DataWriter();
 
         boolean readInLength = false;
         int messageLength;
         byte[] messageData = null;
 
         reader.setBufferLength(4);
         while (!closed) {
 
             PeerAction receivedAction;
 
             //code for parsing the read data from the peer and acting upon it
             if (messageData != null) {
                 int index;
 
                 byte[] messageIDBytes = subarray(messageData,0,1);
                 byte messageID = messageIDBytes[0];
                 byte[] payload = subarray(messageData,1,messageData.length);
                 switch (messageID) {
                     case 0://choke
                         peerChokingHost = true;
                         break;
                     case 1://unchoke
                         peerChokingHost = false;
                         break;
                     case 2://interested
                         peerInterestedHost = true;
                         break;
                     case 3://uninterested
                         peerInterestedHost = false;
                         break;
                     case 4://have
                         index = bytesToInt(payload);
                         break;
                     case 5://bitfield
 
                         break;
                     case 6://request
                         if(!hostChokingPeer && peerInterestedHost) {
 
                         }
 
                         break;
                     case 7://piece
                         //break up the payload
                         byte[] indexBytes = subarray(payload, 0, 4);
                         byte[] beginBytes = subarray(payload, 4, 8);
                         byte[] blockBytes = subarray(payload, 8, payload.length);
 
                         index = bytesToInt(indexBytes);
                         int begin = bytesToInt(beginBytes);
 
                         //write data to file
 
                         //tell all peers to broadcast have message
 
                         break;
                 }
                 messageData = null;
             }
 
             //code for reading data in from the peer
             if (!readInLength) {
                 //if we haven't read in the length bytes already, then read the length (first 4 bytes)
                 try {
                     if (reader.read(socketChannel)) {
                         messageLength = reader.getInt();
                         readInLength = true;
                         reader.setBufferLength(messageLength);
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             } else {
                 //if we've read in the length bytes already, then read the rest of the message
                 try {
                     if (reader.read(socketChannel)) {
                         messageData = reader.getBytes();
                         readInLength = false;
                         reader.setBufferLength(4);
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private boolean handshakeWithPeer(SocketChannel socketChannel, byte[] sha1Hash, byte[] myPeerID, byte[] peerPeerID, BitSet myBitfield) throws IOException {
         byte[] byte19 = new byte[]{19};
         byte[] bitTorrentHeader = "BitTorrent Protocol".getBytes();
         byte[] reservedBytes = new byte[8];
         Arrays.fill(reservedBytes, (byte) 0);
         byte[] handshakeMessage = concat(byte19, bitTorrentHeader, reservedBytes, sha1Hash, myPeerID);
 
         int bitfieldLength = (myBitfield.length() + 7) / 8 + 1;
         byte[] bitfieldLengthBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(bitfieldLength).array();
         byte[] bitfieldID = new byte[]{5};
         byte[] bitfieldBytes = myBitfield.toByteArray();
         byte[] bitfieldMessage = concat(bitfieldLengthBytes, bitfieldID, bitfieldBytes);
 
         ByteBuffer buffer;
 
         if (hosting) {
             //wait for handshake, then send out our handshake and bitfield
 
             //wait for handshake
             buffer = ByteBuffer.allocate(68);
             while (!closed && buffer.hasRemaining()) {
                 socketChannel.read(buffer);
             }
             buffer.flip();
             byte[] handshakeFromPeer = new byte[68];
             buffer.put(handshakeFromPeer);
             if (!verifyHandshake(handshakeFromPeer, sha1Hash, peerPeerID)) {
                 return false;
             }
 
             //send out handshake
             buffer.clear();
             buffer.put(handshakeMessage);
             buffer.flip();
             while (!closed && buffer.hasRemaining()) {
                 socketChannel.write(buffer);
             }
 
             //send out bitfield
             buffer = ByteBuffer.allocate(bitfieldLength + 4);
             buffer.clear();
             buffer.put(bitfieldMessage);
             buffer.flip();
             while (!closed && buffer.hasRemaining()) {
                 socketChannel.write(buffer);
             }
 
         } else {
             //send out our handshake, then wait for handshake, then send bitfield
 
             //send out handshake
             buffer = ByteBuffer.allocate(68);
             buffer.put(handshakeMessage);
             buffer.flip();
             while (!closed && buffer.hasRemaining()) {
                 socketChannel.write(buffer);
             }
 
             //wait for handshake
             buffer.clear();
             while (!closed && buffer.hasRemaining()) {
                 socketChannel.read(buffer);
             }
             buffer.flip();
             byte[] handshakeFromPeer = new byte[68];
             buffer.put(handshakeFromPeer);
             if (!verifyHandshake(handshakeFromPeer, sha1Hash, peerPeerID)) {
                 return false;
             }
 
             //send out bitfield
             buffer = ByteBuffer.allocate(bitfieldLength + 4);
             buffer.clear();
             buffer.put(bitfieldMessage);
             buffer.flip();
             while (!closed && buffer.hasRemaining()) {
                 socketChannel.write(buffer);
             }
         }
         return true;
     }
 
     private boolean verifyHandshake(byte[] handshakeFromPeer, byte[] sha1Hash, byte[] peerPeerID) {
         if (handshakeFromPeer.length != 68) {
             return false;
         }
         for (int i = 0; i < 20; i++) {
             if (handshakeFromPeer[i + 28] != sha1Hash[i]) {
                 return false;
             }
         }
         if (peerPeerID == null) {
             return true;
         }
         for (int i = 0; i < 20; i++) {
             if (peerPeerID[i] != handshakeFromPeer[i + 48]) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Helper function to connect to a peer, either from a server socket or from a socket.
      *
      * @return a SocketChannel object if able to connect, otherwise null
      */
     private SocketChannel connectToPeer() {
         ServerSocketChannel serverSocketChannel;
         SocketChannel socketChannel = null;
         if (hosting) {
 
             //bind a new server socket to the port that this connection uses
             try {
                 serverSocketChannel = ServerSocketChannel.open();
                 serverSocketChannel.configureBlocking(false);
                 serverSocketChannel.socket().bind(new InetSocketAddress(hostPort));
             } catch (IOException e) {
                 System.err.println("Warning: unable to create server socket for port " + hostPort + ".");
                 return socketChannel;
             }
 
             //accept a new connection until we close this connection or we get a remote connection
             try {
                 while (!closed && socketChannel == null) {
                     socketChannel = serverSocketChannel.accept();
                 }
                 if (socketChannel != null) {
                     socketChannel.configureBlocking(false);
                     peerIPAddress = socketChannel.getRemoteAddress().toString();
 
                 }
             } catch (IOException e) {
                 System.err.println("Warning: unable to connect server socket on port " + hostPort + " to a peer.");
             }
 
         } else {
             try {
                 //connect this socket channel to the address 
                 socketChannel = SocketChannel.open();
                 socketChannel.configureBlocking(false);
                 while (!closed && !socketChannel.finishConnect()) {
                     socketChannel.connect(new InetSocketAddress(peerIPAddress, peerPort));
                 }
             } catch (IOException e) {
                 System.err.println("Warning: unable to open or connect socket channel to " + peerIPAddress + ":" + peerPort + ".");
             }
         }
         return socketChannel;
     }
 
     /**
      * Helper function to concatenate a list of byte arrays
      *
      * @param args list of byte arrays to concatenate
      * @return the concatenated byte arrays
      * @throws IOException
      */
     private byte[] concat(byte[]... args) throws IOException {
         ByteArrayOutputStream builder = new ByteArrayOutputStream();
         for (byte[] arg : args) {
             builder.write(arg);
         }
         return builder.toByteArray();
     }
 
     private byte[] subarray(byte[] original, int from, int to) {
         byte[] newArray = new byte[to - from];
         System.arraycopy(original, from, newArray, from - from, to - from);
         return newArray;
     }
 
     private int bytesToInt(byte[] bytes) {
         ByteBuffer buffer = ByteBuffer.wrap(bytes);
         buffer.order(ByteOrder.BIG_ENDIAN);
         return buffer.getInt();
     }
 
     private byte[] intToBytes(int value) {
         ByteBuffer buffer = ByteBuffer.allocate(4);
         buffer.order(ByteOrder.BIG_ENDIAN);
         buffer.putInt(value);
         return buffer.array();
     }
 }
 
 
 class DataReader {
     private ByteBuffer data;
 
     public void setBufferLength(int length) {
         data = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
     }
 
     /**
      * Method to read data from a socket channel
      *
      * @param socketChannel socket connected to a peer
      * @return true if able to read all of the allocated length's worth of byte data or false if not
      * @throws IOException
      */
     public boolean read(SocketChannel socketChannel) throws IOException {
         socketChannel.read(data);
         if (data.hasRemaining()) {
             return false;
         }
         data.flip();
         return true;
     }
 
     public byte[] getBytes() {
         byte[] readData = new byte[data.position()];
         data.get(readData);
         return readData;
     }
 
     public int getInt() {
         return data.getInt();
     }
 }
 
 class DataWriter {
     Queue<byte[]> messageQueue;
     private ByteBuffer data;
    boolean finishedWriting = true;
 
     DataWriter() {
         messageQueue = new LinkedList<byte[]>();
     }
 
     public void putBytes(byte[] bytes) {
         messageQueue.add(bytes);
     }
 
    /**
     * method that writes inputted data into a socket channel
     * @param socketChannel the socket channel to write to
     * @return true if able to input all data, false if still in progress
     * @throws IOException
     */
     public boolean write(SocketChannel socketChannel) throws IOException {
        if(finishedWriting) {
            byte[] currentMessage = messageQueue.poll();
            data = ByteBuffer.wrap(currentMessage);
        }
         socketChannel.write(data);
        if(data.hasRemaining()) {
            finishedWriting = false;
            return false;
        }
        if(!messageQueue.isEmpty()) {
            finishedWriting = true;
             return false;
         }
        finishedWriting = true;
         return true;
     }
 }
