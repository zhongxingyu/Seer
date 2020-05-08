 /*
  *  A Stub that provides datagram send and receive functionality
  *  
  *  Feel free to modify this file to simulate network errors such as packet
  *  drops, duplication, corruption etc. But for grading purposes we will
  *  replace this file with out own version. So DO NOT make any changes to the
  *  function prototypes
  */
 package services;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.Random;
 
 import datatypes.Datagram;
 
 public class DatagramService {
 
 	private int port;
 	private int verbose;
 	private DatagramSocket socket;
 	private int counter;
 
 	public DatagramService(int port, int verbose) throws SocketException {
 		super();
 		this.port = port;
 		this.verbose = verbose;
 
 		socket = new DatagramSocket(port);
 	}
 
 	public void sendDatagram(Datagram datagram) throws IOException {
 		counter++;
 		
 		ByteArrayOutputStream bStream = new ByteArrayOutputStream(1500);
 		ObjectOutputStream oStream = new ObjectOutputStream(bStream);
 		oStream.writeObject(datagram);
 		oStream.flush();
 
 		byte[] data = bStream.toByteArray();
 		InetAddress IPAddress = InetAddress.getByName(datagram.getDstaddr());
 		DatagramPacket packet = new DatagramPacket(data, data.length,
 				IPAddress, datagram.getDstport());
 		
	/*	if(counter%5==0) {
 			System.out.println("Testing with Delayed Packets...");
 			Random r1 = new Random();
 			int delay = r1.nextInt(7500) + 7500;
 			sendDelayedPacket(packet,delay);
 			System.out.println("Packet sent after delay of " + delay);
		}  else if(counter%7==0) {
 			System.out.println("Testing with Duplicate Packets...");
 			Random r2 = new Random();
 			int count = r2.nextInt(3) + 3;
 			sendDuplicatePackets(packet, count);
 			System.out.println("Packet sent " + count + " times");
		} else if (counter%9==0) {
			System.out.println("Testing with Dropped Packets...");
		} else {
 			socket.send(packet);
		}*/
		socket.send(packet);
 	}
 
 	public Datagram receiveDatagram() throws IOException,
 	ClassNotFoundException {
 
 		byte[] buf = new byte[1500];
 		DatagramPacket packet = new DatagramPacket(buf, buf.length);
 
 		socket.receive(packet);
 
 		ByteArrayInputStream bStream = new ByteArrayInputStream(
 				packet.getData());
 		ObjectInputStream oStream = new ObjectInputStream(bStream);
 		Datagram datagram = (Datagram) oStream.readObject();
 
 		return datagram;
 	}
 	private void sendDelayedPacket(DatagramPacket packet,int delay) throws IOException {
 		try {
 			Thread.sleep(delay);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		socket.send(packet);
 	}
 
 	private void sendDuplicatePackets(DatagramPacket packet, int count) throws IOException {
 		for(int i = 0; i<count; i++)
 			socket.send(packet);
 	}
 
 }
