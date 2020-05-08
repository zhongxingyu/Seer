 package de.hswt.hrm.plant.ui.schemeeditor;
 
 
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 import de.hswt.hrm.plant.model.GridImage;
 
 /**
  * A Widget that displays scheme parts in a grid.
  * 
  * @author Michael Sieger
  * 
  */
 public class SchemeGrid extends Canvas{
     
    
 
     private final GridImage[][] quads;
 
     public SchemeGrid(Composite parent, int style, int width, int height) {
         super(parent, style);
         quads = new GridImage[height][width];
         super.addPaintListener(new PaintListener() {
 
             @Override
             public void paintControl(PaintEvent ev) {
                 draw(ev.gc);
             }
         });
       
     }
 
     private void draw(GC gc) {
         drawImages(gc);
         drawHorizontalLines(gc);
         drawVerticalLines(gc);
     }
 
     private void drawImages(GC gc) {
         final int w = getGridWidth();
         final int h = getGridHeight();
         boolean[][] drawn = new boolean[h][w];
         for (int i = 0; i < h; i++) {
             for (int j = 0; j < w; j++) {
                 if (!drawn[i][j] && quads[i][j] != null) {
                     final int imgW = getImageSpanWidth(j, i);
                     final int imgH = getImageSpanHeight(j, i);
                     drawImage(gc, quads[i][j], j, i, imgW, imgH);
                     fillTrue(drawn, j, i, imgW, imgH);
                 }
             }
         }
     }
 
     private void drawImage(GC gc, GridImage gridImage, int x, int y, int w, int h) {
         Image image = gridImage.getRenderedImage();
         final float quadW = getQuadWidth();
         final float quadH = getQuadHeight();
         Rectangle rec = image.getBounds();
        gc.drawImage(image, 0, 0, rec.x, rec.y, Math.round(quadW * x), Math.round(quadH * y),
                Math.round(quadW * w), Math.round(quadH));
     }
 
     private void fillTrue(boolean[][] arr, int x, int y, int w, int h) {
         for (int i = 0; i < h; i++) {
             for (int j = 0; j < w; j++) {
                 arr[i + y][j + x] = true;
             }
         }
     }
 
     private int getImageSpanWidth(int x, int y) {
         GridImage img = quads[y][x];
         final int w = getGridWidth();
         int i = x;
         while (i < w && quads[y][i] == img) {
             i++;
         }
         return i - x;
     }
 
     private int getImageSpanHeight(int x, int y) {
         GridImage img = quads[y][x];
         final int h = getGridHeight();
         int i = y;
         while (i < h && quads[i][x] == img) {
             i++;
         }
         return i - y;
     }
 
     private void drawHorizontalLines(GC gc) {
         Rectangle rec = this.getBounds();
         final int h = getGridHeight();
         final float quadH = getQuadHeight();
         for (int i = 0; i < h; i++) {
             final int y = Math.round(i * quadH);
             gc.drawLine(0, y, rec.width, y);
         }
     }
 
     private void drawVerticalLines(GC gc) {
         Rectangle rec = this.getBounds();
         final int w = getGridWidth();
         final float quadW = getQuadWidth();
         for (int i = 0; i < w; i++) {
             final int x = Math.round(i * quadW);
             gc.drawLine(x, 0, x, rec.height);
         }
     }
 
     private float getQuadWidth() {
         return ((float) getBounds().width) / getGridWidth();
     }
 
     private float getQuadHeight() {
         return ((float) getBounds().height) / getGridHeight();
     }
 
     private int getGridWidth() {
         return quads[0].length;
     }
 
     private int getGridHeight() {
         return quads.length;
     }
 
    public void setPartAt(GridImage image, int x, int y) {
         final int w = image.getWidth();
         final int h = image.getHeight();
         for (int i = y; i < y + h; i++) {
             for (int j = x; j < x + w; j++) {
                 quads[i][j] = image;
             }
         }
     }
 
 }
