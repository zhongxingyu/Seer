 package communication.rudp.socket;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 import java.util.concurrent.Semaphore;
 
 import javax.jws.Oneway;
 
 import communication.rudp.socket.exceptions.InvalidRUDPPacketException;
 import communication.rudp.socket.listener.RUDPLinkTimeoutListener;
 import communication.rudp.socket.listener.RUDPReceiveListener;
 import communication.rudp.socket.rangeset.DeltaRangeList;
 
 public class RUDPLink implements RUDPPacketSenderInterface {
 	private static final int MAX_ACK_DELAY = 100;
 	private static final int WINDOW_SIZE = 4;
 	private static final int PERSIST_PERIOD = 1000;
 	
 	//Global variables
 	private InetSocketAddress sa;
 	private Timer timer;
 	private int avg_RTT;
 	private Semaphore semaphoreWindowSize;
 
 	//Listener  & interfaces
 	private RUDPSocketInterface socket;
 	private RUDPLinkTimeoutListener listener_timeout;
 	private RUDPReceiveListener listener_receive;
 	
 	//Sender stuff
 	private int currentPacketSeq;		//Current packet sequence number
 	private boolean isSynced = false;	//Is the window in sync with the other side?!?
 	private boolean isFirst = true;		//The first packet must be marked as that!
 	
 	//Contains sent unacknowledged packets
 	private HashMap<Integer,RUDPDatagramPacket> packetBuffer_out;
 	
 	//Receiver list
 	//private int currentReceivePointer;
 	private TreeMap<Integer,RUDPDatagram> packetBuffer_in;
 	
 	//Acknowledge stuff
 	private int ackRangeOffset;
 	private DeltaRangeList ackRange;
 	private TimerTask task_ack;
 	
 	public RUDPLink(InetSocketAddress sa,RUDPSocketInterface socket,RUDPLinkTimeoutListener listener_to,RUDPReceiveListener listener_recv,Timer timer) {
 		//Create data structures
 		packetBuffer_out = new HashMap<Integer,RUDPDatagramPacket>();
 		packetBuffer_in = new TreeMap<Integer,RUDPDatagram>();
 		ackRange = new DeltaRangeList();
 		
 		//Set timer and listener
 		this.listener_timeout = listener_to;
 		this.listener_receive = listener_recv;
 		this.socket = socket;
 		this.semaphoreWindowSize = new Semaphore(1,true);
 		
 		//Set or create timer
 		if(timer == null) {
 			this.timer = new Timer();
 		}
 		else {
 			this.timer = timer;
 		}
 		
 		//Network address
 		this.sa = sa;
 	}
 	
 	public InetSocketAddress getSocketAddress() {
 		return sa;
 	}
 	
 	public void send(RUDPDatagram datagram) throws InterruptedException {
 		List<RUDPDatagramPacket> packetList = new ArrayList<RUDPDatagramPacket>();
 		RUDPDatagramPacket packet;
 		int dataSize;
 		int dataLen;
 		int remainingPacketLength;
 		
 		//Create new packet
 		packet = new RUDPDatagramPacket(timer,this);
 		//packet.setDataFlag(true);
 		
 		//Length of the data to send
 		dataSize = datagram.getData().length;
 		dataLen = dataSize;
 		
 		if(dataSize > packet.getMaxDataLength()) {
 			short fragmentCounter = 0;
 			
 			//For each fragment
 			for(int offset = 0; offset < dataSize;) {
 				//Create datagram packet
 				packet.setFragment(fragmentCounter,(short)0);
 				remainingPacketLength = packet.getMaxDataLength();
				packet.setData(datagram.getData(), offset,remainingPacketLength < dataLen ? remainingPacketLength : dataLen , false);
 				packetList.add(packet);
 				
 				//Increment offset
 				offset += remainingPacketLength;
 				dataLen -= remainingPacketLength; 
 				
 				//Create new packet
 				packet = new RUDPDatagramPacket(timer,this);
 				//packet.setDataFlag(true);
 				
 				//Increment fragment and sequence counter
 				fragmentCounter++;
 			}
 			
 			//Set fragment count, because now we know it
 			for(RUDPDatagramPacket p: packetList) {
 				p.setFragment(p.getFragmentNr(), (short)packetList.size());
 			}
 		}
 		else {
 			//NO fragmentation
			packet.setData(datagram.getData(),0, datagram.getData().length, false);
 			packetList.add(packet);
 		}
 		
 		//Send packets
 		for(RUDPDatagramPacket p: packetList) {
 			//Enter the semaphore to stay within window size
 			semaphoreWindowSize.acquire();
 			
 			//Add packet to out list
 			synchronized(this) {
				p.setPacketSequence(currentPacketSeq);
 				packetBuffer_out.put(currentPacketSeq,p);
 
 				//Increment sequence number
 				currentPacketSeq++;
 			}
 			
 			
 			//Forward to socket interface
 			//TODO replace with a nice function
 			//p.triggerSend(avg_RTT * 1.5);
 			p.triggerSend(1000);
 		}
 	}
 	
 	public void putReceivedData(byte[] data,int len) {
 		RUDPDatagramPacket packet;
 
 		try {	
 			//Extract packet
 			packet = new RUDPDatagramPacket(data);
 		}
 		catch (InvalidRUDPPacketException e1) {
 			System.out.println("INVALID PACKET RECEIVED");
 			return;
 		}
 		
 		System.out.println("RECEIVE\n" + packet.toString() + "\n");
 		
 		//Handle reset packets
 		handleResetFlag(packet);
 
 		//Handle ACK-data
 		handleAckData(packet);
 		
 		//Handle new window sequence
 		handleAckWindowSequence(packet);
 		
 		//Handle payload data
 		handlePayloadData(packet);
 		
 		System.out.println("AckRangeOffset:\t" + ackRangeOffset + "\n");
 	}
 	
 	private void handleAckData(RUDPDatagramPacket packet) {
 		RUDPDatagramPacket ack_pkt;
 		DeltaRangeList rangeList;
 		int ackSeqOffset;
 		
 		//First process acknowledge data, if present
 		if(packet.getFlag(RUDPDatagramPacket.FLAG_ACK)) {
 			//Recreate range list
 			rangeList = new DeltaRangeList(packet.getAckSeqData());
 			ackSeqOffset = packet.getAckSeqOffset();
 			
 			synchronized(this) {
 				//Acknowledge all packets
 				for(Integer i: rangeList.toElementArray()) {
 					ack_pkt = packetBuffer_out.remove(i + ackSeqOffset);
 					if(ack_pkt != null) {
 						ack_pkt.acknowldege();
 					}
 				}
 			}
 		}
 	}
 	
 	//Handle reset flag
 	private void handleResetFlag(RUDPDatagramPacket packet) {
 		if(packet.getFlag(RUDPDatagramPacket.FLAG_RESET)) {
 			synchronized(this) {
 				//Reset link state
 				isFirst = true;
 				isSynced = false;
 			
 				//Clear internal data structures to have a new start
 				ackRange.clear();
 				packetBuffer_in.clear();
 				packetBuffer_out.clear();
 				
 				//TODO reset semaphore
 				semaphoreWindowSize.drainPermits();
 				semaphoreWindowSize.release(packet.getRemainingWindowSize());
 			}
 		}
 	}
 	
 	private void handleAckWindowSequence(RUDPDatagramPacket packet) {
 		//Calculate delta to old window
 		int delta;
 
 		synchronized(this) {
 			if(packet.getFlag(RUDPDatagramPacket.FLAG_FIRST)) {
 				//First packet => take ACK-window as the new window start
 				ackRangeOffset = packet.getPacketSeq();
 				
 				//We are sync'ed now
 				isSynced = true;
 			}
 			else if(packet.getFlag(RUDPDatagramPacket.FLAG_DATA)) {
 				if(isSynced) {
 					//Calculate window shift / check for overflow
 					delta = packet.getPacketSeq() - ackRangeOffset;
 		
 					//Check if the window in [0,WINDOW_SIZE]
 					if(delta < 0 || delta > WINDOW_SIZE) {
 						System.out.println("UNSYNCHRONIZED PACKET RECEIVED - OUT OF WINDOW BOUNDS");
 						return;
 					}
 					
 					//Shift range and foreign window
 					ackRange.shiftRanges((short)(-1 * delta));
 					
 					//Release semaphore delta times
 					semaphoreWindowSize.release(delta);
 				}
 				else {
 					//Send a reset packet, because we need a first packet for synchronization
 					RUDPDatagramPacket resetPacket = new RUDPDatagramPacket();
 					
 					//Send
 					resetPacket.setResetFlag(true);
 					sendPacket(resetPacket);
 					
 					System.out.println("UNSYNCHRONIZED PACKET RECEIVED - FIRST PACKET MISSING");
 				}
 			}
 		}
 	}
 	
 	private void handlePayloadData(RUDPDatagramPacket packet) {
 		RUDPDatagram dgram;
 		int newRangeElement;
 		List<RUDPDatagram> readyDatagrams = null;
 		
 		//Process data packet
 		synchronized(this) {
 			if(isSynced && packet.getFlag(RUDPDatagramPacket.FLAG_DATA)) {
 				//Check if packet is within window bounds
 				if((packet.getPacketSeq() - ackRangeOffset) < 0 || (packet.getPacketSeq() - ackRangeOffset) > WINDOW_SIZE) {
 					System.out.println("INVALID PACKET RECEIVED - PACKET SEQ OUT OF WINDOW BOUNDS");
 				}
 				else {
 					//Insert into receiving packet buffer
 					if(packet.getFlag(RUDPDatagramPacket.FLAG_FRAGMENT)) {
 						if((dgram = packetBuffer_in.get(packet.getPacketSeq() - packet.getFragmentNr())) == null) {
 							//Create new fragmented datagram
 							dgram = new RUDPDatagram(sa.getAddress(), sa.getPort(), packet.getData());
 							packetBuffer_in.put(packet.getPacketSeq() - packet.getFragmentNr(),dgram);
 						}
 						else {
 							//We are the Borg, and we will...
 							dgram.assimilateFragment(packet);
 						}
 					}
 					else {
 						//Create new normal datagram
 						dgram = new RUDPDatagram(sa.getAddress(), sa.getPort(), packet.getData());
 						packetBuffer_in.put(packet.getPacketSeq() - packet.getFragmentNr(),dgram);
 					}
 					
 					//Calculate relative position and add to packet range list
 					newRangeElement = packet.getPacketSeq() - ackRangeOffset;
 					ackRange.add((short)newRangeElement);
 					
 					//Start ACK-timer, if necessary...
 					//...or send immediately if it is the very first packet
 					//to speed up the window-size negotiation process
 					if(packet.getFlag(RUDPDatagramPacket.FLAG_FIRST)) {
 						sendPacket(new RUDPDatagramPacket());
 					}
 					else {
 						if(task_ack == null) {
 							task_ack = new AcknowledgeTask();
 							timer.schedule(task_ack, MAX_ACK_DELAY);
 						}
 					}
 					
 					//Datagrams ready for delivery
 					readyDatagrams = new ArrayList<RUDPDatagram>();
 					
 					//Forward all ready packets to upper layer
 					while((dgram = packetBuffer_in.get(ackRangeOffset)) != null)  {
 						if(dgram.isComplete()) {
 							//Remove from list
 							packetBuffer_in.remove(ackRangeOffset);
 							
 							//Remember ready datagrams
 							readyDatagrams.add(dgram);
 							
 							//Shift receive pointer
 							ackRangeOffset += dgram.getFragmentCount();
 						}
 						else break;
 					}
 				}
 			}
 		}
 		
 		//Forward all ready datagrams to upper layer
 		if(readyDatagrams != null) {
 			for(RUDPDatagram d: readyDatagrams) listener_receive.onRUDPDatagramReceive(d);
 		}
 	}
 	
 	private void setAckStream(RUDPDatagramPacket packet) {
 		//Check if we can acknowledge something
 		synchronized(this) {
 			if(!ackRange.isEmpty()) {
 				List<Short> ackList;
 				
 				//Put ACK stream into packet
 				ackList = ackRange.toDifferentialArray();
 				if(ackList.size() == 0) ackList = ackRange.toDifferentialArray();
 				packet.setACKData(ackRangeOffset,ackList);
 				
 				//DEBUG - report if the ACK list did not fit into one packet
 				if(ackList.size() > RUDPDatagramPacket.RESERVED_ACK_COUNT) {
 					System.out.println("RESERVED_ACK_COUNT OVERFLOW");
 				}
 				
 				//TODO there could be more ranges then we are able to send
 				//in one packet. test if this is fine
 				//Disable ACK timer
 				if(task_ack != null) {
 					task_ack.cancel();
 					task_ack = null;
 				}
 			}
 			else {
 				//Remove ACK data
 				packet.setACKData(0, null);
 			}
 		}
 	}
 	
 	private void setWindowSequence(RUDPDatagramPacket packet) {
 		synchronized(this) {
 			//When all packets have been passed to the upper layer it is window size
 			if(packetBuffer_in.isEmpty()) {
 				packet.setRemainingWindowSize(WINDOW_SIZE);
 			}
 			else {
 				//Calculate current window size
 				//The size is WINDOW_SIZE - (last packet seq - packet seq of first gap)
 				packet.setRemainingWindowSize(WINDOW_SIZE - (packetBuffer_in.lastKey() - ackRangeOffset));
 			}
 		}
 	}
 	
 	@Override
 	public void sendPacket(RUDPDatagramPacket p) {
 		//Add the ACK overlay stream
 		setAckStream(p);
 		setWindowSequence(p);
 		
 		//Set first flag at first packet
 		synchronized(this) {
 			if(isFirst) {
				p.setFirstFlag(true);
 				isFirst = false;
 			}
 		}
 
 		//TODO remove debug output
 		System.out.println("SEND\n" + p.toString() + "\n");
 
 		//Send packet
 		socket.triggerSend(this, p);
 	}
 	
 	private class AcknowledgeTask extends TimerTask {
 		@Override
 		public void run() {
 			//Send new empty packet that will contain ACK data
 			RUDPDatagramPacket packet = new RUDPDatagramPacket();
 			sendPacket(packet);
 		}
 	}
 }
