 package net.mayateck.DataShuttle;
 
 import java.net.*;
 import java.io.*;
 
 //@SuppressWarnings("unused")
 public class FetchNetworkData {
	static int checksum = DataShuttle.jarFile.hashCode(); // For security reasons, must match version hash.
	
	public static int checkConnectionToServer(){
		int connected = 1;
 		try {
			URL url = new URL("http://services.mayateck.net/datashuttle/dataHandler.php?type=ping&hash="+checksum);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			BufferedReader read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			String result="";
 			while ((line = read.readLine()) != null) {
 				result += line;
 			}
 			if (result=="connected"){
				connected=0;
			} else if (result=="bad hash"){
				connected=2;
			} else if (result=="maintinence"){
				connected=3;
			} else {
				connected=1;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
			connected=1;
 		}
 		
 		return connected;
 	}
 	
 	public static int fetchPlayerData(String pName){
 		int returnCode = 0;
 		
 		return returnCode;
 	}
 }
