 package com.github.roburrito.bukkit.util;
 /*TODO: FIX IMPORT LIST*/
 import java.util.List;
 
 import com.github.roburrito.bukkit.godly.Godly;
import com.github.roburrito.bukkit.godly.listeners.FixedMetadataValue;
import com.github.roburrito.bukkit.godly.listeners.MetadataValue;
import com.github.roburrito.bukkit.godly.listeners.Player;
 
 public class FavorMetadataMethods {
 	
 	public String godlyNames[] = {"Arcamorde", "Terra", "Aegis", "Zi", "Raz", "Meta"};
 	public String godlyFavors[] = {"Arcamorde_Favor", "Terra_Favor", "Aegis_Favor", "Zi_Favor", "Raz_Favor", "Meta_Favor"};
 	private final Godly plugin;
 	
 	/*This method is for ease of access, so we don't have to constantly deal with large numbers.*/
 	public int getRank(Player player, String key) {
 		/*TODO: Map metadata into a sensible range of "ranks" for ease of access.*/
 		int exp = getMetadata(player, key);
 	}
 	
 	public void setMetadata(Player player, String key, int value){
 		player.setMetadata(key, new FixedMetadataValue(plugin,value));
 	}
 		
 	public int getMetadata(Player player, String key){
 		/*The list is here because there may be multiple plugins that use the same key*/
 		List<MetadataValue> values = player.getMetadata(key);  
 		for(MetadataValue value : values){
 			if(value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())){
 				return value.asInt();
 			}
 		}
 		/*This should only occur if the player does not have the corresponding favor.*/
 		return Integer.MIN_VALUE;
 	}
 
 }
