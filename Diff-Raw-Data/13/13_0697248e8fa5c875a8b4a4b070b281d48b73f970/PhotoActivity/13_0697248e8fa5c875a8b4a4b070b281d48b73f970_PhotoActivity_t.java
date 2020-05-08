 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 
 public class PhotoActivity extends Activity {
 
	private static final int CAMERA_REQUEST = 1; 
 	private ImageView mImageView;
 	private Bitmap mImageBitmap;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_photo);
 		mImageView = (ImageView)findViewById(R.id.photo);
 		Intent received = getIntent();
 		final long entryId = received.getLongExtra(ListActivity.EXTRA_MESSAGE, 0);
 		
 		/*
 		 * Al click di takePicture lancia la fotocamera di sistema
 		 */
 		Button takePicture = (Button)findViewById(R.id.take_picture);
 		takePicture.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				startActivityForResult(intent, CAMERA_REQUEST);
 			}
 		});
 		
 		/*
 		 * Al click di accept salva l'immagine associandola alla corretta Entry,
 		 * poi ritorna alla ListActivity
 		 */
 		Button accept = (Button)findViewById(R.id.accept);
 		accept.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				DB data = new DB(getApplicationContext());
 				Entry entry = data.getEntry(entryId); 
 			//	data.setPhoto(entry, mImageBitmap.);
 				Intent intent = new Intent(getApplicationContext(), ListActivity.class);
 				startActivity(intent);
 			}
 		});
 		
 		/*
 		 * Al click di delete non salva l'immagine attuale e torna alla ListActivity
 		 */
 		Button delete = (Button)findViewById(R.id.delete);
 		delete.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(), ListActivity.class);
 				startActivity(intent);
 			}
 		});
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		handleCameraPhoto(data);
 	}
 	
 	private void handleCameraPhoto(Intent intent) {
 	    Bundle extras = intent.getExtras();
 	    mImageBitmap = (Bitmap) extras.get("data");
 	    mImageView.setImageBitmap(mImageBitmap);
 	}
 
 }
