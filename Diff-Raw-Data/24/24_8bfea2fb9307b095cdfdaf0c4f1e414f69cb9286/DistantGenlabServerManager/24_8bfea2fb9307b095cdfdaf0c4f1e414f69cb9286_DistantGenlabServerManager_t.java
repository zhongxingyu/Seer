 package genlab.core.exec.client;
 
 import genlab.core.exec.IAlgoExecutionRemotable;
 import genlab.core.exec.server.GenlabComputationServer;
 import genlab.core.exec.server.IGenlabComputationServer;
 import genlab.core.model.exec.IAlgoExecution;
 import genlab.core.usermachineinteraction.ListOfMessages;
 import genlab.core.usermachineinteraction.ListsOfMessages;
 
 import java.rmi.AccessException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 
 public class DistantGenlabServerManager {
 
 
 	public enum ManagerState {
 		DISCONNECTED,
 		CONNECTING,
 		CONNECTED,
 		CONNECTION_PROBLEM,
 		STOPPING,
 		STOPPED;
 	}
 	
 	private ManagerState state = ManagerState.DISCONNECTED;
 	
 	private final String hostname;
 	private final int port;
 	
 	private IGenlabComputationServer server = null;
 	
 	private ListOfMessages messages = ListsOfMessages.getGenlabMessages();
 	
 	private int numberOfThreads = 0;
 	
 	/**
 	 * The set of threads running on this server
 	 */
 	private Set<WorkingRunnerDistanceThread> threads = new HashSet<WorkingRunnerDistanceThread>();
 	
 	public DistantGenlabServerManager(String hostname, int port) {
 
 		this.hostname = hostname;
 		this.port = port;
 		
 	}
 	
 	public IGenlabComputationServer getDistantServer() {
 		return server;
 	}
 
 	public ManagerState getState() {
 		return state;
 	}
 	
 	public void connect() {
 	
 		state = ManagerState.CONNECTING;
 		
 		Registry registry;
 		messages.infoUser("attempting to connect server "+hostname+":"+port, getClass());
 		messages.infoTech("connecting to RMI registry...", getClass());
 		
 		try {
 			registry = LocateRegistry.getRegistry(hostname, port);
 		} catch (RemoteException e) {
 			messages.errorUser("unable to connect to server "+hostname+":"+port+" : "+e.getMessage(), getClass(), e);
 			state = ManagerState.CONNECTION_PROBLEM;
 			return;
 		}
 
 		server = null;
 		
 		// change class loader
 		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); 
 
 		// retrieve the server on the registry
 		try {
 			server = (IGenlabComputationServer) registry.lookup(GenlabComputationServer.BOUNDING_NAME);
 		} catch (AccessException e) {
 			messages.errorUser("unable to connect to server "+hostname+":"+port+": "+e.getMessage(), getClass(), e);
 			state = ManagerState.CONNECTION_PROBLEM;
 			return;
 		} catch (RemoteException e) {
 			messages.errorUser("unable to connect to server "+hostname+":"+port+": "+e.getMessage(), getClass(), e);
 			state = ManagerState.CONNECTION_PROBLEM;
 			return;
 		} catch (NotBoundException e) {
 			messages.errorUser("unable to connect to server "+hostname+":"+port+": "+e.getMessage(), getClass(), e);
 			state = ManagerState.CONNECTION_PROBLEM;
 			return;
 		}  
 	
 		// retrieve informations
 		try {
 			numberOfThreads = server.getNumberTasksAccepted();
 			messages.infoUser("server "+hostname+":"+port+" accepts up to "+numberOfThreads+" parallel tasks", getClass());
 		} catch (RemoteException e) {
 			messages.errorUser("unable to connect to server "+hostname+":"+port+": "+e.getMessage(), getClass(), e);
 			state = ManagerState.CONNECTION_PROBLEM;
 			return;
 		}
 		
 		doStatisticsPing();
 
 		if (state != ManagerState.CONNECTION_PROBLEM) 
 			state = ManagerState.CONNECTED;
 		
 	}
 		
 	public void doStatisticsPing() {
 		
 		final int total = 10;
 		long cumulated = 0;
 		
 		for (int i=0; i<total; i++) {
 			long timeSend = System.currentTimeMillis();
 			try {
 				long timeReceived = server.ping(timeSend);
 				long diff = System.currentTimeMillis() - timeSend;
 				cumulated += diff;
 			} catch (RemoteException e) {
 				messages.errorUser("unable to ping server "+hostname+":"+port+": "+e.getMessage(), getClass(), e);
 				state = ManagerState.CONNECTION_PROBLEM;
 			}
 		}
 		messages.infoTech("delay between us and server "+hostname+":"+port+": "+(cumulated/total)+" ms", getClass());
 
 	}
 
 
 	public void createWorkerThreads(BlockingQueue<IAlgoExecutionRemotable> mainQueue, BlockingQueue<IAlgoExecution> backupQueue) {
 		
 		
 	}
 
 	public void createWorkerThreads() {
 		
 		if (state != ManagerState.CONNECTED)
 			return;
 		
 		Runner runner = ComputationNodes.getSingleton().getDefaultRunner();
 		while (threads.size() < numberOfThreads) {
 			
 			try {
 				messages.infoUser("starting working thread on server "+hostname+":"+port+" n°"+threads.size(), getClass());
 				WorkingRunnerDistanceThread thread =  new WorkingRunnerDistanceThread(
 						hostname+":"+port,
 						"worker_distant_"+threads.size(), 
 						runner.readyToComputeRemotable, 
						runner.readyToComputeWithThreads, 		
						runner.lockerBothQueues,
 						this
 						);
 				threads.add(thread);
 				runner.addRunnerDistant(thread);
 				
 				
 			} catch (RuntimeException e) {
 				messages.errorUser("unable to create a distant worker thread: "+e.getMessage(), getClass(), e);
 				break;
 			}
 		}
 	}
 
 	public void disconnect() {
 		
 		if (state != ManagerState.CONNECTED)
 			return;
 		
 		state = ManagerState.STOPPING;
 		Runner runner = ComputationNodes.getSingleton().getDefaultRunner();
 		messages.infoUser("disconnecting server "+hostname+":"+port+"...", getClass());
 		for (WorkingRunnerDistanceThread thread: threads) {
 			thread.askStop();
 			runner.removeRunnerDistant(thread);
 		}
 		threads.clear();
 		state = ManagerState.STOPPED;
 	}
 	
 	
 }
