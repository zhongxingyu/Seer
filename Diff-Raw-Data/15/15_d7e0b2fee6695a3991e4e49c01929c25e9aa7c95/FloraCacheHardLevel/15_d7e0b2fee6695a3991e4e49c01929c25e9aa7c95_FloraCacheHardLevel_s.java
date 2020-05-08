 package cens.ucla.edu.budburst.floracaching;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import cens.ucla.edu.budburst.PBBHelpPage;
 import cens.ucla.edu.budburst.PBBMainPage;
 import cens.ucla.edu.budburst.PBBSync;
 import cens.ucla.edu.budburst.R;
 import cens.ucla.edu.budburst.adapter.MyListAdapterFloracache;
import cens.ucla.edu.budburst.artools.ARManager;
 import cens.ucla.edu.budburst.database.OneTimeDBHelper;
 import cens.ucla.edu.budburst.helper.HelperGpsHandler;
 import cens.ucla.edu.budburst.helper.HelperListItem;
 import cens.ucla.edu.budburst.helper.HelperPlantItem;
 import cens.ucla.edu.budburst.helper.HelperSettings;
 import cens.ucla.edu.budburst.helper.HelperSharedPreference;
 import cens.ucla.edu.budburst.helper.HelperValues;
 import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
 import cens.ucla.edu.budburst.utils.PBBItems;
 import cens.ucla.edu.budburst.utils.QuickCapture;
 
 import com.google.android.maps.GeoPoint;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FloraCacheHardLevel extends ListActivity {
 	
 	private LocationManager mLocManager = null;
 	private HelperGpsHandler gpsHandler;
 	private boolean mIsBound;
 	private boolean mFirstGps;
 	private double mLatitude 		= 0.0;
 	private double mLongitude 		= 0.0;
 	private int mNumSpecies;
 	private int mGroupID;
 	private int mIndex;
 	private int mImageID;
 	
 	private static int MAX_NUM_SHOWN = 4;
 	
 	private EditText mLatTxt;
 	private EditText mLonTxt;
 	private EditText mInfoTxt;
 	
 	private MyListAdapterFloracache mListapdater;
 	private ArrayList<HelperPlantItem> mListArr;
 	private HelperListItem mItem;
 	
 	private ArrayList<FloracacheItem> mPlantList = new ArrayList<FloracacheItem>();
 	
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		public void onServiceConnected(ComponentName className, IBinder binder) {
 			gpsHandler = ((HelperGpsHandler.GpsBinder) binder).getService();
 			//Toast.makeText(PBBMapMain.this, "Connected", Toast.LENGTH_SHORT).show();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			gpsHandler = null;
 		}
 	};
 	
 	private void doBindService() {
 		
 		Log.i("K", "BindService");
 		
 		bindService(new Intent(FloraCacheHardLevel.this, HelperGpsHandler.class), mConnection,
 				Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 	
 	private void doUnbindService() {
 		
 		Log.i("K", "UnBindService");
 		
 		if(mIsBound) {
 			if(mConnection != null) {
 				
 			}
 		
 			unbindService(mConnection);
 			mIsBound = false;
 		}
 	}
 
 	private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO Auto-generated method stub
 			Bundle extras = intent.getExtras();
 			
 			if(extras.getBoolean("signal")) {
 				mLatitude = extras.getDouble("latitude");
 				mLongitude = extras.getDouble("longitude");
 				
 				TextView infoTxt = (TextView)findViewById(R.id.gps_info);
 				infoTxt.setVisibility(View.GONE);
 				
 				getFloraLists();
 				
 			}
 			// if Gps signal is bad
 			else {
 				new AlertDialog.Builder(FloraCacheHardLevel.this)
 				.setTitle("Weak Gps Signal")
 				.setMessage("Cannot get Gps Signal, Make sure you are in the good connectivity area")
 				.setPositiveButton(getString(R.string.Button_back), new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 					}
 				})
 				.show();
 			}
 		}	
 	};
 	
 	private GeoPoint getPoint(double lat, double lon) {
 		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
 	}
 	
 	private float getClosestSpecies() {
 		
 		float minDist = Float.MAX_VALUE;
 		
 		for(int i = 0 ; i < mPlantList.size() ; i++) {
 			float distResult[] = new float[1];
 			Location.distanceBetween(mLatitude, mLongitude, mPlantList.get(i).getLatitude(), mPlantList.get(i).getLongitude(), distResult);
 			
 			if(distResult[0] < minDist) {
 				minDist = distResult[0];
 			}
 		}
 		return minDist;
 	}
 	
 	
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    Intent gIntent = getIntent();
 	    mGroupID = gIntent.getExtras().getInt("group_id");
 	    
 	    mListArr = new ArrayList<HelperPlantItem>();
 	    
 	    setTitleBar();
 	    checkGPS();
 	    // TODO Auto-generated method stub
 	}
 	
 	private void getFloraLists() {
 		HelperSharedPreference hPref = new HelperSharedPreference(this);
 		if(!hPref.getPreferenceBoolean("floracache")) {
 			Toast.makeText(FloraCacheHardLevel.this, "To download the list, Go 'Settings' page", Toast.LENGTH_SHORT).show();
 		}
 		else {
 			OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
 			mPlantList = oDBH.getFloracacheLists(FloraCacheHardLevel.this, HelperValues.FLORACACHE_HARD, mGroupID);
 			
 			mListArr = new ArrayList<HelperPlantItem>();
 			
 			for(int i = 0 ; i < mPlantList.size() ; i++) {
 				
 				float dist[] = new float[1];
 				Location.distanceBetween(mLatitude, mLongitude, mPlantList.get(i).getLatitude(), mPlantList.get(i).getLongitude(), dist);
 				
 				HelperPlantItem pi = new HelperPlantItem();
 				pi.setSpeciesID(mPlantList.get(i).getUserSpeciesID());
 				pi.setCategory(mPlantList.get(i).getUserSpeciesCategoryID());
 				pi.setSpeciesName(mPlantList.get(i).getScienceName());
 				pi.setCommonName(mPlantList.get(i).getCommonName());
 				pi.setFloracacheID(mPlantList.get(i).getFloracacheID());
 				pi.setDistance(dist[0]);
 				pi.setDescription(mPlantList.get(i).getFloracacheNotes());
 				pi.setUserName(mPlantList.get(i).getUserName());
 				
 				mListArr.add(pi);
 			}
 			
 			// sorting
 			Comparator<HelperPlantItem> compare = new Comparator<HelperPlantItem>() {
 
 				@Override
 				public int compare(HelperPlantItem obj1, HelperPlantItem obj2) {
 					// TODO Auto-generated method stub
 					return String.valueOf(obj1.getDistance()).compareToIgnoreCase(String.valueOf(obj2.getDistance()));
 				}
 			};
 			
 			Collections.sort(mListArr, compare);
 			
 			ArrayList<HelperPlantItem> newArr = new ArrayList<HelperPlantItem>();
 			
 			int count = 0;
 			for(HelperPlantItem item: mListArr) {
 				
 				if(count++ < MAX_NUM_SHOWN) {
 					try {
 						newArr.add(item.clone());
 					} catch (CloneNotSupportedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			mListapdater = new MyListAdapterFloracache(this, R.layout.floracache_item ,newArr);
 			ListView MyList = getListView();
 			MyList.setAdapter(mListapdater);	
 			
 			doUnbindService();
 		}
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id){
 		
 		mIndex = position;
 		
 		if(mListArr.get(mIndex).getCategory() == HelperValues.LOCAL_WHATSINVASIVE_LIST 
 				|| mListArr.get(mIndex).getCategory() == HelperValues.LOCAL_POISONOUS_LIST
 				|| mListArr.get(mIndex).getCategory() == HelperValues.LOCAL_THREATENED_ENDANGERED_LIST) {
 			OneTimeDBHelper oDBH = new OneTimeDBHelper(FloraCacheHardLevel.this);
 			mImageID = oDBH.getImageID(FloraCacheHardLevel.this, mListArr.get(mIndex).getSpeciesName(), mListArr.get(mIndex).getCategory());
 		}
 		
 		if(mListArr.get(mIndex).getDistance() < 6.0) {
 			new AlertDialog.Builder(FloraCacheHardLevel.this)
 			.setTitle(getString(R.string.PlantInfo_makeObs))
 			.setMessage(getString(R.string.Floracache_Easy_Success))
 			.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					/*
 					 * Move to QuickCapture
 					 */
 					Intent intent = new Intent(FloraCacheHardLevel.this, QuickCapture.class);
 					PBBItems pbbItem = new PBBItems();
 					pbbItem.setCommonName(mListArr.get(mIndex).getCommonName());
 					pbbItem.setScienceName(mListArr.get(mIndex).getSpeciesName());
 					pbbItem.setSpeciesID(mListArr.get(mIndex).getSpeciesID());
 					pbbItem.setProtocolID(mListArr.get(mIndex).getProtocolID());
 					pbbItem.setCategory(mListArr.get(mIndex).getCategory());
 					pbbItem.setFloracacheID(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
 					
 					intent.putExtra("pbbItem", pbbItem);
 					intent.putExtra("image_id", mImageID);
 					intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
 					
 					startActivity(intent);
 
 				}
 			})
 			.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					/*
 					 * Move to Getphenophase without a photo.
 					 */
 					Intent intent = new Intent(FloraCacheHardLevel.this, OneTimePhenophase.class);
 					PBBItems pbbItem = new PBBItems();
 					pbbItem.setCommonName(mListArr.get(mIndex).getCommonName());
 					pbbItem.setScienceName(mListArr.get(mIndex).getSpeciesName());
 					pbbItem.setSpeciesID(mListArr.get(mIndex).getSpeciesID());
 					pbbItem.setProtocolID(mListArr.get(mIndex).getProtocolID());
 					pbbItem.setCategory(mListArr.get(mIndex).getCategory());
 					pbbItem.setFloracacheID(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
 					pbbItem.setLocalImageName("");
 					
 					intent.putExtra("pbbItem", pbbItem);
 					intent.putExtra("image_id", mImageID);
 					intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
 					
 					startActivity(intent);
 				}
 			})
 			.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 				}
 			})
 			.show();
 		}
 		else {
 			Toast.makeText(FloraCacheHardLevel.this, "Not close enough. Dist: " + String.format("%5.2f", mListArr.get(mIndex).getDistance()) + "m", Toast.LENGTH_SHORT).show();	
 		}
 	}
 	
 	private void checkGPS() {
 		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		
 		Location lastLoc = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(lastLoc != null) {
 			mLatitude = lastLoc.getLatitude();
 			mLongitude = lastLoc.getLongitude();			
 		}
 		
 		IntentFilter inFilter = new IntentFilter(HelperGpsHandler.GPSHANDLERFILTER);
 		registerReceiver(gpsReceiver, inFilter);
 		
 		doBindService();
 	}
 	
 	
 	public void setTitleBar() {
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.floracachemidlevel);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
 		
 		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
 		v = (ViewGroup)v.getChildAt(0);
 		v.setPadding(0, 0, 0, 0);
 
 		TextView myTitleText = (TextView) findViewById(R.id.my_title);
 		myTitleText.setText(" " + getString(R.string.Floracache_Game));
 	}
 	
 	@Override
 	public void onDestroy() {
 		// when user finish this activity, turn off the gps
 		// if there's a overlay, should call disableCompass() explicitly
 		doUnbindService();
 		if(gpsReceiver != null) {
 			unregisterReceiver(gpsReceiver);
 		}
 		super.onDestroy();
 	}
 	
 	
 	/*
 	 * Menu option(non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		
 		menu.add(0, 1, 0, getString(R.string.Floracache_Hard_Refresh)).setIcon(android.R.drawable.ic_menu_rotate);
 			
 		return true;
 	}
 	
 	/*
 	 * Menu option selection handling(non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	public boolean onOptionsItemSelected(MenuItem item){
 
 		Intent intent;
 		switch(item.getItemId()){
 			case 1:
 				Toast.makeText(FloraCacheHardLevel.this, getString(R.string.Map_Getting_GPS_Signal), Toast.LENGTH_SHORT).show();
 				doBindService();
 				return true;					
 		}
 		return false;
 	}
 }
