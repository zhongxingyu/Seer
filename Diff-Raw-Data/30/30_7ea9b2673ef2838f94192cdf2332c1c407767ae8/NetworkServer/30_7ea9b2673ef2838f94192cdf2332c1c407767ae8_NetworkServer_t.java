 package ms.server.networking;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import ms.server.log.Log;
 
 import org.apache.log4j.Logger;
 
 
 public class NetworkServer implements Runnable {
 
 	private ServerSocket mediastop_ServerSocket;
 	private Logger logger = Log.getLogger();
 	private int THREADCOUNT;
 	
 	public NetworkServer(int port, int thread_count) {
 		try {
 			mediastop_ServerSocket = new ServerSocket(port);
 			THREADCOUNT = thread_count;
 		} catch (IOException e) {
 			logger.fatal("Error: MediaStopf cannot bind to port: " + port);
 			logger.info("Exiting...");
 			System.exit(-1);
 		}
 	}
 
 	public void run() {
 		ExecutorService exec = null;
 
		exec = Executors.newFixedThreadPool(THREADCOUNT);
 
 		while (true) {
 			try {
 				exec.execute(new NetworkServerThread(mediastop_ServerSocket.accept()));
 			} catch (IOException e) {
 				logger.error("Error on accepting connection");
 				e.printStackTrace();
 			}
 		}
 	}
 }
