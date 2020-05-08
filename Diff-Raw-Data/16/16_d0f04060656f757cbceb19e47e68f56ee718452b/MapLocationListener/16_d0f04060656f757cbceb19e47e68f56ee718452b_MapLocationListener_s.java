 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
  */
 
 package se.team05.listener;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import se.team05.R;
 import se.team05.content.ParcelableGeoPoint;
 import se.team05.content.Track;
 import se.team05.overlay.CheckPoint;
 import se.team05.service.MediaService;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 
 /**
  * The MapLocationListener class is a LocationListener which uses callback to
  * update the location
  * 
  * @author Patrik Thituson, Daniel Kvist
  * 
  */
 public class MapLocationListener implements LocationListener
 {
 	public interface Callbacks
 	{
 		public void onLocationChanged(ParcelableGeoPoint geoPoint, String userSpeed, String userDistance, float totalDistance);
 	}
 
 	private static final String TAG = "Personal trainer";
 	private Callbacks callback;
 	private Context context;
 	private boolean newRoute;
 	private Intent serviceIntent;
 	private ArrayList<CheckPoint> checkPoints;
 	private CheckPoint currentCheckPoint;
 	private Location lastLocation;
 
 	/**
 	 * The Constructor
 	 * 
 	 * @param callback
 	 */
 	public MapLocationListener(Callbacks callback, boolean newRoute, ArrayList<CheckPoint> checkPoints)
 	{
 		this.callback = callback;
 		this.context = (Context) callback;
 		this.newRoute = newRoute;
 		this.checkPoints = checkPoints;
 		this.serviceIntent = new Intent(context, MediaService.class);
 		lastLocation = null;
 	}
 
 	/**
 	 * Helper method to calculate the distance between geo point A and B.
 	 * 
 	 * @param geoPointA
 	 *            the first point
 	 * @param geoPointB
 	 *            the second point
 	 * @return a float representing the distance.
 	 */
 	public static float getDistance(GeoPoint geoPointA, GeoPoint geoPointB)
 	{
 		double latitudeA = ((double) geoPointA.getLatitudeE6()) / 1E6;
 		double longitudeA = ((double) geoPointA.getLongitudeE6()) / 1E6;
 		double latitudeB = ((double) geoPointB.getLatitudeE6()) / 1E6;
 		double longitudeB = ((double) geoPointB.getLongitudeE6()) / 1E6;
 		float[] distance = new float[1];
 		Location.distanceBetween(latitudeA, longitudeA, latitudeB, longitudeB, distance);
 		return distance[0];
 	}
 	
 	public void stopService()
 	{
 		context.stopService(serviceIntent);
 	}
 
 	/**
 	 * Calls updateLocation in callback with the location
 	 */
 	@Override
 	public void onLocationChanged(Location location)
 	{
 		GeoPoint geoPoint;
		float totalDistance= 0;
		String userSpeed = "";
		String userDistance = "";
 		ParcelableGeoPoint currentGeoPoint = new ParcelableGeoPoint((int) (location.getLatitude() * 1E6),
 				(int) (location.getLongitude() * 1E6));
		userSpeed = (3.6 * location.getSpeed()) + context.getString(R.string.km) + "/" + context.getString(R.string.h);
 		
 		if (lastLocation != null)
 		{
 			totalDistance = totalDistance + lastLocation.distanceTo(location);
 			userDistance = new DecimalFormat("#.##").format(totalDistance / 1000);
 		}
 		
 		if (!newRoute)
 		{
 			for (CheckPoint checkPoint : checkPoints)
 			{
 				geoPoint = checkPoint.getPoint();
 				if (MapLocationListener.getDistance(currentGeoPoint, geoPoint) <= checkPoint.getRadius())
 				{
 					if (checkPoint != currentCheckPoint)
 					{
 						context.stopService(serviceIntent);
 						ArrayList<Track> playList = checkPoint.getTracks();
 						if (playList.size() > 0)
 						{
 							serviceIntent.putExtra(MediaService.DATA_PLAYLIST, playList);
 							serviceIntent.setAction(MediaService.ACTION_PLAY);
 							try
 							{
 								context.startService(serviceIntent);
 							}
 							catch (Exception e)
 							{
 								Log.e(TAG, context.getString(R.string.could_not_start_media_service_) + e.getMessage());
 							}
 						}
 						currentCheckPoint = checkPoint;
 					}
 					break;
 				}
 			}
 		}
 		lastLocation = location;
 		callback.onLocationChanged(currentGeoPoint, userSpeed, userDistance, totalDistance);
 	}
 
 	/**
 	 * Unused method
 	 */
 	@Override
 	public void onProviderDisabled(String provider)
 	{
 
 	}
 
 	/**
 	 * Unused method
 	 */
 	@Override
 	public void onProviderEnabled(String provider)
 	{
 
 	}
 
 	/**
 	 * Unused method
 	 */
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras)
 	{
 
 	}
 
 }
