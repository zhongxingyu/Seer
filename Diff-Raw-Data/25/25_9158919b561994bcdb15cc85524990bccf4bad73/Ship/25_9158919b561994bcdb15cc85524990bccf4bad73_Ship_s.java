 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Anthony
  */
 
import javax.swing.ImageIcon;
 
 public class Ship extends MapObject{
     private int lives;
     private int fireRate;
     // Contructor
    Ship(int heading, int[] coordinates, ImageIcon img,int fireRate, int lives){
        super(heading,coordinates,img);
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
       Projectile p = new Projectile(int velocity, int heading, int[] coordinates, ImageIcon img, GameState gs, int ttl);        
     }
 }
