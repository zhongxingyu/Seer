 package Lihad.Conflict;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 
 import Lihad.Conflict.Information.BeyondInfo;
 
 public class City extends Node {
 
     public City(String n) { 
         super(n); 
         
         // HACK HACK - Read this from conf, once it's in
         setRadius(500);
     }
 
 	Set<String> players = new HashSet<String>();
 	Location spawnLocation;
 	Location drifterLocation;
 	Set<String> mayors = new HashSet<String>();
 	Set<String> trades = new HashSet<String>();
 	Set<String> perks = new HashSet<String>();
 	
 	/**
 	 * The password to enter the city's chat channel.
 	 */
 	String password = "";
 	int bankBalance;
 	int spawnProtectRadius;
     
     //Set<PerkNode> ownedNodes = new HashSet<PerkNode>();
 
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
     }
     public void removePlayer(String playerName) {
     	List<String> removeThese = new ArrayList<String>();
     	for (Iterator<String> iter = this.players.iterator(); iter.hasNext();) {
     		String found = iter.next();
     		if (found.equalsIgnoreCase(playerName)) {
     			removeThese.add(found);
     		}
     	}//Ugh... concurrent modification exception made me have to build a list:
     	for (Iterator<String> iter = removeThese.iterator(); iter.hasNext();) {
     		String found = iter.next();
     		this.players.remove(found);
     	}
     	removeMayor(playerName);
 
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
     	List<String> removeThese = new ArrayList<String>();
     	for (Iterator<String> iter = this.mayors.iterator(); iter.hasNext();) {
     		String found = iter.next();
     		if (found.equalsIgnoreCase(playerName)) {
     			removeThese.add(found);
     		}
     	}//Ugh... concurrent modification exception made me have to build a list:
     	for (Iterator<String> iter = removeThese.iterator(); iter.hasNext();) {
     		String found = iter.next();
     		this.mayors.remove(found);
     	}    	
     }
     
     // public void addPerkNode(PerkNode p) { ownedNodes.add(p); }
     // public void clearPerkNodes() { ownedNodes.clear(); }
     
     public Set<String> getTrades() { return trades; }
     public void addTrade(String p) { trades.add(p); }
     public void clearTrades() { trades.clear(); }
 
     public Set<String> getPerks() { return perks; }
     public void addPerk(String p) { perks.add(p); }
     public void clearPerks() { perks.clear(); }
 
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
     
     public void loadConfig(org.bukkit.configuration.ConfigurationSection section) {
         
         players.clear();
         players.addAll(section.getStringList("Players"));
         
         ConfigurationSection members = section.getConfigurationSection("Members");
         int spewed = 0;
         for (Iterator <String> iter = members.getKeys(false).iterator(); iter.hasNext();) {
         	String playerName = iter.next();
         	ConfigurationSection member = members.getConfigurationSection(playerName);
         	boolean isMayor = member.getBoolean("isMayor", true);//TODO: DO NOT DO THIS (just testing)
         	long lastSwitch = member.getLong("lastSwitch", -1);
         	if (spewed < 10) {
         		System.out.println("Member name: " + playerName);
         		System.out.println("isMayor: " + isMayor);
         		System.out.println("lastSwitch" + lastSwitch);
         	}
         }
         
         center = BeyondInfo.toLocation(section, "Location");
         spawnLocation = BeyondInfo.toLocation(section, "Spawn");
         drifterLocation = BeyondInfo.toLocation(section, "Drifter");
 
         mayors.clear();
         mayors.addAll(section.getStringList("Mayors"));
 
         trades.clear();
         trades.addAll(section.getStringList("Trades"));
 
         perks.clear();
         perks.addAll(section.getStringList("Perks"));
 
         bankBalance = section.getInt("Worth");
         spawnProtectRadius = section.getInt("Protection");
         
         setPassword(section.getString("Password"));
     }
     
     public void saveConfig(org.bukkit.configuration.ConfigurationSection section) {
 
         java.util.List<String> setAsList = null;
         setAsList = new java.util.ArrayList<String>(players);
 
         section.set("Players", setAsList);
         section.createSection("Members");
        
         ConfigurationSection members = section.getConfigurationSection("Members");
         for (Iterator <String> iter = setAsList.iterator(); iter.hasNext();) {
         	String playerName = iter.next();
         	Map <String, Object> map = new HashMap();
         	map.put("isMayor", false);
         	map.put("lastSwitch", (long) 0);
         	members.createSection(playerName, map);
         }
         section.set("Location", BeyondInfo.toString(center));
         section.set("Spawn", BeyondInfo.toString(spawnLocation));
         section.set("Drifter", BeyondInfo.toString(drifterLocation));
 
         setAsList = new java.util.ArrayList<String>(mayors);
         section.set("Mayors", setAsList);
 
         setAsList = new java.util.ArrayList<String>(trades);
         section.set("Trades", setAsList);
 
         setAsList = new java.util.ArrayList<String>(perks);
         section.set("Perks", setAsList);
 
         section.set("Worth", bankBalance);
         section.set("Protection", spawnProtectRadius);
         
         section.set("Password", getPassword());
     }
     
     public void purgeInactivePlayers() {
 
         long now = java.lang.System.currentTimeMillis();
     
         for (java.util.Iterator<String> it = players.iterator(); it.hasNext();) {
             long lastseen = 0;
             String name = it.next();
             lastseen = Conflict.getPlayerLastSeenTime(name);
 
             long days = (now - lastseen) / 86400000;
             
             // Purge anyone who hasn't logged in the last four weeks
             if (days > 28) {
                 Conflict.info("Removing " + name + " from Abatton (last seen " + days + " days ago)");
                 it.remove();
             }            
         }
     }
     
     @Override
     public String toString() {
     	return this.name;
     }
     
 };
