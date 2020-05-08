 package ca.projectkarma.gradetrackr.activity;
 
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import ca.projectkarma.gradetrackr.EditMode;
 import ca.projectkarma.gradetrackr.activity.DatePickerDialogFragment.DatePickedListener;
 import ca.projectkarma.gradetrackr.db.adapter.CategoriesDBAdapter;
 import ca.projectkarma.gradetrackr.db.adapter.CoursesDBAdapter;
 import ca.projectkarma.gradetrackr.db.adapter.EvaluationsDBAdapter;
 
 import ca.projectkarma.gradetrackr.R;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddEval extends FragmentActivity implements DatePickedListener {
 
     // database
     CoursesDBAdapter coursesDB = new CoursesDBAdapter(this);
     CategoriesDBAdapter categoriesDB = new CategoriesDBAdapter(this);
     EvaluationsDBAdapter evaluationsDB = new EvaluationsDBAdapter(this);
 
     // context
     Context context = this;
 
     // views
     EditText etEvalName;
     EditText etEvalMark;
     EditText etEvalOutOf;
     Spinner sEvalCat;
     Button bEvalPickDate;
     EditText etWeight;
     EditText etEvalNotes;
     Button bEvalDone;
 
     // date and time
     private int mYear;
     private int mMonth;
     private int mDay;
     static final String STATE_YEAR = "selectedYear";
     static final String STATE_MONTH = "selectedMonth";
     static final String STATE_DAY = "selectedDay";
     static final int DATE_DIALOG_ID = 0;
     NumberFormat twoDigit;
 
     // variables
     final private String TAG = "AddEval";
     int[] refID;
     int selectedRefID; // category reference ID
     String selectedDate;
     int refIDGet_Term;
     int refIDGet_Course;
     int refIDGet_Category;
     int idGet_Mode; // use with EditMode constants
     int idEditGet_Item;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_eval);
 
         etEvalName = (EditText) findViewById(R.id.etEvalName);
         etEvalMark = (EditText) findViewById(R.id.etEvalMark);
         etEvalOutOf = (EditText) findViewById(R.id.etEvalOutOf);
         sEvalCat = (Spinner) findViewById(R.id.sEvalCat);
         bEvalPickDate = (Button) findViewById(R.id.bPickEvalDate);
         etWeight = (EditText) findViewById(R.id.etEvalWeight);
 //        etEvalNotes = (EditText) findViewById(R.id.etEvalNotes);
         bEvalDone = (Button) findViewById(R.id.bEvalDone);
 
         Intent iAddEval = getIntent();
         refIDGet_Term = iAddEval.getIntExtra("refID_Term", -1);
         refIDGet_Course = iAddEval.getIntExtra("refID_Course", -1);
         refIDGet_Category = iAddEval.getIntExtra("refID_Category", -1);
         idGet_Mode = iAddEval.getIntExtra("id_Mode", EditMode.ADD_MODE);
                                                          // edit
         idEditGet_Item = iAddEval.getIntExtra("idEdit_Item", -1);
 
         coursesDB.open();
         Cursor cCourse = coursesDB.getCourse(refIDGet_Course);
         cCourse.moveToFirst();
         if (idGet_Mode == EditMode.ADD_MODE)
         {
             setTitle("Add Evaluation to "
                     + cCourse.getString(cCourse.getColumnIndex("courseTitle")));
         }
         else if (idGet_Mode == EditMode.EDIT_MODE)
         {
             setTitle("Edit Evaluation in "
                     + cCourse.getString(cCourse.getColumnIndex("courseTitle")));
             bEvalDone.setText(R.string.doneEditEval);
         }
         coursesDB.close();
 
         bEvalPickDate.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Bundle b = new Bundle();
                 b.putInt("curYear", mYear);
                 b.putInt("curMonth", mMonth);
                 b.putInt("curDay", mDay);
                 // show the time picker dialog
                 DialogFragment newFragment = new DatePickerDialogFragment();
                 newFragment.setArguments(b);
                 newFragment.show(getSupportFragmentManager(), "datePicker");
             }
         });
 
         evaluationsDB.open();
         // set the date
         Cursor cEvaluation = evaluationsDB.getEvaluation(idEditGet_Item);
         cEvaluation.moveToFirst();
         if (savedInstanceState != null)
         {
             // recreate date from state
             mYear = savedInstanceState.getInt(STATE_YEAR);
             mMonth = savedInstanceState.getInt(STATE_MONTH);
             mDay = savedInstanceState.getInt(STATE_DAY);
         }
         else if (idGet_Mode == EditMode.ADD_MODE)
         {
             // set date to today
             final Calendar cal = Calendar.getInstance();
             mYear = cal.get(Calendar.YEAR);
             mMonth = cal.get(Calendar.MONTH);
             mDay = cal.get(Calendar.DAY_OF_MONTH);
         }
         else if (idGet_Mode == EditMode.EDIT_MODE)
         {
             // set date from database
             SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
             Date date = new Date();
             try
             {
                 date = format.parse(cEvaluation.getString(cEvaluation.getColumnIndex("evalDate")));
             }
             catch (ParseException e)
             {
                 e.printStackTrace();
             }
 
             SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
             SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.US);
             SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
             mYear = Integer.parseInt(yearFormat.format(date));
             mMonth = Integer.parseInt(monthFormat.format(date)) - 1;
             mDay = Integer.parseInt(dayFormat.format(date));
         }
         evaluationsDB.close();
 
         // set format
         twoDigit = NumberFormat.getInstance();
         twoDigit.setMinimumIntegerDigits(2);
         twoDigit.setMaximumIntegerDigits(2);
         twoDigit.setMinimumFractionDigits(0);
         twoDigit.setMaximumFractionDigits(0);
 
         selectedDate = mYear + "-" + twoDigit.format(mMonth + 1) + "-" + twoDigit.format(mDay);
         bEvalPickDate.setText(selectedDate);
 
         List<String> categories = new ArrayList<String>();
 
         // read data from database
         categoriesDB.open();
         Cursor c = categoriesDB.getCategoriesOfCourse(refIDGet_Course);
         int i = 0;
         refID = new int[c.getCount()];
         if (c.moveToFirst())
         {
             do
             {
                 refID[i] = c.getInt(0); // get ids of each.
                 categories.add(c.getString(c.getColumnIndex("catTitle")));
                 i++;
             }
             while (c.moveToNext());
         }
         categoriesDB.close();
 
         ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_spinner_item, categories);
         dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         sEvalCat.setAdapter(dataAdapter);
         // sEvalCat.setSelection(refIDGet_Category);
         sEvalCat.setOnItemSelectedListener(new OnItemSelectedListener() {
 
             public void onItemSelected(AdapterView<?> adapter, View v, int i, long lng) {
                 // String selecteditem =
                 // adapter.getItemAtPosition(i).toString();
                 // Toast.makeText(context, "Selected item: " + selecteditem +
                 // "\n" + "ID of selected item: " + refID[i],
                 // Toast.LENGTH_SHORT).show();
                 selectedRefID = refID[i];
             }
 
             public void onNothingSelected(AdapterView<?> arg0) {
                 // TODO Auto-generated method stub
 
             }
 
         });
 
         if (idGet_Mode == EditMode.EDIT_MODE)
         {
             for (int j = 0; j < c.getCount(); j++)
             {
                 if (cEvaluation.getInt(cEvaluation.getColumnIndex("catRef")) == refID[j])
                 {
                     sEvalCat.setSelection(j);
                 }
             }
 
             // set other data
             etEvalName.setText(cEvaluation.getString(cEvaluation.getColumnIndex("evalTitle")));
             etEvalMark.setText(cEvaluation.getString(cEvaluation.getColumnIndex("evalMark")));
             etEvalOutOf.setText(cEvaluation.getString(cEvaluation.getColumnIndex("evalOutOf")));
             etWeight.setText(cEvaluation.getString(cEvaluation.getColumnIndex("evalWeight")));
             // sEvalCat.setSelection(selectedRefID);
             // etEvalNotes.setText(cEvaluation.getString(cEvaluation.getColumnIndex("evalNotes")));
         }
 
     }
 
     @Override
     protected void onSaveInstanceState(Bundle savedInstanceState) {
         // TODO Auto-generated method stub
 
         // Save the user's current state
         savedInstanceState.putInt(STATE_YEAR, mYear);
         savedInstanceState.putInt(STATE_MONTH, mMonth);
         savedInstanceState.putInt(STATE_DAY, mDay);
 
         super.onSaveInstanceState(savedInstanceState);
     }
 
     public void updateDBCatMark(int selectedCatId) {
         double mark = 0;
         int totalWeight = 0;
 
         evaluationsDB.open();
         categoriesDB.open();
         Cursor c = evaluationsDB.getEvaluationsOfCategory(selectedCatId);
 
         if (c.moveToFirst())
         {
             do
             {
                 totalWeight += c.getInt(c.getColumnIndex("evalWeight"));
             }
             while (c.moveToNext());
         }
 
         // When the user leaves weight blank
         if (totalWeight == 0)
         {
            int count = c.getCount();
             if (c.moveToFirst())
             {
                 do
                 {
                     mark += 100 * c.getDouble(c.getColumnIndex("evalMark"))
                            / c.getDouble(c.getColumnIndex("evalOutOf")) / count;
                 }
                 while (c.moveToNext());
             }
         }
         // Normal mark calculation
         else
         {
             if (c.moveToFirst())
             {
                 do
                 {
                     double weightFraction = c.getDouble(c.getColumnIndex("evalWeight"))
                             / totalWeight;
                     mark += 100 * weightFraction * c.getDouble(c.getColumnIndex("evalMark"))
                             / c.getDouble(c.getColumnIndex("evalOutOf"));
                 }
                 while (c.moveToNext());
             }
         }

         categoriesDB.updateCategoryAverage(selectedCatId, mark);
 
         evaluationsDB.close();
         categoriesDB.close();
     }
 
     public void addEval(View v) {
 
         Log.d(TAG, "Adding or Editing Evaluation.");
 
         if (etEvalName.getText().toString().trim().equals("")
                 || etEvalMark.getText().toString().trim().equals("")
                 || etEvalOutOf.getText().toString().trim().equals(""))
         {
             // TODO check if Mark, OutOf, and Weight are numeric.
             // TODO weight and notes are not required values
 
             new AlertDialog.Builder(this).setMessage("Make sure all fields are entered.")
                     .setPositiveButton("OK", null).show();
 
         }
 
         else if ((Double.parseDouble(etEvalOutOf.getText().toString().trim()) <= 0.0)
                 || ((Double.parseDouble(etEvalMark.getText().toString().trim()) < 0.0)))
         {
             new AlertDialog.Builder(this).setMessage("Make sure all fields are entered correctly.")
                     .setPositiveButton("OK", null).show();
         }
 
         else
         {
             String evalName = etEvalName.getText().toString();
             double evalMark = 0.0;
             double evalOutOf = 0.0;
             double evalWeight = 0.0;
 //            double catAverage = 100.00;
 //            String evalNotes = etEvalNotes.getText().toString();
 
             try
             {
                 evalMark = Double.parseDouble(etEvalMark.getText().toString());
             }
             catch (NumberFormatException nfe)
             {
                 Log.d(TAG, nfe.getMessage());
             }
 
             try
             {
                 evalOutOf = Double.parseDouble(etEvalOutOf.getText().toString());
             }
             catch (NumberFormatException nfe)
             {
                 Log.d(TAG, nfe.getMessage());
             }
 
             try
             {
                 evalWeight = Double.parseDouble(etWeight.getText().toString());
             }
             catch (NumberFormatException nfe)
             {
                 Log.d(TAG, nfe.getMessage());
             }
 
             if (idGet_Mode == EditMode.ADD_MODE)
             {
                 // adding data to database
                 evaluationsDB.open();
                 evaluationsDB.createEvaluation(evalName, evalMark, evalOutOf, evalWeight,
                         selectedDate, refIDGet_Term, refIDGet_Course, selectedRefID);
                 evaluationsDB.close();
                 updateDBCatMark(selectedRefID);
                 Toast toast = Toast.makeText(context, "Evaluation " + evalName
                         + " was added successfully.", Toast.LENGTH_SHORT);
                 try
                 {
                     ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                             .setGravity(Gravity.CENTER_HORIZONTAL);
                 }
                 catch (ClassCastException cce)
                 {
                     Log.d(TAG, cce.getMessage());
                 }
                 toast.show();
                 Log.d(TAG, "Edited Evaluation Successfully.");
                 finish();
             }
             else if (idGet_Mode == EditMode.EDIT_MODE)
             {
                 // editing data in database
                 evaluationsDB.open();
                 evaluationsDB.updateEvaluation(idEditGet_Item, evalName, evalMark, evalOutOf,
                         evalWeight, selectedDate, refIDGet_Term, refIDGet_Course, selectedRefID);
                 evaluationsDB.close();
                 updateDBCatMark(refIDGet_Category);
                 updateDBCatMark(selectedRefID);
                 Toast toast = Toast.makeText(context, "Evaluation " + evalName
                         + " was edited successfully.", Toast.LENGTH_SHORT);
                 try
                 {
                     ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                             .setGravity(Gravity.CENTER_HORIZONTAL);
                 }
                 catch (ClassCastException cce)
                 {
                     Log.d(TAG, cce.getMessage());
                 }
                 toast.show();
                 Log.d(TAG, "Added Evaluation Successfully.");
                 finish();
             }
         }
     }
 
 	@Override
 	public void onDatePicked(Calendar date) {
 		mYear = date.get(Calendar.YEAR);
 		mMonth = date.get(Calendar.MONTH);
 		mDay = date.get(Calendar.DAY_OF_MONTH);
         selectedDate = mYear + "-" + twoDigit.format(mMonth + 1) + "-" + twoDigit.format(mDay);
         bEvalPickDate.setText(selectedDate);
 	}
 }
