 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package net.redgeek.android.eventrend.category;
 
 import java.util.ArrayList;
 
 import net.redgeek.android.eventrend.EvenTrendActivity;
 import net.redgeek.android.eventrend.R;
 import net.redgeek.android.eventrend.db.CategoryDbTable;
 import net.redgeek.android.eventrend.graph.plugins.TimeSeriesInterpolator;
 import net.redgeek.android.eventrend.synthetic.Formula;
 import net.redgeek.android.eventrend.synthetic.FormulaEditorActivity;
 import net.redgeek.android.eventrend.util.ColorPickerDialog;
 import net.redgeek.android.eventrend.util.ComboBox;
 import net.redgeek.android.eventrend.util.DynamicSpinner;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.LinearLayout.LayoutParams;
 
 public class CategoryEditActivity extends EvenTrendActivity {
   static final int DELETE_DIALOG_ID = 0;
   static final int DIALOG_HELP_GROUP = 1;
   static final int DIALOG_HELP_CATEGORY = 2;
   static final int DIALOG_HELP_GOAL = 3;
   static final int DIALOG_HELP_COLOR = 4;
   static final int DIALOG_HELP_AGGREGATE = 5;
   static final int DIALOG_HELP_INTERP = 6;
   // synthetic config:
   static final int DIALOG_HELP_SYNTHETIC = 7;
   static final int DIALOG_HELP_FORMULA = 8;
   // standard config:
   static final int DIALOG_HELP_DEFAULT_VALUE = 9;
   static final int DIALOG_HELP_INCREMENT = 10;
   static final int DIALOG_HELP_TYPE = 11;
   static final int DIALOG_HELP_ZEROFILL = 12;
 
   // UI elements
   private LinearLayout mGroupComboLayout;
   private LinearLayout mPeriodRow;
   private LinearLayout mInterpRow;
   private ComboBox mGroupCombo;
   private EditText mCategoryText;
   private Button mColorButton;
   private DynamicSpinner mPeriodSpinner;
   private DynamicSpinner mInterpSpinner;
   private EditText mGoalText;
   private Button mOk;
   private Button mDelete;
   private CheckBox mSyntheticCheck;
   private RadioGroup mTypeRadioGroup;
   private RadioButton mTypeRadio;
   // synthetic elements:
   private TableRow mFormulaRow;
   private Button mFormulaEdit;
   // standard elements:
   private TableRow mDefaultValueRow;
   private EditText mDefaultValueText;
   private TableRow mIncrementRow;
   private EditText mIncrementText;
   private TableRow mZeroFillRow;
   private CheckBox mZeroFillCheck;
 
   // Private data
   private CategoryDbTable.Row mRow;
   private String mPeriod;
   private String mInterp;
   private String mGroupName;
   private long mPeriodMs;
   private int mRank;
 
   private Paint mPickerPaint;
   private String mColorStr;
   private Long mRowId;
   private int mMaxRank = 1;
   private boolean mSave = false;
 
   private Formula mFormula;
 
   // Listeners
   private RadioGroup.OnCheckedChangeListener mTypeListener;
   private ColorPickerDialog.OnColorChangedListener mColorChangeListener;
   private View.OnClickListener mColorButtonListener;
   private Spinner.OnItemSelectedListener mInterpolationListener;
   private Spinner.OnItemSelectedListener mPeriodListener;
   private CompoundButton.OnCheckedChangeListener mSyntheticListener;
   private View.OnClickListener mFormulaEditListener;
   private View.OnClickListener mOkListener;
   private View.OnClickListener mDeleteListener;
 
   @Override
   public void onCreate(Bundle icicle) {
     super.onCreate(icicle);
 
     getSavedStateContent(icicle);
     setupUI();
     populateFields();
     updatePaint(mColorStr);
   }
 
   private void getSavedStateContent(Bundle icicle) {
     if (icicle != null) {
       mRowId = icicle.getLong(CategoryDbTable.KEY_ROWID);
       if (mRowId < 0)
         mRowId = null;
     }
     if (mRowId == null) {
       Bundle extras = getIntent().getExtras();
       if (extras != null) {
         mRowId = extras.getLong(CategoryDbTable.KEY_ROWID);
       }
     }
 
     mMaxRank = getDbh().fetchCategoryMaxRank() + 1;
     if (mRowId == null)
       mRow = new CategoryDbTable.Row();
     else
       mRow = getDbh().fetchCategory(mRowId);
   }
 
   private void setupUI() {
     setContentView(R.layout.category_edit);
 
     setupListeners();
 
     mGroupCombo = new ComboBox(getCtx());
     mGroupComboLayout = (LinearLayout) findViewById(R.id.category_edit_group);
     mGroupComboLayout.addView(mGroupCombo, new LinearLayout.LayoutParams(
         LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
     Cursor c = getDbh().fetchAllGroups();
     c.moveToFirst();
     for (int i = 0; i < c.getCount(); i++) {
       String group = CategoryDbTable.getGroupName(c);
       mGroupCombo.addMenuItem(group);
       if (mGroupName != null && mGroupName.equals(group))
         mGroupCombo.setSelection(i);
       c.moveToNext();
     }
     c.close();
     setHelpDialog(R.id.category_edit_group_view, DIALOG_HELP_GROUP);
 
     mCategoryText = (EditText) findViewById(R.id.category_edit_name);
     setHelpDialog(R.id.category_edit_name_view, DIALOG_HELP_CATEGORY);
 
     mGoalText = (EditText) findViewById(R.id.category_edit_goal);
     setHelpDialog(R.id.category_edit_goal_view, DIALOG_HELP_GOAL);
 
     mColorButton = (Button) findViewById(R.id.category_edit_color);
     mColorButton.setOnClickListener(mColorButtonListener);
     setHelpDialog(R.id.category_edit_color_view, DIALOG_HELP_COLOR);
 
     mInterpSpinner = new DynamicSpinner(getCtx());
     mInterpRow = (LinearLayout) findViewById(R.id.category_edit_interp_menu);
     mInterpRow.addView(mInterpSpinner, new LinearLayout.LayoutParams(
         LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
     ArrayList<TimeSeriesInterpolator> interpolators = ((EvenTrendActivity) getCtx())
         .getInterpolators();
     for (int i = 0; i < interpolators.size(); i++) {
       String name = interpolators.get(i).getName();
       mInterpSpinner.addSpinnerItem(name, new Long(i));
     }
     mInterpSpinner.setOnItemSelectedListener(mInterpolationListener);
     setHelpDialog(R.id.category_edit_interp_view, DIALOG_HELP_INTERP);
 
     mPeriodSpinner = new DynamicSpinner(getCtx());
     mPeriodRow = (LinearLayout) findViewById(R.id.category_edit_agg_menu);
     mPeriodRow.addView(mPeriodSpinner, new LinearLayout.LayoutParams(
         LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
     for (int i = 0; i < CategoryDbTable.KEY_PERIODS.length; i++) {
       mPeriodSpinner
           .addSpinnerItem(CategoryDbTable.KEY_PERIODS[i], new Long(i));
     }
     mPeriodSpinner.setOnItemSelectedListener(mPeriodListener);
     setHelpDialog(R.id.category_edit_agg_view, DIALOG_HELP_AGGREGATE);
 
     mOk = (Button) findViewById(R.id.category_edit_ok);
     mOk.setOnClickListener(mOkListener);
 
     mDelete = (Button) findViewById(R.id.category_edit_delete);
     mDelete.setOnClickListener(mDeleteListener);
     if (mRowId == null) {
       mDelete.setVisibility(View.INVISIBLE);
     }
 
     mTypeRadioGroup = (RadioGroup) findViewById(R.id.category_edit_type);
     mTypeRadioGroup.setOnCheckedChangeListener(mTypeListener);
     mTypeRadio = (RadioButton) findViewById(mTypeRadioGroup
         .getCheckedRadioButtonId());
     setHelpDialog(R.id.category_edit_type_view, DIALOG_HELP_TYPE);
 
     mSyntheticCheck = (CheckBox) findViewById(R.id.category_edit_synthetic);
     setHelpDialog(R.id.category_edit_synthetic_view, DIALOG_HELP_SYNTHETIC);
     mSyntheticCheck.setOnCheckedChangeListener(mSyntheticListener);
     mSyntheticCheck.setChecked(false);
 
     // synthetic elements:
     mFormulaRow = (TableRow) findViewById(R.id.category_edit_formula_row);
     mFormulaEdit = (Button) findViewById(R.id.category_edit_formula);
     mFormulaEdit.setOnClickListener(mFormulaEditListener);
     setHelpDialog(R.id.category_edit_formula_view, DIALOG_HELP_FORMULA);
 
     // standard elements:
     mDefaultValueRow = (TableRow) findViewById(R.id.category_edit_default_value_row);
     mDefaultValueText = (EditText) findViewById(R.id.category_edit_default_value);
     setHelpDialog(R.id.category_edit_default_value_view,
         DIALOG_HELP_DEFAULT_VALUE);
 
     mIncrementRow = (TableRow) findViewById(R.id.category_edit_increment_row);
     mIncrementText = (EditText) findViewById(R.id.category_edit_increment);
     setHelpDialog(R.id.category_edit_increment_view, DIALOG_HELP_INCREMENT);
 
     mZeroFillRow = (TableRow) findViewById(R.id.category_edit_zerofill_row);
     mZeroFillCheck = (CheckBox) findViewById(R.id.category_edit_zerofill);
     setHelpDialog(R.id.category_edit_zerofill_view, DIALOG_HELP_ZEROFILL);
 
     setSyntheticView(false);
   }
 
   private void setSyntheticView(boolean synthetic) {
     if (synthetic == true) {
       mFormulaRow.setVisibility(View.VISIBLE);
       mDefaultValueRow.setVisibility(View.GONE);
       mIncrementRow.setVisibility(View.GONE);
       mZeroFillRow.setVisibility(View.GONE);
     } else {
       mFormulaRow.setVisibility(View.GONE);
       mDefaultValueRow.setVisibility(View.VISIBLE);
       mIncrementRow.setVisibility(View.VISIBLE);
       mZeroFillRow.setVisibility(View.VISIBLE);
     }
   }
 
   private void setupListeners() {
     mTypeListener = new RadioGroup.OnCheckedChangeListener() {
       public void onCheckedChanged(RadioGroup group, int checkedId) {
         mTypeRadio = (RadioButton) findViewById(checkedId);
         if (mTypeRadio.getText().equals(CategoryDbTable.KEY_TYPE_AVERAGE)) {
           mZeroFillCheck.setChecked(false);
           mZeroFillCheck.setClickable(false);
         } else {
           if (mPeriodMs == 0) {
             mZeroFillCheck.setChecked(false);
             mZeroFillCheck.setClickable(false);
           } else {
             mZeroFillCheck.setClickable(true);
           }
         }
       }
     };
 
     mColorChangeListener = new ColorPickerDialog.OnColorChangedListener() {
       public void colorChanged(int color) {
         mPickerPaint.setColor(color);
         mColorButton.setBackgroundColor(color);
         mColorStr = String.format("#%02x%02x%02x", Color.red(color), Color
             .green(color), Color.blue(color));
       }
     };
 
     mColorButtonListener = new View.OnClickListener() {
       public void onClick(View view) {
         ColorPickerDialog d = new ColorPickerDialog(getCtx(),
             mColorChangeListener, mPickerPaint.getColor());
         d.show();
       }
     };
 
     mInterpolationListener = new Spinner.OnItemSelectedListener() {
       public void onItemSelected(AdapterView parent, View v, int position,
           long id) {
         if (v != null) {
           mInterp = ((TextView) v).getText().toString();
         }
         return;
       }
 
       public void onNothingSelected(AdapterView arg0) {
         return;
       }
     };
 
     mPeriodListener = new Spinner.OnItemSelectedListener() {
       public void onItemSelected(AdapterView parent, View v, int position,
           long id) {
         mPeriod = ((TextView) v).getText().toString();
         mPeriodMs = CategoryDbTable.mapPeriodToMs(mPeriod);
         if (mPeriodMs == 0) {
           mZeroFillCheck.setChecked(false);
           mZeroFillCheck.setClickable(false);
         } else {
           String type = mTypeRadio.getText().toString();
           if (type.equals(CategoryDbTable.KEY_TYPE_AVERAGE)) {
             mZeroFillCheck.setChecked(false);
             mZeroFillCheck.setClickable(false);
           } else {
             mZeroFillCheck.setClickable(true);
           }
         }
         return;
       }
 
       public void onNothingSelected(AdapterView arg0) {
         return;
       }
     };
 
     mSyntheticListener = new CompoundButton.OnCheckedChangeListener() {
       public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         setSyntheticView(isChecked);
       }
     };
 
     mFormulaEditListener = new View.OnClickListener() {
       public void onClick(View view) {
         mSave = true;
         saveState();
         mSave = false;
         Intent i = new Intent(getCtx(), FormulaEditorActivity.class);
         i.putExtra(CATEGORY_ID, mRowId);
         startActivityForResult(i, FORMULA_EDIT);
       }
     };
 
     mOkListener = new View.OnClickListener() {
       public void onClick(View view) {
         mSave = true;
         setResult(RESULT_OK);
         finish();
       }
     };
 
     mDeleteListener = new View.OnClickListener() {
       public void onClick(View view) {
         showDialog(DELETE_DIALOG_ID);
       }
     };
   }
 
   private void updatePaint(String color) {
     int colorInt = Color.parseColor(color);
     mPickerPaint = new Paint();
     mPickerPaint.setAntiAlias(true);
     mPickerPaint.setDither(true);
     mPickerPaint.setColor(colorInt);
     mPickerPaint.setStyle(Paint.Style.FILL);
     mColorButton.setBackgroundColor(colorInt);
   }
 
   private void populateFields() {
     if (mRowId != null) {
       if (mRow == null)
         mRow = getDbh().fetchCategory(mRowId);
 
       mGroupCombo.setText(mRow.getGroupName());
       mCategoryText.setText(mRow.getCategoryName());
       mDefaultValueText.setText(Float.valueOf(mRow.getDefaultValue())
           .toString());
       mIncrementText.setText(Float.valueOf(mRow.getIncrement()).toString());
       mGoalText.setText(Float.valueOf(mRow.getGoal()).toString());
       mColorStr = mRow.getColor();
       mGroupName = mRow.getGroupName();
 
       mZeroFillCheck.setChecked(mRow.getZeroFill());
       mSyntheticCheck.setChecked(mRow.getSynthetic());
 
       mInterp = mRow.getInterpolation();
       mRank = mRow.getRank();
       mPeriodMs = mRow.getPeriodMs();
       mPeriod = CategoryDbTable.mapMsToPeriod(mPeriodMs);
       mPeriodSpinner.setSelection(CategoryDbTable.mapMsToIndex(mPeriodMs));
 
       ArrayList<TimeSeriesInterpolator> interpolators = ((EvenTrendActivity) getCtx())
           .getInterpolators();
       for (int i = 0; i < interpolators.size(); i++) {
         String name = interpolators.get(i).getName();
         if (mInterp != null && mInterp.equals(name)) {
           mInterpSpinner.setSelection(i);
         }
       }
 
       String type = mRow.getType();
       if (type.equals(CategoryDbTable.KEY_TYPE_AVERAGE)) {
         mTypeRadio = (RadioButton) findViewById(R.id.category_edit_type_rating);
         mZeroFillCheck.setChecked(false);
         mZeroFillCheck.setClickable(false);
       } else {
         mTypeRadio = (RadioButton) findViewById(R.id.category_edit_type_sum);
         mZeroFillCheck.setClickable(true);
       }
       mTypeRadio.setChecked(true);
     } else {
       mGroupName = "";
       mColorStr = "#cccccc";
       mRank = mMaxRank;
       mZeroFillCheck.setClickable(true);
     }
   }
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
     if (mRowId != null)
       outState.putLong(CategoryDbTable.KEY_ROWID, mRowId);
     else
       outState.putLong(CategoryDbTable.KEY_ROWID, -1);
     super.onSaveInstanceState(outState);
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     saveState();
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     populateFields();
   }
 
   private void saveState() {
     if (mSave == true) {
       if (mRow == null)
         mRow = new CategoryDbTable.Row();
 
       mRow.setGroupName(mGroupCombo.getText().toString());
       mRow.setCategoryName(mCategoryText.getText().toString());
       mRow.setDefaultValue(Float
           .valueOf(mDefaultValueText.getText().toString()).floatValue());
       mRow.setIncrement(Float.valueOf(mIncrementText.getText().toString())
           .floatValue());
       mRow.setGoal(Float.valueOf(mGoalText.getText().toString()).floatValue());
       mRow.setColor(mColorStr);
       mRow.setPeriodMs(mPeriodMs);
       mRow.setInterpolation(mInterp);
       mRow.setZeroFill(mZeroFillCheck.isChecked());
       mRow.setSynthetic(mSyntheticCheck.isChecked());
 
       if (null == mTypeRadio) {
         mTypeRadio = (RadioButton) findViewById(mTypeRadioGroup
             .getCheckedRadioButtonId());
       }
       mRow.setType(mTypeRadio.getText().toString());
 
       if (mRowId == null) {
         mRow.setRank(mMaxRank);
         long catId = getDbh().createCategory(mRow);
         if (catId > 0) {
           mRowId = catId;
         }
       } else {
         mRow.setId(mRowId);
         mRow.setRank(mRank);
         getDbh().updateCategory(mRow);
       }
     }
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     super.onActivityResult(requestCode, resultCode, intent);
     String formula = null;
     if (intent != null)
       formula = intent.getStringExtra(FORMULA);
     if (formula != null)
       mFormula.setFormula(formula);
 
     if (mRowId > 0)
       mRow = getDbh().fetchCategory(mRowId);
     populateFields();
   }
 
   @Override
   protected Dialog onCreateDialog(int id) {
     String title;
     String msg;
     switch (id) {
       case DELETE_DIALOG_ID:
         title = "Delete " + mCategoryText.getText().toString() + "?";
         msg = "All associated entries will also be deleted!";
         return dialog(title, msg);
       case DIALOG_HELP_GROUP:
         title = getResources().getString(R.string.cat_group_title);
         msg = getResources().getString(R.string.cat_group_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_CATEGORY:
         title = getResources().getString(R.string.cat_category_title);
         msg = getResources().getString(R.string.cat_category_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_DEFAULT_VALUE:
         title = getResources().getString(R.string.cat_default_value_title);
         msg = getResources().getString(R.string.cat_default_value_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_INCREMENT:
         title = getResources().getString(R.string.cat_increment_title);
         msg = getResources().getString(R.string.cat_increment_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_GOAL:
         title = getResources().getString(R.string.cat_goal_title);
         msg = getResources().getString(R.string.cat_goal_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_COLOR:
         title = getResources().getString(R.string.cat_color_title);
         msg = getResources().getString(R.string.cat_color_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_TYPE:
         title = getResources().getString(R.string.cat_type_title);
         msg = getResources().getString(R.string.cat_type_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_AGGREGATE:
         title = getResources().getString(R.string.cat_aggregate_title);
         msg = getResources().getString(R.string.cat_aggregate_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_INTERP:
         title = getResources().getString(R.string.cat_interp_title);
         msg = getResources().getString(R.string.cat_interp_desc);
 
         ArrayList<TimeSeriesInterpolator> interpolators = ((EvenTrendActivity) getCtx())
             .getInterpolators();
         for (int i = 0; i < interpolators.size(); i++) {
           int helpId = interpolators.get(i).getHelpResId();
           msg += "\n---\n" + getResources().getString(helpId);
         }
 
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_ZEROFILL:
         title = getResources().getString(R.string.cat_zerofill_title);
         msg = getResources().getString(R.string.cat_zerofill_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_SYNTHETIC:
         title = getResources().getString(R.string.cat_synthetic_title);
         msg = getResources().getString(R.string.cat_synthetic_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
       case DIALOG_HELP_FORMULA:
         title = getResources().getString(R.string.cat_formula_title);
         msg = getResources().getString(R.string.cat_formula_desc);
         return getDialogUtil().newOkDialog(title, msg + "\n");
     }
     return null;
   }
 
   private Dialog dialog(String title, String msg) {
     Builder b = new AlertDialog.Builder(getCtx());
     b.setTitle(title);
     b.setMessage(msg);
     b.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
         getDbh().deleteCategory(mRowId);
         getDbh().deleteCategoryEntries(mRowId);
        setResult(RESULT_OK);
         finish();
       }
     });
     b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
       }
     });
     Dialog d = b.create();
     return d;
   }
 
   private void setHelpDialog(int resId, final int dialog) {
     TextView tv = (TextView) findViewById(resId);
     tv.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         showDialog(dialog);
       }
     });
   }
 }
