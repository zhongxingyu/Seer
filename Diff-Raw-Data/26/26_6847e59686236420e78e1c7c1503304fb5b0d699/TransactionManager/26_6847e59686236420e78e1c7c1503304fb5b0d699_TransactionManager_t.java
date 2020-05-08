 package nyu.ads.conctrl.tm;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.*;
 import nyu.ads.conctrl.site.Site;
 import nyu.ads.conctrl.entity.*;
 import nyu.ads.conctrl.tm.entity.*;
 
 /**
  * Transaction Manager class
  * 
  * @author Matt Sarnak
  *
  */
 public class TransactionManager {
 	public TransactionTable transTable;  // List of transactions, statuses, and timestamp of origin
 	
 	public Map<Integer, WaitingQueue> waitingQueueList; 
 	
 	public List<Site> siteList; // list of sites
 	
 	//public List<Resource> varList; // List of variables, and latest timestamp 
 	
 	public Map<String, List<Integer>> varLocations;
 	
 	public TimeStamp currentTimestamp;
 	
 	public List<ArrayList<Integer>> visitorList;
 	
 	//public List<Integer> commitLog; // List of committed transactions
 	
 	public static Boolean DEBUG = true; 
 	
 	public static Boolean BUFFER = true;
 	
 	/**
 	 * Default constructor. Initializes all member variables.
 	 */
 	public TransactionManager() {
 		transTable = new TransactionTable();
 		siteList = new ArrayList<Site>();
 		//varList = new ArrayList<Resource>();
 		waitingQueueList = new HashMap<Integer, WaitingQueue>();
 		varLocations = new HashMap<String, List<Integer>>();
 		visitorList = new ArrayList<ArrayList<Integer>>();
 	}
 	
 	/**
 	 * Entry function to the program.
 	 * @param [the input file of instructions]
 	 */
 	public static void main(String[] args) {
 		TransactionManager transManager = new TransactionManager();
 		
 		Scanner inputScanner = new Scanner(System.in);
 		inputScanner.useDelimiter(System.getProperty("line.separator"));
 		String inputLine = "";
 		
 		transManager.initSites();
 
 		// get line of user input
 		inputLine = inputScanner.next();
 		
 		while(!inputLine.equalsIgnoreCase("exit") && !inputLine.equalsIgnoreCase("exit()")) {
 			// set the new current time stamp
 			transManager.currentTimestamp = new TimeStamp();
 			
 			
 			// parse input into instruction list
 			List<ParsedInstrEnty> instructionList = transManager.parse(inputLine);
 			
 			List<Integer> transactionsProcessed = new ArrayList<Integer>();
 			
 			// execute any fail, recover, dump commands
 			List<ParsedInstrEnty> deleteList = new ArrayList<ParsedInstrEnty>();
 			for (ParsedInstrEnty i : instructionList)
 			{
 				if(i.opcode == OpCode.FAIL || i.opcode == OpCode.RECOVER || i.opcode == OpCode.DUMP)
 				{
 					transManager.process(i);
 					deleteList.add(i);
 				}
 			}
 			
 			for (ParsedInstrEnty d : deleteList) {
 				instructionList.remove(d);			
 			}
 			
 			// check WaitingQueues
 			Iterator it = transManager.waitingQueueList.entrySet().iterator();
 			while(it.hasNext() ){
 				Map.Entry<Integer, WaitingQueue> pairs = (Map.Entry<Integer, WaitingQueue>)it.next();
 				// check to see if the transactions are still blocked
 				boolean workToDo = true;
 				while (pairs.getValue().isBlocked() && workToDo) {
 					workToDo = false;
 					// try to process the first one
 					if (BUFFER)
 						System.out.println("WARNING: Attempting to process buffered command: " + pairs.getValue().viewFirst());
 					int result = transManager.process(pairs.getValue().viewFirst());
 					if(result == 0){
 						if (BUFFER)
 							System.out.println("WARNING: Buffered command processed successfully: " + pairs.getValue().viewFirst());
 						int i = pairs.getValue().viewFirst().transactionId;
 						pairs.getValue().dequeue();
 						transactionsProcessed.add(i);
 						workToDo = true;
 					} else {
 						// still could not process instruction, remain buffered
 					}
 				}		
 			}
 			
 			for (ParsedInstrEnty i : instructionList)
 			{
 				if (transManager.waitingQueueList.containsKey(i.transactionId) && 
 						transManager.waitingQueueList.get(i.transactionId).isBlocked()){
 					transManager.waitingQueueList.get(i.transactionId).enqueue(i);
 					if (BUFFER)
 						System.out.println("WARNING: Transaction already has buffered commands, buffering command: " + i.originalInstruction);
 				}
 				else 
 				{
 					int result = transManager.process(i);
 					// process each instruction sequentially
 					if(result == 0) {
 						// instruction processed correctly
 					}
 					else if(result == 1 && transManager.transTable.containsTransaction(i.transactionId)) {
 						transManager.waitingQueueList.get(i.transactionId).enqueue(i);
 					}
 					else {
 						// bad instruction, ignore it
 					}
 				}
 			}			
 			// get the next line of user input
 			inputLine = inputScanner.next();
 		}  
 	}
 
 	/**
 	 * Initialize the variable lists at the sites. Sites are stored in the file "sites" in the following format
 	 * <site number> <variable name>:<inital value>[:UNIQ] | ...
 	 * UNIQ is appended if the variable is unique, as in the variable only occurs on that site
 	 */
 	public void initSites() {
 		// init sites
 		try {
 			File sitesFile = new File("sites");
 			Scanner sc = new Scanner(sitesFile);
 			sc.useDelimiter(System.getProperty("line.separator"));
 			while(sc.hasNext()){
 				String siteTextFull = sc.next();
 				String[] siteText = siteTextFull.split(" ");
 				int site = new Integer(siteText[0]);
 				
 				Site s = new Site();
 				String[] variables = new String[siteText.length-1];
 				String buffer = "INIT ";
 				for(int i = 1; i < siteText.length; i++){
 					buffer += siteText[i] + " ";
 					variables[i-1] = siteText[i].split(":")[0]; 
 				}
 				s.setBuffer(buffer);
 				s.process();
 				siteList.add(s);
 				
 				addVariableLocations(variables, site);
 				visitorList.add(new ArrayList<Integer>());
 			}			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * Helper method for initSites method. Will add the list of variables
 	 * to the specified site as represented by varLocations
 	 * @param variables
 	 * @param site
 	 */
 	public void addVariableLocations(String[] variables, int site) {
 		for (int i = 0; i < variables.length; i++) {
 			if (varLocations.containsKey(variables[i])) {
 				List<Integer> curr = varLocations.get(variables[i]);
 				curr.add(new Integer(site));
 			}
 			else {
 				List<Integer> curr = new ArrayList<Integer>();
 				curr.add(new Integer(site));
 				varLocations.put(variables[i], curr);
 			}
 		}
 	}
 
 	/**
 	 * Process method that processes the current instruction.
 	 * @param: the instruction
 	 */
 	public int process(ParsedInstrEnty i) {
 		switch(i.opcode) {
 			case BEGIN:
 				return op_begin(i);
 			case BEGINRO:
 				return op_beginRO(i);				
 			case END:
 				return op_end(i);
 			case DUMP:
 				return op_dump(i);
 			case FAIL:
 				return op_fail(i);
 			case RECOVER:
 				return op_recover(i);
 			case W:
 				return op_write(i);
 			case RO:
 			case R:
 				return op_read(i);
			case QUERYSTATE:
				return op_querystate(i);
 			default:
 				return -1;
 		}		
 	}
 	
 	/**
 	 * Method to handle our helper function trans(), which will display the current status of 
 	 * the transaction table. Can also pass a number to check a specific transaction. 
 	 * @param i
 	 * @return
 	 */
	public int op_querystate(ParsedInstrEnty i) {
 		if(i.transactionId == -1) {
 			for (Transaction t : transTable.TransactionList) {
 				System.out.print("T" + t.transID + ": ");
 				if(t.status == 0) {
 					System.out.println("COMMITTED");
 				} else if (t.status == 1) {
 					System.out.println("RUNNING");
 				} else {
 					System.out.println("ABORTED (" + transTable.getReason(t.transID) + ")");
 				}
 			}
 		} else {
 			if (!transTable.containsTransaction(i.transactionId)) {
 				System.out.println("ERROR: No such transaction: " + i.transactionId);
 				return -1;
 			}
 			
 			System.out.print("T" + i.transactionId + ": ");			
 			if(transTable.getStatus(i.transactionId) == 0) {
 				System.out.println("COMMITTED");
 			} else if (transTable.getStatus(i.transactionId) == 1) {
 				System.out.println("RUNNING");
 			} else {
 				System.out.println("ABORTED");
 			}
 		}
 		
 		return 0;
 	}
 
 	/**
 	 * Method to handle the read and readRO command
 	 * Will find a site that is currently up that holds the requested variable.
 	 * If that variable has a recover lock on it at a site, try the next site on the list.
 	 * Print out the read value of the requested variable.
 	 * If the variable has already been locked, perform the wait-die procedure
 	 * @param i
 	 * @return
 	 */
 	public int op_read(ParsedInstrEnty i) {
 		// send message to applicable sites
 		// recieve receipt
 		if(!transTable.containsTransaction(i.transactionId)){
 			System.out.println("ERROR: Do not have a record for transaction: " + i.transactionId);
 			return -1;
 		}
 		int site = -1;
 		if(varLocations.containsKey(i.resource)) {
 			// TODO: check sitelist to make sure site  is up
 			List<Integer> siteIndexList = varLocations.get(i.resource);
 			for (Integer siteIndex : siteIndexList)
 			{
 				if(siteList.get(siteIndex-1).getStatus() == 1) {
 					site = siteIndex;
 					String command = "INSTR " + i.transactionId + " " + transTable.getTimestamp(i.transactionId).getTime() +
 					" " + i.opcode + " " + i.resource + (i.value!=null ? " " + i.value : "");
 					String response = sendToSite(site, command);				
 									
 					// parse response
 					String resp[] = response.split(" ");
 					String result = resp[1];
 					
 					if (result.equals("-1")) {
 						// site has failed 
 						System.out.println("Sent command to failed site. Shouldn't ever get here.");
 						System.out.println("Command: " + i.originalInstruction);
 						System.exit(-1);
 					}
 					else if (result.equals("0"))
 					{
 						// could not acquire lock
 						if(resp.length == 2) {
 							System.out.println("ERROR: Could not read, there is a recover lock at site " + site + ": " + i.originalInstruction);
 							continue;
 						}
 						else {
 						
 							// EXE_RESP 0 [{T_NAME_HOLDER,T_NAME_HOLDER...}] [T_NAME_REQ] [V_NAME:V_VALUE]
 							// perform wait-die protocol	
 							String holdersFull = resp[2].substring(1, resp[2].length()-1);
 							String holders[] = holdersFull.split(",");
 							
 							int[] holderID = new int[holders.length];
 							
 							int j = 0;
 							for(String h : holders) {
 								holderID[j] = new Integer(h);
 								j++;
 							}
 							
 							int reqID = new Integer(resp[3]);
 							TimeStamp oldestHolderTimestamp = transTable.getTimestamp(holderID[0]);
 							int oldestHolderID = holderID[0];
 							for(int ho : holderID) {
 								TimeStamp holderTimestamp = transTable.getTimestamp(ho);
 								if (holderTimestamp.before(oldestHolderTimestamp)) {
 									oldestHolderTimestamp = holderTimestamp;
 									oldestHolderID = ho;
 								}
 							}
 							TimeStamp reqTimestamp = transTable.getTimestamp(reqID);
 							
 							if (oldestHolderTimestamp.before(reqTimestamp)) {
 								op_abort(reqID, "Conflict with Transaction T" + oldestHolderID);
 								clearVisitorsByTransId(reqID);
 								return 0;
 							} else {
 								// Buffer request
 								System.out.println("WARNING: Transaction is requesting lock but it is held by a younger transaction.");
 								if (BUFFER)
 									System.out.println("WARNING: Buffering command: " + i.originalInstruction);
 								return 1;						
 							}					
 						}
 					}
 					else {
 						// successful read operation
 						System.out.println("READ = " + resp[1]);
 						if(!visitorList.get(site-1).contains(i.transactionId))
 							visitorList.get(site-1).add(i.transactionId);
 					}
 					
 					return 0;
 				}
 			}
 			if (site == -1) {
 				// all sites with the variable are failed
 				System.out.println("WARNING: All sites are failed for resource: " + i.resource);
 				if (BUFFER)
 					System.out.println("WARNING: Buffering command: " + i.originalInstruction);
 				return 1;
 			}
 			else {
 				System.out.println("WARNING: All sites have recover lock for resource: " + i.resource);
 				if (BUFFER)
 					System.out.println("WARNING: Buffering command: " + i.originalInstruction);
 				return 1;
 			}
 		}
 		else {
 			System.out.println("ERROR: No site holds the resource: " + i.resource);
 			return -1;
 		}
 	}
 
 	/**
 	 * Method to abort transactions
 	 * Will abort the transaction, and log the reason in the Transaction table why the transaction was aborted.
 	 * Send the abort command for the transaction to all sites.
 	 * @param reqID
 	 * @param reason
 	 */
 	public void op_abort(int reqID, String reason) {
 		// abort req
 		System.out.println("TRANSACTION T"+reqID+" ABORTS " + " (" + reason + ")");
 		sendAllSites("ABORT " + reqID);
 		transTable.setStatus(reqID, -1);
 		transTable.setFailReason(reqID, reason);
 	}
 	
 	/**
 	 * Method to handle a write instruction
 	 * Make sure at least one site that has the variable is available.
 	 * Send the write command to all available sites.
 	 * If the variable is locked, perform the wait-die procedure.
 	 * @param i
 	 * @return
 	 */
 	public int op_write(ParsedInstrEnty i) {
 		if(!transTable.containsTransaction(i.transactionId)){
 			System.out.println("ERROR: Do not have a record for transaction: " + i.transactionId);
 			return -1;
 		}
 		boolean canWrite = false;
 		if(varLocations.containsKey(i.resource)) {
 			// TODO: check sitelist to make sure a site is up
 			List<Integer> siteIndexList = varLocations.get(i.resource);
 			for (Integer siteIndex : siteIndexList)
 			{
 				if(siteList.get(siteIndex-1).getStatus() == 1) {
 					canWrite = true;
 					break;
 				}
 			}
 			if (!canWrite) {
 				// all sites with the variable are failed
 				System.out.println("WARNING: All sites are failed for resource: " + i.resource);
 				if (BUFFER)
 					System.out.println("WARNING: Buffering command: " + i.originalInstruction);
 				return 1;
 			}
 		}
 		else {
 			System.out.println("ERROR: No site holds the resource: " + i.resource);
 			return -1;
 		}
 		
 		String command = "INSTR " + i.transactionId + " " + transTable.getTimestamp(i.transactionId).getTime() +
 			" " + i.opcode + " " + i.resource + (i.value!=null ? " " + i.value : "");
 		List<String> responses = sendAllSites(command);
 		
 		int siteCounter = -1;
 		for (String response : responses)
 		{
 			siteCounter++;
 			// parse response
 			String resp[] = response.split(" ");
 			String result = resp[1];
 			
 			if (result.equals("1")) {  
 				// write successful
 				// add transaction to visitor list for site
 				if(!visitorList.get(siteCounter).contains(i.transactionId))
 						visitorList.get(siteCounter).add(i.transactionId);
 				continue;
 			}
 			else if (result.equals("-2")) {
 				// site does not have the variable
 				continue;
 			}
 			else if (result.equals("-1")) {
 				// site has failed
 				continue;
 			}
 			else if (result.equals("0"))
 			{	
 				// EXE_RESP 0 [{T_NAME_HOLDER,T_NAME_HOLDER...}] [T_NAME_REQ] [V_NAME:V_VALUE]
 				// perform wait-die protocol	
 				String holdersFull = resp[2].substring(1, resp[2].length()-1);
 				String holders[] = holdersFull.split(",");
 				
 				int[] holderID = new int[holders.length];
 				
 				int j = 0;
 				for(String h : holders) {
 					holderID[j] = new Integer(h);
 					j++;
 				}
 				
 				int reqID = new Integer(resp[3]);
 				TimeStamp oldestHolderTimestamp = transTable.getTimestamp(holderID[0]);
 				int oldestHolderID = holderID[0];
 				for(int ho : holderID) {
 					TimeStamp holderTimestamp = transTable.getTimestamp(ho);
 					if (holderTimestamp.before(oldestHolderTimestamp)) {
 						oldestHolderTimestamp = holderTimestamp;
 						oldestHolderID = ho;
 					}
 				}
 				TimeStamp reqTimestamp = transTable.getTimestamp(reqID);
 				
 				if (oldestHolderTimestamp.before(reqTimestamp)) {
 					// abort req
 					op_abort(reqID, "Conflict with Transaction T" + oldestHolderID);
 					clearVisitorsByTransId(reqID);
 					return 0;
 				} else {
 					// Buffer request
 					System.out.println("WARNING: Transaction is requesting lock but it is held by a younger transaction.");
 					if (BUFFER)
 						System.out.println("WARNING: Buffering command: " + i.originalInstruction);
 					return 1;						
 				}
 			}
 		}
 			
 		return 0;
 	}
 
 	/**
 	 * Method to handle the recover command
 	 * Sends the recover command to the site
 	 * @param i
 	 * @return
 	 */
 	public int op_recover(ParsedInstrEnty i) {
 		// send message to applicable site
 		sendToSite(i.site, "RECOVER");
 		return 0;
 	}
 
 	/**
 	 * Method to handle the fail command
 	 * Sends the fail command to the site
 	 * Aborts all transactions that have visited this site
 	 * @param i
 	 * @return
 	 */
 	public int op_fail(ParsedInstrEnty i) {
 		// send message to applicable site
 		sendToSite(i.site, "FAIL");
 		failVisitors(i.site);
 		return 0;
 	}
 
 	/**
 	 * Method to handle the dump command
 	 * Send the dump command to the correct sites
 	 * Prints out the dumped values
 	 * @param i
 	 * @return
 	 */
 	public int op_dump(ParsedInstrEnty i) {
 		// send message to applicable sites
 		String msg = "DUMP";
 		if(i.resource != null) {  msg += " " + i.resource; }
 		if(i.site != -1) {
			String m = sendToSite(i.site, msg);
			m = (m.equals("EXE_RESP -1") ? "FAILED" : m);
			System.out.println("Site " + i.site +": " +m);
 		}
 		else {
 			List<String> result = sendAllSites(msg);
 			int siteI = 1;
 			for(String r : result)
 			{
				System.out.println("Site " + siteI + ": " + (r.equals("EXE_RESP -1") ? "FAILED" : r));
 				siteI++;
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * Method to handle the end command
 	 * Perform two-phase commit.
 	 * Update the transaction table.
 	 * @param i
 	 * @return
 	 */
 	public int op_end(ParsedInstrEnty i) {
 		// Two-phase commit:
 		// send message to all sites, get receipts
 		if(transTable.containsTransaction(i.transactionId) && transTable.getStatus(i.transactionId) == 1){
 			sendAllSites("PREPARE_COMMIT " + i.transactionId);
 			
 			// if all are good to go, send message to commit
 			sendAllSites("COMMIT " + i.transactionId);
 			clearVisitorsByTransId(i.transactionId);
 								
 			// 	update trans table
 			transTable.setStatus(i.transactionId, 0);
 			System.out.println("TRANSACTION T" + i.transactionId + " COMMITS");
 			return 0;
 		} else if (transTable.containsTransaction(i.transactionId)) {
 			if (transTable.getStatus(i.transactionId) == 0) {
 				System.out.println("WARNING: Transaction has already been committed: " + i.transactionId);
 			} else {
 				System.out.println("WARNING: Transaction has already been aborted: " + i.transactionId + " (" + transTable.getReason(i.transactionId) + ")");
 			}
 			return -1;
 		}	else {
 			System.out.println("ERROR: Do not have a record for transaction: " + i.transactionId);
 			return -1;
 		}
 	}
 
 	/**
 	 * Method to handle the beginRO command
 	 * Create a new transaction, and add it to the transaction table
 	 * Send a snapshot command to all sites so that they can perform multi-version read consistency
 	 * @param i
 	 * @return
 	 */
 	public int op_beginRO(ParsedInstrEnty i) {
 		// update trans table
 		Transaction tro = new Transaction();
 		tro.transID = i.transactionId;
 		tro.timestamp = currentTimestamp;
 		tro.status = 1;
 		tro.readOnly = true;
 		
 		if(!transTable.containsTransaction(i.transactionId)) {
 			transTable.addTransaction(tro);				
 			waitingQueueList.put(i.transactionId, new WaitingQueue());
 			sendAllSites("SNAPSHOT " + tro.timestamp.getTime());
 			return 0;
 		} else {
 			System.out.println("ERROR: Already have a transaction: " + i.transactionId);
 			return -1;
 		}
 	}
 
 	/**
 	 * Method to handle the begin command
 	 * Create a new transaction and add it to the transaction table
 	 * @param i
 	 * @return
 	 */
 	public int op_begin(ParsedInstrEnty i) {
 		// update trans table
 		Transaction t = new Transaction();
 		t.transID = i.transactionId;
 		t.timestamp = currentTimestamp;
 		t.status = 1;
 		t.readOnly = false;
 		
 		if(!transTable.containsTransaction(i.transactionId)) {
 			transTable.addTransaction(t);
 			waitingQueueList.put(i.transactionId, new WaitingQueue());
 			return 0;
 		} else {
 			System.out.println("ERROR: Already have a transaction: " + i.transactionId);
 			return -1;
 		}
 	}
 	
 	/**
 	 * Method to fail the visitors of a failed site
 	 * Go through the list of all visiting transactions of the site and abort each one
 	 * @param site
 	 */
 	public void failVisitors(int site) {
 		List<Integer> abortedTransactions = new ArrayList<Integer>();
 		for(Integer i : visitorList.get(site-1)) {
 			op_abort(i, "Site " + site + " failed");
 		}
 		for(Integer a : abortedTransactions){
 			clearVisitorsByTransId(a);
 		}
 	}
 	
 	/**
 	 * Helper function
 	 * After ending a transaction for any reason, clear out that transaction from any 
 	 * site's visitor list 
 	 * @param transID
 	 */
 	public void clearVisitorsByTransId(int transID) {
 		for(ArrayList<Integer> vl : visitorList) {
 			int counter = 0;
 			for (;counter<vl.size(); counter++) {
 				if(vl.get(counter) == transID) {
 					vl.remove(counter);
 					counter=-1;
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Method to send a command to a site
 	 * Takes the command and send it to a specific site, returning the result
 	 * @param site
 	 * @param command
 	 * @return
 	 */
 	public String sendToSite(int site, String command) {
 		
 		siteList.get(site-1).setBuffer(command);
 		
 		String result = siteList.get(site-1).process();
 		
 		if (DEBUG) {
 			System.out.println("DEBUG: Sending site={" + site + "} Command=\"" + command + "\"");			
 			System.out.println("DEBUG: Result=\"" + result + ";\"");
 		}
 		return result;
 	}
 	
 	/**
 	 * Sends all sites the same command, and returns the list of the responses
 	 * @param command
 	 * @return response list
 	 */
 	public List<String> sendAllSites(String command) {
 		List<String> responseList = new ArrayList<String>();
 		
 		String result = "";
 		
 		for(int i = 0; i < siteList.size(); i++) {
 			siteList.get(i).setBuffer(command);
 			responseList.add(siteList.get(i).process());
 			result += responseList.get(i) + "; ";
 		}
 		
 		if (DEBUG) {
 			System.out.println("DEBUG: Sending site={all} Command=\"" + command + "\"");			
 			System.out.println("DEBUG: Result=\"" + result + "\"");
 		}
 		
 		return responseList;
 	}
 	
 
 	/**
 	 * Parse the input string and create an list of instructions
 	 * @param input to be parsed
 	 * @return the list of instructions
 	 */
 	public List<ParsedInstrEnty> parse(String input) {
 		String msgs[] = input.split(";");
 		
 		List<ParsedInstrEnty> instrList = new ArrayList<ParsedInstrEnty>();
 		for(String m : msgs){
 			boolean misformed = false;
 			String[] msg = m.trim().split("\\(|,|\\)");
 			
 			// clean whitespace, make each token upper case
 			for(int i = 0; i < msg.length; i++) {
 				msg[i] = msg[i].trim().toUpperCase();
 			}
 			
 			if(msg.length==0) {
 				continue;
 			} else if (msg.length == 1 && msg[0].isEmpty()) {
 				continue;
 			}
 			
 			try {
 				OpCode op  = OpCode.valueOf(msg[0]);
 				ParsedInstrEnty pie = new ParsedInstrEnty();
 				pie.opcode = op;
 				pie.originalInstruction = m;
 				
 				switch(op) {
 					case BEGIN:
 					case BEGINRO:
 					case END:
 						pie.transactionId = new Integer(msg[1].substring(1));
 						break;
 					case R:
 						pie.transactionId = new Integer(msg[1].substring(1));
 						pie.resource = msg[2];
 						if(transTable.containsTransaction(pie.transactionId) &&
 							transTable.TransactionList.get(pie.transactionId-1).readOnly) {				
 							pie.opcode = OpCode.RO;
 						}
 						break;
 					case W:
 						pie.transactionId = new Integer(msg[1].substring(1));
 						pie.resource = msg[2];
 						if(msg.length==4)
 							pie.value = msg[3];
 						else 
 							misformed = true;
 						break;
 					case FAIL:
 					case RECOVER:
 						pie.site = new Integer(msg[1]);
 						break;
 					case DUMP:
 						if (msg.length == 1) {
 							pie.site = -1;
 							break;
 						}
 						else {
 							if(msg[1].startsWith("X")) {
 								pie.resource = msg[1];
 								pie.site = -1;
 							}
 							else {
 								pie.site = new Integer(msg[1]);
 								pie.resource = (msg.length == 3 ? msg[2] : "");
 							}
 						}
 						break;
					case QUERYSTATE:
 						if (msg.length == 1) {
 							pie.transactionId = -1;							
 						} else {
 							pie.transactionId = new Integer(msg[1]);							
 						}
 						break;
 				}
 				if(!misformed)
 					instrList.add(pie);
 				else
 					System.out.println("ERROR: Misformed command: " + m);
 			} catch (Exception e) {
 				System.out.println("ERROR: Misformed command: " + m);
 			}			
 		}
 		
 		
 		return instrList;
 	}
 }
