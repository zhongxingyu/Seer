 package com.darkrockstudios.apps.tminus.fragments;
 
 
 import android.animation.Animator;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.NetworkImageView;
 import com.darkrockstudios.apps.tminus.R;
 import com.darkrockstudios.apps.tminus.R.string;
 import com.darkrockstudios.apps.tminus.TMinusApplication;
 import com.darkrockstudios.apps.tminus.database.tables.RocketDetail;
 import com.darkrockstudios.apps.tminus.launchlibrary.Rocket;
 import com.darkrockstudios.apps.tminus.loaders.RocketDetailLoader;
 import com.darkrockstudios.apps.tminus.loaders.RocketDetailLoader.Listener;
 import com.darkrockstudios.apps.tminus.loaders.RocketLoader;
 import com.darkrockstudios.apps.tminus.loaders.RocketLoader.RocketLoadListener;
 import com.darkrockstudios.apps.tminus.misc.TminusUri;
 import com.darkrockstudios.apps.tminus.misc.Utilities;
 import com.darkrockstudios.apps.tminus.updatetasks.DataUpdaterService;
 import com.darkrockstudios.apps.tminus.updatetasks.RocketDetailUpdateTask;
 
 import java.io.File;
 
 import butterknife.ButterKnife;
 import butterknife.InjectView;
 import butterknife.Optional;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 
 /**
  * Created by Adam on 7/14/13.
  * Dark Rock Studios
  * darkrockstudios.com
  */
 public class RocketDetailFragment extends DialogFragment implements Listener, RocketLoadListener, Utilities.ZoomAnimationHandler
 {
 	public static final String TAG          = LaunchDetailFragment.class.getSimpleName();
 	public static final String ARG_ITEM_ID  = "item_id";
 	public static final String ARG_NO_IMAGE = "no_image";
 
 	private File         m_dataDirectory;
 	private Rocket       m_rocket;
 	private RocketDetail m_rocketDetail;
 
	@Optional
 	@InjectView(R.id.ROCKETDETAIL_container)
 	View m_containerView;
 
 	@Optional
 	@InjectView(R.id.ROCKETDETAIL_rocket_image)
 	NetworkImageView m_rocketImage;
 
 	@Optional
 	@InjectView(R.id.ROCKETDETAIL_expanded_rocket_image)
 	NetworkImageView m_rocketImageExpanded;
 
 	@InjectView(R.id.ROCKETDETAIL_name)
 	TextView m_rocketName;
 
 	@InjectView(R.id.ROCKETDETAIL_configuration)
 	TextView m_rocketConfiguration;
 
 	@InjectView(R.id.ROCKETDETAIL_details)
 	TextView m_rocketSummary;
 
 	private RocketDetailUpdateReceiver m_updateReceiver;
 
 	private Animator m_currentAnimator;
 	private int      m_shortAnimationDuration;
 
 	public RocketDetailFragment()
 	{
 	}
 
 	public static RocketDetailFragment newInstance( final int rocketId, final boolean noImage )
 	{
 		RocketDetailFragment rocketDetailFragment = new RocketDetailFragment();
 
 		Bundle arguments = new Bundle();
 		arguments.putInt( ARG_ITEM_ID, rocketId );
 		arguments.putBoolean( ARG_NO_IMAGE, noImage );
 		rocketDetailFragment.setArguments( arguments );
 
 		return rocketDetailFragment;
 	}
 
 	@Override
 	public void onAttach( final Activity activity )
 	{
 		super.onAttach( activity );
 
 		String dataDirPath = activity.getApplicationInfo().dataDir;
 		m_dataDirectory = new File( dataDirPath );
 
 		Log.d( TAG, "registering for events" );
 
 		m_updateReceiver = new RocketDetailUpdateReceiver();
 		IntentFilter filter = new IntentFilter();
 		filter.addAction( RocketDetailUpdateTask.ACTION_ROCKET_DETAILS_UPDATED );
 		filter.addAction( RocketDetailUpdateTask.ACTION_ROCKET_DETAILS_UPDATE_FAILED );
 		filter.addDataScheme( TminusUri.SCHEME );
 		activity.registerReceiver( m_updateReceiver, filter );
 
 		Log.d( TAG, "registering for events" );
 	}
 
 	@Override
 	public void onCreate( final Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 
 		setHasOptionsMenu( true );
 
 		m_shortAnimationDuration =
 				getResources().getInteger( android.R.integer.config_shortAnimTime );
 	}
 
 	@Override
 	public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
 	                          final Bundle savedInstanceState )
 	{
 		Dialog dialog = getDialog();
 		if( dialog != null )
 		{
 			dialog.setTitle( R.string.ROCKETDETAIL_title );
 		}
 
 		final View rootView;
 
 		if( shouldDisplayImage() )
 		{
 			rootView = inflater.inflate( R.layout.fragment_rocket_detail, container, false );
 		}
 		else
 		{
 			rootView = inflater.inflate( R.layout.fragment_rocket_detail_no_image, container, false );
 		}
 
 		if( rootView != null )
 		{
 			ButterKnife.inject( this, rootView );
 
 			if( m_rocketImage != null )
 			{
 				m_rocketImage.setDefaultImageResId( R.drawable.launch_detail_no_rocket_image );
 				// Disable until an image is loaded
 				m_rocketImage.setEnabled( false );
 			}
 
 			loadRocket();
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onDetach()
 	{
 		super.onDetach();
 
 		Activity activity = getActivity();
 		if( m_updateReceiver != null && activity != null )
 		{
 			activity.unregisterReceiver( m_updateReceiver );
 			m_updateReceiver = null;
 		}
 	}
 
 	@Override
 	public void onDestroyView()
 	{
 		super.onDestroyView();
 		ButterKnife.reset( this );
 	}
 
 	private void updateViews()
 	{
 		if( m_rocket != null && isAdded() )
 		{
 			m_rocketName.setText( m_rocket.name );
 			m_rocketConfiguration.setText( m_rocket.configuration );
 
 			if( m_rocketDetail != null )
 			{
 				if( m_rocketImage != null )
 				{
 					if( m_rocketDetail.imageUrl != null )
 					{
 						ImageLoader imageLoader = new ImageLoader( TMinusApplication
 								                                           .getRequestQueue(),
 						                                           TMinusApplication.getBitmapCache() );
 						m_rocketImage.setImageUrl( m_rocketDetail.imageUrl, imageLoader );
 						m_rocketImage.setEnabled( true );
 
 						m_rocketImageExpanded.setImageUrl( m_rocketDetail.imageUrl, imageLoader );
 					}
 					else
 					{
 						m_rocketImage.setEnabled( false );
 					}
 				}
 
 				if( m_rocketDetail.summary != null )
 				{
 					m_rocketSummary.setText( Html.fromHtml( m_rocketDetail.summary ) );
 				}
 			}
 		}
 	}
 
 	private void loadRocket()
 	{
 		final Activity activity = getActivity();
 		final int rocketId = getRocketId();
 
 		if( rocketId >= 0 && activity != null )
 		{
 			RocketLoader rocketLoader = new RocketLoader( this, activity );
 			rocketLoader.execute( rocketId );
 		}
 	}
 
 	public int getRocketId()
 	{
 		int rocketId = -1;
 
 		final Bundle arguments = getArguments();
 		if( arguments != null && arguments.containsKey( ARG_ITEM_ID ) )
 		{
 			rocketId = arguments.getInt( ARG_ITEM_ID );
 		}
 
 		return rocketId;
 	}
 
 	@Override
 	public void rocketLoaded( final Rocket rocket )
 	{
 		m_rocket = rocket;
 
 		final Activity activity = getActivity();
 		if( m_rocket != null && activity != null )
 		{
 			RocketDetailLoader detailLoader = new RocketDetailLoader( activity, this );
 			detailLoader.execute( m_rocket.id );
 
 			updateViews();
 		}
 	}
 
 	@Override
 	public void rocketLoadFailed( final int rocketId )
 	{
 		m_rocketSummary.setText( R.string.ROCKETDETAIL_no_summary );
 	}
 
 	@Override
 	public void rocketDetailLoaded( final RocketDetail rocketDetail )
 	{
 		m_rocketDetail = rocketDetail;
 		updateViews();
 	}
 
 	@Override
 	public void rocketDetailMissing( final int rocketId )
 	{
 		final Activity activity = getActivity();
 		if( activity != null && m_rocket != null )
 		{
 			startRocketDetailsUpdate();
 		}
 	}
 
 	private boolean shouldDisplayImage()
 	{
 		boolean displayImage = false;
 
 		final Bundle arguments = getArguments();
 		if( arguments != null && arguments.containsKey( ARG_NO_IMAGE ) )
 		{
 			displayImage = !arguments.getBoolean( ARG_NO_IMAGE );
 		}
 
 		return displayImage;
 	}
 
 	private void startRocketDetailsUpdate()
 	{
 		Activity activity = getActivity();
 		if( m_rocket != null && activity != null && isAdded() )
 		{
 			Intent intent = new Intent( activity, DataUpdaterService.class );
 			intent.setData( TminusUri.buildRocketUri( m_rocket.id ) );
 			intent.putExtra( DataUpdaterService.EXTRA_UPDATE_TYPE, RocketDetailUpdateTask.UPDATE_TYPE );
 
 			activity.startService( intent );
 		}
 	}
 
 	public void zoomRocketImage()
 	{
 		// If we don't have rocket info yet, don't bother zooming
 		if( m_rocketDetail != null && m_rocketDetail.imageUrl != null )
 		{
 			Utilities.zoomImage( m_rocketImage, m_rocketImageExpanded, m_containerView, this,
 			                     m_shortAnimationDuration );
 		}
 	}
 
 	@Override
 	public void setCurrentAnimator( final Animator animator )
 	{
 		m_currentAnimator = animator;
 	}
 
 	@Override
 	public Animator getCurrentAnimator()
 	{
 		return m_currentAnimator;
 	}
 
 	private class RocketDetailUpdateReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive( final Context context, final Intent intent )
 		{
 			Log.d( TAG, "onReceive " + intent );
 
 			final Activity activity = getActivity();
 			if( activity != null && isAdded() )
 			{
 				if( RocketDetailUpdateTask.ACTION_ROCKET_DETAILS_UPDATED.equals( intent.getAction() ) )
 				{
 					Log.i( TAG, "Received Rocket Detail update SUCCESS broadcast, will update the UI now." );
 
 					final int rocketId = TminusUri.extractRocketId( intent.getData() );
 					if( rocketId > 0 )
 					{
 						Log.i( TAG, "Rocket Detail fetch completely successfully for rocket id: " + rocketId );
 
 						RocketDetailLoader detailLoader = new RocketDetailLoader( activity, RocketDetailFragment.this );
 						detailLoader.execute( rocketId );
 
 						activity.setProgressBarIndeterminateVisibility( false );
 						Crouton.makeText( activity, string.TOAST_rocket_detail_update_complete, Style.CONFIRM )
 						       .show();
 					}
 				}
 				else if( RocketDetailUpdateTask.ACTION_ROCKET_DETAILS_UPDATE_FAILED.equals( intent.getAction() ) )
 				{
 					Log.w( TAG, "Received Rocket Detail update FAILURE broadcast." );
 
 					final int rocketId = TminusUri.extractRocketId( intent.getData() );
 					if( rocketId > 0 )
 					{
 						Log.w( TAG, "Rocket Detail fetch completely failed for rocket id: " + rocketId );
 					}
 
 					m_rocketSummary.setText( R.string.ROCKETDETAIL_no_summary );
 
 					Crouton.makeText( activity, R.string.TOAST_rocket_detail_update_failed, Style.ALERT ).show();
 					activity.setProgressBarIndeterminateVisibility( false );
 				}
 			}
 		}
 	}
 }
