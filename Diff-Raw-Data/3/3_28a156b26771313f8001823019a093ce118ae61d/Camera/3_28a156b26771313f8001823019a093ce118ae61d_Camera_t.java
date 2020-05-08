 package com.ravicious.boston;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.app.WallpaperManager;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 public class Camera extends Activity implements View.OnClickListener {
 
 	ImageButton imageButton;
 	Button button;
 	ImageView imageView;
 	Intent intent;
 	final static int cameraData = 0;
 	Bitmap bitmap;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.photo);
 		initialize();
 		
 		InputStream inputStream = getResources().openRawResource(R.drawable.ic_launcher);
 		bitmap = BitmapFactory.decodeStream(inputStream);
 	}
 
 	private void initialize() {
 		imageView = (ImageView) findViewById(R.id.imageViewReturnedPicture);
 		imageButton = (ImageButton) findViewById(R.id.imageButtonTakePicture);
 		button = (Button) findViewById(R.id.buttonSetWallpaper);
 		button.setOnClickListener(this);
 		imageButton.setOnClickListener(this);
 	}
 
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.buttonSetWallpaper:
 			try {
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
 				wallpaperManager.setBitmap(bitmap);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			break;
 		case R.id.imageButtonTakePicture:
 			intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 			startActivityForResult(intent, cameraData);
 			break;
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == RESULT_OK) {
 			Bundle extras = data.getExtras();
 			bitmap = (Bitmap) extras.get("data");
 			imageView.setImageBitmap(bitmap);
 		}
 	}
 }
