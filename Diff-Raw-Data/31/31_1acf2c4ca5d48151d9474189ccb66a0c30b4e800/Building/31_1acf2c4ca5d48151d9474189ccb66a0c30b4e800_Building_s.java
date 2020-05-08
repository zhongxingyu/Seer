 package net.lotrcraft.strategycraft.buildings;
 
 import net.lotrcraft.strategycraft.schematic.MissingSchematicError;
 import net.lotrcraft.strategycraft.schematic.Schematic;
 
 import org.bukkit.Location;
 import org.bukkit.block.BlockFace;
 
 public abstract class Building {
 	
	static Castle castle;
 	
	static Location location;
 	
 	public void build(Location l){
 		Schematic s = getSchematic();
 		byte[] bytes = s.getBlocks();
 		location = l;
 		
 		
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
					l.getWorld().getBlockAt(location).setData(bytes[y*s.getHeight() + x*s.getWidth() + z]);
 				}
 			}
 		}
 		
 	}
 	
 	public void destroy(){
 		Schematic s = getSchematic();
 		for (int y = 0; y < s.getHeight(); y++){
 			for (int x = 0; x < s.getWidth(); x++){
 				for (int z = 0; z < s.getLength(); z++){
					location.getWorld().getBlockAt(location).setTypeId(0);
 				}
 			}
 		}
 	}
 	
 	public String getName(){
		return this.getClass().getName().substring(0, this.getClass().getName().indexOf('.'));
 	}
 	
 	public Schematic getSchematic(){
 		try {
 			return new Schematic(getClass().getClassLoader().getResourceAsStream(getName() + ".schematic"));
 		} catch (MissingSchematicError e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	BuildingManager getBuildingManager(){
 		BuildingManager bm = new BuildingManager();
 		return bm;
 	}
 
 	public Castle getCastle() {
 		return castle;
 		
 	}
 
 	public void setCastle(Castle cstl) {
 		this.castle = cstl;
 	}
 
 
 }
