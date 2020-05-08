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
 
 			while(clientSocket.isConnected()) {
 				capture();
 
 				if(detectHalf) {
 					if(motionDetector.detect()) {
 						if(monitor.getMode() == ServerMonitor.AUTO_MODE) {
 							monitor.setMode(ServerMonitor.MOVIE_MODE);
							if(!monitor.sendCommand(os)) {
 								break;	// something is wrong with connection so break loop and get new.
 							}
 						}
 					}
 				}
 				detectHalf = !detectHalf;
 
 
 				switch (monitor.getMode()) {
 				case ServerMonitor.IDLE_MODE:
 					if(System.currentTimeMillis() >= t) {
 						System.out.println("now: " + System.currentTimeMillis() + " t: " + t);
 						t += idleSendTime;
 						monitor.send(os,jpeg,len);
 					}
 					break;
 				case ServerMonitor.MOVIE_MODE:
 					t = System.currentTimeMillis();
 					monitor.send(os,jpeg,len);
 					break;
 				case ServerMonitor.AUTO_MODE:
 					if(System.currentTimeMillis() > t) {
 						t += idleSendTime;
 						monitor.send(os,jpeg,len);
 					}
 					break;
 
 				default:
 					System.out.println("CaptureAndSend: Mode does not exist!");
 					System.exit(1);
 					break;
 				}
 			} 
 		}
 	}
 
 	/**
 	 * Get jpeg from camera and length of data.
 	 */
 	private void capture() {
 		len = camera.getJPEG(jpeg, 0);	
 	}
 
 	/**
 	 * Tries to connect to camera. If not possible, sleeps and tries again.
 	 */
 	private void connectCamera() {
 
 		while(!camera.connect()) {
 			System.out.println("Server: Camera failed to connect!");
 			try {
 				sleep(2000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
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
