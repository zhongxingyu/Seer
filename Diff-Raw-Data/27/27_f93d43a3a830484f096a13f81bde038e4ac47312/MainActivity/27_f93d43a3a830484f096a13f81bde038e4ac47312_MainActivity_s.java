 package com.example.phillycrime;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.StreamCorruptedException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Window;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.GetObjectRequest;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.GoogleMapOptions;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MainActivity extends Activity implements OnMarkerClickListener {
 	private GoogleMap map;
 	public static final String key="HAIlFtZEDI3hRTgOf1MwBRzNst7oiBnLYGqNODUi";
 	public static final String id= "AKIAJYSNO2HXNLWXF72Q";
 	public static final String bucket= "appcrimedata";
 	public HashMap<Marker, Crime> marks= new HashMap<Marker, Crime>();
 	protected AmazonS3Client s3Client;
 
 	public boolean AWSused=false;
	public MainActivity(){
		s3Client =   new AmazonS3Client( new BasicAWSCredentials( id, key ) );

	}
 	public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
 		MapFragment mMapFragment = MapFragment.newInstance();
 		GoogleMapOptions option= new GoogleMapOptions();
 		option.camera(new CameraPosition(map.getCameraPosition().target,(float)14.0,(float)0.0,(float)0.0));
 		setUpMap();
 		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 		map.setMyLocationEnabled(true);
 		map.setOnMarkerClickListener(this);
 		GPSGrabber locdate= new GPSGrabber(this, mMapFragment);
 		locdate.execute((Void)null);
 	}
 	protected void setUpMap() {
 	    // Do a null check to confirm that we have not already instantiated the map.
 	    if (map == null) {
 	        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
 	                            .getMap();
 	        // Check if we were successful in obtaining the map.
 	        if (map == null) {
 	            finish();
 	        }
 	    }
 	}
 	public void addMark(MarkerOptions mark, Crime c){
 		marks.put(map.addMarker(mark), c);
 	}
 	
 	private double distance(double lat1, double lat2, double lon1, double lon2,
 	        double el1, double el2) {
 
 	    final int R = 6371; // Radius of the earth
 
 	    Double latDistance = deg2rad(lat2 - lat1);
 	    Double lonDistance = deg2rad(lon2 - lon1);
 	    Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
 	            + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
 	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
 	    Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 	    double distance = R * c * 1000; // convert to meters
 
 	    double height = el1 - el2;
 	    distance = Math.pow(distance, 2) + Math.pow(height, 2);
 	    return Math.sqrt(distance);
 	}
 
 	private double deg2rad(double deg) {
 	    return (deg * Math.PI / 180.0);
 	}
 	 public class GPSGrabber  extends AsyncTask<Void, Void, Void>{
 			LocationClient mLocationClient;
 			Location mCurrentLocation;
 			MainActivity main;
 			MapFragment mmap;
 
 	    	public GPSGrabber(MainActivity man, MapFragment mapfrag){
 	    		main=man;
 	    		mmap=mapfrag;
 	    		
 	    	}
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
 				fragmentTransaction.add(R.id.map, mmap);
 				fragmentTransaction.commit();												
 				ObjectInputStream in;
 				try {
 					in = new ObjectInputStream(s3Client.getObject(new GetObjectRequest(MainActivity.bucket, "locations")).getObjectContent());
 				String locations=(String)in.readObject();
 				in.close();
 				in=new ObjectInputStream(s3Client.getObject(new GetObjectRequest(MainActivity.bucket, "crime")).getObjectContent());
 				String crime=(String)in.readObject();
 				in.close();
 				in=new ObjectInputStream(s3Client.getObject(new GetObjectRequest(MainActivity.bucket, "time")).getObjectContent());
 				String time=(String)in.readObject();
 				in.close();
 				
 				ArrayList<LatLng> latlngs= new ArrayList<LatLng>();
 				ArrayList<String> crimes= new ArrayList<String>();
 				ArrayList<String> times= new ArrayList<String>();
 				int first=0;
 				int last=0;
 				int index=0;
 				boolean done=false;
 				for (char c:locations.toCharArray()){
 					if (c==';'){
 						if (done){
 							latlngs.add(new LatLng(Double.parseDouble(locations.substring(first, last)), Double.parseDouble(locations.substring(last, index))));
 							first=index+1;
 							last=index+1;
 							done=false;
 						}
 						else{
 							last=index;
 							done=true;
 						}
 					}
 					
 					index++;
 				}
 				for (char c:crime.toCharArray()){
 					if (c==';'){
 						crimes.add(crime.substring(last, index));
 						last=index+1;
 					}
 					
 					index++;
 				}
 				for (char c:time.toCharArray()){
 					if (c==';'){
 						times.add(time.substring(last, index));
 						last=index+1;
 					}
 					
 					index++;
 				}
 				ArrayList<Crime> list= new ArrayList<Crime>();
 				for (int i=0; i<crimes.size();i++){
 					list.add(new Crime(latlngs.get(i), crimes.get(i),times.get(i)));
 				}
 				for (int i=0; i<list.size();i++){
 	            	MarkerOptions mark= new MarkerOptions();
 	            	mark.position(list.get(i).getLocation());
 	            	mark.draggable(false);
 	            	mark.visible(false);
 	            	main.addMark(mark, list.get(i));
 				}
 			} catch (StreamCorruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (AmazonServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (AmazonClientException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 				return null;
 	    	
 	    }
 	 }
 	@Override
 	public boolean onMarkerClick(Marker arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	} 
 }
