 package com.maptime.maptime;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class NavOverlay extends Overlay {
 
     private ArrayList<GeoPoint> navGPs; //Local list of GeoPoints in the route
     private ArrayList<Point> navPoints = new ArrayList<Point>();; //List of Points used when drawing the map
     private double length; //length of the route
     private boolean isFullyCreated = false; //Has the constructor finished
     MainActivity ma;
     
     /**
      * Standard constructor
      * @param gp1 Start of Route
      * @param gp2 End of Route
      */
     
     public NavOverlay(GeoPoint gp1, GeoPoint gp2, MainActivity a) throws NoRouteException{
     	ma = a;
         navGPs = new ArrayList<GeoPoint>();
         navGPs.add(gp1); //add start point
         try {
         	URL url = makeURL(gp1,gp2);
         	URLConnection urlC = url.openConnection();
        	urlC.setReadTimeout(5000);
         	urlC.addRequestProperty("X-Yours-client", "MapTime"); //The YOURS API requests an extra header when using their servers
         	BufferedReader in = new BufferedReader(new InputStreamReader(urlC.getInputStream()));
 			String str;
 			String[] gpCouple;
 			while ((str = in.readLine()) != null) { //Maybe switch to a javax parser at some point?
 				// str is one line of text; readLine() strips the newline character(s)
 				if (str.trim().startsWith("<dista")) { //route length is in <distance> tags. If it's 0 it means a route was unable to be found
 					str = str.trim().substring(10);
 					String[] strTemp = str.split("<");
 					length = Double.parseDouble(strTemp[0]);
 					if (length == 0.0) {
 						throw new NoRouteException();
 					}
 				}
 				else if (str.trim().startsWith("<coordi")) { //marks start of navigation coordinates
 					gpCouple = str.trim().substring(14).split(","); //trims the <coordinate> tag, splits on the , between lat and long
 					navGPs.add(new GeoPoint((int)(Double.valueOf(gpCouple[1])*(double)1000000.0), //list is in long,lat and degrees
 							(int)(Double.valueOf(gpCouple[0])*(double)1000000.0)));				  //GeoPoint wants lat,long and microdegrees
 					str = in.readLine();
 					while (!str.trim().startsWith("</")) { //second while loop takes us to end of <coordinates>
 						gpCouple = str.trim().split(",");
 						navGPs.add(new GeoPoint((int)(Double.valueOf(gpCouple[1])*(double)1000000.0),
 								(int)(Double.valueOf(gpCouple[0])*(double)1000000.0)));
 						str = in.readLine();
 					}
 				}
 				
 			}
 			in.close();
         } catch (MalformedURLException e) {
         } catch (IOException e) {
        	ma.runOnUiThread(new Runnable() {
				public void run() {
					AlertDialog.Builder dialog = new AlertDialog.Builder(ma);
					dialog.setTitle("Navigation Server Error");
					dialog.setMessage("There was an error when contacting the Navigation Server");
					dialog.setNeutralButton("OK", new DismissListener());
					dialog.show();
				}
			});
 		} catch (Exception e) {
 			ma.runOnUiThread(new Runnable() {
 				public void run() {
 					AlertDialog.Builder dialog = new AlertDialog.Builder(ma);
 					dialog.setTitle("Navigation Server Error");
 					dialog.setMessage("There was an error when contacting the Navigation Server");
					dialog.setNeutralButton("OK", new DismissListener());
 					dialog.show();
 				}
 			});
 		}
         navGPs.add(gp2); //add end point
         length = 0.0; //null length again so we can work it out for ourselves (length supplied does not match our needs 
         for (int i = 0; i < navGPs.size()-1; i++) { // when working out where each TimePoint goes)
         	length += MainActivity.distanceKm((double)(navGPs.get(i).getLatitudeE6())/(double)1000000.0,
 					(double)(navGPs.get(i).getLongitudeE6())/(double)1000000.0,
 					(double)(navGPs.get(i+1).getLatitudeE6())/(double)1000000.0,
 					(double)(navGPs.get(i+1).getLongitudeE6())/(double)1000000.0);
         }
         isFullyCreated = true;
     }
 
     /**
      * Constructor used when the list of GeoPoints and their length are already available
      * @param gps List of Geopoints that make up the route
      * @param routeLength Length of the route
      */
     
     public NavOverlay(ArrayList<ParcelableGeoPoint> gps, double routeLength, MainActivity a) {
     	ma = a;
     	ArrayList<GeoPoint> geopoints = new ArrayList<GeoPoint>();
     	for (ParcelableGeoPoint pgp : gps) {
     		geopoints.add(new GeoPoint(pgp.getLatitudeE6(), pgp.getLongitudeE6()));
     	}
     	navGPs = geopoints;
     	length = routeLength;
     	isFullyCreated = true;
     }
     
     /**
      * Creates a suitable URL for getting navigation data from the YOURS API
      * {@link http://wiki.openstreetmap.org/wiki/YOURS}
      * @param g1 Start point of the route
      * @param g2 End point of the route
      * @return The URL for retrieving the data
      * @throws MalformedURLException
      */
     
     private URL makeURL (GeoPoint g1, GeoPoint g2) throws MalformedURLException { //using YOURS example server for navigation for now
     	
     	return new URL("http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat="+
     			(double)((double)g1.getLatitudeE6()/1000000.0)+ //flat/flon are start latitude and longitude
     			"&flon="+(double)((double)g1.getLongitudeE6()/1000000.0)+
     			"&tlat="+(double)((double)g2.getLatitudeE6()/1000000.0)+ //tlat/tlon are dest longi/lati
     			"&tlon="+(double)((double)g2.getLongitudeE6()/1000000.0)+
     			"&v=motorcar&fast=1&layer=mapnik"); //rest of URL is static, at least until this service goes down
     } //http://wiki.openstreetmap.org/wiki/YOURS
     
     @Override
     public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
             long when) {
 
         Projection projection = mapView.getProjection();
         if (shadow == false) {
 
         	navPoints.clear();
             Paint paint = new Paint();
             paint.setAntiAlias(true);
             paint.setColor(Color.BLUE);
             paint.setStrokeWidth(2);
             for (int i = 0; i < navGPs.size(); i++) { //For every adjacent pair of GeoPoints, draw a line between them
             	navPoints.add(new Point());
             	projection.toPixels(navGPs.get(i), navPoints.get(i));
             }
             for (int i = 0; i < navPoints.size()-1; i++) {
             	canvas.drawLine((float) navPoints.get(i).x, (float) navPoints.get(i).y, (float) navPoints.get(i+1).x,
                         (float) navPoints.get(i+1).y, paint);
             }
         }
         return super.draw(canvas, mapView, shadow, when);
     }
 
     /**
      * Getter for length
      * @return The length of the route in kilometres (km)
      */
     
     public double getLength() {
 		return length;
 	}
     
     /**
      * Getter for isFullyCreated
      * @return true if the constructor has finished running, false otherwise
      */
     
     public boolean isCreated() {
     	return isFullyCreated;
     }
     
     /**
      * Getter for navGPs
      * @return The local List of GeoPoints
      */
     
     public ArrayList<GeoPoint> getNavGPs() {
 		return navGPs;
 	}
     
     @Override
     public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 
         super.draw(canvas, mapView, shadow);
     }
 
 }
