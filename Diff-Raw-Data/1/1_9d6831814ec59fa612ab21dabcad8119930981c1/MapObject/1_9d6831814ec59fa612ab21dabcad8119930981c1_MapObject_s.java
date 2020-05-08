 package game;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Nikolaos
  */
 import java.awt.Polygon;
 import javax.swing.ImageIcon;
 
 public class MapObject {
     private int[] velocity;
     private int[] coordinates;
     private int heading;
     private int acceleration;
     private GameState gameState;
     private Polygon shape;
     
     public MapObject(int[] velocity, int heading, int[] coordinates, int acceleration, GameState gameState)
     {
         this.velocity = velocity;
         this.heading = heading;
         this.coordinates = coordinates;
         this.acceleration = acceleration;
         this.gameState = gameState;
     }
     
     public int[] getVelocity()
     {
         return this.velocity;
     }
     
     public void setVelocity(int[] velocity)
     {
         this.velocity = velocity;
     }
     
     public int getAcceleration()
     {
         return this.acceleration;
     }
     
     public void setAcceleration(int acceleration)
     {
         this.acceleration = acceleration;
     }
     
     public int getHeading()
     {
         return this.heading;
     }
     
     public void setHeading(int heading)
     {
         this.heading = heading;
     }
     
     public int[] getCoord()
     {
         return this.coordinates;
     }
     
     public void setCoord(int[] coordinates)
     {
         this.coordinates = coordinates;
     }
     
         public int getX()
     {
         return coordinates[0];
     }
     
     public void setX(int x)
     {
         this.coordinates[0] = x;
     }
     
     public int getY()
     {
         return coordinates[1];
     }
     
     public void setY(int y)
     {
         this.coordinates[1] = y;
     }
     
     public Polygon getShape()
     {
         return this.shape;
     }
     
     public void setShape(Polygon shape)
     {
         this.shape = shape;
     }
     
     public GameState getGameState()
     {
         return this.gameState;
     }
     
     public void destroy()
     {
         
     }
 }
