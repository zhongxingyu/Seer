 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class TransactionLayer {
 
 	public final static int MASTER_NODE = 0;
 	
 	private DistNode n;
 	private ReliableInOrderMsgLayer RIOLayer;
 	private Map<String, File> cache;
 	private int lastTXNnum;
 	private Transaction txn;
 	
 	/**
 	 * key = addr of node uploading its commit
 	 * value = list of commands received so far of the commit
 	 */
 	private Map<Integer, List<Command>> commitQueue;
 	/**
 	 * key = addr of node trying to commit
 	 * value = commit status
 	 */
 	private Map<Integer, Commit> waitingQueue;
 	
 	
 	public TransactionLayer(RIONode n, ReliableInOrderMsgLayer RIOLayer){
 		this.cache = new HashMap<String, File>();
 		this.n = (DistNode)n;
 		this.RIOLayer = RIOLayer;
 		this.lastTXNnum = n.addr;
 		this.commitQueue = this.n.addr == MASTER_NODE ? new HashMap<Integer, List<Command>>() : null;
 		this.waitingQueue = new HashMap<Integer, Commit>();
 	}
 	
 	public void send(int server, int protocol, byte[] payload) {
 		TXNPacket pkt = new TXNPacket(protocol, payload);
 		this.RIOLayer.sendRIO(server, Protocol.TXN, pkt.pack());
 	}
 	
 	public void onReceive(int from, byte[] payload) {
 		TXNPacket packet = TXNPacket.unpack(payload);
 		if( packet.getProtocol() == TXNProtocol.HB)
 			this.sendHB(from);
 		else if(this.n.addr == MASTER_NODE){
 			masterReceive(from, packet);
 		}else
 			slaveReceive(packet);
 	}
 	
 	public void sendHB(int dest){
 		this.RIOLayer.sendRIO(dest, TXNProtocol.HB, new byte[0]);
 	}
 	
 	public void onTimeout(int dest, byte[] payload){
 		TXNPacket pkt = TXNPacket.unpack(payload);
 		if(this.n.addr == MASTER_NODE){
 			if(pkt.getProtocol() == TXNProtocol.HB){
 				for(Integer committer : waitingQueue.keySet()){
 					Commit com = waitingQueue.get(committer);
 					for(Integer dep : com){
 						if(dep == dest){
 							this.send(committer, TXNProtocol.ABORT, new byte[0]);
 							break;
 						}
 					}
 				}
 			}else if(pkt.getProtocol() == TXNProtocol.WF){
 				String fileName = Utility.byteArrayToString(pkt.getPayload());
 				MasterFile f = (MasterFile)this.getFileFromCache(fileName);
 				f.changePermissions(dest, File.INV);
 				if(!f.isWaiting()){
 					Update u = f.chooseProp(f.requestor);
 					this.send(f.requestor, TXNProtocol.WD, Utility.stringToByteArray(fileName + " " + u.version + " " + u.contents));
 					f.requestor = -1;
 				}
 			}
 		}else
 			this.n.printError(DistNode.buildErrorString(dest, this.n.addr, pkt.getProtocol(), Utility.byteArrayToString(pkt.getPayload()), Error.ERR_20));
 	}
 	
 	private void masterReceive(int from, TXNPacket pkt){
 		MasterFile f;
 		String contents, fileName;
 		int i, lastSpace;
 		
 		switch(pkt.getProtocol()){
 			case TXNProtocol.WQ:
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.getState() == File.INV){
 					String payload = fileName + " " + DistNode.buildErrorString(this.n.addr, from, TXNProtocol.WQ, fileName, Error.ERR_10);
 					this.send(from, TXNProtocol.ERROR, Utility.stringToByteArray(payload));
 				}else if(!f.isCheckedOut()){
 					try{
 						contents = this.n.get(fileName);
 						byte[] payload = Utility.stringToByteArray(f.getName() + " " + f.getVersion() + " " + contents);
 						f.addDep(from, new Update(contents, f.getVersion(), MASTER_NODE));
 						this.send(from, TXNProtocol.WD, payload);
 					}catch(IOException e){
 						String payload = fileName + " " + DistNode.buildErrorString(this.n.addr, from, TXNProtocol.WQ, fileName, Error.ERR_10);
 						this.send(from, TXNProtocol.ERROR, Utility.stringToByteArray(payload));
 					}
 				}else{
 					f.requestor = from;
 					try{
 						f.propose(this.n.get(fileName), f.getVersion(), MASTER_NODE);
 					}catch(IOException e){
 						f.propose("", f.getVersion(), MASTER_NODE);
 					}
 					for(Integer client : f){
 						f.changePermissions(client, MasterFile.WF);
 						this.send(client, TXNProtocol.WF, Utility.stringToByteArray(f.getName()));
 					}
 				}
 				break;
 			case TXNProtocol.WD:
 				contents = Utility.byteArrayToString(pkt.getPayload());
 				i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				lastSpace = i + 1;
 				int version = Integer.parseInt(contents.substring(lastSpace, i));
 				contents = contents.substring(i + 1);
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				f.changePermissions(from, MasterFile.FREE);
 				if(f.getVersion() < version){
 					f.propose(contents, version, from);
 				}
 				if(!f.isWaiting()){
 					Update u = f.chooseProp(f.requestor);
 					this.send(f.requestor, TXNProtocol.WD, Utility.stringToByteArray(fileName + " " + u.version + " " + u.contents));
 					f.requestor = -1;
 				}
 				break;
 			case TXNProtocol.ERROR:
 				String[] parts = Utility.byteArrayToString(pkt.getPayload()).split(" ");
 				
 				if(parts.length == 2){
 					fileName = parts[0];
 					int errCode = Integer.parseInt(parts[1]);
 					if(errCode == Error.ERR_10){
 						//This is a client saying it doesn't have a file after the server sent it a WF
 						//This means the MasterFile has some corrupted state, change permissions for that client to invalid.
 						f = (MasterFile)this.getFileFromCache(fileName);
 						f.changePermissions(from, File.INV);
 					}
 				}
 				break;
 			case TXNProtocol.COMMIT_DATA:
 				if(!this.commitQueue.containsKey(from))
 					this.commitQueue.put(from, new ArrayList<Command>());
 				
 				contents = Utility.byteArrayToString(pkt.getPayload());
 				i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				lastSpace = i + 1;
 				i = contents.indexOf(' ', lastSpace);
 				int commandType = Integer.parseInt(contents.substring(lastSpace, i));
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				Command c = null;
 				if(commandType == Command.APPEND || commandType == Command.PUT || commandType == Command.UPDATE){
 					contents = i == contents.length() ? "" : contents.substring(i + 1);
 					c = new Command(MASTER_NODE, commandType, f, contents);
 				} else {
 					c = new Command(MASTER_NODE, commandType, f);
 				}
 				
 				this.commitQueue.get(from).add(c);
 				break;
 			case TXNProtocol.COMMIT:
 				this.commit(from, Integer.parseInt(Utility.byteArrayToString(pkt.getPayload())));
 				break;
 				
 				
 				
 				
 				
 			case TXNProtocol.CREATE:
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.getState() == File.INV){
 					f.addDep(from, new Update("", 0, MASTER_NODE));
 					f.setState(File.RW);
 					f.changePermissions(from, MasterFile.FREE);
 					String payload = fileName + " " + f.getVersion() + " ";
 					this.send(from, TXNProtocol.WD, Utility.stringToByteArray(payload));
 				}else{
 					String payload = DistNode.buildErrorString(this.n.addr, from, TXNProtocol.CREATE, fileName, Error.ERR_11);
 					this.send(from, TXNProtocol.ERROR, Utility.stringToByteArray(payload));
 				}
 				break;
 		}
 	}
 	
 	private void commit(int client, int size){
 		List<Command> commands = this.commitQueue.get(client);
 		if( commands != null && size != commands.size()){
 			this.send(client, TXNProtocol.ERROR, Utility.stringToByteArray(Error.ERROR_STRINGS[Error.ERR_40]));
 		} else if( commands == null ) {
 			this.send(client, TXNProtocol.COMMIT, new byte[0]);
 		}else {
 			
 			Log log = new Log(commands);
 			Commit c = new Commit(client, log);
 			
 			if(c.abort() && true){
 				for(MasterFile f : log)
 					f.abort(client);
 				this.send(client, TXNProtocol.ABORT, new byte[0]);
 			}else if(c.isWaiting()){
 				//add commit to queue and send heartbeat to nodes that the commit is waiting for
 				for(Integer addr : c){
 					this.send(addr, TXNProtocol.HB, new byte[0]);
 				}
 				this.waitingQueue.put(client, c);
 			}else{
 				//push changes to disk and put most recent version in memory in MasterFile
 				for(MasterFile f : log){
 					try{
 						int version = f.getVersion();
 						Update u = f.getInitialVersion(client + 0);
 						String contents = u.contents;
 						
 						for(Command cmd : log.getCommands(f)){
 							 if(cmd.getType() == Command.CREATE){
 								 this.n.create(cmd.getFileName());
 							 }else if(cmd.getType() == Command.APPEND){
 								contents += cmd.getContents();
 								version++;
 								this.n.write(f.getName(), contents, false, true);
 							}else if(cmd.getType() == Command.PUT){
 								contents = cmd.getContents();
 								version++;
 								this.n.write(f.getName(), contents, false, true);
 							} else if(cmd.getType() == Command.DELETE ) {
 								f.setState(File.INV);
 								this.n.delete(f.getName());
 							}
 						}
 						f.commit(client);
 					}catch(IOException e){
 						//TODO: send back error to client
 						return;
 					}
 				}
 				this.send(client, TXNProtocol.COMMIT, new byte[0]);
 			}
 		}
 	}
 	
 	private void slaveReceive(TXNPacket pkt){
 		String fileName;
 		File f;
 		
 		switch(pkt.getProtocol()){
 			case TXNProtocol.WF:
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = this.getFileFromCache(fileName);
 				
 				if(f.getState() == File.INV){
 					byte[] payload = Utility.stringToByteArray(fileName + " " + -1 + " ");
 					this.send(MASTER_NODE, TXNProtocol.WD, payload);
 				}else{
 					try {
 						byte[] payload = this.txn.getVersion(f, this.n.get(fileName));
 						this.send(MASTER_NODE, TXNProtocol.WD, payload);
 					} catch (IOException e) {
 						this.send(MASTER_NODE, TXNProtocol.ERROR, Utility.stringToByteArray(fileName + " " + Error.ERR_10));
 					}
 				}
 				break;
 			case TXNProtocol.WD:
 				String contents = Utility.byteArrayToString(pkt.getPayload());
 				int i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				int lastSpace = i + 1;
 				i = contents.indexOf(' ', lastSpace);
 				int version = Integer.parseInt(contents.substring(lastSpace, i));
 				contents = i == contents.length() - 1 ? "" : contents.substring(i + 1);
 				
 				f = this.getFileFromCache(fileName);
 				Command c = (Command)f.execute(); //Get command that originally requested this Query
 				try {
 					this.n.write(fileName, contents, false, true);
 					f.setState(File.RW);
 					f.setVersion(version);
 					this.txn.add(new Command(MASTER_NODE, Command.UPDATE, f, version + ""));
 					this.txn.add(c);
 				} catch (IOException e) {
 					this.n.printError("Fatal Error: Couldn't update file: " + fileName + " to version: " + version);
 				}
 				
 				executeCommandQueue(f);
 				break;
 			case TXNProtocol.ABORT:
 				this.abort();
 				break;
 			case TXNProtocol.COMMIT:
 				this.commitChangesLocally();
 				this.commitConfirm();
 				break;
 			case TXNProtocol.ERROR:
 				contents = Utility.byteArrayToString(pkt.getPayload());
 				i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				contents = contents.substring(i + 1);
 				
 				f = this.getFileFromCache(fileName);
 				f.execute();
 				this.n.printError(contents);
 		}
 	}
 	
 	public void commitChangesLocally() {
 		for( Command c : this.txn ) {
 			try {
 				int type = c.getType();
 				switch( type ) {
 				case Command.GET :
 					this.n.printData(this.n.get(c.getFileName() ));
 					break;
 				case Command.APPEND:
 					this.n.write(c.getFileName(), c.getContents(), true, false);
 					break;
 				case Command.PUT:
 					this.n.write(c.getFileName(), c.getContents(), false, false);
 					break;
 				case Command.CREATE:
 					this.n.create(c.getFileName());
 					break;
 				case Command.DELETE:
 					this.n.delete(c.getFileName());
 					break;
 				}
 			} catch(IOException e) {
 				this.n.printError("Fatal Error: When applying commit locally on: " + c.getFileName() + "  command: " + c ) ;
 			}
 			if(c.getType() == Command.UPDATE)
 				this.n.printSuccess(c);
 		}
 		
 	}
 	
 	public void executeCommandQueue(File f){
 		Command c = (Command)f.peek();
 		boolean stop = false;
 		while(c != null && !stop){
 			switch(c.getType()){
 			case Command.APPEND:
 				stop = !append(c, f);
 				break;
 			case Command.CREATE:
 				stop = !create(c, f);
 				break;
 			case Command.DELETE:
 				stop = !delete(c, f);
 				break;
 			case Command.PUT:
 				stop = !put(c, f);
 				break;
 			case Command.GET:
 				stop = !get(c, f);
 				break;
 			}
 			c = (Command)f.peek();
 		}
 	}
 
 
 	
 
 	/*=====================================================
 	 * Methods DistNode uses to talk to TXNLayer
 	 *=====================================================*/
 	
 	public boolean get(String filename){
 		if( assertTXNStarted() ) {
 			File f = this.cache.get( filename );
 			Command c = new Command(MASTER_NODE, Command.GET, f);
 			
 			if(f.execute(c)){
 				return get(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean get(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(MASTER_NODE, RPCProtocol.GET, Utility.stringToByteArray(f.getName()));
 			return false;
 		}else{
 			f.execute();
 			if(this.txn.isDeleted(f))
 				this.n.printError(c, Error.ERR_10);
 			else
 				this.txn.add( c );
 			txnExecute();
 			return true;
 		}
 	}
 
 	public boolean create(String filename){
 		boolean rtn = false;
 		if( assertTXNStarted() ) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.CREATE, f, "");
 			
 			if(f.execute(c)){
 				return create(c, f);
 			}
 		}
 		return rtn;
 	}
 	
 	private boolean create(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(MASTER_NODE, TXNProtocol.CREATE, Utility.stringToByteArray(f.getName()));
 			return false;
 		}else{
 			f.execute();
 			txnExecute();
 			return true;
 		}
 	}
 
 	public boolean put(String filename, String content){
 		if( assertTXNStarted() ) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.PUT, f, content);
 			
 			if(f.execute(c)){
 				return put(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean put(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(MASTER_NODE, TXNProtocol.WQ, Utility.stringToByteArray(f.getName() + " " + File.RW)); //WQ
 			return false;
 		}else{
 			f.execute();
 			if(this.txn.isDeleted(f)) {
 				this.n.printError(c, Error.ERR_10);
 			}
 			else {
 				this.txn.add( c );
 			}
 			txnExecute();
 			return true;
 		}
 	}
 
 	public boolean append(String filename, String content){
 		if( assertTXNStarted() ) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.APPEND, f, content);
 			
 			if(f.execute(c)) {
 				return append(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean append(Command c, File f){
 		if(f.getState() != File.RW) {
 			this.send(MASTER_NODE, TXNProtocol.WQ, Utility.stringToByteArray(f.getName() + " " + File.RW)); //WQ
 			return false;
 		}else{
 			f.execute();
 			
 			if(this.txn.isDeleted(f)) {
 				this.n.printError(c, Error.ERR_10);
 			} else {
 				this.txn.add( c );
 			}
 			txnExecute();
 			return true;
 		}
 		
 	}
 
 	//TODO: Decide what to do for creates/deletes and transactions
 	public boolean delete(String filename){
 		if( assertTXNStarted() && notCommited() ) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.DELETE, f);
 		
 			if(f.execute(c)) {
 				return delete(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean delete(Command c, File f){
 		if(f.getState() != File.RW) {
 			this.send(MASTER_NODE, TXNProtocol.WQ, Utility.stringToByteArray(f.getName()));
 			return false;//WQ
 		} else {
 			f.execute();
 			f.setState(File.INV);
 			this.txn.add(c);
 			txnExecute();
 			return true;
 			
 		}
 	}
 
 	public void abort() {
 		if( assertTXNStarted() )
 			this.txn = null;
 	}
 	
 	public void txnExecute() {
 		if( this.txn.willCommit ) {
 			this.txn.decrementNumQueued();
 			if( this.txn.getNumQueued() == 0 ) {
 				this.commit();
 			}
 		}
 
 	}
 
 	public void commit() {
 
 		if( assertTXNStarted() ) {
 			//Check to see if there are queued commands before committing
 			if( noQueuedCommands() ) {
 				//Send all of our commands to the master node
 				int cnt = 0;
 				for( Command c : this.txn ) {
 					String payload = c.getFileName() + " " + c.getType() + " ";
 					if( c.getType() == Command.PUT || c.getType() == Command.APPEND || c.getType() == Command.UPDATE ) {
 						payload += c.getContents();
 					}
 					this.send(MASTER_NODE, TXNProtocol.COMMIT_DATA, Utility.stringToByteArray(payload));
 					cnt++;
 				}
 				//Send the final commit message
 				this.send(MASTER_NODE, TXNProtocol.COMMIT, Utility.stringToByteArray(cnt + "") );
 			} else {
 				//set will commit to true to that the txn commits after all queued commands complete
 				this.txn.willCommit = true;
 			}
 		}
 	}
 	
 	public boolean noQueuedCommands() {
 		int commandCount = 0;
		for( File f : this.txn.getFiles() ) {
 			commandCount += f.numCommandsOnQueue();
 		}
 		if( commandCount > 0 ) {
 			this.txn.setNumQueued( commandCount );
 			return false;
 		}
 		return true;
 	}
 	
 	public void commitConfirm() {
 		this.txn = null;
 	}
 
 	public void start() {
 		if( this.txn == null ) {
 			int newTXNnum = this.lastTXNnum + RIONode.NUM_NODES;
 			
 			//start a new transaction by creating a new transaction object
 			this.txn = new Transaction( newTXNnum );
 		} else {
 			this.n.printError("ERROR: Transaction in progress: can not start new transaction");
 		}
 	}
 	
 	public boolean assertTXNStarted() {
 		if( this.txn == null ) {
 			this.n.printError("ERROR: No transaction in progress: please start new transaction");
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean notCommited() {
 		if( this.txn.willCommit ) {
 			this.n.printError("ERROR: Current transaction to be commited: please start new transaction");
 			return false;
 		}
 		return true;
 	}
 	
 	private File getFileFromCache(String fileName) {
 		File f = this.cache.get(fileName);
 		
 		if(f == null){
 			f = this.n.addr == MASTER_NODE ? new MasterFile(fileName, "") : new File(File.INV, fileName);
 			this.cache.put(fileName, f);
 		}
 		return f;
 	}
 
 }
