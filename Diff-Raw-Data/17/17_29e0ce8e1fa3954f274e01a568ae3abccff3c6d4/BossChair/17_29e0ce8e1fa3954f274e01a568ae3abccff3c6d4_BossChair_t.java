 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 /**
  *
  * @author Ian
  */
 public class BossChair extends GameActor {
 
     int health = 5000;
 
     void update(float tpf) {
     }
 
     public void takeDamage(int damage) {
         health -= damage;
     }
    
    public void BossChair(){
        this.type = GameObjectType.ACTOR;
        
    }
    public void onCollision(GameObject object){}
 }
