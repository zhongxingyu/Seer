 package de.hswt.hrm.plant.ui.schemeeditor;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 import de.hswt.hrm.plant.model.GridImage;
 
 import static com.google.common.base.Preconditions.*;
 
 /**
  * A Widget that displays components in a grid.
  * 
  * @author Michael Sieger
  * 
  */
 public class SchemeGrid extends Canvas{
     
     private static class GridImageContainer{
         GridImage image;
         int x, y;
         public GridImageContainer(GridImage image, int x, int y) {
             super();
             this.image = image;
             this.x = x;
             this.y = y;
         }
         
         /**
          * Returns true if the image intersects the other image.
          * 
          * @param o
          * @return
          */
         boolean intersects(GridImageContainer o){
             //TODO some better implementation
             final int w1 = image.getWidth();
             final int h1 = image.getHeight();
             final int w2 = o.image.getWidth();
             final int h2 = o.image.getHeight();
             for(int i = 0; i < w1; i++){
                 for(int j = 0; j < h1; j++){
                     for(int k = 0; k < h2; k++){
                         for(int n = 0; n < w2; n++){
                             final int x1 = j + x;
                             final int y1 = i + y;
                             final int x2 = n + o.x;
                             final int y2 = k + o.y;
                             if(x1 == x2 && y1 == y2){
                                 return true;
                             }
                         }
                     }
                 }
             }
             return false;
         }
     }
     
     private final List<GridImageContainer> images = new ArrayList<>();
     private final int width, height;
     
     public SchemeGrid(Composite parent, int style, int width, int height) {
         super(parent, style);
         this.width = width;
         this.height = height;
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
         for(GridImageContainer container : images){
             drawImage(gc, container.image, container.x, container.y);
         }
     }
 
     private void drawImage(GC gc, GridImage gridImage, int x, int y) {
         Image image = gridImage.getRenderedImage();
         final float quadW = getQuadWidth();
         final float quadH = getQuadHeight();
         Rectangle rec = image.getBounds();
         gc.drawImage(image, 0, 0, rec.width, rec.height, Math.round(quadW * x), Math.round(quadH * y),
                 Math.round(quadW * gridImage.getWidth()), Math.round(quadH * gridImage.getHeight()));
     }
 
     private void drawHorizontalLines(GC gc) {
         Rectangle rec = this.getBounds();
         final float quadH = getQuadHeight();
         for (int i = 0; i < height; i++) {
             final int y = Math.round(i * quadH);
             gc.drawLine(0, y, rec.width, y);
         }
     }
 
     private void drawVerticalLines(GC gc) {
         Rectangle rec = this.getBounds();
         final float quadW = getQuadWidth();
         for (int i = 0; i < width; i++) {
             final int x = Math.round(i * quadW);
             gc.drawLine(x, 0, x, rec.height);
         }
     }
 
     private float getQuadWidth() {
         return ((float) getBounds().width) / width;
     }
 
     private float getQuadHeight() {
         return ((float) getBounds().height) / height;
     }
 
     /**
      * The image is placed at the given grid position.
      * 
      * @param image
      * @param x
      * @param y
      * @throws PlaceOccupiedException
      */
     public void setImageAt(GridImage image, int x, int y) throws PlaceOccupiedException {
         final int w = image.getWidth();
         final int h = image.getHeight();
        checkArgument(x >= 0 && x + w < width);
        checkArgument(y >= 0 && y + h < height);
         GridImageContainer toInsert = new GridImageContainer(image, x, y);
         for(GridImageContainer c : images){
             if(c.intersects(toInsert)){
                 throw new PlaceOccupiedException("The image intersects with an image that is already there");
             }
         }
         images.add(toInsert);
         this.redraw();
     }
     
     /**
      * The image is placed at the given position in pixel.
      * The image snaps to the nearest grid position.
      * 
      * @param image
      * @param x
      * @param y
      * @throws PlaceOccupiedException
      */
     public void setImageAtPixel(GridImage image, int x, int y) throws PlaceOccupiedException{
         setImageAt(image, Math.round(((float)x)/getQuadWidth()), Math.round(((float)y)/getQuadHeight()));
     }
 
 }
