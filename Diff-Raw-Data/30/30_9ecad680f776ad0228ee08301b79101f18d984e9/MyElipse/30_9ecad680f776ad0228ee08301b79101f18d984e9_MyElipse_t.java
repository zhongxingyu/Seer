 package timer;
 
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Ellipse2D.Float;
 
 public class MyElipse {
 
     private Ellipse2D.Float elipse;
     private double size;
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

    @Override
    public String toString() {
        return "MyElipse{" + "elipse=" + elipse + ", size=" + size + ", stroke=" + stroke + '}';
    }
 }
