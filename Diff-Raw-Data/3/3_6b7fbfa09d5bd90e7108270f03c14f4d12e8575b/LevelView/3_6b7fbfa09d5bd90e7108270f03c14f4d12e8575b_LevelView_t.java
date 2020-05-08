 package net.vincentpetry.nodereviver.view;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import net.vincentpetry.nodereviver.model.Edge;
 import net.vincentpetry.nodereviver.model.Level;
 import net.vincentpetry.nodereviver.model.Node;
 
 public class LevelView extends View {
 
     private Bitmap levelBitmap;
     private Canvas bitmapCanvas;
     private Level level;
 
     private Paint nodePaint;
     private Paint edgePaint;
     private Paint markedEdgePaint;
 
     private ViewContext viewContext;
 
     // temp rect that can be reused
     private Rect rect;
 
     public LevelView(Level level, ViewContext viewContext) {
         this.level = level;
         this.levelBitmap = null;
         this.bitmapCanvas = null;
         this.viewContext = viewContext;
         this.rect = new Rect();
     }
 
     public void init(int width, int height) {
         this.levelBitmap = Bitmap.createBitmap(width, height,
                 Bitmap.Config.RGB_565);
         this.bitmapCanvas = new Canvas(this.levelBitmap);
         this.nodePaint = new Paint();
         this.nodePaint.setColor(Color.rgb(255, 255, 255));
         this.edgePaint = new Paint();
         this.edgePaint.setColor(Color.rgb(128, 128, 128));
         this.edgePaint.setStrokeWidth(3);
         this.markedEdgePaint = new Paint();
         this.markedEdgePaint.setColor(Color.rgb(0, 255, 255));
         this.markedEdgePaint.setStrokeWidth(3);
 
         redrawLevel();
     }
 
     public void setLevel(Level level) {
         this.level = level;
         redrawLevel();
     }
 
     public void render(Canvas c) {
         if (level == null || levelBitmap == null) {
             return;
         }
         if (level.isDirty()) {
             redrawLevel();
         }
         c.drawBitmap(levelBitmap, 0, 0, null);
     }
 
     private void redrawLevel() {
        if (this.bitmapCanvas == null){
            return;
        }
         this.bitmapCanvas.drawARGB(255, 0, 0, 0);
 
         if (level == null) {
             return;
         }
         for (Edge edge : level.getEdges()) {
             this.drawEdge(edge);
         }
 
         for (Node node : level.getNodes()) {
             if (node.getType() == Node.TYPE_SQUARE) {
                 this.drawNode(node);
             }
         }
     }
 
     private void drawEdge(Edge edge) {
         Node node1 = edge.getSourceNode();
         Node node2 = edge.getTargetNode();
         int x1 = node1.getX();
         int y1 = node1.getY();
         int x2 = node2.getX();
         int y2 = node2.getY();
         this.bitmapCanvas.drawLine(x1, y1, x2, y2,
                 edge.isMarked() ? markedEdgePaint : edgePaint);
 
         if (edge.isOneWay() && node2.getType() != Node.TYPE_JOINT) {
             // Draw arrow
             int diffX = node2.getX() - node1.getX();
             int diffY = node2.getY() - node1.getY();
             int offsetX = 0;
             int offsetY = 0;
             int spriteIndex = 0;
 
             if (diffX == 0) {
                 if (diffY < 0) {
                     spriteIndex = SpriteManager.SPRITE_ARROW_UP;
                     offsetX = -6;
                     offsetY = 4;
                 }
                 else {
                     spriteIndex = SpriteManager.SPRITE_ARROW_DOWN;
                     offsetX = -6;
                     offsetY = -10;
                 }
             }
             else {
                 if (diffX < 0) {
                     spriteIndex = SpriteManager.SPRITE_ARROW_LEFT;
                     offsetX = 4;
                     offsetY = -6;
                 }
                 else {
                     spriteIndex = SpriteManager.SPRITE_ARROW_RIGHT;
                     offsetX = -10;
                     offsetY = -6;
                 }
             }
             if (edge.isMarked()) {
                 spriteIndex += 4;
             }
 
             x2 += offsetX;
             y2 += offsetY;
             viewContext.getSpriteManager().draw(spriteIndex, x2, y2,
                     this.bitmapCanvas);
         }
     }
 
     private void drawNode(Node node) {
         this.rect.left = node.getX() - 5;
         this.rect.top = node.getY() - 5;
         this.rect.right = this.rect.left + 10;
         this.rect.bottom = this.rect.top + 10;
         viewContext.getSpriteManager().draw(SpriteManager.SPRITE_NODE_NORMAL,
                 rect, this.bitmapCanvas);
     }
 }
