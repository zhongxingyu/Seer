 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.radicaldynamic.groupinform.activities;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 import java.util.Map.Entry;
 
 import org.ektorp.Attachment;
 import org.ektorp.AttachmentInputStream;
 import org.ektorp.DocumentNotFoundException;
 import org.ektorp.ReplicationStatus;
 import org.odk.collect.android.utilities.FileUtils;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.webkit.MimeTypeMap;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.adapters.BrowserListAdapter;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.documents.FormDefinition;
 import com.radicaldynamic.groupinform.documents.FormInstance;
 import com.radicaldynamic.groupinform.documents.Generic;
 import com.radicaldynamic.groupinform.logic.AccountFolder;
 import com.radicaldynamic.groupinform.repositories.FormDefinitionRepo;
 import com.radicaldynamic.groupinform.repositories.FormInstanceRepo;
 import com.radicaldynamic.groupinform.services.DatabaseService;
 import com.radicaldynamic.groupinform.utilities.Base64Coder;
 import com.radicaldynamic.groupinform.utilities.DocumentUtils;
 import com.radicaldynamic.groupinform.utilities.FileUtilsExtended;
 import com.radicaldynamic.groupinform.xform.FormReader;
 import com.radicaldynamic.groupinform.xform.FormWriter;
 
 /**
  * Responsible for displaying buttons to launch the major activities. Launches
  * some activities based on returns of others.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  * @author Yaw Anokwa (yanokwa@gmail.com)
  */
 public class BrowserActivity extends ListActivity
 {
     private static final String t = "BrowserActivity: ";
     
     // Dialog status codes
     private static final int DIALOG_CREATE_FORM = 0;
     private static final int DIALOG_COPY_TO_FOLDER = 1;
     private static final int DIALOG_FOLDER_OUTDATED = 2;
     private static final int DIALOG_FOLDER_UNAVAILABLE = 3;
     private static final int DIALOG_FORM_BUILDER_LAUNCH_ERROR = 4;
     private static final int DIALOG_INSTANCES_UNAVAILABLE = 5;
     private static final int DIALOG_OFFLINE_ATTEMPT_FAILED = 6;
     private static final int DIALOG_OFFLINE_MODE_UNAVAILABLE_FOLDERS = 7;
     private static final int DIALOG_ONLINE_ATTEMPT_FAILED = 8;
     private static final int DIALOG_ONLINE_STATE_CHANGING = 9;
     private static final int DIALOG_REMOVE_FORM = 10;
     private static final int DIALOG_RENAME_FORM = 11;
     private static final int DIALOG_TOGGLE_ONLINE_STATE = 12;
     private static final int DIALOG_UNABLE_TO_COPY_DUPLICATE = 13;
     private static final int DIALOG_UNABLE_TO_RENAME_DUPLICATE = 14;
     private static final int DIALOG_UPDATING_FOLDER = 15;
     
     // Keys for option menu items
     private static final int MENU_OPTION_REFRESH = 0;
     private static final int MENU_OPTION_FOLDERS = 1;
     private static final int MENU_OPTION_NEWFORM = 2;
     private static final int MENU_OPTION_ODKTOOLS = 4;
     private static final int MENU_OPTION_INFO = 5;
     
     // Keys for persistence between screen orientation changes
     private static final String KEY_COPY_TO_FOLDER_AS   = "copy_to_folder_as";
     private static final String KEY_COPY_TO_FOLDER_ID   = "copy_to_folder_id";
     private static final String KEY_COPY_TO_FOLDER_NAME = "copy_to_folder_name";
     private static final String KEY_DIALOG_MESSAGE      = "dialog_msg";
     private static final String KEY_FORM_DEFINITION     = "form_definition_doc";
     private static final String KEY_SELECTED_DB         = "selected_db";
         
     // Request codes for returning data from specified intent 
     private static final int RESULT_ABOUT = 1;
     private static final int RESULT_COPY = 2;    
 
     private FormDefinition mFormDefinition;     // Stash for a selected form definition
     
     private String mCopyToFolderId;             // Data passed back from user selection on AccountFolderList
     private String mCopyToFolderName;           // Same
     private String mCopyToFolderAs;             // Used to pass to DIALOG_UNABLE_TO_COPY_DUPLICATE
     private String mSelectedDatabase;           // To save & restore the currently selected database
     private boolean mSpinnerInit = false;       // See s1...OnItemSelectedListener() where this is used in a horrid workaround
     
     private CopyToFolderTask mCopyToFolderTask;
     private RefreshViewTask mRefreshViewTask;
     private RemoveTask mRemoveTask;
     private RenameTask mRenameTask;
     private UpdateFolderTask mUpdateFolderTask;
     
     private Dialog mDialog;
     private String mDialogMessage;              // Custom message consumed by onCreateDialog()
 
     @SuppressWarnings("unchecked")
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);       
         setContentView(R.layout.browser);                
 
         // Load our custom window title
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_browser_activity);
         
         if (savedInstanceState == null) {
             mDialogMessage = "";
             mSelectedDatabase = null;
         } else {
             // For "copy to folder" operation, restore destination folder
             if (savedInstanceState.containsKey(KEY_COPY_TO_FOLDER_AS))
                 mCopyToFolderAs = savedInstanceState.getString(KEY_COPY_TO_FOLDER_AS);
 
             if (savedInstanceState.containsKey(KEY_COPY_TO_FOLDER_ID))
                 mCopyToFolderId = savedInstanceState.getString(KEY_COPY_TO_FOLDER_ID);
             
             if (savedInstanceState.containsKey(KEY_COPY_TO_FOLDER_NAME))
                 mCopyToFolderName = savedInstanceState.getString(KEY_COPY_TO_FOLDER_NAME);
 
             if (savedInstanceState.containsKey(KEY_DIALOG_MESSAGE))
                 mDialogMessage = savedInstanceState.getString(KEY_DIALOG_MESSAGE);
             
             // Restore custom dialog message
             if (savedInstanceState.containsKey(KEY_DIALOG_MESSAGE))
                 mDialogMessage = savedInstanceState.getString(KEY_DIALOG_MESSAGE);
             
             if (savedInstanceState.containsKey(KEY_SELECTED_DB))
                 mSelectedDatabase = savedInstanceState.getString(KEY_SELECTED_DB);
             
             Object data = getLastNonConfigurationInstance();
             
             if (data instanceof HashMap<?, ?>) {
                 mFormDefinition = (FormDefinition) ((HashMap<String, Generic>) data).get(KEY_FORM_DEFINITION);
             }
         }
 
         // Initiate and populate spinner to filter forms displayed by instances types
         ArrayAdapter<CharSequence> instanceStatus = ArrayAdapter
             .createFromResource(this, R.array.tf_task_spinner_values, android.R.layout.simple_spinner_item);        
         instanceStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         Spinner s1 = (Spinner) findViewById(R.id.taskSpinner);
         s1.setAdapter(instanceStatus);
         s1.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
             {
                 /*
                  * Probably an implementation bug, the listener erroneously is called during layout.
                  * Since this listener in effect triggers an Ektorp repository and this repository
                  * in turn creates Couch views and having the repository initiated twice within the same
                  * thread will cause a segfault we had to implement this little workaround to ensure
                  * that loadScreen() is not called twice.
                  * 
                  * See https://groups.google.com/group/android-developers/browse_thread/thread/d93ce1ef583a2a29
                  * and http://stackoverflow.com/questions/2562248/android-how-to-keep-onitemselected-from-firing-off-on-a-newly-instantiated-spinn
                  * for more on this disgusting issue. 
                  */
                 if (mSpinnerInit == false)
                     mSpinnerInit = true;
                 else
                     loadScreen();
             }
 
             public void onNothingSelected(AdapterView<?> parent) { }
         });
 
         // Set up listener for Folder Selector button in title
         Button b1 = (Button) findViewById(R.id.folderTitleButton);
         b1.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v)
             {
                 startActivity(new Intent(BrowserActivity.this, AccountFolderList.class));
             }
         });
 
         // Set up listener for Online Status button in title
         Button b2 = (Button) findViewById(R.id.onlineStatusTitleButton);
         b2.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v)
             {
                 showDialog(DIALOG_TOGGLE_ONLINE_STATE);
             }
         });
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
         loadScreen();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         
         if (resultCode == RESULT_CANCELED)
             return;
         
         switch (requestCode) {
         // "Exit" if the user resets GC Mobile
         case RESULT_ABOUT:
             Intent i = new Intent();
             i.putExtra("exit_app", true);
             setResult(RESULT_OK, i);
             finish();
             break; 
             
         case RESULT_COPY:
             mCopyToFolderId   = intent.getStringExtra(AccountFolderList.KEY_FOLDER_ID);
             mCopyToFolderName = intent.getStringExtra(AccountFolderList.KEY_FOLDER_NAME);
             showDialog(DIALOG_COPY_TO_FOLDER);
             break;
         }
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) 
     {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         FormDefinition form = (FormDefinition) getListAdapter().getItem((int) info.id);
         Intent i;
         
         switch (item.getItemId()) {
         case R.id.copy:            
             mFormDefinition = form;            
             i = new Intent(this, AccountFolderList.class);
             i.putExtra(AccountFolderList.KEY_COPY_TO_FOLDER, true);
             startActivityForResult(i, RESULT_COPY);
             return true;
             
         case R.id.edit:
             FormBuilderLauncherTask fbl = new FormBuilderLauncherTask();
             fbl.execute(form.getId());
             return true;
             
         case R.id.export:
             i = new Intent(this, DataExportActivity.class);
             i.putExtra(FormEntryActivity.KEY_FORMPATH, form.getId());
             startActivity(i);
             return true;
             
         case R.id.remove:
             mFormDefinition = form;
             showDialog(DIALOG_REMOVE_FORM);
             return true;
             
         case R.id.rename:
             mFormDefinition = form;
             showDialog(DIALOG_RENAME_FORM);
             return true;    
             
         default:
             return super.onContextItemSelected(item);
         }
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
     {        
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.browseractivity_cmenu, menu);
     }
     
     public Dialog onCreateDialog(int id)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);        
         View view = null;
         mDialog = null;
         
         switch (id) {
         // User wishes to make a new form
         case DIALOG_CREATE_FORM:
             view = inflater.inflate(R.layout.dialog_create_or_rename_form, null);
             
             // Set an EditText view to get user input 
             final EditText newFormName = (EditText) view.findViewById(R.id.formName);
             
             builder.setView(view);
             builder.setInverseBackgroundForced(true);
             builder.setTitle(getText(R.string.tf_create_form_dialog));
             
             builder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {                
                     FormDefinition form = new FormDefinition();
                     form.setName(newFormName.getText().toString().trim());
                     form.setStatus(FormDefinition.Status.placeholder);
                     
                     if (form.getName().length() == 0) {
                         removeDialog(DIALOG_CREATE_FORM);
                         Toast.makeText(getApplicationContext(), getString(R.string.tf_form_name_required), Toast.LENGTH_LONG).show();                        
                         showDialog(DIALOG_CREATE_FORM);
                     } else {                        
                         // Create a new form document and use an XForm template as the "xml" attachment
                         try {
                             // Basic deduplication
                             FormDefinitionRepo formDefinitionRepo = new FormDefinitionRepo(Collect.getInstance().getDbService().getDb(Collect.getInstance().getInformOnlineState().getSelectedDatabase()));
                             List<FormDefinition> definitions = formDefinitionRepo.findByName(form.getName());
 
                             if (!definitions.isEmpty()) {
                                 removeDialog(DIALOG_CREATE_FORM);
                                 Toast.makeText(getApplicationContext(), getString(R.string.tf_form_name_duplicate), Toast.LENGTH_LONG).show();
                                 showDialog(DIALOG_CREATE_FORM);
                                 return;
                             }
 
                             // Create empty form from template
                             InputStream is = getResources().openRawResource(R.raw.xform_template);
             
                             // Set up variables to receive data
                             ByteArrayOutputStream data = new ByteArrayOutputStream();
                             byte[] inputbuf = new byte[8192];            
                             int inputlen;
             
                             while ((inputlen = is.read(inputbuf)) > 0) {
                                 data.write(inputbuf, 0, inputlen);
                             }
 
                             form.addInlineAttachment(new Attachment("xml", new String(Base64Coder.encode(data.toByteArray())).toString(), FormWriter.CONTENT_TYPE));
                             Collect.getInstance().getDbService().getDb().create(form);
                             
                             is.close();
                             data.close();
                             
                            // Ensure that dialog is reset
                            removeDialog(DIALOG_CREATE_FORM);
                            
                             // Launch the form builder with the NEWFORM option set to true
                             Intent i = new Intent(BrowserActivity.this, FormBuilderFieldList.class);
                             i.putExtra(FormEntryActivity.KEY_FORMPATH, form.getId());
                             startActivity(i);
                         } catch (Exception e) {
                             Log.e(Collect.LOGTAG, t + "unable to read XForm template file; create new form process will fail");
                             e.printStackTrace();
                         }          
                     }
                 }
             });
         
             builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     removeDialog(DIALOG_CREATE_FORM);
                 }
             });
             
             mDialog = builder.create();
             break;
         
         case DIALOG_COPY_TO_FOLDER:
             view = inflater.inflate(R.layout.dialog_copy_to_folder, null);
             
             // Set an EditText view to get user input 
             final TextView copyDestination = (TextView) view.findViewById(R.id.copyDestination);
             final EditText copyName = (EditText) view.findViewById(R.id.copyName);
             
             copyDestination.setText(mCopyToFolderName);
             copyName.setText(mFormDefinition.getName());
             
             builder
                 .setTitle(R.string.tf_copy_to_folder)
                 .setView(view)
                 .setInverseBackgroundForced(true);
             
             builder.setPositiveButton(getText(R.string.tf_copy), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String copyAsName = copyName.getText().toString().trim();
                     
                     if (copyAsName.length() > 0) {
                         mCopyToFolderAs = copyAsName;
                         mCopyToFolderTask = new CopyToFolderTask();
                         mCopyToFolderTask.execute(mFormDefinition, mCopyToFolderId, copyAsName);
                         removeDialog(DIALOG_COPY_TO_FOLDER);                        
                     } else {
                         removeDialog(DIALOG_COPY_TO_FOLDER);   
                         Toast.makeText(getApplicationContext(), getString(R.string.tf_form_name_required), Toast.LENGTH_LONG).show();
                         showDialog(DIALOG_COPY_TO_FOLDER);
                     }
                 }
             });
             
             builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     removeDialog(DIALOG_COPY_TO_FOLDER);
                 }
             });
             
             mDialog = builder.create();
             break;
             
         // Local folder is most likely out-of-date
         case DIALOG_FOLDER_OUTDATED:
             builder
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_info)
                 .setTitle(R.string.tf_folder_outdated_dialog)
                 .setMessage(getString(R.string.tf_folder_outdated_dialog_msg, getSelectedFolderName()));
             
             builder.setPositiveButton(getString(R.string.tf_update), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {                    
                     removeDialog(DIALOG_FOLDER_OUTDATED);
                     mUpdateFolderTask = new UpdateFolderTask();
                     mUpdateFolderTask.execute();
                 }
             });
             
             builder.setNeutralButton(getString(R.string.tf_form_folders), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     startActivity(new Intent(BrowserActivity.this, AccountFolderList.class));
                     removeDialog(DIALOG_FOLDER_OUTDATED);
                 }
             });
             
             mDialog = builder.create();
             break;
             
         // Couldn't connect to DB (for a specific reason)
         case DIALOG_FOLDER_UNAVAILABLE:
             builder
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_info)
                 .setTitle(R.string.tf_folder_unavailable)
                 .setMessage(mDialogMessage);
             
             builder.setPositiveButton(getString(R.string.tf_form_folders), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     startActivity(new Intent(BrowserActivity.this, AccountFolderList.class));
                     removeDialog(DIALOG_FOLDER_UNAVAILABLE);
                 }
             });
             
             if (!Collect.getInstance().getIoService().isSignedIn()) {
                 builder.setNeutralButton(getString(R.string.tf_go_online), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         new ToggleOnlineStateTask().execute();
                         removeDialog(DIALOG_FOLDER_UNAVAILABLE);
                     }
                 });
             }
             
             mDialog = builder.create();
             break;        
             
         // Unable to launch form builder (instances present) 
         case DIALOG_FORM_BUILDER_LAUNCH_ERROR:
             builder
                 .setIcon(R.drawable.ic_dialog_info)
                 .setTitle(R.string.tf_unable_to_launch_form_builder_dialog)
                 .setMessage(R.string.tf_unable_to_launch_form_builder_dialog_msg);
                 
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.cancel();
                 }
             });
             
             mDialog = builder.create();
             break;
             
         // User requested forms (definitions or instances) to be loaded but none could be found 
         case DIALOG_INSTANCES_UNAVAILABLE:
             builder
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_info)
                 .setTitle(R.string.tf_unable_to_load_instances_dialog)
                 .setMessage(R.string.tf_unable_to_load_instances_dialog_msg);
 
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     loadScreen();
                     dialog.cancel();
                 }
             });
             
             mDialog = builder.create();
             break;
 
         // We can't go offline (user has not selected any databases to be replicated)
         case DIALOG_OFFLINE_MODE_UNAVAILABLE_FOLDERS:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_info)
             .setTitle(R.string.tf_unable_to_go_offline_dialog)
             .setMessage(R.string.tf_unable_to_go_offline_dialog_msg_reason_folders);
 
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.cancel();
                 }
             });
 
             mDialog = builder.create();
             
             break;
 
         // Simple progress dialog for online/offline
         case DIALOG_ONLINE_STATE_CHANGING:
             if (Collect.getInstance().getIoService().isSignedIn())
                 mDialog = ProgressDialog.show(this, "", getText(R.string.tf_inform_state_disconnecting));
             else
                 mDialog = ProgressDialog.show(this, "", getText(R.string.tf_inform_state_connecting));
             
             break;
         
         // Prompt user to connect/disconnect
         case DIALOG_TOGGLE_ONLINE_STATE:
             String buttonText;
             
             builder
                 .setCancelable(false) 
                 .setIcon(R.drawable.ic_dialog_info);
             
             if (Collect.getInstance().getIoService().isSignedIn()) {
                 builder.setTitle(getText(R.string.tf_go_offline) + "?").setMessage(R.string.tf_go_offline_dialog_msg);
                 buttonText = getText(R.string.tf_go_offline).toString();
             } else {
                 builder.setTitle(getText(R.string.tf_go_online) + "?").setMessage(R.string.tf_go_online_dialog_msg);
                 buttonText = getText(R.string.tf_go_online).toString();
             }
 
             builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     new ToggleOnlineStateTask().execute();
                     removeDialog(DIALOG_TOGGLE_ONLINE_STATE);
                 }
             });
 
             builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     removeDialog(DIALOG_TOGGLE_ONLINE_STATE);
                 }
             });
             
             mDialog = builder.create();
             break;
             
         // Tried going offline but couldn't
         case DIALOG_OFFLINE_ATTEMPT_FAILED:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle(R.string.tf_unable_to_go_offline_dialog)
             .setMessage(R.string.tf_unable_to_go_offline_dialog_msg_reason_generic);
 
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     loadScreen();
                     dialog.cancel();
                 }
             });
 
             mDialog = builder.create();
             break;            
             
         // Tried going online but couldn't
         case DIALOG_ONLINE_ATTEMPT_FAILED:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle(R.string.tf_unable_to_go_online_dialog)
             .setMessage(R.string.tf_unable_to_go_online_dialog_msg_reason_generic);
 
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     loadScreen();
                     dialog.cancel();
                 }
             });
 
             mDialog = builder.create();
             break;
             
         case DIALOG_REMOVE_FORM:
             String removeFormMessage = getString(R.string.tf_remove_form_without_instances_dialog_msg, mFormDefinition.getName());
             
             try {
                 // Determine if draft or complete instances exist for this definition
                 if (new FormInstanceRepo(Collect.getInstance().getDbService().getDb()).findByFormId(mFormDefinition.getId()).size() > 0) {
                     removeFormMessage = getString(R.string.tf_remove_form_with_instances_dialog_msg, mFormDefinition.getName());
                 }
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, t + "unexpected exception while processing DIALOG_REMOVE_FORM");
                 e.printStackTrace();
             }
             
             builder
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle(R.string.tf_remove_form)
             .setMessage(removeFormMessage);
 
             builder.setPositiveButton(getString(R.string.tf_remove), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {                    
                     removeDialog(DIALOG_REMOVE_FORM);
                     mRemoveTask = new RemoveTask();
                     mRemoveTask.execute(mFormDefinition);
                 }
             });
             
             builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     removeDialog(DIALOG_REMOVE_FORM);
                 }
             });
             
             mDialog = builder.create();
             break;
             
         case DIALOG_RENAME_FORM:
             view = inflater.inflate(R.layout.dialog_create_or_rename_form, null);
             
             // Set an EditText view to get user input 
             final EditText renamedFormName = (EditText) view.findViewById(R.id.formName);
             
             builder.setView(view);
             builder.setInverseBackgroundForced(true);
             builder.setTitle(getText(R.string.tf_rename_form_dialog));
             
             renamedFormName.setText(mFormDefinition.getName());
             
             builder.setPositiveButton(getText(R.string.tf_rename), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) { 
                     String newName = renamedFormName.getText().toString().trim();
                     
                     if (newName.length() == 0) {
                         removeDialog(DIALOG_RENAME_FORM);
                         Toast.makeText(getApplicationContext(), getString(R.string.tf_form_name_required), Toast.LENGTH_LONG).show();                        
                         showDialog(DIALOG_RENAME_FORM);
                     } else {
                         if (newName.equals(mFormDefinition.getName())) {
                             // Do nothing
                         } else {
                             // Hijack this variable in case we need to display DIALOG_UNABLE_TO_RENAME_DUPLICATE
                             mCopyToFolderAs = newName;
                             
                             mRenameTask = new RenameTask();
                             mRenameTask.execute(mFormDefinition, newName);
                         }
                     }
                 }
             });
         
             builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     removeDialog(DIALOG_RENAME_FORM);
                 }
             });
             
             mDialog = builder.create();
             break;            
 
         case DIALOG_UNABLE_TO_COPY_DUPLICATE:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle(R.string.tf_unable_to_copy)
             .setMessage(getString(R.string.tf_unable_to_copy_duplicate_dialog_msg, mCopyToFolderName, mCopyToFolderAs));
 
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     removeDialog(DIALOG_UNABLE_TO_COPY_DUPLICATE);
                     showDialog(DIALOG_COPY_TO_FOLDER);
                 }
             });
 
             mDialog = builder.create();
             break;
 
         case DIALOG_UNABLE_TO_RENAME_DUPLICATE:
             builder
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle(R.string.tf_unable_to_rename_dialog)
             .setMessage(getString(R.string.tf_unable_to_rename_dialog_msg, mCopyToFolderAs));
 
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     removeDialog(DIALOG_UNABLE_TO_RENAME_DUPLICATE);
                     showDialog(DIALOG_RENAME_FORM);
                 }
             });
 
             mDialog = builder.create();
             break;    
 
         case DIALOG_UPDATING_FOLDER:
             mDialog = ProgressDialog.show(this, "", getString(R.string.tf_updating_with_param, getSelectedFolderName()));            
             break;
         }
         
         return mDialog;        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
         menu.add(0, MENU_OPTION_REFRESH, 0, getString(R.string.refresh)).setIcon(R.drawable.ic_menu_refresh);
         menu.add(0, MENU_OPTION_FOLDERS, 0, getString(R.string.tf_form_folders)).setIcon(R.drawable.ic_menu_archive);
         menu.add(0, MENU_OPTION_NEWFORM, 0, getString(R.string.tf_create_form)).setIcon(R.drawable.ic_menu_add);
         menu.add(0, MENU_OPTION_ODKTOOLS, 0, getString(R.string.open_data_kit)).setIcon(R.drawable.ic_menu_upload);
         menu.add(0, MENU_OPTION_INFO, 0, getString(R.string.tf_inform_info)).setIcon(R.drawable.ic_menu_info_details);
         return true;
     }
     
     /*
      * (non-Javadoc)
      *
      * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
      */
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_BACK:
                 setResult(RESULT_OK);
                 finish();
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
     /**
      * Stores the path of selected form and finishes.
      */
     @Override
     protected void onListItemClick(ListView listView, View view, int position, long id)
     {
         FormDefinition form = (FormDefinition) getListAdapter().getItem(position);
         InstanceLoadPathTask ilp;
         Intent i;
 
         Log.d(Collect.LOGTAG, t + "selected form " + form.getId() + " from list");
 
         Spinner s1 = (Spinner) findViewById(R.id.taskSpinner);
         
         switch (s1.getSelectedItemPosition()) {
         // When showing all forms in folder... start a new form
         case 0:
             i = new Intent(this, FormEntryActivity.class);
             i.putStringArrayListExtra(FormEntryActivity.KEY_INSTANCES, new ArrayList<String>());
             i.putExtra(FormEntryActivity.KEY_FORMPATH, form.getId());
             startActivity(i);
             break;
         // When showing all forms in folder... edit a form
         case 1:
             FormBuilderLauncherTask fbl = new FormBuilderLauncherTask();
             fbl.execute(form.getId());
             break;
         // When showing all forms in folder... export records
         case 2:
             Intent dea = new Intent(this, DataExportActivity.class);
             dea.putExtra(FormEntryActivity.KEY_FORMPATH, form.getId());
             startActivity(dea);
             break;
         // When showing all draft forms in folder... browse selected form instances
         case 3:
             ilp = new InstanceLoadPathTask();
             ilp.execute(form.getId(), FormInstance.Status.draft);
             break;
         // When showing all completed forms in folder... browse selected form instances
         case 4:
             ilp = new InstanceLoadPathTask();
             ilp.execute(form.getId(), FormInstance.Status.complete);
             break;
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId()) {
         case MENU_OPTION_REFRESH:
             loadScreen();
             break;
         case MENU_OPTION_FOLDERS:
             startActivity(new Intent(this, AccountFolderList.class));
             break;
         case MENU_OPTION_NEWFORM:
             showDialog(DIALOG_CREATE_FORM);
             break;
         case MENU_OPTION_ODKTOOLS:
             startActivity(new Intent(this, ODKActivityTab.class));
             break;          
         case MENU_OPTION_INFO:
             startActivityForResult(new Intent(this, ClientInformationActivity.class), RESULT_ABOUT);
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public Object onRetainNonConfigurationInstance()
     {
         // Avoid refetching documents from database by preserving them
         HashMap<String, Generic> persistentData = new HashMap<String, Generic>();
         persistentData.put(KEY_FORM_DEFINITION, mFormDefinition);
         
         return persistentData;
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         outState.putString(KEY_COPY_TO_FOLDER_AS, mCopyToFolderAs);
         outState.putString(KEY_COPY_TO_FOLDER_ID, mCopyToFolderId);
         outState.putString(KEY_COPY_TO_FOLDER_NAME, mCopyToFolderName);
         outState.putString(KEY_DIALOG_MESSAGE, mDialogMessage);
         outState.putString(KEY_SELECTED_DB, mSelectedDatabase);
     }
     
     private class CopyToFolderTask extends AsyncTask<Object, Void, Void>
     {
         private static final String tt = t + "CopyToFolderTask: ";
         
         private static final String KEY_ITEM = "key_item";
         
         private boolean copied = false;
         private boolean duplicate = false;
         private String copyFormAsName;
         private String copyToFolderId;
         private FormDefinition formDefinition;
 
         ProgressDialog progressDialog = null;
         
         final Handler progressHandler = new Handler() {
             public void handleMessage(Message msg) {
                 progressDialog.setMessage(getString(R.string.tf_copying_with_param, msg.getData().getString(KEY_ITEM)));
             }
         };
         
         @Override
         protected Void doInBackground(Object... params)
         {            
             formDefinition = (FormDefinition) params[0];
             copyToFolderId = (String) params[1];
             copyFormAsName = (String) params[2];
             
             Log.d(Collect.LOGTAG, tt + "about to copy " + formDefinition.getId() + " to " + copyToFolderId);
             
             Message msg = progressHandler.obtainMessage();
             Bundle b = new Bundle();
             b.putString(KEY_ITEM, formDefinition.getName());
             msg.setData(b);
             progressHandler.sendMessage(msg);
             
             AttachmentInputStream ais = null;;
             ByteArrayOutputStream output = null;
             byte [] xml = null;
             
             byte [] buffer = new byte[8192];
             int bytesRead;
             
             try {
                 // Basic deduplication
                 FormDefinitionRepo formDefinitionRepo = new FormDefinitionRepo(Collect.getInstance().getDbService().getDb(copyToFolderId));
                 List<FormDefinition> definitions = formDefinitionRepo.findByName(copyFormAsName);
                 
                 if (!definitions.isEmpty()) {
                     duplicate = true;
                     return null;
                 }
 
                 ais = Collect.getInstance().getDbService().getDb().getAttachment(formDefinition.getId(), "xml");
                 
                 FormDefinition copyOfFormDefinition = new FormDefinition();
 
                 // If copying with the exact same name
                 if (copyFormAsName.equals(formDefinition.getName())) {
                     output = new ByteArrayOutputStream();
 
                     while ((bytesRead = ais.read(buffer)) != -1) {
                         output.write(buffer, 0, bytesRead);
                     }
                     
                     xml = output.toByteArray();
                     output.close();
                     
                     // No need to recompute this if it is an exact copy
                     copyOfFormDefinition.setXmlHash(formDefinition.getXmlHash());
                 } else {
                     // Rename form definition
                     xml = renameFormDefinition(ais, copyFormAsName);
                     
                     // Save to file first so we can get md5 hash
                     File f = new File(FileUtilsExtended.EXTERNAL_CACHE + File.separator + UUID.randomUUID() + ".xml");
                     FileOutputStream fos = new FileOutputStream(f);
                     fos.write(xml);
                     fos.close();
                     
                     copyOfFormDefinition.setXmlHash(FileUtils.getMd5Hash(f));
                     
                     f.delete();
                 }
 
                 ais.close();
                 
                 copyOfFormDefinition.setName(copyFormAsName);
                 copyOfFormDefinition.addInlineAttachment(new Attachment("xml", new String(Base64Coder.encode(xml)).toString(), FormWriter.CONTENT_TYPE));                
                 
                 Collect.getInstance().getDbService().getDb(copyToFolderId).create(copyOfFormDefinition);
 
                 // Copy all remaining attachments from the original form definition; preserve names
                 if (formDefinition.getAttachments().size() > 1) {
                     String formCachePath = FileUtilsExtended.FORMS_PATH + File.separator + formDefinition.getId();
                     String formCacheMediaPath = formCachePath + File.separator + FileUtilsExtended.MEDIA_DIR;
 
                     FileUtils.createFolder(formCachePath);
                     FileUtils.createFolder(formCacheMediaPath);
 
                     // Download attachments
                     for (Entry<String, Attachment> entry : formDefinition.getAttachments().entrySet()) {
                         ais = Collect.getInstance().getDbService().getDb().getAttachment(formDefinition.getId(), entry.getKey());
                         FileOutputStream file;
 
                         if (!entry.getKey().equals("xml")) {
                             file = new FileOutputStream(formCacheMediaPath + File.separator + entry.getKey());
 
                             buffer = new byte[8192];
                             bytesRead = 0;
 
                             while ((bytesRead = ais.read(buffer)) != -1) {
                                 file.write(buffer, 0, bytesRead);
                             }
 
                             file.close();
                         }
 
                         ais.close();
                     }
 
                     // Upload to new form definition document
                     String revision = copyOfFormDefinition.getRevision();
 
                     for (File f : new File(formCacheMediaPath).listFiles()) {
                         String fileName = f.getName();
                         String attachmentName = fileName;
 
                         Log.v(Collect.LOGTAG, t + ": attaching " + fileName + " to " + copyOfFormDefinition.getId());
 
                         String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                         String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
 
                         FileInputStream fis = new FileInputStream(f);
                         ais = new AttachmentInputStream(attachmentName, fis, contentType, f.length()); 
                         revision = Collect.getInstance().getDbService().getDb(copyToFolderId).createAttachment(copyOfFormDefinition.getId(), revision, ais);
                         fis.close();
                         ais.close();
                     }
                 }
 
                 copied = true;             
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, tt + "unexpected exception");
                 e.printStackTrace();
             }
             
             return null;
         }
         
         @Override
         protected void onPreExecute()
         {
             progressDialog = new ProgressDialog(BrowserActivity.this);
             progressDialog.setMessage(getString(R.string.tf_copying_please_wait));  
             progressDialog.show();
         }        
 
         @Override
         protected void onPostExecute(Void nothing)
         {   
             progressDialog.cancel();
             
             if (copied) {
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_something_was_successful, getString(R.string.tf_copy)), Toast.LENGTH_SHORT).show();
             } else if (duplicate) {
                 // Show duplicate explanation dialog
                 showDialog(DIALOG_UNABLE_TO_COPY_DUPLICATE);
             } else {
                 // Some other failure
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_something_failed, getString(R.string.tf_copy)), Toast.LENGTH_LONG).show();
             }
         }        
     }
     
     /*
      * Determine whether it is safe to launch the form browser.  For the time
      * being we need this so that we can allow/disallow access based on whether
      * instances exist for a given form.
      */
     private class FormBuilderLauncherTask extends AsyncTask<String, Void, String>
     {
         @Override
         protected String doInBackground(String... arg0)
         {
             String docId = arg0[0];
             List<FormInstance> instanceIds = new ArrayList<FormInstance>();
             String result = "";
             
             try {
                 instanceIds = new FormInstanceRepo(Collect.getInstance().getDbService().getDb()).findByFormId(docId);
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, t + "unexpected exception " + e.toString());
                 e.printStackTrace();
             } finally {                
                 if (instanceIds.isEmpty())
                     result = docId;
             }
             
             return result;
         }
         
         @Override
         protected void onPreExecute()
         {
             setProgressVisibility(true);
         }
         
         @Override
         protected void onPostExecute(String docId)
         {
             if (docId.length() > 0) {
                 // Success                
                 Intent i = new Intent(BrowserActivity.this, FormBuilderFieldList.class);
                 i.putExtra(FormEntryActivity.KEY_FORMPATH, docId);
                 startActivity(i);
             } else {
                 // Failure (instances present)
                 showDialog(DIALOG_FORM_BUILDER_LAUNCH_ERROR);
             }
             
             setProgressVisibility(false);
         }        
     }
 
     /*
      * Retrieve all instances of a certain status for a specified definition,
      * populate the instance browse list and start FormEditActivity accordingly.
      */
     private class InstanceLoadPathTask extends AsyncTask<Object, Integer, Void>
     {
         String formId;
         ArrayList<String> instanceIds = new ArrayList<String>();
         boolean caughtExceptionInBackground = true;
 
         @Override
         protected Void doInBackground(Object... params)
         {
             try {
                 formId = (String) params[0];
                 FormInstance.Status status = (FormInstance.Status) params[1];
                 instanceIds = new FormInstanceRepo(Collect.getInstance().getDbService().getDb()).findByFormAndStatus(formId, status);
                 caughtExceptionInBackground = false;
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, t + "unhandled exception while processing InstanceLoadPathTask.doInBackground(): " + e.toString());
                 e.printStackTrace();
             }
             
             return null;
         }
 
         @Override
         protected void onPreExecute()
         {
             setProgressVisibility(true);
         }
 
         @Override
         protected void onPostExecute(Void nothing)
         {
             if (caughtExceptionInBackground) {
                 mDialogMessage = getString(R.string.tf_unable_to_open_folder, getSelectedFolderName());
                 showDialog(DIALOG_FOLDER_UNAVAILABLE);
             } else {
                 try {
                     Intent i = new Intent(BrowserActivity.this, FormEntryActivity.class);
                     i.putStringArrayListExtra(FormEntryActivity.KEY_INSTANCES, instanceIds);
                     i.putExtra(FormEntryActivity.KEY_INSTANCEPATH, instanceIds.get(0));
                     i.putExtra(FormEntryActivity.KEY_FORMPATH, formId);
                     startActivity(i);
                 } catch (IndexOutOfBoundsException e) {
                     // There were no mInstanceIds returned (no DB error, per-se but something was missing)
                     showDialog(DIALOG_INSTANCES_UNAVAILABLE);
                 }
             }
 
             setProgressVisibility(false);
         }
     }
     
     /*
      * Refresh the main form browser view as requested by the user
      */
     private class RefreshViewTask extends AsyncTask<FormInstance.Status, Integer, FormInstance.Status>
     {
         private ArrayList<FormDefinition> documents = new ArrayList<FormDefinition>();
         private HashMap<String, HashMap<String, String>> tallies = new HashMap<String, HashMap<String, String>>();
         private boolean folderOutdated = false;
         private boolean folderUnavailable = false;        
 
         @Override
         protected FormInstance.Status doInBackground(FormInstance.Status... status)
         {
             try {               
                 // Clean up the currently selected database before we display anything from it
                 Collect.getInstance().getDbService().performHousekeeping(Collect.getInstance().getInformOnlineState().getSelectedDatabase());
                 
                 FormDefinitionRepo repo = new FormDefinitionRepo(Collect.getInstance().getDbService().getDb());                
                 tallies = repo.getFormsByInstanceStatus(status[0]);
                 
                 if (status[0].equals(FormInstance.Status.any)) {
                     documents = (ArrayList<FormDefinition>) repo.getAllActive();
                 } else {
                     documents = (ArrayList<FormDefinition>) repo.getAllActiveByKeys(new ArrayList<Object>(tallies.keySet()));    
                 }
                 
                 DocumentUtils.sortByName(documents);
             } catch (ClassCastException e) {
                 // TODO: is there a better way to handle empty lists?
                 Log.w(Collect.LOGTAG, t + e.toString());
             } catch (DocumentNotFoundException e) {
                 /*
                  * This most likely cause of this exception is that a design document could not be found.  This will happen if we are
                  * running a version of Inform that expects a design document by a certain name but the local folder does not have
                  * the most recent design documents.
                  */
                 Log.w(Collect.LOGTAG, t + e.toString());
                 folderOutdated = true;
                 folderUnavailable = true;
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, t + "unexpected exception " + e.toString());
                 folderUnavailable = true;
             }
 
             return status[0];                
         }
 
         @Override
         protected void onPreExecute()
         {
             setProgressVisibility(true);
         }
 
         @Override
         protected void onPostExecute(FormInstance.Status status)
         {
             RelativeLayout onscreenProgress = (RelativeLayout) findViewById(R.id.progress);
             onscreenProgress.setVisibility(View.GONE);
             
             /*
              * Special hack to ensure that our application doesn't crash if we terminate it
              * before the AsyncTask has finished running.  This is stupid and I don't know
              * another way around it.
              * 
              * See http://dimitar.me/android-displaying-dialogs-from-background-threads/
              */
             if (isFinishing())
                 return;
             
             BrowserListAdapter adapter = new BrowserListAdapter(getApplicationContext(), R.layout.browser_list_item, documents, tallies, (Spinner) findViewById(R.id.taskSpinner));
             
             setListAdapter(adapter);
 
             if (folderUnavailable) {
                 String db = Collect.getInstance().getInformOnlineState().getSelectedDatabase();
                 boolean isReplicated = Collect.getInstance().getInformOnlineState().getAccountFolders().get(db).isReplicated();
                 
                 if (folderOutdated && isReplicated) {
                     showDialog(DIALOG_FOLDER_OUTDATED);
                 } else {
                     mDialogMessage = getString(R.string.tf_unable_to_open_folder, getSelectedFolderName());
                     showDialog(DIALOG_FOLDER_UNAVAILABLE);
                 }
             } else {
                 // Provide hints to user
                 if (documents.isEmpty()) {
                     TextView nothingToDisplay = (TextView) findViewById(R.id.nothingToDisplay);
                     nothingToDisplay.setVisibility(View.VISIBLE);
                 } else {
                     if (mDialog == null || !mDialog.isShowing()) { 
                         Spinner s1 = (Spinner) findViewById(R.id.taskSpinner);
                         String descriptor = s1.getSelectedItem().toString().toLowerCase();
 
                         switch (s1.getSelectedItemPosition()) {
                         case 0:
                             Toast.makeText(getApplicationContext(), getString(R.string.tf_begin_instance_hint), Toast.LENGTH_SHORT).show();
                             break;
                         case 1:
                             Toast.makeText(getApplicationContext(), getString(R.string.tf_edit_form_definition_hint), Toast.LENGTH_SHORT).show();
                             break;
                         case 2:
                             Toast.makeText(getApplicationContext(), getString(R.string.tf_export_records_hint), Toast.LENGTH_SHORT).show();
                             break;
                         case 3:
                         case 4:
                             Toast.makeText(getApplicationContext(), getString(R.string.tf_browse_instances_hint, descriptor), Toast.LENGTH_SHORT).show();
                         }
                     }
                 }
             }
 
             setProgressVisibility(false);
         }
     }
     
     private class RemoveTask extends AsyncTask<Object, Void, Void>
     {
         FormDefinition formDefinition;
         ProgressDialog progressDialog;
         boolean removed = false;   
         
         @Override
         protected Void doInBackground(Object... params)
         {
             formDefinition = (FormDefinition) params[0];
             formDefinition.setStatus(FormDefinition.Status.removed);
             
             try {
                 Collect.getInstance().getDbService().getDb().update(formDefinition);
                 removed = true;
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, t + "unexpected exception");
                 e.printStackTrace();
             }
             
             return null;
         }
     
         @Override
         protected void onPreExecute()
         {
             progressDialog = new ProgressDialog(BrowserActivity.this);
             progressDialog.setMessage(getString(R.string.tf_removing_please_wait));  
             progressDialog.show();
         }
     
         @Override
         protected void onPostExecute(Void nothing)
         {   
             progressDialog.cancel();
             
             // TODO
             if (removed) {
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_something_was_successful, getString(R.string.tf_removal)), Toast.LENGTH_SHORT).show();
             } else {
                 // Unspecified failure
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_something_failed, getString(R.string.tf_removal)), Toast.LENGTH_LONG).show();                
             }   
                         
             loadScreen();
         }
     }
     
     private class RenameTask extends AsyncTask<Object, Void, Void>
     {
         private static final String tt = t + "RenameTask: ";        
         private static final String KEY_ITEM = "key_item";
         
         private boolean renamed = false;
         private boolean duplicate = false;
         private String newName;
         private FormDefinition formDefinition;
         
         ProgressDialog progressDialog = null;
         
         final Handler progressHandler = new Handler() {
             public void handleMessage(Message msg) {
                 progressDialog.setMessage(getString(R.string.tf_renaming_with_param, msg.getData().getString(KEY_ITEM)));
             }
         };
         
         @Override
         protected Void doInBackground(Object... params)
         {            
             formDefinition = (FormDefinition) params[0];
             newName = (String) params[1];
             
             Log.d(Collect.LOGTAG, tt + "about to rename " + formDefinition.getId() + " to " + newName);
             
             Message msg = progressHandler.obtainMessage();
             Bundle b = new Bundle();
             b.putString(KEY_ITEM, formDefinition.getName());
             msg.setData(b);
             progressHandler.sendMessage(msg);
             
             AttachmentInputStream ais = null;;
             byte [] xml = null;
             
             try {
                 // Basic deduplication
                 FormDefinitionRepo formDefinitionRepo = new FormDefinitionRepo(Collect.getInstance().getDbService().getDb());
                 List<FormDefinition> definitions = formDefinitionRepo.findByName(newName);
                 
                 if (!definitions.isEmpty()) {
                     // If there is more than one match OR the first (and only) match isn't the form that was selected
                     if (definitions.size() > 1 || definitions.get(0).getId() != formDefinition.getId()) {
                         duplicate = true;
                         return null;
                     }
                 }
                 
                 ais = Collect.getInstance().getDbService().getDb().getAttachment(formDefinition.getId(), "xml");
 
                 // Rename form definition
                 xml = renameFormDefinition(ais, newName);
 
                 // Save to file first so we can get md5 hash
                 File f = new File(FileUtilsExtended.EXTERNAL_CACHE + File.separator + UUID.randomUUID() + ".xml");
                 FileOutputStream fos = new FileOutputStream(f);
                 fos.write(xml);
                 fos.close();
 
                 formDefinition.setXmlHash(FileUtils.getMd5Hash(f));
 
                 f.delete();
                 ais.close();
                 
                 formDefinition.setName(newName);
                 formDefinition.addInlineAttachment(new Attachment("xml", new String(Base64Coder.encode(xml)).toString(), FormWriter.CONTENT_TYPE));                
                 
                 Collect.getInstance().getDbService().getDb().update(formDefinition);
                 
                 renamed = true;           
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, tt + "unexpected exception");
                 e.printStackTrace();
             }
             
             return null;
         }
         
         @Override
         protected void onPreExecute()
         {
             progressDialog = new ProgressDialog(BrowserActivity.this);
             progressDialog.setMessage(getString(R.string.tf_renaming_please_wait));  
             progressDialog.show();
         }        
 
         @Override
         protected void onPostExecute(Void nothing)
         {   
             progressDialog.cancel();
             
             if (renamed) {
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_something_was_successful, getString(R.string.tf_rename)), Toast.LENGTH_SHORT).show();
             } else if (duplicate) {
                 // Show duplicate explanation dialog
                 showDialog(DIALOG_UNABLE_TO_RENAME_DUPLICATE);
             } else { 
                 // Some other failure
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_something_failed, getString(R.string.tf_rename)), Toast.LENGTH_LONG).show();
             }
         }        
     }
     
     /*
      * Go online or offline at users request; synchronize folders accordingly.
      */
     private class ToggleOnlineStateTask extends AsyncTask<Void, Void, Void>
     {        
         Boolean hasReplicatedFolders = false;
         Boolean missingSynchronizedFolders = false;
         Boolean unableToGoOffline = false;
         Boolean unableToGoOnline = false;
         
         ProgressDialog progressDialog = null;        
         
         final Handler progressHandler = new Handler() {
             public void handleMessage(Message msg) {
                 progressDialog.setMessage(getString(R.string.tf_synchronizing_folder_count_dialog_msg, msg.arg1, msg.arg2));
             }
         };
         
         @Override
         protected Void doInBackground(Void... nothing)
         {
             // TODO? Perform checkin on demand -- this gives us the most accurate online/offline state 
             // Or maybe just again when the app starts up/is shown
             
             if (Collect.getInstance().getIoService().isSignedIn()) {
                 if (hasReplicatedFolders) {
                     // Only attempt to synchronize if we can reasonably do so
                     if (Collect.getInstance().getIoService().isSignedIn())
                         synchronize();                      
                     
                     if (!Collect.getInstance().getIoService().goOffline())
                         unableToGoOffline = true;
                 } else {
                     missingSynchronizedFolders = true;
                 }
             } else {
                 if (Collect.getInstance().getIoService().goOnline()) {                
                     if (hasReplicatedFolders)
                         synchronize();
                 } else
                     unableToGoOnline = true;
             }
 
             return null;
         }
 
         @Override
         protected void onPreExecute()
         {
             hasReplicatedFolders = Collect.getInstance().getInformOnlineState().hasReplicatedFolders();
 
             if (hasReplicatedFolders) {
                 progressDialog = new ProgressDialog(BrowserActivity.this);
                 progressDialog.setMessage(getString(R.string.tf_synchronizing_folders_dialog_msg));  
                 progressDialog.show();
             } else {
                 showDialog(DIALOG_ONLINE_STATE_CHANGING);
             }
             
             // Not available while toggling
             Button b1 = (Button) findViewById(R.id.onlineStatusTitleButton);
             b1.setEnabled(false);
             b1.setText(R.string.tf_inform_state_transition);
             
             Button b2 = (Button) findViewById(R.id.folderTitleButton);
             b2.setEnabled(false);
             b2.setText("...");
         }
 
         @Override
         protected void onPostExecute(Void nothing)
         {   
             if (progressDialog == null)
                 removeDialog(DIALOG_ONLINE_STATE_CHANGING);
             else
                 progressDialog.cancel();
             
             if (missingSynchronizedFolders) {
                 showDialog(DIALOG_OFFLINE_MODE_UNAVAILABLE_FOLDERS);
                 loadScreen();
             } else if (unableToGoOffline) {
                 // Load screen after user acknowledges to avoid stacking of dialogs
                 showDialog(DIALOG_OFFLINE_ATTEMPT_FAILED);
             } else if (unableToGoOnline) {
                 // Load screen after user acknowledges to avoid stacking of dialogs
                 showDialog(DIALOG_ONLINE_ATTEMPT_FAILED);
             } else { 
                 loadScreen();
             }
         }
         
         private void synchronize()
         {
             Set<String> folderSet = Collect.getInstance().getInformOnlineState().getAccountFolders().keySet();
             Iterator<String> folderIds = folderSet.iterator();
             
             int progress = 0;
             int total = 0;
             
             // Figure out how many folders are marked for replication
             while (folderIds.hasNext()) {
                 AccountFolder folder = Collect.getInstance().getInformOnlineState().getAccountFolders().get(folderIds.next());
                 
                 if (folder.isReplicated())
                     total++;
             }
             
             // Reset iterator
             folderIds = folderSet.iterator();    
                 
             while (folderIds.hasNext()) {
                 AccountFolder folder = Collect.getInstance().getInformOnlineState().getAccountFolders().get(folderIds.next());                
                 
                 if (folder.isReplicated()) {
                     Log.i(Collect.LOGTAG, t + "about to begin triggered replication of " + folder.getName());
                     
                     // Update progress dialog
                     Message msg = progressHandler.obtainMessage();
                     msg.arg1 = ++progress;
                     msg.arg2 = total;
                     progressHandler.sendMessage(msg);
                     
                     try {                        
                         Collect.getInstance().getDbService().replicate(folder.getId(), DatabaseService.REPLICATE_PUSH);
                         Collect.getInstance().getDbService().replicate(folder.getId(), DatabaseService.REPLICATE_PULL);
                     } catch (Exception e) {
                         Log.w(Collect.LOGTAG, t + "problem replicating " + folder.getId() + ": " + e.toString());
                         e.printStackTrace();
                     }
                 }
             }
         }
     }
     
     /*
      * Update (synchronize) a local database by pulling from the remote database.
      * Needed if the local database becomes outdated.
      */
     private class UpdateFolderTask extends AsyncTask<Void, Void, Void>
     {
         String db = Collect.getInstance().getInformOnlineState().getSelectedDatabase();
         AccountFolder folder = Collect.getInstance().getInformOnlineState().getAccountFolders().get(db);
         ReplicationStatus status = null;
         
         @Override
         protected Void doInBackground(Void... nothing)
         {
             try {
                 status = Collect.getInstance().getDbService().replicate(folder.getId(), DatabaseService.REPLICATE_PULL);
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, t + "unable to replicate during UpdateFolderTask: " + e.toString());
                 e.printStackTrace();
                 status = null;
             }
             
             return null;
         }
     
         @Override
         protected void onPreExecute()
         {
             showDialog(DIALOG_UPDATING_FOLDER);
         }
     
         @Override
         protected void onPostExecute(Void nothing)
         {   
             removeDialog(DIALOG_UPDATING_FOLDER);
             
             // No changes is the same as "unable to update" because chances are it will lead to the same problem
             if (status == null || !status.isOk() || status.isNoChanges())
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_unable_to_update_folder, getSelectedFolderName()), Toast.LENGTH_LONG).show();
             else
                 Toast.makeText(getApplicationContext(), getString(R.string.tf_folder_updated, getSelectedFolderName()), Toast.LENGTH_SHORT).show();
             
             loadScreen();
         }
     }
 
     // Attempt to return the current folder name (shortened to an appropriate length)
     public static String getSelectedFolderName()
     {
         String folderName = "...";
         
         try {
             folderName = Collect
                 .getInstance()
                 .getInformOnlineState()
                 .getAccountFolders()
                 .get(Collect.getInstance().getInformOnlineState().getSelectedDatabase())
                 .getName();
             
             // Shorten names that are too long
             if (folderName.length() > 23) 
                 folderName = folderName.substring(0, 20) + "...";
         } catch (NullPointerException e) {
             // Database metadata is not available at this time
             Log.w(Collect.LOGTAG, t + "folder metadata not available at this time");
             folderName = "?";
         }
         
         return folderName;
     }
 
     /*
      * Load the various elements of the screen that must wait for other tasks to complete
      */
     private void loadScreen()
     {
         // Reflect the online/offline status (may be disabled thanks to toggling state)
         Button b1 = (Button) findViewById(R.id.onlineStatusTitleButton);
         b1.setEnabled(true);
 
         if (Collect.getInstance().getIoService().isSignedIn())
             b1.setText(getText(R.string.tf_inform_state_online));
         else
             b1.setText(getText(R.string.tf_inform_state_offline));
 
         // Re-enable (may be disabled thanks to toggling state)
         Button b2 = (Button) findViewById(R.id.folderTitleButton);
         b2.setEnabled(true);
 
         // Spinner must reflect results of refresh view below
         Spinner s1 = (Spinner) findViewById(R.id.taskSpinner);        
         triggerRefresh(s1.getSelectedItemPosition());
         
         registerForContextMenu(getListView());
     }
     
     /*
      * Parse an attachment input stream (form definition XML file), affect h:title and instance 
      * root & id attribute and return the XML file as byte[] for consumption by the controlling task. 
      */
     private byte[] renameFormDefinition(AttachmentInputStream ais, String newName) throws Exception
     {
         FormReader fr = new FormReader(ais);
 
         // Populate global state (expected by FormWriter)
         Collect.getInstance().getFormBuilderState().setBinds(fr.getBinds());
         Collect.getInstance().getFormBuilderState().setFields(fr.getFields());
         Collect.getInstance().getFormBuilderState().setInstance(fr.getInstance());
         Collect.getInstance().getFormBuilderState().setTranslations(fr.getTranslations());
         
         return FormWriter.writeXml(newName, fr.getInstanceRoot(), fr.getInstanceRootId());
     }
     
     private void setProgressVisibility(boolean visible)
     {
         ProgressBar pb = (ProgressBar) getWindow().findViewById(R.id.titleProgressBar);
         
         if (pb != null) {
             if (visible) {
                 pb.setVisibility(View.VISIBLE);
             } else {
                 pb.setVisibility(View.GONE);
             }
         }
     }
 
     private void triggerRefresh(int position)
     {
         // Hide "nothing to display" message
         TextView nothingToDisplay = (TextView) findViewById(R.id.nothingToDisplay);
         nothingToDisplay.setVisibility(View.INVISIBLE);
         
         // Restore selected database (but only once)
         if (mSelectedDatabase != null) {
             Log.v(Collect.LOGTAG, t + "restoring selected database " + mSelectedDatabase);
             Collect.getInstance().getInformOnlineState().setSelectedDatabase(mSelectedDatabase);
             mSelectedDatabase = null;
         }
         
         String folderName = getSelectedFolderName();
         
         try {            
             // Reflect the currently selected folder
             Button b2 = (Button) findViewById(R.id.folderTitleButton);
             b2.setText(folderName);
 
             // Open selected database
             Collect.getInstance().getDbService().open(Collect.getInstance().getInformOnlineState().getSelectedDatabase());
         
             mRefreshViewTask = new RefreshViewTask();
 
             switch (position) {
             // Show all forms (in folder)
             case 0:
             case 1:
             case 2:
                 mRefreshViewTask.execute(FormInstance.Status.any);
                 break;
             // Show all draft forms
             case 3:
                 mRefreshViewTask.execute(FormInstance.Status.draft);
                 break;
             // Show all completed forms
             case 4:
                 mRefreshViewTask.execute(FormInstance.Status.complete);
                 break;
             }
         } catch (DatabaseService.DbUnavailableDueToMetadataException e) {            
             mDialogMessage = getString(R.string.tf_unable_to_open_folder_missing_metadata);
             showDialog(DIALOG_FOLDER_UNAVAILABLE);
         } catch (DatabaseService.DbUnavailableWhileOfflineException e) {
             mDialogMessage = getString(R.string.tf_unable_to_open_folder_while_offline, folderName);
             showDialog(DIALOG_FOLDER_UNAVAILABLE);
         } catch (DatabaseService.DbUnavailableException e) {
             mDialogMessage = getString(R.string.tf_unable_to_open_folder, folderName);
             showDialog(DIALOG_FOLDER_UNAVAILABLE);
         }
     }
 }
