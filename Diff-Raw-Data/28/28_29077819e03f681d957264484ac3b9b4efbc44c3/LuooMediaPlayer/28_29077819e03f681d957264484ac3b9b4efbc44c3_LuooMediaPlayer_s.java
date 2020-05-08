 package com.aliyun.LuooFm;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Service;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 public class LuooMediaPlayer extends Service {
 	private static final int INTIAL_KB_BUFFER =  128*10/8;//assume 128kbps*10secs/8bits per byte
 	private MediaPlayer player;  
 	private MediaPlayer mediaPlayer;
 	private final Handler handler = new Handler();  
 	private File downloadingMediaFile;
 	private int totalKbRead = 0;
 	private int counter = 0;
 	private boolean isInterrupted;
 	private LocalBinder localBinder = new LocalBinder();
 	private static Runnable r;
 	private static Thread playerThread;
 	private File destFile;
 	private int totalLength = 1;
 	private int totalBytesRead = 1;
 	private String lastPlay = "";
 	private String lastDownloading = "";
 
 	public LuooMediaPlayer() {
 	}
 	public void startStreaming(final String mediaUrl, File destFile) throws IOException {
		this.pausePlayer();
 		this.isInterrupted = false;
 		if (this.playerThread != null){
			if (!lastDownloading.equals(mediaUrl) && this.playerThread.isAlive()){
 				this.isInterrupted = true;
				this.mediaPlayer.release();
				this.mediaPlayer = null;
			}else{
				lastDownloading = mediaUrl;
 			}
 		}
 		
 	    this.destFile = destFile;
 		r = new Runnable() {     
 			public void run() {     
 				try {  
 					downloadAudioIncrement(mediaUrl);  
 				} catch (IOException e) {  
 					Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);  
 					return;  
 				}     
 			}     
 		};     
 		playerThread = new Thread(r);  
 		playerThread.start();
 	}  
 
 	public void downloadAudioIncrement(String mediaUrl) throws IOException {  
 		HttpURLConnection cn = (HttpURLConnection)new URL(mediaUrl).openConnection();  
 		cn.addRequestProperty("User-Agent","LuooFM Player/10.0.0.4072");  
		cn.connect();
 		cn.setConnectTimeout(10000);
 		cn.setReadTimeout(30000);
 		totalLength = cn.getContentLength();
 		InputStream stream = cn.getInputStream();  
 		if (stream == null) {  
 			Log.e("my", "Unable to create InputStream for mediaUrl:" + mediaUrl);  
 		}  
 		downloadingMediaFile = new File(this.getCacheDir(),"downloadingMedia.dat");  
 
 		// Just in case a prior deletion failed because our code crashed or something, we also delete any previously   
 		// downloaded file to ensure we start fresh.  If you use this code, always delete   
 		// no longer used downloads else you'll quickly fill up your hard disk memory.  Of course, you can also   
 		// store any previously downloaded file in a separate data cache for instant replay if you wanted as well.  
 		if (downloadingMediaFile.exists()) {  
 			downloadingMediaFile.delete();  
 		}
 		FileOutputStream out = new FileOutputStream(downloadingMediaFile);     
 		byte buf[] = new byte[16384];  
 		totalBytesRead = 0;  
 		do {  
 			int numread = stream.read(buf);     
 			if (numread <= 0)     
 				break;     
 			out.write(buf, 0, numread);  
 			totalBytesRead += numread;  
 			totalKbRead = totalBytesRead/1000;
 			testMediaBuffer();  
 			fireDataLoadUpdate();  
 		} while (validateNotInterrupted());     
 		stream.close();  
 		if (totalBytesRead == totalLength) {
 			fireDataFullyLoaded();  
 		}  
 	}
 	public void flushCacheFiles() {
 		File file = new File(Environment.getExternalStorageDirectory() + "/LuooFm/download/");
 		this.deleteDirectory(file);
 	}
 	private void testMediaBuffer() {
 		// TODO Auto-generated method stub
 		Runnable updater = new Runnable() {  
 			public void run() {  
 				if (mediaPlayer == null) {  
 					//  Only create the MediaPlayer once we have the minimum buffered data  
 					if ( totalKbRead >= INTIAL_KB_BUFFER) {  
 						try {  
 							startMediaPlayer();  
 						} catch (Exception e) {  
 							Log.e(getClass().getName(), "Error copying buffered conent.", e);                  
 						}  
 					}  
 				} else{
 					if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() < 1000 ){  
 					//  NOTE:  The media player has stopped at the end so transfer any existing buffered data  
 						//  We test for < 1second of data because the media player can stop when there is still  
 						//  a few milliseconds of data left to play  
 						transferBufferToMediaPlayer();  
 					}					
 				}  
 			}
 
 
 		};  
 		handler.post(updater);  
 	}   
 	private boolean validateNotInterrupted() {  
 		if (isInterrupted) {  
 			if (mediaPlayer != null) {  
 				mediaPlayer.pause();  
 				//mediaPlayer.release();  
 			}  
 			this.isInterrupted = false;
 			return false;  
 		} else {  
 			return true;  
 		}  
 	}
 	private void startMediaPlayer() {  
 		try {     
 			File bufferedFile = new File(this.getCacheDir(),"playingMedia" + (counter++) + ".dat");  
 
 			// We double buffer the data to avoid potential read/write errors that could happen if the   
 			// download thread attempted to write at the same time the MediaPlayer was trying to read.  
 			// For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while   
 			// the media is playing.  This would permanently deadlock the file download.  To avoid such a deadloack,   
 			// we move the currently loaded data to a temporary buffer file that we start playing while the remaining   
 			// data downloads.    
 			moveFile(downloadingMediaFile,bufferedFile);  
 			
 			mediaPlayer = createMediaPlayer(bufferedFile);  
 			mediaPlayer.seekTo(0);
 			// We have pre-loaded enough content and started the MediaPlayer so update the buttons & progress meters.  
 			mediaPlayer.start();  
 			startPlayProgressUpdater();              
 		} catch (IOException e) {  
 			Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);  
 			return;  
 		}     
 	}  
 
 	public void startPlayProgressUpdater() {  
 //		float progress = (((float)mediaPlayer.getCurrentPosition()/1000)/mediaLengthInSeconds); 
 		if (mediaPlayer != null) return;
 		if (mediaPlayer.isPlaying()) {  
 			Runnable notification = new Runnable() {  
 				public void run() {  
 					startPlayProgressUpdater();  
 				}  
 			};  
 			handler.postDelayed(notification, 1000);  
 		}  
 	}     
 	
 	public String getPlayingStatus() {
 		MediaPlayer temp = this.getMediaPlayer();
 		if (temp != null){
 			return "{\"cur\":" + temp.getCurrentPosition() + ", \"dur\":" + temp.getDuration() + ", \"playing\":" + temp.isPlaying() + ", \"downloading\":" + (1.0 * this.totalBytesRead/this.totalLength)+ "}";
 		}else{
 			return "{}";
 		}						
 	}
 
 	private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {  
 		MediaPlayer mPlayer = new MediaPlayer();  
 		mPlayer.setOnErrorListener(  
 				new MediaPlayer.OnErrorListener() {  
 					public boolean onError(MediaPlayer mp, int what, int extra) {  
 						Log.e("my", "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );  
 						return false;  
 					}  
 				});  
 
 		//  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.  
 		//  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to  
 		//  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.  
 		FileInputStream fis = new FileInputStream(mediaFile);  
 		mPlayer.setDataSource(fis.getFD());  
 		mPlayer.prepare();  
 		return mPlayer;  
 	} 
 	public void pausePlayer(){  
 		try {  
 			getMediaPlayer().pause();  
 		} catch (Exception e) {  
 			e.printStackTrace();  
 		}  
 	}  
 
 	public void moveFile(File oldLocation, File newLocation) throws IOException { 
 		if ( oldLocation.exists( )) {  
 			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );  
 			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));  
 			try {  
 				byte[]  buff = new byte[8192];  
 				int numChars;  
 				while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {  
 					writer.write( buff, 0, numChars );  
 				}  
 			} catch( IOException ex ) {  
 				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());  
 			} finally {  
 				try {  
 					if ( reader != null ){                          
 						writer.close();  
 						reader.close();  
 					}  
 				} catch( IOException ex ){  
 					Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );   
 				}  
 			}  
 		} else {  
 			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );  
 		}  
 	}  
 
 	public MediaPlayer getPlayer() {  
 		return this.player;  
 	}  
 	public MediaPlayer getMediaPlayer() {  
 		return this.mediaPlayer;  
 	}  
 
 	private void fireDataLoadUpdate() {  
 		Runnable updater = new Runnable() {  
 			public void run() {  
 				//textStreamed.setText((totalKbRead + " Kb read"));  
 //				float loadProgress = ((float)totalKbRead/(float)mediaLengthInKb);  
 				//progressBar.setSecondaryProgress((int)(loadProgress*100));  
 			}  
 		};  
 		handler.post(updater);  
 	}
 	private void fireDataFullyLoaded() {  
 		Runnable updater = new Runnable() {   
 			public void run() {  
 				transferBufferToMediaPlayer();  
 				// Delete the downloaded File as it's now been transferred to the currently playing buffer file.
 				try {
 					if (!destFile.exists()){
 						destFile.getParentFile().mkdirs();
 						destFile.createNewFile();
 					}					
 					moveFile(downloadingMediaFile, destFile);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				downloadingMediaFile.delete();  
 				//textStreamed.setText(("Audio full loaded: " + totalKbRead + " Kb read"));  
 			}  
 		};  
 		handler.post(updater);  
 	}  
 	public void playLocalMedia(File destFile) throws IOException{
 		Log.d("my", "Play local file" + destFile.getAbsolutePath());
 		if (mediaPlayer != null ){
 			mediaPlayer.pause(); 
 		}		 
 		// Create a new MediaPlayer rather than try to re-prepare the prior one.  
 		mediaPlayer = createMediaPlayer(destFile);
 		mediaPlayer.seekTo(0);  
 		mediaPlayer.start();
 	}
 	private void transferBufferToMediaPlayer() {  
 		try {  
 			// First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause  
 			boolean wasPlaying = mediaPlayer.isPlaying();  
 			int curPosition = mediaPlayer.getCurrentPosition();  
 			// Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later.   
 			File oldBufferedFile = new File(this.getCacheDir(),"playingMedia" + counter + ".dat");  
 			File bufferedFile = new File(this.getCacheDir(),"playingMedia" + (counter++) + ".dat");  
 
 			//  This may be the last buffered File so ask that it be delete on exit.  If it's already deleted, then this won't mean anything.  If you want to   
 			// keep and track fully downloaded files for later use, write caching code and please send me a copy.  
 			bufferedFile.deleteOnExit();     
 			moveFile(downloadingMediaFile,bufferedFile); 
 			// Pause the current player now as we are about to create and start a new one.  So far (Android v1.5),  
 			// this always happens so quickly that the user never realized we've stopped the player and started a new one  
 			// Create a new MediaPlayer rather than try to re-prepare the prior one.  
 			mediaPlayer = createMediaPlayer(bufferedFile);  
 			mediaPlayer.seekTo(curPosition); 
 			//  Restart if at end of prior buffered content or mediaPlayer was previously playing.    
 			//    NOTE:  We test for < 1second of data because the media player can stop when there is still  
 			//  a few milliseconds of data left to play  
 			mediaPlayer.start();
 			// Lastly delete the previously playing buffered File as it's no longer needed.  
 			oldBufferedFile.delete(); 
 		}catch (Exception e) {  
 			Log.e(getClass().getName(), "Error updating to newly loaded content.", e);                      
 		}  
 	}  
 	
 	public void play() throws Exception {  
 		try {
 			mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
 			mediaPlayer.start();   
 		} catch (Exception e) {  
 			e.printStackTrace();  
 		}  
 
 	}  
 	
 	private boolean deleteDirectory(File path) {
 		Log.d("my", "deleteDirectory");
 	    if( path.exists() ) {
 	      File[] files = path.listFiles();
 	      for(int i=0; i<files.length; i++) {
 	         if(files[i].isDirectory()) {
 	           deleteDirectory(files[i]);
 	         }
 	         else {
 	           files[i].delete();
 	           Log.d("my", files[i].toURI().toString());
 	         }
 	      }
 	    }
 	    return( path.delete() );
 	  }
 	public class LocalBinder extends Binder {  
 		LuooMediaPlayer getService() {  
 			return LuooMediaPlayer.this;  
 		}  
 	}  
 	@Override
 	public void onCreate() {
 
 	}
 
 	@Override  
 	public void onStart(Intent intent, int startId) {  
 		Log.d("my", "on start");       
 	}
 
 	@Override  
 	public void onDestroy() { 
 		super.onDestroy();
 		Log.d("my", "GONE");		
 //		this.mediaPlayer.release();
 //		this.player.release();
 		this.flushCacheFiles();
 		if (this.mediaPlayer != null){
 			mediaPlayer.stop();
 			mediaPlayer.release();
 			mediaPlayer = null;
 		}
 		if (this.player != null){
 			player.stop();
 			player.release();			
 			player = null;
 		}
 	}  
 	
 
 
 	@Override  
 	public boolean onUnbind(Intent intent) {  
 		Log.d("my", "unbind");		
 		return super.onUnbind(intent);  
 	}  
 
 	@Override  
 	public IBinder onBind(Intent intent) { 
 		Log.e("my", "start IBinder~~~");  
 		return localBinder ;  
 	}
 
 	public String getSystemTime() {
 		// TODO Auto-generated method stub
 		Date date = new Date();
 		return date.toGMTString();
 	}  
 
 }
