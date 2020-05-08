 package hk.hku.cs.srli.widget;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageButton;
 
 import hk.hku.cs.srli.widget.TooltipManager.TooltipView;
 
 /**
  * ImageButton with Hover support.
  */
 public class HoverImageButton extends ImageButton implements TooltipView {
     
     private HoverHandler hover;
     
     public HoverImageButton(Context context) {
         super(context);
         init();
     }
 
     public HoverImageButton(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public HoverImageButton(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     private void init() {
         setOnLongClickListener(new OnLongClickListener() {
             
             @Override
             public boolean onLongClick(View v) {
                 TooltipManager.showAndHide(HoverImageButton.this, getContentDescription(),
                         getWidth() / 2, getHeight() / 2, TooltipManager.LONG_DELAY);
                 return false;
             }
         });
         hover = new HoverHandler(this);
         hover.setOnLongHoverListener(new HoverHandler.OnLongHoverListener() {
             
             @Override
             public boolean onLongHover(View v, int x, int y) {
                 TooltipManager.show(HoverImageButton.this, getContentDescription(), x, y);
                 return true;
             }
         });
     }
     
     @Override
     public void onHoverChanged(boolean hovered) {
         super.onHoverChanged(hovered);
         if (!hovered) TooltipManager.hide(this);
     }
     
     @Override
     public boolean onHoverEvent(MotionEvent event) {
        return hover.onHoverEvent(event);
     }
     
     @Override
     public void setTooltip(Tooltip tooltip) {
         if (tooltip != null) {
             tooltip.setHoverHandler(hover);
             hover.setTooltipMode(true);
         } else {
             hover.setTooltipMode(false);
         }
     }
 }
