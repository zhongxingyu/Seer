 // Package Declaration //
 package Second_Project_Code;
 
 // Java Package Support //
 import java.util.ArrayList;
 import java.io.IOException;
 
 import javax.sound.sampled.LineUnavailableException;
 
 // Internal Package Support //
 // { Not Applicable } 
 
 /**
  * 
  * Second_Project_Code/Node.java
  * 
  * @author(s)	: Ian Middleton, Zach Ogle, Matthew J Swann
  * @version  	: 1.0
  * Last Update	: 2013-03-19
  * Update By	: Ian Middleton
  * 
  * 
  * Second_Project_Code PACKAGE :: Source code for Comp 6360: Wireless & Mobile Networks
  * 	               Assignment 2 :: VOIP
  * 
  * Source code for Comp 6360: Wireless & Mobile Networks. This code
  * encompasses the node object which will store all the information regarding
  * each node in the network.
  *  
  *  *** INCOMPLETE ***
  *  
  */
 
 public class Node{
 	int number, port, x, y;
 	String address;
 	ArrayList<Node> links = new ArrayList<Node>();
 	ArrayList<SocketSender> sendRunnables = new ArrayList<SocketSender>();
 	ArrayList<Thread> sendThreads = new ArrayList<Thread>();
 	SocketSender sender;
 	SocketReceiver receiver;
 	Thread receiverThread, senderThread;
 	
 	/**
 	 * Constructor for the Node class.
 	 */
 	public Node(){
 		number = 0;
 		port = 0;
 		x = 0;
 		y = 0;
 		address = "";
 	} // end Node()
 	
 	/**
      * Constructor for the Node class.
      * 
 	 * @param  number		: Number of the node.
 	 * @param  address      : IP address of the node.
 	 * @param  port         : Port number of the node.
 	 * @param  x           	: X coordinate of the node.
 	 * @param  y           	: Y coordinate of the node.
 	 * @param  links        : An array that contains all nodes that this node
 	 * 							is linked to.
 	 * @param numLinks		: The number of links in the array.
      */
 	public Node(int number, String address, int port, int x, int y){
 		this.number = number;
 		this.address = address;
 		this.port = port;
 		this.x = x;
 		this.y = y;
 	} // end Node()
 	
 	/**
 	 * Gets the number of the node.
 	 * 
 	 * @return 				: An int representing the node.
 	 */
 	public int getNumber(){
 		return number;
 	} // end getNumber()
 	
 	/**
 	 * Gets the IP address of the node.
 	 * 
 	 * @return				: A string containing the IP address.
 	 */
 	public String getAddress(){
 		return address;
 	} // end getAddress()
 	
 	/**
 	 * Gets the port of the node.
 	 * 
 	 * @return				: An int containing the port number.
 	 */
 	public int getPort(){
 		return port;
 	} // end getPort()
 	
 	/**
 	 * Gets the X coordinate of the node.
 	 * 
 	 * @return				: An int containing the X coordinate.
 	 */
 	public int getX(){
 		return x;
 	} // end getX()
 	
 	/**
 	 * Sets the X coordinate of the node.
 	 * 
 	 * @param x				: What the X coordinate is to be changed to.
 	 */
 	public void setX(int x){
 		this.x = x;
 	} // end setX()
 	
 	/**
 	 * Gets the Y coordinate of the node.
 	 * 
 	 * @return				: An int containing the Y coordinate.
 	 */
 	public int getY(){
 		return y;
 	}
 	
 	/**
 	 * Sets the Y coordinate of the node.
 	 * 
 	 * @param y				: What the Y coordinate is to be changed to.
 	 */
 	public void setY(int y){
 		this.y = y;
 	} // end setY()
 	
 	/**
 	 * Sets up the Node for this execution by reading in the configuration file,
 	 * setting its variables according to which node it is, and then creating Node
 	 * objects for all nodes it is linked to.
 	 * 
 	 * @param fileLoc		: A string designating the location of the configuration file.
 	 * @param nodeNumber	: The number of this node.
 	 * @throws IOException	: General IOException for if the file is not found.
 	 */
 	public void setup(String fileLoc, int nodeNumber) throws IOException{
		ArrayList<String> lines = ConfigReader.getLines(fileLoc);
 		ArrayList<Node> otherNodes = new ArrayList<Node>();
 		ArrayList<Integer> toBeLinked = new ArrayList<Integer>();
 		int number, port, x, y;
 		String address;
 		
 		for(int i=0; i < lines.size(); i++){
 			String[] parts = lines.get(i).split("\\s+");
 			if(nodeNumber == Integer.parseInt(parts[1])){
 				this.number = Integer.parseInt(parts[1]);
 				this.address = parts[2].replace(",", "");
 				this.port = Integer.parseInt(parts[3]);
 				this.x = Integer.parseInt(parts[4]);
 				this.y = Integer.parseInt(parts[5]);
 				for(int j = 7; j < parts.length; j++){
 					toBeLinked.add(Integer.parseInt(parts[j]));
 				} // end for
 			} // end if
 			else{
 				number = Integer.parseInt(parts[1]);
 				address = parts[2];
 				port = Integer.parseInt(parts[3]);
 				x = Integer.parseInt(parts[4]);
 				y = Integer.parseInt(parts[5]);
 				otherNodes.add(new Node(number, address, port, x, y));
 			} // end else
 		} // end for
 		
 		if(toBeLinked.size() > 0){
 			for(int i=0; i < toBeLinked.size(); i++){
 				for(int j = 0; j < otherNodes.size(); j++){
 					if(toBeLinked.get(i) == otherNodes.get(j).getNumber()){
 						links.add(otherNodes.get(j));
 						break;
 					} // end if
 				} // end for
 			} // end for
 		} // end if
 	} // end setup()
 	
 	/**
 	 * Creates a number of senders equal to the number of links this node has. As each
 	 * sender is created it starts sending immediately.
 	 * 
 	 * @param destNumber				: The destination node for the information being sent.
 	 * @throws IOException				: General IOException.
 	 * @throws LineUnavailableException	: General LineUnavailableException
 	 */
 	public void startSending(int destNumber) throws IOException, LineUnavailableException{
 		sender = new SocketSender(links, number, destNumber);
 		senderThread = new Thread(sender);
 		senderThread.start();
 	} // end startSending()
 	
 	/**
 	 * Stops the threads running the SocketSender objects.
 	 * 
 	 * @throws InterruptedException		: General InterruptedException.
 	 */
 	public void stopSending() throws InterruptedException{
 		sender.terminate();
 		senderThread.join();
 	} // end stopSending()
 	
 	/**
 	 * Starts the receiver for this node.
 	 * 
 	 * @throws IOException				: General IOException
 	 * @throws LineUnavailableException	: General LineUnavailableException
 	 */
 	public void startReceiving() throws IOException, LineUnavailableException{
 		receiver = new SocketReceiver(address, port, number, links);
 		receiverThread = new Thread(receiver);
 		receiverThread.start();
 	} // end startReceiving()
 	
 	/**
 	 * Stops the receiver for this node.
 	 * 
 	 * @throws InterruptedException		: General InterruptedException
 	 */
 	public void stopReceiving() throws InterruptedException{
 		receiver.terminate();
 		receiverThread.join();
 	} // end stopReceiving
 	
 } // end Node class
 
