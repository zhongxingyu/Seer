 
 package hk.hku.cs.srli.widget;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 /**
  * ImageButton with Hover support.
  */
 public class HoverImageButton extends ImageButton {
 
     private float mHoverX;
     private float mHoverY;
     
     public HoverImageButton(Context context) {
         super(context);
         init(null, 0);
     }
 
     public HoverImageButton(Context context, AttributeSet attrs) {
         super(context, attrs);
         init(attrs, 0);
     }
 
     public HoverImageButton(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init(attrs, defStyle);
     }
 
     private void init(AttributeSet attrs, int defStyle) {
         setOnLongClickListener(new OnLongClickListener() {
             
             @Override
             public boolean onLongClick(View v) {
                 int[] location = new int[2];
                 getLocationInWindow(location);
                showTooltipAt(location[0], location[1]);
                 return false;
             }
         });
 
     }
     
     private boolean hovered;
     
     @Override
     public void onHoverChanged(boolean hovered) {
         this.hovered = hovered;
         super.onHoverChanged(hovered);
     }
     
     @Override
     public boolean onHoverEvent(MotionEvent event) {
         if (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE) {
             mHoverX = event.getRawX();
             mHoverY = event.getRawY();
             if (hovered) {
                 showTooltipAt((int) mHoverX, (int) mHoverY);
             }
         }
         return super.onHoverEvent(event);
     }
     
     private Toast tooltip;
     
     private void showTooltipAt(int x, int y) {
         if (tooltip != null) tooltip.cancel();
         tooltip = Toast.makeText(getContext(), getContentDescription(), Toast.LENGTH_SHORT);
         if (x > 0 && y > 0) {
             tooltip.setGravity(Gravity.TOP|Gravity.LEFT, x, y);
         }
         tooltip.show();
     }
 }
