 package com.halcyonwaves.apps.meinemediathek.services;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 
 import com.halcyonwaves.apps.meinemediathek.Consts;
 import com.halcyonwaves.apps.meinemediathek.threads.DownloadStreamThread;
 
 /**
  * This class describes a service which is responsible for all movie downloads of the
  * application. If an Activity wants to initiate a download, it has to connect to this
  * service and send a request message with the required information.
  * 
  * @author Tim Huetz
  */
 public class BackgroundDownloadService extends Service {
 
 	/**
 	 * This tag is used for all logging messages of this service.
 	 */
 	private static final String TAG = "BackgroundDownloadService";
 
 	/**
 	 * This message is used to request to start a new download.
 	 */
 	public static final int SERVICE_MSG_INITIATE_DOWNLOAD = 1;
 
 	/**
 	 * This message is used to request to cancel a running download.
 	 */
 	public static final int SERVICE_MSG_CANCEL_DOWNLOAD = 2;
 
 	/**
 	 * This is the messager which is used to communicate with this service.
 	 */
 	private final Messenger serviceMessenger = new Messenger( new IncomingHandler() );
 
 	/**
 	 * This map is used to store all running threads which are managed my this service.
 	 */
 	private Map< UUID, Thread > managedThreads = new HashMap< UUID, Thread >();
 
 	/**
 	 * 
 	 * @author Tim Huetz
 	 */
 	private class IncomingHandler extends Handler {
 
 		@Override
 		public void handleMessage( final Message msg ) {
 			switch( msg.what ) {
 				case BackgroundDownloadService.SERVICE_MSG_INITIATE_DOWNLOAD:
 					// get the additional data send with the download request
 					final Bundle suppliedExtras = msg.getData();
 
 					// get some required information to starting the download
 					final String episodeTitle = suppliedExtras.getString( Consts.EXTRA_NAME_MOVIE_TITLE );
 					final String downlaodURL = suppliedExtras.getString( Consts.EXTRA_NAME_MOVIE_DOWNLOADLINK );
 					final UUID uniqueId = UUID.fromString( suppliedExtras.getString( Consts.EXTRA_NAME_MOVIE_UNIQUE_ID ) );
 
 					// start the download
 					if( !BackgroundDownloadService.this.managedThreads.containsKey( uniqueId ) ) {
 						Thread downloadThread = new DownloadStreamThread( BackgroundDownloadService.this.getApplicationContext(), downlaodURL, episodeTitle );
 						BackgroundDownloadService.this.managedThreads.put( uniqueId, downloadThread );
						downloadThread.start();
 						Log.d( BackgroundDownloadService.TAG, "The background downloader servies started a thread trying to download the following URL: " + downlaodURL );
 					} else {
 						Log.d( BackgroundDownloadService.TAG, "Not starting a new download thread because the following URL is already queued:" + downlaodURL );
 					}
 					break;
 				case BackgroundDownloadService.SERVICE_MSG_CANCEL_DOWNLOAD:
 					break; // TODO: this
 				default:
 					super.handleMessage( msg );
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public IBinder onBind( final Intent intent ) {
 		return this.serviceMessenger.getBinder();
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public int onStartCommand( final Intent intent, final int flags, final int startid ) {
 		return Service.START_STICKY;
 	}
 
 }
