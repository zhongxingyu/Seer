 package interiores.presentation.swing.views.map;
 
 import interiores.business.models.Orientation;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author hector
  */
 public class Walls implements Drawable {
     private static final int DEPTH = 5;
     private static final Color COLOR = Color.decode("#999999");
     private int width;
    private int depth;
     private Map<Point, WallElement> elements;
     
     public Walls(int width, int height) {
         this.width = width;
        this.depth = height;
         elements = new HashMap();
     }
     
     public static int getDepth() {
         return DEPTH;
     }
     
     public static Color getColor() {
         return COLOR;
     }
     
     @Override
     public void draw(Graphics2D g) {
         g.setColor(COLOR);
         
         drawHorizontalWall(g, 0, DEPTH);
        drawHorizontalWall(g, depth, 0);
         
         drawVerticalWall(g, 0, DEPTH);
         drawVerticalWall(g, width, 0);
         
         for(WallElement element : elements.values())
             element.draw(g);
     }
     
     public void drawHorizontalWall(Graphics2D g, int y, int valign) {
         int padding = GridMap.getPadding();
         int drawableWidth = width + DEPTH;
         
         for(int i = 0 - DEPTH; i < drawableWidth; i += DEPTH) {
             Point p = new Point(i, y - valign);
         
             if(elements.containsKey(p))
                 i += elements.get(p).getSize() - DEPTH;
             else
                 g.fillRect(i + padding, y + padding - valign, DEPTH, DEPTH);
         }
     }
     
     public void drawVerticalWall(Graphics2D g, int x, int halign) {
         int padding = GridMap.getPadding();
         
        for(int i = 0; i < depth; i += DEPTH) {
             Point p = new Point(x - halign, i);
         
             if(elements.containsKey(p))
                 i += elements.get(p).getSize() - DEPTH;
             else
                 g.fillRect(x + padding - halign, i + padding, DEPTH, DEPTH);
         }
     }
     
     private Point getPosition(Orientation wall, int displacement) {
         Point p = new Point(-DEPTH, -DEPTH);
         
         switch(wall) {
             case E:
                 p.x = width;
                 
             case W:
                 p.y = displacement;
                 break;
             
             case S:
                p.y = depth;
                 
             case N:
                 p.x = displacement;
                 break;
         }
         
         return p;
     }
     
     public void addDoor(Door door, Orientation wall, int displacement) {        
         Point key = getPosition(wall, displacement);
         door.setPosition(key.x, key.y, door.hasToOpenOutwards() ? wall : wall.complementary());
         
         elements.put(key, door);
     }
     
     public void addWindow(Window window, Orientation wall, int displacement) {
         Point key = getPosition(wall, displacement);
         window.setPosition(key.x, key.y, wall.rotateRight());
         
         elements.put(key, window);
     }
 }
