 package ag.mackenzie.httpd;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
  
 public class HttpServer extends Thread {
	private int maxThreads = 10;
	private int initialThreads = 2;
	private long threadKeepalive = 60;
 	public int port = 8900;
 	public String webroot = System.getProperty("user.dir") + "/htdocs";
 	public static Logger logger = Logger.getLogger("ag.mackenzie.httpd");	
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(
    				initialThreads, maxThreads,threadKeepalive, 
    				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
 	private ServerSocket serverSocket = null;
 	private boolean running;
 	
 	
 	public HttpServer() {
 		initializeWebroot(webroot);
 		
 	}
 	
	public HttpServer(int port, String webroot, int initialThreads, int maxThreads, int threadKeepalive) {
 		this.port = port;
 		this.webroot = webroot;
		this.initialThreads = initialThreads;
		this.maxThreads = maxThreads;
		this.threadKeepalive = threadKeepalive;
 		initializeWebroot(webroot);
 		
 	}
 	
 	public void run() {
 		serve();
 	}
 	
 	private void initializeWebroot(String webroot) {
 		File wwwdir = new File(webroot);
 		if (!wwwdir.exists()) {
 			if (wwwdir.mkdir()) {
 				logger.info("Created web directory because it didn't exist at " + webroot);
 				try {
 					FileWriter writer = new FileWriter(webroot + "/index.html");
 					BufferedWriter out = new BufferedWriter(writer);
 					out.write("<html><head><title>Hello World</title></head><body><h1>Hello World</h1></body></html>");
 					out.close();
 				} catch (IOException e) {
 					logger.info("Tried and failed to create default web content: " + e.getMessage());
 				}
 			}
 		}
 	}
     public void serve()  {
     	
         try {
             serverSocket = new ServerSocket(this.port);
         } catch (IOException e) {
         	logger.log(Level.SEVERE, "Could not bind to port " + this.port);
         	pool.shutdownNow();
             System.exit(-1);
         }
         logger.info("Now accepting connections on port " + this.port + " and serving files from " + webroot + ".");
  
         running = true;
         
         while (running) {
         	try {
 				pool.execute(new HttpConnectionHandler(serverSocket.accept(), webroot, logger));
 			} catch (IOException e) {
 				logger.severe("Error when accepting client socket: " + e.getMessage());
 			}
         }
  
         
     }
     
     public void stopServer() {
     	running = false;
     }
     
     public static void main(String[] argv) {
     	HttpServer server = new HttpServer();
     	server.start();
     }
 }
