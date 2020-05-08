 package com.sysu.youtour.controller;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PointF;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
 import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;
 import com.sysu.shen.youtour.R;
 import com.sysu.youtour.dao.JSONFunctions;
 
 public class DecoderActivity extends Activity implements OnQRCodeReadListener {
 
     private QRCodeReaderView                   mydecoderview;
 
     private ImageView                          line_image;
 
     private AsyncTask<String, Void, JSONArray> task;
 
     private JSONArray                          jarray;
 
     private TranslateAnimation                 mAnimation;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.sendBroadcast(new Intent(MainActivity.SHOWLOCALLIST_HIDE_PROGRESS));
         setContentView(R.layout.activity_decoder);
 
         mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
         mydecoderview.setOnQRCodeReadListener(this);
 
         line_image = (ImageView) findViewById(R.id.red_line_image);
 
         mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f,
                 TranslateAnimation.RELATIVE_TO_PARENT, -0.2f, TranslateAnimation.RELATIVE_TO_PARENT, 0.2f);
         mAnimation.setDuration(1500);
         mAnimation.setRepeatCount(-1);
         mAnimation.setRepeatMode(Animation.RESTART);
         mAnimation.setInterpolator(new LinearInterpolator());
         line_image.setAnimation(mAnimation);
 
     }
 
     // Called when a QR is decoded
     // "text" : the text encoded in QR
     // "points" : points where QR control points are placed
     @Override
     public void onQRCodeRead(String URLString, PointF[] points) {
         if (URLString.contains("browseByID")) {
             if ((task == null || task.getStatus() == AsyncTask.Status.PENDING)) {
                 task = new GetJSONAsynTack(this);
                 task.execute(URLString);
             }
         }
     }
 
     // Called when your device have no camera
     @Override
     public void cameraNotFound() {
 
     }
 
     // Called when there's no QR codes in the camera preview image
     @Override
     public void QRCodeNotFoundOnCamImage() {
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mAnimation.reset();
         mAnimation.start();
         task = null;
         mydecoderview.getCameraManager().startPreview();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mAnimation.cancel();
         mydecoderview.getCameraManager().stopPreview();
     }
 
     private class GetJSONAsynTack extends AsyncTask<String, Void, JSONArray> {
 
         public Activity activity;
 
         public GetJSONAsynTack(Activity activity2) {
             activity = activity2;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         protected void onPreExecute() {
             Toast.makeText(this.activity, "正在处理", Toast.LENGTH_SHORT).show();
             super.onPreExecute();
         }
 
         @Override
         protected JSONArray doInBackground(String... strings) {
             String URLString = strings[0];
 
             final ConnectivityManager conMgr = (ConnectivityManager) activity
                     .getSystemService(Context.CONNECTIVITY_SERVICE);
             final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
             if (activeNetwork != null && activeNetwork.isConnected()) {
                 jarray = JSONFunctions.getJsonFromNetwork(activity, URLString);
             } else {
                 jarray = JSONFunctions.getJSONFromFile(activity, URLString);
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(JSONArray result) {
             if (jarray != null) {
                 Intent it = new Intent(DecoderActivity.this, MapPopup.class);
                 try {
                     JSONObject lineJson = jarray.getJSONObject(0);
                     it.putExtra("lineString", lineJson.toString());
                     it.putExtra("type", "line");
                     it.putExtra("stopLineID", "null");
                 } catch (NumberFormatException e) {
                     e.printStackTrace();
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 startActivity(it);
             } else {
                 Toast.makeText(this.activity, "无法识别此二维码", Toast.LENGTH_SHORT).show();
             }
             super.onPostExecute(result);
         }
     }
 
     /**
      * 点击返回
      * 
      * @param v
      */
     public void backClicked(View v) {
         if (task != null) {
             task.cancel(true);
         }
         this.finish();
     }
 
 }
