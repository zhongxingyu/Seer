 package com.davidjennes.ElectroJam.Server;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceInfo;
 
 import android.util.Log;
 
 public class Server implements Runnable {
 	private final String TAG = "Server";
 	private final String TYPE = "_eljam._tcp.local.";
 	private final int PORT = 7654;
 	
 	private JmDNS m_jmdns;
 	private String m_name, m_description;
 	private ServerSocket m_server;
 	private final Set<ServerWorker> m_workers; 
 	private volatile boolean m_running, m_stop;
 	
 	/**
 	 * Constructor
 	 * @param context The activity's context
 	 */
 	public Server() {
 		super();
 		
 		m_workers = new CopyOnWriteArraySet<ServerWorker>();
 		m_running = false;
 		m_stop = true;
 		m_server = null;
		m_jmdns = null;
 	}
 	
 	/**
 	 * Destructor
 	 */
 	protected void finalize() throws Throwable {
 		try {
 			m_jmdns.close();
 		} finally {
 			super.finalize();
 		}
 	}
 	
 	/**
 	 * Start running the server
 	 * @param name The server's name
 	 * @param description The server's description
 	 */
 	public void start(String name, String description) {
 		if (m_running)
 			return;
 		
 		// store info
 		m_name = name;
 		m_description = description;
 		
 		// progress dialog
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 	
 	/**
 	 * Stop the server if it's running
 	 */
 	public void stop() {
 		if (!m_running)
 			return;
 		
 		// progress dialog
 		m_stop = true;
 		try {
 			m_server.close();
 		} catch (Throwable e) {}
 	}
 	
 	/**
 	 * Server thread.
 	 * Listen for incoming instrument connections and publish service on ZeroConf
 	 */
 	public void run() {
 		m_stop = false;
 		m_running = true;
 		m_server = null;
 
 		// --- startup sequence ---
 		try {
 			if (m_jmdns == null)
				m_jmdns = JmDNS.create();
 			
 			m_server = new ServerSocket(PORT);
 			ServiceInfo info = ServiceInfo.create(TYPE, m_name, PORT, m_description);
 			m_jmdns.registerService(info);
 		} catch (Throwable e) {
 			m_stop = true;
 			Log.e(TAG, "Error while starting server.");
 			e.printStackTrace();
 		}
 
 		// --- main loop ---
 		try {
 			while (!m_stop) {
 				Socket client = m_server.accept();
 				ServerWorker worker = new ServerWorker(this, client);
 				m_workers.add(worker);
 				worker.start();
 			}
 		} catch (Throwable e) {}
 		
 		// --- shutdown sequence ---
 		if (m_jmdns != null)
 			m_jmdns.unregisterAllServices();
 		for (ServerWorker worker : m_workers)
 			worker.shutdown();
 		m_workers.clear();
 		
 		m_running = false;
 	}
 	
 	/**
 	 * Called when a worker thread finished
 	 * @param thread The worker thread
 	 */
 	public void threadFinished(Thread thread) {
 		m_workers.remove(thread);
 	}
 }
