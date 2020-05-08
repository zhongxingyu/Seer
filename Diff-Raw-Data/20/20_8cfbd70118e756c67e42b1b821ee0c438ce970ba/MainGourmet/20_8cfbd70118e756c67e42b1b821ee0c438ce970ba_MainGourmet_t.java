 package com.gourmet6;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.sqlite.SQLiteException;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.AndroidRuntimeException;
 import android.util.Log;
 import android.view.Menu;
 
 public class MainGourmet extends Activity
 {
 	public static final int LOGIN = 0;
 	public static final int TOWN_LIST = 1;
 	public static final int RESTO_LIST = 2;
 	
	public static String currentTown;

 	@SuppressWarnings("unused")
 	private Location location = null;
 	public DBHandler dbHand;
 	private Gourmet g;
 	public static boolean isDone = false;
 	public static boolean showResto = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
 		{
 			
 			@Override
 			public void uncaughtException(Thread thread, Throwable ex)
 			{
 				Log.e("MainGourmet", Log.getStackTraceString(ex), ex);
 				ExceptionHandler.kill();
 				/*runOnUiThread(new Runnable()
 				{
 					
 					@Override
 					public void run()
 					{
 				        ExceptionHandler.showDialog(MainGourmet.this, false, stackTrace.toString());
 					}
 				});*/
 			}
 		});
 
 		/** LOCATION MANAGEMENT **/
 		/*
 		 * LocationManager locationManager = (LocationManager)
 		 * this.getSystemService(Context.LOCATION_SERVICE); LocationListener
 		 * locationListener = new LocationListener() { public void
 		 * onLocationChanged(Location location) { // Called when a new location
 		 * is found by the network location provider. MainGourmet.this.location
 		 * = location; }
 		 * 
 		 * public void onStatusChanged(String provider, int status, Bundle
 		 * extras) {
 		 * 
 		 * }
 		 * 
 		 * public void onProviderEnabled(String provider) {
 		 * 
 		 * }
 		 * 
 		 * public void onProviderDisabled(String provider) {
 		 * 
 		 * } };
 		 * locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
 		 * , 0, 0, locationListener);
 		 * //locationManager.removeUpdates(locationListener);
 		 */
 		dbHand = new DBHandler(this);
 		g = (Gourmet) getApplicationContext();
 	}
 
 	@Override
 	protected void onStart()
 	{
 		super.onStart();
 		if (showResto)
 		{
 			showResto = false;
 			isDone = true;
 			showRestaurants();
 		}
 		else
 		{
 			if (g.getClient() == null && !isDone)
 			{
 				isDone = true;
 				login();
 			}
 			else
 			{
 				if (!isDone)
 					showTowns();
 			}
 		}
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 	}
 
 	@Override
 	protected void onStop()
 	{
 		super.onStop();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		if (resultCode == RESULT_CANCELED)
 		{
 			switch (requestCode)
 			{
 				case LOGIN:
 					isDone = false;
 					finish();
 					break;
 				case TOWN_LIST:
 					if (g.getClient() != null)
 						exitDialog();
 					else
 						login();
 					break;
 				case RESTO_LIST:
 					showTowns();
 					break;
 				default:
 					throw new AndroidRuntimeException("No such request code.");
 			}
 			return;
 		}
 		switch (requestCode)
 		{
 			case LOGIN:
 				showTowns();
 				break;
 			case TOWN_LIST:
 				currentTown = data.getStringExtra("selection");
 				showRestaurants();
 				break;
 			case RESTO_LIST:
 				try
 				{
 					g.setRest(dbHand.getRestaurant(data.getStringExtra("selection")));
 				}
 				catch (SQLiteException e)
 				{
 					ExceptionHandler.caughtException(this, e);
 					ExceptionHandler.kill();
 				}
 				startActivity(new Intent(this, RestaurantActivity.class));
 				break;
 			default:
 				throw new AndroidRuntimeException("No such request code.");
 		}
 	}
 
 	public void login()
 	{
 		startActivityForResult(new Intent(this, LoginActivity.class), LOGIN);
 	}
 
 	public void showTowns()
 	{
 		startActivityForResult(new Intent(this, TownActivity.class), TOWN_LIST);
 	}
 
 	public void showRestaurants()
 	{
 		Intent intent = new Intent(this, RestaurantListActivity.class);
 		intent.putExtra("currentTown", currentTown);
 		startActivityForResult(intent, RESTO_LIST);
 		/*
 		setContentView(R.layout.lists);
 		ListView restoList = (ListView) findViewById(R.id.list);
 		ArrayAdapter<String> restoAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, restaurants);
 		restoList.setAdapter(restoAdapter);
 		restoList.setOnItemClickListener(new AdapterView.OnItemClickListener()
 		{
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id)
 			{
 				Button buttonClicked = (Button) parent
 						.getItemAtPosition(position);
 				makeRestaurant(buttonClicked.getText().toString());
 			}
 		});
 		*/
 	}
 	
 	public void exitDialog()
 	{
 		AlertDialog.Builder exit = new AlertDialog.Builder(this);
 		exit.setTitle("Logout");
 		exit.setMessage("You are about to log out. Are you sure ?");
 		exit.setPositiveButton("Yes", new DialogInterface.OnClickListener()
 		{
 			@Override
 			public void onClick(DialogInterface dialog, int which)
 			{
 				g.setClient(null);
 				login();
 			}
 		});
 		exit.setNegativeButton("No", new DialogInterface.OnClickListener()
 		{
 			@Override
 			public void onClick(DialogInterface dialog, int which)
 			{
 				isDone = true;
 				showTowns();
 			}
 		});
 		exit.show();
 	}
 }
