 /* This program is used to identify type of a flower via taking a photo.
  * User takes a photo of an unknown flower then this program list top 5 most likely.
  * 
  * Written at 2554-7-4 by Rungkarn Siricharoenchai Project manager.
  * 						  Treepop Sunpetchniyom	   TU Master.
  *  
  * Version 1.7
  * Platform Android
  *  
  * History
  * -------
  * 1.0 Original.
  * 1.1 7 July 54 Preview image was wrong orientation but when it was corrected,
  * the take picture button was wrong orientation instead.
  * This bug was solved.
  * 1.2 7 July 54 I think macro wasn't effect.
  * 1.3 20 July 54 Write file to SD CARD.
  * 1.4 21 July 54 Fix bug,the saved image file was still wrong orientation.
  * 1.5 try to use macro focus but not work.
  * 1.6 Rotate saved image.
  * 1.7 24 July 54 Fix bug when rotated photo not align.
  * Edit error handling. 
  * 
  * Future work
  * -----------
  * 
  * Abbreviation for any comment.
  * 
  */
 
 package com.img.jk.beethelion;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Bitmap.CompressFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class mainActivity extends Activity implements SurfaceHolder.Callback {
 	
 	private Camera x10Camera;
 	private byte[] jpegData;
 	private boolean mPreviewRunning = false;
 	private Button macroBtn;
 	private boolean inMacro = false;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // Connect SurfaceView.
         SurfaceView x10SurfaceView;
         SurfaceHolder x10SurfaceHolder;
         x10SurfaceView = (SurfaceView)findViewById(R.id.surface);
         x10SurfaceHolder = x10SurfaceView.getHolder();
         x10SurfaceHolder.addCallback(this);
         x10SurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         
         // Display overlay.
         LayoutInflater mInflater = null;
         mInflater = LayoutInflater.from(this);
         View overlayView = mInflater.inflate(R.layout.cameraoverlay, null);
         this.addContentView(overlayView,
         		new LayoutParams(LayoutParams.FILL_PARENT,
         				LayoutParams.FILL_PARENT));
         
         // Connect take picture button.
         Button takePictureBtn;
         takePictureBtn = (Button)findViewById(R.id.button);
         takePictureBtn.setOnClickListener(new OnClickListener() {
         	
         	@Override
         	public void onClick(View v) {
 				 x10Camera.takePicture(previewCallback, rawCallback, jpegCallback);
 			}
 		});
         
         // Connect macro button.
         macroBtn = (Button)findViewById(R.id.macroBtn);
         macroBtn.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(inMacro) {
 					x10Camera.stopPreview();
 					Camera.Parameters x10Parameters = x10Camera.getParameters();
 					x10Parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
 					x10Camera.setParameters(x10Parameters);
 					macroBtn.setText("Auto Focus");
 					inMacro = false;
 					x10Camera.startPreview();
 				} else {
 					x10Camera.stopPreview();
 					Camera.Parameters x10Parameters = x10Camera.getParameters();
 					x10Parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
 					x10Camera.setParameters(x10Parameters);
 					macroBtn.setText("Macro");
 					inMacro = true;
 					x10Camera.startPreview();
 				}
 			}
 		});
         
         /*This line has bug. The screen has wrong object orientation.
         This following command doesn't correct the orientation bug.
         this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);*/
     }
     
     ShutterCallback previewCallback = new ShutterCallback() {
     // For manipulate preview image.
 		
 		@Override
 		public void onShutter() {}
 	};
 	
     PictureCallback rawCallback = new PictureCallback() {
     // For manipulate raw image.
 		
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {}
 	};
 	
 	PictureCallback jpegCallback = new PictureCallback() {
 	// For manipulate jpeg image.
 		
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 			if(data != null) {
 				
 				// Rotate image.
 				Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
 				Bitmap rotatedBmp = Bitmap.createBitmap(bmp.getHeight(),
 						bmp.getWidth(), bmp.getConfig());
 						// Interchange width and height.
 				Canvas canvas = new Canvas(rotatedBmp);
 				Paint paint = new Paint();
 				
 				Matrix matrix = new Matrix();
 				matrix.setRotate(90);
 				matrix.postTranslate(bmp.getHeight(), 0);
 				canvas.drawBitmap(bmp, matrix, paint);
 				// -------------
 				
 				jpegData = data;
				String fNameUnknowFlower = "unknowFlower.jpg";
 				File sdDir = Environment.getExternalStorageDirectory();
 				if(sdDir.exists()) {
 					if(sdDir.canWrite()) {
 						File beeDir = new File(sdDir.getAbsolutePath()+"/beeTheLion");
 						if(!beeDir.exists()) {
 							beeDir.mkdir();
 						}
 						if(beeDir.exists() && beeDir.canWrite()) {
							File fileUnknowFlower = new File(beeDir.getAbsolutePath()+
									"/"+fNameUnknowFlower);
 							try {
								fileUnknowFlower.createNewFile();
 							} catch(IOException e) {
 								Toast.makeText(mainActivity.this,
 										e.getMessage(),
 										Toast.LENGTH_LONG).show();
 								Log.e("jkError", e.getMessage());
 							}
							if(fileUnknowFlower.exists() &&
									fileUnknowFlower.canWrite()) {
 								// FileOutputStream fos = null;
 								BufferedOutputStream fos = null;
 								try {
 									fos = new BufferedOutputStream(
											new FileOutputStream(fileUnknowFlower));
 									// fos.write(jpegData);
 									rotatedBmp.compress(CompressFormat.JPEG, 100, fos);
 								} catch(FileNotFoundException e) {
 									Toast.makeText(mainActivity.this,
 											e.getMessage(),
 											Toast.LENGTH_LONG).show();
 									Log.e("jkError", e.getMessage());
 								} finally {
 									if(fos != null) {
 										try {
 											fos.flush();
 											fos.close();
 										} catch (IOException e) {
 											Toast.makeText(mainActivity.this,
 													e.getMessage(),
 													Toast.LENGTH_LONG).show();
 											Log.e("jkError", e.getMessage());
 										}
 									}
 								}
 							}
 						}
 					} else {
 						// SD CARD can't write!
 						Toast.makeText(mainActivity.this, "SD CARD can't write!",
 								Toast.LENGTH_LONG).show();
 						Log.e("jkERROR", "SD CARD can't write!");
 					}
 				} else {
 					// SD CARD not found!
 					Toast.makeText(mainActivity.this, "SD CARD not found!",
 							Toast.LENGTH_LONG).show();
 					Log.e("jkERROR", "SD CARD not found!");
 				}
 				done();
 			}
 		}
 	};
 	
 	void done() {
 		finish(); // Exit program.
 	}
 	
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		x10Camera = Camera.open();
 		Camera.Parameters x10Parameters = x10Camera.getParameters();
         
 		// Set focus to auto focus mode.
         List<String> x10FocusMode = x10Parameters.getSupportedFocusModes();
         if(x10FocusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO))
         	x10Parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
 		
 		// Set focus to macro mode.
         /*List<String> x10FocusMode = x10Parameters.getSupportedFocusModes();
         if(x10FocusMode.contains(Camera.Parameters.FOCUS_MODE_MACRO))
         	x10Parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);*/
         
         // Set picture size.
         List<Camera.Size> x10PicSizeMode = x10Parameters.getSupportedPictureSizes();
         Iterator<Camera.Size> iterPicSizeMode = x10PicSizeMode.iterator();
         while(iterPicSizeMode.hasNext()) {
         	Camera.Size item = iterPicSizeMode.next();
         	if(item.width == 640 && item.height == 480) {
         		x10Parameters.setPictureSize(640, 480);
         	}
         }
         
         // Set preview frame rate.
         List<Integer> x10PreviewFrameRate = x10Parameters
         	.getSupportedPreviewFrameRates();
         if(x10PreviewFrameRate.contains(30))
         	x10Parameters.setPreviewFrameRate(30);
         
         // Set preview size.
         List<Camera.Size> x10PreSizeMode = x10Parameters.getSupportedPreviewSizes();
         Iterator<Camera.Size> iterPreSizeMode = x10PreSizeMode.iterator();
         while(iterPreSizeMode.hasNext()) {
         	Camera.Size item = iterPreSizeMode.next();
         	if(item.width == 320 && item.height == 240) {
         		x10Parameters.setPreviewSize(320, 240);
         	}
         }
         
         
         // Fix bug wrong orientation.
         if(this.getResources().getConfiguration().orientation !=
         	Configuration.ORIENTATION_LANDSCAPE) {
         	// This is an undocumented although widely known feature.
         	x10Parameters.set("orientation", "portrait");
         	
         	// For Android 2.2 and above
        	//x10Camera.setDisplayOrientation(90);
         	
         	// Uncomment for Android 2.0 and above
         	//x10Parameters.setRotation(90);
         } else {
         	// This is an undocumented although widely known feature.
         	x10Parameters.set("orientation", "landscape");
         	
         	// For Android 2.2 and above
        	//x10Camera.setDisplayOrientation(0);
         	
         	// Uncomment for Android 2.0 and above
         	//x10Parameters.setRotation(0);
         }
         
         x10Camera.setParameters(x10Parameters);
         try {
         	x10Camera.setPreviewDisplay(holder);
 		} catch (IOException exception) {
 			x10Camera.release();
 			Toast.makeText(mainActivity.this, exception.getMessage(), Toast.LENGTH_LONG)
 				.show();
 			Log.e("jkError", exception.getMessage());
 		}
 		x10Camera.startPreview();
 		mPreviewRunning = true;
         
         // For debug.
         // =========
         x10Parameters = x10Camera.getParameters();
         
         String infoFocusMode = x10Parameters.getFocusMode();
         Toast.makeText(this, "Focus = " + infoFocusMode, Toast.LENGTH_SHORT).show();
         Size infoPictureSize = x10Parameters.getPictureSize();
         Integer w = infoPictureSize.width;
         Integer h = infoPictureSize.height;
         String strPictureSize = w.toString() + " x " + h.toString();
         Toast.makeText(this, "Pic size = " + strPictureSize, Toast.LENGTH_SHORT).show();
         Integer infoPreviewFrameRate = x10Parameters.getPreviewFrameRate();
         Toast.makeText(this, "Frame rate = " + infoPreviewFrameRate.toString(),
         		Toast.LENGTH_SHORT).show();
         Size infoPreviewSize = x10Parameters.getPreviewSize();
         w = infoPreviewSize.width;
         h = infoPreviewSize.height;
         String strPreviewSize = w.toString() + " x " + h.toString();
         Toast.makeText(this, "Preview size = " + strPreviewSize, Toast.LENGTH_SHORT)
         	.show();
         // =========
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		
 		/*if(mPreviewRunning) {
 			x10Camera.stopPreview();
 			mPreviewRunning = false;
 		}
 		Camera.Parameters p = x10Camera.getParameters();
 		p.setPictureSize(width, height);
 		x10Camera.setParameters(p);
 		x10Camera.startPreview();
 		mPreviewRunning = true;*/
 	}
 	
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		x10Camera.stopPreview();
 		mPreviewRunning = false;
 		x10Camera.release();
 		x10Camera = null;
 	}
 }
