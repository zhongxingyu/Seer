 package communication;
 
 import java.util.HashMap;
 import java.util.Set;
 
 public class Message {
 
 	private String type;
 	private String to, from;
 	private HashMap<String, String> data;
 	
 	public Message(String type, String to, String from) {
 		this.type = type;
 		this.to = to;
 		this.from = from;
		data = new HashMap<>();
 	}
 	
 	public void addData(String key, String value) {
 		data.put(key, value);
 	}
 	
 	public String getData(String key) {
 		return data.get(key);
 	}
 	
 	public Set<String> getKeys() {
 		return data.keySet();
 	}
 	
 	public String getType() {
 		return type;
 	}
 	
 	public String getTo(){
 		return to;
 	}
 	
 	public String getFrom(){
 		return from;
 	}
 	
 	
 }
