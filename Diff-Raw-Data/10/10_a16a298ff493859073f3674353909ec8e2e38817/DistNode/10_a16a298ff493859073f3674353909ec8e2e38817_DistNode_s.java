 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 
 public class DistNode extends RIONode {
 	/**
 	 * Failure rate functions that designate probability of an event happening.
 	 * The student may hide these by implementing static methods with the same
 	 * signature in their own node class. 0<=p<=1.
 	 */
 	public static double getFailureRate() { return 0 / 100.0; }
 	public static double getRecoveryRate() { return 100.0 / 100.0; }
 	public static double getDropRate() { return 0.0 / 100.0; }
 	public static double getDelayRate() { return 0.0 / 100.0; }
 	
 	private Map<String, Update> fileList;
 	
 	public boolean isMaster(){
 		return Arrays.binarySearch(PaxosLayer.ACCEPTORS, this.addr) >= 0;
 	}
 	
 	public static boolean isOptimalLeader(int addr){
 		return addr == 0;
 	}
 	/*========================================
 	 * START FILE INTERFACE METHODS
 	 *========================================*/
 	
 	public String get(String fileName) throws IOException{
 		PersistentStorageReader r = this.getReader(fileName);
 		String file = "";
 		String line = r.readLine();
 		while(line != null){
 			file += line + "\n";
 			line = r.readLine();
 		}
 		r.close();
 		return file.length() == 0 ? "" : file.substring(0, file.length() - 1);
 	}
 	
 	
 	public void write(String fileName, String content, boolean append, boolean force) throws IOException{
 		if(fileExists(fileName) || force){
 			if(force && this.addr == TransactionLayer.MASTER_NODE && !fileList.containsKey(fileName) && !fileName.startsWith(".")){
 				fileList.put(fileName, new Update(null, 0, TransactionLayer.MASTER_NODE));
				PersistentStorageWriter w = this.getWriter(".l", true);
				//TODO: check .l file for formatting issues
				w.write(fileName + "\n");
				w.close();
 			}
 			if(!append)
 				putFile(fileName, content, force);
 			else{
 				PersistentStorageWriter w = this.getWriter(fileName, true);
 				w.write(content);
 				w.close();
 			}
 		}else 
 			throw new IOException();
 	}
 	
 	public void create(String fileName) throws IOException {
 		this.getWriter(fileName, false).write("");
 		if(this.addr == TransactionLayer.MASTER_NODE && !fileList.containsKey(fileName)){
 			fileList.put(fileName, new Update(null, 0, TransactionLayer.MASTER_NODE));
			PersistentStorageWriter w = this.getWriter(".l", true);
			w.write(fileName + "\n");
			w.close();
 		}
 	}
 	
 	public void delete(String fileName, boolean quiet) throws IOException {
 		if(fileExists(fileName)){
 			this.getWriter(fileName, false).delete();
 			if(this.addr == TransactionLayer.MASTER_NODE && fileList.containsKey(fileName)){
 				fileList.remove(fileName);
 				this.updateFileList();
 			}
 		}else if(!quiet)
 			throw new IOException();
 	}
 	
 	/*========================================
 	 * END FILE INTERFACE METHODS
 	 *========================================*/
 	
 	/*========================================
 	 * START FILE UTILS
 	 *========================================*/
 	
 	public void updateFileVersion(String fileName, int source, int version){
 		try{
 			this.fileList.put(fileName, new Update("", version, source));
 			this.updateFileList();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private void updateFileList() throws IOException{
 		String contents = "";
 		for(String file : fileList.keySet()){
 			contents += file + " " + fileList.get(file) + "\n";
 		}
 		putFile(".l", contents, true);
 	}
 	/**
 	 * Handles the special case of replacing a file's contents with PUT
 	 * @param fileName The name of the file to overwrite
 	 * @param content The content to replace the file with.
 	 * @throws IOException 
 	 */
 	private void putFile(String fileName, String content, boolean force) throws IOException{
 		PersistentStorageWriter temp = null;
 		boolean exists = fileExists(fileName);
 		if(exists){
 			PersistentStorageReader oldFile = this.getReader(fileName);
 			temp = getWriter(".temp", false);
 		
 			copyFile(oldFile, temp, fileName + "\n");
 			oldFile.close();
 		}
 		
 		PersistentStorageWriter newFile = getWriter(fileName, false);
 		newFile.write(content);
 		newFile.close();
 		if(exists)
 			temp.delete();
 	}
 	
 	/**
 	 * @param fileName The name of the file to check for
 	 * @return true if the file exists, false otherwise
 	 */
 	public boolean fileExists(String fileName){
 		try {
 			this.getReader(fileName).close();
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Copies the file in r to the file in w, appending start to the beginning of the file
 	 * @param r Buffer to read from - source
 	 * @param w Buffer to write to - destination
 	 * @param start String to append to the beginning of the file
 	 * @throws IOException
 	 */
 	private void copyFile(PersistentStorageReader r, PersistentStorageWriter w, String start) throws IOException{
 		String file = start;
 		String line = r.readLine();
 		while(line != null){
 			file += line + "\n";
 			line = r.readLine();
 		}
 		w.write(file);
 	}
 	
 	/*========================================
 	 * END FILE UTILS
 	 *========================================*/
 	
 	
 	/*========================================
 	 * START OUTPUT METHODS
 	 *========================================*/
 	public void printError(Command c, int errCode){
 		System.out.println(buildErrorString(c.getDest(), this.addr, c.getType(), c.getFileName(), errCode));
 	}
 	
 	public void printError(String msg){
 		System.out.println(msg);
 	}
 	
 	/**
 	 * Prints out an error in the following form Node #{addr}: Error: #{protocol} on server #{server} and file #{fileName} returned error code #{code}"
 	 * @param server server node
 	 * @param client client node
 	 * @param protocol protocol used
 	 * @param fileName name of file in command
 	 * @param code error code returned as defined by Error class
 	 */
 	public static String buildErrorString(int server, int client, int protocol, String fileName, int code){
 		return "Node " + client + ": Error: " + Command.toString(protocol) + " on server " + 
 						server + " and file " + fileName + " returned error code " + Error.ERROR_STRINGS[code];
 	}
 	
 	public void printData(String data){
 		System.out.println(data);
 	}
 	
 	public void printSuccess(Command c){
 		System.out.println("Node " + this.addr + ": Success: Command - " + c + " executed succesfully on file: " + c.getFileName() );
 	}
 	
 	/*========================================
 	 * END OUTPUT METHODS
 	 *========================================*/
 	
 	@Override
 	/**
 	 * Starts up the node. Checks for unfinished PUT commands.
 	 */
 	public void start() {
 		this.TXNLayer.start();
 		
 		//SESSION RECOVERY
 		if(fileExists(".sessions")){
 			try{
 				Map<Integer, InChannel> sessions = new HashMap<Integer, InChannel>();
 				PersistentStorageReader reader = this.getReader(".sessions");
 				String session = reader.readLine();
 				int maxSessionID = -1;
 				
 				while(session != null){
 					String[] parts = session.split(" ");
 					Integer addr = Integer.parseInt(parts[0]);
 					Integer sessionID = Integer.parseInt(parts[1]);
 					Integer lastSeqNum = Integer.parseInt(parts[2]);
 					sessions.put(addr, new InChannel(this, addr, sessionID, lastSeqNum));
 					session = reader.readLine();
 					maxSessionID = Math.max(sessionID, maxSessionID);
 				}
 				
 				this.RIOLayer.addConnections(sessions, maxSessionID);
 				
 			}catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 		
 		//PUT RECOVERY
 		if(fileExists(".temp")){
 			try{
 				PersistentStorageReader tempR = this.getReader(".temp");
 				String fileName = tempR.readLine();
 				
 				if (fileName == null)
 					this.getWriter(".temp", false).delete();
 				else{
 					copyFile(tempR, this.getWriter(fileName, false), "");
 					this.getWriter(".temp", false).delete();
 				}
 			}catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 		
 		//TXN ID RECOVERY
 		if(this.fileExists(".txn_id")){
 			try{
 				int txnID = Integer.parseInt(this.getReader(".txn_id").readLine());
 				this.TXNLayer.initializeLastTxnNumber(txnID);
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}else{
 			this.TXNLayer.initializeLastTxnNumber(this.addr - RIONode.NUM_NODES);
 		}
 		
 		//TXN LAYER SEQNUM RECOVERY
 		if(this.fileExists(".txn_seq")){
 			try{
 				Map<Integer, Integer> seqNums = new HashMap<Integer, Integer>();
 				PersistentStorageReader r = this.getReader(".txn_seq");
 				String line = r.readLine();
 				while(line != null){
 					String[] parts = line.split(" ");
 					int addr = Integer.parseInt(parts[0]);
 					int seqNum = Integer.parseInt(parts[1]);
 					seqNums.put(addr, seqNum);
 					line = r.readLine();
 				}
 				
 				this.TXNLayer.initializeTimeoutSeqNums(seqNums);
 				
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		
 		if(this.isMaster()){
 			//CACHE RECOVERY ON MASTER NODE
 			this.fileList = new HashMap<String, Update>();
 			if(this.addr == TransactionLayer.MASTER_NODE){
 				if(!fileExists(".l")){
 					try {
 						this.getWriter(".l", false).write("");
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					
 				} else {
 					try{
 						PersistentStorageReader files = getReader(".l");
 						String line = files.readLine();
 						while(line != null){
 							String[] parts = line.split(" ");
 							String fileName = parts[0];
 							int version = Integer.parseInt(parts[1]);
 							int lastCommitter = Integer.parseInt(parts[2]);
 							if(fileExists(fileName)){
 								fileList.put(fileName, new Update("", version, lastCommitter));
 							}
 							line = files.readLine();
 						}
 						
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				this.TXNLayer.setupCache(fileList);
 			}
 			
 			//TXN LOG RECOVERY
 			if(this.fileExists(".txn_log")){
 				try{
 					Map<Integer, Boolean> log = new HashMap<Integer, Boolean>();
 					PersistentStorageReader reader = this.getReader(".txn_log");
 					String line = reader.readLine();
 					while(line != null){
 						String[] parts = line.split(" ");
 						log.put(Integer.parseInt(parts[0]), parts[1].equals("1"));
 						line = reader.readLine();
 					}
 					
 					this.TXNLayer.initializeLog(log);
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		
 			//WRITE AHEAD LOG RECOVERY - ON COMMITS
 			if(this.fileExists(".wh_log")){
 				try {
 					PersistentStorageReader reader = this.getReader(".wh_log");
 					int txID = Integer.parseInt(reader.readLine());
 					this.TXNLayer.pushUpdatesToDisk(txID, Update.fromString(this.TXNLayer, reader));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	@Override
 	/**
 	 * Handles given command. Prints error if command is not properly formed.
 	 */
 	public void onCommand(String command) {
 		command = command.trim();
 		if(Pattern.matches("^((create|get|delete) [^ ]+|(put|append) [^ ]+ (\\\".+\\\"|[^ ]+))$", command)){
 			int indexOfSpace = command.indexOf(' ');
 			int lastSpace = indexOfSpace + 1;
 			String code = command.substring(0, indexOfSpace).trim();
 			
 			indexOfSpace = command.indexOf(' ', lastSpace);
 			if(indexOfSpace < 0)
 				indexOfSpace = command.length();
 			String fileName = command.substring(lastSpace, indexOfSpace).trim();
 			lastSpace = indexOfSpace + 1;
 			
 			if(code.equals("create")){
 				this.TXNLayer.create(fileName);
 			}else if(code.equals("get")){
 				this.TXNLayer.get(fileName);
 			}else if(code.equals("put")){
 				String content = command.substring(lastSpace);
 				this.TXNLayer.put( fileName, content);
 			}else if(code.equals("append")){
 				String content = command.substring(lastSpace);
 				this.TXNLayer.append(fileName, content);
 			}else if(code.equals("delete")){
 				this.TXNLayer.delete(fileName);
 			}
 		}else if(Pattern.matches("^(txstart|txcommit|txabort)$", command)){
 			if(command.equals("txstart"))
 				this.TXNLayer.txstart();
 			else if(command.equals("txcommit"))
 				this.TXNLayer.commit(true);
 			else
 				this.TXNLayer.abort(true, true);
 		}else{
 			System.out.println("Node: " + this.addr + " Error: Invalid command: " + command);
 			return;
 		}
 	}
 	
 	
 	
 }
 
 
