 package com.cyberkitsune.prefixchat;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class ChatParties {
 	
 	private KitsuneChat plugin;
 	public ChatParties(KitsuneChat plugin) {
 		this.plugin = plugin;
 	}
 
 	public HashMap<Player, String> partyData = new HashMap<Player, String>();
 	
 	public void changeParty(Player target, String name) {
 		if(!partyData.containsKey(target))
 		{
 			partyData.put(target, name);
 			notifyParty(name, ChatColor.YELLOW+"[KitsuneChat] "+target.getDisplayName()+" has joined the party "+name);
 			Set<Player> channelPeople = getKeysByValue(partyData, name);
 			String memeberList = "";
 			for(Player plr : channelPeople)
 			{
 				memeberList = memeberList + plr.getDisplayName()+", ";
 			}
 			target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] "+channelPeople.size()+((channelPeople.size() == 1) ? " person " : " people ")+"in the party.");
 			target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] They are: "+memeberList);
 		} else {
 			target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] Changing party from "+partyData.get(target)+" to "+name);
 			notifyParty(partyData.get(target), ChatColor.YELLOW+"[KitsuneChat] "+target.getDisplayName()+" has left "+partyData.get(target));
 			partyData.remove(target);
 			partyData.put(target, name);
 			notifyParty(name, ChatColor.YELLOW+"[KitsuneChat] "+target.getDisplayName()+" has joined "+name);
 			Set<Player> channelPeople = getKeysByValue(partyData, name);
 			String memeberList = "";
 			for(Player plr : channelPeople)
 			{
 				memeberList = memeberList + plr.getDisplayName()+", ";
 			}
 			target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] "+channelPeople.size()+((channelPeople.size() == 1) ? " person " : " people ")+"in the party.");
 			target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] They are: "+memeberList+".");
 		}
 		plugin.dataFile.setUserParty(target, name);
 		
 	}
 	
 	public void notifyParty(String party, String message) {
 		Set<Player> channelMembers = getKeysByValue(partyData, party);
 		for(Player plr : channelMembers) {
 			plr.sendMessage(message);
 		}
 	}
 	
 	public Set<Player> getPartyMembers(String party) {
 		return getKeysByValue(partyData, party);
 	}
 	
 	public String getPartyName(Player target) {
 		return partyData.get(target);
 	}
 	
 	public boolean isInAParty(Player target) {
 		return partyData.containsKey(target);
 	}
 	
 	public void leaveParty(Player target, boolean disconnect) {
 		String party = getPartyName(target);
 		notifyParty(party, ChatColor.YELLOW+"[KitsuneChat] "+target.getDisplayName()+" has left "+party+".");
 		if(disconnect) {
 			plugin.dataFile.setUserParty(target, getPartyName(target));
 			partyData.remove(target);
 			
 		} else {
 			partyData.remove(target);
 			plugin.dataFile.setUserParty(target, "");
 		}
 	}
 	
 	public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
 	     Set<T> keys = new HashSet<T>();
 	     for (Entry<T, E> entry : map.entrySet()) {
 	         if (value.equals(entry.getValue())) {
 	             keys.add(entry.getKey());
 	         }
 	     }
 	     return keys;
 	}
 	
 }
