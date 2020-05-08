 package jp.gr.java_conf.neko_daisuki.android.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 
 public abstract class UzumakiSlider extends ViewGroup {
 
     public interface OnSliderChangeListener {
 
         public void onProgressChanged(UzumakiSlider slider);
     };
 
     public interface Logger {
 
         public void log(String msg);
     }
 
     public interface OnStartHeadMovingListener {
 
         public void onStartHeadMoving(UzumakiSlider slider, UzumakiHead head);
     }
 
     public interface OnStopHeadMovingListener {
 
         public void onStopHeadMoving(UzumakiSlider slider, UzumakiHead head);
     }
 
     private class FakeLogger implements Logger {
 
         public void log(String msg) {
         }
     }
 
     private enum SizeType {
         TYPE_PIXEL,
         TYPE_PERCENT
     };
 
     // document
     private int mMin;
     private int mMax;
     private int mProgress;
 
     // view data
     private int mStartAngle;
     private int mSweepAngle;
     private SizeType mOuterDiameterType;
     private int mOuterDiameter;
     private SizeType mInnerDiameterType;
     private int mInnerDiameter;
     private SizeType mOutlineInnerDiameterType;
     private int mOutlineInnerDiameter;
 
     private int mStrokeWidth;
 
     private List<OnStartHeadMovingListener> mOnStartHeadMovingListenerList;
     private List<OnStopHeadMovingListener> mOnStopHeadMovingListenerList;
     private List<OnSliderChangeListener> mOnSliderChangeListenerList;
 
     // helper
     private Path mDiscPath = new Path();
     private Paint mDiscPaint = new Paint();
     private Paint mSpiralPaint = new Paint();
     private Logger mLogger;
 
     /*
      * These two objects are reused in layout operation. Eclipse warns to avoid
      * allocations in every draw/layout operations.
      */
     private List<View> mNotHeadList;
     private List<View> mHeadList;
 
     public UzumakiSlider(Context context) {
         super(context);
         initialize();
     }
 
     public UzumakiSlider(Context context, AttributeSet attrs) {
         super(context, attrs);
         initialize();
         readAttribute(attrs);
     }
 
     public UzumakiSlider(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         initialize();
         readAttribute(attrs);
     }
 
     public int getProgress() {
         return mProgress;
     }
 
     public void setProgress(int progress) {
         int min = getMin();
         int max = getMax();
         mProgress = Math.min(Math.max(min, progress), max);
 
         fireOnSliderChangeListeners();
         requestLayout();
         invalidate();
     }
 
     public int getMax() {
         return mMax;
     }
 
     public int getSize() {
         return mMax - mMin;
     }
 
     public void setMax(int max) {
         mMax = max;
     }
 
     public int getMin() {
         return mMin;
     }
 
     public void setMin(int min) {
         mMin = min;
     }
 
     public int getAbsoluteOuterDiameter() {
         return computeDiameter(mOuterDiameterType, mOuterDiameter);
     }
 
     public int getAbsoluteInnerDiameter() {
         return computeDiameter(mInnerDiameterType, mInnerDiameter);
     }
 
     public int getAbsoluteOutlineInnerDiameter() {
         return computeDiameter(mOutlineInnerDiameterType,
                                mOutlineInnerDiameter);
     }
 
     public int getSweepAngle() {
         return mSweepAngle;
     }
 
     private void initialize() {
         setWillNotDraw(false);
 
         mMin = mProgress = 0;
         mMax = 100;
 
         mStartAngle = 0;
         mSweepAngle = - (10 * 360 + 180);
 
         mOuterDiameter = 95;
         mOuterDiameterType = SizeType.TYPE_PERCENT;
         mInnerDiameter = 45;
         mInnerDiameterType = SizeType.TYPE_PERCENT;
         mOutlineInnerDiameter = 40;
         mOutlineInnerDiameterType = SizeType.TYPE_PERCENT;
 
         mStrokeWidth = 2;
 
         mOnStartHeadMovingListenerList = new ArrayList<OnStartHeadMovingListener>();
         mOnStopHeadMovingListenerList = new ArrayList<OnStopHeadMovingListener>();
         mOnSliderChangeListenerList = new ArrayList<OnSliderChangeListener>();
 
         setLogger(new FakeLogger());
         mDiscPaint.setARGB(255, 0, 0, 0);
         mDiscPaint.setAntiAlias(true);
         mSpiralPaint.setARGB(255, 255, 255, 255);
         mSpiralPaint.setAntiAlias(true);
         mSpiralPaint.setStyle(Paint.Style.STROKE);
 
         mNotHeadList = new ArrayList<View>();
         mHeadList = new ArrayList<View>();
     }
 
     public void addOnSliderChangeListener(OnSliderChangeListener listener) {
         mOnSliderChangeListenerList.add(listener);
     }
 
     public void addOnStartHeadMovingListener(OnStartHeadMovingListener listener) {
         mOnStartHeadMovingListenerList.add(listener);
     }
 
     public void addOnStopHeadMovingListener(OnStopHeadMovingListener listener) {
         mOnStopHeadMovingListenerList.add(listener);
     }
 
     public void setInnerDiameter(int value) {
         mInnerDiameter = value;
     }
 
     public void setInnerDiameterType(SizeType type) {
         mInnerDiameterType = type;
     }
 
     public void setLogger(Logger logger) {
         mLogger = logger;
     }
 
     protected void onFinishInflate() {
         super.onFinishInflate();
 
         List<View> _ = new ArrayList<View>();
         List<View> headList = new ArrayList<View>();
         groupChildren(_, headList);
         for (View view: headList) {
             UzumakiHead head = (UzumakiHead)view;
             head.setSlider(this);
         }
     }
 
     protected void log(String msg) {
         mLogger.log(msg);
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
 
     public void clearOnSliderChangeListeners() {
         mOnSliderChangeListenerList.clear();
     }
 
     public void setOutlineInnerDiameter(int value) {
         mOutlineInnerDiameter = value;
     }
 
     public void setOutlineInnerDiameterType(SizeType type) {
         mOutlineInnerDiameterType = type;
     }
 
     public void setOuterDiameter(int value) {
         mOuterDiameter = value;
     }
 
     public void setOuterDiameterType(SizeType type) {
         mOuterDiameterType = type;
     }
 
     public void fireOnStartHeadMovingListeners(UzumakiHead head) {
         for (OnStartHeadMovingListener listener: mOnStartHeadMovingListenerList) {
             listener.onStartHeadMoving(this, head);
         }
     }
 
     public void fireOnStopHeadMovingListeners(UzumakiHead head) {
         for (OnStopHeadMovingListener listener: mOnStopHeadMovingListenerList) {
             listener.onStopHeadMoving(this, head);
         }
     }
 
     public abstract void slideHead(int progressOld, float deltaX, float deltaY);
 
     @Override
     public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 
         int outlineInnerDiameter = getAbsoluteOutlineInnerDiameter();
         int spec = MeasureSpec.makeMeasureSpec(outlineInnerDiameter, MeasureSpec.EXACTLY);
         int nChildren = getChildCount();
         for (int i = 0; i < nChildren; i++) {
             getChildAt(i).measure(spec, spec);
         }
     }
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         mNotHeadList.clear();
         mHeadList.clear();
         groupChildren(mNotHeadList, mHeadList);
 
         int width = r - l;
         int height = b - t;
         int diameter = mOutlineInnerDiameterType == SizeType.TYPE_PERCENT ? Math.min(width, height) * mOutlineInnerDiameter / 100 : mOutlineInnerDiameter;
         int left = (width - diameter) / 2;
         int top = (height - diameter) / 2;
         int right = left + diameter;
         int bottom = top + diameter;
         int nChildren = mNotHeadList.size();
         for (int i = 0; i < nChildren; i++) {
             mNotHeadList.get(i).layout(left, top, right, bottom);
         }
 
         for (View head: mHeadList) {
             layoutHead(head, l, t, r, b);
         }
     }
 
     private void groupChildren(List<View> notHeadList, List<View> headList) {
         int nChildren = getChildCount();
         for (int i = 0; i < nChildren; i++) {
             View child = getChildAt(i);
             List<View> l = child instanceof UzumakiHead ? headList : notHeadList;
             l.add(child);
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
         mStartAngle = attrs.getAttributeIntValue(null, "start_angle",
                                                  mStartAngle);
         mSweepAngle = attrs.getAttributeIntValue(null, "sweep_angle",
                                                  mSweepAngle);
 
         parseSize(attrs.getAttributeValue(null, "outline_inner_diameter"),
                   new OutlineInnerDiameterPercentSetter(),
                   new OutlineInnerDiameterPixelSetter());
         parseSize(attrs.getAttributeValue(null, "outer_diameter"),
                   new OuterDiameterPercentSetter(),
                   new OuterDiameterPixelSetter());
         parseSize(attrs.getAttributeValue(null, "inner_diameter"),
                   new InnerDiameterPercentSetter(),
                   new InnerDiameterPixelSetter());
 
         mStrokeWidth = attrs.getAttributeIntValue(null, "stroke_width",
                                                   mStrokeWidth);
     }
 
     private int computeDiameter(SizeType type, int size) {
         int baseSize = getOutlineOuterDiameter();
         return type == SizeType.TYPE_PERCENT ? baseSize * size / 100 : size;
     }
 
     protected int getOutlineOuterDiameter() {
         return Math.min(getWidth(), getHeight());
     }
 
     protected void drawDisc(Canvas canvas) {
         int x = getWidth() / 2;
         int y = getHeight() / 2;
         mDiscPath.addCircle(x, y, getOutlineOuterDiameter() / 2,
                             Path.Direction.CW);
         int outlineInnerDiameter = getAbsoluteOutlineInnerDiameter();
         mDiscPath.addCircle(x, y, outlineInnerDiameter / 2, Path.Direction.CCW);
 
         canvas.drawPath(mDiscPath, mDiscPaint);
     }
 
     protected void drawUzumaki(Canvas canvas) {
         int x = getWidth() / 2;
         int y = getHeight() / 2;
         mSpiralPaint.setStrokeWidth(mStrokeWidth);
 
         int outerDiameter = getAbsoluteOuterDiameter();
         int innerDiameter = getAbsoluteInnerDiameter();
         UzumakiDiagram uzumaki = new UzumakiDiagram(x, y, mStartAngle,
                                                     mSweepAngle, outerDiameter,
                                                     innerDiameter,
                                                     mSpiralPaint);
         uzumaki.draw(canvas);
     }
 
     protected abstract void layoutHead(View head, int l, int t, int r, int b);
 
     private void fireOnSliderChangeListeners() {
         for (OnSliderChangeListener l: mOnSliderChangeListenerList) {
             l.onProgressChanged(this);
         }
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
