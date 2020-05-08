 package org.topq.jsystem.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.util.Log;
 
 public class ConfUtil {
 
 	private  Map<String, String> confMap = null;
 	private static ConfUtil confUtil = null;
 	private static final String TAG = "ConfUtil";
 
 	private ConfUtil() throws IOException{
 		confMap = new HashMap<String, String>();
 		InputStream in =null;
 		try {		
 			// init all the configuration
 			File config = new File("/data/conf.txt");
 			if (config.exists()){
 				in = new FileInputStream(config);
 				
 				InputStreamReader input = new InputStreamReader(in);
 				BufferedReader buffer = new BufferedReader(input);
 				String line = " ";
 				
 				while ((line = buffer.readLine()) != null) {
 					confMap.put(line.split("=")[0].trim(),
 							line.split("=")[1].trim());
 				}
 				
 			}else {
 				confMap.put("LAUNCHER_ACTIVITY_FULL_CLASSNAME", "com.gettaxi.android.activities.login.LoadingActivity");
 				confMap.put("TARGET_PACKAGE_ID", "com.gettaxi");
 			}
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to open file:/data/conf.txt" + e.getMessage());
 			e.printStackTrace();
 		}finally{
			if (null != in){
				in.close();
			}
 		}
 
 	}
 
 	public static ConfUtil getInstance() {
 		if(confUtil == null){
 			try{
 				confUtil = new ConfUtil();
 			} catch (IOException e) {
 				Log.e(TAG, "Failed to Create confUtil  " + e.getMessage());
 				e.printStackTrace();
 
 			}
 		}
 		return confUtil;
 	}
 
 	public String getConffigParmters(String paramName){
 		return confMap.get(paramName);
 	}
 
 }
