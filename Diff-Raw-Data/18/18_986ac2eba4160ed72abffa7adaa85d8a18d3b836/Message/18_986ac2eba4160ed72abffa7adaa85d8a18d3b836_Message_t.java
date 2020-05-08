 /**
  * 
  */
 package de.tr0llhoehle.buschtrommel.models;
 
 import java.net.InetAddress;
 
 /**
  * @author tobi
  * 
  */
 public abstract class Message {
 	public static final char FIELD_SEPERATOR = 31;
 	public static final char MESSAGE_SPERATOR = 30;
	public static final String ENCODING = "UTF-8";
 
 	protected String type;
 	protected InetAddress source;
 
 	public abstract String Serialize();
 	
 	/**
 	 * Indicates the sender of the message, or null if the message was created on this system
 	 * @return sender or null
 	 */
 	public InetAddress getSource() {
 		return source;
 	}
 	
 	public void setSource(InetAddress s) {
 		source = s;
 	}
 	
 	@Override
 	public String toString() {
 		return Serialize();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(!(obj instanceof Message))
 			return false;
 		return ((Message) obj).Serialize().equals(Serialize());
 	}
 
 	public String getType() {
 		return type;
 	}
 }
