 package mnet;
 
 import java.io.IOException;
 import java.net.*;
 import java.util.Calendar;
 public class DHCPServer{
 	
 	private DatagramSocket socket;
 	private final static int MAX_LEN = 2048;
 	
 	public DHCPServer() throws SocketException{
 		try{
 			socket = new DatagramSocket(67);
 			System.out.println("DHCP Server Created.\n" +
 					   "Use capturePacket to capture an incoming request.");
 		}catch(SocketException e){
 			System.out.println("\nAn error occurred creating socket on port 67.\n" + 
 					"If running under linux, try using sudo.\n");
 			throw e;
 		}
 	}
 	
 	public void start(){
 		System.out.println("Server started listening on port 67...");
 		while(true){
 			byte[] temp = new byte[MAX_LEN];
 			DatagramPacket packet = new DatagramPacket(temp, MAX_LEN);
 			try {
 				socket.receive(packet);
 				ReplyThread reply  = new ReplyThread(packet);
 				Thread replyThread = new Thread(reply);
 				replyThread.run();
				System.out.println("Packet received at " + Calendar.getInstance().getTime().toString() + ".");
 			} catch (IOException e) { e.printStackTrace(); }
 		}
 	}
 	public byte[] getNextAddress(){
 		return new byte[] {(byte)192, (byte)168, 1, 53};
 	}
 	
 	public byte[] capturePacket(){
 		byte[] temp = new byte[MAX_LEN];
 		DatagramPacket packet = new DatagramPacket(temp, MAX_LEN);
 		System.out.println("Listening started on port 67...");
 		try{
 			socket.receive(packet);
 			System.out.println("Packet captured.");
 			int len = packet.getLength();
 			byte[] data = java.util.Arrays.copyOf(temp, len);
 			return data;
 		}catch(IOException e){e.printStackTrace();}
 		return null;
 	}
 	
 	private class ReplyThread implements Runnable{
 		DatagramPacket requestPacket;
 		public ReplyThread(DatagramPacket requestdp){
 			requestPacket = requestdp;
 		}
 		@Override
 		public void run() {
 			int len = requestPacket.getLength();
 			byte[] requestdata = java.util.Arrays.copyOf(requestPacket.getData(), len);
 			DHCPPacket request = new DHCPPacket();
 			request.read(requestdata);
			if(request.getOption((byte)53)[0] == Constants.DHCPDISCOVER)
				sendOffer(request);
		}
		public void sendOffer(DHCPPacket request){
 			DHCPPacket reply = new DHCPPacket();
 			reply.setOp(Constants.BOOTREQUEST)
 				 .setHtype((byte) 1)
 				 .setHlen ((byte) 6)
 				 .setHops ((byte) 0)
 				 .setXid  (request.getXid())
 				 .setSecs (0)
 				 .setBroadcastFlag(true)
 				 .setCiaddr(0)
 				 .setYiaddr(getNextAddress())
 				 .setSiaddr(requestPacket.getAddress().getAddress())
 				 .setGiaddr(0)
 				 .setChaddr(request.getChaddr())
 				 .setSname(0)
 				 .setFile(0);
 			
 			reply.addOption((byte)53, (byte)1, new byte[]{2}) //dhcp offer
				 .addOption((byte)1 , (byte)4, new byte[]{(byte)255, (byte)255, (byte)255 , (byte)0   }) //subnet mask
 				 .addOption((byte)3 , (byte)4, new byte[]{(byte)192, (byte)168, (byte)1   , (byte)1   }) //gateway
 				 .addOption((byte)6 , (byte)4, new byte[]{(byte)192, (byte)168, (byte)1   , (byte)1   }) //dns
 				 .addOption((byte)58, (byte)4, new byte[]{(byte)0  , (byte)1  , (byte)0xfa, (byte)0x40}) //renewal time
 				 .addOption((byte)59, (byte)4, new byte[]{(byte)0  , (byte)3  , (byte)0x75, (byte)0xf0}) //rebinding time
 				 .addOption((byte)51, (byte)4, new byte[]{(byte)0  , (byte)3  , (byte)0xf4, (byte)0x80}) //lease time
 				 .addOption((byte)54, (byte)4, new byte[]{(byte)192, (byte)168, (byte)1   , (byte)1   }); //server identifier
 			   //.addOption((byte)12, (byte)n, new byte[]{(byte)'y', 'our-pc' ...}) // client host name
 			
 			byte[] replydata = reply.array();
 			DatagramPacket replyPacket = new DatagramPacket(replydata, replydata.length);
 			
 			DatagramSocket socket;
 			try {
 				socket = new DatagramSocket();
 				replyPacket.setPort(68);
 				replyPacket.setAddress(InetAddress.getByAddress(new byte[]{(byte)255, (byte)255, (byte)255, (byte)255}));
 				socket.setBroadcast(true);
 				socket.send(replyPacket);
 			} catch (SocketException e) { e.printStackTrace();
 			} catch (UnknownHostException e) { e.printStackTrace();
 			} catch (IOException e) { e.printStackTrace(); }
 		}
 	}
 }
