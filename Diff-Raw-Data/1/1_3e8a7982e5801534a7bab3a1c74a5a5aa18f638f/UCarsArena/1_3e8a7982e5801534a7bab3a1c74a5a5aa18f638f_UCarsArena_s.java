 package com.amazar.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 
 import com.amazar.plugin.ac;
 
 public class UCarsArena extends Arena {
     private List<SerializableLocation> grid = new ArrayList<SerializableLocation>();
     private List<SerializableLocation> line = new ArrayList<SerializableLocation>();
     private int laps = 1;
 	public UCarsArena(Location center, int radius, ArenaShape shape,
 			ArenaType type, int playerLimit) {
 		super(center, radius, shape, type, playerLimit);
 	}
 	public void check(){
 		this.setPlayerLimit(this.grid.size());
 		return;
 	}
 	public void setGridPosition(int pos, Location loc){
 		SerializableLocation newLoc = new SerializableLocation(loc);
 		this.grid.add(pos, newLoc);
 		return;
 	}
 	public void clearGridPositions(){
 		this.grid.clear();
 		return;
 	}
 	public void setLineLocation(List<Location> lineLoc){
 		this.line.clear();
 		for(Location loc:lineLoc){
 			SerializableLocation newLoc = new SerializableLocation(loc);
 			this.line.add(newLoc);
 		}
 		return;
 	}
 	public List<Location> getStartGrid(){
 		List<Location> result = new ArrayList<Location>();
 		for(SerializableLocation loc:this.grid){
 			result.add(loc.getLocation(ac.plugin.getServer()));
 		}
 		return result;
 	}
 	public List<Location> getLine(){
 		List<Location> result = new ArrayList<Location>();
 		for(SerializableLocation loc:this.line){
 			result.add(loc.getLocation(ac.plugin.getServer()));
 		}
 		return result;
 	}
 	public Boolean validateGridLocationSetRequest(int pos){
 		int number = this.grid.size();
 		if(pos <= number){
 			return true;
 		}
 		return false;
 	}
 	public int getLaps(){
 		return this.laps;
 	}
 	public void setLaps(int laps){
 		this.laps = laps;
 	}
 
 }
