 package com.example.lufteapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.PictureDrawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 
 import com.caverock.androidsvg.SVG;
 import com.caverock.androidsvg.SVGParseException;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		
										//finds the right view for .svg element to populated
 		ImageView  imageView = (ImageView) findViewById(R.id.pictureMain);
 	      imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 	      try
 	      {								//fetching file from drawable resource, rendering, setting and populating
 	         SVG svg = SVG.getFromResource(this, R.drawable.applogo);
 	         Drawable drawable = new PictureDrawable(svg.renderToPicture());
 	         imageView.setImageDrawable(drawable);
 	      }
 	      catch(SVGParseException e)
 	      {}
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void openMap(View view) {
 		Intent intent = new Intent(this, ViewMap.class);
 		startActivity(intent);
 	}
 	
 	public void openLog(View view) {
 		Intent intent = new Intent(this, ActivityLog.class);
 		startActivity(intent);
 	}
 	
 	public void checkIn(View view) {
 		Intent intent = new Intent(this, GpsData.class);
 		startActivity(intent);
 	}
 }
