 package com.photonoter;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ViewAnimator;
 import co.spark.jajasdk.ConnectionStartedException;
 import co.spark.jajasdk.JajaControlConnection;
 import co.spark.jajasdk.JajaControlListener;
 
 import com.photonoter.imaging.BitmapUtil;
 import com.photonoter.imaging.ImageUtility;
 import com.photonoter.imaging.MediaStoreUtil;
 import com.photonoter.networking.ImageUpload;
 import com.photonoter.networking.ImageUpload.OnImageUploadListener;
 import com.photonoter.view.DrawingSurface;
 import com.tekle.oss.android.animation.AnimationFactory;
 import com.tekle.oss.android.animation.AnimationFactory.FlipDirection;
 
 import de.devmil.common.ui.color.ColorSelectorDialog;
 import de.devmil.common.ui.color.ColorSelectorDialog.OnColorChangedListener;
 
 
 public class AnnotatePhotoActivity extends Activity implements OnImageUploadListener {
 	
 	private static final String LOG_TAG = "AnnotatePhotoActivity";
 			
 	private static final String FILE_URI_PREFIX = "file://"; //$NON-NLS-1$
 
 	public JajaControlConnection jajaConnection;
 
 	private Handler handler;
 	
 	private DrawingSurface photoFrontDrawingSurface;
 	
 	private DrawingSurface photoBackDrawingSurface;
 
 	private String imagePath;
 	
 	private ImageView photo;
 	
 	private ViewAnimator viewAnimator;
 	
 	private View frontView;
 	
 	private boolean showingFront = true;
 	
     private PhotoBackWriterApp app;
     
     public boolean isJajaStarted;
 	
 	private void sendUpdate() {
 		handler.sendMessage(new Message());
 	}
 	
 	OnColorChangedListener colorListener = new OnColorChangedListener() {
 		@Override
 		public void colorChanged(int color) {
 			photoFrontDrawingSurface.setColor(color);
 			photoBackDrawingSurface.setColor(color);
 		}
 	};
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		Log.i(LOG_TAG, "Touch event reached activity. Flipping.");
 		
 		if ( MotionEvent.ACTION_DOWN == event.getAction() ) {
 			flip();
 			return false;
 		}
 		
 		return super.onTouchEvent(event);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         
         app = PhotoBackWriterApp.getApp(this);
 
 		setContentView(R.layout.main);
 		
 		frontView = (View) findViewById(R.id.frontView);
 
 		photoFrontDrawingSurface = (DrawingSurface) findViewById(R.id.drawing_surface);
 		photoBackDrawingSurface = (DrawingSurface) findViewById(R.id.drawing_surface2);
 
 		final Button shareButton = (Button) findViewById(R.id.share_button);
 		shareButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				
 				final Intent intent = createShareIntent();
 				startActivity(intent);
 			}
 		});
 		
 		final Button saveButton = (Button) findViewById(R.id.save_button);
 		saveButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				
 				BitmapUtil.savePing(photoFrontDrawingSurface.getBitmap(), BitmapUtil.getFrontImagePath(AnnotatePhotoActivity.this));
 				BitmapUtil.savePing(photoBackDrawingSurface.getBitmap(), BitmapUtil.getBackPingPath(AnnotatePhotoActivity.this));
 
 				BitmapUtil.saveJpeg(photoBackDrawingSurface.getBitmap(), BitmapUtil.getBackImagePath(AnnotatePhotoActivity.this));
 				
 				final Bitmap frontAnnotated = drawToBitmap();		
 				BitmapUtil.createNonmediaFile();
 				BitmapUtil.saveJpeg(frontAnnotated, BitmapUtil.getCombinedImagePath(AnnotatePhotoActivity.this));
 				
 				BitmapUtil.copyExif(imagePath, BitmapUtil.getCombinedImagePath(AnnotatePhotoActivity.this), 
 						frontAnnotated.getWidth(), frontAnnotated.getHeight());
 				frontAnnotated.recycle();
 				
 				File combinedImage = new File(BitmapUtil.getCombinedImagePath(AnnotatePhotoActivity.this));
 				
 				File backFile = getBackUploadFile();
 				
 				if ( null == backFile ) {
 					new ImageUpload(AnnotatePhotoActivity.this, AnnotatePhotoActivity.this, 
 							new File[] {combinedImage}, null).execute();
 				} else {
 					new ImageUpload(AnnotatePhotoActivity.this, AnnotatePhotoActivity.this, 
 							new File[] {combinedImage, backFile}, null).execute();
 					
 				}
 			}
 		});
 		
 		final Button colors = (Button) findViewById(R.id.color_button);
 		colors.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				new ColorSelectorDialog(AnnotatePhotoActivity.this, colorListener, photoFrontDrawingSurface.getColor()).show();
 			}
 		});
 		
         viewAnimator = (ViewAnimator)this.findViewById(R.id.viewFlipper);
         final Button flipButton = (Button) findViewById(R.id.flip_button);
         flipButton.setOnClickListener(new OnClickListener() { 
 			@Override
 			public void onClick(View v) { 
 				flip();
 			}
         	
         });
 		
 		
 		if ( null != PhotoBackWriterApp.pickedImageId ) {
 			photo = (ImageView) findViewById(R.id.photo);
 
 			// Load image we are writing on.
 			imagePath = MediaStoreUtil.getImageFilePath(this, PhotoBackWriterApp.pickedImageId);
 			Bitmap bitmap = ImageUtility.getBitmapFromLocalPath(imagePath, 1);
 			photo.setImageBitmap(bitmap);
 			
 			// Load writing on the face of the image.
 			final String frontPath = BitmapUtil.getFrontImagePath(AnnotatePhotoActivity.this);
 			final Bitmap front = ImageUtility.getBitmapFromLocalPath(frontPath, 1);
 			photoFrontDrawingSurface.setBitmap(front);
 			
 			// Load writing on the back of the image.
 			final String backPath = BitmapUtil.getBackPingPath(AnnotatePhotoActivity.this);
 			final Bitmap back = ImageUtility.getBitmapFromLocalPath(backPath, 1);
 			photoBackDrawingSurface.setBitmap(back);
 			
 		}
 		
 		Button clearButton = (Button) findViewById(R.id.clear_button);
 		
 		photoFrontDrawingSurface.setRadius(0);
 		photoBackDrawingSurface.setRadius(0);
 
 		handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 
 				String text = "Pressure: "
 						+ (jajaConnection.isSignalAvailable() ? Double.toString(jajaConnection.getSignalValue()) : "---") 
 						+ "\nFirst button:"
 						+ Boolean.toString(jajaConnection.isFirstButtonPressed())
 						+ "\nSecond button:"
 						+ Boolean.toString(jajaConnection.isSecondButtonPressed());
 				
 				if (jajaConnection.getSignalValue() > 0) {
 					int newRadius = (int) Math.max(1,
 							Math.round(jajaConnection.getSignalValue() * 20));
 					photoFrontDrawingSurface.setRadius(newRadius);
 					photoBackDrawingSurface.setRadius(newRadius);
 				} else {
 					photoFrontDrawingSurface.setRadius(0);
 					photoBackDrawingSurface.setRadius(0);
 				}
 				
 				Log.i("UI", text);
 				//tv.setText(text);
 			}
 		};
 
 		clearButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if ( showingFront ) {
 					photoFrontDrawingSurface.clear();
 				} else {
 					photoBackDrawingSurface.clear();
 				}
 			}
 		});
 
 	}
 	
 	private void flip() {
 		// This is all you need to do to 3D flip
 		AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
 		showingFront = !showingFront;	
 	}
 	
 	private void stopJaja() {
 		if (!isJajaStarted) {
 			return;
 		}
 		
 		if(jajaConnection != null) {
 			jajaConnection.stop();
 			isJajaStarted = false;
 		}
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		Log.i(LOG_TAG, "onWindowFocusChanged(). hasFocus = " + hasFocus);
 		super.onWindowFocusChanged(hasFocus);
 		if ( hasFocus ) {
 			if ( app.mPrefs.isJajaEnabled() ) {
 				startJaja();
 			}
 		}
 	}
 
 	@Override
 	public void onPause() {
 		Log.i(LOG_TAG, "onPause()");
 		
 		super.onPause();
 		stopJaja();
 	}
 	
 	private void startJaja() {
 		Log.i(LOG_TAG, "startJaja()");
 		if ( isJajaStarted ) {
 			return;
 		}
 		isJajaStarted = true;
 		jajaConnection = new JajaControlConnection();
 		jajaConnection.setJajaControlListener(new JajaControlListener() {
 
 			@Override
 			public void signalValueChanged(double value) {
 				
 				sendUpdate();
 			}
 
 			@Override
 			public void secondButtonValueChanged(boolean isPressed) {
 				Log.i("UI","secondButtonValueChanged: "+isPressed);
 				sendUpdate();
 			}
 
 			@Override
 			public void firstButtonValueChanged(boolean isPressed) {
 				Log.i("UI","firstButtonValueChanged: "+isPressed);
 				sendUpdate();
 			}
 
 			@Override
 			public void jajaControlSignalLost() {
 				Log.e("UI","jajaControlSignalLost");
 				sendUpdate();						
 			}
 
 			@Override
 			public void jajaControlSignalRestored() {
 				Log.e("UI","jajaControlSignalRestored");						
 			}
 
 			@Override
 			public void jajaControlError() {
 				Log.e("UI","jajaControlError");						
 			}
 		});
 		try {
 			jajaConnection.start();
 		} catch (ConnectionStartedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		Log.i(LOG_TAG, "onDestroy - cleaning up bitmaps");
 		
 		super.onDestroy();
 		photoFrontDrawingSurface.setBitmap(null);
 		photoBackDrawingSurface.setBitmap(null);
 		
 		Drawable drawable = photo.getDrawable();
 		if (drawable instanceof BitmapDrawable) {
 		    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
 		    Bitmap bitmap = bitmapDrawable.getBitmap();
 		    bitmap.recycle();
 		}
 	}
 	
     private Intent createShareIntent() {
     	
 		final Intent share = new Intent(Intent.ACTION_SEND);
 
 		final String screenshot = saveImage();
 		
 		if ( null != screenshot ) {
 				share.setType("image/jpeg");
 		        //share.setType("image/*");
 				share.putExtra(Intent.EXTRA_STREAM,Uri.parse(FILE_URI_PREFIX + screenshot));
 				share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); 
 		} else {
 			share.setType("text/plain");			
 		}
     					
 		final String shareText = "My Notes";
 		
 		share.putExtra(Intent.EXTRA_TEXT, shareText); 
 		share.putExtra("sms_body", shareText);   
 		share.putExtra(Intent.EXTRA_TITLE, shareText);
 		share.putExtra(Intent.EXTRA_SUBJECT, shareText);
 
         share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
         return share;
     }
 	
     public String saveImage() {
     	Bitmap bitmap = drawToBitmap();
     	
     	try {
 
 			BitmapUtil.saveJpeg(bitmap, BitmapUtil.getCombinedImagePath(AnnotatePhotoActivity.this));
 	        
 	        return BitmapUtil.getCombinedImagePath(AnnotatePhotoActivity.this);
 	        
     	} catch (final Exception e) {
         	Log.e(LOG_TAG, "Error saving image.", e); //$NON-NLS-1$
         } finally {
             if ( null != bitmap ) {
                	bitmap.recycle();
             }
         }
     	
     	return null;
     }
     
     public Bitmap drawToBitmap() {
     	
     	final Bitmap bitmap = Bitmap.createBitmap(
     			frontView.getWidth(), 
     			frontView.getHeight(),
                 Bitmap.Config.ARGB_8888);
         final Canvas canvas = new Canvas(bitmap);
         		        
         frontView.draw(canvas);
         return bitmap;
     }
     
     private File getBackUploadFile() {
 		BitmapUtil.copyExif(imagePath, BitmapUtil.getBackImagePath(this), null, null);
 		
 		final String backPath = BitmapUtil.getBackImagePath(this);
 		final File backFile = new File(backPath);
 		if ( !backFile.exists() ) {
 			return null;
 		}    	
 		
 		return backFile;
     }
 
 	@Override
 	public void onImageUploaded(Uri uploadLocation) {
 
 		finish();
 
 		
 	}
 }
