 package com.lauren.wordsprout;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BlurMaskFilter;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore.Images;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.view.Window;
 import android.widget.ImageView;
 
 public class ImageActivity extends Activity {
 
 	private int cId; 
 
 	ArrayList<ImageView> imageViews;
 	ImageView sharedBtn;
 	ImageView restartBtn;
 	TextView monthLabel;
 	TextView dayLabel;
 	TextView yearLabel;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_images);
 		imageViews = new ArrayList<ImageView>();
 		imageViews.add((ImageView) findViewById(R.id.img01));
 		imageViews.add((ImageView) findViewById(R.id.img02));
 	
 		sharedBtn = (ImageView) findViewById(R.id.sharedBtn);
 		restartBtn = (ImageView) findViewById(R.id.restartBtn);
 		monthLabel = (TextView) findViewById(R.id.month_label);
 		dayLabel = (TextView) findViewById(R.id.day_label);
 		yearLabel = (TextView) findViewById(R.id.year_label);
 		sharedBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//Hide buttons
 				sharedBtn.setVisibility(View.INVISIBLE);
 				restartBtn.setVisibility(View.INVISIBLE);
 				
 				//Take photo
 				View view =  findViewById(R.id.rView);
 				view.setDrawingCacheEnabled(true);
 				BitmapFactory.Options options = new BitmapFactory.Options();
 	            options.inSampleSize = 4;
 				Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
 				
 				//Re-show buttons
				sharedBtn.setVisibility(View.INVISIBLE);
 				restartBtn.setVisibility(View.VISIBLE);
 				
 				//Share
 				String path = Images.Media.insertImage(getContentResolver(), bitmap, "Image to Share", null);
 				Uri sharedUri = Uri.parse(path);
 
 				Intent sharedIntent = new Intent(Intent.ACTION_SEND);
 				
 				sharedIntent.setType("image/png");
 				sharedIntent.putExtra(Intent.EXTRA_STREAM, sharedUri);
 				startActivity(Intent.createChooser(sharedIntent, "Share the pictures using..."));
 			}
 		});
 		restartBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				setResult(Activity.RESULT_OK);
 				finish();
 			}
 		});
         cId = this.getIntent().getExtras().getInt("cID");
         for(int i = 0; i < 2; i++)
         	setupImages(i);
         setDate();
 	}
 	
 	private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
 	private void setDate() {
 		
 		Calendar calendar = Calendar.getInstance(); 
 		monthLabel.setText(months[calendar.get(Calendar.MONTH)]);
 		dayLabel.setText("" + calendar.get(Calendar.DATE));
 		yearLabel.setText("" + calendar.get(Calendar.YEAR));
 	}
 	
 	private void setupImages(int index) {
 		File imageFile = new File(getCurrentImagePath(index));
 	   	if (imageFile.exists()){
     		BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = 4;
     	    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
     	    bitmap = rotateImage(bitmap, 80 + index * 20);
     	    
     	    BlurMaskFilter blurFilter = new BlurMaskFilter(5, BlurMaskFilter.Blur.OUTER);
     	    Paint shadowPaint = new Paint();
     	    shadowPaint.setMaskFilter(blurFilter);
     	    //shadowPaint.setShadowLayer(2f, 2f, 2f, );
 
     	    int[] offsetXY = {0, 0};
     	    Bitmap shadowImage = bitmap.extractAlpha(shadowPaint, offsetXY);
     	    Bitmap shadowImage32 = shadowImage.copy(Bitmap.Config.ARGB_8888, true);
 
 
     	    Canvas c = new Canvas(shadowImage32);
     	    c.drawBitmap(bitmap, 5, 5, null);
     	    
     	    ImageView iv = imageViews.get(index);
     	    
     	    iv.setImageBitmap(shadowImage32);
 
     	   
     	    iv.setVisibility(View.VISIBLE);
     	}
     	else {
     		imageViews.get(index).setVisibility(View.INVISIBLE);
     	}
 	}
 	
 	   private String getCurrentImagePath(int index) {
 	    	String path = Environment.getExternalStorageDirectory().toString();
 	        path += "/WordSprout-image-" + cId + "_" + index + ".jpg";
 	        return path;
 	    }
 	   
 	    private Bitmap rotateImage(Bitmap originalImage, int rotation) {
 	    	Matrix matrix = new Matrix();
 	    	matrix.postRotate(rotation);
 	    	return Bitmap.createBitmap(originalImage, 0, 0, 
 	    			originalImage.getWidth(), originalImage.getHeight(), 
 	    	                              matrix, true);
 	    }
 }
