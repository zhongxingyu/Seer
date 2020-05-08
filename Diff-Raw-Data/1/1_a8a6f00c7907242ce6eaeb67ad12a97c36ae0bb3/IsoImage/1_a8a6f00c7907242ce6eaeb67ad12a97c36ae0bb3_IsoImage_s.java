 package ui.isometric;
 
import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import clientinterface.GameThing;
 
 import util.*;
 
 /**
  * 
  * An image abstraction for isometric rendering
  * 
  * @author melby
  *
  */
 public class IsoImage {
 	private BufferedImage image;
 	private GameThing gameThing = null;
 	
 	/**
 	 * Create an IsoImage with a given resource path/name
 	 * Converts the color white into alpha
 	 * @param path
 	 */
 	public IsoImage(String path) {
 		image = Resources.readImageResourceUnfliped(path);
 	}
 
 	/**
 	 * Create an IsoImage with an existing image
 	 * @param image
 	 */
 	public IsoImage(BufferedImage image) {
 		this.image = image;
 	}
 
 	/**
 	 * Get the image that this IsoImage wraps
 	 * @return
 	 */
 	public BufferedImage image() {
 		return image;
 	}
 	
 	/**
 	 * Get the width that this image should be displayed at
 	 * @return
 	 */
 	public int width() {
 		return image.getWidth();
 	}
 	
 	/**
 	 * Get the height this image should be displayed at
 	 * @return
 	 */
 	public int height() {
 		return image.getHeight();
 	}
 
 	/**
 	 * Get the GameThing that this IsoImage represents
 	 * @return
 	 */
 	public GameThing gameThing() {
 		return gameThing;
 	}
 	
 	/**
 	 * Set the GameThing that this IsoImage represents
 	 * @param thing
 	 */
 	public void setGameThing(GameThing thing) {
 		gameThing = thing;
 	}
 }
