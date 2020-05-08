 package com.halcyonwaves.apps.meinemediathek.worker;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.halcyonwaves.apps.meinemediathek.ndk.MMSInputStream;
 
 /**
  * Thread implementation for downloading a stream in an asynchronous way
  * to the local device.
  * 
  * @author Tim Huetz <tim@huetz.biz>
  */
 public class DownloadStreamTask extends AsyncTask< String, Integer, Void > {
 
 	private TextView bytesDownloadedField = null;
 	private ProgressBar progressbarToUpdate = null;
 
 	public DownloadStreamTask( final ProgressBar pb, final TextView tv ) {
 		this.progressbarToUpdate = pb;
 		this.bytesDownloadedField = tv;
 		this.progressbarToUpdate.setMax( 100 );
 		this.bytesDownloadedField.setText( "Initializing..." );
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	protected Void doInBackground( final String... streamUrlToDownload ) {
 		try {
 			//
 			final File file = File.createTempFile( "streamer-downloading", ".wmv" );
 			final FileOutputStream out = new FileOutputStream( file );
 
 			//
 			final MMSInputStream st = new MMSInputStream( streamUrlToDownload[ 0 ] );
 
 			//
 			byte[] buffer = new byte[ 8192 ];
 			int comReadB = 0;
 
 			//
 			while( comReadB < st.length() ) {
 				final int readB = st.read( buffer, 0, buffer.length );
 				if( readB <= 0 ) {
 					break;
 				}
 				out.write( buffer, 0, readB );
 				comReadB += readB;
 
 				this.publishProgress( comReadB, st.length() );
 			}
 
 			//
			st.close();
 			out.close();
 			buffer = null;
 		} catch( final IOException e ) {
 			Log.e( "SearchMoveFragment", e.getMessage() );
 		}
 
 		return null;
 	}
 
 	@Override
 	protected void onProgressUpdate( final Integer... values ) {
 		final float currentProgress = (float) values[ 0 ] / (float) values[ 1 ];
 		this.progressbarToUpdate.setProgress( Math.round( currentProgress * 100.0f ) );
 		this.bytesDownloadedField.setText( String.format( "%d bytes of %d bytes downloaded", values[ 0 ], values[ 1 ] ) );
 	}
 }
