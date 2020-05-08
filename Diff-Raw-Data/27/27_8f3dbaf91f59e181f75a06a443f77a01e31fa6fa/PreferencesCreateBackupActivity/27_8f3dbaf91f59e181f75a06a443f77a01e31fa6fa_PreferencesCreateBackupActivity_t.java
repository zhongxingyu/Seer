 package org.dodgybits.android.shuffle.activity.preferences;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import org.dodgybits.android.shuffle.R;
 import org.dodgybits.android.shuffle.model.Context;
 import org.dodgybits.android.shuffle.model.Project;
 import org.dodgybits.android.shuffle.model.Task;
 import org.dodgybits.android.shuffle.provider.Shuffle;
 import org.dodgybits.android.shuffle.service.Progress;
 import org.dodgybits.android.shuffle.util.AlertUtils;
 import org.dodgybits.android.shuffle.util.BindingUtils;
 import org.dodgybits.shuffle.dto.ShuffleProtos.Catalogue;
 import org.dodgybits.shuffle.dto.ShuffleProtos.Catalogue.Builder;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class PreferencesCreateBackupActivity extends Activity 
 	implements View.OnClickListener {
     private static final String CREATE_BACKUP_STATE = "createBackupState";
 	private static final String cTag = "PreferencesCreateBackupActivity";
     
     private enum State {EDITING, IN_PROGRESS, COMPLETE, ERROR};
     
     private State mState = State.EDITING;
     private EditText mFilenameWidget;
     private Button mSaveButton;
     private Button mCancelButton;
     private ProgressBar mProgressBar;
     private TextView mProgressText;
     
     private AsyncTask<?, ?, ?> mTask;
     
     @Override
     protected void onCreate(Bundle icicle) {
         Log.d(cTag, "onCreate+");
         super.onCreate(icicle);
         setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
         setContentView(R.layout.backup_create);
         findViewsAndAddListeners();
 		onUpdateState();
     }
 
     private void findViewsAndAddListeners() {
         mProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);
         mProgressText = (TextView) findViewById(R.id.progress_label);
         mSaveButton = (Button) findViewById(R.id.saveButton);
         mCancelButton = (Button) findViewById(R.id.discardButton);
         mFilenameWidget = (EditText) findViewById(R.id.filename);
         
         mSaveButton.setOnClickListener(this);
         mCancelButton.setOnClickListener(this);
         
         // save progress text when we switch orientation
         mProgressText.setFreezesText(true);
     }
     
     public void onClick(View v) {
         switch (v.getId()) {
 
             case R.id.saveButton:
             	setState(State.IN_PROGRESS);
             	createBackup();
                 break;
 
             case R.id.discardButton:
             	finish();
                 break;
         }
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     	
     	outState.putString(CREATE_BACKUP_STATE, mState.name());
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     	
     	String stateName = savedInstanceState.getString(CREATE_BACKUP_STATE);
     	if (stateName == null) {
     		stateName = State.EDITING.name();
     	}
     	setState(State.valueOf(stateName));
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (mTask != null && mTask.getStatus() != AsyncTask.Status.RUNNING) {
             mTask.cancel(true);
         }
     }
     
     private void setState(State value) {
     	if (mState != value) {
     		mState = value;
     		onUpdateState();
     	}
     }
     
     private void onUpdateState() {
     	switch (mState) {
 	    	case EDITING:
 	    		setButtonsEnabled(true);
 	    		if (TextUtils.isEmpty(mFilenameWidget.getText())) {
 		            Date today = new Date();
 		            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
 		            String defaultText = "shuffle-" + formatter.format(today) + ".bak";
 		            mFilenameWidget.setText(defaultText);
 		            mFilenameWidget.setSelection(0, defaultText.length() - 4);
 	    		}    		
 	        	mFilenameWidget.setEnabled(true);
 	            mProgressBar.setVisibility(View.INVISIBLE);
 	            mProgressText.setVisibility(View.INVISIBLE);
 	            mCancelButton.setText(R.string.cancel_button_title);
 	    		break;
 	    		
 	    	case IN_PROGRESS:
 	    		setButtonsEnabled(false);
 	        	mFilenameWidget.setSelection(0, 0);
 	        	mFilenameWidget.setEnabled(false);
 	        	
 	        	mProgressBar.setProgress(0);
 		        mProgressBar.setVisibility(View.VISIBLE);
 	            mProgressText.setVisibility(View.VISIBLE);
 	    		break;
 	    		
 	    	case COMPLETE:
 	    		setButtonsEnabled(true);
 	        	mFilenameWidget.setEnabled(false);
 		        mProgressBar.setVisibility(View.VISIBLE);
 	            mProgressText.setVisibility(View.VISIBLE);
 	        	mSaveButton.setVisibility(View.GONE);
 	        	mCancelButton.setText(R.string.ok_button_title);
 	    		break;
 	    		
 	    	case ERROR:
 	    		setButtonsEnabled(true);
 	        	mFilenameWidget.setEnabled(true);
 		        mProgressBar.setVisibility(View.VISIBLE);
 	            mProgressText.setVisibility(View.VISIBLE);
 	        	mSaveButton.setVisibility(View.VISIBLE);
 	            mCancelButton.setText(R.string.cancel_button_title);
 	    		break;	
     	}
     }
 
     private void setButtonsEnabled(boolean enabled) {
     	mSaveButton.setEnabled(enabled);
     	mCancelButton.setEnabled(enabled);
     }
     
     private void createBackup() {
     	String filename = mFilenameWidget.getText().toString();
     	if (TextUtils.isEmpty(filename)) {
     		String message = getString(R.string.warning_filename_empty);
 			Log.e(cTag, message);
     		AlertUtils.showWarning(this, message);
 			setState(State.EDITING);
     	} else {
     		mTask = new CreateBackupTask().execute(filename);
     	}
     }
     
     private class CreateBackupTask extends AsyncTask<String, Progress, Void> {
 
     	public Void doInBackground(String... filename) {
             try {
             	String message = getString(R.string.status_checking_media);
 				Log.d(cTag, message);
             	publishProgress(Progress.createProgress(0, message));
 
             	String storage_state = Environment.getExternalStorageState();
             	if (! Environment.MEDIA_MOUNTED.equals(storage_state)) {
             		message = getString(R.string.warning_media_not_mounted, storage_state);
             		reportError(message);
             	} else {
 	        		File dir = Environment.getExternalStorageDirectory();
 	        		final File backupFile = new File(dir, filename[0]);
 	        		message = getString(R.string.status_creating_backup);
         	    	Log.d(cTag, message);
                 	publishProgress(Progress.createProgress(5, message));
                 	
                 	if (backupFile.exists()) {
                     	publishProgress(Progress.createErrorProgress("", new Runnable() {
                     		@Override
                     		public void run() {
                     			showFileExistsWarning(backupFile);
                     		}
                     	}));
                 	} else {
 	        			backupFile.createNewFile();
 	        			FileOutputStream out = new FileOutputStream(backupFile);
 	                	writeBackup(out);
                 	}
         		}
             } catch (Exception e) {
             	String message = getString(R.string.warning_backup_failed, e.getMessage());
         		reportError(message);
             }
             
             return null;
         }
     	
     	private void showFileExistsWarning(final File backupFile) {
     		OnClickListener buttonListener = new OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {
     				if (which == DialogInterface.BUTTON1) {
     			    	Log.i(cTag, "Overwriting file " + backupFile.getName());
     		            try {
 	            			FileOutputStream out = new FileOutputStream(backupFile);
 	                    	writeBackup(out);
     		            } catch (Exception e) {
     		            	String message = getString(R.string.warning_backup_failed, e.getMessage());
     		        		reportError(message);
     		            }
     				} else {
     					Log.d(cTag, "Hit Cancel button.");
     					setState(State.EDITING);
     				}
     			}
     		};
     		OnCancelListener cancelListener = new OnCancelListener() {
     			public void onCancel(DialogInterface dialog) {
 					Log.d(cTag, "Hit Cancel button.");
 					setState(State.EDITING);
     			}
     		};
     		
 			AlertUtils.showFileExistsWarning(PreferencesCreateBackupActivity.this, 
 					backupFile.getName(), buttonListener, cancelListener);
     		
     	}
     	
         private void writeBackup(OutputStream out) throws IOException {
         	Builder builder = Catalogue.newBuilder();
         	
         	writeContexts(builder, 10, 20);
         	writeProjects(builder, 20, 30);
         	writeTasks(builder, 30, 100);
 
         	builder.build().writeTo(out);
         	out.close();
 
         	String message = getString(R.string.status_backup_complete);
 			Progress progress = Progress.createProgress(100, message);
         	publishProgress(progress);
         	
         }
         
         private void writeContexts(Builder builder, int progressStart, int progressEnd)
         {
 	    	Log.d(cTag, "Writing contexts");
             Cursor cursor = getContentResolver().query(
             		Shuffle.Contexts.CONTENT_URI, Shuffle.Contexts.cFullProjection, 
             		null, null, null);
             int i = 0;
             int total = cursor.getCount();
             String type = getString(R.string.context_name);
         	while (cursor.moveToNext()) {
         		Context context = BindingUtils.readContext(cursor, getResources());
             	builder.addContext(context.toDto());
     			String text = getString(R.string.backup_progress, type, context.name);
     			int percent = calculatePercent(progressStart, progressEnd, ++i, total);
             	publishProgress(Progress.createProgress(percent, text));
         	}
         	cursor.close();
         }
 
         private void writeProjects(Builder builder, int progressStart, int progressEnd)
         {
 	    	Log.d(cTag, "Writing projects");
             Cursor cursor = getContentResolver().query(
             		Shuffle.Projects.CONTENT_URI, Shuffle.Projects.cFullProjection, 
             		null, null, null);
             int i = 0;
             int total = cursor.getCount();
             String type = getString(R.string.project_name);
         	while (cursor.moveToNext()) {
         		Project project = BindingUtils.readProject(cursor);
             	builder.addProject(project.toDto());
     			String text = getString(R.string.backup_progress, type, project.name);
     			int percent = calculatePercent(progressStart, progressEnd, ++i, total);
             	publishProgress(Progress.createProgress(percent, text));
         	}
         	cursor.close();
         }
         
         private void writeTasks(Builder builder, int progressStart, int progressEnd)
         {
 	    	Log.d(cTag, "Writing tasks");
             Cursor cursor = getContentResolver().query(
             		Shuffle.Tasks.CONTENT_URI, Shuffle.Tasks.cExpandedProjection, 
             		null, null, null);
             int i = 0;
             int total = cursor.getCount();
             String type = getString(R.string.task_name);
         	while (cursor.moveToNext()) {
         		Task task = BindingUtils.readTask(cursor, getResources());
             	builder.addTask(task.toDto());
     			String text = getString(R.string.backup_progress, type, task.description);
     			int percent = calculatePercent(progressStart, progressEnd, ++i, total);
             	publishProgress(Progress.createProgress(percent, text));
         	}
         	cursor.close();
         }
         
         private int calculatePercent(int start, int end, int current, int total) {
         	return start + (end - start) * current / total;
         }
         
         private void reportError(String message) {
 			Log.e(cTag, message);
         	publishProgress(Progress.createErrorProgress(message));
         }
                 
 		@Override
 		public void onProgressUpdate (Progress... progresses) {
		    Progress progress = progresses[0];
            String details = progress.getDetails();
 	        mProgressBar.setProgress(progress.getProgressPercent());
	        mProgressText.setText(details);
 
 	        if (progress.isError()) {
	            if (!TextUtils.isEmpty(details)) {
	                AlertUtils.showWarning(PreferencesCreateBackupActivity.this, details);
	            }
         		Runnable action = progress.getErrorUIAction();
 	        	if (action != null) {
 	        		action.run();
 	        	} else {
 		        	setState(State.ERROR);
 	        	}
 	        } else if (progress.isComplete()) {
 	        	setState(State.COMPLETE);
 	        }
 		}
 		
         @SuppressWarnings("unused")
         public void onPostExecute() {
             mTask = null;
         }
     	
     }
     
 }
