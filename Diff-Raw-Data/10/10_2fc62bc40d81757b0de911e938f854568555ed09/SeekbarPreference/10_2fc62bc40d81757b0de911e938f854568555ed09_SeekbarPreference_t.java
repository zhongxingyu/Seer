 /*
  * Copyright 2012 akaiosorani(akaiosorani@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package jp.srgtndr.akaiosorani.android.util;
 
 import android.content.Context;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class SeekbarPreference extends DialogPreference {
 
     public static final int MIN_VALUE = 0;
     public static final int MAX_VALUE = 100;
     
     private int minValue = MIN_VALUE;
     private int maxValue = MAX_VALUE;
 
     private SeekBar bar;
     public SeekbarPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
         setPersistent(true);
     }
 
     public void setMin(int min)
     {
         this.minValue = min;
     }
     public void setMax(int max)
     {
         this.maxValue = max;
     }
 
     public void setRange(int min, int max)
     {
         minValue = Math.min(min, max);
         maxValue = Math.max(min, max);
     }
     @Override
     protected View onCreateView(ViewGroup parent) {
         return super.onCreateView(parent);
     }
 
     @Override
     protected void onBindDialogView(View view) {
         super.onBindDialogView(view);
     }
     @Override
     protected void onDialogClosed(boolean positiveResult) {
         super.onDialogClosed(positiveResult);
         if(positiveResult) {
             persistInt(getValue());
         }
     }
     @Override
     protected View onCreateDialogView() {
         int defaultValue = getPersistedInt(minValue);
         LinearLayout layout = new LinearLayout(getContext());
         layout.setOrientation(LinearLayout.VERTICAL);
 
         TextView label = new TextView(getContext());
         label.setFocusable(false);
         label.setText(getTitle());
 
         LinearLayout container = new LinearLayout(getContext());
         container.setOrientation(LinearLayout.HORIZONTAL);
         container.setPadding(5, 5, 5, 5);
         container.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 
         final EditText currentValue = new EditText(getContext());
         currentValue.setEnabled(false);
         currentValue.setText(String.format("%d", getProgress(defaultValue)));
         LayoutParams params = new LayoutParams(25, LayoutParams.WRAP_CONTENT, 1);
         currentValue.setLayoutParams(params);
         currentValue.setGravity(Gravity.CENTER | Gravity.RIGHT);
 
         bar = new SeekBar(getContext());
         bar.setPadding(5, 5, 5, 5);
         bar.setMax(maxValue - minValue);
         bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentValue.setText(Integer.valueOf(getValue()).toString());
             }
         });
         bar.setProgress(getProgress(defaultValue));
         bar.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 4));
 
         container.addView(bar);
         container.addView(currentValue);
         layout.addView(container);
         return layout;
     }
     
     private int getValue()
     {
         if (bar == null) {
             return minValue;
         }
         return bar.getProgress() + minValue;
     }
     private int getProgress(int value)
     {
         if (bar == null) {
             return 0;
         }
         if (value > maxValue) {
             return bar.getMax();
         }
         if (value < minValue) {
             return 0;
         }
         return value - minValue;
     }
 }
