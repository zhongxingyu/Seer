 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Anthony
  */
 
 import java.awt.Polygon;
 
 public class Ship extends MapObject{
     int lives;
     private int fireRate;
     // Contructor
    public Ship(int velocity, int heading, int[] coordinates, GameState gameState,int fireRate, int lives){
         super(velocity, heading, coordinates, gameState);
         this.lives = lives;
         this.fireRate = fireRate;
     }
     
     public int getFireRate()
     {
         return this.fireRate;
     }
     
     public void setFireRate(int fireRate)
     {
         this.fireRate = fireRate;
     }
     
     public int getLives()
     {
         return this.lives;
     }
     
     public void setLives(int lives)
     {
         this.lives = lives;
     }
     
     public void shoot()
     {
        Projectile p = new Projectile(velocity, heading, coordinates, gameState, ttl);        
     }
 }
