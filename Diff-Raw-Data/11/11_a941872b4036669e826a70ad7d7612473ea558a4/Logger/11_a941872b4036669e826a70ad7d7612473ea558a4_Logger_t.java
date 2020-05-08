 package nl.giantit.minecraft.GiantShop.core.Logger;
 
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Database.Database;
 import nl.giantit.minecraft.GiantShop.core.Database.drivers.iDriver;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Logger {
 	
 	public static void Log(LoggerType type, String playerName, HashMap<String, String> data) {
 		config conf = config.Obtain();
 		if(conf.getBoolean("GiantShop.log.useLogging")) {
 			if(conf.getBoolean("GiantShop.log.log." + type.getName().toLowerCase())) {
 				String json = "{";
 				int i = 0;
 				for(Map.Entry<String, String> d : data.entrySet()) {
					i++;
 					json += "\"" + d.getKey() + "\": \"" + d.getValue() + "\"";
 					if(i < data.size()) {
 						json += ",";
 					}
 				}
 				json += "}";
 				
 				iDriver DB = Database.Obtain().getEngine();
 				int t = type.getID();
 				
 				ArrayList<String> fields = new ArrayList<String>();
 				ArrayList<HashMap<Integer, HashMap<String, String>>> values = new ArrayList<HashMap<Integer, HashMap<String, String>>>();
 		
 				fields.add("type");
 				fields.add("user");
 				fields.add("data");
 				fields.add("date");
 				
 				HashMap<Integer, HashMap<String, String>> tmp = new HashMap<Integer, HashMap<String, String>>();
 				i = 0;
 				for(String field : fields) {
 					HashMap<String, String> temp = new HashMap<String, String>();
 					if(field.equalsIgnoreCase("type")) {
 						temp.put("kind", "INT");
 						temp.put("data", "" + t);
 						tmp.put(i, temp);
 					}else if(field.equalsIgnoreCase("user")) {
 						temp.put("data", "" + playerName);
 						tmp.put(i, temp);
 					}else if(field.equalsIgnoreCase("data")) {
 						temp.put("data", "" + json);
 						tmp.put(i, temp);
 					}else if(field.equalsIgnoreCase("date")) {
						temp.put("data", "" + Logger.getTimestamp());
 						tmp.put(i, temp);
 					}
 					i++;
 				}
 				values.add(tmp);
 				
 				DB.insert("#__log", fields, values).updateQuery();
 			}
 		}
 	}
 	
 	public static long getTimestamp() {
		return System.currentTimeMillis() / 1000;
 	}
 }
