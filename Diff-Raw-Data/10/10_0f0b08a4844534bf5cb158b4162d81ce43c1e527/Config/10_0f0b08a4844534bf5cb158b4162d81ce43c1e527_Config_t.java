 package dbtLab3;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import com.google.gson.Gson;
 
 public class Config {
	public static Settings config = new Config.Settings();
 	private static final String SETTINGS_FILE_NAME = ".eda216-lab3-settings";
 	
 	public static void reload() {
 		try {
 			FileReader fr = new FileReader(new File(SETTINGS_FILE_NAME));
 			Gson gson = new Gson();
 			config = gson.fromJson(fr, Config.Settings.class);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
	public static void commit(){
 		File settingsFile = null;
 		FileWriter fw = null;
 		BufferedWriter bw = null;
 		try {
 			settingsFile = new File(SETTINGS_FILE_NAME);
 			fw = new FileWriter(settingsFile);
 			bw = new BufferedWriter(fw);
 			Gson gson = new Gson();
 			bw.write(gson.toJson(config, Config.Settings.class));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if(bw != null){
 				try {
 					bw.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if(fw != null){
 				try {
 					fw.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	public static class Settings {
		public String username;
		public String password;
		public String db;
 	}
 }
