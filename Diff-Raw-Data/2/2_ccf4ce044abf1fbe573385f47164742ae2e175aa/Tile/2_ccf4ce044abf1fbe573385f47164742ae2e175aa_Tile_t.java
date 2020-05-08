 package org.noip.staticattic.entities;
 
 import javax.swing.JLabel;
 
 import org.noip.staticattic.fileutils.TextureHandler;
 
 public class Tile extends Inanimate {
 	
 	private Type type = Type.GRASS;
 	
 		
 	public enum Type{
 		
 		GRASS, FLOWER, TREE, RIVER, SEA, SAND, SNOW, STONE, COBBLE, TILE, GRAVEL, CARPET, WOOD, FULL, DOOR
 		
 	}
 
 	public Tile(int xloc, int yloc) {
 		
 		super(xloc, yloc);
 		
 		if (this.getType().equals(Type.GRASS)) {
 			   
 			this.setIcon(TextureHandler.getGrass());
 			   
 	    }
 			  
 		this.setLabel(new JLabel(this.getIcon()));
		this.getLabel().setBounds(this.getLocation().getX(), this.getLocation().getY(), 34, 34);
 		
 	}
 	
 	public Type getType() {
 		
 		return type;
 		
 	}
 
 	public void setType(Type type) {
 		
 		this.type = type;
 		
 	}
 	
 	public void updatePosition(){
 		
 		this.getLabel().setLocation(this.getLocation().getX(), this.getLocation().getY());
 		
 	}
 	
 }
