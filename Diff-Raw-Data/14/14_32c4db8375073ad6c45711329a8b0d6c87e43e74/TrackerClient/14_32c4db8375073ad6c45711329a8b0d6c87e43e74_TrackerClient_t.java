 package cargame.desktop.gui;
 import java.io.InputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 
 public class TrackerClient extends Thread {
 
 	private int TIME_BETWEEN_TRIES = 1000;
 	//Tracker Server Address and port
 	private String ip;
 	private int port;
 	private GUILogOutput guiLogOutput;
 	private boolean ifGotAnswerFromTracker = false;
 	public boolean isServer;
 	public String toConnectIP;
 	public boolean stopClient = false;
 	
 	public TrackerClient(String ip, int port, GUILogOutput guiLogOutput){
 		super("MulticastServerThread");
 		this.ip = ip;
 		this.port = port;
 		this.guiLogOutput = guiLogOutput;
 	}
 	
 	public boolean ifGotAnswerFromTracker(){
 		return ifGotAnswerFromTracker;
 	}
 
 	public void run() {
 		try {
 			queryTheTracker();
 		} catch (UnreachableTracker e) {
 			// TODO Auto-generated catch block
 			guiLogOutput.writeOutput("Tracker is Unreachable");
 			ShootError.shoot("Unreachable Tracker", "Tracker is Unreachable!");            
 		}
 	}
 	
 	private void queryTheTracker() throws UnreachableTracker{
 		//------------Define and Initialize vars--------------
 		DatagramSocket socket = null;
 		DatagramPacket sendPacket = null;
 		DatagramPacket recPacket = null;
 		InetAddress serverAddress = null;
 		InputStream inputStream = null;
 		try {
 			// ------Preparing the Socket----------------------
 			byte[] buf = new byte[1000];
 			socket = new DatagramSocket();
 			socket.setSoTimeout(TIME_BETWEEN_TRIES);
 			// -------Preparing the Send Packet----------------
			String dString = ip+"!Alive__________________________";
 			buf = dString.getBytes();
 			serverAddress = InetAddress.getByName(this.ip);
 			sendPacket = new DatagramPacket(buf, buf.length, serverAddress,
 					this.port);
 			// -------Preparing the receive Packet Data-------
 			recPacket = new DatagramPacket(buf, buf.length, serverAddress, this.port);
 		} catch (SocketException e) {
 			// Can not open new socket, check the permissions
 			// To-Do the Appropriate job for Socket Exception
 			e.printStackTrace();
 		} catch (java.net.UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch ( Exception e){
 			System.out.println("Hi Exception Happened!");
 		}
 		int tries = 0;
 		while (tries < 5 && !stopClient) {
 			try {
 				//It sends the request and wait for TIME_BETWEEN_TRIES ms
 				socket.send(sendPacket);
 				socket.receive(recPacket);
 				
 				String receivedMSG = new String(recPacket.getData(), 0,
 						recPacket.getLength());
 				System.out.println(receivedMSG);
 				ifGotAnswerFromTracker = true;
 				parseDataRecieved(receivedMSG);
 				
 				break;
 
 			} catch (SocketTimeoutException e) {
 				tries++;
 				guiLogOutput.writeOutput("Couldn't reach the tracker("+this.ip+":"+this.port+"), try #"+tries);
 				if(tries == 5)
 					throw new UnreachableTracker();
 			} catch (Exception e){
 				guiLogOutput.writeOutput("System error, please contact the developers!");
 			}
 		}
 		
 	}
 	
 	private void parseDataRecieved(String receivedMSG){
		System.out.println(receivedMSG);
 		String [] splittedMessage = receivedMSG.split("!");
 		
 		if(splittedMessage[0].equalsIgnoreCase("true"))
 			isServer = true;
 		else
 			isServer = false;
 		
 		toConnectIP = splittedMessage[1];
 	}
 }
