 package com.charlesmadere.android.fishnspots;
 
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 
 import com.charlesmadere.android.fishnspots.models.SimpleLocation;
 
 
 /**
  * This class is the app's entry point.
  */
 public class MainActivity extends Activity implements
 	LocationListFragment.LocationListFragmentListeners,
 	SaveCurrentLocationFragment.SaveCurrentLocationListeners
 {
 
 
 	private LocationListFragment locationListFragment;
 	private SaveCurrentLocationFragment saveCurrentLocationFragment;
 
 
 
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_activity);
 
 		final FragmentManager fManager = getFragmentManager();
 		final FragmentTransaction fTransaction = fManager.beginTransaction();
 
 		if (savedInstanceState == null)
 		{
 			if (isDeviceLarge())
 			{
 				locationListFragment = (LocationListFragment) fManager.findFragmentById(R.id.main_activity_fragment_location_list_fragment);
 			}
 			else
 			{
 				locationListFragment = new LocationListFragment();
 				fTransaction.add(R.id.main_activity_container, locationListFragment);
 			}
 
 			fTransaction.commit();
 		}
 	}
 
 
 	@Override
 	public void onBackPressed()
 	{
 		super.onBackPressed();
 		refreshActionBar();
 	}
 
 
 
 
 	/**
 	 * Checks to see what size screen this device has. This method will return
 	 * true if the device has a large screen, and as such said device should be
 	 * using the dual pane, fragment based, layout stuff.
 	 * 
 	 * @return
 	 * Returns true if this device has a large screen.
 	 */
 	private boolean isDeviceLarge()
 	{
 		return findViewById(R.id.main_activity_container) == null;
 	}
 
 
 	private void refreshActionBar()
 	{
 		final ActionBar actionBar = getActionBar();
 
 		if (saveCurrentLocationFragment != null && saveCurrentLocationFragment.isVisible())
 		{
 			actionBar.setDisplayHomeAsUpEnabled(true);
 			actionBar.setTitle(R.string.save_current_location);
 		}
 		else
 		{
 			actionBar.setDisplayHomeAsUpEnabled(false);
 			actionBar.setTitle(R.string.fish_n_spots);
 		}
 	}
 
 
 
 
 	@Override
 	public void onClickSaveCurrentLocation()
 	{
 		final FragmentManager fManager = getFragmentManager();
 
 		if (locationListFragment == null)
 		{
 			if (isDeviceLarge())
 			{
 				locationListFragment = (LocationListFragment) fManager.findFragmentById(R.id.main_activity_fragment_location_list_fragment);
 			}
 			else
 			{
 				locationListFragment = (LocationListFragment) fManager.findFragmentById(R.id.main_activity_container);
 			}
 		}
 
 		saveCurrentLocationFragment = new SaveCurrentLocationFragment();
 
 		if (isDeviceLarge())
 		{
 			saveCurrentLocationFragment.show(fManager, null);
 		}
 		else
 		{
 			final FragmentTransaction fTransaction = fManager.beginTransaction();
 			fTransaction.hide(locationListFragment);
 			fTransaction.add(R.id.main_activity_container, saveCurrentLocationFragment);
 			fTransaction.addToBackStack(null);
 			fTransaction.commit();
 
 			fManager.executePendingTransactions();
 		}
 
 		refreshActionBar();
 	}
 
 
 	@Override
 	public void onLocationSave(final SimpleLocation simpleLocation)
 	{
		final FragmentManager fManager = getFragmentManager();
		fManager.popBackStack();

		final FragmentTransaction fTransaction = fManager.beginTransaction();
		fTransaction.remove(saveCurrentLocationFragment);
		fTransaction.commit();
 
		fManager.executePendingTransactions();

		refreshActionBar();
 	}
 
 
 }
