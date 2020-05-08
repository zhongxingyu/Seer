 package hk.hku.cs.srli.widget;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.PixelFormat;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 import hk.hku.cs.srli.widget.util.HoverHandler;
 
 import java.lang.ref.WeakReference;
 
 public class Tooltip extends FrameLayout {
     
     private static final int[] ATTRS = new int[] { android.R.attr.text };
     private static final int INDEX_OF_TEXT_ATTR = 0;
     
     private static final WindowManager.LayoutParams PARAMS = new WindowManager.LayoutParams();
     
     static {
         PARAMS.gravity = Gravity.CENTER;
         PARAMS.height = WindowManager.LayoutParams.WRAP_CONTENT;
         PARAMS.width = WindowManager.LayoutParams.WRAP_CONTENT;
         PARAMS.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                     | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
         PARAMS.format = PixelFormat.TRANSLUCENT;
         PARAMS.windowAnimations = android.R.style.Animation_Dialog;
         PARAMS.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
         PARAMS.verticalMargin = 0;
         PARAMS.horizontalMargin = 0; 
         PARAMS.setTitle("Tooltip");
     }
     
     private WeakReference<HoverHandler> hover;
     
     private TextView textView;
 
     public Tooltip(Context context) {
         super(context);
         init(context, null, 0);
     }
 
     public Tooltip(Context context, AttributeSet attrs) {
         super(context, attrs);
         init(context, attrs, 0);
     }
 
     public Tooltip(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init(context, attrs, defStyle);
     }
         
     public void init(Context context, AttributeSet attrs, int defStyle) {
         // Load attributes
         final TypedArray a = getContext().obtainStyledAttributes(
                 attrs, ATTRS, defStyle, 0);
         String text = a.getString(INDEX_OF_TEXT_ATTR);
         a.recycle();
         build(context, text);
     }
     
     private void build(Context context, String text) {
         View rootView = LayoutInflater.from(context).inflate(R.layout.tooltip_text, this, true);
         textView = (TextView) rootView.findViewById(R.id.message);
         textView.setText(text);
     }
     
     public static WindowManager.LayoutParams getDefaultLayoutParams() {
         WindowManager.LayoutParams ret = new WindowManager.LayoutParams();        
         ret.copyFrom(PARAMS);
         return ret;
     }
     
     public CharSequence getText() {
         return textView.getText();
     }
 
     public void setText(CharSequence text) {
         textView.setText(text);
     }
     
     public void setText(int resid) {
         textView.setText(resid);
     }
     
     public void setHoverHandler(HoverHandler hover) {
         this.hover = new WeakReference<HoverHandler>(hover);
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         return false;
     }
     
     @Override
     public boolean onHoverEvent(MotionEvent event) {
        if (hover != null && hover.get() != null) {
             hover.get().onTooltipHoverEvent(event);
             return true; // Stop event from propaganda.
         } else {
             return false;
         }
     }
 }
