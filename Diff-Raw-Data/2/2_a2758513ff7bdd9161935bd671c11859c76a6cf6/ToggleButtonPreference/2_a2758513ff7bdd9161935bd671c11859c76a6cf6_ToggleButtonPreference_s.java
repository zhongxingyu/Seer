 /**
  * @file   ToggleButtonPreference.java
  * @author yenliangl <yenliangl@gmail.com>
  * @date   Thu Oct  8 19:49:43 2009
  *
  * @brief
  *
  *
  */
 package org.startsmall.alarmclockplus.preference;
 
 import org.startsmall.alarmclockplus.*;
 //import android.app.AlertDialog;
 //import android.app.Dialog;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.preference.Preference;
 import android.util.AttributeSet;
 //import android.util.Log;
 import android.view.View;
//import android.view.ViewGroup;
 import android.widget.ToggleButton;
 
 public class ToggleButtonPreference extends Preference {
     //private static final String TAG = "ToggleButtonPreference";
     private boolean mValue;
 
     public ToggleButtonPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
         setWidgetLayoutResource(
             R.layout.alarm_toggle_button_preference_widget);
         setDefaultValue(false);
     }
 
     public void setChecked(boolean state) {
         mValue = state;
         if(shouldPersist()) {
             persistBoolean(mValue);
             notifyChanged();
         }
     }
 
     public boolean isChecked() {
         return mValue;
     }
 
     @Override
     protected void onClick() {
 
         boolean newValue = !isChecked();
         // if (!callChangeListener(newValue)) {
         //     return;
         // }
         setChecked(newValue);
 
         super.onClick();
 
     }
 
     @Override
     protected void onBindView(View view) {
 
         final ToggleButton toggle =
             (ToggleButton)view.findViewById(R.id.toggle);
         toggle.setChecked(mValue);
 
         super.onBindView(view);
     }
 
     protected View onCreateView(ViewGroup parent) {
         View view = super.onCreateView(parent);
         view.setOnClickListener(
             new View.OnClickListener() {
                 public void onClick(View view) {
                     ToggleButtonPreference.this.onClick();
                 }
             });
         return view;
     }
 
     @Override
     protected Object onGetDefaultValue(TypedArray a, int index) {
         return a.getBoolean(index, false);
     }
 
     @Override
     protected void onSetInitialValue(boolean restorePersistedValue,
                                      Object defValue) {
         if(restorePersistedValue &&
            shouldPersist()) {
             setChecked(getPersistedBoolean(mValue));
             return;
         }
         setChecked((Boolean)defValue);
     }
 
     @Override
     protected Parcelable onSaveInstanceState() {
         final Parcelable superState = super.onSaveInstanceState();
         if(isPersistent()) {    // persistent preference
             return superState;
         }
 
         SavedState myState = new SavedState(superState);
         myState.value = mValue;
         return myState;
     }
 
     @Override
     protected void onRestoreInstanceState(Parcelable state) {
         if(state != null && state.getClass().equals(SavedState.class)) {
             SavedState myState = (SavedState)state;
             super.onRestoreInstanceState(myState.getSuperState());
             setChecked(myState.value);
         } else {
             super.onRestoreInstanceState(state);
         }
     }
 
     private static class SavedState extends BaseSavedState {
         boolean value;
 
         public SavedState(Parcelable in) {
             super(in);
         }
 
         public SavedState(Parcel in) {
             super(in);
             value = (in.readInt() == 1);
         }
 
         @Override
         public void writeToParcel(Parcel dest, int flags) {
             super.writeToParcel(dest, flags);
             dest.writeInt(value ? 1 : 0);
         }
 
         public static final Parcelable.Creator<SavedState> CREATOR =
             new Parcelable.Creator<SavedState>() {
 
             public SavedState createFromParcel(Parcel in) {
                 return new SavedState(in);
             }
 
             public SavedState[] newArray(int size) {
                 return new SavedState[size];
             }
         };
     }
 }
