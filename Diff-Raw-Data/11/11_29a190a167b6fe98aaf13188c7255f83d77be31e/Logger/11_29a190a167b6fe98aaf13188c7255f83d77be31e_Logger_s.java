 package ds.lab.messagepasser;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.TreeSet;
 
 import ds.lab.bean.NodeBean;
 import ds.lab.message.TimeStampMessage;
 
 /**
  * Similar to MessagePasser
  * 
  * @author dmei
  * 
  */
 public class Logger {
 	/** multithreading, node management */
 	private int MAX_THREAD;
 	private ServerSocket listenSocket;
 	private HashMap<String, NodeBean> nodeList;// name-user
 	/** message */
 	private ArrayList<TimeStampMessage> messageList;
 	/** other local information */
 	private final static String localName = "logger";
 	private String configFileName;
 	private HashMap<String, String> ipNameMap; // for reverse lookup by ip, <ip,
 												// name>
 	private Config config;
 	/** clock */
 	private ClockService clock;
 	private int clockid;
 	/** log file */
 	private final String logPath = "/Users/dmei/Desktop/log";
 	private File file;
 
 	public Logger(String configurationFile) throws IOException {
 		config = new Config(configurationFile, localName);
 		MAX_THREAD = config.NUM_NODE;
 		Scanner sc = new Scanner(System.in);
 		System.err.println("Enter type of clock that you require for your application");
 		System.out.println("0. Logical \t 1. Vector");
 		String input = sc.nextLine().toLowerCase();
 		int numOfNodes;
 		if (input.equals("0") || input.equals("logical") || input.equals("l")) {
 			clockid = 0;
 			numOfNodes = 0;
 			clock = ClockService.getClock(clockid, localName, numOfNodes, config.NODELIST);
 		} else if (input.equals("1") || input.equals("vector") || input.equals("v")) {
 			clockid = 1;
 			numOfNodes = MAX_THREAD;
 			clock = ClockService.getClock(clockid, localName, numOfNodes, config.NODELIST);
 		}
 
 		nodeList = config.NODELIST;
 		/* build my listening socket */
 		NodeBean me = nodeList.get(localName);
 		if (me == null) {
 			throw new IllegalArgumentException("Config file error: no logger configuration");
 		}
 		this.configFileName = configurationFile;
 		ipNameMap = new HashMap<String, String>();
 		for (NodeBean n : nodeList.values())
 			ipNameMap.put(n.getIp(), n.getName());
 		/* messeage list */
 		messageList = new ArrayList<TimeStampMessage>();// TODO comparator based
 														// on timestamps
 		/* log file, shared by threads */
 		file = new File(logPath);
 		/* listener */
 		listenSocket = new ServerSocket(me.getPort());
 		ListenThread listener = new ListenThread();
 		// TODO for loop MAX_THREAD
 		new Thread(listener).start();
 		UserThread userIf = new UserThread();
 		new Thread(userIf).start();
 	}
 
 	private class ListenThread implements Runnable {
 		@Override
 		public void run() {
 			System.err.println(localName + " Starts");
 			/**
 			 * when a new socket connected, create a new thread to handle the
 			 * request, who's responsible for reading message from the socket
 			 * and add to input queue
 			 */
 			try {
 				while (true) {
 					Socket connection = listenSocket.accept();
 					// connection.setKeepAlive(true);
 					assert connection.isConnected();
 					String remote = connection.getInetAddress().getHostAddress();
 					System.err.println("Listener> " + remote + " has connected you");
 					new LoggerWorkerThread(connection, messageList, file);
 				}
 			} catch (EOFException e) {// someone offline
 				String remote = e.getMessage();
 				System.err.println("Messager> " + remote + " went offline");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Responsible for interating with user: send or receive
 	 * 
 	 * @author dmei
 	 * 
 	 */
 	private class UserThread implements Runnable {
 		@Override
 		public void run() {
 			Scanner sc = null;
 			while (true) {
 				System.err.println("Logger> Press any key to output logs");
 				sc = new Scanner(System.in);
 				sc.nextLine();
 				// print out messages
 				System.out.println("id |\tTS\tmID\t | Src | Dest || Src | kind");
 				int id = 0;
 				for (TimeStampMessage m : messageList) {
 					System.out.println(id + " |\t" + m);
 					id++;
 				}
 				if (clockid == 1) {
 					// print out matrix
 					int size = messageList.size();
 					for (int i = 0; i < size; i++)
 						System.out.print(i + "\t");
 					System.out.println();
 					for (int i = 0; i < size; i++) {
 						for (int j = 0; j < size; j++) {
 							if (i == j) {
 								System.out.print("/\t");
 								continue;
 							}
 							int relation = messageList.get(i).getTimeStamp().compareTo(messageList.get(j).getTimeStamp());
 							switch (relation) {
 							case -1:
 								System.out.print("BEFORE\t");
 								break;
 							case 1:
 								System.out.print("AFTER\t");
 								break;
 							default:
 								System.out.print("CUNCURRENT\t");
 								break;
 							}
 						}
 						System.out.println();
 					}
 				} else {
 					HashMap<String, TreeSet<TimeStampMessage>> eachUser = new HashMap<String, TreeSet<TimeStampMessage>>();
 					for (TimeStampMessage m : messageList) {
 						String src = m.getSrc();
 						if (!eachUser.containsKey(src))
 							eachUser.put(src, new TreeSet<TimeStampMessage>(new Comparator<TimeStampMessage>() {
 
 								@Override
 								public int compare(TimeStampMessage o1, TimeStampMessage o2) {
 									return o1.getTimeStamp().compareTo(o2.getTimeStamp());
 								}
 							}));
 						eachUser.get(src).add(m);
 					}
 					for (String src : eachUser.keySet()) {
 						System.out.print(src+": ");
 						for (TimeStampMessage localM : eachUser.get(src))
 							System.out.print(localM.getTimeStamp()+"->");
 						System.out.println();
 					}
 				}
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// VectorTimeStamp t1 = new VectorTimeStamp();
 		// HashMap<String,AtomicInteger> v1 = new HashMap<String,
 		// AtomicInteger>();
 		// v1.put("1", new AtomicInteger(0));
 		// v1.put("2", new AtomicInteger(0));
 		// v1.put("3", new AtomicInteger(0));
 		// t1.setVector(v1);
 		// VectorTimeStamp t2 = new VectorTimeStamp();
 		// HashMap<String,AtomicInteger> v2 = new HashMap<String,
 		// AtomicInteger>();
 		// v2.put("1", new AtomicInteger(0));
 		// v2.put("2", new AtomicInteger(0));
 		// v2.put("3", new AtomicInteger(1));
 		// t2.setVector(v2);
 		// System.out.println(t1.compareTo(t2));
 		if (args.length < 1) {
 			System.out.println("Usage: configFile");
 			System.exit(0);
 		}
 
 		try {
 			new Logger(args[0]);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
