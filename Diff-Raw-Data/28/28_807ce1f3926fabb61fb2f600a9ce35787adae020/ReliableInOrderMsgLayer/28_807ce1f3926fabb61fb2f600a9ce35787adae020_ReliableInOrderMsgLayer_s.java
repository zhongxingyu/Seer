 package Rio;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 
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
 	public static int TIMEOUT = 3;
 	
 	private HashMap<Integer, InChannel> inConnections;
 	private HashMap<Integer, OutChannel> outConnections;
 	public RIONode n;
 
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
 		this.n = n;
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
 		SendInt(from, Protocol.ACK, riopkt.getSeqNum());
 		
 		InChannel in = GetInChannel(from);
 		
 		LinkedList<RIOPacket> toBeDelivered = in.gotPacket(riopkt);
 		for(RIOPacket p: toBeDelivered) {
 			// deliver in-order the next sequence of packets
 			n.onRIOReceive(from, p.getProtocol(), p.getPayload());
 		}
 	}
 
 	protected void SendInt(int destAddr, int protocol, int num) {
 		byte[] seqNumByteArray = Utility.stringToByteArray("" + num);
 		n.send(destAddr, protocol, seqNumByteArray);
 	}
 
 	protected InChannel GetInChannel(int from) {
 		InChannel in = inConnections.get(from);
 		if(in == null) {
 			in = new InChannel(this, from);
 			inConnections.put(from, in);
 		}
 		return in;
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
 		GetOutChannel(from).gotACK(seqNum);
 	}
 	
 	public void RIOSynReceive(int from, byte[] msg){
 		int seqNum = Integer.parseInt( Utility.byteArrayToString(msg) );
 		GetInChannel(from).gotSYN(seqNum);
 	}
 	
 	public void RIORstReceive(int from, byte[] msg){
 		int seqNum = Integer.parseInt( Utility.byteArrayToString(msg) );
 		GetOutChannel(from).gotRST(seqNum);
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
 	 */
 	public void RIOSend(int destAddr, int protocol, byte[] payload) {
 		OutChannel out = GetOutChannel(destAddr);
 		out.sendRIOPacket(n, protocol, payload);
 	}
 
 	protected OutChannel GetOutChannel(int destAddr) {
 		OutChannel out = outConnections.get(destAddr);
 		if(out == null) {
 			out = new OutChannel(this, destAddr);
 			outConnections.put(destAddr, out);
 		}
 		return out;
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
 		outConnections.get(destAddr).onTimeout(n, seqNum);
 	}
 	
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		for(Integer i: inConnections.keySet()) {
 			sb.append(inConnections.get(i).toString() + "\n");
 		}
 		
 		return sb.toString();
 	}
 }
 
 /**
  * Representation of an incoming channel to this node
  */
 class InChannel {
 	private int lastSeqNumDelivered;
 	private HashMap<Integer, RIOPacket> outOfOrderMsgs;
 	private ReliableInOrderMsgLayer parent;
 	private int from;
 	private boolean gotSyn;
 	
 	InChannel(ReliableInOrderMsgLayer parent, int from) {
 		this.parent = parent;
 		lastSeqNumDelivered = -1;
 		outOfOrderMsgs = new HashMap<Integer, RIOPacket>();
 		this.from = from;
 		gotSyn = false;
 	}
 	
 	public void gotSYN(Integer seqNum){
 		lastSeqNumDelivered = seqNum;
 		outOfOrderMsgs.clear();
 		gotSyn = true;
 		parent.SendInt(from, Protocol.ACK, lastSeqNumDelivered);
 	}
 
 	/**
 	 * Method called whenever we receive a data packet.
 	 * 
 	 * @param pkt
 	 *            The packet
 	 * @return A list of the packets that we can now deliver due to the receipt
 	 *         of this packet
 	 */
 	public LinkedList<RIOPacket> gotPacket(RIOPacket pkt) {
 		LinkedList<RIOPacket> pktsToBeDelivered = new LinkedList<RIOPacket>();
 		int seqNum = pkt.getSeqNum();
 
 		if(!gotSyn){
 			// reset the connection 
 			parent.SendInt(from, Protocol.RST, pkt.getSeqNum());
 			return pktsToBeDelivered;
 		}
 		
 		if(seqNum == lastSeqNumDelivered + 1) {
 			// We were waiting for this packet
 			pktsToBeDelivered.add(pkt);
 			++lastSeqNumDelivered;
 			deliverSequence(pktsToBeDelivered);
 		}else if(seqNum > lastSeqNumDelivered + 1){
 			// We received a subsequent packet and should store it
 			outOfOrderMsgs.put(seqNum, pkt);
 		}
 		// Duplicate packets are ignored
 		// TODO save lastSeqNumDelivered in a file
 		
 		return pktsToBeDelivered;
 	}
 
 	/**
 	 * Helper method to grab all the packets we can now deliver.
 	 * 
 	 * @param pktsToBeDelivered
 	 *            List to append to
 	 */
 	private void deliverSequence(LinkedList<RIOPacket> pktsToBeDelivered) {
 		while(outOfOrderMsgs.containsKey(lastSeqNumDelivered + 1)) {
 			++lastSeqNumDelivered;
 			pktsToBeDelivered.add(outOfOrderMsgs.remove(lastSeqNumDelivered));
 		}
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
 	private HashMap<Integer, RIOPacket> unACKedPackets;
 	private int lastSeqNumSent;
 	private int destAddr;
 	private ReliableInOrderMsgLayer parent;
 	public boolean gotAck;
 	private int lastSynSent;
 	
 	OutChannel(ReliableInOrderMsgLayer parent, int destAddr){
 		this.parent = parent;
 		lastSeqNumSent = -1;
 		unACKedPackets = new HashMap<Integer, RIOPacket>();
 		this.destAddr = destAddr;
 		
 		// send the destination a SYN message so they know where we start our sequence number
 		SendSyn();
 	}
 
 	protected void SendSyn() {
 		lastSynSent = ++lastSeqNumSent;
 		parent.SendInt(destAddr, Protocol.SYN, lastSynSent);
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
 	 */
 	protected void sendRIOPacket(RIONode n, int protocol, byte[] payload) {
 		try{
 			RIOPacket newPkt = new RIOPacket(protocol, ++lastSeqNumSent, payload);
 			unACKedPackets.put(lastSeqNumSent, newPkt);
 			if(gotAck){
 				n.send(destAddr, Protocol.DATA, newPkt.pack());
 				Method onTimeoutMethod = Callback.getMethod("onTimeout", parent, new String[]{ "java.lang.Integer", "java.lang.Integer" });
 				n.addTimeout(new Callback(onTimeoutMethod, parent, new Object[]{ destAddr, lastSeqNumSent }), ReliableInOrderMsgLayer.TIMEOUT);
 			}
 		}catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Called when a timeout for this channel triggers
 	 * 
 	 * @param n
 	 *            The sender and parent of this channel
 	 * @param seqNum
 	 *            The sequence number of the unACKed packet
 	 */
 	public void onTimeout(RIONode n, Integer seqNum) {
 		if(unACKedPackets.containsKey(seqNum)) {
 			resendRIOPacket(n, seqNum);
 		}
 	}
 	
 	/**
 	 * Called when we get an ACK back. Removes the outstanding packet if it is
 	 * still in unACKedPackets.
 	 * 
 	 * @param seqNum
 	 *            The sequence number that was just ACKed
 	 */
 	protected void gotACK(int seqNum) {
 		if(seqNum == lastSynSent){
 			gotAck = true;
 			// resend all the packets accumulated while waiting for the ack
 			for(RIOPacket p : unACKedPackets.values()){
				resendRIOPacket(parent.n, p.getSeqNum());
 			}
 		} else {
 			unACKedPackets.remove(seqNum);
 		}
 	}
 	
 	/**
 	 * Called when the server resets the connection. This will happen when the server
 	 * receives messages before receiving a SYN with the sequence number of the client.
 	 * 
 	 * @param seqNum
 	 * 			The sequence number of the message that went out of order from the SYN.
 	 */
 	protected void gotRST(int seqNum){
 		// Drop all the messages in the resend queue because we don't know 
 		// if the server received them or not
 		// We could rebase them to the new LSN and attempt to resend instead of dropping them 
 		// but we'd have to make sure their timers don't fire while we wait for the ack.
 		unACKedPackets.clear();
 		gotAck = false;
 		
 		// send the SYN message back to the server. Because we just cleared the list
 		// of unacked packets we can simply push forward lastSeqNumSent.
 		SendSyn();
 	}
 	
 	/**
 	 * Resend an unACKed packet.
 	 * 
 	 * @param n
 	 *            The sender and parent of this channel
 	 * @param seqNum
 	 *            The sequence number of the unACKed packet
 	 */
 	private void resendRIOPacket(RIONode n, int seqNum) {
 		try{
 			Method onTimeoutMethod = Callback.getMethod("onTimeout", parent, new String[]{ "java.lang.Integer", "java.lang.Integer" });
 			RIOPacket riopkt = unACKedPackets.get(seqNum);
 			if( riopkt != null){
 				n.send(destAddr, Protocol.DATA, riopkt.pack());
 				n.addTimeout(new Callback(onTimeoutMethod, parent, new Object[]{ destAddr, seqNum }), ReliableInOrderMsgLayer.TIMEOUT);
 			}
 		}catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
