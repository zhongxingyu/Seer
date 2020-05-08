 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.Class;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /** ProcessManager
  * 
  * Main ProcessManager class
  * Creates a command prompt to accept commands
  * Migrates processes
  * @author Tyler Healy (thealy)
  */
 public class ProcessManager {
 	
 	//Boolean to indicate whether this process manager is a master
 	private boolean isMaster = true;
 	
 	//Boolean to indicate if this process manager is running
 	private boolean isRunning = true;
 	
 	//Slave or Master objects
 	private SocketWrapper sck = null;
 	private ServerSocketWrapper server = null;
 	private ProcessRunner pr = null;
 	private LoadManager lm = null;
 	private SlaveListener sl = null;
 	
 	/** migrate() 
 	 * 
 	 * Sends a process from a slave ProcessManager to a master
 	 * @throws IOException
 	 */
 	public void migrate() throws IOException {
 		MigratableProcessWrapper processToMigrate = pr.getLast();
 		sck.getOut().writeObject(processToMigrate.getName());
 		sck.getOut().flush();
         sck.getOut().writeObject(processToMigrate.getProcess());
 	}
 	
 	/** acceptProcess(String command, String args)
 	 * 
 	 * Creates an instance of a process from a string command
 	 * @param command - name of the process
 	 * @param args - array of string arguments
 	 * @return
 	 */
 	public MigratableProcessWrapper acceptProcess(String command, String[] args) {
 		Class<?> processClass = null;
 		Constructor<?> processCtr = null;
 		MigratableProcessWrapper mpw = null;
 		
 		//Determine if there is a class for the command
 		try {
 			processClass = Class.forName(command);
 		} catch (ClassNotFoundException e) {
 			System.out.println(command + " not found");
 			return null;
 		}
 		
 		//Get the constructor of the class
 		try {
 			processCtr = processClass.getConstructor(String[].class);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		
 		//Create a MigratableProcessWrapper with an instance of the class
 		try {
 			Object[] initArgs = new Object[1];
 			initArgs[0] = args;
 			mpw = new MigratableProcessWrapper((MigratableProcess) processCtr.newInstance(initArgs));
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		
 		return mpw;
 	}
 	
 	/** receiveCommands()
 	 * 
 	 * Runs the command prompt
 	 * @throws Exception
 	 */
 	public void receiveCommands() throws Exception {
 		String result = "";
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		server = new ServerSocketWrapper(2015, 10);
 		server.start();
 		pr = new ProcessRunner();
 		pr.start();
 		lm = new LoadManager(server);
 		lm.start();
 		while(isRunning) {
 			System.out.print("==> ");
 			result = br.readLine();
 			parseCommand(result);
 		}
 	}
 	
 	/** parseCommand(String command)
 	 * 
 	 * Parses the given request
 	 * @param command - String input command
 	 */
 	public void parseCommand(String command) {
 		//Split command based on space
 		String[] words = command.split(" ");
 		
 		//First word is the process/command
 		String com = words[0];
 		
 		//Remaining words are process arguments
 		String[] args = new String[words.length - 1];
 		
 		MigratableProcessWrapper mpw = null;
 		
 		for (int i = 1; i < words.length; i++) {
 			args[i-1] = words[i];
 		}
 		
 		if (com.equals("-c") && args.length == 1) {
 			//Connect as slave command
 			if (isMaster) {
 			  connectAssSlave(args[0]);
 			} else {
 				System.out.println("Already connected as slave");
 			}
 		} else if (com.equals("ps") && words.length == 1) {
 			//Print processes command
 			pr.printProcesses();
 		} else if (com.equals("quit") && words.length == 1) {
 			//Quits the ProcessManager
 			quitPM();
 		} else {
 			//Attempts to start a process with the command
 			if((mpw = acceptProcess(com, args)) != null) {
 				mpw.setName(command);
 				addProcess(mpw);
 			}
 		}
 	}
 	
 	/** addProcess(MigratableProcessWrapper newProcess) 
 	 * 
 	 * Adds a new process to the ProcessRunner
 	 * @param newProcess
 	 */
 	public void addProcess(MigratableProcessWrapper newProcess) {
 		pr.addThread(newProcess);
 	}
 	
 	/** connectAssSlave(String hostname)
 	 * 
 	 * Creates a socket connection (in a SocketWrapper) between this
 	 * ProcessManager and a master ProcessManager
 	 * @param hostname
 	 */
 	public void connectAssSlave(String hostname) {
 		String[] hostArray = hostname.split(":");
 		
 		if (hostArray.length != 2) {
 			try {
 				throw new Exception("Invalide Hostname!");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		String host = hostArray[0];
 		int port = Integer.parseInt(hostArray[1]);
 		
 		try {
 			sck = new SocketWrapper(new Socket(host, port));
 		} catch (UnknownHostException e) {
 			System.out.println("Unknown host. Could not connect to " + host + ".");
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		if (sck != null) {
 			isMaster = false;
 			server.stop();
 			lm.stop();
 			sl = new SlaveListener(sck, this);
 			try {
 				sl.start();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/** quitPM()
 	 * 
 	 * Quits the process manager
 	 */
 	public void quitPM() {
 		/*if (sck != null) {
 			try {
 				sck.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		isMaster = false;
 		isRunning = false;
 
 		server.stop();
 		pr.stop();*/
 		System.exit(1);
 	}
 	
 	/** getNumProcesses()
 	 * 
 	 * @return the number of processes running on this ProcessManager
 	 */
 	public int getNumProcesses() {
 		return pr.getSize();
 	}
 }
