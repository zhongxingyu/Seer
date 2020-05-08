 package uk.org.smithfamily.mslogger.widgets;
 
 import uk.org.smithfamily.mslogger.R;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 
 /**
  * 
  */
 public class NumericIndicator extends Indicator
 {
     final float             scale       = getResources().getDisplayMetrics().density;
     
     private Paint           backgroundPaint;
     private Paint           titlePaint;
     private Paint           valuePaint;
 	
     /**
      * 
      * @param context
      */
     public NumericIndicator(Context context)
     {
         super(context);
         init(context);
     }
 
     /**
      * 
      * @param c
      * @param s
      */
     public NumericIndicator(Context c, AttributeSet s)
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
     public NumericIndicator(Context context, AttributeSet attr, int defaultStyles)
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
         
         titlePaint = new Paint();
         titlePaint.setColor(Color.WHITE);
         titlePaint.setTextSize(0.08f);
         titlePaint.setTextAlign(Paint.Align.CENTER);
         titlePaint.setFlags(anti_alias_flag);
         titlePaint.setAntiAlias(true);
 
         valuePaint = new Paint();
         valuePaint.setColor(Color.WHITE);
         valuePaint.setTextSize(0.2f);
         valuePaint.setTextAlign(Paint.Align.CENTER);
         valuePaint.setFlags(anti_alias_flag);
         valuePaint.setAntiAlias(true);
         
         backgroundPaint = new Paint();
         backgroundPaint.setColor(Color.BLACK);
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
 
         int diameter = Math.min(measuredHeight, measuredWidth);
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
      * 
      * @param canvas
      */
     private void drawBackground(Canvas canvas)
     {
         backgroundPaint.setColor(getBgColour());
         
        canvas.drawRect(0.05f, 0.30f, 0.9f, 0.72f, backgroundPaint);
     }
     
     /**
      * 
      * @param canvas
      */
     private void drawTitle(Canvas canvas)
     {        
         titlePaint.setColor(getFgColour());
         
         String text = getTitle();
         if (!getUnits().equals(""))
         {
             text += " (" + getUnits() + ")";
         }
         
         canvas.drawText(text, 0.48f, 0.65f, titlePaint);
     }
 
     /**
      * 
      * @param canvas
      */
     private void drawValue(Canvas canvas)
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
 
         canvas.drawText(text, 0.5f, 0.5f, valuePaint);
     }
 	
 	
     /**
      * 
      * @return
      */
     private int getFgColour()
     {
         if (isDisabled())
         {
             return Color.DKGRAY;
         }
         if (getValue() > getLowW() && getValue() < getHiW())
         {
             return Color.WHITE;
         }
         else
         {
             return Color.BLACK;
         }
     }
 
     /**
      * 
      * @return
      */
     private int getBgColour()
     {
         int c = Color.GRAY;
         if (this.isDisabled())
         {
             return c;
         }
         if (getValue() > getLowW() && getValue() < getHiW())
         {
             return Color.BLACK;
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
         return getContext().getString(R.string.numeric_indicator);
     }   
 }
