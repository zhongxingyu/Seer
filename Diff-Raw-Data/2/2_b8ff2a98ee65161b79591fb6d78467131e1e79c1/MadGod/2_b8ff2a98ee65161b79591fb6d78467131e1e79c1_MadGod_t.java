 import java.awt.*;
 import java.awt.image.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 public class MadGod {
 
   //TODO dual monitor issue
   private static int LEFT_MONITOR_SIZE = 1920;
 
   private static int RED   = 0;
  private static int GREEN = 1;
   private static int BLUE  = 2;
 
   private static int REST_TIME = 100;
 
   private static double MINIMAP_X_RATIO    = 1.01;
   private static double MINIMAP_SIZE_RATIO = 0.32;
 
   private static double BAR_WIDTH_RATIO = 0.29;
   private static double BAR_X_RATIO   = 1.035;
   private static double LEVEL_Y_RATIO = 0.40;
   private static double HP_Y_RATIO    = 0.44;
   private static double MP_Y_RATIO    = 0.48;
 
   private int screenX;
   private int screenY;
   private int screenLength;
 
   private int barStartX;
   private int barWidth;
   private int levelY;
   private int hpY;
   private int mpY;
 
   private int mapStartX;
   private int mapStartY;
   private int mapWidth;
 
   private Robot control;
 
   public MadGod(Robot controller, Point topLeft, Point bottomRight) {
     control = controller;
     screenX = (int)(topLeft.getX() - LEFT_MONITOR_SIZE);
     screenY = (int)(topLeft.getY());
     screenLength = (int)(bottomRight.getX() - topLeft.getX());
 
     mapStartX = (int)(MINIMAP_X_RATIO * screenLength + screenX);
     mapStartY = (4 + screenY); //ew
     mapWidth  = (int)(MINIMAP_SIZE_RATIO * screenLength);
 
     barStartX = (int)(BAR_X_RATIO * screenLength + screenX);
     barWidth  = (int)(BAR_WIDTH_RATIO * screenLength);
     levelY    = (int)(LEVEL_Y_RATIO * screenLength + screenY);
     hpY = (int)(HP_Y_RATIO * screenLength + screenY);
     mpY = (int)(MP_Y_RATIO * screenLength + screenY);
   }
   // Returns HP on a scale of 1 to 100
   public int getHP() {
     return getBar(hpY, RED);
   }
 
   // Returns Level Progress on a scale of 1 to 100
   public int getLevelProgress() {
     return getBar(levelY, GREEN);
   }
 
   public int getMP() {
     return getBar(mpY, BLUE);
   }
 
   private int getBar(int height, int colorType) {
     Rectangle bounds  = new Rectangle(barStartX, height, barWidth, 2);
     BufferedImage bar = control.createScreenCapture(bounds);
     Raster pixels = bar.getData();
 
     int result = 0;
     int maintainedWeight = 5;
     for(int x = 0; x < barWidth; x += barWidth / 100) {
       int[] pixel = new int[3];
       pixels.getPixel(x, 1, pixel);
       if(!barMaintained(pixel, colorType)) {
         maintainedWeight--;
         if(maintainedWeight <= 0)
           break;
       } else
         maintainedWeight = Math.min(5, maintainedWeight + 1);
       result += barWidth / 100;
     }
     return (int)((1.0 * result / barWidth) * 100);
   }
 
   private boolean barMaintained(int[] pixel, int colorType) {
     boolean correct = true;
     for(int col = 0; col < 3; col++) {
       if(colorType != col && pixel[col] >= pixel[colorType] && pixel[col] < 200)
         correct = false;
     }
     return correct;
   }
 
   // Returns a List of enemy coordinates where current player is at (0, 0)
   public List<int[]> getEnemies() {
     return MadGod.cartesianToPolar(getMapType(RED));
   }
 
   // Returns a List of ally coordinates where current player is at (0, 0)
   // Not well accurate because allies can clump
   public List<int[]> getAllies() {
     List<int[]> results =  MadGod.cartesianToPolar(getMapType(GREEN));
     System.out.println("-----------------------------------------");
     for(int[] r: results) {
       System.out.println(Arrays.toString(r));
     }
     return results;
   }
 
   private List<Point> getMapType(int colorType) {
     List<Point> results = new ArrayList<Point>();
     Rectangle bounds    = new Rectangle(mapStartX, mapStartY, mapWidth, mapWidth);
     BufferedImage map   = control.createScreenCapture(bounds);
     Raster pixels = map.getData();
 
     for(int x = 0; x < mapWidth; x += mapWidth / 80) {
       for(int y = 0; y < mapWidth; y += mapWidth / 80) {
         int[] pixel = new int[3];
         pixels.getPixel(x, y, pixel);
 
         if(mapTargetIdentified(pixel, colorType)) {
           int itemX = x - (mapWidth / 2);
           int itemY = (y - (mapWidth / 2)) * -1;
           results.add(new Point(itemX, itemY));
         }
       }
     }
     return results;
   }
 
   private boolean mapTargetIdentified(int[] pixel, int colorType) {
     boolean correct = true;
     for(int col = 0; col < 3; col++) {
       if(col == colorType) {
         if(pixel[col] < 240)
           correct = false;
       } else {
         if(pixel[col] > 15)
           correct = false;
       }
     }
     return correct;
   }
 
   public static List<int[]> cartesianToPolar(List<Point> ps) {
     List<int[]> results = new ArrayList<int[]>();
     // for(int i = 0; i < ps.size(); i++) {
     //   results.add(cartesianToPolar(ps.get(i)));
     // }
     return results;
   }
 
   public static int[] cartesianToPolar(Point p) {
     double radians = (Math.atan2(p.getX(), p.getY()));
     int degrees    = (int)(radians / (2 * Math.PI) * 360);
     if(degrees < 0) {
       degrees *= -1;
       degrees  = 180 - degrees;
       degrees += 180;
     }
 
     int distance = (int)(Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY()));
     int[] polar  = {distance, degrees};
     return polar;
   }
 
   public void rest() {
     control.delay(REST_TIME);
   }
 
 }
