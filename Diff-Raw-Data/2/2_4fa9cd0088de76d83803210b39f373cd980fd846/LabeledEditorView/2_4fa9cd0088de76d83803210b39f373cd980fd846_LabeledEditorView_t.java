 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.contacts.editor;
 
 import com.android.contacts.ContactsUtils;
 import com.android.contacts.R;
 import com.android.contacts.model.AccountType.DataKind;
 import com.android.contacts.model.AccountType.EditType;
 import com.android.contacts.model.EntityDelta;
 import com.android.contacts.model.EntityDelta.ValuesDelta;
 import com.android.contacts.model.EntityModifier;
 import com.android.contacts.util.DialogManager;
 import com.android.contacts.util.DialogManager.DialogShowingView;
 import com.android.contacts.util.ThemeUtils;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Entity;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import java.util.List;
 
 /**
  * Base class for editors that handles labels and values.
  * Uses {@link ValuesDelta} to read any existing
  * {@link Entity} values, and to correctly write any changes values.
  */
 public abstract class LabeledEditorView extends ViewGroup implements Editor, DialogShowingView {
     protected static final String DIALOG_ID_KEY = "dialog_id";
     private static final int DIALOG_ID_CUSTOM = 1;
 
     private static final int INPUT_TYPE_CUSTOM = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS;
 
     private Spinner mLabel;
     private EditTypeAdapter mEditTypeAdapter;
     private ImageButton mDelete;
 
     private DataKind mKind;
     private ValuesDelta mEntry;
     private EntityDelta mState;
     private boolean mReadOnly;
 
     private EditType mType;
 
     private ViewIdGenerator mViewIdGenerator;
     private DialogManager mDialogManager = null;
     private EditorListener mListener;
     protected int mMinLineItemHeight;
 
     /**
      * A marker in the spinner adapter of the currently selected custom type.
      */
     public static final EditType CUSTOM_SELECTION = new EditType(0, 0);
 
     private OnItemSelectedListener mSpinnerListener = new OnItemSelectedListener() {
 
         @Override
         public void onItemSelected(
                 AdapterView<?> parent, View view, int position, long id) {
             onTypeSelectionChange(position);
         }
 
         @Override
         public void onNothingSelected(AdapterView<?> parent) {
         }
     };
 
     public LabeledEditorView(Context context) {
         super(context);
         init(context);
     }
 
     public LabeledEditorView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init(context);
     }
 
     public LabeledEditorView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init(context);
     }
 
     private void init(Context context) {
         mMinLineItemHeight = context.getResources().getDimensionPixelSize(
                 R.dimen.editor_min_line_item_height);
     }
 
     public boolean isReadOnly() {
         return mReadOnly;
     }
 
     public int getBaseline(int row) {
         if (row == 0 && mLabel != null) {
             return mLabel.getBaseline();
         }
         return -1;
     }
 
     /**
      * Returns the number of rows in this editor, including the invisible ones.
      */
     protected int getLineItemCount() {
         return 1;
     }
 
     protected boolean isLineItemVisible(int row) {
         return true;
     }
 
     protected int getLineItemHeight(int row) {
         int fieldHeight = 0;
         int buttonHeight = 0;
         if (row == 0) {
             // summarize the EditText heights
             if (mLabel != null) {
                 fieldHeight = mLabel.getMeasuredHeight();
             }
 
             // Ensure there is enough space for the minus button
             View deleteButton = getDelete();
             final int deleteHeight = (deleteButton != null) ? deleteButton.getMeasuredHeight() : 0;
             buttonHeight += deleteHeight;
         }
 
         return Math.max(Math.max(buttonHeight, fieldHeight), mMinLineItemHeight);
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         measureChildren(widthMeasureSpec, heightMeasureSpec);
 
         int height = 0;
         height += getPaddingTop() + getPaddingBottom();
 
         int count = getLineItemCount();
         for (int i = 0; i < count; i++) {
             if (isLineItemVisible(i)) {
                 height += getLineItemHeight(i);
             }
         }
 
         setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                 resolveSize(height, heightMeasureSpec));
     }
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         // Subtract padding from the borders ==> x1 variables
         int t1 = getPaddingTop();
         int r1 = getMeasuredWidth() - getPaddingRight();
         int b1 = getMeasuredHeight() - getPaddingBottom();
 
         final int r2;
         if (mDelete != null) {
             r2 = r1 - mDelete.getMeasuredWidth();
             // Vertically center the delete button in the first line item
             int height = mDelete.getMeasuredHeight();
             int top = t1 + (mMinLineItemHeight - height) / 2;
             mDelete.layout(
                     r2, top,
                     r1, top + height);
         } else {
             r2 = r1;
         }
 
         if (mLabel != null) {
             int baseline = getBaseline(0);
             int y = t1 + baseline - mLabel.getBaseline();
             mLabel.layout(
                     r2 - mLabel.getMeasuredWidth(), y,
                     r2, y + mLabel.getMeasuredHeight());
         }
     }
 
     /**
      * Creates or removes the type/label button. Doesn't do anything if already correctly configured
      */
     private void setupLabelButton(boolean shouldExist) {
         if (shouldExist && mLabel == null) {
             mLabel = new Spinner(mContext);
             final int width =
                     mContext.getResources().getDimensionPixelSize(R.dimen.editor_type_label_width);
             mLabel.setLayoutParams(new LayoutParams(width, LayoutParams.WRAP_CONTENT));
             mLabel.setOnItemSelectedListener(mSpinnerListener);
             mLabel.setEnabled(!mReadOnly && isEnabled());
             addView(mLabel);
         } else if (!shouldExist && mLabel != null) {
             removeView(mLabel);
             mLabel = null;
         }
     }
 
     /**
      * Creates or removes the remove button. Doesn't do anything if already correctly configured
      */
     private void setupDeleteButton(boolean shouldExist) {
         if (shouldExist && mDelete == null) {
             mDelete = new ImageButton(mContext);
             mDelete.setImageResource(R.drawable.ic_menu_remove_field_holo_light);
             mDelete.setBackgroundResource(
                     ThemeUtils.getSelectableItemBackground(mContext.getTheme()));
             final Resources resources = mContext.getResources();
             mDelete.setPadding(
                     resources.getDimensionPixelOffset(R.dimen.editor_round_button_padding_left),
                     resources.getDimensionPixelOffset(R.dimen.editor_round_button_padding_top),
                     resources.getDimensionPixelOffset(R.dimen.editor_round_button_padding_right),
                     resources.getDimensionPixelOffset(R.dimen.editor_round_button_padding_bottom));
             mDelete.setContentDescription(
                     getResources().getText(R.string.description_minus_button));
             mDelete.setLayoutParams(
                     new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
             mDelete.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     // defer removal of this button so that the pressed state is visible shortly
                     new Handler().post(new Runnable() {
                         @Override
                         public void run() {
                             // Keep around in model, but mark as deleted
                             mEntry.markDeleted();
 
                             ((ViewGroup) getParent()).removeView(LabeledEditorView.this);
 
                             if (mListener != null) {
                                 // Notify listener when present
                                 mListener.onDeleted(LabeledEditorView.this);
                             }
                         }
                     });
                 }
             });
             mDelete.setEnabled(!mReadOnly && isEnabled());
             addView(mDelete);
         } else if (!shouldExist && mDelete != null) {
             removeView(mDelete);
             mDelete = null;
         }
     }
 
     protected void onOptionalFieldVisibilityChange() {
         if (mListener != null) {
             mListener.onRequest(EditorListener.EDITOR_FORM_CHANGED);
         }
     }
 
     @Override
     public void setEditorListener(EditorListener listener) {
         mListener = listener;
     }
 
     @Override
     public void setDeletable(boolean deletable) {
         setupDeleteButton(deletable);
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
         if (mLabel != null) mLabel.setEnabled(!mReadOnly && enabled);
         if (mDelete != null) mDelete.setEnabled(!mReadOnly && enabled);
     }
 
     public Spinner getLabel() {
         return mLabel;
     }
 
     public ImageButton getDelete() {
         return mDelete;
     }
 
     protected DataKind getKind() {
         return mKind;
     }
 
     protected ValuesDelta getEntry() {
         return mEntry;
     }
 
     protected EditType getType() {
         return mType;
     }
 
     /**
      * Build the current label state based on selected {@link EditType} and
      * possible custom label string.
      */
     private void rebuildLabel() {
         if (mLabel == null) return;
         mEditTypeAdapter = new EditTypeAdapter(mContext);
         mLabel.setAdapter(mEditTypeAdapter);
         if (mEditTypeAdapter.hasCustomSelection()) {
             mLabel.setSelection(mEditTypeAdapter.getPosition(CUSTOM_SELECTION));
         } else {
             mLabel.setSelection(mEditTypeAdapter.getPosition(mType));
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void onFieldChanged(String column, String value) {
         String oldValue = mEntry.getAsString(column);
         if (oldValue == null && value.equals("") || oldValue != null && oldValue.equals(value)) {
             return;
         }
 
         // Field changes are saved directly
         mEntry.put(column, value);
         if (mListener != null) {
             mListener.onRequest(EditorListener.FIELD_CHANGED);
         }
     }
 
     protected void rebuildValues() {
         setValues(mKind, mEntry, mState, mReadOnly, mViewIdGenerator);
     }
 
     /**
      * Prepare this editor using the given {@link DataKind} for defining
      * structure and {@link ValuesDelta} describing the content to edit.
      */
     @Override
     public void setValues(DataKind kind, ValuesDelta entry, EntityDelta state, boolean readOnly,
             ViewIdGenerator vig) {
         mKind = kind;
         mEntry = entry;
         mState = state;
         mReadOnly = readOnly;
         mViewIdGenerator = vig;
         setId(vig.getId(state, kind, entry, ViewIdGenerator.NO_VIEW_INDEX));
 
         if (!entry.isVisible()) {
             // Hide ourselves entirely if deleted
             setVisibility(View.GONE);
             return;
         }
         setVisibility(View.VISIBLE);
 
         // Display label selector if multiple types available
         final boolean hasTypes = EntityModifier.hasEditTypes(kind);
         setupLabelButton(hasTypes);
         if (mLabel != null) mLabel.setEnabled(!readOnly && isEnabled());
         if (hasTypes) {
             mType = EntityModifier.getCurrentType(entry, kind);
             rebuildLabel();
         }
     }
 
     public ValuesDelta getValues() {
         return mEntry;
     }
 
     /**
      * Prepare dialog for entering a custom label. The input value is trimmed: white spaces before
      * and after the input text is removed.
      * <p>
      * If the final value is empty, this change request is ignored;
      * no empty text is allowed in any custom label.
      */
     private Dialog createCustomDialog() {
         final EditText customType = new EditText(mContext);
         customType.setInputType(INPUT_TYPE_CUSTOM);
         customType.setSaveEnabled(true);
         customType.requestFocus();
 
         final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
         builder.setTitle(R.string.customLabelPickerTitle);
         builder.setView(customType);
 
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 final String customText = customType.getText().toString().trim();
                 if (ContactsUtils.isGraphic(customText)) {
                     final List<EditType> allTypes =
                             EntityModifier.getValidTypes(mState, mKind, null);
                     mType = null;
                     for (EditType editType : allTypes) {
                         if (editType.customColumn != null) {
                             mType = editType;
                             break;
                         }
                     }
                     if (mType == null) return;
 
                     mEntry.put(mKind.typeColumn, mType.rawValue);
                     mEntry.put(mType.customColumn, customText);
                     rebuildLabel();
                     requestFocusForFirstEditField();
                     onLabelRebuilt();
                 }
             }
         });
 
         builder.setNegativeButton(android.R.string.cancel, null);
 
         return builder.create();
     }
 
     /**
      * Called after the label has changed (either chosen from the list or entered in the Dialog)
      */
     protected void onLabelRebuilt() {
     }
 
     protected void onTypeSelectionChange(int position) {
         EditType selected = mEditTypeAdapter.getItem(position);
         // See if the selection has in fact changed
         if (mEditTypeAdapter.hasCustomSelection() && selected == CUSTOM_SELECTION) {
             return;
         }
 
         if (mType == selected && mType.customColumn == null) {
             return;
         }
 
         if (selected.customColumn != null) {
             showDialog(DIALOG_ID_CUSTOM);
         } else {
             // User picked type, and we're sure it's ok to actually write the entry.
             mType = selected;
             mEntry.put(mKind.typeColumn, mType.rawValue);
             rebuildLabel();
             requestFocusForFirstEditField();
             onLabelRebuilt();
         }
     }
 
     /* package */
     void showDialog(int bundleDialogId) {
         Bundle bundle = new Bundle();
         bundle.putInt(DIALOG_ID_KEY, bundleDialogId);
         getDialogManager().showDialogInView(this, bundle);
     }
 
     private DialogManager getDialogManager() {
         if (mDialogManager == null) {
             Context context = getContext();
             if (!(context instanceof DialogManager.DialogShowingViewActivity)) {
                 throw new IllegalStateException(
                         "View must be hosted in an Activity that implements " +
                         "DialogManager.DialogShowingViewActivity");
             }
             mDialogManager = ((DialogManager.DialogShowingViewActivity)context).getDialogManager();
         }
         return mDialogManager;
     }
 
     @Override
     public Dialog createDialog(Bundle bundle) {
         if (bundle == null) throw new IllegalArgumentException("bundle must not be null");
         int dialogId = bundle.getInt(DIALOG_ID_KEY);
         switch (dialogId) {
             case DIALOG_ID_CUSTOM:
                 return createCustomDialog();
             default:
                 throw new IllegalArgumentException("Invalid dialogId: " + dialogId);
         }
     }
 
     protected abstract void requestFocusForFirstEditField();
 
     private class EditTypeAdapter extends ArrayAdapter<EditType> {
         private final LayoutInflater mInflater;
         private boolean mHasCustomSelection;
 
         public EditTypeAdapter(Context context) {
             super(context, 0);
             mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
            if (mType != null && mType.customColumn != null) {
 
                 // Use custom label string when present
                 final String customText = mEntry.getAsString(mType.customColumn);
                 if (customText != null) {
                     add(CUSTOM_SELECTION);
                     mHasCustomSelection = true;
                 }
             }
 
             addAll(EntityModifier.getValidTypes(mState, mKind, mType));
         }
 
         public boolean hasCustomSelection() {
             return mHasCustomSelection;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             return createViewFromResource(
                     position, convertView, parent, android.R.layout.simple_spinner_item);
         }
 
         @Override
         public View getDropDownView(int position, View convertView, ViewGroup parent) {
             return createViewFromResource(
                     position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
         }
 
         private View createViewFromResource(int position, View convertView, ViewGroup parent,
                 int resource) {
             View view;
             TextView textView;
 
             if (convertView == null) {
                 view = mInflater.inflate(resource, parent, false);
             } else {
                 view = convertView;
             }
 
             textView = (TextView) view;
 
             EditType type = getItem(position);
             String text;
             if (type == CUSTOM_SELECTION) {
                 text = mEntry.getAsString(mType.customColumn);
             } else {
                 text = getContext().getString(type.labelRes);
             }
             textView.setText(text);
             return view;
         }
     }
 }
