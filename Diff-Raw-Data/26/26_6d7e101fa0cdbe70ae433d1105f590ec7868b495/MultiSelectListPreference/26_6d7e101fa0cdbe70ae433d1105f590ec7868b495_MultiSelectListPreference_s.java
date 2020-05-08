 package com.sndurkin.notificationcheck;
 
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.TypedArray;
 import android.preference.ListPreference;
 import android.util.AttributeSet;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 public class MultiSelectListPreference extends ListPreference {
 
     public static final String SEPARATOR = "|";
     public static final String SEPARATOR_REGEX = "\\|";
 
     private boolean[] entryChecked;
 
     public MultiSelectListPreference(Context context, AttributeSet attributeSet) {
         super(context, attributeSet);
         entryChecked = new boolean[getEntries().length];
     }
 
     public MultiSelectListPreference(Context context) {
         this(context, null);
     }
 
     @Override
     protected void onPrepareDialogBuilder(Builder builder) {
         CharSequence[] entries = getEntries();
         CharSequence[] entryValues = getEntryValues();
         if (entries == null || entryValues == null || entries.length != entryValues.length) {
             throw new IllegalStateException(
                     "MultiSelectListPreference requires an entries array and an entryValues "
                             + "array which are both the same length");
         }
 
         restoreCheckedEntries();
         DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
             public void onClick(DialogInterface dialog, int which, boolean val) {
                 entryChecked[which] = val;
             }
         };
         builder.setMultiChoiceItems(entries, entryChecked, listener);
     }
 
     private CharSequence[] unpack(CharSequence val) {
         if (val == null || val.length() == 0) {
             return new CharSequence[0];
         } else {
             return ((String) val).split(SEPARATOR_REGEX);
         }
     }
 
     public static List<Integer> getValuesFromString(String valuesStr) {
        if(valuesStr.isEmpty()) {
             return new ArrayList<Integer>();
         }
         String[] values = valuesStr.split(SEPARATOR_REGEX);
         List<Integer> intValues = new ArrayList<Integer>();
         for(int i = 0; i < values.length; ++i) {
             intValues.add(Integer.parseInt(values[i]));
         }
         return intValues;
     }
 
     public CharSequence[] getCheckedValues() {
         return unpack(getValue());
     }
 
     private void restoreCheckedEntries() {
         CharSequence[] entryValues = getEntryValues();
         CharSequence[] vals = unpack(getValue());
         if (vals != null) {
             List<CharSequence> valuesList = Arrays.asList(vals);
             for (int i = 0; i < entryValues.length; i++) {
                 CharSequence entry = entryValues[i];
                 entryChecked[i] = valuesList.contains(entry);
             }
         }
     }
 
     @Override
     protected void onDialogClosed(boolean positiveResult) {
         List<CharSequence> values = new ArrayList<CharSequence>();
 
         CharSequence[] entryValues = getEntryValues();
         if (positiveResult && entryValues != null) {
             for (int i = 0; i < entryValues.length; i++) {
                 if (entryChecked[i] == true) {
                     String val = (String) entryValues[i];
                     values.add(val);
                 }
             }
 
             String value = join(values, SEPARATOR);
             setValueAndEvent(value);
         }
     }
 
     private void setValueAndEvent(String value) {
         if (callChangeListener(unpack(value))) {
             setValue(value);
         }
     }
 
     // TODO
     /*
     private CharSequence prepareSummary(List<CharSequence> joined) {
         List<String> titles = new ArrayList<String>();
         CharSequence[] entryTitle = getEntries();
         CharSequence[] entryValues = getEntryValues();
         int ix = 0;
         for (CharSequence value : entryValues) {
             if (joined.contains(value)) {
                 titles.add((String) entryTitle[ix]);
             }
             ix += 1;
         }
         return join(titles, ", ");
     }
     */
 
     @Override
     protected Object onGetDefaultValue(TypedArray typedArray, int index) {
         return typedArray.getTextArray(index);
     }
 
     @Override
     protected void onSetInitialValue(boolean restoreValue,
                                      Object rawDefaultValue) {
         CharSequence[] defaultValue;
         if (rawDefaultValue == null) {
             defaultValue = new CharSequence[0];
         }
         else {
             defaultValue = (CharSequence[]) rawDefaultValue;
         }
         List<CharSequence> joined = Arrays.asList(defaultValue);
         String joinedDefaultValue = join(joined, SEPARATOR);
 
         String value;
         if (restoreValue) {
             value = getPersistedString(joinedDefaultValue);
         }
         else {
             value = joinedDefaultValue;
         }
 
         setValueAndEvent(value);
     }
 
     /**
      * Joins an array of objects to a single string by a separator.
      *
      * Credits to kurellajunior on this post: http://snippets.dzone.com/posts/show/91
      */
     protected static String join(Iterable<?> iterable, String separator) {
         Iterator<?> oIter;
         if (iterable == null || (!(oIter = iterable.iterator()).hasNext()))
             return "";
         StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
         while (oIter.hasNext())
             oBuilder.append(separator).append(oIter.next());
         return oBuilder.toString();
     }
 
 }
