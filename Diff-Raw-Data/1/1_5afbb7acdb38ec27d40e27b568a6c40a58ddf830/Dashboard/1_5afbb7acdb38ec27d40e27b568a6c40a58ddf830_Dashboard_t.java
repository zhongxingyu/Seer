 package com.example.huntersandzombiesdemo;
 
 import android.content.Intent;
import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 public class Dashboard extends FragmentActivity {
 	private Button inventoryButton;
 	private Button scoreButton;
 	private Button duelButton;
     private GoogleMap googleMap;
     public final static String USER_NAME = "com.example.huntersandzombies.USERNAME";
     public final static String USER_MONEY = "com.example.huntersandzombies.MONEY";
     public final static String INVENTORY = "com.example.huntersandzombies.INVENTORY";
     public static String username;
     public static int money;
     public static String[] inventory;
 //    public final static 
     
     
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dashboard);
         setUpMapIfNeeded();
 		
 		scoreButton = (Button) findViewById(R.id.scoreButton);
 		duelButton = (Button) findViewById(R.id.duelButton);
 		inventoryButton = (Button) findViewById(R.id.inventoryButton);
 		
 		scoreButton.setOnClickListener(scoreHandler);
 		inventoryButton.setOnClickListener(inventoryHandler);
 		duelButton.setOnClickListener(duelHandler);
 
 	}
 	/*
 	 * on click listener for the scorebutton
 	 */
 	View.OnClickListener scoreHandler = new View.OnClickListener() {		
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			Bundle bundle = new Bundle();
 			bundle.putString(USER_NAME, username);
 			bundle.putInt(USER_MONEY, money);
 			bundle.putStringArray(INVENTORY, inventory);
 			Intent intent = new Intent(Dashboard.this, Score.class);
 			intent.putExtras(bundle);
 //			startActivityForResult(intent, 1); //add request code for each
 			startActivity(intent);
 		}
 	};
 	
 	/**
 	 * On click listener for the inventory button
 	 */
 	View.OnClickListener inventoryHandler = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 //			Intent intent = new Intent(this, ...intent..);
 //			startActivity(intent);
 			Bundle bundle = new Bundle();
 			bundle.putString(USER_NAME, username);
 			bundle.putInt(USER_MONEY, money);
 			bundle.putStringArray(INVENTORY, inventory);
 			//do nothing for now until inventory class is created
 //			Intent intent = new Intent(Dashboard.this, Score.class);//fix to inventory view
 //			intent.putExtras(bundle);
 //			startActivityForResult(intent, 2);
 		}
 	};
 	
 	/**
 	 * On click listener for the duel handler button
 	 */
 	View.OnClickListener duelHandler = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			Bundle bundle = new Bundle();
 			bundle.putString(USER_NAME, username);
 			bundle.putInt(USER_MONEY, money);
 			bundle.putStringArray(INVENTORY, inventory);
 			Intent intent = new Intent(Dashboard.this, Duel.class);
 			intent.putExtras(bundle);
 			startActivity(intent);
 		}
 	};
 	
 	/**
 	 * Basically need to store the data and do nothing more, so it can be passed to other screens
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
 		super.onActivityResult(requestCode, resultCode, intent);
         Bundle extras = intent.getExtras();
 		switch(requestCode){
 		case 1: //from score
 			break;
 		case 2: //from inventory
 			break;
 		case 3: //from duel
 			break;
 		}
 	}
 	
     @Override
     protected void onResume() {
         super.onResume();
         setUpMapIfNeeded();
     }
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.dashboard, menu);
 		return true;
 	}
 	
     /**
      * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
      * installed) and the map has not already been instantiated.. This will ensure that we only ever
      * call {@link #setUpMap()} once when {@link #googleMap} is not null.
      * <p>
      * If it isn't installed {@link SupportMapFragment} (and
      * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
      * install/update the Google Play services APK on their device.
      * <p>
      * A user can return to this FragmentActivity after following the prompt and correctly
      * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
      * completely destroyed during this process (it is likely that it would only be stopped or
      * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
      * {@link #onResume()} to guarantee that it will be called.
      */
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (googleMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             // Check if we were successful in obtaining the map.
             if (googleMap != null) {
                 setUpMap();
             }
         }
     }
 
     /**
      * This is where we can add markers or lines, add listeners or move the camera. In this case, we
      * just add a marker near Africa.
      * <p>
      * This should only be called once and when we are sure that {@link #googleMap} is not null.
      */
     private void setUpMap() {
     	googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 //    	if (googleMap.isMyLocationEnabled()) {
 //    		Location x = googleMap.getMyLocation();
 //    		googleMap.addMarker(
 //    				new MarkerOptions()
 //    				.position(new LatLng(x.getLatitude(), x.getLongitude()))
 //    				.title("Here you are!"));
 //            googleMap.addMarker(new MarkerOptions()
 //      	.position(new LatLng(42.36036686, -71.08679982))
 //	     	.title("Wohoo!"));
 //  	}
     	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.35848655, -71.09211361), 21.0f));
         googleMap.addMarker(new MarkerOptions()
         	.position(new LatLng(42.35848655, -71.09211361))
         	.title("Me!"));
         googleMap.addMarker(new MarkerOptions()
         	.position(new LatLng(42.35850253, -71.09212430))
         	.title("Zombie!"));
     }
 }
