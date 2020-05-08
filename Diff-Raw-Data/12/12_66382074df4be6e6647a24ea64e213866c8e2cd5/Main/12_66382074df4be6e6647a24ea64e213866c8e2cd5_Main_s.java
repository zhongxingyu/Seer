 package org.runetekk;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Main.java
  * @version 1.0.0
  * @author RuneTekk Development (SiniSoul)
  */
 public final class Main implements Runnable {
     
     /**
      * The {@link Logger} for this class.
      */
     private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
     
     /**
      * The maximum amount of players allowed to connect to this server.
      */
     private static final int MAXIMUM_CLIENTS = 2048;
     
     /**
      * The size of all the incoming packets from the client.
      */
     private static final int[] INCOMING_SIZES;
     
     /**
      * The private key for deciphering the enciphered login block.
      */
     private static final BigInteger PRIVATE_KEY;
     
     /**
      * The modulus for deciphering the enciphered login block.
      */
     private static final BigInteger MODULUS;
     
     /**
      * The main handler.
      */
     private static Main main;
     
     /**
      * The local thread.
      */
     private Thread thread;
     
     /**
      * The local thread is currently paused.
      */
     private boolean isPaused;
     
     /**
      * The {@link ServerSocket} to accept connections from.
      */
     private ServerSocket serverSocket;
     
     /**
      * The {@link Client} array.
      */
     Client[] clientArray;
     
     /**
      * The list of clients currently connected to the server.
      */
     ListNode activeList;
     
     /**
      * The list of client ids removed from the server.
      */
     ListNode removedList;
     
     /**
      * The offset in the client list.
      */
     int listOffset;
     
     /**
      * The {@link IoWriter} for this handler.
      */
     IoWriter writer;
     
     /**
      * Prints the application tag.
      */
     private static void printTag() {
         System.out.println(""
         + "                     _____               _______   _    _                           "
         + "\n                    |  __ \\             |__   __| | |  | |                       "
         + "\n                    | |__) |   _ _ __   ___| | ___| | _| | __                     "
         + "\n                    |  _  / | | | '_ \\ / _ \\ |/ _ \\ |/ / |/ /                  "
         + "\n                    | | \\ \\ |_| | | | |  __/ |  __/   <|   <                    "
         + "\n                    |_|  \\_\\__,_|_| |_|\\___|_|\\___|_|\\_\\_|\\_\\             "
         + "\n----------------------------------------------------------------------------------"
         + "\n                                Game Server 1.0.3                               "
         + "\n                                 See RuneTekk.com                                 "
         + "\n                               Created by SiniSoul                                "
         + "\n----------------------------------------------------------------------------------");
     }
     
     /**
      * Initializes the local thread.
      */
     private void initialize() {
         writer = new IoWriter(this);
         thread = new Thread(this);
         thread.start();
     }
     
      @Override
     public void run() {
         for(;;) {
             synchronized(this) {
                 if(isPaused)
                     break;
                 Client acceptedClient = null;
                 try {
                      Socket socket = serverSocket.accept();
                      acceptedClient = new Client(socket);
                 } catch(IOException ex) {
                     if(!(ex instanceof SocketTimeoutException))
                         destroy();
                 }     
                 if(acceptedClient != null) {
                     int position = listOffset;
                     if(position >= MAXIMUM_CLIENTS) {
                         position = -1;
                         synchronized(removedList) {
                             ListNode node = removedList.childNode;
                             if(node != null) {
                                 position = ((IntegerNode) node).value;
                                 if(node.parentNode != null) {
                                     node.childNode.parentNode = node.parentNode;
                                     node.parentNode.childNode = node.childNode;
                                     node.childNode = null;
                                     node.parentNode = null;
                                 }
                             }   
                         }
                     } else
                         ++listOffset;
                     try {
                         byte[] response = new byte[9];
                         if(position < 0) {
                             response[8] = 7;
                             acceptedClient.outputStream.write(response);
                             acceptedClient.destroy();
                         } else {
                             IntegerNode positionNode = new IntegerNode(position);
                             acceptedClient.localId = positionNode;                          
                             if(acceptedClient.inputStream.read() != 14) {
                                 response[8] = 20;
                                 acceptedClient.outputStream.write(response);
                                 LOGGER.log(Level.WARNING, "Client disconnected : Invalid op!");
                                 throw new IOException();
                             }  
                             if((acceptedClient.nameHash = acceptedClient.inputStream.read()) < 0 || acceptedClient.nameHash > 31) {
                                 response[8] = 10;
                                 acceptedClient.outputStream.write(response);
                                 LOGGER.log(Level.WARNING, "Client disconnected : Invalid name hash - {0}!", acceptedClient.nameHash);
                                 throw new IOException();
                             } 
                             long sessionKey = UUID.randomUUID().getMostSignificantBits();
                             byte[] oldResponse = response;
                             response = new byte[17];
                             System.arraycopy(oldResponse, 0, response, 0, oldResponse.length);
                             response[9] =  (byte) (sessionKey >>> 56L);
                             response[10] = (byte) (sessionKey >>> 48L);
                             response[11] = (byte) (sessionKey >>> 40L);
                             response[12] = (byte) (sessionKey >>> 32L);
                             response[13] = (byte) (sessionKey >>> 24L);
                             response[14] = (byte) (sessionKey >>> 16L);
                             response[15] = (byte) (sessionKey >>> 8L);
                             response[16] = (byte) (sessionKey & 0xFFL);
                             positionNode.parentNode = activeList.parentNode;
                             positionNode.childNode = activeList;
                             positionNode.parentNode.childNode = positionNode;
                             positionNode.childNode.parentNode = positionNode;
                             acceptedClient.sessionKey = sessionKey;
                             acceptedClient.incomingBuffer = new byte[Client.BUFFER_SIZE];
                             acceptedClient.outputStream.write(response);
                             clientArray[position] = acceptedClient;
                             acceptedClient.timeoutStamp = System.currentTimeMillis() + 5000L;
                         }
                     } catch(Exception ex) {
                         acceptedClient.destroy();
                     }
                 }
                 ListNode node = activeList;
                 while((node = node.childNode) != null) { 
                     if(!(node instanceof IntegerNode))
                         break;
                     IntegerNode position = (IntegerNode) node;
                     Client client = clientArray[position.value];
                     if(client == null) {
                         LOGGER.log(Level.WARNING, "Null client id, removed from active list!");
                         removeClient(position);
                         continue;
                     }
                     if(client.timeoutStamp > -1L && client.timeoutStamp < System.currentTimeMillis()) {
                         LOGGER.log(Level.WARNING, "Client disconnected : timeout reached!");
                         removeClient(position);
                         client.destroy();
                         continue;
                     }
                     try {
                         int avail = client.inputStream.available();
                         if(avail > 0) {
                             if((client.iWritePosition < client.iReadPosition ? 
                                 client.iReadPosition - client.iWritePosition : 
                                 Client.BUFFER_SIZE - client.iWritePosition - client.iReadPosition) < avail) {
                                 LOGGER.log(Level.WARNING, "Client disconnected : ib overflow!");
                                 removeClient(position);
                                 client.destroy();
                                 continue;
                             }
                             client.inputStream.read(client.incomingBuffer, client.iWritePosition, avail);
                             client.iWritePosition = (client.iWritePosition + avail) % Client.BUFFER_SIZE;
                         }
                     } catch(IOException ex) {
                         LOGGER.log(Level.WARNING, "Client disconnected : exception caught while reading data!");
                         removeClient(position);
                         client.destroy();
                         continue;
                     }
                     try {
                         switch(client.state) {
                             case 0:
                                 if(client.iWritePosition != client.iReadPosition) {
                                     int opcode = client.incomingBuffer[client.iReadPosition];
                                     if(opcode != 18 && opcode != 16) {
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid login op!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }
                                     client.isReconnecting = opcode == 18;
                                     int size = client.incomingBuffer[client.iReadPosition + 1] & 0xFF;
                                     if((client.iWritePosition < client.iReadPosition ? 
                                         client.iReadPosition - client.iWritePosition : 
                                         Client.BUFFER_SIZE - client.iWritePosition - client.iReadPosition) < size)
                                         continue;
                                     client.iReadPosition += 2;
                                     ByteBuffer buffer = new ByteBuffer(size);
                                     if(client.iReadPosition < client.iWritePosition) {
                                         System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, size);
                                     } else {
                                         System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, Client.BUFFER_SIZE - client.iReadPosition);
                                         System.arraycopy(client.incomingBuffer, 0, buffer.payload, 0, client.iWritePosition);
                                     }
                                     client.iReadPosition += size;
                                     if(buffer.getUbyte() != 255) {
                                         client.outputStream.write(10);
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid initop!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }   
                                     if(buffer.getUword() != 317) {
                                         client.outputStream.write(6);
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid rev!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }
                                     client.isLowMemory = buffer.getUbyte() == 1;
                                     /* CRC CHECKS OF THE DOWNLOADED ARCHIVES */
                                     for(int i = 0; i < 9; i++)
                                         buffer.getDword();
                                     int encipheredBlockSize = buffer.getUbyte();
                                     byte[] encipheredData = new byte[encipheredBlockSize];
                                     System.arraycopy(buffer.payload, buffer.offset, encipheredData, 0, encipheredBlockSize);
                                     BigInteger encipheredBlock = new BigInteger(encipheredData);
                                     buffer.payload = encipheredBlock.modPow(PRIVATE_KEY, MODULUS).toByteArray();
                                     buffer.offset = 0;
                                     if(buffer.getUbyte() != 10) {
                                         client.outputStream.write(10);
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid rsachk!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }
                                     int[] seeds = new int[4];
                                     for(int i = 0; i < 4; i++)
                                         seeds[i] = buffer.getDword();
                                     client.uid = buffer.getDword();
                                     client.username = buffer.getString();
                                     client.password = buffer.getString();
                                     client.timeoutStamp = -1L;
                                     /* REDESIGN BIT */
                                     byte[] response = new byte[3];
                                     response[0] = (byte) 2;
                                     response[1] = (byte) client.rights;
                                     client.outputStream.write(response);     
                                     client.outgoingBuffer = new byte[Client.BUFFER_SIZE];
                                     client.incomingCipher = new IsaacCipher(seeds);
                                     for(int i = 0; i < seeds.length; i++)
                                         seeds[i] += 50;
                                     client.outgoingCipher = new IsaacCipher(seeds);
                                     client.state = 1;
                                 }
                                 break;
                              
                             case 1:
                             case 2:
                                 if(client.lastRecievedPing + 15000L < System.currentTimeMillis())
                                     client.timeoutStamp = System.currentTimeMillis() + 60000L;
                                 while(client.iReadPosition != client.iWritePosition) {
                                     int opcode = client.incomingBuffer[client.iReadPosition] - client.incomingCipher.getNextValue() & 0xFF;
                                     int size = INCOMING_SIZES[opcode];
                                     if(size < -2) {
                                         LOGGER.log(Level.WARNING, "Client disconnected : unknown packet!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }
                                     int avail = client.iWritePosition < client.iReadPosition ? 
                                                  client.iReadPosition - client.iWritePosition : 
                                                  Client.BUFFER_SIZE - client.iWritePosition - client.iReadPosition;
                                     int offAmt = 1;
                                     if(size == -2)
                                         if(avail < 2)
                                             break;
                                         else {
                                             size = ((client.incomingBuffer[client.iReadPosition + 1] & 0xFF) << 8) | 
                                                     (client.incomingBuffer[client.iReadPosition + 2] & 0xFF);
                                             avail -= 2;
                                             offAmt += 2;
                                         }
                                     if(size == -1)
                                         if(avail < 1)
                                             break;
                                         else {
                                             size = client.incomingBuffer[client.iReadPosition + 1] & 0xFF;
                                             avail -= 1;
                                             offAmt += 1;
                                         }
                                     if(avail < size)
                                         break;
                                     client.iReadPosition = (client.iReadPosition + offAmt) % Client.BUFFER_SIZE;
                                     ByteBuffer buffer = null;
                                     if(size > 0) {
                                         buffer = new ByteBuffer(size);
                                         if(client.iReadPosition < client.iWritePosition) {
                                             System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, size);
                                         } else {
                                             System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, Client.BUFFER_SIZE - client.iReadPosition);
                                             System.arraycopy(client.incomingBuffer, 0, buffer.payload, 0, client.iWritePosition);
                                         }
                                         client.iReadPosition = (client.iReadPosition + size) % Client.BUFFER_SIZE;
                                     }
                                     switch(opcode) {
                                         
                                         /* Ping */
                                         case 0:
                                             client.lastRecievedPing = System.currentTimeMillis();
                                             client.timeoutStamp = -1L;
                                             break;
                                     }
                                 }
                                 break;
                         }
                     } catch(Exception ex) {
                         LOGGER.log(Level.WARNING, "Client disconnected : ", ex);
                         removeClient(position);
                         client.destroy();
                         continue;
                     }
                 }
                 /* PUT SERVER UPDATE PROCEDURES HERE 
                  * Includes anything or any data that may
                  * be sent out to the clients in the
                  * client write block.
                  */
                 
                 node = activeList;
                 while((node = node.childNode) != null) { 
                    if(!(node instanceof IntegerNode))
                        break;
                    IntegerNode position = (IntegerNode) node;
                    Client client = clientArray[position.value];
                    if(client == null) {
                        LOGGER.log(Level.WARNING, "Null client id, removed from active list!");
                        removeClient(position);
                        continue;
                    }
                    if(client.playerMob == null) {
                        LOGGER.log(Level.WARNING, "Client disconnected : Null player mob!");
                        removeClient(position);
                        client.destroy();
                        continue;
                    }
                    try {
                        switch(client.state) {
                            
                            case 1:
                                Client.sendMessage(client, "Welcome to Runescape.");
                                Client.sendCurrentChunk(client);
                                client.state = 2;
                                break;
                                
                            case 2:
                                if(client.oWritten) {                                 
                                    client.oWritten = false;
                                }
                                break;
                        }
                    } catch(Exception ex) {
                        LOGGER.log(Level.WARNING, "Client disconnected : ", ex);
                        removeClient(position);
                        client.destroy();
                        continue;
                    }           
                 }
             }
         }
      }
      
      /**
       * Removes a client from this handler.
       * @param client The client to remove.
       */
      void removeClient(IntegerNode client) {
          synchronized(removedList) {             
              if(client.parentNode != null) {
                  client.childNode.parentNode = client.parentNode;
                  client.parentNode.childNode = client.childNode;
                  client.childNode = null;
                  client.parentNode = null;
              }
              client.parentNode = removedList.parentNode;
              client.childNode = removedList;
              client.parentNode.childNode = client;
              client.childNode.parentNode = client;
          }
      }
        
     /**
      * Destroys this local application.
      */
     private void destroy() {
         if(!isPaused)  {
             if(thread != null) {
                 synchronized(this) {
                     isPaused = true;
                     notifyAll();
                 }
                 try {
                     thread.join();
                 } catch(InterruptedException ex) {
                 }
             }
             activeList = null;
             removedList = null;
             clientArray = null;
             thread = null;
         }
     }
     
     /**
      * The starting point for this application.
      * @param args The command line arguments.
      */
     public static void main(String[] args) {
        args = args.length == 0 ? new String[] { "server", "1" } : args;
         printTag();
         if(args[0].equals("setup")) {
             
         } else if(args[0].equals("server")) {
             int portOff = -1;
             try {
                 portOff = Integer.parseInt(args[1]);
             } catch(Exception ex) {
                 LOGGER.log(Level.SEVERE, "Exception thrown while parsing the port offset : {0}", ex);
             }
             main = new Main(portOff);
         }
     }  
     
     /**
      * Prevent external construction;
      * @param portOff The port offset to initialize this server on.
      */
     private Main(int portOff) {
         try {
             activeList = new ListNode();
             activeList.parentNode = activeList;
             activeList.childNode = activeList;
             removedList = new ListNode();
             removedList.parentNode = removedList;
             removedList.childNode = removedList;
             clientArray = new Client[MAXIMUM_CLIENTS];
             serverSocket = new ServerSocket();
             serverSocket.setSoTimeout(5);
             serverSocket.bind(new InetSocketAddress(43594 + portOff));
             initialize();
         } catch(Exception ex) {
             LOGGER.log(Level.SEVERE, "Exception thrown while initializing : {0}", ex);
             throw new RuntimeException();
         }
     }
     
     static {
         INCOMING_SIZES = new int[] {
              0, -3, -3,  1, -1, -3, -3, -3, -3, -3,
             -3, -3, -3, -3,  8, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
              4, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3,
         };
         
         PRIVATE_KEY = new BigInteger("834770591012857827640080639045432158672036"
                                    + "332921897115808929595140145146005194253762"
                                    + "779032390472037351677263434664687344175834"
                                    + "243081727930336566362994680698738590975776"
                                    + "850294050839976416113702926128859739698260"
                                    + "975713377976273616378038375714231420479829"
                                    + "941906054307584484688808996961472506323292"
                                    + "27683313213345");
         MODULUS = new BigInteger("9411057631461099471899808167811272170730276809"
                                + "7953573382594472711586892647324048387895220267"
                                + "2785486684044464558549845428941919231900626219"
                                + "1300411087109062176691151141707764775553043533"
                                + "4378014024591274020906279816717181801750245525"
                                + "6847336335570778443716610104475663472453168488"
                                + "50605330431469711797488557035631");
     }
 }
