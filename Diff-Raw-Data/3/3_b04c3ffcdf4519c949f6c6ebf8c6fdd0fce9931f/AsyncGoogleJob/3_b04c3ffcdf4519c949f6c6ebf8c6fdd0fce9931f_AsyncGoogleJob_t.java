 package anywayanyday.pointsonmap;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.AsyncTask;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class AsyncGoogleJob extends AsyncDataDownload implements MapFragmentWithCreatedListener.MapCreatedListener {
 
 	public static final int MAX_GEOCODE_RESULTS = 1;
 	private DataRequest request;
 
 	@Override
 	public void dataDownload(DataRequest request, DownloaderListener downloaderListener) {
 		this.downloaderListener = downloaderListener;
 		this.request = request;
 
 		if (!isNetworkOnline()) {
 			downloaderListener.sendToast(downloaderListener.getContext().getResources().getString(R.id.toast_offline));
 			return;
 		}
 
 		switch (request.getRequestType()) {
 		case DataRequest.GEO_DATA:
 			AsyncGeoDataDownload geoDownloader = new AsyncGeoDataDownload();
 			geoDownloader.execute(request);
 			break;
 		case DataRequest.MAP_TO_IMAGE_VIEW:
 			showMap(request);
 			break;
 
 		}
 
 	}
 
 	private class AsyncGeoDataDownload extends AsyncTask<DataRequest, Void, LatLng> {
 
 		private DataRequest dataRequest;
 
 		@Override
 		protected LatLng doInBackground(DataRequest... params) {
 			dataRequest = params[0];
 			Geocoder geocoder = new Geocoder(downloaderListener.getContext());
 			try {
 				List<Address> addresses = geocoder.getFromLocationName(dataRequest.getDot().getAddress(), MAX_GEOCODE_RESULTS);
				if (addresses.size() == 0) return null;
                Address address = addresses.get(0);
 				return new LatLng(address.getLongitude(), address.getLatitude());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(LatLng latLng) {
 			if (latLng == null)
 				return;
 			String geoData = latLng.latitude + " " + latLng.longitude;
 			downloaderListener.onDownloaderResponse(dataRequest, geoData);
 		}
 	}
 
 	public static LatLng getGeoData(String requestAddress, Context context) throws IOException {
 		if (context == null)
 			throw new IOException();
 		Geocoder geocoder = new Geocoder(context);
 		List<Address> addresses = geocoder.getFromLocationName(requestAddress, MAX_GEOCODE_RESULTS);
 		Address address = addresses.get(0);
 		return new LatLng(address.getLongitude(), address.getLatitude());
 	}
 
 	private MapFragment mMapFragment;
 
 	private MapFragment showMap(DataRequest request) {
 
 		mMapFragment = MapFragmentWithCreatedListener.newInstanceCreate(this);
 		downloaderListener.onDownloaderResponse(request, mMapFragment);
 		return mMapFragment;
 	}
 
 	@Override
 	public void onGoogleMapCreation() {
 
 		GoogleMap mMap = mMapFragment.getMap();
 		if (mMap != null) {
 			ArrayList<Dot> dots = request.getDots();
 			for (Dot dot : dots) {
 				try {
 					LatLng geoLoc = getGeoData(dot.address, downloaderListener.getContext());
 					mMap.addMarker(new MarkerOptions().position(geoLoc).title(dot.name + geoLoc.toString()));
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 
 }
