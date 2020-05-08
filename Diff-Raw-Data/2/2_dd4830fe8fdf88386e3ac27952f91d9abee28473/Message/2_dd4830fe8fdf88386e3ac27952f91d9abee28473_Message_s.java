 package edu.berkeley.cs.cs162;
 
 public class Message {
 	private String timestamp;
 	private String source;
 	private String dest;
 	private String content;
 	private int sqn;
 	private boolean isFromGroup = false;
 	
 	public Message(String timestamp, String source, String dest, String content) {
 		this.timestamp = timestamp;
 		this.source = source;
 		this.dest = dest;
 		this.content = content;
 		sqn = 0;
 	}
 	
 	public void setSQN(int num){
 		sqn = num;
 	}
 	
 	public void setIsFromGroup() {
 		isFromGroup = true;
 	}
 	
 	public String getTimestamp() {
 		return timestamp;
 	}
 	
 	public String getSource() {
 		return source;
 	}
 	
 	public String getDest() {
 		return dest;
 	}
 	
 	public String getContent() {
 		return content;
 	}
 	
 	public boolean isFromGroup() {
 		return isFromGroup;
 	}
 	
 	/**
 	 * format: SRC DST TIMESTAMP_UNIXTIME SQN
 	 */
 	
 	public String toString(){
		return source + " " + dest + " " + timestamp + " " + sqn;
 	}
 	
 }
