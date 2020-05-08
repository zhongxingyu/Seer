 package com.allplayers.android;
 
 import java.util.concurrent.ExecutionException;
 
 import com.allplayers.objects.PhotoData;
 import com.allplayers.rest.RestApiV1;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.ViewFlipper;
 
 public class PhotoDisplayActivity extends Activity implements OnTouchListener {
     float  downYValue;
 
     /**
      * The current photo displayed.
      */
     private PhotoData mPhoto;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.photodisplay);
 
         mPhoto = (new Router(this)).getIntentPhoto();
         String photoUrl = mPhoto.getPhotoFull();
         
         GetRemoteImageOnTouchTask helper = new GetRemoteImageOnTouchTask();
         helper.execute(photoUrl);
        try {
			helper.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
     }
 
     public boolean onTouch(View arg0, MotionEvent arg1) {
         switch (arg1.getAction()) {
         case MotionEvent.ACTION_DOWN: {
             downYValue = arg1.getX();
             break;
         }
 
         case MotionEvent.ACTION_UP: {
             float currentX = arg1.getX();
 
             if (downYValue < currentX) {
                 ViewFlipper slider = (ViewFlipper)findViewById(R.id.Slider);
                 slider.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                 slider.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
 
                 //change image
                 if (mPhoto.previousPhoto() != null) {
                     mPhoto = mPhoto.previousPhoto();
                 }
 
                 PhotoData photo = mPhoto;
                 String photoUrl = photo.getPhotoFull();
                 
                 GetRemoteImageTask helper = new GetRemoteImageTask();
                 helper.execute(photoUrl);
                 try {
         			helper.get();
         		} catch (InterruptedException e) {
         			e.printStackTrace();
         		} catch (ExecutionException e) {
         			e.printStackTrace();
         		}
             }
 
             if (downYValue > currentX) {
                 ViewFlipper slider = (ViewFlipper) findViewById(R.id.Slider);
                 slider.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                 slider.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
 
                 //change image
                 if (mPhoto.nextPhoto() != null) {
                     mPhoto = mPhoto.nextPhoto();
                 }
 
                 PhotoData photo = mPhoto;
                 String photoUrl = photo.getPhotoFull();
 
                 GetRemoteImageTask helper = new GetRemoteImageTask();
                 helper.execute(photoUrl);
                 try {
         			helper.get();
         		} catch (InterruptedException e) {
         			e.printStackTrace();
         		} catch (ExecutionException e) {
         			e.printStackTrace();
         		}
             }
 
             break;
         }
         }
 
         return true;
     }
     
     public class GetRemoteImageOnTouchTask extends AsyncTask<String, Void, Bitmap> {
     	
     	protected Bitmap doInBackground(String... photoUrl) {
     		return RestApiV1.getRemoteImage(photoUrl[0]);
     	}
     	
     	protected void onPostExecute(Bitmap image) {
     		ImageView imView = (ImageView)findViewById(R.id.fullPhotoDisplay);
             imView.setImageBitmap(image);
             imView.setOnTouchListener((OnTouchListener) PhotoDisplayActivity.this);
     	}
     }
 
     public class GetRemoteImageTask extends AsyncTask<String, Void, Bitmap> {
     	
     	protected Bitmap doInBackground(String... photoUrl) {
     		return RestApiV1.getRemoteImage(photoUrl[0]);
     	}
     	
     	protected void onPostExecute(Bitmap image) {
     		ImageView imView = (ImageView)findViewById(R.id.fullPhotoDisplay);
             imView.setImageBitmap(image);
     	}
     }
 }
