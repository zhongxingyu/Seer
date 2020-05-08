 package net.meneame.fisgodroid;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 
 public class ThreeStateChecboxHackView extends CheckBox
 {
     public enum State
     {
         UNCHECKED,
         CHECKED,
         THIRD_STATE
     }
     
     private State mState;
     private Drawable mOriginalBackground;
     private Drawable mThirdStateDrawable = null;
 
     public ThreeStateChecboxHackView(Context context)
     {
         super(context);
         if ( !isInEditMode() )
             init();
     }
 
     public ThreeStateChecboxHackView(Context context, AttributeSet attrs)
     {
         super(context, attrs);
         if ( !isInEditMode() )
             init();
     }
     
     private void init ()
     {
         if ( isChecked() )
             mState = State.CHECKED;
         else
             mState = State.UNCHECKED;
         
         mOriginalBackground = getBackground();
         
         setOnCheckedChangeListener(new OnCheckedChangeListener()
         {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
             {
                 if ( mThirdStateDrawable == null )
                 {
                     mState = isChecked ? State.CHECKED : State.UNCHECKED;
                 }
                 else if ( !isChecked )
                 {
                     if ( mState == State.CHECKED )
                     {
                         mState = State.THIRD_STATE;
                         setBackgroundDrawable(mThirdStateDrawable);
                         setChecked(true);
                     }
                     else if ( mState == State.THIRD_STATE )
                     {
                         mState = State.UNCHECKED;
                         setBackgroundDrawable(mOriginalBackground);
                     }
                 }
                 else
                 {
                     mState = State.CHECKED;
                 }
             }
         });
     }
     
     public void setThirdStateDrawable ( Drawable drawable )
     {
         mThirdStateDrawable = drawable;
     }
     
     public State getState ()
     {
         return mState;
     }
     
     public void setState ( State state )
     {
         mState = state;
     }
 }
