 import java.util.HashMap;
 
 public class WClient implements Runnable {
 	private HashMap<Integer, String> Users = new HashMap<Integer, String>(); //connected users
 	private Communication c; //link to server
 	private Whiteboard w;
 	private String name = "User";
 
 	public WClient(Whiteboard w, String how, String where, String name) {
 		this.w = w;
 		this.name = name;
 		c = new Communication(where, how);
 		sendMessage("user: "+name);
 	}
 
 	public void sendMessage(String m) {
 		c.sendMessage(m);
 	}
 
 	public void run() {
 		try {
 			while (true) {
 				String m = c.readMessage();
 				decodeMessage(m); //TODO implement message system
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			c.close();
 		}
 		System.exit(0);
 	}
 
 	private void decodeMessage(String message) throws Exception {
 		String[] m = message.split(" ");
 		
 		if(message.startsWith("users"))
 			populateUserList(message.split("#"));
 		//else if (message.startsWith("draw"))
 			
 		//else
 			
 	}
 	
 	private void populateUserList(String[] m) {
 		Users.clear();
 		
 		for (String s: m) {
 			String[] user = s.split(" ");
			Users.put(Integer.parseInt(user[0]), user[1]);
 		}
 
 		w.populateUserList();
 	}
 }
