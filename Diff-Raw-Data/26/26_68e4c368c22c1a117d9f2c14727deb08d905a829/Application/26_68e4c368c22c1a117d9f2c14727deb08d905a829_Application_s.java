 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * @author csak7117
  * 
  */
 public class Application {
 
 	public static final int H = 5;
 	public static final int S = 5;
 	public static final int C = 5;
	public static final int PORT = 12347;
 	public static final int MAX_CAP = 10;
 	public static final String BROADCAST_ADDR = "138.232.94.255";
 	public static final int BROADCAST_PORT = 5200;
 
 	public static final int INSTANCES = 1;
 
	public static final int TIMEOUT_MS = 5000;
 
 	public static final int MAX_BYTES = "255.255.255.255 65535 10000;"
 			.getBytes().length;
 
 	private static ArrayList<ActiveThread> athreads = new ArrayList<ActiveThread>();
 	private static ArrayList<PassiveThread> pthreads = new ArrayList<PassiveThread>();
 	private static ArrayList<DatagramSocket> sockets = new ArrayList<DatagramSocket>();
 
 	public static void main(String[] args) throws IOException,
 			InterruptedException {
 
 		try {
 			for (int i = 0; i < INSTANCES; i++)
				initInstance(PORT + i);
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					System.in));
 			do {
 				System.out.println("To stop the application, type: quit");
 			} while (!in.readLine().equals("quit"));
 
 		} catch (SocketException e) {
 			e.printStackTrace();
 			System.exit(-1);
 		} finally {
 			System.out.println("Quitting application ...");
 			stopInstances();
 			System.out.println("Threads cleaned up");
 		}
 	}
 
 	private static void initInstance(int port) {
 		DatagramSocket socket = null;
 		try {
 			socket = new DatagramSocket(port);
 			// sets socket packet reception timeout
 			socket.setSoTimeout(TIMEOUT_MS);
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 
 		View view = new View();
 		ActiveThread aThread = new ActiveThread(socket, view);
 		PassiveThread pThread = new PassiveThread(socket, view);
 		aThread.start();
 		pThread.start();
 
 		athreads.add(aThread);
 		pthreads.add(pThread);
 
 		sockets.add(socket);
 	}
 
 	private static void stopInstances() throws InterruptedException {
 		// close active threads
 		Iterator<ActiveThread> aitr = athreads.iterator();
 		while (aitr.hasNext()) {
 			ActiveThread aThread = aitr.next();
 			aThread.endThread();
 			aThread.join();
 		}
 
 		// close passive threads
 		Iterator<PassiveThread> pitr = pthreads.iterator();
 		while (pitr.hasNext()) {
 			PassiveThread pThread = pitr.next();
 			pThread.endThread();
 			pThread.join();
 		}
 
 		// close sockets
 		Iterator<DatagramSocket> sitr = sockets.iterator();
 		while (sitr.hasNext()) {
 			sitr.next().close();
 		}
 	}
 }
