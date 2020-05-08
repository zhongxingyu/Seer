 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gameoflife;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.geom.Rectangle2D;
 
 /**
  *
  * @author Hendrik
  */
 public class Cel {
 
     public Cel() {
         IsAlive = false;
     }
     private boolean IsAlive;
     final Color dead = Color.RED;
     final Color live = Color.GREEN;
 
     public void ChangeStatusCel() {
         IsAlive = !IsAlive;
     }
 
     void DrawCel(Graphics2D image, int x, int y, int cellType) {
         if (IsAlive == false) {
             image.setColor(dead);
 
         } else {
             image.setColor(live);
         }
         if (cellType == 0) {
 
             image.fill(new Rectangle2D.Double(x * Controller.celWidth, y * Controller.celHeight, Controller.celWidth, Controller.celHeight));
 
             image.setColor(Color.BLACK);
             image.drawLine(x * Controller.celWidth, (y) * Controller.celHeight, x * Controller.celWidth,
                     (y + 1) * Controller.celHeight);
             image.drawLine((x + 1) * Controller.celWidth, (y) * Controller.celHeight, (x + 1) * Controller.celWidth,
                     (y + 1) * Controller.celHeight);
 
             image.drawLine(x * Controller.celWidth, (y) * Controller.celHeight, (x + 1) * Controller.celWidth,
                     (y) * Controller.celHeight);
             image.drawLine(x * Controller.celWidth, (y + 1) * Controller.celHeight, (x + 1) * Controller.celWidth,
                     (y + 1) * Controller.celHeight);
         } else if (cellType == 1) {
             Point p1 = new Point();
             Point p2;
             Point p3;
            if (y % 2 == 0) {
                p1 = new Point((x * Controller.celWidth),
                         (y * (Controller.celHeight)));
                p2 = new Point((x + 1) * Controller.celWidth,
                         (y * (Controller.celHeight)));
                p3 = new Point(((x * Controller.celWidth)),
                         ((y + 1) * (Controller.celHeight)));
             } else {
                 p1 = new Point(((x - 1) * Controller.celWidth),
                         ((y + 1) * Controller.celHeight) - (Controller.celHeight));
                 p2 = new Point((x) * Controller.celWidth,
                         (y * (Controller.celHeight) - (Controller.celHeight)));
                p3 = new Point(((x * Controller.celWidth)),
                        ((y + 1) * Controller.celHeight) - (Controller.celHeight));
            }
 
 
 
 
 
 
             int[] xcor = {p1.x, p2.x, p3.x};
 
            int[] ycor = {p1.y, p2.y, p3.y};
 
             Polygon triangle = new Polygon(xcor, ycor, xcor.length);
 
             image.fill(triangle);
             image.setColor(Color.BLACK);
             image.draw(triangle);
 
 
 
         }
 
     }
 }
