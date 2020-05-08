 /**
  * SYSC3303 - Term Project
  * The server will receive connections and log the information into the proper poll
  * @author Matthew Smith - 100 827 363
  * @author Mohamed Ahmed - 100 828 374
  * @version 03/05/2013
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
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import pollingSystem.PollingMessage;
 
 public class Server extends Observable implements Observer {
 
 
 	private ArrayList<Poll> polls;
 
 	/**The port which admin messages will be recieved on*/
 	public static final int ADMIN_RECEIVE_PORT = 23545;
 
 	/**The port which admin messages will be recieved on*/
 	public static final int VOTER_RECEIVE_PORT = 23461;
 
 	private int numberOfOngoingPolls;
 
 	/**
 	 * Constructor, does nothing at the moment
 	 */
 	public Server() {
 		numberOfOngoingPolls = 0;
 		polls = new ArrayList<Poll>();
 	}
 	
 	public static void main(String[] args) {
 		Server serv = new Server();
 
 		VoteListener voteListener = new VoteListener();
 		voteListener.addObserver(serv);
 		
 		AdminListener adminListener = new AdminListener();
 		adminListener.addObserver(serv);
 		
 		
 		VoteObserver voteObserver = new VoteObserver();
 		
 		serv.addObserver(voteObserver);
 
 		Thread voteListenThread = new Thread(voteListener);
 		voteListenThread.start();
 		
 		Thread adminListenThread = new Thread(adminListener);
 		adminListenThread.start();
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
 
 	/**
 	 * Update 
 	 */
 	public void update(Observable o, Object arg) {
 		System.out.println("Recieved a message: "+arg.toString());
 		
 		if(o.getClass().equals(VoteListener.class)) {
 			System.out.println("Vote Listener Sent me");
 			String[] stringArray = ((String)arg).split(" ");
 			long pollID = Long.parseLong(stringArray[0]);
 			long voteID = Long.parseLong(stringArray[1]);
 			for(Poll poll : polls) {
 				if(poll.getPollID().equals(pollID)) {
 					poll.vote(voteID);
 					this.setChanged();
 					this.notifyObservers(this);
 				}
 			}
 		} else if(o.getClass().equals(AdminListener.class)) {
 			String[] stringArray = ((String)arg).split("$");
 			String pollID = stringArray[0];
 			String secondWord = stringArray[1];
 			if(stringArray[1].equals("Create")){
 				ArrayList<String> questions = new ArrayList<String>();
 				for(int i = 2;  i < stringArray.length ; i++){
 					questions.add(stringArray[i]);
 				}
 				polls.add(new Poll(stringArray[1], questions));
 				this.notifyObservers(this);
 			}
 			else for(Poll poll : polls){
 				if(poll.getPollID().equals(pollID)){
 					if(secondWord.equals("Pause"))
 						poll.pause();
 					
 				}
			}
 		}
 	}
 	
 	
 	public void printPolls() {
 		for(Poll poll : polls){
 			poll.print();
 		}
 	}
 
 }
