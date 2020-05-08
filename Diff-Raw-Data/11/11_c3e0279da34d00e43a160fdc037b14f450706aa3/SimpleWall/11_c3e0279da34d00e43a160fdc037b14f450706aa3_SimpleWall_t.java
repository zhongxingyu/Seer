 package structures;
 
 import image.ImageLoader;
 
 import java.awt.image.BufferedImage;
 
 import model.Location;
import structureAIs.NullAI;
import structureAIs.StructureAI;
 
 public class SimpleWall extends Structure {
 	
 	public SimpleWall(Location l) {
 		super(l);
 		this.health = 1000;
 	}
 
 	@Override
 	protected StructureAI makeAI() { // no AI
		return new NullAI ();
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
 	}
 }
