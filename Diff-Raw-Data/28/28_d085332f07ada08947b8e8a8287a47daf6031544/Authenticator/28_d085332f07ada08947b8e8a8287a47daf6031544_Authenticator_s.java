 package api;
 
 public class Authenticator {
 
 	public static int auth(String user, String pass){
 		
 		if(user.equals("test") && pass.equals("1234")) return 1;
 		if(user.equals("bob") && pass.equals("1234")) return 111111;
 		if(user.equals("frank") && pass.equals("1234")) return 222222;
 		if(user.equals("teach") && pass.equals("1234")) return 999999;
 		
 		return 0;
 	}
 	
 }
