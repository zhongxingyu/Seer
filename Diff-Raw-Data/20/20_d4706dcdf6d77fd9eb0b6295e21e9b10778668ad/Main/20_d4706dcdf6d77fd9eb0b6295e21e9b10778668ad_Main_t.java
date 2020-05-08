 package vms;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
import common.Coord;

 import vms.gui.*;
 
 public class Main {
 	final static private String HOSTNAME = "localhost";
 	final static private int PORT = 11233;
 	
 	private static ConnectionServer _Server;
 	private static RadarMonitor _Monitor;
 	private static MainGUI _MainGUI;
 	
 	private static class CSThread extends Thread {
 		@Override
 		public void run() {
 			try {
 				_Server.start();
 				//Server was stopped!
 				_Server.close();
 			} catch (IOException e) {
 				System.out.println(
 					"ConnectionServer threw an exception: " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		// Create ConnectionServer instance
 		_Server = new ConnectionServer();
 		try {
 			_Server.bind(new InetSocketAddress(HOSTNAME, PORT));
 		} catch (IOException e) {
 			System.out.println("Could not bind to " + HOSTNAME + ":" + PORT + " : " + e.getMessage());
 			System.exit(1);
 		}
 		
 		// Create RadarMonitor instance
 		_Monitor = new RadarMonitor();
		_Monitor.setRange(new Coord(-2500.0, -2500.0), new Coord(2500.0, 2500));
 		// Register RadarMonitor as observer of ConnectionServer
 		_Server.registerObserver(_Monitor);
 		
 		// Spin a new thread, and run ConnectionServer::start() in it.
 		Thread t = new CSThread();
 		t.start();
 		
 		// Create Main GUI instance, give it the ConnectionServer
 		_MainGUI = new MainGUI(_Server);
 		
 		// Register Main GUI instance as observer of RadarMonitor
 		_Monitor.registerObserver(_MainGUI);
 		
 		// Start GUI
 		_MainGUI.start();
 		
 		//Main GUI will take care of closing down ConnectionServer
 	}
 
 }
