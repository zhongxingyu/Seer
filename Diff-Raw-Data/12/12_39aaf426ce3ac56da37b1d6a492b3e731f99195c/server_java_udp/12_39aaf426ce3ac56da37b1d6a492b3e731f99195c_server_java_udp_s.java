 import java.io.*;
 import java.math.*;
 import java.net.*;
 import java.security.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Fricken Hamster
  * Date: 10/9/13
  * Time: 11:34 AM
  */
 
 /*
 	I looked over the slides and the code here http://systembash.com/content/a-simple-java-udp-server-and-udp-client/ and basically did what I thought made sense
 	*/
 
 public class server_java_udp
 {
 	private DatagramSocket socket;
 	private int port;
 	
 	private byte netbit;//netbit is used to identify packets for ACK and stuff
 
 	private Dictionary<String, String> usernameDict;
 	private ArrayList<String> messages;
 
 	public static String WELCOME;
 
 	public static final byte CONNECT = 0x01;
 	public static final byte ACCEPTED = 0x02;
 	public static final byte PRINTALL = 0x03;
 	public static final byte SEND = 0x04;
 	public static final byte CONTENT = 0x05;
 	public static final byte SIZE = 0x09;
 	public static final byte ACK = 0x08;
 	
 	public server_java_udp(int port)
 	{
 		if (port < 1024 || port > 49151)
 		{
 			System.err.println("Invalid port. Terminating.");
 			System.exit(1);
 		}
 		this.port = port;
 		netbit = 0;
 		
 		usernameDict = new Hashtable<String, String>();
 		messages = new ArrayList<String>();
 	}
 	
 	public void start()
 	{
 		try
 		{
 			socket = new DatagramSocket(port);
 		} catch (SocketException e)
 		{
 			System.err.println("Could not bind port. Terminating.");
 			System.exit(1);
 		}
 
 		while (true)
 		{
 //			System.out.println("listen");
 			listenCommand();
 		}
 	}
 	
 	public void listenCommand()
 	{//this method just listens for any packets
 		try
 		{
 			byte[] sizeByte = new byte[16];
 			int packetSize;
 			while (true)
 			{
 				socket.setSoTimeout(0);
 //				System.out.println("listening for size");
 				DatagramPacket sizePacket = new DatagramPacket(sizeByte, sizeByte.length);
 				socket.receive(sizePacket);
 				DataInputStream in = new DataInputStream( new ByteArrayInputStream(sizePacket.getData()));
 				int id = in.readByte();
 
 				if (id == SIZE)
 				{
 					int netbit = in.readByte();
 					packetSize = in.readShort();
 					sendACK(netbit, sizePacket.getAddress(), sizePacket.getPort());
 //					System.out.println("recieved size," + netbit );
 					break;
 				}
 			}
 			while (true)
 			{
 				byte[] packetByte = new byte[packetSize];
 				DatagramPacket packet = new DatagramPacket(packetByte, packetByte.length);
 				socket.receive(packet);
 				socket.setSoTimeout(2000);
 				DataInputStream in = new DataInputStream( new ByteArrayInputStream(packet.getData()));
 				int id = in.readByte();
 				int packetnb = in.readByte();
 				InetAddress packetAddress = packet.getAddress();
 				int packetPort = packet.getPort();
 				switch(id)
 				{
 					case CONNECT:
 					{
 //						System.out.println("received connect from:" + packet.getAddress() + "," + packetnb);
 						String name = in.readUTF();
 						String identifier = generateIdentifier(name);
 						usernameDict.put(identifier, name);
 						sendACK(packetnb, packet.getAddress(), packet.getPort());
 						sendContent(identifier, netbit, packetAddress, packetPort);
 						sendWelcome(netbit, packetAddress, packetPort);
 						return;
 					}
 					case SEND:
 					{
 //						System.out.println("received send from:" + packet.getAddress() + "," + packetnb);
 						String identifier = in.readUTF();
 						String msg = in.readUTF();
 						newMessage(identifier, msg);
 						sendACK(packetnb, packet.getAddress(), packet.getPort());
 						return;
 					}
 					case PRINTALL:
 					{
 //						System.out.println("received print from:" + packet.getAddress() + "," + packetnb);
 						sendACK(packetnb, packet.getAddress(), packet.getPort());
 						sendPrintAll(netbit, packetAddress, packetPort);
 						return;
 					}
 				}
 			}
 
 		} catch (SocketTimeoutException e)
 		{
 			System.err.println("Failed to receive message. Terminating.");
 			System.exit(1);
 			return;
 		} catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 
 	public void sendContent(String identifier, int nb, InetAddress address, int port)
 	{
 		try
 		{
 			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(byteOut);
 			out.writeByte(ACCEPTED);
 			out.writeByte(nb + 1);
 			out.writeUTF(identifier);
 			byte[] bb = byteOut.toByteArray();
 			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, address, port);
 
 			sendSizePacket(bb.length, nb, address, port);
 			int timesSent = 0;
 			do
 			{
 				if (timesSent == 3)
 				{// sends a packet up to 3 times if ack is not recieved
 					System.err.println("Failed to send message. Terminating.");
 					System.exit(1);
 				}
 				timesSent++;
 				socket.send(packet);
 //				System.out.println("sent accepted" + (nb + 1));
 			}while(!waitAck(nb + 1));
 			incNetbit(2);
 		} catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 	
 	public void sendWelcome(int nb, InetAddress address, int port)
 	{
 		sendMessage("Welcome message: " + WELCOME, nb, address, port);
 	}
 	
 	public void sendMessage(String msg, int nb, InetAddress address, int port)
 	{
 		try
 		{
 			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(byteOut);
 			out.writeByte(CONTENT);
 			out.writeByte(nb + 1);
 			out.writeUTF(msg);
 			byte[] bb = byteOut.toByteArray();
 			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, address, port);
 			sendSizePacket(bb.length, nb, address, port);
 			int timesSent = 0;
 			do
 			{
 				if (timesSent == 3)
 				{// sends a packet up to 3 times if ack is not recieved
 					System.err.println("Failed to send message. Terminating.");
 					System.exit(1);
 				}
 				timesSent++;
 				socket.send(packet);
 //				System.out.println("sent message" + (nb + 1));
 			}while(!waitAck(nb + 1));
 			incNetbit(2);
 		} catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 
 	public String getMessagesString()
 	{
 		StringBuilder stringBuilder = new StringBuilder();
 		for (String message : messages)
 		{
 			stringBuilder.append(message);
 		}
 		return stringBuilder.toString();
 	}
 	
 	public void sendPrintAll(int nb, InetAddress address, int port)
 	{
 		try
 		{
 			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(byteOut);
 			out.writeByte(CONTENT);
 			out.writeByte(nb + 1);
 			out.writeUTF(getMessagesString());
 			byte[] bb = byteOut.toByteArray();
 			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, address, port);
 
 			sendSizePacket(bb.length, nb, address, port);
 			int timesSent = 0;
 			do
 			{
 				if (timesSent == 3)
 				{// sends a packet up to 3 times if ack is not recieved
 					System.err.println("Failed to send message. Terminating.");
 					System.exit(1);
 				}
 				timesSent++;
 				socket.send(packet);
 //				System.out.println("sent print shit" + (nb + 1));
 			}while(!waitAck(nb + 1));
 			incNetbit(2);
 		} catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 
 	public void sendSizePacket(int size, int nb, InetAddress address, int port)
 	{//size packet is sent first so other end knows how big the incoming file is
 		try
 		{
 			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(byteOut);
 			out.writeByte(SIZE);
 			out.writeByte(nb);
 			out.writeShort(size);
 			byte[] bb = byteOut.toByteArray();
 			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, address, port);
 			do
 			{
 //				System.out.println("size sent");
 				socket.send(packet);
 			}while(!waitAck(nb));
 
 		} catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 
 	public boolean waitAck(int nb)
 	{
 		try
 		{
 			while(true)
 			{
 				byte[] recieveByte = new byte[2];
 				DatagramPacket packet = new DatagramPacket(recieveByte, recieveByte.length);
 				socket.setSoTimeout(500);
 				socket.receive(packet);
 
 				DataInputStream in = new DataInputStream( new ByteArrayInputStream(packet.getData()));
 				int id = in.readByte();
 				if (id == ACK)
 				{
 					int packetnb = in.readByte();
 					if (packetnb != nb)
 						return false;
 //					System.out.println("recieved ACK:" + packetnb);
 					return true;
 				}
 			}
 		}
 		catch (SocketTimeoutException e)
 		{
 //			System.out.println("ack timeout:" + nb);
 			return false;
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 		return false;
 	}
 	
 	public void sendACK(int nb, InetAddress address, int port)
 	{
 		try
 		{
 			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(byteOut);
 			out.writeByte(ACK);
 			out.writeByte(nb);
 			byte[] bb = byteOut.toByteArray();
 			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, address, port);
 			socket.send(packet);
 			//System.out.println("sent ack:" + nb);
 		} catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 	public void incNetbit(int nn)
 	{
 		netbit += nn;
 		if (netbit > 100)
 			netbit -= 100;
 	}
 
 	public void newMessage(String identifier, String msg)
 	{
 		String username = usernameDict.get(identifier);
 		if (username == null)
 			return;
 		messages.add(username + ": " + msg + "\n");
 	}
 
 	public static void main(String[] args)
 	{
 		if (args.length != 1)
 		{
 			System.err.println("Invalid number of args. Terminating.");
 			System.exit(0);
 		}
 		Scanner scanner = new Scanner(System.in);
		WELCOME = scanner.nextLine();
 		server_java_udp server = new server_java_udp(Integer.parseInt(args[0]));
 		server.start();
 	}
 
 	public String generateIdentifier(String username)
 	{
 		String id = new BigInteger(130, new SecureRandom()).toString();
 		usernameDict.put(id, username);
 		return id;
 	}
 }
