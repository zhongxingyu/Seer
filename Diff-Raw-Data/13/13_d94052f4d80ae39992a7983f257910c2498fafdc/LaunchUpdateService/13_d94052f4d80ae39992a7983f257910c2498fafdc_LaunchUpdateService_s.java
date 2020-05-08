 package com.darkrockstudios.apps.tminus;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.darkrockstudios.apps.tminus.database.DatabaseHelper;
 import com.darkrockstudios.apps.tminus.database.DatabaseUtilities;
 import com.darkrockstudios.apps.tminus.launchlibrary.Launch;
 import com.darkrockstudios.apps.tminus.launchlibrary.LaunchLibraryGson;
 import com.darkrockstudios.apps.tminus.launchlibrary.LaunchLibraryUrls;
 import com.darkrockstudios.apps.tminus.launchlibrary.Mission;
 import com.darkrockstudios.apps.tminus.launchlibrary.Pad;
 import com.darkrockstudios.apps.tminus.misc.Preferences;
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.misc.TransactionManager;
 import com.j256.ormlite.stmt.DeleteBuilder;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by Adam on 7/13/13.
  * Dark Rock Studios
  * darkrockstudios.com
  */
 public class LaunchUpdateService extends Service
 {
 	private static final String TAG = LaunchUpdateService.class.getSimpleName();
 
 	public static final String ACTION_LAUNCH_LIST_UPDATED       =
 			LaunchUpdateService.class.getPackage() + ".ACTION_LAUNCH_LIST_UPDATED";
 	public static final String ACTION_LAUNCH_LIST_UPDATE_FAILED =
 			LaunchUpdateService.class.getPackage() + ".ACTION_LAUNCH_LIST_UPDATE_FAILED";
 	public static final String EXTRA_REQUEST_PREVIOUS_LAUNCHES  =
 			LaunchUpdateService.class.getPackage() + ".REQUEST_PREVIOUS_LAUNCHES";
 
 	private static final int UPCOMING_LAUNCH_REQUEST_COUNT = 20;
 	private static final int PREVIOUS_LAUNCH_REQUEST_COUNT = 10;
 
 	private PowerManager.WakeLock m_wakeLock;
 
 	public LaunchUpdateService()
 	{
 		super();
 	}
 
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 
 		Log.d( TAG, "LaunchUpdateService created." );
 
 		final PowerManager powerManager = (PowerManager) getSystemService( Context.POWER_SERVICE );
 		m_wakeLock = powerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, TAG );
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 
 		Log.d( TAG, "LaunchUpdateService destroyed." );
 
 		if( m_wakeLock.isHeld() )
 		{
 			m_wakeLock.release();
 		}
 	}
 
 	@Override
 	public int onStartCommand( final Intent intent, final int flags, final int startId )
 	{
 		Log.d( TAG, "LaunchUpdateService started." );
 
 		final boolean previousLaunches;
 		if( intent != null && intent.hasExtra( EXTRA_REQUEST_PREVIOUS_LAUNCHES ) )
 		{
 			previousLaunches = intent.getBooleanExtra( EXTRA_REQUEST_PREVIOUS_LAUNCHES, false );
 		}
 		else
 		{
 			previousLaunches = false;
 		}
 
 		requestLaunches( previousLaunches );
 
 		return START_NOT_STICKY;
 	}
 
 	@Override
 	public IBinder onBind( final Intent intent )
 	{
 		return null;
 	}
 
 	private void requestLaunches( final boolean previousLaunches )
 	{
 		Log.d( TAG, "Requesting Launches..." );
 		if( !m_wakeLock.isHeld() )
 		{
 			m_wakeLock.acquire();
 
 			Log.d( TAG, "WakeLock acquired." );
 		}
 
 		final String url;
 		if( !previousLaunches )
 		{
 			url = LaunchLibraryUrls.next( UPCOMING_LAUNCH_REQUEST_COUNT );
 		}
 		else
 		{
 			url = LaunchLibraryUrls.last( PREVIOUS_LAUNCH_REQUEST_COUNT );
 		}
 
 		LaunchListResponseListener listener = new LaunchListResponseListener( previousLaunches );
 		JsonObjectRequest request = new JsonObjectRequest( url, null, listener, listener );
 		request.setTag( this );
 		TMinusApplication.getRequestQueue().add( request );
 	}
 
 	private void stopService()
 	{
 		TMinusApplication.getRequestQueue().cancelAll( this );
 		if( m_wakeLock.isHeld() )
 		{
 			m_wakeLock.release();
 		}
 		stopSelf();
 	}
 
 	private void sendSuccessBroadcast()
 	{
 		Log.i( TAG, "Launches successfully updates, sending success broadcast." );
 		final Intent intent = new Intent( ACTION_LAUNCH_LIST_UPDATED );
 		sendBroadcast( intent );
 	}
 
 	private void sendFailureBroadcast()
 	{
 		Log.i( TAG, "Launches update failed, sending failure broadcast." );
 		final Intent intent = new Intent( ACTION_LAUNCH_LIST_UPDATE_FAILED );
 		sendBroadcast( intent );
 	}
 
 	private class LaunchListResponseListener implements Response.Listener<JSONObject>, Response.ErrorListener
 	{
 		private final boolean m_previousLaunches;
 
 		public LaunchListResponseListener( final boolean previousLaunches )
 		{
 			m_previousLaunches = previousLaunches;
 		}
 
 		@Override
 		public void onResponse( final JSONObject response )
 		{
 			Log.i( TAG, "Launches successfully retrieved from sever." );
 			LaunchListSaver loader = new LaunchListSaver( m_previousLaunches );
 			loader.execute( response );
 		}
 
 		@Override
 		public void onErrorResponse( final VolleyError error )
 		{
 			Log.i( TAG, "Failed to retrieve Launches from sever." );
 			String errorMessage = error.getMessage();
 			if( errorMessage != null )
 			{
 				Log.i( TAG, "VolleyError: " + errorMessage );
 			}
 			sendFailureBroadcast();
 
 			stopService();
 		}
 	}
 
 	private class LaunchListSaver extends AsyncTask<JSONObject, Void, Long>
 	{
 		private final boolean m_previousLaunches;
 
 		public LaunchListSaver( final boolean previousLaunches )
 		{
 			super();
 
 			m_previousLaunches = previousLaunches;
 		}
 
 		@Override
 		protected Long doInBackground( final JSONObject... response )
 		{
 			Log.d( TAG, "Beginning background processing of new Launches..." );
 
 			long numLaunches = 0;
 
 			final Gson gson = LaunchLibraryGson.create();
 
 			final JSONObject launchListObj = response[ 0 ];
 
 			final DatabaseHelper databaseHelper = OpenHelperManager
 					                                      .getHelper( LaunchUpdateService.this, DatabaseHelper.class );
 			if( databaseHelper != null )
 			{
 				try
 				{
 					final Dao<Launch, Integer> launchDao = databaseHelper.getDao( Launch.class );
 					final Dao<Mission, Integer> missionDao = databaseHelper.getDao( Mission.class );
 
 					final JSONArray launchListArray = launchListObj.getJSONArray( "launches" );
 					for( int ii = 0; ii < launchListArray.length(); ++ii )
 					{
 						try
 						{
 							final JSONObject launchObj = launchListArray.getJSONObject( ii );
 							if( launchObj != null )
 							{
 								final Launch launch = parseLaunch( launchObj, gson );
 
 								try
 								{
 									TransactionManager.callInTransaction( databaseHelper.getConnectionSource(),
 									                                      new Callable<Void>()
 									                                      {
 										                                      public Void call() throws Exception
 										                                      {
 											                                      // If the launch already exists, cancel any alarms for it
 											                                      if( launchDao.idExists( launchDao
 													                                                              .extractId( launch ) ) )
 											                                      {
 												                                      UpdateAlarmsService
 														                                      .cancelAlarmsForLaunch( launch,
 														                                                              LaunchUpdateService.this );
 											                                      }
 
 											                                      DatabaseUtilities
 													                                      .saveRocket( launch.rocket,
 													                                                   databaseHelper );
 
 											                                      DatabaseUtilities
 													                                      .saveLocation( launch.location,
 													                                                     databaseHelper );
 
											                                      missionDao.query(
													                                                      missionDao
															                                                      .deleteBuilder()
															                                                      .where()
															                                                      .eq( "launch_id",
															                                                           launch.id )
															                                                      .prepare() );
 
 											                                      if( launch.missions != null )
 											                                      {
 												                                      for( final Mission mission : launch.missions )
 												                                      {
 													                                      missionDao.createOrUpdate( mission );
 												                                      }
 											                                      }
 
 											                                      // This must be run after all the others are created so the IDs of the child objects can be set
 											                                      launchDao.createOrUpdate( launch );
 
 											                                      return null;
 										                                      }
 									                                      } );
 								}
 								catch( final SQLException e )
 								{
 									Log.w( TAG, e.getMessage() );
 								}
 							}
 						}
 						catch( final JsonParseException e )
 						{
 							e.printStackTrace();
 						}
 					}
 
 					Log.d( TAG, "Parsing and database work complete" );
 
 					// Only do this work for upcoming launches
 					if( !m_previousLaunches )
 					{
 						Log.d( TAG, "Launching AlarmUpdateService..." );
 
 						// Now that we have new data, ensure our Alarms are set correctly
 						startService( new Intent( LaunchUpdateService.this, UpdateAlarmsService.class ) );
 
 						final SharedPreferences preferences = PreferenceManager
 								                                      .getDefaultSharedPreferences( LaunchUpdateService.this );
 						preferences.edit().putLong( Preferences.KEY_LAST_UPDATED, new Date().getTime() ).commit();
 						Log.d( TAG, "Refresh successful: " + numLaunches + " Launches in database." );
 					}
 
 					numLaunches = launchDao.countOf();
 				}
 				catch( final SQLException | JSONException e )
 				{
 					e.printStackTrace();
 				}
 				finally
 				{
 					OpenHelperManager.releaseHelper();
 				}
 			}
 
 			cleanUpOldLaunches();
 
 			return numLaunches;
 		}
 
 		private Launch parseLaunch( final JSONObject launchObj, final Gson gson ) throws JSONException
 		{
 			final Launch launch = gson.fromJson( launchObj.toString(), Launch.class );
 
 			// We need to hook up the parent child relationship for the database
 			if( launch.missions != null && launch.missions.size() > 0 )
 			{
 				for( final Mission mission : launch.missions )
 				{
 					mission.launch = launch;
 				}
 			}
 
 			// We need to hook up the parent child relationship for the database
 			for( final Pad pad : launch.location.pads )
 			{
 				pad.location = launch.location;
 			}
 
 			return launch;
 		}
 
 		@Override
 		protected void onPostExecute( final Long result )
 		{
 			Log.d( TAG, "Background update complete." );
 			sendSuccessBroadcast();
 			stopService();
 		}
 
 		private Date getOldLaunchThreshold()
 		{
 			final long MAX_DAYS_OLD = 365;
 
 			Date now = new Date();
 			Date cutOffDate = new Date( now.getTime() - TimeUnit.DAYS.toMillis( MAX_DAYS_OLD ) );
 
 			return cutOffDate;
 		}
 
 		private void cleanUpOldLaunches()
 		{
 			final DatabaseHelper databaseHelper = OpenHelperManager
 					                                      .getHelper( LaunchUpdateService.this, DatabaseHelper.class );
 			if( databaseHelper != null )
 			{
 				try
 				{
 					final Dao<Launch, Integer> launchDao = databaseHelper.getDao( Launch.class );
 
 					DeleteBuilder<Launch, Integer> builder = launchDao.deleteBuilder();
 					builder.where().lt( "net", getOldLaunchThreshold() );
 
 					launchDao.delete( builder.prepare() );
 				}
 				catch( final SQLException e )
 				{
 					e.printStackTrace();
 				}
 				finally
 				{
 					OpenHelperManager.releaseHelper();
 				}
 			}
 		}
 	}
 }
