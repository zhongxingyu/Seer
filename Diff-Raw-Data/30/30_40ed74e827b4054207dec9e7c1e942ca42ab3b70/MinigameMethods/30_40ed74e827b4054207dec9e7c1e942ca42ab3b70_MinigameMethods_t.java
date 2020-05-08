 package com.amazar.utils;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
 
 import com.amazar.plugin.ac;
 
 public class MinigameMethods {
 	private ac plugin = null;
 	public MinigameMethods(){
 		this.plugin = ac.plugin;
 	}
     public Minigame inAGame(String playername){
     	HashMap<String, Minigame> games = plugin.gameScheduler.getGames();
     	Set<String> keys = games.keySet();
     	Boolean inAGame = false;
         Minigame mgame = null;
     	for(String key:keys){
     		Minigame game = games.get(key);
     		if(game.getPlayers().contains(playername)){
     			inAGame = true;
     			mgame = game;
     		}
     	}
     	if(inAGame){
     	return mgame;
     	}
     	return null;
     }
     public String inGameQue(String playername){
     	Set<String> arenaNames = plugin.minigamesArenas.getArenas();
     	for(String arenaName:arenaNames){
     		List<String> que = plugin.gameScheduler.getQue(plugin.minigamesArenas.getArena(arenaName));
     		if(que.contains(playername)){
     			return arenaName;
     		}
     	}
     	return null;
     }
     public String isArena(Location loc){
     	Set<String> arenaNames = plugin.minigamesArenas.getArenas();
     	String name = null;
     	for(String arenaName:arenaNames){
     	Arena arena = plugin.minigamesArenas.getArena(arenaName);
     	if(arena.isLocInArena(loc)){
     		name = arenaName;
     	}
     	}
     	return name;
     }
    public void purgeMobs(Arena arena){
		Entity ent = arena.getCenter().getWorld().spawnEntity(arena.getCenter(), EntityType.ARROW);
		List<Entity> ents = ent.getNearbyEntities((arena.getRadius() + 1), (arena.getRadius()+1), (arena.getRadius()+1));
		for(Entity e:ents){
			if(e.getType() != EntityType.PLAYER || e.getType() != EntityType.WEATHER){
				e.remove();
			}
		}
		ent.remove();
		return;
	}
 }
