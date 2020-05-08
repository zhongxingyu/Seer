 package akrasia.environment;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 import akrasia.AException;
 
 /**
  * This ia a random map generator.
  */
 public class LevelGenerator {
     public LevelGenerator(LevelMap m) {
         this(m, 0, m.width, 0, m.height);
     }
     public LevelGenerator(LevelMap m, int x, int w, int y, int h) {
        map = m;
         x_offset = x;
         y_offset = y;
         width = w;
         height = h;
         
         try{
             TestDimensions();
         }
         catch(AException e){
             e.printStackTrace();
         }
         
         Generate();
     }
     
     int x_offset   = 0;
         int width  = 0;
     int y_offset   = 0;
         int height = 0;
     
     LevelMap map;
     
     ArrayList<Rectangle> rooms = new ArrayList<Rectangle>();
 
     private void Generate() {
         for(int i = 0; i < 10; i++){
             
         }
     }
     
     public void RectangularRoomTest(int x, int y, int w, int h){
         ArrayList<Point> points = new ArrayList<Point>();
        for(int xx = x; xx <= x+w; xx++){
            for(int yy = y; yy <= y+h; yy++){
                 points.add(new Point(xx,yy));
             }
         }

         for(Point p : points){
             if(map.FloorTiles.containsKey(p)){
                 System.out.println("Intersection");
             }
             else{
                 map.FloorTiles.put(p, -1);
             }
             
         }
     }
     
     public void TestDimensions() throws AException{
         // Check that basic fields are unsigned
         if (x_offset < 0 || width < 0 || y_offset < 0 || height < 0){
             throw new AException(AException.ExceptionType.INVALID_DIMENSIONS);
         }
         
         if (x_offset + width > map.width || y_offset + height > map.height){
             throw new AException(AException.ExceptionType.OUT_OF_MAP_BOUNDS);
         }
     }
 }
