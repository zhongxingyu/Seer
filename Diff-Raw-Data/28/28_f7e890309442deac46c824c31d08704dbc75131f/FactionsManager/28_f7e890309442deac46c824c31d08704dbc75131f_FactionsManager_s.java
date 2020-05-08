 package io.snw.magicfurnace.factions;
 
 import io.snw.magicfurnace.MagicFurnace;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class FactionsManager {
 
 	private FactionsHook hook;
 
	public FactionsManager(MagicFurnace plugin) {
 		// Detect hook
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource("factions.txt")));
 			String line;
 			while((line = reader.readLine()) != null) {
 				if (!line.startsWith("#") && line.trim().length() > 1) {
 					String[] parts = line.split(" ");
 					if (parts.length < 2) {
 						plugin.getLogger().warning("Invalid hook: " + line);
 					} else {
 						String internal = parts[0];
 						String fac = parts[1];
 						try {
 							Class<?> clazz = Class.forName(internal);
							try {
								Class.forName(fac);
								// We have a factions hook at this point.

 								try {
 									Object o = clazz.newInstance();
 									if (o instanceof FactionsHook) {
 										this.hook = (FactionsHook) o;
 										break;
 									} else {
 										plugin.getLogger().warning("Invalid hook (found classes, not a hook): " + line);
 									}
 								} catch(InstantiationException e) {
 									e.printStackTrace();
 								} catch(IllegalAccessException e) {
 									e.printStackTrace();
 								}
							} catch(ClassNotFoundException e2) {
								// Ignore this error - Just means they don't have this version of Factions
 							}
 						} catch(ClassNotFoundException e) {
 							plugin.getLogger().warning("Invalid hook (internal class not found): " + line);
 						}
 					}
 				}
 			}
 			reader.close();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public FactionsHook getFactions() {
 		return hook;
 	}
 
 }
