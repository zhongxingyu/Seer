 package fi.testbed2.android.ui.view.util;
 
 import android.graphics.Rect;
 import android.view.MotionEvent;
 import com.googlecode.androidannotations.annotations.EBean;
 import fi.testbed2.android.app.Logger;
 import fi.testbed2.android.ui.view.AnimationView;
 import lombok.Getter;
 import lombok.Setter;
 
 /**
  * Utility class for map image bounds calculation related functions
  * in AnimationView.
  */
 @EBean
 public class AnimationViewBoundsUtil {
 
     /**
      * Used for bounds calculation
      */
     private static final float GESTURE_THRESHOLD_DIP = 16.0f;
 
     @Getter @Setter
     private Rect bounds;
 
     private float boundsStartY;
     private float boundsStartX;
     private float boundsDistanceY;
     private float boundsDistanceX;
     private boolean boundsMoveMap;
 
     // Animation frame related
     @Setter
     private int frameWidth;
     @Setter
     private int frameHeight;
 
     private boolean doNotMoveMap;
 
     AnimationView view;
 
     public void setView(AnimationView view) {
         this.view = view;
     }
 
     public void initializeBounds() {
 
         // scale bounds
         int measuredHeight = view.getMeasuredHeight();
         int measuredWidth = view.getMeasuredWidth();
 
         Logger.debug("Initializing bounds...");
         Logger.debug("measuredHeight: " + measuredHeight);
         Logger.debug("measuredWidth: " + measuredWidth);
 
         if(measuredHeight > measuredWidth) {
             int scaleBy = measuredHeight-frameHeight;
             if(scaleBy > 0)
                 bounds = new Rect(0, 0, frameWidth + scaleBy, frameHeight + scaleBy);
             else
                 bounds = new Rect(0, 0, frameWidth, frameHeight);
         } else {
             int scaleBy = measuredWidth-frameWidth;
             if(scaleBy > 0)
                 bounds = new Rect(0, 0, frameWidth + scaleBy, frameHeight + scaleBy);
             else
                 bounds = new Rect(0, 0, frameWidth, frameHeight);
         }
     }
 
     /**
      * This method is quite a mess. It is hard to understand how the bounds
      * are calculated. Do not touch this if you don't know what you are doing.
      *
      * @param event
      * @return
      */
     public boolean calculateNewBounds(MotionEvent event) {
 
         int action = event.getAction();
         boolean isMultiTouchEvent = event.getPointerCount()>1;
 
         // Do not move the map in multi-touch
         if (isMultiTouchEvent) {
             doNotMoveMap = true;
         }
 
         // Allow map movement again when new non-multi-touch down event occurs
         if (!isMultiTouchEvent && action==MotionEvent.ACTION_DOWN) {
             doNotMoveMap = false;
         }
 
         if (doNotMoveMap) {
             return true;
         }
 
         switch(action) {
             case MotionEvent.ACTION_CANCEL:
                 break;
             case MotionEvent.ACTION_UP:
                 boundsMoveMap = false;
                 break;
             case MotionEvent.ACTION_DOWN:
                 boundsStartY = event.getY();
                 boundsDistanceY = 0.0f;
                 boundsStartX = event.getX();
                 boundsDistanceX = 0.0f;
                 break;
             case MotionEvent.ACTION_MOVE:
                 boundsDistanceY = -(boundsStartY - event.getY());
                 boundsDistanceX = -(boundsStartX - event.getX());
 
                 Rect savedBounds = new Rect(bounds);
 
                 Logger.debug("Calculating new bounds...");
 
                 // Convert the dips to pixels
                 final float scale = view.getContext().getResources().getDisplayMetrics().density;
                 int mGestureThreshold = (int) (GESTURE_THRESHOLD_DIP * scale / view.getScaleInfo().getScaleFactor() + 0.5f);
 
                 if(!boundsMoveMap && (Math.abs(boundsDistanceY) > mGestureThreshold || Math.abs(boundsDistanceX) > mGestureThreshold)) {
                     boundsMoveMap = true;
                 }
 
                 if(boundsMoveMap) {
 
                     view.getScaleAndGestureUtil().hideMunicipalityToast();
 
                     float mDistance_y_dip = boundsDistanceY * scale / view.getScaleInfo().getScaleFactor() + 0.5f;
                     float mDistance_x_dip = boundsDistanceX * scale / view.getScaleInfo().getScaleFactor() + 0.5f;
 
                     bounds.offset((int)mDistance_x_dip, (int)mDistance_y_dip);
 
                     Logger.debug("New bounds left: " + bounds.left);
                     Logger.debug("New bounds top: " + bounds.top);
                     Logger.debug("New bounds right: " + bounds.right);
                     Logger.debug("New bounds bottom: " + bounds.bottom);
 
                     // restart measuring distances
                     boundsStartY = event.getY();
                     boundsDistanceY = 0.0f;
                     boundsStartX = event.getX();
                     boundsDistanceX = 0.0f;
 
                     view.invalidate();
                 }
 
                 break;
 
         }
 
         return true;
     }
 
 
 }
