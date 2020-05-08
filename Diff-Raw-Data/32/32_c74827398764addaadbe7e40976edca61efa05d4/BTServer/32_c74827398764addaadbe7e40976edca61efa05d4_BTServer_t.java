 package de.uulm.presenter.bluetooth;
 
 import java.io.IOException;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 
 import javax.bluetooth.DiscoveryAgent;
 import javax.bluetooth.LocalDevice;
 import javax.microedition.io.Connector;
 import javax.microedition.io.StreamConnection;
 import javax.microedition.io.StreamConnectionNotifier;
 
import de.uulm.presenter.control.Control;
 import de.uulm.presenter.control.Main;
 import de.uulm.presenter.exceptions.ServerAlreadyStartedException;
 import de.uulm.presenter.io.IORemoteImpl;
 import de.uulm.presenter.protocol.MessageProtocol;
 
 
 public class BTServer implements RemoteHCIService, Runnable{
 	
 	private boolean active=true;
 	private StreamConnection con;
 	
 	private ArrayBlockingQueue<Runnable> queue;
 	private ThreadPoolExecutor threadPool; 
 	private boolean alreadyStarted = false;
 
 	private Class handler;
 	public BTServer(Class handler) {
 		this.handler=handler;
 		
 	}
 	
 	public void init() throws IOException, ServerAlreadyStartedException{
 		if (alreadyStarted){
 			throw new ServerAlreadyStartedException();
 		}
 		//queue = new ArrayBlockingQueue<Runnable>(10);
 		//threadPool = new ThreadPoolExecutor(1,1,10,TimeUnit.SECONDS,queue);
 		LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
 		alreadyStarted=true;
 		
 	}
 	
 	public void listen(){
 		
 		Thread listenThread = new Thread(this);
 		listenThread.start();
 	}
 	
 	public void stop(){
 		threadPool.shutdown();
 	}
 
 	@Override
 	public void run() {
 		StreamConnectionNotifier service;
 			
 			try {
 				service = (StreamConnectionNotifier) Connector.open( "btspp://localhost:" + SERVICEUUID + ";name="+NAME );
 				
 				Main.control.stateServerListening();
 				
 				while(active){
 					try {
 						con = (StreamConnection) service.acceptAndOpen();
 						MessageProtocol handler = (MessageProtocol)this.handler.newInstance();
 						handler.init(con);
 						//threadPool.execute(handler); //for multiple connection handles
 						//handler.run();
						
 						handler.run();
 						
 						
						
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (InstantiationException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 					
 				}
 				Main.control.stateServerStopped();
 			} catch (IOException e1) {
				Main.control.bluetoothError();
				return;
 			}
 		
 
 	}
 	
 }
