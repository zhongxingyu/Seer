 package graphics;
 
 import java.awt.Color;
 import actor.Actor;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.media.opengl.GLAutoDrawable;
 import com.jogamp.opengl.util.awt.Overlay;
 import actor.*;
 import game.Game;
 import math.Vector3;
 /**
  * @author Tim Mikeladze
  * 
  * Draws hud elements on an Overlay with Graphics2d
  * @author Tim Mikeladze
  *
  */
 public class Hud implements ImageObserver {
     Overlay overlay;
     Graphics2D graphics;
     BufferedImage healthBar;
     private int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
     private int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
 
     private static final String HEALTHBAR_PATH="assets/images/healthbar.png";
 
     /**
      * Constructor loads images
      */
     public Hud() {
         try {
             healthBar = ImageIO.read(new File(HEALTHBAR_PATH));  
         } catch (IOException e) {
             System.out.println("Can't find image in assets");
             e.printStackTrace();
         }
     }
     /**
      * Draws the hud elements
      * @param glDrawable
      */
     public void drawHud(GLAutoDrawable glDrawable) {
 
         overlay = new Overlay(glDrawable);  
 
         Graphics2D graphics = overlay.createGraphics();
         graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 
        graphics.drawImage(healthBar, screenWidth-250, screenHeight-50, this);
       
    
         graphics.drawString("Distance " + calcDistanceVector(), screenWidth-1200, screenHeight-50);
         graphics.drawString("Player Direction " + game.Game.getPlayer().getShip().getDirection(), screenWidth-1200, screenHeight-100);
        
         graphics.finalize();
         overlay.drawAll(); 
     }
 
     /**
      * Calculates distance between player and asteroid
      * @return distance
      */
     public float calcDistance() {
         float distance=0;  
         float xPlayer, yPlayer, zPlayer;
         float xAsteroid, yAsteroid, zAsteroid;
        
         xPlayer = game.Game.getPlayer().getShip().getPosition().x;
         yPlayer = game.Game.getPlayer().getShip().getPosition().y;
         zPlayer = game.Game.getPlayer().getShip().getPosition().z;
 
         xAsteroid = game.Game.getAsteroid().getPosition().x;
         yAsteroid = game.Game.getAsteroid().getPosition().y;
         zAsteroid = game.Game.getAsteroid().getPosition().z;
         
         
         distance = (float)Math.sqrt(Math.pow((xPlayer - xAsteroid),2) + Math.pow((yPlayer - yAsteroid),2) + 
                 Math.pow((zPlayer - zAsteroid),2)); 
         return distance;
     }
     /**
      * Calculates Vector distance between player and asteroid
      * @return Vector3 distance
      */
     public Vector3 calcDistanceVector() {
         return game.Game.getPlayer().getShip().getPosition().minus(game.Game.getAsteroid().getPosition());
     }
 
 
     @Override
     public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
             int arg4, int arg5) {
         // TODO Auto-generated method stub
         return false;
     }
 }
