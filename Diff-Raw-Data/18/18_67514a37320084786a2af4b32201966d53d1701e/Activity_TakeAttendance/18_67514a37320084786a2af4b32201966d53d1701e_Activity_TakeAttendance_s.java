 package com.placella.socialconnections;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 /**
  * This is activity is used by the lecurer
  * to take the attendance of a group by
  * using facial recognition, and by using
  * a fallback method if the facial recognition
  * fails.
  */
 public class Activity_TakeAttendance extends CallbackActivity {
 	private String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/test.jpg";
 	private String facePath = "data/data/com.placella.socialconnections/files/";
 	private final int CAMERA_REQUEST = 1;
 	private CallbackActivity self = this;
 	private int detectedFaces = 0;
 	private int numFaces = 0;
 	private int pictures = 0;
 	private String session;
 	private String token;
 
 	/**
 	 * Called when the activity is starting.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 	    super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_take_attendance);
 		
 		token = getIntent().getStringExtra("token");
 
 		Button button = (Button) findViewById(R.id.back);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		button = (Button) findViewById(R.id.add);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Uri uri = Uri.fromFile(new File(path));
 				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
 				startActivityForResult(intent, CAMERA_REQUEST);
 			}
 		});
 		button = (Button) findViewById(R.id.ok);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (pictures == 0) {
 					new Dialog(self, R.string.noPicture).show();
 				} else {
 					Activity_Web.launch(self, "facialRec", token, session);
 				}
 			}
 		});
 		
 		SecureRandom random = new SecureRandom();
 		session = new BigInteger(130, random).toString(32);
 	}
 	/**
 	 * Called when an activity you launched exits, giving you the requestCode you
 	 * started it with, the resultCode it returned, and any additional data from it.
 	 * The resultCode will be RESULT_CANCELED if the activity explicitly returned that,
 	 * didn't return any result, or crashed during its operation.
 	 */
     protected void onActivityResult(int requestCode, int resultCode, Intent data) 
     {  
     	if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
     		detectFaces();
     	}
     }
     /**
      * Detects and recognises the faces in a picture taken with the camera
      */
     private void detectFaces()
     {
     	FacialRecognition f = new FacialRecognition(self);
     	numFaces = f.detect(path, detectedFaces);
     	if (numFaces < 1) {
     		new Dialog(this, R.string.noFaces).show();
     	} else {
 			pictures++;
 			self.showOverlay();
 			System.out.println("Query: " + numFaces + "," + detectedFaces);
 			f.upload(session, facePath, numFaces, detectedFaces);
 			detectedFaces += numFaces;
     	}
     }
 	/**
 	 * Called when the FacialRecognition class has finished
 	 * processing the images submitted to it.
 	 *
 	 * @param success  Whether the facial recognition was successful
 	 * @param messages A list of messages. In case of an error there will
 	 *                 be only one message, the reason of the failure.
 	 *                 In case of a success, there may be no messages at all,
 	 *                 if the request was for uploading a picture.
 	 */
 	public void callback(boolean success, String[] messages) {
 		self.hideOverlay();
     	if (success) {
 			for (int i = detectedFaces - numFaces; i<detectedFaces; i++) {
 				//BitmapFactory.Options options = new BitmapFactory.Options();
 				//options.inSampleSize = 2;
 				Bitmap bm = BitmapFactory.decodeFile(facePath + "face" + i + ".jpg");//, options);
 				ImageView iv = new ImageView(self);
 				iv.setImageBitmap(bm);
 				iv.setPadding(5, 5, 5, 5);
 				LinearLayout rl = (LinearLayout) findViewById(R.id.faces);
 				rl.addView(iv);
 			}
     	} else {
 			new Dialog(self, messages[0]).show();
     	}
     }
 }
