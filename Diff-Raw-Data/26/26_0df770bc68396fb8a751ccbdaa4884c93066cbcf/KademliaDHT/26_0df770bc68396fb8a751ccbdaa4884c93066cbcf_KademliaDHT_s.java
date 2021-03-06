 /**
  * Copyright (C) 2009  HungryHobo@mail.i2p
  *
  * The GPG fingerprint for HungryHobo@mail.i2p is:
  * 6DD3 EAA2 9990 29BC 4AD2 7486 1E2C 7B61 76DC DC12
  *
  * This file is part of I2P-Bote.
  * I2P-Bote is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * I2P-Bote is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with I2P-Bote.  If not, see <http://www.gnu.org/licenses/>.
  */
 package i2p.bote.network.kademlia;
 
 import i2p.bote.Util;
 import i2p.bote.folder.DeletionAwareDhtFolder;
 import i2p.bote.network.DHT;
 import i2p.bote.network.DhtException;
 import i2p.bote.network.DhtPeerStats;
 import i2p.bote.network.DhtResults;
 import i2p.bote.network.DhtStorageHandler;
 import i2p.bote.network.I2PPacketDispatcher;
 import i2p.bote.network.I2PSendQueue;
 import i2p.bote.network.PacketBatch;
 import i2p.bote.network.PacketListener;
 import i2p.bote.network.PeerFileAnchor;
 import i2p.bote.network.kademlia.SBucket.BucketSection;
 import i2p.bote.packet.CommunicationPacket;
 import i2p.bote.packet.DataPacket;
 import i2p.bote.packet.DeleteRequest;
 import i2p.bote.packet.PeerList;
 import i2p.bote.packet.ResponsePacket;
 import i2p.bote.packet.StatusCode;
 import i2p.bote.packet.dht.DhtStorablePacket;
 import i2p.bote.packet.dht.FindClosePeersPacket;
 import i2p.bote.packet.dht.RetrieveRequest;
 import i2p.bote.packet.dht.StoreRequest;
 import i2p.bote.service.I2PBoteThread;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.URL;
 import java.security.NoSuchAlgorithmException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import net.i2p.data.DataFormatException;
 import net.i2p.data.Destination;
 import net.i2p.data.Hash;
 import net.i2p.util.ConcurrentHashSet;
 import net.i2p.util.Log;
 import net.i2p.util.RandomSource;
 
 import com.nettgryppa.security.HashCash;
 import java.util.Iterator;
 
 /**
  * The main class of the Kademlia implementation. All the high-level Kademlia logic
  * is in here.
  * In addition to standard Kademlia, the sibling list feature of S-Kademlia is implemented.
  *
  * Resources used:
  *   [1] http://pdos.csail.mit.edu/~petar/papers/maymounkov-kademlia-lncs.pdf
  *   [2] http://xlattice.sourceforge.net/components/protocol/kademlia/specs.html
  *   [3] http://en.wikipedia.org/wiki/Kademlia
  *   [4] http://www.barsoom.org/papers/infocom-2006-kad.pdf
  *   [5] http://doc.tm.uka.de/SKademlia_2007.pdf
  *   [6] OverSim (http://www.oversim.org/), which includes a S/Kademlia implementation
  *
  */
 public class KademliaDHT extends I2PBoteThread implements DHT, PacketListener {
 
     private static final int RESPONSE_TIMEOUT = 60;   // Max. number of seconds to wait for replies to retrieve requests
     private Log log = new Log(KademliaDHT.class);
     private I2PSendQueue sendQueue;
     private I2PPacketDispatcher i2pReceiver;
     private File peerFile;
     private ReplicateThread replicateThread;   // is notified of <code>store</code> calls
     private CountDownLatch readySignal;   // switches to 0 when bootstrapping is done
     private Destination localDestination;
     private Hash localDestinationHash;
     private Set<KademliaPeer> initialPeers;
     private BucketManager bucketManager;
     private Map<Class<? extends DhtStorablePacket>, DhtStorageHandler> storageHandlers;
 
     /**
      *
      * @param sendQueue
      * @param i2pReceiver
      * @param peerFile
      */
     public KademliaDHT(I2PSendQueue sendQueue, I2PPacketDispatcher i2pReceiver, File peerFile) {
         super("Kademlia");
 
         this.sendQueue = sendQueue;
         this.i2pReceiver = i2pReceiver;
         this.peerFile = peerFile;
 
         readySignal = new CountDownLatch(1);
         localDestination = sendQueue.getLocalDestination();
         localDestinationHash = localDestination.calculateHash();
         initialPeers = new ConcurrentHashSet<KademliaPeer>();
         // Read the built-in peer file
         URL builtInPeerFile = PeerFileAnchor.getBuiltInPeersFile();
         List<String> builtInPeers = Util.readLines(builtInPeerFile);
         addPeers(builtInPeers);
         // Read the updateable peer file if it exists
         if(peerFile.exists()) {
             List<String> receivedPeers = Util.readLines(peerFile);
             addPeers(receivedPeers);
         } else {
             log.info("Peer file doesn't exist, using built-in peers only (File not found: <" + peerFile.getAbsolutePath() + ">)");
         }
 
         bucketManager = new BucketManager(localDestinationHash);
         storageHandlers = new ConcurrentHashMap<Class<? extends DhtStorablePacket>, DhtStorageHandler>();
         replicateThread = new ReplicateThread(localDestination, sendQueue, i2pReceiver, bucketManager, readySignal);
     }
 
     /**
      * Creates peer destinations from a <code>String</code> each, and adds them to <code>initialPeers</code>.
      * @param peerFileEntries A list of <code>String</code>s as they appear in the peer file
      */
     private void addPeers(List<String> peerFileEntries) {
         for(String line: peerFileEntries) {
             if(!line.startsWith("#")) {
                 try {
                     Destination destination = new Destination(line);
 
                     // don't add the local destination as a peer
                     if(!destination.equals(localDestination)) {
                         KademliaPeer peer = new KademliaPeer(destination, 0);
                         initialPeers.add(peer);
                     }
                 } catch(DataFormatException e) {
                     log.error("Invalid destination key in line " + line, e);
                 }
             }
         }
     }
 
     /**
      * Queries the DHT for the <code>k</code> peers closest to a given key.
      * This method blocks.
      * @see ClosestNodesLookupTask
      */
     private List<Destination> getClosestNodes(Hash key) {
         bucketManager.updateLastLookupTime(key);
 
         ClosestNodesLookupTask lookupTask = new ClosestNodesLookupTask(key, sendQueue, i2pReceiver, bucketManager);
         lookupTask.run();
         return lookupTask.getResults();
     }
 
     @Override
     public DhtResults findOne(Hash key, Class<? extends DhtStorablePacket> dataType) {
         return find(key, dataType, false);
     }
 
     @Override
     public DhtResults findAll(Hash key, Class<? extends DhtStorablePacket> dataType) {
         return find(key, dataType, true);
     }
 
     @Override
     public void setStorageHandler(Class<? extends DhtStorablePacket> packetType, DhtStorageHandler storageHandler) {
         storageHandlers.put(packetType, storageHandler);
         replicateThread.addDhtStoreToReplicate(storageHandler);
     }
 
     @Override
     public CountDownLatch readySignal() {
         return readySignal;
     }
 
     @Override
     public boolean isReady() {
         try {
             return readySignal().await(0, TimeUnit.SECONDS);
         } catch(InterruptedException e) {
             log.error("Interrupted during a zero-second wait!", e);
             return false;
         }
     }
 
     @Override
     public int getNumPeers() {
         return bucketManager.getPeerCount();
     }
 
     @Override
     public DhtPeerStats getPeerStats() {
         return bucketManager.getPeerStats();
     }
 
     private DhtResults find(Hash key, Class<? extends DhtStorablePacket> dataType, boolean exhaustive) {
         final Collection<Destination> closeNodes = getClosestNodes(key);
         log.info("Querying localhost + " + closeNodes.size() + " peers for data type " + dataType.getSimpleName() + ", Kademlia key " + key);
 
         DhtStorablePacket localResult = findLocally(key, dataType);
         // if a local packet exists and one result is requested, return the local packet
         if(!exhaustive && localResult != null) {
             log.debug("Locally stored packet found for hash " + key + " and data type " + dataType.getSimpleName());
             DhtResults results = new DhtResults();
             results.put(localDestination, localResult);
             return results;
         }
 
         // Send the retrieve requests
         PacketBatch batch = new PacketBatch();
         for(Destination node: closeNodes) {
             if(!localDestination.equals(node)) // local has already been taken care of
             {
                 batch.putPacket(new RetrieveRequest(key, dataType), node);
             }
         }
         sendQueue.send(batch);
         try {
             batch.awaitSendCompletion();
         } catch(InterruptedException e) {
             log.warn("Interrupted while waiting for Retrieve Requests to be sent.", e);
         }
 
         // wait for replies
         try {
             if(exhaustive) {
                 batch.awaitAllResponses(RESPONSE_TIMEOUT, TimeUnit.SECONDS);
             } else {
                 batch.awaitFirstReply(RESPONSE_TIMEOUT, TimeUnit.SECONDS);
             }
         } catch(InterruptedException e) {
             log.warn("Interrupted while waiting for responses to Retrieve Requests.", e);
         }
         log.info(batch.getResponses().size() + " response packets received for hash " + key + " and data type " + dataType.getSimpleName());
 
         sendQueue.remove(batch);
 
         return getDhtResults(batch, localResult);
     }
 
     private DhtStorablePacket findLocally(Hash key, Class<? extends DhtStorablePacket> dataType) {
         DhtStorageHandler storageHandler = storageHandlers.get(dataType);
         if(storageHandler != null) {
             return storageHandler.retrieve(key);
         } else {
             return null;
         }
     }
 
     /**
      * Returns all <code>DhtStorablePacket</code> packets that have been received as a response to a send batch,
      * plus <code>localResult</code> if it is non-<code>null</code>.
      * @param batch
      * @param localResult
      * @return
      */
     private DhtResults getDhtResults(PacketBatch batch, DhtStorablePacket localResult) {
         Map<Destination, DataPacket> responses = batch.getResponses();
 
         DhtResults results = new DhtResults();
         for(Entry<Destination, DataPacket> result: responses.entrySet()) {
             DataPacket packet = result.getValue();
             if(packet instanceof DhtStorablePacket) {
                 results.put(result.getKey(), (DhtStorablePacket)packet);
             }
         }
 
         int totalResponses = responses.size();
         if(localResult != null) {
             results.put(localDestination, localResult);
             totalResponses++;
         }
         results.setTotalResponses(totalResponses);
 
         return results;
     }
 
     @Override
     public void store(DhtStorablePacket packet) throws DhtException {
         Hash key = packet.getDhtKey();
         log.info("Looking up nodes to store a " + packet.getClass().getSimpleName() + " with key " + key);
 
         List<Destination> closeNodes = getClosestNodes(key);
         if(closeNodes.isEmpty()) {
             throw new DhtException("Cannot store packet because no storage nodes found.");
         }
         // store on local node if appropriate
         if(!closeNodes.contains(localDestination)) {
             if(closeNodes.size() < KademliaConstants.K || isCloser(localDestination, closeNodes.get(0), key)) {
                 closeNodes.add(localDestination);
             }
         }
 
         log.info("Storing a " + packet.getClass().getSimpleName() + " with key " + key + " on " + closeNodes.size() + " nodes");
 
         HashCash hashCash;
         try {
             hashCash = HashCash.mintCash("", 1);   // TODO
         } catch(NoSuchAlgorithmException e) {
             throw new DhtException("Cannot mint HashCash.", e);
         }
 
         PacketBatch batch = new PacketBatch();
         for(Destination node: closeNodes) {
             if(localDestination.equals(node)) {
                 storeLocally(packet);
             } else {
                 StoreRequest storeRequest = new StoreRequest(hashCash, packet);   // use a separate packet id for each request
                 batch.putPacket(storeRequest, node);
             }
         }
         sendQueue.send(batch);
 
         try {
             batch.awaitSendCompletion();
         // TODO awaitAllResponses, repeat if necessary
         } catch(InterruptedException e) {
             log.warn("Interrupted while waiting for responses to Storage Requests to be sent.", e);
         }
 
         sendQueue.remove(batch);
     }
 
     /**
      * Returns <code>true</code> if <code>dest1</code> is closer to <code>key</code> than <code>dest2</code>.
      * @param key
      * @param destination
      * @param peers
      * @return
      */
     private boolean isCloser(Destination dest1, Destination dest2, Hash key) {
         return new PeerDistanceComparator(key).compare(dest1, dest2) < 0;
     }
 
     /**
      * Connects to the Kademlia network; blocks until done.
      */
     private void bootstrap() {
         new BootstrapTask(i2pReceiver).run();
     }
 
     /**
      * Inject a list of peers.
      */
     public void injectPeers(List<String> botePeers) {
         List<String> b32peers = botePeers;
         Iterator it = b32peers.iterator();
         while(it.hasNext()) {
             KademliaPeer peer = new KademliaPeer((String)it.next(), false);
            bucketManager.addOrUpdate(peer);
         }
     }
 
     private class BootstrapTask implements Runnable, PacketListener {
 
         private Log log = new Log(BootstrapTask.class);
         I2PPacketDispatcher i2pReceiver;
 
         public BootstrapTask(I2PPacketDispatcher i2pReceiver) {
             this.i2pReceiver = i2pReceiver;
         }
 
         @Override
         public void run() {
             log.info("Bootstrap start");
             i2pReceiver.addPacketListener(this);
             outerLoop:
             while(!shutdownRequested()) {
                 for(KademliaPeer bootstrapNode: initialPeers) {
                     bootstrapNode.setFirstSeen(System.currentTimeMillis());   // Set the "first seen" time to the current time before every bootstrap attempt
                     bootstrapNode.responseReceived();   // unlock the peer so ClosestNodesLookupTask will give it a chance
                     bucketManager.addOrUpdate(bootstrapNode);
                     log.info("Trying " + bootstrapNode.calculateHash().toBase64() + " bootstrapping.");
                     Collection<Destination> closestNodes = getClosestNodes(localDestinationHash);
 
                     if(closestNodes.isEmpty()) {
                         log.info("No response from bootstrap node " + bootstrapNode.calculateHash());
                         bucketManager.remove(bootstrapNode);
                     } else {
                         log.info("Response from bootstrap node received, refreshing all buckets. Bootstrap node = " + bootstrapNode.calculateHash().toBase64());
                         refreshAll();
                         log.info("Bootstrapping finished. Number of peers = " + bucketManager.getPeerCount());
                         for(Destination peer: bucketManager.getAllPeers()) {
                             log.debug("  Peer: " + peer.calculateHash().toBase64());
                         }
                         break outerLoop;
                     }
                 }
 
                 log.warn("Can't bootstrap off any known peer, will retry shortly.");
                 awaitShutdownRequest(1, TimeUnit.MINUTES);
             }
             i2pReceiver.removePacketListener(this);
             readySignal.countDown();
             log.info(getClass().getSimpleName() + " exiting.");
         }
 
         /**
          * When a previously unknown peer contacts us, this method adds it to <code>initialPeers</code>
          * so it can be used as a bootstrap node.
          * @param packet
          * @param sender
          * @param receiveTime
          */
         @Override
         public void packetReceived(CommunicationPacket packet, Destination sender, long receiveTime) {
             initialPeers.add(new KademliaPeer(sender, receiveTime));
         }
     }
 
     /**
      * "refresh all k-buckets further away than the closest neighbor. This refresh is just a
      * lookup of a random key that is within that k-bucket range."
      */
     public void refreshAll() {
         for(KBucket bucket: Util.synchronizedCopy(bucketManager)) {
             refresh(bucket);
         }
     }
 
     /**
      * Refreshes all buckets whose <code>lastLookupTime</code> is too old.
      */
     private void refreshOldBuckets() {
         long now = System.currentTimeMillis();
 
         // refresh k-buckets
         for(KBucket bucket: Util.synchronizedCopy(bucketManager)) {
             if(now > bucket.getLastLookupTime() + KademliaConstants.BUCKET_REFRESH_INTERVAL) {
                 log.info("Refreshing k-bucket: " + bucket);
                 refresh(bucket);
             }
         }
 
         // Refresh the s-bucket by doing a lookup for a random key in each section of the bucket.
         // For example, if k=20 and s=100, there would be a lookup for a random key between
         // the 0th and the 20th sibling (i=0), another one for a random key between the 20th
         // and the 40th sibling (i=1), etc., and finally a lookup for a random key between the
         // 80th and the 100th sibling (i=4).
         SBucket sBucket = bucketManager.getSBucket();
         BucketSection[] sections = sBucket.getSections();
         for(int i = 0; i < sections.length; i++) {
             BucketSection section = sections[i];
             if(now > section.getLastLookupTime() + KademliaConstants.BUCKET_REFRESH_INTERVAL) {
                 log.info("Refreshing s-bucket section " + i + " of " + sections.length + " (last refresh: " + new Date(section.getLastLookupTime()) + ")");
                 refresh(section);
             }
         }
     }
 
     private void refresh(KBucket bucket) {
         Hash key = createRandomHash(bucket.getStartId(), bucket.getEndId());
         getClosestNodes(key);
     }
 
     private void refresh(BucketSection section) {
         Hash key = createRandomHash(section.getStart(), section.getEnd());
         getClosestNodes(key);
     }
 
     /**
      * Returns a random value <code>r</code> such that <code>min &lt;= r &lt; max</code>.
      * @param min
      * @param max
      * @return
      */
     private Hash createRandomHash(BigInteger min, BigInteger max) {
         BigInteger hashValue;
         if(min.compareTo(max) >= 0) {
             hashValue = min;
         } else {
             hashValue = new BigInteger(Hash.HASH_LENGTH * 8, RandomSource.getInstance());   // a random number between 0 and 2^256-1
             hashValue = min.add(hashValue.mod(max.subtract(min)));   // a random number equal to or greater than min, and less than max
         }
         byte[] hashArray = hashValue.toByteArray();
         if(hashArray.length > Hash.HASH_LENGTH + 1 || (hashArray.length == Hash.HASH_LENGTH + 1 && hashArray[0] != 0)) // it's okay for the array length to be Hash.HASH_LENGTH if the zeroth byte only contains the sign bit
         {
             log.error("Hash value too big to fit in " + Hash.HASH_LENGTH + " bytes: " + hashValue);
         }
         byte[] hashArrayPadded = new byte[Hash.HASH_LENGTH];
         if(hashArray.length == Hash.HASH_LENGTH + 1) {
             System.arraycopy(hashArray, 1, hashArrayPadded, 0, Hash.HASH_LENGTH);
         } else {
             System.arraycopy(hashArray, 0, hashArrayPadded, Hash.HASH_LENGTH - hashArray.length, hashArray.length);
         }
         return new Hash(hashArrayPadded);
     }
 
     /**
      * Writes all peers to a file, sorted in descending order of uptime.
      * @param file
      */
     private void writePeersSorted(File file) {
         List<KademliaPeer> peers = bucketManager.getAllPeers();
         if(peers.isEmpty()) {
             return;
         }
 
         sortByUptime(peers);
 
         log.info("Writing peers to file: <" + file.getAbsolutePath() + ">");
         writePeers(peers, file);
     }
 
     private void writePeers(List<KademliaPeer> peers, File file) {
         BufferedWriter writer = null;
         try {
             writer = new BufferedWriter(new FileWriter(file));
             writer.write("# Each line is one Base64-encoded I2P destination.");
             writer.newLine();
             writer.write("# Do not edit while I2P-Bote is running as it will be overwritten.");
             writer.newLine();
             for(KademliaPeer peer: peers) {
                 if(peer.wasFound()) {
                     writer.write(peer.toBase64());
                     writer.newLine();
                 }
             }
         } catch(IOException e) {
             log.error("Can't write peers to file <" + file.getAbsolutePath() + ">", e);
         } finally {
             if(writer != null) {
                 try {
                     writer.close();
                 } catch(IOException e) {
                     log.error("Can't close BufferedWriter for file <" + file.getAbsolutePath() + ">", e);
                 }
             }
         }
     }
 
     /**
      * Sorts a list of peers in descending order of "first seen" time.
      * Locked peers are placed after the last unlocked peer.
      * @param peers
      */
     private void sortByUptime(List<KademliaPeer> peers) {
         Collections.sort(peers, new Comparator<KademliaPeer>() {
 
             @Override
             public int compare(KademliaPeer peer1, KademliaPeer peer2) {
                 if(peer1.isLocked() || peer2.isLocked()) {
                     int n1 = peer1.isLocked() ? 0 : 1;
                     int n2 = peer2.isLocked() ? 0 : 1;
                     return n2 - n1;
                 } else {
                     return Long.valueOf(peer2.getFirstSeen()).compareTo(peer1.getFirstSeen());
                 }
             }
         });
     }
 
     private void sendPeerList(FindClosePeersPacket packet, Destination destination) {
         Collection<Destination> closestPeers = bucketManager.getClosestPeers(packet.getKey(), KademliaConstants.K);
         PeerList peerList = new PeerList(closestPeers);
         sendQueue.sendResponse(peerList, destination, packet.getPacketId());
     }
 
     // PacketListener implementation
     @Override
     public void packetReceived(CommunicationPacket packet, Destination sender, long receiveTime) {
         if(packet instanceof FindClosePeersPacket) {
             sendPeerList((FindClosePeersPacket)packet, sender);
         } else if(packet instanceof StoreRequest) {
             DhtStorablePacket packetToStore = ((StoreRequest)packet).getPacketToStore();
             // If another peer is trying to store a packet that we know has been deleted, let them know and don't store the packet.
             DhtStorageHandler storageHandler = storageHandlers.get(packetToStore.getClass());
             if(storageHandler instanceof DeletionAwareDhtFolder<?>) {
                 DeletionAwareDhtFolder<?> folder = (DeletionAwareDhtFolder<?>)storageHandler;
                 DeleteRequest delRequest = folder.storeAndCreateDeleteRequest(packetToStore);
                 if(delRequest != null) {
                     sendQueue.send(delRequest, sender);
                 }
             } else {
                 storeLocally(packetToStore);
             }
         } else if(packet instanceof RetrieveRequest) {
             RetrieveRequest retrieveRequest = (RetrieveRequest)packet;
             DhtStorageHandler storageHandler = storageHandlers.get(retrieveRequest.getDataType());
             if(storageHandler != null) {
                 DhtStorablePacket storedPacket = storageHandler.retrieve(retrieveRequest.getKey());
                 // if requested packet found, send it to the requester
                 ResponsePacket response = new ResponsePacket(storedPacket, StatusCode.OK, retrieveRequest.getPacketId());
                 if(storedPacket != null) {
                     log.debug("Packet found for retrieve request: [" + retrieveRequest + "], replying to sender: [" + sender.calculateHash() + "]");
                 } else {
                     log.debug("No matching packet found for retrieve request: [" + retrieveRequest + "]");
                 }
                 sendQueue.send(response, sender);
             } else {
                 log.warn("No storage handler found for type " + packet.getClass().getSimpleName() + ".");
             }
         } else if(packet instanceof DeleteRequest) {
             DeleteRequest delRequest = (DeleteRequest)packet;
             DhtStorageHandler storageHandler = storageHandlers.get(delRequest.getDataType());
             if(storageHandler instanceof DeletionAwareDhtFolder<?>) {
                 ((DeletionAwareDhtFolder<?>)storageHandler).process(delRequest);
             }
         }
 
         // bucketManager is not registered as a PacketListener, so notify it here
         bucketManager.packetReceived(packet, sender, receiveTime);
     }
 
     private void storeLocally(DhtStorablePacket packetToStore) {
         if(packetToStore != null) {
             DhtStorageHandler storageHandler = storageHandlers.get(packetToStore.getClass());
             if(storageHandler != null) {
                 storageHandler.store(packetToStore);
                 replicateThread.packetStored(storageHandler, packetToStore);
             } else {
                 log.warn("No storage handler found for type " + packetToStore.getClass().getSimpleName() + ".");
             }
         }
     }
 
     @Override
     public void start() {
         super.start();
         replicateThread.start();
     }
 
     @Override
     public void run() {
         i2pReceiver.addPacketListener(this);
         bootstrap();
 
         while(!shutdownRequested()) {
             if(bucketManager.getUnlockedPeerCount() == 0) {
                 log.info("All peers are gone. Re-bootstrapping.");
                 bootstrap();
             }
             refreshOldBuckets();
             awaitShutdownRequest(1, TimeUnit.MINUTES);
         }
         writePeersSorted(peerFile);
         i2pReceiver.removePacketListener(this);
         log.info(getClass().getSimpleName() + " exiting.");
     }
 
     @Override
     public void awaitShutdown(long timeout) throws InterruptedException {
         join(timeout);
     }
 
     @Override
     public void requestShutdown() {
         super.requestShutdown();
         replicateThread.requestShutdown();
     }
 }
