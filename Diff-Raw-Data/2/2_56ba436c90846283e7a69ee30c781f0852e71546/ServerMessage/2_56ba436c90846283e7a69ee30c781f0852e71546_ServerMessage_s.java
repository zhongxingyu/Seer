 import java.io.Serializable;
 
 import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
 
 
 public class ServerMessage implements Serializable {
 
 	//required by serializable in order to send over the object sender.
 	private static final long serialVersionUID = 1L;
 	//paxos message types
 	static final int PAXOS_PREPARE = 0;
 	static final int PAXOS_ACK = 1;
 	static final int PAXOS_ACCEPT = 2;
 	static final int PAXOS_ADD_LEADER = 3;
 	//two phase commit message types
 	static final int TWOPHASE_VOTE_REQUEST = 10;
 	static final int TWOPHASE_VOTE_YES = 11;
 	static final int TWOPHASE_VOTE_NO = 12;
 	static final int TWOPHASE_COMMIT = 13;
 	static final int TWOPHASE_ABORT = 14;
 	//client message types
 	static final int CLIENT_READ = 20;
 	static final int CLIENT_APPEND = 21;
 	static final int CLIENT_GET_LEADER = 22;
 	static final int LEADER_RESPONSE = 23;
 	
 	private int type;
 	private String msg;
 	private String sourceAddress;
 	private int ballotNumber;
 	private int ballotProcID;
 	private int lastAcceptNumber;
 	private String lastAcceptVal;
 
 	public ServerMessage(int newType, String newMsg, String src){
 		setMessage(newMsg);
 		setType(newType);
 		setSourceAddress(src);
 		ballotNumber = -1;
 		ballotProcID = -1;
 		lastAcceptVal = null;
 	}
 	
 	public ServerMessage(int newType, String newMsg){
 		setMessage(newMsg);
 		setType(newType);
 		ballotNumber = -1;
 		ballotProcID = -1;
 		lastAcceptVal = null;
 	}
 	
 	public ServerMessage(int newType){
 		setMessage(null);
 		setType(newType);
 		setSourceAddress(null);
 		ballotNumber = -1;
 		ballotProcID = -1;
 		lastAcceptVal = null;
 	}
 	
 	public ServerMessage(){
 		setMessage(null);
 		setType(-1);
 		setSourceAddress(null);
 		ballotNumber = -1;
 		ballotProcID = -1;
 		lastAcceptVal = null;
 	}
 	
 	public String toString(){
 		
 		return "ServerMessage{source:" + getSourceAddress() + " type:" + this.getTypeName() + " msg:" + this.getMessage() + "}";
 	}
 
 	public String getTypeName() {
 		
 		Hashtable typemapping = new Hashtable();
 		typemapping.put(0, "PAXOS_PREPARE");
 		typemapping.put(1, "PAXOS_ACK");
 		typemapping.put(2, "PAXOS_ACCEPT");
 		typemapping.put(3, "PAXOS_ADD_LEADER");
 		typemapping.put(10, "TWOPHASE_VOTE_REQUEST");
 		typemapping.put(11, "TWOPHASE_VOTE_YES");
 		typemapping.put(12, "TWOPHASE_VOTE_NO");
 		typemapping.put(13, "TWOPHASE_COMMIT");
 		typemapping.put(14, "TWOPHASE_ABORT");
 		typemapping.put(20, "CLIENT_READ");
 		typemapping.put(21, "CLIENT_APPEND");
 	
 		if (this.type == -1) {
 			return null;
 		}
 		
 		return (String) typemapping.get(this.type);
 
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
 	
 	public int getBallotNumber() {
 		return ballotNumber;
 	}
 
 	public void setBallotNumber(int ballotNumber) {
 		this.ballotNumber = ballotNumber;
 	}
 
 	public int getBallotProcID() {
 		return ballotProcID;
 	}
 
 	public void setBallotProcID(int ballotProcID) {
 		this.ballotProcID = ballotProcID;
 	}
 
 	public int getLastAcceptNumber() {
 		return lastAcceptNumber;
 	}
 
 	public void setLastAcceptNumber(int lastAcceptNumber) {
 		this.lastAcceptNumber = lastAcceptNumber;
 	}
 
 	public String getLastAcceptVal() {
 		return lastAcceptVal;
 	}
 
 	public void setLastAcceptVal(String lastAcceptVal) {
 		this.lastAcceptVal = lastAcceptVal;
 	}
 
 	
 }
