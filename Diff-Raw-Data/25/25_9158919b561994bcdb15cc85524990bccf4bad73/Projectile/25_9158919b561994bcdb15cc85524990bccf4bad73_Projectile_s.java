 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Anthony
  */
 
import javax.swing.ImageIcon;
 
 public class Projectile extends MapObject {
     private int velocity; // to be fixed
     private int ttl;
     
    public Projectile(int heading, int[] coordinates, ImageIcon img, int velocity, GameState gs, int ttl){     
        super(heading,coordinates,img);
         this.ttl = ttl;      
     }
     
     public int getTTL(){
        return this.heading;
     }
     public void setTTL(int ttl){
         this.ttl = ttl;
     }
 }
