 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.util.Hashtable;
 import java.util.Scanner;
 
 
 public class IntegrityChecker implements Serializable {
 
 	private Hashtable<String, Record> integrityTable;
 	private class Record{
 		String contentHash;
 		long lastUpdate;
 		public int hashCode(){
 			return contentHash.hashCode();
 		}
 	}
 	
 	
 	 /**
 	   * Always treat de-serialization as a full-blown constructor, by
 	   * validating the final state of the de-serialized object.
 	   */
 	   private void readObject(
 	     ObjectInputStream aInputStream
 	   ) throws ClassNotFoundException, IOException {
 	     //always perform the default de-serialization first
 	     integrityTable = (Hashtable<String, IntegrityChecker.Record>)aInputStream.readObject();
 	  }
 
 	    /**
 	    * This is the default implementation of writeObject.
 	    * Customize if necessary.
 	    */
 	    private void writeObject(
 	      ObjectOutputStream aOutputStream
 	    ) throws IOException {
 	      //perform the default serialization for all non-transient, non-static fields
 	      aOutputStream.writeObject(integrityTable);
 	    }
 	
 	public IntegrityChecker() throws Exception{
 		File tmp = null; 
 		try{
 			tmp = new File("icheck.config");
 		}catch(Exception e){}
 		if(tmp == null || !tmp.exists())
 			integrityTable = new Hashtable<String, Record>();
 		else{
 			FileInputStream is = new FileInputStream(tmp);
 			ObjectInputStream restore = new ObjectInputStream(is);
 			readObject(restore);
 		}
 	}
 	
 	public void SaveTable() throws Exception{
 		File tmp = new File("icheck.config");
 		FileOutputStream os = new FileOutputStream(tmp);
 		ObjectOutputStream restore = new ObjectOutputStream(os);
 		writeObject(restore);
 	}
 	
 	public static String checkFile(String filename) throws Exception{
 		File input = new File(filename);
 		if(input == null || !input.exists() || !input.isFile()){
 			return "";
 		}
 		String body = "";
 		Scanner in = new Scanner(input);
 		while(in.hasNextByte()){
 			body += new String(new byte[]{in.nextByte()});
 		}
 		return calculateMD5(body);
 	}
 	
 	public static String calculateMD5(String text) throws Exception{
 		byte[] bytesOfMessage = text.getBytes("UTF-8");
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		byte[] thedigest = md.digest(bytesOfMessage);
 		return new String(thedigest);
 	}
 	
 	public static void addFile(Hashtable<String, Record> table, String filename){
		IntegrityChecker inte = new IntegriyChecker();
		Record newRecord = inte.new Record();
		newRecord.contentHash = checkFile(filename);
		newRecord.lastUpdate = System.currentTimeMillis();
		table.put(filename,newRecord);		
 	}
 	
 	public static void updateFile(Hashtable<String, Record> table, String filename){
		IntegrityChecker inte = new IntegrityChecker();
		Record newRecord  = inte.new Record();
		newRecord = table.get(filename);
		newRecord.contentHash = checkFile(filename);
		newRecord.lastUpdate = System.currentTimeMillis();
		table.get(filename).equals(newRecord);
 	}
 	
 	public static void removeFile(Hashtable<String, Record> table, String filename){
 		table.remove(filename);	
 	}
 	
 	public static void main(String[] args) throws Exception{
 		IntegrityChecker checker = new IntegrityChecker();
 		String cmd = args[0];
 		//list name of files currently secured by IntegrityChecker
 		if(cmd.equalsIgnoreCase("list")){
 			for(String key : checker.integrityTable.keySet()){
 				Record stats = checker.integrityTable.get(key);
 				System.out.println("Filename: " + key);
 				System.out.println("Stored Value: " + stats.contentHash);
 				System.out.println("Last Update: " + stats.lastUpdate);
 				String nmd5 = checkFile(key);
 				System.out.println("OK?: " + (nmd5.equals(stats.contentHash) ? "yes" : "no"));
 				System.out.println();
 			}
 		}
 		else if(cmd.equalsIgnoreCase("add")){
 			addFile(checker.integrityTable, args[1]);
 		}
 		else if(cmd.equalsIgnoreCase("update")){
 			updateFile(checker.integrityTable, args[1]);
 		}
 		else if(cmd.equalsIgnoreCase("check")){
 			String key = args[1];
 			String nmd5 = checkFile(key);
 			Record stats = checker.integrityTable.get(key);
 			if(nmd5.equals(stats.contentHash)){
 				System.out.println(key + " is OK");
 			}
 			else{
 				System.err.println("Warning integrity lost in " + key);
 			}
 		}
 		else if(cmd.equalsIgnoreCase("remove")){
 			removeFile(checker.integrityTable, args[1]);
 		}
 		checker.SaveTable();
 	}
 }
