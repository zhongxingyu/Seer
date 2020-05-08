 package Main;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import org.apache.shiro.session.Session;
 
 /**
  * Provides support for originating work and handing it out as well as receiving
  * completed work
  * 
  * @author Cole Christie
  * 
  */
 public class Server extends Thread {
 	private Logging mylog;
 	private Networking network;
 	private Auth subject;
 	private int PortUsed;
 	private Object JobLock;
 	private JobManagement MasterJobQueue;
 	private Session ServerSession;
 
 	/**
 	 * CONSTRCUTOR
 	 */
 	public Server(Logging passedLog, Auth passedSubject, int PortNumber, Session session) {
 		mylog = passedLog;
 		PortUsed = PortNumber;
 		network = new Networking(mylog, PortNumber);
 		subject = passedSubject;
 		ServerSession = session;
 		// Setup master thread communication
 		JobLock = new Object();
 		MasterJobQueue = new JobManagement();
 	}
 
 	/**
 	 * This displays the CLI menu and advertised commands
 	 */
 	private void DisplayMenu(String Mode) {
 		if (Mode.compareToIgnoreCase("Server") == 0) {
 			System.out.println("======================================================================");
 			System.out.println("Welcome. This server is accepting connections on port [" + PortUsed + "]");
 			System.out.println("Commands are:");
 			System.out.println("QUIT - Closes connection with the server and quits");
 			System.out.println("SW   - Generates a sample set of jobs that can be sent to Windows clients");
 			System.out.println("SL   - Generates a sample set of jobs that can be sent to Linux/UNIX clients");
 			System.out.println("SA   - Generates a sample set of jobs that can be sent to any client");
 			System.out.println("LOAD - Loads a list of pre-defined jobs from a file");
 			System.out.println("CUQ  - Clears our (empties) the queue of unassigned jobs");
 			System.out.println("CAQ  - Clears our (empties) the queue of assigned jobs");
 			System.out.println("LIST - Displays the count of the assigned and unassigned job queues jobs");
 			System.out.println("HELP - Displays this menu");
 			System.out.println("*    - Anything else is just echo'ed back");
 			System.out.println("======================================================================");
 		} else if (Mode.compareToIgnoreCase("DropOff") == 0) {
 			System.out.println("======================================================================");
 			System.out.println("Welcome. This server is accepting connections on port [" + PortUsed + "]");
 			System.out.println("Commands are:");
 			System.out.println("QUIT - Closes connection with the server and quits");
 			System.out.println("SAVE - Flushes the job results cache to a file");
 			System.out.println("LIST - Displays the count of completed jobs in the job results cache ");
 			System.out.println("HELP - Displays this menu");
 			System.out.println("*    - Anything else is just echo'ed back");
 			System.out.println("======================================================================");
 		}
 	}
 
 	/**
 	 * Launches the server and provides the UI
 	 */
 	public void LaunchServer() {
 		// Prepare
 		String UserInput = null;
 
 		// Display the UI boilerplate
 		DisplayMenu("Server");
 
 		// Enter the UI loop
 		UserInput = readUI();
 		while ((UserInput != null) && (UserInput.compareToIgnoreCase("quit") != 0)) {
 			if (UserInput.compareToIgnoreCase("sw") == 0) {
 				// Load a sample set of jobs to WINDOWS clients
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("SW");
 			} else if (UserInput.compareToIgnoreCase("sl") == 0) {
 				// Load a sample set of jobs to LINUX/UNIX clients
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("SL");
 			} else if (UserInput.compareToIgnoreCase("sa") == 0) {
 				// Load a sample set of jobs to any client
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("SA");
 			} else if (UserInput.compareToIgnoreCase("cuq") == 0) {
 				// Clears out the unassigned queue
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("CUQ");
 			} else if (UserInput.compareToIgnoreCase("caq") == 0) {
 				// Clears out the assigned queue
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("CAQ");
 			} else if (UserInput.compareToIgnoreCase("list") == 0) {
 				// Clears out the assigned queue
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("LIST");
 			} else if (UserInput.compareToIgnoreCase("load") == 0) {
 				// Load a set of jobs from a text file located on this system
 				// (in this CWD)
 
 				// Only list/load files from the current working directory
 				String CWD = ".";
 
 				// Generate a list of files
 				String FileName;
 				File folder = new File(CWD);
 				File[] RawResults = folder.listFiles();
 
 				// Filter results
 				ArrayList<File> FitleredResults = new ArrayList<File>();
 				for (int scan = 0; scan < RawResults.length; scan++) {
 					if (RawResults[scan].isFile()) {
 						FileName = RawResults[scan].getName();
 						if (FileName.endsWith(".txt") || FileName.endsWith(".TXT")) {
 							FitleredResults.add(RawResults[scan]);
 						}
 					}
 				}
 
 				// List files that end with a txt file extension
 				int ListSize = FitleredResults.size();
 				if (ListSize > 0) {
 					System.out.println("These files can be loaded. Please make a selection:");
 					int At = 0;
 					while (At < ListSize) {
						System.out.println(FitleredResults.get(At).getName());
 						At++;
 					}
 					System.out.println("===================================================");
 					System.out.println("What file would you like to load? Enter nothing to abort.");
 					UserInput = readUI(); // Prompt for user input
					// If the user input is good, pass that file name to the file parser
					if ((UserInput.length() > 0) && (Integer.parseInt(UserInput) >= 0)
							&& (Integer.parseInt(UserInput) < ListSize)) {
						new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("LOAD",
								FitleredResults.get(Integer.parseInt(UserInput)).getName());
 					} else {
						mylog.out("INFO", "Load aborted");
 					}
 				} else {
 					mylog.out("ERROR", "No files ending in txt exisit in the current working directory. Load aborted");
 				}
 			} else if (UserInput.compareToIgnoreCase("help") == 0) {
 				// Display the UI boilerplate
 				DisplayMenu("Server");
 			} else {
 				// Base case - echo back what was typed in
 				System.out.println("Unknown Command [" + UserInput + "]. Try using 'help'.");
 			}
 			UserInput = readUI(); // Prompt agian for user input
 		}
 		// Code exits upon "quit" which then proceeds to end the code
 	}
 
 	/**
 	 * Launches the server and provides the UI
 	 */
 	public void LaunchDropOff() {
 		// Prepare
 		String UserInput = null;
 
 		// Display the UI boilerplate
 		DisplayMenu("DropOff");
 
 		// Enter the UI loop
 		UserInput = readUI();
 		while ((UserInput != null) && (UserInput.compareToIgnoreCase("quit") != 0)) {
 			if (UserInput.compareToIgnoreCase("sw") == 0) {
 				// Load a sample set of jobs to WINDOWS clients
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("SW");
 			} else if (UserInput.compareToIgnoreCase("list") == 0) {
 				// Displays the list of jobs currently being held in the buffer
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("LISTDO");
 			} else if (UserInput.compareToIgnoreCase("save") == 0) {
 				// Flush the buffer to a file
 				String filename = "";
 				// TODO - Get filename from prompt
 				// TODO - Implement flushing the queue to a file
 				new ServerThread(mylog, JobLock, MasterJobQueue).JobLoader("SAVE", filename);
 			} else if (UserInput.compareToIgnoreCase("help") == 0) {
 				// Display the UI boilerplate
 				DisplayMenu("DropOff");
 			} else {
 				// Base case - echo back what was typed in
 				System.out.println("Unknown Command [" + UserInput + "]. Try using 'help'.");
 			}
 			UserInput = readUI(); // Prompt agian for user input
 		}
 		// Code exits upon "quit" which then proceeds to end the code
 	}
 
 	/**
 	 * Threads the listening agent so it is separate from the Servers UI
 	 */
 	public void run() {
 		// Seed client numeric labeling
 		int UIDcounter = 0;
 
 		// Determine mode to use (Server or Drop Off)
 		String purpose = (String) ServerSession.getAttribute("USE");
 		boolean ServerMode = true;
 		if (purpose == "dropoff") {
 			ServerMode = false;
 		}
 
 		// Listen for new connections indefinitely
 		while (1 > 0) {
 			// Block until a new connection is made
 			Socket socket = network.ListenForNewConnection();
 			UIDcounter++;
 			new ServerThread(subject, mylog, socket, UIDcounter, JobLock, MasterJobQueue, ServerMode).start();
 		}
 	}
 
 	/**
 	 * Reads input provided by the user, returns a string
 	 * 
 	 * @return
 	 */
 	private String readUI() {
 		System.out.flush();
 		System.out.print("> ");
 		System.out.flush();
 		String data = null;
 		BufferedReader inputHandle = new BufferedReader(new InputStreamReader(System.in));
 		boolean wait = true;
 		while (wait) {
 			try {
 				if (inputHandle.ready()) {
 					wait = false;
 				} else {
 					try {
 						Thread.sleep(10);
 					} catch (InterruptedException e) {
 						mylog.out("ERROR", "Failed to sleep");
 					}
 				}
 			} catch (IOException err) {
 				mylog.out("ERROR", "Failed to check if buffered input was ready [" + err + "]");
 			}
 		}
 		try {
 			data = inputHandle.readLine();
 		} catch (IOException err) {
 			mylog.out("ERROR", "Failed to collect user input [" + err + "]");
 		}
 		return data;
 	}
 }
