 package com.example.gifting;
 
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 //import java.sql.Date;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 //import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.VideoView;
 
 public class Record extends Activity implements OnClickListener{
 
 	private static final int ACTION_TAKE_VIDEO = 3;
 
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
 	Display d;
 	private Button b1;
 	private ImageView mImageView;
 	private VideoView mVideoView;
 	private Uri mVideoUri;
 	private Bitmap mImageBitmap;
 	boolean mExternalStorageAvailable = false;
 	boolean mExternalStorageWriteable = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_record);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
 		//d=(Display)
 		PackageManager pm = this.getPackageManager();
 		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
 			// create an alert dialog
 			alertbox("Error", "You Do Not Have A Camera");
 			
 		}else{
 			b1=(Button)findViewById(R.id.button1);
 			b1.setOnClickListener(this);
 			//dispatchTakeVideoIntent();
 			
 			mImageView = (ImageView) findViewById(R.id.imageView1);
 			mVideoView = (VideoView) findViewById(R.id.videoView1);
 			mImageBitmap = null;
 			mVideoUri = null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_record, menu);
 		return true;
 	}
 
 	private void dispatchTakeVideoIntent() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		Date date = new Date();
 		Uri fileUri = null;
 		fileUri.parse(dateFormat.format(date).toString());
 		System.out.println(dateFormat.format(date));
 		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 		takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5000);
		takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name
 
 		//startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
 		startActivityForResult(takeVideoIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
 	}
 
 
 	protected void alertbox(String title, String mymessage) {
 		new AlertDialog.Builder(this)
 				.setMessage(mymessage)
 				.setTitle(title)
 				.setCancelable(true)
 				.setNeutralButton(android.R.string.cancel,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								finish();
 							}
 						}).show();
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		case R.id.button1:
 			//checks if external storage is mounted properly
 			String state = Environment.getExternalStorageState();
 			if (Environment.MEDIA_MOUNTED.equals(state)) {
 			    // We can read and write the media
 			    mExternalStorageAvailable = mExternalStorageWriteable = true;
 			    dispatchTakeVideoIntent();
 			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			    // We can only read the media
 			    mExternalStorageAvailable = true;
 			    mExternalStorageWriteable = false;
 			    alertbox("Read Only Error", "External Storage is mounted Read-Only. Recommend Restart");
 			} else {
 			    // Something else is wrong. It may be one of many other states, but all we need
 			    //  to know is we can neither read nor write
 			    mExternalStorageAvailable = mExternalStorageWriteable = false;
 			    alertbox("Read/Write Error", "External Storage is does not have proper permissions. Recommend Restart");
 			}
 			
 			//basically the same as before but sends an empty string for the address and sends the string containing all previous searches
			dispatchTakeVideoIntent();
 			//this.setRequestedOrientation(d.getRotation());
 			break;
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 	        if (resultCode == RESULT_OK) {
 	            // Image captured and saved to fileUri specified in the Intent
 	            Toast.makeText(this, "Image saved to:\n" +
 	                     data.getData(), Toast.LENGTH_LONG).show();
 	        } else if (resultCode == RESULT_CANCELED) {
 	            // User cancelled the image capture
 	        } else {
 	            // Image capture failed, advise user
 	        }
 	    }
 
 	    if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
 	        if (resultCode == RESULT_OK) {
 	            // Video captured and saved to fileUri specified in the Intent
 	            Toast.makeText(this, "Video saved to:\n" +
 	                     data.getData(), Toast.LENGTH_LONG).show();
 	        } else if (resultCode == RESULT_CANCELED) {
 	            // User cancelled the video capture
 	        } else {
 	            // Video capture failed, advise user
 	        }
 	    }
 	    
 		//switch (requestCode) {
 		//case ACTION_TAKE_VIDEO: {
 			//if (resultCode == RESULT_OK) {
 				//handleCameraVideo(data);
 			//}
 			//break;
 		//} // ACTION_TAKE_VIDEO
 		//} // switch
 	}
 	
 	private void handleCameraVideo(Intent intent) {
 		mVideoUri = intent.getData();
 		mVideoView.setVideoURI(mVideoUri);
 		mImageBitmap = null;
 		mVideoView.setVisibility(View.VISIBLE);
 		mImageView.setVisibility(View.INVISIBLE);
 	}
 
 	private void doFileUpload(){
         HttpURLConnection conn = null;
         DataOutputStream dos = null;
         DataInputStream inStream = null;
         String existingFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mypic.png";
         String lineEnd = "\r\n";
         String twoHyphens = "--";
         String boundary =  "*****";
         int bytesRead, bytesAvailable, bufferSize;
         byte[] buffer;
         int maxBufferSize = 1*1024*1024;
         String responseFromServer = "";
         String urlString = "http://pushbackstudios.com/directory/upload.php";
         try
         {
          //------------------ CLIENT REQUEST
         FileInputStream fileInputStream = new FileInputStream(new File(existingFileName) );
          // open a URL connection to the Servlet
          URL url = new URL(urlString);
          // Open a HTTP connection to the URL
          conn = (HttpURLConnection) url.openConnection();
          // Allow Inputs
          conn.setDoInput(true);
          // Allow Outputs
          conn.setDoOutput(true);
          // Don't use a cached copy.
          conn.setUseCaches(false);
          // Use a post method.
          conn.setRequestMethod("POST");
          conn.setRequestProperty("Connection", "Keep-Alive");
          conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
          dos = new DataOutputStream( conn.getOutputStream() );
          dos.writeBytes(twoHyphens + boundary + lineEnd);
          dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
          dos.writeBytes(lineEnd);
          // create a buffer of maximum size
          bytesAvailable = fileInputStream.available();
          bufferSize = Math.min(bytesAvailable, maxBufferSize);
          buffer = new byte[bufferSize];
          // read file and write it into form...
          bytesRead = fileInputStream.read(buffer, 0, bufferSize);
          while (bytesRead > 0)
          {
           dos.write(buffer, 0, bufferSize);
           bytesAvailable = fileInputStream.available();
           bufferSize = Math.min(bytesAvailable, maxBufferSize);
           bytesRead = fileInputStream.read(buffer, 0, bufferSize);
          }
          // send multipart form data necesssary after file data...
          dos.writeBytes(lineEnd);
          dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
          // close streams
          Log.e("Debug","File is written");
          fileInputStream.close();
          dos.flush();
          dos.close();
         }
         catch (MalformedURLException ex)
         {
              Log.e("Debug", "error: " + ex.getMessage(), ex);
         }
         catch (IOException ioe)
         {
              Log.e("Debug", "error: " + ioe.getMessage(), ioe);
         }
         //------------------ read the SERVER RESPONSE
         try {
               inStream = new DataInputStream ( conn.getInputStream() );
               String str;
 
               while (( str = inStream.readLine()) != null)
               {
                    Log.e("Debug","Server Response "+str);
               }
               inStream.close();
 
         }
         catch (IOException ioex){
              Log.e("Debug", "error: " + ioex.getMessage(), ioex);
         }
       }
 }
