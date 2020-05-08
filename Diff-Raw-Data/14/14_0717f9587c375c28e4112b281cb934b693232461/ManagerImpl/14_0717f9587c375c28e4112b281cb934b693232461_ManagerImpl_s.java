 package aic.manager.model;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 import aic.manager.interfaces.IManager;
 import aic.manager.interfaces.IMonitor;
 import aic.manager.interfaces.ITaskProcessor;
 
 public class ManagerImpl implements IManager {
 
 	public static void main(String[] args) throws InterruptedException,
 			IOException {
 		try {
 			String name = "Manager";
 			ManagerImpl manager = new ManagerImpl();
 			IManager stub = (IManager) UnicastRemoteObject.exportObject(
 					manager, 0);
 			Registry registry = null;
 			try {
 				// get existing registry
 				registry = LocateRegistry.getRegistry();
 				registry.rebind(name, stub);
 			} catch (RemoteException e) {
 				// create new registry
 				registry = LocateRegistry
 						.createRegistry(Registry.REGISTRY_PORT);
 				registry.rebind(name, stub);
 			}
 			manager.start();
 			System.out.println("Manager Service ready on port: "
 					+ Registry.REGISTRY_PORT);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private ITaskProcessor processor;
 	private IMonitor monitor;
 
 	public ManagerImpl() {
 		// TODO: Init processor
 //		processor = (ITaskProcessor) new TaskProcessor(new MongoAnalyser(host,
 //				db));
 		monitor = (IMonitor) new Monitor();
 	}
 
 	@Override
 	public void addSentimentAnalysisTask(SentimentTask task)
 			throws RemoteException {
 		processor.addSentimentTask(task);
 	}
 
 	@Override
 	public void subscribe(String serviceUrl) {
 		processor.changeSubscriber(serviceUrl);
 	}
 
 	/*
 	 * Start runnable instances.
 	 */
 	public void start() {
 		// start processor
 		Thread processorThread = new Thread(processor);
 		processorThread.run();
 
 		// start monitor
 		Thread monitorThread = new Thread(monitor);
 		monitorThread.run();
 	}
 }
