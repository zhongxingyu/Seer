 package com.WordFinder;
 
 import android.graphics.Rect;
 import android.graphics.BitmapFactory;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.View;
 import java.util.Observable;
 import java.util.Observer;
 import static java.lang.Math.*;
 
 // -------------------------------------------------------------------------
 /**
  * Character Grid
  *
  * @author John Mooring (jmooring)
  * @author Bryan Malyn (bmalyn)
  * @version Oct 30, 2011
  */
 public class LetterGridView
     extends View
     implements Observer
 {
 
     private LetterGrid model;
     private Bitmap     upButton;
     private Bitmap     downButton;
     private Bitmap     badButton;
     private Bitmap     goodButton;
 
 
     // ----------------------------------------------------------
     /**
      * Create a new LetterGridView object.
      *
      * @param context
      *            the context in which this was created
      * @param attrs
      *            the xml attributes of the view.
      */
     public LetterGridView(Context context, AttributeSet attrs)
     {
         super(context, attrs);
 
         upButton =
             BitmapFactory.decodeResource(
                 context.getResources(),
                 R.drawable.button_up);
         downButton =
             BitmapFactory.decodeResource(
                 context.getResources(),
                 R.drawable.button_down);
         badButton =
             BitmapFactory.decodeResource(
                 context.getResources(),
                 R.drawable.button_good);
         goodButton =
             BitmapFactory.decodeResource(
                 context.getResources(),
                 R.drawable.button_bad);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the model add attaches a observer to it.
      *
      * @param model
      */
     public void setModel(LetterGrid model)
     {
         model.addObserver(this);
         this.model = model;
     }
 
 
     public void onDraw(Canvas c)
     {
         if (model != null)
         {
             Paint paint = new Paint();
             for (int x = 0; x < model.size(); x++)
             {
                 for (int y = 0; y < model.size(); y++)
                 {
 
                     Rect drawArea =
                         new Rect(
                             convertToCanvasSize(x),
                             convertToCanvasSize(y),
                             convertToCanvasSize(x + 1) - 1,
                             convertToCanvasSize(y + 1) - 1);
                     Bitmap toDraw = null;
                     switch (model.getTile(x, y).getState())
                     {
                         case UP:
                             toDraw = upButton;
                             break;
                         case DOWN:
                             toDraw = downButton;
                             break;
                         case GOOD:
                             toDraw = goodButton;
                             break;
                         case BAD:
                             toDraw = badButton;
                             break;
                     }
                     c.drawBitmap(toDraw, null, drawArea, paint);
                     c.drawText(
                         model.getTile(x, y).getLetter() + "",
                         convertToCanvasSize(x),
                         convertToCanvasSize(y),
                         convertToCanvasSize(x + 1) - 1,
                         convertToCanvasSize(y + 1) - 1,
                         paint);
                 }
             }
         }
     }
 
 
     /**
      * Convert cell number to pixel size.
      *
      * @param cellNumber
      *            to be converted
      * @return float the converted number
      */
     private int convertToCanvasSize(int cellNumber)
     {
         return cellNumber * getWidth() / model.size();
     }
 
 
     /**
      * Convert pixel size to cell number.
      *
      * @param canvasSize
      *            the converted number
      * @return int to be converted
      */
     private int[] convertToCellNumber(float x, float y)
     {
         int[] toReturn = null;
        float diameter = model.size() / (float)getWidth();
         float radius = diameter / 2;
         float scaledX = x % diameter;
         float scaledY = y % diameter;
 
         if (pow(radius - scaledX, 2) + pow(radius - scaledY, 2) <= pow(
             radius,
             2))
         {
             toReturn = new int[2];
             toReturn[0] = (int)(x / diameter);
             toReturn[1] = (int)(y / diameter);
         }
         return toReturn;
 
     }
 
 
     // ----------------------------------------------------------
     /**
      * Overridden to force the view to be square (have the same width and
      * height).
      *
      * @param widthMeasureSpec
      *            the desired width as determined by the layout
      * @param heightMeasureSpec
      *            the desired height as determined by the layout
      */
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
     {
         // Choose the smallest of the two dimensions to use for both.
         int measureSpec = Math.min(widthMeasureSpec, heightMeasureSpec);
 
         // Call the superclass implementation but pass it our modified width
         // and height instead of the incoming ones.
         super.onMeasure(measureSpec, measureSpec);
     }
 
 
     /**
      * When the model changes redraw the view
      */
     public void update(Observable observable, Object data)
     {
         postInvalidate();
     }
 }
