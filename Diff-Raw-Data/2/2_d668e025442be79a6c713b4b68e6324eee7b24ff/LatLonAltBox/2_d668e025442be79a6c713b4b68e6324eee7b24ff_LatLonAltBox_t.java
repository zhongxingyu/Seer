 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.vesalainen.mailblog;
 
 import com.google.appengine.api.datastore.GeoPt;
 import net.opengis.kml.LatLonAltBoxType;
 
 /**
  * @author Timo Vesalainen
  */
 public class LatLonAltBox
 {
     private static final double HalfCircle = 180;
     private static final double FullCircle = 360;
     private boolean init;
     private double north;
     private double south;
     private double west;
     private double east;
 
     public LatLonAltBox()
     {
     }
     
     /**
      * 
      * @param center Center location
      * @param dia diameter in NM
      */
     public LatLonAltBox(GeoPt center, double dia)
     {
         this(center.getLatitude(), center.getLongitude(), dia);
     }
     /**
      * 
      * @param latitude
      * @param longitude
      * @param dia diameter in NM
      */
     public LatLonAltBox(double latitude, double longitude, double dia)
     {
         dia = dia / 60;    // 60 NM
         north = normalize(latitude+dia);
         south = normalize(latitude-dia);
         west = normalize(longitude-dia);
         east = normalize(longitude+dia);
         init = true;
     }
 
     public void add(GeoPt location)
     {
         add(location.getLatitude(), location.getLongitude());
     }
     /**
      * @param latitude
      * @param longitude 
      */
     public void add(double latitude, double longitude)
     {
         assert latitude >= -90;
         assert latitude <= 90;
         assert longitude >= -180;
         assert longitude <= 180;
         if (init)
         {
             north = Math.max(north, latitude);
             south = Math.min(south, latitude);
 
             east = isWestToEast(east, longitude) ? longitude : east;
             west = !isWestToEast(west, longitude) ? longitude : west;
         }
         else
         {
             north = latitude;
             south = latitude;
             west = longitude;
             east = longitude;
             init = true;
         }
     }
     public void clear()
     {
         north = 0;
         south = 0;
         west = 0;
         east = 0;
         init = false;
     }
     /**
      * Return area in square degrees
      * @return 
      */
     public double getArea()
     {
         double abs = Math.abs(east-west);
         if (abs > HalfCircle)
         {
             abs = FullCircle - abs;
         }
        return abs*(north-south)*Math.cos(Math.toRadians((north+south)/2));
     }
     public GeoPt getSouthWest()
     {
         return new GeoPt((float)south, (float)west);
     }
     public GeoPt getNorthEast()
     {
         return new GeoPt((float)north, (float)east);
     }
     private static final double normalize(double val)
     {
         return ((val + HalfCircle + FullCircle) % FullCircle) - HalfCircle;
     }
     /**
      * Return true if a2 right of a1
      * @param west
      * @param east
      * @return 
      */
     private static final boolean isWestToEast(double west, double east)
     {
         west += HalfCircle;
         east += HalfCircle;
         double d = east-west;
         if (Math.abs(d) <= HalfCircle)
         {
             return d >= 0;
         }
         else
         {
             return d < 0;
         }
     }
     public void populate(LatLonAltBoxType box)
     {
         box.setNorth(north);
         box.setSouth(south);
         box.setWest(west);
         box.setEast(east);
     }
 }
