 package justen;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 public class Status implements Serializable {
 		
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public Hashtable<String, ArrayList<Integer>> fileVersionMap; // fileName, 0001010: bit string for stable file verions
	public HashMap<String, Lock> lockMap;
 	
 	public Status(FileManager fm) {
 		fileVersionMap = fm.getVersionMap();
 		lockMap = fm.getLockMap();
 	}
 }
