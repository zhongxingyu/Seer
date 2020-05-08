 package wifi;
 
 import java.io.PrintWriter;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import rf.RF;
 
 /**
  * Use this layer as a starting point for your project code. See
  * {@link Dot11Interface} for more details on these routines.
  * 
  * @author
  */
 
 public class LinkLayer implements Dot11Interface {
 	private RF theRF; // Simulates a physical layer for us to send on
 	private short ourMAC; // The address we are using 
 	private PrintWriter output; // The output stream we'll write to
 
 	private static final int QUEUE_SIZE = 10;
 	private static final int FULL_DEBUG = -1;
 	private int debug = 0;
 
 	private  BlockingQueue<Packet> in = new ArrayBlockingQueue(QUEUE_SIZE);
 	private  BlockingQueue<Packet> out = new ArrayBlockingQueue(QUEUE_SIZE);
 	
 	private HashMap<Short,Short> sendSequences = new HashMap();
 	private HashMap<Short,Short> recvSequences = new HashMap();
 	
 	private  HashMap<Short,ArrayList<Short>> recievedACKS = new HashMap();
 	
 	public synchronized BlockingQueue<Packet> getIn() { //These Queues will facilitate communication between the LinkLayer and its Sender and Receiver helper classes
 		return in;
 	}
 
 	public synchronized BlockingQueue<Packet> getOut() {
 		return out;
 	}
 
 	/**
 	 * Constructor takes a MAC address and the PrintWriter to which our output
 	 * will be written.
 	 * 
 	 * @param ourMAC
 	 *            MAC address
 	 * @param output
 	 *            Output stream associated with GUI
 	 */
 	public LinkLayer(short ourMAC, PrintWriter output) {
 		this.ourMAC = ourMAC;
 		this.output = output;
 		theRF = new RF(null, null);
 		output.println("LinkLayer: Constructor ran.");
 
 		Receiver theReceiver = new Receiver(this, theRF); //Creates the sender and receiver instances
 		Sender theSender = new Sender(this, theRF);
 
 		Thread r = new Thread(theReceiver); //Threads them
 		Thread s = new Thread(theSender);
 
 		r.start(); //Starts the threads running
 		s.start();
 	}
 
 	public short nextSeqNum(short addr){
 		short nextSeq;
 		if(sendSequences.containsKey(addr)){
 			nextSeq = (short) (sendSequences.get(addr)+1);
 		}
 		else{
 			nextSeq = 0;
 		}
 		this.sendSequences.put(addr, (short)(nextSeq));
 	    return nextSeq;
 	}
 	
 	
 	public short gotRecvSeqNum(short addr){
 		short nextSeq;
 		if(sendSequences.containsKey(addr)){
 			nextSeq = (short) (sendSequences.get(addr)+1);
 		}
 		else{
 			nextSeq = 0;
 		}
 		this.recvSequences.put(addr, (short)(nextSeq));
 	    return nextSeq;
 	}
 	
 	
 	/**
 	 * Send method takes a destination, a buffer (array) of data, and the number
 	 * of bytes to send. See docs for full description.
 	 */
 	public int send(short dest, byte[] data, int len) {
 		output.println("LinkLayer: Sending " + len + " bytes to " + dest);
 
 		byte[] fakeCRC = {-1, -1, -1, -1}; //Actual CRC stuff not implemented yet
 		
 		short seqNum = nextSeqNum(dest);
 
 		Packet p = new Packet(0, seqNum, dest, ourMAC, data, fakeCRC); //Builds a packet using the supplied data
                                                                           //Some parts of the packet are fake for now
 		output.println("NEXT SEQ TEST: Sending packet " + seqNum + "  to " + dest);
 		try {
 			out.put(p); //Puts the created packet into the outgoing queue
 		} catch (InterruptedException e) {
 			// Auto-generated catch block
 			e.printStackTrace();
 		}
 		return len;
 	}
 
 	/**
 	 * Recv method blocks until data arrives, then writes it an address info
 	 * into the Transmission object. See docs for full description.
 	 */
 	public int recv(Transmission t) { //Called by the above layer when it wants to receive data
 
 		Packet p;
 		try {
 			p = in.take();  //Grabs the next packet from the incoming queue
 			if(p.getSeqNum() < recvSequences.get(p.getSrcAddr())){
 				byte[] data = p.getData();  //Extracts the necessary parts from the packet and puts them into the supplied transmission object
 				t.setSourceAddr((short) p.getSrcAddr());
 				t.setDestAddr((short) p.getDestAddr());
 				t.setBuf(data);
 				return data.length; //Returns the length of the data recieved
 			}
 
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return -1;
 
 	}
 
 	/**
 	 * Returns a current status code. See docs for full description.
 	 */
 	public int status() {
 		output.println("LinkLayer: Faking a status() return value of 0");
 		return 0;
 	}
 
 	/**
 	 * Passes command info to your link layer. See docs for full description.
 	 */
 	public int command(int cmd, int val) {
 		switch (cmd) {
 		case 0:
 			output.println("Options & Settings:");
 			output.println("-----------------------------------------");
 			output.println("Cmd 0: \t View all options and settings.");
 			output.println("Cmd 1: \t Set debug value. Debug currently at " + debug);
 			output.println("\t Use -1 for full debug output, 0 for no output.");
 			output.println("Cmd 2: \t Set slot for link layer.");
 			output.println("Cmd 3: \t Set desired wait time between start of beacon transmissions (in seconds).");
 			output.println("-----------------------------------------");
 			break;
 		case 1:
 			if(val == FULL_DEBUG){
 				debug = FULL_DEBUG;
 			}
 			output.println("Setting debug to " + debug);
 			break;
 		case 2:
 			break;
 		case 3:
 			break;
 		default:
 			output.println("Command " + cmd + " not recognized.");
 		}
 		output.println("LinkLayer: Sending command " + cmd + " with value "
 				+ val);
 		return 0;
 	}
 
 	class Sender implements Runnable { //Handles sending functions for the LinkLayer
 
 		private RF theRF;
 		private LinkLayer theLinkLayer;
 
 		public Sender(LinkLayer thisLink, RF thisRF) {
 
 			theRF = thisRF;
 			theLinkLayer = thisLink;
 		}
 
 		public void run() {
 			while (true) {	
 				try {
 					//Dont forget about exponential backoff!
 					Thread.sleep(10); //Sleeps each time through, in order to not monopolize the CPU
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				
 				if (theLinkLayer.getOut().isEmpty() == false && theRF.inUse() == false) { // If there are Packets to be  sent in the  LinkLayer's  outbound queue
 					                                                                      //Also makes sure the RF is not in use
 					
 					Packet p = null;
 					
 					try {
 						p = theLinkLayer.getOut().take();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
                                                           
 					theRF.transmit(p.getFrame()); // Send the first packet out on the RF layer
 					output.println("SENT PACKET with SEQ NUM: "+ p.getSeqNum());
 					
 					if(debug == FULL_DEBUG){
 						output.println("Sent packet with sequence number" + p.getSeqNum() + " to MAC address " + p.getDestAddr());
 					}
 					
 					try {
 						Thread.sleep((long) 10);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					
 					int counter = 0;
 
 					while(counter < RF.dot11RetryLimit && (theLinkLayer.recievedACKS.containsKey(p.getDestAddr())&&theLinkLayer.recievedACKS.get(p.getDestAddr()).contains(p.getSeqNum()) == false)){
 
 						Packet retryPacket = new Packet(p.getFrameType(),p.getSeqNum(),p.getDestAddr(), p.getSrcAddr(), p.getData(), p.getCrc());
 						retryPacket.setRetry(true);
 						
 						if(debug == FULL_DEBUG){
 							output.println("Resending packet with sequence "+ p.getSeqNum()+". Attempt number: "+ counter);
 						}
 						
 						output.println("RESENDING PACKET: "+ retryPacket.getSeqNum()+" Attempt number: "+ counter);
 						theRF.transmit(retryPacket.getFrame()); // Send the first packet out on the RF layer
 						
 						try {
 							Thread.sleep((long) 10);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						
 						counter++;
 					}
 				}
 			}
 		}
 	}
 
 	class Receiver implements Runnable { //Handles receiving functions for the LinkLayer
 
 		private RF theRF;
 		private LinkLayer theLinkLayer;
 
 		public Receiver(LinkLayer thisLink, RF thisRF) {
 
 			theRF = thisRF;
 			theLinkLayer = thisLink;
 		}
 
 		public void run() {
 			while (true) {
 				try {
 					Thread.sleep(10); //Sleeps each time through, in order to not monopolize the CPU
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				Packet recvPacket = new Packet(theRF.receive()); // Gets data from the RF layer, turns it into packet form
 				
 				short destAddr = recvPacket.getDestAddr();
 				
 				if((destAddr&0xffff) == ourMAC || (destAddr&0xffff) == 65535){
 					output.println("Packet for us: "+ recvPacket.getSeqNum());	
 					
 					if(debug == FULL_DEBUG){
 						output.println("Packet for us arrived from " + recvPacket.srcAddr);
 					}
 					
 					if((destAddr&0xffff) == ourMAC && recvPacket.getFrameType() == 0){
 						
 						short nextSeq = gotRecvSeqNum(recvPacket.getSrcAddr());
 						if(recvPacket.getSeqNum() > nextSeq){
 							
 							output.println("Sequence out of order, expected: "+ nextSeq+ " got: "+ recvPacket.getSeqNum() );
 						}
 						try {
 							theLinkLayer.getIn().put(recvPacket); // Puts the new Packet into the LinkLayer's inbound queue
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						
 						
 						output.println("Got: "+ recvPacket.getSeqNum()+ " next is "+ nextSeq);	
 						
 						
 						byte[] fakeCRC = {-1, -1, -1, -1}; //Actual CRC stuff not implemented yet
 
 						Packet ack = new Packet(1, recvPacket.getSeqNum(), recvPacket.getSrcAddr(), ourMAC, null, fakeCRC);
 						
 						
 						try {
 							Thread.sleep(RF.aSIFSTime); //Sleeps to wait SIFS
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						
 						if(debug == FULL_DEBUG){
 							output.println("Sending ACK with sequence number " + ack.getSeqNum() + " to MAC address " + ack.getDestAddr());
 						}
 						
 						theRF.transmit(ack.getFrame());
 						output.println("Sent an ACK: " + ack.getSeqNum());
 					}
 					else if((destAddr&0xffff) == ourMAC && recvPacket.getFrameType() == 1){
 						output.println("Saw an ACK: " + recvPacket.getSeqNum());
 						
 						
 						if(theLinkLayer.recievedACKS.containsKey(recvPacket.getSrcAddr())){
 							
 							if(theLinkLayer.recievedACKS.get(recvPacket.getSrcAddr()).contains(recvPacket.getSeqNum())){
 								output.println("Already got this ACK: "+ recvPacket.getSeqNum());
 							}
 							else{
 								
 								theLinkLayer.recievedACKS.get(recvPacket.getSrcAddr()).add(recvPacket.getSeqNum());
 								output.println("Added an ACK for "+ recvPacket.getSeqNum()+ " from "+recvPacket.getSrcAddr());
 							}
 							
 						}
 						else{
 							ArrayList<Short> newHost = new ArrayList<Short>();
 							newHost.add(recvPacket.getSeqNum());
 							theLinkLayer.recievedACKS.put(recvPacket.getSrcAddr(), newHost);
 							output.println("Added an ACK for "+ recvPacket.getSeqNum()+ " from "+recvPacket.getSrcAddr());
 						}
 						
 						
 						
 					}
 					else{
 						output.println("Saw a packet of type: "+ recvPacket.getFrameType() + " from address "+ (destAddr&0xffff));
 					}
 				}
 				else{
 					output.println("Addr: "+ destAddr);
 				}
 			}
 		}
 	}
 }
