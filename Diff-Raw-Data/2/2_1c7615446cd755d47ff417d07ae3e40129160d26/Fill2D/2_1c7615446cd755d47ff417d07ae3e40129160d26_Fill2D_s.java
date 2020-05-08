 package amber.gui.editor.map.tool._2d;
 
 import amber.data.map.Tile;
 import amber.data.res.Tileset;
 import amber.gui.editor.map.MapContext;
 import java.awt.Point;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 
 /**
  *
  * @author Tudor
  */
 public class Fill2D extends Brush2D {
 
     public Fill2D(MapContext context) {
         super(context);
     }
 
    protected boolean floodFillAt(int x, int y) {
         boolean modified = false;
         if (isInBounds(x, y)) {
             Tileset.TileSprite target = spriteAt(x, y);
             Stack<Point> stack = new Stack<Point>() {
                 Set<Point> visited = new HashSet<Point>();
 
                 @Override
                 public Point push(Point t) {
                     return visited.add(t) ? super.push(t) : t;
                 }
             };
 
             stack.push(new Point(x, y));
             while (!stack.empty()) {
                 Point p = stack.pop();
                 if (spriteAt(p.x, p.y) != target) {
                     continue;
                 }
 
                 if (super.apply(p.x, p.y)) {
                     modified = true;
                 }
                 if (target == spriteAt(p.x - 1, p.y)) {
                     stack.push(new Point(p.x - 1, p.y));
                 }
                 if (target == spriteAt(p.x + 1, p.y)) {
                     stack.push(new Point(p.x + 1, p.y));
                 }
                 if (target == spriteAt(p.x, p.y - 1)) {
                     stack.push(new Point(p.x, p.y - 1));
                 }
                 if (target == spriteAt(p.x, p.y + 1)) {
                     stack.push(new Point(p.x, p.y + 1));
                 }
             }
         }
         return modified;
     }
 
     protected Tileset.TileSprite spriteAt(int x, int y) {
         if (isInBounds(x, y)) {
             Tile tile = context.map.getLayer(context.layer).getTile(x, y, 0);
             return tile != null ? tile.getSprite() : Tileset.TileSprite.NULL_SPRITE;
         }
         return null;
     }
 }
