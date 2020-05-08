 package trains;
 
 public class MessageHeader {
 
 	//length of the whole message
 	private int len;
 	//type of message -> enum AM_BROADCAST, AM_ARRIVAL or A_DEPARTURE
 	private String type;
 	
 	private MessageHeader(){
 		//do something
 	}
 	
 	public static void MessageHeader(){
 		//do something
 	}
 
 	public void setLen(int len){
 		this.len = len;
 	}
 
	public static seType(String type){
 		this.type = type;
 	}
 }
