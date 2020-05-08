 package net.mayateck.DataShuttle;
 
 import java.net.*;
 import java.io.*;
 
 //@SuppressWarnings("unused")
 public class FetchNetworkData {

	public static boolean checkConnectionToServer(){
		boolean connected = false;
 		try {
			URL url = new URL("http://services.mayateck.net/datashuttle/dataHandler.php?type=ping");
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			BufferedReader read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			String result="";
 			while ((line = read.readLine()) != null) {
 				result += line;
 			}
 			if (result=="connected"){
				connected=true;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return connected;
 	}
 	
 	public static int fetchPlayerData(String pName){
 		int returnCode = 0;
 		
 		return returnCode;
 	}
 }
