 package udprsa.network.util;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 
 public class UDPSplitterSender {
 
 	private DatagramSocket udpSocket;
 	private int id;
 	private InetAddress ip;
 	public static final int SIZE  = 560;
 	public static final int FULLSIZE  = 569;
 	public UDPSplitterSender(DatagramSocket udpSocket, InetAddress ip){
 		this.udpSocket=udpSocket;
 		 id = 0;
 		 this.ip=ip;
 	}
 	
 	
 	public void splitAndSend(byte[] array) throws IOException{
 		int roundingCorrection=1;
 		if(array.length % SIZE == 0){
 			//no correction for rounding down needed
 			roundingCorrection = 0;
 		}
 
 		for(int i = 0; i < (array.length/SIZE)+roundingCorrection;i++){
 			byte[] idArray = ByteBuffer.allocate(4).putInt(id).array();
 			byte[] volgnrArray = ByteBuffer.allocate(4).putInt(i).array();
 			byte [] endArray = new byte[]{(byte) (i == (array.length/SIZE)+roundingCorrection-1?1:0)};
 			byte[] sending = new byte[FULLSIZE];
 			System.arraycopy(idArray, 0, sending, 0, 4);
 			System.arraycopy(volgnrArray, 0, sending, 4, 4);
 			System.arraycopy(endArray, 0, sending, 8, 1);
 			int length = Math.min(SIZE, array.length - (i*SIZE));
 			System.arraycopy(array, i*SIZE, sending, 9, length);
 			DatagramPacket packet = new DatagramPacket(sending, sending.length, ip,udpSocket.getLocalPort());
 			udpSocket.send(packet);
 			
 		}
 		id++;
		if(id == Integer.MAX_VALUE){
			id=0;
		}
 	}
 	
 }
