 package com.anotherbrick.inthewall;
 
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 
 public class VizPieChart extends VizPanel {
 
   private ArrayList<Float> values;
   private ArrayList<Float> angles;
   private ArrayList<MyColorEnum> palette;
   private float lastAngle;
 
  public VizPieChart(float x0, float y0, float diameter, float parentX0, float parentY0) {
    super(x0, y0, diameter, diameter, parentX0, parentY0);
   }
 
   public void setup(ArrayList<Float> values, ArrayList<MyColorEnum> palette) {
     this.values = values;
     this.angles = new ArrayList<Float>();
     this.palette = palette;
     lastAngle = 0;
     setAngles();
   }
 
   private void setAngles() {
 
     for (int i = 0; i < values.size(); i++) {
       float f = values.get(i);
       float sum = 0;
       for (int j = 0; j < values.size(); j++) {
         sum += values.get(j);
       }
 
       angles.add(f * (2 * PApplet.PI / sum));
     }
 
   }
 
   @Override
   public boolean draw() {
     pushStyle();
     lastAngle = 0;
     for (int i = 0; i < angles.size(); i++) {
       fill(palette.get(i));
      arc(x0, y0, width, width, lastAngle, lastAngle + angles.get(i));
       lastAngle += angles.get(i);
     }
     popStyle();
     return false;
   }
 
   public void changeColors(ArrayList<MyColorEnum> palette) {
     this.palette = palette;
 
   }
 
 }
