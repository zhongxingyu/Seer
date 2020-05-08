 package asl;
 
 public class Message {
 	public String content;
	public long context;
 	public long sender;
 	public long id;
 	
	public Message(long sender, long context, long id, String content){
 		this.content = content;
 		this.context = context;
 		this.sender = sender;
 		this.id = id;
 	}
 }
