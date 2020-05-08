 import java.io.*;
 import java.security.AccessControlException;
 import java.security.AccessController;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.*;
 import java.util.concurrent.atomic.*;
 import java.util.concurrent.locks.*;
 import org.yaml.snakeyaml.*;
 
 /*
  * This class serves as collector of TimeStampedMessages
  * It stores all incoming messages
  * It allow users to check out the orders 
  */
 
 public class Logger {
 	
 	/* fields of configuration */
 	int max_vals = 7; //max number of fields in config file for a rule
 	int max_config_items = 100; //this is an arbitrary number to take place of hard-coded values
 	String[][] conf = new String[max_vals][max_config_items];
 	String[][] send_rules = new String[max_vals][max_config_items]; 
 	String[][] recv_rules = new String[max_vals][max_config_items]; 
 	String[] conf_headers = {"name", "ip", "port"};
 	String[] send_recv_headers = {"action", "src", "dest", "kind", "id", "nth", "everynth"};
 
 	/* fields of the object */
 	private static Logger uniqInstance = null;
 	ReentrantLock globalLock;							 // may be used to synchronize 
 	String config_file;
 	String local_name;
 	Hashtable<String, ConnState> connections; 					// maintain all connection state information
 	Hashtable<String, ArrayList<TimeStampedMessage>> queues;	// maintain all incoming timestamped messages
 	
 	
 	/*
 	 * Constructor: private
 	 * IMPORTANT: make it singleton
 	 */
 	private Logger() {		
 		// Smart part
 		this.globalLock = new ReentrantLock();
 		this.connections = new Hashtable<String, ConnState>();
 		this.queues = new Hashtable<String, ArrayList<TimeStampedMessage>>();
 	}
 	
 	/*
 	 * The way how other can get the singleton instance of this class
 	 */
 	public static synchronized Logger getInstance() {
 		if (uniqInstance == null) {
 			uniqInstance = new Logger();
 		}
 		return uniqInstance;
 	}
 	
 	/*
 	 * Remember to call it after we firstly getInstance() in our application
 	 */
 	public void setConfigAndName(String configuration_filename, String local_name) {
 		this.config_file = configuration_filename;
 		this.local_name = local_name;		
 	}
 	
 	
 	public int getVectorSize()
 	{
 		/* Returns how large a vector clock should be made. */
 		
 		int numNames = 0;
 		String[] names = this.getField("name");
 		for(int i=0; i<names.length; i++)
 			System.out.println("Name "+i+" is "+names[i]);
 		return names.length-1; //because we store the heading names as the first name
 	}
 	
 	
 	public void runServer() {
 		// Init the local server which waits for incoming connection
 		Runnable runnableServer = new LoggerServerThread();
 		Thread threadServer = new Thread(runnableServer);
 		threadServer.start();
 	}
 	
 	
 	public void buildRule(HashMap rule, int ctr, String type)
 	{
 		/* Builds the rule in an easy to use format for user
 		 * examination.
 		 * Return: void */
 		
 		int header = 0;
 		
 		if(type.equals("send"))
 		{
 			for(int i=0; i<send_rules.length; i++)
 				rule.put(send_rules[i][header], send_rules[i][ctr]);			
 			rule.put("rule_num", ctr); //add a field stating which rule number a rule is
 		}
 		else if(type.equals("receive"))
 		{
 			for(int i=0; i<recv_rules.length; i++)
 				rule.put(recv_rules[i][header], recv_rules[i][ctr]);
 			rule.put("rule_num", ctr+send_rules.length); //add a field stating which rule number a rule is
 			//needs to take into account the send rules so as to not overwrite the value of a send rule in the hashmap
 		}
 	}
 	
 	
 	public HashMap matchRules(String type, Message message)
 	{
 		/*Returns the first rule matched so appropriate actions
 		 *can be taken. 
 		 *Return: HashMap 
 		 */
 		
 		int header = 0;
 		//String field_name = "";
 		HashMap rule = new HashMap();
 		
 		String[][] tmp = null;
 		
 		if(type.equals("send"))
 		{
 			tmp = new String[send_rules.length][];
 			for (int i = 0; i < send_rules.length; i++) 
 				tmp[i] = Arrays.copyOf(send_rules[i], send_rules[i].length);
 	    }
 		else if(type.equals("receive"))
 		{
 			tmp = new String[recv_rules.length][];
 			for (int i = 0; i < recv_rules.length; i++) 
 				tmp[i] = Arrays.copyOf(recv_rules[i], recv_rules[i].length);
 	    }
 		
 		for(int i=0; i<tmp.length; i++)
 		{
 			rule = new HashMap();
 			buildRule(rule, i, type); //builds the current rule
 			
 			/* Grab each rule that we build and check its fields against those in the message */
 			for(int j=0; j<send_recv_headers.length; j++)
 			{
 				//iterate through each of the fields and check if the rule value and the message vals match.
 				int ctr;
 				String field_name = send_recv_headers[j]; //the current field we're trying to check
 				int rule_tmp = (Integer)rule.get("rule_num"); //save a copy
 				rule.put("rule_num", "*"); //overwrite it for the check
 				
 				String vals = rule.values().toString().replaceAll("[\\[,\\] ]", "");
 
 				if(vals.matches("^\\**$")) //and all fields were wildcards, including action (thus not a real rule)
 				{					
 					rule = null;
 					break; //it didn't match
 				}
 				else
 					rule.put("rule_num", rule_tmp); //re-add the rule number, as it is a real rule
 				
 				if(field_name.equals("rule_num")) //not for use in here
 					continue;
 				else if(field_name.equals("action")) //we don't act on this here
 					continue;
 				else if(field_name.equals("nth") || field_name.equals("everynth")) //this value set will get checked right before send time
 					continue;
 				else if(field_name.equals("id")) //we have to access this specially
 				{
 						ctr = message.get_id();
 					
 					if(!rule.get(field_name).equals(ctr+"") && !rule.get(field_name).equals("*"))
 					{
 						rule = null;
 						break; //it didn't match
 					}
 				}
 				else
 				{
 					if(!rule.get(field_name).toString().equalsIgnoreCase(message.getVal(field_name, message)) && !rule.get(field_name).equals("*"))
 					{
 						rule = null;
 						break; //it didn't match
 					}
 				}
 			}
 			if(rule != null) //we've successfully matched a rule
 			{
 				int times = 0;
 				int tmp_i= i; //save a copy of i in case we need it again
 				String direction = "dest"; //for the send side rules
 				if(type.equals("receive"))
 				{
 					i = i+send_rules.length; //so we don't erroneously overwrite vals of the send rules
 					direction = "src"; //for the receive side rules
 				}
 				if(this.connections.get(message.getVal(direction, message)).special_rules.containsKey(i)) //it has already been initialized
 						times = this.connections.get(message.getVal(direction, message)).getTimesRuleSeen(i); //get value 
 				this.connections.get(message.getVal(direction, message)).setTimesRuleSeen(i, times+1); //update it OR initialize it
 				i = tmp_i; //reset just in case
 			
 				return rule;
 			}
 		}
 		return rule; //at this point, we can safely return based on the above declarations of rule
 	}
 
 	
 	void initHeaders()
 	{
 		/* Statically defines the headers for each of the arrays. */
 		
 		for(int i=0; i<conf_headers.length; i++)
 			conf[i][0] = conf_headers[i];
 		
 		for(int j=0; j<send_recv_headers.length; j++)
 		{
 			send_rules[j][0] = send_recv_headers[j];
 			recv_rules[j][0] = send_recv_headers[j];
 		}
 	}
 	
 	
 	void parseConfig(String fname) {
 		/*Parses the configuration file and stores all of the sections into
 		 *their own 2D arrays. Any field not present is stored as "*" to 
 		 *denote a wildcard functionality. 
 		 */
 		
 		InputStream yamlInput = null;
 		Yaml yaml = new Yaml();
 		String config = "";
 		
 		try {
 			yamlInput = new FileInputStream(new File(fname));			
 		} catch (FileNotFoundException e) { 
 			//error out if not found
 			System.out.println("File "+fname+" does not exist.\n");
 			System.exit(-1); 
 		}
 		
 		Object data = yaml.load(yamlInput);
 		LinkedHashMap<String,String> file_map = (LinkedHashMap<String,String>) data;
 		
 		String whole = "";
 		String[] elements = new String[10]; //figure out appropriate size for these
 		String[] pairs = new String[10];
 		String file_part = "";	
 		
 		for(int ctr=0; ctr<max_vals; ctr++) //quickly initialize all elements
 		{
 			for(int j=1; j<10; j++) //because the headers are already initialized by initHeaders
 			{
 				conf[ctr][j] = "*";
 				send_rules[ctr][j] = "*";
 				recv_rules[ctr][j] = "*";
 			}
 		}
 		
 		Set set = file_map.entrySet();
 		Iterator i = set.iterator();
 		int j = 0;
 		
 		while(i.hasNext())
 		{
 			Map.Entry me = (Map.Entry)i.next();
 			file_part = me.getKey().toString().toLowerCase();
 			ArrayList<String> inner = new ArrayList<String>();
 			inner.add(me.getValue().toString());
 			whole = inner.toString();
 			whole = whole.replaceAll("[\\[\\]\\{]", "");
 			elements = whole.split("\\},?");
 			
 			for(j=0; j<elements.length; j++) //the number of rules in this section of the config
 			{				
 				pairs = elements[j].split(", "); //the number of elements in a particular rule
 				
 				if(file_part.equals("sendrules"))
 					fillLoop(send_rules, pairs, j+1); //pass in 2D array, all of rule's elements in key-val form, and rule number
 				else if(file_part.equals("receiverules"))
 					fillLoop(recv_rules, pairs, j+1);
 				else if(file_part.equals("configuration")) //handle config third because it happens only once
 					fillLoop(conf, pairs, j+1);
 				else
 				{
 					System.out.println("Error parsing configuration file");
 					break;
 				}
 			}
 		}	
 				
 		try {
 			yamlInput.close();
 		} catch (IOException e) {
 			System.out.println("Could not close configuration file\n");
 		}
 	}
 	
 	
 	void fillLoop(String[][] arr, String[] pairs, int rule_num)
 	{
 		/* Populates all of the configuration file options into a 2D array. */
 		//System.out.println("have pairs of "+pairs.length+" like"+pairs[0].toString());
 		
 		int key=0;
 		int val=1;
 		
 		for(int i=0; i<pairs.length; i++)
 		{
 			String[] choices = pairs[i].split("=");
 			choices[key] = choices[key].trim().toString().toLowerCase();	//grab heading
 
 			for(int j=0; j<arr.length; j++) //loop through 2D array's headers and put the values where they belong
 			{
 				if(arr[j][key].equals(choices[key])) //we found the correct heading
 				{
 					arr[j][rule_num] = choices[val]; //only set the ones with real values; all the rest stay as a wildcard
 					break; //go to next pair instead of continuing through loop needlessly
 				}
 			}
 		}
 	}
 	
 			
 	String[] getField(String field){
 		/* Accessor to return any field desired by the user
 		 * program. 
 		 * */
 		
 		String[] tmp = null;
 		int i, j;
 		
 		for(i=0; i<send_rules.length; i++)
 		{
 			for(j=0; j<send_rules[i].length; j++)
 			{
 				if(send_rules[i][j].equals("*"))
 					break;
 			}
 			if(send_rules[i][0].equalsIgnoreCase(field))
 			{
 				tmp = Arrays.copyOfRange(send_rules[i], 0, j);
 				return tmp;
 			}
 		}
 		
 		for(i=0; i<recv_rules.length; i++)
 		{
 			for(j=0; j<recv_rules[i].length; j++)
 			{
 				if(recv_rules[i][j].equals("*"))
 					break;
 			}
 			if(recv_rules[i][0].equalsIgnoreCase(field))
 			{
 				tmp = Arrays.copyOfRange(recv_rules[i], 0, j);
 				return tmp;
 			}
 		}
 		
 		for(i=0; i<conf.length; i++)
 		{
 			for(j=0; j<conf[i].length; j++)
 			{
 				if(conf[i][j].equals("*"))
 					break;
 			}
 			if(conf[i][0].equalsIgnoreCase(field))
 			{
 				tmp = Arrays.copyOfRange(conf[i], 0, j);
 				return tmp;
 			}
 		}
 		System.out.println("Could not match "+field+" to any field.");
 		return tmp;
 	}
 	
 	
 	public String[][] populateOptions(MessagePasser mp, String user_input, int max_fields, int max_options)
 	{
 		/*Stores all of the possible options for each field of a message from
 		 *the lab information (such as for the message action) and from the 
 		 *configuration file (in the case of names, for example) into a 
 		 *two-dimensional array.
 		 */
 
 		String[][] all_fields = new String[max_fields][max_options]; 
 		int ctr;
 		
 		for(ctr=0; ctr<max_fields; ctr++) //quickly initialize all elements
 		{
 			for(int j=0; j<max_options; j++)
 				all_fields[ctr][j] = "";
 		}
 		
 		for (ctr = 0; ctr<max_fields; ctr++) 
 		{
 			switch(ctr)
 			{
 				case 0: //type of message
 					all_fields[ctr][1] = "send";
 					all_fields[ctr][2] = "receive";
 					break;
 				case 1:
 					String[] names = mp.getField("name");
 					int i;
 					for(i=0; i<names.length; i++)
 					{
 						if(!names[i].equals("*"))
 							all_fields[ctr][i] = names[i];
 					}
 					all_fields[ctr][i] = "*";
 					break;
 				case 2: //what kind of message
 					String[] kind = mp.getField("kind");
 					for(i=0; i<kind.length; i++)
 					{
 						if(!kind[i].equals("*"))
 							all_fields[ctr][i] = kind[i];
 					}
 					all_fields[ctr][i] = "*"; //since it's possible to have a message with only an action specified
 					break;
 				default:
 					break;
 			}
 		}
 		return all_fields;
 	}
 	
 	
 	public boolean validateUserRequests(String user_input, MessagePasser mp, String local_name)
 	{
 		/* Determines whether the user has followed the usage
 		 * guidelines. A send rule can have three elements:
 		 * send <dest> <kind>
 		 * A receive rule can have one element:
 		 * receive
 		 * 
 		 * Return: false (no) or true (yes)
 		 *  */
 		
 		String[] kind = mp.getField("kind");
 		String[] names = mp.getField("name");
 		int ctr = 0;
 		int max_fields = 3;
 		int max_options =  (names.length >= kind.length) ? names.length+1 : kind.length+1; 
 		int dest = 1;
 		String[] user_options = new String[max_options];
 		String[][] all_fields = new String[max_fields][max_options]; /*2D array; outside is all possible fields,
 																	   inner is all possible options*/
 		user_options = user_input.trim().split("\\s");
 		if(user_options.length == 1)
 		{
 			if(user_input.trim().equalsIgnoreCase("receive"))
 				return true;
 			else
 				return false;
 		}
 		else if(user_options.length != max_fields)
 		{	
 			System.out.println("Usage error - must be receive OR send <dest> <kind>");
 			return false;
 		}	
 		
 		if(user_options[dest].equalsIgnoreCase(local_name)) //same src and dest
 		{
 			System.out.println("Error - same src and dest "+local_name+". No loopback functionality offered.");
 			return false;
 		}
 		
 		all_fields = populateOptions(mp, user_input, max_fields, max_options);	
 		
 		for (ctr = 0; ctr<max_fields; ctr++) //verify user entered valid options 
 		{
 			for(int j=1; j<max_options; j++)
 			{
 				if((all_fields[ctr][j].equalsIgnoreCase((user_options[ctr].toString()))))
 					break; //found a match for that field
 				else if(all_fields[ctr][0].equalsIgnoreCase("kind") && all_fields[ctr][j].equals("*")) //because it can be anything really
 				{
 					break;
 				}
 				else
 				{
 					if(j == max_options-1)
 					{
 						if(all_fields[ctr][j].equals(""))
 							return false;
 					}
 					else
 						continue;
 				}
 				return false; //item not in list
 			}
 		}
 		return true;
 	}
 
 	
 	public int validOption(String user_input)
 	{
 		int user_action = -1;
 		
 		if(user_input.length() > 1)
 		{
 			System.out.println("Unrecognized option "+user_input+". Choices are 1, 2, and 3.");
 			return -1;
 		}
 		
 		try {
 			user_action = Integer.parseInt(user_input);
 		} catch(NumberFormatException e) {
 			System.out.println(user_input+" is not an integer.");
 			return -1;
 		}
 		return user_action;
 	}	
 	
 	
 	public boolean isNewestConfig(SFTPConnection svr_conn)
 	{
 		// get the YAML file at first
 		if(!svr_conn.isConnected())
 			svr_conn.connect(CONSTANTS.HOST, CONSTANTS.USER);
 		
     	int global_modification_time = svr_conn.getLastModificationTime(CONSTANTS.CONFIGFILE); // record the time-stamp of YAML file
     	
     	if(global_modification_time != TestSuite.local_modification_time)
     	{
     		svr_conn.downloadFile(CONSTANTS.CONFIGFILE, CONSTANTS.LOCALPATH); // download the YAML file
     		TestSuite.local_modification_time = global_modification_time;
     		clearCounters(); //get rid of the Nth and EveryNth counters upon new config file, as per lab specs.
     		return false;
     	}
     	return true;
 	}
 
 	
 	public void clearCounters()
 	{
 		Set<String> keys = this.connections.keySet();
 		Iterator<String> itr = keys.iterator();
 		
 		// reset all the in/out message counters 
 		if(itr.hasNext()) {
 			String key = itr.next();
 			ConnState conn = this.connections.get(key);
 			conn.in_messsage_counter.set(0);
 			conn.out_message_counter.set(0);
 			conn.special_rules.clear(); //empty all rule mappings when a config file changes 
 		}
 	}
 	
 	/*
 	 * Clear one message queue
 	 */
 	public void clearQueue(String remote_name) {
 		
 		// lock this logger at first
 		this.globalLock.lock();
 		
 		ArrayList<TimeStampedMessage> queue = this.queues.get(remote_name);
 		if(queue == null)
 			return;
 		else
 			queue.clear();
 		
 		// unlock this logger
 		this.globalLock.unlock();
 	}
 
 	public void clearAllQueues() {
 		
 		// lock this logger 
 		this.globalLock.lock();
 		
 		// clear all queues
 		Iterator<String> itr = this.queues.keySet().iterator();
 		while(itr.hasNext())
 			this.queues.get(itr.next()).clear();
 		
 		// unlock this logger
 		this.globalLock.unlock();
 	}
 	
 	public void printConnectsions() {
 		
 		// print all connections
 		System.out.println("#############################  MessagePasser Status  ###############################");
 		String conns = "--> We have connections: \n";
 		Iterator<String> itr = this.connections.keySet().iterator();
 		while(itr.hasNext()) {
 			String next_conn = itr.next();
 			ConnState conn = connections.get(next_conn);
 			conns += "\t" + next_conn + ": in-message-counter = " + conn.in_messsage_counter
 					+ ", out-message-counter = " + conn.out_message_counter + "\n";
 		}
 		System.out.println(conns);
 	}
 	
 	public void print() {
 		
 		// print all connections
 		this.printConnectsions();
 		
 		// lock the Logger for now
 		this.globalLock.lock();
 		
 		// print all queues
 		System.out.println("We have these message queues: \n");
 		Iterator<String> itr = this.queues.keySet().iterator();
 		while(itr.hasNext()) {
 			String remote_name = itr.next();
 			System.out.println("\t" + remote_name + ": ");
 			ArrayList<TimeStampedMessage> queue = this.queues.get(remote_name);
 			for(int i = 0; i < queue.size(); i++) {
				System.out.println("\t\t" + queue.get(i).toString());
 			}
 		}
 		
 		// unlock the Logger's locker
 		this.globalLock.unlock();
 		
 	}
 }
 
 
 /*
  * This class will be created as a new thread, to wait for incoming connections
  */
 class LoggerServerThread implements Runnable {
 	
 	// get the singleton
	private Logger logger = null;
 	
 	/*
 	 * Constructor: just get the singleton
 	 */
 	public LoggerServerThread() {
 		logger = Logger.getInstance();
 	}
 	
 	
 	public void run() {
 		
 		// Get the configuration of local server
 		int i;
 		for(i = 0; i < 10; i++) {
 			if(this.logger.conf[0][i].equals(logger.local_name))
 				break;
 		}
 		
 		// if no such local name, terminate the application
 		// actually useless here, I guess ...
 		if(i == this.logger.max_vals) {
 			System.out.println("No such name: " + logger.local_name);
 			System.exit(0);
 		} 
 		
 		// local name found, setup the local server
 		else 
 			try {
 				
 				// Init the local listening socket
 				System.out.println(this.logger.conf[0][i]);
 				System.out.println(this.logger.conf[1][i]);
 				System.out.println(this.logger.conf[2][i]);
 
 				ServerSocket socket = new ServerSocket(Integer.parseInt(this.logger.conf[2][i]));
 
 				// keep listening on the WELL-KNOWN port
 				while(true) {
 					Socket s = socket.accept();
 					ObjectOutputStream oos_tmp = new ObjectOutputStream(s.getOutputStream());
 					ObjectInputStream ois_tmp = new ObjectInputStream(s.getInputStream());
 	
 					TimeStampedMessage login_msg = (TimeStampedMessage)ois_tmp.readObject();
 					String remote_name = login_msg.src;
 
					// Put the new socket into mmp's connections, and initialize the message queue
 					ConnState conn_state = new ConnState(remote_name, s);					
 					conn_state.setObjectOutputStream(oos_tmp);
 					conn_state.setObjectInputStream(ois_tmp);
 					
 					this.logger.connections.put(remote_name, conn_state);					
					this.logger.queues.put(remote_name, new ArrayList<TimeStampedMessage>());
 					
 					// create and run the LoggerReceiveThread
 					Runnable receiveRunnable = new LoggerReceiveThread(remote_name);
 					Thread LoggerReceiveThread = new Thread(receiveRunnable);
 					LoggerReceiveThread.start();
 									
 				}
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 	}
 }
 
 /*
  * Child thread created by the Server
  * Only keep track of the input stream of a given socket, and deal with the buffer issue
  * It also maintain some connection state information, like the counter of input message number
  */
 class LoggerReceiveThread implements Runnable {
 	
 	String remote_name;
 	Logger logger;
 	
 	/*
 	 * Constructor
 	 */
 	public LoggerReceiveThread(String remote_name) {
 
 		this.remote_name = remote_name;
 		this.logger = Logger.getInstance();
 		
 	}
 	
 	/*
 	 * This method do the real work when this thread is created
 	 * It listens to the input stream of the socket
 	 * The connection will never close until the application terminates
 	 */
 	public void run() {
 		
 		// get the connection state of this socket at first
 		ConnState conn_state = this.logger.connections.get(this.remote_name);
 		ObjectInputStream ois = conn_state.getObjectInputStream();
 		
 		// Infinite loop: listen for input
 		try {
 			
 			try {
 				while(true) {
 						// block here until one message comes in
 						TimeStampedMessage message = (TimeStampedMessage)ois.readObject(); 
 						
 						// put the message to the specific message queue
 						ArrayList<TimeStampedMessage> queue = this.logger.queues.get(message.src);
 						if(queue == null)
 							continue;
 						else
 							queue.add(message);
 				}
 			} finally {
 				
 				conn_state.getObjectInputStream().close();
 				conn_state.getObjectOutputStream().close();
 				conn_state.local_socket.close();
 				this.logger.connections.remove(remote_name);
				this.logger.queues.remove(remote_name);
				
				int i = 0;
				i++;
 			}
 		} catch (Exception e){
 			if(e instanceof EOFException) {
 				System.out.println("Connection to " + remote_name + " is disconnected");
 			}
 			return;
 		}
 	}
 }
