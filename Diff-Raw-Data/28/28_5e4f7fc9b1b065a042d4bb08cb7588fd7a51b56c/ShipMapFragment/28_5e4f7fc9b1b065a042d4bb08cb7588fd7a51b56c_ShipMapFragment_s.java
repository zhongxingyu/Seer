 package org.omships.omships;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Scanner;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * Shows the current ship on the map of the world.
  * @author jimtahu
  * Based on the basic map demo
  */
 public class ShipMapFragment extends Fragment {
 	private GoogleMap daMap;
 	//private MarkerOptions marker;
 	
 	static class FetchLocation extends AsyncTask<String,Integer,LatLng>{
 		private ShipMapFragment map;
 		
 		public FetchLocation(ShipMapFragment map) {
 			this.map=map;
 		}
 		
 		@Override
 		protected LatLng doInBackground(String... urls) {
 			Double lat=0.0,lng=0.0;
 			try {
 				Scanner input=new Scanner(new URL(urls[0]).openStream());
 				input.useDelimiter(",");
 				//unix time
 				input.nextLong();
 				//lng then lat (backwards)
 				lng=input.nextDouble();
 				lat=input.nextDouble();
 				input.close();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return new LatLng(lat, lng);
 		}//end doInBackground
 		
 		protected void onPostExecute(LatLng pos){
 			map.setUpMapIfNeeded(pos);
 		}
 	}//end class LocationFetch
 	
 	private void setUpMap(LatLng pos) {
         daMap.addMarker(new MarkerOptions()
         .position(pos)
         .title(Settings.getShip().getName()));
         daMap.moveCamera(
         		CameraUpdateFactory.newCameraPosition(
         				CameraPosition.fromLatLngZoom(
         						pos,5.0f)));
     }//end setUpMap
 	
 	protected void setUpMapIfNeeded(LatLng pos) {
         // Do a null check to confirm that we have not already instantiated the map.
         if (daMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             daMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             // Check if we were successful in obtaining the map.
             if (daMap != null) {
                 setUpMap(pos);
             }
         }
     }
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		Settings.loadConfig(getResources());
 		new FetchLocation(this).execute(Settings.getShip().getLocation());
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle){
 		return inflater.inflate(R.layout.maplayout, container, false);
 	}
 }//end MapActivity
