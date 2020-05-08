 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.socialhitchhiking.map;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.User;
 import android.content.Context;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 
 /**
  * A route, from one place to another, with points in between to represent the 
  * places the route goes through.
  */
 public class MapRoute extends Route implements Serializable{
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * A {@link String} to display the distance on the map. 
 	 * <br>On this format: Distance: 0km (about 0 mins)
 	 */
 	private String routeDistanceDescription;
 	
 	/**
 	 * Approximately how long the route is, in kilometers.
 	 */
 	private double distanceInKilometers = 0;
 	
 	/**
 	 * Approximately how long the route is, in minutes.
 	 */
 	private int distanceInMinutes = 0;
 	
 	private boolean drawable = false;
 	
 	public void setDrawable(boolean drawable){
 		this.drawable = drawable;
 	}
 	public MapRoute(){
 		super(); 
 		this.routeData = new ArrayList<Location>();
 		this.mapPoints = new ArrayList<MapLocation>();
 	}
 	
 	public MapRoute(User user, String name, int serial, List<MapLocation> drivingThrough){
 		this();
 		this.owner = user;
 		this.name = name;
 		this.serial = serial;
 		loadRoutePath(drivingThrough);
 	}
 		
 	public MapRoute(Route oldRoute, List<MapLocation> newDrivingThroughList, boolean drawable){
 		this();
 		this.name = oldRoute.getName();
 		this.owner = oldRoute.getOwner();
 		this.serial = oldRoute.getSerial();
 		this.drawable = drawable;
 		/*
 		List<Location> tempList = new ArrayList<Location>();
 		for(int i = 0; i < newDrivingThroughList.size(); i++)
 		{
 			//GeoPoint from = GeoHelper.getGeoPoint(newDrivingThroughList.get(i));
 			//GeoPoint to = GeoHelper.getGeoPoint(newDrivingThroughList.get(i+1));
 			tempList.add(new Location(newDrivingThroughList.get(i).latitude, newDrivingThroughList.get(i).longitude));
 		}
 		*/
 		//this.mapPoints = newDrivingThroughList;
 		
 		loadRoutePath(newDrivingThroughList);
 		//this.routeData = tempList;
 	}
 	
 	private void loadRoutePath(List<MapLocation> drivingThrough) {
 		this.mapPoints = drivingThrough;
 		for (int i = 0; i < drivingThrough.size()-1; i++) {
 			GeoPoint gpFrom = GeoHelper.getGeoPoint(drivingThrough.get(i));
 			GeoPoint gpTo = GeoHelper.getGeoPoint(drivingThrough.get(i+1));
 			MapRoute route = loadRoutePath(gpFrom, gpTo);
 			addRouteAsPartOfThis(route, (i == 0));
 		}
 	}
 	
 	private MapRoute loadRoutePath(GeoPoint gpFrom, GeoPoint gpTo){
 		GeoPoint startPoint = gpTo;
 		GeoPoint destPoint = gpFrom;
 
 		MapRoute entireRoute = new MapRoute();
 
 		double fromLat, fromLon, toLat, toLon;
 		int directionRequestCounter 		= 0;
 		final int MAX_DIRECTION_REQUESTS 	= 2;
 
 		do{
 			fromLat = 	(startPoint.getLatitudeE6()  / 1E6); 
 			fromLon = 	(startPoint.getLongitudeE6() / 1E6); 
 			toLat = 	(destPoint.getLatitudeE6()   / 1E6); 
 			toLon = 	(destPoint.getLongitudeE6()  / 1E6);
 
 			MapRoute route;
 			
 				try {
 					route = RouteProvider.getRoute(fromLat, fromLon, toLat, toLon, drawable);
 				} catch (MalformedURLException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 					Log.e("Errpr",e1.getMessage());
 					return null;
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 					Log.e("Errpr",e1.getMessage());
 					return null;
 				} catch (XmlPullParserException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 					Log.e("Errpr",e1.getMessage());
 					return null;
 				}
 
 			
 			entireRoute.addRouteAsPartOfThis(route, (directionRequestCounter == 0)); 
 
 			//Increasing the request counter:
 			directionRequestCounter++;
 
 			//Checking if the route has been done loading:
 			try{
 				if(isClose(startPoint, route.getStartGeoPoint(), 100)){
 					break;
 				}
 			}catch (IndexOutOfBoundsException e) {
 				//ERROR
 				break;
 			}
 
 			//Making sure the loop doesn't run infinitely:
 			if(directionRequestCounter > MAX_DIRECTION_REQUESTS) {
 				break;
 			}
 			destPoint = route.getStartGeoPoint();
 		} while(true);
 
 		return entireRoute;
 	}
 	
 	/**
 	 * I method for determining whether two {@link GeoPoint}s are close to each other. 
 	 * The integer given as an argument is how many meters away from each other the two GeoPoints maximum 
 	 * can be for the method to return true. <br><br>
 	 * This method is used by the {@link #drawPathOnMap(GeoPoint, GeoPoint, String, String)}-method, 
 	 * to 
 	 * @param a A {@link GeoPoint}
 	 * @param b A {@link GeoPoint}
 	 * @param closeNess How close you want the two {@link GeoPoint}s to be to each other.
 	 * @return True if the {@link GeoPoint}s are closer to each other than, or as close as,  
 	 * the given integer, false otherwise.
 	 */
 	private boolean isClose(GeoPoint a, GeoPoint b, int closeNess){
 		android.location.Location lA = new android.location.Location("");
 		lA.setLatitude((a.getLatitudeE6() / 1E6));
 		lA.setLongitude((a.getLongitudeE6() / 1E6));
 		
 		android.location.Location lB = new android.location.Location("");
 		lB.setLatitude((b.getLatitudeE6() / 1E6));
 		lB.setLongitude((b.getLongitudeE6() / 1E6));
 		
 		float distanceInMeters = lA.distanceTo(lB);
 		if(closeNess < 1) closeNess = 1;
 
 		if(distanceInMeters <= closeNess) return true;
 		return false;
 	}
 	
 	/**
 	 * Adds a {@link MapLocation} to the {@link #mapPoints} list.
 	 */
 	public void addToDrivingThroughList(MapLocation loc){
 		mapPoints.add(loc);
 	}
 	
 	public void setRouteName(String name){
 		this.name = name;
 	}
 
 	/**
 	 * This method sets the {@link #routeDistanceDescription}, and 
 	 * tries to use this {@link String} (convert it) to set the {@link #distanceInKilometers}
 	 * and {@link #distanceInMinutes}.
 	 */
 	public void setDistanceDescription(String description){
 		routeDistanceDescription = description;
 		int indexDist  = description.indexOf("Distance:") + 9;
 		int indexKM    = description.indexOf("km");
 		int indexAbout = description.indexOf("about")	  + 5;
 		int indexMins  = description.indexOf("mins");
 		if(indexMins == -1){
 			indexMins = description.indexOf("secs");
 			String distMin = description.substring(indexAbout, indexMins).trim();
 			setDistanceInMinutes((double)Math.round(Double.parseDouble(distMin)/60));
 		}else{
 			try{
 				String distMin = description.substring(indexAbout, indexMins).trim();
 				setDistanceInMinutes(Double.parseDouble(distMin));
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if(indexKM == -1){
 			indexKM = description.indexOf("m ");
 			String distKM = description.substring(indexDist, indexKM).trim();
 			setDistanceInKilometers(Double.parseDouble(distKM)/1000);
 		}else{
 			try{
 				String distKM  = description.substring(indexDist, indexKM).trim();
 				setDistanceInKilometers(Double.parseDouble(distKM));
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	public double getDistanceInKilometers(){
 		return distanceInKilometers;
 	}
 	public double getDistanceInMinutes(){
 		return distanceInMinutes;
 	}
 	public void setDistanceInKilometers(double distKM){
 		double formatted;
 		try{
 			DecimalFormat df = new DecimalFormat("#.##");
 			formatted = Double.parseDouble(df.format(distKM));
 		}catch (Exception e) {
 			formatted = (int)distKM;
 		}
 		distanceInKilometers = formatted;
 	}
 	public void setDistanceInMinutes(double distMin){
 		try{
 			distanceInMinutes = ((int)distMin);
 			if(distanceInMinutes == 0){
 				distanceInMinutes = 1;
 			}
 		}catch (Exception e) {
 			distanceInMinutes = 1;
 		}
 	}
 	public String getRouteName(){
 		return name;
 	}
 	public String getRouteDescription(){
 		return routeDistanceDescription;
 	}
 	public GeoPoint getStartGeoPoint(){
 		if(mapPoints.size()>0){
 			return GeoHelper.getGeoPoint(mapPoints.get(0));
 		}
 		return null;
 	}
 	public MapLocation getStartLocation(){
 		if(mapPoints.size()>0){
 			return mapPoints.get(0);
 		}
 		return null;
 	}
 	public MapLocation getEndLocation(){
 		if(mapPoints.size()>0){
 			return mapPoints.get(mapPoints.size()-1);
 		}
 		return null;
 	}
 	public GeoPoint getEndGeoPoint(){
 		if(mapPoints.size()>0){
 			return GeoHelper.getGeoPoint(mapPoints.get(mapPoints.size()-1));
 		}
 		return null;
 	}
 	
 	public int getNrOfGeoPoints(){
 		return routeData.size();
 	}
 
 	public synchronized void addCoordinate(MapLocation loc){
 		routeData.add(loc);
 	}
 	public synchronized void addCoordinate(Context context, double lat, double lon){
 		routeData.add(new MapLocation(lat, lon));
 	}
 	public List<GeoPoint> getGeoPointList(){
 		List<GeoPoint> list = new ArrayList<GeoPoint>();
 		for (Location loc: routeData) {
 			list.add(GeoHelper.getGeoPoint(loc));
 		}
 		return list;
 	} 
 	public void addLocationList(List<MapLocation> list){
 		for (MapLocation loc : list) {
 			addCoordinate(loc);
 		}
 	}
 	
 	/**
 	 * This method adds another {@link MapRoute} as a part of this route,
 	 * by adding the route data. It does not check whether the two routes
 	 * actually "fit" together, so make sure they do.
 	 */
 	public synchronized void addRouteAsPartOfThis(final MapRoute routePart, final boolean addInfo){
 		if(routePart == null) return;
 		
 		setDistanceInKilometers(distanceInKilometers + routePart.getDistanceInKilometers());
 		setDistanceInMinutes(distanceInMinutes + routePart.getDistanceInMinutes());
 		routeData.addAll(0, routePart.getRouteData());
 
 		if(distanceInKilometers > 0 && distanceInMinutes > 0){
 			routeDistanceDescription = "Distance: "+distanceInKilometers+"km (about "+distanceInMinutes+" mins)";
 		}else if(distanceInKilometers > 0){
 			routeDistanceDescription = "Distance: "+distanceInKilometers+"km";
		}else if(distanceInMinutes > 0){
			routeDistanceDescription = "Distance: About "+distanceInMinutes+" minutes";
 		}else{
 			routeDistanceDescription = "";
 		}
 		
 		if(addInfo){
 			if(name == null || name.equals("")){
 				name = routePart.getRouteName();
 			}
 		}
 	}
 
 }
