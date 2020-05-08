 /*
  * Copyright 2010 - Brion Noble Emde
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.eyebrowssoftware.bptrackerfree.activity;
 
 
 import java.lang.reflect.Field;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewStub;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
 import com.eyebrowssoftware.bptrackerfree.BPTrackerFree;
 import com.eyebrowssoftware.bptrackerfree.R;
 
 /**
  * @author brionemde
  *
  */
 public class BPRecordEditorText extends BPRecordEditorBase {
 
     // Static constants
 
     protected static final String TAG = "BPRecordEditorText";
 
     private static final int SYS_IDX = 0;
     private static final int DIA_IDX = 1;
     private static final int PLS_IDX = 2;
     private static final int VALUES_ARRAY_SIZE  = PLS_IDX + 1;
 
     private EditText[] mEditValues = new EditText[VALUES_ARRAY_SIZE];
 
     @TargetApi(5)
     @Override
     protected void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         ViewStub myStub = (ViewStub) findViewById(R.id.edit_texts_stub);
         View myEditTexts = myStub.inflate();
 
         mEditValues[SYS_IDX] = (EditText) myEditTexts.findViewById(R.id.systolic_edit_text);
 
         mEditValues[DIA_IDX] = (EditText) myEditTexts.findViewById(R.id.diastolic_edit_text);
 
         mEditValues[PLS_IDX] = (EditText) myEditTexts.findViewById(R.id.pulse_edit_text);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         // The user is going somewhere else, so make sure their current
         // changes are safely saved away in the provider. We don't need
         // to do this if only editing.
         if (mCurrentValues != null) {
            mCurrentValues.put(BPRecord.SYSTOLIC, Integer.valueOf(mEditValues[SYS_IDX].getText().toString()));
            mCurrentValues.put(BPRecord.DIASTOLIC, Integer.valueOf(mEditValues[DIA_IDX].getText().toString()));
            mCurrentValues.put(BPRecord.PULSE, Integer.valueOf(mEditValues[PLS_IDX].getText().toString()));

             updateFromCurrentValues();
         }
     }
 
     @TargetApi(5)
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         Field sdk_field = null;
         int sdk = 0;
         try {
             sdk_field = android.os.Build.VERSION.class.getField("SDK_INT");
             Build build = new android.os.Build();
             sdk = sdk_field.getInt(build);
         } catch (SecurityException e) {
         } catch (NoSuchFieldException e) {
         } catch (IllegalArgumentException e) {
         } catch (IllegalAccessException e) {
         }
         if(sdk_field == null) {
             sdk = 0;
         }
         if(sdk < android.os.Build.VERSION_CODES.ECLAIR
             && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
             onBackPressed();
             return true;
         } else {
             return super.onKeyDown(keyCode, event);
         }
     }
 
     @Override
     public void onBackPressed() {
         boolean invalid = false;
         String range;
 
         String sys_string = mEditValues[SYS_IDX].getText().toString();
         String dia_string = mEditValues[DIA_IDX].getText().toString();
         String pls_string = mEditValues[PLS_IDX].getText().toString();
         if(sys_string == null || sys_string.length() == 0) {
             Toast.makeText(this, R.string.sys_not_empty, Toast.LENGTH_SHORT).show();
             invalid = true;
         } else {
             int systolic = Integer.valueOf(sys_string);
             if(systolic < BPTrackerFree.SYSTOLIC_MIN_DEFAULT || systolic > BPTrackerFree.SYSTOLIC_MAX_DEFAULT) {
                 range = getString(R.string.sys_range_error);
                 Toast.makeText(this, String.format(range, BPTrackerFree.SYSTOLIC_MIN_DEFAULT,
                         BPTrackerFree.SYSTOLIC_MAX_DEFAULT), Toast.LENGTH_SHORT).show();
                 invalid = true;
             }
         }
         if(dia_string == null || dia_string.length() == 0) {
             Toast.makeText(this, R.string.dia_not_empty, Toast.LENGTH_SHORT).show();
             invalid = true;
         } else {
             int diastolic = Integer.valueOf(dia_string);
             if(diastolic < BPTrackerFree.DIASTOLIC_MIN_DEFAULT || diastolic > BPTrackerFree.DIASTOLIC_MAX_DEFAULT) {
                 range = getString(R.string.dia_range_error);
                 Toast.makeText(this, String.format(range, BPTrackerFree.DIASTOLIC_MIN_DEFAULT,
                     BPTrackerFree.DIASTOLIC_MAX_DEFAULT), Toast.LENGTH_SHORT).show();
                 invalid = true;
             }
         }
         if(pls_string == null || pls_string.length() == 0) {
             Toast.makeText(this, R.string.pls_not_empty, Toast.LENGTH_SHORT).show();
             invalid = true;
         } else {
             int pulse = Integer.valueOf(pls_string);
             if(pulse < BPTrackerFree.PULSE_MIN_DEFAULT || pulse > BPTrackerFree.PULSE_MAX_DEFAULT) {
                 range = getString(R.string.pls_range_error);
                 Toast.makeText(this, String.format(range, BPTrackerFree.PULSE_MIN_DEFAULT,
                     BPTrackerFree.PULSE_MAX_DEFAULT), Toast.LENGTH_SHORT).show();
                 invalid = true;
             }
         }
 
         if(!invalid) {
             finish();
         }
         return;
     }
 
     @Override
     protected void setUIState() {
         super.setUIState();
         if (mCurrentValues != null) {
             mEditValues[SYS_IDX].setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.SYSTOLIC)));
             mEditValues[DIA_IDX].setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.DIASTOLIC)));
             mEditValues[PLS_IDX].setText(String.valueOf(mCurrentValues.getAsInteger(BPRecord.PULSE)));
         }
     }
 }
