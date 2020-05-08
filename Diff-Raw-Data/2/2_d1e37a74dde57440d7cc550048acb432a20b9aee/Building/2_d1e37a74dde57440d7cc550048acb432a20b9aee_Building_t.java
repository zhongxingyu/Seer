 package net.lotrcraft.strategycraft.buildings;
 
 import org.bukkit.Location;
 import org.bukkit.block.BlockFace;
 
 public abstract class Building {
 	
 	static Castle castle;
 	
 	
 	private static BuildingManager bm;
 	
 	static Location location;
 	
 	void build(Location l){
 		Schematic s = getSchematic();
 		byte[] bytes = s.getBlocks();
 		
 		
 		/*
 		if (blockFace == BlockFace.NORTH){
 			
 			location = new Location(l.getWorld(), l.getBlockX() - s.getWidth()/2, l.getBlockY(), l.getBlockZ() - s.getLength()/2);
 			
 		} else if (blockFace == BlockFace.EAST){
 			
 			location = new Location(l.getWorld(), l.getBlockX() + s.getWidth()/2, l.getBlockY(), l.getBlockZ() - s.getLength()/2);
 			
 		} else if (blockFace == BlockFace.WEST){
 			
 			location = new Location(l.getWorld(), l.getBlockX() - s.getWidth()/2, l.getBlockY(), l.getBlockZ() + s.getLength()/2);
 			
 		} else {
 			
 			location = new Location(l.getWorld(), l.getBlockX() + s.getWidth()/2, l.getBlockY(), l.getBlockZ() + s.getLength()/2);
 			
 		}
 		*/
 		
 		for (int y = 0; y < s.getHeight(); y++){
 			for (int x = 0; x < s.getWidth(); x++){
 				for (int z = 0; z < s.getLength(); z++){
 					l.getWorld().getBlockAt(location).setData(bytes[y + x + z]);
 				}
 			}
 		}
 		
 	}
 	
 	void destroy(){
 		
 	}
 	
	public abstract String getName();
 	
 	Schematic getSchematic(){
 		return new Schematic(getClass().getClassLoader().getResourceAsStream(getName() + ".schematic"));
 	}
 	
 	BuildingManager getBuildingManager(){
 		return bm;
 	}
 
 	public Castle getCastle() {
 		return castle;
 		
 	}
 
 	public void setCastle(Castle cstl) {
 		this.castle = cstl;
 	}
 
 
 }
