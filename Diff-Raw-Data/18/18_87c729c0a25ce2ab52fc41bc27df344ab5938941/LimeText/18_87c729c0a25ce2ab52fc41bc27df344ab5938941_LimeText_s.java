 package me.lihong.limedroid;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class LimeText extends FragmentActivity {
 
 	private static final String TAG = "dialog";
 	
 	// some state variables
 	private boolean autoComplete = true;
 
 	private boolean creatingFile = false;
 	private boolean savingFile = false;
 	private boolean openingFile = false;
 	private boolean openingError = false;
 	private boolean openingRecent = false;
 	private boolean sendingAttachment = false;
 	private static CharSequence temp_filename = "";
 	private boolean fromIntent = false;
 	private boolean openingIntent = false;
 	private Intent newIntent = null;
 	
 	private boolean fromSearch = false;
 	private String queryString = "";
 	
 	private CharSequence errorFname = "File";
 	private boolean errorSaving = false;
 
 	private int fileformat;
 	
 	// some global variables
 	protected static EditText text = null;
 	protected static TextView title = null; 
 	protected CharSequence filename = "";
 	protected long lastModified = 0;
 	protected boolean untitled = true;
 	
 	// file format ids
 	private final static int FILEFORMAT_NL = 1;
 	private final static int FILEFORMAT_CR = 2;
 	private final static int FILEFORMAT_CRNL = 3;
 	
 	// dialog ids
 	private final static int DIALOG_SAVE_FILE = 1;
 	private final static int DIALOG_OPEN_FILE = 2;
 	private final static int DIALOG_SHOULD_SAVE = 3;
 	private final static int DIALOG_OVERWRITE = 4;
 	private final static int DIALOG_SAVE_ERROR = 5;
 	private final static int DIALOG_SAVE_ERROR_PERMISSIONS = 6;
 	private final static int DIALOG_SAVE_ERROR_SDCARD = 7;
 	private final static int DIALOG_READ_ERROR = 8;
 	private final static int DIALOG_NOTFOUND_ERROR = 9;
 	private final static int DIALOG_SHOULD_SAVE_INTENT = 13;
 	private final static int DIALOG_MODIFIED = 14;
 	
 	private final static int DIALOG_SAVE_FILE_AUTOCOMPLETE = 10;
 	private final static int DIALOG_OPEN_FILE_AUTOCOMPLETE = 11;
 
 	private final static int DIALOG_RECENT_FILE_DIALOG = 12;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_lime_text);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_lime_text, menu);
         return true;
     }
     
 //    @Override
     public boolean onOptionsItemSelected(MenuItem item){
     	switch (item.getItemId()) {
 	    	case R.id.save:
	    		save();
 	    		return true;
 	    	case R.id.save_as:
 	    		saveAs();
 	    		return true;
 	    	default:
 	    		return super.onOptionsItemSelected(item);
     	}
     }
     
    private boolean save() {
    	return true;
     }
    
     /****************************************************************
 	 * saveNote()
 	 * 		What to do when saving note */
 	public void save(CharSequence fname)
 	{
 		errorSaving = false;
 		
 		// actually save the file here
 		try {
 			File f = new File(fname.toString());
 			
 			if ( (f.exists() && !f.canWrite()) || (!f.exists() && !f.getParentFile().canWrite()))
 			{
 				creatingFile = false;
 				openingFile = false;
 				errorSaving = true;
 				
 				if (fname.toString().indexOf("/sdcard/") == 0) {
					showDialog(DIALOG_SAVE_ERROR_SDCARD);
 				}else{
					showDialog(DIALOG_SAVE_ERROR_PERMISSIONS);
 				}
 
 				text.requestFocus();
 			
 				f = null;
 				return;
 			}
 			f = null; // hopefully this gets garbage collected
 			
 			// Create file 
 			FileWriter fstream = new FileWriter(fname.toString());
 			BufferedWriter out = new BufferedWriter(fstream);
 			
 			if (fileformat == FILEFORMAT_CR)
 			{
 				out.write(text.getText().toString().replace("\n", "\r"));
 			} else if (fileformat == FILEFORMAT_CRNL) {
 				out.write(text.getText().toString().replace("\n", "\r\n"));
 			} else {
 				out.write(text.getText().toString());
 			}
 			
 			out.close();
 			
 			// give a nice little message
 			Toast.makeText(this, R.string.onSaveMessage, Toast.LENGTH_SHORT).show();
 			
 			// the filename is the new title
 			title.setText(fname);
 			filename = fname;
 			untitled = false;
 		
 			lastModified = (new File(filename.toString())).lastModified();
 			
 			temp_filename = "";
 			
 			//addRecentFile(fname);
 		} catch (Exception e) { //Catch exception if any
 			creatingFile = false;
 			openingFile = false;
 			
 			if (fname.toString().indexOf("/sdcard/") == 0) {
				DialogFragment df = new SaveFileDialogFragment();
				df.show(getSupportFragmentManager(), TAG);
 			} else {
 			}
 			
 			errorSaving = true;
 		}
 		
 		text.requestFocus();
 	} // end saveNote()
     
     private boolean saveAs() {
     	return true;
     }
     
 }
