 package edu.bonn.mobilegaming.geoquest.mission;
 
 import java.io.IOException;
 
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import edu.bonn.mobilegaming.geoquest.Globals;
 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.Variables;
 
 public class AudioRecord extends InteractiveMission {
 
     private static final String TAG = "AudioRecord";
 
     public static final int BUTTON_TAG_RECORD = 1;
     public static final int BUTTON_TAG_STOP_RECORDING = 2;
     public static final int BUTTON_TAG_PLAY = 3;
     public static final int BUTTON_TAG_STOP_PLAYING = 4;
 
     public static final int MODE_INITIAL = 1;
     public static final int MODE_RECORDING = 2;
     public static final int MODE_READY = 3;
     public static final int MODE_PLAYING = 4;
     private int mode;
 
     private TextView taskView;
     private TextView activityIndicator;
 
     private Button recBT = null;
     private Button useBT = null;
     private Button playBT = null;
 
     private MediaRecorder mRecorder = null;
     private MediaPlayer mPlayer = null;
 
     private String mFileName = null;
 
     // private CharSequence mURL;
 
     @Override
     public void onCreate(Bundle bundle) {
 	super.onCreate(bundle);
 
 	setContentView(R.layout.audiorecord);
 
 	mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()
 		+ "/" + getMissionAttribute("file",
 					    R.string.audiorecord_file_default);
 
 	// mURL = getMissionAttribute("url",
 	// XMLUtilities.OPTIONAL_ATTRIBUTE);
 
 	initTaskViewAndActivityIndicator();
 	initButtons();
 
 	setMode(MODE_INITIAL);
     }
 
     private void startPlaying() {
 	mPlayer = new MediaPlayer();
 	try {
 	    mPlayer.setDataSource(mFileName);
 	    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
 		public void onCompletion(MediaPlayer mp) {
 		    setMode(MODE_READY);
 		}
 	    });
 	    mPlayer.prepare();
 	    mPlayer.start();
 
 	} catch (IOException e) {
 	    Log.e(TAG,
 		  "prepare() failed");
 	}
     }
 
     private void stopPlaying() {
 	mPlayer.release();
 	mPlayer = null;
     }
 
     private void startRecording() {
 	mRecorder = new MediaRecorder();
 	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 	mRecorder.setOutputFile(mFileName);
 	mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
 	try {
 	    mRecorder.prepare();
 	} catch (IOException e) {
 	    Log.e(TAG,
 		  "prepare() failed");
 	}
 
 	mRecorder.start();
     }
 
     private void stopRecording() {
 	mRecorder.stop();
 	mRecorder.release();
 	mRecorder = null;
     }
 
     private void setMode(int newMode) {
 	switch (newMode) {
 	case MODE_INITIAL:
 	    activityIndicator
 		    .setText(R.string.audiorecord_activityIndicator_initial);
 	    recBT.setEnabled(true);
	    recBT.setText(R.string.button_text_record);
 	    recBT.setTag(BUTTON_TAG_RECORD);
 	    useBT.setEnabled(false);
 	    playBT.setEnabled(false);
	    playBT.setText(R.string.button_text_play);
 	    playBT.setTag(BUTTON_TAG_PLAY);
 	    mode = newMode;
 	    break;
 	case MODE_RECORDING:
 	    activityIndicator
 		    .setText(R.string.audiorecord_activityIndicator_recording);
 	    recBT.setEnabled(true);
	    recBT.setText(R.string.button_text_stop);
 	    recBT.setTag(BUTTON_TAG_STOP_RECORDING);
 	    useBT.setEnabled(false);
 	    playBT.setEnabled(false);
	    playBT.setText(R.string.button_text_play);
 	    playBT.setTag(BUTTON_TAG_PLAY);
 	    startRecording();
 	    mode = newMode;
 	    break;
 	case MODE_READY:
 	    activityIndicator
 		    .setText(R.string.audiorecord_activityIndicator_ready);
 	    recBT.setEnabled(true);
	    recBT.setText(R.string.button_text_record);
 	    recBT.setTag(BUTTON_TAG_RECORD);
 	    useBT.setEnabled(true);
 	    playBT.setEnabled(true);
	    playBT.setText(R.string.button_text_play);
 	    playBT.setTag(BUTTON_TAG_PLAY);
 	    if (mode == MODE_RECORDING)
 		stopRecording();
 	    if (mode == MODE_PLAYING)
 		stopPlaying();
 	    mode = newMode;
 	    break;
 	case MODE_PLAYING:
 	    activityIndicator
 		    .setText(R.string.audiorecord_activityIndicator_playing);
 	    recBT.setEnabled(false);
	    recBT.setText(R.string.button_text_record);
 	    recBT.setTag(BUTTON_TAG_RECORD);
 	    useBT.setEnabled(false);
 	    playBT.setEnabled(true);
	    playBT.setText(R.string.button_text_stop);
 	    playBT.setTag(BUTTON_TAG_STOP_PLAYING);
 	    startPlaying();
 	    mode = newMode;
 	    break;
 	default:
 	    Log.e(TAG,
 		  "Undefined mode " + newMode);
 	}
     }
 
     private void initButtons() {
 	recBT = (Button) findViewById(R.id.audioRecordRecordButton);
 	recBT.setOnClickListener(new OnClickListener() {
 	    public void onClick(View v) {
 		switch (mode) {
 		case MODE_INITIAL:
 		case MODE_READY:
 		    setMode(MODE_RECORDING);
 		    break;
 		case MODE_RECORDING:
 		    setMode(MODE_READY);
 		    break;
 		default:
 		    Log.e(TAG,
 			  "Record Button should not be enabled in mode " + mode);
 		}
 	    }
 	});
 
 	playBT = (Button) findViewById(R.id.audioRecordPlayButton);
 	playBT.setOnClickListener(new OnClickListener() {
 	    public void onClick(View v) {
 		switch (mode) {
 		case MODE_PLAYING:
 		    setMode(MODE_READY);
 		    break;
 		case MODE_READY:
 		    setMode(MODE_PLAYING);
 		    break;
 		default:
 		    Log.e(TAG,
 			  "Play Button should not be enabled in mode " + mode);
 		}
 
 	    }
 	});
 
	useBT = (Button) findViewById(R.id.audioRecordUseButton);
	useBT.setText(R.string.button_text_use);
 	useBT.setOnClickListener(new OnClickListener() {
 	    public void onClick(View v) {
 		switch (mode) {
 		case MODE_READY:
 		    performFinish();
 		    break;
 		default:
 		    Log.e(TAG,
 			  "Use Button should not be enabled in mode " + mode);
 		}
 	    }
 
 	});
     }
 
     private void initTaskViewAndActivityIndicator() {
 	taskView = (TextView) findViewById(R.id.audioRecordTextView);
 	taskView.setText(getMissionAttribute("task",
 					     R.string.audiorecord_task_default));
 
 	activityIndicator = (TextView) findViewById(R.id.audioRecordActivityIndicator);
     }
 
     @Override
     public void onPause() {
 	super.onPause();
 	if (mRecorder != null) {
 	    mRecorder.release();
 	    mRecorder = null;
 	}
 
 	if (mPlayer != null) {
 	    mPlayer.release();
 	    mPlayer = null;
 	}
     }
 
     // private boolean upload() {
     // boolean uploaded = false;
     // // Create a new HttpClient and Post Header
     // HttpClient httpclient = new DefaultHttpClient();
     // HttpPost httppost = new HttpPost(mURL.toString());
     //
     // try {
     // // Adding data:
     // MultipartEntity requestEntity = new MultipartEntity(
     // HttpMultipartMode.BROWSER_COMPATIBLE);
     // requestEntity.addPart("uploaded_file",
     // new InputStreamBody(new ByteArrayInputStream(
     // getBytesFromFile(mFileName)),
     // "audio/3gpp", mFileName));
     // requestEntity.addPart("secret",
     // new StringBody(TAG));
     // httppost.setEntity(requestEntity);
     //
     // // Execute HTTP Post Request
     // HttpResponse response = httpclient.execute(httppost);
     // CharSequence result = makeReadableResponse(response);
     // if (result.equals(getText(R.string.audioRecordUploadOK))) {
     // // TODO make generic
     // uploaded = true;
     // useBT.setText(R.string.audioRecordFinishedButton);
     // }
     // taskView.setText(result);
     //
     // } catch (ClientProtocolException e) {
     // e.printStackTrace();
     // } catch (IOException e) {
     // e.printStackTrace();
     // }
     // return uploaded;
     // }
     //
     // public static byte[] getBytesFromFile(String fileName) throws IOException
     // {
     // File file = new File(fileName);
     // InputStream is = new FileInputStream(file);
     //
     // // Get the size of the file
     // long length = file.length();
     //
     // if (length > Integer.MAX_VALUE) {
     // // File is too large
     // }
     //
     // // Create the byte array to hold the data
     // byte[] bytes = new byte[(int) length];
     //
     // // Read in the bytes
     // int offset = 0;
     // int numRead = 0;
     // while (offset < bytes.length
     // && (numRead = is.read(bytes,
     // offset,
     // bytes.length - offset)) >= 0) {
     // offset += numRead;
     // }
     //
     // // Ensure all the bytes have been read in
     // if (offset < bytes.length) {
     // throw new IOException("Could not completely read file "
     // + file.getName());
     // }
     //
     // // Close the input stream and return bytes
     // is.close();
     // return bytes;
     // }
     //
     // private CharSequence makeReadableResponse(HttpResponse response) {
     // HttpEntity responseEntity = response.getEntity();
     // ByteArrayOutputStream os = new ByteArrayOutputStream();
     // try {
     // responseEntity.writeTo(os);
     // } catch (IOException e) {
     // // TODO Auto-generated catch block
     // e.printStackTrace();
     // }
     // String responseText = os.toString();
     // if (responseText.startsWith("OK")) {
     // return getText(R.string.audioRecordUploadOK);
     // } else {
     // String errno = responseText.substring(0,
     // responseText.indexOf(':'));
     // return getText(R.string.audioRecordUploadERROR) + "  Error: "
     // + errno;
     // }
     // }
 
     private void performFinish() {
 	Variables.registerMissionResult(mission.id,
 					mFileName.toString());
 	invokeOnSuccessEvents();
 	finish(Globals.STATUS_SUCCESS);
     }
 
     public void onBlockingStateUpdated(boolean blocking) {
 	// TODO Auto-generated method stub
 
     }
 
 }
