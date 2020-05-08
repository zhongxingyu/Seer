 package com.darkrockstudios.apps.tminus.misc;
 
 import android.net.Uri;
 
 import java.util.List;
 
 /**
  * Created by Adam on 8/4/13.
  * Dark Rock Studios
  * darkrockstudios.com
  */
 public class TminusUri
 {
 	public static final String SCHEME               = "tminus";
 	public static final String AUTHORITY            = "tminus.com";
 	public static final String SEGMENT_LAUNCH       = "launch";
 	public static final String SEGMENT_COUNTDOWN    = "countdown";
 	public static final String SEGMENT_NOTIFICATION = "notification";
 	public static final String SEGMENT_REMINDER     = "reminder";
	public static final String SEGMENT_IMMINENT     = "reminder";
 
 	private static Uri constructBaseUri()
 	{
 		Uri.Builder builder = new Uri.Builder();
 		builder.scheme( SCHEME );
 		builder.authority( AUTHORITY );
 		return builder.build();
 	}
 
 	public static Uri buildLaunchDetail( int launchId )
 	{
 		return constructBaseUri().buildUpon().appendPath( SEGMENT_LAUNCH ).appendPath( launchId + "" ).build();
 	}
 
 	public static Uri buildLaunchCountDown( int launchId )
 	{
 		return constructBaseUri().buildUpon().appendPath( SEGMENT_LAUNCH ).appendPath( SEGMENT_COUNTDOWN )
 				       .appendPath( launchId + "" ).build();
 	}
 
 	private static Uri constructBaseLaunchNotificationUri()
 	{
 		Uri.Builder builder = constructBaseUri().buildUpon();
 		builder.appendPath( SEGMENT_LAUNCH );
 		builder.appendPath( SEGMENT_NOTIFICATION );
 		return builder.build();
 	}
 
 	public static Uri buildLaunchReminderNotification( int launchId )
 	{
 		Uri.Builder builder = constructBaseLaunchNotificationUri().buildUpon();
 		builder.appendPath( SEGMENT_REMINDER );
 		builder.appendPath( launchId + "" );
 		return builder.build();
 	}
 
 	public static Uri buildLaunchImminentNotification( int launchId )
 	{
 		Uri.Builder builder = constructBaseLaunchNotificationUri().buildUpon();
 		builder.appendPath( SEGMENT_IMMINENT );
 		builder.appendPath( launchId + "" );
 		return builder.build();
 	}
 
 	public static int extractLaunchId( Uri uri )
 	{
 		int launchId = -1;
 
 		if( uri.getScheme() == SCHEME && uri.getAuthority() == AUTHORITY )
 		{
 			List<String> segments = uri.getPathSegments();
 			if( segments.size() == 2 && segments.get( 0 ) == SEGMENT_LAUNCH )
 			{
 				try
 				{
 					launchId = Integer.getInteger( segments.get( 1 ) );
 				}
 				catch( NumberFormatException e )
 				{
 
 				}
 			}
 		}
 
 		return launchId;
 	}
 }
