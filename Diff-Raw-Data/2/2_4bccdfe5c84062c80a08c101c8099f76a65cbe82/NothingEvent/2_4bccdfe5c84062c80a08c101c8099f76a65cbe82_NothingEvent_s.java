 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package devfortress.view.animation.events;
 
 import com.tabuto.j2dgf.Game2D;
 import com.tabuto.j2dgf.Group;
 import devfortress.view.animation.GameSprite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Sherlock
  */
 public class NothingEvent extends Game2D implements EventAnimationEngine{
     private BufferedImage BGR_IMAGE;
     private Group<GameSprite> sprites;
     private double widthScale, heightScale;
     private GameSprite bgr;
     private long timestamp = 0;
 
     public NothingEvent(Dimension dim) {
         super(dim);
     }
 
     /**
      * Activate/Re-activate this engine.
      */
     @Override
     public void activate() {
         super.activate();
     }
 
     /**
      * Deactivate this engine and reset it to original state.
      */
     @Override
     public void deactivate() {
         super.deactivate();
 
         timestamp = 0;
         sprites.clear();
         sprites.add(bgr);
     }
 
     @Override
     public void drawStuff(Graphics g) {
         sprites.move();
 
         /* Draw sprites. */
         if (!sprites.isEmpty()) {
             sprites.draw(g);
         }
     }
 
     @Override
     public void initGame() {
         URL bgrURL = getClass().getResource("../../resources/imgNothing.png");
         try {
             BGR_IMAGE = ImageIO.read(bgrURL);
             widthScale = (double) DIM.width / BGR_IMAGE.getWidth(null);
             heightScale = (double) DIM.height / BGR_IMAGE.getHeight(null);
             sprites = new Group<GameSprite>();
 
             /* Add sprites. */
             bgr = new GameSprite(DIM, 0, 0, BGR_IMAGE);
             bgr.setScales(widthScale, heightScale);
             sprites.add(bgr);
         } catch (Exception ex) {
             Logger.getLogger(FeatureCut.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public String getInformation() {
        return "Customer wants to cut out some features. Finish one field of the project";
     }
 }
