/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package timer;
 
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Ellipse2D.Float;
 
/**
 *
 * @author ilia
 */
 public class MyElipse {
 
     private Ellipse2D.Float elipse;
     private double size;

    @Override
    public String toString() {
        return "MyElipse{" + "elipse=" + elipse + ", size=" + size + ", stroke=" + stroke + '}';
    }
     private float stroke;
   
     public MyElipse() {
         elipse = new Ellipse2D.Float();
     }
 
     public Float getElipse() {
         return elipse;
     }
 
     public void setElipse(Float elipse) {
         this.elipse = elipse;
     }
 
     public double getSize() {
         return size;
     }
 
     public void setSize(double size) {
         this.size = size;
     }
 
     public float getStroke() {
         return stroke;
     }
 
     public void setStroke(float stroke) {
         this.stroke = stroke;
     }
 }
