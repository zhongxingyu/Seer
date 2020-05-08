 package com.androidlearner.widget;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import com.teamluper.luper.Playhead;
 import com.teamluper.luper.TrackView;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: ericsmith
  * Date: 4/25/13
  * Time: 7:47 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 //Dragging Widget Associated with Playhead
 public class DragThingPlayhead extends Playhead {
 
   //original x and y vals (to be changed when dragged)
   private int prevX = 100;
 
   //constructor!
 
   public DragThingPlayhead(Context context) {
     super(context);
   }
 
   public DragThingPlayhead(Context context, AttributeSet attribute, int style) {
     super(context, attribute, style);
   }
 
   public DragThingPlayhead(Context context, AttributeSet attribute) {
     this(context, attribute, 0);
   }
 
   //returns the position
   public int getCurrent(){ return this.getCurrentTimeMS(); }
 
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
 
     final int action = event.getAction();
 
     final float x = event.getX(); //teh current x val
     //final float y = event.getY(); //teh current y val
 
     switch(action){
       case MotionEvent.ACTION_DOWN:
        this.editorActivity.isPaused = true;
         setCurrentTimeMS(Math.round(x / TrackView.PIXELS_PER_MILLISECOND));
         invalidate();
         break;
 
       case MotionEvent.ACTION_MOVE:
 //        int changex = x - prevX; //calculates the change in position
 //        final int left = getLeft();
 //        if(changex != 0) { // if there is a change, then update the layout
 //          //values and stuff
 //          layout(left + changex, getTop(), getWidth(), getHeight());
 //        }
 //
 //        prevX = x - changex;
 //
 //        //redefining array
 //        current = left + changex;
         break;
 
       case MotionEvent.ACTION_UP:
         break;
 
       case MotionEvent.ACTION_CANCEL:
         break;
     }
 
 
     return true;
   }
 }
 
