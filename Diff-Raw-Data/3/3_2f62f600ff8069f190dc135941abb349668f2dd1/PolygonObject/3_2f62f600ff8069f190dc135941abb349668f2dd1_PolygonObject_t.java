 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.geom.Ellipse2D;
 import java.util.ArrayList;
 
 /**
  *
  * @author s0935850
  */
 public class PolygonObject {
     
     private ArrayList<Point> points;
     private Polygon poly;
     public Color color;
     private String name;
     public Point temp;
     public boolean isValid;
     private int _id;
     private boolean isSelected;
     
     public PolygonObject() {
         
         points = new ArrayList<Point>();
         color = new Color(255,255,255);
         name = "null";
         poly = null;
         isValid = false;
     }
     
     public void setName(String _name) {
         name = _name;
     }
     
     public boolean isSelected() {
         return isSelected;
     }
     
     public void select() {
         isSelected = true;
     }
     
     public void deSelect() {
         isSelected = false;
     }
     
     public String getName() {
         return name;
     }
     
     public void setID(int i) {
         
         _id = i;
     }
     
     public void setColor(int r, int g, int b) {
         color = new Color(r,g,b);
    
     }
     
     public Color getColor() {
         return color;
     }
     
     public int getID() {
         return _id;
         
     }
     
     public String toString() {
         
         return name;
     }
     
     public void draw(Graphics g, boolean shade)
     {
         g.setColor(color);
         Graphics2D g2 = (Graphics2D) g;
         g2.setStroke(new BasicStroke(7.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f));
 
         if(points.size() == 1) {
            if(temp != null)
                g2.drawLine(points.get(0) .x,points.get(0).y, temp.x, temp.y);
             return;
         }
         if(poly == null) {
             
             Point prev = null;
             boolean first = true;
             
             
             g2.drawOval(points.get(0).x, points.get(0).y, 7, 7);
             
             for(Point p : points)
             {
                 if(!first) {
                     g2.drawLine(prev.x, prev.y, p.x, p.y);
                 }
                 first = false;
                 prev = p;
             }
             
             if(temp != null) {
                 g2.drawLine(prev.x, prev.y, temp.x, temp.y);
                 
             }
             
         } else {
             g.setColor(color);
             
             g2.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,8.0f));
             
             g2.drawPolygon(poly);
             g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
             
                 
             
         }
         
         if(isSelected) {
             g2.fillPolygon(poly);
             for(Point p : points) {
                 
                 g2.setColor(color);
                 g2.fillOval(p.x - 6, p.y -6 , 12, 12);
                 g2.setColor(new Color(255-color.getRed(), 255-color.getGreen(), 255-color.getBlue()));
                 g2.drawOval(p.x -6, p.y -6, 12, 12);
                 
                 
             }
         }
         
     }
     
     public boolean isTouching(int mx, int my) {
         Point prev = null;
         for(Point p : points) {
             if(prev!=null) {
                 double res = 100;
                 //double res = (double)(((double)mx-(double)prev.x)*((double)p.x-(double)prev.x) + ((double)my-(double)prev.y)*((double)p.y-(double)prev.y))/Math.pow(((double)prev.x-(double)p.x)*((double)prev.x-(double)p.x) + ((double)prev.y-(double)p.y)*((double)prev.y-(double)p.y), 2);
                 if(res<7) {
                     System.out.println("Highlight + " + res);
                     return true;
                 }
             
             }
             prev = p;
         }
         return false;
     }
     
     public void addPoint(int _x, int _y) {
         points.add(new Point(_x, _y));
         if(points.size() > 1) {
             isValid = true;
         }
     }
     
     
     
     public void generatePoly()
     {
         poly = new Polygon();
         for(Point p : points)
         {
             poly.addPoint(p.x, p.y);
             
         }
         
     
     }
     
     
     
     
 }
