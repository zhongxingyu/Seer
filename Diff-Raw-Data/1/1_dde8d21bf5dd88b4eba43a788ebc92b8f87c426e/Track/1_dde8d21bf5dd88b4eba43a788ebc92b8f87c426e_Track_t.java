 package de.hydrox.bukkit.DroxPerms.data.flatfile;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.util.config.ConfigurationNode;
 
 public class Track {
 	private static HashMap<String, Track> tracks = new HashMap<String, Track>();
 
 	private String name;
 	private HashMap<String, String> mapping;
 
 	public Track(String name, ConfigurationNode node) {
		mapping = new HashMap<String, String>();
 		Map<String, Object> tmp = node.getAll();
 		Set<String> keys = tmp.keySet();
 		for (String key : keys) {
 			mapping.put(key.toLowerCase(), ((String) tmp.get(key)).toLowerCase());
 		}
 	}
 	
 	public String getPromoteGroup(String before) {
 		return mapping.get(before.toLowerCase());
 	}
 	
 	public String getDemoteGroup(String before) {
 		Set<String> keys = mapping.keySet();
 		for (String key : keys) {
 			if (mapping.get(key).equalsIgnoreCase(before)) {
 				return key;
 			}
 		}
 		return null;
 	}
 	
 	public static boolean addTrack(Track track) {
 		if (existTrack(track.name.toLowerCase())) {
 			return false;
 		}
 		tracks.put(track.name.toLowerCase(), track);
 		return true;
 	}
 
 	public static boolean removeTrack(String name) {
 		if (existTrack(name.toLowerCase())) {
 			tracks.remove(name.toLowerCase());
 			return true;
 		}
 		return false;
 	}
 
 	public static Track getTrack(String name) {
 		return tracks.get(name.toLowerCase());
 	}
 
 	public static boolean existTrack(String name) {
 		if (tracks.containsKey(name.toLowerCase())) {
 			return true;
 		}
 		return false;
 	}
 
 	public static void clearTracks() {
 		tracks.clear();
 	}
 	
 	public static Iterator<Track> iter() {
 		return tracks.values().iterator();
 	}
 
 }
