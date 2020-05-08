 package com.github.tdudziak.gps_lock_lock;
 
 import android.content.Context;
 import android.preference.Preference;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class SeekBarPreference extends Preference implements OnSeekBarChangeListener
 {
     private static final String XML_NAMESPACE = "http://github.com/tdudziak/gps-lock-lock/schema";
 
     private SeekBar mSeekBar;
     private TextView mValue;
 
     private int mMin;
     private int mMax;
 
     public SeekBarPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
         loadXml(attrs);
     }
 
     public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         loadXml(attrs);
     }
 
     private void loadXml(AttributeSet attrs) {
         mMax = attrs.getAttributeIntValue(XML_NAMESPACE, "max", 100);
         mMin = attrs.getAttributeIntValue(XML_NAMESPACE, "min", 1);
     }
 
     @Override
     protected void onBindView(View view) {
        mSeekBar = (SeekBar) view.findViewById(R.id.preferenceSeekBar);
        mValue = (TextView) view.findViewById(R.id.preferenceTextValue);
 
         mSeekBar.setOnSeekBarChangeListener(this);
         mSeekBar.setMax(mMax-mMin);
        mSeekBar.setProgress(getPersistedInt(mMin)-mMin);
 
         super.onBindView(view);
     }
 
     @Override
     protected View onCreateView(ViewGroup parent) {
         LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.seek_bar_preference, parent, false);
 
         // Add default Preference view created by the superclass. It contains the title and summary which
         // may be styled in some special, hard to detect way. This way of doing this ensures consistency
         // between different preferences.
         View title = super.onCreateView(parent);
         layout.addView(title, 0);
 
         return layout;
     }
 
     @Override
     protected void onSetInitialValue(boolean restore, Object defaultValue) {
         if(!restore) {
             persistInt((Integer) defaultValue);
         }
     }
 
     @Override
     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         int value = progress + mMin;
         mValue.setText(Integer.toString(value));
         persistInt(value);
     }
 
     @Override
     public void onStartTrackingTouch(SeekBar seekBar) {}
 
     @Override
     public void onStopTrackingTouch(SeekBar seekBar) {
         notifyChanged();
     }
 }
