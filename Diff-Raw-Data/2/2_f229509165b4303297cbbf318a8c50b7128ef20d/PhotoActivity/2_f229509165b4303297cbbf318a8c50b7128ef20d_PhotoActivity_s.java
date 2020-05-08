 package com.github.groupENIGMA.journalEgocentrique;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 import com.github.groupENIGMA.journalEgocentrique.model.Photo;
 
 public class PhotoActivity extends Activity {
 
 	private static final int CAMERA_REQUEST = 1; 
 	private ImageView mImageView;
 	private Bitmap mImageBitmap;
 	private Entry entry;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_photo);
 		setView();
 		mImageView = (ImageView)findViewById(R.id.photo);
 		final DB data = new DB(getApplicationContext());
 		Intent received = getIntent();
 		data.open();
 		final long entryId = received.getLongExtra(ListActivity.EXTRA_MESSAGE, 0);
 		entry = data.getEntry(entryId); 
 		ImageView actualImg = (ImageView)findViewById(R.id.photo);
 		Photo tmp = entry.getPhoto();
 		if(tmp != null)
 			actualImg.setImageURI(Uri.parse(tmp.getPath()));
 		
 		/*
 		 * Al click di takePicture lancia la fotocamera di sistema
 		 */
 		Button takePicture = (Button)findViewById(R.id.take_picture);
 		takePicture.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				data.close();
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
 				data.setPhoto(entry, mImageBitmap);
 				Intent intent = new Intent(getApplicationContext(), ListActivity.class);
 				data.close();
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
 				data.close();
 				startActivity(intent);
 			}
 		});
 	}
 	
     /**
      * Sets dinamically proportioned the size of the Entries, Images and Notes
      */
     private void setView(){
     	Display display = getWindowManager().getDefaultDisplay();
     	int width = display.getWidth();
     	int height = display.getHeight();
     	
     	ImageView photo = (ImageView)findViewById(R.id.photo);
     	LinearLayout list = (LinearLayout)findViewById(R.id.linear_layout);
     	Button button = (Button)findViewById(R.id.button);
     	
     	// Set the photo
     	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)photo.getLayoutParams();
     	params.width = width * 3 / 4;
     	photo.setLayoutParams(params);
     	// Set the list
     	params = (FrameLayout.LayoutParams)list.getLayoutParams();
     	params.width = width/6;
     	list.setLayoutParams(params);
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
