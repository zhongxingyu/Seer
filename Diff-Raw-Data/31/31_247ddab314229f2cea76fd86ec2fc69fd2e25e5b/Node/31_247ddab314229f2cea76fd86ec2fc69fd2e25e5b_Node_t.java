 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.concurrent.Semaphore;
 
 
 public class Node extends Thread implements Comparable<Node>{
 	private boolean debuggingOn = true;
 	Map<String, List<String>> sortMessages = new HashMap<String, List<String>>();
 
 	// my 'letterbox'
 	private ServerSocket serverSocket;
 	private ServerSocket serverSocket2;
 	int port;
 	int port2;
 
 	private int seqNum = -1;
 	private int N;
 	List<Integer> list;
 
 	// channels
 	String masterHost;
 	int masterPort;
 	private Socket masterSocket;
 	private PrintWriter masterOut;
 	private BufferedReader masterIn;
 
 
 	//	private Socket leftSocket;
 	//	private PrintWriter leftOut;
 	//	private BufferedReader leftIn;
 	//
 	//	private Socket rightSocket;
 	//	private PrintWriter rightOut;
 	//	private BufferedReader rightIn;
 
 	Semaphore listSem = new Semaphore(1, true);
 	Semaphore threadSynchSem = new Semaphore(0, true);
 	boolean done = false;
 
 
 	/**
 	 * Constructor 
 	 * @param myPort
 	 * @param masterHost
 	 * @param masterPort
 	 */
 	public Node(int myPort, int myPort2, String masterHost, int masterPort, boolean debuggingOn){
 
 		
 		
 		this.masterHost = masterHost;
 		this.masterPort = masterPort;
 		this.debuggingOn = debuggingOn;
 		this.list = new ArrayList<Integer>();
 		this.port = myPort;
 		this.port2 = myPort2;
 		try {
 			// create serverSocket
 			this.serverSocket = new ServerSocket(myPort);
 			this.serverSocket2 = new ServerSocket(myPort2);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method connects to master, sets up for the sort, performs the sort, and returns the results to master.
 	 */
 	public void run(){
 		try{	
 			String fromServer;
 			
 			// connect to master
 			this.masterSocket = new Socket(masterHost, masterPort);
 			this.masterOut = new PrintWriter(masterSocket.getOutputStream(), true);
 			this.masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
 
 			// tell master my port numbers and hostname
 			this.masterOut.println(port + "");
 			this.masterOut.println(port2 + "");
 			this.masterOut.println(masterSocket.getLocalAddress().getHostName());
 
 			// get seqNum and N from master
 			print("Getting seqNum and N from master");
 			fromServer = masterIn.readLine();
 			Scanner scan = new Scanner(fromServer);
 			seqNum = scan.nextInt();
 			N = scan.nextInt();
 			scan.close();
 			
 			// initialise the sortMessages lists
 			sortMessages.put(String.format("t1node%d: ", seqNum), new ArrayList<String>());
 			sortMessages.put(String.format("t2node%d: ", seqNum), new ArrayList<String>());
 
 			// get right node info (from master) if not last in sequence
 			int port = -1;
 			int port2 = -1;
 			String host = null;
 			if(seqNum != N - 1){
 				print("Getting right node info from master.");			
 				fromServer = masterIn.readLine();
 				scan = new Scanner(fromServer);	
 				port = scan.nextInt();
 				port2 = scan.nextInt();
 				host = scan.next();
 				scan.close();
 			}
 
 			// recieve array from master
 			print("Receiving array...");
 			fromServer = masterIn.readLine();
 			scan = new Scanner(fromServer);
 			while(scan.hasNext()){
 				list.add(Integer.parseInt(scan.next()));
 			}
 			print("Received: " + listToString());
 
 			// start sorting woooo.  
 			//---------------------			
 			long startTime = System.currentTimeMillis();	
 
 			
 			Collections.sort(list);
 
 
 			SearchThread search1 = new SearchThread(false, serverSocket, port, host);
			//SearchThread search2 = new SearchThread(true, serverSocket2, port2, host);
 			search1.start();
			//search2.start();
 			search1.join();	
			//search2.join();
 
 			long timeTaken = System.currentTimeMillis() - startTime;
 
 			// print results for now
 			print("Partial answer: " + listToString());
 
 			// connect to master again (close previous connections first)
 			this.masterOut.close();
 			this.masterIn.close();
 			this.masterSocket = new Socket(masterHost, masterPort);
 			this.masterOut = new PrintWriter(masterSocket.getOutputStream(), true);
 			this.masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
 
 			// tell master the results, and how long it took
 			masterOut.println(seqNum);
 			masterOut.println(listToString());
 			masterOut.println(timeTaken);
 
 			this.masterIn.readLine();
 
 		} catch (Exception e){
 			e.printStackTrace();
 		} finally{
 			try {
 				// close everything
 				serverSocket.close();
 				serverSocket2.close();
 				masterSocket.close();
 				masterIn.close();
 				masterOut.close();		
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	private class SearchThread extends Thread{
 
 		boolean startAtTop;
 		ServerSocket serverSocket;
 		int rightPort;
 		String rightHost;
 
 		boolean firstRound = true;
 
 		// channels
 		private Socket leftSocket;
 		private PrintWriter leftOut;
 		private BufferedReader leftIn;
 
 		private Socket rightSocket;
 		private PrintWriter rightOut;
 		private BufferedReader rightIn;
 
 
 		public SearchThread(boolean startAtTop, ServerSocket serverSocket, int rightPort, String rightHost) throws IOException{			
 			this.startAtTop = startAtTop;
 			this.serverSocket = serverSocket;
 			this.rightPort = rightPort;
 			this.rightHost = rightHost;
 		}
 
 		public void run(){
 			Scanner scan;
 			
 			try{	
 				//  if not last in sequence, connect to right node
 				if(seqNum < N - 1){
 					print("Connecting to right node.", startAtTop);
 					this.rightSocket = new Socket(rightHost, rightPort);
 					this.rightOut = new PrintWriter(rightSocket.getOutputStream(), true);
 					this.rightIn = new BufferedReader(new InputStreamReader(rightSocket.getInputStream()));
 					print("Connected to right!", startAtTop);
 				}
 
 				// if not first in sequence, connect to left node if not first in sequence
 				if(seqNum != 0){
 					print("Connecting to left node.", startAtTop);
 					leftSocket = serverSocket.accept();
 					this.leftOut = new PrintWriter(leftSocket.getOutputStream(), true);
 					this.leftIn = new BufferedReader(new InputStreamReader(leftSocket.getInputStream()));
 					print("Connected to left!", startAtTop);
 				}
 				
 								
 				print("sort method started.", startAtTop);
 				
 	
 				boolean swapHappened = false;
 
 				if(!(leftSocket == null && rightSocket == null)){ // skip this if N = 1
 					while(!done){
 						// 1. the upwards pass
 						//=====================
 
 						if(!firstRound || (firstRound && !startAtTop)){
 
 							// 1a,b. receive highest from left, send reply
 							//------------------------------------------
 							if(seqNum != 0){ // the first node has already passed up
 								print("1a. receiving from left...", startAtTop);
 								scan = new Scanner(leftIn.readLine());
 
 								// check for exit signal
 								if(!scan.hasNextInt()){ 
 									print("1a. received done signal from node" + (seqNum - 1), startAtTop);
 									done = true;
 									if(seqNum < N-1){ // pass on if not at the end
 										rightOut.println("d");
 									}
 									continue;
 								}
 
 								// receive bubbled up highest value from left
 								int received = scan.nextInt();
 								swapHappened = scan.nextBoolean();
 
 								print(String.format("1a. received %d %b from node%d.", received, swapHappened, (seqNum-1)), startAtTop);
 
 								// if will swap
 								if(received > list.get(0)){ 
									print("acquiring list...", startAtTop);
									listSem.acquire();
									print("acquired list.", startAtTop);
													
 									swapHappened = true;
 									int sent = list.remove(0);  // swap for your lowest
 									leftOut.println(sent + ""); // send reply
 
 									print(String.format("1b. sendng reply %d to node%d, swap.", sent, (seqNum-1)), startAtTop);
 
 									list.add(received);
 									Collections.sort(list);  // sort my array again
									
									listSem.release();
									print("released list.", startAtTop);
 								}
 								// otherwise no swap
 								else {
 									print(String.format("1b. sending reply %d to node%d, no swap.", received, (seqNum-1)), startAtTop);
 									leftOut.println(received + ""); // send reply
 								}

 							}	
 
 							// 1c,d. send highest right, receive reply
 							//--------------------------------------
 							if(seqNum < N - 1){ // all except the last node
 
 								print("acquiring list...", startAtTop);
 								listSem.acquire();
 								print("acquired list.", startAtTop);
 								
 								// send highest value right
 								int sent = list.remove(list.size() - 1);		
 								print(String.format("1c. sending %d %b to node%d, awaiting reply.", sent, swapHappened, (seqNum+1)), startAtTop);
 								rightOut.println(sent + " " + swapHappened);
 
 								
 								// receive lowest value reply from right
 								print(String.format("awaiting lowest value reply from right..."));
 								scan = new Scanner(rightIn.readLine());
 								int received = scan.nextInt();
 								print(String.format("1d. received %d from node%d.", received, (seqNum+1)), startAtTop);
 								if(list.isEmpty()){
 									list.add(received);
 								} else {
									list.add(list.size(), received); // add to the end
									print(listToString() + "will sort: " + (sent != received), startAtTop);
 								}
 								if(sent != received){
 									Collections.sort(list); // sort if received a new value
 								}
 								
 								listSem.release();
 								print("released list.", startAtTop);
 								
 							}
 
 							// At the top - see if sorted
 							//===========================
 							// the node at the end can tell if complete!
 							if(seqNum == N - 1 && !swapHappened){
 								print("at the top, sort is complete. Sending message down.", startAtTop);
 								done = true;
 								leftOut.println("d");
 								continue;
 							}	
 							
 //							// wait for other thread to finish before you switch
 //							if(seqNum == N - 1){
 //								if(threadSynchSem.hasQueuedThreads()){
 //									print("waking other thread.", startAtTop);
 //									threadSynchSem.release();
 //								} else{
 //									print("waiting for other thread to finish.", startAtTop);
 //									threadSynchSem.acquire();
 //								}
 //							}
 
 							// reset
 							swapHappened = false;
 							firstRound = false;
 						}
 
 						if(!firstRound || (firstRound && startAtTop)){
 							// 2. downwards pass
 							//==================
 
 							// 2a. receive lowest from right, send reply right
 							//------------------------------------------------
 							if(seqNum != N - 1){ // the last node has already passed down
 								scan = new Scanner(rightIn.readLine());
 
 								// check for exit signal
 								if(!scan.hasNextInt()){ 
 									print("2a. received done signal from node" + (seqNum+1), startAtTop);
 									done = true;
 									if(seqNum > 0){ // pass on if not the start node
 										leftOut.println("d");
 									}
 									continue;
 								}
 
 								// receive lowest value from right
 								int received = scan.nextInt();
 								swapHappened = scan.nextBoolean();
 
 								print(String.format("2a. received %d %b from node%d.", received, swapHappened, (seqNum+1)), startAtTop);
 
 								// if will swap
 								print("acquiring list...", startAtTop);
 								listSem.acquire();
 								print("acquired list.", startAtTop);
 								
 								if(received < list.get(list.size()-1)){ 
 									swapHappened = true;
 									int sent = list.remove(list.size()-1); // swap for your highest
 									rightOut.println(sent + ""); // send reply
 
 									print(String.format("2b. sendng reply %d to node%d, swap.", sent, (seqNum+1)), startAtTop);
 
 									list.add(received); 
 									Collections.sort(list);  // sort my array again
 								}
 								// otherwise no swap
 								else {
 									rightOut.println(received + ""); // send reply
 									print(String.format("2b. sending reply %d to node%d, no swap.", received, (seqNum+1)), startAtTop);
 								}
 								listSem.release();
 								print("released list.", startAtTop);
 							}
 
 							// 2b. send lowest left and wait for a reply from left
 							//----------------------------------------------------
 							print("acquiring list...", startAtTop);
 							listSem.acquire();
 							print("acquired list.", startAtTop);
 							if(seqNum != 0){ // all except the first node
 								// send lowest value left
 								int sent = list.remove(0);
 								leftOut.println(sent + " " + swapHappened);
 
 								print(String.format("2c. sending %d %b to node%d, awaiting reply.", sent, swapHappened, (seqNum-1)), startAtTop);
 
 								// receive highest value reply from left
 								scan = new Scanner(leftIn.readLine());
 								int received = scan.nextInt();
 								list.add(0, received); // put at start
								print(listToString() + "will sort: " + (sent != received), startAtTop);
 								if(sent != received){
 									Collections.sort(list); // sort if we received a different value
 								}
 
 								print(String.format("2d. received %d from node%d.", received, (seqNum-1)), startAtTop);
 							}
 							listSem.release();
 							print("released list.", startAtTop);
 
 							// At the bottom - see if sorted
 							//==============================
 							// the node at the start can tell if complete!
 							if(seqNum == 0 && !swapHappened){
 								print("at the bottom, sort is complete. Sending message up.", startAtTop);
 								done = true;
 								rightOut.println("d");
 								continue;
 							}
 
 //							// wait for other thread to finish before you switch
 //							if(seqNum == 0){
 //								if(threadSynchSem.hasQueuedThreads()){
 //									print("waking other thread.", startAtTop);
 //									threadSynchSem.release();
 //								} else{
 //									print("waiting for other thread to finish.", startAtTop);
 //									threadSynchSem.acquire();
 //								}
 //							}
 
 							// reset
 							swapHappened = false;
 							firstRound = false;
 						}
 					}
 				}
 
 			} catch(Exception e){
 				e.printStackTrace();
 			} finally {
 				try {
 					// close everything
 					if(leftSocket != null){
 						leftSocket.close();
 						leftIn.close();
 						leftOut.close();
 					}
 					if(rightSocket != null){
 						rightSocket.close();
 						rightIn.close();
 						rightOut.close();
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}	
 	
 	/**
 	 * A method to print debugging messages to the screen - will do nothing if the 'debuggingOn' field is false.
 	 * @param s
 	 */
 	private void print(String s){
 		if(!debuggingOn){
 			return;
 		}
 
 		if(seqNum == -1){
 			System.out.println("node?: " + s);
 		}
 		else {
 			System.out.println(String.format("node%d: ", seqNum) + s);
 		}
 	}
 	
 	
 
 	
 	/**
 	 * Print method for the different search threads to differentiate their messages.
 	 * @param s
 	 * @param startAtTop
 	 */
 	private void print(String s, boolean startAtTop){
 		if(debuggingOn){ 
 			String key = String.format("t%dnode%d: ", startAtTop ? 2 : 1, seqNum );
 			System.out.println(key + s);
 			sortMessages.get(key).add(s);
 		}
 	}
 
 	
 	public void printDebuggingForThread1(){		
 		// print messages in order
 		String key = String.format("t1node%d: ", seqNum);
 		for(String s: sortMessages.get(key)){
 			System.out.println(key + s);
 		}
 	}
 	
 	public void printDebuggingForThread2(){
 		// print messages in order
 		String key = String.format("t2node%d: ", seqNum);
 		for(String s: sortMessages.get(key)){
 			System.out.println(key + s);
 		}	
 	}
 	
 	/**
 	 * Converts the current list to a String representation.
 	 * eg. "1 2 3 4 5 "
 	 * @return
 	 */
 	private String listToString(){
 		String s = "";
 		for(Integer i: list){
 			s += i + " ";
 		}
 		return s;
 	}
 
 	@Override
 	public int compareTo(Node arg0) {
 		return this.seqNum - arg0.seqNum;
 	}
 
 
 }
