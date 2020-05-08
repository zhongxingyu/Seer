 package com.davelabs.wakemehome;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 
 public class MapPinPointActivity extends Activity {
 
 	private GoogleMap _map;
 	
 	private Dialog _searchQueryNotFoundDialog;
 	private Dialog _searchQueryLookupFailedDialog;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map_pinpoint);
         setInstanceVariables();
         
         tryToLookupSearchQuery();
     }
 
 	private void setInstanceVariables() {        
         MapFragment f = (MapFragment) this.getFragmentManager().findFragmentById(R.id.searchMap);
         _map = f.getMap();
 	}
 	
 	private void tryToLookupSearchQuery() {
 		final Context c = this;
 		String searchQuery = getPassedSearchQuery();
 		
 		LocationSearcher.ISearchListener listener = new LocationSearcher.ISearchListener() {
 			@Override
 			public void onSearchError(Exception e) {
 				searchQueryLookupFailed(e);
 			}
 			
 			@Override
 			public void onSearchComplete(LatLng result) {
 				searchQueryLookupComplete(result);
 			}
 		};
 		
 		LocationSearcher searcher = new LocationSearcher(listener);
 		searcher.startSearch(new GeocoderSearchProvider(this), searchQuery);
 	}
 	
 	private void searchQueryLookupFailed(Exception e) {
 		hideOverlay();
 		if (e instanceof IOException) {
			Toast.makeText(this, "Unable to connect to google maps servers", Toast.LENGTH_LONG).show();
 		} else {
 			Toast.makeText(this, "Unknown error", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	private void searchQueryLookupComplete(LatLng result) {
 		hideOverlay();
 		if (result != null) {
 			moveMapToTargetPoint(result);
 			showConfirmDestinationButton();
 		} else {
 			Dialog d = getSearchQueryNotFoundDialog();
 			d.show();
 		}
 	}
 
 	private void hideOverlay() {
 		View v = this.findViewById(R.id.progressOverlay);
 		v.setVisibility(View.INVISIBLE);
 	}
 
 	private void showConfirmDestinationButton() {
 		View b = this.findViewById(R.id.confirmDestinationButton);
 		b.setVisibility(View.VISIBLE);
 	}
 
 	private void moveMapToTargetPoint(LatLng targetPoint) {
 		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(targetPoint,_map.getMaxZoomLevel() -5);
 		_map.animateCamera(cameraUpdate);
 	}
 
 	private String getPassedSearchQuery() {
         Bundle extras = this.getIntent().getExtras();
 		String passedSearchType = extras.getString("searchQuery");
 		return passedSearchType;
 	}
 	
 	public void onLocationOkButtonClicked (View v)  {
     	Intent intent = new Intent(this, MapTrackingActivity.class);
     	intent.putExtra("LocationCoords", _map.getCameraPosition());
     	this.startActivity(intent);
     }
 	
 	public Dialog getSearchQueryNotFoundDialog() {
 		if (_searchQueryNotFoundDialog == null) {
 			final Activity a = this;
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Unable to find the address, please try another search query.");
 			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					a.finish();
 				}
 			});
 			
 			_searchQueryNotFoundDialog = builder.create();
 		}
 		
 		return _searchQueryNotFoundDialog;
 	}
 
 	public Dialog getSearchQueryLookupFailedDialog() {
 		if (_searchQueryLookupFailedDialog == null) {
 			final Activity a = this;
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Unable to find the address, please try another search query.");
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					a.finish();
 				}
 			});
 			
 			builder.setPositiveButton("Retry Now", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					tryToLookupSearchQuery();
 				}
 			});
 			
 			_searchQueryLookupFailedDialog = builder.create();
 		}
 		
 		return _searchQueryLookupFailedDialog;
 	}
 }
