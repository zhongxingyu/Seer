 /**
  * SYSC3303 - Term Project
  * The server will receive connections and log the information into the proper poll
  * @author Matthew Smith - 100 827 363
  * @version 02/5/2013
  */
 
package pollingSystem.server;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
import pollingSystem.PollingMessage;
 
 
 
 public class Server {
 	/**The port which admin messages will be recieved on*/
 	public static final int ADMIN_RECEIVE_PORT = 5000;
 
 	/**The port which admin messages will be recieved on*/
 	public static final int VOTER_RECEIVE_PORT = 5001;
 	
 	private int numberOfOngoingPolls;
 	
 	/**
 	 * Constructor, does nothing at the moment
 	 */
 	public Server() {
 		numberOfOngoingPolls = 0;
 	}
 	
 	public int numberOfPolls() {
 		return numberOfOngoingPolls;
 	}
 
 	/**
 	 * This method creates the admin thread and the TCP 
 	 * connection which listens for new polls being created.
 	 */
 	public void startAdminListener() {
 		Thread adminThread = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				ServerSocket receiveSocket;
 				try {
 					receiveSocket = new ServerSocket(ADMIN_RECEIVE_PORT);// bind to port 5000, socket will be used to receive TCP packets.
 					adminListen(receiveSocket);
 
 				} catch (IOException e) { //catch any exceptions thrown by creating the socket
 					e.printStackTrace();
 					System.exit(1);
 				}
 			}
 		});
 
 		adminThread.start();
 	}
 
 	/**
 	 * This method is for the adminThread to loop in. It receives data over the socket
 	 * and if a createPoll message is received then a poll thread is created.
 	 * @param receiveSocket The socket to receive TCP data on.
 	 */
 	private void adminListen(ServerSocket receiveSocket) {
 		PollingMessage incomingMessage; 
 		numberOfOngoingPolls++;
 		while(true) {
 			try {
 				Socket connectionSocket = receiveSocket.accept();
 				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
 				
 				incomingMessage = new PollingMessage(inFromClient.readLine());
 				System.out.println("Admin Received: " + incomingMessage);
 				
 				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
 				outToClient.writeBytes("Recieved Message");
 				
 				if(incomingMessage.isCreatePollMessage()) {
 					createPollThread();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				numberOfOngoingPolls--;
 			}
 		}
 		
 	}
 	
 	/**
 	 * This helper method creates the poll threads and their UDP 
 	 * connections which listen for new poll votes on the poll.
 	 */
 	private void createPollThread() {
 		//create a thread that listens on UDP for voters
 		Thread pollThread = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				DatagramSocket receiveSocket;
 				try {
 					receiveSocket = new DatagramSocket(VOTER_RECEIVE_PORT);// bind to port 5001, socket will be used to receive UDP packets.
 					
 					pollListen(receiveSocket);
 				} catch (SocketException se) {
 					se.printStackTrace();
 					System.exit(1);
 				}
 			}
 		});
 		pollThread.start();
 	}
 
 	/**
 	 * This method is for the Poll Thread to loop in. It receives data over the socket
 	 * and if a vote message is received then a vote for the poll is recorded.
 	 * @param receiveSocket The socket to receive UDP data on.
 	 */
 	private void pollListen(DatagramSocket receiveSocket) {
 		byte data[] = new byte[100];
 		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
 
 		// Block until a datagram packet is received from receiveSocket.
 		try {
 			receiveSocket.receive(receivePacket);
 		} catch (IOException e) {  e.printStackTrace(); System.exit(1); }
 
 		// Process the received datagram.
 		System.out.println("Poll Received " + new String(receivePacket.getData(),0,receivePacket.getLength()));
 	}
 	
 }
