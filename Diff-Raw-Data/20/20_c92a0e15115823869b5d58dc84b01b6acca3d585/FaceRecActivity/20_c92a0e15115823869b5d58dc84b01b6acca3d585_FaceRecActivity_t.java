 package com.wingman;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Rect;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.List;
 import java.util.Random;
 import com.github.mhendred.face4j.model.Face;
 import com.github.mhendred.face4j.model.Photo;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.wingman.models.PersonModel;
 import com.wingman.parsers.FaceComWrapper;
 
 public class FaceRecActivity extends Activity implements OnTouchListener{
 
 	private static final String TAG = "FaceRecActivity";
 	private static final String IMAGE_PATH = "/sdcard/wingman/LatestWingman.jpg";
 	//private static final String MY_AD_UNIT_ID = "a14e50830f3a4d4";
 	private static Photo photo = null;
 	//private AdView adView;
 	
 	
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		Log.e(TAG, "onCreate");
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.facerec);
 		
 		
 		
 		// Setup touch listener, to detect x,y coordinates of touch events
 		ImageView image = (ImageView) findViewById(R.id.imageView1);
 		image.setOnTouchListener(this);
 		
 		// Display "psuedo" debug msg's
 		Context context = getApplicationContext();
 		Resources res = getResources();
 		String text = new String();
 		Random generator = new Random();
 		int rand = generator.nextInt(3) +1;
 		switch(rand){
 		case 1: text = res.getString(R.string.progressText1);
 				break;
 		case 2: text = res.getString(R.string.progressText2);
 				break;
 		case 3: text = res.getString(R.string.progressText3);
 				break;
 		case 4: text = res.getString(R.string.progressText4);
 				break;
 		}
 
 		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
 		toast.show();
 		new RecognitionTask().execute(IMAGE_PATH);
 	}
 
 	class RecognitionTask extends AsyncTask<String, Void, Photo> {
 		private static final String IMAGE_PATH_DRAWN = "/sdcard/wingman/WingDraw.jpg";
 
 	    protected Photo doInBackground(String... urls) {
     		FaceComWrapper fc = new FaceComWrapper();
     		photo = fc.detectAndDrawFaces();
     		return photo;
 	    }
 
 		// Execute in UI thread after task completes
 		protected void onPostExecute(Photo p) {
 			photo = p;
			
 			//TextView tv = (TextView) findViewById(R.id.textView1);
 		    ImageView image = (ImageView) findViewById(R.id.imageView1);
 			
		    // Stop progress bars / loading...'s
		    ProgressBar pd = (ProgressBar) FaceRecActivity.this.findViewById(R.id.progressBar1);
 		    pd.setEnabled(false);
 			pd.setWillNotDraw(true);
 			
			TextView tv = (TextView) findViewById(R.id.textView1);
			tv.setWillNotDraw(true);
			tv.setEnabled(false);
			
 			if(photo != null)
 			{
 		        Bitmap bmImage = BitmapFactory.decodeFile(IMAGE_PATH_DRAWN);
 		        image.setImageBitmap(bmImage);
 		        
 		        CharSequence text = "";
 		        if(photo.getFaceCount() > 0)
 		        {
 		        	text = "Faces found: " + photo.getFaceCount() + ". Please select which face to recognize.";
 		        } else {
 		        	text = "ERROR! No faces found, please try again.";
 		        }
 		        Toast toast = Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG);
 				toast.show();
 
 			} else{
				tv = (TextView) findViewById(R.id.textView1);
				tv.setWillNotDraw(false);
				tv.setEnabled(true);
 				tv.setText("Not found/Error!. Please try again");
 			}
 		}
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	protected void onResume() {
 		Log.e(TAG, "onResume");
 		super.onResume();
 	}
 
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	protected void onStop() {
 		Log.e(TAG, "onStop");
 		super.onStop();
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		int selectedFace = 9999;
 		
 		// Find input coordinates
 		float inputRawX = arg1.getRawX();
 		float inputRawY = arg1.getY();
 
 		// Get dimensions of the ImageView presented on the screen
 		Rect rect = new Rect();
 		arg0.getDrawingRect(rect);
 
 		/* Find the coefficient (delta) between the "old" image, which is the bitmap file
 		 * that was used for drawing the rectangles, and
 		 * the "new" which is the ImageView in which the bitmap is being presented.
 		 * We need this, in order to detect when a user presses within any of the rectangles,
 		 * that has previously been drawn on the bitmap.
 		 */
 		Photo myPhoto = photo;
 		float orgW = (float)myPhoto.getWidth();
 		float orgH = (float)myPhoto.getHeight();
 		float newW = (float)arg0.getMeasuredWidth();
 		float newH = (float)arg0.getMeasuredHeight();
 		
 		float deltaW = (orgW / newW);
 		float deltaH = (orgH / newH);
 		
 		// A "face" in the list, faces, contains the rectangle that was drawn to bitmap.
 		//myPhoto.scaleFaceRects(newW, newH);
 		int faceCount = myPhoto.getFaceCount();
 		List<Face> faces = myPhoto.getFaces();
 		
 		// Traverse the list, to see if the inputted x,y coordinates are within any of the
 		// rectangles drawn on the bitmap.
 		for(int i=0; i < faceCount; i++)
 		{
 			// We use the deltas to scale the proportions of the rectangles, such that
 			// the rectangles on the image essentially becomes clickable buttons
 			float top = faces.get(i).getRectangle().top;
 			top = (top / deltaH);
 			float bottom = faces.get(i).getRectangle().bottom;
 			bottom = (bottom / deltaH);
 			float right = faces.get(i).getRectangle().right;
 			right = (right / deltaW);
 			float left = faces.get(i).getRectangle().left;
 			left = (left / deltaW);
 			
 			// If the input was within a rectangle, which rectangle was it?
 			if( (inputRawX >= left) && (inputRawX <= right) && (inputRawY >= top) && (inputRawY <= bottom))
 			{
 				selectedFace = i;
 			}
 		}
 		
 		if(selectedFace != 9999)
 		{
 			Intent myIntent = new Intent(getBaseContext(), PersonActivity.class);
 			PersonModel pm = new PersonModel(photo, selectedFace);
 			//pm.setPhoto(myPhoto);
 			
 			myIntent.putExtra("PersonModel", pm);
 			startActivity(myIntent);
 		}
 		
 		return false;
 	}
 }
