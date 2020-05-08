 /**
  * @file   ListPreference.java
  * @author Josh Liu <yenliangl@gmail.com>
  * @date   Wed Oct  7 17:13:07 2009
  *
  * @brief
  *
  *
  */
 package org.startsmall.openalarm;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.AttributeSet;
 import android.util.Log;
 
 import java.util.ArrayList;
 
 public abstract class ListPreference extends TextViewPreference {
     private int mCheckedEntryIndex = -1;
     private ArrayList<CharSequence> mEntries;
     private ArrayList<CharSequence> mEntryValues;
 
     public ListPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
         mEntries = new ArrayList<CharSequence>();
         mEntryValues = new ArrayList<CharSequence>();
         generateListItems(mEntries, mEntryValues);
     }
 
     public CharSequence[] getEntries() {
         CharSequence[] entries = new CharSequence[mEntries.size()];
         return mEntries.toArray(entries);
     }
 
     public CharSequence[] getEntryValues() {
         CharSequence[] entryValues = new CharSequence[mEntryValues.size()];
         return mEntryValues.toArray(entryValues);
     }
 
     public void setPreferenceValue(String value) {
        mCheckedEntryIndex = findIndexOfValue(value);
        if(mCheckedEntryIndex != -1) {
             super.setPreferenceValue(value);
        } else {                // use default value at index 0
            super.setPreferenceValue("");
         }
     }
 
     public final void setPreferenceValueIndex(int index) {
        if(mEntries.size() >= index && index > -1) {
             mCheckedEntryIndex = index;
             setPreferenceValue(mEntryValues.get(index).toString());
         }
     }
 
     public int findIndexOfValue(String value) {
         if(mEntryValues.size() > 0) {
             // Just search from start to the end.
             for(int i = 0; i < mEntryValues.size(); i++) {
                 if(value.equals(mEntryValues.get(i))) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     protected int getCheckedEntryIndex() {
         return mCheckedEntryIndex;
     }
 
     @Override
     protected String transformValueBeforeDisplay(Object value) {
         if(mCheckedEntryIndex < 0)  {
             return (String)value;
         }
         return mEntries.get(mCheckedEntryIndex).toString();
     }
 
     protected abstract void generateListItems(
         ArrayList<CharSequence> entries,
         ArrayList<CharSequence> entryValues);
 }
