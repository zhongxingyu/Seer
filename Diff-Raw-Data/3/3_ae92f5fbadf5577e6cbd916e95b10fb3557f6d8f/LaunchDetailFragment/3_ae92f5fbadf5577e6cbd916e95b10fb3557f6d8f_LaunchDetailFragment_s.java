 package com.darkrockstudios.apps.tminus;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 
 import com.darkrockstudios.apps.tminus.R.id;
 import com.darkrockstudios.apps.tminus.launchlibrary.Launch;
 import com.darkrockstudios.apps.tminus.loaders.LaunchLoader;
 import com.darkrockstudios.apps.tminus.misc.Preferences;
 import com.darkrockstudios.apps.tminus.misc.Utilities;
 
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 /**
  * A fragment representing a single Launch detail screen.
  * This fragment is either contained in a {@link LaunchListActivity}
  * in two-pane mode (on tablets) or a {@link LaunchDetailActivity}
  * on handsets.
  */
 public class LaunchDetailFragment extends Fragment implements LaunchLoader.Listener
 {
 	public static final  String TAG                         = LaunchDetailFragment.class.getSimpleName();
 	public static final  String ARG_ITEM_ID                 = "item_id";
 	private static final long   DISPLAY_COUNTDOWN_THRESHOLD = TimeUnit.DAYS.toMillis( 2 );
 	private ShareActionProvider m_shareActionProvider;
 	private Launch              m_launchItem;
 	private TimeReceiver        m_timeReceiver;
 	private View m_contentView;
 	private View m_progressBar;
 	private View m_countDownContainer;
 	private View m_rocketDetailButton;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public LaunchDetailFragment()
 	{
 	}
 
 	public static LaunchDetailFragment newInstance( int launchId )
 	{
 		Bundle arguments = new Bundle();
 		arguments.putInt( LaunchDetailFragment.ARG_ITEM_ID, launchId );
 		LaunchDetailFragment fragment = new LaunchDetailFragment();
 		fragment.setArguments( arguments );
 		return fragment;
 	}
 
 	@Override
 	public void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 
 		setHasOptionsMenu( true );
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		handleCountDownContainer();
 	}
 
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 
 		m_timeReceiver = new TimeReceiver();
 		IntentFilter intentFilter = new IntentFilter( Intent.ACTION_TIME_TICK );
 
 		Activity activity = getActivity();
 		activity.registerReceiver( m_timeReceiver, intentFilter );
 	}
 
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 
 		Activity activity = getActivity();
 		activity.unregisterReceiver( m_timeReceiver );
 
 		m_timeReceiver = null;
 	}
 
 	@Override
 	public void onAttach( Activity activity )
 	{
 		super.onAttach( activity );
 	}
 
 	@Override
 	public void onDetach()
 	{
 		super.onDetach();
 	}
 
 	@Override
 	public View onCreateView( LayoutInflater inflater, ViewGroup container,
 	                          Bundle savedInstanceState )
 	{
 		View rootView = inflater.inflate( R.layout.fragment_launch_detail, container, false );
 
 		if( rootView != null )
 		{
 			m_contentView = rootView.findViewById( R.id.content_view );
 			m_progressBar = rootView.findViewById( R.id.progressBar );
 			m_countDownContainer = rootView.findViewById( R.id.LAUNCHDETAIL_imminent_launch_container );
 			m_countDownContainer.setVisibility( View.GONE );
 			m_rocketDetailButton = rootView.findViewById( id.LAUNCHDETAIL_rocket_detail_button );
 
 			loadLaunch();
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
 	{
 		inflater.inflate( R.menu.launch_detail, menu );
 
 		MenuItem item = menu.findItem( R.id.menu_item_share );
 		if( item != null )
 		{
 			m_shareActionProvider = (ShareActionProvider)item.getActionProvider();
 		}
 		updateShareIntent();
 
 		super.onCreateOptionsMenu( menu, inflater );
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( MenuItem item )
 	{
 		final boolean handled;
 
 		switch( item.getItemId() )
 		{
 			case id.menu_item_add_to_calendar:
 				addLaunchToCalendar();
 				handled = true;
 				break;
 			default:
 				handled = super.onOptionsItemSelected( item );
 		}
 
 		return handled;
 	}
 
 	private void addLaunchToCalendar()
 	{
 		if( m_launchItem != null )
 		{
 			final String title = getString( R.string.CALENDAR_event_title, m_launchItem.name );
 			final String description = getString( R.string.CALENDAR_event_description, m_launchItem.rocket.name, m_launchItem.rocket.configuration );
 
 			Intent intent = new Intent( Intent.ACTION_INSERT )
 					                .setData( Events.CONTENT_URI )
 					                .putExtra( CalendarContract.EXTRA_EVENT_BEGIN_TIME, m_launchItem.net.getTime() )
 					                .putExtra( CalendarContract.EXTRA_EVENT_END_TIME, m_launchItem.windowend.getTime() )
 					                .putExtra( Events.TITLE, title )
 					                .putExtra( Events.DESCRIPTION, description )
 					                .putExtra( Events.EVENT_LOCATION, m_launchItem.location.name )
 					                .putExtra( Events.AVAILABILITY, Events.AVAILABILITY_BUSY );
 			startActivity( intent );
 		}
 	}
 
 	public int getLaunchId()
 	{
 		int launchId = -1;
 
 		final Bundle arguments = getArguments();
 		if( arguments != null && arguments.containsKey( ARG_ITEM_ID ) )
 		{
 			launchId = arguments.getInt( ARG_ITEM_ID );
 		}
 
 		return launchId;
 	}
 
 	private void showContent()
 	{
 		if( m_contentView != null && m_progressBar != null )
 		{
 			m_contentView.setVisibility( View.VISIBLE );
 			m_progressBar.setVisibility( View.GONE );
 		}
 	}
 
 	private void showLoading()
 	{
 		if( m_contentView != null && m_progressBar != null )
 		{
 			m_contentView.setVisibility( View.GONE );
 			m_progressBar.setVisibility( View.VISIBLE );
 		}
 	}
 
 	private void updateViews()
 	{
 		if( m_launchItem != null )
 		{
 			final View rootView = getView();
 
 			final TextView name = (TextView)rootView.findViewById( R.id.LAUNCHDETAIL_mission_name );
 			name.setText( m_launchItem.name );
 
 			final TextView description = (TextView)rootView.findViewById( R.id.LAUNCHDETAIL_mission_description );
 			description.setText( m_launchItem.mission.description );
 
 			final TextView status = (TextView)rootView.findViewById( R.id.LAUNCHDETAIL_status );
 			status.setText( Utilities.getStatusText( m_launchItem, rootView.getContext() ) );
 
 			final TextView launchWindow = (TextView)rootView.findViewById( R.id.LAUNCHDETAIL_launch_window );
 			launchWindow.setText( Utilities.getDateText( m_launchItem.windowstart ) );
 
 			final TextView location = (TextView)rootView.findViewById( R.id.LAUNCHDETAIL_location );
 			location.setText( m_launchItem.location.name );
 
 			final TextView windowLength = (TextView)rootView.findViewById( R.id.LAUNCHDETAIL_window_length );
 			final long windowLengthMs = m_launchItem.windowend.getTime() - m_launchItem.windowstart.getTime();
 			windowLength.setText( Utilities.getFormattedTime( windowLengthMs ) );
 
 			updateTimeViews();
 			handleCountDownContainer();
 		}
 	}
 
 	public void updateTimeViews()
 	{
 		if( m_launchItem != null )
 		{
 			final View rootView = getView();
 
 			final TextView timeRemaining = (TextView)rootView.findViewById( R.id.LAUNCHDETAIL_time_remaining );
 			final Date now = new Date();
 
 			final long totalMsLeft = m_launchItem.windowstart.getTime() - now.getTime();
 			timeRemaining.setText( Utilities.getFormattedTime( totalMsLeft ) );
 
 			handleCountDownContainer();
 		}
 	}
 
 	public void loadLaunch()
 	{
 		final int launchId = getLaunchId();
 		if( launchId >= 0 )
 		{
 			showLoading();
 
 			LaunchLoader loader = new LaunchLoader( getActivity(), this );
 			loader.execute( launchId );
 		}
 	}
 
 	private void handleCountDownContainer()
 	{
 		boolean alwaysShow = false;
 
 		final Activity activity = getActivity();
 		if( activity != null )
 		{
 			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( activity );
 			alwaysShow = preferences.getBoolean( Preferences.KEY_ALWAYS_SHOW_COUNT_DOWN, false );
 		}
 
 		if( m_launchItem != null )
 		{
 			final Date thresholdDate = new Date( m_launchItem.net.getTime() - DISPLAY_COUNTDOWN_THRESHOLD );
 			if( thresholdDate.before( new Date() ) || alwaysShow )
 			{
 				m_countDownContainer.setVisibility( View.VISIBLE );
 			}
 			else
 			{
 				m_countDownContainer.setVisibility( View.GONE );
 			}
 		}
 	}
 
 	private void updateShareIntent()
 	{
 		if( m_launchItem != null && m_shareActionProvider != null )
 		{
 			Intent intent = new Intent( android.content.Intent.ACTION_SEND );
 			intent.setType( "text/plain" );
 			intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET );
 
 			// Add data to the intent, the receiving app will decide what to do with it.
 			intent.putExtra( Intent.EXTRA_SUBJECT, "Upcoming Space Launch: " + m_launchItem.name );
 			intent.putExtra( Intent.EXTRA_TEXT, generateShareBody() );
 
 			m_shareActionProvider.setShareIntent( intent );
 		}
 	}
 
 	private String generateShareBody()
 	{
 		String body = "";
 
 		if( m_launchItem != null )
 		{
 			body += m_launchItem.mission.description + "\n\n";
 			body += "Location: " + m_launchItem.location.name + "\n\n";
 			body += "Expected Launch Time: " + m_launchItem.net + "\n\n";
 		}
 
 		return body;
 	}
 
 	@Override
 	public void launchLoaded( Launch launch )
 	{
 		m_launchItem = launch;
 		m_rocketDetailButton.setTag( m_launchItem.rocket );
 
 		updateViews();
 		updateShareIntent();
 		showContent();
 	}
 
 	private class TimeReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive( Context context, Intent intent )
 		{
 			updateTimeViews();
 		}
 	}
 }
