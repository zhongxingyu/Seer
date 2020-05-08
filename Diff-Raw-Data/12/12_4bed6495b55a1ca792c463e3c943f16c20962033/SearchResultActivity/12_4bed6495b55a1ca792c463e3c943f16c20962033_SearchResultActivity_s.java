 package mmt.uit.placehelper.activities;
 
 
 
 import java.util.Collections;
 import java.util.List;
 
 import mmt.uit.placehelper.models.Place;
 import mmt.uit.placehelper.models.PlaceLocation;
 import mmt.uit.placehelper.models.PlacesList;
 import mmt.uit.placehelper.services.FavDataService;
 import mmt.uit.placehelper.services.SearchPlace;
 import mmt.uit.placehelper.utilities.ConstantsAndKey;
 import mmt.uit.placehelper.utilities.SortPlace;
 
 import mmt.uit.placehelper.R;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchResultActivity extends ListActivity {
 	
 
 	private ImageButton btnSearch;
 	private ImageButton btnShowAllMap;
 	private TextView txtCurAdd, txtrs;
 	private String keyWord;
     private RslistAdapter rsAdapter;       
     private PlaceLocation curLoc = new PlaceLocation();
     private PlacesList lsPlace = null;
     private Context mContext;
     private ProgressBar search_progress;
     private int img;
     private String lang = "en";
 	private int radius=5000;
 	private SharedPreferences mSharePref;
 	private String types =null;
 	private static final int ONLY_NAME=1,ONLY_TYPES=2,BOTH_NAME_TYPES=3;
 	private int search_op=1;
       
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         mContext = getApplicationContext();
         setContentView(R.layout.ph_list_results);                
         txtrs = (TextView)findViewById(R.id.txt_result);
         search_progress = (ProgressBar)findViewById(R.id.rs_progress);
         
         //Get value from ListCategoriesActivity
         Bundle b = getIntent().getExtras();
         keyWord = b.getString("search");               
         curLoc.setLat(b.getDouble("curlat"));
         curLoc.setLng(b.getDouble("curlng"));        
         img = b.getInt("img"); 
         types = b.getString("types");
     	mSharePref = PreferenceManager.getDefaultSharedPreferences(mContext);
     	if(mSharePref!=null){
     	lang = mSharePref.getString(getResources().getString(R.string.prefkey_lang), getResources().getStringArray(R.array.arr_lang_value)[0]);
     	radius = 1000*Integer.parseInt(mSharePref.getString(getResources().getString(R.string.prefkey_radius), "5"));
     	search_op = Integer.parseInt(mSharePref.getString(getResources().getString(R.string.prefkey_search), "1"));
     	}
         //Task to get place result from google place service
     	switch (search_op){
     	case ONLY_NAME:
     		FindPlace fp = new FindPlace(keyWord,lang,radius,null);            
     		fp.execute(curLoc);  
     		break;
     	case ONLY_TYPES:
     		fp = new FindPlace(null,lang,radius,types);            
     		fp.execute(curLoc);  
     		break;
     	case BOTH_NAME_TYPES:
     		types += "|establishment";
     		fp = new FindPlace(keyWord,lang,radius,types);            
     		fp.execute(curLoc);  
     		break;
     	}
            
         
     }
 	
 	
 	
 	//Create Task to make request and get places list
 	private class FindPlace extends AsyncTask<PlaceLocation,Void, PlacesList>{
 		private String keyword;//keyword to search 
 		private String lang;
 		private int radius;
 		private String types;
 		
 		public FindPlace(String keyword, String lang, int radius,String types){
 			
 			this.keyword = keyword;
 			this.lang = lang;
 			this.radius = radius;
 			this.types = types;
 		}
 		@Override
 		protected PlacesList doInBackground(PlaceLocation... params) {		
 					
 			PlacesList pl=null; //initialize list result
 			pl = SearchPlace.getPlaceList(params[0].getLat(), params[0].getLng(),keyword,lang,radius,types);
 			return pl; //Return list Place				
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			super.onPreExecute();
 			search_progress.setVisibility(View.VISIBLE);
 			txtrs.setText(getResources().getText(R.string.seact_load));
 			
 		}
 		//Display result to the screen after finish getting place list
 		@Override
 		protected void onPostExecute(PlacesList result) {
 			if (result==null){
 				Toast.makeText(mContext, getResources().getText(R.string.seact_connect_error), Toast.LENGTH_SHORT).show();
 				txtrs.setText(getResources().getText(R.string.seact_connect_error));
 				return;
 			}
 			if (result!=null && result.getStatus().contentEquals(ConstantsAndKey.STATUS_OK)){			
 				FavDataService dataSrv = new FavDataService(mContext);
 				dataSrv.open();
 				txtrs.setText(getResources().getText(R.string.seact_result)+ keyWord);
 				lsPlace = result;				
 				Collections.sort(lsPlace.getResults(), new SortPlace());
 				for (int i=0;i<lsPlace.getResults().size();i++){
 					if (dataSrv.isExisted(lsPlace.getResults().get(i).getId())){
 						lsPlace.getResults().get(i).setFavorite(true);
 					}
 					else {
 						lsPlace.getResults().get(i).setFavorite(false);
 					}
 				}
 				rsAdapter = new RslistAdapter(mContext, R.layout.ph_result_row,  lsPlace.getResults(), img);
 				setListAdapter(rsAdapter);
 				dataSrv.close();
 			}
 			else
 				if (result.getStatus().contentEquals(ConstantsAndKey.NO_RESULT))
 					//Toast.makeText(getApplicationContext(), getResources().getText(R.string.seact_nors), Toast.LENGTH_SHORT).show();
 				{									
 					txtrs.setText(getResources().getText(R.string.seact_nors) + keyWord);
 				}
 				else 
 					Toast.makeText(mContext, getResources().getText(R.string.seact_rq_error), Toast.LENGTH_SHORT).show();
 			
 			search_progress.setVisibility(View.GONE);
 			
 		}
 	}
 	
 	
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		Intent mIntent = new Intent(this, DetailPlaceActivity.class);
 		Place pl = lsPlace.getResults().get(position);
 		Bundle mBundle = new Bundle();
 		mBundle.putParcelable("place", pl);
 		mBundle.putParcelable(ConstantsAndKey.KEY_CURLOC, curLoc);
 		mIntent.putExtras(mBundle);
 		startActivity(mIntent);
 		
 	}
 	
 	 @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		MenuInflater mInflater = getMenuInflater();
 		mInflater.inflate(R.menu.mn_search_result, menu);
 		return true;
 	}
 	 
 	 @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		 switch (item.getItemId()){
 		 case R.id.mn_all_map:
 			 	Intent mIntent = new Intent(this, ViewOnMapActivity.class);				
 				Bundle mBundle = new Bundle();
 				mBundle.putBoolean(ConstantsAndKey.KEY_SHOW_ALL, true);
 				mBundle.putParcelable(ConstantsAndKey.KEY_LST_PLACES, lsPlace);
 				mBundle.putParcelable(ConstantsAndKey.KEY_CURLOC, curLoc);
 				mIntent.putExtras(mBundle);
 				startActivity(mIntent);
 		 default:
 				return super.onOptionsItemSelected(item);
 		 }
 	 }
 	
 	public class RslistAdapter extends ArrayAdapter<Place> {
 
 		
 		int resourceId;
 		List<Place> array;
 		int imgType;
 		
 		
 		public RslistAdapter(Context context, int textViewResourceId,
 				List<Place> objects, int img) {
 			super(context, textViewResourceId, objects);
 			// TODO Auto-generated constructor stub
 			
 			this.resourceId = textViewResourceId;
 			this.array = objects;
 			this.imgType = img;
 		}
 		
 			
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {		
 			View view = convertView;
 			
 			if(view == null) {
 				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
 				view = li.inflate(resourceId, null);
 			}
 			
 			Place place = array.get(position);
 			if(place != null) {
 				
 				TextView name = (TextView) view.findViewById(R.id.rs_name);					
 				name.setText(place.getName());
 				
 				TextView address = (TextView)view.findViewById(R.id.rs_address);
 				address.setText(place.getVicinity());
 				
 				TextView distance = (TextView)view.findViewById(R.id.rs_distance);
 				distance.setText(String.valueOf(place.getDistance()));
 				ImageView img = (ImageView)view.findViewById(R.id.rs_infavorite);
 				if(place.isFavorite()){				
 				img.setImageResource(R.drawable.ic_rsfavorite);
 				}
 				else img.setImageResource(R.drawable.ic_no_rsfavorite);
 				RatingBar rate = (RatingBar)view.findViewById(R.id.rs_rate_bar);
 				rate.setRating(place.getRating());
 				ImageView type = (ImageView)view.findViewById(R.id.rs_img);
 				type.setImageResource(imgType);
 			}
 			
 			return view;
 		}
 	}
 	
 	
 }
 
