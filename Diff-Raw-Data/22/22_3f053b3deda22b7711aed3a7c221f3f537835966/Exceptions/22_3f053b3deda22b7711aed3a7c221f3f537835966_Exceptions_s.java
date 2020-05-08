 package tcwi.exception;
 
 import java.util.HashMap;
 
 public class Exceptions {
 	private HashMap<Integer, String> exception;
 	
	public void Exception(){
 		exception = new HashMap<Integer, String>();
 		exception.put(1, "ERROR 1: Can't read the setting-file");
 		exception.put(2, "ERROR 2: Error during parse the setting-file. Is it malformed?");
 		exception.put(3, "ERROR 3: Can't generate and write the javascript file.");
 		exception.put(4, "ERROR 4: Can't write the setting-file.");
 		exception.put(5, "ERROR 5: Project name already set!");
 		exception.put(6, "ERROR 6: Can't access the given path. Maybe you do not have the rights for it?");
 		exception.put(7, "ERROR 7: File doesn't exist.");
 		exception.put(8, "ERROR 8: Can't read / write the given file.");
 	}
 	
 	public void throwException(int i, Exception e, boolean isExit, String pathInformation){
 		if(pathInformation!=""){
			System.err.println(exception.get(i) + "The Path is: " + pathInformation);
 		}else{
 			System.err.println(exception.get(i));
 		}
 		System.err.println();
 		if(e!=null){
 			e.printStackTrace();
 		}
 		if(isExit){
 			System.exit(i);
 		}
 	}
 }
