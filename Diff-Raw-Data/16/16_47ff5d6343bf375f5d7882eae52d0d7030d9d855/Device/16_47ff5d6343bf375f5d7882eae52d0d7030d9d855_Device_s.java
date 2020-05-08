 package netproj.skeleton;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import netproj.hosts.Pinger;
 
 /**
  * Base class for all devices in a network (hosts and routers).
  *
  * Implements the IP-level functionality of a network device that has a number of outgoing links
  * and can receive packets from multiple sources.
  * 
  * Specific devices should extend this class and implement the {@code processPacket()} method that
  * will be called with all incoming packets that have not been dropped. Implementing classes should
  * call {@code sendPacket()} and {@code broadcastPacket()} to send packets on the network.
  * 
  * Each device has a terminal that can be started by calling the {@code terminal()} method.
 * Devices should implement the {@handleCommand()} method to handle any device-specific
  * commands necessary for control.
  * 
  * Each instance has an input buffer of certain size (in bits) that stores incoming packets. Packets
  * are then processed using {@code processPacket()} that implements the higher level logic of a
  * specific device. Each instance also keeps track of a virtual output buffer (that is in fact
  * implemented in {@link Link} but this should be transparent to anyone who is using this class at a
  * higher level. All packets that arrive at full buffers will be dropped.
  * 
  * All traffic is logged at {@code FINEST} level.
  * 
  * @author emil
  */
 public abstract class Device {
 	private int address;
 	
 	private Queue<Packet> buffer;
 	private ArrayList<Link> links;
 	
 	private int inputBuffSize;
 	private int inputBuffUsed;
 	private int outputBuffSize;
 	private int outputBuffUsed;
 	
 	private PacketProcessor packetProcessor;
 	
 	private static final Logger logger = Logger.getLogger(Pinger.class.getCanonicalName());
 	
 	/**
 	 * Initializes the IP level attributes of the device.
 	 * 
 	 * @param inputBuffSize		Input Buffer size in bits.
 	 * @param outputBuffSize	Output Buffer size in bits.
 	 * @param address			IPv4 address.
 	 */
 	public Device(int inputBuffSize, int outputBuffSize, int address) {
 		this.inputBuffSize = inputBuffSize;
 		this.outputBuffSize = outputBuffSize;
 		inputBuffUsed = 0;
 		outputBuffUsed = 0;
 		
 		this.address = address;
 		
 		buffer = new LinkedList<Packet>();
 		packetProcessor = new PacketProcessor();
 		links = new ArrayList<Link>();
 	}
 	
 	/**
 	 * Changes the amount of output buffer used by {@code size} bits.
 	 * 
 	 * This method is used to simulate having an output buffer. Since this class doesn't actually
 	 * have one, links are in charge of updating the amount of output buffer used by every device.
 	 * 
 	 * All calls to this method must be synchronized by the device instance!
 	 * 
 	 * If you are using this method outside of the {@link Link} class, you are probably doing
 	 * something wrong!
 	 * 
 	 * @param size	Number of bits that you are taking from the buffer (can be negative, if you are
 	 * 				removing stuff from the buffer)
 	 */
 	void useOutputBuff(int size) {
 		if (outputBuffUsed + size > outputBuffSize) {
 			logger.warning("Output buffer overflow!");
 		}
 		if (outputBuffUsed + size < 0) {
 			logger.severe("Output buffer overflow!");
 		}
 		outputBuffUsed += size;
 	}
 	
 	/**
 	 * Number of bits waiting to be sent in the output buffer.
 	 */
 	public int getOutputBuffUsed() {
 		return outputBuffUsed;
 	}
 	
 	/**
 	 * Number of bits waiting to be processed in the input buffer.
 	 */
 	public int getInputBuffUsed() {
 		return inputBuffUsed;
 	}
 
 	/**
 	 * Size of the output buffer in bits.
 	 */
 	public int getOutputBuffSize() {
 		return outputBuffSize;
 	}
 	
 	/**
 	 * Sets the size of the output buffer in bits.
 	 * 
 	 * If you shrink the buffer, all messages that are already in the buffer will remain there until
 	 * they are processed. You may see a couple of warnings.
 	 */
 	public void setOutputBuffSize(int outputBuffSize) {
 		this.outputBuffSize = outputBuffSize;
 	}
 	
 	/**
 	 * Size of the input buffer in bits.
 	 */
 	public int getInputBuffSize() {
 		return inputBuffSize;
 	}
 	
 	/**
 	 * Sets the size of the input buffer in bits.
 	 * 
 	 * If you shrink the buffer, all messages that are already in the buffer will remain there until
 	 * they are processed.
 	 */
 	public void setInputBuffSize(int inputBuffSize) {
 		this.inputBuffSize = inputBuffSize;
 	}
 	
 	/**
 	 * List of all outgoing links.
 	 * 
 	 * The links in the list correspond to gates 0, 1, 2...
 	 */
 	public List<Link> getLinks() {
 		synchronized (links) {
 			return links;
 		}
 	}
 	
 	/**
 	 * Sets the list of outgoing links.
 	 * 
      * The links in the list correspond to gates 0, 1, 2...
 	 */
 	public void setLinks(ArrayList<Link> links) {
 		this.links = links;
 	}
 	
 	/**
 	 * Sets the IPv4 address of this device.
 	 * 
 	 * Use with caution!
 	 */
 	public void setAddress(int address) {
 		this.address = address;
 	}	
 	
 	/**
 	 * Starts processing packets.
 	 * 
 	 * Note that messages are still accepted in the buffer even before starting the processor.
 	 */
 	public void start() {
 		packetProcessor.start();
 	}
 	
 	/**
 	 * IPv4 address.
 	 */
 	public int getAddress() {
 		return address;
 	}
 	
 	/**
 	 * Plugs {@code link} into {@code gate}.
 	 */
 	public void setLink(int gate, Link link) {
 		synchronized (links) {
 			links.set(gate,link);
 		}
 	}
 	
 	/**
 	 * Adds {@code link} to the next available gate.
 	 */
 	public void addLink(Link link) {
 		synchronized (links) {
 			links.add(link);
 		}
 	}
 	
 	/**
 	 * Sends {@code packet} to the link at {@code gate}.
 	 * 
 	 * You should call this method when you want to send a packet from some host you are
 	 * implementing.
 	 */
 	final protected void sendPacket(Packet packet, int gate) {
 		Link link;
 		
 		synchronized (links) {
 			link = links.get(gate);
 		}
 		
 		Packet p = new Packet(packet);
 		if (p.hop()) {
 			logger.finest(Integer.toHexString(this.address) + " is sending packet "
                           + packet.getPacketId() + " to gate " + gate);
 			link.sendMessage(this, p);
 		} else {
 			logger.finest(Integer.toHexString(this.address) + " dropped packet "
                           + packet.getPacketId() + " because there is no more ttl.");
 		}
 	}
 	
 	/**
 	 * Sends {@code packet} to all outgoing links.
 	 */
 	public void broadcastPacket(Packet packet) {
 		logger.finest(Integer.toHexString(this.address) + " is broadcasting packet "
                       + packet.getPacketId() + " to all " + links.size() + " gates");
 		
 		for (int i =0; i < links.size() ; i++) {
 			this.sendPacket(packet, i);
 		}
 	}
 	
 	/**
 	 * Receives a packet for processing.
 	 * 
 	 * Appends the packet to the input buffer if there is enough space. If the packet gets into the
 	 * buffer, it gets processed (using {@code processPacket}), once its turn comes.
 	 * 
 	 * If you are using this method outside of the {@link Link} class, you are probably doing
 	 * something wrong!
 	 */
 	final void receivePacket(Packet packet) {
 		logger.finest(Integer.toHexString(this.address) + " received packet "
 				      + packet.getPacketId());
 		
 		synchronized (buffer) {
 			if (inputBuffUsed + packet.getSizeInBits() <= inputBuffSize) {
 				inputBuffUsed += packet.getSizeInBits();
 				buffer.add(packet);
 				buffer.notify();
 			} else {
 				logger.finest(Integer.toHexString(this.address) + " dropped packet "
 						      + packet.getPacketId());
 			}
 		}
 	}
 	
 	/**
 	 * Processes an incoming packet.
 	 * 
 	 * This method will be called with every packet that arrives at this device and is not dropped.
 	 * This is where the actual higher level logic should be implemented.
 	 * 
 	 * This method should never be called directly outside of {@code Device}'s implementation!
 	 */
 	public abstract void processPacket(Packet packet);
 	
 	/**
 	 * Thread that processes packets from the input buffer.
 	 * 
 	 * This thread will wait on the buffer if there are no more packets to process, so it should be
 	 * notified whenever a new packet arrives. 
 	 * 
 	 * @author emil
 	 */
 	private class PacketProcessor extends Thread {
 		/**
 		 * Processes any packets in the buffer and then waits for new ones.
 		 */
 		@Override
 		public void run() {
 			while (!this.isInterrupted()) {
 				Packet packet = null;
 				
 				// Get a packet from the input buffer
 				synchronized (buffer) {
 					packet = buffer.poll();
 					if (packet != null) {
 						inputBuffUsed -= packet.getSizeInBits();
 					}
 				}
 				
 				// Process the packet
 				if (packet != null) {
 					processPacket(packet);
 				} else {
 					// No packets in the buffer, so wait until the buffer is notified.
 					try {
 						synchronized (buffer) {
 							buffer.wait();
 						}
 					} catch (InterruptedException e) {
 						logger.log(Level.WARNING, "Message processor for " + address + " was "
 								   + "interrupted.", e);
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Handles device specific commands.
 	 * 
 	 * You should implement this method to handle any commands necessary to control your device. You
 	 * must support the "help" command (although, it might just do nothing, if there are no specific
 	 * commands that your device supports).
 	 * 
 	 * Make sure you call {@code super.handleCommand(command)}! 
 	 */
 	public void handleCommand(String command) {
 		if (command.equals("help")) {
 			System.out.println("help - prints this message");
 			System.out.println("ifconfig - prints network information about this device");
 		}
 		if (command.equals("ifconfig")) {
 			System.out.println("IPv4 address: " + Integer.toHexString(getAddress()));
 			
 			synchronized (links) {
 				int i = 0;
 				for (Link link: links) { 
 					System.out.println("gate " + (i++) + ": " + link);
 				}
 			}
 		}
 	}
 	
 	public final void terminal(Scanner input) {
 		while (true) {
 			System.out.print(Integer.toHexString(getAddress()) + "$ ");
 			String command = input.nextLine().trim();
 
 			if (command.equals("exit")) {
 				System.out.println("Bye.");
 				break;
 			}
 			handleCommand(command);
 		}
 	}
 }
