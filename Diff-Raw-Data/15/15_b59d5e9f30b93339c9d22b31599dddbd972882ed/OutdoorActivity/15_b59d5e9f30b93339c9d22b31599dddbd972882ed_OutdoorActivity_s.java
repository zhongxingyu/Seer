 /**
  * 
  */
 package fr.utc.nf33.ins;
 
 import java.util.List;
 
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMapOptions;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 
 import fr.utc.nf33.ins.location.Building;
 import fr.utc.nf33.ins.location.CloseBuildingsService;
 import fr.utc.nf33.ins.location.CloseBuildingsService.LocalBinder;
 import fr.utc.nf33.ins.location.LocationHelper;
 import fr.utc.nf33.ins.location.LocationIntent;
 import fr.utc.nf33.ins.location.SnrService;
 
 // SPECIFICATION : MOD_010
 /**
  * 
  * @author
  * 
  */
 public final class OutdoorActivity extends FragmentActivity
     implements
       GpsDialogFragment.GpsDialogListener {
   //
   private static final float DEFAULT_ZOOM_LEVEL = 17.0F;
   //
   private static final GoogleMapOptions GOOGLE_MAP_OPTIONS = new GoogleMapOptions();
 
   // SPECIFICATION : MAP_020, MAP_030, MAP_040
   static {
     GOOGLE_MAP_OPTIONS.compassEnabled(false);
     GOOGLE_MAP_OPTIONS.mapType(GoogleMap.MAP_TYPE_NORMAL);
     GOOGLE_MAP_OPTIONS.rotateGesturesEnabled(true);
     GOOGLE_MAP_OPTIONS.tiltGesturesEnabled(true);
     GOOGLE_MAP_OPTIONS.zoomControlsEnabled(false);
     GOOGLE_MAP_OPTIONS.zoomGesturesEnabled(true);
   }
 
   //
   private CloseBuildingsService mCloseBuildingsService;
   //
   private boolean mCloseBuildingsServiceBound;
   //
   private ServiceConnection mCloseBuildingsServiceConnection;
   //
   private SupportMapFragment mMapFragment;
   //
   private BroadcastReceiver mNewCloseBuildingsReceiver;
   //
   private BroadcastReceiver mNewLocationReceiver;
   //
   private BroadcastReceiver mNewSnrReceiver;
   //
   private boolean mSnrServiceBound;
   //
   private ServiceConnection mSnrServiceConnection;
 
   // SPECIFICATION : POS_080
   /**
    * Called when the user clicks the Entry Points button.
    * 
    * @param view
    */
   public final void onButtonEntryPointsClick(View view) {
     startActivity(new Intent(this, EntryPointsActivity.class));
   }
 
   @Override
   protected final void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.activity_outdoor);
 
     // SPECIFICATION : MAP_010
     // Create a Google Map Fragment with desired options.
     mMapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
     FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
     fragmentTransaction.add(R.id.activity_outdoor_map, mMapFragment);
     fragmentTransaction.commit();
 
     // Start the SNR Service.
     Intent snrIntent = new Intent(this, SnrService.class);
     startService(snrIntent);
 
     // Start the Close Buildings Service.
     Intent closeBuildingsIntent = new Intent(this, CloseBuildingsService.class);
     startService(closeBuildingsIntent);
   }
 
   @Override
   public final boolean onCreateOptionsMenu(Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.main, menu);
     return true;
   }
 
   @Override
   protected final void onDestroy() {
     super.onDestroy();
 
     // Stop the Close Buildings Service.
     Intent closeBuildingsIntent = new Intent(this, CloseBuildingsService.class);
     stopService(closeBuildingsIntent);
 
     // Stop the SNR Service.
     Intent snrIntent = new Intent(this, SnrService.class);
     stopService(snrIntent);
   }
 
   @Override
   public final void onGpsDialogPositiveClick(DialogFragment dialog) {
     Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
     startActivity(settingsIntent);
   }
 
   @Override
   protected final void onStart() {
     super.onStart();
 
     // SPECIFICATION : MOD_020
     // Check whether the GPS provider is enabled.
     if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
         .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
       DialogFragment dialog = new GpsDialogFragment();
       dialog.show(getSupportFragmentManager(), GpsDialogFragment.NAME);
     }
 
     // SPECIFICATION : MAP_050, MAP_060
     // Setup the map.
     GoogleMap map = mMapFragment.getMap();
     map.setMyLocationEnabled(true);
 
     // Connect to the SNR Service.
     Intent snrIntent = new Intent(this, SnrService.class);
     mSnrServiceConnection = new ServiceConnection() {
       @Override
       public final void onServiceConnected(ComponentName name, IBinder service) {
         mSnrServiceBound = true;
       }
 
       @Override
       public final void onServiceDisconnected(ComponentName name) {
         mSnrServiceBound = false;
       }
     };
     bindService(snrIntent, mSnrServiceConnection, Context.BIND_AUTO_CREATE);
 
     // Connect to the Close Buildings Service.
     Intent closeBuildingsIntent = new Intent(this, CloseBuildingsService.class);
     mCloseBuildingsServiceConnection = new ServiceConnection() {
       @Override
       public final void onServiceConnected(ComponentName name, IBinder service) {
         // We've bound to CloseBuildingsService, cast the IBinder and get LocationService instance.
         mCloseBuildingsService = ((LocalBinder) service).getService();
         // SPECIFICATION : POS_010
         mMapFragment.getMap().setLocationSource(mCloseBuildingsService.getBestLocationProvider());
         mCloseBuildingsServiceBound = true;
       }
 
       @Override
       public final void onServiceDisconnected(ComponentName name) {
         mCloseBuildingsServiceBound = false;
       }
     };
     bindService(closeBuildingsIntent, mCloseBuildingsServiceConnection, Context.BIND_AUTO_CREATE);
 
     // Register receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
 
     // SPECIFICATION : POS_070, POS_090
     mNewCloseBuildingsReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         if (!mCloseBuildingsServiceBound) return;
 
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         if (buildings == null) return;
         int numberOfEntryPoints = 0;
         for (int i = 0; i < buildings.size(); ++i) {
           numberOfEntryPoints += buildings.get(i).getEntryPoints().size();
         }
         ((Button) OutdoorActivity.this.findViewById(R.id.activity_outdoor_button_entry_points))
             .setText(Integer.toString(numberOfEntryPoints));
       }
     };
     lbm.registerReceiver(mNewCloseBuildingsReceiver,
         LocationIntent.NewCloseBuildings.newIntentFilter());
 
     mNewLocationReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         double lat = intent.getDoubleExtra(LocationIntent.NewLocation.EXTRA_LATITUDE, 0);
         double lon = intent.getDoubleExtra(LocationIntent.NewLocation.EXTRA_LONGITUDE, 0);
         mMapFragment.getMap().animateCamera(
             CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), DEFAULT_ZOOM_LEVEL));
       }
     };
     lbm.registerReceiver(mNewLocationReceiver, LocationIntent.NewLocation.newIntentFilter());
 
     mNewSnrReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         if (!mCloseBuildingsServiceBound) return;
 
         float snr = intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
 
         // SPECIFICATION : TRS_030
         List<Building> buildings = mCloseBuildingsService.getCloseBuildings();
         switch (LocationHelper.shouldGoIndoor(snr, buildings)) {
           case ASK_USER:
             Intent epIntent = new Intent(OutdoorActivity.this, EntryPointsActivity.class);
             epIntent.putExtra(EntryPointsActivity.EXTRA_CHOOSE_ENTRY_POINT,
                 R.string.entry_points_choose);
             startActivity(epIntent);
             break;
           case NO:
             break;
           case YES:
             Intent indoorIntent = new Intent(OutdoorActivity.this, IndoorActivity.class);
             StringBuilder sb = new StringBuilder();
             Building building = buildings.get(0);
             sb.append(building.getName());
             sb.append("\n");
             sb.append(building.getEntryPoints().get(0).getName());
             indoorIntent
                 .putExtra(LocationIntent.NewCloseBuildings.EXTRA_ENTRY_POINT, sb.toString());
             startActivity(indoorIntent);
             break;
           default:
             break;
         }
       }
     };
     lbm.registerReceiver(mNewSnrReceiver, LocationIntent.NewSnr.newIntentFilter());
   }
 
   @Override
   protected final void onStop() {
     super.onStop();
 
     // Unregister receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
     lbm.unregisterReceiver(mNewCloseBuildingsReceiver);
     mNewCloseBuildingsReceiver = null;
     lbm.unregisterReceiver(mNewLocationReceiver);
     mNewLocationReceiver = null;
     lbm.unregisterReceiver(mNewSnrReceiver);
     mNewSnrReceiver = null;
 
     // Disconnect from the Close Buildings Service.
     if (mCloseBuildingsServiceBound) {
       unbindService(mCloseBuildingsServiceConnection);
       mCloseBuildingsService = null;
       mCloseBuildingsServiceBound = false;
     }
     mCloseBuildingsServiceConnection = null;
 
     // Disconnect from the SNR Service.
     if (mSnrServiceBound) {
       unbindService(mSnrServiceConnection);
       mSnrServiceBound = false;
     }
     mSnrServiceConnection = null;
   }
 }
