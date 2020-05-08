 package cs437.som.visualization;
 
 import java.awt.Color;
 
 /**
  * A color progression from green (#00FF00) to red (#FF0000).
  */
 public class GreenToRedHeat implements ColorProgression {
     private static final float GREEN_HUE_SCALE = 1.2f;
     private static final float CIRCLE_DEG = 360.0f;
 
     @Override
     public Color getColor(int intensity) {
         if (intensity < 0)
             return Color.green;
         else if (intensity > 100)
             return Color.red;
         else {
            final float hue = ((intensity) * GREEN_HUE_SCALE) / CIRCLE_DEG;
             final int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
             return new Color(rgb);
         }
     }
 }
