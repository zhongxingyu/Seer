 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;
 import java.io.EOFException;
 import java.util.ArrayList;
 
 import android.app.ExpandableListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.MenuItemCompat;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListAdapter;
 import android.widget.Toast;
 import android.widget.AbsListView.OnScrollListener;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.BillingItem;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.BillingListHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RawHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Adapters.BillingListAdapter;
 public class BillingScreen extends ExpandableListActivity implements Observer, OnScrollListener{
     private BillingListAdapter adapter;
     private BillingListHandler handler;
     private Session session;
     private SharedPreferences billingscreen_preferences;
     private Boolean isInitialized =false;
     private Integer tempCount = 25;
     
     private String limitString;
     private Integer limitValue = 25;
     public static ArrayList<BillingItem> billinglist = new ArrayList<BillingItem>();
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (savedInstanceState != null) {
             session = (Session) savedInstanceState.getSerializable(Session.IDENTIFIER);
         } else {
             session = (Session) getIntent().getSerializableExtra(Session.IDENTIFIER);
         }
         billingscreen_preferences = PreferenceManager.getDefaultSharedPreferences(this);
         
     	handler = new BillingListHandler(BillingScreen.this, session);
     	limitString = billingscreen_preferences.getString("limit", "25");
     	limitValue = Integer.valueOf(limitString);
     	isInitialized =false;
     	handler.execute();
 		
     }
 	@Override
 	public void onCompleted(RawHandler caller, Object object) {
 		handler = null;
 		if(isInitialized==false){
 		adapter = new BillingListAdapter(this,billinglist);
 		setListAdapter(adapter);
     	getExpandableListView().setOnScrollListener(this);
   	  	registerForContextMenu(getExpandableListView());
 	    isInitialized = true;
 			}
		
    	}
 	@Override
 	public void onResume(){
 		super.onResume();
 	//TODO::refresh new values from preferencescreen
 		
 	}
 	@Override
 	public void onError(RawHandler caller, Object object) {
 		handler = null;
 		//TODO::better error message
 }
 	//------------menu---------------
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.billingscreenmenu, menu);
 		return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.billinglistpreferences:
 			startActivity(new Intent(this, BillingScreenPreferences.class));
 			return true;
 				
 		}
 		return false;}
 	@Override
 	public void onScroll(AbsListView arg0, int firstVisible, int visibleCount, int totalCount) {
 		boolean loadMore = /* maybe add a padding */
				firstVisible + visibleCount >= totalCount-25;
 
 				if(loadMore && !adapter.isEnd) {
 					   adapter.count += 25;
 					   handler = new BillingListHandler(BillingScreen.this, session);
					   handler.setLimit(adapter.count);
					   adapter.setBillinglist(billinglist);
				       adapter.SetupList();
 					   adapter.notifyDataSetChanged();
					   handler.execute();
					 
 					}
 			
 	}
 	@Override
 	public void onScrollStateChanged(AbsListView arg0, int arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
