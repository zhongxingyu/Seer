 package se.chalmers.chessfeud.constants;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import android.os.Environment;
 
 public class PlayerInfo {
 	
 	private static PlayerInfo instance = null;
 	
 	private String userName;
 	private String password;
 
 	
 	protected PlayerInfo() {
 	}
 	
 	public static PlayerInfo getInstance() {
 		if(instance == null) {
 			return new PlayerInfo();
 		}
 		return instance;
 	}
 	
 	private String getUserName() {
		;
 	}
 	
	private String getPassWord() {
		;
 	}
 	private void loadInfoFromFile() {
 		File sdcard = Environment.getExternalStorageDirectory();
 		File credentials = new File(sdcard, "usercred.txt");
		StringBuilder info = new StringBuilder();
		
 		try {
 		    BufferedReader br = new BufferedReader(new FileReader(credentials));
		    String line;

		    while ((line = br.readLine()) != null) {
		        info.append(line);
		        info.append('\n');
		    }
		}
 		catch (IOException e) {
 		}
 
 		
 	}
 
 
 }
