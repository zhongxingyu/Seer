 package client.data;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 import javax.microedition.io.file.FileSystemRegistry;
 
 import net.rim.device.api.io.FileNotFoundException;
 import net.rim.device.api.io.IOUtilities;
 import client.Common;
 import data.bonus.Bonus;
 import data.me.json.JSONException;
 import data.me.json.JSONObject;
 
 
 /**
  * Use this class to get the keys, to write to a file, and getting values.
  * This differs from justing using the JSON classes directly as we keep
  * references to the object centralized. We can take care of any additional
  * code that is needed to read/write to files rather.
  */
 public class JSONUtils{
 	
 	
 	
 	//some code from http://supportforums.blackberry.com/t5/Java-Development/Unable-to-read-SDCard-data/td-p/492822
 	
 	/**
 	 * Get a json object from file path
 	 * @param path The file path where the json string is stored
 	 * @param displayError true if you want to display an error on file not found.
 	 */
 	public static JSONObject readFile(String path,boolean displayError) throws FileNotFoundException{
 		InputStream is = null;
 		if(!isSDCardInserted()){
 			Common.displayErrorMsg("SD Card not inserted.");
 			return null;
 		}
 		
 		try {
 			FileConnection fconn = (FileConnection)Connector.open(path,Connector.READ_WRITE);
 			if (!fconn.exists()) {//file doesn't exist
 				if(displayError)
 					Common.displayErrorMsg(path+" not found.");
 				return null;
 			}
 			
 			is = fconn.openInputStream();
 			String str = new String(IOUtilities.streamToBytes(is), "UTF-8");
 			JSONObject obj = new JSONObject(str);
 			return obj; //return json obejct
 			
 		}catch (IOException e) {
 			Common.displayErrorMsg("Error reading "+path);
 			e.printStackTrace();
 			return null;
 		
 		}catch (JSONException e) {
 			Common.displayErrorMsg("File is malformed. Contact admin.");
 			e.printStackTrace();
 			return null;
 		
 		}finally{
 			if(is != null){
 				try { is.close(); } catch (IOException ignored) {}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Write all the necessary files
 	 * @throws JSONException 
 	 */
 	public static void writeData() throws JSONException{
 		//http://stackoverflow.com/questions/1519328/j2me-blackberry-how-to-read-write-text-file
 		
 		if(!isSDCardInserted()){
 			Common.displayErrorMsg("SD Card not inserted.");
 			return;
 		}
 		
 		try {
 			writeData(GameData.getCurrentGame().toJSONObject(),GameData.filePath);
 			writeData(Bonus.toJSONObject(),Bonus.filePath);
 			
 		}catch (IOException e) {
 			Common.displayErrorMsg("Error writing file");
 			e.printStackTrace();
 			return;
 		
 		}
 	}
 	
 	/**
 	 * Write json object to a specified object
 	 * @param json
 	 * @param filePath
 	 * @throws IOException
 	 */
 	private static void writeData(JSONObject json,String filePath) throws IOException{
 		DataOutputStream os = null;
 		//TODO: remvove the .as extensions after write data is confirmed to work
 		FileConnection fconn = (FileConnection)Connector.open(filePath+".as",Connector.READ_WRITE);
 		if (!fconn.exists()) {
 			fconn.create();
 		}
 		
 		os = fconn.openDataOutputStream();
 		String jsonString = json.toString();
		System.out.println("**************"+jsonString);
 		os.write(jsonString.getBytes());
 		os.close();
 		fconn.close();
 	}
 	
 	/**
 	 * Quick check to see if sd card has been inserted
 	 * @return
 	 */
 	private static boolean isSDCardInserted(){
 		Enumeration r = FileSystemRegistry.listRoots();
 		
 		//makes sure the sd card is inserted
 		while (r.hasMoreElements()) {
 			String root = (String) r.nextElement();
 			if( root.equalsIgnoreCase("sdcard/") ) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
