 package com.example.coverflow.producealmanac;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Gravity;
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
 import android.widget.SearchView;
 import android.widget.TextView;
 
 import com.example.coverflow.CoverFlow;
 import com.example.coverflow.R;
 import com.example.coverflow.ReflectingImageAdapter;
 import com.example.coverflow.ResourceImageAdapter;
 import com.example.coverflow.producealmanac.MultiSpinner.MultiSpinnerListener;
 
 /**
  * The Class CoverFlowTestingActivity.
  */
 public class CoverFlowTestingActivity extends Activity {
 
 	//currentItems will be the items currently in season, use a hashmap with key=time of year
 	ArrayList<Item> currentItems;
 	boolean populated=false;
 	TextView textView;
 	ResourceImageAdapter myAdapter;
 	
 	//search/filter terms
 	public String searchTerms = "";
 	public ArrayList<String> activeFilters;
 	public Month currentMonth;
 	public Month[] months;
 	//public Store activeStore = null;
 	
 	
 	//Static filter strings
 	public final static String BERRIES = "Berries";
 	public final static String ROOTS  = "Root Vegetables";
 	public final static String LEAFY = "Leafy Greens";
 	public final static String CITRUS = "Citrus Fruits";
 	public final static String HERBS = "Fresh Herbs";
 	
 	public final static String[] FILTERS = {BERRIES,ROOTS,LEAFY,CITRUS,HERBS};
 	
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
     	
     	// set search functionality
     	
         // Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchView = (SearchView) findViewById(R.id.searchView);
         // Assumes current activity is the searchable activity
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
         
     	System.out.println("before i set the linstener");
         //final SearchView.OnQueryTextListener queryTextListener = ; 
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { 
             @Override 
             public boolean onQueryTextChange(String newText) { 
                 // Do something 
             	System.out.println("asdasdads");
             	showResults(newText);
                return true; 
             } 
 
             @Override 
             public boolean onQueryTextSubmit(String query) { 
                 // Do something 
             	System.out.println("submitttttt");
                return true; 
             } 
         });
         //searchView.setOnCloseListener(this);
  
         mListView = (ListView) findViewById(R.id.list);
 
 		if(!populated){
 			populated=true;
 			populateMap();
 		}
 		
 		
 		currentItems = new ArrayList<Item>();
 		
 		//@TODO GET CURRENT MONTH
 		int monthNum = 4;
 		
 		this.activeFilters = new ArrayList<String>();
 		
 		currentItems = new ArrayList<Item>();
 		months = new Month[13];
 		
 		createAllItems();
 		createAllMonths();
 		
 		this.currentMonth = months[monthNum];
 		//getAllItems() returns copy of ArrayList, OK to modify
 		currentItems = currentMonth.getAllItems();
 
 		//set all filters as active by default
 		this.activeFilters = new ArrayList<String>(Arrays.asList(FILTERS));
 		
 		//done initializing backend data
		
		setContentView(R.layout.main);
 
 		showUpdatedItems();
 
         
         final MultiSpinner multispinner = (MultiSpinner) findViewById(this.getResources().getIdentifier("SpinnerCollegues", "id", "com.example.coverflow"));
       
         List<String> spinnerItems = new ArrayList<String>();
         spinnerItems.add(BERRIES);
         spinnerItems.add(ROOTS);
         spinnerItems.add(LEAFY);
         spinnerItems.add(CITRUS);
         spinnerItems.add(HERBS);
 
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
         		updateList();
         		System.out.println();
         	}
         });
         multispinner.setPrompt("FILTER");
     }
 
     public void showUpdatedItems() {
 		LinearLayout gridLinearLayout = (LinearLayout) findViewById(R.id.grid_linearlayout);
 		// delete previous views under this
 		gridLinearLayout.removeAllViews();
     	
 		ArrayList<ArrayList<Item>> itemsByFilter = new ArrayList<ArrayList<Item>>();
 		itemsByFilter.add(currentItems);
 		
 		ArrayList<Item> tmpItems = new ArrayList<Item>();
 		tmpItems.add(new Item("kale"));
 		tmpItems.add(new Item("artichoke"));
 		itemsByFilter.add(tmpItems);
 		itemsByFilter.add(tmpItems);
 		itemsByFilter.add(tmpItems);
 		itemsByFilter.add(tmpItems);
 		
 		
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
 				myImageAdapter.add(getResources().getIdentifier(items.get(j).name + "_coverflow", "drawable", getPackageName()));
 				//System.out.println(getResources().getIdentifier(currentItems.get(i).name + "_coverflow", "drawable", getPackageName()));
 			}
 
 		}
 	}
 
 	private void createAllMonths() {
     	/**Creates all Month instances...should only be called after
     	*  all Item instances have been created. Month Zero is "empty"
     	*  so 1->Jan, 2->Feb, ... 12->Dec
     	**/
     	for (int i=0; i < 13; i++){
     		months[i] = new Month(i);
     	}		
 	}
 
 	private void createAllItems() {
 		//@TODO We should not be adding EVERYTHING to currentItems, we should
 		//just be creating item instances and copy the currentMonth's ArrayList
 		
 		
 		//FIX BELOW
 		currentItems.add(new Item("artichoke"));
 		currentItems.add(new Item("cabbage"));
 		currentItems.add(new Item("celeriac"));
 		currentItems.add(new Item("kale"));
 		currentItems.add(new Item("leek"));
 		currentItems.add(new Item("peas"));
 		currentItems.add(new Item("turnip"));
 		
 	}
 
 	public void populateMap() {	
 		
 		
 		this.activeFilters = new ArrayList<String>();
 		//@TODO add all filter Strings to activeFilters by default
 		
 		//temporary... dummy group
 		String group = "group";
 		
 		//@TODO include filter Strings as last argument to putEntry()
 		putEntry(R.drawable.celeriac_detail, "artichokes are pretty", "anywhere", "red", "artichoke", LEAFY);
 		putEntry(R.drawable.celeriac_detail, "cabbages are evil", "trashcan", "green", "cabbage", LEAFY);
 		putEntry(R.drawable.celeriac_detail, "When peeled, celeriac's creamy white flesh resembles that of a turnip and tastes like a subtle blend of celery and parsley. This time of year, celeriac can be a perfect non-starch substitute for potatoes in a warming meal, and can be prepared in a similar way. It goes well with fresh green vegetables or salad and anything roasted or grilled.", "Celeriac can be stored for up to four months in the fridge.", "A ripe celeriac is firm with its peel intact.", "celeriac", ROOTS);
 		putEntry(R.drawable.celeriac_detail, "kale is kind", "who knows", "green", "kale", LEAFY);
 		putEntry(R.drawable.celeriac_detail, "leeks are...", "asasasd", "violet", "leek", ROOTS);
 		putEntry(R.drawable.celeriac_detail, "peas are small", "asdasd", "invisible", "peas", BERRIES);
 		putEntry(R.drawable.celeriac_detail, "turnips.. what are these?", "asdfasdf", "no idea", "turnip", CITRUS);
 	}
 	
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
 	
 	public void setMonth(int monthNum){
 		this.currentMonth = this.months[monthNum];
 	}
 	
 	public void setSearchString(String characters){
 		this.searchTerms = characters;
 	}
 	
 	public void updateList(){
 		this.currentItems = this.currentMonth.getAllItems();
 		@SuppressWarnings("unchecked")
 		ArrayList<Item> copy =(ArrayList<Item>) this.currentItems.clone();
 		for (Item i : copy){
 			//if statement checks the filters, else checks search terms
 			if (! this.activeFilters.contains(i.group)){
 				if( ! this.currentItems.remove(i)){
 					System.out.println("Trying to remove item not in list... ERROR, check item: " + i.toString());
 				}
 			}
 			else{
 				if (! i.name.contains(this.searchTerms)){
 					if( ! this.currentItems.remove(i)){
 						System.out.println("Trying to remove item not in list... ERROR, check item: " + i.toString());
 					}
 				}					
 			}
 		}
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
 
  
     // add some dummy stuff for localNow to test
     ArrayList<String> localNow = new ArrayList<String>();
     
     private void showResults(String newText) {
     	System.out.println("showResults is called!");
     	localNow.add("apple");
     	localNow.add("cabbage");
     	localNow.add("bananas"); 
     	localNow.add("cantaloupe");
     	localNow.add("melon");
     	
     	ArrayList<String> result = new ArrayList<String>();
     	
     	for(int i = 0; i < localNow.size(); i++) {
     		String tmp = localNow.get(i).toLowerCase();
     		if (tmp.contains(newText.toLowerCase())) {
     			result.add(tmp);
     		}
     	}
     	
     	// empty result.. just return
     	if (result.size() == 0) return;
     	
    	ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.main, result);
     	mListView.setAdapter(myAdapter);
     }
 }
