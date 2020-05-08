 package com.anotherbrick.inthewall;
 
 import static com.anotherbrick.inthewall.Helper.*;
 import processing.core.PApplet;
 import processing.core.PVector;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 import com.anotherbrick.inthewall.Model.Datas;
 
 public class VizTableRow extends VizPanel {
 
   private static final int ROW_HEIGHT = 0;
   private static final float MOVIE_ROW_X = 0;
   private static final float YEAR_VALUE_X = 0;
   private static final float FIRST_PLOT_X = 0;
   private static final float SECOND_PLOT_X = 0;
   private static final float THIRD_PLOT_X = 0;
   private static final float FOURTH_PLOT_X = 0;
   private PVector[] values;
   public boolean selected = false;
   public MyColorEnum backgroundColorSelected, backgroundColor, strokeColor;
 
  public VizTableRow(float x0, float y0, float width, float height, float parentX0, float parentY0) {
    super(x0, y0, width, height, parentX0, parentY0);
     values = new PVector[4];
   }
 
   public void setValues(PVector[] values) {
     this.values = values;
   }
 
   public PVector[] getValues() {
     return this.values;
   }
 
   public void addValue(PVector value, int index) {
     values[index] = value;
   }
 
   public boolean drawStrings(String name) {
 
     pushStyle();
     noStroke();
     textSize(12);
     textAlign(PApplet.LEFT, PApplet.BASELINE);
     text(name, MOVIE_ROW_X, ROW_HEIGHT - 1);
     popStyle();
 
     return false;
   }
 
   public void drawBackground() {
     pushStyle();
     if (selected) {
       background(backgroundColorSelected);
     } else {
       background(backgroundColor);
     }
     if (strokeColor != null) {
       stroke(strokeColor);
     } else {
       noStroke();
     }
     popStyle();
   }
 
   public boolean draw() {
     pushStyle();
     drawBackground();
     textSize(11);
     textAlign(PApplet.CENTER, PApplet.BASELINE);
     if (values != null && values.length > 0) {
       int year = 0;
       for (int i = 0; i < values.length; i++) {
         if (values[i] != null) {
           year = (int) values[i].x;
           break;
         }
       }
       if (year != 0) {
         text(Integer.toString(year), YEAR_VALUE_X, ROW_HEIGHT - 1);
         textAlign(PApplet.RIGHT, PApplet.BASELINE);
       }
 
       int nOfDecimals;
       if (m.currentDataDisplayed == Datas.AVERAGE_VOTES) {
         nOfDecimals = 1;
       } else {
         nOfDecimals = 0;
       }
 
       if (values[0] != null)
         text(floatToString((Float) values[0].y, nOfDecimals, true), FIRST_PLOT_X, ROW_HEIGHT - 2);
       if (values[1] != null)
         text(floatToString((Float) values[1].y, nOfDecimals, true), SECOND_PLOT_X, ROW_HEIGHT - 2);
       if (values[2] != null)
         text(floatToString((Float) values[2].y, nOfDecimals, true), THIRD_PLOT_X, ROW_HEIGHT - 2);
       if (values[3] != null)
         text(floatToString((Float) values[3].y, nOfDecimals, true), FOURTH_PLOT_X, ROW_HEIGHT - 2);
     }
     popStyle();
     return false;
   }
 }
