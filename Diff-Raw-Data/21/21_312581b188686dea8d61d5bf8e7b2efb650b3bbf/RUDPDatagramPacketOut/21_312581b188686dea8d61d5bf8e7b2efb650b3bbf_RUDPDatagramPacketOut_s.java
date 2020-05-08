 package communication.rudp.socket.datagram;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import communication.rudp.socket.RUDPDatagramPacketSenderInterface;
 import communication.rudp.socket.listener.RUDPLinkFailListener;
 import communication.rudp.socket.rangeset.DeltaRangeList;
 
 public class RUDPDatagramPacketOut extends RUDPDatagramPacket {
 
 	//Send attempts
 	protected int attempts;
 	protected int maxAttempts;
 
 	//Resend timer
 	protected Timer timer;
 	protected TimerTask taskResend;
 	
 	//Interface to send packets with
 	protected RUDPDatagramPacketSenderInterface sendInterface;
 	protected RUDPLinkFailListener failListener;
 	
 	//TODO for debug
 	private static int newId = 0; 
 
 	public RUDPDatagramPacketOut() {
 		//Random id for debugging
 		id = newId++;
 	}
 	
 	public RUDPDatagramPacketOut(RUDPDatagramPacket packet) {
 		this.ack_seq_data = packet.ack_seq_data;
 		this.ack_window_start = packet.ack_window_start;
 		this.data = packet.data;
 		this.flag_ack = packet.flag_ack;
 		this.flag_data = packet.flag_data;
 		this.flag_first = packet.flag_first;
 		this.flag_fragment = packet.flag_fragment;
 		this.flag_resend = packet.flag_resend;
 		this.flag_reset = packet.flag_reset;
 		this.frag_count = packet.frag_count;
 		this.frag_nr = packet.frag_nr;
 		this.id = packet.id;
 		this.packet_seq = packet.packet_seq;
 		this.window_size = packet.window_size;
 	}
 		
 	public byte[] serializePacket() {
 		ByteArrayOutputStream bos;
 		DataOutputStream dos;
 		int ack_count = 0;
 		
 		try {
 			bos = new ByteArrayOutputStream();
 			dos = new DataOutputStream(bos);
 			
 			//TODO remove debug
 			dos.writeInt(id);
 			
 			//Write flags
 			dos.writeByte((flag_first ? FLAG_FIRST : 0) + (flag_reset ? FLAG_RESET : 0) + (flag_ack ? FLAG_ACK : 0) + (flag_data ? FLAG_DATA : 0) + (flag_resend ? FLAG_RESEND : 0) + (flag_fragment ? FLAG_FRAGMENT : 0) + (flag_persist ? FLAG_PERSIST : 0));
 			
 			//Write window sequence
 			dos.writeInt(packet_seq);
 			dos.writeInt(window_size);
 			dos.writeShort(frag_nr);
 			dos.writeShort(frag_count);
 			
 			//Write static length fields
 			if(flag_ack) {
 				//ACK window start sequence
 				dos.writeInt(ack_window_start);
 
 				//ACK range element count
 				ack_count = ack_seq_data.size() / 2;
 				if(ack_count > RESERVED_ACK_COUNT) ack_count = RESERVED_ACK_COUNT;
 				dos.writeShort(ack_count);
 
 				//ACK-entries
 				for(int i = 0; i < ack_count * 2; i++) {
 					dos.writeShort(ack_seq_data.get(i));
 				}
 			}
 
 			//Write data
 			if(flag_data) dos.write(data);
 			
 			//Flush, close and write
 			dos.flush();
 			dos.close();
 			bos.close();
 			
 			return bos.toByteArray();
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void sendPacket(RUDPDatagramPacketSenderInterface sendInterface,RUDPLinkFailListener failListener,Timer timer,int timeout,int retries) {
 		//Set listener and timer
 		this.sendInterface = sendInterface;
 		this.failListener = failListener;
 		this.timer = timer;
 		this.maxAttempts = retries;
 		this.attempts = 0;
 
 		//Send packet
 		triggerSend(timeout);
 	}
 	
 	private void triggerSend(int timeout) {
 		boolean resend = false;
 		
 		//Set the resend flag if it is not the first attempt
 		if(attempts > 0) {
 			flag_resend = true;
 		}
 		
 		//Cancel old timer
 		synchronized(this) {
 			if(taskResend != null) {
 				taskResend.cancel();
 				taskResend = null;
 			}
 		
 			//TODO handle a failed packet
 			if(attempts >= maxAttempts) {
 				System.out.println("Packet failed - " + id);
 				
 				//Call the fail listener if one has been specified
 				if(failListener != null) {
 					failListener.eventLinkFailed();
 				}
 			} 
 			else {
 				//Sending should be outside of the sync block
 				resend = true;
 				
 				//Restart new timer
 				//The timeout is always doubled
 				taskResend = new TimeoutTask(timeout * 2);
 				timer.schedule(taskResend,timeout);
 	
 				//Increment retries
 				attempts++;
 			}
 		}
 		
 		if(resend) {
 			//Send packet
 			sendInterface.sendDatagramPacket(this);
 		}
 	}
 	
 	private class TimeoutTask extends TimerTask {
 		private int timeout;
 		
 		public TimeoutTask(int timeout) {		
 			this.timeout = timeout;
 		}
 		
 		@Override
 		public void run() {
 			triggerSend(timeout);
 		}
 	}
 	
 	public void acknowldege() {
 		//Cancel resend task
 		synchronized(this) {
 			if(taskResend != null) {
 				taskResend.cancel();
 				taskResend = null;
 			}
 		}
 	}
 	
 	public boolean isAcknowledged() {
 		synchronized(this) {
 			return taskResend == null;
 		}
 	}
 	
 	public String toString(int port) {
 		String result;
 		
 		result = ">>>>> Port: " + port +"\nID:\t\t0x" + Integer.toHexString(id).toUpperCase() + "\nFlags:\t\t";
 		result += flag_first ? "FIRST" : "";
 		result += (flag_reset ? ",RESET" : "");
 		result += (flag_ack ? ",ACK" : "");
 		result += (flag_fragment ? ",FRAGMENT" : "");
 		result += (flag_data ? ",DATA" : "");
 		result += (flag_resend ? ",RESEND" : "");
 		
 		//Window start
 		result += "\nWND_SIZE:\t" + window_size; 
 		result += "\nACK_WND_START:\t" + ack_window_start; 
 		if(flag_data || flag_first) result += "\nPACKET_SEQ:\t" + packet_seq + "\nATTEMPTS:\t" + attempts + "/" + maxAttempts;
 		if(flag_fragment) result += "\nFRAG_NR:\t" + frag_nr + "\nFRAG_COUNT:\t" + frag_count;
 		
 		if(flag_ack && ack_seq_data.size() > 0) {
 			result += "\nACK_RANGES:\t" + (new DeltaRangeList(ack_seq_data)).toString(ack_window_start);
 		}
 		
 		result += "\n<<<<<";
 
 		return result;
 	}
 	
 	//Set fragment information
 	public void setFragmentFlag(boolean flag) {
 		flag_fragment = flag;
 	}
 	
 	public void setFragment(short nr,short count) {
 		this.frag_nr = nr; 
 		this.frag_count = count;
		this.flag_fragment = count == 0 ? false : true;
 	}
 	
 	//Set acknowledge 
 	public void setACKData(int seq,List<Short> ack_data) {
 		if(ack_data == null) {
 			flag_ack = false;
 		}
 		else {
 			flag_ack = true;
 			this.ack_seq_data = ack_data;
 		}
 	}
 	
 	//Sequence window
 	public void setWindowSize(int window_size) {
 		this.window_size = window_size;
 	}
 
 	public void setData(byte[] data,boolean resend) {
 		flag_data = true;
 		flag_resend = resend;
 		this.data = data;
 	}
 	
 	public void setPacketSequence(int seq) {
 		this.packet_seq = seq;
 	}
 	
 	public void setFirstFlag(boolean flag) {
 		flag_first = flag;
 	}
 	
 	public void setResetFlag(boolean flag) {
 		flag_reset = flag;
 	}
 	
 	public void setPersistFlag(boolean flag) {
 		flag_persist = flag;
 	}
 }
