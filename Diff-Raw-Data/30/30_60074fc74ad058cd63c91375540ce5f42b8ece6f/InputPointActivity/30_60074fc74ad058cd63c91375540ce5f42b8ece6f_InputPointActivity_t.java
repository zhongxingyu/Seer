 /******************************************************************************
  * Project:  NextGIS mobile
  * Purpose:  Mobile GIS for Android.
  * Author:   Baryshnikov Dmitriy (aka Bishop), polimax@mail.ru
  ******************************************************************************
 *   Copyright (C) 2012-2013 NextGIS
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ****************************************************************************/
 package com.nextgis.mobile;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.osmdroid.util.GeoPoint;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public class InputPointActivity extends SherlockFragmentActivity {
 	private static final int NUM_ITEMS = 4;
 	
 	private FragmentRollAdapter m_Adapter;
     private ViewPager m_Pager;
 	
     protected Location m_CurrentLocation;
     protected String m_sCat, m_sSubCat;
     protected float m_fAzimuth;
     protected float m_fDist;
     protected String m_sNote;
     
     protected ArrayList <String> image_lst = new ArrayList<String>(255);
     protected ArrayList <Double> image_rotation = new ArrayList<Double>(2000);
 	
     protected static final String CSV_CHAR = ";"; 
     
     protected static DescriptionFragment descriptfrag;
 	protected static PositionFragment positionfrag;
 	protected static NoteFragment notefrag;
 	protected static CameraFragment camfrag; 
 	
 	private final static int MENU_ADD = 0;
 	private final static int MENU_CANCEL = 1;
 	public final static int MENU_SETTINGS = 4;
 	public final static int MENU_ABOUT = 5;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         
         //get location from calling class
         Bundle extras = getIntent().getExtras();  
         if(extras != null)
         	m_CurrentLocation = (Location)extras.get(MainActivity.LOACTION_HINT);
 
         setContentView(R.layout.input_point);
 
        // setup action bar for tabs
         final ActionBar actionBar = getSupportActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setHomeButtonEnabled(false);
         
         m_Adapter = new FragmentRollAdapter(getSupportFragmentManager());
     	m_Adapter.setActionBar(actionBar);
         m_Pager = (ViewPager)findViewById(R.id.pager);
         m_Pager.setAdapter(m_Adapter);
         
         m_Pager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			public void onPageScrollStateChanged(int arg0) {
 				}
 
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				}
 
 			public void onPageSelected(int arg0) {
 				Log.d("ViewPager", "onPageSelected: " + arg0);
 				
 				if(descriptfrag != null)
 					descriptfrag.onStoreValues();
 				if(positionfrag != null)
 					positionfrag.onStoreValues();
 				if(camfrag != null)
 					camfrag.onStoreValues();
 				if(notefrag != null)
 					notefrag.onStoreValues();
 
 				actionBar.getTabAt(arg0).select();	
 			}
         } );        
         
         
         Tab tab = actionBar.newTab()
                 .setText(R.string.tabs_description_tab)
                 .setTabListener(new TabListener<SherlockFragment>(0 + "", m_Pager));
         actionBar.addTab(tab);
 
         tab = actionBar.newTab()
             .setText(R.string.tabs_position_tab)
             .setTabListener(new TabListener<SherlockFragment>(1 + "", m_Pager));
         actionBar.addTab(tab); 
         
         tab = actionBar.newTab()
             .setText(R.string.tabs_camera_tab)
             .setTabListener(new TabListener<SherlockFragment>(2 + "", m_Pager));
         actionBar.addTab(tab);       
         
         tab = actionBar.newTab()
             .setText(R.string.tabs_note_tab)
             .setTabListener(new TabListener<SherlockFragment>(3 + "", m_Pager));
         actionBar.addTab(tab);       
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
        
		if(descriptfrag != null)
			descriptfrag.onStoreValues();
		if(positionfrag != null)
			positionfrag.onStoreValues();
		if(camfrag != null)
			camfrag.onStoreValues();
		if(notefrag != null)
			notefrag.onStoreValues();
        
        
         outState.putString("cat", m_sCat);
         outState.putString("subcat", m_sSubCat);
         outState.putFloat("az", m_fAzimuth);
         outState.putFloat("dist", m_fDist);
         outState.putString("note", m_sNote);
         outState.putStringArrayList("photos", image_lst);
         int nAzArraySize = image_rotation.size();
         double[] adfAz = new double [nAzArraySize];
         for(int i = 0; i < nAzArraySize; i++)
         	adfAz[i] = image_rotation.get(i);
         
         outState.putDoubleArray("photos_az", adfAz);
     } 
     
     @Override
     protected void onRestoreInstanceState  (Bundle outState) {
     	 super.onRestoreInstanceState(outState);
     	 
     	 m_sCat = outState.getString("cat");
     	 m_sSubCat = outState.getString("subcat");
     	 m_fAzimuth = outState.getFloat("az");
     	 m_fDist = outState.getFloat("dist");
     	 m_sNote = outState.getString("note"); 
     	 image_lst = outState.getStringArrayList("photos");
     	 double[] adfAz = outState.getDoubleArray("photos_az");
     	 for(int i = 0; i < adfAz.length; i++)
     	 {
     		 image_rotation.add(adfAz[i]);
     	 }
     } 
     
      public static class TabListener<T extends SherlockFragment> implements ActionBar.TabListener {
     	 private final String m_Tag;
     	 private ViewPager m_Pager;
     	 
 	   	 public TabListener(String tag, ViewPager pager) {
    	        m_Tag = tag;
    	        m_Pager = pager;
 	   	 }
    	    
 
 		@Override
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			int nTag = Integer.parseInt(m_Tag);
 			m_Pager.setCurrentItem(nTag);
 		}
 
 		@Override
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 			// TODO Auto-generated method stub
 			
 		}
    }
    
    public static class FragmentRollAdapter extends FragmentPagerAdapter {
 	   ActionBar m_ActionBar;
    	
        public FragmentRollAdapter(FragmentManager fm) {
            super(fm);
        }
 
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
 
        public void setActionBar( ActionBar bar ) {
        	m_ActionBar = bar;
        	}
 
 		@Override
 		public SherlockFragment getItem(int arg0) {
 			switch(arg0)
 			{
 			case 0:
 				descriptfrag = new DescriptionFragment();
 				return (SherlockFragment) descriptfrag;//
 			case 1:
 				positionfrag = new PositionFragment();
 				return (SherlockFragment) positionfrag;//
 			case 2:
 				camfrag = new CameraFragment();
 				return (SherlockFragment) camfrag;//
 			case 3:
 				notefrag = new NoteFragment();
 				return (SherlockFragment) notefrag;//
 			default:
 				return null;
 			}
 		}
     }    
    
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getSupportMenuInflater().inflate(R.menu.main, menu);
        menu.add(Menu.NONE, MENU_ADD, Menu.NONE, R.string.sMark)
        .setIcon(R.drawable.ic_navigation_accept)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);//MenuItem.SHOW_AS_ACTION_WITH_TEXT | 
        menu.add(Menu.NONE, MENU_CANCEL, Menu.NONE, R.string.sCancel)
        .setIcon(R.drawable.ic_navigation_cancel)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.sSettings)
        .setIcon(R.drawable.ic_action_settings)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);		
        menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.sAbout)
        .setIcon(R.drawable.ic_action_about)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);	
       return true;
 	}
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
 	   switch (item.getItemId()) {
        case android.R.id.home:
            return false;
        case MENU_SETTINGS:
            // app icon in action bar clicked; go home
            Intent intentSet = new Intent(this, PreferencesActivity.class);
            intentSet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentSet);
            return true;
        case MENU_ABOUT:
            Intent intentAbout = new Intent(this, AboutActivity.class);
            intentAbout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentAbout);
            return true;	  
        case MENU_ADD:
     	onFinish();
        	return true;
        case MENU_CANCEL:
     	finish();
        	return true;
 	   }
 	   return super.onOptionsItemSelected(item);
 	}   
    
     
     public void onFinish() {
    	
		if(descriptfrag != null)
			descriptfrag.onStoreValues();
		if(positionfrag != null)
			positionfrag.onStoreValues();
		if(camfrag != null)
			camfrag.onStoreValues();
		if(notefrag != null)
			notefrag.onStoreValues();
		
     	//add point to the file
     	File file = new File(getExternalFilesDir(null), "points.csv");
     	boolean bExist = file.exists();
     	try {  
             FileOutputStream os = new FileOutputStream(file, true);
             PrintWriter pw = new PrintWriter(os);
             if(!bExist)
             {
             	pw.println("date_time;lat;lon;acc;error_est;h;dir;src;speed;gps_t;cat;subcut;az;len;desc;photos;photos_az");
             }
             
             final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             final String CE = prefs.getString(PreferencesActivity.KEY_PREF_ACCURATE_CE, "None");
             
             double dfLat = 0,dfLon = 0, dfAcc = 0, dfAlt = 0, dfBearing = 0, dfSpeed = 0;
             String sProv = "";
             long nTime = 0;
             if(m_CurrentLocation != null){
             	dfLat = m_CurrentLocation.getLatitude();
             	dfLon = m_CurrentLocation.getLongitude();
             	dfAcc = m_CurrentLocation.getAccuracy();
             	dfAlt = m_CurrentLocation.getAltitude();
             	dfBearing = m_CurrentLocation.getBearing();
             	dfSpeed = m_CurrentLocation.getSpeed();
             	sProv = m_CurrentLocation.getProvider();
             	nTime = m_CurrentLocation.getTime();
             }
             pw.println(
             		java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + CSV_CHAR + 	//0
             		dfLat + CSV_CHAR + 														//1
             		dfLon + CSV_CHAR + 														//2
             		dfAcc + CSV_CHAR + 	
             		CE + CSV_CHAR +
             		dfAlt + CSV_CHAR + 														//4
             		dfBearing + CSV_CHAR + 														//5
             		sProv + CSV_CHAR + 														//6
             		dfSpeed + CSV_CHAR + 															//7
             		nTime + CSV_CHAR + 															//8
             		m_sCat + CSV_CHAR + 																				//9
             		m_sSubCat + CSV_CHAR + 																				//10
         		    m_fAzimuth + CSV_CHAR + 																			//11
             		m_fDist + CSV_CHAR + 																				//12
             		m_sNote + CSV_CHAR +  																				//13
             		image_lst.toString() + CSV_CHAR +
             		image_rotation.toString()
             		);
             
             pw.flush();
             pw.close();
             os.close();
             
             MediaScannerConnection.scanFile(this,
                     new String[] { file.toString() }, null,
                     new MediaScannerConnection.OnScanCompletedListener() {
 				@Override
                 public void onScanCompleted(String path, Uri uri) {
                     Log.i("ExternalStorage", "Scanned " + path + ":");
                     Log.i("ExternalStorage", "-> uri=" + uri);
                 }
             });
         } catch (IOException e) {
             Log.w("ExternalStorage", "Error writing " + file, e);
         } 
     	//add point to the map
     	Toast.makeText(InputPointActivity.this, R.string.input_poi_added, Toast.LENGTH_SHORT).show();    	
 
     	finish();
     }
     
     public void SetDescription(String sCat, String sSubCat) {
     	m_sCat = sCat;
     	m_sSubCat = sSubCat;
     }
     
     public void SetDistance(float fDist) {
     	m_fDist = fDist;
     }
     
     public void SetAzimuth(float fAz) {
     	m_fAzimuth = fAz;
     }
     
     public void SetNotes(String sNotes) {
     	m_sNote = sNotes;
     }
     
     public Location getLocation() {
     	return m_CurrentLocation;
     }
     
     public void AddImage(String sImageName, double dfRotation){
     	image_lst.add(sImageName);
     	image_rotation.add(dfRotation);
     }
 }
