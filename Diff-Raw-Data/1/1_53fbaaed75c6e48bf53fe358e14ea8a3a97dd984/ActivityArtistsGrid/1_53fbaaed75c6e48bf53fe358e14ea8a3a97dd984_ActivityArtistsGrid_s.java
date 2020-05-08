 package com.prcse.jamjar;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 
 import com.prcse.datamodel.Artist;
 import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
 import com.slidingmenu.lib.SlidingMenu.OnOpenedListener;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ActivityArtistsGrid extends Activity implements OnClickListener, OnTouchListener, OnItemClickListener, OnClosedListener, OnOpenedListener {
 
 	private RelativeLayout menu_profile_btn;
 	private RelativeLayout menu_spotlight_btn;
 	private RelativeLayout menu_search_btn;
 	private RelativeLayout menu_artists_btn;
 	private RelativeLayout menu_venues_btn;
 	private RelativeLayout menu_tours_btn;
 	private ImageView menu_profile_icon;
 	
 	private ActionBar actionBar;
 	private GridView artistGrid;
 	private ArtistGridAdapter artistAdapter;
 	private ArrayList<Artist> artists;
 	private JarLid appState;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_artists_grid);
         setTitle(R.string.title_activity_artists);
         
         // get global variables
         appState = ((JarLid)this.getApplication());
         for(Object a : appState.getArtists()) {
         	artists.add(((Artist)a));
         }
         
         // sets up sliding menu tray
         menuTraySetUp();
 
         // gets grid, set values to custom adapter then sets adapter to grid.
         // Finally, sets listener for artist select.
         artistGrid = (GridView) findViewById(R.id.artists_grid);
         artistAdapter = new ArtistGridAdapter(this, appState);
         artistGrid.setAdapter(artistAdapter);
         artistGrid.setOnItemClickListener(this);
         
         artistAdapter.setArtists(artists);
 
         artistGrid.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				Toast.makeText(ActivityArtistsGrid.this, "" + position, Toast.LENGTH_SHORT).show();
 				Intent intent = new Intent(v.getContext(), ActivityArtistDetail.class);
 				intent.putExtra("artist", artists.get(position));
 				startActivity(intent);
 			}
         });
         
         appState.getConnection().addObserver(new Observer() {
 			
 			@Override
 			public void update(Observable arg0, Object arg1) {
 				
 				for(Object a : appState.getArtists()) {
 		        	artists.add(((Artist)a));
 		        }
 				artistAdapter.setArtists(artists);
 			}	
 		});
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.artists, menu);
         return true;
     }
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	Intent intent = null;
     	
     	switch (item.getItemId()) {
 			case android.R.id.home:
 				MenuTraySingleton.getInstance().getMenu_tray().toggle();
 				break;
 		}
 		
 		return true;
 	}
     
 	public ArrayList<Artist> getArtists() {
 		return artists;
 	}
 
 	public void setArtists(ArrayList<Artist> artists) {
 		this.artists = artists;
 	}
 

 	private void menuTraySetUp() {
 		
 		//Get variables needed to calculate width of display. Also some nifty stuff with the action bar button.
 		actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         
         
         // get width of application on device
         Display display = getWindowManager().getDefaultDisplay();
 		Point point = new Point();
 		display.getSize(point);
 		int width = point.x;
         
 		//Call instance of singleton and its setup method
         MenuTraySingleton.getInstance().menuTraySetUp(this, width);
     	
         //Attach listeners to the sliding menu
     	MenuTraySingleton.getInstance().getMenu_tray().setOnOpenedListener(this);
         MenuTraySingleton.getInstance().getMenu_tray().setOnClosedListener(this);
 		
         
         // Get menu elements (items)
 		menu_profile_btn = (RelativeLayout) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.profile);
 		menu_spotlight_btn = (RelativeLayout) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.spotlight);
 		menu_search_btn = (RelativeLayout) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.search);
 		menu_artists_btn = (RelativeLayout) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.artists);
 		menu_venues_btn = (RelativeLayout) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.venues);
 		menu_tours_btn = (RelativeLayout) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.tours);
 
 		//Set listeners to clickable things
 		menu_profile_btn.setOnClickListener(this);
 		menu_spotlight_btn.setOnClickListener(this);
 		menu_search_btn.setOnClickListener(this);
 		menu_artists_btn.setOnClickListener(this);
 		menu_venues_btn.setOnClickListener(this);
 		menu_tours_btn.setOnClickListener(this);
 		
 
 		//Change background colour of menu item representing the current activity to make it stand out
 		menu_artists_btn.setBackgroundColor(Color.parseColor("#7f4993"));
 		
 		//Check to see if user is logged in and if so places profile picture in the sliding menu next to the profile selection
 		if (appState.isLoggedIn())
 		{
 			// change the menu to display there name...
 			TextView menu_profile_text = (TextView) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.profile_text);
 			ImageView menu_profile_icon = (ImageView) MenuTraySingleton.getInstance().getMenu_tray().findViewById(R.id.profile_icon);
 			
 			// and, if available, their profile icon. 
 			menu_profile_text.setText(appState.getUser().getCustomer().getFullName());
 			if (appState.getUser().getCustomer().getThumb() != null)
 			{
 				menu_profile_icon = (ImageView) findViewById(R.id.profile_icon);
 				menu_profile_icon.setImageBitmap(appState.getUser_image());
 			}
 		}
 	}
 	
     @Override
     public void onClick(View view)
     {
     	Intent intent = null;
     	
     	//Toggles sliding menu if open
     	if(MenuTraySingleton.getInstance().getMenu_tray().isMenuShowing()){
     		MenuTraySingleton.getInstance().getMenu_tray().toggle();
     	}
     	
     	switch(view.getId()){
     	
     	//Implementation of OnClickListener. Starts an appropriate intent, using flags to prevent duplicate activities
     	//being created and wasting memory.
     	case R.id.profile:
     		if (appState.isLoggedIn())
     		{
     			intent = new Intent(view.getContext(), ActivityProfile.class);
         		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
     			startActivity(intent);
     		}
     		else 
     		{
     			intent = new Intent(view.getContext(), ActivityLogin.class);
         		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
     			startActivity(intent);
     		}
     		break;
     		
     	case R.id.spotlight:
     		intent = new Intent(view.getContext(), ActivitySpotlight.class);
     		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
 			startActivity(intent);
     		break;
     		
     	case R.id.search:
     		intent = new Intent(view.getContext(), ActivitySearch.class);
     		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
     		startActivity(intent);
     		break;
     		
     	case R.id.artists:
     		intent = new Intent(view.getContext(), ActivityArtistsGrid.class);
     		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
 			startActivity(intent);
     		break;
     		
     	case R.id.venues:
     		intent = new Intent(view.getContext(), ActivityVenuesGrid.class);
     		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
 			startActivity(intent);
     		break;
     			
     	case R.id.tours:
     		intent = new Intent(view.getContext(), ActivityToursGrid.class);
     		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
 			startActivity(intent);
     		break;
     	}
     }
 
     //TODO comment this. Vlad has literally no idea why this is here because he didn't code ths bit.
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		
 		switch(view.getId())
 		{
     	
     	case R.id.profile:
     		menu_profile_btn.setBackgroundColor(Color.parseColor("#7f4993"));
     		break;
     		
     	case R.id.spotlight:
     		menu_spotlight_btn.setBackgroundColor(Color.parseColor("#7f4993"));
     		break;
     		
     	case R.id.search:
     		menu_search_btn.setBackgroundColor(Color.parseColor("#7f4993"));
     		break;
     		
     	case R.id.artists:
     		menu_artists_btn.setBackgroundColor(Color.parseColor("#7f4993"));
     		break;
     		
     	case R.id.venues:
     		menu_venues_btn.setBackgroundColor(Color.parseColor("#7f4993"));
     		break;
     			
     	case R.id.tours:
     		menu_tours_btn.setBackgroundColor(Color.parseColor("#7f4993"));
     		break;
 		}
 		return false;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 		switch(parent.getId())
 		{
 		case R.id.artist_venue_grid:
 			Intent intent = new Intent(view.getContext(), ActivityArtistDetail.class);
 			intent.putExtra("artist", artists.get(position));
 			startActivity(intent);
 			break;
 		}
 	}
 
 	@Override
 	public void onOpened() 
 	{
 		//Makes little arrow next to action bar icon disappear. Mainly because it drove me insane that it didn't do that. - Vlad.
 		actionBar.setDisplayHomeAsUpEnabled(false);
 	}
 
 	@Override
 	public void onClosed() 
 	{
 		//Makes little arrow next to action bar icon appear when menu is closed. Mainly because it drove me insane that it didn't do that. - Vlad
 		actionBar.setDisplayHomeAsUpEnabled(true);
 	}	
 }
