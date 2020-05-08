 /**
  * Programmers: Ed Broxson & Chase McCowan
  * Date: 02/22/2013
  * Purpose: Build particles and handle collisions and movement.
  */
 package layout;
 
 import javax.swing.JOptionPane;
 import processing.core.PVector;
 
 public class Element {
 
     int numParts = 40;
     PVector location;
     PVector velocity;
     float x, y;
     float diameter;
     float vx = 0;
     float vy = 0;
     int id;
     float height = 200;
     float width = 775;
     float startBoxWidth = 200;
     Element[] others;
     static float topSpeed = 10f;
     static boolean isWinner = false;
     static boolean secondWinner = false;
     static boolean gateOpen = false;
 
     Element() {
         location = new PVector(90, 100);
         x = location.x;
         y = location.y;
         velocity = new PVector(1, 1);
         diameter = 10;
     }
 
     Element(float w, float h, float vin, float din, int idin, Element[] oin) {
         location = new PVector(w, h);
         x = location.x;
         y = location.y;
         velocity = new PVector(vin, vin);
         diameter = din;
         id = idin;
         others = oin;
     }
 
     /**
      * method to update the element information based on collisions
      */
     public void update() {
 
         location.add(velocity);
 
         for (int i = id + 1; i < numParts; i++) {
             float dx = others[i].x - x;
             float dy = others[i].y - y;
             float distance = (float) Math.sqrt(dx * dx + dy * dy);
             float minDist = others[i].diameter / 2 + diameter / 2;
             if (distance < minDist) {
                 float angle = (float) Math.atan2(dy, dx);
                 float targetX = (float) (x + Math.cos(angle) * minDist);
                 float targetY = (float) (y + Math.sin(angle) * minDist);
                 float ax = (targetX - others[i].x);
                 float ay = (targetY - others[i].y);
                 velocity.x -= ax;
                 velocity.y -= ay;
                 others[i].velocity.x += ax;
                 others[i].velocity.y += ay; 
             }
         }
         velocity.limit(topSpeed);
     }
 
     /**
      * method to check if element is at the edge and reverse it's direction
      */
     public void checkEdges() {
 
         x += velocity.x;
         y += velocity.y;
 
         if (!gateOpen && x + diameter / 2 > startBoxWidth) {
             x = startBoxWidth - diameter / 2;
             velocity.x *= -1;
         } else if (x + diameter / 2 > width) {
             if (!isWinner && (id % 2 == 0)) {
                 JOptionPane.showMessageDialog(null, "WINNER!");
                 isWinner = true;
             } else if (isWinner && !secondWinner && (id % 2 != 0)) {
                 JOptionPane.showMessageDialog(null, "Other Winner!");
                 secondWinner = true;
             }
         } else if (x - diameter / 2 < 0) {
 
             x = diameter / 2;
             velocity.x *= -1;
         }
         if (y + diameter / 2 > height) {
             y = height - diameter / 2;
             velocity.y *= -1;
         } else if (y - diameter / 2 < 0) {
             y = diameter / 2;
             velocity.y *= -1;
         }
     }
 
     public PVector getLocation() {
         return location;
     }
 
     public PVector getVelocity() {
         return velocity;
     }
 
     public void setVelocity(PVector velocity) {
         this.velocity = velocity;
     }
     
     
 
     public static void setGateOpen(boolean gateO) {
         gateOpen = gateO;
     }
 
     public static float getTopSpeed() {
         return topSpeed;
     }
 
     public static void setTopSpeed(float topSpeed) {
         Element.topSpeed = topSpeed;
     }
 }
