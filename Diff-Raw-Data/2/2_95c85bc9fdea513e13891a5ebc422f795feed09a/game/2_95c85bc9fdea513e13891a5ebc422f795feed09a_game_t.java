
 
 public class game {
 	
 	public static void main(String[] args) {
 
 		createchar createChar = new createchar();
 		
 		//pass data to character object
 		new character(createChar.fetchUserData());
 		
 	}
 	
 }
