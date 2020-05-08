 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.math.Vector2f;
 import com.jme3.math.Quaternion;
 import com.jme3.scene.Spatial;
 
 /**
  *
  * @author Alex
  */
 public class Projectile extends MoveableGameObject {
     
     private OfficeChair owner;
     private int damage;
     private float angle;
     private float speed;
     
     private Vector2f direction;
     
     public Projectile(OfficeChair owner, Vector2f location, Vector2f velocity, int damage) {
        super(new CircleF(location, 0.5f));
         this.owner = owner;
         this.type = GameObjectType.PROJECTILE;
         this.velocity = velocity;
         this.damage = damage;
         
         
     }
     
     @Override
     public void setSpatial(Spatial spatial) {
         super.setSpatial(spatial);
         float angle = getOwner().getAngle();
         float[] angles = {0, angle, 0};
         Quaternion rot = new Quaternion(angles);
         this.objectModel.setLocalRotation(rot);// = rot.mult(vel);
     }
     
     public OfficeChair getOwner() {
         return this.owner;
     }
     
     public int getDamage() {
         return this.damage;
     }
     
     public void update(float tpf) {
        super.update(tpf);
     }
     
     public void onCollision(GameObject object) {
         
     }
     
 }
