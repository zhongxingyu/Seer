 package se.tidensavtryck.gateway;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import android.content.res.AssetManager;
 import android.location.Location;
 import com.google.android.maps.GeoPoint;
 import se.tidensavtryck.model.User;
 import se.tidensavtryck.model.Place;
 import se.tidensavtryck.model.Record;
 import se.tidensavtryck.model.Route;
 
 public class RouteGateway {
     private AssetManager mAssetManager;
 
     public RouteGateway(AssetManager assetManager) {
         this.mAssetManager = assetManager;
     }
 
     public List<Route> list() {
 		List<Route> routeList = new LinkedList<Route>();
 
         Route route = new Route();
         route.setTitle("En kort rundtur i Sundsvall");
         User user = new User("Me");
         route.setCreator(user);
         route.setDescription("Information hämtad från K-samsök.");
 
         Place place1 = new Place();
         place1.setTitle("Place 1");
         place1.setDescription("Runsten");
         
         Place place2 = new Place();
         place2.setTitle("Place 2");
         place2.setDescription("Båt");
         
         Place place3 = new Place();
         place3.setTitle("Place 3");
         place3.setDescription("Hedbergska");
         
         Place place4 = new Place();
         place4.setTitle("Place 4");
         place4.setDescription("Stenstaden");
 
         Location loc1 = new Location("se.tidensavtryck");
         loc1.setLatitude(62.4007043202567);
         loc1.setLongitude(17.2577392061653);
         place1.setGeoLocation(loc1);
 
         Location loc2 = new Location("se.tidensavtryck");
         loc2.setLatitude(62.394369903217); 
         loc2.setLongitude(17.2816450479837);
         place2.setGeoLocation(loc2);
 
         Location loc3 = new Location("se.tidensavtryck");
         loc3.setLatitude(62.3897829867526); 
         loc3.setLongitude(17.2995418371631);
         place3.setGeoLocation(loc3);
         
         Location loc4 = new Location("se.tidensavtryck");
         loc4.setLatitude(62.391178326117); 
         loc4.setLongitude(17.3004228024664);
         place4.setGeoLocation(loc4);
    
         /*
         Location loc5 = new Location("se.tidensavtryck");
         loc5.setLatitude(62.3900820918969);
         loc5.setLongitude(17.3091424714359); 
         place2.setGeoLocation(loc5);
 		*/
         
         Map<Integer, Place> placeRecordMap = new HashMap<Integer, Place>();
         placeRecordMap.put(1, place1);
         placeRecordMap.put(2, place2);
         placeRecordMap.put(3, place3);
         placeRecordMap.put(4, place4);
 
         List<Record> placeRecords = new ArrayList<Record>();
         try {
         	for (Integer i : placeRecordMap.keySet()) {
         		String filename = String.format("place%d.xml", i);
                InputStream is = this.mAssetManager.open(filename);
                 XMLPull pull = new XMLPull(is);
                 placeRecordMap.get(i).setRecords(pull.parse());
         		
         	}
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         List<Place> places = new LinkedList<Place>();
         places.add(place1);
         places.add(place2);
         places.add(place3);
         places.add(place4);
 
         route.setPlaces(places);
         routeList.add(route);
 
 		return routeList;
 	}
 }
