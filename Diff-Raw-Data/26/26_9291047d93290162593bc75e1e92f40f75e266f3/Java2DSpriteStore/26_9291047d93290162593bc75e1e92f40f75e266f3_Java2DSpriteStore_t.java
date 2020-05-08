 package ca.couchware.wezzle2d.java2d;
 
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 
 import ca.couchware.wezzle2d.Sprite;
import ca.couchware.wezzle2d.util.Util;
 
 /**
  * A resource manager for sprites in the game. Its often quite important how and
  * where you get your game resources from. In most cases it makes sense to have
  * a central resource loader that goes away, gets your resources and caches them
  * for future use.
  * <p>
  * [singleton]
  * <p>
  * 
  * @author Kevin Glass
  */
 public class Java2DSpriteStore
 {
 	/**
 	 * The single instance of this class
 	 */
 	private static Java2DSpriteStore single = new Java2DSpriteStore();
 
 	/**
 	 * Get the single instance of this class .
 	 * 
 	 * @return The single instance of this class
 	 */
 	public static Java2DSpriteStore get()
 	{
 		return single;
 	}
 
 	/**
 	 * The cached sprite map, from reference to sprite instance
 	 */
 	private HashMap sprites = new HashMap();
 
 	/**
 	 * Retrieve a sprite from the store
 	 * 
 	 * @param window
 	 *            The window to which the sprite will be drawn
 	 * @param ref
 	 *            The reference to the image to use for the sprite
 	 * @return A sprite instance containing an accelerate image of the request
 	 *         reference
 	 */
 	public Sprite getSprite(Java2DGameWindow window, String path)
 	{
 		// if we've already got the sprite in the cache
 		// then just return the existing version
 		if (sprites.get(path) != null)
 		{
 			return (Sprite) sprites.get(path);
 		}
 
 		// otherwise, go away and grab the sprite from the resource
 		// loader
 		BufferedImage sourceImage = null;
 
 		try
 		{
 			// The ClassLoader.getResource() ensures we get the sprite
 			// from the appropriate place, this helps with deploying the game
 			// with things like webstart. You could equally do a file look
 			// up here.
 			URL url = this.getClass().getClassLoader().getResource(path);
 
 			if (url == null)
 			{
				Util.handleMessage("Can't find path: " + path, 
                        Thread.currentThread());
 			}
 
 			// use ImageIO to read the image in
 			sourceImage = ImageIO.read(url);
 		}
 		catch (IOException e)
 		{
			Util.handleMessage("Failed to load: " + path,
                    Thread.currentThread());
 		}
 
 		// Create an accelerated image of the right size to store our sprite in.
 		GraphicsConfiguration gc = GraphicsEnvironment
 				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
 				.getDefaultConfiguration();
        
 		Image image = gc.createCompatibleImage(sourceImage.getWidth(),
 				sourceImage.getHeight(), Transparency.TRANSLUCENT);
 
 		// draw our source image into the accelerated image
 		image.getGraphics().drawImage(sourceImage, 0, 0, null);
 
 		// create a sprite, add it the cache then return it
 		Sprite sprite = new Java2DSprite(window, image);
 		sprites.put(path, sprite);
 
 		return sprite;
 	}
 }
