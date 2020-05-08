 package cs407.snapendar.main;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 
 
 import cs407.snapendar.main.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 //TODO: Make it so that when the screen is rotated we stay in the same state (i.e. with camera,
 //TODO: with picture, etc.)
 //TODO: Need to save data across the "Activity Lifetime Cycle" i.e onCreate/onDestroy
 //TODO: http://developer.android.com/guide/topics/resources/runtime-changes.html
 
 public class MainActivity extends HawaiiBaseAuthActivity {
 	private static final int SELECT_IMAGE = 2888;
 
 	protected ImageView imageView;
 	protected ProgressBar progressBar;
 	protected LinearLayout resultContainer;
 	protected TextView ocrResultView;
 	protected SurfaceView camSurface;
 
 	protected Button loadButton;
 	protected Button captureButton;
 	protected Button shutterButton;
 
 	Preview preview;
 	Camera camera;
 	String fileName;
 	Activity act;
 	Context ctx;
 
 	protected Calendar chronicCalendar;
 
 	/* Class variable to represent the "photo" captured by the camera */
 	protected Bitmap photo = null;
 
 	/* Task for calling the Project Hawaii OCR to keep it off the main thread */
 	protected AsyncTask<Void, Integer, AlertDialog.Builder> currentOcrTask;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		/* Check if something was saved during onSaveInstanceState() */
 		if(savedInstanceState != null) {
 			photo = savedInstanceState.getParcelable("bitmap");
 		}
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		/* Setup all the class members from the view objects*/
 		this.progressBar = (ProgressBar) this.findViewById(R.id.ocr_progressbar);
 		this.imageView = (ImageView) this.findViewById(R.id.imageView);
 		this.camSurface = (SurfaceView)findViewById(R.id.surfaceView);
 
 		this.captureButton = (Button) this.findViewById(R.id.CaptureImgBtn);
 		this.loadButton = (Button) this.findViewById(R.id.LoadImgBtn);
 		this.shutterButton = (Button) this.findViewById(R.id.SnapItBtn);
 
 		this.resultContainer = (LinearLayout) this.findViewById(R.id.ocr_result_container);
 		this.ocrResultView = (TextView) this.findViewById(R.id.ocrResult_textview);
 		this.ocrResultView.setTextSize(25);
 
 		/* Setup some stuff for the camera preview */
 		ctx = this;
 		act = this;
 		//requestWindowFeature(Window.FEATURE_NO_TITLE); //Removes title bar at top
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		preview = new Preview(this, camSurface);
 		preview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		((FrameLayout) findViewById(R.id.preview)).addView(preview);
 		preview.setKeepScreenOn(true);
 
 		/* Setup the OnClickListeners for each button of the UI */
 
 		this.captureButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				captureButton.setVisibility(View.GONE);
 				imageView.setVisibility(View.GONE);
 				resultContainer.setVisibility(View.GONE);
 				shutterButton.setVisibility(View.VISIBLE);
 				camSurface.setVisibility(View.VISIBLE);
 			}
 		});
 
 		this.loadButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Create a new Intent to open the picture selector
 				Intent loadPicture = new Intent(
 						Intent.ACTION_PICK,
 						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				startActivityForResult(loadPicture, SELECT_IMAGE);
 			}
 		});
 
 		shutterButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
 			}
 		});
 
 		shutterButton.setOnLongClickListener(new OnLongClickListener(){
 			@Override
 			public boolean onLongClick(View arg0) {
 				camera.autoFocus(new AutoFocusCallback(){
 					@Override
 					public void onAutoFocus(boolean arg0, Camera arg1) {
 						camera.takePicture(shutterCallback, rawCallback, jpegCallback);
 					}
 				});
 				return true;
 			}
 		});
 
 		/* First-run test code for adding a new event to the calendar. May want to 
 		 * write our own calendar insertion code instead of using an Intent. Needed
 		 * to change the Min Android version to 4.0 for this to work. */
 		/*
 		this.setEventButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if(chronicCalendar != null) {
 					Intent intent = new Intent(Intent.ACTION_INSERT)
 				        .setData(Events.CONTENT_URI)
 				        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, chronicCalendar.getTimeInMillis())
 				        .putExtra(Events.TITLE, "My Event on " + (chronicCalendar.get(Calendar.MONTH)+1) + "/" + chronicCalendar.get(Calendar.DAY_OF_MONTH))
 				        //.putExtra(Events.DESCRIPTION, "Super cool thing")
 				        //.putExtra(Events.EVENT_LOCATION, "CS 1240")
 				        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
 				startActivity(intent);
 				}
 				// TODO: Might want to throw a pop-up/error or something here
 			}
 		});
 		 */
 
 		/* TODO: We're probably not going to use this anymore since we jump right to camera */
 		/*if(photo == null) {
 			this.imageView.setImageResource(R.drawable.example2);
 		}
 		else {
 			this.imageView.setImageBitmap(photo);
 		}*/
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		/* If the user pauses (i.e. leaves) the application during the OCR process it's
 		 * safest to cancel the OCR task altogether */
 		AsyncTask<Void, Integer, AlertDialog.Builder> task = this.currentOcrTask;
 		if (task != null) {
 			task.cancel(true);
 			this.currentOcrTask = null;
 		}
 
 		if(camera != null) {
 			camera.stopPreview();
 			preview.setCamera(null);
 			camera.release();
 			camera = null;
 		}
 	}
 
 	/* We need to save the currently taken/selected photo between configuration changes
 	 * (e.g. screen rotation) because any config change destroys and recreates the activity */
 	@Override
 	protected void onSaveInstanceState(Bundle bundle) {
 		super.onSaveInstanceState(bundle);
 		bundle.putParcelable("bitmap", photo);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		// If our Intent calls were a great success
 		if (resultCode == Activity.RESULT_OK && data != null) {
 			if (requestCode == SELECT_IMAGE) {
 				// This gets the URI of the image the user selected
 				Uri imgUri = data.getData();
 				this.imageView.setImageURI(imgUri);
 
 				/* Convert the URI to a Bitmap we can store; may break if 
 				 * the image of the URI is large*/
 				try {
 					photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
 				}
 				catch(IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 
 			beginOcr();
 		}
 	}
 
 	public void beginOcr(){
 		shutterButton.setVisibility(View.GONE);
 		captureButton.setVisibility(View.VISIBLE);
 		camSurface.setVisibility(View.GONE);
 		imageView.setVisibility(View.VISIBLE); //Make the image view visible.
 		resultContainer.setVisibility(View.VISIBLE); 
 		
 		currentOcrTask = new OcrTask(this);
 		currentOcrTask.execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 
 		//preview.mCamera = Camera.open();
 
 		camera = Camera.open();
 		preview.setCamera(camera);
 		//	Log.v("CAM", "HELLEOROROASD");
		
		camera = Camera.open();
		preview.setCamera(camera);
 
 		camera.startPreview(); //Crashes on this line
 
 	}
 
 
 	private void resetCam() {
 		camera.startPreview();
 		preview.setCamera(camera);
 	}
 
 	ShutterCallback shutterCallback = new ShutterCallback() {
 		public void onShutter() {
 			// Log.d(TAG, "onShutter'd");
 		}
 	};
 
 	PictureCallback rawCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			// Log.d(TAG, "onPictureTaken - raw");
 		}
 	};
 
 	PictureCallback jpegCallback = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 			FileOutputStream outStream = null;
 			try {
 				// Write to External Storage
 				// TODO: Use a non-hardcoded path; also, check if external storage is available
 				// TODO: http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
 				// File path = Environment.getExternalStoragePublicDirectory(
 	            // 		Environment.DIRECTORY_PICTURES);
 	            // File file = new File(path, "DemoPicture.jpg");
 
 				fileName = String.format("/sdcard/camtest/%d.jpg", System.currentTimeMillis());
 				outStream = new FileOutputStream(fileName);
 				outStream.write(data);
 				outStream.close();
 				Log.d("snap", "onPictureTaken - wrote bytes: " + data.length);
 
 				InputStream is = new ByteArrayInputStream(data);
 				Bitmap bmp = BitmapFactory.decodeStream(is);
 
 				imageView.setImageBitmap(bmp);
 				beginOcr();
 
 
 				resetCam();
 
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 			}
 			Log.d("snap", "onPictureTaken - jpeg");
 		}
 	};
 }
