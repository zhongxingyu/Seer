 package mmt.uit.placehelper.activities;
 
 import java.net.UnknownHostException;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import mmt.uit.placehelper.models.Direction;
 import mmt.uit.placehelper.models.Place;
 import mmt.uit.placehelper.models.PlaceDetail;
 import mmt.uit.placehelper.models.PlaceDetailRs;
 import mmt.uit.placehelper.models.PlaceLocation;
 import mmt.uit.placehelper.services.FavDataService;
 import mmt.uit.placehelper.services.SearchPlace;
 import mmt.uit.placehelper.utilities.ConstantsAndKey;
 import mmt.uit.placehelper.utilities.CustomOverlay;
 import mmt.uit.placehelper.utilities.PointAddressUtil;
 import mmt.uit.placehelper.utilities.RouteOverlay;
 import mmt.uit.placehelper.R;
 import mmt.uit.placehelper.R.id;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RatingBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DetailPlaceActivity extends MapActivity  {
 	
 	//Button Bar
 	private ImageButton btnFacebook, btnWeb, btnDetail, btnMode, btnEmail; 
 	//Textview
 	private TextView txtPlaceName, txtAddress, txtPhone,txtWebsite,txtDistance,txtTime;
 	//Image View
 	private ImageView add_fav;
 	private RatingBar ratBar;	
 	private PlaceDetail plDetail;	
 	private Place place;
 	private Bundle mBundle;
 	private String lang = "en";
 	private int img;
 	private SharedPreferences mSharePref;
 	private PlaceLocation curLoc;
 	private Context mContext;
 	private MapView routeMap;
 	private List<Overlay> mapOverlays;
 	private MapController mapControl;	
 	private CustomOverlay itemizedOverlay;
 	private static final int LIST_MODE=1;
 	private static final int DETAIL_ROUTE=2;
 	private ProgressBar mProgressBar;
 	private String dirInstruct ="";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ph_detail_place);
 		//Get instance from view
 		btnFacebook = (ImageButton)findViewById(id.btnFacebook);
 		btnEmail = (ImageButton) findViewById(R.id.btnEmail);
 		btnDetail = (ImageButton) findViewById(R.id.btnDetail);
 		btnMode = (ImageButton) findViewById(R.id.btnDirMode);
 		btnWeb = (ImageButton) findViewById(R.id.btnWeb);
 		txtPlaceName = (TextView)findViewById(R.id.de_place_name);
 		txtAddress = (TextView)findViewById(R.id.de_address);
 		txtPhone = (TextView)findViewById(R.id.de_phone);
 		txtWebsite = (TextView)findViewById(R.id.de_website);
 		txtDistance = (TextView)findViewById(R.id.dt_distance);
 		txtTime = (TextView)findViewById(R.id.dt_time);
 		//txtDirection = (TextView)findViewById(R.id.dt_guide);
 		ratBar = (RatingBar)findViewById(R.id.de_rate_bar);
 		add_fav = (ImageView)findViewById(R.id.de_add_fav);
 		btnWeb.setOnClickListener(mClickListener);
 		btnMode.setOnClickListener(mClickListener);
 		btnDetail.setOnClickListener(mClickListener);
 		btnEmail.setOnClickListener(mClickListener);
 		mProgressBar = (ProgressBar)findViewById(R.id.de_progress);
 		//spDirMode = (Spinner)findViewById(R.id.de_dir_mode);
 		routeMap = (MapView)findViewById(R.id.de_routeMap);
 		mContext = this;
 		//Map settings
     	routeMap.setBuiltInZoomControls(true);
     	mapOverlays = routeMap.getOverlays();
     	mapControl = routeMap.getController();
     	mapControl.setZoom(15);    	
 		mBundle = getIntent().getExtras();
 		if(mBundle.getBoolean(ConstantsAndKey.KEY_FROM_FAV)){
 			plDetail = mBundle.getParcelable(ConstantsAndKey.KEY_PL_DETAIL);
 			setDetail(plDetail);
 		}
 		else
 		{
 		place = mBundle.getParcelable("place");
 		img = mBundle.getInt("img");
 		curLoc = mBundle.getParcelable(ConstantsAndKey.KEY_CURLOC);
 		mSharePref = PreferenceManager.getDefaultSharedPreferences(this);
     	if(mSharePref!=null){
     	lang = mSharePref.getString(getResources().getString(R.string.prefkey_lang), getResources().getStringArray(R.array.arr_lang_value)[0]);    	
     	}
     	
     	
     	
 		GetDetail gd = new GetDetail(lang);
 		gd.execute(place);
 		}
 						
 		}
 	
 		
 		@Override
 		protected Dialog onCreateDialog(int id) {
 			switch(id){
 			case LIST_MODE:
 				return new AlertDialog.Builder(this)
                 .setTitle(R.string.de_dir_mode_text)
                 .setItems(R.array.arr_dir_mode, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                     	GetDirection getDir = new GetDirection(getResources().getIntArray(R.array.dir_color)[which]);
         				getDir.execute(getResources().getStringArray(R.array.arr_dir_mode_value)[which]);
                     }
                 })
                 .create();
 				
 			case DETAIL_ROUTE:
 	            return new AlertDialog.Builder(this)
 	                .setIcon(R.drawable.ic_route)
 	                .setTitle(R.string.de_detail_route)
 	                .setMessage(dirInstruct)
 	                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 	                    public void onClick(DialogInterface dialog, int whichButton) {
 	    
 	                        dismissDialog(DETAIL_ROUTE);
 	                    }
 	                })
 	                
 	                .create();
 			}
 			return null;
 		}
 		
 		//Show current location on map
 		private void showMarker(GeoPoint point, int  drawableId, boolean isCenter, String title)
 		{
 			Drawable drawable = this.getResources().getDrawable(drawableId);
 	        itemizedOverlay = new CustomOverlay(drawable, routeMap);        
 			
 			try {
 			String currentAdd = PointAddressUtil.ConvertPointToAddress(point, getBaseContext());
 			OverlayItem overlayitem = new OverlayItem(point, title, currentAdd);	    	
 	    	if(isCenter){
 	    		mapControl.setCenter(point);
 			}
 	    	itemizedOverlay.addOverlay(overlayitem);	    	
 	    	mapOverlays.add(itemizedOverlay);
 			}
 			catch (UnknownHostException e){
 				e.printStackTrace();
 			}
 	    	
 	    	
 		}
 		//Create Task to make request and get places detail
 		private class GetDetail extends AsyncTask<Place,Void, PlaceDetailRs>{
 			private String lang;
 			
 			
 			public GetDetail(String lang){
 				this.lang = lang;
 			}
 			
 			@Override
 			protected void onPreExecute() {
 				mProgressBar.setVisibility(View.VISIBLE);
 				super.onPreExecute();
 			}
 			
 			@Override
 			protected PlaceDetailRs doInBackground(Place... params) {
 				
 								
 				PlaceDetailRs pd=null; //initialize place detail result						
 				pd = SearchPlace.getDetail(params[0].getReference(),lang);				 				
 				return pd;				
 			}
 			
 			@Override
 			protected void onPostExecute(PlaceDetailRs result) {
 				mProgressBar.setVisibility(View.GONE);
 				if (result!=null && result.getStatus().contentEquals(ConstantsAndKey.STATUS_OK)){	
 					plDetail = result.getResult();
 					setDetail(plDetail);					
 					GetDirection getDir = new GetDirection(getResources().getIntArray(R.array.dir_color)[0]);
 					getDir.execute(result.getResult().getAddress(),"driving");
 					//spDirMode.setOnItemSelectedListener(new MyOnItemSelectListener());
 											
 				}
 				else
 					{
 					Toast.makeText(getApplicationContext(), getResources().getText(R.string.seact_rq_error), Toast.LENGTH_SHORT).show();
 					}				
 			}
 		}
 		
 		//Create Task to make request and get places detail
 				private class GetDirection extends AsyncTask<String,Void, Direction>{
 					private int color;					
 					
 					public GetDirection (int color){
 						this.color = color;
 					}
 					
 					@Override
 					protected void onPreExecute() {
 						mProgressBar.setVisibility(View.VISIBLE);
 						super.onPreExecute();
 					}
 					
 					/**
 					 * @param params[0] destination address, params[1] is direction mode (driving, walking or bicycling)
 					 */
 					@Override
 					protected Direction doInBackground(String... params) {
 						Direction direct = new Direction();	
 						 
 						direct = SearchPlace.getDirection(curLoc.getLat(), curLoc.getLng(),
								plDetail.getGeometry().getLocation().getLat(),plDetail.getGeometry().getLocation().getLng(), params[1]);						
 						
 						try {
 							Thread.sleep(1000);//Thread pause for 1s 
 						} catch (InterruptedException e) {							
 							e.printStackTrace();
 						}
 						return direct;				
 					}
 					
 					@Override
 					protected void onPostExecute(Direction result) {
 						
 						mProgressBar.setVisibility(View.GONE);
 						if(result !=null&&result.getStatus().equalsIgnoreCase(ConstantsAndKey.STATUS_OK)){
 							txtDistance.setText(getResources().getText(R.string.de_distance)+" " + result.getRoutes().get(0).getLegs().get(0).getDistance().getDistanceInText());
 							txtTime.setText(getResources().getText(R.string.de_time)+" " + result.getRoutes().get(0).getLegs().get(0).getDuration().getDurationInText());
 							//txtDirection.setMovementMethod(new ScrollingMovementMethod());
 							dirInstruct = Html.fromHtml(result.getRoutes().get(0).getLegs().get(0).getInstructions()).toString();
 							
 							List<GeoPoint> routes = result.getRoutes().get(0).getOverviewPolyline().getDecodePoly();
 							showMarker(routes.get(0),R.drawable.marker_start,true,getResources().getString(R.string.de_start));
 							showMarker(routes.get(routes.size()-1),R.drawable.marker_end,false,plDetail.getName());
 							for (int i=1; i<routes.size();i++){
 							mapOverlays.add(new RouteOverlay(routes.get(i),routes.get(i-1),color));							
 							}
 						}
 						
 						else {
 							Toast.makeText(mContext, R.string.de_dir_not_found,Toast.LENGTH_SHORT);
 						}
 					}
 				}
 		
 		private void setDetail (PlaceDetail item){							
 			txtPlaceName.setText(item.getName());
 			txtAddress.setText(item.getAddress());
 			ratBar.setRating(item.getRating());
 			txtPhone.setText(item.getPhone());
 			txtWebsite.setText(item.getWebsite());					
 			if (item.isFavorite()){
 				add_fav.setImageResource(R.drawable.ic_rsfavorite);
 			}
 			else {
 				add_fav.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						
 						FavDataService dataService = new FavDataService(getApplicationContext());
 						dataService.open();
 						
 						if(dataService.isExisted(plDetail.getId())){
 							Toast.makeText(getApplicationContext(), "Existed", Toast.LENGTH_LONG).show();
 							dataService.close();
 						}else{
 							dataService.createFav(plDetail.getId(), plDetail.getName(), plDetail.getAddress(), plDetail.getPhone(), plDetail.getRating(), plDetail.getGeometry().getLocation().getLng(), plDetail.getGeometry().getLocation().getLat(), plDetail.getUrl(), plDetail.getWebsite(), img);
 							dataService.close();
 							Toast.makeText(getApplicationContext(), getResources().getString(R.string.de_success), Toast.LENGTH_LONG).show();
 							add_fav.setImageResource(R.drawable.ic_rsfavorite);
 							add_fav.setClickable(false);
 						}
 					}
 				});
 			}			
 		}
 		
 	
 	
 	//Button Click Listener
 		private View.OnClickListener mClickListener = new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				//Button Web
 				if(btnWeb.isPressed()){
 					Intent mIntent = new Intent(mContext, WebInfoActivity.class);
 					mIntent.putExtra("url", plDetail.getUrl());
 					startActivity(mIntent);
 				}
 				//Button Call
 				/*if(btnCall.isPressed()){
 					try {
 					   	Intent intent = new Intent(Intent.ACTION_CALL);
 					   	String phoneNumber=null;
 					   	if (fromFv==false){
 		            	phoneNumber = "tel: " + place.getPhoneNumbers().get(0).getNumber();
 					   	}
 					   	else {
 					   		if (b.getString("phone")!=null){
 					   		phoneNumber = "tel: " + b.getString("phone");
 					   		}
 					   	}
 		            	intent.setData(Uri.parse(phoneNumber));
 		            	startActivity(intent);
 						}
 						catch (Exception e) {
 		            	Toast.makeText(getApplicationContext(), 
 		            			"KhĂ´ng gá»�i Ä‘Æ°á»£c", Toast.LENGTH_SHORT).show();
 						}
 					}*/
 				//Button Email
 				//Call extra email application to send mail
 				if (btnEmail.isPressed()){
 					final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);                
 	                emailIntent.setType("plain/text");           
 	                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, getResources().getString(R.string.de_email_add));         
 	                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.de_subject)); 
 	                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, plDetail.getUrl());						                 
 	                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
 				}
 				//Detail route
 				if (btnDetail.isPressed()){
 					showDialog(DETAIL_ROUTE);
 				}
 				//Button Map
 				if(btnMode.isPressed()){
 					showDialog(LIST_MODE);
 				}
 				//Button View On google place site
 				if(btnWeb.isPressed()){
 					
 				}
 				}
 			
 		};
 		
 		
 		//Menu creation
 	    @Override
 	    public boolean onCreateOptionsMenu(Menu menu) {
 	    	  	
 	    	MenuInflater inflater = getMenuInflater();
 			inflater.inflate(R.menu.mn_detail, menu);
 			return true;
 	    }
 	    
 		@Override
 		public boolean onOptionsItemSelected(MenuItem item) {
 			// TODO Auto-generated method stub
 			switch (item.getItemId())
 			{
 			case R.id.mn_home:
 				Intent mIntent = new Intent(DetailPlaceActivity.this,CategoriesListActivity.class);				
 				Bundle mBundle = new Bundle();
                 //mBundle.putDouble(ConstantsAndKey.KEY_LAT,curLat);
                 //mBundle.putDouble(ConstantsAndKey.KEY_LNG, curLng);
                 mIntent.putExtras(mBundle);
                 DetailPlaceActivity.this.startActivity(mIntent);
 				DetailPlaceActivity.this.finish();
 				return true;
 			case R.id.mn_lstfavorite:
 				Intent intent = new Intent(getApplicationContext(), ListFavoriteActivity.class);
 				/*Bundle mBundle1 = new Bundle();
 					mBundle1.putDouble("curlat", curLat);			
 					mBundle1.putDouble("curlon", curLon);
 				intent.putExtras(mBundle1);*/
 				startActivity(intent);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 			}
 			
 		}
 
 		@Override
 		protected boolean isRouteDisplayed() {
 			
 			return true;
 		}
 }
