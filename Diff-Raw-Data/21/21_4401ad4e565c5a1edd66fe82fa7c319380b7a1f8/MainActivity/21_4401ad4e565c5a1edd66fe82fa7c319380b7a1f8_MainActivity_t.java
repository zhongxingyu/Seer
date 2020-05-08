 package com.conica.DailyCapture;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class MainActivity extends Activity{
 	private static final String TAG = "capture";
     private static final int ACTIVITY_CREATE=0;
     private static final int ACTIVITY_EDIT=1;
     private boolean mDeleteMode;
     private Button mAddRecord_bt;
     private Button mDeleteRecord_bt;
 	private ListView mRecord_listview;  
 	private RecordsDBAdapter mRecordsDBAdapter;
     private RecordCursorAdapter mRecordCursorAdapter;
     private Cursor mRecordCursor;
     private AddPhoto mAddPhotoActivity;
     
 	  /** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.recordlist);
 	    mRecordsDBAdapter = new RecordsDBAdapter(getApplicationContext());
 	    MainApp.gRecordsDBAdapter = mRecordsDBAdapter;
         mRecordsDBAdapter.open(); 
 	    
 	    mRecord_listview = (ListView)findViewById(R.id.record_listview);  
 	    
 	    mDeleteMode = false;
 	    
 	    fillList(mDeleteMode);
         registerForContextMenu(mRecord_listview);
    
 	    mAddRecord_bt = (Button)findViewById(R.id.addrecord_bt);
 	    mAddRecord_bt.setOnClickListener(new OnClickListener() {			
 			public void onClick(View v) {
 				addRecord();								
 			}
 		});   
 	    
 	    mDeleteRecord_bt = (Button)findViewById(R.id.deleterecord_bt);
 	    mDeleteRecord_bt.setOnClickListener(new OnClickListener() {			
 			public void onClick(View v) {
 				deleteRecord();								
 			}
 		}); 
 	}
 	
 	private void fillList(boolean mode) {  
 		/*try{
 			mRecordsDBAdapter.open();
 		}catch(Exception e){
 			Log.i(TAG,"Open mDbAdapter database error :" + e.getMessage());
 		}*/
 		
 		if(mRecordCursor != null){
 			stopManagingCursor(mRecordCursor);
 			mRecordCursor.close();
 			mRecordCursor = null;
 		}
 		mRecordCursor = mRecordsDBAdapter.fetchAllRecords();
         startManagingCursor(mRecordCursor);
 
         // Create an array to specify the fields we want to display in the list (only TITLE)
         String[] from = new String[]{RecordsDBAdapter.KEY_RECORD_NAME, RecordsDBAdapter.KEY_RECORD_COUNT, RecordsDBAdapter.KEY_RECORD_TILE};
 
         // and an array of the fields we want to bind those fields to (in this case just text1)
         int[] to = new int[]{R.id.record_name, R.id.record_photono, R.id.record_imagetile};
     
         mRecordCursorAdapter = 
             new RecordCursorAdapter(this, R.layout.recordlistviewrow, mRecordCursor, from, to);
         mRecordCursorAdapter.setCaptureActivity(this);
 		mRecordCursorAdapter.setDelete(mDeleteMode);
         mRecord_listview.setAdapter(mRecordCursorAdapter);
     }
 	
 	private void addRecord(){
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setClass(getApplicationContext(), RecordEditActivity.class);
 		//startActivity(intent);
 		startActivityForResult(intent, ACTIVITY_CREATE);
 	}
 	
 	private void deleteRecord(){
 		mRecord_listview.removeAllViewsInLayout();
 		mDeleteMode = !mDeleteMode;
 		fillList(mDeleteMode);
 	}
 		
 	public void onItemEditClick(long id){
 		Log.i(TAG, "onItemEditClick id = " + id);
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setClass(getApplicationContext(), RecordEditActivity.class);
 		intent.putExtra(RecordsDBAdapter.KEY_RECORD_ROWID, id);
 		startActivityForResult(intent, ACTIVITY_EDIT);
 	}
 	
 	public void onItemShootClick(long id){
 		Log.i(TAG, "onItemShootClick id = " + id);
 		if(mAddPhotoActivity == null)
 			mAddPhotoActivity = new AddPhoto(this); 
 		mAddPhotoActivity.setParam(id, AddPhoto.ACTIVITY_SHOOT);
 		mAddPhotoActivity.doMission();
 	}
 	
 	public void onItemPickClick(long id){
 		Log.i(TAG, "onItemPickClick id = " + id);
 		if(mAddPhotoActivity == null)
 			mAddPhotoActivity = new AddPhoto(this); 
 		mAddPhotoActivity.setParam(id, AddPhoto.ACTIVITY_PICK);
 		mAddPhotoActivity.doMission();
 	}
 	
 
 	public void onItemDeleteClick(long id){
 		Log.i(TAG, "onItemDeleteClick id = " + id);
 		mRecordsDBAdapter.deleteRecord(id);
 		mRecord_listview.removeAllViewsInLayout();
 		fillList(mDeleteMode);
 	}
 		
 	public void onListItemClick(long id){
 		Log.i(TAG, "onListItemClick id = " + id);
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setClass(getApplicationContext(), PhotoGridViewActivity.class);
 		intent.putExtra(RecordsDBAdapter.KEY_RECORD_ROWID, id);
 		startActivityForResult(intent, ACTIVITY_EDIT);
 	}
 	
 	public void setViewVisiblity(final View v, final boolean visible){		
 		runOnUiThread(new Runnable() {
 			public void run() {
 				// TODO Auto-generated method stub
 				if(visible)
 					v.setVisibility(View.VISIBLE);
 				else
 					v.setVisibility(View.GONE);			
 			}
 		});
 	}
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);  
         if (resultCode != -1)   //RESULT_OK = -1
 			return;  
 		switch (requestCode) {  
 			case ACTIVITY_CREATE:
 			case ACTIVITY_EDIT:
 				break;
 			case AddPhoto.ACTIVITY_PICK:
 			case AddPhoto.ACTIVITY_SHOOT:
 				mAddPhotoActivity.onActivityResult(requestCode, resultCode, intent);
 				break;	
 		}
         mRecord_listview.removeAllViewsInLayout();
         fillList(mDeleteMode);
     }
 /*	
 	protected void onResume() {
     	Log.i(TAG,"MainActivity onResume");
     	fillList(deleteMode);
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
     	Log.i(TAG,"MainActivity onPause");
     	recordCursor.close();
 		mDbAdapter.close();
 		super.onPause();
 	}
 */
 
 }
