 package com.halcyonwaves.apps.meinemediathek.threads;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.SocketException;
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
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 import com.halcyonwaves.apps.meinemediathek.Consts;
 import com.halcyonwaves.apps.meinemediathek.R;
 import com.halcyonwaves.apps.meinemediathek.activities.ManageDownloadActivity;
 import com.halcyonwaves.apps.meinemediathek.ndk.MMSInputStream;
 
 public class DownloadStreamThread extends Thread {
 
 	private static final String TAG = "DownloadStreamThread";
 	private final UUID DOWNLOAD_NOTIFICATION_FILE_ID = UUID.randomUUID();
 	private String downloadLink = null;
 	private String movieTitle = null;
 
 	private NotificationCompat.Builder notificationBuilder = null;
 	private NotificationManager notificationManager = null;
 	private File outputFile = null;
 
 	private Context threadContext = null;
 	private final int usedTimeoutInSeconds = 10;
 
 	public DownloadStreamThread( final Context context, final String downloadLink, final String movieTitle ) {
 		this.downloadLink = downloadLink;
 		this.movieTitle = movieTitle;
 		this.threadContext = context;
 
 		this.outputFile = context.getExternalFilesDir( Environment.DIRECTORY_MOVIES );
 
 		// prepare the notification for the download
 		this.notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
 		this.notificationBuilder = new NotificationCompat.Builder( context );
 		this.notificationBuilder.setContentTitle( String.format( context.getString( R.string.not_title_download_of_movie ), this.movieTitle ) ).setContentText( context.getString( R.string.not_desc_download_of_movie ) ).setSmallIcon( android.R.drawable.stat_sys_download ).setOngoing( true );
 
 		// tell the notification what to do if it gets pressed
 		final Intent notificationIntent = new Intent( context, ManageDownloadActivity.class );
 		notificationIntent.putExtra( "movieTitle", movieTitle );
 		notificationIntent.putExtra( "notificationDownloadId", this.DOWNLOAD_NOTIFICATION_FILE_ID.toString() );
 		PendingIntent.getActivity( context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT );
 
 	}
 
 	@Override
 	public void run() {
 		//
 		Thread.currentThread().setName( String.format( "StreamDownloadTask(%s)", this.DOWNLOAD_NOTIFICATION_FILE_ID.toString() ) );
 
 		// since we currently don't know how big the file is, show a progress with an undefined state
 		this.notificationBuilder.setProgress( 100, 0, true );
 		this.notificationManager.notify( this.DOWNLOAD_NOTIFICATION_FILE_ID.toString(), Consts.NOTIFICATION_DOWNLOADING_MOVIE, this.notificationBuilder.build() );
 
 		// the first step is to parse the ASX file and to get the MMS stream URL to download the movie
 		String extractedURL = "";
 		Document fetchedResults = null;
 		try {
 
 			// try it two times to fetch the file (if the first time fails for a socket timeout)
 			try {
 				fetchedResults = Jsoup.connect( this.downloadLink ).ignoreContentType( true ).userAgent( Consts.DESKTOP_USER_AGENT ).timeout( this.usedTimeoutInSeconds * 1000 ).get();
 			} catch( SocketTimeoutException e ) {
 				try {
 					fetchedResults = Jsoup.connect( this.downloadLink ).ignoreContentType( true ).userAgent( Consts.DESKTOP_USER_AGENT ).timeout( this.usedTimeoutInSeconds * 1000 ).get();
 				} catch( SocketTimeoutException innerE ) {
 					Log.e( DownloadStreamThread.TAG, "Failed to fetch the ASX file for parsing.", e );
 					ACRA.getErrorReporter().putCustomData( "downloadLink", this.downloadLink );
 					ACRA.getErrorReporter().putCustomData( "extractedURL", extractedURL );
					ACRA.getErrorReporter().putCustomData( "outputFileAbsolutePath", this.outputFile.getAbsolutePath() );
 					ACRA.getErrorReporter().putCustomData( "exceptionMessage", e.getMessage() );
					ACRA.getErrorReporter().handleException( e );
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
 
 		} catch( final IOException e ) {
 			Log.e( DownloadStreamThread.TAG, "Failed to fetch the ASX file for parsing.", e );
 			ACRA.getErrorReporter().putCustomData( "downloadLink", this.downloadLink );
 			ACRA.getErrorReporter().putCustomData( "extractedURL", extractedURL );
			ACRA.getErrorReporter().putCustomData( "outputFileAbsolutePath", this.outputFile.getAbsolutePath() );
 			ACRA.getErrorReporter().putCustomData( "exceptionMessage", e.getMessage() );
 			ACRA.getErrorReporter().handleException( e );
 		}
 
 		//
 		byte[] downloadBuffer = null;
 		int movieFullLength = 0;
 		int comReadB = 0;
 		int readB = 0;
 		try {
 			final MMSInputStream mmsInputStream = new MMSInputStream( extractedURL );
 
 			// get a output stream
 			final FileOutputStream outputStream = new FileOutputStream( this.outputFile );
 
 			// since we know the length of the full movie now, we can set the progress bar to a known state
 			movieFullLength = mmsInputStream.length();
 			this.notificationBuilder.setProgress( movieFullLength, 0, false );
 			this.notificationManager.notify( this.DOWNLOAD_NOTIFICATION_FILE_ID.toString(), Consts.NOTIFICATION_DOWNLOADING_MOVIE, this.notificationBuilder.build() );
 
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
 			while( comReadB < movieFullLength ) {
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
 				this.notificationManager.notify( this.DOWNLOAD_NOTIFICATION_FILE_ID.toString(), Consts.NOTIFICATION_DOWNLOADING_MOVIE, this.notificationBuilder.build() );
 			}
 
 			// close all opened streams
 			downloadBuffer = null;
 			outputStream.flush();
 			outputStream.close();
 			mmsInputStream.close();
 
 		} catch( final IOException e ) {
 			Log.e( DownloadStreamThread.TAG, "Failed to fetch the movie file from the MMS stream.", e );
 			ACRA.getErrorReporter().putCustomData( "downloadLink", this.downloadLink );
			ACRA.getErrorReporter().putCustomData( "outputFileAbsolutePath", this.outputFile.getAbsolutePath() );
 			ACRA.getErrorReporter().putCustomData( "downloadBufferSize", String.format( "%d", downloadBuffer.length ) );
 			ACRA.getErrorReporter().putCustomData( "extractedURL", extractedURL );
 			ACRA.getErrorReporter().putCustomData( "movieFullLength", String.format( "%d", movieFullLength ) );
 			ACRA.getErrorReporter().putCustomData( "readBytesFromMovie", String.format( "%d", comReadB ) );
 			ACRA.getErrorReporter().putCustomData( "lastReadBytes", String.format( "%d", readB ) );
 			ACRA.getErrorReporter().putCustomData( "exceptionMessage", e.getMessage() );
 			ACRA.getErrorReporter().handleException( e );
 		}
 
 		// ensure that the media scanner sees the file we have added
 		MediaScannerConnection.scanFile( this.threadContext, new String[] { this.outputFile.getAbsolutePath() }, null, null );
 
 		// we finished download the movie, change the notification again
 		this.notificationBuilder.setContentText( this.threadContext.getString( R.string.not_desc_download_of_movie_finished ) ).setSmallIcon( android.R.drawable.stat_sys_download_done ).setOngoing( false ).setProgress( 0, 0, false );
 		this.notificationManager.notify( this.DOWNLOAD_NOTIFICATION_FILE_ID.toString(), Consts.NOTIFICATION_DOWNLOADING_MOVIE, this.notificationBuilder.build() );
 
 	}
 
 }
