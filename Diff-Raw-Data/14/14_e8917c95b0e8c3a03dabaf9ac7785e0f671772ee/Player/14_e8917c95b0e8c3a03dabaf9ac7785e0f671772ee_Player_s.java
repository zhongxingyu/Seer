 package nl.duckson.zombiesiege.entity;
 
import nl.duckson.zombiesiege.Game;
 import nl.duckson.zombiesiege.bullet.Bullet;
 import nl.duckson.zombiesiege.weapon.*;
 
import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * Able to move around, carry and fire weapons.
  *
  * User: mathijs
  * Date: 23/09/2013
  * Time: 17:21
  */
 public class Player extends Human {
     public ArrayList<Bullet> bullets;
 
     public ArrayList<Weapon> weapons;
     public int current_weapon;
 
     private static final int speed = 2;
 
     public Player() {
         super();
         width = height = 64;
 
         bullets = new ArrayList<Bullet>();
         weapons = new ArrayList<Weapon>();
 
         // Equip some weapons! :D
         weapons.add(new Handgun());
         weapons.add(new Revolver());
         weapons.add(new Shotgun());
         weapons.add(new Minigun());
 
         current_weapon = 0;
     }
 
     public Weapon getWeapon() {
         return (Weapon) weapons.get(current_weapon);
     }
 
     public void equipNextWeapon() {
         if(current_weapon == weapons.size() - 1)
             current_weapon = 0;
         else
             current_weapon += 1;
         System.out.printf("Equipped %s\n", getWeapon().getName());
     }
 
     public void equipPreviousWeapon() {
         if(current_weapon == 0)
             current_weapon = weapons.size() - 1;
         else
             current_weapon -= 1;
         System.out.printf("Equipped %s\n", getWeapon().getName());
     }
 
    public ArrayList getBullets() {
         return bullets;
     }
 
     public String getIcon() { return "player.png"; }
 
     public String getName() {
         return "Duck";
     }
 
     public void fire() {
         Weapon w = getWeapon();
         // Spawn the bullet(s) at the center of the player
         Collections.addAll(bullets, w.fire(x + (width / 2), y + (height / 2), direction));
     }
 
     /**
      * Whether the player is holding the trigger (firing)
      */
     private boolean firing = false;
 
     public boolean isFiring() {
         return firing;
     }
 
     public void setFiring(boolean f) {
         firing = f;
     }
 
     // Key press handling
     public void keyPressed(KeyEvent e) {
         int key = e.getKeyCode();
 
         if(key == KeyEvent.VK_W) dy = -speed; // Up
         if(key == KeyEvent.VK_S) dy = speed;  // Down
         if(key == KeyEvent.VK_A) dx = -speed; // Left
         if(key == KeyEvent.VK_D) dx = speed;  // Right
 
         if(key == KeyEvent.VK_SPACE) fire();
 
         if(key == KeyEvent.VK_Q) equipPreviousWeapon();
         if(key == KeyEvent.VK_E) equipNextWeapon();
 
         if(key == KeyEvent.VK_R) getWeapon().reload();
     }
 
     public void keyReleased(KeyEvent e) {
         int key = e.getKeyCode();
 
         if(key == KeyEvent.VK_W) dy = 0; // Up
         if(key == KeyEvent.VK_S) dy = 0; // Down
         if(key == KeyEvent.VK_A) dx = 0; // Left
         if(key == KeyEvent.VK_D) dx = 0; // Right
     }
 
     public Rectangle getBounds() {
         return new Rectangle(x, y, width, height);
     }
 
     public AffineTransform getAffineTransform() {
         AffineTransform trans = new AffineTransform();
         trans.translate(x, y);
         trans.rotate(Math.toRadians(direction), width / 2, height / 2);
 
         return trans;
     }
 }
