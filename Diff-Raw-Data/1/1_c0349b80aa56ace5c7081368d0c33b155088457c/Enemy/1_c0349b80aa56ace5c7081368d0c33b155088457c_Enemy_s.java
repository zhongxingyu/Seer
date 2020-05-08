 package edu.ycp.cs496.model;
 
 import java.util.List;
 
 public class Enemy extends The_Superclass {
 	private Location loc;
 	private int xSpeed;
 	private int ySpeed;
 	private List<Location> locations;
 	private int index = 0;
 	private int prevVal;
 	
 	public Enemy (Location loc, List<Location> locations){
 		this.loc = loc;
 		this.xSpeed = 10; // Enemy Location is current set to be static
 		this.ySpeed = 10; // in both directions
 		this.locations = locations;
 	}
 
 	public void setLocation(Location loc){
 		this.loc = loc;
 	}
 	
 	public void setXSpeed(int xSpeed){
 		this.xSpeed = xSpeed;
 	}
 	
 	public void setYSpeed(int ySpeed){
 		this.ySpeed = ySpeed;
 	}
 	
 	public void setLocationList(List<Location> locations){
 		this.locations = locations;
 	}
 	
 	public void setIndex(int index){
 		this.index = index;
 	}
 	
 	public Location getLocation(){
 		return loc;
 	}
 	
 	public int getXSpeed(){
 		return xSpeed;
 	}
 	
 	public int getYSpeed(){
 		return ySpeed;
 	}
 	
 	public List<Location> getLocationList(){
 		return locations;
 	}
 	
 	public int getIndex(){
 		return index;
 	}
 	
 	public void setPrevVal(int i)
 	{
 		prevVal = i;
 	}
 	
 	public int getPrevVal()
 	{
 		return prevVal;
 	}
 	
 	public Location move(){	//Changed here - make sure it works
 		index++;
 		if (index == locations.size()){
 			index = 0;
 		}
 		loc = locations.get(index);
 		return locations.get(index);
 	}
 	
 	public boolean equals(Enemy enemy){
 		if (loc.equals(enemy.getLocation()) && xSpeed == enemy.getXSpeed() 
 				&& ySpeed == enemy.getYSpeed() && locations.equals(enemy.getLocationList())){
 			return true;
 		}
 		return false;
 	}
 	
 }
