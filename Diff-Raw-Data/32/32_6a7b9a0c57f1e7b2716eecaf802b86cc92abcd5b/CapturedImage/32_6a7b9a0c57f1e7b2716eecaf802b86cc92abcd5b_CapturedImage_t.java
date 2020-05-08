 package com.rohit.bookalive;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.opencv.android.Utils;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 import org.opencv.core.Size;
 import org.opencv.highgui.Highgui;
 import org.opencv.imgproc.Imgproc;
 
 import android.app.ActionBar.LayoutParams;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 
 
 public class CapturedImage {
 	private Mat 	image;
 	private Mat 	backup_image;
 	private Mat 	orig;	// the original db image of the same page
 	public static Mat	H;		// homography matrix orig to image, i.e. Hx will give pt on original image
 	private static final String SD_CARD_PATH 	= Environment.getExternalStorageDirectory().toString();
 	public static final String ROOT_PATH = SD_CARD_PATH + "/Pictures/BookAlive/";
 	private String STOR_PATH 	= ROOT_PATH + "test.jpg";
 	private final String PAGES_STOR = ROOT_PATH + "Pages/";
 	private final String QS_STOR = ROOT_PATH + "Ques/";
 	private final int CAPTURE_IMAGE 	= 1;
 	private final static String TAG = "CapturedImage";
 	
 	SplImageView imageView = null; 			// storing, to use later to find size ratios for clicks
 	RelativeLayout layout = null;
 	private int matchingPage = 1;
 	private Context context;				// store once, when making capture intent
 	private Question q = new Question_Type1();
 	
 	Animator animator;
 	
 	// Modes
 	private final int NONE = 0;
 	private final int ASK = 1;
 	private final int INFO = 2;
 	private final int READ = 3;
 	private int mode = NONE;
 	
 	Information info = null;
 	
 	public CapturedImage(SplImageView mImageView, RelativeLayout mLayout) {
 		imageView = mImageView;
 		layout = mLayout;
 	}
 	
 	public Intent createCaptureIntent(Context context) {
 		this.context = context;
 		File photo = new File(STOR_PATH); photo.delete();
 		Uri photoUri = Uri.fromFile(photo);
 		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
 		return captureIntent;
 	}
 		
 	public void processIntentResult() {
 		image = Highgui.imread(STOR_PATH);
 		Imgproc.resize(image, image, new Size((double)640.0, (double)640.0*image.rows()/image.cols()));
 		Highgui.imwrite(STOR_PATH, image);	
 		backup_image = image.clone();
 		matchingPage = ImageMatcher.match(image);
 		readPageImage();
 		H = new Mat();
 		getQuestion();
 		getInformation();
 	}
 	
 	private void readPageImage() {
 		String fname = Integer.toString(matchingPage) + ".jpg";
 		String path = PAGES_STOR + fname;
 		orig = Highgui.imread(path);
 	}
 	
 
 	private void getQuestion() {
 		// get the question type, make object, from the meta file
 		String fname = QS_STOR + Integer.toString(matchingPage) + ".xml";
 		q = q.readType(fname, this);
 	}
 	
 	private void getInformation() {
 		info = new Information(matchingPage, context);
 	}
 	
 	public void drawTemp() {
 		// TODO remove this fun
 		q.draw();
 	}
 	
 	public void processDev() {
 		// TODO : Remove this function, only for development as a replacement for processIntentResult
		STOR_PATH = SD_CARD_PATH + "/Pictures/BookAlive/" + "test42.jpg";
 		processIntentResult();
 	}
 	
 	public void drawLine(double x1, double y1, double x2, double y2) {
 		// TODO : temporary func, only for testing
 		Log.i(TAG + " H", Double.toString(H.get(0,0)[0]) + " " + Double.toString(H.get(0,1)[0]));
 		Point p = new Point(x1,y1), p2 = new Point(x2,y2);
 		//Core.line(image, p, p2, new Scalar(0,255,0),3);
 		p = Util.getPointOnOrig(H.inv(), p);
 		p2 = Util.getPointOnOrig(H.inv(), p2);
 		Log.i(TAG, Double.toString(p.x)+ " " + Double.toString(p.y)); 
 		Log.i(TAG, Double.toString(p2.x)+ " " + Double.toString(p2.y)); 
 		Core.line(image, p, p2, new Scalar(255,0,0),3);
 		updateImage();
 	}
 	
 	public void drawAnimation() {
 		animator = new Animator(layout, context, matchingPage);
 		animator.animate();			
 	}
 	
 	public void processHomography() {
 		computeHomography(image.getNativeObjAddr(), orig.getNativeObjAddr(), H.getNativeObjAddr());
 	}
 	
 	public void setImageView() {
 		File filePath = new File(STOR_PATH);
 		imageView.setImageDrawable(Drawable.createFromPath(filePath.toString()));
 	}
 	
 	public void updateImage() {
 		Bitmap bmp = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
 		Utils.matToBitmap(image, bmp);
 		imageView.setImageBitmap(bmp);
 	}
 	
 	public void ask() {
 		/* Function to ask the question for this image */
 		mode = ASK;
 		q.ask(context);
 	}
 	
 	private Point mapPointToOrig(Point p) {
 		double vht = imageView.getHeight();
 		double vwd = imageView.getWidth();
 		double iht = image.rows();
 		double iwd = image.cols();
 		p.x = (iwd/vwd)*p.x;
 		p.y = (iht/vht)*p.y;
 		p = Util.getPointOnOrig(H, p);
 		return p;
 	}
 	
 	// TODO remove this static int c = 0;
 	public void getTouch(MotionEvent event) {
 		// TODO remoe thisLog.v(TAG, "touched "+ Integer.toString(c)); c++;
 		/* Function to handle touch input to image */
 		//Toast t = Toast.makeText(context, "Touched at " + Double.toString(x) + " " + Double.toString(y), Toast.LENGTH_SHORT);
 		//t.show();
 		double x = event.getX();
 		double y = event.getY();
 		Point p = new Point(x,y);
 		p = mapPointToOrig(p);
 		if(mode == ASK) {
 			q.clicked(p.x, p.y, event);
 			if(q.checkDone()) {
 				showTip();
 				mode = INFO;
 				drawAnimation();
 			}
 		} else if(mode == INFO) {
 			info.clicked(p.x, p.y, event);
 		}
 	}
 	
 	private void showTip() {
 		q.showTip(context);
 	}
 	
 	private void correctOrientation() {
 		BitmapFactory.Options bounds = new BitmapFactory.Options();
         bounds.inJustDecodeBounds = true;
         BitmapFactory.decodeFile(STOR_PATH, bounds);
 
         Bitmap bm = BitmapFactory.decodeFile(STOR_PATH);
         ExifInterface exif = null;
 		try {
 			exif = new ExifInterface(STOR_PATH);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
         String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
         int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
         int rotationAngle = 0;
         if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
         if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
         if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
 
         Matrix matrix = new Matrix();
         matrix.setRotate(rotationAngle);
         Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
         File file = new File(STOR_PATH);
         
         // TODO : not working, gives permission denied
         if(file.exists()) file.delete();
         try {
         	FileOutputStream out = new FileOutputStream(file);
         	rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
         	out.flush();
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 	}
 	
 	/*
 	 * Reverts the image to the original image
 	 */
 	public void revertImage() {
 		image = backup_image.clone();
 		updateImage();
 	}
 	
 	public native void computeHomography(long addrOrig, long addrImage, long addrH);
 }
