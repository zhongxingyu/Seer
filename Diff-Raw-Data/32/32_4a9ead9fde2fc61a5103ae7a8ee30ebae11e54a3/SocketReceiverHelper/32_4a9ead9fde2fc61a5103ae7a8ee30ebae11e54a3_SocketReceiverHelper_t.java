 //Package Declaration //
 package Third_Project_Code;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 //Java Package Support //
 
 //Internal Package Support //
 // { Not Applicable }
 
 
 /**
 * 
 * Project2/SocketReceiver.java
 * 
 * @author(s)	: Ian Middleton, Zach Ogle, Matthew J Swann
 * @version  	: 1.0
 * Last Update	: 2013-03-26
 * Update By		: Ian Middleton
 * 
 * 
 * Second_Project_Code PACKAGE :: Source code for Comp 6360: Wireless & Mobile Networks
 * 	               Assignment 2 :: VOIP
 * 
 * This is source code for the SocketReceiver class. This class accepts a
 * set of packets, unpacks the BYTES and plays back the sound.
 * 
 */
 
 
 public class SocketReceiverHelper implements Runnable{
 	
 	private DatagramSocket  s;
 	private DatagramPacket  dp;
 	
 	int number;
 	ArrayList<Node> links;
 	
 	ArrayList<NeighborRow> neighborTable = new ArrayList<NeighborRow>();
 	ArrayList<NeighborRow> newNeighborTable = new ArrayList<NeighborRow>();
 	ArrayList<int[]> topologyTable = new ArrayList<int[]>();
 	ArrayList<int[]> newTopologyTable = new ArrayList<int[]>();
 	private boolean neighborTableUpdated = false;
 	private boolean topologyTableUpdated = false;
 	
 	private boolean running = true;
 	private Object runLock = new Object();
 	private Object neighborTableLock = new Object();
 	private Object topologyTableLock = new Object();
 	
 	/**
 	 * Base constructor.
 	 * 
 	 * @throws IOException			: General IOException for package functions.
 	 * @throws LineUnavailable		: General LineUnavailable for package 
 	 * 										functions.
 	 */
 	public SocketReceiverHelper(int number, ArrayList<Node> linkedNodes){
 		try {
 			s       = new DatagramSocket();
 		} catch (SocketException e) {
 			System.out.println("SocketSender: Socket Creation Problem");
 		}
 		
 		this.number = number;
 		links = linkedNodes;
 	} // end SocketReceiver()
 	
 	public void updateNeighborTable(ArrayList<NeighborRow> newTable){
 		synchronized(neighborTableLock){
 			newNeighborTable = newTable;
 			neighborTableUpdated = true; 
 		}
 	}
 	
 	public void updateTopologyTable(ArrayList<int[]> newTable){
 		synchronized(topologyTableLock){
 			newTopologyTable = newTable;
 			topologyTableUpdated = true; 
 		}
 	}
 	
 	private byte[] createHeader(byte[] packet, int srcAddress, int destAddress){
 		packet[0]   = -128;
 		packet[1]   = -128;
 		
 		// Add source address to the packet
 		packet[2] = (byte)((srcAddress/256)-128);
 		packet[3] = (byte)((srcAddress%256)-128);
 		
 		// Add destination address to the packet
 		packet[4] = (byte)((destAddress/256)-128);
 		packet[5] = (byte)((destAddress%256)-128);
 		
 		// Add previous hop to the packet
 		packet[6] = (byte)((srcAddress/256)-128);
 		packet[7] = (byte)((srcAddress%256)-128);
 		
 		return packet;
 	}
 	
 	private void sendPacket(byte[] packet, String destAddress, int port){
 		InetAddress address;
 		
 		try {
 			address = InetAddress.getByName(destAddress);
 		} catch (UnknownHostException e) {
 			address = null;
 			System.out.println("SocketReceiverHelper: Can't find address.");
 		}
 		
 		dp       = new DatagramPacket(packet, packet.length, address, port);
 		
 		try{
 			s.send(dp);
			//System.out.println("Sending packet: " + (((dp.getData()[0] + 128) * 256) + dp.getData()[1] + 128) + "	" + (((dp.getData()[2] + 128) * 256) + dp.getData()[3] + 128) + "	" + (((dp.getData()[4] + 128) * 256) + dp.getData()[5] + 128) + "	" + (((dp.getData()[6] + 128) * 256) + dp.getData()[7] + 128));
 		}// end try
 		catch (IOException e){
 			System.out.println("SocketReceiverHelper: Unable to send datagram packet.");
 		}// end catch
 		
 	}
 	
 	public void sendNeighborMessages(){
 		int srcAddress, destAddress;
 		String address;
 		int port;
 		
 		int length = 10 + (neighborTable.size()*3);
 		byte[] packet = new byte[length];			
 		
 		packet[8] = (byte)(1-128);
 		packet[9] = (byte)(10 + (neighborTable.size()*3)-128);
 		
		int j = 0;
		for(int i = 10; i < length; i+=3){
			System.out.println(neighborTable.get(j).getNodeNumber());
			System.out.println(neighborTable.get(j).getLinkStatus());
			packet[i] = (byte)((neighborTable.get(j).getNodeNumber()/256)-128);
			packet[i+1] = (byte)((neighborTable.get(j).getNodeNumber()%256)-128);
			packet[i+2] = (byte)((neighborTable.get(j).getLinkStatus())-128);
			j++;
 		}
 		
 		for(int i = 0; i < links.size(); i++){				
 				srcAddress = number;
 				destAddress = links.get(i).getNumber();
 				packet = createHeader(packet, srcAddress, destAddress);
 				
 				address = links.get(i).getAddress();
 				port = links.get(i).getPort();
 				sendPacket(packet, address, port);
 		}
 	}
 	
 	public void sendTopologyMessages(){
 		
 	}
 	
 	/**
 	 * Terminate can be called to terminate execution of the thread. A join should be
 	 * called afterward in order to wait for the thread to finish.
 	 */
 	public void terminate(){
 		synchronized(runLock){
 			running = false;
 		}
 	} // end terminate()
 	
 	/**
 	 * Run command called automatically by Thread.start().
 	 * 
 	 * @throws IOException			: General IOException for package functions.
 	 * @throws LineUnavailable		: General LineUnavailable for package 
 	 * 										functions.
 	 */
 	@Override
 	public void run(){
 		
 		while(true){
 			synchronized(runLock){
 				if(!running){
 					break;
 				}
 			}
 				
 			synchronized(neighborTableLock){
 				if(neighborTableUpdated){
 					neighborTable = newNeighborTable;
 					neighborTableUpdated = false;
 				}
 			}
 			
 			synchronized(topologyTableLock){
 				if(topologyTableUpdated){
 					topologyTable = newTopologyTable;
 					topologyTableUpdated = false;
 				}
 			}
 			
 			sendNeighborMessages();
 			sendTopologyMessages();
 			
 			for(int i = 0; i < 4; i++){
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					System.out.println("SocketReceiverHelper: Thread interrupted while sleeping.");
 				}
 				synchronized(runLock){
 					if(!running){
 						break;
 					}
 				}
 			}
 		}
 		s.close();
 	} // end SocketReceiver.run()
 
 	
 } // end SocketReceiver class
