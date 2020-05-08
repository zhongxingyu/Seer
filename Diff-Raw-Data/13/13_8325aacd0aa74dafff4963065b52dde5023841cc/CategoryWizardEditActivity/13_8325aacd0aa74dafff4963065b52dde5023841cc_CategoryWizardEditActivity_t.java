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
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.TranslateAnimation;
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
 import android.widget.ViewFlipper;
 
 import net.redgeek.android.eventrecorder.TimeSeriesData;
 import net.redgeek.android.eventrecorder.TimeSeriesProvider;
 import net.redgeek.android.eventrecorder.TimeSeriesData.TimeSeries;
 import net.redgeek.android.eventrend.EvenTrendActivity;
 import net.redgeek.android.eventrend.R;
 import net.redgeek.android.eventrend.util.ColorPickerDialog;
 import net.redgeek.android.eventrend.util.ComboBox;
 import net.redgeek.android.eventrend.util.DynamicSpinner;
 import net.redgeek.android.eventrend.util.Number;
 
 import java.util.ArrayList;
 
 public class CategoryWizardEditActivity extends EvenTrendActivity {
   public static final int CATEGORY_CANCELED = RESULT_FIRST_USER + 1;
   public static final int CATEGORY_CREATED  = RESULT_FIRST_USER + 2;
   public static final int CATEGORY_MODIFIED = RESULT_FIRST_USER + 3;
   public static final int CATEGORY_DELETED  = RESULT_FIRST_USER + 4;
   public static final int CATEGORY_OP_ERR   = RESULT_FIRST_USER + 10;
   
   static final int DELETE_DIALOG_ID = 0;
   static final int DIALOG_HELP_GROUP = 1;
   static final int DIALOG_HELP_CATEGORY = 2;
   static final int DIALOG_HELP_GOAL = 3;
   static final int DIALOG_HELP_COLOR = 4;
   static final int DIALOG_HELP_AGGREGATE = 5;
   static final int DIALOG_HELP_AGGREGATE_PERIOD = 6;
   static final int DIALOG_HELP_UNITS = 8;
   // synthetic config:
   static final int DIALOG_HELP_SYNTHETIC = 9;
   static final int DIALOG_HELP_FORMULA = 10;
   // standard config:
   static final int DIALOG_HELP_DEFAULT_VALUE = 11;
   static final int DIALOG_HELP_INCREMENT = 12;
   static final int DIALOG_HELP_SERIES_TYPE = 13;
   static final int DIALOG_HELP_ZEROFILL = 14;
   // previously preferences:
   static final int DIALOG_HELP_HISTORY = 15;
   static final int DIALOG_HELP_DECIMALS = 16;
   static final int DIALOG_HELP_SMOOTHING = 17;
   static final int DIALOG_HELP_SENSITIVITY = 18;
 
   // UI elements
   private ViewFlipper    mFlipper;
   private LayoutInflater mInflater;
 
   // Page 1:  name + group editing:
   private EditText mCategoryText;
   private ComboBox mGroupCombo;
   private Button   mCancelPage1;
   private Button   mNextPage1;
   
   // Page 2:  category type:
   private DynamicSpinner mSeriesTypeSpinner;
   private Button   mCancelPage2;
   private Button   mBackPage2;
   private Button   mNextPage2;
 
   // Page 3a: discrete settings:
   private EditText mDefaultValueText;
   private EditText mIncrementText;
   private Button   mCancelPage3a;
   private Button   mBackPage3a;
   private Button   mNextPage3a;
 
   // Page 3b: synthetics setting:
   private Button   mFormulaEdit;
   private Button   mCancelPage3b;
   private Button   mBackPage3b;
   private Button   mNextPage3b;
 
   // Page 4:  aggregation
   private RadioGroup mAggRadioGroup;
   private RadioButton mAggRadio;
   private DynamicSpinner mAggregatePeriodSpinner;
   private CheckBox mZeroFillCheck;
   private Button   mCancelPage4;
   private Button   mBackPage4;
   private Button   mNextPage4;
   private TextView mZerofillHelp;
   private LinearLayout mZerofillLayout;
   
   // Page 5:  customization
   private Button mColorButton;
   private EditText mUnitsText;
   private EditText mGoalText;
   private Button   mCancelPage5;
   private Button   mSavePage5;
   private Button   mAdvancedPage5;
 
   // page 6:  trending
   private EditText mHistoryText;
   private EditText mDecimalsText;
   private EditText mSmoothingText;
   private EditText mSensitivityText;
   private Button   mCancelPage6;
   private Button   mSavePage6;
 
   // Private data
   private CategoryRow mRow;
   private String mGroupName;
  private int mPeriodSeconds = 0;
 
   private Paint mPickerPaint;
   private String mColorStr;
   private Long mRowId;
   private int mMaxRank;
   private boolean mSave = false;
   private String mType;
   
   // Listeners
   private RadioGroup.OnCheckedChangeListener mAggListener;
   private ColorPickerDialog.OnColorChangedListener mColorChangeListener;
   private View.OnClickListener mColorButtonListener;
   private Spinner.OnItemSelectedListener mAggregatePeriodListener;
   private CompoundButton.OnCheckedChangeListener mAdvancedListener;
   private Spinner.OnItemSelectedListener mSeriesTypeListener;
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
     Intent it = getIntent();
     // prefer uris ...
     if (it != null) {
       Uri uri = it.getData();
       if (uri != null) {
         try {
           String rowIdStr = uri.getPathSegments().get(TimeSeriesProvider.PATH_SEGMENT_TIMERSERIES_ID);
           mRowId = Long.valueOf(rowIdStr);
         } catch (Exception e) { } // nothing
       }
     }
 
     // try the icicle next ...
     if (mRowId == null || mRowId < 1) {
       if (icicle != null) {
         mRowId = icicle.getLong(TimeSeriesData.TimeSeries._ID);
         if (mRowId < 0)
           mRowId = null;
       }
       // lastly, fall back on the icicle ...
       if (mRowId == null) {
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
           mRowId = extras.getLong(TimeSeriesData.TimeSeries._ID);
         }
       }
     }
 
     Uri timeseries = TimeSeriesData.TimeSeries.CONTENT_URI;
     Cursor c = getContentResolver().query(timeseries, null, null, null, null);
     mMaxRank = 0;
     if (c != null) {
       int count = c.getCount();
       c.moveToFirst();
       for (int i = 0; i < count; i++) {
         int rank = TimeSeries.getRank(c);
         if (rank > mMaxRank)
           mMaxRank = rank;
         if (mRowId != null && TimeSeries.getId(c) == mRowId)
           mRow = new CategoryRow(c);
 
         c.moveToNext();
       }
       c.close();
     }
 
     if (mRow == null)
       mRow = new CategoryRow();
   }
   
   public static class RestrictedNameFilter implements InputFilter {
     private String mInvalidRegex;
 
     public RestrictedNameFilter(String invalidRegex) {
       mInvalidRegex = invalidRegex;
     }
 
     public CharSequence filter(CharSequence source, int start, int end,
         Spanned dest, int dstart, int dend) {
       String out = "" + source;
       out = out.replaceAll(mInvalidRegex, "");
       return out.subSequence(0, out.length());
     }
   }
 
   private void setupUI() {
     setContentView(R.layout.timeseries_edit_root);
 
     mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     mFlipper = (ViewFlipper) findViewById(R.id.wizard_flipper);
     mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
     mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
 
     setuiUIPage1();
     setuiUIPage2();
     setuiUIPage3a();
     setuiUIPage3b();
     setuiUIPage4();
     setuiUIPage5();
     setuiUIPage6();
   }
   
   private void setuiUIPage1() {
     LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.timeseries_edit_name, null);
     mFlipper.addView(page);
 
     // Category name:
     mCategoryText = (EditText) page.findViewById(R.id.category_edit_name);
     InputFilter[] FilterArray = new InputFilter[1];
     FilterArray[0] = new RestrictedNameFilter("[\"\\\\]");
     mCategoryText.setFilters(FilterArray);
     setHelpDialog(R.id.category_edit_name_view, DIALOG_HELP_CATEGORY);
 
     // Group name:
     mGroupCombo = (ComboBox) page.findViewById(R.id.category_edit_group);
     ArrayList<String> groups = fetchAllGroups();
     int size = groups.size();
     String group;
     for (int i = 0; i < size; i++) {
       group = groups.get(i);
       mGroupCombo.addMenuItem(group);
       if (mGroupName != null && mGroupName.equals(group))
         mGroupCombo.setSelection(i);
     }
     setHelpDialog(R.id.category_edit_group_view, DIALOG_HELP_GROUP);
 
     // Cancel and next:
     mCancelPage1 = (Button) page.findViewById(R.id.category_edit_cancel);
     mCancelPage1.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         mSave = false;
         setResult(CATEGORY_CANCELED);
         finish();
       }
     });
     
     mNextPage1 = (Button) page.findViewById(R.id.category_edit_next);
     mNextPage1.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideLeft();
       }
     });
   }
 
   private void setuiUIPage2() {
     LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.timeseries_edit_type, null);
     mFlipper.addView(page);
 
     // Category type (discrete, range, type):
     mSeriesTypeSpinner = (DynamicSpinner) page.findViewById(R.id.category_edit_series_type_menu);
     for (int i = 0; i < TimeSeries.TYPES.length; i++) {
       mSeriesTypeSpinner.addSpinnerItem(TimeSeries.TYPES[i], new Long(i));
       if (mRow.mType != null && mRow.mType.equals(TimeSeries.TYPES[i])) {
         mSeriesTypeSpinner.setSelection(i);
         mType = mRow.mType;
       }
     }
     mSeriesTypeListener = new Spinner.OnItemSelectedListener() {
       public void onItemSelected(AdapterView parent, View v, int position,
           long id) {
         mType = ((TextView) v).getText().toString();
         return;
       }
 
       public void onNothingSelected(AdapterView arg0) {
         return;
       }
     };
     mSeriesTypeSpinner.setOnItemSelectedListener(mSeriesTypeListener);
     mSeriesTypeSpinner.setSelection(0);
     setHelpDialog(R.id.category_edit_type_view, DIALOG_HELP_SERIES_TYPE);
 
     // Cancel and next:
     mCancelPage2 = (Button) page.findViewById(R.id.category_edit_cancel);
     mCancelPage2.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         mSave = false;
         setResult(CATEGORY_CANCELED);
         finish();
       }
     });
     
     mBackPage2 = (Button) page.findViewById(R.id.category_edit_back);
     mBackPage2.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideRight();
       }
     });
 
     mNextPage2 = (Button) page.findViewById(R.id.category_edit_next);
     mNextPage2.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         if (mType.toLowerCase().equals(TimeSeries.TYPE_DISCRETE)) {
           slideLeft();
         }
         else if (mType.toLowerCase().equals(TimeSeries.TYPE_SYNTHETIC)) {
           slideLeft();
           slideLeft();
         }
         else if (mType.toLowerCase().equals(TimeSeries.TYPE_RANGE)) {
           slideLeft();
           slideLeft();
           slideLeft();
         }        
       }
     });
   }
 
   private void setuiUIPage3a() {
     LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.timeseries_edit_discrete, null);
     mFlipper.addView(page);
 
     // Default value:
     mDefaultValueText = (EditText) page.findViewById(R.id.category_edit_default_value);
     setHelpDialog(R.id.category_edit_default_value_view,
         DIALOG_HELP_DEFAULT_VALUE);
 
     // Increment/decrement:
     mIncrementText = (EditText) page.findViewById(R.id.category_edit_increment);
     setHelpDialog(R.id.category_edit_increment_view, DIALOG_HELP_INCREMENT);
 
     // Cancel and next:
     mCancelPage3a = (Button) page.findViewById(R.id.category_edit_cancel);
     mCancelPage3a.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         mSave = false;
         setResult(CATEGORY_CANCELED);
         finish();
       }
     });
     
     mBackPage3a = (Button) page.findViewById(R.id.category_edit_back);
     mBackPage3a.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideRight();
       }
     });
 
     mNextPage3a = (Button) page.findViewById(R.id.category_edit_next);
     mNextPage3a.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideLeft();
         slideLeft();
       }
     });
   }
 
   private void setuiUIPage3b() {
     LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.timeseries_edit_formula, null);
     mFlipper.addView(page);
 
     // Formula:
     mFormulaEdit = (Button) page.findViewById(R.id.category_edit_formula);
     mFormulaEditListener = new View.OnClickListener() {
       public void onClick(View view) {
         // TODO:  make this not have to save the category first:
         mSave = true;
         saveState();
         mSave = false;
         Intent i = new Intent(mCtx, FormulaEditorActivity.class);
         i.putExtra(TimeSeries._ID, mRowId);
         startActivityForResult(i, ARC_FORMULA_EDIT);
       }
     };
     mFormulaEdit.setOnClickListener(mFormulaEditListener);
     setHelpDialog(R.id.category_edit_formula_view, DIALOG_HELP_FORMULA);
 
     // Cancel and next:
     mCancelPage3b = (Button) page.findViewById(R.id.category_edit_cancel);
     mCancelPage3b.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         mSave = false;
         setResult(CATEGORY_CANCELED);
         finish();
       }
     });
     
     mBackPage3b = (Button) page.findViewById(R.id.category_edit_back);
     mBackPage3b.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideRight();
         slideRight();
       }
     });
 
     mNextPage3b = (Button) page.findViewById(R.id.category_edit_next);
     mNextPage3b.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideLeft();
       }
     });
   }
 
   private void setuiUIPage4() {
     LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.timeseries_edit_aggregation, null);
     mFlipper.addView(page);
 
     // zerofill:
     mZerofillHelp = (TextView) page.findViewById(R.id.category_edit_zerofill_row_help);
     mZerofillLayout = (LinearLayout) page.findViewById(R.id.category_edit_zerofill_row);
     mZeroFillCheck = (CheckBox) findViewById(R.id.category_edit_zerofill);
     setHelpDialog(R.id.category_edit_zerofill_view, DIALOG_HELP_ZEROFILL);
     mZerofillHelp.setVisibility(View.GONE);
     mZerofillLayout.setVisibility(View.GONE);
     
     // aggregation period:
     mAggregatePeriodSpinner = (DynamicSpinner) page.findViewById(R.id.category_edit_agg_period_menu);
     for (int i = 0; i < TimeSeries.AGGREGATION_PERIOD_NAMES.length; i++) {
       mAggregatePeriodSpinner.addSpinnerItem(
           TimeSeries.AGGREGATION_PERIOD_NAMES[i], new Long(
               TimeSeries.AGGREGATION_PERIOD_TIMES[i]));
     }
     mAggregatePeriodListener = new Spinner.OnItemSelectedListener() {
       public void onItemSelected(AdapterView parent, View v, int position,
           long id) {
         mPeriodSeconds = (int) mAggregatePeriodSpinner.getMappingFromPosition(position);
        if (mPeriodSeconds == 0
            || mType.toLowerCase().equals(TimeSeries.TYPE_SYNTHETIC)
            || mAggRadio.getText().toString().toLowerCase().equals(
                TimeSeries.AGGREGATION_AVG)) {
          mZerofillHelp.setVisibility(View.GONE);
          mZerofillLayout.setVisibility(View.GONE);
        } else {
          mZerofillHelp.setVisibility(View.VISIBLE);
          mZerofillLayout.setVisibility(View.VISIBLE);
        }
         return;
       }
 
       public void onNothingSelected(AdapterView arg0) {
         return;
       }
     };
     mAggregatePeriodSpinner.setOnItemSelectedListener(mAggregatePeriodListener);
     mAggregatePeriodSpinner.setSelection(0);
     setHelpDialog(R.id.category_edit_agg_view, DIALOG_HELP_AGGREGATE_PERIOD);
 
     // aggregation type: sum or average:
     mAggRadioGroup = (RadioGroup) page.findViewById(R.id.category_edit_agg);
     mAggRadio = (RadioButton) findViewById(mAggRadioGroup
         .getCheckedRadioButtonId());
     setHelpDialog(R.id.category_edit_agg_view, DIALOG_HELP_AGGREGATE);
     mAggListener = new RadioGroup.OnCheckedChangeListener() {
       public void onCheckedChanged(RadioGroup group, int checkedId) {
         mAggRadio = (RadioButton) findViewById(checkedId);
         if (mAggRadio == null || mType == null)
           return;
 
         if (mPeriodSeconds == 0
             || mType.toLowerCase().equals(TimeSeries.TYPE_SYNTHETIC)
             || mAggRadio.getText().toString().toLowerCase().equals(
                 TimeSeries.AGGREGATION_AVG)) {
           mZerofillHelp.setVisibility(View.GONE);
           mZerofillLayout.setVisibility(View.GONE);
         } else {
           mZerofillHelp.setVisibility(View.VISIBLE);
           mZerofillLayout.setVisibility(View.VISIBLE);
         }
       }
     };
     mAggRadioGroup.setOnCheckedChangeListener(mAggListener);
 
     // Cancel and next:
     mCancelPage4 = (Button) page.findViewById(R.id.category_edit_cancel);
     mCancelPage4.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         mSave = false;
         setResult(CATEGORY_CANCELED);
         finish();
       }
     });
     
     mBackPage4 = (Button) page.findViewById(R.id.category_edit_back);
     mBackPage4.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         if (mType.toLowerCase().equals(TimeSeries.TYPE_DISCRETE)) {
           slideRight();
           slideRight();
         }
         else if (mType.toLowerCase().equals(TimeSeries.TYPE_SYNTHETIC)) {
           slideRight();
         }
         else if (mType.toLowerCase().equals(TimeSeries.TYPE_RANGE)) {
           slideRight();
           slideRight();
           slideRight();
         }        
       }
     });
 
     mNextPage4 = (Button) page.findViewById(R.id.category_edit_next);
     mNextPage4.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideLeft();
       }
     });
   }
 
   private void setuiUIPage5() {
     LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.timeseries_edit_customize, null);
     mFlipper.addView(page);
 
     // Color:
     mColorButton = (Button) findViewById(R.id.category_edit_color);
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
         ColorPickerDialog d = new ColorPickerDialog(mCtx, mColorChangeListener,
             mPickerPaint.getColor());
         d.show();
       }
     };
     mColorButton.setOnClickListener(mColorButtonListener);
     setHelpDialog(R.id.category_edit_color_view, DIALOG_HELP_COLOR);
 
     // Goal:
     mGoalText = (EditText) findViewById(R.id.category_edit_goal);
     setHelpDialog(R.id.category_edit_goal_view, DIALOG_HELP_GOAL);
 
     // Units:
     mUnitsText = (EditText) findViewById(R.id.category_edit_units);
     setHelpDialog(R.id.category_edit_units_view, DIALOG_HELP_UNITS);
 
     // Cancel and next:
     mCancelPage5 = (Button) page.findViewById(R.id.category_edit_cancel);
     mCancelPage5.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         mSave = false;
         setResult(CATEGORY_CANCELED);
         finish();
       }
     });
     
     mSavePage5 = (Button) page.findViewById(R.id.category_edit_save);
     mSavePage5.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
         mSave = true;
         setResult(CATEGORY_CREATED);
         finish();
       }
     });
 
     mAdvancedPage5 = (Button) page.findViewById(R.id.category_edit_next);
     mAdvancedPage5.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         slideLeft();
       }
     });
   }
 
   private void setuiUIPage6() {
     LinearLayout page = (LinearLayout) mInflater.inflate(R.layout.timeseries_edit_trending, null);
     mFlipper.addView(page);
 
     // History:
     mHistoryText = (EditText) findViewById(R.id.category_edit_history);
     setHelpDialog(R.id.category_edit_history_view, DIALOG_HELP_HISTORY);
 
     // Decimal Places:
     mDecimalsText = (EditText) findViewById(R.id.category_edit_decimals);
     setHelpDialog(R.id.category_edit_decimals_view, DIALOG_HELP_DECIMALS);
 
     // Smoothing:
     mSmoothingText = (EditText) findViewById(R.id.category_edit_smoothing);
     setHelpDialog(R.id.category_edit_smoothing_view, DIALOG_HELP_SMOOTHING);
 
     // Sensitivity:
     mSensitivityText = (EditText) findViewById(R.id.category_edit_sensitivity);
     setHelpDialog(R.id.category_edit_sensitivity_view, DIALOG_HELP_SENSITIVITY);
     
     // Cancel and next:
     mCancelPage6 = (Button) page.findViewById(R.id.category_edit_cancel);
     mCancelPage6.setOnClickListener(new View.OnClickListener() {
       public void onClick(View view) {
         mSave = false;
         setResult(CATEGORY_CANCELED);
         finish();
       }
     });
     
     mSavePage6 = (Button) page.findViewById(R.id.category_edit_save);
     mSavePage6.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
         mSave = true;
         setResult(CATEGORY_CREATED);
         finish();
       }
     });
   }
   
   private ArrayList<String> fetchAllGroups() {
     ArrayList<String> groups = new ArrayList<String>();
     String[] projection = new String[] { TimeSeries.GROUP_NAME };
     Uri timeseries = TimeSeriesData.TimeSeries.CONTENT_URI;
     Cursor c = getContentResolver().query(timeseries, projection, null, null,
         TimeSeries.GROUP_NAME + " asc ");
     if (c != null) {
       String group;
       int count = c.getCount();
       c.moveToFirst();
       for (int i = 0; i < count; i++) {
         group = TimeSeries.getGroupName(c);
         if (groups.contains(group) == false) {
           groups.add(group);
         }
         c.moveToNext();
       }
       c.close();
     }
     return groups;
   }
 
   private void updatePaint(String color) {
     int colorInt = Color.LTGRAY;
     try {
       colorInt = Color.parseColor(color);
     } catch (Exception e) {
     }
     mPickerPaint = new Paint();
     mPickerPaint.setAntiAlias(true);
     mPickerPaint.setDither(true);
     mPickerPaint.setColor(colorInt);
     mPickerPaint.setStyle(Paint.Style.FILL);
     mColorButton.setBackgroundColor(colorInt);
   }
 
   private void populateFields() {
     if (mRowId != null && mRow != null) {
       mCategoryText.setText(mRow.mTimeSeriesName);
       mDefaultValueText.setText(Double.valueOf(mRow.mDefaultValue).toString());
       mIncrementText.setText(Double.valueOf(mRow.mIncrement).toString());
       mGoalText.setText(Double.valueOf(mRow.mGoal).toString());
       mSmoothingText.setText(Double.valueOf(mRow.mSmoothing).toString());
       mSensitivityText.setText(Double.valueOf(mRow.mSensitivity).toString());
       mHistoryText.setText(Integer.valueOf(mRow.mHistory).toString());
       mDecimalsText.setText(Integer.valueOf(mRow.mDecimals).toString());
       mUnitsText.setText(mRow.mUnits);
 
       mColorStr = mRow.mColor;
       mGroupName = mRow.mGroup;
       mGroupCombo.setText(mGroupName);
 
       mZeroFillCheck.setChecked(mRow.mZerofill > 0 ? true : false);
       
       mPeriodSeconds = mRow.mPeriod;
       int index = TimeSeries.periodToIndex(mPeriodSeconds);
       if (index < 0)
         index = 0;
       mAggregatePeriodSpinner.setSelection(index);
 
       for (int i = 0; i < TimeSeries.TYPES.length; i++) {
         if (mRow.mType.toLowerCase().equals(TimeSeries.TYPES[i])) {
           mSeriesTypeSpinner.setSelection(i);
           break;
         }
       }
         
       String aggregation = mRow.mAggregation;
       if (aggregation.toLowerCase().equals(TimeSeries.AGGREGATION_AVG)) {
         mAggRadio = (RadioButton) findViewById(R.id.category_edit_agg_sum);
       } else {
         mAggRadio = (RadioButton) findViewById(R.id.category_edit_agg_sum);
       }
       mAggRadio.setChecked(true);
     } else {
       mGroupName = "Default";
       mGroupCombo.setText(mGroupName);
       mColorStr = "#cccccc";
     }
   }
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
     if (mRowId != null)
       outState.putLong(TimeSeries._ID, mRowId);
     else
       outState.putLong(TimeSeries._ID, -1);
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
     // TODO: check to see if we need this
     // populateFields();
   }
 
   private void saveState() {
     if (mSave == true) {
       double d;
       String value;
       ContentValues values = new ContentValues();
 
       mRow.mDecimals = Integer.valueOf(mDecimalsText.getText().toString()).intValue();
       
       values.put(TimeSeries.TIMESERIES_NAME, mCategoryText.getText().toString());
       values.put(TimeSeries.GROUP_NAME, mGroupCombo.getText().toString());
       d = Number.Round(Double.valueOf(mGoalText.getText().toString()).doubleValue(), mRow.mDecimals);
       values.put(TimeSeries.GOAL, d);
       values.put(TimeSeries.COLOR, mColorStr);
       values.put(TimeSeries.PERIOD, mPeriodSeconds);
       values.put(TimeSeries.UNITS, mUnitsText.getText().toString());
       values.put(TimeSeries.ZEROFILL, mZeroFillCheck.isChecked() ? 1 : 0);
       d = Number.Round(Double.valueOf(mSensitivityText.getText().toString()).doubleValue(), mRow.mDecimals * 2);
       values.put(TimeSeries.SENSITIVITY, d);
       d = Number.Round(Double.valueOf(mSmoothingText.getText().toString()).doubleValue(), mRow.mDecimals * 2);
       values.put(TimeSeries.SMOOTHING, d);
       values.put(TimeSeries.HISTORY, Integer.valueOf(mHistoryText.getText().toString()).intValue());
       values.put(TimeSeries.DECIMALS, Integer.valueOf(mDecimalsText.getText().toString()).intValue());
       values.put(TimeSeries.TYPE, mSeriesTypeSpinner.getSelectedItem().toString());
       values.put(TimeSeries.INTERPOLATION, "");
       // TODO:  formula-related stuff
       if (mRow.mFormula == null || TextUtils.isEmpty(mRow.mFormula))
         values.put(TimeSeries.FORMULA, "");
       else
         values.put(TimeSeries.FORMULA, mRow.mFormula);
       
       mAggRadio = (RadioButton) findViewById(mAggRadioGroup.getCheckedRadioButtonId());
       values.put(TimeSeries.AGGREGATION, mAggRadio.getText().toString().toLowerCase());
 
       if (mSeriesTypeSpinner.getSelectedItem().toString().toLowerCase().equals(TimeSeries.TYPE_SYNTHETIC)) {
         values.put(TimeSeries.DEFAULT_VALUE, 0.0f);
         values.put(TimeSeries.INCREMENT, 1.0f);
       } else {
         d = Number.Round(Double.valueOf(mDefaultValueText.getText().toString()).doubleValue(), mRow.mDecimals);
         values.put(TimeSeries.DEFAULT_VALUE, d);
         d = Number.Round(Double.valueOf(mIncrementText.getText().toString()).doubleValue(), mRow.mDecimals);
         values.put(TimeSeries.INCREMENT, d);
       }
 
       if (mRowId == null) {
         // insert
         values.put(TimeSeries.RECORDING_DATAPOINT_ID, 0);
         values.put(TimeSeries.RANK, mMaxRank + 1);
         Uri uri = getContentResolver().insert(TimeSeriesData.TimeSeries.CONTENT_URI, values);
         if (uri != null) {
           String rowIdStr = uri.getPathSegments().get(TimeSeriesProvider.PATH_SEGMENT_TIMERSERIES_ID);
           mRowId = Long.valueOf(rowIdStr);
           setResult(CATEGORY_CREATED);
         } else {
           setResult(CATEGORY_OP_ERR);
         }
       } else {
         // update
         values.put(TimeSeries.RANK, mRow.mRank);
         Uri uri = ContentUris.withAppendedId(TimeSeriesData.TimeSeries.CONTENT_URI, mRowId);
         getContentResolver().update(uri, values, null, null);
         setResult(CATEGORY_MODIFIED);
       }
     }
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     super.onActivityResult(requestCode, resultCode, intent);
     String formula = null;
     // TODO: formula stuff
     if (intent != null) {
       mRow.mFormula = intent.getStringExtra(TimeSeries.FORMULA);
     }
     if (mRow != null && mRow.mFormula == null && mRowId > 0) {
       String[] projection = new String[] { TimeSeries.FORMULA };
       Uri timeseries = ContentUris.withAppendedId(TimeSeries.CONTENT_URI, mRow.mId);
       Cursor c = getContentResolver().query(timeseries, projection, null, null, null);
       if (c.getCount() < 1) {
         c.close();
         return;
       }
 
       c.moveToFirst();
       mRow.mFormula = TimeSeries.getFormula(c);
       c.close();
     }
 //    populateFields();
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
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_CATEGORY:
         title = getResources().getString(R.string.cat_category_title);
         msg = getResources().getString(R.string.cat_category_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_DEFAULT_VALUE:
         title = getResources().getString(R.string.cat_default_value_title);
         msg = getResources().getString(R.string.cat_default_value_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_INCREMENT:
         title = getResources().getString(R.string.cat_increment_title);
         msg = getResources().getString(R.string.cat_increment_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_GOAL:
         title = getResources().getString(R.string.cat_goal_title);
         msg = getResources().getString(R.string.cat_goal_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_COLOR:
         title = getResources().getString(R.string.cat_color_title);
         msg = getResources().getString(R.string.cat_color_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_AGGREGATE:
         title = getResources().getString(R.string.cat_aggregate_title);
         msg = getResources().getString(R.string.cat_aggregate_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_AGGREGATE_PERIOD:
         title = getResources().getString(R.string.cat_aggregate_period_title);
         msg = getResources().getString(R.string.cat_aggregate_period_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_ZEROFILL:
         title = getResources().getString(R.string.cat_zerofill_title);
         msg = getResources().getString(R.string.cat_zerofill_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_SERIES_TYPE:
         title = getResources().getString(R.string.cat_series_type_title);
         msg = getResources().getString(R.string.cat_series_type_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_FORMULA:
         title = getResources().getString(R.string.cat_formula_title);
         msg = getResources().getString(R.string.cat_formula_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_UNITS:
         title = getResources().getString(R.string.cat_units_title);
         msg = getResources().getString(R.string.cat_units_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_HISTORY:
         title = getResources().getString(R.string.cat_history_title);
         msg = getResources().getString(R.string.cat_history_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_DECIMALS:
         title = getResources().getString(R.string.cat_decimals_title);
         msg = getResources().getString(R.string.cat_decimals_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_SMOOTHING:
         title = getResources().getString(R.string.cat_smoothing_title);
         msg = getResources().getString(R.string.cat_smoothing_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
       case DIALOG_HELP_SENSITIVITY:
         title = getResources().getString(R.string.cat_sensitivity_title);
         msg = getResources().getString(R.string.cat_sensitivity_desc);
         return mDialogUtil.newOkDialog(title, msg + "\n");
     }
     return null;
   }
 
   private Dialog dialog(String title, String msg) {
     Builder b = new AlertDialog.Builder(mCtx);
     b.setTitle(title);
     b.setMessage(msg);
     b.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
         Uri timeseries = ContentUris.withAppendedId(
             TimeSeriesData.TimeSeries.CONTENT_URI, mRow.mId);
         mCtx.getContentResolver().delete(timeseries, null, null);
         setResult(CATEGORY_DELETED);
         finish();
       }
     });
     b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
         setResult(RESULT_CANCELED);
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
   
   protected void slideLeft() {
     Animation slideInLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
         1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
         0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
     Animation slideOutRight = new TranslateAnimation(
         Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
         Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
     slideInLeft.setDuration(500);
     slideOutRight.setDuration(500);
 
     mFlipper.setInAnimation(slideInLeft);
     mFlipper.setOutAnimation(slideOutRight);
 
     mFlipper.showNext();
   }
 
   protected void slideRight() {
     Animation slideOutLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
         0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
         0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
     Animation slideInRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
         -1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
         0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
     slideOutLeft.setDuration(500);
     slideInRight.setDuration(500);
 
     mFlipper.setInAnimation(slideInRight);
     mFlipper.setOutAnimation(slideOutLeft);
     mFlipper.showPrevious();
   }
 }
