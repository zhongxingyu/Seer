 package com.example.coverflow.producealmanac;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.LruCache;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.coverflow.R;
 import com.example.coverflow.ResourceImageAdapter;
 
 /**
  * The Class CoverFlowTestingActivity.
  */
 public class CoverFlowTestingActivity extends Activity {
 
 	ArrayList<Item> activeItems; // is this needed?
 	ArrayList<String> localNow; // used for searching, filters not relevant
 	ArrayList<String> localOut; // used for searching, local out of season
 	boolean populated=false;
 	int NUMBEROF = 6;
 	public ArrayList<ArrayList<Item>> itemsByFilter = new ArrayList<ArrayList<Item>>();
 	
 	//public String searchTerms = "";
 	public ArrayList<String> activeFilters;
 	public ArrayList<Store> activeStores = null;
 	public Store selectedStore = null;
 	
 	// caching bitmaps..
 	private LruCache<String, Bitmap> mMemoryCache;
 	
 	
 	TextView textView;
 	ResourceImageAdapter myAdapter;
 	
 	ActionBar actionBar;
 	
 	//Button/notification stuffs
 	ArrayList<View> allViews= new ArrayList<View>();
 	HashMap<View, View> subLayouts= new HashMap<View, View>();
 	HashMap<View, Boolean> expanded = new HashMap<View, Boolean>();
 	LinearLayout masterView;
 	Button button1;
 	Button button2;
 	LinearLayout row1;
 
 	
 	
 	//Static filter strings
 	public final static String BULBS = "bulb vegetables";
 	public final static String ROOTS  = "root vegetables";
 	public final static String FLOWER = "flower bud vegetables";
 	public final static String LEAF = "leaf vegetables";
 	public final static String FVEG = "fruit vegetables";
 	public final static String STALK = "stalk vegetables";
 	public final static String BERRIES = "berries";
 	public final static String DRUPES = "drupes";
 	
 	//Just use the ones below
 	public final static String FRUITS = "fruits";
 	public final static String VEGGIES = "vegetables";
 	
 	public final static String BERKELEYBOWL = "Berkeley Bowl";
 	public final static String TRADERJOES = "Trader Joes";
 	public final static String SAFEWAY = "Safeway";
 	public final static String YASAIMARKET = "Yasai Market";
 	public final static String WHOLEFOODS = "Whole Foods";
 	
 	ArrayAdapter<String> storeAdapter;
 	
 	
 	public final static String[] FILTERS = {FRUITS,VEGGIES};
 	public final static String[] STORES = {BERKELEYBOWL, TRADERJOES, SAFEWAY, YASAIMARKET, WHOLEFOODS};
 	
 	
 	// for gridview
 	ImageAdapter myImageAdapter;
 
 	// for search
     private ListView mListView;
     private SearchView searchView;
 
     /*
      * (non-Javadoc)
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate(final Bundle savedInstanceState) {
 
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
     	
     	//actionBar = getActionBar();
     	//actionBar.show();
     	//TESTING NOTIFICATION SCREEN
     	//testNotifs();
     	
         // Get max available VM memory, exceeding this amount will throw an
         // OutOfMemory exception. Stored in kilobytes as LruCache takes an
         // int in its constructor.
         final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
 
         // Use 1/8th of the available memory for this memory cache.
         final int cacheSize = maxMemory / 4;
 
         mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
             @Override
             protected int sizeOf(String key, Bitmap bitmap) {
                 // The cache size will be measured in kilobytes rather than
                 // number of items.
                 return bitmap.getByteCount() / 1024;
             }
         };
     	
     	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
     	
     	// set search functionality
     	
         // Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchView = (SearchView) findViewById(R.id.searchView);
         
         
         // Assumes current activity is the searchable activity
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
         
         //final SearchView.OnQueryTextListener queryTextListener = ; 
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { 
             @Override 
             public boolean onQueryTextChange(String newText) { 
                 // Do something 
             	showResults(newText);
             	System.out.println("showResults DONE");
                 return true; 
             } 
 
             @Override 
             public boolean onQueryTextSubmit(String query) { 
                 // Do something 
             	searchView.setIconified(true); 
                 return true; 
             } 
         });
         //searchView.setOnCloseListener(this);
         mListView = (ListView) findViewById(R.id.list);
 
 		if(!populated){
 			populated=true;
 			populateMap();
 		}
 		
 	
 		createAllItems();
 		Log.i("debugging", "after createAllItems");
 		
 		getClosestStores();
 		Log.i("debugging", "after getClosestStores");	
 		this.activeFilters = getActiveFilters();
 		this.localNow = getLocalNow();
 		this.localOut = getLocalOut();
 		Log.i("debugging", "after this. stuff");
 		//activeItems = getItemsNow();
 		
 		// temporarily set to initial selectedStore to BERKELEYBOWL
 		this.selectedStore = Store.storeMap.get(BERKELEYBOWL);
 		
 		this.itemsByFilter = getItemsByFilter();
 		Log.i("debugging", "after getitemsbyfilter");
 		//done initializing backend data
 
 		showUpdatedItems();
 /*
 		Log.i("debugging", "after showupdateditems");
         final MultiSpinner multispinner = (MultiSpinner) findViewById(this.getResources().getIdentifier("SpinnerCollegues", "id", "com.example.coverflow"));
       
         List<String> spinnerItems = new ArrayList<String>();
         for (String s: FILTERS){
         	spinnerItems.add(s);
         }
         Log.i("debugging", "after filers");
         multispinner.setItems(spinnerItems, "filter by category", new MultiSpinnerListener() {
         	public void onItemsSelected(boolean[] selected) {
         		// what happens when selected.
         		System.out.println("What's selected ? ");
         		ArrayList<String> selectedFilter = new ArrayList<String>();
         		for (int i = 0; i < selected.length; i++) {
         			if (selected[i] == true) {
         				System.out.print(i + " , ");
         				selectedFilter.add(FILTERS[i]);
         			}
         		}
         		setFilters(selectedFilter);
         		updateItemsByFilter();
         	}
         });
         Log.i("debugging", "after spinner");
         multispinner.setPrompt("FILTER");
         Log.i("debugging", "end of oncreate");
 */
 		
         final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        storeAdapter = new ArrayAdapter<String>(this, R.layout.spinner,STORES);
        storeAdapter.setDropDownViewResource(R.layout.spinner);
         spinner2.setAdapter(storeAdapter);
         
        
        
        
        
         spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				setStoreAndUpdateItems(Store.storeMap.get(((TextView)arg1).getText()));
 				System.out.println("### onitemselected called");
 				storeChanged();
 				System.out.println("### onitemselected ended....");
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}
         	
         });
         
         // NOTIFICATION!!
         createNotificationService();
     }
     
     public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
         if (getBitmapFromMemCache(key) == null) {
             mMemoryCache.put(key, bitmap);
         }
     }
 
     public Bitmap getBitmapFromMemCache(String key) {
         return mMemoryCache.get(key);
     }
     
     public void storeChanged() {
     	this.updateItemsByFilter();
     }
     
     public void setStoreAndUpdateItems(Store store) {
     	this.selectedStore = store;
     }
     
     private ArrayList<ArrayList<Item>> getItemsByFilter() {
 		/**Based on the active filters set, create a list of items for each
 		 * filter. The outer list being returned should always map 1 to 1 with
 		 * the FILTERS array, but the inner list of items can be any size (even empty).
 		 */
     	ArrayList<ArrayList<Item>> filteredItems = new ArrayList<ArrayList<Item>>();
     	ArrayList<Item> items;
     	for (int i =0; i < FILTERS.length; i++){
     		items = getActiveFilterList(i);
     		filteredItems.add(items);    		
     	}
     	return filteredItems;
 	}
 
 	@SuppressWarnings("unchecked")
 	private ArrayList<Item> getActiveFilterList(int filterIndex) {
 		/**Check if filter index is active. If active, then 
 		 * return a list of all items in both this group
 		 * and localNow. if inactive, return an empty list.
 		 */
 		ArrayList<Item> items = new ArrayList<Item>();
 		Item item;
 		if (activeFilters.contains(FILTERS[filterIndex])){
 			for (String name: localNow){
 				item = Item.itemMap.get(name);
 				if (item.group==FILTERS[filterIndex] && selectedStore.hasActive(item)){
 					items.add(item);
 				}
 			}
 			
 		}
 		Collections.sort(items);
 		return items;
 	}
 
 	public void testNotifs(){
 		Intent notifIntent = new Intent(this, NotificationActivity.class);
 		startActivity(notifIntent);
 	}
 	
 	//don't think we need this method, keep for now though
 	private ArrayList<Item> getItemsNow() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private ArrayList<String> getLocalOut() {
 		/**This method relies on this.localNow being up to date and
 		 * returns a list of the names of items that are inactive in
 		 * at least one store, and active in NO stores.
 		 */
 		ArrayList<String> names = new ArrayList<String>();
 		ArrayList<Item> items;
 		for (Store store : this.activeStores){
 			items = store.getAllInactiveItems();
 			for (Item i : items){
 				if((!names.contains(i.name))  && (! this.localNow.contains(i.name))){
 					names.add(i.name);
 				}
 			}
 		}
 		return names;
 	}
 
 	private ArrayList<String> getLocalNow() {
 		/**Iterate through each store in activeStores, append item name to
 		 * list if it is not already in the list, then return names.
 		 */
 		ArrayList<String> names = new ArrayList<String>();
 		ArrayList<Item> items;
 		for (Store store : this.activeStores){
 			items = store.getAllActiveItems();
 			for (Item i : items){
 				if(! names.contains(i.name)){
 					names.add(i.name);
 				}
 			}
 		}
 		return names;
 	}
 
 	private ArrayList<String> getActiveFilters() {
 		//By default, return all filters
 		return new ArrayList<String>(Arrays.asList(FILTERS));
 	}
 
 	private void getClosestStores() {
 		/**Gets a list of the closest Stores and populates
 		 * the store's active and inactive lists.
 		 */
 		
 		// get data from GPS if possible
 		this.activeStores = new ArrayList<Store>();
 		for (String store : STORES){
 			activeStores.add(new Store(store));
 		}
 		Store.buildMaps();
 	}
     
     
 
     public final void showUpdatedItems() {
 		LinearLayout gridLinearLayout = (LinearLayout) findViewById(R.id.grid_linearlayout);
 		// delete previous views under this
 		gridLinearLayout.removeAllViews();
 
 		// create gridviews dynamically...
 		for (int i = 0; i < FILTERS.length; i++) {
 			final ArrayList<Item> items = itemsByFilter.get(i);
 
 			// skip if size of items is zero.
 			if (items.size() == 0) continue;
 			
 
 			final LinearLayout s1 = new LinearLayout(this);
 			s1.setOrientation(LinearLayout.HORIZONTAL);
 			
 
 				/**
 				public void onClick(View v){
 					boolean expanding;
 					if (expanded.get(v)){
 						expanding = false;
 						expanded.put(v, false);
 					}
 					else {
 						expanded.put(v, true);
 						expanding=true;
 					}
 					
 					
 					View subLayout = subLayouts.get(v);
 					
 					//if (allViews.contains(subLayout)){
 						//masterView.removeView(subLayout);
 					//}
 					int i = 0;
 					int position=0;
 					ViewGroup currentViewGroup;
 					for (View currentView : allViews){
 						currentViewGroup = (ViewGroup) currentView;
 						int numChildren = currentViewGroup.getChildCount();
 						for (int j = 0; j < numChildren; j++){
 							if (currentViewGroup.getChildAt(j)==v){
 								position=i+1;
 								break;
 							}
 						}
 						i++;
 					}
 						
 					LinearLayout nextRow = (LinearLayout) allViews.get(position-1);
 					
 					Log.i("debugging", "gets past j/i loop and position was: "+ position);
 					if (expanding){
 						Button nextButton = (Button) v;
 						nextButton.setText("-");
 						
 						//REMOVE OLD BUTTON
 						nextRow.removeViewAt(0);
 						nextRow.addView(nextButton, 0);
 						//TODO edge case end of list 
 						if (position!=0){
 							allViews.remove(position-1);
 							allViews.add(position-1, nextRow);
 							allViews.add(position, subLayout);
 						}
 					}
 					else {
 						//CONTRACTING
 						Button nextButton = (Button) v;
 						nextButton.setText("+");
 						nextRow.removeViewAt(0);
 						nextRow.addView(nextButton, 0);
 						if (position!=0){
 							allViews.remove(position-1);
 							allViews.add(position-1, nextRow);
 							allViews.remove(position);
 							
 						}
 					}
 					resetMasterLayout();
 				
 				}
 			**/
 			
 			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
 				     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 
 			final CheckBox button = new CheckBox(this);
 			button.setChecked(true);
 			button.setButtonDrawable(getResources().getDrawable(R.drawable.expand));
 			button.setText(FILTERS[i]);
 			button.setTextColor(getResources().getColor(R.color.Brown));
 			button.setTextSize(40);
 	
 			
 			s1.addView(button, layoutParams);
 			gridLinearLayout.addView(s1);
 
 			final GridView gridview = new GridView(this);
 			gridview.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, 230 * ((int) Math.ceil(items.size()/3.0))));
 			gridview.setColumnWidth(220);
 			gridview.setGravity(Gravity.CENTER);
 			gridview.setNumColumns(GridView.AUTO_FIT);
 			gridview.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
 			
 			button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
 				
 				
 		 		public void onCheckedChanged(CompoundButton v,
 						boolean expanding) {
 					if (!expanding) {
 						gridview.setVisibility(GridView.GONE);
 					} else {
 						gridview.setVisibility(GridView.VISIBLE);
 						
 					}
 				}
 			});
 			
 			
 			
 
 	        myImageAdapter = new ImageAdapter(this);
 	        gridview.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 						long arg3) {
 
 					System.out.println("clicked item : " + arg2);
 					Intent intent = new Intent(CoverFlowTestingActivity.this, DetailActivity.class);
 					intent.putExtra("name", items.get(arg2).name);
 					startActivity(intent);
 				}
 
 	        });
 
 			gridLinearLayout.addView(gridview);
 	        gridview.setAdapter(myImageAdapter);
 
 			for(int j = 0; j < items.size(); j++) {
 				//System.out.println(currentItems.get(i).name);
 				myImageAdapter.add(getResources().getIdentifier(items.get(j).name.replace(' ', '_') + "_coverflow", "drawable", getPackageName()));
 				//System.out.println(getResources().getIdentifier(currentItems.get(i).name + "_coverflow", "drawable", getPackageName()));
 			}
 
 		}
 	}
 
 	private void createAllMonths() {
     	/**Creates all Month instances...should only be called after
     	*  all Item instances have been created. Month Zero is "empty"
     	*  so 1->Jan, 2->Feb, ... 12->Dec
     	
     	for (int i=0; i < 13; i++){
     		months[i] = new Month(i);
     	}		
     	**/
 	}
 
 	private void createAllItems() {
 		//this method is not modular, it should take a list of names
 		//TODO make this method less stupid
 		//@TODO We should not be adding EVERYTHING to currentItems, we should
 		//just be creating item instances and copy the local items in another method
 		
 		
 		//FIX BELOW
 
 		new Item("garlic");
 		
 		new Item("carrots");
 		new Item("onion");
 		new Item("parsnip");
 		
 		Log.i("debugging", "check in createallitems 1");
 	
 
 
 		new Item("broccoli");
 		
 		new Item("cauliflower");
 		
 		new Item("asian greens");
 		//worked up to asian greens
 		Log.i("debugging", "before lettuce");
 		new Item("lettuce");
 		Log.i("debugging", "before spinach");
 		new Item("spinach");
 		Log.i("debugging", "before eggplant");
 		new Item("eggplant");
 		Log.i("debugging", "before summer squash");
 		new Item("summer squash");
 		Log.i("debugging", "before cucumber");
 		//new Item("cucumber");
 		
 		Log.i("debugging", "bp");
 		//new Item("bell peppers");
 		Log.i("debugging", "asp");
 
 		//new Item("asparagus");
 		Log.i("debugging", "cel");
 		//new Item("celery");
 		
 		new Item("kiwifruit");
 		Log.i("debugging", "kiw");
 		
 		new Item("strawberries");
 		Log.i("debugging", "str");
 		
 		Log.i("debugging", "bl");
 		new Item("blueberries");
 		
 		Log.i("debugging", "ch");
 		new Item("cherries");
 		Log.i("debugging", "nec");
 		new Item("nectarines");
 		Log.i("debugging", "pea");
 		new Item("peaches");
 		Log.i("debugging", "pl");
 		new Item("plums");
 		Log.i("debugging", "pl");
 		new Item("pluots");
 		Log.i("debugging", "ap");
 		new Item("apricots");
 		
 		//problematic
 		//Log.i("debugging", "on");
 		
 	}
 
 	public void populateMap() {		
 		
 		
 		//temporary... dummy group
 		String group = "group";
 		
 		//@TODO include filter Strings as last argument to putEntry()
 		//putEntry("artichokes are pretty", "anywhere", "red", "artichoke", VEGGIES);
 		putEntry("Garlic is a bulbous plant with a pungent and slightly spicy taste and odor that mellows with cooking. It can be sauteed, roasted, or added raw to a dish to enhance its flavor.", "Garlic is typically stored at room temperature, but peeled cloves can be preserved in wine or vinegar", "firm with no soft spots", "garlic", VEGGIES);
 		putEntry("Onions are common bulbous vegetables used in many cuisines around the world. They have a strong odor and taste that mellows with cooking.", "Onions are typically stored at room temperature.", "firm with no brown spots", "onion", VEGGIES);
 		putEntry("Carrots are known for their high vitamin-A content, which can improve vision. They have a sweet and crisp taste and are used in dishes ranging from soups and stews to deserts.", "Carrots can last for several months when stored in the refrigerator", "firm and crisp texture", "carrots", VEGGIES);
 		putEntry("Parsnips are closely related to carrots, but typically sweeter, especially when cooked. They can be roasted, baked, boiled, pureed, fried or steamed.", "last up to 3 weeks in the refrigerator inside a perforated plastic bag", "firm and sturdy", "parsnip", VEGGIES);
 		putEntry("Broccoli has large edible florets sprouting from its stem, and is typically steamed or boiled, or occasionally stir-fried or eaten raw.", "Store inside a plastic bag in the refrigerator for up to 10 days", "compact floret clusters and not bruised, uniform color", "broccoli", VEGGIES);
 		putEntry("Cauliflower is closely related to brocolli, rich with antioxidants and provides numerous health benefits. It can be boiled, steamed, sauteed or eaten raw.", "Store in a plastic bag inside the refrigerator for up to a week", "compact, creamy white buds without separated clusters", "cauliflower", VEGGIES);
 		putEntry("None require long cooking; on the contrary, most Asian greens should be cooked quickly, sealing in their sweetness by stir-frying or steaming. Try swapping Asian greens for mustard, Swiss chard, or spinach when preparing a favorite recipe.", "Larger, more mature greens can remain in the refrigerator for up to five days, while smaller, tender greens should be used within three days of purchase.", "dry and firm", "asian greens", VEGGIES);
 		
 		putEntry("With many varieties such as Romaine, Red Leaf, King Crown, and Butter Lettuce, this leafy vegetable goes great in salads or as a topping for other savory dishes.", "Keep lettuce in the refrigerator for up to 10 days in a loose plastic bag.", "Ripe lettuce is typically firm at the base with varying shades of green.", "lettuce", VEGGIES);
 		putEntry("Spinach is highly nutritious, versatile, leafy vegetable that can be added to dishes raw or cooked.", "Place in an air-tight bag or container for up to 5 days in the refrigerator", "Leaves should be dark green with no signs of yellowing", "spinach", VEGGIES);
 		putEntry("The eggplant is a bulky vegetable with a spongy texture and a pleasant slightly bitter taste. It can be grilled, baked, roasted or steamed.", "Eggplants are sensitive to both heat and cold, and should be stored at around 50 degrees Fahrenheit.","Ripe eggplants should be firm and heavy for their size, with smooth, shiny skin and vivid color.", "eggplant", VEGGIES);
 
 		putEntry("Summer squash can be grilled, steamed, boiled, sauteed, fried or used in stir fry recipes. They mix well with onions, tomatoes and okra in vegetable medleys.", "Place, unwashed in plastic bags, in the crisper drawer of the refrigerator. The storage life of summer squash is brief, so use within two to three days.", "Summer squash is best when immature, young and tender.", "summer squash", VEGGIES);
 		//putEntry("c", "t", "green", "cucumber", VEGGIES);
 		
 		//putEntry("c", "t", "green", "bell peppers", VEGGIES);
 		//putEntry("c", "t", "green", "asparagus", VEGGIES);
 		//putEntry("c", "t", "green", "celery", VEGGIES);
 		
 		
 		putEntry("Kiwifruits are a unique fruit common in Califormia, with a sweet and sour taste. They can be peeled and sliced, or the flesh can be scraped out with a sppon.", "Kiwifruits can be stored for several days at room temperature, or up to 4 weeks in the refrigerator.", "Kiwifruits should give slightly to pressure when ripe, be plump and smooth, and have no wrinkles, punctures, or bruises.", "kiwifruit", FRUITS);
 		putEntry("The common strawberry is actually a hybrid of fruits, and not a member of the botanical berry family. Nevertheless, this sweet and succulent fruit is enjoyed by many on it's own, mixed into a fruit salad, or even used in ice creams or milkshakes.", "Strawberries spoil quickly so they should be consumed as soon as possible. They may last in the refrigerator for a few days, but freezing them will preserve them for up to a year.", "Ripe strawberries should be a beautiful bright red; spots of light green indicate under-ripeness and a dark red indicates over-ripeness.", "strawberries", FRUITS);
 		putEntry("Blueberries are high in antioxidants and very nutritious; ranging in flavor from tart and sour to sweet. While they can be baked into pies or other deserts, eating them raw provides the greatest nutritional benefits.", "t", "green", "blueberries", FRUITS);
 		
		putEntry("Cherries are small tree fruits with a central pit seed surrounded by edible fleshy fruit. Externally the fruits have bright red or purple color with very thin peel.", "Place your cherries in the refrigerator as soon as possible in a plastic bag. Do not wash prior to storage, as moisture can be absorbed where the stem meets the fruit and lead to splits or spoilage.", "Cherries are best with a red glow and no brown specks", "cherries", FRUITS);
		putEntry("Nectarines is virtually identical to the fruit we call peaches, except for one noticeable feature. The skin of most peaches contains fuzz, while the skin of nectarines is smooth. Nectarines can be tracked back to ancient China", "Store nectarines at room temperature until ripe � this usually takes 2 to 3 days.", "Nectarines are best when you slightly apply a little bit of pressure to show a little bit of squishiness. Any squishier, the fruit is most likely rotten", "nectarines", FRUITS);
		putEntry("Peaches are a round juicy fruit with yellowish-red skin and a rough pit in the middle. ", "Refrigerate in a plastic bag and use within two days", "A good indicator of maturity is a well-defined cleft in the shape of the peach. Avoid those with any hint of green as they will never fully ripen", "peaches", FRUITS);
 		putEntry("Plums are part of the Drupe family, which are fruits that  have a hard stone pit surrounding their seeds. When plums are dried, they are known as prunes.", "Store at room temperature until ripe, then refrigerate in a plastic bag", "Plums smell sweet and fruity when they are ripe. An underripe plum will have little to no scent at all and avoid mushy plums", "plums", FRUITS);
 		putEntry("The pluot is a hybrid between plums and apricots that come in vast array of colors", "Store ripe stone fruit in the refrigerator and use promptly", "Ripe pluots taste like plums but are less acidic, thanks to their apricot parent. They also have a noticeable fragrance when ripened", "pluots", FRUITS);
 		putEntry("Apricot is a yellow/orange fruit that has a hue of red when exposed to sunlight for an extended period of time", "Once ripe, refrigerate in a plastic bag", "Place Apricots on a flat surface with space between the fruit to ripen them at room temperature. It is best to turn them occasionally so that they will ripen evenly", "apricots", FRUITS);
 	}
 
 	public void putEntry(String general, String storage, String ripe, String name, String group) {
 		Object[] info = {general, storage, ripe ,group};
 		Item.infoMap.put(name, info);
 	}
 
 	public void setFilters(ArrayList<String> filters){
 		this.activeFilters= (ArrayList<String>) filters;
 	}
 
 	/**
 	public void setMonth(int monthNum){
 		this.currentMonth = this.months[monthNum];
 	}
 	**/
 
 
 
 	public void updateItemsByFilter(){
 		/** This method is called when a filter is toggled or when a store is selected / deselected.
 		 * It updates this.itemsByFilter 
 		 */
 		this.itemsByFilter = getItemsByFilter();
 		// UPDATE STORE INFO HERE
 		// must update this.acvtiveStores
 		showUpdatedItems();
 	}
 
     public class ImageAdapter extends BaseAdapter {
         
         private Context mContext;
         ArrayList<Integer> itemList = new ArrayList<Integer>();
         
         public ImageAdapter(Context c) {
          mContext = c; 
         }
         
         void add(int resourceId){
          itemList.add(resourceId); 
         }
 
      @Override
      public int getCount() {
       return itemList.size();
      }
 
      @Override
      public Object getItem(int arg0) {
       // TODO Auto-generated method stub
       return null;
      }
 
      @Override
      public long getItemId(int position) {
       // TODO Auto-generated method stub
       return 0;
      }
 
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
       ImageView imageView;
             if (convertView == null) {  // if it's not recycled, initialize some attributes
                 imageView = new ImageView(mContext);
                 imageView.setLayoutParams(new GridView.LayoutParams(220, 220));
                 imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                 imageView.setPadding(8, 8, 8, 8);
             } else {
                 imageView = (ImageView) convertView;
             }
             
             final String imageKey = String.valueOf(itemList.get(position));
             final Bitmap bitmap = getBitmapFromMemCache(imageKey);
             if (bitmap != null) {
                 imageView.setImageBitmap(bitmap);
             } else {
             	Bitmap bm = decodeSampledBitmapFromResource(itemList.get(position), 220, 220);
             	addBitmapToMemoryCache(String.valueOf(itemList.get(position)), bm);
             	imageView.setImageBitmap(bm);
             }
 
             return imageView;
      }
      
      public Bitmap decodeSampledBitmapFromResource(int resourceId, int reqWidth, int reqHeight) {
       
       Bitmap bm = null;
       // First decode with inJustDecodeBounds=true to check dimensions
       final BitmapFactory.Options options = new BitmapFactory.Options();
       options.inJustDecodeBounds = true;
       BitmapFactory.decodeResource(getApplicationContext().getResources(), resourceId, options);
           
       // Calculate inSampleSize
       options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
           
       // Decode bitmap with inSampleSize set
       options.inJustDecodeBounds = false;
       bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), resourceId, options); 
           
       return bm;   
      }
      
      public int calculateInSampleSize(
        
       BitmapFactory.Options options, int reqWidth, int reqHeight) {
       // Raw height and width of image
       final int height = options.outHeight;
       final int width = options.outWidth;
       int inSampleSize = 1;
       
       if (height > reqHeight || width > reqWidth) {
        if (width > height) {
         inSampleSize = Math.round((float)height / (float)reqHeight);    
        } else {
         inSampleSize = Math.round((float)width / (float)reqWidth);    
        }   
       }
       
       return inSampleSize;    
      }
 
     // methods for search
     }
     
     private void showResults(String newText) {
     	System.out.println("showResults is called!");
 /*
         // add some dummy stuff for localNow to test
         ArrayList<String> localNow = new ArrayList<String>();
     	localNow.add("apple");
     	localNow.add("cabbage");
     	localNow.add("bananas"); 
     	localNow.add("cantaloupe");
     	localNow.add("melon");
 */    	
     	ArrayList<String> result = new ArrayList<String>();
     	
     	if (!newText.equals("")) {
 	    	for(int i = 0; i < localNow.size(); i++) {
 	    		String tmp = localNow.get(i).toLowerCase();
 	    		if (tmp.contains(newText.toLowerCase())) {
 	    			result.add(tmp);
 	    		}
 	    	}
     	}
     	
     	ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result);
     	mListView.setAdapter(myAdapter);
 
 
         mListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				

				
				
 				System.out.println("clicked item : " + arg2);
 				Intent intent = new Intent(CoverFlowTestingActivity.this, DetailActivity.class);
 				intent.putExtra("name", ((TextView) arg1).getText());
 				startActivity(intent);
 			}
         	
         });
     	}
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 
     public boolean onOptionsItemSelected(MenuItem item) {
 		
    	
    	
    	
    	
       switch (item.getItemId()) { //basic structure borrowed from Kate's drawing app in section
       case R.id.notification:
             Toast.makeText(this, "Button pressed: " + getResources().getString(R.string.notification) + " .",
                         Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(CoverFlowTestingActivity.this, NotificationActivity.class);
             startActivity(intent);
             return true;
      default:
             return super.onOptionsItemSelected(item);
       }
     }
     /*
     public void showPopup(View v) {
         LayoutInflater inflater = (LayoutInflater) this
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             PopupWindow pw = new PopupWindow(inflater.inflate(
                     R.layout.search, null, false), 300, 400, true);
             pw.showAtLocation(findViewById(R.id.searchView), Gravity.CENTER, 0,
                     0);
     }*/
     public void createNotificationService() {
 		Calendar Calendar_Object = Calendar.getInstance();
 		
 		/*
 		Calendar_Object.set(Calendar.MONTH, 4);
 		Calendar_Object.set(Calendar.YEAR, 2013);
 		Calendar_Object.set(Calendar.DAY_OF_MONTH, 2);
 
 		Calendar_Object.set(Calendar.HOUR_OF_DAY, 0);
 		Calendar_Object.set(Calendar.MINUTE, 22);
 		Calendar_Object.set(Calendar.SECOND, 0);
 		*/
 		// notify after 15 sec...
 		Calendar_Object.add(Calendar.SECOND, 15);
 		
 		// MyView is my current Activity, and AlarmReceiver is the
 		// BoradCastReceiver
 		Intent myIntent = new Intent(CoverFlowTestingActivity.this, AlarmReceiver.class);
 
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(CoverFlowTestingActivity.this,
 				0, myIntent, 0);
 
 		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
 		/*
 		 * The following sets the Alarm in the specific time by getting the long
 		 * value of the alarm date time which is in calendar object by calling
 		 * the getTimeInMillis(). Since Alarm supports only long value , we're
 		 * using this method.
 		 */
 		//alarmManager.setRepeating(AlarmManager.RTC, 0, 20000, pendingIntent);
 		alarmManager.set(AlarmManager.RTC, Calendar_Object.getTimeInMillis(),
 				pendingIntent);
     }
     
 	 public void initializeButtons(){
 			
 		 button1.setText("+");
 		 //row1 = (LinearLayout) findViewById(R.id.bowlRow);
 		 LinearLayout s1 = new LinearLayout(this);
 		 s1.setOrientation(LinearLayout.VERTICAL);
 		 CheckBox checkBox1 = new CheckBox(this);
 		 checkBox1.setText("since i'm coming home");
 		 
 		 CheckBox checkBox2 = new CheckBox(this);
 		 checkBox2.setText("well my body's been a mess");
 		//TODO add checkboxes		
 		 s1.addView(checkBox1);
 		 s1.addView(checkBox2);
 		 subLayouts.put(button1, s1);
 		 allViews.add(row1);
 		 //allViews.add(s1);
 		 button1.setOnClickListener(buttonListener);
 		 expanded.put(button1, false);
 	 }
 	 
 		View.OnClickListener buttonListener = new View.OnClickListener(){
 			
 			public void onClick(View v){
 				boolean expanding;
 				if (expanded.get(v)){
 					expanding = false;
 					expanded.put(v, false);
 				}
 				else {
 					expanded.put(v, true);
 					expanding=true;
 				}
 				
 				
 				View subLayout = subLayouts.get(v);
 				
 				//if (allViews.contains(subLayout)){
 					//masterView.removeView(subLayout);
 				//}
 				int i = 0;
 				int position=0;
 				ViewGroup currentViewGroup;
 				for (View currentView : allViews){
 					currentViewGroup = (ViewGroup) currentView;
 					int numChildren = currentViewGroup.getChildCount();
 					for (int j = 0; j < numChildren; j++){
 						if (currentViewGroup.getChildAt(j)==v){
 							position=i+1;
 							break;
 						}
 					}
 					i++;
 				}
 					
 				LinearLayout nextRow = (LinearLayout) allViews.get(position-1);
 				
 				Log.i("debugging", "gets past j/i loop and position was: "+ position);
 				if (expanding){
 					
 					Button nextButton = (Button) v;
 					nextButton.setBackground(getResources().getDrawable(R.drawable.navigation_collapse));
 				
 					
 					//REMOVE OLD BUTTON
 					nextRow.removeViewAt(0);
 					nextRow.addView(nextButton, 0);
 					//TODO edge case end of list 
 					if (position!=0){
 						allViews.remove(position-1);
 						allViews.add(position-1, nextRow);
 						allViews.add(position, subLayout);
 					}
 				}
 				else {
 					//CONTRACTING
 					Button nextButton = (Button) v;
 					nextButton.setBackground(getResources().getDrawable(R.drawable.navigation_expand));
 					nextRow.removeViewAt(0);
 					nextRow.addView(nextButton, 0);
 					if (position!=0){
 						allViews.remove(position-1);
 						allViews.add(position-1, nextRow);
 						allViews.remove(position);
 						
 					}
 				}
 				resetMasterLayout();
 			
 			}
 		};
 		
 		public void resetMasterLayout(){
 			masterView.removeAllViews();
 			for (View v: allViews){
 				Log.i("debuggingNew", "adding a view");
 				masterView.addView(v);
 			}
 		}
 }
