 package uk.org.smithfamily.mslogger.widgets;
 
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 /**
  *
  */
 public class BarMeter extends View implements Indicator
 {
     public static final String DEAD_GAUGE_NAME = "deadGauge";
     private String          name        = DEAD_GAUGE_NAME;
     private String          title       = "RPM";
     private String          channel     = "rpm";
     private String          units       = "";
     private double          min;
     private double          max;
     private double          lowD        = 0;
     private double          lowW        = 0;
     private double          hiW         = 5000;
     private double          hiD         = 7000;
     private int             vd          = 0;
     private int             ld          = 0;
 
     final float             scale       = getResources().getDisplayMetrics().density;
     
     private int             diameter;
     
     private Paint           barPaint;
     private Paint           titlePaint;
     private Paint           valuePaint;
     
     public enum Orientation
     {
         HORIZONTAL, VERTICAL
     }
     
     private Orientation orientation     = Orientation.HORIZONTAL;
     
     private double          value;
     private boolean         disabled;
 
     private GaugeDetails deadGauge = new GaugeDetails(DEAD_GAUGE_NAME, "deadValue",value, "---", "", 0, 1, -1, -1, 2, 2, 0, 0, 0);
 
     /**
      * 
      * @param context
      */
     public BarMeter(Context context)
     {
         super(context);
         init(context);
     }
 
     /**
      * 
      * @param c
      * @param s
      */
     public BarMeter(Context c, AttributeSet s)
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
     public BarMeter(Context context, AttributeSet attr, int defaultStyles)
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
      * 
      * @param gd
      */
     public void initFromGD(GaugeDetails gd)
     {
         name = gd.getName();
         title = gd.getTitle();
         channel = gd.getChannel();
         units = gd.getUnits();
         min = gd.getMin();
         max = gd.getMax();
         lowD = gd.getLoD();
 
         lowW = gd.getLoW();
         hiW = gd.getHiW();
 
         hiD = gd.getHiD();
         vd = gd.getVd();
         ld = gd.getLd();
         value = (max - min) / 2.0;
     }
     
     /**
      * 
      * @param nme
      */
     public void initFromName(String nme)
     {
         GaugeDetails gd = GaugeRegister.INSTANCE.getGaugeDetails(nme);
         if (gd == null)
         {   
             DebugLogManager.INSTANCE.log("Can't find gauge : " + nme,Log.ERROR);
             gd = deadGauge;
         }
         initFromGD(gd);
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
        titlePaint.setTextAlign(Paint.Align.LEFT);
         titlePaint.setTextSize(0.06f);
         titlePaint.setFlags(anti_alias_flag);
         titlePaint.setAntiAlias(true);
 
         valuePaint = new Paint();
         valuePaint.setColor(Color.WHITE);
         valuePaint.setTextSize(0.06f);
         valuePaint.setTextAlign(Paint.Align.RIGHT);
         valuePaint.setFlags(anti_alias_flag);
         valuePaint.setAntiAlias(true);
         
         barPaint = new Paint();
         barPaint.setColor(Color.BLACK);
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
         
         drawBars(canvas);
         
         canvas.translate(dx, dy);       
         
         if (!disabled)
         {
             drawValue(canvas);
         }
 
         drawTitle(canvas);
         canvas.restore();     
     }
     
     public void drawValue(Canvas canvas)
     {
         valuePaint.setColor(getFgColour());
 
         float displayValue = (float) (Math.floor(value / Math.pow(10, -vd) + 0.5) * Math.pow(10, -vd));
 
         String text;
 
         if (vd <= 0)
         {
             text = Integer.toString((int) displayValue);
         }
         else
         {
             text = Float.toString(displayValue);
         }
 
         canvas.drawText(text, 0.9f, 0.95f, valuePaint);
     }
     
     public void drawTitle(Canvas canvas)
     {
         titlePaint.setColor(getFgColour());
        
        String text = title;
        if (!units.equals(""))
        {
            text += " (" + units + ")";
        }
        
        canvas.drawText(text, 0.05f, 0.95f, titlePaint);
     }
     
     public void drawBars(Canvas canvas)
     {
         double percent = ((value - min) / (max - min)) * 100;
         
         barPaint.setColor(Color.WHITE);
         barPaint.setColor(getBarColour((((max - min) / 100) * percent) + min));
                 
         final float barWidth = 0.025f;
         final float barSpacing = 0.02f;        
         
         if (orientation == Orientation.VERTICAL)
         {       
             final int nbBars = 21;
             
             for (int i = 1; i <= nbBars; i++)
             {
                 if ((i * 100 / nbBars) <= percent) 
                 {            
                     canvas.drawRect((i * (barWidth + barSpacing)) + 0.03f,0.05f,(i * (barWidth + barSpacing)) + barWidth + 0.03f,0.85f,barPaint);
                 }
             }    
         }
         else
         {
             final int nbBars = 19;
             
             for (int i = 1; i <= nbBars; i++)
             {
                 if ((i * 100 / nbBars) <= percent) 
                 {            
                     canvas.drawRect(0.05f,0.9f - (i * (barWidth + barSpacing)),0.9f,0.9f - (i * (barWidth + barSpacing)) + barWidth,barPaint);
                 }
             }
         }
     }
     
     /**
      * 
      * @return
      */
     private int getFgColour()
     {
         int c = Color.WHITE;
         
         if (disabled)
         {
             return Color.DKGRAY;
         }
         
         if (value > lowW && value < hiW)
         {
             return Color.WHITE;
         }
         else if (value <= lowW || value >= hiW)
         {
             c = Color.YELLOW;
         }
         if (value <= lowD || value >= hiD)
         {
             c = Color.RED;
         }
         
         return c;
     }
     
     /**
      * 
      * @return
      */
     private int getBarColour(double value)
     {
         int c = Color.GRAY;
         if (this.disabled)
         {
             return c;
         }
         if (value > lowW && value < hiW)
         {
             return Color.WHITE;
         }
         else if (value <= lowW || value >= hiW)
         {
             c = Color.YELLOW;
         }
         if (value <= lowD || value >= hiD)
         {
             c = Color.RED;
         }
         
         return c;
     }
     
     /**
      * @return
      */
     public String getName()
     {
         return this.name;
     }
     
     /**
      * @param name
      */
     @Override
     public void setName(String name)
     {
         this.name = name;        
     }
 
     /**
      * @param channelName
      */
     @Override
     public void setChannel(String channelName)
     {
         this.channel = channelName;        
     }
 
     /**
      * @param title
      */
     @Override
     public void setTitle(String title)
     {
         this.title = title;        
     }
 
     /**
      * @param units
      */
     @Override
     public void setUnits(String units)
     {
         this.units = units;        
     }
 
     /**
      * @param min
      */
     @Override
     public void setMin(float min)
     {
         this.min = min;        
     }
 
     /**
      * @param max
      */
     @Override
     public void setMax(float max)
     {
         this.max = max;        
     }
 
     /**
      * @param lowD
      */
     @Override
     public void setLowD(float lowD)
     {
         this.lowD = lowD;
     }
 
     /**
      * @param lowW
      */
     @Override
     public void setLowW(float lowW)
     {
         this.lowW = lowW;
     }
 
     /**
      * @param hiW
      */
     @Override
     public void setHiW(float hiW)
     {
         this.hiW = hiW;
     }
 
     /**
      * @param hiD
      */
     @Override
     public void setHiD(float hiD)
     {
         this.hiD = hiD;
     }
 
     /**
      * @param vd
      */
     @Override
     public void setVD(int vd)
     {
         this.vd = vd;
     }
 
     /**
      * @param ld
      */
     @Override
     public void setLD(int ld)
     {
         this.ld = ld;
     }
 
     /**
      * @return
      */
     public int getLD()
     {
         return ld;
     }
     
     /**
      * @param value
      */
     @Override
     public void setCurrentValue(double value)
     {
         this.value = value;
         invalidate();
     }
 
     /**
      * @return
      */
     @Override
     public String getChannel()
     {
         return channel;
     }
     
     /**
      * 
      * @param disabled
      */
     @Override
     public void setDisabled(boolean disabled)
     {
         this.disabled = disabled;
         this.invalidate();
     }
     
     /**
      * @param orientation
      */
     public void setOrientation(Orientation orientation)
     {
        this.orientation = orientation; 
     }
     
     public Orientation getOrientation()
     {
         return orientation;
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
 }
