 /*
  *  Copyright (C) 2011 Inferior Human Organs Software
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.inferiorhumanorgans.WayToGo.ListAdapter;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.inferiorhumanorgans.WayToGo.R;
 import com.inferiorhumanorgans.WayToGo.TheApp;
 import com.inferiorhumanorgans.WayToGo.Util.Stop;
 import java.util.ArrayList;
 import org.osmdroid.util.GeoPoint;
 
 /**
  *
  * @author alex
  */
 public class StopAdapter extends ArrayAdapter<Stop> {
 
     private ArrayList<Stop> theItems;
     private Context theContext = null;
     private Location theLocation;
     private double[] theDistances;
     private int theHighlightPosition = Integer.MIN_VALUE;
 
     public StopAdapter(Context aContext) {
         this(aContext, new ArrayList<Stop>());
     }
 
     public StopAdapter(Context aContext, ArrayList<Stop> someItems) {
         super(aContext, R.layout.list_item_text, someItems);
         theItems = someItems;
         theContext = aContext;
     }
 
     public void setTheLocation(Location aLocation) {
         theLocation = aLocation;
         theDistances = new double[theItems.size()];
         if (theLocation != null) {
             for (int i = 0; i < theDistances.length; i++) {
                 final double ourLatitude = theLocation.getLatitude();
                 final double ourLongitude = theLocation.getLongitude();
                 final float[] ourDistance = new float[1];
                 final GeoPoint ourPoint = theItems.get(i).point();
                 Location.distanceBetween(ourLatitude, ourLongitude, ourPoint.getLatitudeE6() / 1E6, ourPoint.getLongitudeE6() / 1E6, ourDistance);
                 theDistances[i] = ourDistance[0];
             }
         } else {
             for (int i = 0; i < theDistances.length; i++) {
                 theDistances[i] = Double.MIN_VALUE;
             }
         }
         notifyDataSetChanged();
     }
 
     /**
      *
     * @return The index of the closest stop, or Integer.MAX_VALUE if one can't be found
      */
     public int highlightClosest() {
         int ret = Integer.MIN_VALUE;
         double ourLastDistance = Double.MAX_VALUE;
         for (int i=0; i < theDistances.length; i++) {
             if ((theDistances[i] < ourLastDistance) && (theDistances[i] != Double.MIN_VALUE)) {
                 ourLastDistance = theDistances[i];
                 ret = i;
             }
         }
         theHighlightPosition = ret;
         notifyDataSetChanged();
         return ret;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         RelativeLayout v;
         if (convertView == null) {
             final LayoutInflater inflater = (LayoutInflater) theContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = (RelativeLayout) inflater.inflate(R.layout.list_item_stop, null);
             v.setFocusable(false);
             v.setFocusableInTouchMode(false);
             v.setClickable(false);
             //v.setLinksClickable(false);
         } else {
             v = (RelativeLayout) convertView;
         }
 
         final TextView theLeft = (TextView) v.findViewById(R.id.list_item_stop);
         final TextView theRight = (TextView) v.findViewById(R.id.list_item_distance);
         final Stop ourStop = theItems.get(position);
 
         theLeft.setText(ourStop.shortName());
         if (theHighlightPosition == position) {
             theLeft.setBackgroundColor(Color.WHITE);
             theRight.setBackgroundColor(Color.WHITE);
             theLeft.setTextColor(Color.BLACK);
             theRight.setTextColor(Color.BLACK);
         } else {
             theLeft.setBackgroundColor(Color.TRANSPARENT);
             theRight.setBackgroundColor(Color.TRANSPARENT);
             theLeft.setTextColor(Color.WHITE);
             theRight.setTextColor(Color.WHITE);
         }
 
         if ((theDistances != null) && (theDistances.length == theItems.size()) && theDistances[position] != Double.MIN_VALUE) {
             theRight.setText(TheApp.formatDistance(theDistances[position]));
         } else {
             theRight.setText("");
         }
         return v;
     }
 }
