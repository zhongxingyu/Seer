 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.text.Format;
 
 // Port class
 // 
 public class Port {
 
 	private int localPort;					// real port on this PC for UDP
 	private IPv4 virtualIP;					// virtual IP for routing on this port
 	private int MTU;						// this ports' MTU 
 	private IPv4 remoteIP;					// physical remote IP for UDP packets
 	private int remotePort;					// physical remote port for UDP packets
 	private Listener listenPort;			// listener thread
 	private DatagramSocket datagramSocket;	// used by listener and writer
 	private boolean isConnected = false;	// true after connect, false after disconnect
 	
 	/*----------------------------------------------------------------------------------------*/
 	// constructor 
 	// setup a port, don't connect to anything yet
 	public Port(int localPort, String myIP, int mtu) throws SocketException {
 
 		this.localPort = localPort;									// port on this PC
 		this.virtualIP = new IPv4(myIP);							// this ports virtual IP
 		this.MTU = mtu;												// this ports (segment) MTU
 		this.datagramSocket = new DatagramSocket(localPort);		// used by writer & listener class
 		this.listenPort = new Listener(datagramSocket);				// new listener thread
 
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// store remote IP and remote port number
 	// start listener
 	public void connect(String ipRemotePort) {
 		
		if(isConnected == true) {
			System.out.println(localPort + ": disconnect before connecting");
		}
		else {
			String[] t = ipRemotePort.split(":");						// no input checking
			remoteIP = new IPv4(t[0]);									// store remote IP
			remotePort = Integer.parseInt(t[1]);						// store remote port num
			
			this.listenPort = new Listener(datagramSocket);				// new listener thread
			this.listenPort.start();									// start listening
			
			System.out.println(localPort + " connected to " + remoteIP.toString() + ":" + remotePort);
			isConnected = true;	
		}
 
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// stop listening for packets
 	public void disconnect() {
 		
 		if(isConnected == false) {
 			System.out.println("port " + localPort + " already disconnected");
 			return;
 		}
 		
 		listenPort = null;											// destroy listener thread
 		try {														// by forcing garbage collection
 																	// 
 			finalize();												// collect garbage
 			
 		} catch (Throwable e) {
 			
 			System.out.println("disconnecting port " + localPort);
 			e.printStackTrace();
 		}	
 		
 		System.out.println("port " + localPort + " stoped listening");
 		isConnected = false;	
 		
 	}
 	/*----------------------------------------------------------------------------------------*/
 	// package the data array in a UDP packed and send it to remoteIP:port
 	public void send(byte[] data) throws IOException {
 		
 		
 		if(isConnected == false) {
 			System.out.println("can't send, port " + localPort + " is disconnected");
 			return;
 		}
 		else if(data.length > 65508) {
 			System.out.println("UDP data length to large (> 65508)");
 			return;
 		}
 		
 		InetAddress address = InetAddress.getByAddress(remoteIP.getIP());
 		DatagramPacket packet = new DatagramPacket(data, data.length, address, remotePort);
 		System.out.println("UDP sent: " + data.length +" bytes to " + remoteIP.toString() + ":"+ remotePort );
 		String dataStr = new String(packet.getData());
 		System.out.println(dataStr);
 		datagramSocket.send(packet);
 
 	}
 	/*----------------------------------------------------------------------------------------*/
 	public boolean isConnected() {
 		
 		return isConnected;
 	}
 	/*----------------------------------------------------------------------------------------*/
 	public String getSettings() {
 	
 		String s = null;
 		
 		s  = String.format("local port: %d\n", localPort);
 		s += String.format("virtual IP: %s\n", virtualIP.toString());
 		s += String.format("MTU %d:\n", MTU);
 		
 		if(isConnected == true) {
 			s += String.format("remote IP: %s\n", remoteIP.toString());
 			s += String.format("remote port: %d\n", remotePort);
 		}
 		else {
 			s += "remote IP: not connected\n";
 			s += "remote port: not connected\n";
 		}
 		s += String.format("port connected: %s\n", isConnected);
 		
 		return s;
 	}
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	/*----------------------------------------------------------------------------------------*/
 	// return port number
 	public int getPortNum() {
 		return localPort;
 	}
 	/*----------------------------------------------------------------------------------------*/
 
 }
