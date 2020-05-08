 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.TreeMap;
 import java.util.SortedMap;
 
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.Utility;
 
 /**
  * Layer above the basic messaging layer that provides reliable, in-order
  * delivery in the absence of faults. This layer does not provide much more than
  * the above.
  *
  * At a minimum, the student should extend/modify this layer to provide
  * reliable, in-order message delivery, even in the presence of node failures.
  */
 public class ReliableInOrderMsgLayer {
   public class SyncState {
     ReliableInOrderMsgLayer parent;
     int remoteAddress;
     long localStartUpTime;
     long remoteStartUpTime;
     int session;
     boolean established;
     boolean localTimeSent;
 
     public SyncState(ReliableInOrderMsgLayer parent, int remoteAddress) {
       this.parent = parent;
       this.remoteAddress = remoteAddress;
       reset();
     }
 
     private void log(String s) {
       System.err.println("NET: " + parent.n.addr + " <-> " + remoteAddress + ": " + s);
     }
 
     public void reset() {
       log("RESET");
       remoteStartUpTime = 0;
       established = false;
       session = 0;
       localTimeSent = false;
       localStartUpTime = System.currentTimeMillis();
       log("Local startup now " + localStartUpTime);
 
       // Need to notify our in/out channels to make them stop all messages
       onReset();
     }
 
     private void onSync() {
       log("SYNC");
       established = true;
       InChannel in = parent.getOrCreateInChannel(remoteAddress);
       in.sessionValid(session);
 
       OutChannel out = parent.outConnections.get(remoteAddress);
       out.sessionValid(session);
     }
 
     private void onReset() {
       InChannel in = inConnections.get(remoteAddress);
       if (in != null)
         in.resetSession();
 
       OutChannel out = getOrCreateOutChannel(remoteAddress);
 
       out.resetSession(localStartUpTime);
     }
 
     public boolean isEstablished() {
       return established;
     }
 
     public void remoteStartUp(long remoteStartUpTime, boolean isResponse) {
      log("Remote startup @ " + remoteStartUpTime);
       if (established) {
         if (remoteStartUpTime != this.remoteStartUpTime) {
           reset();
           OutChannel out = getOrCreateOutChannel(remoteAddress);
 
           out.sendSessionSetupMessage(localStartUpTime, true, false);
         }
         return;
       }
 
       this.remoteStartUpTime = remoteStartUpTime;
       long time1 = (remoteAddress > parent.n.addr ? localStartUpTime : remoteStartUpTime);
       long time2 = (remoteAddress > parent.n.addr ? remoteStartUpTime : localStartUpTime);
 
       session = (Long.toString(time1) + Long.toString(time2)).hashCode();
      log("Computed session id: " + session);
 
       OutChannel out = getOrCreateOutChannel(remoteAddress);
       if (isResponse)
         onSync();
       else
         out.sendSessionSetupMessage(localStartUpTime, true, false);
     }
 
     public boolean confirmSession(int remoteSession) {
       log("Confirm session (remote = " + remoteSession + "; local = " + session + ")");
 
       if (remoteSession != session) {
         log("Remote session != local session; resetting");
         reset();
         OutChannel out = getOrCreateOutChannel(remoteAddress);
         out.sendSessionSetupMessage(localStartUpTime, false, false);
         return false;
       }
 
       if (!established) {
         established = true;
         onSync();
       }
 
       return true;
     }
 
     public boolean isLocalTimeSent() {
       return localTimeSent;
     }
 
     public void setLocalTimeSent(boolean sent) {
       localTimeSent = sent;
     }
 
     public long getLocalTime() {
       return localStartUpTime;
     }
   }
 
   public static int TIMEOUT = 3;
   public static final int MAX_RESENDS = 3;
 
   public static final int MAX_PACKET_XFER_TIME = (MAX_RESENDS + 1) * TIMEOUT;
 
   private HashMap<Integer, InChannel> inConnections;
   private HashMap<Integer, OutChannel> outConnections;
   private HashMap<Integer, SyncState> syncStates;
   RIONode n;
 
   /**
    * Constructor.
    *
    * @param destAddr
    *            The address of the destination host
    * @param msg
    *            The message that was sent
    * @param timeSent
    *            The time that the ping was sent
    */
   public ReliableInOrderMsgLayer(RIONode n) {
     inConnections = new HashMap<Integer, InChannel>();
     outConnections = new HashMap<Integer, OutChannel>();
     syncStates = new HashMap<Integer, SyncState>();
     this.n = n;
   }
 
   public InChannel getOrCreateInChannel(int destAddr) {
     InChannel in = inConnections.get(destAddr);
     if (in == null) {
       in = new InChannel(this, destAddr);
       inConnections.put(destAddr, in);
     }
 
     return in;
   }
 
   public OutChannel getOrCreateOutChannel(int destAddr) {
     OutChannel out = outConnections.get(destAddr);
     if (out == null) {
       out = new OutChannel(this, destAddr);
       outConnections.put(destAddr, out);
     }
 
     return out;
   }
 
   public SyncState getOrCreateSyncState(int dest) {
     SyncState state = syncStates.get(dest);
     if (state == null) {
       state = new SyncState(this, dest);
       syncStates.put(dest, state);
     }
 
     return state;
   }
 
   /**
    * Receive a data packet.
    *
    * @param from
    *            The address from which the data packet came
    * @param pkt
    *            The Packet of data
    */
   public void RIODataReceive(int from, byte[] msg) {
     RIOPacket riopkt = RIOPacket.unpack(msg);
 
     // at-most-once semantics
     byte[] seqNumByteArray = Utility.stringToByteArray("" + riopkt.getSeqNum());
     n.send(from, Protocol.ACK, seqNumByteArray);
 
     InChannel in = inConnections.get(from);
     if(in == null) {
       in = new InChannel(this, from);
       inConnections.put(from, in);
     }
 
     in.gotPacket(riopkt);
   }
 
   /**
    * Delivers the given packets in the same order as they are represented
    * in the given list.
    *
    * @param from Sender for all packets.
    * @param toBeDelivered list of packets to be delivered
    */
   public void deliverPackets(int from, List<RIOPacket> toBeDelivered) {
     for(RIOPacket p: toBeDelivered) {
       // deliver in-order the next sequence of packets
       n.onRIOReceive(from, p.getProtocol(), p.getPayload());
     }
   }
 
   /**
    * Receive an acknowledgment packet.
    *
    * @param from
    *            The address from which the data packet came
    * @param pkt
    *            The Packet of data
    */
   public void RIOAckReceive(int from, byte[] msg) {
     int seqNum = Integer.parseInt( Utility.byteArrayToString(msg) );
     OutChannel out = outConnections.get(from);
     if (out != null)
       out.gotACK(seqNum);
   }
 
   public void RIOSessionReceive(int from, byte[] msg) {
     RIOPacket pkt = RIOPacket.unpack(msg);
     if (pkt == null)
       return;
 
     SyncState state = getOrCreateSyncState(from);
 
     if (pkt.getSeqNum() == 1 || pkt.getSeqNum() == 2) {
       // msg contains the remote start up time
       long remoteStartUpTime = BinaryUtils.beLongToUint(pkt.getPayload(), 0);
 
       state.remoteStartUp(remoteStartUpTime, pkt.getSeqNum() == 2);
 
       OutChannel out = outConnections.get(from);
       if (pkt.getSeqNum() == 1) {
         out.sendSessionSetupMessage(state.getLocalTime(), true, false);
       } else {
         out.sendSessionConfirmMessage();
       }
     } else if (pkt.getSeqNum() == 3) {
       // msg contains the session id
       int session = BinaryUtils.beWordToUint(pkt.getPayload(), 0);
       state.confirmSession(session);
     } else {
 //      System.err.println("WARN: Session layer received an unknown message with seqno " + pkt.getSeqNum());
     }
   }
 
   /**
    * Send a packet using this reliable, in-order messaging layer. Note that
    * this method does not include a reliable, in-order broadcast mechanism.
    *
    * @param destAddr
    *            The address of the destination for this packet
    * @param protocol
    *            The protocol identifier for the packet
    * @param payload
    *            The payload to be sent
    * @param callback
    *            Callback called when the network layer has finished with the packet.
    * @return
    *            The sequence number of the packet, or -1 if a sending error occurred.
    */
   public int RIOSend(int destAddr, int protocol, byte[] payload, Callback callback) {
     OutChannel out = getOrCreateOutChannel(destAddr);
 
     return out.sendRIOPacket(n, protocol, payload, callback);
   }
 
   /**
    * Callback for timeouts while waiting for an ACK.
    *
    * This method is here and not in OutChannel because OutChannel is not a
    * public class.
    *
    * @param destAddr
    *            The receiving node of the unACKed packet
    * @param seqNum
    *            The sequence number of the unACKed packet
    */
   public void onTimeout(Integer destAddr, Integer seqNum) {
     // Target OutChannel could have been removed, so we must test its nullity.
     OutChannel out = outConnections.get(destAddr);
     if (out != null) {
       out.onTimeout(n, seqNum);
     }
   }
 
   public void resetConnection(Integer dest) {
     SyncState state = getOrCreateSyncState(dest);
 
     // Reset connection state
     state.reset();
   }
 
   /**
    * Trampoline function.
    */
   public void sessionSetupPacketTimeout(Integer dest, Boolean isResponse) {
     OutChannel channel = outConnections.get(dest);
     if (channel != null)
       channel.sessionSetupPacketTimeout(isResponse);
   }
 
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer();
     for(Integer i: inConnections.keySet()) {
       sb.append(inConnections.get(i).toString() + "\n");
     }
 
     return sb.toString();
   }
 
   /**
    * Remove the element of outConnections corresponding to the argument address
    *
    * @param destAddr
    *            The node corresponding to the outChannel to be removed from
    *            outConnections
    */
   public void removeOutChannel(Integer destAddr) {
     outConnections.remove(destAddr);
   }
 }
 
 /**
  * Representation of an incoming channel to this node
  */
 class InChannel {
   private int lastSeqNumDelivered;
   private int nextUndeliveredPacket;
   private TreeMap<Integer, RIOPacket> outOfOrderMsgs;
   private boolean isCleanupCbScheduled;
   private ReliableInOrderMsgLayer parent;
   private int sender;
   private long session;
 
   InChannel(ReliableInOrderMsgLayer parent, int sender) {
     this.sender = sender;
     this.parent = parent;
 
     lastSeqNumDelivered = -1;
     nextUndeliveredPacket = -1;
     outOfOrderMsgs = new TreeMap<Integer, RIOPacket>();
     isCleanupCbScheduled = false;
   }
 
   private void deliverOutstandingPackets() {
     LinkedList<RIOPacket> deliverables = new LinkedList<RIOPacket>();
     deliverSequence(deliverables);
    System.err.println("IN: Session valid, delivering " + deliverables.size() + " packets. InCh Stats:");
    System.err.println(toString());
     parent.deliverPackets(sender, deliverables);
   }
 
   public void resetSession() {
     outOfOrderMsgs.clear();
     lastSeqNumDelivered = -1;
     nextUndeliveredPacket = -1;
     session = 0;
   }
 
   public void sessionValid(long session) {
     this.session = session;
     deliverOutstandingPackets();
   }
 
   public void maybeScheduleHoleCleanup() {
     Callback cleanupCb;
     try {
       Method method = Callback.getMethod("maybeSkipUnreceivedSeqNoHole", this, new String[]{ "java.lang.Integer", "java.lang.Integer" });
       cleanupCb = new Callback(method, this, new Object[]{ nextUndeliveredPacket, session });
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       return;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       return;
     }
 
     // Schedule cleanup callback iff we have received the timing packet.
     if (!isCleanupCbScheduled && nextUndeliveredPacket != -1 && lastSeqNumDelivered != -1) {
       parent.n.addTimeout(cleanupCb, 12);
       isCleanupCbScheduled = true;
     }
   }
 
   public void maybeSkipUnreceivedSeqNoHole(Integer nextExpectedUndeliveredPacket,
                                            Integer expectedSession) {
     isCleanupCbScheduled = false;
     if (nextUndeliveredPacket == -1 ||
         expectedSession != session)
       return;
 
     // Ensure we didn't deliver the blocked packet during the timeout
     if (nextUndeliveredPacket != nextExpectedUndeliveredPacket) {
       // next undelivered packet != -1, but not what we expected;
       // something wacky happened in the network, but since the error
       // condition is still present, let's schedule us again.
       maybeScheduleHoleCleanup();
       return;
     }
 
     LinkedList<RIOPacket> deliverables = new LinkedList<RIOPacket>();
     deliverSequence(deliverables, nextUndeliveredPacket);
     parent.deliverPackets(sender, deliverables);
   }
 
   /**
    * Method called whenever we receive a data packet.
    *
    * @param pkt
    *            The packet
    * @return A list of the packets that we can now deliver due to the receipt
    *         of this packet
    */
   public void gotPacket(RIOPacket pkt) {
     LinkedList<RIOPacket> pktsToBeDelivered = new LinkedList<RIOPacket>();
     int seqNum = pkt.getSeqNum();
    System.err.print("NET: " + parent.n.addr + ": Got packet " + seqNum + " vs " + lastSeqNumDelivered + ", session = " + session + ": ");
 
     if(seqNum == lastSeqNumDelivered + 1 && session != 0) {
       // We were waiting for this packet
       pktsToBeDelivered.add(pkt);
       ++lastSeqNumDelivered;
       deliverSequence(pktsToBeDelivered);
       parent.deliverPackets(sender, pktsToBeDelivered);
       System.err.println("Delivered.");
     } else if(seqNum >= lastSeqNumDelivered + 1){
       // We received a subsequent packet and should store it
       outOfOrderMsgs.put(seqNum, pkt);
       System.err.println("Queued.");
     }
     // Duplicate packets are ignored
   }
 
   /**
    * Helper method to grab all the packets we can now deliver.
    *
    * @param pktsToBeDelivered
    *            List to append to
    */
   private boolean deliverSequence(LinkedList<RIOPacket> pktsToBeDelivered) {
     return deliverSequence(pktsToBeDelivered, -1);
   }
 
   private boolean deliverSequence(LinkedList<RIOPacket> pktsToBeDelivered,
                                   int skipAllHolesBefore) {
     if (outOfOrderMsgs.size() == 0)
       return false;
 
     Map.Entry<Integer, RIOPacket> entry = outOfOrderMsgs.ceilingEntry(lastSeqNumDelivered + 1);
 //    System.err.println("FE: " + entry + ", LSND = " + lastSeqNumDelivered + ", 0=" + outOfOrderMsgs.get(0));
 
     while (entry != null &&
            (entry.getKey() == Math.max(lastSeqNumDelivered, skipAllHolesBefore) + 1 ||
             entry.getKey() < skipAllHolesBefore)) {
       lastSeqNumDelivered = entry.getKey();
       pktsToBeDelivered.add(outOfOrderMsgs.remove(lastSeqNumDelivered));
       entry = outOfOrderMsgs.ceilingEntry(lastSeqNumDelivered + 1);
     }
 
     if (entry != null) {
       nextUndeliveredPacket = entry.getKey();
       maybeScheduleHoleCleanup();
     } else {
       nextUndeliveredPacket = -1;
     }
 
     return entry == null; // entry == null -> no more packets to deliver
   }
 
   @Override
   public String toString() {
     return "last delivered: " + lastSeqNumDelivered + ", outstanding: " + outOfOrderMsgs.size();
   }
 }
 
 /**
  * Representation of an outgoing channel to this node
  */
 class OutChannel {
   private static class SentPacket implements Comparable<SentPacket> {
     RIOPacket pkt;
     Callback callback;
     boolean acked;
 
     public SentPacket(RIOPacket pkt, Callback successCallback) {
       this.pkt = pkt;
       this.callback = callback;
       this.acked = false;
     }
 
     public int compareTo(SentPacket other) {
       return pkt.getSeqNum() - other.pkt.getSeqNum();
     }
 
     public void ack(boolean callCallback) {
       acked = true;
       if (callCallback && callback != null) {
         try {
           callback.setParams(new Object[]{pkt.getSeqNum(), null});
           callback.invoke();
         } catch (Exception e) {
 //          System.err.println("An exception occurred while trying to call the" +
 //                             "callback for packet " + pkt.getSeqNum() + ":");
           e.printStackTrace();
         }
       }
     }
 
     public boolean callIfAcked() {
       if (acked && callback != null) {
         try {
           callback.setParams(new Object[]{pkt.getSeqNum(), null});
           callback.invoke();
         } catch (Exception e) {
 //          System.err.println("An exception occurred while trying to call the" +
 //                             "callback for packet " + pkt.getSeqNum() + ":");
           e.printStackTrace();
         }
       }
       return acked;
     }
 
     public void timeout() {
       error(new NetworkExceptions.Timeout(pkt));
     }
 
     public void error(Exception exc) {
         if (callback != null) {
           try {
             callback.setParams(new Object[]{pkt.getSeqNum(), exc});
             callback.invoke();
           } catch (Exception e) {
 //            System.err.println("An exception occurred while trying to call the" +
 //                               "callback for packet " + pkt.getSeqNum() + ":");
             e.printStackTrace();
           }
         }
     }
   }
 
   public static final int kBackoffInit = ReliableInOrderMsgLayer.MAX_PACKET_XFER_TIME + 3;
   public static final int kBackoffLimit = ReliableInOrderMsgLayer.MAX_PACKET_XFER_TIME * 3;
 
   private Queue<SentPacket> prestartQueue;
   private TreeMap<Integer, SentPacket> activePackets;
   private int lastSeqNumSent;
   private ReliableInOrderMsgLayer parent;
   private int destAddr;
   private int session;
   private boolean sessionValid;
   private int sessionBackoff;
 
   OutChannel(ReliableInOrderMsgLayer parent, int destAddr){
     this.parent = parent;
     this.destAddr = destAddr;
 
     prestartQueue = new LinkedList<SentPacket>();
     activePackets = new TreeMap<Integer, SentPacket>();
 
     lastSeqNumSent = -1;
     session = 0;
     sessionValid = false;
   }
 
   public void sessionValid(int sessionId) {
     session = sessionId;
     sessionValid = true;
 
     activePackets.clear();
 
     SentPacket p;
     while ((p = prestartQueue.poll()) != null) {
 //      System.err.println("NET: " + parent.n.addr + ": Sending packet with seqno " + p.pkt.getSeqNum());
       p.pkt.setSession(session);
       doSend(p.pkt, p.callback);
     }
   }
 
   public void resetSession(long newLocalStartUpTime) {
     sessionValid = false;
 
 //    System.err.println("NET: " + parent.n.addr + ": on reset, pq is " + prestartQueue.size() + " entries big.");
     if (prestartQueue.size() > 0) {
       HashMap<Integer,Integer> spans = new HashMap<Integer,Integer>();
       for (SentPacket p : prestartQueue) {
         boolean hasGroup = false;
         int s = p.pkt.getSeqNum();
         if (spans.containsKey(s + 1)) {
           hasGroup = true;
           int otherEnd = spans.remove(s + 1);
           spans.put(s, otherEnd);
           spans.put(otherEnd, s);
         }
         if (spans.containsKey(s - 1)) {
           hasGroup = true;
           int otherEnd = spans.remove(s - 1);
           if (spans.containsKey(s)) {
             int upperEnd = spans.remove(s);
             spans.put(otherEnd, upperEnd);
             spans.put(upperEnd, otherEnd);
           } else {
             spans.put(otherEnd, s);
             spans.put(s, otherEnd);
           }
         }
         if (!hasGroup) {
           spans.put(s, s);
         }
       }
 
       lastSeqNumSent = -1;
       for (Integer e1 : spans.keySet()) {
         if (e1 == 0 || spans.get(e1) == 0) {
           lastSeqNumSent = Math.max(e1, spans.get(e1));
           break;
         }
       }
 
 //      System.err.println("NET: " + parent.n.addr + ": on connection reset, saving prestarted packets " +
 //                         "between 0 and " + lastSeqNumSent);
 
       int numOutstanding = prestartQueue.size();
       for (int i = 0; i < numOutstanding; ++i) {
         SentPacket p = prestartQueue.poll();
         if (p.pkt.getSeqNum() >= 0 && p.pkt.getSeqNum() <= lastSeqNumSent)
           prestartQueue.offer(p);
         else
           p.error(new NetworkExceptions.ConnectionReset());
       }
     } else {
 //      System.err.println("LSNS = -1");
       lastSeqNumSent = -1;
     }
 
     sessionBackoff = kBackoffInit;
   }
 
   public void cancelActivePackets() {
     for (Integer seqNo : activePackets.keySet()) {
       activePackets.remove(seqNo).error(new NetworkExceptions.ConnectionReset());
     }
   }
 
   public void cancelPrestartPackets() {
     SentPacket p;
     while ((p = prestartQueue.poll()) != null)
       p.error(new NetworkExceptions.ConnectionReset());
   }
 
   void sessionSetupPacketTimeout(boolean isResponse) {
     if (sessionValid)
       return;
 
     sessionBackoff *= 2;
 //    System.err.println("NET: " + parent.n.addr + ": session backoff is now " + sessionBackoff + " (vs. limit " + kBackoffLimit + ")");
     if (sessionBackoff < kBackoffLimit) {
       ReliableInOrderMsgLayer.SyncState state = parent.getOrCreateSyncState(destAddr);
       if (state.isEstablished())
         return;
 
       sendSessionSetupMessage(state.getLocalTime(), isResponse, true);
     } else {
       ReliableInOrderMsgLayer.SyncState state = parent.getOrCreateSyncState(destAddr);
       state.setLocalTimeSent(false);
       cancelActivePackets();
       cancelPrestartPackets();
     }
   }
 
   void sendSessionSetupMessage(long localStartUpTime, boolean isResponse, boolean force) {
     ReliableInOrderMsgLayer.SyncState state = parent.getOrCreateSyncState(destAddr);
     if (state.isLocalTimeSent() && !force)
       return;
 
     byte[] startUpTimeEncoded = new byte[8];
     BinaryUtils.uintToBELong(localStartUpTime, startUpTimeEncoded, 0);
 
     RIOPacket pkt = new RIOPacket(Protocol.SESSION, isResponse ? 2 : 1, 0, startUpTimeEncoded);
     doSend(pkt, null);
 
     state.setLocalTimeSent(true);
 
     try {
       String[] cbArgs = new String[]{ "java.lang.Integer", "java.lang.Boolean" };
       Method onTimeoutMethod = Callback.getMethod("sessionSetupPacketTimeout", parent, cbArgs);
       parent.n.addTimeout(new Callback(onTimeoutMethod, parent, new Object[]{ destAddr, isResponse }), sessionBackoff);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       return;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       return;
     }
   }
 
   void sendSessionConfirmMessage() {
     byte[] sessionIdEncoded = new byte[4];
     BinaryUtils.uintToBEWord(session, sessionIdEncoded, 0);
 
     RIOPacket pkt = new RIOPacket(Protocol.SESSION, 3, session, sessionIdEncoded);
     doSend(pkt, null);
   }
 
   /**
    * Send a new RIOPacket out on this channel.
    *
    * @param n
    *            The sender and parent of this channel
    * @param protocol
    *            The protocol identifier of this packet
    * @param payload
    *            The payload to be sent
    * @param callback
    *            Callback called when the network has finished with the packet,
    *            successfully or in failure. callback is of the format
    *            void(int, Exception), and the second arg will be null if the
    *            packet was delivered successfully.
    *
    *            NOTE: Callback will _NOT_ be called if an error occurs within
    *            this function. Instead, a sequence number of -1 will be returned.
    *
    * @return sequence number of the packet, or -1 if an error occurred
    */
   protected int sendRIOPacket(RIONode n, int protocol, byte[] payload, Callback callback) {
     RIOPacket newPkt = new RIOPacket(protocol, ++lastSeqNumSent, session, payload);
     if (!sessionValid) {
       ReliableInOrderMsgLayer.SyncState state = parent.getOrCreateSyncState(destAddr);
       if (!state.isLocalTimeSent())
         sendSessionSetupMessage(state.getLocalTime(), false, false);
 
       // Needed because creating the sync state might reset lastSeqNumSent
       if (lastSeqNumSent == -1) {
         if (newPkt.getSeqNum() != 0)
           System.err.println("Unexpectedly created sync state past the first packet?");
         else
           lastSeqNumSent = 0;
       }
 
 //      System.err.println("NET: " + parent.n.addr + ": pq " + toString() + " insert seqno " + newPkt.getSeqNum() + ", LSNS = " + lastSeqNumSent);
       prestartQueue.offer(new SentPacket(newPkt, callback));
       return newPkt.getSeqNum();
     } else {
       return doSend(newPkt, callback);
     }
   }
 
   protected int doSend(RIOPacket newPkt, Callback completeCallback) {
     try {
       String[] cbArgs = new String[]{ "java.lang.Integer", "java.lang.Integer" };
       Method onTimeoutMethod = Callback.getMethod("onTimeout", parent, cbArgs);
       parent.n.addTimeout(new Callback(onTimeoutMethod, parent, new Object[]{ destAddr, newPkt.getSeqNum() }), ReliableInOrderMsgLayer.TIMEOUT);
       parent.n.send(destAddr, newPkt.getProtocol(), newPkt.pack());
       activePackets.put(newPkt.getSeqNum(), new SentPacket(newPkt, completeCallback));
       return newPkt.getSeqNum();
     } catch(Exception e) {
       System.err.println("An exception occurred when sending packet " + lastSeqNumSent + " to node " + destAddr);
       e.printStackTrace();
       if (activePackets.remove(lastSeqNumSent) != null &&
           lastSeqNumSent == newPkt.getSeqNum() &&
           newPkt.getProtocol() == Protocol.DATA)
         lastSeqNumSent--;
       return -1;
     }
   }
 
   /**
    * Called when a timeout for this channel triggers.
    *
    * @param n
    *            The sender and parent of this channel
    * @param seqNum
    *            The sequence number of the unACKed packet
    */
   public void onTimeout(RIONode n, Integer seqNum) {
     if(activePackets.containsKey(seqNum) &&
        (activePackets.get(seqNum).pkt.getProtocol() != 2 ||
         (!sessionValid && activePackets.get(seqNum).pkt.getSession() == session))) {
       resendRIOPacket(n, seqNum);
     }
   }
 
   /**
    * Called when we get an ACK back. Removes the outstanding packet if it is
    * still in activePackets.
    *
    * @param seqNum
    *            The sequence number that was just ACKed
    */
   protected void gotACK(int seqNum) {
     if (activePackets.size() == 0)
       return;
 
     boolean callCallback = activePackets.firstKey() == seqNum;
     SentPacket pkt = activePackets.get(seqNum);
     if (pkt != null) {
       pkt.ack(callCallback);
       if (callCallback)
         activePackets.remove(pkt.pkt.getSeqNum());
     }
 
     flushACKedPackets();
   }
 
   /**
    * Flush the earliest contiguous group of ACK'd packets from our queue, if
    * they exist. Calls the callback for each ACK'd packet, and deletes it until
    * it encounters an unACK'd packet.
    */
   protected void flushACKedPackets() {
     while (activePackets.size() > 0) {
       SentPacket sentPkt = activePackets.firstEntry().getValue();
       if (!sentPkt.callIfAcked())
         break;
       else
         activePackets.remove(sentPkt.pkt.getSeqNum());
     }
   }
 
   /**
    * Resend an unACKed packet, or if it has been resent MAX_RESENDS times,
    * drop the packet and call failureCallback (if non-null) with a
    * TimeoutException.
    *
    * @param n
    *            The sender and parent of this channel
    * @param seqNum
    *            The sequence number of the unACKed packet
    */
   private void resendRIOPacket(RIONode n, int seqNum) {
     SentPacket pkt = activePackets.get(seqNum);
     if (pkt == null)
       return;
 
     try{
       RIOPacket riopkt = pkt.pkt;
       if (riopkt.getNumResends() < ReliableInOrderMsgLayer.MAX_RESENDS) {
         // Resend the packet.
         Method onTimeoutMethod = Callback.getMethod("onTimeout", parent, new String[]{ "java.lang.Integer", "java.lang.Integer" });
 
         riopkt.incNumResends();
         n.send(destAddr, riopkt.getProtocol(), riopkt.pack());
         n.addTimeout(new Callback(onTimeoutMethod, parent, new Object[]{ destAddr, seqNum }), ReliableInOrderMsgLayer.TIMEOUT);
       } else {
         // Resend limit has been reached, so stop resending.
         activePackets.remove(seqNum);
         pkt.timeout();
         flushACKedPackets();
       }
     }catch(Exception e) {
       System.err.println("Exception occurred while resending the packet: ");
       e.printStackTrace();
       activePackets.remove(pkt.pkt.getSeqNum());
       pkt.error(e);
     }
   }
 }
