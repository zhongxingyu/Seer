 package pt.up.fe.dceg.neptus.plugins.gige;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class GigeManager {
 
 	public static final int CONTROL_PORT = 3956;
 	public static final int LISTEN_CONTROL_PORT = 3957;
 	public static final int LISTEN_STREAM_PORT = 0xE87D;
 
 	public static final int CMD_WRITEREG = 0x0082;
 	public static final int CMD_READREG = 0x0080;
 	public static final int CMD_RESENDREQUEST = 0x0040;
 	
 	public static final int REG_CCP = 0x00000A00;
 	public static final int REG_S0_DEST_PORT = 0x0D00;
 	public static final int REG_S0_SOURCE_PORT = 0x0D1C;
 	public static final int REG_S0_DEST_ADDR = 0x0D18;
 	public static final int REG_HEARTBEAT_TIMEOUT = 0x0938;
 	
 	DatagramSocket controlSocket = null;
 	DatagramSocket receiveSocket = null;
 	InetAddress IPAddress;
 
 	int packetSize;
 	
 	ArrayList<GigeDatagramListener> listenerList = new ArrayList<GigeDatagramListener>();
 	
 //	LinkedBlockingDeque<DatagramPacket> queue = new LinkedBlockingDeque<DatagramPacket>(1000);
 	ConcurrentLinkedQueue<DatagramPacket> queue = new ConcurrentLinkedQueue<DatagramPacket>();
 	
 	// Heartbeat timer task
 	// Just read CCP register to keep the connection alive
 	TimerTask heartbeat = new TimerTask() {
 		
 		@Override
 		public void run() {
 			try {
 				readRegister(0x0A00);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	};
 	
 	Thread receivingThread;
 	
 	public static int seq_num = 1;
 	
 	public GigeManager(String host) {
 		try {
 			controlSocket = new DatagramSocket(LISTEN_CONTROL_PORT);
 			receiveSocket = new DatagramSocket(LISTEN_STREAM_PORT);
 			
 			IPAddress = InetAddress.getByName(host);
 						
 //			// Stream dispatching thread
 //			new Thread(new Runnable() {
 //				@Override
 //				public void run() {
 //					while (true) {
 //						DatagramPacket packet;
 //
 //						if (queue.size() != 0) {
 //							packet = queue.poll();
 //
 //							// Relay packet
 ////							for (GigeDatagramListener listener : listenerList) {
 ////								listener.receivedDatagram(packet);
 ////							}
 //						}
 //					}
 //				}	
 //			}).start();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void addDatagramListener(GigeDatagramListener l) {
 		listenerList.add(l);
 	}
 	
	public void connect() throws IOException {
         int version = readRegister(0x0000);
         
         System.out.println("GigE Version: " + (version >> 16 & 0x0000FFFF) + "." + (version & 0x000000FF));
         System.out.println("Device Mode: " + readRegister(0x0004));
         System.out.println("Device Name: " + readRegisterString(0x0048, 4) + readRegisterString(0x0050, 4) + readRegisterString(0x0054, 4) + readRegisterString(0x0058, 4));
         System.out.println("S0 Source port: " + readRegister(REG_S0_SOURCE_PORT));
         System.out.println("S0 Destination port:" + readRegister(REG_S0_DEST_PORT));
         System.out.println("S0 Acquisition Mode:" + readRegister(0x13100));
         System.out.println("Write CCP: " + writeRegister(REG_CCP, 0x00000002));
         System.out.println("CCP : " + readRegister(REG_CCP));
         
         System.out.println(writeRegister(REG_S0_DEST_PORT, LISTEN_STREAM_PORT));
         System.out.println("S0 Destination port:" + readRegister(REG_S0_DEST_PORT));
         
        System.out.println(writeRegister(REG_S0_DEST_ADDR, ipToInt("10.0.10.79")));
         int address = readRegister(0xD18);
         System.out.println("Destination 32bit IP address for Channel 0: " + address);
 
         packetSize = (readRegister(0xD04) & 0x0000FFFF) - 28;
         System.out.println("Packet size: " + packetSize);
     
        Timer timer = new Timer(false);
        
         timer.scheduleAtFixedRate(heartbeat, 0, 1000); // Start heartbeat thread
         
         // Stream receiving thread
         receivingThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 byte[] buf = new byte[5000];
                 while(true) {
                     DatagramPacket packet = new DatagramPacket(buf,
                             buf.length);
                     try {
                         receiveSocket.receive(packet);
                         for (GigeDatagramListener listener : listenerList) {
                             listener.receivedDatagram(packet);
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
         
         receivingThread.start();
         
 	}
 	
 	public void startStream() {
 		try {
 			System.out.println("Write acquisition start register: " + writeRegister(0xD314, 0x01));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void stopStream() {
 		try {
 			System.out.println("Write acquisition stop register: " + writeRegister(0x13120, 0x01));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean writeRegister(int register, int data) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream os = new DataOutputStream(baos);
 
 		byte[] sendData;
 
 		os.writeByte(0x42); // Hard-coded
 		os.writeByte(0x01); // bit 7 to positive (decimal value 1) means require
 							// ack
 		os.writeShort(CMD_WRITEREG); // Command WRITEREG
 		os.writeShort(8); // Length = 8 bytes (address + value)
 		os.writeShort(seq_num++); // Req_id
 
 		os.writeInt(register);
 		os.writeInt(data);
 
 		sendData = baos.toByteArray();
 		DatagramPacket sendPacket = new DatagramPacket(sendData,
 				sendData.length, IPAddress, CONTROL_PORT);
 		controlSocket.send(sendPacket);
 
 		byte[] receiveData = new byte[1024];
 		DatagramPacket receivePacket = new DatagramPacket(receiveData,
 				receiveData.length);
 
 		controlSocket.receive(receivePacket);
 
 		ByteBuffer buf = ByteBuffer.wrap(receiveData);
 		if (buf.getShort(10) > 0)
 			return true;
 		return false;
 	}
 	
 	public boolean writeRegisterLong(int register, long data) throws IOException {
 		for(int i = 0; i < 2; i++) {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream os = new DataOutputStream(baos);
 
 		byte[] sendData;
 		
 		os.writeByte(0x42); // Hard-coded
 		os.writeByte(0x01); // require  ack
 		os.writeShort(CMD_WRITEREG); // Command WRITEREG
 		os.writeShort(8); // int(4 bytes) * 2(register + data) * 2(long = 2 * int) 
 		os.writeShort(seq_num++); // Req_id
 
 			os.writeInt(register + i * 2);
 			os.writeInt((int) ((data >> i * 16) & 0x0000FFFF));
 		
 		sendData = baos.toByteArray();
 		DatagramPacket sendPacket = new DatagramPacket(sendData,
 				sendData.length, IPAddress, CONTROL_PORT);
 		controlSocket.send(sendPacket);
 
 		byte[] receiveData = new byte[1024];
 		DatagramPacket receivePacket = new DatagramPacket(receiveData,
 				receiveData.length);
 
 		controlSocket.receive(receivePacket);
 
 		ByteBuffer buf = ByteBuffer.wrap(receiveData);
 //		if (buf.getShort(10) > 0)
 //			return true;
 //		return false;
 		}
 		return true;
 	}
 	
 	public int readRegister(int register) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream os = new DataOutputStream(baos);
 
 		byte[] sendData;
 		os.writeByte(0x42); // Hard-coded
 		os.writeByte(0x01); // bit 7 to positive (decimal value 1) means require
 							// ack
 		os.writeShort(CMD_READREG); // Command WRITEREG
 		os.writeShort(4); // Length = 8 bytes (address + value)
 		os.writeShort(seq_num++); // Req_id
 
 		os.writeInt(register);
 
 		sendData = baos.toByteArray();
 		DatagramPacket sendPacket = new DatagramPacket(sendData,							
 				sendData.length, IPAddress, CONTROL_PORT);
 		controlSocket.send(sendPacket);
 
 		byte[] receiveData = new byte[1024];
 		DatagramPacket receivePacket = new DatagramPacket(receiveData,
 				receiveData.length);
 
 		controlSocket.receive(receivePacket);
 
 		ByteBuffer buf = ByteBuffer.wrap(receiveData);
 		return buf.getInt(8);
 	}
 
 	public String readRegisterString(int register, int size) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream os = new DataOutputStream(baos);
 
 		byte[] sendData;
 
 		os.writeByte(0x42); // Hard-coded
 		os.writeByte(0x01); // bit 7 to positive (decimal value 1) means require
 							// ack
 		os.writeShort(CMD_READREG); // Command WRITEREG
 		os.writeShort(4); // Length = 8 bytes (address + value)
 		os.writeShort(seq_num++); // Req_id
 
 		os.writeInt(register);
 
 		sendData = baos.toByteArray();
 		DatagramPacket sendPacket = new DatagramPacket(sendData,
 				sendData.length, IPAddress, CONTROL_PORT);							//
 
 		controlSocket.send(sendPacket);
 
 		byte[] receiveData = new byte[1024];
 		DatagramPacket receivePacket = new DatagramPacket(receiveData,
 				receiveData.length);
 
 		controlSocket.receive(receivePacket);
 
 		ByteBuffer buf = ByteBuffer.wrap(receiveData);
 		byte[] dst = new byte[size];
 		buf.position(8);
 		buf.get(dst, 0, size);							//
 
 		return new String(dst);
 	}
 
 	public void requestResend(int firstPackageId, int lastPackageId, int blockID) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream os = new DataOutputStream(baos);
 
 		byte[] sendData;
 
 		os.writeByte(0x42); // Hard-coded
 		os.writeByte(0x00); 
 		os.writeShort(CMD_RESENDREQUEST); // Command WRITEREG
 		os.writeShort(12); // Length = 8 bytes (first + last)
 		os.writeShort(seq_num++); // Req_id
 
 		os.writeShort(0);
 		os.writeShort(blockID);
 		os.writeInt(firstPackageId);
 		os.writeInt(lastPackageId);
 
 		sendData = baos.toByteArray();
 		DatagramPacket sendPacket = new DatagramPacket(sendData,
 				sendData.length, IPAddress, CONTROL_PORT);							//
 
 		controlSocket.send(sendPacket);
 	}
 	
 	public int ipToInt(String addr) {
 		String[] addrArray = addr.split("\\.");
 
 		int num = 0;
 
 		for (int i = 0; i < addrArray.length; i++) {
 
 			int power = 3 - i;
 			//
 			//
 
 			num += ((Integer.parseInt(addrArray[i]) % 256 * Math
 					.pow(256, power)));
 
 		}
 		return num;
 	}
 	
 	public int getPacketSize() {
 		return packetSize;
 	}
 	public interface GigeDatagramListener {
 		public void receivedDatagram(DatagramPacket packet);
 	}
 }
