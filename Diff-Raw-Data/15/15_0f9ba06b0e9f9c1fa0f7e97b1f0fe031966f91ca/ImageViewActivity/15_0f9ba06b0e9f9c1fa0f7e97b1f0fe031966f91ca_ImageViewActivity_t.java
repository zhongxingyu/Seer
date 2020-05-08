 package net.sourcewalker.smugview.gui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import net.sourcewalker.smugview.R;
 import net.sourcewalker.smugview.data.Cache;
 import net.sourcewalker.smugview.parcel.Extras;
 import net.sourcewalker.smugview.parcel.ImageInfo;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
import android.content.Intent;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.gesture.Prediction;
 import android.gesture.GestureOverlayView.OnGesturePerformedListener;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class ImageViewActivity extends Activity implements
         OnGesturePerformedListener {
 
     private static final String GESTURE_BACK = "back";
     private static final String GESTURE_NEXT = "next";
 
     private ImageView viewer;
     private ImageInfo image;
     private GestureLibrary gestureLibrary;
     private GestureOverlayView gestureOverlay;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.image);
 
         if (Cache.get() == null) {
             Cache.init(this);
         }
 
         gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
         if (!gestureLibrary.load()) {
             Toast.makeText(this, R.string.nogestures, Toast.LENGTH_LONG);
         }
         gestureOverlay = (GestureOverlayView) findViewById(R.id.gestures);
         gestureOverlay.addOnGesturePerformedListener(this);
 
         viewer = (ImageView) findViewById(R.id.imageview);
         loadImage((ImageInfo) getIntent().getExtras().get(Extras.EXTRA_IMAGE));
     }
 
     private void loadImage(ImageInfo imageToLoad) {
         image = imageToLoad;
 
         setTitle();
 
         Drawable viewImage = image.getImage();
         if (viewImage != null) {
             viewer.setImageDrawable(viewImage);
         } else {
             if (image.getThumbnail() != null) {
                 viewer.setImageDrawable(image.getThumbnail());
             }
             if (image.getViewUrl() != null) {
                 startGetImage();
             }
         }
     }
 
     @Override
     public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
         ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
         for (Prediction p : predictions) {
             if (p.score > 1.0) {
                 runGesture(p.name);
                 return;
             }
         }
     }
 
     private void runGesture(String gestureName) {
         if (gestureName.equals(GESTURE_BACK)) {
             ImageInfo previous = Cache.get().getPreviousInAlbum(image);
             if (previous != null) {
                replaceActivity(previous);
             }
         } else if (gestureName.equals(GESTURE_NEXT)) {
             ImageInfo next = Cache.get().getNextInAlbum(image);
             if (next != null) {
                replaceActivity(next);
             }
         }
     }
 
    private void replaceActivity(ImageInfo newImage) {
        Intent replace = new Intent(this, ImageViewActivity.class);
        replace.putExtra(Extras.EXTRA_IMAGE, newImage);
        startActivity(replace);
        finish();
    }

     private void setTitle() {
         String description = image.getDescription();
         if (description != null && description.length() > 0) {
             setTitle(description);
         } else {
             setTitle(image.getFileName());
         }
     }
 
     private void startGetImage() {
         new GetImageTask().execute(image);
     }
 
     private class GetImageTask extends AsyncTask<ImageInfo, Void, Drawable> {
 
         @Override
         protected void onPreExecute() {
             setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected Drawable doInBackground(ImageInfo... params) {
             ImageInfo image = params[0];
             Drawable result = null;
             String viewUrl = image.getViewUrl();
             HttpGet get = new HttpGet(viewUrl);
             HttpClient client = new DefaultHttpClient();
             try {
                 HttpResponse response = client.execute(get);
                 result = new BitmapDrawable(response.getEntity().getContent());
                 image.setImage(result);
             } catch (IOException e) {
                 Log.e("GetImageTask", "Error while getting image: "
                         + e.getMessage());
             }
             return result;
         }
 
         @Override
         protected void onPostExecute(Drawable result) {
             if (result != null) {
                 viewer.setImageDrawable(result);
             }
             setProgressBarIndeterminateVisibility(false);
         }
 
     }
 
 }
