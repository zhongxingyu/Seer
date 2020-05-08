 package me.tehbeard.BeardStat.containers;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.DataProviders.IStatDataProvider;
 import net.dragonzone.promise.Delegate;
 import net.dragonzone.promise.Promise;
 
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Provides a cache between backend storage and the stats plugin
  * @author James
  *
  */
 public class PlayerStatManager implements CommandExecutor {
 
 	private HashMap<String,Promise<PlayerStatBlob>> cache = new HashMap<String,Promise<PlayerStatBlob>>();
 	private IStatDataProvider backendDatabase = null;
 
 
 	public PlayerStatManager(IStatDataProvider database){
 		backendDatabase = database;
 	}
 
 
 	/**
 	 * Force save of all cached stats to backend storage
 	 */
 	public void saveCache(){
 		if(backendDatabase == null){return;}
 		Iterator<Entry<String, Promise<PlayerStatBlob>>> i = cache.entrySet().iterator();
 
 		while(i.hasNext()){
 			Entry<String, Promise<PlayerStatBlob>> entry = i.next();
 			String player = entry.getKey();
			
			//check if rejected promise, remove from cache silently
			if(entry.getValue().isRejected()){
				BeardStat.printDebugCon("Promise[" + player + "] rejected, removing from cache");//alert debug dump
				i.remove();//clear it out
				continue;//Skip now
			}
			
			
			//skip if not resolved
 			if(!entry.getValue().isResolved()){
 				continue;
 			}
 
 			int seconds = getSessionTime(player);
 
 			BeardStat.printDebugCon("saving time: [Player : " + player +" ] time: " +seconds);
 			if(entry.getValue().getValue() != null){
 				entry.getValue().getValue().getStat("stats","playedfor").incrementStat(seconds);
 
 				backendDatabase.pushPlayerStatBlob(entry.getValue().getValue());
 
 				if(isPlayerOnline(player)){
 					setLoginTime(player,System.currentTimeMillis());
 				}
 				else
 				{
 					wipeLoginTime(player);
 					i.remove();
 				}
 			}
 			else
 			{
 				//Nulled player data
 				BeardStat.printCon("ALERT! NULL RECORD FOR " + player);
 				i.remove();
 			}
 
 
 
 		}
 
 
 	}
 
 
 
 	private boolean isPlayerOnline(String player) {
 		for(Player p : Bukkit.getOnlinePlayers()){
 			if(p.getName().equals(player)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	/**
 	 * Retrieve a players Stat Blob, or create one if it doesn't exist
 	 * @param name
 	 * @return
 	 */
 	public Promise<PlayerStatBlob> getPlayerBlobASync(String name){
 		if(backendDatabase == null){return null;}
 		if(!cache.containsKey(name)){
 			cache.put(name,backendDatabase.pullPlayerStatBlob(name));
 		}
 		return cache.get(name);
 	}
 
 	public PlayerStatBlob getPlayerBlob(String name){
 		return getPlayerBlobASync(name).getValue();
 	}
 
 	/**
 	 * Finds a player's stat blob, but does not try to make it
 	 * @param name player to find
 	 * @return The player's stat blob or a null if not found
 	 */
 	public Promise<PlayerStatBlob> findPlayerBlobASync(final String name){
 		if(backendDatabase == null){return null;}
 		if(!cache.containsKey(name)){
 			Promise<PlayerStatBlob> pbs = backendDatabase.pullPlayerStatBlob(name,false);
 			pbs.onResolve(new Delegate<Void, Promise<PlayerStatBlob>>() {
 
 				public <P extends Promise<PlayerStatBlob>> Void invoke(P params) {
 					cache.put(name, params);
 					return null;
 				}
 			});
 			return pbs;
 		}
 		return cache.get(name);
 	}
 
 	public PlayerStatBlob findPlayerBlob(String name){
 		return findPlayerBlobASync(name).getValue();
 	}
 
 	public void flush(){
 
 		backendDatabase.flush();
 	}
 
 
 
 	private HashMap<String,Long> loginTimes = new HashMap<String, Long>();
 
 	/**
 	 * Returns length of current session in memory
 	 * @param player
 	 * @return
 	 */
 	public int getSessionTime(String player){
 		if(loginTimes.containsKey(player)){
 			return Integer.parseInt("" + ((System.currentTimeMillis()  - loginTimes.get(player))/1000L));
 
 		}
 		return 0;
 	}
 
 	public Long getLoginTime(String player){
 		if(!loginTimes.containsKey(player)){
 			setLoginTime(player,System.currentTimeMillis());
 		}
 		return loginTimes.get(player);
 
 	}
 
 	public void setLoginTime(String player,long time){
 		loginTimes.put(player,time);
 
 	}
 
 	public void wipeLoginTime(String player){
 		loginTimes.remove(player);
 	}
 
 
 	public boolean onCommand(CommandSender sender, Command cmd, String lbl,
 			String[] args) {
 		Iterator<Entry<String, Promise<PlayerStatBlob>>> i = cache.entrySet().iterator();
 		sender.sendMessage("Players in Stat cache");
 		while(i.hasNext()){
 			Entry<String, Promise<PlayerStatBlob>> entry = i.next();
 			String player = entry.getKey();
 			sender.sendMessage(ChatColor.GOLD + player);
 		}
 
 		Iterator<String> ii = loginTimes.keySet().iterator();
 		sender.sendMessage("Players in login cache");
 		while(ii.hasNext()){
 			String player = ii.next();
 			sender.sendMessage(ChatColor.GOLD + player);
 		}
 		return true;
 	}
 
 	public boolean deletePlayer(String player){
 		if(!backendDatabase.hasStatBlob(player)){
 			return false;
 		}
 		cache.remove(player);
 		backendDatabase.deletePlayerStatBlob(player);
 		return true;
 	}
 
 }
