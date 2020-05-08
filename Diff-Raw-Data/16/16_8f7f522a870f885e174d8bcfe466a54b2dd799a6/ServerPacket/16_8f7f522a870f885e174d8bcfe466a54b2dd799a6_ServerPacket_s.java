 package common;
 
 import java.io.Serializable;
 import java.net.InetAddress;
 
 public class ServerPacket implements Serializable{
 
 	private static final long serialVersionUID = 6561981141432776662L;
 
 	public final static boolean delete = false;
 	public final static boolean add = true;
 		
 	private ServerWorkflow sw;
 	
 	//used in 1,2,3
 	private String username;
 	
 	//used in 2
 	private Status newStatus;
 	
 	//used in 3
 	private String buddyName;
 	private boolean operation;
 	
 	//returned in 1
 	private InetAddress IP;
 
 	//1st workflow -- get IP for a given uname
 	public ServerPacket( String username ){
 		sw = ServerWorkflow.getIP;
 		this.username = username;
 	}
 	
 	//2nd workflow -- client changes status to newStatus
 	public ServerPacket( String username, Status newStatus ){
 		sw = ServerWorkflow.statusChange;
 		this.username = username;
 		this.newStatus = newStatus;
 	}
 	
 	//3rd workflow -- client adds or removes a buddy
 	public ServerPacket( String sendingClient, String buddyName, boolean operation ){
 		sw = ServerWorkflow.editBuddy;
 		this.username = sendingClient;
 		this.buddyName = buddyName;
 		this.operation = operation;
 	}
 	
 	//used in 1,2,3
 	public String getUsername(){
 		return username;
 	}
 	
 	//used in 2
 	public Status getStatus(){
 		return newStatus;
 	}
 	
 	//used in 3
 	public String getBuddyName(){
 		return buddyName;
 	}
 	
 	//used in 3
 	public boolean getOperation(){
 		return operation;
 	}
 	
 	//returned in 1
 	public void setIP( InetAddress IP ){
 		this.IP = IP;
 	}
 	
 	//returned in 1
 	public InetAddress getIP(){
 		return IP;
 	}
 	
 	public ServerWorkflow getWorkflowType(){
 		return sw;
 	}
 
 }
