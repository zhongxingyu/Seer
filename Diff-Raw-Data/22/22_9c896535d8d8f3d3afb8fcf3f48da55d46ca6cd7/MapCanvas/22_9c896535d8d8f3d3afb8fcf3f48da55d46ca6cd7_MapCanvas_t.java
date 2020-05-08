 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package routetaxies;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import javax.swing.JPanel;
 
 /**
  *
  * @author seth
  */
 public class MapCanvas extends JPanel implements MouseListener, MouseMotionListener{
 
     private double longitudeLeft = 23.91;
     private double longitudeRight = 24.1;
     private double latitudeBottom = 49.76;
     private double latitudeTop = 49.82;
 
     private int mouseDownX, mouseDownY;
 
     private int mouseX, mouseY;
 
     private MapModel model;
 
     public MapCanvas(){
         super();
         init();
     }
 
     public MapCanvas(MapModel model){
         super();
         init();
         this.model = model;
     }
 
     private void init(){
         addMouseMotionListener(this);
        addMouseListener(this);
         setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         setBackground(Color.WHITE);
     }
 
     @Override
     public void paintComponent(Graphics g){
         super.paintComponent(g);
         double latitude = yToLatitude(mouseY);
         double longitude = xToLongitude(mouseX);
         g.drawString(String.format("(%.6f; %.6f)", latitude, longitude), 10, 10);
     }
 
     public void mouseClicked(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mousePressed(MouseEvent e) {
         this.mouseDownX = e.getX();
         this.mouseDownY = e.getY();
        System.out.println("Mouse pressed");
     }
 
     public void mouseReleased(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseEntered(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseExited(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseDragged(MouseEvent e) {
         int x = e.getX(), y = e.getY();
         double dLongitude = xToLongitude(x) - xToLongitude(this.mouseDownX);
         double dLatitude = yToLatitude(y) - yToLatitude(this.mouseDownY);
        System.out.println(String.format("(%f, %f)", dLatitude, dLongitude));
        this.setLatitude_bottom(this.getLatitude_bottom() - dLatitude);
        this.setLatitude_top(this.getLatitude_top() - dLatitude);
        this.setLongitude_left(this.getLongitude_left() - dLongitude);
        this.setLongitude_right(this.getLongitude_right() - dLongitude);
        this.mouseDownX = x;
        this.mouseDownY = y;
     }
 
     public void mouseMoved(MouseEvent e) {
         mouseX = e.getX();
         mouseY = e.getY();
         this.repaint();
         //System.out.println(xToLongitude(e.getX()));
         //System.out.println(yToLatitude(e.getY()));
     }
 
     public MapModel getModel() {
         return model;
     }
 
     public void setModel(MapModel model) {
         this.model = model;
     }
 // <editor-fold defaultstate="collapsed" desc="Coordinate transformation functions">
     private double xToLongitude(int x) {
         double result = ((double) x) / getWidth();
         result *= (getLongitude_right() - getLongitude_left());
         result += getLongitude_left();
         return result;
     }
 
     private double yToLatitude(int y) {
         double result = 1 - ((double) y) / getHeight();
         result *= (getLatitude_top() - getLatitude_bottom());
         result += getLatitude_bottom();
         return result;
     }
 
     private int longitudeToX(double longitude) {
         double result = (longitude - getLongitude_left()) / (getLongitude_right() - getLongitude_left());
         result *= this.getWidth();
         return (int) result;
     }
 
     private int latitudeToY(double latitude) {
         double result = (latitude - getLatitude_bottom()) / (getLatitude_top() - getLatitude_bottom());
         result *= this.getHeight();
         result = this.getHeight() - result;
         return (int) result;
     }
 
     private Point degreesToXY(double latitude, double longitude) {
         Point pt = new Point();
         pt.y = (int) (((getLatitude_top() - latitude) / (getLatitude_top() - getLatitude_bottom())) * (this.getHeight()));
         pt.x = (int) ((longitude - getLongitude_left()) / (getLongitude_right() - getLongitude_left())) * (this.getWidth());
         return pt;
     }// </editor-fold>
 // <editor-fold defaultstate="collapsed" desc="Coordinate getters and setters">
     public double getLatitude_bottom() {
         return latitudeBottom;
     }
 
     public void setLatitude_bottom(double latitude_bottom) {
         this.latitudeBottom = latitude_bottom;
     }
 
     public double getLatitude_top() {
         return latitudeTop;
     }
 
     public void setLatitude_top(double latitude_top) {
         this.latitudeTop = latitude_top;
     }
 
     public double getLongitude_left() {
         return longitudeLeft;
     }
 
     public void setLongitude_left(double longitude_left) {
         this.longitudeLeft = longitude_left;
     }
 
     public double getLongitude_right() {
         return longitudeRight;
     }
 
     public void setLongitude_right(double longitude_right) {
         this.longitudeRight = longitude_right;
     }// </editor-fold>
 }
