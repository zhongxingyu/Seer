 package com.boh.flatmate;
 
 import java.io.File;
 import com.boh.flatmate.R;
 import com.boh.flatmate.connection.Flat;
 import com.boh.flatmate.connection.ServerConnection;
 import com.boh.flatmate.connection.ShopItem;
 import com.boh.flatmate.connection.Shops;
 import com.boh.flatmate.connection.User;
 import com.google.android.maps.MapView;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 public class FlatMate extends FragmentActivity implements ActionBar.TabListener {
 
 	AppSectionsPagerAdapter mAppSectionsPagerAdapter;
 
 	NonSwipeableViewPager mViewPager;
 	View mMapViewContainer;
 	MapView mMapView;
 
 	public static Intent service;
 
 	public void onCreate(Bundle savedInstanceState) {
 		setTheme(R.style.flatMateTheme);
 		super.onCreate(savedInstanceState);
 
 		String value = "";
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			value = extras.getString("page");
 		}
 		//final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
 		contextExchanger.context = this;
 
 		ConnectionExchanger.connection = new ServerConnection();
 
 		FlatDataExchanger.flatData = new Flat();
 
 		FlatDataExchanger.flatData = ConnectionExchanger.connection.getMyFlat();
 		FlatDataExchanger.flatData.setCurrentUser(ConnectionExchanger.connection.getMe());
 
 		if(FlatDataExchanger.flatData.getCurrentUser().getFlatApproved().equals("true")){
 			setContentView(R.layout.flat_mate);
 
 			FlatDataExchanger.flatData.orderShopItems();
 
 			mapExchanger.mMapView = new MapView(this, getString(R.string.maps_api_key));
 
 			if(service == null) service = new Intent(FlatMate.this, UpdateService.class);
 			startService(service);
 
 			mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
 
 			final ActionBar actionBar = getActionBar();
 
 			//actionBar.setHomeButtonEnabled(false);
 
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 			mViewPager = (NonSwipeableViewPager) findViewById(R.id.pager);
 			mViewPager.setAdapter(mAppSectionsPagerAdapter);
 			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 				@Override
 				public void onPageSelected(int position) {
 					actionBar.setSelectedNavigationItem(position);
 				}
 			});
 			if(value.equals("shopping")){
 				actionBar.addTab(
 						actionBar.newTab()
 						.setIcon(R.drawable.flatmates_tab)
 						.setTabListener(this),false);
 				actionBar.addTab(
 						actionBar.newTab()
 						.setIcon(R.drawable.shopping_tab)
 						.setTabListener(this),true);
 				actionBar.addTab(
 						actionBar.newTab()
 						.setIcon(R.drawable.location_tab)
 						.setTabListener(this),false);
 			}else{
 				actionBar.addTab(
 						actionBar.newTab()
 						.setIcon(R.drawable.flatmates_tab)
 						.setTabListener(this),true);
 				actionBar.addTab(
 						actionBar.newTab()
 						.setIcon(R.drawable.shopping_tab)
 						.setTabListener(this),false);
 				actionBar.addTab(
 						actionBar.newTab()
 						.setIcon(R.drawable.location_tab)
 						.setTabListener(this),false);
 			}
 		}else{
			setContentView(R.layout.flat_mate_not_approved);
 		}
 
 	}
 
 	@Override
 	protected void onRestart() {
 
 	    // TODO Auto-generated method stub
 	    super.onRestart();
 	    Intent i = new Intent(FlatMate.this, FlatMate.class);  //your class
 	    startActivity(i);
 	    finish();
 
 	}
 
 	@Override 
 	public void onResume(){
 		super.onResume();
 		contextExchanger.context = this;
 	}
 
 	@Override
 	public void onStart(){
 		super.onStart();
 		//new loadData().execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_logout:
 			File logFile = new File(ConnectionExchanger.connection.FileName);
 			if (logFile.exists())
 			{
 				logFile.delete();
 			}
 			if(service != null) stopService(service);
 			finish();
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction){
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 	}
 
 	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public AppSectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int i) {
 			switch (i) {
 			case 0:
 				Fragment FlatList =  new FlatFragment();
 				return FlatList;
 			case 1:
 				Fragment ShoppingList =  new ShoppingFragment();
 				return ShoppingList;   
 			case 2:
 				return new MapFragment();
 			default:
 				Fragment FlatListDefault =  new FlatListFragment();
 				return FlatListDefault;
 			}
 		}
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return "";
 		}
 	}
 	
 	private class serverUpdateItem extends AsyncTask<Void,Void,Void> {
 		protected Void doInBackground(Void... item) {
 			return null;
 		}
 
 		protected void onPostExecute(Void result) {
 			onRestart();
 		}
 	}
 	
 	static public void userApproved(String approvedId, String approverName){
 		Log.v("APPRV",approverName + " has approved " + approvedId);
 		User me = FlatMate.FlatDataExchanger.flatData.getCurrentUser();
 		if(me.getId() == Integer.parseInt(approvedId)){
 			//new FlatMate.contextExchanger.context.serverUpdateItem().execute();
 		} else {
 			Log.v("APPRV","But that's not me...");			
 		}
 	}
 
 	public static class ConnectionExchanger{
 		public static ServerConnection connection;
 	}
 
 	public static class FlatDataExchanger{
 		public static Flat flatData;
 	}
 
 	public static class contextExchanger{
 		public static Context context;
 	}
 
 	public static class mapExchanger {
 		public static MapView mMapView;
 	}
 
 	public static class ShopsExchanger{
 		public static Shops nearShops;
 		public static Shops bestShops;
 	}
 }
