 package com.darkrockstudios.apps.tminus.experiences.launch.detail;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 
 import com.darkrockstudios.apps.tminus.R;
 import com.darkrockstudios.apps.tminus.base.activities.DatabaseActivity;
 import com.darkrockstudios.apps.tminus.experiences.countdown.CountDownActivity;
 import com.darkrockstudios.apps.tminus.experiences.launch.detail.fragments.LaunchDetailFragment;
 import com.darkrockstudios.apps.tminus.experiences.location.detail.LocationDetailActivity;
 import com.darkrockstudios.apps.tminus.experiences.rocket.detail.RocketDetailActivity;
 import com.darkrockstudios.apps.tminus.launchlibrary.Pad;
 import com.darkrockstudios.apps.tminus.launchlibrary.Rocket;
 import com.darkrockstudios.apps.tminus.misc.CommonMenuHandler;
import com.darkrockstudios.apps.tminus.misc.UpNavUtil;
 
 public class LaunchDetailActivity extends DatabaseActivity
 {
 	private static final String FRAGMENT_TAG = "LaunchDetailFragment";
 	private int m_launchId;
 
 	@Override
 	protected void onCreate( final Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
 		setContentView( R.layout.activity_launch_detail );
 
 		m_launchId = getLaunchId();
 		if( m_launchId >= 0 )
 		{
 			if( savedInstanceState == null )
 			{
 				// Create the detail fragment and add it to the activity
 				// using a fragment transaction.
 				LaunchDetailFragment fragment = LaunchDetailFragment.newInstance( m_launchId );
 				getSupportFragmentManager().beginTransaction()
 				                           .add( R.id.COMMON_detail_fragment_container, fragment, FRAGMENT_TAG )
 				                           .commit();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( final Menu menu )
 	{
 		getMenuInflater().inflate( R.menu.settings, menu );
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( final MenuItem item )
 	{
 		final boolean handled;
 
 		if( !CommonMenuHandler.onOptionsItemSelected( item, this ) )
 		{
			switch( item.getItemId() )
			{
				case android.R.id.home:
					UpNavUtil.standardUp( this );
					handled = true;
					break;
				default:
					handled = super.onOptionsItemSelected( item );
					break;
			}
 		}
 		else
 		{
 			handled = true;
 		}
 
 		return handled;
 	}
 
 	private int getLaunchId()
 	{
 		int launchId = -1;
 
 		final Intent intent = getIntent();
 		if( intent != null )
 		{
 			launchId = intent.getIntExtra( LaunchDetailFragment.ARG_ITEM_ID, -1 );
 		}
 
 		return launchId;
 	}
 
 	public void countDownClicked( final View v )
 	{
 		if( m_launchId >= 0 )
 		{
 			Intent countDownIntent = new Intent( this, CountDownActivity.class );
 			countDownIntent.putExtra( CountDownActivity.ARG_ITEM_ID, m_launchId );
 			startActivity( countDownIntent );
 		}
 	}
 
 	public void rocketDetailsClicked( final View v )
 	{
 		Rocket rocket = (Rocket) v.getTag();
 
 		final Context context = v.getContext();
 		Intent intent = new Intent( context, RocketDetailActivity.class );
 		intent.putExtra( RocketDetailActivity.ARG_ITEM_ID, rocket.id );
 		context.startActivity( intent );
 	}
 
 	public void locationDetailsClicked( final View v )
 	{
 		Pad pad = (Pad) v.getTag();
 
 		final Context context = v.getContext();
 		Intent intent = new Intent( context, LocationDetailActivity.class );
 		intent.putExtra( RocketDetailActivity.ARG_ITEM_ID, pad.id );
 		context.startActivity( intent );
 	}
 
 	public void rocketImageClicked( final View v )
 	{
 		LaunchDetailFragment fragment =
 				(LaunchDetailFragment) getSupportFragmentManager()
 						                       .findFragmentByTag( FRAGMENT_TAG );
 		if( fragment != null )
 		{
 			fragment.zoomRocketImage();
 		}
 	}
 }
