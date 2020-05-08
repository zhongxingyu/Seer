 package com.dotcom.nextup.activities;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.dotcom.nextup.R;
 import com.dotcom.nextup.categorymodels.Category;
 import com.dotcom.nextup.categorymodels.CategoryHistogram;
 import com.dotcom.nextup.classes.RecommendationInput;
 import com.dotcom.nextup.classes.Venue;
 import com.dotcom.nextup.datastoring.BackendManager;
 import com.dotcom.nextup.datastoring.CategoryHistogramManager;
 import com.dotcom.nextup.yelp.Yelp;
 import com.dotcom.nextup.yelp.YelpVenue;
 import com.google.android.maps.GeoPoint;
 
 public class Home extends ListActivity {
 	Bundle bundle;
 	
 	double latitude;
 	double longitude;
 	String name;
 	double max_distance = 3000;
 	
 	private Context context;
 	
 	ArrayList<Category> categories_now = new ArrayList<Category>();
 	ArrayList<Category> categories_next_custom = null;
 	ArrayList<Category> categories_next_cloud = null;
 	RecommendationInput input = null;
 	Button before;
 	Button forward;
 	private ArrayList<Venue> my_venues = null;
 	private VenueAdapter m_adapter;
 	ProgressDialog dialog = null;
 	private Runnable viewVenues;
 	
 	int my_venues_index_of_first_to_display = 0; // inclusive
 	int my_venues_index_of_last_to_display = 2;  // inclusive
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = this;
 		try {
 			// all of Home hinges on being able to extractLocationData
 			// if we fail at that, there is no point in doing anything else
 			
 			extractLocationData(getIntent());
 			setContentView(R.layout.main_checkedin);
 			before = (Button)findViewById(R.id.ShowVenuesBefore);
 			before.setVisibility(View.INVISIBLE);
 			before.setClickable(false);
 			forward = (Button)findViewById(R.id.ShowVenuesForward);
 			forward.setVisibility(View.INVISIBLE);
 			forward.setClickable(false);
 			my_venues = new ArrayList<Venue>();
 			this.m_adapter = new VenueAdapter(this, R.layout.row, my_venues);
 			setListAdapter(this.m_adapter);
 			
 			viewVenues = new Runnable() {
 				@Override
 				public void run() {
 					getVenues();
 				}
 			};
 
 			Thread thread = new Thread(null, viewVenues, "GettingVenuesThread");
 			dialog = ProgressDialog.show(Home.this, "", "Loading. Please wait...", true);
 			thread.start();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			setContentView(R.layout.fail);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			setContentView(R.layout.fail);
 		} catch (NullPointerException e) {
 			// we couldn't find their location, so there's no data to extract
 			e.printStackTrace();
 			setContentView(R.layout.fail);
 		}
 	}
 
 
 	private void extractLocationData(Intent intent) 
 		throws IOException, ClassNotFoundException, NullPointerException {
 		//Pull location data
 		String[] latlong = intent.getStringExtra("location").split(",");
 
 		this.latitude = Double.parseDouble(latlong[0])/1E6;
 		this.longitude = Double.parseDouble(latlong[1])/1E6;
 		// isn't the "distance" here the distance the person is from the selected venue?
 		// this is NOT the same as the max distance they are willing to travel to go to the next place
 		this.max_distance = Double.parseDouble(String.valueOf((intent.getIntExtra("distance", -1))));
 		
 		//Pull name
 		this.name = intent.getStringExtra("name");
 		
 		//Pull Categories
 		int iter = intent.getIntExtra("numCats", 0);
 		
 		ArrayList<String> alreadyAccounted = new ArrayList<String>();
 		for (int i = 0; i < iter; i++) {
 			Category category = (Category)intent.getParcelableExtra("Category" + new Integer(i).toString());
 			if (!alreadyAccounted.contains(category.getName())) {
 				this.categories_now.add(category);
 				alreadyAccounted.add(category.getName());
 			}
 		}		
 	}
 
 
 	public void toFriends(View view) {
 		Intent toFriends = new Intent(this, Friends.class);
 		startActivity(toFriends);
 	}
 
 	public void toMap(View view) {
 		Intent toMap = new Intent(this, Map.class);
 		startActivity(toMap);
 	}
 
 	public void toPreferences(View view) {
 		Intent toPreferences = new Intent(this, Preferences.class);
 		startActivity(toPreferences);
 	}
 	
 	public void showNextThreeVenues(View view) {
 		if (my_venues == null || my_venues.size() == 0) return;
 		if (my_venues_index_of_last_to_display == my_venues.size() - 1) return;
 		int start = my_venues_index_of_last_to_display + 1;
		int end = min(start + 3, my_venues.size());
 		updateAdapter(start, end);
 	}
 	
 	public void showPrevThreeVenues(View view) {
 		if (my_venues == null || my_venues.size() == 0) return;
 		if (my_venues_index_of_first_to_display == 0) return;
 		int end = my_venues_index_of_first_to_display;
 		int start = end - 3;
 		updateAdapter(start, end);
 	}
 
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 	    Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( my_venues.get(position).getURL() ) );
 	    startActivity( browse );
 	}
 	
 	private Runnable returnRes = new Runnable() {
 		@Override
 		public void run() {
 			dialog.dismiss();
 			Log.v("Home", "running returnRes");
 			updateAdapter(0,3);
 		}
 	};
 	
 	private void updateAdapter(int start, int end) { // start is inclusive, end is exclusive
 		// updates adapter to display venues in range [start, end) of my_venues
 		// ex. [0, 3) displays 0, 1, and 2
 		// ex. [2, 2) displays nothing
 		
 		Log.v("Home", "updateAdapter("+Integer.toString(start)+","+Integer.toString(end)+")");
 				
 		if (my_venues == null) return;
 		int nven = my_venues.size();
 		if (nven == 0) return;
 		if ( start < 0 || start >= nven || start > end ) start = 0;
 		if ( end <= 0   || end > nven    || end < start ) end = min(3, nven);
 		
 		Log.v("Home", "updateAdapter corrected start="+Integer.toString(start)+", end="+Integer.toString(end));
 		
 		m_adapter.clear();
 		m_adapter.notifyDataSetChanged();
 		
 		int i = start;
 		my_venues_index_of_first_to_display = i;
 		while (i < end && i < nven) {
 			m_adapter.add(my_venues.get(i));
 			i++;
 		}
 		my_venues_index_of_last_to_display = i - 1;
 		
 		if (my_venues_index_of_first_to_display == 0) {
 			before.setVisibility(View.INVISIBLE);
 			before.setClickable(false);
 		} else {
 			before.setVisibility(View.VISIBLE);
 			before.setClickable(true);
 		}
 		if (my_venues_index_of_last_to_display == my_venues.size()-1) {
 			forward.setVisibility(View.INVISIBLE);
 			forward.setClickable(false);
 		} else {
 			forward.setVisibility(View.VISIBLE);
 			forward.setClickable(true);
 		}
 		
 		Log.v("Home", "my_venues_index_of_first_to_display="+Integer.toString(my_venues_index_of_first_to_display));
 		Log.v("Home", "my_venus_index_of_last_to_display="+Integer.toString(my_venues_index_of_last_to_display));
 
 		
 		m_adapter.notifyDataSetChanged();
 	}
 	
 	private void getVenues() {
 		try {
 			Log.v("Home", "entering getVenues()");
 			/* uses up limited actual Yelp queries */
 			Yelp yelp = getYelp();
 			getNextCategories();
 			makeRecommendationInput();
 			ArrayList<YelpVenue> venues;
 			if (input != null) {
 				venues = yelp.getRecommendation(input);
 			} else {
 				venues = new ArrayList<YelpVenue>();
 			}
 			
 			my_venues = new ArrayList<Venue>();			
 			for (int i = 0; i < venues.size(); i++) {
 				YelpVenue yven = venues.get(i);
 				int lat = (int)(yven.getLatitude() * 1E6);
 				int lon = (int)(yven.getLongitude() * 1E6);
 				GeoPoint gp = new GeoPoint(lat, lon);
 				Venue ven = new Venue(yven.getName(), yven.getURL(), yven.getImageURL(), gp, yven.getDistance(), null);
 				ven.setRating(yven.getRating());
 				my_venues.add(ven);
 			} 
 
 		} catch (Exception e) {
 			Log.e("Home", "getVenues(): "+e.toString());
 		}
 		runOnUiThread(returnRes);
 	}
 	
 	private void makeRecommendationInput() {
 		input = new RecommendationInput(categories_next_custom, categories_next_cloud, latitude, longitude, 3000, 9);
 	}
 	
 	private void getNextCategories() {
 		categories_next_custom = new ArrayList<Category>();
 		categories_next_cloud = new ArrayList<Category>();
 		CategoryHistogram ch = CategoryHistogramManager.getHistogramFromPhone(context);
 		
 		for (Category inputCat: categories_now) {
 			if (ch != null)
 				categories_next_custom.addAll(ch.getAllSuffixes(inputCat));
 				categories_next_cloud.addAll(BackendManager.getSuggestionsFromCloud(inputCat));
 		}
 		
 		// if there are too few 'real' categories, add a few 'fake' ones
 		if (categories_next_custom.size() + categories_next_cloud.size() < 3) {
 			categories_next_cloud.add(new Category("burritos", 2, 18));
 			categories_next_cloud.add(new Category("ice cream", 1, 15));
 			categories_next_cloud.add(new Category("coffee", 3, 14));
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private boolean isIn(Category kitty, ArrayList<Category> cats) {
 		for (Category cat : cats) {
 			if ( kitty.getName().equals(cat.getName())) return true;
 		}
 		return false;
 	}
 	
 	/* like everything in Java, you need to make a Yelp object in order to actually do anything
 	 * (actually there's a reason for this:  it authorizes you with the Yelp API) */
     public Yelp getYelp() {
     	Log.v("Yelp", "entering getYelp()");
         String consumerKey = getString( R.string.oauth_consumer_key );
         String consumerSecret = getString( R.string.oauth_consumer_secret);
         String token = getString(R.string.oauth_token);
         String tokenSecret = getString(R.string.oauth_token_secret);
         
         Yelp yelp = new Yelp(consumerKey, consumerSecret, token, tokenSecret);
         
         return yelp;
     }
     
     private int min(int a, int b) { if (a < b) { return a; } return b; }
     
     public class VenueAdapter extends ArrayAdapter<Venue> {
 
     	private Context context;
     	private ArrayList<Venue> items;
 
     	public VenueAdapter(Context context, int textViewResourceId, ArrayList<Venue> items) {
     		super(context, textViewResourceId, items);
     		this.items = items;
     		this.context = context;
     	}
 
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent) {
     		View v = convertView;
     		if (v == null) {
     			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     			v = vi.inflate(R.layout.row, null);
     		}
     		Venue o = items.get(position);
     		if (o != null) {
     			TextView tt = (TextView) v.findViewById(R.id.toptext);
     			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
     			ImageView iv = (ImageView) v.findViewById(R.id.icon);
     			if (tt != null) {
     				tt.setText(o.getName());
     			}
     			if (bt != null) {
     				bt.setText(Double.toString(o.getRating()));
     			}
     			if (iv != null) {
     	    		Drawable image = ImageOperations(context, items.get(position).getImageURL(), "item" + Integer.toString(position) + ".jpg");
     	    		if (image == null) {
     	    			/* supposed to display this when the image can't be gotten from the url
     	    			 * but instead, no image displays, which is ok but doesn't look so good
     	    			 * probably returning null because it's an incorrect path name */
     	    			 
     	    			image = Drawable.createFromPath("../../../../../res/drawable/default_venue_image.png");
     	    		}
     				iv.setImageDrawable(image);
     			}
     		}
     		return v;
     	}
     	
     	public ArrayList<Venue> getItems() { return items; }
         
         /* http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/ 
          * returns a Drawable from a URL for an image
          * an ImageView can set itself to display this Drawable as its image
          * example usage:
          * ArrayList<YelpVenue> venues; //already got them using yelp.getRecommendation(), see getVenues() method in Home
          * Drawable image = ImageOperations(this,venues.get(0).rating_img_url_small,"image.jpg");
            ImageView imgView = (ImageView)findViewById(R.id.image1);
            imgView.setImageDrawable(image); */
          
     	private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
     		try {
     			InputStream is = (InputStream) this.fetch(url);
     			Drawable d = Drawable.createFromStream(is, saveFilename);
     			return d;
     		} catch (MalformedURLException e) {
     			e.printStackTrace();
     			return null;
     		} catch (IOException e) {
     			e.printStackTrace();
     			return null;
     		}
     	}
 
         /* http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/
          * used by ImageOperations to get an image from a URL */
          
     	public Object fetch(String address) throws MalformedURLException,IOException {
     		URL url = new URL(address);
     		Object content = url.getContent();
     		return content;
     	}
     } 
 }
 
