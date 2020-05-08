 package com.clarity.glassviewer;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageView;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParser;
 
 public class FullscreenActivity extends Activity {
 	private static final String USER_AGENT = "Mozilla/5.0";
 	  
     private GestureDetector mGestureDetector;
     private GlassGestureListener mGlassGestureListener;
     private Chronometer mChronometer;
     private Slide[] mSlides;
     private ImageView mImageView;
     private TextView mTextView;
     private ImageView mThumbnailView;
     private int mNumImagesLoaded = 0;
     private int mCurrentSlide = 0;
     private boolean mDisplayPreview = true;
     private boolean mDisplayNotes = false;
    private String mPresenterID;
    private String mPresentationID;
     private TextView mSlideNumberView;
     private View mSlideNumberWrapper;
 
     private boolean imagesLoaded() {
         return mNumImagesLoaded == mSlides.length;
     }
 
     private void goToSlide(int slideNo) {
         new SlideChangeTask(mSlides[slideNo].getPage_id()).execute();
         mCurrentSlide = slideNo;
         renderSlide();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getActionBar().hide();
         
         setContentView(R.layout.activity_fullscreen);
 
         mImageView = (ImageView) findViewById(R.id.imageView);
         mThumbnailView = (ImageView) findViewById(R.id.previewThumbnail);
         mChronometer = (Chronometer) findViewById(R.id.timer);
         mChronometer.setVisibility(View.INVISIBLE);
         mTextView = (TextView) findViewById(R.id.textView);
         mSlideNumberView = (TextView) findViewById(R.id.slideNumber);
         mSlideNumberWrapper = findViewById(R.id.slideNumWrapper);
 
 
         mGlassGestureListener = new GlassGestureListener();
 
         mGestureDetector = new GestureDetector(this, mGlassGestureListener);
 
         mSlides = new Slide[]{
             new Slide("hi this is notes 1111", "a", "http://yoshi.2yr.net/pics/yoshis-story-yoshi.png"),
             new Slide("hi I am looking at notes 2222!", "a", "http://pad3.whstatic.com/images/thumb/0/07/MarioNintendoImage.png/350px-MarioNintendoImage.png"),
             new Slide("hi. I'm lazy.", "a", "http://images.wikia.com/mariofanon/images/c/c9/Toad.png"),
         };
 
         for (int i = 0; i < mSlides.length; i++) {
             new DownloadImageTask(i).execute(mSlides[i].getImg_url());
         }
 
         Log.d("FullscreenActivity", "onCreate complete");
     }
     
     private String getServerPayload() throws Exception {
 		String url = "http://www.google.com/search?q=developer";
 		 
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(url);
  
 		// add request header
 		request.addHeader("User-Agent", USER_AGENT);
  
 		HttpResponse response = client.execute(request);
  
 		System.out.println("\nSending 'GET' request to URL : " + url);
 		System.out.println("Response Code : " + 
                        response.getStatusLine().getStatusCode());
  
 		BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
  
 		StringBuffer result = new StringBuffer();
 		String line = "";
 		while ((line = rd.readLine()) != null) {
 			result.append(line);
 		}
  
 		return result.toString();
     }
 
     private void renderSlide() {
 
         mSlideNumberView.setText(mCurrentSlide + " / " + mSlides.length);
 
 
         if (!mDisplayPreview || mCurrentSlide + 1 == mSlides.length) {
             mThumbnailView.setVisibility(View.GONE);
         }
         else if (mCurrentSlide + 1 < mSlides.length) {
             mThumbnailView.setVisibility(View.VISIBLE);
             mThumbnailView.setImageBitmap(mSlides[mCurrentSlide+1].getBitmap());
         }
 
         if (mDisplayNotes) {
             mTextView.setVisibility(View.VISIBLE);
             mImageView.setVisibility(View.GONE);
             mTextView.setText(mSlides[mCurrentSlide].getSpeaker_notes());
             mThumbnailView.setVisibility(View.GONE);
         }
         else {
             mTextView.setVisibility(View.GONE);
             mImageView.setVisibility(View.VISIBLE);
             mImageView.setImageBitmap(mSlides[mCurrentSlide].getBitmap());
         }
 
     }
 
     @Override
     public boolean onGenericMotionEvent(MotionEvent event) {
         mGestureDetector.onTouchEvent(event);
         Log.d("FullscreenActivity", "generic! generic! generic!");
         return true;
     }
 
     private HttpResponse httpPost(String initURL, List<NameValuePair> nameValuePairs) {
         HttpClient httpclient = new DefaultHttpClient();
         try {
                 HttpPost httppost = new HttpPost(initURL);
                 UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(nameValuePairs);
                 httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                 HttpResponse response = httpclient.execute(httppost);
                 return response;
                 // Log.i("RESPONSE", "sigh... " + response.toString());
 
                 // BufferedReader reader = new BufferedReader(new InputStreamReader(
                 //         response.getEntity().getContent(), "UTF-8"));
                 // String json = reader.readLine();
                 // // Instantiate a JSON object from the request response
                 // JSONArray jsonArray = new JSONArray(json);
 
         } catch(Exception e) {
                 Log.d("Exception", e.toString());
         }
         return null;
     }
 
     private HttpResponse httpGet(String initURL, List<NameValuePair> nameValuePairs) {
         HttpClient httpclient = new DefaultHttpClient();
         try {
             HttpGet httpGet = new HttpGet(initURL);
             HttpResponse response = httpclient.execute(httpGet);
             return response;
                 // Log.i("RESPONSE", "sigh... " + response.toString());
 
                 // BufferedReader reader = new BufferedReader(new InputStreamReader(
                 //         response.getEntity().getContent(), "UTF-8"));
                 // String json = reader.readLine();
                 // // Instantiate a JSON object from the request response
                 // JSONArray jsonArray = new JSONArray(json);
 
         } catch(Exception e) {
                 Log.d("Exception", e.toString());
         }
         return null;
     } 
 
     private class SlideChangeTask extends AsyncTask<Void, Void, Void> {
 
         private String mPageID;
 
         public SlideChangeTask(String pageid) {
             mPageID = pageid;
         }
 
         @Override
         protected Void doInBackground(Void... voids) {
             String initURL = "http://clarity-uho.appspot.com/api/controller";
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
             nameValuePairs.add(new BasicNameValuePair("page_id", mPageID));
             nameValuePairs.add(new BasicNameValuePair("presenter_id", mPresenterID));
             nameValuePairs.add(new BasicNameValuePair("presentation_id", mPresentationID));
             httpPost(initURL, nameValuePairs);
             return null;
         }
     }
 
     private class StartPresentationTask extends AsyncTask<Void, Void, Void> {
 
         public StartPresentationTask() {
             super();
         }
 
         @Override
         protected void onPostExecute(Void aVoid) {
             super.onPostExecute(aVoid);
             mChronometer.setVisibility(View.VISIBLE);
             mChronometer.start();
         }
 
         @Override
         protected Void doInBackground(Void... voids) {
 
             String initURL = "http://clarity-uho.appspot.com/api/controller/" +
                     mPresentationID + "?presenter_id=" + mPresenterID;
             HttpResponse response = httpGet(initURL);
 
                // Log.i("RESPONSE", "sigh... " + response.toString());
 
                // BufferedReader reader = new BufferedReader(new InputStreamReader(
                //         response.getEntity().getContent(), "UTF-8"));
                // String json = reader.readLine();
                // // Instantiate a JSON object from the request response
                // JSONArray jsonArray = new JSONArray(json);
 
             return null;
         }
     }
 
     private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 
         private int mIndex;
 
         public DownloadImageTask(int bitmapIndex) {
             mIndex = bitmapIndex;
         }
 
         protected Bitmap doInBackground(String... urls) {
             String urldisplay = urls[0];
             Bitmap mIcon11 = null;
             try {
                 InputStream in = new java.net.URL(urldisplay).openStream();
                 mIcon11 = BitmapFactory.decodeStream(in);
             } catch (Exception e) {
                 Log.e("Error", e.getMessage());
                 e.printStackTrace();
             }
             return mIcon11;
         }
 
         protected void onPostExecute(Bitmap result) {
             mSlides[mIndex].setBitmap(result);
             mNumImagesLoaded++;
 
             if (imagesLoaded()) {
                 Log.i("Async", "findme: Finished loading all images.");
                 goToSlide(0);
             }
         }
     }
 
     private class GlassGestureListener extends GestureDetector.SimpleOnGestureListener {
         private static final int SWIPE_MIN_DISTANCE = 100;
         private static final int SWIPE_THRESHOLD_VELOCITY = 1000;
 
         @Override
         public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
             try {
                 float totalXTraveled = finish.getX() - start.getX();
                 float totalYTraveled = finish.getY() - start.getY();
                 if (Math.abs(totalXTraveled) > Math.abs(totalYTraveled)) {
                     if (Math.abs(totalXTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                         if (totalXTraveled > 10) {
                             Log.d("Event", "findme: On Fling Forward");
                             if (imagesLoaded() && mCurrentSlide + 1 < mSlides.length) {
                                 goToSlide(mCurrentSlide + 1);
                             }
 
                         } else {
                             Log.d("Event", "findme: On Fling Backward");
                             if (imagesLoaded() && mCurrentSlide > 0) {
                                 goToSlide(mCurrentSlide - 1);
                             }
                         }
                     }
                 } else {
                     if (Math.abs(totalYTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                         if(totalYTraveled > 0) {
                             Log.d("Event", "findme: On Fling Down");
                         } else {
                             Log.d("Event", "findme: On Fling Up");
                         }
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
             return true;
         }
 
         @Override
         public boolean onSingleTapConfirmed(MotionEvent e) {
             Log.d("Event", "findme: On Single Tap");
             mDisplayPreview = !mDisplayPreview;
             renderSlide();
             return true;
         }
 
     }
 }
