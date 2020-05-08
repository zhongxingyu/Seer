 /**
  * 
  */
 package org.vimeoid.media;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.vimeoid.connection.VideoLinkRequestException;
 import org.vimeoid.connection.VimeoVideoStreamer;
 import org.vimeoid.util.Utils;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.media</dd>
  * </dl>
  *
  * <code>VimeoVideoPlayingTask</code>
  *
  * <p>Description</p>
  * 
  * <p>used  
  * <a href="http://ballardhack.wordpress.com/2010/04/25/android-audio-streaming/">this article about streaming media</a></p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 21, 2010 9:20:36 PM 
  *
  */
 public class VimeoVideoPlayingTask extends AsyncTask<Long, Long, FileInputStream> {
 	
 	public static final String TAG = "VimeoVideoPlayingTask";
     
     public static final String CACHE_DIR_NAME = "___v_video_player";
     public static final String STREAM_FILE_NAME = "___v_video_streamed";
     
     private static final int COPY_CHUNK_SIZE =  4 << 10; // 4 kBytes
     
     private static MediaPlayer mediaPlayer;
     
     private static File cacheDir;
     private final SurfaceHolder canvas;
     
     public VimeoVideoPlayingTask(Context context, SurfaceHolder canvas) { 
         if (cacheDir == null) cacheDir = Utils.createCacheDir(context, CACHE_DIR_NAME);
         this.canvas = canvas;
         ensureCleanedUp();
     }
     
     @Override
     protected void onPreExecute() {
     	super.onPreExecute();
     	
     	Log.d(TAG, "Creating player");
     	
 		 if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
 		 if (mediaPlayer == null) throw new IllegalStateException("Failed to create media player");
 		 
 		 mediaPlayer.setDisplay(canvas);
 		 mediaPlayer.setAudioStreamType(2);
 		 
 		 mediaPlayer.setOnErrorListener(new OnErrorListener() {
 			 @Override
 			 	public boolean onError(MediaPlayer mp, int what, int extra) {
 				 	Log.e(TAG, "Media player error: " + what + "/" + extra);
 				 	return false;
 			 	}
 		 });    	
     }
 
 	@Override
 	protected FileInputStream doInBackground(Long... params) {
 		if ((params == null) || (params.length == 0)) throw new IllegalStateException("No params were specified, please specify one!");
 		if (params.length > 1) throw new IllegalStateException("I can play only one video at a time.");
 		
 		final long videoId = params[0];
 		
		final long cacheSize = VimeoVideoStreamer.getLastContentLength();
 		try {
 			InputStream videoStream = VimeoVideoStreamer.getVideoStream(videoId);
 			if (videoStream == null) {
 				Log.e(TAG, "The returned video stream is null, so I will not play anything :(");
 				return null;
 			}
			ensureWeHaveEnoughSpace(cacheSize);
 			
 			Log.d(TAG, "Starting the media thread");
 			
 			File streamFile = File.createTempFile(STREAM_FILE_NAME, ".dat", cacheDir);
 			streamFile.deleteOnExit();
 
 			FileOutputStream out = new FileOutputStream(streamFile);
 			byte buf[] = new byte[COPY_CHUNK_SIZE];
 				 
 			Log.i(TAG, "Now we will write stream to file");
 				 
 			do {
 				int numread = videoStream.read(buf);
 				if (numread <= 0) break;
 				out.write(buf, 0, numread);
 			} while (true);
 			
 			Log.i(TAG, "File is ready");
 			
 			videoStream.close();
 			Log.d(TAG, "Video stream closed");
 			 
 			return new FileInputStream(streamFile);
 		} catch (NoSpaceForVideoCacheException nsfvce) {
 			//onException(nsfvce);
 			onNoSpaceForVideoCache(nsfvce.getRequiredSpace(), nsfvce.getActualSpace());
 		} catch (VideoLinkRequestException vle) { onException(vle); }
 	      catch (IOException ioe) { onException(ioe); }
 	      
 	    return null;
 	}
 	
 	@Override
 	protected void onPostExecute(FileInputStream dataSource) {
 		try {
 			mediaPlayer.setDataSource(dataSource.getFD());		
 			dataSource.close();
 			mediaPlayer.prepare();
 			Log.i(TAG, "Starting player! Duration: " + Utils.adaptDuration(mediaPlayer.getDuration() / 1000));
 			mediaPlayer.start();
 		} catch (IOException ioe) { onException(ioe); }		
 	}
 	
     public static void ensureCleanedUp() {
         if (mediaPlayer != null) {        	
             if (mediaPlayer.isPlaying()) mediaPlayer.stop();
             mediaPlayer.reset();
             Log.i(TAG, "Stopped previous instance and called reset for it");
         }        
         for (File file: cacheDir.listFiles()) if (file.isFile() && file.exists()) file.delete();
         Log.i(TAG, "Cleared player cache");
     }
     
     private void informException(Exception exception) {
 		 //Dialogs.makeToast(context, exception.getLocalizedMessage());
 	     Log.e(TAG, exception.getLocalizedMessage());
 	     exception.printStackTrace();
 	}
 	
     private void ensureWeHaveEnoughSpace(long expectedSpace) throws NoSpaceForVideoCacheException {
         final long spaceLeft = Utils.computeFreeSpace();
 		Log.i(TAG, "Checking free space | required " + expectedSpace + " : left " + spaceLeft);
 		if (expectedSpace > (spaceLeft * 0.8)) throw new NoSpaceForVideoCacheException(expectedSpace, spaceLeft);
 	}
     
 	protected void onException(Exception e) {
 		informException(e);
 	}
 	
 	protected void onNoSpaceForVideoCache(long required, long actual) { }
     
     @SuppressWarnings("serial")
     public static final class NoSpaceForVideoCacheException extends IOException {
         
         private final long requiredSpace;
         private final long actualSpace;
         
         public NoSpaceForVideoCacheException(long requiredSpace, long actualSpace) {
             this.requiredSpace = requiredSpace;
             this.actualSpace = actualSpace;
         }
         
         public long getRequiredSpace() {
             return requiredSpace;
         }
         
         public long getActualSpace() {
             return actualSpace;
         }        
         
     }
 
 
 }
