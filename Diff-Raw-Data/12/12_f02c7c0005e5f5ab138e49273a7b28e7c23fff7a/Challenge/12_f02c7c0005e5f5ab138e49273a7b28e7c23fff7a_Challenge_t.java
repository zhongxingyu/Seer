 package frontend;
 
 public class Challenge extends Message {
 	private String url;
 	
	public Challenge(String src, String dest, String body, String url, String time){
		super(src, dest, body, time);
 		this.url = url;
 	}
 	
 	
 }
