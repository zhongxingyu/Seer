 package classes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import classes.config.Device;
 import classes.config.Location;
 
 public class Config {
 	
 	private static HashMap<String, Location> config = new HashMap<String, Location>();
 	
 	public static HashMap<String, Location> getConfig() {
 		return config;
 	}
 	
 	public static void parse(JSONObject jloc) {
 
 		Iterator<?> lit = jloc.keys();
 		/* Iterate through all locations */
 		while(lit.hasNext()) {
 
 			String lkey = (String)lit.next();
 
 			try {
 
 				/* Create new location object */
 				Location location = new Location();
 				HashMap<String, Device> devices = new HashMap<String, Device>();
 
 				JSONObject jdev = jloc.getJSONObject(lkey);
 				Iterator<?> dit = jdev.keys();
 
 				/* Iterate through all devices of this location */
 				while(dit.hasNext()) {
 					String dkey = (String)dit.next();
 					
 					if(new String("name").equals(dkey)) {
 						location.setName(jdev.getString(dkey));
 					} else {
 
 						try {
 
 							/* Create new device object for this location */
 							Device device = new Device();
 							HashMap<String, ArrayList<String>> settings = new HashMap<String, ArrayList<String>>();
 							
 							JSONObject jset = jdev.getJSONObject(dkey);
 							Iterator<?> sit = jset.keys();
 							
 							/* Iterate through all settings of this device */
 							while(sit.hasNext()) {
 								String skey = (String)sit.next();
 								
 								if(new String("name").equals(skey)) {
 									device.setName(jset.getString(skey));
 								} else {
 
 									try {
 
 										/* Create new settings array for this device */
 										ArrayList<String> value = new ArrayList<String>();
 										
 										JSONArray jvalarr = jset.optJSONArray(skey);
 										String jvalstr = jset.optString(skey);
 										Double jvaldbl = jset.optDouble(skey);
 										Long jvallng = jset.optLong(skey);
 										
 										if(jvalarr != null) {
 											/* Iterate through all values for this setting */
 											for(Short i=0; i<jvalarr.length(); i++) {
 												value.add(jvalarr.get(i).toString());
 											}
 										} else if(jvalstr != null) {
 											value.add(jvalstr);
 										} else if(jvaldbl != null) {
 											value.add(jvaldbl.toString());
 										} else if(jvallng != null) {
 											value.add(jvallng.toString());
 										}							
 
 										settings.put(skey, value);
 									
 									} catch (JSONException e) {
 							            Log.printf("The received config is of an incorrent format");
 									}
 								}
 							}
 							device.setSettings(settings);
 							devices.put(dkey, device);
 							
 						} catch (JSONException e) {
 				            Log.printf("The received config is of an incorrent format");
 				        }
 
 					}
 				}
 				location.setDevices(devices);
 				config.put(lkey, location);
 
 			} catch (JSONException e) {
 	            Log.printf("The received config is of an incorrent format");
 	        }
 		}
 	}
 	
 	public static void print() {
 		int i = 0;
 		
 		for(Map.Entry<String, Location> lentry : config.entrySet()) {
 
 			Location location = (Location)lentry.getValue();
 			System.out.println(lentry.getKey()+" "+location.getName());
 
 			for(Map.Entry<String, Device> dentry : location.getDevices().entrySet()) {
 
 				Device device = (Device)dentry.getValue();
 				System.out.println("\t-"+dentry.getKey()+" "+device.getName());
 				
 				for(Map.Entry<String, ArrayList<String>> sentry : device.getSettings().entrySet()) {
 					ArrayList<String> val = (ArrayList<String>)sentry.getValue();
 					System.out.print("\t\t*"+sentry.getKey()+" ");
 					for(i=0; i<val.size(); i++) {
 						System.out.print(val.get(i).toString()+" ");
 					}
 					System.out.println();
 				}
 			}
 		}
 	}
 }
