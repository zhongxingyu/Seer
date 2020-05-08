 package com.amazar.utils;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 
 import com.amazar.plugin.ac;
 
 public class ArenaSurvival extends Arena {
 
 	private int countDown = 50;
 	private Boolean doCountDown = false;
 	private SerializableLocation playerSpawn;
 	private SerializableLocation enemySpawn;
 	private String[] items = {"0"};
 	public ArenaSurvival(Location center, int radius, ArenaShape shape,
 			ArenaType type, int countDown, Boolean doCountdown, Location playerSpawn, Location enemySpawn, String[] items, int playerLimit) {
 		super(center, radius, shape, type, playerLimit);
 		this.doCountDown = doCountdown;
 		this.countDown = countDown;
 		this.playerSpawn = ac.plugin.invalidLoc;
 		this.enemySpawn = ac.plugin.invalidLoc;
 		if(playerSpawn != null){
 		this.playerSpawn = new SerializableLocation(playerSpawn);
 		}
 		if(enemySpawn != null){
 		this.enemySpawn = new SerializableLocation(enemySpawn);
 		}
 		if(items != null){
 		this.items = items;
 		}
 	}
 	public Boolean isValid(){
 		if(this.playerSpawn.equals(ac.plugin.invalidLoc) || this.enemySpawn.equals(ac.plugin.invalidLoc)){
 			return false;
 		}
 		return true;
 	}
 	public void setDoCountdown(Boolean doCountdown){
 		this.doCountDown = doCountdown;
 		return;
 	}
 	public void setCountdown(int startPoint){
 		this.countDown = startPoint;
 		return;
 	}
 	public void setPlayerSpawn(Location loc){
 		this.playerSpawn = new SerializableLocation(loc);
 		return;
 	}
 	public void setEnemySpawn(Location loc){
 		this.enemySpawn = new SerializableLocation(loc);
 		return;
 	}
 	public void setItems(String[] ids){
 		return;
 	}
 	public Boolean getDoCountdown(){
 		return this.doCountDown;
 	}
 	public Integer getCountdownStartPoint(){
 		return this.countDown;
 	}
 	public Location getPlayerSpawnpoint(){
 		return this.playerSpawn.getLocation(ac.plugin.getServer());
 	}
 	public Location getEnemySpawnpoint(){
 		return this.enemySpawn.getLocation(ac.plugin.getServer());
 	}
 	public String[] getItems(){
 		return this.items;
 	}
 }
