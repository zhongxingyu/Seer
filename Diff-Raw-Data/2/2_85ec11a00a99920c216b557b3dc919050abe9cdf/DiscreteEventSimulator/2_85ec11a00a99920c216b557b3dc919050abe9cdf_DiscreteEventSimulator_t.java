 package ece358.networks.assignment2;
 
 import java.util.Random;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.lang.Math;
 
 public class DiscreteEventSimulator {
 	
 	public static boolean isMediumBusy = false;
 	public static boolean dbg = false;
 	public static int fails = 0;
 	public static Random randomGenerator = new Random();
 
 	public static DiscreteEventSimulator des = new DiscreteEventSimulator();
 	
 	class Packet {
 		public int generationTime;
 		public int serviceTime;
 	}
 
 	public static int MAX_TICKS = 10000000;
 
 	public static int TEST_RUN_TIMES = 5;
 	public static double TICK_DURATION = 0.0001; 	// 1 tick / 1 millionth of a second
 	public static int DEFAULT_TRANSMIT_TIME = 3;
 	public static double PROBABILITY_LIMIT = 0.01;
 	
 	public static Random rand = new Random();
 
 	public static double unifRand() { 
 		Random random = new Random();
 		return random.nextDouble();
 
 	}
 	public static double unifExp(int packetsPerSecond) { 
 		return (double) -Math.log(1-unifRand())/(packetsPerSecond * TICK_DURATION);
 	}
 	
 	public static double calculatePropogationTime(int nodeSender, int nodeReceiver)
 	{
 		debug("CalculatePropogationTime: Sender: " + nodeSender + " Receiver: " + nodeReceiver);
 		return ((10* Math.abs(nodeSender - nodeReceiver))/200000000) / TICK_DURATION;
 	}
 	
 	public static double calculateTransferTime(int lanSpeed, int packetLength)
 	{
 		return packetLength/lanSpeed;
 	}
 	
 	class Node {
 		int waitDuration = 0;
 		boolean isTransmitting = false;
 		int transmissionRemaining = 0;
 		int pktGenerationTime = 0;
 		int id = -1;
 		int collisionCounter = 1;
 		boolean greaterThanP = false;
 		public double probability = 0.0;
 		
 		public Node() {}
 		
 		public void generateNextPacketArrival(int packetsPerSecond, int currentTick) {
 			if (pktGenerationTime == 0) {
 				pktGenerationTime = currentTick + (int)unifExp(packetsPerSecond);				
 			}
 		}
 		public boolean isTransmissionSuccessful() {
 			return true;
 		}
 		public boolean isCollisionDetected() {
 			return true;
 		}
 		public int calculateBEB() {
 			int exponentialTime = randomGenerator.nextInt((int) (Math.pow(2,collisionCounter) - 1));
 			if (exponentialTime == 0) {
 				exponentialTime += 1;
 			}
 			fails++;
 			return exponentialTime;
 		}
 	}
 
 	private static int driver(int n1, int a1, int w1, int l1, int p1)
 	{
 		int compNum = n1;
 		int pktArrivalRate = a1;
 		int lanSpeed = w1;
 		int pktLength = l1;
 		int persistanceParam = p1;
 		
 		int transmitTime = (int) ((pktLength * 8) / ((lanSpeed * 1000000) * TICK_DURATION));
 		System.out.println("Transmission time: " + transmitTime);
 		int pktsTransmittedSuccessfully = 0;
 		
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		HashSet<Node> collisionsDetected = new HashSet<Node>();
 		
 		for (int i = 0; i < compNum; i++) {
 			Node n = des.new Node();
 			n.id = i;
 			n.generateNextPacketArrival(pktArrivalRate, 0);
 			//n.probability  = Math.random() % 10;
 			//if (p == 3) {
 		//	Random generator = new Random();
 		//	double probablity = generator.nextDouble();
 		//	n.probablity = probablity;
 		//}
 			nodes.add(n);
 		}
 		
 		Node transmitter = null;
 		
 		for (int currentTick = 0; currentTick < MAX_TICKS; currentTick++) {
 			
 			// i think this should be the last thing we do, not the first ...?
 			
 			/* CHECK IF A NODE IS TRANSMITTING */
 			if (transmitter != null && transmitter.transmissionRemaining > 0) {
 				transmitter.transmissionRemaining -= 1;
 			}
 			else if (transmitter != null && transmitter.transmissionRemaining == 0) {	
  				for (Node n : nodes) {
  					if (n.id == transmitter.id) {
  						n.generateNextPacketArrival(pktArrivalRate, currentTick);
  					}
  				}
 				transmitter = null;
 				isMediumBusy = false;
 				pktsTransmittedSuccessfully++;
 			}
 			/* END OF TRANSMISSION CHECK */
 						
 			if (!isMediumBusy) { //nobody else is using the medium, yay :)
 				collisionsDetected.clear();
 				for (int j = 0; j < nodes.size(); j++) {
 					Node currentNode = nodes.get(j); 
  					if (currentTick == currentNode.pktGenerationTime) {
  						
 						if (persistanceParam == 3 && currentNode.greaterThanP) {
 		 					collisionsDetected.add(currentNode);	
 	 					}
 						else {
 							collisionsDetected.add(currentNode);	
 						}
 					}
 				}
 				
 				if (collisionsDetected.size() > 1) { 
 					debug("Collision array: \n");
 					for (Node n : collisionsDetected) {
 						String temp = "";
 						n.collisionCounter++;
 						if(n.collisionCounter == 11)
 						{
 							n.collisionCounter = 1;
 						}
 						temp = "\t\tID: " + n.id + "\t Collision Counter:" + n.collisionCounter + "\tOld Packet Generation time:" + n.pktGenerationTime;
 						n.pktGenerationTime = currentTick + n.calculateBEB();
 						temp = temp + "\tNew Packet Geneartion time:" + n.pktGenerationTime;
 						debug(temp);							
 					}
 				}
 				else if (collisionsDetected.size() == 1) { //only 1 transmitter, life is good :)
 					
 					Node source = collisionsDetected.iterator().next();
  					
  					if (persistanceParam == 3) {
  						if (source.greaterThanP) { //this means we got lucky and are able to send, IN ADDITION, the medium is NOT BUSY ... YAY :)
  							
 		 					// create packet
 		 					Packet p = des.new Packet();
 		 					// send packet (ignore distance for now)
 		 					isMediumBusy = true;
 		 					source.transmissionRemaining = transmitTime;
 		 					
 		 					int receiver_id;
 		 					
 		 					do
 		 					{
 		 						// Generate a random number between 1 and 100 and mod it by the number of computers
 		 						receiver_id = ((int) (randomGenerator.nextInt(100)) % compNum);
 		 					}while(source.id == receiver_id);
 		 					
 		 					double propogationTime = calculatePropogationTime(source.id, receiver_id);
 		 					
 		 					source.transmissionRemaining += propogationTime;
 
 							source.pktGenerationTime = 0;
 							
 							transmitter = source; 
  						}
  						else 
  						{
 							double probability = (randomGenerator.nextInt(100)+1)/100.0;			 					
 		 					if (probability < PROBABILITY_LIMIT) {
 		 						/*
 		 						source.collisionCounter++;
 		 						if(source.collisionCounter == 11)
 								{
 									source.collisionCounter = 1;
 								}
 								source.pktGenerationTime = currentTick + source.calculateBEB();
 								*/
 		 					}
 		 					else 
 		 					{
 		 						source.greaterThanP = true;
 		 						source.pktGenerationTime = currentTick + 1;
 		 						//wait for next slot to send the packet
 		 					}
  						}
  					}
  					else 
  					{
 	 					// create packet
 	 					Packet p = des.new Packet();
 	 					// send packet (ignore distance for now)
 	 					isMediumBusy = true;
 	 					source.transmissionRemaining = transmitTime;
 
 	 					int receiver_id;
 	 					
 	 					do
 	 					{
 	 						// Generate a random number between 1 and 100 and mod it by the number of computers
 	 						receiver_id = ((int) (randomGenerator.nextInt(100)) % compNum);
 	 					}while(source.id == receiver_id);
 	 						
 	 					double propogationTime = calculatePropogationTime(source.id, receiver_id);
 	 					
 	 					source.transmissionRemaining += propogationTime;
 	 					
 						source.pktGenerationTime = 0;
 						
 	 					transmitter = source; 
  					}
  				}
 				}
 				else if (isMediumBusy)
 				{
  				//debug("Medium is busy");
 					for (int j = 0; j< nodes.size(); j++)
 					{
 						Node currentNode = nodes.get(j);
 						if(currentNode != transmitter && currentNode.pktGenerationTime == currentTick)
 						{
 							if(persistanceParam == 1)
 							{
 								currentNode.pktGenerationTime = currentTick + 1;
 							}
 							else if (persistanceParam == 2)
 							{
 								currentNode.pktGenerationTime = currentTick + randomGenerator.nextInt(9) + 2;
 							}
 							//TODO: FIX LOGIC
 							else if (persistanceParam == 3)
 							{
 								//currentNode.pktGenerationTime += 1;
 								for (Node n : nodes) {
 									if (n.greaterThanP) {
 										n.collisionCounter++;
 										if(n.collisionCounter == 11)
 										{
 											n.collisionCounter = 1;
 										}
 										n.pktGenerationTime = currentTick + n.calculateBEB();
 									}		
 								}
 							}
 						}
 					}
 				}
 				
 				
 			}
 			System.out.println("Number of packets successfully sent: " + pktsTransmittedSuccessfully);
 			System.out.println("Number of failed packets: " + fails);
 			return pktsTransmittedSuccessfully;
 	}
 	
 	//private final static int LAN_SPEED = 1000000;
 	//private static final int PACKET_LENGTH = 100;
 	private final static int NODE_DISTANCE = 10;
 	private final static double PROPAGATION_SPEED = 2.5E8;
 	
 	public static void simulate() {
 		
 		int lanSpeed = 1;
 		int pktLength = 1500;
 		int persistanceParam = 1;
 
 		int total = 0;
 		
		for (int compNum = 20; compNum <= 100; compNum += 20) {
 			System.out.println("=============================================");
 			System.out.println("Number of nodes : " + compNum);
 			for (int pktArrivalRate = 5; pktArrivalRate <= 7; pktArrivalRate ++) {
 				System.out.println("Arrival Rate : " + pktArrivalRate);
 				total = 0;
 				for(int n = 0; n < 5; n++)
 				{
 					fails = 0;
 					isMediumBusy = false;
 					System.out.println("Test #" + n);
 					total = total + driver(compNum, pktArrivalRate, lanSpeed, pktLength, persistanceParam);
 				}
 				System.out.println("Number of nodes : " + compNum+ " -- Arrival rate : " + pktArrivalRate + " -- The average is " + total/5 +"\n");
 				System.out.println("=============================================\n");
 			}
 		}
 
 	}
 	
 	public static void main(String args[]) {
 		
 		simulate();
 		
 		/*if (args.length == 1 && args[0].equals("-usage")) {
 			debug("------------------------------------------------------------------------");
 			debug("Parameter description:");
 			debug("N: the number of computers connected to the LAN (variable)");
 			debug("A: Data packets arrive at the MAC layer following a Poisson process with an average arrival rate of A packets/second (variable)");
 			debug("W: the speed of the LAN (fixed)");
 			debug("L: packet length (fixed)");
 			debug("P: Persistence parameter for P-persistent CSMA protocols");
 			debug("-----------------------------------------------------------------------");
 			debug("Command Format: ");
 			debug("DiscreteEventSimulator <N> <A> <W> <L> <P>");
 			debug("");
  		}
  		else if (args.length == 5) 
  		{
  			int compNum = Integer.parseInt(args[0]);
 			int pktArrivalRate = Integer.parseInt(args[1]);
 			int lanSpeed = Integer.parseInt(args[2]);
 			int pktLength = Integer.parseInt(args[3]);
 			int persistanceParam = Integer.parseInt(args[4]);
 			
 			int total = 0;
 			for(int n = 0; n < 5; n++)
 			{
 				fails = 0;
 				isMediumBusy = false;
 				System.out.println("Test #" + n);
 				total = total + driver(compNum, pktArrivalRate, lanSpeed, pktLength, persistanceParam);
 			}
  			
 			System.out.println("The average is " + total/5);
  		}
  		else {
  			System.out.println("Not enough arguments! Enter DiscreteEventSimulator -usage");
  		}*/
 	}
 
 	public static void debug(String msg) {
 		if(dbg)
 		{
 			System.out.println("DEBUG:	" + msg);
 		}
 	}
 }
