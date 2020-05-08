 package client;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.util.logging.Logger;
 
 public class UdpPacketReceiver implements Runnable {
 
 	private static final Logger LOG = Logger.getLogger(UdpPacketReceiver.class.getName());
 
 	private DatagramSocket socket;
 	private DatagramPacket packet;
 	private MessageParser parser = new MessageParser();
 
 	private byte[] buf = new byte[256];
 
 	private int port;
 
 	public UdpPacketReceiver(int udpPort) {
 		port = udpPort;
 	}
 
 	@Override
 	public void run() {
 		try {
 			socket = new DatagramSocket(port);
 		} catch (SocketException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
//		LOG.info("get response");
 		packet = new DatagramPacket(buf, buf.length);
 		while(true) {
 			//blocking!
 			try {
 				socket.receive(packet);
 			} catch (IOException e) {
 				LOG.warning("problems receiving udp packet from server");
 			}
 
//			LOG.info("display response");
 			String received = new String(packet.getData(), 0, packet.getLength());
 			parser.parseMsg(received);
 		}
 	}
 	
 	public void closeAll() {
 		socket.close();
 	}
 
 }
