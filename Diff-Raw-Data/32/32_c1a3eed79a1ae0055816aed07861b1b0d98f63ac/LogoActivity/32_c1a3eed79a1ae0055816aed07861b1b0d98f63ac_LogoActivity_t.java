 package com.example.maple_android;
 
 import java.io.ByteArrayOutputStream;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.drawable.Drawable;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class LogoActivity extends Activity {
 	private byte[] byteArray;
 	private ImageView photo;
 	private String companyTag;
 	private Bitmap srcBitmap;
 	
 	/* Logo details */
 	private ImageView logoView;
 	private Bitmap logoSrc;
 	private Bitmap logoScaled;
 	private int logoWidth;
 	private int logoHeight;
 	private final double SCALE_FACTOR = 0.3;
 	private float logo_x_offset;
 	private float logo_y_offset;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_logo);
 		
 		// get picture 
 		Bundle extras = getIntent().getExtras();
 		byteArray = extras.getByteArray("photoByteArray");
 		srcBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
 		
 		// get company name
 		companyTag = extras.getString("companyTag");
 		
 		// set photo
 		photo = (ImageView)this.findViewById(R.id.photo);
         photo.setImageBitmap(srcBitmap);
         
        
        
         // initialize photo for clicking
         photo.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 return placeLogo(v, event); 	
             	
             }		
         });
         
         // set page title
         TextView title = (TextView)this.findViewById(R.id.companyTag);
      	title.setText("Add A " + companyTag + " Logo!");
         
      	// load logo
      	logoView = (ImageView)this.findViewById(R.id.logoPic);
      	logoSrc = BitmapFactory.decodeResource(getResources(), R.drawable.nike);
      	logoView.setImageBitmap(logoSrc);
     	
     	
      	// initialize for scaling
      	logoWidth = logoSrc.getWidth();
      	logoHeight = logoSrc.getHeight();
      	logoScaled = logoSrc;
      	
     	// scale logo to a quarter of picture size
     	while(logoScaled.getWidth() > srcBitmap.getWidth()){
     		changeLogoSize(findViewById(R.id.decreaseSize));
     	}
     	
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.logo, menu);
 		return true;
 	}
 	
 	private boolean placeLogo(View v, MotionEvent event) {
		// hide instructions
		findViewById(R.id.logoInstructions).setVisibility(View.INVISIBLE);
		
		// show logo
		logoView.setVisibility(View.VISIBLE);
		
 		logo_x_offset = event.getX() - logoWidth / 2;
 		logo_y_offset = event.getY() - logoHeight / 2;		
 		
 		logoView.setX(logo_x_offset + v.getX());
 		logoView.setY(logo_y_offset + v.getY());
 		
 		return true;		
 	}
 
 	
 	public void changeLogoSize(View view){
 		// check if we are decreasing or increasing size
 		double modifier = SCALE_FACTOR;
		if(view.equals(findViewById(R.id.decreaseSize))) modifier *= -1.0;
 		
 		// change logo dimensions
 		logoWidth = (int) (logoWidth * (1 + modifier)); 
 		logoHeight = (int) (logoHeight * (1 + modifier)); 
 		
 		// scale source and update ImageView
 		// make filter flag true to improve quality. Worth it?
 		logoScaled = Bitmap.createScaledBitmap(logoSrc, logoWidth, logoHeight, true);
 		logoView.setImageBitmap(logoScaled);
 	}
 	
 	public void save(View view){
 		// combine two bitmaps
 		Bitmap bmOverlay = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
         Canvas canvas = new Canvas(bmOverlay);
         canvas.drawBitmap(srcBitmap, new Matrix(), null);
         canvas.drawBitmap(logoScaled, logo_x_offset, logo_y_offset, null);
 		
 		
 		// save picture to byte array and return
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();        
 		bmOverlay.compress(Bitmap.CompressFormat.PNG, 100, stream);
         byteArray = stream.toByteArray();
         
         returnToEditor(view);        
 	}
 	
 	public void returnToEditor(View view){
 		Intent i = new Intent(this, EditorActivity.class);
 		i.putExtra("photoByteArray", byteArray);
 		i.putExtra("companyTag", companyTag);
 		i.putExtra("accessToken", getIntent().getExtras().getString("accessToken"));
 		startActivity(i);
 	}
 
 }
