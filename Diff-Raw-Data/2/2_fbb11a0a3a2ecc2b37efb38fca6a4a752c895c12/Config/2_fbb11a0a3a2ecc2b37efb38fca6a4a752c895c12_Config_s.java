 package com.ekstemicraft.plugin.ecsync;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Config {
 	
 
 	public ECSync plugin;
 
 	public Map<String, Object> simpleSyncronizationGroupMap = new HashMap<String, Object>();
 	
 	public Config(ECSync plugin){
 	load();	
 	this.plugin = plugin;
 	}
 
 	public String getGroupNamebyGroupID(String groupID){
 		return (String)simpleSyncronizationGroupMap.get(groupID);
 		
		//gets groupname by Group ID from Configg
 	}
 	
 	public String getWebappGroupIDbyGroupName(String groupName)
 	{
 		for (Entry<String, Object> entry: simpleSyncronizationGroupMap.entrySet())
 		{
 			if (groupName.equalsIgnoreCase((String)entry.getValue()))
 			{
 				return entry.getKey();
 				
 			}
 		}
     return null;
     
     //** Gets webapplications group ID from the config file.
 	}
 	
 	
 	public void load(){
 		simpleSyncronizationGroupMap = ECSync.main.getConfig().getConfigurationSection("groups.group-mapping").getValues(true);
 	//Loads group-map to HashMap upon initializion.
 	}
 }
