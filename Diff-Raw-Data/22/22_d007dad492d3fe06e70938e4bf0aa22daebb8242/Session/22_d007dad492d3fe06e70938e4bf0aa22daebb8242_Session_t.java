 package session;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.concurrent.*;
 
 public class Session {
 	private String sessionID;
 	private Integer versionNumber;
	private Timestamp expires = new Timestamp(0);
 	private ConcurrentHashMap<String, String[]> sessionTable = new ConcurrentHashMap<String, String[]>();
 	
 	public Session(String data, String clientIP){
 		Date date = new Date();
 		Timestamp stamp = new Timestamp(date.getTime());
 		
 		expires.setTime(stamp.getTime() + 600);
 		
 		//Creating UniqueID
 		setSessionID(clientIP + stamp.toString());
 		
 		//Initialize Version Number
 		versionNumber = 0;
 		
 		//Add This Session Object To The Hashtable
 		writeData(data);
 	}
 	
 	protected String getSessionID() {
 		return sessionID;
 	}
 	
 	protected void setSessionID(String sessionID) {
 		this.sessionID = sessionID;
 	}
 	
 	protected int getVersionNumber() {
 		return versionNumber;
 	}
 
 	protected Timestamp getExpires() {
 		return expires;
 	}
 	
 	protected void writeData(String data){
 		versionNumber++;
 		String[] temp = {data, versionNumber.toString(), expires.toString()};
 		sessionTable.put(sessionID, temp);
 	}
 }
