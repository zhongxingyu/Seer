 package com.valfom.sports.tracker;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.Button;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public class TrackerInfoActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
 	
 	private TrackerInfoSectionsPagerAdapter sectionsPagerAdapter;
 	protected TrackerViewPager viewPager;
 	
 	private SensorManager mSensorManager;
 	private TrackerShakeEventListener mShakeEventListener;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.info);
 		
 		sectionsPagerAdapter = new TrackerInfoSectionsPagerAdapter(getSupportFragmentManager());
 
         final ActionBar actionBar = getSupportActionBar();
         
       actionBar.setDisplayShowHomeEnabled(true);
       actionBar.setDisplayShowTitleEnabled(true);
         
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         viewPager = (TrackerViewPager) findViewById(R.id.pager);
         viewPager.setAdapter(sectionsPagerAdapter);
         viewPager.setOffscreenPageLimit(2);
         
         viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             
         	@Override
             public void onPageSelected(int position) {
                 
         		actionBar.setSelectedNavigationItem(position);
             }
         });
 
         for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
             
         	actionBar.addTab(actionBar.newTab()
         			.setText(sectionsPagerAdapter.getPageTitle(i))
                     .setTabListener(this));
         }
         
         viewPager.setCurrentItem(1);
         
         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mShakeEventListener = new TrackerShakeEventListener();
 
 		mShakeEventListener.setOnShakeListener(new TrackerShakeEventListener.OnShakeListener() {
 
 			public void onShake() {
 				
 				int tabId = viewPager.getCurrentItem();
 				
				if (tabId == 1) {
 				
 					Intent intent = getIntent();
 					
 					if (intent.hasExtra("choise") && intent.getBooleanExtra("choise", false)) {
 	
 				        Intent buttons = new Intent(TrackerInfoActivity.this, TrackerButtonsActivity.class);
				        buttons.putExtra("tabId", tabId + 1);
 				        startActivityForResult(buttons, 1);
 					} 
 				}
 				
 				mSensorManager.unregisterListener(mShakeEventListener);
 			}
 		});
 	}
 
 	@Override
 	protected void onPause() {
 		
 		Intent intent = getIntent();
 		
 		if (intent.hasExtra("choise") && intent.getBooleanExtra("choise", false)) {
 		
 			TrackerSettings settings = new TrackerSettings(this);
 			
 			if (settings.isShaking())
 				mSensorManager.unregisterListener(mShakeEventListener);
 		}
 		
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		
 		super.onResume();
 		
 		Intent intent = getIntent();
 		
 		if (intent.hasExtra("choise") && intent.getBooleanExtra("choise", false)) {
 			
 			TrackerSettings settings = new TrackerSettings(this);
 			
 			if (settings.isShaking())
 				mSensorManager.registerListener(mShakeEventListener,
 						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 						SensorManager.SENSOR_DELAY_UI);
 		}
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent result) {
 
 		if (result != null) {
 			
 			int btnId = result.getIntExtra("btnId", -1);
 			
 			switch (btnId) {
 					
 				case R.id.ivSaveDialog:
 					Button btnSave = (Button) viewPager.getChildAt(0).findViewById(R.id.saveBtn);
 					btnSave.performClick();
 					break;
 				
 				case R.id.ivDeleteDialog:
 					Button btnDelete = (Button) viewPager.getChildAt(0).findViewById(R.id.deleteBtn);
 					btnDelete.performClick();
 					break;
 					
 				default:
 					break;
 			}
 		}
 	}
 
 	
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		
 		switch (keyCode) {
 		
 			case KeyEvent.KEYCODE_DPAD_RIGHT:
 				
 				if (viewPager.getCurrentItem() < (viewPager.getChildCount() - 1))
 					viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
 				
 				return true;
 				
 			case KeyEvent.KEYCODE_DPAD_LEFT:
 				
 				if (viewPager.getCurrentItem() > 0)
 					viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
 				
 				return true;
 				
 			default:
 				return super.onKeyUp(keyCode, event);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		getSupportMenuInflater().inflate(R.menu.menu_info, menu);
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		switch (item.getItemId()) {
         
 	        case android.R.id.home:
 	            
 	        	onBackPressed();
 	            
 	            return true;
 	            
 	        case R.id.menu_settings:
 	        	
 	        	Intent settings = new Intent(TrackerInfoActivity.this, TrackerPreferenceActivity.class);
 	    		startActivity(settings);
 	    		
 	    		return true;
 	    		
 	        case R.id.menu_delete:
 	        	
 	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 	        	builder.setTitle(R.string.dialog_delete_title);
 	        	builder.setMessage(R.string.dialog_delete_message);
 	        	
 	        	final TrackerDB db = new TrackerDB(this);
 	        	
 	        	builder.setNegativeButton(getString(R.string.dialog_btn_delete), new DialogInterface.OnClickListener() {
 	        		
 	                public void onClick(DialogInterface dialog, int id) {
 	                    
 	                	Intent intent = getIntent();
 	    	            int trackId = intent.getIntExtra("trackId", -1);
 	    	            
 	    	            if (trackId != -1) {
 	    	            
 	    	            	db.deleteTrack(trackId);
 	    	            	db.close();
 	    	            }
 	    	            
 	    	            dialog.cancel();
 	    	            
 	    	            onBackPressed();
 	                }
 	            });
 	        	
 	        	builder.setNeutralButton(getString(R.string.dialog_btn_cancel), new DialogInterface.OnClickListener() {
 	        		
 	                public void onClick(DialogInterface dialog, int id) {
 	                    
 	                	dialog.cancel();
 	                }
 	            });
 
 	        	AlertDialog dialog = builder.create();
 	        	
 	        	dialog.show();
 	        	
 	        	return true;
 	        	
 	        default:
 	        	return super.onMenuItemSelected(featureId, item);
 		}
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		
 		viewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {}
 	
 	public class TrackerInfoSectionsPagerAdapter extends FragmentPagerAdapter {
 
 	    public TrackerInfoSectionsPagerAdapter(FragmentManager fm) {
 	    	
 	        super(fm);
 	    }
 
 	    @Override
 	    public Fragment getItem(int i) {
 	    	
 			switch (i) {
 			
 			case 0:
 				return new TrackerMapInfoFragment();
 			case 1:
 				return new TrackerInfoFragment();
 			case 2:
 				return new TrackerGraphsInfoFragment();
 			default:
 				return new TrackerInfoFragment();	
 			}
 	    }
 
 	    @Override
 	    public int getCount() {
 	    	
 	        return 3;
 	    }
 
 	    @Override
 	    public CharSequence getPageTitle(int position) {
 	    	
 	        switch (position) {
 	        
 	            case 0: return getString(R.string.tab_route).toUpperCase();
 	            case 1: return getString(R.string.tab_info).toUpperCase();
 	            case 2: return getString(R.string.tab_graphs).toUpperCase();
 	        }
 	        
 	        return null;
 	    }
 	}
 }
