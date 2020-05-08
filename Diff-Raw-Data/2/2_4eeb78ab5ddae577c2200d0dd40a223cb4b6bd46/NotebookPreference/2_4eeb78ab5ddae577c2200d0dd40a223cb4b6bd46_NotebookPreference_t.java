 /*
  * Copyright 2011 Takuo Kitame.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jp.takuo.android.twicca.plugin.evernote;
 
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.preference.DialogPreference;
 import android.text.TextUtils;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class NotebookPreference extends DialogPreference {
 
     private Spinner mSpinner;
     private EditText mEditText;
 
     private ArrayList<String> mNotebooks;
     private String mText;
 
     public NotebookPreference(Context context,
             AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         mSpinner = new Spinner(context, attrs);
         mSpinner.setId(android.R.id.list);
         mSpinner.setEnabled(true);
 
         mEditText = new EditText(context, attrs);
         mEditText.setId(android.R.id.edit);
         mEditText.setEnabled(true);
         mEditText.setHint(context.getString(R.string.hint_empty));
     }
 
     public NotebookPreference(Context context, AttributeSet attrs) {
         this(context, attrs, android.R.attr.editTextPreferenceStyle);
     }
 
     public NotebookPreference(Context context) {
         this(context, null);
     }
 
     public void setText(String text) {
         final boolean wasBlocking = shouldDisableDependents();
         mText = text;
         persistString(text);
         final boolean isBlocking = shouldDisableDependents();
         if (isBlocking == wasBlocking) {
             notifyDependencyChange(isBlocking);
         }
     }
 
     public String getText() {
        return mText != null ? mText : "";
     }
 
     public int getSelection() {
         if (mNotebooks == null) return 0;
         int index = 0;
         String text = getText();
         ListIterator<String> itr = mNotebooks.listIterator();
         while (itr.hasNext()) {
             String string = itr.next();
             if (text.equals(string)) break;
             index++;
         }
         return index;
     }
 
     @Override
     protected void onBindDialogView(View view) {
         super.onBindDialogView(view);
 
         Spinner spinner = mSpinner;
         spinner.setSelection(getSelection());
         ViewParent oldParent = spinner.getParent();
         if (oldParent != view) {
             if (oldParent != null) {
                 ((ViewGroup) oldParent).removeView(spinner);
             }
             onAddSpinnerToDialogView(view, spinner);
         }
 
         EditText editText = mEditText;
         if (spinner.getSelectedItemPosition() == spinner.getCount() - 1) {
             editText.setText(getText());
         } else {
             editText.setText("");
         }
 
         oldParent = editText.getParent();
         if (oldParent != view) {
             if (oldParent != null) {
                 ((ViewGroup) oldParent).removeView(editText);
             }
             onAddEditTextToDialogView(view, editText);
         }
 
     }
 
     protected void onAddSpinnerToDialogView(View dialogView,
             Spinner spinner) {
         ViewGroup container = (ViewGroup) dialogView
             .findViewById(R.id.base_container);
         if (container != null) {
             container.addView(spinner, ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT);
         }
     }
 
     protected void onAddEditTextToDialogView(View dialogView,
             EditText textView) {
         ViewGroup container = (ViewGroup) dialogView
             .findViewById(R.id.base_container);
         if (container != null) {
             container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT);
         }
     }
 
     @Override
     protected View onCreateDialogView() {
         LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = inflater.inflate(R.layout.notebook_preference, null);
         return view;
     }
 
     @Override
     protected void onDialogClosed(boolean positiveResult) {
         super.onDialogClosed(positiveResult);
 
         if (positiveResult) {
             int index = getSpinner().getSelectedItemPosition();
             String value = (String) getSpinner().getSelectedItem();
             if (index == getSpinner().getCount() - 1) {
                 value = getEdit().getText().toString();
             }
             if (callChangeListener(value)) {
                 setText(value);
             }
         }
     }
 
     @Override
     protected Object onGetDefaultValue(TypedArray a, int index) {
         return a.getString(index);
     }
 
     @Override
     protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
         String value = restoreValue ? getPersistedString(mText) : (String) defaultValue;
         setText(value);
     }
 
     @Override
     public boolean shouldDisableDependents() {
         return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
     }
 
     public EditText getEdit() {
         return mEditText;
     }
 
     public Spinner getSpinner() {
         return mSpinner;
     }
 
     public void setNotebookList(ArrayList<String> array) {
         mNotebooks = (ArrayList<String>) array.clone();
         String notebook = getText();
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                 R.layout.dropdown_list_item, array);
         Spinner spinner = getSpinner();
         spinner.setAdapter(adapter);
         int i = 0;
         if (notebook.length() > 0) {
             ListIterator<String> itr = array.listIterator();
             while (itr.hasNext()) {
                 String name = itr.next();
                 if (notebook.equalsIgnoreCase(name))
                     break;
                 i++;
             }
         }
         adapter.add(getContext().getString(R.string.new_or_default));
         spinner.setAdapter(adapter);
         spinner.setSelection(i);
         if (i == mNotebooks.size()) {
             getEdit().setEnabled(true);
             if (notebook.length() > 0) {
                 getEdit().setText(notebook);
             }
         } else {
             getEdit().setEnabled(false);
         }
         spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> arg0, View arg1,
                     int arg2, long arg3) {
                 // TODO Auto-generated method stub
                 if (arg2 == mNotebooks.size()) {
                     getEdit().setEnabled(true);
                     getEdit().requestFocus();
                 } else {
                     getEdit().setEnabled(false);
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) {
                 // TODO Auto-generated method stub
                 getEdit().setEnabled(true);
                 getEdit().requestFocus();
             }
         });
     }
 
     @Override
     protected Parcelable onSaveInstanceState() {
         final Parcelable superState = super.onSaveInstanceState();
         if (isPersistent()) {
             return superState;
         }
         final SavedState myState = new SavedState(superState);
         myState.text = getText();
         return myState;
     }
 
     @Override
     protected void onRestoreInstanceState(Parcelable state) {
         if (state == null || !state.getClass().equals(SavedState.class)) {
             super.onRestoreInstanceState(state);
             return;
         }
         SavedState myState = (SavedState) state;
         super.onRestoreInstanceState(myState.getSuperState());
         setText(myState.text);
     }
 
     private static class SavedState extends BaseSavedState {
         String text;
 
         public SavedState(Parcel source) {
             super(source);
             text = source.readString();
         }
 
         @Override
         public void writeToParcel(Parcel dest, int flags) {
             super.writeToParcel(dest, flags);
             dest.writeString(text);
         }
 
         public SavedState (Parcelable superState) {
             super(superState);
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
