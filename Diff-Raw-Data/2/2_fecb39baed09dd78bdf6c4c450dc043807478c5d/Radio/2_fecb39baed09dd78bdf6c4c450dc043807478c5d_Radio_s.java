 package sat.radio;
 
 import java.io.FileNotFoundException;
 
 import sat.file.*;
 import sat.radio.message.*;
 
 public abstract class Radio {
 	RadioID id = new RadioID(); // UFO !
 	
	public void file(String path, String dest) {
		DataFile file = new DataFile(path);
 	public void file(String path, String dest) throws FileNotFoundException {
 		SegmentableFile file = new SegmentableFile(path);
 	}
 	
 	protected void send(Message msg, String dest) {
 		
 	}
 }
