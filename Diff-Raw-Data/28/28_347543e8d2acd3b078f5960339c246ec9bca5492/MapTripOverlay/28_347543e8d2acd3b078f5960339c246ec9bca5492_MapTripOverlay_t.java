 /**
  * @contributor(s): Kristian Greve Hagen (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			23 May 2011
  * @revised:
  *
  * Copyright (C) 2011 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
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
 
 /**
  * @description:
  * This class keeps hold of the tabs used in planning mode.
  * 
  */
 
 package org.ubicompforall.CityExplorer.map;
 
 import java.util.ArrayList;
 
 import org.ubicompforall.CityExplorer.data.Poi;
 import org.ubicompforall.CityExplorer.data.Trip;
 import org.ubicompforall.CityExplorer.map.route.GoogleKML;
 import org.ubicompforall.CityExplorer.map.route.Road;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 /**
  * The Class MapTripOverlay.
  */
 public class MapTripOverlay extends Overlay
 {
 
 	/** The pencil. */
 	Paint pencil;
 	
 	/** The trip. */
 	Trip trip;
 	
 	/** The road segments. */
 	ArrayList<PoiToPoi>	roadSegments;
 
 	/** The current poi index. */
 	int currentPoiIndex = 0;
 	
 	/** The current poi point. */
 	Point currentPoiPoint = new Point(0,0);
 	
 	/**
 	 * Instantiates a new map trip overlay.
 	 *
 	 * @param trip the trip
 	 */
 	MapTripOverlay(Trip trip) { 
 		this.trip = trip;
 		
 		init();
 	}
 
 	/**
 	 * Inits the.
 	 */
 	private void init(){
 
 		pencil = new Paint();
 		pencil.setColor(Color.BLUE);
 		//pencil.setARGB(255, 255, 0, 0);
 		pencil.setAntiAlias(true);
 		pencil.setFakeBoldText(true);
 		pencil.setStrokeWidth(6);
 		pencil.setAlpha(70);
 		pencil.setStyle(Paint.Style.STROKE);
 
 		roadSegments = new ArrayList<PoiToPoi>(trip.getPois().size());
 
 		for (int i=0; i<trip.getPois().size()-1; i++){
 			roadSegments.add( 
 				new PoiToPoi( 
 					trip.getPois().get(i), 
 					trip.getPois().get(i+1))
 			);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean)
 	 */
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		super.draw(canvas, mapView, shadow);
 
 		for (PoiToPoi ptp : roadSegments){
 			ptp.drawRoadSegment(mapView,canvas,pencil);
 		}
		if ( trip.getPoiAt(currentPoiIndex) != null){
			mapView.getProjection().toPixels(trip.getPoiAt(currentPoiIndex).getGeoPoint(), currentPoiPoint);
			Paint p = new Paint();
			p.setColor(Color.GREEN);
			canvas.drawCircle(currentPoiPoint.x, currentPoiPoint.y, 8, p);
			//drawRoute(canvas, mapView, pencil);
		}// if a POI is at the currentPoiIndex in the tour
	}//draw current POI
 	
 	/**
 	 * Sets the current poi index.
 	 *
 	 * @param currentPoiIndex the new current poi index
 	 */
 	public void setCurrentPoiIndex(int currentPoiIndex)
 	{
 		this.currentPoiIndex = currentPoiIndex;
 	}
 	
 	/**
 	 * Gets the current poi index.
 	 *
 	 * @return the current poi index
 	 */
 	public int getCurrentPoiIndex()
 	{
 		return currentPoiIndex;
 	}
 
 	/**
 	 * Gets the trip.
 	 *
 	 * @return the trip
 	 */
 	public Trip getTrip()
 	{
 		return trip;
 	}
 	
 	/**
 	 * Gets the pois.
 	 *
 	 * @return the pois
 	 */
 	public ArrayList<Poi> getPois()
 	{
 		return trip.getPois();
 	}
 	
 	/**
 	 * Gets the center.
 	 *
 	 * @return the center
 	 */
 	public GeoPoint getCenter(){
 		
 		int l = trip.getPois().size()-1;
 
 		int lat50 = (int)( ( trip.getPois().get(0).getAddress().getLatitude() +
 					 		(trip.getPois().get(l).getAddress().getLatitude() -
 					 			trip.getPois().get(0).getAddress().getLatitude()
 					 		)/2 )*1e6);
 
 		int lon50 = (int)( ( trip.getPois().get(0).getAddress().getLongitude() +
 							(trip.getPois().get(l).getAddress().getLongitude() - 
 								trip.getPois().get(0).getAddress().getLongitude()
 						    )/2 )*1e6);
 
 		return new GeoPoint( lat50, lon50);
 
 	}
 	
 	/**
 	 * represents the road between two pois.
 	 * has a method for drawing the road between them
 	 */
 	private class PoiToPoi {
 		
 		/** The from to. */
 		Road fromTo;
 		
 		/**
 		 * Instantiates a new poi to poi.
 		 *
 		 * @param from the from
 		 * @param to the to
 		 */
 		PoiToPoi(Poi from, Poi to){
 			final Poi f = from;
 			final Poi t = to;
 			//new Thread(){ public void run(){
 				fromTo = GoogleKML.getRoad(
 					f.getAddress().getLatitude(),
 					f.getAddress().getLongitude(),
 					t.getAddress().getLatitude(),
 					t.getAddress().getLongitude()
 				);
 				fromTo.getGeoPoints();
 			//}}.start();
 		}
 		
 		/**
 		 * Draw road segment.
 		 *
 		 * @param mv the mv
 		 * @param cs the cs
 		 * @param pen the pen
 		 */
 		public void drawRoadSegment(MapView mv, Canvas cs, Paint pen){
 			Point last = null;  
 
 			for ( GeoPoint gp : fromTo.getGeoPoints()){
 				Point onMap = new Point();
 				mv.getProjection().toPixels(gp, onMap);
 
 				if ( last != null) {
 					cs.drawLine(last.x, last.y,  onMap.x, onMap.y, pen);
 				}
 				last = onMap;
 			}
 		}
 	}
 }
 
 
