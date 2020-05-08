 package server;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import constants.Constants;
 
 import messages.MessagesRepository;
 import messages.ReceiveServerMessages;
 
 /* Message types and their definitions:
  * I_WILL_BE_PRIMARY_SERVER:
  *     -> This is the first message sent when the server is up, informing
  *        the other server that it desires to be the primary server.
  *        Then, it waits for a response from the other server, indicating
  *        whether it is already the primary server or not.
  *        
  * I_M_ALREADY_PRIMARY_SERVER:
  *     -> Upon receiving a I_WILL_BE_PRIMARY_SERVER message, if it is
  *        already primary server, it will send this message.
  *        
  * OK:
  *     -> Upon receiving a I_WILL_BE_PRIMARY_SERVER message, if it isn't
  *        already the primary server, it will send this message. The use
  *        of this message is unlikely.
  *        
  * KEEP_ALIVE:
  *     -> Message used by the main server to inform the secondary server
  *        that it is functioning.
  */
 
 public class ConnectionWithServerManager extends Thread{
 	/*Set to true if you want the program to display debugging messages.*/
 	private boolean debugging = true;
 
 	/* Connection variables. */
 	private int serverPort;
 	private int partnerPort;
 	private MessagesRepository msgToReceiveList;
 	private ReceiveServerMessages receiveMessenger;
 	
 	/* The lock to synchronize with the server. */
 	private ChangeStatusLock statusLock;
 	/* This variables is used when both servers decide, at the same time
 	 * that they should be the main server. When this happens and both of them
 	 * detect this inconsistency (two main servers), the one with a 'false' value
 	 * will give up from being main server.
 	 */
 	private boolean isDefaultServer;
 	/* Variables that checks whether this is the primary server or not. */
 	private boolean isPrimaryServer = false;
 	private boolean isPartnerDead = false;
 	
 	/*Variables related to the sending action.*/
 	private DatagramSocket aSocket = null;
 	private String msgToSend = "";
 	
 	/* The port to which we must connect to simulate the STONITH situation. */
 	private int partnerStonithPort;
 	private int stonithPort;
 	private StonithManager stonithManager;
 	private boolean stonithFailed;
 	
 	public ConnectionWithServerManager(int sPort, int pPort, int sStonith, int pStonith, boolean isDefaultServer, ChangeStatusLock lock){
 		serverPort = sPort;
 		partnerPort = pPort;
 		this.isDefaultServer = isDefaultServer;
 		msgToReceiveList = new MessagesRepository();
 		receiveMessenger = new ReceiveServerMessages(serverPort, msgToReceiveList, this);
 		statusLock = lock;
 		partnerStonithPort = pStonith;
 		stonithPort = sStonith;
 		
 		/* Initializes the UDP socket to send messages to the other server. */
 		try {
 			aSocket = new DatagramSocket();
 		} catch (SocketException e) {
 			System.out.println("Socket (ConnectionWithServerManager Constructor): " + e.getMessage());
 		}
 		
 		this.start();
 	}
 	
 	public void run(){
 		String partnerAnswer = "NOT_RECEIVED"; //By default
 		int repetitions = 0; // In this variable, we count the number of times we tried to send
 		                 // the first message to our partner.
 		int limit = 3; //The upper limit of these retries.
 		
 		/* When the server is up, it sends the first message,
 		 * corresponding to the I_WILL_BE_PRIMARY_SERVER.
 		 */
 		while (repetitions < limit){
 			sendMessage("I_WILL_BE_PRIMARY_SERVER");
 			
 			/* Then, it waits for the other server to respond,
 			 * acting accordingly to the answer received (or not
 			 * received at all...) .
 			 */
 			try {
 				Thread.sleep(Constants.FIRST_WAITING_TIME);
 			} catch (InterruptedException e) {
 				/* We have just received an answer.
 				 * So, we can go off this cycle.
 				 */
 				break;
 			}
 			repetitions++;
 		}
 		
 		/* This means the other server hasn't responded. */
 		if (repetitions == limit){
 			/* Now, we have to test the STONITH scenario. */
 			//TODO: We have to change this local host. */
 			Socket s = null;
 			try {
 				s = new Socket("localHost", partnerStonithPort);
 				stonithFailed = false;
 				/* The other server is alive. */
 				partnerAnswer = "I_M_ALREADY_PRIMARY_SERVER";
 				isPrimaryServer = false;
 				isPartnerDead = false;
 				
 			} catch (Exception e) {
 				/* The other server is dead, so, it's not only a link's problem. */
 				stonithFailed = true;
 				partnerAnswer = "NOT_RECEIVED";
 				isPrimaryServer = true;
 				isPartnerDead = true;
 			}	finally {
 				//TODO: If we implement the todo down there, we can't immediately close the socket.
 			    if (s != null)
 					try {
 					    s.close();
 					} catch (IOException e) {
 					    System.out.println("close:" + e.getMessage());
 					}
 				}
 			
 			if (!stonithFailed){
 				
 			}
 			
 			//TODO: Maybe we should check the STONITH periodically till the network recovers.
 		}
 		
 		/* We initialize and start the STONITH Manager. */
 		stonithManager = new StonithManager(stonithPort);
		
 		
 		while(true){
 			if (debugging){
 				System.out.println("I'm primary server?: " + isPrimaryServer);
 			}
 			synchronized(msgToReceiveList){
 				/* We have some messages to read. */
 				if (msgToReceiveList.listSize() > 0){
 					partnerAnswer = msgToReceiveList.getMsg();
 				}
 			}
 			
 			/* We are now the primary server. */
 			if ((partnerAnswer.equals("OK") && isDefaultServer)
 					|| (partnerAnswer.equals("I_WILL_BE_PRIMARY_SERVER") && isDefaultServer)){
 				
 				if (debugging){
 					System.out.println("ConnectionWithServerManager: We fulfilled the first condition.");
 				}
 				
 				isPrimaryServer = true;
 				
 				/* Informs that parent server about its status. */
 				synchronized (statusLock){
 					statusLock.setInitialProcessConcluded(true);
 					statusLock.setPrimaryServer(true);
 					if (statusLock.hasChangedStatus()){
 						statusLock.notifyAll();
 					}
 				}
 				
 				/* We can now terminate the receiveMensager, it won't be needed any longer. */
 				if (debugging){
 					System.out.println("We are going to terminate the receive messenger thread.");
 				}
 				receiveMessenger.terminateThread();
 				sendTerminateMessage();
 				
 				while(true){
 					/* Once we are simultaneously the main server and default server,
 					 * we will only give up from that position if the server crashes.
 					 * Consequently, we enter this endless cycle, always sending
 					 * KEEP_ALIVE messages hoping the system does never fail.
 					 */
 					try {
 						Thread.sleep(Constants.KEEP_ALIVE_TIME);
 					} catch (InterruptedException e) {
 						/* We have received a message, so keep going. */
 					}
 					sendMessage("KEEP_ALIVE");
 				}
 			}
 			
 			/* We are now the main server, but we are not the default server. 
 			 * (continue down there...)
 			 * TODO: This situation might be cleaned up by some assumptions to be
 			 *       given by the teacher later.
 			 */
 			
 			else if(partnerAnswer.equals("OK")
 					/* It means that our partner has crashed and is now returning to activity. */
 					|| (partnerAnswer.equals("I_WILL_BE_PRIMARY_SERVER") && isPartnerDead)){
 				
 				/* (...continues from above)
 				 * Therefore, we can't just send KEEP_ALIVE messages. If for any reason
 				 * we have a failure in the link, the other server will eventually think
 				 * it is the main server. When the link is reestablished, we shall
 				 * eventually receive a KEEP_ALIVE and in that case, we ought 
 				 */
 				
 				if (debugging){
 					System.out.println("ConnectionWithServerManager: We fulfilled the second condition.");
 				}
 				
 				isPrimaryServer = true;
 				isPartnerDead = false;
 				
 				/* Informs that parent server about its status. */
 				synchronized (statusLock){
 					statusLock.setInitialProcessConcluded(true);
 					statusLock.setPrimaryServer(true);
 					if (statusLock.hasChangedStatus()){
 						statusLock.notifyAll();
 					}
 				}
 				
 				/* We continue here while we don't get any KEEP_ALIVE message. */
 				while(isPrimaryServer){
 					/* Once we are simultaneously the main server and default server,
 					 * we will only give up from that position if the server crashes.
 					 * Consequently, we enter this endless cycle, always sending
 					 * KEEP_ALIVE messages hoping the system does never fail.
 					 */
 					sendMessage("KEEP_ALIVE");
 					
 					try {
 						Thread.sleep(Constants.KEEP_ALIVE_TIME);
 					} catch (InterruptedException e) {
 						/* We have received a message, what wasn't expected
 						 * in a normal situation.
 						 */
 					}
 					
 					synchronized(msgToReceiveList){
 						while (msgToReceiveList.listSize() > 0){
 							partnerAnswer = msgToReceiveList.getMsg();
 							if (partnerAnswer.equals("KEEP_ALIVE")){
 								/* We give up from the primary server status. */
 								isPrimaryServer = false;
 								
 								/*TODO: This may lead to bugs in the server, as we only wait if we
 								 *      are the secondary server. If we are the primary server, this
 								 *      notify is likely to be lost, but let's check it out later.
 								 */
 								
 								/* Informs that parent server about its status. */
 								synchronized (statusLock){
 									statusLock.setPrimaryServer(false);
 									if (statusLock.hasChangedStatus()){
 										statusLock.notifyAll();
 									}
 								}
 								
 							}
 							else if (partnerAnswer.equals("I_WILL_BE_PRIMARY_SERVER")){
 								/* We notify the other server that we are already the
 								 * primary server.
 								 */
 								sendMessage("I_M_ALREADY_PRIMARY_SERVER");
 							}
 						} // while (msgToReceiveList.listSize() > 0)
 					} // synchronized(msgToReceiveList)
 				}
 			}
 			/* We are now the secondary server. If we get here. */
 			else if(partnerAnswer.equals("KEEP_ALIVE") 
 					|| partnerAnswer.equals("I_M_ALREADY_PRIMARY_SERVER")
 					/* The partner isn't considered dead, so it is likely that we have
 					 * just sent the first message. */
 					|| (partnerAnswer.equals("I_WILL_BE_PRIMARY_SERVER") && !isDefaultServer)){
 				
 				if (debugging){
 					System.out.println("ConnectionWithServerManager: We fulfilled the third condition.");
 				}
 				
 				isPrimaryServer = false;
 				
 				/* Informs that parent server about its status. */
 				synchronized (statusLock){
 					statusLock.setInitialProcessConcluded(true);
 					statusLock.setPrimaryServer(false);
 					if (statusLock.hasChangedStatus()){
 						statusLock.notifyAll();
 					}
 				}
 			}
 		
 			/* If we ever get here, it means that we are not the primary server.*/
 			while (!isPrimaryServer && !partnerAnswer.equals("NOT_RECEIVED")){
 				
 				if (debugging){
 					System.out.println("ConnectionWithServerManager: We fulfilled the fourth condition.");
 				}
 				
 				try {
 					Thread.sleep(Constants.SERVER_WAITING_TIME);
 				} catch (InterruptedException e) {
 					/* A message has arrived before the timeout. */
 					synchronized(msgToReceiveList){
 						while (msgToReceiveList.listSize() > 0){
 							partnerAnswer = msgToReceiveList.getMsg();
 							
 							/* Our partner has informed us that it is still alive. */
 							if (partnerAnswer.equals("KEEP_ALIVE")){
 								continue;
 							}
 							/* The other server has crashed but recover before the
 							 * timeout occurred. So, it will still be the primary server.
 							 */
 							else if (partnerAnswer.equals("I_WILL_BE_PRIMARY_SERVER")){
 								sendMessage("OK");
 							}
 						} // while
 						continue;
 					}// synchronized (msgToReceiveList)
 				}// catch (InterruptedException e);
 				
 				/* If we ever get here, it means that the partner hasn't answer before
 				 * the timeout occurred. Consequently, we will now be the primary server.
 				 */
 				isPrimaryServer = true;
 				/* Informs that parent server about its status. */
 				synchronized (statusLock){
 					statusLock.setPrimaryServer(true);
 					if (statusLock.hasChangedStatus()){
 						statusLock.notifyAll();
 					}
 				}
 			}// while (!isPrimaryServer)
 			
 			try {
 				/* We will sleep till the other server sends a sign (i.e. a message)
 				 * that it's alive and communicating. Meanwhile, it's pointless
 				 * to waste both local and network resources by sending messages.
 				 */
 				
 				
 				if (debugging){
 					System.out.println("We are now going to stop sending messages till our partner " +
 							" sends us a message.");
 				}
 				
 				isPartnerDead = true;
 				
 				/* Informs that parent server about its status. */
 				synchronized (statusLock){
 					statusLock.setInitialProcessConcluded(true);
 					statusLock.setPrimaryServer(true);
 					if (statusLock.hasChangedStatus()){
 						statusLock.notifyAll();
 					}
 				}
 				
 				synchronized(msgToReceiveList){
 					msgToReceiveList.wait();
 				}
 			} catch (InterruptedException e) {
 				/* Our partner has awaken! */
 			}
 		}// while(true) from high above!
 	}
 	
 	public void sendMessage(String msgToSend){
 		/* Sends a message to the partner port. */
 		if (debugging){
 			System.out.println("We are sending " + msgToSend + " to the other server, in port " + partnerPort +  ".");
 		}
 		
 		try {
 			byte [] m = msgToSend.getBytes();
 			InetAddress aHost = InetAddress.getByName("localhost");
 
 			DatagramPacket request = new DatagramPacket(m,m.length,aHost,partnerPort);
 			aSocket.send(request);
 			
 		} catch (IOException e){
 			System.out.println("IO from sendMessage (ConnectionWithServerManager): " + e.getMessage());
 		}
 	}
 	
 	public void sendTerminateMessage(){
 		/* Sends a terminate thread message to our receive messenger. */
 		String message = "TERMINATE THREAD";
 		
 		if (debugging){
 			System.out.println("We are sending a terminate thread message.");
 		}
 		
 		try {
 			byte [] m = message.getBytes();
 			InetAddress aHost = InetAddress.getByName("localhost");
 
 			DatagramPacket request = new DatagramPacket(m,m.length,aHost,serverPort);
 			aSocket.send(request);
 			
 		} catch (IOException e){
 			System.out.println("IO from sendTerminateThread (ConnectionWithServerManager): " + e.getMessage());
 		}
 	}
 
 }
