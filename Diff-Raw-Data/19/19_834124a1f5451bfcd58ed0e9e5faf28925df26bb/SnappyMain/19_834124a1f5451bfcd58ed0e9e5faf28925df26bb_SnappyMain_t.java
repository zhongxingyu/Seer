 /*
  * project		Snappy
  * package		com.recarlin.snappy
  * author		Russell Carlin
  * date			Jun 13, 2013
  */
 package com.recarlin.snappy;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.annotation.SuppressLint;
 import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class SnappyMain extends Activity {
 	
 	private String photo;
 	private File image;
 	private static String FILE_TYPE;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_snappy_main);
 		
 		Button newPic = (Button) findViewById(R.id.newPic);
 		newPic.setOnClickListener(new View.OnClickListener() {
 			@SuppressLint("SimpleDateFormat")
 			@Override
 			public void onClick(View v) {
 				File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Snappy");
				String timeStamp = new SimpleDateFormat("MMddyyyyHHmmss").format(new Date());
 				String imageFileName =  "pic_" + timeStamp;
 				try {
 					image = File.createTempFile(imageFileName, FILE_TYPE, storageDir);
 					photo = image.getAbsolutePath();
 				} catch (IOException e) {
 					Log.e("FILE CREATION", "There was an issue creating the file: " + imageFileName + "  " + storageDir);
 				}
 				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
 				startActivityForResult(intent, 100);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.snappy_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Bitmap theImage = (Bitmap) data.getExtras().get("data");
 		ImageView iView = ((ImageView) findViewById(R.id.imageView));
 		iView.setImageBitmap(theImage);
 		
 		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 	    File f = new File(photo);
 	    Uri contentUri = Uri.fromFile(f);
 	    mediaScanIntent.setData(contentUri);
 	    this.sendBroadcast(mediaScanIntent);
	    
	    NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    Notification noti = new Notification();
	    noti.tickerText = "New Picture Saved!";
	    manager.notify(42, noti);
 	}
 }
