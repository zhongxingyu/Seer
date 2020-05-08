 package com.android.interview;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioRecord;
 import android.media.AudioTrack;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.android.interview.CameraSurface.ImageAdapter;
 import com.android.interview.utilities.Data;
 
 public class AudioRecorder extends Activity implements OnClickListener {
 
     private static final int CAMERA_RESULT = 1;
 	
 	RecordAudio recordTask;
 	PlayAudio playTask;
 
 	Button startRecordingButton, stopRecordingButton;
 	Button startPlaybackButton, stopPlaybackButton;
 	
 	Button photoButton;
 	
 	TextView durationText;
 	
 	private Data data = Data.getInstance();
 	File recordingFile;
 
 	boolean isRecording = false;
 	boolean isPlaying = false;
 
 	int frequency = 11025;
 	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
 	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
 
 	long startRecordTime;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.audio_recorder);
 
 		durationText = (TextView) this.findViewById(R.id.DurationTextView);
 		
 		startRecordingButton = (Button) this
 				.findViewById(R.id.StartRecordingButton);
 		stopRecordingButton = (Button) this
 				.findViewById(R.id.StopRecordingButton);
 		startPlaybackButton = (Button) this
 				.findViewById(R.id.StartPlaybackButton);
 		stopPlaybackButton = (Button) this
 				.findViewById(R.id.StopPlaybackButton);
 		photoButton = (Button) this
 				.findViewById(R.id.PhotoButton);
 		
 		startRecordingButton.setOnClickListener(this);
 		stopRecordingButton.setOnClickListener(this);
 		startPlaybackButton.setOnClickListener(this);
 		stopPlaybackButton.setOnClickListener(this);
 		photoButton.setOnClickListener(this);
 
 		stopRecordingButton.setEnabled(false);
 		startPlaybackButton.setEnabled(false);
 		stopPlaybackButton.setEnabled(false);
 		photoButton.setEnabled(true);
 
 		// recordingFile = File.createTempFile("recording", ".pcm", path);
 		String audioFilename = data.GetNewAudioURL();
 		recordingFile = new File(audioFilename);
 
 		showToast(this,audioFilename);       	               
         
 		startRecordTime = System.currentTimeMillis();
 		System.out.println("Current time: " + startRecordTime);
 		// Start recording
 		record();
 	}
 
 	public void onClick(View v) {
 		if (v == startRecordingButton) {
 			record();
 		} else if (v == stopRecordingButton) {
 			stopRecording();
 		} else if (v == startPlaybackButton) {
 			play();
 		} else if (v == stopPlaybackButton) {
 			stopPlaying();
 		} else if (v == photoButton) {
 			takePhoto();
 		}
 	}
 
 	public void play() {
 		startPlaybackButton.setEnabled(true);
 
 		playTask = new PlayAudio();
 		playTask.execute();
 
 		stopPlaybackButton.setEnabled(true);
 	}
 
 	public void stopPlaying() {
 		isPlaying = false;
 		stopPlaybackButton.setEnabled(false);
 		startPlaybackButton.setEnabled(true);
 	}
 
 	public void record() {
 		startRecordingButton.setEnabled(false);
 		stopRecordingButton.setEnabled(true);
 
 		// For Fun
		//startPlaybackButton.setEnabled(true);
 
 		recordTask = new RecordAudio();
 		recordTask.execute();
 	}
 
 	public void stopRecording() {
 		isRecording = false;
 	}
 	
 	public void takePhoto(){
         
 		String photoFilename = data.GetNewPhotoURL();
 		showToast(this,photoFilename);       	               
         
         Uri imageFileUri = Uri.fromFile(new File(photoFilename));
         
         Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
         intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
         
         startActivityForResult(intent, this.CAMERA_RESULT);               		
 	}
 
 	private void showToast(Context mContext, String text) {
     	Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
     }
 	
     protected void onActivityResult(int requestCode, int resultCode, Intent image) {
         super.onActivityResult(requestCode, resultCode, image);
                 
        if(requestCode == this.CAMERA_RESULT){
     	      	                                       	
         }
     }
 
 	private class PlayAudio extends AsyncTask<Void, Integer, Void> {
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
 
 				startPlaybackButton.setEnabled(false);
 				stopPlaybackButton.setEnabled(true);
 
 			} catch (Throwable t) {
 				Log.e("AudioTrack", "Playback Failed");
 			}
 
 			return null;
 		}
 	}
 
 	private class RecordAudio extends AsyncTask<Void, Integer, Void> {
 		@Override
 		protected Void doInBackground(Void... params) {
 			isRecording = true;
 
 			try {
 				DataOutputStream dos = new DataOutputStream(
 						new BufferedOutputStream(new FileOutputStream(
 								recordingFile)));
 
 				int bufferSize = AudioRecord.getMinBufferSize(frequency,
 						channelConfiguration, audioEncoding);
 
 				AudioRecord audioRecord = new AudioRecord(
 						MediaRecorder.AudioSource.MIC, frequency,
 						channelConfiguration, audioEncoding, bufferSize);
 
 				short[] buffer = new short[bufferSize];
 				audioRecord.startRecording();
 
 				int r = 0;
 				while (isRecording) {
 					int bufferReadResult = audioRecord.read(buffer, 0,
 							bufferSize);
 					for (int i = 0; i < bufferReadResult; i++) {
 						dos.writeShort(buffer[i]);
 					}
 
 					publishProgress(new Integer(r));
 					r++;
 				}
 
 				audioRecord.stop();
 				dos.close();
 			} catch (Throwable t) {
 				Log.e("AudioRecord", "Recording Failed");
 			}
 
 			return null;
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
 			long duration = System.currentTimeMillis() - startRecordTime;
 			SimpleDateFormat df = new SimpleDateFormat("mm:ss");			
 			durationText.setText("Time:" + df.format(new Date(duration)));
 		}
 
 		protected void onPostExecute(Void result) {
 			startRecordingButton.setEnabled(true);
 			stopRecordingButton.setEnabled(false);
 			startPlaybackButton.setEnabled(true);
 		}
 	}
 	
 }
