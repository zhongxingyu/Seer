 package org.runetekk;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 
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
      * The amount of time in milliseconds before the garbage should be taken out.
      */
     private static final long GC_TIME = 30000;
     
     /**
      * The main handler.
      */
     private static Main main;
     
     /**
      * The regions for this handler.
      */
     static Region[][] regions;
     
     /**
      * The {@link Client} array.
      */
     static Client[] clientArray;
     
     /**
      * The maximum amount of players allowed to connect to this server.
      */
     static final int MAXIMUM_CLIENTS = 2048;
     
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
      * The next time when the garbage will be taken out.
      */
     long nextGc;
     
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
         + "\n                                Game Server 1.0.7                                 "
         + "\n                                 See RuneTekk.com                                 "
         + "\n                               Created by SiniSoul                                "
         + "\n----------------------------------------------------------------------------------");
     }
     
     /**
      * Loads the regions for the server.
      */
     private static void loadRegions(DataInputStream is) throws IOException {
         regions = new Region[256][];
         int opcode = 0;
         while((opcode = is.read()) != 0) {
             switch(opcode) {
                 case 1:
                     {
                         int regionX = is.read();
                         int amountRegions = is.read();
                         regions[regionX] = new Region[255];                  
                         for(int i = 0; i < amountRegions; i++) {
                             int regionY = is.read();
                             regions[regionX][regionY] = new Region();
                             if(is.read() != 0) {
                                 int amountChunks = is.read();
                                 for(int j = 0; j < amountChunks; j++) {
                                     int hash = is.read();
                                     if(regions[regionX][regionY].chunks == null)
                                         regions[regionX][regionY].chunks = new Chunk[8][];
                                     if(regions[regionX][regionY].chunks[hash >> 3] == null)
                                         regions[regionX][regionY].chunks[hash >> 3] = new Chunk[8];
                                     regions[regionX][regionY].chunks[hash >> 3][hash & 0x7] = new Chunk();
                                 }
                             }
                         }
                         
                     }
                     break;
             }
         }
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
                                 node.removeFromList();
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
                                 response[8] = 10;
                                 acceptedClient.outputStream.write(response);
                                 LOGGER.log(Level.WARNING, "Client disconnected : Invalid handshake op!");
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
                                     /* UPDATE STUFF */
                                     client.flagBuffer = new ByteBuffer(122);
                                     client.activePlayers = new ListNode();
                                     client.activePlayers.childNode = client.activePlayers;
                                     client.activePlayers.parentNode = client.activePlayers;
                                     client.addedPlayers = new ListNode();
                                     client.addedPlayers.childNode = client.addedPlayers;
                                     client.addedPlayers.parentNode = client.addedPlayers;
                                     /* APPEARANCE STUFF */
                                     client.appearanceStates = new int[12];
                                     client.colorIds = new int[5];
                                     client.animationIds = new int[7];
                                     client.incomingCipher = new IsaacCipher(seeds);
                                     for(int i = 0; i < seeds.length; i++)
                                         seeds[i] += 50;
                                     client.outgoingCipher = new IsaacCipher(seeds);
                                     client.state = 1;
                                 }
                                 break;
                              
                             case 2:
                             case 3:
                             case 4:
                             case 5:
                                 if(client.lastRecievedPing + 15000L < System.currentTimeMillis())
                                     client.timeoutStamp = System.currentTimeMillis() + 60000L;
                                 while(client.iReadPosition != client.iWritePosition) {
                                     int opcode = client.incomingBuffer[client.iReadPosition] - client.incomingCipher.getNextValue() & 0xFF;
                                     int size = INCOMING_SIZES[opcode];
                                     if(size < -2) {
                                         LOGGER.log(Level.WARNING, "Client disconnected : unknown packet - {0}!", opcode);
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
                    try {
                        switch(client.state) {
                            
                            /**
                             * Initial connection state.
                             */
                            case 1:
                                client.state = 2;
                                break;
                               
                            /**
                             * Initial loop state.
                             */
                            case 2:  
                                client.state = 3;
                                break;
                             
                            /**
                             * Update state.
                             */
                            case 3:
                                Client.writeFlaggedUpdates(client);
                                client.updateMovement();
                                client.updateRegion();
                                client.state = 4;
                                break;
                              
                            /**
                             * Write update state.
                             */
                            case 4:
                                Client.sendPlayerUpdate(client);
                                client.state = 5;
                                break;
                             
                            /**
                             * Idle state.
                             */
                            case 5:
                                client.activeFlags = 0; 
                                if(client.hasWritten) {
                                    client.hasWritten = false;
                                    client.state = 2;
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
             long curTime = -1L;
             if(nextGc < (curTime = System.currentTimeMillis())) {
                 nextGc = curTime + GC_TIME;
                 System.gc();
             }
         }
      }
      
      /**
       * Removes a client from this handler.
       * @param client The client to remove.
       */
      void removeClient(IntegerNode client) {
          synchronized(removedList) {             
              client.removeFromList();
              client.parentNode = removedList.parentNode;
              client.childNode = removedList;
              client.parentNode.childNode = client;
              client.childNode.parentNode = client;
              clientArray[client.value] = null;
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
      * Report an error to the local logger for this class. All errors
      * are reported as severe.
      * @param message The message to report.
      * @param ex The exceptions to report.
      */
     static void reportError(String message, Exception ex) {
         LOGGER.log(Level.SEVERE, "{0} - {1}", new Object[]{ message, ex.getMessage()});
     }
     
     /**
      * The starting point for this application.
      * @param args The command line arguments.
      */
     public static void main(String[] args) {
         args = args.length == 0 ? new String[] { "server", "./etc/server.properties" } : args;
         printTag();
         Properties serverProperties = new Properties();
         try {
             serverProperties.load(new FileReader(args[1]));
         } catch(Exception ex) {
             reportError("Exception thrown while loading the server properties", ex);
             throw new RuntimeException();
         }
         if(args[0].equals("setup")) {
             FileIndex landscapeIndex = null;
             ArchivePackage versionPack = null;
             try {
                 landscapeIndex = new FileIndex(-1, new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("MAINFILE"), "r"), new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("L-INDEX"), "r"));
                 FileIndex index = new FileIndex(-1, new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("MAINFILE"), "r"), new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("C-INDEX"), "r"));
                 versionPack = new ArchivePackage(index.get(Integer.parseInt(serverProperties.getProperty("V-ID"))));
                 index.destroy();              
             } catch(Exception ex) {
                 reportError("Exception thrown while loading the files for setup", ex);
                 throw new RuntimeException();
             }
             try {
                 DataOutputStream os = new DataOutputStream(new FileOutputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("RFILE")));
                 byte[] mapIndex = versionPack.getArchive("map_index");
                 int amountRegions = mapIndex.length/7;
                 int[][] rCount = new int[256][];
                 int[][] cCount = new int[amountRegions][];
                 int cCountOffset = 1;
                 for(int i = 0; i < mapIndex.length; i += 7) {
                     int oldHash = ((mapIndex[i] & 0xFF) << 8) | (mapIndex[i + 1] & 0xFF);
                     int floorId = ((mapIndex[i + 2] & 0xFF) << 8) | (mapIndex[i + 3] & 0xFF);
                     int hash = oldHash >> 8;
                     if(rCount[hash] == null)
                         rCount[hash] = new int[257];
                     int countOffset = rCount[hash][256]++;
                     rCount[hash][countOffset] = (oldHash & 0xFF);
                     DataInputStream is = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(landscapeIndex.get(floorId))));
                     long updated = 0L;
                     for(int z = 0; z < 4; z++) {
                         for(int x = 0; x < 64; x++) {
                             for(int y = 0; y < 64; y++) {
                                 for(;;) {
                                     int opcode = is.read();
                                     if(opcode == 0)
                                         break;
                                     if(opcode == 1) {
                                         is.read();
                                         break;
                                     }
                                     if(opcode <= 49) {
                                         long bitValue = 1L << (long) ((x >> 3) + (8 * (y >> 3)));
                                         if((bitValue & updated) == 0) {
                                             if(cCount[cCountOffset - 1] == null) {
                                                 cCount[cCountOffset - 1] = new int[65];
                                                 rCount[hash][countOffset] |= cCountOffset << 8;
                                             }
                                             int chunkHash = ((x >> 3) << 3) | (y >> 3);
                                             cCount[cCountOffset - 1][cCount[cCountOffset - 1][64]++] = chunkHash;
                                             updated |= bitValue;
                                         }
                                         is.read();
                                     }                                
                                 }
                             }
                         }
                     }
                     is.close();
                     if(updated != 0L)
                         cCountOffset++;
                 }
                 for(int x = 0; x < rCount.length; x++) {
                     if(rCount[x] != null) {
                         os.write(1);
                         os.write(x);
                         os.write(rCount[x][256] & 0xFF);                     
                         for(int i = 0; i < rCount[x][256]; i++) {
                             os.write(rCount[x][i] & 0xFF);
                             if((rCount[x][i] & ~0xFF) != 0) {
                                 os.write(1);
                                 int[] chunks = cCount[(rCount[x][i] >> 8) - 1];
                                 os.write(chunks[64]);                               
                                 for(int k = 0; k < chunks[64]; k++) {
                                     os.write(chunks[k]);
                                 }
                             } else
                                 os.write(0);                            
                         }
                     }
                 }
                 os.writeByte(0);
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the region files", ex);
                 throw new RuntimeException();
             }
         } else if(args[0].equals("server")) {
             int portOff = -1;
             try {
                 portOff = Integer.parseInt(serverProperties.getProperty("PORTOFF"));
             } catch(Exception ex) {
                 reportError("Exception thrown while parsing the port offset", ex);
                 throw new RuntimeException();
             }
             try {
                 DataInputStream is = new DataInputStream(new FileInputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("RFILE")));
                 loadRegions(is);
                 is.close();
             } catch(Exception ex) {
                 reportError("Exception thrown while reading the regions file", ex);
                 throw new RuntimeException();
             }
             serverProperties = null;
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
             nextGc = System.currentTimeMillis() + GC_TIME;
             serverSocket = new ServerSocket();
             serverSocket.setSoTimeout(5);
             serverSocket.setReceiveBufferSize(Client.BUFFER_SIZE);
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
             -3, -3, -3, -3, -3, -3, -3, -1, -3, -3,
             -3, -3, -3, -3, -3, -3,  4, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,           
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3,  0, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,           
             -3, -3,  0, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3,  4, -3, -3, -3, -3, -3, -3, -3, -3,        
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
              4, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
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
