 package com.hack.regionunlocked;
 
 import java.util.HashMap;
 
 public class GameStatus {
 	
 	private String upcCode;
 	private String name;
 	private GameRegion gameRegion;
 	private HashMap<GameRegion,RegionSupportStatus> supportStatuses;
 	
 	public GameStatus(String upcCode) {
 		this.upcCode = upcCode;
 	}
 	
 	private void checkStatus() {
 		if (!this.name.equals("")) {
 			String content = getWebsiteContent("http://gaming.wikia.com/wiki/Region_Free_Xbox_360_Games");
 			
 		}
 	}
 	private String getWebsiteContent(String urlString) {
 		try {
 			URL url = new URL(urlString);
 			InputStream inStream = url.openStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
 			String content = "";
 			String line;
 			while ((line = br.readLine()) != null) {
 				content += line;
 			}
 			return content;
 		} catch (Exception ex) {
 			return "";
 		}
 	}
 	
 }
