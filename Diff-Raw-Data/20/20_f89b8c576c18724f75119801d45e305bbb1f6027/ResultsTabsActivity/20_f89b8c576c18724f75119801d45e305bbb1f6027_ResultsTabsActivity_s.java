 package com.example.grubber;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.example.grubber.R;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabContentFactory;
 
 public class ResultsTabsActivity extends Activity {
 
 	//borrowed from http://thepseudocoder.wordpress.com/2011/10/04/android-tabs-the-fragment-way/
     private TabHost mTabHost;
     private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
     private TabInfo mLastTab = null;
 
 	private ListView result_list;
 	private ProgressDialog progDialog; 
 	public final Context context = this;
 	//private View main_view;
 	private GoogleMap mMap;	
 	
 	int current_page = 0;
 	ArrayList<ResultContent> list_result = null;
 	final int itemsPerPage = 10;
 	Button loadMore = null;
 	int totalResults = 0;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
   		setContentView(R.layout.activity_results_tabbed);
   		
         // setup TabHost
         initialiseTabHost(savedInstanceState);
         if (savedInstanceState != null) {
             mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
         }
         
 		
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		
 	}
 	
 
 	public void onResume() {
     	super.onResume();
     	//Refresh the options menu when this activity comes in focus
     	invalidateOptionsMenu();
     	//this.tracker.trackPageView("/TopTracksActivity");
     }	
 	
 	
 	
 	
     private class TabInfo {
          private String tag;
          private Class<?> clss;
          private Bundle args;
          private Fragment fragment;
          TabInfo(String tag, Class<?> clazz, Bundle args) {
              this.tag = tag;
              this.clss = clazz;
              this.args = args;
          }
  
     }
  
     class TabFactory implements TabContentFactory {
  
         private final Context mContext;
  
         /**
          * @param context
          */
         public TabFactory(Context context) {
             mContext = context;
         }
  
         /** (non-Javadoc)
          * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
          */
         public View createTabContent(String tag) {
             View v = new View(mContext);
             v.setMinimumWidth(0);
             v.setMinimumHeight(0);
             return v;
         }
  
     }
 
  
     /** (non-Javadoc)
      * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
      */
     protected void onSaveInstanceState(Bundle outState) {
         outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
         super.onSaveInstanceState(outState);
     }
  
     /**
      * Step 2: Setup TabHost
      */
     private void initialiseTabHost(Bundle args) {
         mTabHost = (TabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup();
         TabInfo tabInfo = null;
         ResultsTabsActivity.addTab(this, this.mTabHost, this.mTabHost.newTabSpec("ResultsListFragment").setIndicator("List"), ( tabInfo = new TabInfo("ResultsListFragment", ResultsListFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         ResultsTabsActivity.addTab(this, this.mTabHost, this.mTabHost.newTabSpec("MapFragment").setIndicator("Map"), ( tabInfo = new TabInfo("MapFragment", MapFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         // Default to first tab
         this.onTabChanged("ResultsListFragment");
         //
         mTabHost.setOnTabChangedListener(new OnTabChangeListener(){
         	public void onTabChanged(String tag) {
                 TabInfo newTab = (TabInfo) mapTabInfo.get(tag);
                 if (mLastTab != newTab) {
                     FragmentTransaction ft = getFragmentManager().beginTransaction();
                     if (mLastTab != null) {
                         if (mLastTab.fragment != null) {
                             ft.detach(mLastTab.fragment);
                         }
                     }
                     if (newTab != null) {
                         if (newTab.fragment == null) {
                             newTab.fragment = Fragment.instantiate(getBaseContext(),
                                     newTab.clss.getName(), newTab.args);
                             if(tag.equals("ResultsListFragment"))
                             	ft.add(R.id.results_tab, newTab.fragment, newTab.tag);
                            else if(tag.equals("ResultsFragment"))
                             	ft.add(R.id.map_tab, newTab.fragment, newTab.tag);
                         } else {
                             ft.attach(newTab.fragment);
                         }
                     }
          
                     mLastTab = newTab;
                     ft.commit();
                     getFragmentManager().executePendingTransactions();
                 }
             }
         	});
     }
  
     
     
     /**
      * @param activity
      * @param tabHost
      * @param tabSpec
      * @param clss
      * @param args
      */
     private static void addTab(ResultsTabsActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
         // Attach a Tab view factory to the spec
         tabSpec.setContent(activity.new TabFactory(activity));
         String tag = tabSpec.getTag();
  
         // Check to see if we already have a fragment for this tab, probably
         // from a previously saved state.  If so, deactivate it, because our
         // initial state is that a tab isn't shown.
         tabInfo.fragment = activity.getFragmentManager().findFragmentByTag(tag);
         if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
             FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
             ft.detach(tabInfo.fragment);
             ft.commit();
             activity.getFragmentManager().executePendingTransactions();
         }
  
         tabHost.addTab(tabSpec);
     }
  
     /** (non-Javadoc)
      * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
      */
     public void onTabChanged(String tag) {
         TabInfo newTab = (TabInfo) this.mapTabInfo.get(tag);
         if (mLastTab != newTab) {
             FragmentTransaction ft = this.getFragmentManager().beginTransaction();
             if (mLastTab != null) {
                 if (mLastTab.fragment != null) {
                     ft.detach(mLastTab.fragment);
                 }
             }
             if (newTab != null) {
                 if (newTab.fragment == null) {
                     newTab.fragment = Fragment.instantiate(this,
                             newTab.clss.getName(), newTab.args);
                     if(tag.equals("ResultsListFragment"))
                     	ft.add(R.id.results_tab, newTab.fragment, newTab.tag);
                     else if(tag.equals("MapFragment"))
                     	ft.add(R.id.map_tab, newTab.fragment, newTab.tag);
                 } else {
                     ft.attach(newTab.fragment);
                 }
             }
  
             mLastTab = newTab;
             ft.commit();
             this.getFragmentManager().executePendingTransactions();
         }
     }
     
     
     
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.results, menu);
                       
         //Change profile button to login/register if they are not logged in
         if(SaveSharedPreference.getUserId(ResultsTabsActivity.this) == 0)
         {
             MenuItem profileItem = menu.findItem(R.id.action_profile);
         	profileItem.setTitle(R.string.login);
             //Toast.makeText(this,"Not logged in",Toast.LENGTH_SHORT).show();
         }
         else {
         	MenuItem signout = menu.findItem(R.id.action_signout);
         	signout.setVisible(true);
             signout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
             	public boolean onMenuItemClick(MenuItem item) {            		        	
         			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
         			alertDialogBuilder.setTitle(R.string.logout_msg);
         			alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
         				public void onClick(DialogInterface dialog,int id) {            					    							
 							//int tempUserName = SaveSharedPreference.getUserId(context);    			        		
 			        		dialog.cancel();    			        		
 			        		SaveSharedPreference.setUserId(context, 0);
         					Toast.makeText(context , "Logged out" , Toast.LENGTH_SHORT).show();
         					invalidateOptionsMenu();
 						}    						
 					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
     					public void onClick(DialogInterface dialog,int id) {    						
     						dialog.cancel();    					
     					}}
     				  );            		
             		AlertDialog alertDialog = alertDialogBuilder.create();
             		alertDialog.show();   
             		return true;            		
             	} 	
             });        	
         }        
         return true;
       }
 	
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
 	      case R.id.action_profile:
 	    	  if(SaveSharedPreference.getUserId(ResultsTabsActivity.this) != 0){
 	    		  Intent intent3 = new Intent(context, ProfileActivity.class);
 	    		  startActivity(intent3);   
 	    	  } else {
 	    		  Intent intent3 = new Intent(context, SignInTabsActivity.class);
 	    		  startActivity(intent3);   
 	    	  }
 	          break;   
 	       // Respond to the action bar's Up/Home button
 	      case android.R.id.home:
 	    	  finish();
 	          return true;	          
 	      default:
 	    	  break;
       }
 
       return true;
     }
  
 }
