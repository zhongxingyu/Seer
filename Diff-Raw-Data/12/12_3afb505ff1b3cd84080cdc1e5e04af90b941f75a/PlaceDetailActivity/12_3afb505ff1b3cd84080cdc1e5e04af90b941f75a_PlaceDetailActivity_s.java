 package edu.vanderbilt.vm.guide.ui;
 
 /**
  * @author Athran
  * Origin: GuideMain
  * Desc: A home page for interaction with Place
  */
 
 import java.io.InputStream;
 import java.net.URL;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import edu.vanderbilt.vm.guide.R;
 import edu.vanderbilt.vm.guide.container.Place;
 import edu.vanderbilt.vm.guide.util.GlobalState;
 import edu.vanderbilt.vm.guide.util.GuideConstants;
 
 @TargetApi(11)
 public class PlaceDetailActivity extends Activity{
 
 	private TextView mPlaceNameTv;
 	private ImageView mPlaceIv;
 	private TextView mPlaceDescTv;
 	private TextView mPlaceHoursTv;
 	private Button mMapButton;
 	private Bitmap mPlaceBitmap;
 	private Button mAgendaActionButton;
 	private Menu mMenu;
 	private boolean mIsOnAgenda = false;
 	private Place mPlace;
 	
 	private static final String ADD_STR = "Add to Agenda";
 	private static final String REMOVE_STR = "Remove";
 	private final int MENU_ADD_AGENDA = Menu.FIRST;	//
 	private static final Logger logger = LoggerFactory.getLogger("ui.PlaceDetailActivity");
 	
 	
 	@Override
 	public void onCreate(Bundle SavedInstanceState) {
 		super.onCreate(SavedInstanceState);
 		setContentView(R.layout.activity_place_detail);
 		
 		findViews();
 
 		/**
 		 * Sets the content of the page based on data from the place we got
 		 */
 		mPlace = getPlaceFromIntent();
 		// XXX: We can get a null place here right now.  This intentionally not
 		// being handled at the moment.  I want the app to crash if we get a
 		// null place so we'll get a stack trace and find out what went wrong.
 		// We'll handle null places at a later time (after we've switched to a
 		// Content Provider model instead of a list-based model).
 
 		mPlaceNameTv.setText(mPlace.getName());
 		mPlaceDescTv.setText(mPlace.getDescription());
 		mPlaceHoursTv.setText("Hours of operation: " + mPlace.getHours());
 		
 		// Setup ActionBar
 		ActionBar ab = getActionBar();
 		ab.setTitle("Place Detail");
 		ab.setDisplayHomeAsUpEnabled(true);
 		ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
 		
 		// Download image
 		Thread downloadImage = new Thread() {
 			@Override
 			public void run() {
 				try {
 					InputStream is = (InputStream) new URL(mPlace.getPictureUri().getPath()).getContent();
 					logger.trace("Download succeeded");
 					mPlaceBitmap = BitmapFactory.decodeStream(is);
 				} catch (Exception e) {
 					logger.error("Download failed", e);
 					mPlaceBitmap = null;
 				}
 			}
 		};
 		downloadImage.start();
 		try {
 			downloadImage.join();
 			mPlaceIv.setImageBitmap(mPlaceBitmap);
 		} catch (InterruptedException e) {
 			logger.error("Download failed", e);
 		}
 		
 		/* Check if this place is already on Agenda */
 		if(GlobalState.getUserAgenda().isOnAgenda(mPlace)) {
 			mAgendaActionButton.setText(REMOVE_STR);
 			mIsOnAgenda = true;
 		} else {
 			mAgendaActionButton.setText(ADD_STR);
 			mIsOnAgenda = false;
 		}
 		
 		/* Buttons' click definitions */
 		mAgendaActionButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				addRemoveToAgenda();
 			}
 		});
 		
 		mMapButton.setOnClickListener(new View.OnClickListener(){
 			@Override
 			public void onClick(View v){
 				Intent i = new Intent(PlaceDetailActivity.this, ViewMapActivity.class);
 				i.putExtra(GuideConstants.MAP_FOCUS, mPlace.getUniqueId());
 				startActivity(i);
 			}
 		});
 		/* End of Buttons' click definitions */
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.place_detail_activity, menu);
 	    mMenu = menu;
 	    
 	    if (this.mIsOnAgenda){
 	    	/*
 	    	 * The default icon is a "+"
 	    	 * therefore change to "-"
 	    	 */
 	    	mMenu.findItem(R.id.add_agenda).setIcon((Drawable)getResources().getDrawable(R.drawable.content_remove));
 	    } else {
 	    	// Use default icon "+" as defined in xml
 	    }
 	    
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item){
 		Intent i;
 		
 		switch (item.getItemId()){
 		case R.id.add_agenda:
 			addRemoveToAgenda();
 			return true;
 		case R.id.menu_map:
 			i = new Intent(PlaceDetailActivity.this, ViewMapActivity.class);
 			i.putExtra("map_focus", mPlace.getUniqueId());
 			startActivity(i);
 			return true;
 		case android.R.id.home:
 			i = new Intent(this, GuideMain.class);
 			startActivity(i);
 			return true;
 		default: 
 			return false;
 		}
 	}
 
 	private Place getPlaceFromIntent() {
 		Intent myIntent = getIntent();
 		if (myIntent == null)
 			return null;
 		int placeId = myIntent.getIntExtra(GuideConstants.PLACE_ID_EXTRA,
 				GuideConstants.BAD_PLACE_ID);
 		return GlobalState.getPlaceById(placeId);
 	}
 	
 	private void findViews() {
 		mPlaceNameTv = (TextView) findViewById(R.id.PlaceName);
 		mPlaceIv = (ImageView) findViewById(R.id.PlaceImage);
 		mPlaceDescTv = (TextView) findViewById(R.id.PlaceDescription);
 		mMapButton = (Button) findViewById(R.id.BMap);
 		mAgendaActionButton = (Button) findViewById(R.id.BAgendaAction);
 		mPlaceHoursTv = (TextView) findViewById(R.id.PlaceHours);
 	}
 	
 	private void addRemoveToAgenda(){
 		
 		if(mIsOnAgenda) {
 			GlobalState.getUserAgenda().remove(mPlace);
 			mAgendaActionButton.setText(ADD_STR);
			mMenu.getItem(0).setIcon((Drawable)getResources().getDrawable(R.drawable.content_new));
 			Toast.makeText(this, "Removed from Agenda", Toast.LENGTH_SHORT).show();
 		} else {
 			GlobalState.getUserAgenda().add(mPlace);
 			mAgendaActionButton.setText(REMOVE_STR);
			mMenu.getItem(0).setIcon((Drawable)getResources().getDrawable(R.drawable.content_remove));
 			Toast.makeText(this, "Added to from Agenda", Toast.LENGTH_SHORT).show();
 		}
 		mIsOnAgenda = !mIsOnAgenda;
 	}
 }
