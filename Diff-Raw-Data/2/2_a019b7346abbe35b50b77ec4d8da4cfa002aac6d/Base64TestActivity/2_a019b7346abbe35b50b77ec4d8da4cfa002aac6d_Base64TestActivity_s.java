 package com.base64test;
 
 import java.io.ByteArrayOutputStream;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Base64;
 import android.util.Log;
 import android.widget.ImageView;
 
 public class Base64TestActivity extends Activity {
 	
 	private ImageView image1;
 	private ImageView image2;
 	
 	private Bitmap bmp;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         image1 = (ImageView) findViewById(R.id.image1);
         image1.setImageResource(R.drawable.ic_launcher);
         
     	image2 = (ImageView) findViewById(R.id.image2);
     	bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
     	
     	ByteArrayOutputStream output = new ByteArrayOutputStream();
     	bmp.compress(Bitmap.CompressFormat.JPEG, 40, output);
 
        
     }
 }
