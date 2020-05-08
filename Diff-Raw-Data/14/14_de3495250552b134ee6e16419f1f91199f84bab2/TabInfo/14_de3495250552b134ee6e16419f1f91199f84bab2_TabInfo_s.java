 package com.datakom;
 
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.datakom.POIObjects.HaggleConnector;
 import com.datakom.POIObjects.POIObject;
 import com.google.android.maps.GeoPoint;
 
 public class TabInfo extends Activity implements OnClickListener {
 	HaggleConnector conn;
 	
 	private double global_rating = 0.0;
 	private boolean real_object = false;
 	private String searchTitle = "";
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tabinfoview);
 
 		TableLayout tl = (TableLayout) findViewById(R.id.comment_table);
 		Button showOnMap = (Button) findViewById(R.id.view_on_map_button);
 		
 		//First we need to get the data from the intent
 		Bundle extras = getIntent().getExtras();
 		if(extras != null)
 		{
 			this.real_object = true;
 			
 			searchTitle = extras.getString(com.datakom.POIObjects.HaggleConnector.SEARCH_TITLE);
 			setTitle(searchTitle);
 			
 			ArrayList<POIObject> objects = HaggleConnector.getInstance().getPOIObjectsByName(searchTitle);
 			
 			GeoPoint position;
 			
 			for(POIObject poiobj : objects)
 			{
 				setTitle(poiobj.getName());
 				tl.addView(addReview(poiobj.getDescription(), (int)poiobj.getRating()));
 				getPicture(HaggleConnector.STORAGE_PATH + "/" + poiobj.getPicPath());
 				
 				position = poiobj.getPoint();
 				this.global_rating+=poiobj.getRating();
 			}
 			this.global_rating=this.global_rating/objects.size();
 			
 			setStars();
 			// Bind the button to listenerclass (this)
 			showOnMap.setOnClickListener(this);
 			
 		} else {
 			setTitle("Haggle POI Application");
 			showOnMap.setVisibility(8); // Sets the visibility of this object to "GONE"
 			tl.addView(addReview(
 					"This is a sample application to show how Haggle    \n" +
 					"can be used within a point-of-interest application\n" +
 					"This was part of a school-project in the spring\n" +
 					"of 2010.", 5));
 			getPicture("/sdcard/hehe");
 		}
 	}
 	
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    if(this.real_object == true) {
	    	inflater.inflate(R.layout.tabmenu, menu);
	    }
	    	
	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
	        case R.id.track_exchanges:     
 	        	//Bundle objects = new Bundle();
 	        	//objects.putString("TRACE_OBJECTS", this.searchTitle);
 	        	Intent traceview = new Intent(TabInfo.this, TabTrace.class);
 	        	traceview.putExtra(com.datakom.POIObjects.HaggleConnector.SEARCH_TITLE, searchTitle);
 				traceview.putExtra(com.datakom.POIObjects.HaggleConnector.SHOW_TRACE, "true");
 				TabInfo.this.startActivity(traceview);
         	break;
 	    }
 	    return true;
 	}
 	
 	
 	protected void getPicture(String path) 
 	{
 		File imageFile = new File(path);
 		ImageView iv = (ImageView) findViewById(R.id.poi_img);
 		
 		if(imageFile.exists())
 		{
 			Bitmap thePicture = BitmapFactory.decodeFile(path);
 			iv.setImageBitmap(thePicture);
 		} else {
 			iv.setImageResource(R.drawable.btn_rating_star_off_selected);
 		}
 	}
 	
 	protected void getPicture(Bitmap picture) 
 	{
 		ImageView iv = (ImageView) findViewById(R.id.poi_img);
 		iv.setImageBitmap(picture);
 	}
 	
 	// Sets the title of the info tab
 	protected TextView setTitle(String strtitle) {
 		TextView title = (TextView) findViewById(R.id.info_title);
 		title.setText(strtitle);
 		
 		return title;
 	}
 	
 	// This sets the stars
 	protected void setStars()
 	{
 		ImageView star1 = (ImageView) findViewById(R.id.star_1);
 		ImageView star2 = (ImageView) findViewById(R.id.star_2);
 		ImageView star3 = (ImageView) findViewById(R.id.star_3);
 		ImageView star4 = (ImageView) findViewById(R.id.star_4);
 		ImageView star5 = (ImageView) findViewById(R.id.star_5);
 
 		if(global_rating>4.75){
 			star5.setImageResource(R.drawable.rate_star_big_on);
 		} else if(global_rating>4.25) {
 			star5.setImageResource(R.drawable.rate_star_big_half);
 		} else {
 			star5.setImageResource(R.drawable.rate_star_big_off);
 		}
 		if(global_rating>3.75){
 			star4.setImageResource(R.drawable.rate_star_big_on);
 		} else if(global_rating>3.25) {
 			star4.setImageResource(R.drawable.rate_star_big_half);
 		} else {
 			star4.setImageResource(R.drawable.rate_star_big_off);
 		}
 		if(global_rating>2.75){
 			star3.setImageResource(R.drawable.rate_star_big_on);
 		} else if(global_rating>2.25) {
 			star3.setImageResource(R.drawable.rate_star_big_half);
 		} else {
 			star3.setImageResource(R.drawable.rate_star_big_off);
 		}
 		if(global_rating>1.75){
 			star2.setImageResource(R.drawable.rate_star_big_on);
 		} else if(global_rating>1.25) {
 			star2.setImageResource(R.drawable.rate_star_big_half);
 		} else {
 			star2.setImageResource(R.drawable.rate_star_big_off);
 		}
 		if(global_rating>0.75){
 			star1.setImageResource(R.drawable.rate_star_big_on);
 		} else if(global_rating> 0.25){
 			star1.setImageResource(R.drawable.rate_star_big_half);
 		} else {
 			star1.setImageResource(R.drawable.rate_star_big_off);
 		}
 	}
 	
 	// Inserts a row with a review
 	protected TableRow addReview(String comment, int rating){
 		TextView commentView = new TextView(TabInfo.this);
 		commentView.setText(comment);
 		
 					
 		TextView ratingView = new TextView(TabInfo.this);
 		ratingView.setText(rating + "/5");
 		ratingView.setGravity(0x05);
 		
 		
 		TableRow tr = new TableRow(TabInfo.this);
 		tr.addView(commentView);
 		tr.addView(ratingView);
 		return tr;
 	}
 
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.view_on_map_button:
 			
 			// View the new intent
 			Intent mapview = new Intent(TabInfo.this, TabMap.class);
 			mapview.putExtra(com.datakom.POIObjects.HaggleConnector.SEARCH_TITLE, searchTitle);
 			mapview.putExtra(com.datakom.POIObjects.HaggleConnector.SHOW_TRACE, "false");
 			TabInfo.this.startActivity(mapview);
 			break;
 		}
 		
 	}
 }
