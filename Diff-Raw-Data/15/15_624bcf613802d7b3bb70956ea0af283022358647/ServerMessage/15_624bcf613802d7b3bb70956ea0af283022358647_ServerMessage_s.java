 import java.io.Serializable;
 
 
 public class ServerMessage implements Serializable {
 
 	//required by serializable in order to send over the object sender.
 	private static final long serialVersionUID = 1L;
 	//paxos message types
 	static final int PAXOS_PREPARE = 0;
 	static final int PAXOS_ACK = 1;
 	static final int PAXOS_ACCEPT = 2;
 	static final int PAXOS_DECIDE = 3;
 	//two phase commit message types
 	static final int TWOPHASE_VOTE_REQUEST = 10;
 	static final int TWOPHASE_VOTE_YES = 11;
 	static final int TWOPHASE_VOTE_NO = 12;
 	static final int TWOPHASE_COMMIT = 13;
 	static final int TWOPHASE_ABORT = 14;
 	//client message types
 	static final int CLIENT_READ = 20;
 	static final int CLIENT_APPEND = 21;
 	
 	private int type;
 	private String msg;
 	private String sourceAddress;
 	
 
 	public ServerMessage(int newType, String newMsg, String src){
 		setMessage(newMsg);
 		setType(newType);
 		setSourceAddress(src);
 	}
 	
 	public ServerMessage(int newType){
 		setMessage(null);
 		setType(newType);
 		setSourceAddress(null);
 	}
 	
 	public ServerMessage(){
 		setMessage(null);
 		setType(-1);
 		setSourceAddress(null);
 	}
 	
 	
 	//accessors and mutators
 	public int getType() {
 		return this.type;
 	}
 	
 	public String getMessage(){
 		return this.msg;
 	}
 
 	public String getSourceAddress() {
 		return sourceAddress;
 	}
 
 	public void setSourceAddress(String sourceAddress) {
 		this.sourceAddress = sourceAddress;
 	}
 	
 	public void setMessage(String newMsg){
 		this.msg = newMsg;
 	}
 	
 	public void setType(int newType){
 		this.type = newType;
 	}
 	
 	public String toString(){
 		
		return "ServerMessage{" + this.getType() + ": " + this.getMessage() + "}";
 	}
 }
