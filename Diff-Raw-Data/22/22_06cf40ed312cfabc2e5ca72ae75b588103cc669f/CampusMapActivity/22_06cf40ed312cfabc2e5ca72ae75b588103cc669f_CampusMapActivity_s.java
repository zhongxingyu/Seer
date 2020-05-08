 // CampusMapActivity.java
 /**
  * Copyright 2012 Kristin Mead
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.mapper.activities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Pair;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.mapper.map.MapEdge;
 import com.mapper.map.MapNode;
 import com.mapper.map.MapOverlay;
 import com.mapper.map.NodeDB;
 
 public class CampusMapActivity extends MapActivity
 {
     private static NodeDB campusDB;
     private MapController mc;
     private LocationManager lm;
     private LocationListener ll;
     GeoPoint p = null;
 
     private static double MapCenterLatitude = 44.973785;
     private static double MapCenterLongitude = -93.232191;
 
<<<<<<< HEAD
=======
    /** 
     * Called when the activity is first created. 
     * @parm savedInstanceState  
     */
>>>>>>> cd4de3b12902a3bc145ca536559af9c23e2c2406
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map);
 
         // Create map view
         MapView mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapView.setSatellite(false);
 
         // Get MapOverlap Object List
         List<Overlay> mapOverlays = mapView.getOverlays();
 
         // Get campus from adaptation
         campusDB = new NodeDB(readCampusAdaptation());
 
         ArrayList<MapNode> campusNodeList = campusDB.getNodeList();
         ArrayList<Integer> alreadyDrawnEdge = new ArrayList<Integer>();
 
         for(MapNode node : campusNodeList)
         {
             for(MapEdge edge : node.getAdjacentEdges())
             {
                 if(!alreadyDrawnEdge.contains(edge.getUniqueID()))
                 {
                     mapOverlays.add(new MapOverlay(edge.getFirstNode(), edge.getSecondNode()));
                     alreadyDrawnEdge.add(edge.getUniqueID());
                 }
             }
         }
 
         // Get Map Controller to set location and zoom
         mc = mapView.getController();
 
         // Center Map
         p = new GeoPoint((int) (MapCenterLatitude * 1000000), (int) (MapCenterLongitude * 1000000));
         mc.animateTo(p);
         mc.setZoom(17);
 
         MyLocationOverlay myLocationOverlay = new MyLocationOverlay();
         List<Overlay> list = mapView.getOverlays();
         list.add(myLocationOverlay);
 
         lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         ll = new MyLocationListener();
         lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.layout.campus_options_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         // Handle item selection
         switch(item.getItemId())
         {
<<<<<<< HEAD
             case R.id.search:
                 onSearchRequested();
=======
            case R.id.get_directions:
                // newGame();
>>>>>>> cd4de3b12902a3bc145ca536559af9c23e2c2406
                return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private ArrayList<Pair<GeoPoint, GeoPoint>> readCampusAdaptation()
     {
         ArrayList<String> stringXmlContent = null;
         ArrayList<Pair<GeoPoint, GeoPoint>> returnList = new ArrayList<Pair<GeoPoint, GeoPoint>>();
         try
         {
             stringXmlContent = getEventsFromAnXML(this);
         }
         catch(XmlPullParserException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch(IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         for(String line : stringXmlContent)
         {
             String coordinate = line.replaceAll("\n", ",");
             coordinate = coordinate.replaceAll(" ", "");
             String[] coordinates = coordinate.split(",");
 
             returnList.add(new Pair<GeoPoint, GeoPoint>(new GeoPoint(
                     (int) (Double.valueOf(coordinates[1]) * 1000000),
                     (int) (Double.valueOf(coordinates[0]) * 1000000)),
                     new GeoPoint(
                             (int) (Double.valueOf(coordinates[4]) * 1000000),
                             (int) (Double.valueOf(coordinates[3]) * 1000000))));
         }
 
         return returnList;
     }
 
     private ArrayList<String> getEventsFromAnXML(Activity activity)
             throws XmlPullParserException, IOException
     {
         ArrayList<String> coordinateList = new ArrayList<String>();
         Resources res = activity.getResources();
         XmlResourceParser xpp = res.getXml(R.xml.campusxml);
         xpp.next();
 
         int eventType = xpp.getEventType();
 
         while(eventType != XmlPullParser.END_DOCUMENT)
         {
             if(eventType == XmlPullParser.TEXT)
             {
                 coordinateList.add(xpp.getText());
             }
             eventType = xpp.next();
         }
 
         return coordinateList;
     }
 
     private class MyLocationListener implements LocationListener
     {
         public void onLocationChanged(Location argLocation)
         {
             // TODO Auto-generated method stub
             GeoPoint myGeoPoint = new GeoPoint(
                     (int) (argLocation.getLatitude() * 1000000),
                     (int) (argLocation.getLongitude() * 1000000));
 
             p = myGeoPoint;
             mc.animateTo(myGeoPoint);
         }
 
         public void onProviderDisabled(String provider)
         {
             // TODO Auto-generated method stub
         }
 
         public void onProviderEnabled(String provider)
         {
             // TODO Auto-generated method stub
         }
 
         public void onStatusChanged(String provider, int status, Bundle extras)
         {
             // TODO Auto-generated method stub
         }
     }
 
     protected class MyLocationOverlay extends com.google.android.maps.Overlay
     {
         @Override
         public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
                 long when)
         {
             super.draw(canvas, mapView, shadow);
 
             // Converts lat/lng-Point to OUR coordinates on the screen.
             Point myScreenCoords = new Point();
             mapView.getProjection().toPixels(p, myScreenCoords);
 
             Paint paint = new Paint();
             paint.setStrokeWidth(1);
             paint.setARGB(255, 255, 255, 255);
             paint.setStyle(Paint.Style.STROKE);
 
<<<<<<< HEAD
             Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
=======
            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher);
>>>>>>> cd4de3b12902a3bc145ca536559af9c23e2c2406
             canvas.drawBitmap(bmp, myScreenCoords.x, myScreenCoords.y, paint);
 
             return true;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.google.android.maps.MapActivity#isRouteDisplayed()
      */
     @Override
     protected boolean isRouteDisplayed()
     {
         // TODO Auto-generated method stub
         return false;
     }
 }
