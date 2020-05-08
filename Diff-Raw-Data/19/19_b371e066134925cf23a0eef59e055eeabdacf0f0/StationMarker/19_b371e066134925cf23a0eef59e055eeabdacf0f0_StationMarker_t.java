 package com.onelightstudio.onebike.model;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.onelightstudio.onebike.R;
 
 /**
  * Created by thomas on 12/09/13.
  */
 public class StationMarker {
 
    public static final float MARKER_ANCHOR_U = 0.3f;
    public static final float MARKER_ANCHOR_V = 1.0f;
 
     private static Bitmap markerBitmap;
     private static Paint paint;
 
     public static MarkerOptions createCluster(LatLngBounds bounds) {
         LatLng center = new LatLng(
                 (bounds.northeast.latitude + bounds.southwest.latitude) / 2,
                 (bounds.northeast.longitude + bounds.southwest.longitude) / 2);
        return new MarkerOptions().position(center).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_station_cluster)).anchor(MARKER_ANCHOR_U, MARKER_ANCHOR_V);
     }
 
     public static MarkerOptions createMarker(Context ctx, Station station) {
         if (station.markerOptions == null) {
             Bitmap bmp = createBitmap(ctx, station);
             // Do not display the 'xxx - ' before the station name
             String title = station.displayName;
             station.markerOptions = new MarkerOptions().position(station.latLng).title(title).icon(BitmapDescriptorFactory.fromBitmap(bmp)).anchor(MARKER_ANCHOR_U, MARKER_ANCHOR_V);
             bmp.recycle();
         }
         return station.markerOptions;
     }
 
     private static Bitmap createBitmap(Context ctx, Station station) {
         Resources res = ctx.getResources();
 
         // load any static object if needed
         if (markerBitmap == null) {
             markerBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_marker_station);
         }
         if (paint == null) {
             paint = new Paint();
             paint.setAntiAlias(true);
             paint.setTextSize(res.getDimensionPixelSize(R.dimen.marker_text_size));
             paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
             paint.setFakeBoldText(true);
             paint.setColor(Color.WHITE);
         }
 
         // get the bitmap size in pixels
         int bmpWidth = res.getDimensionPixelSize(R.dimen.marker_width);
         int bmpHeight = res.getDimensionPixelSize(R.dimen.marker_height);
         int xMinBikes = res.getDimensionPixelSize(R.dimen.marker_text_x_min);
         int xMaxBikes = res.getDimensionPixelSize(R.dimen.marker_text_x_max);
         int yBikes = res.getDimensionPixelSize(R.dimen.marker_text_y_bikes);
         int xMinStands = res.getDimensionPixelSize(R.dimen.marker_text_x_min);
         int xMaxStands = res.getDimensionPixelSize(R.dimen.marker_text_x_max);
         int yStands = res.getDimensionPixelSize(R.dimen.marker_text_y_stands);
 
         Bitmap bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(bmp);
         canvas.drawBitmap(markerBitmap, 0, 0, paint);
         canvas.drawText(String.valueOf(station.availableBikes), station.availableBikes < 10 ? xMaxBikes : xMinBikes, yBikes, paint);
         canvas.drawText(String.valueOf(station.availableBikeStands), station.availableBikeStands < 10 ? xMaxStands : xMinStands, yStands, paint);
 
         return bmp;
     }
 }
