 package uk.org.smithfamily.mslogger.widgets;
 
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.content.Context;
 import android.graphics.*;
 import android.graphics.Paint.Style;
 import android.graphics.Path.FillType;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 /**
  *
  */
 public class MSGauge extends View implements Indicator
 {
     public static final String DEAD_GAUGE_NAME = "deadGauge";
     private int                diameter;
    private String             name        = DEAD_GAUGE_NAME;
     private String             title       = "RPM";
     private String             channel     = "rpm";
     private String             units       = "";
     private double             min         = 0;
     private double             max         = 7000;
     private double             lowD        = 0;
     private double             lowW        = 0;
     private double             hiW         = 5000;
     private double             hiD         = 7000;
     private int                vd          = 0;
     private int                ld          = 0;
     private double             value       = 2500;
     private double             pi          = Math.PI;
     private double             offsetAngle = 45;
     final float                scale       = getResources().getDisplayMetrics().density;
     private Paint              titlePaint;
     private Paint              valuePaint;
     private Paint              pointerPaint;
     private Paint              scalePaint;
     private RectF              rimRect;
     private Paint              rimPaint;
     private Paint              rimCirclePaint;
     private RectF              faceRect;
     private Paint              facePaint;
     private boolean            disabled;
     private static final float rimSize     = 0.02f;
 
     private GaugeDetails deadGauge = new GaugeDetails(DEAD_GAUGE_NAME, "deadValue",value, "---", "", 0, 1, -1, -1, 2, 2, 0, 0, offsetAngle);
     
     /**
      * 
      * @param context
      */
     public MSGauge(Context context)
     {
         super(context);
         init(context);
     }
 
     /**
      * 
      * @param c
      * @param s
      */
     public MSGauge(Context c, AttributeSet s)
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
     public MSGauge(Context context, AttributeSet attr, int defaultStyles)
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
 
         drawFace(canvas);
 
         drawScale(canvas);
 
         if (!disabled)
         {
             drawPointer(canvas);
             drawValue(canvas);
         }
 
         drawTitle(canvas);
         canvas.restore();
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
         rimRect = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
 
         faceRect = new RectF();
         if (!isInEditMode())
         {
             faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, rimRect.right - rimSize, rimRect.bottom - rimSize);
         }
         else
             faceRect = rimRect;
 
         // the linear gradient is a bit skewed for realism
         rimPaint = new Paint();
         if (!this.isInEditMode())
         {
             rimPaint.setFlags(anti_alias_flag);
             rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, Color.rgb(0xf0, 0xf5, 0xf0), Color
                     .rgb(0x30, 0x31, 0x30), Shader.TileMode.CLAMP));
         }
         rimCirclePaint = new Paint();
         if (!this.isInEditMode())
         {
             rimCirclePaint.setAntiAlias(true);
             rimCirclePaint.setStyle(Paint.Style.STROKE);
             rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
             rimCirclePaint.setStrokeWidth(0.005f);
         }
         facePaint = new Paint();
         facePaint.setFilterBitmap(true);
         facePaint.setStyle(Paint.Style.FILL);
         facePaint.setColor(Color.BLACK);
         facePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
         
         titlePaint = new Paint();
         titlePaint.setColor(Color.WHITE);
         titlePaint.setTextAlign(Paint.Align.CENTER);
         titlePaint.setFlags(anti_alias_flag);
         titlePaint.setAntiAlias(true);
 
         valuePaint = new Paint();
         valuePaint.setColor(Color.WHITE);
         valuePaint.setTextSize(0.1f);
         valuePaint.setTextAlign(Paint.Align.CENTER);
         valuePaint.setFlags(anti_alias_flag);
         valuePaint.setAntiAlias(true);
         
         pointerPaint = new Paint();
         pointerPaint.setColor(Color.WHITE);
         pointerPaint.setAntiAlias(true);
         pointerPaint.setStrokeWidth((0.5f / 48.0f));
         pointerPaint.setStyle(Style.FILL_AND_STROKE);
         pointerPaint.setFlags(anti_alias_flag);
         pointerPaint.setAntiAlias(true);
         
         scalePaint = new Paint();
         scalePaint.setColor(Color.WHITE);
         scalePaint.setAntiAlias(true);
         scalePaint.setTextSize(0.05f);
         scalePaint.setTextAlign(Paint.Align.CENTER);
         scalePaint.setFlags(anti_alias_flag);
         scalePaint.setAntiAlias(true);
     }
 
     /**
      * 
      * @param canvas
      */
     private void drawTitle(Canvas canvas)
     {
         titlePaint.setTextSize(0.07f);
         titlePaint.setColor(getFgColour());
         canvas.drawText(title, 0.5f, 0.25f, titlePaint);
         
         titlePaint.setTextSize(0.05f);
         canvas.drawText(units, 0.5f, 0.32f, titlePaint);
     }
 
     /**
      * 
      * @param canvas
      */
     private void drawValue(Canvas canvas)
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
 
         canvas.drawText(text, 0.5f, 0.65f, valuePaint);
     }
 
     /**
      * 
      * @param canvas
      */
     private void drawPointer(Canvas canvas)
     {
         float back_radius = 0.042f;
                 
         double range = 270.0 / (max - min);
         double pointerValue = value;
         if (pointerValue < min)
         {
             pointerValue = min;
         }
         if (pointerValue > max)
         {
             pointerValue = max;
         }
         
         pointerPaint.setColor(getFgColour());
 
         canvas.drawCircle(0.5f,0.5f,back_radius / 2.0f, pointerPaint);
         
         Path pointerPath = new Path(); // X Y
         pointerPath.setFillType(FillType.EVEN_ODD);
 
         pointerPath.moveTo(0.5f, 0.1f);                     // 0.500, 0.100
         pointerPath.lineTo(0.5f + 0.010f, 0.5f + 0.05f);    // 0.501, 0.505
         pointerPath.lineTo(0.5f - 0.010f, 0.5f + 0.05f);    // 0.499, 0.505
         pointerPath.lineTo(0.5f, 0.1f);                     // 0.500, 0.100
         canvas.save(Canvas.MATRIX_SAVE_FLAG);
         
         double angle = ((pointerValue - min) * range + offsetAngle) - 180;
         canvas.rotate((float) angle, 0.5f, 0.5f);
         canvas.drawPath(pointerPath, pointerPaint);
         canvas.restore();
     }
 
     /**
      * 
      * @param canvas
      */
     private void drawScale(Canvas canvas)
     {
         float radius = 0.42f;
         scalePaint.setColor(getFgColour());
         double range = (max - min);
         double tenpower = Math.floor(Math.log10(range));
         double scalefactor = Math.pow(10, tenpower);
 
         double gaugeMax = max;
         double gaugeMin = min;
 
         double gaugeRange = gaugeMax - gaugeMin;
 
         double step = scalefactor;
 
         while ((gaugeRange / step) < 10)
         {
             step = step / 2;
         }
         
         for (double val = gaugeMin; val <= gaugeMax; val += step)
         {
             float displayValue = (float) (Math.floor(val / Math.pow(10, -ld) + 0.5) * Math.pow(10, -ld));
 
             String text;
 
             if (ld <= 0)
             {
                 text = Integer.toString((int) displayValue);
             }
             else
             {
                 text = Float.toString(displayValue);
             }
 
             double anglerange = 270.0 / gaugeRange;
             double angle = (val - gaugeMin) * anglerange + offsetAngle;
             double rads = angle * pi / 180.0;
             float x = (float) (0.5f - radius * Math.cos(rads - pi / 2.0));
             float y = (float) (0.5f - radius * Math.sin(rads - pi / 2.0));
             canvas.drawText(text, x, y, scalePaint);
         }
     }
 
     /**
      * 
      * @return
      */
     private int getFgColour()
     {
         if(disabled)
         {
             return Color.DKGRAY;
         }
         if (value > lowW && value < hiW)
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
         if (this.disabled)
         {
             return c;
         }
         if (value > lowW && value < hiW)
         {
             return Color.BLACK;
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
      * @param canvas
      */
     private void drawFace(Canvas canvas)
     {
         if (isInEditMode())
         {
             facePaint.setColor(Color.RED);
             
             facePaint.setStyle(Style.FILL);
             canvas.drawOval(rimRect, facePaint);
             return;
         }
         canvas.drawOval(rimRect, rimPaint);
         // now the outer rim circle
         canvas.drawOval(rimRect, rimCirclePaint);
         facePaint.setColor(getBgColour());
         canvas.drawOval(faceRect, facePaint);
 
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
      * 
      * @return
      */
     public GaugeDetails getDetails()
     {
         GaugeDetails gd = new GaugeDetails(name, channel,value, title, units, min, max, lowD, lowW, hiW, hiD, vd, ld, offsetAngle);
 
         return gd;
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
         offsetAngle = gd.getOffsetAngle();
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
      * @return
      */
     public String getName()
     {
         return name;
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
      * @return
      */
     public double getOffsetAngle()
     {
         return offsetAngle;
     }
 
     /**
      * 
      * @param offsetAngle
      */
     public void setOffsetAngle(double offsetAngle)
     {
         this.offsetAngle = offsetAngle;
     }
 }
