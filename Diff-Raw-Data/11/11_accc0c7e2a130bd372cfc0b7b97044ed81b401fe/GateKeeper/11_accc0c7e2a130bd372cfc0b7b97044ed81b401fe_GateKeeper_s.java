 package linewars.network;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import linewars.network.messages.Message;
 
 /**
  * Abstracts actual interaction with the network protocol, including ensuring
  * that all information is received properly (dropped packets)
  * 
  * Sends Messages over the network to a specified address
  * 
  * Converts incoming network information into Messages
  * 
  * Can be polled for Messages from a specific address for a specific tick id
  * 		- Urgent polling is urgent; this could trigger resend requests, if applicable
  * 		- Normal polling is not urgent, never triggers a resend request
  * 
  * @author Titus Klinge
  */
 public class GateKeeper
 {
 	private static final int MAX_MTU = 1500;
 	private static final int TARGET_MTU = 1000;
 	private static final long SLEEP_TIME = 10;
 	private static final int SO_TIMEOUT = 1;
 	
 	private DatagramSocket socket;
 	private int port;
 	
 	// TODO access to the hash maps of messages is NOT thread safe!!!!!!
 	
 	// map from timeStep to a (map from playerAddress to messages)
 	private HashMap<Integer, HashMap<String, Message[]>> messages;
 	
 	// messages used for resend requests
 	private HashMap<Integer, Message[]> resendMessages;
 	
 	private MessageListener msgListener;
 	
 	private String[] listeningAddresses;
 	
 	public GateKeeper(String[] listeningAddresses, int port, int sendToPort) throws SocketException
 	{
 		messages = new HashMap<Integer, HashMap<String, Message[]>>();
 		resendMessages = new HashMap<Integer, Message[]>();
 		
 		socket = new DatagramSocket(port);
 		socket.setSoTimeout(SO_TIMEOUT);
 		
 		msgListener = new MessageListener(SLEEP_TIME);
 		this.port = sendToPort;
 		
 		this.listeningAddresses = listeningAddresses;
 	}
 	
 	/**
 	 * Steals the current thread by using it to listen for messages.
 	 */
 	public void startListening()
 	{
 		msgListener.start();
 	}
 	
 	/**
 	 * Polls the Gatekeeper for Messages which have arrived from the given address
 	 * which are due to be implemented on the given tickID.
 	 * 
 	 * If the Messages are not all available, in addition to returning null, the
 	 * Gatekeeper will take action to obtain the required Messages - in a nonblocking fashion.
 	 * 
 	 * @param tickID
 	 * The tickID associated with the list of Messages
 	 * @param address
 	 * The address from which the user needs Messages.
 	 * @return
 	 * If all Messages associated with the given tickID from the given address have been received, those Messages.
 	 * If some Messages from that set are not yet received, null.
 	 */
 	public Message[] urgentlyPollMessagesForTick(int tickID, String address)
 	{
 		Message[] toReturn = pollMessagesForTick(tickID, address);
 		
 		if (toReturn == null)
 		{
 			sendMessageResendRequest(tickID, address);
 		}
 		
 		return toReturn;
 	}
 	
 	/**
 	 * Polls the Gatekeeper for Messages which have arrived from the given address
 	 * which are due to be implemented on the given tickID.
 	 * 
 	 * @param tickID
 	 * The tickID associated with the list of Messages
 	 * @param address
 	 * The address from which the user needs Messages.
 	 * @return
 	 * If all Messages associated with the given tickID from the given address have been received, those Messages.
 	 * If some Messages from that set are not yet received, null.
 	 */
 	public Message[] pollMessagesForTick(int tickID, String address)
 	{
 		if(messages.get(tickID) == null) return null;
 		return messages.get(tickID).get(address);
 	}
 	
 	/**
 	 * Sends the given Messages out to the given targetAddress.
 	 * @param msgs
 	 * The Messages to be sent out.
 	 * @param targetAddress
 	 * The address to which they are to be sent.
 	 */
 	public void pushMessagesForTick(Message[] msgs, String targetAddress)
 	{
 		if (msgs == null || msgs.length == 0) return;
 		
 		MessagePacket[] packets = MessageConstructor.createMessagePackets(msgs);
 		for (MessagePacket p : packets)
 		{
 			byte[] packetData = Serializer.serialize(p);
 			try {
 				socket.send(new DatagramPacket(packetData, packetData.length, InetAddress.getByName(targetAddress), port));
 			} catch (Exception e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 		
 		resendMessages.put(msgs[0].getTimeStep(), msgs);
 	}
 	
 	private void sendMessageResendRequest(int tickID, String playerAddress)
 	{
 		ResendRequest request = new ResendRequest(tickID);
 		byte[] packetData = Serializer.serialize(request);
 		try {
 			socket.send(new DatagramPacket(packetData, packetData.length, InetAddress.getByName(playerAddress), port));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private class MessageListener
 	{
 		private long delay;
 		private boolean isListening;
 		
 		private HashMap<MessageID, List<MessagePacket>> incompletePackets;
 		
 		public MessageListener(long delay)
 		{
 			this.delay = delay;
 			incompletePackets = new HashMap<MessageID, List<MessagePacket>>();
 			isListening = true;
 		}
 		
 		public void start()
 		{
 			while (isListening)
 			{
 				DatagramPacket packet = receivePacket();
 				if (packet != null)
 				{
 					if (handleResendRequestPacket(packet) == false)
 					{
 						handleMessagePacket(packet);
 					}
 				} else
 				{
 					// sleep
 					try { Thread.sleep(delay); }
 					catch (InterruptedException e) { e.printStackTrace(); }
 				}
 			}
 		}
 		
 		private DatagramPacket receivePacket()
 		{
 			DatagramPacket packet = new DatagramPacket(new byte[MAX_MTU], MAX_MTU);
 			
 			boolean receivedData = true;
 			try {
 				socket.receive(packet);
 			} catch (IOException e) {
 				receivedData = false;
 			}
 			
 			return (receivedData) ? packet : null;
 		}
 		
 		private boolean handleResendRequestPacket(DatagramPacket packet)
 		{
 			ResendRequest request = null;
 			// deserializes the packet data
 			try {
 				request = (ResendRequest) Serializer.deSerialize(packet.getData());
 			} catch (Exception e) {
 				// if this is the wrong type of packet, return false
 				return false;
 			}
 			
 			Message[] toSend = resendMessages.get(request.timeStep);
 			
 			if (toSend == null)
 			{
 				// count the number of messages
 				int numMessages = 0;
 				for (String player : listeningAddresses)
 				{
					if (messages.get(request.timeStep).get(player) == null)
 					{
 						return true;
 					}
 					numMessages += messages.get(request.timeStep).get(player).length;
 				}
 				toSend = new Message[numMessages];
 				
 				// create a list of messages from all players
 				int curMsg = 0;
 				for (String player : messages.get(request.timeStep).keySet())
 				{
 					for (Message msg : messages.get(request.timeStep).get(player))
 					{
 						toSend[curMsg] = msg;
 						++curMsg;
 					}
 				}
 			}
 			
 			// sends the messages back to the requester
 			pushMessagesForTick(toSend, packet.getAddress().getHostAddress());
 			
 			return true;
 			
 		}
 		
 		private void handleMessagePacket(DatagramPacket packet)
 		{
 			MessagePacket msgPacket = null;
 			
 			// deserializes the packet data
 			try {
 				msgPacket = (MessagePacket) Serializer.deSerialize(packet.getData());
 			} catch (Exception e) {
 				// if this is the wrong type of packet, return
 				return;
 			}
 			
 			// if the packet was able to be deserialized ...
 			if (msgPacket != null)
 			{
 				// this is the ID to know which message this packet goes to
 				MessageID msgID = new MessageID(msgPacket.playerID, msgPacket.timeStep);
 				
 				// if it is the first packet received for the time step ...
 				if (incompletePackets.get(msgID) == null)
 				{
 					// if there is only one packet for this message ...
 					if (msgPacket.numPackets == 1)
 					{
 						buildAndStoreMessages(new MessagePacket[]{msgPacket}, packet.getAddress().getHostAddress());
 					}
 					// if there are multiple packets, create a new list of packets
 					else
 					{
 						List<MessagePacket> packetList = new LinkedList<MessagePacket>();
 						packetList.add(msgPacket);
 						incompletePackets.put(msgID, packetList);
 					}
 				}
 				// if it is not the first packet received ...
 				else
 				{
 					// add it to the list of packets for that id
 					List<MessagePacket> packetList = incompletePackets.get(msgID);
 					packetList.add(msgPacket);
 					
 					// if all packets for a message are found ...
 					if (packetList.size() == msgPacket.numPackets)
 					{
 						buildAndStoreMessages(packetList.toArray(new MessagePacket[packetList.size()]), packet.getAddress().getHostAddress());
 						
 						// remove it from the incomplete packet map
 						incompletePackets.remove(msgID);
 					}
 				}
 			}
 		}
 		
 		/**
 		 * Constructs the original message array from the packets given
 		 * and then stores them in the gatekeeper's map of messages.
 		 * 
 		 * @param packets The packets that compose the messages
 		 */
 		private void buildAndStoreMessages(MessagePacket[] packets, String senderAddress)
 		{
 			int timeStep = packets[0].timeStep;
 			Message[] msgs = null;
 			
 			// create the message
 			try {
 				msgs = MessageConstructor.constructMessage(packets);
 			} catch (InvalidMessageException e) {
 				e.printStackTrace();
 			}
 			
 			// if this player is the first one to send all of its messages, create a new
 			// map for this time step
 			if (messages.get(timeStep) == null)
 			{
 				messages.put(timeStep, new HashMap<String, Message[]>());
 			}
 			
 			messages.get(timeStep).put(senderAddress, msgs);
 		}
 	}
 	
 	/**
 	 * Encapsulates all information that is sent in a packet through
 	 * the UDP socket.  It includes header information and a subset
 	 * of the bytes that compose a message.
 	 */
 	private static class MessagePacket implements Serializable
 	{
 		private static final long serialVersionUID = -4623292576498196472L;
 		
 		private int timeStep;
 		private int numPackets;
 		private int packetNumber;
 		private int playerID;
 		private byte[] data;
 		
 		public MessagePacket(int playerID, int timeStep, int packetNumber, int numPackets, byte[] data)
 		{
 			this.timeStep = timeStep;
 			this.numPackets = numPackets;
 			this.packetNumber = packetNumber;
 			this.playerID = playerID;
 			this.data = data;
 		}
 	}
 	
 	private static class ResendRequest implements Serializable
 	{
 		private static final long serialVersionUID = 5045500930325923878L;
 		private int timeStep;
 		
 		public ResendRequest(int timeStep)
 		{
 			this.timeStep = timeStep;
 		}
 	}
 	
 	private static class MessageConstructor
 	{
 		/**
 		 * Reconstructs the original messages from the given packets.
 		 * 
 		 * @param packets
 		 *            The packets that represent the Message to be returned.
 		 * @return The messages that the packets compose.
 		 * @throws InvalidMessageException If the message could not be constructed.
 		 */
 		public static Message[] constructMessage(MessagePacket[] packets) throws InvalidMessageException
 		{
 			// orders the packets and calculates the total size
 			int dataSize = 0;
 			int[] ordering = new int[packets.length];
 			for (int i = 0; i < packets.length; ++i)
 			{
 				ordering[packets[i].packetNumber] = i;
 				dataSize += packets[i].data.length;
 			}
 			
 			// combine all the byte segments into one array
 			byte[] msgData = new byte[dataSize];
 			int curPos = 0;
 			for (int i = 0; i < packets.length; ++i)
 			{
 				byte[] curData = packets[ordering[i]].data;
 				for (int j = 0; j < curData.length; ++j, ++curPos)
 				{
 					msgData[curPos] = curData[j];
 				}
 			}
 			
 			// return the deserialized object
 			try {
 				return (Message[]) Serializer.deSerialize(msgData);
 			} catch (Exception e) {
 				throw new InvalidMessageException(e);
 			}
 		}
 		
 		/**
 		 * Splits the message into packet objects that can be serialized and sent
 		 * across the network.
 		 * 
 		 * @param msg
 		 *            The message to be split into packets.
 		 * @return A list of packets that together compose the message object.
 		 * @throws IOException
 		 *             If there was a problem serializing the message object into a
 		 *             byte array.
 		 */
 		public static MessagePacket[] createMessagePackets(Message[] msg)
 		{
 			if (msg == null || msg.length == 0)
 				return new MessagePacket[0];
 			
 			// serializes the message
 			byte[] msgBytes = Serializer.serialize(msg);
 			
 			// calculates the number of packets required to send the message
 			int numPackets = (int) Math.ceil((1.0 * msgBytes.length) / TARGET_MTU);
 			MessagePacket[] msgPackets = new MessagePacket[numPackets];
 			
 			// creates the packets
 			int curPos = 0;
 			for (int i = 0; i < numPackets; ++i)
 			{
 				int packetSize = (i == numPackets-1) ? msgBytes.length % TARGET_MTU : TARGET_MTU;
 				
 				// grabs a segment of the message bytes to store in the current packet
 				byte[] packetBytes = new byte[packetSize];
 				for (int j = 0; j < packetSize; ++j, ++curPos)
 				{
 					packetBytes[j] = msgBytes[curPos];
 				}
 				msgPackets[i] = new MessagePacket(msg[0].getPlayerId(), msg[0].getTimeStep(), i, numPackets, packetBytes);
 			}
 			return msgPackets;
 		}
 	}
 	
 	private static class Serializer
 	{
 		/**
 		 * Deserializes an array of bytes into an object.
 		 * 
 		 * @param b
 		 *            The serialized byte array to be used in instantiating the
 		 *            object.
 		 * @return The instantiated object representation of the bytes provided.
 		 * @throws IOException
 		 *             If something bad happens during the serialization process.
 		 * @throws ClassNotFoundException
 		 *             If the class being instaniated isn't recognized.
 		 */
 		private static Object deSerialize(byte[] b) throws IOException, ClassNotFoundException
 		{
 			ObjectInputStream os = new ObjectInputStream(new ByteArrayInputStream(b));
 			Object o = os.readObject();
 			os.close();
 			return o;
 		}
 		
 		/**
 		 * Serializes a serializable object into an array of bytes.
 		 * 
 		 * @param s
 		 *            The serializable object to be serialized.
 		 * @return An array of bytes representing the object provided.
 		 */
 		private static byte[] serialize(Object s)
 		{
 			try {
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				ObjectOutputStream os = new ObjectOutputStream(out);
 				os.writeObject(s);
 				os.close();
 				return out.toByteArray();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}
 	
 	private static class MessageID
 	{
 		private int playerID;
 		private int timeStep;
 		
 		public MessageID(int playerID, int timeStep)
 		{
 			this.playerID = playerID;
 			this.timeStep = timeStep;
 		}
 		
 		@Override
 		public boolean equals(Object o)
 		{
 			if (!(o instanceof MessageID)) return false;
 			
 			MessageID m = (MessageID) o;
 			return (playerID == m.playerID && timeStep == m.timeStep);
 		}
 		
 		@Override
 		public int hashCode()
 		{
 			return (playerID * 31) + (timeStep * 31 * 31);
 		}
 	}
 
 	private static class InvalidMessageException extends Exception
 	{
 		private static final long serialVersionUID = -7580077906255482160L;
 
 		public InvalidMessageException(Throwable e)
 		{
 			super(e);
 		}
 	}
 }
