 package il.ac.tau.team3.shareaprayer;
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.SPGeoPoint;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 import org.mapsforge.android.maps.MapActivity;
 //import org.mapsforge.android.maps.MapViewMode;
 import org.mapsforge.android.maps.IOverlayChange;
 import org.mapsforge.android.maps.OverlayItem;
 import org.mapsforge.android.maps.PrayerArrayItemizedOverlay;
 import org.mapsforge.android.maps.GeoPoint;
 
 
 import org.springframework.http.MediaType;
 import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
 import org.springframework.web.client.RestTemplate;
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.drawable.Drawable;
 
 
 
 
 
 public class FindPrayer 
 extends MapActivity 
 {
     
     	
 	private Drawable userDefaultMarker;
 	private Drawable othersDefaultMarker;
 	private Drawable synagougeMarker;
 	
 	private PrayerArrayItemizedOverlay userOverlay;
 	private PrayerArrayItemizedOverlay otherUsersOverlay;
 	private PlaceArrayItemizedOverlay publicPlaceOverlay;
 	
 	
 	
 	/*** @draw ***/
 	
 	public void drawUserOnMap(GeneralUser user)	
 	{
         //Clears the last location
 		userOverlay.clear();
 		
 		// create an OverlayItem with title and description
         UserOverlayItem item = new UserOverlayItem(user, user.getName(), user.getStatus());
 
         // add the OverlayItem to the PrayerArrayItemizedOverlay
         userOverlay.addItem(item);
 	}
 	
 	public void drawOtherUserOnMap(GeneralUser otheruser)	
 	{
 		// create an OverlayItem with title and description
 		UserOverlayItem other = new UserOverlayItem(otheruser, otheruser.getName(), otheruser.getStatus());
 
         // add the OverlayItem to the PrayerArrayItemizedOverlay
         otherUsersOverlay.addItem(other);
 	}
 	
 	
 	
 
     public void drawPublicPlaceOnMap(GeneralPlace place)    
     {
         // create an OverlayItem with title and description
         String address = place.getAddress();
         String name = place.getName();
         PlaceOverlayItem synagouge = new PlaceOverlayItem(place, name, address);
 
         // add the OverlayItem to the PrayerArrayItemizedOverlay
         publicPlaceOverlay.addItem(synagouge);
         return;
     }
 
     
 	
 	
 	/*** @update ***/
 	
 	public void updatePublicPlace(GeneralPlace place)	
 	{
 		boolean found = false;
 		
 		for (OverlayItem item : publicPlaceOverlay.getOverlayItems())
 		{
 			PlaceOverlayItem placeItem = (PlaceOverlayItem) item;
 			if (place.getId().equals(((GeneralPlace)placeItem.getPlace()).getId()))
 			{
 				publicPlaceOverlay.removeItem(item);
 				placeItem.setPlace(place);
 				publicPlaceOverlay.addItem(item);
 				found = true;
 				break;
 			}
 		}
 		if (!found)
 		{
 			drawPublicPlaceOnMap(place);
 		}
 	}
 
 	
 
 	public void updateUser(GeneralUser user, GeneralUser thisUser)	
 	{
 		boolean found = false;
 		for ( OverlayItem item : otherUsersOverlay.getOverlayItems())
 		{
 			UserOverlayItem userItem = (UserOverlayItem) item;
 			if ( (user.getId().equals(((GeneralUser)userItem.getUser()).getId()))	&&
 				 ((thisUser == null) || (!thisUser.getId().equals(userItem.getUser().getId()))) )	
 			{
 				
 				otherUsersOverlay.removeItem(item);
 				userItem.setUser(user);
 				otherUsersOverlay.addItem(item);
 				found = true;
 				break;
 			}
 		}
 		
 		if (!found)	
 		{
 			if ((thisUser == null) || (!thisUser.getId().equals(user.getId())))	
 			{ 
 				drawOtherUserOnMap(user);
 			}
 		}
 
 	}
 	
 	private  Map<String, String> getParameters(SPGeoPoint center)	{
 		
 		if (center == null)
         {
             return null;
         }
 		
 		GeoPoint screenEdge = mapView.getProjection().fromPixels(mapView.getWidth(), mapView.getHeight());
         if (screenEdge == null)
         {
             return null;
         }
 		
 		double distance = SPUtils.calculateDistanceMeters(
                 center.getLongitudeInDegrees(), center.getLatitudeInDegrees(),
                 screenEdge.getLongitude(), screenEdge.getLatitude());
         
        int distancemeters = (int) Math.ceil(distance);
         
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("latitude", new Double(center.getLatitudeInDegrees()).toString());
         parameters.put("longitude", new Double(center.getLongitudeInDegrees()).toString());
         parameters.put("radius", new Integer(distancemeters).toString());
         
         return parameters;
 	}
 	
 	private GeneralUser[] getUsers(SPGeoPoint center)	{ 
 		Map<String, String> parameters = getParameters(center);
 		
 		if (null == parameters)	{
 			return null;
 		}
 		
 		return restTemplate.getForObject(
                 "http://share-a-prayer.appspot.com/resources/prayerjersy/users?latitude={latitude}&longitude={longitude}&radius={radius}",
                 GeneralUser[].class, parameters);
 	}
 	
 	private GeneralPlace[] getPlaces(SPGeoPoint center)	{ 
 		Map<String, String> parameters = getParameters(center);
 		
 		if (null == parameters)	{
 			return null;
 		}
 		
 		return restTemplate
         .getForObject(
                 "http://share-a-prayer.appspot.com/resources/prayerjersy/places?latitude={latitude}&longitude={longitude}&radius={radius}",
                 GeneralPlace[].class, parameters);
 	}
 
 
 	private void updateMap(SPGeoPoint center)	{
 	
 		final GeneralUser[] users = getUsers(center);
 		if (null == users)	{
 			return;
 		}
 		final GeneralPlace[] places = getPlaces(center);
 		if (null == places)	{
 			return;
 		}
         
 		mapView.post(new Runnable()	
 		{
 			public void run() 
 			{
 				updateMap(service.getLocation(), users, places);
 			}
 		});
         
 	}
 	
     private void updateMap(SPGeoPoint center, GeneralUser[] users, GeneralPlace[] places)
     {
         if (center == null)
         {
             return;
         }
         
         
         GeneralUser thisUser = null;
         
         if (service != null)
         {
             thisUser = service.getUser();
             if (null != thisUser)
             {
                 drawUserOnMap(thisUser);
             }
         }
         
         
         
         
         
         if (null != users)
         {
         	List<UserOverlayItem> usersOverlayList = new ArrayList<UserOverlayItem>(users.length);
             for (GeneralUser user : users)
             {
             	if ((thisUser == null) || (!thisUser.getId().equals(user.getId())))	{
             		usersOverlayList.add(new UserOverlayItem(user, user.getName(), user.getStatus()));
             	}
             }
             otherUsersOverlay.changeItems(usersOverlayList);
         }
         
         
         
         if (null != places)
         {
         	List<PlaceOverlayItem> placesOverlayList = new ArrayList<PlaceOverlayItem>(places.length);
             for (GeneralPlace place : places)
             {
             	placesOverlayList.add(new PlaceOverlayItem(place, place.getName(), place.getAddress()));
             }
             publicPlaceOverlay.changeItems(placesOverlayList);
         }
     }
 	
 	
     
     
     /*** @rest ***/
     
 	private RestTemplate restTemplate;
 	
 	
 	public RestTemplate getRestTemplate()
 	{
 		return restTemplate;
 	}
 
 	public void setRestTemplate(RestTemplate restTemplate)
 	{
 		this.restTemplate = restTemplate;
 	}
 
 	
 	
 	private ILocationSvc  service = null;
 	private ILocationProv locationListener;
 	
 	
 	
 	
 		
 	private SPMapView mapView;
 	
 	private Thread refreshTask  = new Thread() 
    	{
    		@Override
    		public void run() 
    		{
    			while (!isInterrupted())	{
 	   			try {
 	   				synchronized (this)	{
 	   					wait(10000);
 	   				}
 					if(service.getLocation()!= null)
 		   			{
 		   				updateMap(service.getLocation());
 		   			}
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 				}
 	   			
    			}
    		}
    		
    	};
 	
 	
 
 	public void createNewPlaceDialog(String message, final SPGeoPoint point)
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(message);
 		builder.setCancelable(false);
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
 		{
 			public void onClick(DialogInterface dialog, int id) 
 			{
 				Thread t = new Thread()	{
                 	@Override
 					public void run()	{
                 		GeneralPlace newMinyan = new GeneralPlace("New Minyan Place", "", point);
         				newMinyan.addJoiner(service.getUser().getName());
         				
         				restTemplate.postForObject("http://share-a-prayer.appspot.com/resources/prayerjersy/updateplacebylocation", newMinyan, Long.class);
                 		
         				synchronized(refreshTask)	{
         					refreshTask.notify();
         				}
            			}
                 };
                 t.run();
 
 			}
 		});
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() 
 		{
 			public void onClick(DialogInterface dialog, int id) {}
 		});
 		
 		AlertDialog alert = builder.create();
 		
 		alert.show();
 	} 
 	
 	
 	
 	
 	
 	
 	/*** @Override ***/	
 	
 	@Override
     public void onDestroy()
     {
         if (null != service)
         {
             service.UnRegisterListner(locationListener);
         }
     }
         
     	
 		
 	@Override
 	public void onCreate(Bundle savedInstanceState) 	
 	{		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		//mapView = new SPMapView(this);
 		mapView = (SPMapView)findViewById(R.id.view1);
 		
         mapView.registerTapListener(new IMapTapDetect()	
         {
 			public void onTouchEvent(SPGeoPoint sp) 
 			{
 				createNewPlaceDialog("Do you want to create a public praying place?", sp);
 			}
         });
      
         restTemplate = new RestTemplate();
     	restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
         List<HttpMessageConverter<?>>      mc   = restTemplate.getMessageConverters();
         MappingJacksonHttpMessageConverter json = new MappingJacksonHttpMessageConverter();
  
         List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
         supportedMediaTypes.add(new MediaType("text", "javascript"));
         json.setSupportedMediaTypes(supportedMediaTypes);
         mc.add(json);
         restTemplate.setMessageConverters(mc);
         
         /*
          * User overlay and icon:
          */
         //Creates the user's map-icon
         userDefaultMarker = this.getResources().getDrawable(R.drawable.user_kipa_pin);
         // create an PrayerArrayItemizedOverlay for the user
 		userOverlay       = new PrayerArrayItemizedOverlay(userDefaultMarker, this);
 		 // add the PrayerArrayItemizedOverlay to the MapView
         mapView.getOverlays().add(userOverlay);
         
         /*
          * Synagouge overlay
          */
         //Creates the Synagouge's map-icon
         synagougeMarker    = this.getResources().getDrawable(R.drawable.synagouge2);
         // create an PrayerArrayItemizedOverlay for the user
         publicPlaceOverlay = new PlaceArrayItemizedOverlay(synagougeMarker, this);
 		 // add the PrayerArrayItemizedOverlay to the MapView
         mapView.getOverlays().add(publicPlaceOverlay);
         
         /*
          * Other users overlay and icons:
          */
 		//Creates the otherUser's map-icon
 		othersDefaultMarker = this.getResources().getDrawable(R.drawable.others_kipa_pin);
         // create an PrayerArrayItemizedOverlay for the others
 		otherUsersOverlay   = new PrayerArrayItemizedOverlay(othersDefaultMarker, this);
 		
 		
 		
 		// add the PrayerArrayItemizedOverlay to the MapView
         mapView.getOverlays().add(otherUsersOverlay);	 
         
         /*
          * Circle overlay:
          */
         // create the default paint objects for overlay circles
     	
         // Define a listener that responds to location updates
     	locationListener = new ILocationProv() 
     	{
 	    	// Called when a new location is found by the network location provider.
 			public void LocationChanged(SPGeoPoint point) {
 				mapView.getController().setCenter(SPUtils.toGeoPoint(point));
 				
 				synchronized(refreshTask)	{
 					refreshTask.notify();
 				}
 				//drawPointOnMap(mapView, point);
 				
     	    	
     	    }
     	};
     	
 
 /*   	final Timer     timer = new Timer();*/
    	
    	refreshTask.start();
     	
    	
    	
         ServiceConnection svcConn = new ServiceConnection()
         {
             
             public void onServiceDisconnected(ComponentName className)
             {
                 service = null;
             }
             
             public void onServiceConnected(ComponentName arg0, IBinder arg1)
             {
                 service = (ILocationSvc) arg1;
                 
                 try
                 {
                     service.RegisterListner(locationListener);
                     SPGeoPoint gp = service.getLocation();
                     publicPlaceOverlay.setThisUser(service.getUser());
                     mapView.getController().setCenter(SPUtils.toGeoPoint(gp));
                     if (gp == null)	{
                     	return;
                     }
                     Thread t = new Thread()	{
                     	@Override
 						public void run()	{
                     		updateMap(service.getLocation());
                			}
                     };
                     t.run();
                     // send the user to places overlay
                 }
                 catch (Throwable t)
                 {
                     Log.e("ShareAPrayer", "Exception in call to registerListner()", t);
                 }
                 //
                 // timer = new Timer();
                 // task = new TimerTask()
                 // {
                 // @Override
                 // public void run()
                 // {
                 // if(service.getLocation()!=null){
                 // updateMap(service.getLocation());
                 // }
                 // }
                 // };
                 /*timer.schedule(new TimerTask() {
 
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						synchronized (refreshTask)	{
 							refreshTask.notify();
 						}
 					}
                 	
                 }, 1, 100000);
 */                
                 otherUsersOverlay.RegisterListner( new IOverlayChange() {
                 	class TimerRefreshTask	extends TimerTask	{
 
 						@Override
 						public void run() {
 							// TODO Auto-generated method stub
 							synchronized (refreshTask)	{
 								refreshTask.notify();
 							}
 						}
                 		
                 	};
                 	
                 	private Timer t = new Timer();
                 	private TimerTask ts = new TimerRefreshTask();
                 	
         			public void OverlayChangeCenterZoom() {
         				ts.cancel();
         				t.purge();
         				ts = new TimerRefreshTask();
         				t.schedule(ts, 1000);
         				
         			}
         			
         		});
             }
             
             
             
         };
         
         bindService(new Intent(LocServ.ACTION_SERVICE), svcConn, BIND_AUTO_CREATE);
         
     }
 	
 	
 	
 }
 
