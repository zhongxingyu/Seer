 package com.gdg.andconlab.ui;
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.gdg.andconlab.CommunicationService;
 import com.gdg.andconlab.DBUtils;
 import com.gdg.andconlab.DatabaseHelper;
 import com.gdg.andconlab.R;
 
 /**
  * Activity that displays a list of all lectures given in GDG
  * If no lectures were found in local DB - it will auto fetch lectures from the server,
  * displaying a wait dialog to the user. 
  * @author Ran Nachmany
  *
  */
 public class MainActivity extends SherlockActivity implements OnItemClickListener{
 
 	private ListView mList;
 	private ProgressDialog mProgressDialog;
 	private BroadcastReceiver mUpdateReceiver;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main_activity);
 		mList = (ListView) findViewById(R.id.list);
 
 		mList.setOnItemClickListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (null != mUpdateReceiver) {
 			unregisterReceiver(mUpdateReceiver);
 			mUpdateReceiver = null;
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
        mUpdateReceiver = new BroadcastReceiver() {
            //TODO: [Ran] handle network failure
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(CommunicationService.RESULTS_ARE_IN)) {
                    new lecturesLoader().execute((Void) null);
                }

                if (null != mProgressDialog)
                    mProgressDialog.dismiss();
            }
        };

        final IntentFilter filter = new IntentFilter();
        filter.addAction(CommunicationService.RESULTS_ARE_IN);
 		registerReceiver(mUpdateReceiver, filter);
 		
 		new lecturesLoader().execute((Void)null);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
 		Intent i = new Intent(this,SingleLectureActivity.class);
 		i.putExtra(SingleLectureActivity.EXTRA_LECTURE_ID, id);
 		startActivity(i);
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_refresh:
                 refreshList(true);
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 	
 	
 	private void refreshList(boolean showProgressbar) {
 		if (showProgressbar) {
 			mProgressDialog = ProgressDialog.show(this, getString(R.string.progress_dialog_starting_title), getString(R.string.progress_dialog_starting_message));
 		}
 		
 		Intent i = new Intent(this,CommunicationService.class);
 		startService(i);
 //		ServerCommunicationManager.getInstance(getApplicationContext()).startSearch("Android", 1);
 	}
 
 	////////////////////////////////
 	// Async task that queries the DB in background
 	////////////////////////////////
 	private class lecturesLoader extends AsyncTask<Void, Void, Cursor> {
 		private SQLiteDatabase db;
 
 		@Override
 		protected Cursor doInBackground(Void... params) {
 			db = new DatabaseHelper(MainActivity.this.getApplicationContext(), DatabaseHelper.DB_NAME,null , DatabaseHelper.DB_VERSION).getReadableDatabase();
 			return DBUtils.getAllLectures(db);
 		}
 
 		@Override
 		protected void onPostExecute(Cursor result) {
 			if (0 == result.getCount()) {
 				//we don't have anything in our DB, force network refresh
 				refreshList(true);
 			}
 			else {
 				LecturesAdapter adapter = (LecturesAdapter) mList.getAdapter();
 				if (null == adapter) {
 					adapter = new LecturesAdapter(MainActivity.this.getApplicationContext(), result);
 					mList.setAdapter(adapter);	
 				}
 				else {
 					adapter.changeCursor(result);
 				}
 			}
 			db.close();
 		}
 	}
 }
