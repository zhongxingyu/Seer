 package bitter;
 
 import java.io.*;
 

public class FileIO {
	private File file;
 	
	public void createFile(String name) {
		file = new File(new String("../bitter."), name);
		FileInputStream input = new FileInputStream(name);		
		
 	}
 }
