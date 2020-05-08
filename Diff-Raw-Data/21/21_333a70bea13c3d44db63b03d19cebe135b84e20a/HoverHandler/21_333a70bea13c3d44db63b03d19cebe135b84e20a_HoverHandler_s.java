 package hk.hku.cs.srli.widget;
 
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 
 public class HoverHandler {
 
     private View view;
     private OnLongHoverListener onLongHoverListener;
     private OnHoverMoveListener onHoverMoveListener;
     
     private boolean hovered = false;
     private boolean tooltipMode = false;
     private boolean tooltipHovered = false;
     private float hoverX;
     private float hoverY;
     
     private boolean hasPerformedLongHover = false;
     private CheckForLongHover pendingCheckForLongHover = new CheckForLongHover();
     
     public HoverHandler(View view) {
         this.view = view;
     }
     
     public boolean onHoverEvent(MotionEvent event) {
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_HOVER_ENTER:
                 hovered = true;
                 onHoverChangedInternal(true);
                 break;
             case MotionEvent.ACTION_HOVER_EXIT:
                 hovered = false;
                 onHoverChangedInternal(false);
                 break;
             case MotionEvent.ACTION_HOVER_MOVE:
                 if (hovered) {
                     hoverX = event.getRawX();
                     hoverY = event.getRawY();
                     if (onHoverMoveListener != null) {
                         final int[] screenPos = new int[2];
                         getLocalCoordinate(screenPos);
                         onHoverMoveListener.onHoverMove(view, screenPos[0], screenPos[1]);
                     }
                 }
                 break;
         }
         return false;
     }
     
     public void setTooltipMode(boolean on) {
         this.tooltipMode = on;
     }
     
     public boolean onTooltipHoverEvent(MotionEvent event) {
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_HOVER_ENTER:
                 tooltipHovered = true;
                 break;
             case MotionEvent.ACTION_HOVER_EXIT:
                 tooltipHovered = false;
                 break;
         }
         return false;
     }
     
     private boolean isHoveredInternal() {
         return hovered || (tooltipMode && tooltipHovered);
     }
     
     public void setOnLongHoverListener(OnLongHoverListener onLongHoverListener) {
         this.onLongHoverListener = onLongHoverListener;
     }
     
     public void setOnHoverMoveListener(OnHoverMoveListener onHoverMoveListener) {
         this.onHoverMoveListener = onHoverMoveListener;
     }
     
     public interface OnLongHoverListener {
         
         public boolean onLongHover(View v, int x, int y);
     }
     
     public interface OnHoverMoveListener {
         public void onHoverMove(View v, int x, int y); 
     }
     
     private void setHoveredExternal(boolean hovered) {
         view.setHovered(hovered);
         if (hovered) {
             checkForLongHover();
         }
     }
     
     private void onHoverChangedInternal(boolean hovered) {
         // Make sure the change is consistent.
         if (hovered == isHoveredInternal()) {
             // change hover state
             checkForHoverChange(hovered);
         }
     }
     
     private void checkForHoverChange(boolean hovering) {
         if (hovering != view.isHovered()) {
             view.postDelayed(new CheckForHoverChange(hovering),
                     ViewConfiguration.getTapTimeout());
         }
     }
     
     private class CheckForHoverChange implements Runnable {
         private final boolean hovering;
         
         public CheckForHoverChange(boolean hovering) {
             this.hovering = hovering;
         }
         
         public void run() {
             // if still in the same hovered state.
             if (isHoveredInternal() == hovering) {
                 setHoveredExternal(hovering);
             }
         }
     }
     
     private void checkForLongHover() {
         if (onLongHoverListener != null) {
             hasPerformedLongHover = false;
 
             view.postDelayed(pendingCheckForLongHover,
                     ViewConfiguration.getLongPressTimeout());
         }
     }
     
     private class CheckForLongHover implements Runnable {
 
         public void run() {
             if (view.isHovered() && !hasPerformedLongHover
                     && onLongHoverListener != null) {
                 final int[] screenPos = new int[2];
                 getLocalCoordinate(screenPos);
                 if (onLongHoverListener.onLongHover(view, screenPos[0], screenPos[1])) {
                     hasPerformedLongHover = true;
                 }
             }
         }
     }
     
     private void getLocalCoordinate(int[] position) {
         view.getLocationOnScreen(position);
         position[0] = (int) hoverX - position[0];
         position[1] = (int) hoverY - position[1];
     }
 }
