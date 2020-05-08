 package server;
 
 import java.net.*;
 import java.io.*;
 import se.lth.cs.fakecamera.Axis211A;
 import se.lth.cs.fakecamera.MotionDetector;
 
 /**
  * 
  * @author Viktor Andersson
  *
  * Gets as many images from the camera as possible. 
  * Sends some or all of them depending on mode. 
  * Regardless of mode, do motion detection frequently 
  * (maybe not on all images but on many).
  *
  */
 
 public class CaptureAndSend extends Thread{
 	private ServerMonitor monitor;
 	private Axis211A camera;
 	private MotionDetector motionDetector;
 	private byte [] jpeg = new byte[Axis211A.IMAGE_BUFFER_SIZE];
 	private int len;												// length of the (filled slots of) byte array.
 	private ServerSocket serverSocket;
 	private Socket clientSocket;
 	private OutputStream os;
 	private int port;
 	private int idleSendTime;										// Time to "wait" for sending when in idle mode. 
 
 	
 	/**
 	 * Constructor, creates a fake camera and gets port nbr.
 	 * @param port
 	 */
 	public CaptureAndSend(int port, ServerMonitor monitor) {
 		camera = new Axis211A();
 		this.port = port;
 		
 		idleSendTime = 5000;		// 5 sec.
 		
 		this.monitor = monitor;
 		motionDetector = new MotionDetector();
 	}
 
 	/**
 	 * Run method. Connects to camera and client, 
 	 * then while connected, captures images, 
 	 * detects motion and send to client.
 	 */
 	public void run(){
 		
 
 		while(true){
 
 			connectCamera();
 			connectClient();
 			
 			boolean detectHalf = true; // For performance, detect only half.
 			long t = System.currentTimeMillis();
			boolean sent = false;
 			while(clientSocket.isConnected()) {
 				capture();
 				
 				if(detectHalf) {
 					if(motionDetector.detect()) {
						//System.out.println("Server: Motion detected!");
 						// TODO: handle motion i.e. send message to client.
 						if(monitor.getMode() == ServerMonitor.AUTO_MODE) {
 							monitor.setMode(ServerMonitor.MOVIE_MODE);
 						}
 					}
 				}
 				detectHalf = !detectHalf;
 
 
 				switch (monitor.getMode()) {
 				case ServerMonitor.IDLE_MODE:
					if(System.currentTimeMillis() >= t && !sent) {
 						System.out.println("now: " + System.currentTimeMillis() + " t: " + t);
 						t += idleSendTime;
 						send();
						sent = true;
 					}
 					break;
 				case ServerMonitor.MOVIE_MODE:
 					t = System.currentTimeMillis();
 					send();
 					break;
 				case ServerMonitor.AUTO_MODE:
 					if(System.currentTimeMillis() > t) {
 						t += idleSendTime;
 						send();
 					}
 					break;
 
 				default:
 						System.out.println("Mode does not exist!");
 						System.exit(1);
 						break;
 				}
 			} 
 		}
 	}
 
 
 	/**
 	 * For testing...
 	 */
 	private void printByte() {
 		for(int i = 0; i < 20; i++) {
 			System.out.print(jpeg[i] + " ");
 		}
 		System.out.println();
 	}
 
 	/**
 	 * Get jpeg from camera. Prints first bytes for test.
 	 */
 	private void capture() {
 		len = camera.getJPEG(jpeg, 0);	
 	}
 
 	/**
 	 * Gets outputstream and then writes the jpeg byte array to it.
 	 */
 	private void send() {
 		// write        
 		try {
 		    // Create header
 			byte hi = (byte)(len / 255);
 			byte lo = (byte)(len % 255);
 				
 			// Send header
 			os.write(hi);
 			os.write(lo);
 		
 			os.write(jpeg,0,len);
 			os.flush();
 		} catch (IOException e) {
 			System.out.println("Server: /capture() Failed to write");
 			e.printStackTrace();
 		}        
 	}
 
 	/**
 	 * Tries to connect to camera. If not possible, exit system.
	 * TODO: Handle connection problem.
 	 */
 	private void connectCamera() {
		if(!camera.connect()) {
 			System.out.println("Server: Camera failed to connect!");
			System.exit(1);
 		}
 	}
 
 	/**
 	 * Creates ServerSocket and waits for client to connect. Also sets TcpNoDelay to true.
 	 */
 	private void connectClient() {
 		// create ServerSocket
 		try {
 			serverSocket = new ServerSocket(port);
 			System.out.println("Server:serverSocket created at port " + port);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("Server: Failed to create socket with port: " + port);
 		}
 
 		// wait for accept
 		try {
 			clientSocket = serverSocket.accept();
 			System.out.println("Server: Socket accepted!");
 
 			// Set the clientSocket to monitor (and notify in monitor) so that receive thread can access it.
 			monitor.setClientSocket(clientSocket);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("Server; Failed to accept socket!");
 		}
 
 		// Set TcpNoDelay
 		try {
 			clientSocket.setTcpNoDelay(true);
 		} catch (SocketException e) {
 			e.printStackTrace();
 			System.out.println("Server: Failed to set TcpNoDelay");
 		}
 		
 		// Get outputstream
 		try {
 			os = clientSocket.getOutputStream();
 		} catch (IOException e) {
 			System.out.println("Server: /capture() Failed to get Outputstream");
 			e.printStackTrace();
 		}
 		
 	}
 	
 }
