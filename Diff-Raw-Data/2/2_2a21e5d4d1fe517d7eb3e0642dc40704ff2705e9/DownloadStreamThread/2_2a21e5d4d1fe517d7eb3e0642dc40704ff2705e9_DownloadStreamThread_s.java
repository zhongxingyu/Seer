 package com.halcyonwaves.apps.meinemediathek.threads;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.util.UUID;
 
 import org.acra.ACRA;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaScannerConnection;
 import android.os.Environment;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.support.v4.app.NotificationCompat;
 import android.text.method.MovementMethod;
 import android.util.Log;
 
 import com.halcyonwaves.apps.meinemediathek.Consts;
 import com.halcyonwaves.apps.meinemediathek.R;
 import com.halcyonwaves.apps.meinemediathek.activities.ManageDownloadActivity;
 import com.halcyonwaves.apps.meinemediathek.activities.MovieOverviewActivity;
 import com.halcyonwaves.apps.meinemediathek.ndk.MMSInputStream;
 
 public class DownloadStreamThread extends Thread {
 
 	private static final String TAG = "DownloadStreamThread";
 	private String downloadLink = null;
 	private String movieTitle = null;
 
 	private NotificationCompat.Builder notificationBuilder = null;
 	private NotificationManager notificationManager = null;
 	private File outputFile = null;
 	private WakeLock downloadWakeLock = null;
 	private int notificationId = -1;
 
 	private Context threadContext = null;
 
 	public DownloadStreamThread( final Context context, int notificationId, final String downloadLink, final String movieTitle, final String movieDescription, final String moviePreviewImagePath ) {
 		this.downloadLink = downloadLink;
 		this.movieTitle = movieTitle;
 		this.threadContext = context;
 		this.notificationId = notificationId;
 
 		// just log the used notification id
 		Log.v( DownloadStreamThread.TAG, "Using the following notification id for the next download: " + this.notificationId );
 
 		// try to obtain a wake lock object
 		final PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
 		this.downloadWakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, String.format( "%s(%s)", DownloadStreamThread.TAG, movieTitle ) );
 
 		// be sure that the output directory we are trying to use exists
 		this.outputFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_MOVIES ), "MeineMediathek" );
 		this.outputFile.mkdirs();
 
 		// define what should happen if the user clicks on the notification item
 		Intent intent = new Intent( this.threadContext, ManageDownloadActivity.class );
 		intent.putExtra( Consts.EXTRA_NAME_MOVIE_TITLE, movieTitle );
 		intent.putExtra( Consts.EXTRA_NAME_MOVIE_DESCRIPTION, movieDescription );
 		intent.putExtra( Consts.EXTRA_NAME_MOVIE_UNIQUE_ID, this.notificationId );
		PendingIntent contentIntent = PendingIntent.getActivity( this.threadContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
 
 		// prepare the notification for the download
 		this.notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
 		this.notificationBuilder = new NotificationCompat.Builder( context );
 		this.notificationBuilder.setContentTitle( String.format( context.getString( R.string.not_title_download_of_movie ), this.movieTitle ) );
 		this.notificationBuilder.setContentText( context.getString( R.string.not_desc_download_of_movie ) );
 		this.notificationBuilder.setSmallIcon( android.R.drawable.stat_sys_download );
 		this.notificationBuilder.setOngoing( true );
 		this.notificationBuilder.setContentIntent( contentIntent );
 
 	}
 
 	@Override
 	public void run() {
 		//
 		Thread.currentThread().setName( String.format( "StreamDownloadTask(%s)", this.notificationId ) );
 
 		//
 		String extractedURL = "";
 		Document fetchedResults = null;
 		byte[] downloadBuffer = null;
 		boolean reachedDueToException = false;
 		int movieFullLength = 0;
 		int comReadB = 0;
 		int readB = 0;
 
 		try {
 
 			// be sure that the device will not sleep while we try to download our movie
 			this.downloadWakeLock.acquire();
 
 			// since we currently don't know how big the file is, show a progress with an undefined state
 			this.notificationBuilder.setProgress( 100, 0, true );
 			this.notificationManager.notify( DownloadStreamThread.TAG, Consts.NOTIFICATION_DOWNLOADING_MOVIE + this.notificationId, this.notificationBuilder.build() );
 
 			// try it two times to fetch the file (if the first time fails for a socket timeout)
 			try {
 				fetchedResults = Jsoup.connect( this.downloadLink ).ignoreContentType( true ).userAgent( Consts.DESKTOP_USER_AGENT ).timeout( Consts.SOCKET_TIMEOUT_IN_SECONDS * 1000 ).get();
 			} catch( final SocketTimeoutException e ) {
 				try {
 					fetchedResults = Jsoup.connect( this.downloadLink ).ignoreContentType( true ).userAgent( Consts.DESKTOP_USER_AGENT ).timeout( Consts.SOCKET_TIMEOUT_IN_SECONDS * 1000 ).get();
 				} catch( final SocketTimeoutException innerE ) {
 					throw e;
 				}
 
 			}
 			final Elements foundLinks = fetchedResults.select( "Ref[href]" );
 			for( final Element currentLink : foundLinks ) {
 				Log.v( DownloadStreamThread.TAG, "Found a media link inside the ASX file: " + currentLink.attr( "href" ) );
 				if( currentLink.attr( "href" ).startsWith( "mms://" ) ) {
 					extractedURL = currentLink.attr( "href" );
 					final String[] splittedURL = extractedURL.split( "/" );
 					if( splittedURL.length > 0 ) {
 						if( splittedURL[ splittedURL.length - 1 ].endsWith( "wmv" ) ) {
 							this.outputFile = new File( this.outputFile, splittedURL[ splittedURL.length - 1 ] );
 						} else {
 							this.outputFile = new File( this.outputFile, "test.wmv" );
 						}
 					} else {
 						this.outputFile = new File( this.outputFile, "test.wmv" );
 					}
 					break;
 				}
 			}
 
 			//
 			final MMSInputStream mmsInputStream = new MMSInputStream( extractedURL );
 			final FileOutputStream outputStream = new FileOutputStream( this.outputFile );
 			movieFullLength = mmsInputStream.length();
 
 			// since we know the length of the full movie now, we can set the progress bar to a known state
 			this.notificationBuilder.setProgress( 100, 0, false );
 			this.notificationManager.notify( DownloadStreamThread.TAG, Consts.NOTIFICATION_DOWNLOADING_MOVIE + this.notificationId, this.notificationBuilder.build() );
 
 			// select the buffer which is the best for the estimated file size
 			if( movieFullLength < (1024 * 1024 * 10) ) { // if the file is smaller than 10 MB,
 				downloadBuffer = new byte[ 1024 * 128 ]; // use a 128k buffer
 			} else if( movieFullLength < (1024 * 1024 * 50) ) { // if the file is smaller than 50 MB,
 				downloadBuffer = new byte[ 1024 * 256 ]; // use a 256k buffer
 			} else if( movieFullLength < (1024 * 1024 * 100) ) { // if the file is smaller than 100 MB,
 				downloadBuffer = new byte[ 1024 * 512 ]; // use a 512 buffer
 			} else { // if the file is bigger than 100MB
 				downloadBuffer = new byte[ 1024 * 1024 * 1 ]; // use a 1MB buffer
 			}
 			Log.v( DownloadStreamThread.TAG, String.format( "Selected a download buffer size of %d bytes", downloadBuffer.length ) );
 
 			// read the whole movie
 			while( comReadB < movieFullLength && !this.isInterrupted() ) {
 				// get a data chunk
 				readB = mmsInputStream.read( downloadBuffer, 0, downloadBuffer.length );
 				if( readB <= 0 ) {
 					break;
 				}
 
 				// write the data chunk into the output file
 				outputStream.write( downloadBuffer, 0, readB );
 				comReadB += readB;
 
 				// update the notification bar entry
 				this.notificationBuilder.setProgress( movieFullLength, comReadB, false );
 				this.notificationManager.notify( DownloadStreamThread.TAG, Consts.NOTIFICATION_DOWNLOADING_MOVIE + this.notificationId, this.notificationBuilder.build() );
 			}
 
 			// close all opened streams
 			downloadBuffer = null;
 			outputStream.flush();
 			outputStream.close();
 			mmsInputStream.close();
 
 			// if the thread was interrupted, delete the downloaded stuff again
 			if( this.isInterrupted() ) {
 				if( this.outputFile.delete() ) {
 					Log.v( DownloadStreamThread.TAG, "Deleted file which was created before the download thread was interrupted!" );
 				} else {
 					Log.w( DownloadStreamThread.TAG, "Failed to delete the file which was created before the download thread was interrupted!" );
 				}
 			}
 
 			// ensure that the media scanner sees the file we have added
 			else {
 				MediaScannerConnection.scanFile( this.threadContext, new String[] { this.outputFile.getAbsolutePath() }, null, null );
 			}
 
 		} catch( final IOException e ) {
 			Log.e( DownloadStreamThread.TAG, "Failed to fetch the movie file from the MMS stream.", e );
 			reachedDueToException = true;
 			ACRA.getErrorReporter().putCustomData( "downloadLink", this.downloadLink );
 			ACRA.getErrorReporter().putCustomData( "outputFileAbsolutePath", this.outputFile != null ? this.outputFile.getAbsolutePath() : "" );
 			ACRA.getErrorReporter().putCustomData( "downloadBufferSize", String.format( "%d", downloadBuffer.length ) );
 			ACRA.getErrorReporter().putCustomData( "extractedURL", extractedURL );
 			ACRA.getErrorReporter().putCustomData( "movieFullLength", String.format( "%d", movieFullLength ) );
 			ACRA.getErrorReporter().putCustomData( "readBytesFromMovie", String.format( "%d", comReadB ) );
 			ACRA.getErrorReporter().putCustomData( "lastReadBytes", String.format( "%d", readB ) );
 			ACRA.getErrorReporter().putCustomData( "exceptionMessage", e.getMessage() );
 			ACRA.getErrorReporter().handleException( e );
 		} finally {
 			// be sure that we ALWAYS release the wake lock
 			this.downloadWakeLock.release();
 
 			// ensure that the notification item can be removed if we finished or crashed
 			if( !reachedDueToException ) {
 				if( !this.isInterrupted() ) {
 					this.notificationBuilder.setContentText( this.threadContext.getString( R.string.not_desc_download_of_movie_finished ) ).setSmallIcon( android.R.drawable.stat_sys_download_done ).setOngoing( false ).setProgress( 0, 0, false );
 				} else {
 					this.notificationBuilder.setContentText( this.threadContext.getString( R.string.not_desc_download_of_movie_canceled ) ).setSmallIcon( android.R.drawable.stat_sys_download_done ).setOngoing( false ).setProgress( 0, 0, false );
 				}
 			} else {
 				this.notificationBuilder.setContentText( this.threadContext.getString( R.string.not_desc_download_of_movie_failed ) ).setSmallIcon( android.R.drawable.stat_sys_download_done ).setOngoing( false ).setProgress( 0, 0, false );
 			}
 			this.notificationManager.notify( DownloadStreamThread.TAG, Consts.NOTIFICATION_DOWNLOADING_MOVIE + this.notificationId, this.notificationBuilder.build() );
 
 		}
 
 	}
 }
