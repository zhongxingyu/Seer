 /**
  * 
  */
 package net.frontlinesms.plugins.httptrigger.httplistener;
 
import net.frontlinesms.Utils;
 import net.frontlinesms.plugins.httptrigger.HttpTriggerEventListener;
 import net.frontlinesms.plugins.httptrigger.HttpTriggerListener;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 import org.apache.log4j.Logger;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 
 /**
  * @author Alex
  *
  */
 public class HttpTriggerServer extends Thread implements HttpTriggerListener {
 //> STATIC CONSTANTS
 
 //> INSTANCE PROPERTIES
 	/** Logging object */
	private final Logger log = Utils.getLogger(this.getClass());
 	/** The listener for HTTP events */
 	private final HttpTriggerEventListener eventListener;
 	/** The port on which we will listen for incoming HTTP connections */
 	private final int port;
 	/** The Jetty server that will listen for HTTP connections. */
 	private final Server server;
 	private final String I18N_STARTING_ON_PORT = "plugins.httptrigger.starting.on.port";
 	private final String I18N_TERMINATED_ON_PORT = "plugins.httptrigger.terminated.on.port";
 	private final String I18N_TERMINATING_ON_PORT = "plugins.httptrigger.terminating.on.port";
 
 //> CONSTRUCTORS
 	/**
 	 * Create a new {@link HttpTriggerServer}.
 	 * @param eventListener value for {@link #eventListener}
 	 * @param port value for {@link #port}
 	 */
 	public HttpTriggerServer(HttpTriggerEventListener eventListener, int port) {
 		// Give this Thread a meaningful name
 		super(HttpTriggerServer.class.getSimpleName() + "; port: " + port);
 		
 		this.port = port;
 		this.eventListener = eventListener;
 		
 		this.server = new Server();
 		Connector connector = new SocketConnector();
 		connector.setPort(port);
 		server.setConnectors(new Connector[]{connector});
 		
 		Handler handler = new SimpleFrontlineSmsHttpHandler(this.eventListener);
 		server.setHandler(handler);
 	}
 
 //> ACCESSORS
 	/** @return {@link #port} */
 	public int getPort() {
 		return this.port;
 	}
 
 //> INSTANCE METHODS
 	/** Start listening. */
 	public void run() {
 		this.eventListener.log(InternationalisationUtils.getI18NString(I18N_STARTING_ON_PORT, String.valueOf(this.getPort())));
 		try {
 			server.start();
 		} catch (Exception ex) {
 			log.warn("Problem starting listener.", ex);
 			try {
 				this.server.stop();
 			} catch (Exception e) {
 				log.info("Problem stopping listener which failed to start.", e);
 			}
 		}
 		try {
 			server.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.eventListener.log(InternationalisationUtils.getI18NString(I18N_TERMINATED_ON_PORT, String.valueOf(this.getPort())));//"Listener terminated on port: " + this.getPort());
 	}
 
 	/** Request the server to stop listening. */
 	public void pleaseStop() {
 		this.eventListener.log(InternationalisationUtils.getI18NString(I18N_TERMINATING_ON_PORT, String.valueOf(this.getPort())));//"Terminating listener on port: " + this.getPort());
 		try {
 			this.server.stop();
 		} catch (Exception ex) {
 			log.warn(ex);
 		}
 	}
 
 //> STATIC FACTORIES
 
 //> STATIC HELPER METHODS
 	/**
 	 * Run the server from the commandline
 	 * @param args 
 	 */
 	public static void main(String[] args) {
 		HttpTriggerServer serv = new HttpTriggerServer(new CommandLineTriggerEventListener(), 1440);
 		serv.start();
 		
 		boolean running = true;
 		while(running) {
 			System.out.println("sleeping...");
 			try {
 				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				running = false;
 			}
 		}
 	}
 }
 
 /**
  * Implementation of {@link HttpTriggerEventListener} which logs all messages to {@link System#out}
  * @author Alex
  */
 class CommandLineTriggerEventListener implements HttpTriggerEventListener {
 	/** @see net.frontlinesms.plugins.httptrigger.HttpTriggerEventListener#log(java.lang.String) */
 	public void log(String message) {
 		System.out.println("LOG: " + message);
 	}
 
 	/** Handle request to send an SMS. */
 	public void sendSms(String toPhoneNumber, String message) {
 		System.out.println("SEND SMS: to=" + toPhoneNumber + "; message=" + message);
 	}
 }
