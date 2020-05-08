 package com.project.eden;
  
 
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ImageFormat;
 import android.graphics.Paint;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 import android.support.v4.util.LruCache;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 //face detection using android face detector
 import android.media.FaceDetector;
 import android.media.FaceDetector.Face;
 
 //Java IO buffer utils
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 
 //Java OpenCV Wrapper Library
 import com.googlecode.javacpp.Loader;
 import com.googlecode.javacv.cpp.opencv_objdetect;
 import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
 import com.googlecode.javacv.cpp.opencv_core.CvRect;
 import com.googlecode.javacv.cpp.opencv_core.CvSeq;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.*;
 import static com.googlecode.javacv.cpp.opencv_objdetect.*;
 import static com.googlecode.javacv.cpp.opencv_highgui.*;
 
 
 @SuppressLint("NewApi")
 public class EdenCamActivity extends Activity {
 	
     //memory cache for bitmap
     private LruCache<String, Bitmap> mMemoryCache;
 	
     private FrameLayout layout;
     //face recognition following regular OpenCV process
     private FaceRecognition faceReco;
     //action class after face is captured.
     private FaceCapture faceCapture;
     
     //queue for the bitmap memory-cached keys
    public Queue<String> cachedKeyQueue;
     public int MAXCACHENUM = 5;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         //caching setup 
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
         final int cacheSize = maxMemory / 8;
         mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
             @Override
             protected int sizeOf(String key, Bitmap bitmap) {
                 // The cache size will be measured in kilobytes rather than
                 // number of items.
                 return bitmap.getByteCount() / 1024;
             }
         };
 		
         // Hide the window title.
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         super.onCreate(savedInstanceState);
 
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         // Create our Preview view and set it as the content of our activity.
         try {
             layout = new FrameLayout(this);
             faceReco = new FaceRecognition(this);
             faceCapture = new FaceCapture(this, faceReco);
             layout.addView(faceCapture);
             layout.addView(faceReco);
             setContentView(layout);
         } catch (IOException e) {
             e.printStackTrace();
             new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
         }
     }
 	
     //bitmap memory cache handling
     //Current: the keys are numbers 
     //TODO: the keys should be the meta data scraped by the facebook API
     public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
         if (getBitmapFromMemCache(key) == null) {
             mMemoryCache.put(key, bitmap);
 	        
         }
     }
 
     public Bitmap getBitmapFromMemCache(String key) {
         return mMemoryCache.get(key);
     }
 	
     public Bitmap removeBitmapFromMemCache(String key) {
     	return mMemoryCache.remove(key);
     }
     
 }
 
 class FaceRecognition extends View implements Camera.PreviewCallback {
 	
     public static final int SUBSAMPLING_FACTOR = 4;
 
     //OpenCV face detection
     private IplImage grayImage;
     private CvHaarClassifierCascade classifier;
     private CvMemStorage storage;
     private CvSeq faces;
     EdenCamActivity eContext;
     
     public FaceRecognition(EdenCamActivity context) throws IOException {
         super(context);
         eContext = context;
         // Load the classifier file from Java resources.
         File classifierFile = Loader.extractResource(getClass(),
                                                      "/com/project/eden/haarcascade_frontalface_alt.xml",
                                                      context.getCacheDir(), "classifier", ".xml");
         if (classifierFile == null || classifierFile.length() <= 0) {
             throw new IOException("Could not extract the classifier file from Java resource.");
         }
 
         // Preload the opencv_objdetect module to work around a known bug.
         Loader.load(opencv_objdetect.class);
         classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
         classifierFile.delete();
         if (classifier.isNull()) {
             throw new IOException("Could not load the classifier file.");
         }
         storage = CvMemStorage.create();
     }
 
     public void onPreviewFrame(final byte[] data, final Camera camera) {
         try {
             Camera.Size size = camera.getParameters().getPreviewSize();
             processImage(data, size.width, size.height);
             camera.addCallbackBuffer(data);
         } catch (RuntimeException e) {
             // The camera has probably just been released, ignore.
         }
     }
     
     
     	
     protected void processImage(byte[] data, int width, int height) {
         // First, downsample our image and convert it into a grayscale IplImage
         int f = SUBSAMPLING_FACTOR;
         if (grayImage == null || grayImage.width() != width/f || grayImage.height() != height/f) {
             grayImage = IplImage.create(width/f, height/f, IPL_DEPTH_8U, 1);
         }
         int imageWidth  = grayImage.width();
         int imageHeight = grayImage.height();
         int dataStride = f*width;
         int imageStride = grayImage.widthStep();
         ByteBuffer imageBuffer = grayImage.getByteBuffer();
         for (int y = 0; y < imageHeight; y++) {
             int dataLine = y*dataStride;
             int imageLine = y*imageStride;
             for (int x = 0; x < imageWidth; x++) {
                 imageBuffer.put(imageLine + x, data[dataLine + f*x]);
             }
         }
 
         cvClearMemStorage(storage);
         faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
         postInvalidate();
     }
 
     //collect face coordinates
     public List<int[]> getFaceCoordX() {
     	List<int[]> fcoords = new ArrayList<int[]>();
     	
     	//float scaleX = (float)getWidth()/grayImage.width();
         //float scaleY = (float)getHeight()/grayImage.height();
     	
     	if (faces != null) {
             int total = faces.total();
             for (int i = 0; i < total; i++) {
                 CvRect r = new CvRect(cvGetSeqElem(faces, i));
                 int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                 //lower bound x, lower bound y, upper bound x, upper bound y
                 int[] coord = new int[4];
                 coord[0] = x;
                 coord[1] = y;
                 coord[2] = x+w;
                 coord[3] = y+h;
                 fcoords.add(coord);
             }
     	}
     	return fcoords;
     }
     
     @Override
     protected void onDraw(Canvas canvas) {
     	//Draw the icon images 
     	
     	Paint paint = new Paint();
         paint.setColor(Color.BLUE);
         paint.setTextSize(40);
         
        if (eContext.getBitmapFromMemCache(String.valueOf(0)) != null && faces != null) {
        	//align the icons from images cached in memory
        	
        }
        String s = "Project Eden Baby";
         float textWidth = paint.measureText(s);
         canvas.drawText(s, (getWidth()-textWidth)/2, 40, paint);
         
         if (faces != null) {
             float padding = 20;
             paint.setStrokeWidth(4);
             paint.setStyle(Paint.Style.STROKE);
             float scaleX = (float)getWidth()/grayImage.width();
             float scaleY = (float)getHeight()/grayImage.height();
             float topLeftX, topLeftY, botRightX, botRightY;
             float faceWidth;
             float x,y,w,h;
             float boxWidth = 150;
             float boxHeight = 150;
             int total = faces.total();
             for (int i = 0; i < total; i++) {
                 CvRect r = new CvRect(cvGetSeqElem(faces, i));
                 x = r.x(); y = r.y(); w = r.width(); 
                 h = r.height();
                 topLeftX = x*scaleX;
                 topLeftY = y*scaleY;
                 botRightX = (x+w)*scaleX;
                 botRightY = (y+h)*scaleY;
                 faceWidth = botRightX - topLeftX;
                 paint.setColor(Color.GREEN);
                 canvas.drawRect(topLeftX, topLeftY, botRightX, botRightY, paint);
                 // Draw on left or right of face
                 paint.setColor(Color.BLUE);
                 if ((topLeftX + boxWidth + faceWidth + padding) < (float)getWidth()) {
                     canvas.drawRect(topLeftX + faceWidth + padding, topLeftY, 
                                     topLeftX + boxWidth + faceWidth + padding, topLeftY + boxHeight, paint);
                 }
                 else {
                     canvas.drawRect(topLeftX - boxWidth - padding, topLeftY, 
                                     botRightX - faceWidth - padding, topLeftY + boxHeight, paint);
                 }
          
             }
         
         }
     }
 }
 class FaceCapture extends SurfaceView implements SurfaceHolder.Callback {
     SurfaceHolder mHolder;
     Camera mCamera;
     Camera.PreviewCallback previewCallback;
     EdenCamActivity mainContext;
     private int keyCounter = 0;
     FaceCapture(Context context, Camera.PreviewCallback previewCallback) {
         super(context);
         mainContext = (EdenCamActivity) context;
         this.previewCallback = previewCallback;
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         mHolder = getHolder();
         mHolder.addCallback(this);
     }
     
     //touch the designated area to trigger picture event
     @Override
     public boolean onTouchEvent(MotionEvent e) {
         //report touch position
         float x = e.getX();
         float y = e.getY();
         
         //retrieve desired position
         
         int action1 = e.getAction();
         //take picture when the pressure releases
         if (action1 == MotionEvent.ACTION_UP) {
             mCamera.takePicture(null, null, pictureCallback);
         }
     	
         return true;
     }
     
     //picture callback set up 
     PictureCallback pictureCallback = new PictureCallback() {
 
             @Override
             public void onPictureTaken(byte[] data, Camera camera) {
                 // Picture processing
                 Log.d("Camera","Icon Picture Taken! Load the image to Cache!");
                 mCamera.startPreview();
 			
                 //decode raw bytes to bitmap format
                 Bitmap loadImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                 /*************************/
                 /* Image Comparison TODO */
                 /*************************/
                 
                 int qSize = mainContext.cachedKeyQueue.size();
                 
                 if (qSize != mainContext.MAXCACHENUM) {
                 	//add the preview frame to cache
                 	mainContext.addBitmapToMemoryCache(String.valueOf(keyCounter), loadImage);
                 	mainContext.cachedKeyQueue.add(String.valueOf(keyCounter));
                 } else {
                 	//shift the list from top to bottom
                 	//remove the early preview and add the lastest one
                 	String removeKey = mainContext.cachedKeyQueue.poll();
                 	mainContext.removeBitmapFromMemCache(removeKey);
                 	
                 	//add new key pair at the end
                 	mainContext.addBitmapToMemoryCache(String.valueOf(keyCounter), loadImage);
                 	mainContext.cachedKeyQueue.add(String.valueOf(keyCounter));
                 }
                 keyCounter++;
             }
         };
 
     public void surfaceCreated(SurfaceHolder holder) {
         // The Surface has been created, acquire the camera and tell it where
         // to draw.
         mCamera = Camera.open();
         try {
             mCamera.setPreviewDisplay(holder);
         } catch (IOException exception) {
             mCamera.release();
             mCamera = null;
 
         }
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         // Surface will be destroyed when we return, so stop the preview.
         // Because the CameraDevice object is not a shared resource, it's very
         // important to release it when the activity is paused.
         mCamera.stopPreview();
         mCamera.release();
         mCamera = null;
     }
 
 
     private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
         final double ASPECT_TOLERANCE = 0.05;
         double targetRatio = (double) w / h;
         if (sizes == null) return null;
 
         Size optimalSize = null;
         double minDiff = Double.MAX_VALUE;
 
         int targetHeight = h;
 
         // Try to find an size match aspect ratio and size
         for (Size size : sizes) {
             double ratio = (double) size.width / size.height;
             if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
             if (Math.abs(size.height - targetHeight) < minDiff) {
                 optimalSize = size;
                 minDiff = Math.abs(size.height - targetHeight);
             }
         }
 
         // Cannot find the one match the aspect ratio, ignore the requirement
         if (optimalSize == null) {
             minDiff = Double.MAX_VALUE;
             for (Size size : sizes) {
                 if (Math.abs(size.height - targetHeight) < minDiff) {
                     optimalSize = size;
                     minDiff = Math.abs(size.height - targetHeight);
                 }
             }
         }
         return optimalSize;
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // Now that the size is known, set up the camera parameters and begin
         // the preview.
         Camera.Parameters parameters = mCamera.getParameters();
 
         List<Size> sizes = parameters.getSupportedPreviewSizes();
         Size optimalSize = getOptimalPreviewSize(sizes, w, h);
         parameters.setPreviewSize(optimalSize.width, optimalSize.height);
 
         mCamera.setParameters(parameters);
         if (previewCallback != null) {
             mCamera.setPreviewCallbackWithBuffer(previewCallback);
             Camera.Size size = parameters.getPreviewSize();
             byte[] data = new byte[size.width*size.height*
                                    ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
             mCamera.addCallbackBuffer(data);
         }
         mCamera.startPreview();
     }
 
 }
