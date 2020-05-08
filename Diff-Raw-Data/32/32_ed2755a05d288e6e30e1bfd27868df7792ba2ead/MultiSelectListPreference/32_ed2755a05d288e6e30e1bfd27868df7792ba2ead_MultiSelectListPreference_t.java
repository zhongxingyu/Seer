 package se.chalmers.dat255.sleepfighter.preference;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
import se.chalmers.dat255.sleepfighter.utils.debug.Debug;

 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.res.TypedArray;
 import android.preference.ListPreference;
 import android.util.AttributeSet;
 
 /**
  * This class is a modified version of https://gist.github.com/cardil/4754571
  * Because we wish to support API levels below 11, so we need this class. 
  * 
  */
 public class MultiSelectListPreference extends ListPreference {
 
     private static String separator;
     private static final String DEFAULT_SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
     private  boolean[] entryChecked;
 
     public MultiSelectListPreference(Context context, AttributeSet attributeSet) {
         super(context, attributeSet);
         entryChecked = new boolean[getEntries().length];
         separator = DEFAULT_SEPARATOR;
     }
 
     public MultiSelectListPreference(Context context) {
         this(context, null);
     }
 
     @Override
     protected void onPrepareDialogBuilder(Builder builder) {
         CharSequence[] entries = getEntries();
         CharSequence[] entryValues = getEntryValues();
         if (entries == null || entryValues == null
                 || entries.length != entryValues.length) {
             throw new IllegalStateException(
                     "MultiSelectListPreference requires an entries array and an entryValues "
                             + "array which are both the same length");
         }
 
         OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
             public void onClick(DialogInterface dialog, int which, boolean val) {
                 entryChecked[which] = val;
             }
         };
         
         builder.setMultiChoiceItems(entries, entryChecked, listener);
     }
 
     public static CharSequence[] unpack(CharSequence val) {
         if (val == null || "".equals(val)) {
             return new CharSequence[0];
         } else {
             return ((String) val).split(separator);
         }
     }
     
     public static String pack( List<CharSequence> values ) {
     	return  join(values, separator);
     }
     
     /**
      * Gets the entries values that are selected
      * 
      * @return the selected entries values
      */
     public CharSequence[] getCheckedValues() {
         return unpack(getValue());
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
 
             String value = pack(values);
 
            if (callChangeListener(value)) {
            	persistString(value);
             }
         }
     }
 
     @Override
     protected Object onGetDefaultValue(TypedArray typedArray, int index) {
         return typedArray.getTextArray(index);
     }
 
     @Override
     protected void onSetInitialValue(boolean restoreValue,
             Object rawDefaultValue) {
         String value = null;
         CharSequence[] defaultValue;
         if (rawDefaultValue == null) {
             defaultValue = new CharSequence[0];
         } else {
             defaultValue = (CharSequence[]) rawDefaultValue;
         }
         List<CharSequence> joined = Arrays.asList(defaultValue);
         String joinedDefaultValue = join(joined, separator);
         if (restoreValue) {
             value = getPersistedString(joinedDefaultValue);
         } else {
             value = joinedDefaultValue;
         }
     }
 
     /**
      * Joins array of object to single string by separator
      * 
      * Credits to kurellajunior on this post
      * http://snippets.dzone.com/posts/show/91
      * 
      * @param iterable
      *            any kind of iterable ex.: <code>["a", "b", "c"]</code>
      * @param separator
      *            separetes entries ex.: <code>","</code>
      * @return joined string ex.: <code>"a,b,c"</code>
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
     
     public void setEntryChecked(boolean[] entryChecked) {
     	this.entryChecked = entryChecked;
     }
 
     public boolean[] getEntryChecked() {
     	return this.entryChecked;
     }
 }
