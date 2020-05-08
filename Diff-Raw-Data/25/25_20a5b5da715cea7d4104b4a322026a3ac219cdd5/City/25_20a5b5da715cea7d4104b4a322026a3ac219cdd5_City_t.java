 package Lihad.Conflict;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 
 import Lihad.Conflict.Information.BeyondInfo;
 import Lihad.Conflict.Perk.*;
 
 public class City extends Node {
 
     public City(String n) { 
         super(n); 
         
         // HACK HACK - Read this from conf, once it's in
         setRadius(500);
     }
 
 	Set<String> players = new HashSet<String>();
 	Map<String, Long> joins = new HashMap<String, Long>();
 	Location spawnLocation;
 	Location drifterLocation;
 	Set<String> mayors = new HashSet<String>();
 	
 	/**
 	 * The password to enter the city's chat channel.
 	 */
 	String password = "";
 	int bankBalance;
 	int spawnProtectRadius;
     
     //public Set<String> getPlayerList() { return players; }
 	
 	/**
 	 * Tests to see if a city has the player or not
 	 * @param playerName - Case insensitive.
 	 * @return boolean - true if player is in the city, false if not.
 	 */
     public boolean hasPlayer(String playerName)
     {
     	for (Iterator<String> iter = this.players.iterator(); iter.hasNext();) {
     		if (iter.next().equalsIgnoreCase(playerName)) {
     			return true;
     		}
     	}
     	return false;
     }
 
     public void addPlayer(String playerName) {
         // TODO: Remove from other cities
         players.add(playerName);
         joins.put(playerName, System.currentTimeMillis());
     }
     public void removePlayer(String playerName) {
     	for (Iterator<String> iter = this.players.iterator(); iter.hasNext();) {
     		String found = iter.next();
     		if (found.equalsIgnoreCase(playerName)) {
     			iter.remove();
     		}
     	}
     	removeMayor(playerName);
     	joins.remove(playerName);
     }
     public int getPopulation() { return players.size(); }
     
     public Location getSpawn() { return spawnLocation; }
     public void setSpawn(Location l) { spawnLocation = l; }
     
     public Location getSpongeLocation() { return drifterLocation; }
     public void setSpongeLocation(Location l) { drifterLocation = l; }
     
     public Set<String> getMayors() { return mayors; }
     public void addMayor(String playerName) {
         // TODO: Make sure mayor is a member of city
         mayors.add(playerName);
     }
     public void removeMayor(String playerName) {
     	for (Iterator<String> iter = this.mayors.iterator(); iter.hasNext();) {
     		String found = iter.next();
     		if (found.equalsIgnoreCase(playerName)) {
     			iter.remove();
     		}
     	}	
     }
     
     /**
      * Gets the password to join the city's chat.
      * @return password
      */
     public String getPassword() {
 		return password;
 	}
 
     /**
      * Sets the password to join the city's chat.
      * @param password
      */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public int getMoney() { return bankBalance; }
     public void setMoney(int money) { bankBalance = money; }
     public void addMoney(int money) { bankBalance += money; }
     public void subtractMoney(int money) { bankBalance -= money; }
     
     public int getProtectionRadius() { return spawnProtectRadius; }
     public void setProtectionRadius(int r) { spawnProtectRadius = r;}
     
     @Override
     public boolean load(ConfigurationSection section) {
         
         players.clear();
         mayors.clear();
         
         ConfigurationSection members = section.getConfigurationSection("Members");
         Set<String> list = members.getKeys(false);
         if (list != null && !list.isEmpty()) {
 	        for (Iterator <String> iter = list.iterator(); iter.hasNext();) {
 	        	String playerName = iter.next();
 	        	players.add(playerName);
 	        	ConfigurationSection member = members.getConfigurationSection(playerName);
 	        	boolean isMayor = member.getBoolean("isMayor", false);
 	        	joins.put(playerName, member.getLong("joined"));
 	        	if (isMayor) {
 	        		mayors.add(playerName);
 	        	}
 	        }
         }
         
         center = BeyondInfo.toLocation(section, "Location");
         spawnLocation = BeyondInfo.toLocation(section, "Spawn");
         drifterLocation = BeyondInfo.toLocation(section, "Drifter");
 
         bankBalance = section.getInt("Worth");
         spawnProtectRadius = section.getInt("Protection");
         
         setPassword(section.getString("Password"));
 
         return super.load(section);
     }
     
     @Override
     public void save(ConfigurationSection section) {
 
         java.util.List<String> setAsList = null;
         setAsList = new java.util.ArrayList<String>(players);
 
         section.createSection("Members");
         
         ConfigurationSection members = section.getConfigurationSection("Members");
         for (Iterator <String> iter = setAsList.iterator(); iter.hasNext();) {
         	String playerName = iter.next();
         	Map <String, Object> map = new HashMap<String, Object>();
         	map.put("isMayor", mayors.contains(playerName));
         	map.put("joined", getJoinedTime(playerName));
         	members.createSection(playerName, map);
         }
         section.set("Location", BeyondInfo.toString(center));
         section.set("Spawn", BeyondInfo.toString(spawnLocation));
         section.set("Drifter", BeyondInfo.toString(drifterLocation));
         section.set("Worth", bankBalance);
         section.set("Protection", spawnProtectRadius);
         section.set("Password", getPassword());
         section.set("type", "City");
         
         super.save(section);
     }
     
     public void purgeInactivePlayers() {
 
         long now = java.lang.System.currentTimeMillis();
         
     	List<String> removeThese = new ArrayList<String>();
         for (java.util.Iterator<String> it = players.iterator(); it.hasNext();) {
             long lastseen = 0;
             String name = it.next();
             lastseen = Conflict.getPlayerLastSeenTime(name);
 
             long days = (now - lastseen) / 86400000;
             
             // Purge anyone who hasn't logged in the last four weeks
             if (days > 28) {
                 Conflict.info("Removing " + name + " from Abatton (last seen " + days + " days ago)");
     			removeThese.add(name);
             }            
         }
         
     	for (Iterator<String> iter = removeThese.iterator(); iter.hasNext();) {
     		String found = iter.next();
     		this.removePlayer(found);
     	}
     }
     
     @Override
     public String toString() {
     	return this.name;
     }
     
     
 	/**
 	 * Gets the system time at which the player joined the City.
 	 * @param playerName - The name of the player.
 	 * @return long - system time at last join.
 	 */
 	public Long getJoinedTime(String playerName) {
 		return this.joins.get(playerName);
 	}
 
 	public String getInfo(boolean listPlayers) {
 		String info = Conflict.HEADERCOLOR + "------" + Conflict.CITYCOLOR + this.name + Conflict.TEXTCOLOR + "("
 			+ Conflict.PLAYERCOLOR + players.size() + Conflict.TEXTCOLOR + ")" + Conflict.HEADERCOLOR + "------\n"
 			+ Conflict.MAYORCOLOR + mayors.size() + Conflict.TEXTCOLOR + " mayors: "
 			+ Conflict.MAYORCOLOR;
 		boolean firstOne = true;
 		for (Iterator<String> iter = mayors.iterator(); iter.hasNext();)
 		{
 			if (firstOne){
 				info += iter.next();
 				firstOne = false;
 			}
 			else
 				info += ", " + iter.next();
 		}
 		info += Conflict.TEXTCOLOR + "Mini-perks: " + Conflict.PERKCOLOR + Perk.getPerkNameList(getPerks(), false);
 		info += Conflict.TEXTCOLOR + "Treasury: " + Conflict.MONEYCOLOR + this.getMoney();
 		if (listPlayers)
 			info += Conflict.TEXTCOLOR + "Players (" + Conflict.PLAYERCOLOR + players.size() + Conflict.TEXTCOLOR + "): " + Conflict.PLAYERCOLOR + this.getFormattedPlayersList();
 		else
 			info += Conflict.TEXTCOLOR + "Players: " + Conflict.PLAYERCOLOR + players.size() + Conflict.TEXTCOLOR;
 		return info;
 	}
 
 	public String getFormattedPlayersList() {
 		boolean firstOne = true;
 		String info = "";
 		for (Iterator<String> iter = players.iterator(); iter.hasNext();)
 		{
 			if (firstOne)
 			{
 				info += iter.next();
 				firstOne = false;
 			}
 			else
 				info += ", " + iter.next();
 		}
 		return info;
 	}
 };
