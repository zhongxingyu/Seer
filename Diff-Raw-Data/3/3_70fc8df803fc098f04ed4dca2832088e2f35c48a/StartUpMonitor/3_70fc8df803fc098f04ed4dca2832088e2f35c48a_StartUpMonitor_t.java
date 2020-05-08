 package aic.monitor.ui;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.openstack.nova.model.Flavor;
 import org.openstack.nova.model.Image;
 import org.openstack.nova.model.Server;
 
 import aic.monitor.LaunchMonitor;
 import aic.monitor.SSHMonitor;
 
 public class StartUpMonitor {
 	/**
 	 * @param args
 	 */
 
 	/**
 	 * Placeholder for later, we can use this variable to externally terminate the run-loop.
 	 */
 	private volatile boolean running = true;
 	private ArrayList<SSHMonitor> sshConnections=new ArrayList<SSHMonitor>();
 	private ArrayList<Server> managedInstances=new ArrayList<Server>();
 	
 	//instances that are newly created and ready to be included into managedInstances
 	final private ArrayList<Server> readyInstances=new ArrayList<Server>();
 	final private LaunchMonitor monitor;
 	private String flavorRef;
 	private String imgRef;
 	//used to generate names for the instances
 	private int imgNumber=2;
 	
 	public StartUpMonitor(Properties properties){
 		monitor = new LaunchMonitor(properties);
 	}
 	
 	//for now it calculates a simple mean value
 	//TODO use a Strategy object, that receives a List<SSHMonitor> as input
 	private double getCloudMetrci() throws IOException{
 		double sum=0;
 		for(SSHMonitor m : sshConnections){
 			sum+=m.getLoadAvg();
 		}
 		return sum / sshConnections.size();
 	}
 	
 	public void stop(){
 		running = false;
 	}
 	
 	/*
 	 * creates a new instance and starts a thread that waits for it to
 	 * be active, it then adds the resulting server to readyInstances
 	 */
 	public void startNewInstance(){
 		if (!(imgRef == null && flavorRef == null)) {
 			// start instance, the instance will be named mX with X being in [2,7]
 			final Server server = monitor.createServer("m" + imgNumber++,
 					flavorRef, imgRef);
 			new Thread(new Runnable(){
 				public void run(){
 					// waiting for ACTIVE state
 					Boolean isActive = false;
 					while (!isActive) {
 						try {
 							Thread.sleep(10000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						isActive = monitor.getServer(server.getId()).getStatus()
 								.equals("ACTIVE");
 					}
 					Server s=monitor.getServer(server.getId());
 					
 					addToShard(s);
 					
 					synchronized(readyInstances){
 						readyInstances.add(s);
 					}
 				}
 			});
 
 		}
 	}
 	
 	/*
 	 * adds the server to the mongodb shard
 	 */
 	public void addToShard(Server s){
 		//TODO we need to add it with mongo serverip:port/dbname --eval "sh.addShard(serverip)"
 	}
 	
 	/*
 	 * removes the server from the mongodb shard
 	 */
 	public void removeFromShard(Server s){
 		//TODO we need to remove it with mongo serverip:port/dbname --eval "something"
 		//TODO we first need to "drain" it with mongo serverip:port/dbname --eval "sh.status()"
 	}
 	
 	/*
 	 * it immediatly removes the top server from managesInstances and starts a thread
 	 * to do the rest since removeFromShard could take some time
 	 */
 	public void terminateInstance(){
 		final Server s=managedInstances.get(managedInstances.size()-1);
 		managedInstances.remove(managedInstances.size()-1);
		SSHMonitor m = sshConnections.get(sshConnections.size()-1);
		sshConnections.remove(sshConnections.size()-1);
		try {m.close();} catch (IOException e) {}
 		
 		new Thread(new Runnable(){
 			public void run(){
 				removeFromShard(s);
 				// terminate instance
 				monitor.terminateServer(s.getId());
 			}
 		});
 	}
 	
 	public void start() throws IOException{
 		// print all flavors
 		for (Flavor flavor : monitor.getFlavors()) {
 			if (flavor.getName().equals("m1.tiny"))
 				flavorRef = flavor.getLinks().get(0).getHref();
 			System.out.println(flavor);
 		}
 
 		// print all images
 		for (Image image : monitor.getImages()) {
 			if (image.getName().equals("Ubuntu 12.10 amd64")) {
 				imgRef = image.getLinks().get(0).getHref();
 			}
 			System.out.println(image);
 		}
 		
 		//keep a list of all managed instances
 		for(Server s : monitor.getServers()){
 			//exclude website
 			if(!s.getInstanceName().equals("website")){
 				//exclude m1 from the managed instances since it has
 				//to always be running
 				if(!s.getInstanceName().equals("m1")){
 					managedInstances.add(s);
 					System.out.println(s);
 				}
 				sshConnections.add(new SSHMonitor("ubuntu",s.getAccessIPv4()));
 			}
 		}
 		
 		imgNumber += managedInstances.size();
 
 		while(running) {
 			// The broker needs to perform a few steps in order to determine the current load
 			// of the cloud and decide whether to start a new instance or stop a running instance
 			// These steps should run in a loop that is active all the time while the broker is running.
 			//1. Get the information of all running instances
 			//   We need to manage a list of all instances (running and stopped), then we poll the active ones
 			//   and retrieve the data from them, using the SshMonitor.
 			synchronized(readyInstances){
 				for(Server s : readyInstances){
 					managedInstances.add(s);
 					sshConnections.add(new SSHMonitor("ubuntu",s.getAccessIPv4()));
 				}
 				readyInstances.clear();
 			}
 			
 			
 			double loadValue = getCloudMetrci();
 			
 			if(loadValue>0.9){
 				//start new instance
 				startNewInstance();
 			}else if(loadValue<0.2){
 				//shut instance down
 				terminateInstance();
 			}
 
 			//2. Evaluate the load based on the data that was returned by the SshMonitor. It will be best to
 			//   calculate some sort of metric from the information so we have a single value to decide if we
 			//   need to start or stop an instance
 			/**
 			 *
 			 */
 
 			//3. We need to pass the value to a strategy class that decides if an instance needs to be started or
 			//   stopped, particularly the strategy class needs to keep a history of the previous states the cloud was
 			//   in, so we can compensate fluctuation (periodic starting/stopping of instances)
 
 
 			//4. When we have decided on a suitable strategy then it needs to be executed and the right instance must be
 			//   stopped or a new one must be started. Maybe we will need to handle the case somehow that no new instance can
 			//   be started.
 			/**
 			 * Strategy strategy;
 			 *
 			 * strategy.execute();
 			 */
 			
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {}
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		Properties properties = new Properties();
 		try {
 			properties.loadFromXML(new FileInputStream("properties.xml"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		StartUpMonitor m = new StartUpMonitor(properties);
 		m.start();
 	}
 }
