 
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
 import javax.media.opengl.GLDrawable;
 
 import com.jogamp.opengl.util.awt.Overlay;
 import actor.*;
 import game.Game;
 import math.Vector3;
 /**
  * @author Tim Mikeladze
  * 
  * Draws hud elements on an Overlay with Graphics2d
  * 
  *
  */
 public class Hud implements ImageObserver {
     Overlay overlay;
     Graphics2D graphics;
     BufferedImage health_backdrop, health_bar, health_cross;
 
     //244, 821
     private static final String HEALTHBACKDROP="assets/images/hud/health_backdrop.png";
     private static final String HEALTHBAR = "assets/images/hud/health_bar.png";
     private static final String HEALTHCROSS = "assets/images/hud/health_cross.png";
         
 
     int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
     int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
     
     /**
      * Loads the images and gets screen resolution passed from canvas
      * @param width of the canvas
      * @param height of the canvas
      */
     public Hud() {
 
         try {
             health_backdrop = new BufferedImage(122, 410, BufferedImage.TYPE_INT_ARGB);
             health_backdrop = ImageIO.read(new File(HEALTHBACKDROP));  
             health_bar = ImageIO.read(new File(HEALTHBAR));
             health_cross = ImageIO.read(new File(HEALTHCROSS));
             
             
         } catch (IOException e) {
             System.out.println("Can't find image in assets");
             e.printStackTrace();
         }
     }
     /**
      * Draws static hud elements that don't change throughout the game
      * @param glDrawable
      */
     public void drawStaticHud(GLAutoDrawable glDrawable) {
 
         //creates new overlay
         if(overlay == null)
         {
             overlay = new Overlay(glDrawable); 
             graphics = overlay.createGraphics();
         }
         // if an overlay has been created
         else
         {
           overlay.markDirty(0, 0, screenWidth, screenHeight);   
         }
          
         
        graphics.clearRect(0, 0,2000,2000);
         overlay.beginRendering();
         
       //  graphics.drawImage(health_backdrop, 0, 0, this);
      //   health_backdrop.setRGB(0, 0, BufferedImage.TYPE_INT_ARGB);
         graphics.drawImage(health_backdrop, screenWidth-244, 0, 122, 410, this);
         
         graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         
         graphics.finalize();
         
         
         overlay.draw(0, 0, screenWidth, screenHeight);
         overlay.endRendering();
         
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
     public Vector3 getPlayerPosition() {
         return game.Game.getPlayer().getShip().getPosition();
     }
     public Vector3 getAsteroidPosition() {
         return game.Game.getAsteroid().getPosition();
     }
     
     /**
      * Gets player direction
      * @return player direction vector
      */
     public Vector3 getPlayerDirection() {
         return game.Game.getPlayer().getShip().getDirection();
     }
     /**
      * Gets asteroid direction
      * @return asteroid direction vector
      */
     public Vector3 getAsteroidDirection() {
         return game.Game.getAsteroid().getDirection();
     }
 
 
     @Override
     public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
             int arg4, int arg5) {
         // TODO Auto-generated method stub
         return false;
     }
 }
 
