 package com.placella.socialconnections;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.*;
 import android.net.Uri;
 import android.os.*;
 import android.provider.MediaStore;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.*;
 
 /**
  * This is activity is used by the lecturer
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
 	private List<Integer> ignored = new ArrayList<Integer>();
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
 		    		showOverlay();
 		    		setOverlay(getString(R.string.uploading));
 		        	FacialRecognition f = new FacialRecognition(self);
 		    		f.upload(session, facePath, detectedFaces, ignored);
 				}
 			}
 		});
 		
 		SecureRandom random = new SecureRandom();
 		session = new BigInteger(130, random).toString(32);
 		
 	    setResult(1);
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
     		showOverlay();
     		setOverlay(getString(R.string.detecting));
     		new Thread(
 				new Runnable() {
 					@Override
 					public void run() {
 			    		detectFaces();
 					}
 				}
     		).start();
     	} else if (requestCode == Activity_LecturerMenu.WEB_REQUEST) {
     		if (resultCode == 0) {
 			    setResult(0);
     		}
     		finish();
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
 			runOnUiThread(new Runnable() {
 				public void run() {
 		    		self.hideOverlay();
 		    		new Dialog(self, R.string.noFaces).show();
 				}
 			});
     	} else {
 			runOnUiThread(new Runnable() {
 				public void run() {
 		    		self.hideOverlay();
 		    		TextView status = (TextView) findViewById(R.id.status);
 		    		status.setText(R.string.take_attendance_hint2);
 					final LinearLayout rl = (LinearLayout) findViewById(R.id.faces);
					for (int i = detectedFaces - numFaces; i<detectedFaces; i++) {
 						LinearLayout l = new LinearLayout(self);
 				        l.setOrientation(LinearLayout.HORIZONTAL);
 						l.setLayoutParams(
 				        	new LayoutParams(
 				        		LayoutParams.MATCH_PARENT,
 				        		LayoutParams.WRAP_CONTENT
 				        	)
 				        );
 				        
 						Bitmap bm = BitmapFactory.decodeFile(facePath + "face" + i + ".jpg");
 						ImageView iv = new ImageView(self);
 						iv.setImageBitmap(bm);
 						iv.setPadding(5, 5, 5, 5);
 						l.addView(iv);
 						
 						Button b = new Button(self);
 						b.setText("Remove");
 						b.setLayoutParams(
 				        	new LayoutParams(
 				        		LayoutParams.MATCH_PARENT,
 				        		LayoutParams.WRAP_CONTENT
 				        	)
 				        );
 						b.setTag(i);
 						b.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View button) {
 								int index = (Integer) button.getTag();
 								rl.getChildAt(index).setVisibility(View.GONE);
 								ignored.add(index);
 							}
 						});
 						l.addView(b);
 						
 						rl.addView(l);
 					}
 				}
 			});
 			pictures++;
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
     		Activity_Web.launch(self, "facialRec", token, session);
     	} else {
 			new Dialog(self, messages[0]).show();
     	}
     }
 
     /**
      * Creates the menu from XML file
      */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 	
     /**
 	 * Called whenever an item in the options menu is selected.
      *
 	 * @param item The menu item that was selected
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_openbrowser) {
 			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CONFIG.webUrl)));
         }
         return super.onOptionsItemSelected(item);
     }
 }
