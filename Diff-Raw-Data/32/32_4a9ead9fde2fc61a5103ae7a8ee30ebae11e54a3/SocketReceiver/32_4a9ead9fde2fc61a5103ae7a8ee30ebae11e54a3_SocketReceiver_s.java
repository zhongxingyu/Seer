 //Package Declaration //
 package Third_Project_Code;
 
 //Java Package Support //
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 //import java.net.InetAddress;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import java.util.ArrayList;
 
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
 
 
 public class SocketReceiver implements Runnable{
 	// Constants
 	public static final int RESET_MESSAGE = 0;
 	public static final int HELLO_MESSAGE = 1;
 	public static final int TC_MESSAGE = 2;
 	public static final int UNIDIRECTIONAL = 0;
 	public static final int BIDIRECTIONAL = 1;
 	public static final int MPR = 2;
 	
 	// Audio Variables
 	AudioFormat format;
 	
 	// Transmit Variables
 	DatagramPacket dp;
 	DatagramSocket s;
 	SourceDataLine sLine;
 	SocketSender sender;
 	
 	// Control Variables
 	boolean running = true;
 	byte[] buf;
 	int number, port, x, y;
 	String address;
 	ArrayList<Node> nodes;
 	ArrayList<int[]> cache = new ArrayList<int[]>();
 	ArrayList<NeighborRow> neighborTable = new ArrayList<NeighborRow>();
 	ArrayList<int[]> topologyTable = new ArrayList<int[]>();
 	ArrayList<int[]> routingTable = new ArrayList<int[]>();
 	byte[] playbuf = new byte[120];
 	
 	SocketReceiverHelper helper;
 	Thread helperThread;
 	
 	/**
 	 * Base constructor.
 	 * 
 	 * @throws IOException			: General IOException for package functions.
 	 * @throws LineUnavailable		: General LineUnavailable for package 
 	 * 										functions.
 	 */
 	public SocketReceiver(int nodeNum, String address, int port, ArrayList<Node> linkedNodes, int x, int y){
 		this.buf    = new byte[128];
 		
 		try {
 			this.s      = new DatagramSocket(port);
 		} catch (SocketException e1) {
 			System.out.println("SocketReciever: Socket Creation Problem");
 		}
 		
 		this.dp     = new DatagramPacket(buf, buf.length);
 		this.format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100,
 												16, 2, 4, 44100, false);		
 		DataLine.Info sLineInfo = new DataLine.Info(SourceDataLine.class, this.format);
 		
 		try {
 			this.sLine   = (SourceDataLine)AudioSystem.getLine(sLineInfo);
 		} catch (LineUnavailableException e) {
 			System.out.println("SocketReceiver: Missing Data Line (SocketReceiver)");
 		}
 		
 		this.nodes   = linkedNodes;
 		this.number  = nodeNum;
 		this.address = address;
 		this.port    = port;
 		this.x = x;
 		this.y = y;
 		
 		sender = new SocketSender();
 		
 	} // end SocketReceiver()
 	
 	public Node getNode(int nodeNum){
 		for (int count = 0; count < nodes.size(); count++)
 		{
 			if (nodes.get(count).getNumber() == nodeNum)
 			{
 				return nodes.get(count);
 			}
 		}
 		return null;
 	}
 	
 	public boolean checkTable(int sequence, int source, int destination){
 		
 		for (int count = 0; count < cache.size(); count++){
 			if (cache.get(count)[0] == source && cache.get(count)[1] == destination){
 				
 				if (cache.get(count)[2] < sequence){
 					cache.get(count)[2] = sequence;
 					return false;
 				}
 				else if(sequence == 0){
 					cache.get(count)[2] = 0;
 					return true;
 				}else{
 					return true;
 				}
 			}
 		}
 		
 		cache.add(new int[3]);
 		cache.get(cache.size()-1)[0] = source;
 		cache.get(cache.size()-1)[1] = destination;
 		cache.get(cache.size()-1)[2] = sequence;
 				
 		return false;
 	}
 	
 	public void playPacket(){
 		System.arraycopy(this.dp.getData(), 8, playbuf, 0, playbuf.length);
		System.out.println("Playing packet: " + (((this.dp.getData()[0] + 128) * 256) + this.dp.getData()[1] + 128) + "	" + (((this.dp.getData()[2] + 128) * 256) + this.dp.getData()[3] + 128) + "	" + (((this.dp.getData()[4] + 128) * 256) + this.dp.getData()[5] + 128) + "	" + (((this.dp.getData()[6] + 128) * 256) + this.dp.getData()[7] + 128));
 		this.sLine.write(playbuf, 0, playbuf.length);
 	}
 	
 	public void forwardPacket(int prevHop){
 		byte[] buffer = dp.getData();
 		buffer[6] = (byte)((number / 256) - 128);
 		buffer[7] = (byte)((number % 256) - 128);
 	
 		for (int count = 0; count < nodes.size(); count++){
 			
 			if (nodes.get(count).getNumber() != prevHop){
 				sender.forward(nodes.get(count).getAddress(), nodes.get(count).getPort(), buffer);
 			}
 		}
 	}
 	
 	public void updateMPR()
 	{
 		ArrayList<NeighborRow> tempTable = new ArrayList<NeighborRow>();
 		for (int i = 0; i < neighborTable.size(); i++)
 		{
 			tempTable.add(neighborTable.get(i));
 		}
 		int size = tempTable.size();
 		for (int i = 0; i < size; i++)
 		{
 			int maxSize = 0, index = -1, numNode = 0;
 			for (int j = 0; j < tempTable.size(); j++)
 			{
 				if (tempTable.get(j).getTwoHopNeighbors().size() > maxSize)
 				{
 					maxSize = tempTable.get(j).getTwoHopNeighbors().size();
 					numNode = tempTable.get(j).getNodeNumber();
 					index = j;
 				}
 			}
 			if (maxSize != 0)
 			{
 				for (int j = 0; j < neighborTable.size(); j++)
 				{
 					if (neighborTable.get(j).getNodeNumber() == numNode)
 					{
 						neighborTable.get(j).setLinkStatus(2);
 						break;
 					}
 				}
 				for (int j = 0; j < tempTable.get(index).getTwoHopNeighbors().size(); j++)
 				{
 					int neighbor = tempTable.get(j).getTwoHopNeighbors().get(j);
 					int neighborIndex = 0;
 					for (int k = 0; k < neighborTable.size(); k++)
 					{
 						neighborIndex = neighborTable.get(k).getTwoHopNeighbors().indexOf(neighbor);
 						if (neighborIndex != -1)
 						{
 							neighborTable.get(k).getTwoHopNeighbors().remove(neighborIndex);
 						}
 					}
 				}
 				tempTable.remove(index);
 			}
 		}
 	}
 	
 	/**
 	 * Terminate can be called to terminate execution of the thread. A join should be
 	 * called afterward in order to wait for the thread to finish.
 	 */
 	public void terminate(){
 		
 		byte[] buffer = new byte[8];
 		
 		for(int i = 0; i < 8; i++){
 			buffer[i] = -128;
 		}
 		
 		sender.forward(address, port, buffer);
 		
 	} // end terminate()
 	
 	public void startHelper(){
 		helper       = new SocketReceiverHelper(number, nodes);
 		helperThread = new Thread(helper);
 		helperThread.start();
 	}
 	
 	public void stopHelper(){
 		helper.terminate();
 		
 		try {
 			helperThread.join();
 		} catch (InterruptedException e) {
 			System.out.println("SocketReceiver: Helper thread interruption problem.");
 		}
 	}
 	
 	/**
 	 * Run command called automatically by Thread.start().
 	 * 
 	 * @throws IOException			: General IOException for package functions.
 	 * @throws LineUnavailable		: General LineUnavailable for package 
 	 * 										functions.
 	 */
 	@Override
 	public void run(){
 	
 		startHelper();
 		
 		boolean isTrash = false;
 		Node prevNode = null;
 		
 		try {
 			this.sLine.open(this.format);
 		}
 		catch (LineUnavailableException e){
 			System.out.println("SocketReceiver: Missing Data Line (run)");
 		}
 		
 		this.sLine.start();
 		
 		// Continues until program is closed.
 		while(running){
 			
 			try{
 				this.s.receive(this.dp);
 			}
 			catch (IOException e){
 				System.out.println("SocketReceiver: Bad packet reception");	
 			}
 			
 			int sequence    = ((dp.getData()[0] + 128) * 256) + dp.getData()[1] + 128;
 			int source      = ((dp.getData()[2] + 128) * 256) + dp.getData()[3] + 128;
 			int destination = ((dp.getData()[4] + 128) * 256) + dp.getData()[5] + 128;
 			int prevHop     = ((dp.getData()[6] + 128) * 256) + dp.getData()[7] + 128;
 			
 			System.out.println("Playing packet: " + (((this.dp.getData()[0] + 128) * 256) + this.dp.getData()[1] + 128) + "	" + (((this.dp.getData()[2] + 128) * 256) + this.dp.getData()[3] + 128) + "	" + (((this.dp.getData()[4] + 128) * 256) + this.dp.getData()[5] + 128) + "	" + (((this.dp.getData()[6] + 128) * 256) + this.dp.getData()[7] + 128));
 			
 			if (sequence == 0){
 				if(source == 0){
 					running = false;
 				}
 			}
 			
 			if(running){
 				prevNode = getNode(prevHop);
 				if(prevNode == null){
 					System.out.println("Invalid Previous Hop");
 				}
 			}
 			
 			if (running && !PacketDropRate.isPacketDropped(x, y, prevNode.getX(), prevNode.getY()))
 			{
 				if (sequence == 0)
 				{
 					int msgType = dp.getData()[8] + 128;
 					int length, neighbor, status;
 					boolean found = false;
 					switch (msgType)
 					{
 					case RESET_MESSAGE:
 						break;
 					case HELLO_MESSAGE:
 						for (int i = 0; i < neighborTable.size(); i++)
 						{
 							if (neighborTable.get(i).getNodeNumber() == source)
 							{
 								found = true;
 								if (neighborTable.get(i).getNumHops() == 2)
 								{
 									neighborTable.get(i).setNumHops(1);
 									neighborTable.get(i).setLinkStatus(1);
 								}
 							}
 						}
 						if (!found)
 						{
 							ArrayList<Integer> newNeighbors = new ArrayList<Integer>();
 							NeighborRow newNeighborRow = new NeighborRow(source, 0, 1, newNeighbors, 0, 1, false);
 							neighborTable.add(newNeighborRow);
 						}
 						found = false;
 						length = dp.getData()[9] + 128;
 						for (int i = 10; i < length; i += 3)
 						{
 							neighbor = ((dp.getData()[i] + 128) * 256) + dp.getData()[i + 1] + 128;
 							status   = dp.getData()[i + 2] + 128;
 							if (number == neighbor)
 							{
 								for (int j = 0; j < neighborTable.size(); j++)
 								{
 									if (neighborTable.get(j).getNodeNumber() == source)
 									{
 										found = true;
 										neighborTable.get(j).setLinkStatus(1);
 									}
 								}
 							}
 							else
 							{
 								for (int j = 0; j < neighborTable.size(); j++)
 								{
 									if (neighborTable.get(j).getNodeNumber() == source)
 									{
 										found = true;
 										if (neighborTable.get(j).getTwoHopNeighbors().indexOf(neighbor) == -1)
 										{
 											neighborTable.get(j).getTwoHopNeighbors().add(neighbor);
 										}
 									}
 								}
 							}
 							if (!found)
 							{
 								ArrayList<Integer> newNeighbors = new ArrayList<Integer>();
 								newNeighbors.add(neighbor);
 								NeighborRow newNeighborRow = new NeighborRow(source, 0, 2, newNeighbors, 0, 1, false);
 								neighborTable.add(newNeighborRow);
 							}
 						}
 						helper.updateNeighborTable(neighborTable);
 						break;
 					case TC_MESSAGE:
 						break;
 					}
 				}
 				
 				isTrash = checkTable(sequence, source, destination);
 			
 				if (destination == number & !isTrash){
 					playPacket();
 				}
 				else if (source != number && !isTrash){
 					forwardPacket(prevHop);
 				}
 			}
 			else
 			{
 				//System.out.println("Packet Dropped");
 			}
 			isTrash = false;
 		} // end while
 		stopHelper();
 		s.close();
		System.out.println("receiver out");
 	} // end SocketReceiver.run()
 
 	
 } // end SocketReceiver class
