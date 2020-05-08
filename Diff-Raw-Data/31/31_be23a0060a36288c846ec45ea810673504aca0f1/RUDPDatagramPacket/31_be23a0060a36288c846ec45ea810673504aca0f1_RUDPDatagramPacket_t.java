 package communication.rudp.socket;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import communication.rudp.socket.listener.exceptions.InvalidRUDPPacketException;
 
 public class RUDPDatagramPacket {
 	private static final int MAX_PACKET_SIZE = 65535;
 	private static final int MAX_PACKET_RETRY = 3;
 	private static final int RESERVED_ACK_LENGTH = 32;
 	private static final int RESERVED_ACK_SIZE = RESERVED_ACK_LENGTH * 2 * (Short.SIZE / 8) + Integer.SIZE / 8;
 	
 	//Flags
 	public static final int FLAG_FIRST = 1;
 	public static final int FLAG_RESET = 2;
 	public static final int FLAG_ACK = 4;
 	public static final int FLAG_DATA = 8;
 	public static final int FLAG_RESEND = 16;
 	public static final int FLAG_FRAGMENT = 32;
 	
 	private boolean flag_first = false;
 	private boolean flag_reset = false;
 	private boolean flag_ack = false;
 	private boolean flag_data = false;
 	private boolean flag_resend = false;
 	private boolean flag_fragment = false;
 	
 	private Timer timer;
 	private TimerTask task_resend;
 	private RUDPPacketSenderInterface listener;
 	private int retries = 0;
 
 	//Sequence of this packet
 	private int seq;
 	private int seq_window;
 	
 	//If this packet is part of a fragmented datagram,
 	//these flags indicate the first and last fragment
 	private short frag_nr;
 	private short frag_count;
 
 	//ACK data
 	int ack_seq;
 	List<Short> ack_data;
 	
 	//Data
 	int data_off;
 	int data_len;
 	byte[] data;
 	
 	public RUDPDatagramPacket() {
 		//Create an empty packet without resend-timer stuff
 		timer = null;
 		listener = null;
 	}
 		
 	public RUDPDatagramPacket(Timer timer,RUDPPacketSenderInterface listener) {
 		this.timer = timer;
 		this.listener = listener;
 	}
 
 	public RUDPDatagramPacket(byte[] packet) throws InvalidRUDPPacketException,IOException {
 		ByteArrayInputStream bis;
 		DataInputStream dis;
 		int flag;
 		int ack_count = 0;
 		int ack_size;
 		
 		bis = new ByteArrayInputStream(packet);
 		dis = new DataInputStream(bis);
 		
 		//Read flag
 		flag = dis.readByte();
 		flag_first = (flag & FLAG_FIRST) != 0 ? true : false; 
 		flag_reset = (flag & FLAG_RESET) != 0 ? true : false; 
 		flag_ack = (flag & FLAG_ACK) != 0 ? true : false; 
 		flag_data = (flag & FLAG_DATA) != 0 ? true : false; 
 		flag_fragment = (flag & FLAG_FRAGMENT) != 0 ? true : false; 
 		flag_resend = (flag & FLAG_RESEND) != 0 ? true : false;
 		
 		//Read static fields
 		seq_window = dis.readInt();
 		if(flag_data) seq = dis.readInt();
 		if(flag_ack) ack_count = dis.readShort();
 		if(flag_fragment) {
 			frag_nr = dis.readShort();
 			frag_count = dis.readShort();
 		}
 		
 		//Read data and check limits before
 		ack_size = ack_count * 2 * Short.SIZE/8 + Integer.SIZE/8;
 		if(ack_size > RESERVED_ACK_SIZE) throw new InvalidRUDPPacketException();
 		if((bis.available() - ack_size) < 0) throw new InvalidRUDPPacketException();
 		data = new byte[bis.available() - ack_size];
 
 		//Read variable length fields
 		if(flag_ack) {
 			ack_seq = dis.readInt();
 			ack_data = new ArrayList<Short>();
 			for(int i = 0; i < ack_count; i++) {
 				ack_data.add(dis.readShort());				
 			}
 		}
 		
 		dis.read(data);
 		dis.close();
 		bis.close();
 	}
 
 	//Set fragment information
 	public void setFragmentFlag(boolean flag) {
 		flag_fragment = flag;
 	}
 	
 	public void setFragment(short nr,short count) {
 		this.flag_fragment = true;
 		this.frag_nr = nr; 
 		this.frag_count = count;
 	}
 	
 	//Set acknowledge 
 	public void setACK(int ack_seq,List<Short> ack_data) {
 		if(ack_data == null) {
 			flag_ack = false;
 		}
 		else {
 			flag_ack = true;
 			this.ack_data = ack_data;
 			this.ack_seq = ack_seq;
 		}
 	}
 	
 	//Set data
 	public void setDataFlag(boolean flag) {
 		flag_data = flag;
 		data = new byte[0];
 	}
 	
 	//Sequence window
 	public void setWindowSequence(int seq_window) {
 		this.seq_window = seq_window;
 	}
 	
 	public int getWindowSequence() {
 		return seq_window;
 	}
 	
 	public void setData(byte[] data,int data_off,int data_len,int seq,boolean resend) {
 		flag_data = true;
 		flag_resend = resend;
 		this.data_off = data_off;
 		this.data_len = data_len;
 		this.data = data;
 		this.seq = seq;
 	}
 	
 	public Boolean getFlag(int flag) {
 		switch(flag) {
 			case FLAG_FIRST: return flag_first;
 			case FLAG_RESET: return flag_reset;
 			case FLAG_ACK: return flag_ack;
 			case FLAG_DATA: return flag_data;
 			case FLAG_RESEND: return flag_resend;
 			case FLAG_FRAGMENT: return flag_fragment;
 			default: return null;
 		}
 	}
 
 	public byte[] serializePacket() {
 		ByteArrayOutputStream bos;
 		DataOutputStream dos;
 		int ack_size;
 		int ack_count = 0;
 		
 		try {
 			bos = new ByteArrayOutputStream();
 			dos = new DataOutputStream(bos);
 			
 			//Write flags
 			dos.writeByte((flag_first ? FLAG_FIRST : 0) + (flag_reset ? FLAG_RESET : 0) + (flag_ack ? FLAG_ACK : 0) + (flag_data ? FLAG_DATA : 0) + (flag_resend ? FLAG_RESEND : 0) + (flag_fragment ? FLAG_FRAGMENT : 0));
 			
 			//Write window sequence
 			dos.writeInt(seq_window);
 			
 			//Write sequence
 			if(flag_data) dos.writeInt(seq);
 
 			//Write static length fields
 			if(flag_ack) {
				ack_count = ack_data.size();
				if(ack_count > RESERVED_ACK_LENGTH) ack_count = RESERVED_ACK_LENGTH;
				dos.writeShort(ack_count);
 			}
 
 			//Write fragment counters
 			if(flag_fragment) {
 				dos.writeShort(frag_nr);
 				dos.writeShort(frag_count);
 			}
 			
 			//Write data
 			if(flag_data) dos.write(data,data_off,data_len);
 
 			//Write variable length fields
 			if(flag_ack) {
 				dos.write(ack_seq);
				for(int i = 0; i < ack_count; i++) {
					dos.writeShort(ack_data.get(i));
 				}
 			}
 			
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
 	
 	public int getRemainingLength() {
 		//Subtract the IP and UDP header
 		int size;
 		
 		//Packet size minus IP, UDP header
 		size = MAX_PACKET_SIZE - 20 - 8;
 
 		//Minus reserved space for acknowledgements
 		size -= RESERVED_ACK_SIZE;
 		
 		//Flag
 		size -= Byte.SIZE / 8;
 		
 		//Window sequence number
 		size -= Integer.SIZE;
 
 		//Sequence number
 		if(flag_data) size -= Integer.SIZE / 8;
 		
 		//Fragment
 		if(flag_fragment) size -= 2 * (Short.SIZE / 8);
 		
 		if(flag_ack) {
 			//ACK length
 			size -= Short.SIZE / 8;
 
 			//ACK field + ACK sequence number
 			size -= ack_data.size() * (Short.SIZE / 8) + Integer.SIZE / 8;
 		}
 		
 		//Data field
 		if(flag_data) size -= data.length * (Byte.SIZE / 8);
 		return size;
 	}
 	
 	public Short getFragmentNr() {
 		return frag_nr;
 	}
 	
 	public Short getFragmentCount() {
 		return frag_count;
 	}
 	
 	public void triggerSend(int timeout) {
 		//Set the resend flag if it is not the first attempt
 		if(retries > 0) {
 			flag_resend = true;
 		}
 		
 		//Cancel old timer
 		if(task_resend != null) {
 			task_resend.cancel();
 		}
 		
 		//TODO handle a failed packet
 		if(retries >= MAX_PACKET_RETRY) {
 			//Do not schedule the TimerTask again
 			System.out.println("PACKET failed");
 		} 
 		else {
 			//Restart new timer
 			task_resend = new TimeoutTask(timeout * 2);
 			timer.schedule(task_resend,timeout);
 			
 			//Send packet
 			listener.sendPacket(this);
 			
 			//Increment retries
 			retries++;
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
 		task_resend.cancel();
 	}
 	
 	public int getSequenceNr() {
 		return seq;
 	}
 	
 	public List<Short> getAckData() {
 		return ack_data;
 	}
 	
 	public int getAckSeq() {
 		return ack_seq;
 	}
 	
 	public void setFirstFlag(boolean flag) {
 		flag_first = flag;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + seq;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		RUDPDatagramPacket other = (RUDPDatagramPacket) obj;
 		if (seq != other.seq)
 			return false;
 		return true;
 	}
 }
