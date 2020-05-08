 package com.mcnsa.instanceportals.managers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import com.mcnsa.instanceportals.InstancePortals;
 import com.mcnsa.instanceportals.containers.Instance;
 import com.mcnsa.instanceportals.containers.InstanceSet;
 import com.mcnsa.instanceportals.containers.Portal;
 import com.mcnsa.instanceportals.containers.PortalRegion;
 
 @SuppressWarnings("unchecked")
 public class PersistanceManager {
 	private InstancePortals plugin = null;
 	
 	public PersistanceManager(InstancePortals instance) {
 		plugin = instance;
 	}
 	
 	public void writePersistance() {
 		FileWriter outFile;
 		try {
 			// make sure the data folder exists
 			File dataFolder = new File(plugin.getDataFolder().toString());
 			if(!dataFolder.exists()) {
 				dataFolder.mkdirs();
 			}
 			
 			outFile = new FileWriter(plugin.getDataFolder().toString() + "/persist.json");
 			PrintWriter out = new PrintWriter(outFile);
 			
 			JSONObject obj = new JSONObject();
 			
 			// start storing!
 			// store portals first
 			JSONObject portalsObj = new JSONObject();
 			for(int i = 0; i < plugin.transportManager.portals.size(); i++) {
 				JSONObject portalObj = new JSONObject();
 				
 				// the entrance coords
 				JSONArray entranceMin = new JSONArray();
 				entranceMin.add(plugin.transportManager.portals.get(i).entrance.min.getBlockX());
 				entranceMin.add(plugin.transportManager.portals.get(i).entrance.min.getBlockY());
 				entranceMin.add(plugin.transportManager.portals.get(i).entrance.min.getBlockZ());
 				JSONArray entranceMax = new JSONArray();
 				entranceMax.add(plugin.transportManager.portals.get(i).entrance.max.getBlockX());
 				entranceMax.add(plugin.transportManager.portals.get(i).entrance.max.getBlockY());
 				entranceMax.add(plugin.transportManager.portals.get(i).entrance.max.getBlockZ());
 				
 				// and now the exit
 				JSONArray exit = new JSONArray();
 				exit.add(plugin.transportManager.portals.get(i).exit.getX());
 				exit.add(plugin.transportManager.portals.get(i).exit.getY());
 				exit.add(plugin.transportManager.portals.get(i).exit.getZ());
 				exit.add(plugin.transportManager.portals.get(i).exit.getYaw());
 				exit.add(plugin.transportManager.portals.get(i).exit.getPitch());
 
 				portalObj.put("world", plugin.transportManager.portals.get(i).entrance.worldName);
 				portalObj.put("min", entranceMin);
 				portalObj.put("max", entranceMax);
 				portalObj.put("exit", exit);
 				
 				portalsObj.put(plugin.transportManager.portals.get(i).name, portalObj);
 			}
 			
 			// now store instance sets!
 			JSONObject instanceSetsObj = new JSONObject();
 			for(int i = 0; i < plugin.transportManager.instanceSets.size(); i++) {
 				JSONObject instanceSetObj = new JSONObject();
 
 				// the entrance coords
 				JSONArray entranceMin = new JSONArray();
 				entranceMin.add(plugin.transportManager.instanceSets.get(i).entrance.min.getBlockX());
 				entranceMin.add(plugin.transportManager.instanceSets.get(i).entrance.min.getBlockY());
 				entranceMin.add(plugin.transportManager.instanceSets.get(i).entrance.min.getBlockZ());
 				JSONArray entranceMax = new JSONArray();
 				entranceMax.add(plugin.transportManager.instanceSets.get(i).entrance.max.getBlockX());
 				entranceMax.add(plugin.transportManager.instanceSets.get(i).entrance.max.getBlockY());
 				entranceMax.add(plugin.transportManager.instanceSets.get(i).entrance.max.getBlockZ());
 				
 				// and now the exit
 				JSONArray exit = new JSONArray();
 				exit.add(plugin.transportManager.instanceSets.get(i).exit.getX());
 				exit.add(plugin.transportManager.instanceSets.get(i).exit.getY());
 				exit.add(plugin.transportManager.instanceSets.get(i).exit.getZ());
 				exit.add(plugin.transportManager.instanceSets.get(i).exit.getYaw());
 				exit.add(plugin.transportManager.instanceSets.get(i).exit.getPitch());
 				
 				// the boot exit
 				JSONArray bootEntrance = new JSONArray();
 				bootEntrance.add(plugin.transportManager.instanceSets.get(i).bootEntrance.getX());
 				bootEntrance.add(plugin.transportManager.instanceSets.get(i).bootEntrance.getY());
 				bootEntrance.add(plugin.transportManager.instanceSets.get(i).bootEntrance.getZ());
 				bootEntrance.add(plugin.transportManager.instanceSets.get(i).bootEntrance.getYaw());
 				bootEntrance.add(plugin.transportManager.instanceSets.get(i).bootEntrance.getPitch());
 				
 				// now the instances
 				JSONArray instances = new JSONArray();
 				for(int j = 0; j < plugin.transportManager.instanceSets.get(i).instances.size(); j++) {
 					JSONObject instance = new JSONObject();
 					
 					// the departure coords
 					JSONArray instanceEntranceMin = new JSONArray();
 					instanceEntranceMin.add(plugin.transportManager.instanceSets.get(i).instances.get(j).departure.min.getBlockX());
 					instanceEntranceMin.add(plugin.transportManager.instanceSets.get(i).instances.get(j).departure.min.getBlockY());
 					instanceEntranceMin.add(plugin.transportManager.instanceSets.get(i).instances.get(j).departure.min.getBlockZ());
 					JSONArray instanceEntranceMax = new JSONArray();
 					instanceEntranceMax.add(plugin.transportManager.instanceSets.get(i).instances.get(j).departure.max.getBlockX());
 					instanceEntranceMax.add(plugin.transportManager.instanceSets.get(i).instances.get(j).departure.max.getBlockY());
 					instanceEntranceMax.add(plugin.transportManager.instanceSets.get(i).instances.get(j).departure.max.getBlockZ());
 					
 					// the container coords
 					JSONArray instanceContainerMin = new JSONArray();
 					instanceContainerMin.add(plugin.transportManager.instanceSets.get(i).instances.get(j).container.min.getBlockX());
 					instanceContainerMin.add(plugin.transportManager.instanceSets.get(i).instances.get(j).container.min.getBlockY());
 					instanceContainerMin.add(plugin.transportManager.instanceSets.get(i).instances.get(j).container.min.getBlockZ());
 					JSONArray instanceContainerMax = new JSONArray();
 					instanceContainerMax.add(plugin.transportManager.instanceSets.get(i).instances.get(j).container.max.getBlockX());
 					instanceContainerMax.add(plugin.transportManager.instanceSets.get(i).instances.get(j).container.max.getBlockY());
 					instanceContainerMax.add(plugin.transportManager.instanceSets.get(i).instances.get(j).container.max.getBlockZ());
 					
 					// and now the exit
 					JSONArray instanceArrival = new JSONArray();
 					instanceArrival.add(plugin.transportManager.instanceSets.get(i).instances.get(j).arrival.getX());
 					instanceArrival.add(plugin.transportManager.instanceSets.get(i).instances.get(j).arrival.getY());
 					instanceArrival.add(plugin.transportManager.instanceSets.get(i).instances.get(j).arrival.getZ());
 					instanceArrival.add(plugin.transportManager.instanceSets.get(i).instances.get(j).arrival.getYaw());
 					instanceArrival.add(plugin.transportManager.instanceSets.get(i).instances.get(j).arrival.getPitch());
 					
 					// set up the instance
 					instance.put("min", instanceEntranceMin);
 					instance.put("max", instanceEntranceMax);
 					instance.put("containerMin", instanceContainerMin);
 					instance.put("containerMax", instanceContainerMax);
 					instance.put("arrival", instanceArrival);
 					
 					// and add it to the list
 					instances.add(instance);
 				}
 				
 				// now add everything to the instance set
 				//instanceSetObj.put("world", plugin.transportManager.portals.get(i).entrance.worldName);
 				instanceSetObj.put("min", entranceMin);
 				instanceSetObj.put("max", entranceMax);
 				instanceSetObj.put("exit", exit);
 				instanceSetObj.put("bootEntrance", bootEntrance);
 				instanceSetObj.put("instances", instances);
 				instanceSetObj.put("maxPlayers", plugin.transportManager.instanceSets.get(i).maxPlayers);
 				
 				// and the object!
 				instanceSetsObj.put(plugin.transportManager.instanceSets.get(i).name, instanceSetObj);
 			}
 			
 			// and add everything to the head node
 			obj.put("portals", portalsObj);
 			obj.put("instanceSets", instanceSetsObj);
 			
 			// and save it!
 			out.print(obj);			
 			out.close();
 		} catch (IOException e) {
 			plugin.error("failed to write persistance: " + e.getMessage());
 		}
 	}
 	
 	public void readPersistance() {
 		try {
 			// load the file
 			String lineSep = System.getProperty("line.separator");
 			FileInputStream fin = new FileInputStream(plugin.getDataFolder().toString() + "/persist.json");
 			BufferedReader input = new BufferedReader(new InputStreamReader(fin));
 			String nextLine = "";
 			StringBuffer sb = new StringBuffer();
 			while((nextLine = input.readLine()) != null) {
 				sb.append(nextLine);
 				sb.append(lineSep);
 			}
 			
 			// and parse it!
 			Map<String, Object> obj = (HashMap<String, Object>)JSONValue.parse(sb.toString());
 			
 			// and grab the objects!
 			if(obj != null) {
 				// grab the portals
 				HashMap<String, HashMap<String, Object>> portals = (HashMap<String, HashMap<String, Object>>)obj.get("portals");
 				// iterate over all the portals
 				for(String portalName: portals.keySet()) {
 					try {
 						Portal portal = new Portal(plugin, portalName);
 
 						String worldName = (String)portals.get(portalName).get("world");
 						ArrayList<Long> minList = (ArrayList<Long>)portals.get(portalName).get("min");
 						ArrayList<Long> maxList = (ArrayList<Long>)portals.get(portalName).get("max");
 						ArrayList<Double> exitList = (ArrayList<Double>)portals.get(portalName).get("exit");
 	
 						Location min = new Location(plugin.getServer().getWorld(worldName), minList.get(0).doubleValue(), minList.get(1).doubleValue(), minList.get(2).doubleValue());
 						Location max = new Location(plugin.getServer().getWorld(worldName), maxList.get(0).doubleValue(), maxList.get(1).doubleValue(), maxList.get(2).doubleValue());
 						Location exit = new Location(plugin.getServer().getWorld(worldName), exitList.get(0), exitList.get(1), exitList.get(2), exitList.get(3).floatValue(), (float)exitList.get(4).floatValue());
 						
 						// ok, create the portal region
 						PortalRegion region = new PortalRegion(plugin, worldName, min, max);
 						
 						// now set things in the portal
 						portal.entrance = region;
 						portal.exit = exit;
 						
 						// and track the portal!
 						plugin.transportManager.addPortal(portal);
 					}
 					catch(Exception e) {
 						plugin.error("failed to read portal: " + e.getMessage());
 						e.printStackTrace();
 					}
 				}
 				
 				// now grab the instance sets
 				HashMap<String, HashMap<String, Object>> instanceSets = (HashMap<String, HashMap<String, Object>>)obj.get("instanceSets");
 				// iterate over all the instance sets
 				for(String instanceSetName: instanceSets.keySet()) {
 					try {
 						// create an instance set
 						InstanceSet instanceSet = new InstanceSet(plugin, instanceSetName);
 						
 						// get the portal and exit locations
 						ArrayList<Long> minList = (ArrayList<Long>)instanceSets.get(instanceSetName).get("min");
 						ArrayList<Long> maxList = (ArrayList<Long>)instanceSets.get(instanceSetName).get("max");
 						ArrayList<Double> exitList = (ArrayList<Double>)instanceSets.get(instanceSetName).get("exit");
 						ArrayList<Double> bootEntranceList = (ArrayList<Double>)instanceSets.get(instanceSetName).get("bootEntrance");
 	
 						// TODO: fix the world names
 						Location min = new Location(plugin.getServer().getWorld("world"), minList.get(0).doubleValue(), minList.get(1).doubleValue(), minList.get(2).doubleValue());
 						Location max = new Location(plugin.getServer().getWorld("world"), maxList.get(0).doubleValue(), maxList.get(1).doubleValue(), maxList.get(2).doubleValue());
 						Location exit = new Location(plugin.getServer().getWorld("world"), exitList.get(0), exitList.get(1), exitList.get(2), exitList.get(3).floatValue(), (float)exitList.get(4).floatValue());
 						Location bootEntrance = new Location(plugin.getServer().getWorld("world"), bootEntranceList.get(0), bootEntranceList.get(1), bootEntranceList.get(2), bootEntranceList.get(3).floatValue(), (float)bootEntranceList.get(4).floatValue());
 						
 						// ok, create the portal region
 						PortalRegion region = new PortalRegion(plugin, "world", min, max);
 						
 						// now set things in the portal
 						instanceSet.entrance = region;
 						instanceSet.exit = exit;
 						instanceSet.bootEntrance = bootEntrance;
 						
 						// now load all of the instances
 						ArrayList<Object> instanceList = (ArrayList<Object>)instanceSets.get(instanceSetName).get("instances");
 						for(int i = 0; i < instanceList.size(); i++) {
 							HashMap<String, Object> instanceData = (HashMap<String, Object>)instanceList.get(i);
 							
 							// ok, create an instance
 							Instance instance = new Instance(instanceSet);
 							
 							// get the portal and arrival locations
 							ArrayList<Long> instanceMinList = (ArrayList<Long>)instanceData.get("min");
 							ArrayList<Long> instanceMaxList = (ArrayList<Long>)instanceData.get("max");
 							ArrayList<Long> instanceContainerMinList = (ArrayList<Long>)instanceData.get("containerMin");
 							ArrayList<Long> instanceContainerMaxList = (ArrayList<Long>)instanceData.get("containerMax");
 							ArrayList<Double> arrivalList = (ArrayList<Double>)instanceData.get("arrival");
 		
 							// TODO: fix the world names
 							Location instanceMin = new Location(plugin.getServer().getWorld("world"), instanceMinList.get(0).doubleValue(), instanceMinList.get(1).doubleValue(), instanceMinList.get(2).doubleValue());
 							Location instanceMax = new Location(plugin.getServer().getWorld("world"), instanceMaxList.get(0).doubleValue(), instanceMaxList.get(1).doubleValue(), instanceMaxList.get(2).doubleValue());
 							Location instanceContainerMin = new Location(plugin.getServer().getWorld("world"), instanceContainerMinList.get(0).doubleValue(), instanceContainerMinList.get(1).doubleValue(), instanceContainerMinList.get(2).doubleValue());
 							Location instanceContainerMax = new Location(plugin.getServer().getWorld("world"), instanceContainerMaxList.get(0).doubleValue(), instanceContainerMaxList.get(1).doubleValue(), instanceContainerMaxList.get(2).doubleValue());
							Location instanceArrival = new Location(plugin.getServer().getWorld("world"), arrivalList.get(0), arrivalList.get(1), arrivalList.get(2), arrivalList.get(3).floatValue(), (float)arrivalList.get(4).floatValue());
 
 							// ok, create the portal region
 							PortalRegion instanceRegion = new PortalRegion(plugin, "world", instanceMin, instanceMax);
 							PortalRegion instanceContainerRegion = new PortalRegion(plugin, "world", instanceContainerMin, instanceContainerMax);
 							
 							// now setup the instance
 							instance.setArrival(instanceArrival);
 							instance.setDeparture(instanceRegion);
 							instance.setContainer(instanceContainerRegion);
 							
 							// now add it to the instance set
 							instanceSet.addInstance(instance);
 						}
 						
 						// and track the instance set!
 						plugin.transportManager.addInstanceSet(instanceSet);
 					}
 					catch(Exception e) {
 						plugin.error("failed to read instance set: " + e.getMessage());
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		catch(Exception e) {
 			plugin.error("failed to read persistance: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
