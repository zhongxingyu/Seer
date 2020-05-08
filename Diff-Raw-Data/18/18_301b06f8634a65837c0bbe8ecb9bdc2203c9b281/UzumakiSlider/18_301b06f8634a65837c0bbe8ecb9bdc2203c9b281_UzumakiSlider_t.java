 package jp.ddo.neko_daisuki.android.widget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Region;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.ViewGroup;
 
 public abstract class UzumakiSlider extends ViewGroup {
 
     private int min;
     private int max;
     private int progress;
 
     private int startAngle;
     private int sweepAngle;
 
     private enum SizeType { TYPE_PIXEL, TYPE_PERCENT };
     private int outlineOuterDiameter;
     private SizeType outerDiameterType;
     private int outerDiameter;
     private SizeType innerDiameterType;
     private int innerDiameter;
     private SizeType outlineInnerDiameterType;
     private int outlineInnerDiameter;
 
     private int strokeWidth;
 
     public UzumakiSlider(Context context) {
         super(context);
         this.initialize();
     }
 
     public UzumakiSlider(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.initialize();
         this.readAttribute(attrs);
     }
 
     public UzumakiSlider(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         this.initialize();
         this.readAttribute(attrs);
     }
 
     public int getProgress() {
         return this.progress;
     }
 
     public void setProgress(int progress) {
         this.progress = progress;
         this.invalidate();
     }
 
     public int getMax() {
         return this.max;
     }
 
     public void setMax(int max) {
         this.max = max;
     }
 
     public int getMin() {
         return this.min;
     }
 
     public void setMin(int min) {
         this.min = min;
     }
 
     public int getAbsoluteInnerDiameter() {
         return this.computeDiameter(this.innerDiameterType, this.innerDiameter);
     }
 
     public int getAbsoluteOuterDiameter() {
         return this.computeDiameter(this.outerDiameterType, this.outerDiameter);
     }
 
    public int getSweepAngle() {
        return this.sweepAngle;
    }

     private void initialize() {
         this.setWillNotDraw(false);
 
         this.setMin(0);
         this.setMax(100);
         this.setProgress(0);
 
         this.startAngle = 0;
         this.sweepAngle = - (10 * 360 + 180);
 
         this.outerDiameter = 95;
         this.outerDiameterType = SizeType.TYPE_PERCENT;
         this.innerDiameter = 45;
         this.innerDiameterType = SizeType.TYPE_PERCENT;
         this.outlineInnerDiameter = 40;
         this.outlineInnerDiameterType = SizeType.TYPE_PERCENT;
 
         this.strokeWidth = 4;
     }
 
     public void setInnerDiameter(int value) {
         this.innerDiameter = value;
     }
 
     public void setInnerDiameterType(SizeType type) {
         this.innerDiameterType = type;
     }
 
     private abstract class MemberSetter {
 
         public abstract void set(UzumakiSlider slider, int value);
     }
 
     private class InnerDiameterPercentSetter extends MemberSetter {
 
         public void set(UzumakiSlider slider, int value) {
             slider.setInnerDiameter(value);
             slider.setInnerDiameterType(SizeType.TYPE_PERCENT);
         }
     }
 
     private class InnerDiameterPixelSetter extends MemberSetter {
 
         public void set(UzumakiSlider slider, int value) {
             slider.setInnerDiameter(value);
             slider.setInnerDiameterType(SizeType.TYPE_PIXEL);
         }
     }
 
     private class OuterDiameterPercentSetter extends MemberSetter {
 
         public void set(UzumakiSlider slider, int value) {
             slider.setOuterDiameter(value);
             slider.setOuterDiameterType(SizeType.TYPE_PERCENT);
         }
     }
 
     private class OuterDiameterPixelSetter extends MemberSetter {
 
         public void set(UzumakiSlider slider, int value) {
             slider.setOuterDiameter(value);
             slider.setOuterDiameterType(SizeType.TYPE_PIXEL);
         }
     }
 
     private class OutlineInnerDiameterPercentSetter extends MemberSetter {
 
         public void set(UzumakiSlider slider, int value) {
             slider.setOutlineInnerDiameter(value);
             slider.setOutlineInnerDiameterType(SizeType.TYPE_PERCENT);
         }
     }
 
     private class OutlineInnerDiameterPixelSetter extends MemberSetter {
 
         public void set(UzumakiSlider slider, int value) {
             slider.setOutlineInnerDiameter(value);
             slider.setOutlineInnerDiameterType(SizeType.TYPE_PIXEL);
         }
     }
 
     public void setOutlineInnerDiameter(int value) {
         this.outlineInnerDiameter = value;
     }
 
     public void setOutlineInnerDiameterType(SizeType type) {
         this.outlineInnerDiameterType = type;
     }
 
     public void setOuterDiameter(int value) {
         this.outerDiameter = value;
     }
 
     public void setOuterDiameterType(SizeType type) {
         this.outerDiameterType = type;
     }
 
     @Override
     public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 
         int width = MeasureSpec.getSize(widthMeasureSpec);
         int height = MeasureSpec.getSize(heightMeasureSpec);
         int outlineOuterDiameter = Math.min(width, height);
         int outlineInnerDiameter = this.computeDiameter(this.outlineInnerDiameterType, this.outlineInnerDiameter, outlineOuterDiameter);
         int spec = MeasureSpec.makeMeasureSpec(outlineInnerDiameter, MeasureSpec.EXACTLY);
         int nChildren = this.getChildCount();
         for (int i = 0; i < nChildren; i++) {
             this.getChildAt(i).measure(spec, spec);
         }
     }
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         int width = r - l;
         int height = b - t;
         int diameter = this.outlineInnerDiameterType == SizeType.TYPE_PERCENT ? Math.min(width, height) * this.outlineInnerDiameter / 100 : this.outlineInnerDiameter;
         int left = (width - diameter) / 2;
         int top = (height - diameter) / 2;
         int right = left + diameter;
         int bottom = top + diameter;
         int nChildren = this.getChildCount();
         for (int i = 0; i < nChildren; i++) {
             this.getChildAt(i).layout(left, top, right, bottom);
         }
     }
 
     private void parseSize(String value, MemberSetter percentSetter, MemberSetter pixelSetter) {
         if (value == null) {
             return;
         }
         boolean isPercent = value.endsWith("%");
         int n = Integer.parseInt(isPercent ? value.substring(0, value.length() - 1) : value);
         MemberSetter setter = isPercent ? percentSetter : pixelSetter;
         setter.set(this, n);
     }
 
     private void readAttribute(AttributeSet attrs) {
         this.startAngle = attrs.getAttributeIntValue(null, "start_angle", this.startAngle);
         this.sweepAngle = attrs.getAttributeIntValue(null, "sweep_angle", this.sweepAngle);
 
         this.outlineOuterDiameter = attrs.getAttributeIntValue(null, "outline_outer_diameter", 0);
         this.parseSize(attrs.getAttributeValue(null, "outline_inner_diameter"), new OutlineInnerDiameterPercentSetter(), new OutlineInnerDiameterPixelSetter());
         this.parseSize(attrs.getAttributeValue(null, "outer_diameter"), new OuterDiameterPercentSetter(), new OuterDiameterPixelSetter());
         this.parseSize(attrs.getAttributeValue(null, "inner_diameter"), new InnerDiameterPercentSetter(), new InnerDiameterPixelSetter());
 
         this.strokeWidth = attrs.getAttributeIntValue(null, "stroke_width", this.strokeWidth);
     }
 
     private int computeDiameter(SizeType type, int size, int baseSize) {
         return type == SizeType.TYPE_PERCENT ? baseSize * size / 100 : size;
     }
 
     private int computeDiameter(SizeType type, int size) {
         return this.computeDiameter(type, size, this.getOutlineOuterDiameter());
     }
 
     private void drawLine(Canvas canvas, int x, int y) {
         Paint paint = new Paint();
         paint.setARGB(255, 255, 255, 255);
         paint.setAntiAlias(true);
         paint.setStrokeWidth(this.strokeWidth);
         paint.setStyle(Paint.Style.STROKE);
 
         int outerDiameter = this.getAbsoluteOuterDiameter();
         int innerDiameter = this.getAbsoluteInnerDiameter();
         UzumakiDiagram uzumaki = new UzumakiDiagram(x, y, this.startAngle, this.sweepAngle, outerDiameter, innerDiameter, paint);
         uzumaki.draw(canvas);
     }
 
     private int getOutlineOuterDiameter() {
         return Math.min(this.getWidth(), this.getHeight());
     }
 
     private void drawTie(Canvas canvas, int x, int y) {
         Path outerOutline = new Path();
         outerOutline.addCircle(x, y, this.getOutlineOuterDiameter() / 2, Path.Direction.CW);
 
         Path innerOutline = new Path();
         int outlineInnerDiameter = this.computeDiameter(this.outlineInnerDiameterType, this.outlineInnerDiameter);
         innerOutline.addCircle(x, y, outlineInnerDiameter / 2, Path.Direction.CW);
 
         Paint paint = new Paint();
         paint.setARGB(255, 0, 0, 0);
         paint.setAntiAlias(true);
 
         canvas.save();
         try {
             canvas.clipPath(outerOutline);
             canvas.clipPath(innerOutline, Region.Op.DIFFERENCE);
             canvas.drawPaint(paint);
         }
         finally {
             canvas.restore();
         }
     }
 
     protected void drawUzumaki(Canvas canvas) {
         int x = this.getWidth() / 2;
         int y = this.getHeight() / 2;
         this.drawTie(canvas, x, y);
         this.drawLine(canvas, x, y);
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
