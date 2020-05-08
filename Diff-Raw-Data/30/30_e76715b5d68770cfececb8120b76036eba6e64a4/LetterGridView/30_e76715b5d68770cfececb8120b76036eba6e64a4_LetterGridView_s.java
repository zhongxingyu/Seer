 package com.WordFinder;
 
 import java.util.Observable;
 import java.util.Observer;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
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
     private boolean    onTile;
     private boolean    wasOnTile;
 
 
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
                 R.drawable.button_bad);
         goodButton =
             BitmapFactory.decodeResource(
                 context.getResources(),
                 R.drawable.button_good);
         onTile = false;
         wasOnTile = false;
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
                     paint.setColor(0xFFFFFFFF);
                     paint.setTextAlign(Paint.Align.CENTER);
                     paint.setTextSize(convertToCanvasSize(1) * 0.5f);
                     c.drawText(
                         (char)(model.getTile(x, y).getLetter() + 'A' - 'a')
                             + "",
                         convertToCanvasSize(x) + convertToCanvasSize(1) / 2,
                         convertToCanvasSize(y) + convertToCanvasSize(1) * .65f,
                         paint);
                 }
             }
         }
     }
 
 
     /**
      * Called when a touch event occurs on the view; either pressing the finger
      * down for the first time, moving it on the screen, or lifting it back up.
      *
      * @param e
      *            a MotionEvent object that describes the touch event
      * @return true if this method handled the touch, or false if it did not
      */
     @Override
     public boolean onTouchEvent(MotionEvent e)
     {
         if (e.getAction() == MotionEvent.ACTION_DOWN
             || e.getAction() == MotionEvent.ACTION_MOVE)
         {
             if (e.getX() >= 0 && e.getX() < getWidth() && e.getY() >= 0
                 && e.getX() < getHeight())
             {
                 int scaledX =
                     (int)((e.getX() % convertToCanvasSize(1) / convertToCanvasSize(1)) * upButton
                         .getWidth());
                 int scaledY =
                     (int)((e.getY() % convertToCanvasSize(1) / convertToCanvasSize(1)) * upButton
                         .getWidth());
                 onTile = Color.alpha(upButton.getPixel(scaledX, scaledY)) > 128;
                 if (onTile && !wasOnTile)
                 {
                     model.setSelected(convertToTile(e.getX(), e.getY()));
                 }
                 wasOnTile = onTile;
             }
 
             return true;
 
         }
         else if (e.getAction() == MotionEvent.ACTION_UP)
         {
             model.submitWord();
             onTile = false;
             wasOnTile = false;
             return true;
         }
         else
         {
             // For any other touch event, ignore it.
             return false;
         }
     }
 
 
     /**
      * Convert cell number to pixel size.
      *
      * @pa lNumber to be converted
      * @return float the converted number
      */
     private int convertToCanvasSize(int cellNumber)
     {
         return cellNumber * getWidth() / model.size();
     }
 
 
     /**
      * Convert pixel size to cell number.
      *
      * @pa vasSize the converted number
      * @return int to be converted
      */
     private Tile convertToTile(float x, float y)
     {
         return model.getTile(
            (int)(x / convertToCanvasSize(1)),
            (int)(y / convertToCanvasSize(1)));
     }
 
 
     // ----------------------------------------------------------
     /**
      * Overridden to force the view to be square (have the same width and
      * height).
      *
      * @pa thMeasureSpec the desired width as determined by the layout
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
