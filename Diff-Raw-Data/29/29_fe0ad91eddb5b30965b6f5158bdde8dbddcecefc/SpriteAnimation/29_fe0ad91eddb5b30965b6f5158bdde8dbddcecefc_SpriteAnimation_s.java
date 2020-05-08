 package fr.umlv.escape.front;
 
import java.awt.Image;
 
 /**
  * Class that an explosion sprite
  */
public class SpriteAnimation implements Sprite{
 	private int currentImage;
 	private long lastImageChange;
 	private final int speed;
 	private boolean isGrowing;
 	private final int x;
 	private final int y;
 	
 	/**
 	 * constructor
 	 * @param speed the speed of the sprite
 	 * @param x the position x where the explosion is spawn
 	 * @param y the position y where the explosion is spawn
 	 */
	public SpriteAnimation(int speed, int x, int y){
 		if(speed<0){
 			throw new IllegalArgumentException("speed cannot be negative");
 		}
 		
 		this.currentImage=1;
 		this.speed=speed;
 		isGrowing=true;
 		this.x=x;
 		this.y=y;
 	}
	
	/**
	 * Load all images that are needed to the sprites. This method has to be called before using this class.
	 */
	public static void loadImagesSprites() {
		for(int i=1;i<=12;++i){
			Image image=ImagesFactory.getTheImagesFactory().createShipImage("Explosion"+i);
			FrontImages.addImages("Explosion"+i, image);
		}
	}
 
	@Override
	public Image getNextImage() {
 		String imageName = "Explosion"+currentImage;
 		long currentTime=System.currentTimeMillis();
 		
 		if(isGrowing){
 			if(currentTime-lastImageChange>speed){
 				currentImage++;
 				lastImageChange=currentTime;
 			}
 			if(currentImage==11){
 				isGrowing=false;
 			}
 		}
 		else{
 			if(currentTime-lastImageChange>speed){
 				currentImage--;
 				lastImageChange=currentTime;
 			}
 			if(currentImage==0){
 				return null;
 			}
 		}
		return FrontImages.getImage(imageName);
 	}
 	
 	/**
 	 * Get the x position of the current explosion
 	 * @return the x position of the current explosion
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Get the y position of the current explosion
 	 * @return the y position of the current explosion
 	 */
 	public int getY() {
 		return y;
 	}
 
 }
