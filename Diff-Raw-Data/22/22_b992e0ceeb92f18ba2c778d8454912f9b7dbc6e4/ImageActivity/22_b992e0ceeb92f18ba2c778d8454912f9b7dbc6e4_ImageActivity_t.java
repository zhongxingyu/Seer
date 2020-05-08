 package com.github.groupENIGMA.journalEgocentrique;
 
 import android.app.Activity;
 import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
import android.view.ViewGroup;
 import android.widget.ImageView;
 
 public class ImageActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_image);
 		Intent intent = getIntent();
 		String path = intent.getStringExtra(GalleryActivity.EXTRA_MESSAGE);
 		ImageView img = (ImageView)findViewById(R.id.img);
 		img.setImageURI(Uri.parse(path));
		
		ImageTapZoomView imageView = new ImageTapZoomView(this, getWindow()
				.getWindowManager().getDefaultDisplay().getOrientation());

		imageView.setImage(img.getDrawable(), this);
		this.setContentView(imageView);

/*		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap bmp = BitmapFactory.decodeFile(path, options);

		ImagePinchZoomView impz = new ImagePinchZoomView(this);
		impz.setImageBitmap(((BitmapDrawable)img.getDrawable()).getBitmap());
		impz.setMaxZoom(4f);
		((ViewGroup)img.getParent()).removeView(img);
		this.setContentView(img);*/
 	}
 
 }
