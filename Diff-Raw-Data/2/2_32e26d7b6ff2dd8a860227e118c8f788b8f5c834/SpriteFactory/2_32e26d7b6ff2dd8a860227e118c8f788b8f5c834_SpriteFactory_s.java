 package thyscom.spaceinvaders;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.Transparency;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import javax.imageio.ImageIO;
 import org.apache.log4j.Logger;
 
 /**
  * Manufactures sprites on demand, typically the store orders them.
  * @author thys
  */
 public class SpriteFactory {
 
     private static final Logger logger = Logger.getLogger(SpriteStore.class);
 
     /**
      * Get a new sprite given the resource reference. If the resource does not exist
      * a sprite is made up on the spot and returned.
      * @param ref
      * @throws HeadlessException 
      */
     public Sprite getSprite(String ref) {
         Image image = null;
         try {
             // The ClassLoader.getResource() ensures we get the sprite
             // from the appropriate place, this helps with deploying the game
             // with things like webstart. You could equally do a file look
             // up here.
             URL url = this.getClass().getClassLoader().getResource(ref);
 
             // if the source file is available, use it. Else create a figting dot 
             // on the spot.
             if (url != null) {
                 image = loadImageFromDisk(url);
             } else {
                 image = makeSubstituteImage();
             }
         } catch (IOException e) {
             logger.error(e);
             image = makeSubstituteImage();
         }
 
         return new Sprite(image);
 
     }
 
     private Image loadImageFromDisk(URL url) throws IOException {
        BufferedImage sourceImage = sourceImage = ImageIO.read(url);
         // create an accelerated image of the right size to store our sprite in
         Image image = getGraphicsConfiguration().createCompatibleImage(sourceImage.getWidth(), sourceImage.getHeight(), Transparency.BITMASK);
         // draw our source image into the accelerated image
         image.getGraphics().drawImage(sourceImage, 0, 0, null);
 
         return image;
     }
 
     private Image makeSubstituteImage() {
         int WIDTH = 20;
         int HEIGHT = 20;
         GraphicsConfiguration gc = getGraphicsConfiguration();
         BufferedImage image = gc.createCompatibleImage(WIDTH, HEIGHT, Transparency.BITMASK);
         Graphics2D g2d = image.createGraphics();
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setColor(Color.GREEN);
         g2d.fill(new Ellipse2D.Double(0, 0, WIDTH, WIDTH));
         g2d.dispose();
         return image;
     }
 
     private GraphicsConfiguration getGraphicsConfiguration() throws HeadlessException {
         return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
     }
 }
