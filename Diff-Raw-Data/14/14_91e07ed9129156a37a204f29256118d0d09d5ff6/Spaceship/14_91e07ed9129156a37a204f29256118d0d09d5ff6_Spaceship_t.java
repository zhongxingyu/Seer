 
 package objects;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Polygon;
 
 public class Spaceship {
 
     private int positionX;
     private int positionY;
     private int direction;
     private Color color;
     
     public Spaceship(int positionX, int positionY, int direction, Color color) {
         this.positionX = positionX;
         this.positionY = positionY;
         this.direction = direction;
         this.color = color;
     }
     
     public Polygon getPolygon() {
         return getPolygon(new Point(positionX, positionY));
     }
     
     public Polygon getPolygon(Point pt) {
         Polygon p = new Polygon();
        p.addPoint(pt.x, pt.y - 30);
         p.addPoint(pt.x + 10, pt.y);
        p.addPoint(pt.x + 8, pt.y + 10);
        p.addPoint(pt.x - 8, pt.y + 10);
         p.addPoint(pt.x - 10, pt.y);
         return p;
     }
     
     public Color getColor() {
         return color;
     }
 
     public int getDirection() {
         return direction;
     }
 
     public int getPositionX() {
         return positionX;
     }
 
     public int getPositionY() {
         return positionY;
     }
     
 }
