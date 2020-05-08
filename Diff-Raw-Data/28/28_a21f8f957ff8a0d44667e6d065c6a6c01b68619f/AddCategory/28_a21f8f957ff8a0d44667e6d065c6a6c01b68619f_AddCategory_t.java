 package ca.projectkarma.gradetrackr.activity;
 
 import ca.projectkarma.gradetrackr.ColorPickerDialog;
 import ca.projectkarma.gradetrackr.EditMode;
 import ca.projectkarma.gradetrackr.db.adapter.CategoriesDBAdapter;
 import ca.projectkarma.gradetrackr.db.adapter.CoursesDBAdapter;
 
 import ca.projectkarma.gradetrackr.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddCategory extends Activity {
 
     // database
     CoursesDBAdapter coursesDB = new CoursesDBAdapter(this);
     CategoriesDBAdapter categoriesDB = new CategoriesDBAdapter(this);
 
     // context
     Context context = this;
 
     // views
     EditText etCatName;
     EditText etCatWeight;
     Button bChooseColor;
     Button bCatDone;
     View previewColor;
 
     // variables
     private final String TAG = "AddCat";
     int[] refID;
     int selectedRefID;
     int refIDGet_Term;
     int refIDGet_Course;
     int idGet_Mode; // use with EditMode constants
     int idEdit_Item;
     // Default colour for the category and therefore the colour picker.
     // If changing this, change the color of the previewView in the add category xml
    int catColor = 0xA000BFFF;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_category);
 
         etCatName = (EditText) findViewById(R.id.etCatName);
         etCatWeight = (EditText) findViewById(R.id.etCatWeight);
         previewColor = (View) findViewById(R.id.catColorPreview);
         bChooseColor = (Button) findViewById(R.id.bCatChooseColor);
         bCatDone = (Button) findViewById(R.id.bCatDone);
 
         Intent iAddCat = getIntent();
         refIDGet_Term = iAddCat.getIntExtra("refID_Term", -1);
         refIDGet_Course = iAddCat.getIntExtra("refID_Course", -1);
         idEdit_Item = iAddCat.getIntExtra("idEdit_Item", -1);
         idGet_Mode = iAddCat.getIntExtra("id_Mode", EditMode.ADD_MODE);
 
         coursesDB.open();
         Cursor cCourse = coursesDB.getCourse(refIDGet_Course);
         cCourse.moveToFirst();
 
         if (idGet_Mode == EditMode.ADD_MODE)
         {
             setTitle("Add Category to " + cCourse.getString(cCourse.getColumnIndex("courseTitle")));
         }
         else if (idGet_Mode == EditMode.EDIT_MODE)
         {
             setTitle("Edit Category from "
                     + cCourse.getString(cCourse.getColumnIndex("courseTitle")));
             bCatDone.setText(R.string.doneEditCat);
         }
         coursesDB.close();
 
         if (idGet_Mode == EditMode.EDIT_MODE)
         {
             categoriesDB.open();
             Cursor cCategory = categoriesDB.getCategory(idEdit_Item);
             cCategory.moveToFirst();
             etCatName.setText(cCategory.getString(cCategory.getColumnIndex("catTitle")));
             etCatWeight.setText(cCategory.getString(cCategory.getColumnIndex("catWeight")));
             catColor = cCategory.getInt(cCategory.getColumnIndex("catColor"));
             previewColor.setBackgroundColor(catColor);
             categoriesDB.close();
         }
     }
 
     public void addCat(View v) {
 
         Log.d(TAG, "Adding or Editing Category.");
 
         if (etCatName.getText().toString().trim().equals("")
                 || etCatWeight.getText().toString().trim().equals(""))
         {
             new AlertDialog.Builder(this).setMessage("Please enter data for all the fields.")
                     .setPositiveButton("OK", null).show();
         }
         else
         {
             String catName = etCatName.getText().toString();
             double catWeight = 0.0;
 
             try
             {
                 catWeight = Double.parseDouble(etCatWeight.getText().toString());
             }
             catch (NumberFormatException nfe)
             {
                 Log.d(TAG, nfe.getMessage());
             }
 
             if (idGet_Mode == EditMode.ADD_MODE)
             {
                 categoriesDB.open();
                 categoriesDB.createCategory(catName, catWeight, refIDGet_Term, refIDGet_Course,
                         catColor);
                 categoriesDB.close();
 
                 Toast toast = Toast.makeText(context, "Category " + catName
                         + " added successfully.", Toast.LENGTH_SHORT);
                 try
                 {
                     // center toast
                     ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                             .setGravity(Gravity.CENTER_HORIZONTAL);
                 }
                 catch (ClassCastException cce)
                 {
                     Log.d(TAG, cce.getMessage());
                 }
                 toast.show();
 
                 Log.d(TAG, "Added Category Successfully.");
                 finish();
 
             }
             else if (idGet_Mode == EditMode.EDIT_MODE)
             {
                 categoriesDB.open();
                 categoriesDB.updateCategory(idEdit_Item, catName, catWeight, refIDGet_Term,
                         refIDGet_Course, catColor);
                 categoriesDB.close();
 
                 Toast toast = Toast.makeText(context, "Category " + catName
                         + " edited successfully.", Toast.LENGTH_SHORT);
                 try
                 {
                     // center toast
                     ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                             .setGravity(Gravity.CENTER_HORIZONTAL);
                 }
                 catch (ClassCastException cce)
                 {
                     Log.d(TAG, cce.getMessage());
                 }
                 toast.show();
 
                 Log.d(TAG, "Edited Category Successfully.");
                 finish();
             }
 
         }
 
     }
 
     @SuppressWarnings("deprecation")
     public void openColorPickerDiag(View v) {
         final ColorPickerDialog d = new ColorPickerDialog(this, catColor);
         d.setAlphaSliderVisible(true);
         d.setButton("OK", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 catColor = d.getColor();
                 // Update previewColor
                 previewColor.setBackgroundColor(catColor);
             }
         });
         d.setButton2("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
             }
         });
 
         d.show();
 
     }
 }
