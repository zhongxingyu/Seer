 package com.radicaldynamic.gcmobile.android.activities;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.ektorp.AttachmentInputStream;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.LinearLayout.LayoutParams;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.activities.FileDialog;
 import com.radicaldynamic.groupinform.activities.FormEntryActivity;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.listeners.DataImportListener;
 import com.radicaldynamic.groupinform.listeners.SelectFieldImportListener;
 import com.radicaldynamic.groupinform.tasks.DataImportTask;
 import com.radicaldynamic.groupinform.xform.Field;
 import com.radicaldynamic.groupinform.xform.FormReader;
 
 public class DataImportActivity extends Activity implements DataImportListener
 {
     private static final String t = "DataImportActivity: ";
     
     private static final int RESULT_FILE_SELECTED = 0;
     
     // Keys for retaining values between restarts and passing values to tasks, etc.
     private static final String KEY_DIALOG_MSG = "dialog_msg";    
     private static final String KEY_FIELD_IMPORT_MAP = "field_import_map";
     private static final String KEY_FORM_READER = "form_reader";
     public static final String KEY_FORM_SETUP_ASSIGNMENT = "setup_assignment";
     public static final String KEY_FORM_SETUP_NAME = "setup_name";
     public static final String KEY_FORM_SETUP_STATUS = "setup_status";
     public static final String KEY_IMPORT_OPTION_PRO = "preserve_record_order";
     public static final String KEY_IMPORT_OPTION_SFR = "skip_first_row";
     private static final String KEY_PREVIEWED_RECORDS = "previewed_records";
     public static final String KEY_SELECTED_FILE = "selected_file";
     
     // Dialog keys
     private static final int DIALOG_IMPORT_COMPLETE = 0;
     private static final int DIALOG_IMPORT_EMPTY = 1;
     private static final int DIALOG_IMPORT_FAILED = 2;
     private static final int DIALOG_IMPORTING = 3;
     private static final int DIALOG_LOADING_TEMPLATE = 4;
     private static final int DIALOG_UNABLE_TO_PARSE_TEMPLATE = 5;
     
     // Asynchronous tasks
     private DataImportTask mDataImportTask;
     private ParseFormDefinitionTask mParseFormDefinitionTask;
     
     // UI feedback
     private AlertDialog mAlertDialog;
     private ProgressDialog mProgressDialog;
     
     // Import wizard navigation
     private ViewFlipper mViewFlipper;
     private Button mNextStep;
     private Button mPreviousStep;
     
     // Import options
     private CheckBox mImportOptionSkipFirstRow;
     private CheckBox mImportOptionPreserveRecordOrder;
     
     private FormReader mFormReader;
     
     private String mDialogMsg = null;
     private String mSelectedFile = null;
     private String mFormDefinitionId = null;
     
     // Position of new form setup options on step #3
     private int mFormSetupNamePosition = 0;
     private int mFormSetupStatusPosition = 0;
     private int mFormSetupAssignmentPosition = 0;
     
     // Records retrieved from CSV for preview
     ArrayList<List<String>> mPreviewedRecords = new ArrayList<List<String>>();
     
     // Map of field-to-CSV column for prepopulating field values
     Map<String, Integer> mFieldImportMap = new HashMap<String, Integer>();
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
     {
         super.onActivityResult(requestCode, resultCode, intent);
         
         if (resultCode == RESULT_CANCELED)
             return;
         
         switch (requestCode) {
         case RESULT_FILE_SELECTED:
             mSelectedFile = intent.getStringExtra(FileDialog.RESULT_PATH);
             ((TextView) findViewById(R.id.stepSelectedFileName)).setText(new File(mSelectedFile).getName());
             break;
         }
     }
     
     @SuppressWarnings("unchecked")
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.data_import);
         setTitle(getString(R.string.app_name) + " > " + getString(R.string.tf_instance_import_wizard));
         
         // Grab wizard navigation elements
         mViewFlipper = (ViewFlipper) findViewById(R.id.wizardSteps);
         mNextStep = (Button) findViewById(R.id.stepNext);
         mPreviousStep = (Button) findViewById(R.id.stepPrevious);
         
         // Grab import option controls
         mImportOptionSkipFirstRow = (CheckBox) findViewById(R.id.importSkipFirstRow);
         mImportOptionPreserveRecordOrder = (CheckBox) findViewById(R.id.importPreserveOrder);
         
         // Restore data after restart/orientation change
         Object data = getLastNonConfigurationInstance();
         
         if (data instanceof DataImportTask) {
             mDataImportTask = (DataImportTask) data;
             mDataImportTask.setListener(this);
         } else if (data instanceof ParseFormDefinitionTask) {
             mParseFormDefinitionTask = (ParseFormDefinitionTask) data;
         } else if (data instanceof HashMap<?, ?>) {
             mFormReader = (FormReader) ((HashMap<String, Object>) data).get(KEY_FORM_READER);
             mFieldImportMap = (Map<String, Integer>) ((HashMap<String, Object>) data).get(KEY_FIELD_IMPORT_MAP);
             mPreviewedRecords = (ArrayList<List<String>>) ((HashMap<String, Object>) data).get(KEY_PREVIEWED_RECORDS);
         }
          
         if (savedInstanceState == null) {
             // Activity is new
             Intent i = getIntent();
             mFormDefinitionId = i.getStringExtra(FormEntryActivity.KEY_FORMPATH);            
         } else {            
             // Activity has been restarted, restore values
             if (savedInstanceState.containsKey(KEY_DIALOG_MSG))
                 mDialogMsg = savedInstanceState.getString(KEY_DIALOG_MSG);
             
             if (savedInstanceState.containsKey(FormEntryActivity.KEY_FORMPATH))
                 mFormDefinitionId = savedInstanceState.getString(FormEntryActivity.KEY_FORMPATH);
             
             if (savedInstanceState.containsKey(KEY_FORM_SETUP_ASSIGNMENT))
                 mFormSetupAssignmentPosition = savedInstanceState.getInt(KEY_FORM_SETUP_ASSIGNMENT);
             
             if (savedInstanceState.containsKey(KEY_FORM_SETUP_NAME))
                 mFormSetupNamePosition = savedInstanceState.getInt(KEY_FORM_SETUP_NAME);
             
             if (savedInstanceState.containsKey(KEY_FORM_SETUP_STATUS))
                 mFormSetupStatusPosition = savedInstanceState.getInt(KEY_FORM_SETUP_STATUS);
 
             if (savedInstanceState.containsKey(KEY_SELECTED_FILE)) {
                 mSelectedFile = savedInstanceState.getString(KEY_SELECTED_FILE);
                 ((TextView) findViewById(R.id.stepSelectedFileName)).setText(new File(mSelectedFile).getName());
             }
         }
                 
         mNextStep.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v)
             {
                 if (mViewFlipper.getCurrentView().getId() == R.id.wizardStep4) {
                     // Verify import
                     processCsvFile(DataImportListener.MODE_VERIFY);
                 } else {
                     // Proceed to next step
                     if (mSelectedFile == null) {
                         // User may not proceed without selecting a file to import
                         Toast.makeText(getApplicationContext(), "Select a CSV file before continuing", Toast.LENGTH_LONG).show();
                     } else {
                         mViewFlipper.showNext();
                         updateWizardNavigation();                    
                     }                    
                 }
             }   
         });        
      
         mPreviousStep.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) 
             {
                 mViewFlipper.showPrevious();
                 updateWizardNavigation();
             }            
         });
         
         Button selectFile = (Button) findViewById(R.id.importFileSelection);
         selectFile.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0)
             {
                 Intent intent = new Intent(DataImportActivity.this, FileDialog.class);
                 intent.putExtra(FileDialog.SELECTION_MODE, FileDialog.MODE_OPEN);
                 intent.putExtra(FileDialog.START_PATH, "/sdcard");
                 intent.putExtra(FileDialog.WINDOW_TITLE, "Select CSV File To Import");
                 startActivityForResult(intent, RESULT_FILE_SELECTED);
             }
         });
         
         mImportOptionSkipFirstRow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
             {
                 processCsvFile(DataImportListener.MODE_PREVIEW);
             }
         });
     }
     
     @Override
     protected void onPause() 
     {
         if (mProgressDialog != null && mProgressDialog.isShowing())
             mProgressDialog.dismiss();
         
         super.onPause();
     }
     
     @Override
     protected Dialog onCreateDialog(int id)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         mAlertDialog = null; 
         
         switch (id) {
         case DIALOG_IMPORT_COMPLETE:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_info)
             .setTitle("Import Successful")
             .setMessage(mDialogMsg);
             
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     mDialogMsg = null;
                     setResult(RESULT_OK);
                     finish();
                 }
             });
             
             mAlertDialog = builder.create();
             break;
         
         case DIALOG_IMPORT_EMPTY:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_info)
             .setTitle("Nothing To Import")
             .setMessage("The file that you selected does not contain any records to import.\n\nPlease select another file and try again.");
             
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.cancel();
                     mViewFlipper.setDisplayedChild(0);
                 }
             });
             
             mAlertDialog = builder.create();
             break;
             
         case DIALOG_IMPORT_FAILED:
             String dialogImportFailedMsg = "The file that you selected cannot be imported. Either it is not a CSV file or the format of the CSV file is incorrect.\n\nPlease see the Form Editor Guide for further instructions.\n\nhttp://groupcomplete.com/help";
             
             if (mDialogMsg != null)
                 dialogImportFailedMsg = mDialogMsg;
             
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle("Import Failed")
             .setMessage(dialogImportFailedMsg);
             
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     mDialogMsg = null;
                     dialog.cancel();
                     mViewFlipper.setDisplayedChild(0);
                     updateWizardNavigation();
                 }
             });
             
             mAlertDialog = builder.create();
             break;
             
         case DIALOG_IMPORTING:
             mProgressDialog = new ProgressDialog(this);
             mProgressDialog.setMessage(mDialogMsg);
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setCancelable(false);
             return mProgressDialog;
             
         case DIALOG_LOADING_TEMPLATE:
             mProgressDialog = new ProgressDialog(this);
             mProgressDialog.setMessage(getText(R.string.tf_loading_template_please_wait));
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setCancelable(false);            
             return mProgressDialog;
             
         case DIALOG_UNABLE_TO_PARSE_TEMPLATE:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle("Cannot Load Template")
             .setMessage("The template that you selected cannot be parsed due to an unexpected error. The form import wizard cannot continue until this problem is resolved.\n\nPlease contact support@groupcomplete.com with the following error message:\n\n" + mDialogMsg);
             
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     mDialogMsg = null;
                     dialog.cancel();
                     mViewFlipper.setDisplayedChild(0);
                     updateWizardNavigation();
                 }
             });
             
             mAlertDialog = builder.create();
             break;
         }
         
         return mAlertDialog;
     }    
     
     @Override
     public Object onRetainNonConfigurationInstance()
     {
         if (mDataImportTask != null && mDataImportTask.getStatus() != AsyncTask.Status.FINISHED)
             return mDataImportTask;
         
         if (mParseFormDefinitionTask != null && mParseFormDefinitionTask.getStatus() != AsyncTask.Status.FINISHED)
             return mParseFormDefinitionTask;
         
         HashMap<String, Object> data = new HashMap<String, Object>();
         data.put(KEY_FORM_READER, mFormReader);
         data.put(KEY_FIELD_IMPORT_MAP, mFieldImportMap);
         data.put(KEY_PREVIEWED_RECORDS, mPreviewedRecords);    
         
         return data;
     }
     
     @Override
     protected void onResume()
     {
         super.onResume();
         
         // Handle resume of data import task
         if (mDataImportTask != null) {
             mDataImportTask.setListener(this);
             
             if (mDataImportTask != null && mDataImportTask.getStatus() == AsyncTask.Status.FINISHED) {
                 try {
                     dismissDialog(DIALOG_IMPORTING);
                 } catch (Exception e) {
                     // Dialog might not be showing
                 }
             }
         }
         
         // Handle resume of parse form definition task
         if (mParseFormDefinitionTask != null) {           
             if (mParseFormDefinitionTask != null && mParseFormDefinitionTask.getStatus() == AsyncTask.Status.FINISHED) {
                 try {
                     dismissDialog(DIALOG_LOADING_TEMPLATE);
                 } catch (Exception e) {
                     // Dialog might not be showing
                 }
             }
         }
         
         updateWizardNavigation();
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         outState.putString(KEY_DIALOG_MSG, mDialogMsg);
         outState.putString(FormEntryActivity.KEY_FORMPATH, mFormDefinitionId);
         outState.putInt(KEY_FORM_SETUP_ASSIGNMENT, mFormSetupAssignmentPosition);
         outState.putInt(KEY_FORM_SETUP_NAME, mFormSetupNamePosition);
         outState.putInt(KEY_FORM_SETUP_STATUS, mFormSetupStatusPosition);
         outState.putString(KEY_SELECTED_FILE, mSelectedFile);
     }
     
     private class ParseFormDefinitionTask extends AsyncTask<Void, Void, FormReader>
     {
         private static final String tt = t + "ParseFormDefinitionTask: ";
         
         private boolean parseSuccessful = false;        
 
         @Override
         protected FormReader doInBackground(Void... params) 
         {  
             mFormReader = null;
             
             try {
                 AttachmentInputStream ais = Collect.getInstance().getDbService().getDb().getAttachment(mFormDefinitionId, "xml");
                 mFormReader = new FormReader(ais, true);            
                 ais.close();
                 
                 parseSuccessful = true;
             } catch (Exception e) {
                 if (Collect.Log.ERROR) Log.e(Collect.LOGTAG, tt + "critical error while trying to parse form definition: " + e.toString());
                 e.printStackTrace();
                 mDialogMsg = e.toString();
             }
             
             return mFormReader;
         }
         
         @Override
         protected void onPreExecute()
         {
             showDialog(DIALOG_LOADING_TEMPLATE);
         }
         
         @Override
         protected void onPostExecute(FormReader fr)
         {   
             removeDialog(DIALOG_LOADING_TEMPLATE);
             
             if (parseSuccessful && fr != null) {
                 populateImportFieldMapOptions(mFormReader);
             } else {
                 showDialog(DIALOG_UNABLE_TO_PARSE_TEMPLATE);
             }
         }
     }
     
     @Override
     public void importTaskFinished(Bundle data, ArrayList<List<String>> records) 
     {
        dismissDialog(DIALOG_IMPORTING);
         
         // Error out
         if (!data.getBoolean(SelectFieldImportListener.SUCCESSFUL, false)) {
             mDialogMsg = data.getString(DataImportListener.MESSAGE);
             showDialog(DIALOG_IMPORT_FAILED);
             return;
         }        
 
         switch (data.getInt(SelectFieldImportListener.MODE)) {
         case DataImportListener.MODE_PREVIEW:            
             // If import is empty, show message and abort
             if (records.isEmpty()) {
                 showDialog(DIALOG_IMPORT_EMPTY);
                 return;
             }
 
             // Save this because we might use it again
             mPreviewedRecords = records;
 
             // Reset preview 
             TableLayout table = (TableLayout) findViewById(R.id.importPreviewTable);
             table.removeAllViews();
             
             // For each record in preview...
             Iterator<List<String>> recordIterator = records.iterator();
 
             while (recordIterator.hasNext()) {
                 List<String> cells = recordIterator.next();
 
                 TableRow row;
                 TextView c;
 
                 // Display column headers
                 if (table.getChildCount() == 0) {
                     row = new TableRow(this); 
 
                     for (int i = 0; i < cells.size(); i++) {                        
                         c = new TextView(this);
                         c.setText("COLUMN #" + (i + 1));
                         c.setPadding(3, 3, 3, 3);
                         row.addView(c);              
                     }
 
                     table.addView(row, new TableLayout.LayoutParams());
                 }
 
                 row = new TableRow(this);
 
                 // Display columns
                 for (int i = 0; i < cells.size(); i++) {
                     c = new TextView(this);
                     c.setText(cells.get(i));
                     c.setPadding(5, 5, 5, 5);
                     c.setBackgroundDrawable(getResources().getDrawable(R.drawable.table_cell));
                     c.setTextColor(R.color.solid_white);
                     row.addView(c);
                 }
 
                 table.addView(row, new TableLayout.LayoutParams());
             }
 
             break;
 
         case DataImportListener.MODE_VERIFY:
             Toast.makeText(getApplicationContext(), "Verification successful", Toast.LENGTH_LONG).show();
             processCsvFile(DataImportListener.MODE_IMPORT);
             break;
 
         case DataImportListener.MODE_IMPORT:
             mDialogMsg = data.getString(DataImportListener.MESSAGE);
             showDialog(DIALOG_IMPORT_COMPLETE);
             break;
         }
     }
     
     // Setup UI for step #4 (mapping fields to columns in the import file for prepopulation) 
     private void populateImportFieldMapOptions(FormReader fr)
     {
         LinearLayout mapInterface = (LinearLayout) findViewById(R.id.wizardStep4MapContainer);
         mapInterface.removeAllViews();
         
         // Types that can accept default values/pre-population
         List<String> fieldTypes = Arrays.asList(new String [] { "input", "select", "select1" });
         List<String> bindTypes = Arrays.asList(new String [] { "barcode", "date", "dateTime", "decimal", "geopoint", "int", "select", "select1", "string", "time" });
         
         // If we were able to parse the form successfully then the structure will be found in memory
         ArrayList<Field> fields = fr.getFields();
         Iterator<Field> fieldIterator = fields.iterator();
 
         while (fieldIterator.hasNext()) {
             final Field f = fieldIterator.next();
             
             // Only provide a UI for mapping fields that can be prepopulated
             if (!fieldTypes.contains(f.getType()) || !bindTypes.contains(f.getBind().getType()))
                 continue;
 
             // Field label
             LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
             lp.setMargins(0, 5, 0, 0);                
 
             TextView tv = new TextView(this);                
             tv.setText(f.getLabel().toString());
             tv.setTextAppearance(this, android.R.style.TextAppearance_Medium);                
             mapInterface.addView(tv, lp);
 
             // Spinner to map column to field
             lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 
             Spinner sp = new Spinner(this);
             sp.setPrompt("Map " + f.getLabel().toString() + "?");
             mapInterface.addView(sp, lp);  
 
             // Add column map options
             List<String> firstRowPreview = mPreviewedRecords.get(0); 
             ArrayList<String> mapOptionList = new ArrayList<String>();
             
             if (f.getInstance().getDefaultValue().length() > 0)            
                 mapOptionList.add("Use template (" + f.getInstance().getDefaultValue() + ")");
             else
                 mapOptionList.add("Use template (no default value)");
 
             if (firstRowPreview != null) {
                 for (int i = 0; i < firstRowPreview.size(); i++) {
                     mapOptionList.add("Use column #" + (i + 1) + " (" + firstRowPreview.get(i) + ")");
                 }
             }
 
             ArrayAdapter<String> mapOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mapOptionList);
             mapOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
             sp.setAdapter(mapOptions);
             
             if (mFieldImportMap.containsKey(f.getLocation()))
                 sp.setSelection(mFieldImportMap.get(f.getLocation()));
             else
                 sp.setSelection(0);
             
             sp.setOnItemSelectedListener(new OnItemSelectedListener() {            
                 @Override
                 public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
                 {
                     mFieldImportMap.put(f.getLocation(), position);
                 }
 
                 @Override
                 public void onNothingSelected(AdapterView<?> arg0) 
                 {
                 }
             });
         }
     }
     
     private void populateNewFormSetupOptions()
     {
         // Set initial defaults for options
         ArrayList<String> formNameOptionList = new ArrayList<String>();
         formNameOptionList.add("Use template name");
         
         ArrayList<String> formStatusOptionList = new ArrayList<String>();
         formStatusOptionList.add("Set to draft");
         formStatusOptionList.add("Set to completed");
         
         ArrayList<String> formAssignmentOptionList = new ArrayList<String>();
         formAssignmentOptionList.add("Skip assignment");        
         
         // Add column options
         List<String> firstRowPreview = mPreviewedRecords.get(0); 
         
         if (firstRowPreview != null) {
             for (int i = 0; i < firstRowPreview.size(); i++) {
                 formNameOptionList.add("Use column #" + (i + 1) + " (" + firstRowPreview.get(i) + ")");
                 formStatusOptionList.add("Use column #" + (i + 1) + " (" + firstRowPreview.get(i) + ")");
                 formAssignmentOptionList.add("Using column #" + (i + 1) + " (" + firstRowPreview.get(i) + ")");
             }
         }
         
         // Create adapters        
         ArrayAdapter<String> formNameOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formNameOptionList);
         formNameOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         ArrayAdapter<String> formStatusOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formStatusOptionList);
         formStatusOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         ArrayAdapter<String> formAssignmentOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formAssignmentOptionList);
         formAssignmentOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         // Assign adapters to spinners
         Spinner formNames = (Spinner) findViewById(R.id.setFormNames);
         formNames.setAdapter(formNameOptions);
         formNames.setSelection(mFormSetupNamePosition);
         formNames.setOnItemSelectedListener(new OnItemSelectedListener() {            
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
             {
                 mFormSetupNamePosition = position;
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) 
             {
             }            
         });
         
         Spinner formStatuses = (Spinner) findViewById(R.id.setFormStatuses);
         formStatuses.setAdapter(formStatusOptions);
         formStatuses.setSelection(mFormSetupStatusPosition);        
         formStatuses.setOnItemSelectedListener(new OnItemSelectedListener() {            
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
             {
                 mFormSetupStatusPosition = position;
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) 
             {
             }            
         });
         
         Spinner formAssignments = (Spinner) findViewById(R.id.setFormAssignments);
         formAssignments.setAdapter(formAssignmentOptions);
         formAssignments.setSelection(mFormSetupAssignmentPosition);
         formAssignments.setOnItemSelectedListener(new OnItemSelectedListener() {            
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
             {
                 mFormSetupAssignmentPosition = position;
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) 
             {
             }            
         });
     }
 
     private void processCsvFile(int mode)
     {
         mDataImportTask = new DataImportTask();
         mDataImportTask.setListener(this);
 
         // Import path & mode
         mDataImportTask.setImportFilePath(mSelectedFile);
         mDataImportTask.setImportMode(mode);
         
         // Import options (preserve record order & skip first record)
         Bundle options = new Bundle();
         options.putBoolean(KEY_IMPORT_OPTION_PRO, mImportOptionPreserveRecordOrder.isChecked());
         options.putBoolean(KEY_IMPORT_OPTION_SFR, mImportOptionSkipFirstRow.isChecked());
         mDataImportTask.setImportOptions(options);
         
         // New form setup (assignments, names, statuses)
         Bundle formSetup = new Bundle();
         formSetup.putInt(KEY_FORM_SETUP_ASSIGNMENT, mFormSetupAssignmentPosition);
         formSetup.putInt(KEY_FORM_SETUP_NAME, mFormSetupNamePosition);
         formSetup.putInt(KEY_FORM_SETUP_STATUS, mFormSetupStatusPosition);
         mDataImportTask.setFormSetup(formSetup);
         
         // Template definition loaded in form reader & field-to-column import map
         mDataImportTask.setFormReader(mFormReader);
         mDataImportTask.setFieldImportMap(mFieldImportMap);
         
         // New form instances will be assigned to this form definition
         mDataImportTask.setFormDefinitionId(mFormDefinitionId);
         
         switch (mode) {
         case DataImportListener.MODE_PREVIEW:
             mDialogMsg = "Importing preview...";
             break;
             
         case DataImportListener.MODE_VERIFY:
            mDialogMsg = "Verifying import...";
             break;
             
         case DataImportListener.MODE_IMPORT:
             mDialogMsg = "Importing records...";
             break;
         }        
 
         showDialog(DIALOG_IMPORTING);
         mDataImportTask.execute();
     } 
     
     private void updateWizardNavigation()
     {        
         ((ScrollView) findViewById(R.id.wizardScroller)).smoothScrollTo(0, 0);
         
         switch (mViewFlipper.getCurrentView().getId()) {
         case R.id.wizardStep1:
             mPreviousStep.setEnabled(false);
             mNextStep.setText("Next  ");            
             break;
             
         case R.id.wizardStep2:
             // Reset column dependant import options (CSV file may have changed)
             mFieldImportMap = new HashMap<String, Integer>();
             mFormSetupNamePosition = 0;
             mFormSetupStatusPosition = 0;
             mFormSetupAssignmentPosition = 0;           
             
             // Preview
             processCsvFile(DataImportListener.MODE_PREVIEW);
             
             mPreviousStep.setEnabled(true);
             mNextStep.setText("Next  ");
             break;
             
         case R.id.wizardStep3:
             // UI interface
             populateNewFormSetupOptions();
             
             mPreviousStep.setEnabled(true);
             mNextStep.setText("Next  ");            
             break;
             
         case R.id.wizardStep4:
             // After form is correctly parsed, populateImportFieldMapOptions() will be triggered
             mParseFormDefinitionTask = new ParseFormDefinitionTask();
             mParseFormDefinitionTask.execute();            
             
             mPreviousStep.setEnabled(true);
             mNextStep.setText("Verify ");            
             break;
         }
     }
 }
