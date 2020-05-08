 package com.darkrockstudios.apps.tminus;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 
 import com.darkrockstudios.apps.tminus.fragments.LaunchDetailFragment;
 import com.darkrockstudios.apps.tminus.fragments.LaunchListFragment;
 import com.darkrockstudios.apps.tminus.fragments.LaunchListFragment.Callbacks;
 import com.darkrockstudios.apps.tminus.fragments.LocationDetailFragment;
 import com.darkrockstudios.apps.tminus.fragments.RocketDetailFragment;
 import com.darkrockstudios.apps.tminus.launchlibrary.Launch;
 import com.darkrockstudios.apps.tminus.launchlibrary.Pad;
 import com.darkrockstudios.apps.tminus.launchlibrary.Rocket;
 
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 
 
 /**
  * An activity representing a list of Launches. This activity
  * has different presentations for handset and tablet-size devices. On
  * handsets, the activity presents a list of items, which when touched,
  * lead to a {@link LaunchDetailActivity} representing
  * item details. On tablets, the activity presents the list of items and
  * item details side-by-side using two vertical panes.
  * <p/>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link com.darkrockstudios.apps.tminus.fragments.LaunchListFragment} and the item details
  * (if present) is a {@link com.darkrockstudios.apps.tminus.fragments.LaunchDetailFragment}.
  * <p/>
  * This activity also implements the required
  * {@link com.darkrockstudios.apps.tminus.fragments.LaunchListFragment.Callbacks} interface
  * to listen for item selections.
  */
 public class LaunchListActivity extends NavigationDatabaseActivity
 		implements Callbacks, PullToRefreshProvider, ActionBar.OnNavigationListener
 {
 	private static final String TAG                            = LaunchListActivity.class.getSimpleName();
 	private static final String TAG_LAUNCH_LIST                = "LaunchList";
 	private static final String STATE_SELECTED_NAVIGATION_ITEM =
 			LaunchListActivity.class.getPackage() + ".STATE_SELECTED_NAVIGATION_ITEM";
 
 	/**
 	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
 	 * device.
 	 */
 	private boolean               m_twoPane;
 	private PullToRefreshAttacher m_pullToRefreshAttacher;
 
 	private boolean m_navigationSpinnerInitialized;
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
 		setContentView( R.layout.activity_common_list );
 
 		m_navigationSpinnerInitialized = false;
 
 		setupNavigationSpinner();
 
 		m_pullToRefreshAttacher = PullToRefreshAttacher.get( this );
 
 		setUpcomingLaunchesFragment();
 
 		initNavDrawer();
 	}
 
 	private void setUpcomingLaunchesFragment()
 	{
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		LaunchListFragment launchListFragment = LaunchListFragment.newInstance( false );
 		fragmentManager.beginTransaction().replace( R.id.COMMON_list_fragment_container, launchListFragment,
 		                                            TAG_LAUNCH_LIST ).commit();
 	}
 
 	private void setPreviousLaunchesFragment()
 	{
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		LaunchListFragment launchListFragment = LaunchListFragment.newInstance( true );
 		fragmentManager.beginTransaction().replace( R.id.COMMON_list_fragment_container, launchListFragment,
 		                                            TAG_LAUNCH_LIST ).commit();
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		if( findViewById( R.id.COMMON_detail_fragment_container ) != null )
 		{
 			// The detail container view will be present only in the
 			// large-screen layouts (res/values-large and
 			// res/values-sw600dp). If this view is present, then the
 			// activity should be in two-pane mode.
 			m_twoPane = true;
 
 			// In two-pane mode, list items should be given the
 			// 'activated' state when touched.
 			LaunchListFragment launchListFragment =
 					(LaunchListFragment) getSupportFragmentManager().findFragmentByTag( TAG_LAUNCH_LIST );
 			launchListFragment.setActivateOnItemClick( true );
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu )
 	{
 		final MenuInflater inflater = getMenuInflater();
 		inflater.inflate( R.menu.settings, menu );
 		inflater.inflate( R.menu.refresh, menu );
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( MenuItem item )
 	{
 		final boolean handled;
 
 		// Handle item selection
 		switch( item.getItemId() )
 		{
 			case R.id.action_refresh:
 				refreshLaunchList();
 				handled = true;
 				break;
 			case R.id.action_settings:
 			{
 				Intent intent = new Intent( this, SettingsActivity.class );
 				startActivity( intent );
 				handled = true;
 			}
 			break;
 			default:
 				handled = super.onOptionsItemSelected( item );
 		}
 
 		return handled;
 	}
 
 	/**
 	 * Callback method from {@link LaunchListFragment.Callbacks}
 	 * indicating that the item with the given ID was selected.
 	 */
 	@Override
 	public void onItemSelected( Launch launch )
 	{
 		if( m_twoPane )
 		{
 			// In two-pane mode, show the detail view in this activity by
 			// adding or replacing the detail fragment using a
 			// fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putSerializable( LaunchDetailFragment.ARG_ITEM_ID, launch.id );
 			LaunchDetailFragment fragment = new LaunchDetailFragment();
 			fragment.setArguments( arguments );
 			getSupportFragmentManager().beginTransaction()
 					.replace( R.id.COMMON_detail_fragment_container, fragment )
 					.commit();
 		}
 		else
 		{
 			// In single-pane mode, simply start the detail activity
 			// for the selected item ID.
 			Intent detailIntent = new Intent( this, LaunchDetailActivity.class );
 			detailIntent.putExtra( LaunchDetailFragment.ARG_ITEM_ID, launch.id );
 			startActivity( detailIntent );
 		}
 	}
 
 	@Override
 	public void onRestoreInstanceState( Bundle savedInstanceState )
 	{
 		final ActionBar actionBar = getActionBar();
 		if( savedInstanceState.containsKey( STATE_SELECTED_NAVIGATION_ITEM ) && actionBar != null )
 		{
 			actionBar.setSelectedNavigationItem( savedInstanceState.getInt( STATE_SELECTED_NAVIGATION_ITEM ) );
 			m_navigationSpinnerInitialized = true;
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState( Bundle outState )
 	{
 		final ActionBar actionBar = getActionBar();
 		if( actionBar != null )
 		{
 			outState.putInt( STATE_SELECTED_NAVIGATION_ITEM, actionBar.getSelectedNavigationIndex() );
 		}
 	}
 
 	private void setupNavigationSpinner()
 	{
 		ActionBar actionBar = getActionBar();
 		if( actionBar != null )
 		{
 			actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_LIST );
 
 			final String[] navigationValues = getResources().getStringArray( R.array.LAUNCHLIST_navigation_options );
 
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>( actionBar.getThemedContext(),
 			                                                         android.R.layout.simple_spinner_item, android.R.id.text1,
 			                                                         navigationValues );
 			adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
 
 			actionBar.setListNavigationCallbacks( adapter, this );
 		}
 		else
 		{
 			Log.w( TAG, "Failed to setup navigation spinner: Could not get Actionbar." );
 		}
 	}
 
 	private void refreshLaunchList()
 	{
 		LaunchListFragment launchListFragment = (LaunchListFragment) getSupportFragmentManager()
 				                                                             .findFragmentById( R.id.COMMON_list_fragment_container );
 		launchListFragment.refresh();
 	}
 
 	public void countDownClicked( View v )
 	{
 		LaunchDetailFragment launchDetailFragment = (LaunchDetailFragment) getSupportFragmentManager()
 				                                                                   .findFragmentById( R.id.COMMON_detail_fragment_container );
 		if( launchDetailFragment != null )
 		{
 			final int launchId = launchDetailFragment.getLaunchId();
 			if( launchId >= 0 )
 			{
 				Intent intent = new Intent( this, CountDownActivity.class );
 				intent.putExtra( CountDownActivity.ARG_ITEM_ID, launchId );
 				startActivity( intent );
 			}
 		}
 	}
 
 	public void rocketDetailsClicked( View v )
 	{
 		Rocket rocket = (Rocket) v.getTag();
 
 		RocketDetailFragment rocketDetailFragment = RocketDetailFragment.newInstance( rocket.id, true );
 		rocketDetailFragment.show( getSupportFragmentManager(), "dialog" );
 	}
 
 	public void locationDetailsClicked( View v )
 	{
 		Pad pad = (Pad) v.getTag();
 
 		LocationDetailFragment locationDetailFragment =
 				LocationDetailFragment.newInstance( pad.location.id, pad.id, true, true );
 		locationDetailFragment.show( getSupportFragmentManager(), "dialog" );
 	}
 
 	@Override
 	public PullToRefreshAttacher getPullToRefreshAttacher()
 	{
 		return m_pullToRefreshAttacher;
 	}
 
 	public void rocketImageClicked( View v )
 	{
 		LaunchDetailFragment fragment = (LaunchDetailFragment) getSupportFragmentManager()
 				                                                       .findFragmentById( R.id.COMMON_detail_fragment_container );
 
 		if( fragment != null )
 		{
 			fragment.zoomRocketImage();
 		}
 	}
 
 	@Override
 	public boolean onNavigationItemSelected( int itemPosition, long itemId )
 	{
 		final boolean handled;
 
 		if( m_navigationSpinnerInitialized )
 		{
 			switch( itemPosition )
 			{
 				case 0:
 					Log.d( TAG, "Upcoming selected" );
 					setUpcomingLaunchesFragment();
 					handled = true;
 					break;
 				case 1:
 					Log.d( TAG, "Previous selected" );
 					setPreviousLaunchesFragment();
 					handled = true;
 					break;
 				default:
 					handled = false;
 					break;
 			}
 		}
 		else
 		{
 			handled = true;
 			m_navigationSpinnerInitialized = true;
 		}
 
 		return handled;
 	}
 }
