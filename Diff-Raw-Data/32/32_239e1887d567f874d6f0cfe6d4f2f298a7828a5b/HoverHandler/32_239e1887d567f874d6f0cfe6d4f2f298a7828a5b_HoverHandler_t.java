 package hk.hku.cs.srli.widget.util;
 
 import android.content.Context;
 import android.util.TypedValue;
 import android.view.MotionEvent;
 import android.view.View;
 
 import hk.hku.cs.srli.widget.R;
 import hk.hku.cs.srli.widget.Tooltip;
 
 public class HoverHandler {
 
     public static final int HOVER_TIMEOUT = 300;
     public static final int LONGHOVER_TIMEOUT = 800;
     
     private View view;
     private OnLongHoverListener onLongHoverListener;
     private OnHoverMoveListener onHoverMoveListener;
     
     /**
      * Internal hover state.
      */
     private boolean hovering = false;
     private boolean viewEntered = false;
     private boolean tooltipEnabled = false;
     private boolean tooltipEntered = false;
     private float hoverX = 0;
     private float hoverY = 0;
     
     private boolean hasPerformedLongHover = false;
     private CheckForLongHover pendingCheckForLongHover = new CheckForLongHover();
     
     private boolean enabled;
     private int timeout;
     
     public HoverHandler(View view) {
         this.view = view;
         enabled = isHoverEnabled(view.getContext());
         timeout = HOVER_TIMEOUT;
     }
     
     public HoverHandler(View view, int timeout) {
         this(view);
         this.timeout = timeout;
     }
     
     public static boolean isHoverEnabled(Context context) {
         TypedValue a = new TypedValue();
         context.getTheme().resolveAttribute(R.attr.hoverEnabled, a, true);
         if (a.type == TypedValue.TYPE_INT_BOOLEAN) {
             return a.data != 0;
         } else {
             return false;
         }
     }
     
     public boolean onHoverEvent(MotionEvent event) {
         // do nothing if disabled
         if (!enabled) return false;
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_HOVER_ENTER:
                 viewEntered = true;
                 refreshInternalHoverState();
                 return true;
             case MotionEvent.ACTION_HOVER_MOVE:
                 hoverX = event.getRawX();
                 hoverY = event.getRawY();
                 // trigger onHover events only if it's already hovered.
                 if (viewEntered && view.isHovered()) {
                     if (onHoverMoveListener != null) {
                         final int[] screenPos = new int[2];
                         getLocalCoordinate(screenPos);
                         // fire hover move event
                         onHoverMoveListener.onHoverMove(view, screenPos[0], screenPos[1]);
                     }
                 }
                 return true;
             case MotionEvent.ACTION_HOVER_EXIT:
                 viewEntered = false;
                 refreshInternalHoverState();
                 break;
         }
         return false;
     }
     
     public void attachTooltip(Tooltip tooltip) {
         tooltip.setHoverHandler(this);
         tooltipEnabled = true;
     }
     
     public void dettachTooltip() {
         tooltipEnabled = false;
     }
     
     public boolean onTooltipHoverEvent(MotionEvent event) {
         if (!enabled) return false;
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_HOVER_ENTER:
                 tooltipEntered = true;
                 refreshInternalHoverState();
                 break;
             case MotionEvent.ACTION_HOVER_EXIT:
                 tooltipEntered = false;
                 refreshInternalHoverState();
                 break;
         }
         return false;
     }
     
     public void setHoverTimeout(int timeout) {
         this.timeout = timeout;
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
     
     private void refreshInternalHoverState() {
         boolean toHover = viewEntered || (tooltipEnabled && tooltipEntered);
         // check if the state is really different
         if (toHover != hovering) {
             // change internal hover state
             hovering = toHover;
             // try to change external hover state
             checkForExternalHoverChange(hovering);
         }
     }
     
     private void checkForExternalHoverChange(boolean hovering) {
         if (hovering != view.isHovered()) {
             // delay external hover change
             view.postDelayed(new CheckForHoverChange(hovering), timeout);
         }
     }
     
     private class CheckForHoverChange implements Runnable {
         private final boolean oldHoverState;
         
         public CheckForHoverChange(boolean hovering) {
             oldHoverState = hovering;
         }
         
         public void run() {
             // if still in the same internal hover state.
             if (hovering == oldHoverState) {
                 setHoveredExternal(hovering);
             }
         }
     }
     
     private void checkForLongHover() {
         if (onLongHoverListener != null) {
             hasPerformedLongHover = false;
             view.postDelayed(pendingCheckForLongHover, LONGHOVER_TIMEOUT);
         }
     }
     
     private class CheckForLongHover implements Runnable {
 
         public void run() {
             if (view.isHovered()
                     && !hasPerformedLongHover
                     && onLongHoverListener != null) {
                 final int[] screenPos = new int[2];
                 if (getLocalCoordinate(screenPos)) {
                     // fire long hover event
                     hasPerformedLongHover = 
                             onLongHoverListener.onLongHover(view, screenPos[0], screenPos[1]);
                 }
             }
         }
     }
     
     private boolean getLocalCoordinate(int[] position) {
         if (hoverX > 0 && hoverY > 0) {
             view.getLocationOnScreen(position);
             // bound by view rect
             position[0] = Math.max(0, Math.min(view.getWidth(), (int) hoverX - position[0]));
             position[1] = Math.max(0, Math.min(view.getHeight(), (int) hoverY - position[1]));
             return true;
         } else return false;
     }
 }
