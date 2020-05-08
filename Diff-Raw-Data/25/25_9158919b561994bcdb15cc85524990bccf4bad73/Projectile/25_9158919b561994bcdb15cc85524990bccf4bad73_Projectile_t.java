 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Anthony
  */
 
import java.awt.Polygon;
 
 public class Projectile extends MapObject {
     private int velocity; // to be fixed
     private int ttl;
     
    public Projectile(int velocity, int heading, int[] coordinates, Polygon shape, GameState gameState, int ttl){     
        super(velocity, heading, coordinates, shape, gameState);
         this.ttl = ttl;      
     }
     
     public int getTTL(){
        return this.ttl;
     }
     public void setTTL(int ttl){
         this.ttl = ttl;
     }
 }
