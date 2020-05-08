 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class CacheCoherenceLayer {
 	
 	private final static int MASTER_NODE = 0;
 	
 	private ReliableInOrderMsgLayer RIOLayer;
 	private DistNode n;
 	private HashMap<String, File> cache;
 	
 	public CacheCoherenceLayer(RIONode n) {
 		this.n = (DistNode)n;
 		this.RIOLayer = n.getRIOLayer();
 		this.cache = new HashMap<String, File>();
 	}
 	
 	public void sendCC( int server, int protocol, byte[] payload) {
 		RPCPacket pkt = new RPCPacket(protocol, payload);
 		this.RIOLayer.sendRIO(server, Protocol.RPC, pkt.pack());
 	}
 	
 	public void onRPCReceive(int from, byte[] payload) {
 		RPCPacket packet = RPCPacket.unpack(payload);
 		if(this.n.addr == MASTER_NODE)
 			masterReceive(from, packet);
 		else
 			slaveReceive(packet);
 	}
 	
 	
 	public void masterReceive(int from, RPCPacket packet){
 		String fileName;
 		MasterFile f;
 		String data;
 		int permissions;
 		String[] split;
 		Queueable pkt;
 		Map<Integer, List<Integer>> updates;
 		
 		switch(packet.getProtocol()){
 			case RPCProtocol.GET: //Payload structure: "[filename] [permissions]"
 				split = Utility.byteArrayToString(packet.getPayload()).split(" ");
 				fileName = split[0];
 				permissions = Integer.parseInt(split[1]);
 				
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.execute(packet)){ //If the file isn't already executing a request
 					this.get(from, f, fileName, permissions);
 				}
 				
 				break;
 			case RPCProtocol.PUT: //Payload structure: "[filename] [permissions] [contents]"
 				data = Utility.byteArrayToString(packet.getPayload());
 				int index = data.indexOf(' ');
 				fileName = data.substring(0, index);
 				index = data.indexOf(' ', fileName.length() + 1);
 				permissions = Integer.parseInt(data.substring(fileName.length() + 1, index));
 				String contents = data.substring(index + 1);
 				
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				//Update file contents
 				try {
 					this.n.write(fileName, contents, false, true);
 				} catch (IOException e) {
 					this.n.printError("Fatal Error: Couldn't replace file " + fileName);
 				}
 				
 				pkt = f.execute();	//Get the RQ that requested the file & dequeue it
 				f.changePermissions(pkt.getSource(), permissions);
 				f.setState(permissions);
 				
 				//Return the Data to the client that requested it
 				this.sendCC(pkt.getSource(), RPCProtocol.PUT, packet.getPayload());
 				
 				this.executePacketQueue(f);
 				
 				break;
 			case RPCProtocol.CONF: //Payload structure: "[filename] [permissions]"
 				split = Utility.byteArrayToString(packet.getPayload()).split(" ");
 				fileName = split[0];
 				permissions = Integer.parseInt(split[1]);
 				
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				//This is a delete confirmation
 				f.changePermissions(from, File.INV);
 				updates = f.getUpdates(from);
 				
 				pkt = f.peek();
 				if(updates.containsKey(File.RW)){	//Error
 					f.execute();
 					this.n.printError("FATAL ERROR: File permissions inconsistent");
 					this.executeCommandQueue(f);
 				}else if(!updates.containsKey(File.RO)){ //There aren't any more RO copies out there
 					f.execute();
 					if(f.getState() == File.INV){ //The requested operation was a delete
 						this.sendCC(pkt.getSource(), RPCProtocol.DELETE, Utility.stringToByteArray(fileName));
 					}else{ //The requested opertion was a write
 						//Return the Data to the client that requested it
 						this.sendCC(pkt.getSource(), RPCProtocol.PUT, packet.getPayload());
 					}
 					
 					this.executePacketQueue(f);
 				}
 				break;
 			case RPCProtocol.CREATE: //Payload structure: "[filename]"
 				fileName = Utility.byteArrayToString(packet.getPayload());
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.execute(packet)){ //If the file isn't already executing a request
 					this.create(from, f, fileName);
 				}
 				break;
 			case RPCProtocol.DELETE: //Payload structure: "[filename]"
 				fileName = Utility.byteArrayToString(packet.getPayload());
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.execute(packet)){ //If the file isn't already executing a request
 					this.delete(from, f, fileName);
 				}
 				break;
 		}
 	}
 	
 	private boolean get(int from, MasterFile f, String fileName, int type){
 		if(f.getState() == File.INV){ //The file doesn't exist.
 			String error = DistNode.buildErrorString(MASTER_NODE, from, RPCProtocol.GET, fileName, Error.ERR_10);
 			this.sendCC(from, RPCProtocol.ERROR, Utility.stringToByteArray(fileName + " " + error));
 			return true;
 		}
 		
 		Map<Integer, List<Integer>> updates = f.getUpdates(from);
 		
 		if(updates.containsKey(File.RW)){
 			int ownerAddr = updates.get(File.RW).get(0);
 			byte[] payload = Utility.stringToByteArray(fileName + " " + type);
 			
 			this.sendCC(ownerAddr, RPCProtocol.GET, payload); //Send RF to owner and wait for RD back
 			return false;
 		}else if(updates.containsKey(File.RO)){ //There are only RO copies checked out
 			
 			if(type == File.RO){	//This is a RQ
 				try{
 					String contents = this.n.get(fileName);
 					byte[] payload = Utility.stringToByteArray(fileName + " " + type + " " + contents);
 					this.sendCC(from, RPCProtocol.PUT, payload);
 				}catch(Exception e){
 					this.sendCC(from, RPCProtocol.ERROR, Utility.stringToByteArray("Fatal Error: File: " + fileName + " doesn't exist on master node"));
 				}
 				return true;
 			}else{					//This is a WQ
 				for(Integer addr : updates.get(File.RO)){	//Send invalidates to every client that has a RO copy and wait for ICs back
 					this.sendCC(addr, RPCProtocol.INV, Utility.stringToByteArray(fileName));
 				}
 				return false;
 			}
 		}else{ //No one has a copy checked out
 			try{
 				String contents = this.n.get(fileName);
 				byte[] payload = Utility.stringToByteArray(fileName + " " + type + " " + contents);
 				this.sendCC(from, RPCProtocol.PUT, payload);
 			}catch(Exception e){
 				this.sendCC(from, RPCProtocol.ERROR, Utility.stringToByteArray("Fatal Error: File: " + fileName + " doesn't exist on master node"));
 			}
 			return true;
 		}
 	}
 	
 	private boolean create(int from, MasterFile f, String fileName){
 		f.execute();
 		if(f.getState() == File.INV){
 			try {
 				this.n.create(fileName);
 				f.setState(File.RW);
 				
 				String returnPayload = fileName + " " + File.RW;
 				this.sendCC(from, RPCProtocol.PUT, Utility.stringToByteArray(returnPayload));
 			} catch(IOException e) { //File already exists
 				String error = DistNode.buildErrorString(this.n.addr, from, RPCProtocol.CREATE, fileName, Error.ERR_11);
 				this.sendCC(from, RPCProtocol.ERROR, Utility.stringToByteArray(fileName + " " + error) );
 			}
 		}else{ //File already exists
 			String error = DistNode.buildErrorString(this.n.addr, from, RPCProtocol.CREATE, fileName, Error.ERR_11);
 			this.sendCC(from, RPCProtocol.ERROR, Utility.stringToByteArray(fileName + " " + error) );
 		}
 		return true;
 	}
 	
 	public boolean delete(int from, MasterFile f, String fileName){
 		if(f.getState() != File.INV){
 			try {
 				this.n.delete(fileName);
 				f.setState(File.INV);
 				
 				Map<Integer, List<Integer>> updates = f.getUpdates(from);
 				
 				if( updates.containsKey(File.RW)) { //There is a RW copy checked out
 					this.sendCC(updates.get(File.RW).get(0), RPCProtocol.INV, Utility.stringToByteArray(fileName));
 					return false;
 				} else if(updates.containsKey(File.RO)){ //There are RO copies checked out
 					for(Integer addr : updates.get(File.RO)){	//Send invalidates to every client that has a RO copy and wait for ICs back
 						this.sendCC(addr, RPCProtocol.INV, Utility.stringToByteArray(fileName));
 					}
 					return false;
 				}else{ //No one has a copy checked out
 					this.sendCC(from, RPCProtocol.DELETE, Utility.stringToByteArray(fileName));
 					return true;
 				}
 			}catch(IOException e){ //File doesn't exist
 				String error = DistNode.buildErrorString(this.n.addr, from, RPCProtocol.ERROR, fileName, Error.ERR_10);
 				this.sendCC(from, RPCProtocol.ERROR, Utility.stringToByteArray(fileName + " " + error));
 				return true;
 			}
 		}else{ //File doesn't exist
 			String error = DistNode.buildErrorString(this.n.addr, from, RPCProtocol.ERROR, fileName, Error.ERR_10);
 			this.sendCC(from, RPCProtocol.ERROR, Utility.stringToByteArray(fileName + " " + error));
 			return true;
 		}
 	}
 	
 	public void executePacketQueue(MasterFile f){
 		String[] split;
 		String fileName;
 		int permissions;
 		
 		RPCPacket pkt = (RPCPacket)f.peek();
 		boolean stop = false;
 		
 		while(pkt != null && !stop){
 			switch(pkt.getProtocol()){
 				case RPCProtocol.GET: //Payload structure: "[filename] [permissions]"
 					split = Utility.byteArrayToString(pkt.getPayload()).split(" ");
 					fileName = split[0];
 					permissions = Integer.parseInt(split[1]);
 					
 					stop = !this.get(pkt.getSource(), f, fileName, permissions);
 					break;
 				case RPCProtocol.CREATE: //Payload structure: "[filename]"
 					fileName = Utility.byteArrayToString(pkt.getPayload());
 					f = (MasterFile)this.getFileFromCache(fileName);
 					
 					stop = !this.create(pkt.getSource(), f, fileName);
 					break;
 				case RPCProtocol.DELETE: //Payload structure: "[filename]"
 					fileName = Utility.byteArrayToString(pkt.getPayload());
 					f = (MasterFile)this.getFileFromCache(fileName);
 					
 					stop = !this.delete(pkt.getSource(), f, fileName);
 					break;
 			}
 			pkt = (RPCPacket)f.peek();
 		}
 	}
 	
 	public void slaveReceive(RPCPacket packet){
 		String fileName;
 		File f;
 		String data;
 		int permissions;
 		switch(packet.getProtocol()){
 			case RPCProtocol.GET: //Payload structure: "[filename] [permissions]"
 				data = Utility.byteArrayToString(packet.getPayload());
 				String[] dataSplit = data.split(" ");
 				
 				permissions = Integer.parseInt(dataSplit[1]);
 				fileName = dataSplit[0];
 				
 				f = this.getFileFromCache(fileName);
 				if(f.getState() != File.INV){ //This is a RF
 					if(permissions == File.RO){
 						f.setState(File.RO);
 						String payload;
 						
 						try {
 							payload = fileName + " " + File.RO + " " + this.n.get(fileName);
 							sendCC(MASTER_NODE, RPCProtocol.PUT, Utility.stringToByteArray(payload));
 						} catch (IOException e) {
 							this.n.printError("Fatal Error: Missing File " + fileName);
 						}				
 			
 					} else { //This is a WF
 						f.setState(File.INV);
 						String payload;
 						
 						try {
 							payload = fileName + " " + File.RW + " " + this.n.get(fileName);
 							sendCC(MASTER_NODE, RPCProtocol.PUT, Utility.stringToByteArray(payload));
 						} catch (IOException e) {
 							this.n.printError("Fatal Error: Missing File " + fileName);
 						}	
 					}
 				} else {
 					sendCC(MASTER_NODE, RPCProtocol.DELETE, Utility.stringToByteArray(fileName));
 				}		
 				break;
 			case RPCProtocol.INV: //Payload structure: "[filename]"
 				 fileName = Utility.byteArrayToString(packet.getPayload());
 				 f = this.getFileFromCache(fileName);
 				 f.setState(File.INV);
 				 
 				 String payload = fileName + " " + File.INV;
 				 sendCC(MASTER_NODE, RPCProtocol.CONF,  Utility.stringToByteArray(payload));
 				
 				break;
 			case RPCProtocol.PUT: //Payload structure: "[filename] [permissions] [contents]"
 				data = Utility.byteArrayToString(packet.getPayload());
 				int index = data.indexOf(' ');
 				fileName = data.substring(0, index);
 
 				index = data.indexOf(' ', fileName.length() + 1);
 				String contents;
 				if( index == -1 ) {
 					permissions = File.RW;
 					contents = "";
 					
 				} else {
					permissions = Integer.parseInt(data.substring(fileName.length(), index));
 					contents = data.substring(index + 1);
 				}
 				
 				f = this.getFileFromCache(fileName);
 				f.setState(permissions);
 				
 
 				Command c = (Command)f.execute(); //Get command that originally requested this Query
 				try {
 					this.n.write(fileName, contents, false, true);
 					if(permissions == File.RO){	//This was a RQ
 						this.n.printData(contents);
 					} else {					//This was a WQ
 						this.n.printSuccess(c);
 					}
 				} catch (IOException e) {
 					this.n.printError("Fatal Error: Couldn't create file " + fileName);
 				}
 				
 				executeCommandQueue(f);
 			
 				break;
 			case RPCProtocol.ERROR:
 				data = Utility.byteArrayToString(packet.getPayload());
 				index = data.indexOf(' ');
 				f = this.getFileFromCache(data.substring(0, index));
 				f.execute();
 				this.n.printError(data.substring(index + 1));
 				break;
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
 	
 
 	//CLIENT METHODS FOR INITIATING COMMANDS
 	public boolean create(String filename){
 		boolean rtn = false;
 		Command c = new Command(MASTER_NODE, Command.CREATE, filename);
 		
 		File f = getFileFromCache( filename );
 		
 		if(f.execute(c)){
 			return create(c, f);
 		}
 		
 		return rtn;
 	}
 	
 	private boolean create(Command c, File f){
 		if(f.getState() == File.INV){
 			this.sendCC(MASTER_NODE, RPCProtocol.CREATE, Utility.stringToByteArray(f.getName()));
 			return false;
 		}else{
 			f.execute();
 			this.n.printError(c, Error.ERR_11);
 			return true;
 		}
 	}
 	
 	public boolean get(String filename){
 		Command c = new Command(MASTER_NODE, Command.GET, filename);
 		
 		File f = getFileFromCache( filename );
 		
 		if(f.execute(c)){
 			return get(c, f);
 		}
 		
 		return false;
 	}
 	
 	private boolean get(Command c, File f){
 		if(f.getState() == File.INV){
 			this.sendCC(MASTER_NODE, RPCProtocol.GET, Utility.stringToByteArray(f.getName() + " " + File.RO)); //RQ
 			return false;
 		}else{
 
 			try {
 				f.execute();
 				this.n.printData(this.n.get(f.getName()));
 			} catch (IOException e) {
 				this.n.printError(c, Error.ERR_10);
 			}
 			return true;
 		}	
 	}
 	
 	public boolean put(String filename, String content){
 		Command c = new Command(MASTER_NODE, Command.PUT, filename, content);
 		
 		File f = getFileFromCache( filename );
 		
 		if(f.execute(c)){
 			return put(c, f);
 		}
 		
 		return false;
 	}
 	
 	private boolean put(Command c, File f){
 		if(f.getState() != File.RW){
 			this.sendCC(MASTER_NODE, RPCProtocol.GET, Utility.stringToByteArray(f.getName() + " " + File.RW)); //WQ
 			return false;
 		}else{
 			f.execute();
 			try {
 				this.n.write(f.getName(), c.getContents(), false, false);
 				this.n.printSuccess(c);
 			} catch (IOException e) {
 				this.n.printError(c, Error.ERR_10);
 			}
 			return true;
 		}
 		
 	}
 	
 	public boolean append(String filename, String content){
 		Command c = new Command(MASTER_NODE, Command.APPEND, filename, content);
 		
 		File f = getFileFromCache( filename );
 		
 		if(f.execute(c)) {
 			return append(c, f);
 		}
 		return false;
 	}
 	
 	private boolean append(Command c, File f){
 		if(f.getState() != File.RW) {
 			this.sendCC(MASTER_NODE, RPCProtocol.GET, Utility.stringToByteArray(f.getName() + " " + File.RW)); //WQ
 			return false;
 		}else{
 			f.execute();
 			try {
 				this.n.write(f.getName(), c.getContents(), true, false);
 				this.n.printSuccess(c);
 			} catch (IOException e) {
 				this.n.printError(c, Error.ERR_10);
 			}
 			return true;
 		}
 		
 	}
 	
 	public boolean delete(String filename){
 		Command c = new Command(MASTER_NODE, Command.DELETE, filename);
 	
 		File f = getFileFromCache( filename );
 		
 		if(f.execute(c)) {
 			return delete(c, f);
 		}
 		return false;
 	}
 	
 	private boolean delete(Command c, File f){
 		if(f.getState() != File.RW) {
 			this.sendCC(MASTER_NODE, RPCProtocol.DELETE, Utility.stringToByteArray(f.getName() + " " + File.RW));
 			return false;//WQ
 		} else {
 			f.execute();
 			try {
 				this.n.delete(f.getName());
 				this.n.printSuccess(c);
 			} catch (IOException e) {
 				this.n.printError(c, Error.ERR_10);
 			}
 			return true;
 			
 		}
 		
 	}
 	
 	private File getFileFromCache(String filename) {
 		File f = this.cache.get(filename);
 		
 		if(f == null){
 			f = this.n.addr == MASTER_NODE ? new MasterFile(filename) : new File(File.INV, filename);
 			this.cache.put(filename, f);
 		}
 		return f;
 	}
 	
 	public void printCache(){
 		for(String fileName : this.cache.keySet()){
 			File f = this.cache.get(fileName);
 			System.out.println(f);
 		}
 	}
 
 }
