 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.Random;
 
 
 public class Grass extends Entity {
     
    int GRASS_WIDTH = 900;
     int SEGMENT_WIDTH = 100;
     int segments = GRASS_WIDTH / SEGMENT_WIDTH;
     int[] segmentX;
     int[] heights;
     Random random = new Random();
     
     public Grass() {
         segmentX = new int[segments];
         heights = new int[segments];
         heights[0] = 500;
         segmentX[0] = 0;
         for (int i = 1; i < segments; i++) {
             segmentX[i] = i * SEGMENT_WIDTH;
             heights[i] = heights[i - 1] + random.nextInt(SEGMENT_WIDTH) - SEGMENT_WIDTH / 2;
         }
     }
 
     @Override
     void draw(Graphics g) {
         ((Graphics2D) g).setStroke(new BasicStroke(10));
         g.setColor(Color.GREEN);
         g.drawPolyline(segmentX, heights, segments);
     }
 
     public int getGrassHeight(int x) {
         if (x > GRASS_WIDTH - SEGMENT_WIDTH - 1) return 1000;
 
         int segment = x / SEGMENT_WIDTH;
         float segmentOffset = (x % SEGMENT_WIDTH) / (float) SEGMENT_WIDTH;
 
         int y1 = heights[segment];
         int y2 = heights[segment + 1];
 
         int grassHeight = (int) (y1 + segmentOffset * (y2 - y1));
 
         return grassHeight - 25;
     }
 
 }
