 package chatty;
 
 import java.io.Serializable;
 import java.net.Socket;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class User implements Serializable {
 	private final String userName;
 	private String displayName;
 	private int activePort;
 	private Date created;
 	private Date lastMessage;
 
 	public User(String name) {
 		this.userName = name;
 		displayName = name;
 		created = new Date();
 		lastMessage = created;
 	}
 
 	public User(String userName, String displayName, int activePort, Date lastMessage,Date created) {
 		this.created = created;
 		this.userName = userName;
 		this.displayName = displayName;
 		this.activePort = activePort;
 		this.lastMessage = lastMessage;
 	}
 
 	public String getName() {
 		return userName;
 	}
 
 	public String getDisplayName() {
 		return displayName;
 	}
 
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 
 	public void setActivePort(int activePort) {
 		this.activePort = activePort;
 	}
 
 	public int getActivePort() {
 		return activePort;
 	}
 
 	public String toString() {
 		return String.format("[UN: %s - DN: %s - P: %s - C: %s - LM: %s]", userName, displayName, activePort, new SimpleDateFormat("HH:mm:ss:SSSS").format(created), new SimpleDateFormat("mm:ss:SSSS").format(lastMessage));
 	}
 
 	public Date getLastMessage() {
 		return lastMessage;
 	}
 
 	public void setLastMessage() {
 		lastMessage = new Date();
 	}

	public User duplicate() {
		return new User(userName, displayName, activePort, lastMessage,created);

	}
 }
