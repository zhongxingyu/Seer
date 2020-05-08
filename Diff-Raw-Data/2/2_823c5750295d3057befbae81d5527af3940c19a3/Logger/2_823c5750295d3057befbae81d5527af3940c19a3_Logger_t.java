 package nl.giantit.minecraft.GiantShop.core.Logger;
 
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Database.db;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 public class Logger {
 
 	public static void Log(String data) {
 		Log("Unknown", data);
 	}
 	
 	public static void Log(String n, String data) {
 		Log(LoggerType.UNKNOWN, n, data);
 	}
 	
 	public static void Log(Player p, String data) {
 		Log(LoggerType.UNKNOWN, p, data);
 	}
 	
 	public static void Log(CommandSender s, String data) {
 		Log(LoggerType.UNKNOWN, s, data);
 	}
 	
 	public static void Log(LoggerType t, String data) {
 		Log(t, "Unknown", data);
 	}
 	
 	public static void Log(LoggerType t, CommandSender s, String data) {
 		if(s instanceof Player) {
 			Log(t, (Player) s, data);
 		}else{
 			Log(t, "Console", data);
 		}
 	}
 	
 	public static void Log(LoggerType t, Player p, String data) {
 		Log(t, p.getName(), data);
 	}
 	
 	public static void Log(LoggerType t, String n, String data) {
 		config conf = config.Obtain();
 		if(conf.getBoolean("GiantShop.global.logActions")) {
 			db DB = db.Obtain(); 
 			int type = t.getID();
 			
 			ArrayList<String> fields = new ArrayList<String>();
 			ArrayList<HashMap<Integer, HashMap<String, String>>> values = new ArrayList<HashMap<Integer, HashMap<String, String>>>();
 	
 			fields.add("type");
 			fields.add("user");
 			fields.add("data");
 			fields.add("date");
 			
 			HashMap<Integer, HashMap<String, String>> tmp = new HashMap<Integer, HashMap<String, String>>();
 			int i = 0;
 			for(String field : fields) {
 				HashMap<String, String> temp = new HashMap<String, String>();
 				if(field.equalsIgnoreCase("type")) {
 					temp.put("kind", "INT");
 					temp.put("data", "" + type);
 					tmp.put(i, temp);
 				}else if(field.equalsIgnoreCase("user")) {
 					temp.put("data", "" + n);
 					tmp.put(i, temp);
 				}else if(field.equalsIgnoreCase("data")) {
 					temp.put("data", "" + data);
 					tmp.put(i, temp);
 				}else if(field.equalsIgnoreCase("date")) {
					temp.put("data", "" + (int) Logger.getTimestamp());
 					tmp.put(i, temp);
 				}
 				i++;
 			}
 			values.add(tmp);
 			
 			DB.insert("#__log", fields, values).updateQuery();
 		}
 	}
 	
 	public static long getTimestamp() {
 		
 		Date d = new Date();
 		return d.getTime();
 	}
 }
