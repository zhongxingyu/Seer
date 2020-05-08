 package com.github.earthiverse.stats;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class UserList {
 	private HashMap<String, Player> UserList = new HashMap<String, Player>();
 
 	private UserList() { }
 	
     private static class UserListHolder { 
         public static final UserList INSTANCE = new UserList();
     }
     
     public static UserList getInstance() {
         return UserListHolder.INSTANCE;
     }
 	
     // Holds the cache information per player
 	public class Player {
 		// We use "BlockName,BlockData" as the key for the following HashMaps.
 		public HashMap<String, Integer> BlocksDestroyed = new HashMap<String, Integer>();
 		public HashMap<String, Integer> BlocksPlaced = new HashMap<String, Integer>();
 		
 		int Experience;
 		
 		long Login;
 		long Logout;
 	}
 	
 	public Set<Entry<String, Player>> getPlayerSet() {
 		return this.UserList.entrySet();
 	}
 	
 	public void verifyPlayer(String player) {
 		addPlayer(player);
 	}
 	
 	public void addPlayer(String player) {
 		if(!this.UserList.containsKey(player)) {
 			this.UserList.put(player, new Player());
 		}
 	}
 	
 	public void removePlayer(String player) {
 		this.UserList.remove(player);
 	}
 	
 	public Set<Entry<String,Integer>> getBlocksDestroyedSet(String player) {
 		verifyPlayer(player);
 		return this.UserList.get(player).BlocksDestroyed.entrySet();
 	}
 	
 	public Set<Entry<String,Integer>> getBlocksPlacedSet(String player) {
 		verifyPlayer(player);
 		return this.UserList.get(player).BlocksPlaced.entrySet();
 	}
 	
 	public void updateBlocksDestroyed(String player, String stat, int data, int amount) {
 		HashMap<String, Integer> BlocksDestroyed = this.UserList.get(player).BlocksDestroyed;
 		String key = stat + "," + data;
 		
 		if(BlocksDestroyed.containsKey(key)) {
 			// Increase amount
 			BlocksDestroyed.put(key, BlocksDestroyed.get(key) + amount);
 		} else {
 			// Add initial amount
 			BlocksDestroyed.put(key, amount);
 		}
 	}
 	
 	public void updateBlocksPlaced(String player, String stat, int data, int amount) {
 		HashMap<String, Integer> BlocksPlaced = this.UserList.get(player).BlocksPlaced;
 		String key = stat + "," + data;
 		
 		if(BlocksPlaced.containsKey(key)) {
 			// Increase amount
 			BlocksPlaced.put(key, BlocksPlaced.get(key) + amount);
 		} else {
 			// Add initial amount
 			BlocksPlaced.put(key, amount);
 		}
 	}
 	
 	public void updateLogin(String player, Long time) {
 		this.UserList.get(player).Login = time;
 	}
 	
 	public void updateLogout(String player, Long time) {
 		this.UserList.get(player).Logout = time;
 	}
 	
 	public void updateExp(String player, int amount) {
 		this.UserList.get(player).Experience += amount;
 	}
 }
