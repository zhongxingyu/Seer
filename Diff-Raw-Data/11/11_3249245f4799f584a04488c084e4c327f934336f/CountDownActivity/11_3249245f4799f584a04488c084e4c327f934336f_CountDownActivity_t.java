 package com.darkrockstudios.apps.tminus;
 
 
 import android.animation.ObjectAnimator;
 import android.animation.ValueAnimator;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 import com.darkrockstudios.apps.tminus.R.id;
 import com.darkrockstudios.apps.tminus.launchlibrary.Launch;
 import com.darkrockstudios.apps.tminus.loaders.LaunchLoader;
 import com.darkrockstudios.apps.tminus.misc.Preferences;
 import com.darkrockstudios.apps.tminus.misc.Utilities;
 
 import java.text.DecimalFormat;
 import java.util.concurrent.TimeUnit;
 
 public class CountDownActivity extends Activity implements LaunchLoader.Listener
 {
 	public static final  String ARG_ITEM_ID    = "item_id";
 	private static final String TAG            = CountDownActivity.class.getSimpleName();
 	private static final String FONT_PATH      = "fonts/digital_7_mono.ttf";
 	private static final long   INTERVAL_IN_MS = 10;
 	private long      m_endTime;
 	private boolean   m_launched;
 	private TextView  m_timerView;
 	private TextView  m_statusView;
 	private Handler   m_handler;
 	private TickTimer m_timeTicker;
 	private Launch    m_launch;
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
 		getWindow().addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED );
 		getWindow().addFlags( WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD );
 
 		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
 		if( preferences.getBoolean( Preferences.KEY_FULLSCREEN_COUNT_DOWN, true ) )
 		{
 			getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
 			getActionBar().hide();
 		}
 
 		setContentView( R.layout.activity_count_down );
 
 		m_handler = new Handler();
 		m_timeTicker = new TickTimer();
 
 		final Typeface typeface = Typeface.createFromAsset( getAssets(), FONT_PATH );
 		m_timerView = (TextView)findViewById( R.id.COUNTDOWN_timer );
 		m_timerView.setTypeface( typeface );
 
 		m_statusView = (TextView)findViewById( id.COUNTDOWN_launch_status );
 		m_statusView.setTypeface( typeface );
 
 		loadLaunch();
 	}
 
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 
 		if( m_launch != null )
 		{
			updateLaunched();
 			updateTimer();
 			startTimer();
 		}
 	}
 
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 
 		stopTimer();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( MenuItem item )
 	{
 		final boolean handled;
 		// Handle item selection
 		switch( item.getItemId() )
 		{
 			case android.R.id.home:
 				NavUtils.navigateUpTo( this, new Intent( this, LaunchListActivity.class ) );
 				handled = true;
 				break;
 			case id.action_settings:
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
 
	private void updateLaunched()
	{
		final long millis = m_endTime - System.currentTimeMillis();
		if( millis < 0 )
		{
			m_launched = true;
		}
	}

 	public void screenTouched( View view )
 	{
 		WindowManager.LayoutParams attrs = getWindow().getAttributes();
 
 		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
 
 		ActionBar actionBar = getActionBar();
 		if( actionBar != null )
 		{
 			if( actionBar.isShowing() )
 			{
 				actionBar.hide();
 				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
 				preferences.edit().putBoolean( Preferences.KEY_FULLSCREEN_COUNT_DOWN, true ).apply();
 			}
 			else
 			{
 				actionBar.show();
 				attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
 				preferences.edit().putBoolean( Preferences.KEY_FULLSCREEN_COUNT_DOWN, false ).apply();
 			}
 		}
 
 		getWindow().setAttributes( attrs );
 	}
 
 	private void loadLaunch()
 	{
 		final Intent intent = getIntent();
 		if( intent != null )
 		{
 			final int launchId = intent.getIntExtra( ARG_ITEM_ID, -1 );
 			if( launchId > 0 )
 			{
 				LaunchLoader launchLoader = new LaunchLoader( this, this );
 				launchLoader.execute( launchId );
 			}
 		}
 	}
 
 	private void updateStatus()
 	{
 		String status = Utilities.getStatusText( m_launch, this );
 
 		final String statusText = String.format( getString( R.string.COUNTDOWN_launch_status ), status );
 		m_statusView.setText( statusText );
 	}
 
 	private void updateTimer()
 	{
 		final long millis = m_endTime - System.currentTimeMillis();
 
 		final long hr = TimeUnit.MILLISECONDS.toHours( millis );
 
 		final long min = TimeUnit.MILLISECONDS.toMinutes( millis - TimeUnit.HOURS
 		                                                                   .toMillis( hr ) );
 
 		final long sec = TimeUnit.MILLISECONDS.toSeconds( millis - TimeUnit.HOURS
 		                                                                   .toMillis( hr ) - TimeUnit.MINUTES
 		                                                                                             .toMillis( min ) );
 
 		final long centisec = (millis - TimeUnit.HOURS
 		                                        .toMillis( hr ) - TimeUnit.MINUTES
 		                                                                  .toMillis( min ) - TimeUnit.SECONDS
 		                                                                                             .toMillis( sec )) / 10;
 
 		DecimalFormat twoDigit = new DecimalFormat( "00" );
 		twoDigit.setNegativePrefix( "" );
 
 		char sign = '-';
 		if( millis < 0 )
 		{
 			sign = '+';
 
 			if( !m_launched )
 			{
 				m_launched = true;
 				blinkTimer();
 			}
 		}
 
 		m_timerView
 				.setText( sign + twoDigit.format( hr ) + ":" + twoDigit.format( min ) + ":" + twoDigit.format( sec ) + ":" + twoDigit.format( centisec ) );
 	}
 
 	private void blinkTimer()
 	{
 		ValueAnimator blinkAnim = ObjectAnimator.ofFloat( m_timerView, "alpha", 1.0f, 0.0f, 1.0f );
 		blinkAnim.setDuration( 400 );
 		blinkAnim.setRepeatCount( 24 );
 		blinkAnim.start();
 	}
 
 	private void startTimer()
 	{
 		m_handler.removeCallbacks( m_timeTicker );
 		m_handler.postDelayed( m_timeTicker, 0 );
 	}
 
 	private void stopTimer()
 	{
 		m_handler.removeCallbacks( m_timeTicker );
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu )
 	{
 		getMenuInflater().inflate( R.menu.count_down, menu );
 		return true;
 	}
 
 	@Override
 	public void launchLoaded( Launch launch )
 	{
 		m_launch = launch;
 
 		m_endTime = m_launch.windowstart.getTime();
 		updateStatus();
 		updateTimer();
 
 		startTimer();
 	}
 
 	private class TickTimer implements Runnable
 	{
 		@Override
 		public void run()
 		{
 			updateTimer();
 			m_handler.postDelayed( this, INTERVAL_IN_MS );
 		}
 	}
 }
