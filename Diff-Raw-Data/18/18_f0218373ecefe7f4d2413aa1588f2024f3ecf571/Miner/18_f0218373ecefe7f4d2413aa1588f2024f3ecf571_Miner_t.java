 package net.lotrcraft.minepermit.miner;
 
 import java.util.ArrayList;
 
 import net.lotrcraft.minepermit.plot.Plot;
 
 import org.bukkit.Location;
 
 public class Miner{
 	
 	private ArrayList<Plot> plots;
 	private ArrayList<Permit> permits;
 	private String name;
 	
 	public Miner(String name){
 		
 		this.name = name;
 		permits = new ArrayList<Permit>();
 		plots = new ArrayList<Plot>();
 		
 	}
 	
 	public Permit getPermit(Location l)
 	{
 		for(Permit p : permits){
 			if(p.contains(l))
 				return p;
 		}	
 			return null;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public void addPermit(Permit p){
 		
 		permits.add(p);
 		p.setOwner(this);
 		
 	}
 
 	public ArrayList<Plot> getPlots() {
 		return plots;
 	}
 	
 	public boolean removePlot(Plot p){
 		return plots.remove(p);
 	}
 	
 	public boolean addPlot(Plot p){
		if(p.getOwner() != null && !p.getOwner().equals(name))
 			return false;
 		
 		plots.add(p);
		p.setOwner(name);
 		return true;
 	}
 
 	public Plot getPlot(String name) {
 		for(Plot p : plots)
 			if(p.getName().equals(name))
 				return p;
 		return null;
 	}
 }
