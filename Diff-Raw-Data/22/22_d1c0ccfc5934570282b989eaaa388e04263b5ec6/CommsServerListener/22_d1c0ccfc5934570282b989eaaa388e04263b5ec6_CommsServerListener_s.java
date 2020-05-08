 package ac.hw.services.socialLearning.app.comms;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 
 import ac.hw.services.socialLearning.api.ISocialLearningService;
 
 
 
 public class CommsServerListener implements Runnable {
 
 	private static final Logger log = LoggerFactory.getLogger(CommsServerListener.class);
 
 
 	private ServerSocket serverSocket;
 	private int port;
 	private InetAddress address;
 	private ISocialLearningService socialApp;
 
 	public CommsServerListener(ISocialLearningService socialApp)
 	{
 		init();
 		this.socialApp = socialApp;
 	}
 
 	public void run() {
 		listen();		
 	}
 
 	public void init() {
 		try {
 			log.debug("Starting socket...");
 			this.serverSocket = new ServerSocket(0);
 			this.port = this.serverSocket.getLocalPort();
 			this.address = this.serverSocket.getInetAddress();
 			this.serverSocket.close();
 		} catch (IOException e) {
 			log.debug("Error when trying to get port and address!");
 		}
 	}
 
 	//TODO issue new thread for each acception
 	public void listen() {
 		try {
 			serverSocket = new ServerSocket(port);
 			while(true)
 			{			
 				Socket clientSocket = serverSocket.accept();
 				new Thread(new CommsServerAction(socialApp, clientSocket)).start();
 			}
 
 		} catch (Exception e) {
 			log.debug("Error: " + e.toString());
 		}
 
 	}
 
 	public int getSocket(){
 		return this.port;
 	}
 
 	public String getAddress(){
 		return this.address.toString();
 	}
 
 }
