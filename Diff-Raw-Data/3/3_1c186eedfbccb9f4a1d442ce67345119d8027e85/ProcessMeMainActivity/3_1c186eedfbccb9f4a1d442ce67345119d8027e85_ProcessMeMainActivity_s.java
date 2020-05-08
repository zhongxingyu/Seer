 package edu.hrbeu.processme;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 
 public class ProcessMeMainActivity extends Activity {
     private ImageView mImage;
    private ProcessingTimerHorizontal mTimerHorizontal, mTimerHorizontalBlur;
     private ProcessingTimerVertical mTimerVertical;
     private Bitmap originImage, editedImage;
     static Bitmap sBitmap = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.main);
 	mImage = (ImageView) findViewById(R.id.ImageView);
 	Resources res = getResources();
 	originImage = BitmapFactory.decodeResource(res, R.drawable.xu);
 	editedImage = Bitmap.createBitmap(originImage.getWidth(), originImage
 		.getHeight(), Bitmap.Config.ARGB_8888);
 	generateNewImage();
 	// mImage.setImageBitmap(originImage);
 	mTimerHorizontal = new ProcessingTimerHorizontal(this, editedImage,
 		mImage);
 	mTimerVertical = new ProcessingTimerVertical(this, editedImage, mImage);
 	mTimerHorizontalBlur = new ProcessingTimerHorizontalBlur(this,
 		editedImage, mImage);
 	mImage.setOnClickListener(new OnClickListener() {
 
 	    @Override
 	    public void onClick(View v) {
 		Intent i = new Intent(ProcessMeMainActivity.this,
 			CameraPreviewActivity.class);
 
 		startActivityForResult(i, 667);
 	    }
 	});
 
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	int width = sBitmap.getWidth();
 	int height = sBitmap.getHeight();
 	int newWidth = editedImage.getHeight();
 	int newHeight = editedImage.getWidth();
 
 	// calculate the scale - in this case = 0.4f
 	float scaleWidth = ((float) newWidth) / width;
 	float scaleHeight = ((float) newHeight) / height;
 
 	// create matrix for the manipulation
 	Matrix matrix = new Matrix();
 	// resize the bit map
 	matrix.postScale(scaleWidth, scaleHeight);
 	// rotate the Bitmap
 	matrix.postRotate(90);
 	// recreate the new Bitmap
 	editedImage = Bitmap.createBitmap(sBitmap, 0, 0, width, height, matrix,
 		true);
 	mImage.setImageBitmap(editedImage);
 	mTimerHorizontalBlur.setNewBitmap(editedImage);
 	mTimerVertical.setNewBitmap(editedImage);
 	super.onActivityResult(requestCode, resultCode, data);
     }
 
     @Override
     protected void onResume() {
 	try {
 	    mTimerHorizontalBlur.run(0, 20);
 	    mTimerVertical.run(3000, 15);
 	} catch (Exception x) {
 	    Log.e("ONRESUME!", x.toString());
 	}
 
 	super.onResume();
     }
 
     private void generateNewImage() {
 	int rndInt1 = (int) (80 * Math.random());
 	int rndInt2 = (int) (80 * Math.random());
 	int rndInt3 = (int) (70 * Math.random());
 	int mImWidth = originImage.getWidth();
 	int mImHeight = originImage.getHeight();
 	for (int i = 0; i < mImWidth; i++)
 	    for (int j = 0; j < mImHeight; j++) {
 		int red = Color.red(originImage.getPixel(i, j));
 		int green = Color.green(originImage.getPixel(i, j));
 		int blue = Color.blue(originImage.getPixel(i, j));
 		editedImage.setPixel(i, j, Color.rgb(red + rndInt1 - 10, green
 			+ rndInt2 - 10, blue + rndInt3 - 10));
 	    }
 	mImage.setImageBitmap(editedImage);
     }
 
     @Override
     protected void onStop() {
 	mTimerHorizontalBlur.stop();
 	mTimerVertical.stop();
 	super.onStop();
     }
 
     @Override
     protected void onPause() {
 	mTimerHorizontalBlur.stop();
 	mTimerVertical.stop();
 	super.onPause();
     }
 }
