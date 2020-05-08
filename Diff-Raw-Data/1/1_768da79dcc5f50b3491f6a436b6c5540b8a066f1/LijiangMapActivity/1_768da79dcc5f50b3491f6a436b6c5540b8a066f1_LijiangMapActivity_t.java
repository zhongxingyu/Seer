 package com.utopia.lijiang;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 
 import com.baidu.mapapi.GeoPoint;
 import com.baidu.mapapi.MKAddrInfo;
 import com.baidu.mapapi.MKDrivingRouteResult;
 import com.baidu.mapapi.MKPoiResult;
 import com.baidu.mapapi.MKSearch;
 import com.baidu.mapapi.MKSearchListener;
 import com.baidu.mapapi.MKTransitRouteResult;
 import com.baidu.mapapi.MKWalkingRouteResult;
 import com.baidu.mapapi.MapView;
 import com.baidu.mapapi.OverlayItem;
 import com.baidu.mapapi.PoiOverlay;
 import com.utopia.lijiang.R;
 import com.utopia.lijiang.baidu.BaiduItemizedOverlay;
 import com.utopia.lijiang.baidu.BaiduLongPressItemizedOverlay;
 import com.utopia.lijiang.baidu.BaiduMapActivity;
 
 public class LijiangMapActivity extends BaiduMapActivity {
 
 	static String CURRENT_CITY = "";
 	static MapView mMapView = null;
 	static View mPopView = null;	
 	
 	MKSearch mSearch = null;
 	EditText poiNameEditText = null;
 	InputMethodManager imm = null;
 	ProgressDialog progressDialog = null;
 	TextView popName = null;
 	TextView popAddress = null;
 	
 	BaiduItemizedOverlay userOverlay = null;
 	BaiduItemizedOverlay searchOverlay = null;
 	BaiduItemizedOverlay customOverlay = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 	    
 		initialProgressDialog();
 		initialMapView();
 	    initialSearchInput();
 	    initialSearch();
 	    
 	    customOverlay = 
 	    		new BaiduLongPressItemizedOverlay(this,this.getResources().getDrawable(R.drawable.marker_rounded_grey));
 	    mMapView.getOverlays().add(customOverlay);
 	}
 
 
 	public void searchPosition(View targer){
 		String poiName = getPostionName();
 		if(poiName.length()>0){
 			progressDialog.show();
 			mSearch.poiSearchInCity(CURRENT_CITY,poiName);
 		}	
 	}
 	
 	private String getPostionName(){
 		return poiNameEditText.getText().toString().trim();
 	}
 	
 	private void hideIME(View v){
 		if(imm.isActive(v)){
 			imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		}
 	}
 	
 	private void initialProgressDialog(){
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		progressDialog.setMessage(getString(R.string.searching));
 		progressDialog.setCancelable(false);
 	}
 	
 	private void initialMapView(){
 		mMapView = (MapView)findViewById(R.id.bmapView);
 	    mMapView.setBuiltInZoomControls(true);
 	    mMapView.setDrawOverlayWhenZooming(true);
 	    
 	 
 	    
 	    attachPopView();
 	}
 	
 	private void initialSearch(){
 		   mSearch = new MKSearch();
 		   mSearch.init(mBMapMan, new MKSearchListener(){
 
 				@Override
 				public void onGetAddrResult(MKAddrInfo arg0, int arg1) {}
 
 				@Override
 				public void onGetDrivingRouteResult(MKDrivingRouteResult arg0,int arg1) {}
 
 				@Override
 				public void onGetPoiResult(MKPoiResult res, int type, int error) {
 					// TODO Auto-generated method stub
 					if (error != 0 || res == null) {
						progressDialog.hide();
 						Toast.makeText(LijiangMapActivity.this, "Error or Empty", Toast.LENGTH_LONG).show();
 						return;
 					}
 					
 					if(searchOverlay == null){
 						searchOverlay = 
 									new BaiduItemizedOverlay(
 											LijiangMapActivity.this,
 											getResources().getDrawable(R.drawable.marker_rounded_red));		  
 						mMapView.getOverlays().add(searchOverlay);
 					}
 					
 					searchOverlay.setData(res.getAllPoi());
 					mMapView.invalidate();
 					progressDialog.hide();
 				}
 
 				@Override
 				public void onGetTransitRouteResult(MKTransitRouteResult arg0,int arg1) {}
 
 				@Override
 				public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,int arg1) {}
 				});	       
 	}
 	
 	private void initialSearchInput(){
 		  imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		  poiNameEditText = (EditText)this.findViewById(R.id.searchPositionName);
 		  poiNameEditText.setOnEditorActionListener(new OnEditorActionListener(){
 
 		  @Override
 		  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 					// TODO Auto-generated method stub
 					if(actionId !=  EditorInfo.IME_ACTION_SEARCH){
 						return false;
 					}
 					
 					searchPosition(null);
 					hideIME(v);
 					return true;
 				}
 		    });
 	}
 	
 	private void attachPopView(){
 		mPopView=super.getLayoutInflater().inflate(R.layout.popview, null);
 		popName = (TextView)mPopView.findViewById(R.id.popName);
 		popAddress = (TextView)mPopView.findViewById(R.id.popAddress);
 		
 		mMapView.addView( mPopView,
                 new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                 		null, MapView.LayoutParams.TOP_LEFT));
 		mPopView.setVisibility(View.GONE);
 	}
 	
 	@Override
 	protected int getConentViewId() {
 		// TODO Auto-generated method stub
 		return R.layout.baidumap;
 	}
 	
 	@Override
 	public MapView getMapView() {
 		// TODO Auto-generated method stub
 		return mMapView;
 	}
 
 	@Override
 	public boolean onTapped(int i, OverlayItem item) {
 		// TODO Auto-generated method stub
 		Log.d("lijiang","onTapped");
 		popName.setText(item.getTitle());
 		popAddress.setText(item.getSnippet());
 		
 		mMapView.getController().setCenter(item.getPoint());
 		
 		LijiangMapActivity.mMapView.updateViewLayout( LijiangMapActivity.mPopView,
                 new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                 		item.getPoint(), 0,-30,MapView.LayoutParams.BOTTOM_CENTER));
 		LijiangMapActivity.mPopView.setVisibility(View.VISIBLE);
 		return true;
 	}
 
 
 	@Override
 	public void onTapping(GeoPoint pt, MapView v) {
 		// TODO Auto-generated method stub
 		Log.d("lijiang","onTapping");
 		LijiangMapActivity.mPopView.setVisibility(View.GONE);
 	}
 
 
 	
 
 }
