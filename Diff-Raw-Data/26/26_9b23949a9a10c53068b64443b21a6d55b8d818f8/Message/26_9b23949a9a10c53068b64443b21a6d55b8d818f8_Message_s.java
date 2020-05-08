 package asl;
 
 public class Message {
 	public String content;
	public String context;
 	public long sender;
 	public long id;
 	
	public Message(long sender, String context, long id, String content){
 		this.content = content;
 		this.context = context;
 		this.sender = sender;
 		this.id = id;
 	}
 }
