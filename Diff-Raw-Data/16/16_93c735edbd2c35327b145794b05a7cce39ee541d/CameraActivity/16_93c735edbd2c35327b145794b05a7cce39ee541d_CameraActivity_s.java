 package com.sosohan.snapmv;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.media.ThumbnailUtils;
 import android.media.MediaRecorder.OnInfoListener;
 import android.os.Bundle;
 import android.provider.MediaStore.Images.Thumbnails;
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 
 import android.view.OrientationEventListener;
 
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 
 public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnInfoListener{
 	String cam_tag = "CameraActivity";
 	String promisedPath = "/sdcard/DCIM/";
 	SurfaceView camSurfaceView;
 	ImageView logoBtn;
 	ImageView prevThumbBtn;
 	TextView seqTxt;
 	ImageView changeCamBtn;
 	Camera camera;
 	int frontCam;
 	int backCam;
 	int useCam;
 	MediaRecorder recorder;
 	SurfaceHolder holder;
 	
 	ToggleButton recordToggle;
 	ToggleButton backFrontCamToggle;
 	CamcorderProfile camProfile;
 	OrientationEventListener oel;
 	
 	MediaDataPreference mediaPref = null;
 	int idx = 0;
 	int rotation = 0;
 	boolean recording = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_camera);
 		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		mediaPref = MediaDataPreference.getInstance(getApplicationContext());
 				
 		camSurfaceView = (SurfaceView) findViewById(R.id.camSurfaceView);
 		
 		frontCam = getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT);
 		backCam = getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK);
 		
 		if(backCam != -1)
 			useCam = backCam;
 		
 		logoBtn = (ImageView)  findViewById(R.id.logo_btn);
 		logoBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {	
 				oel.disable();
 				if(!recording){	
 					start();
 					recording = true;
 					setLogoImage(rotation);	
 					Log.i(cam_tag, "setOnClickListener idx"+idx);	
 					idx++;
 				}								
 			}
 		});
 		prevThumbBtn = (ImageView)  findViewById(R.id.prev_thumnail);
 		prevThumbBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override			
 			public void onClick(View v) {
 				if(idx > 0)
 				{
 					idx--;
 					updatePrev(idx);
 					seqTxt.setText("#"+ (idx + 1));
 					mediaPref.setCurrentIdx(idx);
 				}
 			}
 		});
 		changeCamBtn = (ImageView)  findViewById(R.id.change_cam);
 		changeCamBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override			
 			public void onClick(View v) {
 				stopCamera();
 				if(useCam == frontCam)
 				{
 					useCam = backCam;
 					
 				}
 				else //if (useCam == backCam)
 				{
 					useCam = frontCam;				
 				}
 				Log.i(cam_tag, "setOnClickListener useCam"+useCam);	
 				startCamera(useCam);
 			}
 		});
 		seqTxt = (TextView) findViewById(R.id.seq_MV_txt);
 		
 		holder = camSurfaceView.getHolder();
 		holder.addCallback(this);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//this is a deprecated method, is not required after 3.0 
         		
 		camProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_480P);
 		//if s3
 		//camProfile.videoFrameWidth = 720;
 		oel = new OrientationEventListener(this) {
 			@Override
 			public void onOrientationChanged(int orientation) {				
 				if ((orientation <= 45 && orientation >= 0) || (orientation <= 359 && orientation >= 311)) {
 					Log.i("Test", "Portrait");					
 					rotation = 270;
 				} else if (orientation <= 310 && orientation >= 225) {					
 					Log.i("Test", "Landscape");
 					rotation = 0;
 				}
 				if(!recording)
 					setLogoImage(rotation);
 			}
 		};
 		oel.enable();
 		Log.v(cam_tag,"w" + camProfile.videoFrameWidth + "/h"+ camProfile.videoFrameHeight); 
 	}
 		
 	private void setLogoImage(int rot)
 	{
 		if(!recording)
 		{
 			if(rot ==0)
 			{
 				logoBtn.setImageResource(R.drawable.ic_snap_cam);
 			}
 			else if(rot == 270)
 			{
 				logoBtn.setImageResource(R.drawable.ic_snap_cam_po);
 			}
 		}
 		else //if(recording)
 		{
 			if(rot ==0)
 			{
 				logoBtn.setImageResource(R.drawable.ic_snap_cam_rec);
 			}
 			else if(rot == 270)
 			{
 				logoBtn.setImageResource(R.drawable.ic_snap_cam_rec_po);
 			}
 		}
 	}
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		Log.v(cam_tag, "onResume");	
 		setLogoImage(rotation);
 		
 		idx = mediaPref.getCurrentIdx();
 		seqTxt.setText("#"+ (idx + 1));
		if(idx > 0)
		{			
			updatePrev(idx-1);
		}		
 	}	
 	
 	private int getCameraInfo(int info)
 	{
 		int result = -1;
 		int cameraCount = 0;
 		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
 		cameraCount = Camera.getNumberOfCameras();
 		Log.v(cam_tag, "cameraCount="+cameraCount);
 		for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
 			Camera.getCameraInfo( camIdx, cameraInfo );
 			if ( cameraInfo.facing == info  ) {
 				try {
 					Log.v(cam_tag, "camIdx="+camIdx);
 					result = camIdx;
 					Log.v(cam_tag, "camera="+camera);
 				} catch (RuntimeException e) {
 					Log.e(cam_tag, "Camera failed to open: " + e.getLocalizedMessage());
 				}
 			}
 		}	
 		return result;		
 	}
 	
 	private boolean previewRunning;
 	private void startCamera(int cameraIdx)
 	{
 		camera = Camera.open( cameraIdx );
 		
 		if (previewRunning) {
 			camera.stopPreview();
 		}
 		Camera.Parameters p = camera.getParameters();
 //		if(p != null) {
 //			List<Camera.Size> sizeList = p.getSupportedPreviewSizes();
 //			for(Size size : sizeList)
 //				Log.d(cam_tag+"prv", "size="+size.width+", "+size.height);
 //			sizeList = p.getSupportedVideoSizes();
 //			for(Size size : sizeList)
 //				Log.d(cam_tag+"vd", "size="+size.width+", "+size.height);
 //		}
 //		p.setPreviewSize(camProfile.videoFrameWidth, camProfile.videoFrameHeight);
 //		p.setPreviewSize(1280, 720);
 //		p.setPreviewFormat(PixelFormat.JPEG);
 		camera.setParameters(p);
 		
 		try {
 			camera.setPreviewDisplay(holder);
 			camera.startPreview();
 			previewRunning = true;
 		}
 		catch (IOException e) {
 			Log.e(cam_tag,e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	private void stopCamera() 
 	{
 		camera.stopPreview();
 		previewRunning = false;
 		camera.release();
 	}
 	private void resizeCamSurfaceView()
 	{
 		DisplayMetrics displayMetrics = new DisplayMetrics(); 
 		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
 		
 		int DisplayWidth = displayMetrics.widthPixels;
 		int DisplayHeight = displayMetrics.heightPixels;
 		
 		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){			
 			holder.setFixedSize(DisplayWidth, (DisplayWidth*camProfile.videoFrameWidth/camProfile.videoFrameHeight));			
 		}
 		else{					
 			holder.setFixedSize((camProfile.videoFrameWidth*DisplayHeight)/camProfile.videoFrameHeight, DisplayHeight);
 		}
 	}
 	
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.i(cam_tag, "surfaceCreated");
 		resizeCamSurfaceView();
 		startCamera(useCam);
 	}	
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int pixelFormat, int width,
 			int height) {
 		Log.v(cam_tag, "::surfaceChanged::"+width+"*"+height);			
 	}
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.i(cam_tag, "surfaceDestroyed");	
 		stopCamera();		
 	}
 	
 	@Override
 	public void onInfo(MediaRecorder mr, int what, int extra) {
 		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
 			Log.v(cam_tag,"Maximum Duration Reached"); 
 			mr.stop();			
 			recorder.release();
 			recording = false;
 			setLogoImage(rotation);
 			Log.i(cam_tag, "onInfo idx"+idx);	
 			if(idx >= 8) {
 				finish();
 			} else {
 				mediaPref.setCurrentIdx(idx);
				updatePrev(idx-1);
 				seqTxt.setText("#"+ (idx + 1));
 			}			
 		}
 	}	
 	private void updatePrev(int seq)
 	{
		Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(promisedPath+(seq)+".mp4", Thumbnails.MICRO_KIND);
		prevThumbBtn.setImageBitmap(thumbnail);
 	}
 //	@Override
 //	public boolean onTouchEvent(MotionEvent event) {
 //		// TODO Auto-generated method stub		
 //		if (event.getAction() == MotionEvent.ACTION_DOWN && !recording){
 //			
 //			recording = true;
 //			start();
 //			//camSurfaceView.setClickable(false);
 //			logoBtn.setImageResource(R.drawable.ic_snap_cam_rec);
 //			if(idx >= 8)
 //				idx = 1;
 //			else
 //				idx++;
 //		}				
 //		return super.onTouchEvent(event);
 //	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.camera, menu);
 		return true;
 	}
 	private void start() {
 		Log.v(cam_tag, "start");		
 		try {
 			recorder = new MediaRecorder();
 			camera.unlock();
 			recorder.setCamera(camera);
 			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 			//recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
 			
 //			CamcorderProfile camcorderProfile_HQ = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
 //			recorder.setProfile(camcorderProfile_HQ);
 			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
 			recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
 			
 			recorder.setVideoSize(camProfile.videoFrameWidth,camProfile.videoFrameHeight);
 			//if g3
 			//recorder.setVideoSize(1280,720);
 			recorder.setVideoFrameRate(30);
 			recorder.setVideoEncodingBitRate(6000000);
 			recorder.setMaxDuration(3000);	
 			recorder.setOnInfoListener(this);
 			recorder.setOutputFile(promisedPath+idx+".mp4"); 
 			recorder.setPreviewDisplay(holder.getSurface());
 			recorder.prepare();
 			recorder.start();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void stop() {
 		Log.v(cam_tag, "stop");		
 		recorder.stop();
 		recorder.release();
 		finish();
 	}
 }
