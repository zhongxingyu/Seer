package mygame;
import com.jme3.math.Vector3f;
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Ian
  */
 public class BossChair extends GameActor {
 
     int health = 5000;
    Vector3f position;
    
     void update(float tpf) {
     }
 
     public void takeDamage(int damage) {
         health -= damage;
     }
     
     public void BossChair(){
        this.type = GameObjectType.ACTOR;    
    }
    public void onCollision(GameObject object){
     }
 }
