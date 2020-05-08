 package com.android.interview;
 
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
import java.io.IOException;

import com.android.interview.CameraSurface.ImageAdapter;
 import com.android.interview.utilities.Data;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
import android.widget.Button;
 import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 
 public class Audio extends Activity {
       
     private Data data = Data.getInstance();
     private String[] audioFilePaths;
 
     boolean isPlaying = false;
     PlayAudio playTask;
     
     File recordingFile;
    
         
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.audio);
         
         android.widget.Gallery audio_gallery = (android.widget.Gallery)findViewById(R.id.audio_gallery);
         
         audio_gallery.setAdapter(new AudioAdapter(this));
 
         audio_gallery.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
 	        	recordingFile = new File(audioFilePaths[position]);
 	        	playTask = new PlayAudio();
 	    		playTask.execute();
 	        }
 	    });
 	    
     }
       
     
     public void recordAudio(View view) {
         Intent intent = new Intent(this, AudioRecorder.class);
         startActivityForResult(intent, 0);
     	
     }
     public class AudioAdapter extends BaseAdapter {
 	    int mGalleryItemBackground;
 	    
 	    private Context mContext;
 
 	    private void getAudioPaths()
 	    {
 	    	String[] audioFiles = data.GetAudioURLs();
 	    	audioFilePaths = new String[audioFiles.length];
 	    	
 	    	for(int i=0;i<audioFiles.length;i++)
 	    	{		    	   			    
 		    	audioFilePaths[i] = audioFiles[i];
 	    	}
 	    }
 	    
 	    
 
 	    public AudioAdapter(Context c) {
 	    	getAudioPaths();
 	    	
 	        mContext = c;
 	        TypedArray a = obtainStyledAttributes(R.styleable.Gallery);
 	        mGalleryItemBackground = a.getResourceId(
 	                R.styleable.Gallery_android_galleryItemBackground, 0);
 	        a.recycle();
 	    }
 
 	    public int getCount() {
 	        return audioFilePaths.length;
 	    }
 
 	    public Object getItem(int position) {
 	        return position;
 	    }
 
 	    public long getItemId(int position) {
 	        return position;
 	    }
 
 	    @Override	
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	    	
 	    	ImageView musicNote = new ImageView(mContext);
 	    	Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
 	    			   R.drawable.sub_audio_wotext);
 	    	
 	    	musicNote.setLayoutParams(new android.widget.Gallery.LayoutParams(250, 250));	    	
 	    	musicNote.setBackgroundResource(mGalleryItemBackground);
 	    	musicNote.setImageBitmap(mBitmap);
 	    	
 	        return musicNote;
 	    }	   	   
 
 	}
     
     private class PlayAudio extends AsyncTask<Void, Integer, Void> {
     	
     	private int frequency = 11025;
     	private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
     	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
 
     	
     	
 		@Override
 		protected Void doInBackground(Void... params) {
 			isPlaying = true;
 
 			int bufferSize = AudioTrack.getMinBufferSize(frequency,
 					channelConfiguration, audioEncoding);
 			short[] audiodata = new short[bufferSize / 4];
 
 			try {
 				DataInputStream dis = new DataInputStream(
 						new BufferedInputStream(new FileInputStream(
 								recordingFile)));
 
 				AudioTrack audioTrack = new AudioTrack(
 						AudioManager.STREAM_MUSIC, frequency,
 						channelConfiguration, audioEncoding, bufferSize,
 						AudioTrack.MODE_STREAM);
 
 				audioTrack.play();
 
 				while (isPlaying && dis.available() > 0) {
 					int i = 0;
 					while (dis.available() > 0 && i < audiodata.length) {
 						audiodata[i] = dis.readShort();
 						i++;
 					}
 					audioTrack.write(audiodata, 0, audiodata.length);
 				}
 
 				dis.close();
 
 			} catch (Throwable t) {
 				Log.e("AudioTrack", "Playback Failed");
 			}
 
 			return null;
 		}
 	}
 }
