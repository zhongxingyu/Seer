 package com.hag.bucketlst.activity;
 
 // test push from eclipse in testBranch?
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.hag.bucketlst.R;
 import com.hag.bucketlst.adapter.CatAdapter;
 import com.hag.bucketlst.application.BLApp;
 import com.hag.bucketlst.db.TbDbAdapter;
 
 public class NCategoryView extends Activity {
     /** Called when the activity is first created. */
 	
     private static final int ACTIVITY_VOICE_RECOGNITION_TASK = 1002;
     //private static final int ACTIVITY_BUCK_VIEW = 10;
     
     private EditText mCatName;
     private ListView mCatList;
     private TbDbAdapter mDbHelper;
     private Cursor mCatCursor;
     private CatAdapter mCatAdapter;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.ncedit);
         
         mDbHelper = BLApp.getHelper();
         mDbHelper.open();
         
         mCatName = (EditText)findViewById(R.id.catGet);
         mCatList = (ListView)findViewById(R.id.catList);
         
         ImageButton mCatAdd = (ImageButton) findViewById(R.id.catAdd);
         mCatAdd.setOnClickListener(mAddListener);
         ImageButton mCatSpeak = (ImageButton)findViewById(R.id.catSpeakNow);
         // Check to see if a recognition activity is present
         PackageManager pm = getPackageManager();
         List<ResolveInfo> activities = pm.queryIntentActivities(
                 new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
         if (activities.size() != 0) {
         	mCatSpeak.setOnClickListener(mAutoCat);
         } else {
         	mCatSpeak.setEnabled(false);
         }
 		
         initCatData();
     }
 
     // ********************************************************
     // * HELPER METHODS
     // * 
     // * 
     // * 
     // ********************************************************
     
     private void initCatData() {
     	mCatList.setOnItemClickListener(new mCatClickL());
         
     	mCatCursor = mDbHelper.fetchAllCategories();
         startManagingCursor(mCatCursor);
         
         mCatAdapter = new CatAdapter(this, R.layout.n_cat_rows, mCatCursor);
         mCatList.setAdapter(mCatAdapter);
     }    
     
 	
     private void startVoiceRecognitionTitle()
     {
 		Intent localIntent1 = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		localIntent1.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		localIntent1.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your Task");
 		startActivityForResult(localIntent1, ACTIVITY_VOICE_RECOGNITION_TASK);
     }
     
     private void createBucket() {
     	String catNameFin = BLApp.wordfix(mCatName.getText());
    	mDbHelper.makeCategory(catNameFin);
     	mCatName.setText("");
         InputMethodManager mgr = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
         mgr.hideSoftInputFromWindow(mCatName.getApplicationWindowToken(), 0);
     	updateCatView();
     }
     
     private void updateCatView()
     {
     	mCatCursor.requery();
 	    //initCatData();
     }
     
     private void deleteBucket(long rowId)
     {
     	mDbHelper.deleteCategory(rowId);
     	updateCatView();
     }
 
     // ********************************************************
     // * LIFE CYCLE
     // * 
     // * 
     // * 
     // ********************************************************
     
     @Override
     protected void onResume() {
        super.onResume();
     }
     
     // ********************************************************
     // * LISTENERS
     // * 
     // * 
     // * 
     // ********************************************************
     
 	private class mCatClickL implements OnItemClickListener 
     {
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View view, int position,
 				long id) {
 	        Intent i = new Intent(getApplicationContext(), ByBuckets.class);
 	        i.putExtra(TbDbAdapter.KEY_CAT_ID, id);
 	        startActivity(i);
 	        //startActivityForResult(i, ACTIVITY_BUCK_VIEW);
 		}
     }
 	
 	private OnClickListener mAutoCat = new OnClickListener()
     {
     	public void onClick(View v)
     	{
     		startVoiceRecognitionTitle();
     	}
     };
     
     // Create an anonymous implementation of OnClickListener
     private OnClickListener mAddListener = new OnClickListener()
     {
 		public void onClick(View v)
 		{
 	    	try
 	    	{
 	    		createBucket();
 	    	}
 	    	catch (Exception ex)
 	    	{
 	    		Toast.makeText(getApplicationContext(), "Please Add a Bucket", Toast.LENGTH_SHORT).show();
 	    	}
 		}
 	};
 
 	public void onDelClick(final long l) {
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Delete Bucket?");
     	builder.setMessage("All tasks associated with this bucket will also be deleted.");
     	builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             	deleteBucket(l);
             }
         });
     	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             	dialog.cancel();
             }
         });
 
     	Dialog dialog = builder.create();
     	dialog.show();
 	}
 
 	public void onEditClick(long l) {
		Toast.makeText(getApplicationContext(), "Edit", Toast.LENGTH_SHORT).show();	
 	}
 
 }
