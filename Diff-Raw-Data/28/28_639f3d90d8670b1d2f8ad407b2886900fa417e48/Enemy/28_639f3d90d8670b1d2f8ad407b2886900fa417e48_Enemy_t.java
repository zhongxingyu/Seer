 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
package Unit;
 
 import B3.trunk.src.Weapon.Weapon;
 import java.awt.Shape;
 import java.awt.Color;
 
 /**
  *
  * @author michal
  */
 public class Enemy extends Unit{
 
     public static void main(String[] args) {
         Enemy p = new Enemy(1, 2, null, null, 3, 0, 0, Color.RED);
         Player r = new Player(4,5, null, null, 6, 0, 0, Color.GREEN);
     }
 
    public Enemy(int health, int speed, Shape shape, Weapon weaponType, int pointValue, int xCoord, int yCoord, Color color) {
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
