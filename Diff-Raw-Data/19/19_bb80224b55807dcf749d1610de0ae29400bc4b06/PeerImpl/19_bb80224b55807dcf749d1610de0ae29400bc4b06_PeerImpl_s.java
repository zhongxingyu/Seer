 package system;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Random;
 import java.util.UUID;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import system.Task.Shared;
 
 public class PeerImpl implements Peer {
 	static PeerImpl peer;
 	
 	static final BlockingDeque<Message> messagesIn = new LinkedBlockingDeque<Message>();
 	static final BlockingDeque<Message> messagesOut = new LinkedBlockingDeque<Message>();
 	//public ArrayList<Peer> peers = new ArrayList<Peer>();
 	public Shared shared = null;
 	UUID peerID;
 	public ArrayList<UUID> keys = new ArrayList<UUID>();
 	public Map<UUID, Peer> peerMap = new ConcurrentHashMap<UUID , Peer>();
 	
 	public int argumentThrownAway = 0;
 	
 	public Map<UUID, UUID> translations = new ConcurrentHashMap<UUID, UUID>();
 	
 	
 	
 	public RemoteQueue remoteQ;
 	public UUID remoteQueueHost;
 	public Map<UUID, RemoteQueueImpl> hostedQueues = new ConcurrentHashMap<UUID , RemoteQueueImpl>();
 	
 	// Compute stuff: 
 	public Map<String, Task> waitMap = new ConcurrentHashMap<String , Task>();
 	public final BlockingDeque<Task> readyQ = new LinkedBlockingDeque<Task>();
 	private final BlockingQueue<Object> result = new LinkedBlockingQueue<Object>();
 	public int taskExcecuted = 0;
 	public int composeTasksCreated = 0;
 	
 	public static void main(String[] args) {
 		
 		try {
 			if (System.getSecurityManager() == null ) { 
 				System.setSecurityManager(new java.rmi.RMISecurityManager() ); 
 			}
 			
 			// Read user input:
 			String remotePeer;
 			int remotePort;
 			int localPort;
 			
 			
 	        System.out.println("Type in hostname of remote peer: ");
 	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 	        String input = br.readLine();
 	        
 	        if (!input.equals("")){
 	        	remotePeer = input;	
 	        }else{
 	        	remotePeer = "localhost";
 	        }
 	        System.out.println("Type in remote port: ");
 	        input = br.readLine();
 	        if (!input.equals("")){
 	        	remotePort = Integer.parseInt(input);	
 	        }else{
 	        	remotePort = 1099;
 	        }
 	        System.out.println("Type in local port: ");
 	        input = br.readLine();
 	        if (!input.equals("")){
 
 		        localPort = Integer.parseInt(input);	
 	        }else{
 	        	localPort = 1099;
 	        }
 			
 			
 			// ------------------------------------ Starting RMI server. Portnumber is arg[0] -----------------------
 			peer = new PeerImpl();
 			peer.peerID = UUID.randomUUID();
 			Peer stub = (Peer)UnicastRemoteObject.exportObject((Peer)peer, 0);
 			Registry registry = LocateRegistry.createRegistry(localPort);
 			registry.rebind("Peer", stub);
 			peer.addPeer(peer.peerID, (Peer)peer);
 			
 			//-------------------------------------- Connecting to initPeer. Address is args[1], port is args[2] --------------------------------
 			if (!remotePeer.equals("none")){
 				Registry remoteRegistry = LocateRegistry.getRegistry(remotePeer, remotePort);
 	    		Peer initPeer  = (Peer) remoteRegistry.lookup("Peer");
 	    		peer.updatePeerMap(initPeer.getPeerMap());
 	    		Message msg = new EntryMessage(peer.peerID, peer);
 	    		msg.broadcast(peer, false);
 			}else{
 			
 			}
 			
 			GetRemoteQueue getRemoteQueue = peer.new GetRemoteQueue();
 			getRemoteQueue.start();
 			
 			
 			MessageInProxy messageProxy = peer.new MessageInProxy();
 			messageProxy.start();
 			
 			MessageOutProxy messageOutProxy = peer.new MessageOutProxy();
 			messageOutProxy.start();
 
 			Executor executor = new Executor(peer);
 			executor.start();
 			
 			WorkStealer workStealer = peer.new WorkStealer();
 			workStealer.start();
 			
 			UI ui = peer.new UI();
 			ui.run();
 		} catch (RemoteException e) {
 			System.out.println("ERROR: Peer.main()\n");
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			System.out.println("ERROR: Peer.main()\n");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// ----------------------------------------Message system-----------------------------------
  	public void message(Message msg) throws RemoteException{
 		messagesIn.add(msg);
 	}
  	
  	public void messageQ(Message msg){
 		messagesIn.add(msg);
 	}
 	
  		
 	public static int log2(int n){
 	    if(n <= 0) throw new IllegalArgumentException();
 	    return 31 - Integer.numberOfLeadingZeros(n);
 	}
 	
 	public void hasDisconnected(UUID disconnectedPeer){
 		Message msg = new DisconnectedMessage(disconnectedPeer);
 		msg.broadcast(this, false);
 		msg.action(this);
 	}
 	
 	
 	// ------------------------------ Console User Interface---------------------------------
 	
 	public class UI extends Thread{
 		public UI(){}
 		public void run(){
 			while(true){
 				
 				System.out.println("Options: id, exit, terminate, size, hello, mandelbrot, random, readyQ, waitMap");
 				try {
 					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			        String input = br.readLine();
 					if (input.equals("exit")){
 						System.out.println("The size of readyQ is : " +  readyQ.size());
 						System.out.println("The size of waitMap is : " +  waitMap.size());
 						System.exit(0);
 					}
 					if (input.equals("id")){
 						System.out.println("This peer has ID: " + peerID);
 					}
 					if (input.equals("size")){
 						System.out.println("Number of nodes is: " +  peerMap.size());
 					}
 					if (input.equals("keys")){
 						System.out.println("The size of keys is: " +  keys.size());
 					}
 					if (input.equals("terminate")){
 						Message msg = new TerminateMessage();
 						msg.broadcast(peer, false);
 						System.exit(0);
 					}
 					if (input.equals("hello")){
 						Message msg = new HelloMessage();
 						msg.broadcast(peer, true);
 					}
 					if (input.equals("mandelbrot")){
 						MandelbrotClient client = new MandelbrotClient(peer, "mandelbrot");
 						client.start();
 					}
 					
 					if (input.equals("tsp")){
 						TspCli client = new TspCli(peer);
 						client.start();
 					}
 					
 					if (input.equals("fib")){
 						FibClient client = new FibClient(peer);
 						client.start();
 					}
 					
 					if (input.equals("readyQ")){
 							System.out.println("The size of readyQ is : " +  readyQ.size());
 					}
 					if (input.equals("waitMap")){
 						System.out.println("The size of waitMap is : " +  waitMap.size());
 					}
 					if (input.equals("executed")){
 						System.out.println("The number of tasks executed is : " +  taskExcecuted);
 					}
 					if (input.equals("compose")){
 						System.out.println("The number of compose tasks created is : " +  composeTasksCreated);
 					}
 					if(input.equals("arg")){
 						System.out.println(argumentThrownAway);
 					}
 				} catch (IOException e) {
 					System.out.println("ERROR: UI.run()");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	// ---------------------------------------- Task computation stuff ----------------------------
 	public void putTask(Task t){
 		// Notify all that a new task has arrived. Spesify an ID
 		// PUT TASK TO QUEUE
 		// Start a handler that steals/gives tasks
 		try {
 			t.returnID = "0";
 			t.ID = "0";
 			t.creator = peerID;
 			readyQ.putFirst(t);
 			System.out.println("New task registered");
 			
 		} catch (InterruptedException e) {
 			System.out.println("Failed to put first task to readyQ");
 			e.printStackTrace();
 		}
 	}
 	
 	public void giveTask(Task t){
 			putReadyQ(t);
 	}
 	
 	
 	public Task takeTask(){
 		Task temp = null;
 		try {
 			temp = readyQ.takeFirst();
 		} catch (InterruptedException e) {
 			System.out.println("Failed to take task from readyQ");
 			e.printStackTrace();
 		}
 		try {
 			if (remoteQ != null){
 				if (peerMap.containsKey(remoteQueueHost)){
 					remoteQ.removeTask(temp.ID);
 				}
 			}
 		} catch (RemoteException e) {
 			peer.hasDisconnected(peer.remoteQueueHost);
 			e.printStackTrace();
 		}
 		
 		return temp;
 	};
 	
 	public void putWaitMap(Task task){
 		if(remoteQ != null){
 			if (peerMap.containsKey(remoteQueueHost)){
 				try {
 					remoteQ.putWaitTask(task.clone());
 				} catch (RemoteException e) {
 					hasDisconnected(remoteQueueHost);
 					System.out.println("ERROR: Could not send waitTask to remoteQ");
 				}
 			}
 		}
 		waitMap.put(task.ID, task);
 	}
 	
 	public void putReadyQ(Task t){
 		if (remoteQ != null){
 			if (peerMap.containsKey(remoteQueueHost)){
 				try {
 					remoteQ.putTask(t.clone());
 				} catch (RemoteException e) {
 					hasDisconnected(remoteQueueHost);
 					System.out.println("ERROR: Could not send task to remoteQ");
 				}
 			}
 		}
 		try {
 			readyQ.put(t);
 		} catch (InterruptedException e) {
 			System.out.println("ERROR: putRreadyQ");
 			e.printStackTrace();
 		}
 	}
 	
 	public synchronized void addPeer(UUID id, Peer peer){
 		if (!peerMap.containsKey(id)){
 			keys.add(id);
 			peerMap.put(id, peer);
 		}
 	}
 	public UUID randomPeer(){
 		Random rnd = new Random();
 		return keys.get(rnd.nextInt(keys.size()));
 	}
 	
 	public synchronized void removePeer(UUID id){
 		if (peerMap.containsKey(id)){
 			keys.remove(id);
 			peerMap.remove(id);
 		}
 	}
 	
 	public synchronized void clearPeerMap(){
 		keys.clear();
 		peerMap.clear();
 	}
 	
 	public Map<UUID, Peer> getPeerMap() throws RemoteException{
 		return peerMap;
 	}
 	
 	public synchronized void updatePeerMap(Map<UUID, Peer> peerMap){
 		for (Map.Entry<UUID, Peer> entry : peerMap.entrySet()){
 			if (!keys.contains(entry.getKey())){
 					keys.add(entry.getKey());
 			}
 		}
 		this.peerMap.putAll(peerMap);
 	}
 	
 	public boolean placeArgument(UUID receiver, String ID, Object returnValue, int returnArgumentNumber){
 		Message msg = new SendArgumentMessage(ID, returnValue, returnArgumentNumber);
 		if (peerMap.containsKey(receiver)){
 			return msg.send(peer, receiver);
 		}else if (translations.containsKey(receiver)){ // recursive call
 			return placeArgument(translations.get(receiver), ID, returnValue, returnArgumentNumber);
 		}else{
 			return false;
 		}
 	}
 	
 	public void putResult(Object resultObject){
 		try {
 			result.put(resultObject);
 		} catch (InterruptedException e) {
 			System.out.println("ERROR: putResult");
 			e.printStackTrace();
 		}
 	}
 	
 	public Object getResult(){
 		try {
 			return result.take();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 	
 	public class MessageInProxy extends Thread{
 		public MessageInProxy(){}
 		
 		public void run(){
 			while(true){
 				Message msg;
 				try {
 					msg = messagesIn.take();
 					msg.action(peer);
 				} catch (InterruptedException e) {
 					System.out.println("ERROR: MessageProxy()");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	public class MessageOutProxy extends Thread{
 		public MessageOutProxy(){}
 		
 		public void run(){
 			while(true){
 				Message msg;
 				try {
 					msg = messagesOut.take();
 					argumentThrownAway++;
 					
 				} catch (InterruptedException e) {
 					System.out.println("ERROR: MessageProxy()");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	
 	public class WorkStealer extends Thread{
 		public WorkStealer(){}
 		
 		public void run(){
 			while (true){
 				if (peer.readyQ.size() < 4){
 					Message msg = new GetTaskMessage(peer.peerID);
 					UUID temp = peer.randomPeer();
 					if (temp!=peer.peerID){
 						msg.send(peer, temp);
 					}
 				}
 				try {
 					sleep(10);
 				} catch (InterruptedException e) {
 					System.out.println("ERROR: workstealer failed to sleep");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public Shared getShared(){
 		if (shared != null){
 			try {
 				return shared.clone(); 
 			} catch (CloneNotSupportedException e) {
 	
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 		return null;
 	}
 	
 	public void setShared(Shared proposedShared){
 		try {
 			if (proposedShared.isNewerThan(shared)){
 				shared = proposedShared.clone();
 				SetSharedVarMessage msg = new SetSharedVarMessage(shared);
 				msg.broadcast(peer, false); 
 			}
 
 		} catch (CloneNotSupportedException e) {
 			System.out.println("proposedShared not clonable");
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 	
 	public void setSharedFromRemote(Shared proposedShared){
 		try {
 			if (proposedShared.isNewerThan(shared)){
 				shared = proposedShared.clone();
 			}
 
 		} catch (CloneNotSupportedException e) {
 			System.out.println("proposedShared not clonable");
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	public class GetRemoteQueue extends Thread{
 		public GetRemoteQueue(){
 			remoteQ = null;
 			remoteQueueHost = null;
 		}
 		public void run(){
 			while(remoteQ == null){
 				Message msg = new GetRemoteQueueMessage(peerID);
 				msg.broadcast(peer, false);
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					System.out.print("Error sleeping in GetRemoteQueue");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	synchronized public boolean registerQueue(RemoteQueue rq, UUID remoteQueueHost) throws RemoteException {
 		if (remoteQ == null){
 			remoteQ = rq;
 			this.remoteQueueHost = remoteQueueHost;
 			System.out.println("Recieved reference to a remote Queue!");
 			return true;
 		}
 		return false;
 	}
 }
 
 
 
 //--------------------------------------------------------------------------------------------------
 
