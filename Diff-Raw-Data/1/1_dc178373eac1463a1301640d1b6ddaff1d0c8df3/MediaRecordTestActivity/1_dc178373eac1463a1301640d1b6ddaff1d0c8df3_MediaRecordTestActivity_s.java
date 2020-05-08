 package com.example.mediarecordtest;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class MediaRecordTestActivity extends Activity implements SurfaceHolder.Callback {
 	private static final String TAG = "MediaRecorderExample";
 
 	Camera mCamera;
 	MediaRecorder mMediaRecorder;
 	
 	SurfaceView mSurface;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_media_record_test);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		mSurface = (SurfaceView) findViewById(R.id.surfaceViewCamera);
 		mSurface.getHolder().addCallback(this);
 		
 		mCamera = Camera.open();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		stopRecord();
 		
 		mCamera.stopPreview();
 		mCamera.release();
 		mCamera = null;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.media_record_test, menu);
 		return true;
 	}
 
 	public void onRecordClick(View v) {
 		boolean on = ((ToggleButton) v).isChecked();
 		
 		if (on) {
 			try {
 				startRecord();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 				Toast.makeText(this, "Fail to start recording/illegal state", Toast.LENGTH_SHORT).show();
 			} catch (IOException e) {
 				e.printStackTrace();
 				Toast.makeText(this, "Fail to start recording/io exception", Toast.LENGTH_SHORT).show();
 			}
 		}
 		else {
 			stopRecord();
 		}
 	}
 	
 	private int nFrameCount = 0;
 	
 	private void startRecord() throws IllegalStateException, IOException {
 		mCamera.unlock();
 
 		mMediaRecorder = new MediaRecorder();
 		mMediaRecorder.setCamera(mCamera);
 		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
 		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 		
 		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
 			mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
 		}
 		else {
 			mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
 		}
 		
 		File outputFile = new File(Environment.getExternalStorageDirectory(), "hello.mp4");
 		mMediaRecorder.setOutputFile(outputFile.getCanonicalPath());
 		mMediaRecorder.prepare();
 		mMediaRecorder.start();
 		
 		final TextView frameCountView = (TextView) findViewById(R.id.textViewFrameCount); 
 		mCamera.setPreviewCallback(new PreviewCallback() {
 		
 			@Override
 			public void onPreviewFrame(byte[] data, Camera camera) {
 				Log.d(TAG, "Receive data size: " + data.length);
 				frameCountView.setText(String.valueOf(++nFrameCount));
 			}
 			
 		});
 	}
 	
 	private void stopRecord() {
 		if (mMediaRecorder != null) {		
 			mMediaRecorder.stop();
 			mMediaRecorder.reset();
 			mMediaRecorder.release();
 			mMediaRecorder = null;
 			
 			mCamera.setPreviewCallback(null);
 			mCamera.lock();			
 		}
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		try {
 			mCamera.setPreviewDisplay(holder);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		mCamera.startPreview();			
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		
 	}
 }
