 package com.trugertech.quickbart;
 
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Displays the selected favorite schedule. The name of the favorite 
  * is displayed on top followed by a list or of future
  * departure times from the station.
  * The departure time, arrival time, and fare are displayed for
  * each departure for the selected favorite route.
  * @author scott
  *
  */
 public class ActivityFavoriteSchedule extends ListActivity{
 	
 	private static final int ACTIVITY_DETAILS = 10;
 	private static final int ACTIVITY_EDIT = 11;
 	
 	private static final int EDIT_ID = Menu.FIRST;
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	
 	private QuickBartDbAdapter mDbHelper;
 	private Long mRowId;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         try{
         	mDbHelper = new QuickBartDbAdapter(this);
         	mDbHelper.open();
         }
         catch(Exception e){
         	System.out.print(e.getMessage().toString());
         }
         
 
         setContentView(R.layout.favorite_schedule);
         setTitle(R.string.schedule_name); 
         
         //retrieve stored reference to current favorite to display
         mRowId = (savedInstanceState == null) ? null :
             (Long) savedInstanceState.getSerializable(QuickBartDbAdapter.KEY_ROWID);
 		if (mRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			mRowId = extras != null ? extras.getLong(QuickBartDbAdapter.KEY_ROWID)
 									: null;
 		}
         
         populateSchedule();
     }
 	
 	/**
 	 * Menu items for Edit and Delete the currently displayed favorite
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.menu_favorites_edit);
 		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_favorites_delete);
 		return true;
 	}
 	
 	
 	/**
      * Sets menu action to create a favorite when selected
      */
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
     	boolean result = super.onOptionsItemSelected(item);
 		
 		FavoriteRouteAdapter info;
     	
         switch(item.getItemId()) {
             case EDIT_ID:
             	info = (FavoriteRouteAdapter) this.getListAdapter();
             	Intent i = new Intent(this, ActivityFavoriteEdit.class);
             	i.putExtra(QuickBartDbAdapter.KEY_ROWID, info.getFavoriteId());
                	startActivityForResult(i, ACTIVITY_EDIT);
                	return true;
                 
             case DELETE_ID:
             	info = (FavoriteRouteAdapter) this.getListAdapter();
                 mDbHelper.deleteFavorite(info.getFavoriteId());
                 setResult(RESULT_OK);
                 finish();
                 return true;
             	
         }
         
         return result;
 	}
 	
 	/**
 	 * When a specific schedule is clicked display the details for that schedule
 	 */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id){
 		super.onListItemClick(l, v, position, id);
         Intent i = new Intent(this, ActivityFavoriteDetails.class);
         
         //determine which route was selected
         FavoriteRoute selectedRoute = (FavoriteRoute) getListView().getItemAtPosition(position);
         //save off the trip legs for the selected route
         i.putExtra("TripLegs", selectedRoute.getTripLegs());
         
         startActivityForResult(i, ACTIVITY_DETAILS);
 	}
 
 	/**
 	 * Populates the schedule display for the selected favorite route
 	 * Goes to the DB to get the route information
 	 * Gets the schedule information for the route from BART API
 	 */
     private void populateSchedule() {
         String favName;
     	String favDest;
         String favDepart;
         ArrayList<FavoriteRoute> favRoute = new ArrayList<FavoriteRoute>();
     	
         if (mRowId != null) {
             Cursor favorite = mDbHelper.fetchFavorite(mRowId);
             startManagingCursor(favorite);
             
             favName = favorite.getString(
             		favorite.getColumnIndexOrThrow(QuickBartDbAdapter.KEY_FAVORITE_NAME));
             favDest = favorite.getString(
             		favorite.getColumnIndexOrThrow(QuickBartDbAdapter.KEY_DESTINATION_SHORT));
             favDepart = favorite.getString(
             		favorite.getColumnIndexOrThrow(QuickBartDbAdapter.KEY_DEPARTURE_SHORT));
             
             favRoute = getSchedule(favDepart, favDest);
             
             // set layout attributes with Schedule Item values
             FavoriteRouteAdapter favAdapter = new FavoriteRouteAdapter(
             		this, R.layout.favorite_schedule_item, favRoute, mRowId);
             setListAdapter(favAdapter);
             
             //Set the favorite name that is being displayed
             TextView tvFavName = (TextView)findViewById(R.id.favName);
             tvFavName.setText(favName);
             
         }
         else{
         	//TODO: Toast that there was no route selected. Go back to main screen
         }
     }
     
     /**
      * Uses the parameters to pass to the Bart API to get the schedule information for the
      * departure and destination stations and returns the result as an ArrayList of FavoriteRoutes.
      * 
      * @param departure
      * @param destination
      * @param favoriteRoutes
      * @return
      */
     private ArrayList<FavoriteRoute> getSchedule(String departure, String destination){
     	ArrayList<FavoriteRoute> favoriteRoutes = new ArrayList<FavoriteRoute>();
     	try{
     		// set up XML source
         	URL bartURL = new URL(BartAPI_URIGenerator.getCmd_Sched(departure, destination));
         	InputSource is = new InputSource(bartURL.openStream());
         	
         	//create XML factory
         	SAXParserFactory factory = SAXParserFactory.newInstance();
         	
         	//crate XML parser
         	SAXParser parser = factory.newSAXParser();
         	
         	//create XML reader
         	XMLReader xmlReader = parser.getXMLReader();
         	
         	//instantiate handler	        	
         	BartAPI_cmd_sched bfh = new BartAPI_cmd_sched();
         	
         	//assign handler
         	xmlReader.setContentHandler(bfh);
         	
         	//parse
         	xmlReader.parse(is);
         	
         	//get some data!
         	favoriteRoutes = bfh.getResults();	
     	}
     	catch(Exception e){
     		Context context = getApplicationContext();
     		CharSequence text = "Sorry there was an error accessing BART information. Please try again.";
     		// TODO: for debugging, remove next line
     		text = e.getMessage();
     		int duration = Toast.LENGTH_LONG;
 
     		Toast toast = Toast.makeText(context, text, duration);
     		toast.show();
     	}	
     	
     	return favoriteRoutes;
     }
 
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putSerializable(QuickBartDbAdapter.KEY_ROWID, mRowId);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         populateSchedule();
     }
 
 }
