 package com.example.coverflow.producealmanac;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.coverflow.R;
 import com.example.coverflow.ResourceImageAdapter;
 import com.example.coverflow.producealmanac.MultiSpinner.MultiSpinnerListener;
 
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
 	
 	
 	TextView textView;
 	ResourceImageAdapter myAdapter;
 	
 	//search/filter terms
 
 	
 	
 	//Static filter strings
 	public final static String BULBS = "bulb vegetables";
 	public final static String ROOTS  = "root vegetables";
 	public final static String FLOWER = "flower bud vegetables";
 	public final static String LEAF = "leaf vegetables";
 	public final static String FVEG = "fruit vegetables";
 	public final static String STALK = "stalk vegetables";
 	public final static String BERRIES = "berries";
 	public final static String DRUPES = "drupes";
 	
 	public final static String[] FILTERS = {BULBS, ROOTS, FLOWER, LEAF, FVEG, STALK, BERRIES, DRUPES};
 	
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
 		
 		this.itemsByFilter = getItemsByFilter();
 		Log.i("debugging", "after getitemsbyfilter");
 		//done initializing backend data
 
 
 		showUpdatedItems();
 
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
 				if (item.group==FILTERS[filterIndex]){
 					items.add(item);
 				}
 			}
 			
 		}
 		Collections.sort(items);
 		return items;
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
 		activeStores.add(new Store("Berkeley Bowl"));
 		activeStores.add(new Store("Yasai Market"));
 		activeStores.add(new Store("Safeway - College Ave."));
 		activeStores.add(new Store("Trader Joes - University Ave."));
 		activeStores.add(new Store("Whole Foods - Telegraph Ave."));	
 		Store.buildMaps();
 	}
     
     
 
     public void showUpdatedItems() {
 		LinearLayout gridLinearLayout = (LinearLayout) findViewById(R.id.grid_linearlayout);
 		// delete previous views under this
 		gridLinearLayout.removeAllViews();    	
 
 
 		// create gridviews dynamically...
 		for (int i = 0; i < FILTERS.length; i++) {
 			final ArrayList<Item> items = itemsByFilter.get(i);
 
 			// skip if size of items is zero.
 			if (items.size() == 0) continue;
 
 			TextView txtView = new TextView(this);
 			//should be different per each one
 			txtView.setText(FILTERS[i]);
 			txtView.setTextColor(getResources().getColor(R.color.Brown));
 			txtView.setTextSize(40);
 			gridLinearLayout.addView(txtView);
 
 			GridView gridview = new GridView(this);
 			gridview.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, 230 * ((int) Math.ceil(items.size()/3.0))));
 			gridview.setColumnWidth(220);
 			gridview.setGravity(Gravity.CENTER);
 			gridview.setNumColumns(GridView.AUTO_FIT);
 			gridview.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
 
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
 		new Item("cucumber");
		/**
 		Log.i("debugging", "bp");
 		new Item("bell peppers");
 		Log.i("debugging", "asp");
 		new Item("asparagus");
 		Log.i("debugging", "cel");
 		new Item("celery");
 		Log.i("debugging", "kiw");
 		new Item("kiwifruit");
 		Log.i("debugging", "str");
 		new Item("strawberries");
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
 		Log.i("debugging", "on");
 
 		*/
 	}
 
 	public void populateMap() {		
 		
 		
 		//temporary... dummy group
 		String group = "group";
 		
 		//@TODO include filter Strings as last argument to putEntry()
 		putEntry(R.drawable.celeriac_detail, "artichokes are pretty", "anywhere", "red", "garlic", BULBS);
 		putEntry(R.drawable.celeriac_detail, "cabbages are evil", "trashcan", "green", "onion", BULBS);
 		putEntry(R.drawable.celeriac_detail, "cabbages are evil", "trashcan", "green", "carrots", ROOTS);
 		putEntry(R.drawable.celeriac_detail, "cabbages are evil", "trashcan", "green", "parsnip", ROOTS);
 		putEntry(R.drawable.celeriac_detail, "cabbages are evil", "trashcan", "green", "broccoli", FLOWER);
 		putEntry(R.drawable.celeriac_detail, "cabbages are evil", "trashcan", "green", "cauliflower", FLOWER);
 		putEntry(R.drawable.asian_greens_detail, "None require long cooking; on the contrary, most Asian greens should be cooked quickly, sealing in their sweetness by stir-frying or steaming. Try swapping Asian greens for mustard, Swiss chard, or spinach when preparing a favorite recipe.", "Larger, more mature greens can remain in the refrigerator for up to five days, while smaller, tender greens should be used within three days of purchase.", "dry and firm", "asian greens", LEAF);
 		
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "lettuce", LEAF);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "spinach", LEAF);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "eggplant", FVEG);
 
 		putEntry(R.drawable.summer_squash_detail, "Summer squash can be grilled, steamed, boiled, sauteed, fried or used in stir fry recipes. They mix well with onions, tomatoes and okra in vegetable medleys.", "Place, unwashed in plastic bags, in the crisper drawer of the refrigerator. The storage life of summer squash is brief, so use within two to three days.", " Summer squash is best when immature, young and tender.", "summer squash", FVEG);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "cucumber", FVEG);
		/*
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "bell peppers", FVEG);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "asparagus", STALK);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "celery", STALK);
		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "kiwifruit", BERRIES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "strawberries", BERRIES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "blueberries", BERRIES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "cherries", DRUPES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "nectarines", DRUPES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "peaches", DRUPES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "plums", DRUPES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "pluots", DRUPES);
 		putEntry(R.drawable.celeriac_detail, "c", "t", "green", "apricots", DRUPES);	
 		*/}
 
 	public void putEntry(int resID, String general, String storage, String ripe, String name, String group) {
 		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resID);
 		ImageView imgView = new ImageView(this);
 		imgView.setImageBitmap(bitmap);
 		Object[] info = {general, storage, ripe, bitmap,group};
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
 
             Bitmap bm = decodeSampledBitmapFromResource(itemList.get(position), 220, 220);
 
             imageView.setImageBitmap(bm);
             return imageView;
      }
      
      public Bitmap decodeSampledBitmapFromResource(int resourceId, int reqWidth, int reqHeight) {
       
       Bitmap bm = null;
       // First decode with inJustDecodeBounds=true to check dimensions
       final BitmapFactory.Options options = new BitmapFactory.Options();
       options.inJustDecodeBounds = true;
       BitmapFactory.decodeResource(getApplicationContext().getResources(), resourceId);
           
       // Calculate inSampleSize
       options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
           
       // Decode bitmap with inSampleSize set
       options.inJustDecodeBounds = false;
       bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), resourceId); 
           
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
     /*
     public void showPopup(View v) {
         LayoutInflater inflater = (LayoutInflater) this
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             PopupWindow pw = new PopupWindow(inflater.inflate(
                     R.layout.search, null, false), 300, 400, true);
             pw.showAtLocation(findViewById(R.id.searchView), Gravity.CENTER, 0,
                     0);
     }*/
 }
