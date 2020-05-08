 package pl.com.henrietta.lnx2;
 
 import java.util.LinkedList;
 import java.util.HashMap;
 import java.lang.System;
 import pl.com.henrietta.lnx2.RetransmissionMode;
 import pl.com.henrietta.lnx2.Packet;
 import pl.com.henrietta.lnx2.exceptions.NothingToRead;
 import pl.com.henrietta.lnx2.exceptions.NothingToSend;
 
 /**
  * Single LNX2 channel
  * @author Henrietta
  */
 public class Channel {
 	// Parameters
 	public byte channel_id;
 	private RetransmissionMode retransmission_mode;
	private long retransmission_timeout;
 	private int max_bundle_size;
 	
 	// Class properties
 	/** ID of next window ID to send */
 	private int next_send_window_id = 0;
 	
 	/** ID of next window ID to receive */
 	private int next_expc_window_id = 0;
 	
 	
 	private class PacketWithResendingLabel {
 		/** Timestamp of 'when sent' */
		public long when_sent;
 		/** Packet object */
 		public Packet packet;
 		
		public PacketWithResendingLabel(long when_sent, Packet packet) {
 			this.when_sent = when_sent;
 			this.packet = packet;
 		}		
 	}
 	
 	/** for unACKed but sent packets */
 	private HashMap<Integer, PacketWithResendingLabel> packs_in_transit;
 	
 	/** for reordering data in RTM_AUTO_ORDERED */
 	private Packet reassemble_buffer[];
 	
 	/** for received packets to avoid duplication */
 	private Packet holding_buffer[];
 	
 	/** transmission requests */
 	private LinkedList<Packet> tx_requests;
 	
 	/** data available for user to read */
 	private LinkedList<byte[]> data_to_read;
 	
 	/** buffer for data to send */
 	private LinkedList<byte[]> buffer;
 	
 	/**
 	 * Initialized a LNX2 channel
 	 * @param channel_id this channel ID
 	 * @param retransmission_mode specifies retransmission behaviour
 	 * @param retransmission_timeout number of seconds after which delivery will
 	 * be retried
 	 * @param max_bundle_size maximum amount of window IDs in use in a single moment.
 	 * Maximum is 60. If value is larger, it will be silently truncated to 60.
 	 */
 	public Channel(byte channel_id, RetransmissionMode retransmission_mode,
				   float retransmission_timeout, int max_bundle_size) {
 		this.channel_id = channel_id;
 		this.retransmission_mode = retransmission_mode;
		this.retransmission_timeout = (long)(retransmission_timeout * 1000);
 		this.max_bundle_size = max_bundle_size;
 		
 		if (this.max_bundle_size > 60) this.max_bundle_size = 60;
 		
 		this.reassemble_buffer = new Packet[64];
 		this.holding_buffer = new Packet[64];
 		this.packs_in_transit = new HashMap<>();
 		
 		this.tx_requests = new LinkedList<>();
 		this.data_to_read = new LinkedList<>();
 		this.buffer = new LinkedList<>();		
 	}
 	
 	/**
 	 * Checks if there is any outbound data
 	 * @return true if there is data to send or there are unACKnowledged packets
 	 */
 	public boolean is_tx_in_progress() {
 		return (this.packs_in_transit.size() > 0) || (this.buffer.size() > 0) ||
 			   (this.tx_requests.size() > 0);
 	}
 	
 	/**
 	 * Arranges to have data sent
 	 * @param data data to send. Reference borrowed by this class.
 	 */
 	public void write(byte[] data) {
 		switch (this.retransmission_mode) {
 		case RTM_NONE:
 			this.tx_requests.addFirst(new Packet(data, this.channel_id, (byte)0));
 			break;
 		case RTM_MANUAL:
 			this.buffer.clear();
 			this.buffer.addFirst(data);
 			break;
 		case RTM_AUTO:
 		case RTM_AUTO_ORDERED:
 			this.buffer.addFirst(data);
 			break;
 		}
 	}
 	
 	/**
 	 * Reads a piece of data
 	 * @return data readed
 	 */
 	public byte[] read() throws NothingToRead {
 		byte[] data = this.data_to_read.pollLast(); 
 		if (data == null) throw new NothingToRead();
 		return data;
 	}
 	
 	/**
 	 * Enqueues an ACK to be sent
 	 */
 	private void enq_ack(byte window_id) {
 		this.tx_requests.addFirst(Packet.create_ack(this.channel_id, window_id));
 	}
 	
 	/**
 	 * Called when something can be sent.
 	 * Returns a packet when this class wants something to be sent. That thing
 	 * should be sent ASAP.
 	 */
 	public Packet on_sendable() throws NothingToSend {
 		// tx_requests take precedence
 		if (!this.tx_requests.isEmpty())
 			return this.tx_requests.pollLast();
 
 		switch (this.retransmission_mode) {
 		case RTM_MANUAL:
 			// two cases are possible - either a new packet can be dispatched
 			// or retransmission should be considered
 			if (this.packs_in_transit.size() == 0) {
 				// it looks like a new packet can be sent
 				if (this.buffer.isEmpty()) throw new NothingToSend();
 				byte[] data_to_send = this.buffer.pollLast();
 				Packet pkt = new Packet(data_to_send, this.channel_id, (byte)this.next_send_window_id);
				long ctime = System.currentTimeMillis();
 				PacketWithResendingLabel pwrl = new PacketWithResendingLabel(ctime, pkt);
 				this.next_send_window_id = (this.next_send_window_id + 1) % 64;
 				
 				this.packs_in_transit.put(new Integer(pkt.window_id), pwrl);
 				return pkt;
 			} else {
 				// a retransmission should be reconsidered
				long ctime = System.currentTimeMillis();
 				PacketWithResendingLabel pwrl = this.packs_in_transit.values().iterator().next();
 				if (ctime - pwrl.when_sent > this.retransmission_timeout) {
 					// yes, retransmission should be done
 					if (!this.buffer.isEmpty())
 						// user changed data to send in meantime
 						pwrl.packet.data = this.buffer.pollLast();
 					
 					pwrl.when_sent = ctime;
 					return pwrl.packet;
 				} else 
 					throw new NothingToSend();
 			}
 		case RTM_AUTO:
 		case RTM_AUTO_ORDERED:		
 			if (!this.buffer.isEmpty()) {
 				// we can consider sending a new packet
 				if (this.packs_in_transit.size() < this.max_bundle_size) {
 					// we are allowed to send a new packet
 					int nwid = this.next_send_window_id;
 					
 					int naxid = nwid - this.max_bundle_size;
 					if (naxid < 0) naxid += 64;
 					
 					if (this.packs_in_transit.get(naxid) == null) {
 						// there are no missing windows. Go on.
 						this.next_send_window_id = (this.next_send_window_id + 1) % 64;
 						Packet pk = new Packet(this.buffer.pollLast(), this.channel_id, (byte)nwid);
						long ctime = System.currentTimeMillis();
 						this.packs_in_transit.put(nwid, new PacketWithResendingLabel(ctime, pk));
 						return pk;
 					}
 				}
 			}
 			// check for retransmissions then
			long ctime = System.currentTimeMillis();
 			for (PacketWithResendingLabel pwrl : this.packs_in_transit.values()) {
 				if (ctime - pwrl.when_sent > this.retransmission_timeout) {
 					// retransmission needs to be made
 					pwrl.when_sent = ctime;
 					return pwrl.packet;
 				}
 			}			
 			throw new NothingToSend();			
 		case RTM_NONE:
 			// RTM_NONE reports directly to tx_requests, so if code follows here
 			// then it's a FAIL
 			throw new NothingToSend();
 		}
 		throw new NothingToSend();
 	}
 	
 	
 	/**
 	 * Signal from upper layer that a data packet was received. There may be data
 	 * available to read after calling this.
 	 * @param packet
 	 */
 	public void on_received(Packet packet) {
 		if (packet.is_ack) {
 			// this is an ack
 			Integer win_id = new Integer(packet.window_id); 
 			if (this.packs_in_transit.get(win_id) != null) {
 				// Acknowledgement for a real packet that we sent earlier. Dequeue it
 				// from holding buffer
 				this.packs_in_transit.remove(win_id);
 				
 				if ((this.retransmission_mode == RetransmissionMode.RTM_AUTO) || 
 				    (this.retransmission_mode == RetransmissionMode.RTM_AUTO_ORDERED)) {
 					// those may need to flush their holding_buffer to manage memory
 					// intelligently
 					int ind_to_flush = packet.window_id - this.max_bundle_size;
 					if (ind_to_flush < 0) ind_to_flush += 64;
 					
 					this.holding_buffer[ind_to_flush] = null;
 				}
 			}
 		} else {
 			// this is a data packet
 			Packet held_packet = this.holding_buffer[packet.window_id];
 			if (held_packet != null) {
 				// this was a retransmission
 				// RTM_NONE couldn't care less, as it doesn't use holding_buffer
 				// All retransmissions in RTM_MANUAL are important, as they may contain
 				// fresh data. Verify if that's the case..
 				// Both RTM_AUTO and RTM_AUTO_ORDERED care a lot about packets
 				if (held_packet.equals(packet)) this.enq_ack(packet.window_id);
 				return;
 			}
 			
 			switch (this.retransmission_mode) {
 			case RTM_NONE:
 				this.data_to_read.addFirst(packet.data);
 				break;
 			case RTM_MANUAL:
 			case RTM_AUTO:
 				this.data_to_read.addFirst(packet.data);
 				this.enq_ack(packet.window_id);
 				this.holding_buffer[packet.window_id] = packet;
 				break;
 			case RTM_AUTO_ORDERED:
 				this.enq_ack(packet.window_id);
 				this.reassemble_buffer[packet.window_id] = packet;
 				
 				if (this.next_expc_window_id == packet.window_id) {
 					// this is the next expected packet. Perform reassemblage.
 					while (this.reassemble_buffer[this.next_expc_window_id] != null) {
 						Packet pfb = this.reassemble_buffer[this.next_expc_window_id];
 						this.reassemble_buffer[this.next_expc_window_id] = null;
 						this.data_to_read.addFirst(pfb.data);
 						this.next_expc_window_id = (this.next_expc_window_id + 1) % 64;
 					}
 				}
 			}
 		}
 	}
 	
 }
