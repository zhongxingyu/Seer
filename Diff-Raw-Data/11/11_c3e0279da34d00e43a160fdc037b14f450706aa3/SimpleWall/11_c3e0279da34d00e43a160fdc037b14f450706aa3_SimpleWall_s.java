 package structures;
 
 import image.ImageLoader;
 
 import java.awt.image.BufferedImage;
 
import structureAIs.StructureAI;
 import model.Location;
import model.Model;
 
 public class SimpleWall extends Structure {
 	
 	public SimpleWall(Location l) {
 		super(l);
 		this.health = 1000;
 	}
 
 	@Override
 	protected StructureAI makeAI() { // no AI
		return null;
 	}
 
 	@Override
 	public BufferedImage getSprite() {
 		if (image == null){
 			ImageLoader il = new ImageLoader();
 			image = il.getImage("wip yolo crystal.png");
 		
 		}
 		return image;
 	}
 
 	@Override
 	public void fire() {
		// TODO Auto-generated method stub
		
 	}
 }
