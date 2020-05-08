 package sat.radio;
 
 import sat.file.*;
 import sat.radio.message.*;
 
 public abstract class Radio {
 	RadioID id = new RadioID(); // UFO !
 	
 	public void file(String path, String dest) {
 		DataFile file = new DataFile(path);
 	}
 	
 	protected void send(Message msg, String dest) {
 		
 	}
 }
