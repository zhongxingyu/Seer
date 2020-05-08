 package settings;
 
 public class Settings {
 
 	//Fields
 		public static int jumpKeyCode,leftKeyCode,rightKeyCode;
 	
 	//Methods
 		public static void init(){
			Settings.jumpKeyCode = 32;
			Settings.leftKeyCode = 37;
			Settings.rightKeyCode = 39;
 		}
 }
