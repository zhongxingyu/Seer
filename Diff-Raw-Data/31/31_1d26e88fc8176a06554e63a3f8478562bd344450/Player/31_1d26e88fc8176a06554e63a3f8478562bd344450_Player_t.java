 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Unit;
 
 import Weapon.Weapon;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Area;
 import java.awt.geom.Path2D;
 
 /**
  *
  * @author michal
  */
 public class Player extends Unit {
 
     public Player(int health, int speed, Weapon weaponType, int pointValue, int xCoord, int yCoord, Color color) {
         super(health, speed, weaponType, pointValue, xCoord, yCoord, color);
         check();
     }
 
     public void check() {
         System.out.println(health);
 //        System.out.println(shape);
         System.out.println(speed);
         System.out.println(weaponType);
         System.out.println(pointValue);
 		System.out.println(xCoord + ", " + yCoord);
 		System.out.println(color);
     }
 //    public Polygon ship(Graphics g){
 //       Graphics2D g2 = (Graphics2D) g;
 //
 //       Point2D one = new Point2D.Double(20, 20);
 //       Point2D two = new Point2D.Double(50, 20);
 //       Point2D three = new Point2D.Double(20, 15);
 //
 //       int[] pointX = {(int) one.getX(), (int) two.getX(), (int) three.getX()};
 //       int[] pointY = {(int) one.getY(), (int) two.getY(), (int) three.getY()};
 //       ship = new Polygon(pointX, pointY, 3);
 //
 //
 //
 //        return ship;
 //    }
 
     public void draw(Graphics g){
         Graphics2D g_ = (Graphics2D) g;
         g_.setColor(color);
         Path2D triangle = new Path2D.Double();
         triangle.moveTo(xCoord, yCoord - 20);
         triangle.lineTo(xCoord - 10, yCoord + 10);
         triangle.lineTo(xCoord + 10, yCoord + 10);
         Area a = new Area(triangle);
         g_.fill(a);
     }
 }
