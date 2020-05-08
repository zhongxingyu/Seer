 package my.b1701.SB.Activities;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ToggleButton;
 import com.google.analytics.tracking.android.EasyTracker;
 import my.b1701.SB.Adapter.HistoryAdapter;
 import my.b1701.SB.Fragments.HistoryDailyPoolFragment;
 import my.b1701.SB.Fragments.HistoryInstaShareFragment;
 import my.b1701.SB.HelperClasses.ProgressHandler;
 import my.b1701.SB.R;
 import my.b1701.SB.Users.ThisUser;
 import my.b1701.SB.provider.HistoryContentProvider;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 
 public class SBHistoryActivity extends FragmentActivity{
     public static final String TAG = "my.b1701.SB.Activites.SBHistoryActivity";
     private static Uri mHistoryUri = Uri.parse("content://" + HistoryContentProvider.AUTHORITY + "/db_fetch_only");
     private static String[] columns = new String[]{ "sourceLocation",
             "destinationLocation",
             "timeOfRequest",
             "dailyInstantType",
             "takeOffer",
             "freq",
             "reqDate",
             "srclati",
             "srclongi",
             "dstlati",
             "dstlongi"};
 
 	FragmentManager fm = this.getSupportFragmentManager();
 	ToggleButton dailyinstatoggle_btn = null;
 	Button instaShare = null;	
 		
     @Override
     protected void onStart(){
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
     
     @Override
     protected void onStop(){
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
     }
     
 	 @Override
 	    protected void onCreate(Bundle savedInstanceState){
 		 super.onCreate(savedInstanceState);
 		 setContentView(R.layout.history_layout);
 		 dailyinstatoggle_btn = (ToggleButton)findViewById(R.id.history_dailyinsta_toggle_button);
 
 		 dailyinstatoggle_btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 EasyTracker.getTracker().sendEvent("ui_action", "toggle_press", "dailyInstaHistory", 1L);
 				if(isChecked)
 					showDailypoolHistory();
 				else
 					showInstaHistory();
 				
 			}
 		});
 
          loadHistoryFromDB();
          showInstaHistory();
 	 }
 	 
 	 public void showInstaHistory()
 	    {
 	    	if (fm != null) {
 	            FragmentTransaction fragTrans = fm.beginTransaction();
 	            fragTrans.replace(R.id.historyviewcontent, new HistoryInstaShareFragment());
 	            //ft.replace(R.id.historyviewcontent, instaFrag);
 	            fragTrans.commit();
 	           
 	        }
 	    }
 	    
 	    public void showDailypoolHistory()
 	    {
 	    	if (fm != null) {	    		
 	            FragmentTransaction ft = fm.beginTransaction();
 	            ft.replace(R.id.historyviewcontent, new HistoryDailyPoolFragment());
 	            ft.commit();
 	           
 	        }
 	    }
 
     private void loadHistoryFromDB() {
         ProgressHandler.showInfiniteProgressDialoge(this, "Fetching history", "Please wait...");
        List<HistoryAdapter.HistoryItem> historyItemList;
         Log.e(TAG, "Fetching searches");
         ContentResolver cr = getContentResolver();
         Cursor cursor = cr.query(mHistoryUri, columns, null, null, null);
 
         if (cursor == null || cursor.getCount() == 0) {
             Log.e(TAG, "Empty result");
            historyItemList = Collections.EMPTY_LIST;
         } else {
             List<HistoryAdapter.HistoryItem> historyItems = new ArrayList<HistoryAdapter.HistoryItem>();
             if (cursor.moveToFirst()) {
                 do {
                     HistoryAdapter.HistoryItem historyItem = new HistoryAdapter.HistoryItem(cursor.getString(0),
                             cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4),
                             cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getInt(8), cursor.getInt(9)
                             , cursor.getInt(10));
                     historyItems.add(historyItem);
                 } while (cursor.moveToNext());
 
                 cursor.close();
             }
             if(historyItems.size()>0)
                 historyItemList = historyItems;
            else
                historyItemList = Collections.EMPTY_LIST;
         }
 
         ThisUser.getInstance().setHistoryItemList(historyItemList);
         ProgressHandler.dismissDialoge();
     }
 
 
 }
