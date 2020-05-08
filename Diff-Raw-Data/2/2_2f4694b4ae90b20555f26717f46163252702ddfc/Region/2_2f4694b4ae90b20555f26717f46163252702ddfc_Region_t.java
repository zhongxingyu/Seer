 package terminator.model;
 
 import java.awt.Dimension;
 import e.util.*;
 
 class Region {
         public static final Region INITIAL = new Region(new Dimension(1, 1), 0, 1);
 
         private Dimension area;
         private int top;
         private int bottom;
 
         private Region(Dimension area, int top, int bottom) {
                 this.area = area;
                 this.top = Math.max(0, top);
                 this.bottom = bottom < 0 ? (area.height - 1) :
                                            Math.min(bottom, area.height - 1);
         }
 
         public Region constrain(Dimension area) {
                return new Region(area, 0, area.height - 1);
         }
 
         public Region set(int top, int bottom) {
                 if (top >= bottom) {
                         Log.warn("Ignoring illegal scrolling region (" +
                                  top + ", " + bottom + ")");
                         return this;
                 }
                 return new Region(area, top, bottom);
         }
 
         public int top() {
                 return top;
         }
 
         public int bottom() {
                 return bottom;
         }
 }
