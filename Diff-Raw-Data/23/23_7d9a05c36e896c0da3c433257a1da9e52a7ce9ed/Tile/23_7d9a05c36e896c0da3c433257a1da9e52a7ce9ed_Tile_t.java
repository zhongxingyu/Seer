 package org.noip.staticattic.entities;
 
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 public class Tile extends Inanimate {
 	
 	private Type type = Type.GRASS;
 	private ImageIcon icon = new ImageIcon();
 	private JLabel lbl;
 		
 	public enum Type{
 		
 		GRASS, FLOWER, TREE, RIVER, SEA, SAND, SNOW, STONE, COBBLE, TILE, GRAVEL, CARPET, WOOD, FULL, DOOR
 		
 	}
 
 	public Tile(int xloc, int yloc) {
 		
 		super(xloc, yloc);
 		
 		if (this.getType().equals(Type.GRASS)) {
 			   
 			   try {
 			    
				   this.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResource("/resources/img/tiles/grass.fw.png"))));
 			    
 			   } catch (Exception e1) {
 			    
				   try {
 			     
				       this.setIcon(new ImageIcon(ImageIO.read(new File(System.getProperty("user.home")+"/village/grass.fw.png"))));
 			     
			       } catch (Exception e2) {
 			     
			           e2.printStackTrace();
 			     
			       }
 			    
	           }
 			   
	    }
 			  
 			  this.setLbl(new JLabel(this.getIcon()));
 			  this.getLbl().setBounds(this.getLocation().getX(), this.getLocation().getY(), 48, 48);
 		
 	}
 	
 	public ImageIcon getIcon() {
 		
 		return icon;
 		
 	}
 
 	public void setIcon(ImageIcon icon) {
 		
 		this.icon = icon;
 		
 	}
 
 	public Type getType() {
 		
 		return type;
 		
 	}
 
 	public void setType(Type type) {
 		
 		this.type = type;
 		
 	}
 	
 	public void updatePosition(){
 		
 		this.lbl.setLocation(this.getLocation().getX(), this.getLocation().getY());
 		
 	}
 
 	public JLabel getLbl() {
 		
 		return lbl;
 		
 	}
 
 	public void setLbl(JLabel lbl) {
 		
 		this.lbl = lbl;
 		
 	}
 
 }
