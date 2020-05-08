 package com.stormdev.minigamez.utils;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 import com.stormdev.minigamez.main.minigamez;
 
 public class Arena implements Serializable {
 	private static final long serialVersionUID = -4717148066333846981L;
 	private SerializableLocation center;
 	private int radius = 0;
 	private ArenaShape shape = ArenaShape.INVALID;
 	private ArenaType type = ArenaType.INAVLID;
 	private int playerLimit = 0;
 	private Boolean transitioning = false;
 	private List<String> players = new ArrayList<String>();
 	private HashMap<String, Option> options = new HashMap<String, Option>();
 	public String game;
 public Arena(Location center, int radius, ArenaShape shape, ArenaType type, int playerLimit, String game){
 	this.center = minigamez.plugin.invalidLoc;
 	if(center != null){
 	this.center = new SerializableLocation(center);
 	}
 	this.radius = radius;
 	if(shape != null){
 	this.shape = shape;
 	}
 	if(type != null){
 	this.type = type;
 	}
 	this.playerLimit = playerLimit;
	
 }
 public HashMap<String, Option> getOptions(){
 	return this.options;
 }
 public Option getOption(String key){
 	if(!this.options.containsKey(key)){
 		return null;
 	}
 	return this.options.get(key);
 }
 public Boolean validate(){
 	for(Option opt:this.options.values()){
 		if(opt.type == OptionType.INVALID){
 			return false;
 		}
 		if(opt.type == OptionType.COMPULSARY && opt.val == null){
 			return false;
 		}
 	}
 	return true;
 }
 public Boolean setOption(String key, Option val){
 	if(val.type == OptionType.INVALID || val.type == OptionType.GAME){
 		return false;
 	}
 	this.options.put(key, val);
 	return true;
 }
 public void setTransitioning(Boolean trans){
 	this.transitioning = trans;
 	return;
 }
 public Boolean getTransitioning(){
 	return this.transitioning;
 }
 public Boolean isValid(){
 	if(this.center.equals(minigamez.plugin.invalidLoc)){
 		return false;
 	}
 	if(this.radius < 1){
 		return false;
 	}
 	if(this.shape == ArenaShape.INVALID){
 		return false;
 	}
 	if(this.type == ArenaType.INAVLID){
 		return false;
 	}
 	if(this.playerLimit < 1){
 		return false;
 	}
 	return true;
 }
 public Boolean addPlayer(String name){
 	if((this.players.size() + 1) <= this.playerLimit){
 		this.players.add(name);
 		return true;
 	}
 	else{
 	return false;
 	}
 }
 public int getPlayerLimit(){
 	return this.playerLimit;
 }
 public void removePlayer(String name){
 	this.players.remove(name);
 	return;
 }
 public void validatePlayers(){
 	for(String pname:this.players){
 		if(minigamez.plugin.getServer().getPlayer(pname) == null){
 			this.players.remove(pname);
 		}
 	}
 	return;
 }
 public Boolean isPlayerInArena(String name){
 	for(String pname:this.players){
 		if(minigamez.plugin.getServer().getPlayer(pname) == null){
 			this.players.remove(pname);
 		}
 	}
 	return this.players.contains(name);
 }
 public List<String> getPlayers(){
 	for(String pname:this.players){
 		if(minigamez.plugin.getServer().getPlayer(pname) == null){
 			this.players.remove(pname);
 		}
 	}
 	return this.players;
 }
 public int getHowManyPlayers(){
 	for(String pname:this.players){
 		if(minigamez.plugin.getServer().getPlayer(pname) == null && !(minigamez.plugin.getServer().getPlayer(pname).isOnline())){
 			this.players.remove(pname);
 		}
 	}
 	return this.players.size();
 }
 public void setCenter(Location loc){
 	this.center = new SerializableLocation(loc);
 	return;
 }
 public void setRadius(int radius){
 	this.radius = radius;
 	return;
 }
 public void setShape(ArenaShape shape){
 	this.shape = shape;
 	return;
 }
 public void setType(ArenaType type){
 	this.type = type;
 	return;
 }
 public Location getCenter(){
 	return this.center.getLocation(minigamez.plugin.getServer());
 }
 public Integer getRadius(){
 	return this.radius;
 }
 public ArenaShape getShape(){
 	return this.shape;
 }
 public ArenaType getType(){
 	return this.type;
 }
 public Boolean isLocInArena(Location check){
 	Location center = this.center.getLocation(minigamez.plugin.getServer());
 	if(center.getWorld().getName() != check.getWorld().getName()){
 		return false;
 	}
 	if(this.shape == ArenaShape.CIRCLE){
 		int radius = this.radius;
 		int radiusSquared = radius * radius;
 		 if(check.getY() < (center.getY() - 2)){
 			 return false;
 		 }
 		for(int x = -radius; x <= radius; x++) {
 		    for(int z = -radius; z <= radius; z++) {
 		        if( (x*x) + (z*z) <= radiusSquared) {
 		            double locX = center.getX() + x;
 		            double locZ = center.getZ() + z;
 		            Location loc = new Location(check.getWorld(), locX, check.getY(), locZ);
 		            if(check.distance(loc) <= 1.1){
 		            	return true;
 		            }
 		        }
 		    }
 		}
 	return false;
 	}
 	else if(this.shape == ArenaShape.SQUARE){
 		if(check.getY() < (center.getY() - 2)){
 			 return false;
 		 }
 		double minX = (center.getX()-this.radius)-1;
 		double maxX = center.getX()+this.radius;
 		double minZ = (center.getZ()-this.radius)-1;
 		double maxZ = center.getZ()+this.radius;
 		double x = check.getX();
 		double z = check.getZ();
 		if(x >= minX && x <=maxX && z >= minZ && z <= maxZ){
 			return true;
 		}
 		return false;
 	}
 	return false;
 }
 public void setPlayerLimit(int limit){
 	this.playerLimit = limit;
 	return;
 }
 public void markArena(Material marker){
 	Location center = this.center.getLocation(minigamez.plugin.getServer());
 	if(this.shape == ArenaShape.CIRCLE){
 		int radius = this.radius;
 		int radiusSquared = radius * radius;
 		 
 		for(int x = -radius; x <= radius; x++) {
 		    for(int z = -radius; z <= radius; z++) {
 		        if( (x*x) + (z*z) <= radiusSquared) {
 		            double y = center.getY();
 		            double locX = center.getX() + x;
 		            double locZ = center.getZ() + z;
 		            World world = center.getWorld();
 		            Location block = new Location(world,locX,y,locZ);
 		            block.getBlock().setType(marker);
 		        }
 		    }
 		}
 		center.getBlock().setType(Material.GLOWSTONE);
 	}
 	else if(this.shape == ArenaShape.SQUARE){
 		int radius = this.radius;
 		for(int x=-radius; x <= radius; x++){
 			for(int z=-radius; z <= radius; z++){
 				double y = center.getY();
 	            double locX = center.getX() + x;
 	            double locZ = center.getZ() + z;
 	            World world = center.getWorld();
 	            Location block = new Location(world,locX,y,locZ);
 	            block.getBlock().setType(marker);
 			}
 		}
 		center.getBlock().setType(Material.GLOWSTONE);
 	}
 	return;
 }
 }
