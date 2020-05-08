 package statsviewer;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ColorGenerator {
 
     public static List<Color> getColors(Integer count) {
 
         List<Color> colors = new ArrayList();
 
        Integer RGBInterval = getInterval(count);
         Integer currentColor = 0;
         for(int i=0;i<count;i++) {
             currentColor += RGBInterval;
            colors.add(new Color(currentColor));
         }
         return colors;
     }
 
    private static Integer getInterval(Integer count) {

        Integer interval = java.lang.Integer.MAX_VALUE / count;
         return interval;
     }
 }
