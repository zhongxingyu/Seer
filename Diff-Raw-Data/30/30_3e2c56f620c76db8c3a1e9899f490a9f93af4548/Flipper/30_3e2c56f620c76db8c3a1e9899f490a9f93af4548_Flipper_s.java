 package cz.minig;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.view.View;
 
 @SuppressLint("ViewConstructor")
 public class Flipper extends View {
 
     // Position properties
     private int rowCount;
     private int columnCount;
     private int index;
     // State properties
     private boolean state = true;
     // Drawing properties
     private Bitmap picture;
     private Rect sourceRect;
     private Rect targetRect;
     private Paint framePaint = new Paint();
 
     public Flipper(Context context) {
         super(context);
         framePaint.setStrokeWidth(0);
        framePaint.setColor(Color.GRAY);
         framePaint.setStyle(Paint.Style.STROKE);
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         if (state) {
             canvas.drawBitmap(picture, sourceRect, targetRect, null);
         } else {
             canvas.drawColor(Color.BLACK);
         }
        canvas.drawRect(targetRect, framePaint);
     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         if (w == 0 || h == 0) {
             return;
         }
         sourceRect = createSourceRect(w, h);
         targetRect = new Rect(0, 0, w, h);
     }
 
     private Rect createSourceRect(int tileWidth, int tileHeight) {
         float layoutRatio = 1F * tileWidth * columnCount / (tileHeight * rowCount);
         float imageRatio = 1F * picture.getWidth() / picture.getHeight();
         float sourceWidth = (imageRatio > layoutRatio) ?
                 layoutRatio * picture.getHeight() :
                 picture.getWidth();
         float sourceHeight = (imageRatio > layoutRatio) ?
                 picture.getHeight() :
                 picture.getWidth() / layoutRatio;
         float tileSourceWidth = sourceWidth / columnCount;
         float tileSourceHeight = sourceHeight / rowCount;
         float tileSourceX = (picture.getWidth() - sourceWidth) / 2 +
                 tileSourceWidth * (index % columnCount);
         float tileSourceY = (picture.getHeight() - sourceHeight) / 2 +
                 tileSourceHeight * (index / columnCount);
         return new Rect((int) tileSourceX, (int) tileSourceY,
                 (int) (tileSourceX + tileSourceWidth),
                 (int) (tileSourceY + tileSourceHeight));
     }
 
     public void setPosition(int rowCount, int columnCount, int index) {
         this.rowCount = rowCount;
         this.columnCount = columnCount;
         this.index = index;
     }
 
     public void setPicture(Bitmap picture) {
         this.picture = picture;
     }
 
     public void reverseState() {
         state = !state;
         invalidate();
     }
 
     public void resetState() {
         state = true;
         invalidate();
     }
 
     public boolean isFlipped() {
         return !state;
     }
 
     public int getIndex() {
         return index;
     }
 
 }
