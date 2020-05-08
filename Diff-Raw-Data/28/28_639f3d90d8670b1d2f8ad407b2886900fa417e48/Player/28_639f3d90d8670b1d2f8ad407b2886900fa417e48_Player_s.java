 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
package B3.trunk.src.Unit;
 
 import B3.trunk.src.Weapon.Weapon;
 import java.awt.Shape;
 import java.awt.Color;
 
 /**
  *
  * @author michal
  */
 public class Player extends Unit {
 
     public static void main(String[] args) {
         Player p = new Player(1, 2, null, null, 3, 0, 0, Color.GREEN);
     }
 
     public Player(int health, int speed, Shape shape, Weapon weaponType, int pointValue, int xCoord, int yCoord, Color color) {
         super(health, speed, shape, weaponType, pointValue, xCoord, yCoord, color);
         check();
     }
 
     public void check() {
         System.out.println(health);
         System.out.println(shape);
         System.out.println(speed);
         System.out.println(weaponType);
         System.out.println(pointValue);
 		System.out.println(xCoord + ", " + yCoord);
 		System.out.println(color);
     }
 }
