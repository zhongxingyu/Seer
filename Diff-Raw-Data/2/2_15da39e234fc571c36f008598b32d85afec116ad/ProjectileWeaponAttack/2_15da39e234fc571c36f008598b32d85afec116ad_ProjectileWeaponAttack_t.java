 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.math.Vector3f;
 
 /**
  *
  * @author Alex
  */
 public class ProjectileWeaponAttack extends WeaponAttack {
     
     private int damage;
     private float speed; 
     
     public ProjectileWeaponAttack(Weapon weapon) {
         super(weapon);
     }
     
     public void update(float tpf) {
         
     }
     public void use() {
        Projectile shot = new Projectile(getWeapon().getOwner().getPosition(), damage, getWeapon().getOwner().getAngle(), speed);
         getWeapon().getOwner().getLevel().spawnProjectile(shot);
     }
     
 }
