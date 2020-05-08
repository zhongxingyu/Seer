 package uk.org.smithfamily.mslogger.widgets;
 
 import uk.org.smithfamily.mslogger.R;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 
 public class Histogram extends Indicator
 {    
     private int             diameter;
     
     private Paint           backgroundPaint;
     private Paint           linePaint;
     private Paint           valuePaint;
     
     private static final int NB_VALUES   = 50;
     private double[]        values      = new double[NB_VALUES];
     private int             indexValue  = 0;
     
     /**
      * 
      * @param context
      */
     public Histogram(Context context)
     {
         super(context);
         init(context);
     }
 
     /**
      * 
      * @param c
      * @param s
      */
     public Histogram(Context c, AttributeSet s)
     {
         super(c, s);
         init(c);
     }
 
     /**
      * 
      * @param context
      * @param attr
      * @param defaultStyles
      */
     public Histogram(Context context, AttributeSet attr, int defaultStyles)
     {
         super(context, attr, defaultStyles);
         init(context);
     }
     
     /**
      * 
      * @param c
      */
     private void init(Context c)
     {
         initDrawingTools(c);
     }
     
     /**
      * 
      * @param context
      */
     private void initDrawingTools(Context context)
     {        
         int anti_alias_flag = Paint.ANTI_ALIAS_FLAG;
         if (this.isInEditMode())
         {
             anti_alias_flag = 0;
         }
         
         backgroundPaint = new Paint();
         backgroundPaint.setColor(Color.WHITE);
         backgroundPaint.setTextSize(0.06f);
         backgroundPaint.setTextAlign(Paint.Align.LEFT);
         backgroundPaint.setStyle(Paint.Style.STROKE);
         backgroundPaint.setFlags(anti_alias_flag);
         backgroundPaint.setAntiAlias(true);
         
         valuePaint = new Paint();
         valuePaint.setColor(Color.DKGRAY);
         valuePaint.setTextSize(0.06f);
         valuePaint.setTextAlign(Paint.Align.RIGHT);
         valuePaint.setFlags(anti_alias_flag);
         valuePaint.setAntiAlias(true);
         
         linePaint = new Paint();
         linePaint.setColor(Color.BLUE);
         linePaint.setFlags(anti_alias_flag);
         linePaint.setAntiAlias(true);       
     }
     
     /**
      * @param widthSpec
      * @param heightSpec
      */
     @Override
     protected void onMeasure(int widthSpec, int heightSpec)
     {
 
         int measuredWidth = MeasureSpec.getSize(widthSpec);
 
         int measuredHeight = MeasureSpec.getSize(heightSpec);
 
         /*
          * measuredWidth and measured height are your view boundaries. You need to change these values based on your requirement E.g.
          * 
          * if you want to draw a circle which fills the entire view, you need to select the Min(measuredWidth,measureHeight) as the radius.
          * 
          * Now the boundary of your view is the radius itself i.e. height = width = radius.
          */
 
         /*
          * After obtaining the height, width of your view and performing some changes you need to set the processed value as your view dimension by using the method setMeasuredDimension
          */
 
         diameter = Math.min(measuredHeight, measuredWidth);
         setMeasuredDimension(diameter, diameter);
 
         /*
          * If you consider drawing circle as an example, you need to select the minimum of height and width and set that value as your screen dimensions
          * 
          * int d=Math.min(measuredWidth, measuredHeight);
          * 
          * setMeasuredDimension(d,d);
          */
 
     }
 
     /**
      * @param value
      */
     @Override
     public void setValue(double value)
     {
         // We haven't reach the limit of the array yet
         if (indexValue < NB_VALUES)
         {
            values[indexValue++] = (float) value; 
         }
         // Otherwise we shift all the values and replace the last one with the new value
         else
         {
             int i;
             for (i = 0; i < NB_VALUES - 1; i++)
             {
                 values[i] = values[i + 1];
             }        
             
             values[i] = value;
         }
         
         super.setValue((float) value);
     }
     
     
     /**
      * @param canvas
      */
     @Override
     protected void onDraw(Canvas canvas)
     {
         int height = getMeasuredHeight();
 
         int width = getMeasuredWidth();
 
         float scale = (float) getWidth();
         canvas.save(Canvas.MATRIX_SAVE_FLAG);
         canvas.scale(scale, scale);
         float dx = 0.0f;
         float dy = 0.0f;
         if (width > height)
         {
             dx = (width - height) / 2.0f;
         }
         if (height > width)
         {
             dy = (height - width) / 2.0f;
         }
         canvas.translate(dx, dy);
         
         drawBackground(canvas);
         
         if (!isDisabled())
         {
             drawValue(canvas);
         }
 
         drawTitle(canvas);
         
         canvas.restore();     
     }
     
     public void drawBackground(Canvas canvas)
     {
        canvas.drawRect(0.05f, 0.05f, 0.95f, 0.88f, backgroundPaint);
     }
     
     public void drawValue(Canvas canvas)
     {
         valuePaint.setColor(getFgColour());
 
         float displayValue = (float) (Math.floor(getValue() / Math.pow(10, -getVd()) + 0.5) * Math.pow(10, -getVd()));
 
         String text;
 
         if (getVd() <= 0)
         {
             text = Integer.toString((int) displayValue);
         }
         else
         {
             text = Float.toString(displayValue);
         }
 
        canvas.drawText(text, 0.97f, 0.95f, valuePaint);
         
         // We need at least two pair of coords to draw a line
         if (indexValue > 1)
         {            
            final float x = 0.04f;
             final float y = 0.06f;
            final float height = 0.81f;
             final float pixelsBetweenValue = 0.018f;
             
             for (int i = 1; i < indexValue; i++)
             {
                 double oldValue = values[i - 1];
                 double currentValue = values[i];
               
                 double currentPercent = (1 - (currentValue - getMin()) / (getMax() - getMin()));
                 if (currentPercent < 0) currentPercent = 0;
                 if (currentPercent > 100) currentPercent = 100;
                 
                 double currentY = y + height * currentPercent;              
               
                 double oldPercent = (1 - (oldValue - getMin()) / (getMax() - getMin()));
                 if (oldPercent < 0) oldPercent = 0;
                 if (oldPercent > 100) oldPercent = 100;
                 
                 double oldX = x + (pixelsBetweenValue * i);
                 double oldY = y + height * oldPercent;     
                 
                 // Draw one value
                 canvas.drawLine((float) oldX, (float) oldY, (float) oldX + pixelsBetweenValue, (float) currentY, linePaint);
             }
         }
     }
     
     public void drawTitle(Canvas canvas)
     {
         backgroundPaint.setColor(getFgColour());
         
         String text = getTitle();
         if (!getUnits().equals(""))
         {
             text += " (" + getUnits() + ")";
         }
         
        canvas.drawText(text, 0.05f, 0.95f, backgroundPaint);
     }
     
     /**
      * 
      * @return
      */
     private int getFgColour()
     {
         int c = Color.WHITE;
         
         if (isDisabled())
         {
             return Color.DKGRAY;
         }
         
         if (getValue() > getLowW() && getValue() < getHiW())
         {
             return Color.WHITE;
         }
         else if (getValue() <= getLowW() || getValue() >= getHiW())
         {
             c = Color.YELLOW;
         }
         if (getValue() <= getLowD() || getValue() >= getHiD())
         {
             c = Color.RED;
         }
         
         return c;
     }
     
     /**
      * 
      */
     @Override
     protected void onAttachedToWindow()
     {
         super.onAttachedToWindow();
 
         IndicatorManager.INSTANCE.registerIndicator(this);
     }
 
     /**
      * 
      */
     @Override
     protected void onDetachedFromWindow()
     {
         super.onDetachedFromWindow();
 
         IndicatorManager.INSTANCE.deregisterIndicator(this);
     }
 
     @Override
     public String getType()
     {
         return getContext().getString(R.string.histogram);
     }   
 }
