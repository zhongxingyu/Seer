 package edu.iit.swyne.crawler.server;
 
import java.io.BufferedOutputStream;
import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.Date;
 import java.util.InvalidPropertiesFormatException;
 import java.util.Properties;
 
 import edu.iit.swyne.crawler.SwyneCrawler;
 
 public class SwyneCrawlerServer extends Thread implements CrawlerServer {
 	
 	public static final String NAME = "Swyne Crawler Server";
 	public static final String VERSION = "v0.1";
 	
 	private final static String DEFAULT_PORT = "6970";
 	private static final String DEFAULT_MAX_PENDING_CONNECTIONS = "5";
 	private static final String DEFAULT_MAX_REQUEST_THREADS = "3";
 	private static final String DEFAULT_MIN_REQUEST_THREADS = "1";
 	private static final String DEFAULT_MAX_REQUESTS = "5";
 	
 	private Properties props;
 	private RequestQueue requestQueue;
 	private ServerSocket serverSocket;
 	private boolean running;
 	private SwyneCrawler crawler;
 	
 	public SwyneCrawlerServer() throws Exception {
 		this(new Properties());
 	}
 
 	public SwyneCrawlerServer(Properties properties) throws Exception {
 		Properties defaultProps = new Properties();
 		defaultProps.setProperty("server.port", DEFAULT_PORT);
 		defaultProps.setProperty("server.maxPendingConnections", DEFAULT_MAX_PENDING_CONNECTIONS);
 		defaultProps.setProperty("server.maxThreads", DEFAULT_MAX_REQUEST_THREADS);
 		defaultProps.setProperty("server.minThreads", DEFAULT_MIN_REQUEST_THREADS);
 		defaultProps.setProperty("server.maxRequests", DEFAULT_MAX_REQUESTS);
 		defaultProps.setProperty("server.requestHandler", "edu.iit.swyne.crawler.server.SwyneCrawlerServerThread");
 		defaultProps.setProperty("server.protocol", "edu.iit.swyne.crawler.server.SwyneCrawlerServerProtocol");
 		
 		this.props = new Properties(defaultProps);
 		this.props.putAll(properties);
 		
 		this.crawler = new SwyneCrawler(this.props);
 		
 		int maxQueueSize = Integer.parseInt(this.props.getProperty("server.maxRequests"));
 		int maxThreads = Integer.parseInt(this.props.getProperty("server.maxThreads"));
 		int minThreads = Integer.parseInt(this.props.getProperty("server.minThreads"));
 		this.requestQueue = new RequestQueue(this, this.props.getProperty("server.requestHandler"), maxQueueSize, maxThreads, minThreads);
 	}
 	
 	public SwyneCrawler getCrawler() {
 		return crawler;
 	}
 
 	public synchronized void startServer() {
 		if (!this.running) {
 			int port = Integer.parseInt(this.props.getProperty("server.port"));
 			int backlog = Integer.parseInt(this.props.getProperty("server.maxPendingConnections"));
 			
 			try {
 				this.serverSocket = new ServerSocket(port, backlog);
 				
 				this.start();
 				Thread.sleep(3000);
 			} catch (IOException e) {
 				System.err.println("ERROR: Couldn't create server socket");
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public synchronized void stopServer() {
 		if (this.running) {
 			try {
 				this.running = false;
 				this.serverSocket.close();
 				this.crawler.shutdown();
 			} catch (IOException e) {
 				System.err.println("ERROR: Could not close server socket");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public void run() {
 		// Start running the server
 		System.out.println(NAME + " " + VERSION);
 		System.out.println("Server started...");
 		System.out.println("Listening on port: " + this.props.getProperty("server.port"));
 		this.running = true;
 
 		this.crawler.init();
 		this.crawler.start();
 		
 		while (this.running) {
 			try {
 				Socket s = this.serverSocket.accept();
 				
 				InetAddress addr = s.getInetAddress();
 				System.out.println("Connection from: " + addr.getHostAddress() + " " + addr.getHostName() + " " + new Date().toString());
 				
 				this.requestQueue.add(s);
 			} catch (SocketException e) {
 				// This should be caught when we shutdown the server
 				// Otherwise, this is an error
 				if (this.running) {
 					System.err.println("ERROR: Unexpected socket exception");
 					if (e.getMessage() != null) System.err.println(e.getMessage());
 					e.printStackTrace();
 				}
 			} catch (Exception e) {
 				System.err.println("ERROR : "+e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		
 		System.out.println("Server shuting down...");
 		this.requestQueue.shutdown();
 	}
 
 	public boolean isRunning() {
 		return this.running;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		Properties props = new Properties();
 		
 		// If a file path is specified on the command-line, load that file into properties
 		if (args.length > 0)
 			try {
 				props.loadFromXML(new FileInputStream(args[0]));
				
				String errFile, outFile;
				if ((errFile = props.getProperty("output.err.file")) != null) {
					File err = new File(errFile);
					if (!err.canWrite()) throw new Exception("Cannot write to specified error file");
					System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(err)), true));
				}
				if ((outFile = props.getProperty("output.out.file")) != null) {
					File out = new File(outFile);
					if (!out.canWrite()) throw new Exception("Cannot write to specified output file");
					System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(out)), true));
				}
 			} catch (InvalidPropertiesFormatException e) {
 				System.err.println("ERROR: "+e.getMessage());
 				e.printStackTrace();
 			} catch (FileNotFoundException e) {
 				System.err.println("ERROR: "+e.getMessage());
 				e.printStackTrace();
 			} catch (IOException e) {
 				System.err.println("ERROR: "+e.getMessage());
 				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("ERROR: "+e.getMessage());
				e.printStackTrace();
 			}
 			
 		SwyneCrawlerServer server = new SwyneCrawlerServer(props);
 		server.startServer();
 	}
 
 	public RequestHandler getRequestHandlerInstance() {
 		try {
 			return (RequestHandler) Class.forName(this.props.getProperty("server.requestHandler")).getConstructor(SwyneCrawlerServer.class).newInstance(this);
 		} catch (Exception e) {
 			System.err.println("ERROR: Failed to create server request handler");
 			if (e.getMessage() != null) System.err.println(e.getMessage());
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
