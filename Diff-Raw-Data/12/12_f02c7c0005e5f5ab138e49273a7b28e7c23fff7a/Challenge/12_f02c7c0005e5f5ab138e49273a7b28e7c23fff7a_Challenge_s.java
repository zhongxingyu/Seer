 package frontend;
 
 public class Challenge extends Message {
 	private String url;
 	
	public Challenge(String src, String dest, String body, String url){
		super(src, dest, body);
 		this.url = url;
 	}
 	
 	
 }
