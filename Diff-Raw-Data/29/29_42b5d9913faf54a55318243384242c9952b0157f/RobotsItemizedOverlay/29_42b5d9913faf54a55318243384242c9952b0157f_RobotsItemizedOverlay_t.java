 package com.example.android.apis.overlays;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.graphics.drawable.Drawable;
 
 import com.example.android.apis.model.Robot;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
public class RobotsItemizedOverlay extends ItemizedOverlay<OverlayItem> {
     private ArrayList<Robot> mOverlays = new ArrayList<Robot>();
 
     public RobotsItemizedOverlay(Drawable defaultMarker) {
         super(boundCenterBottom(defaultMarker));
     }
 
     public void addRobot(Robot robot) {
         mOverlays.add(robot);
         populate();
     }
 
     @Override
     protected boolean onTap(int index) {
         Random rand = new Random();
         android.util.Log.d("RobotsItemizedOverlay", "Item tapped. Id: " + index
                 + " Total items: " + size());
         Robot oldRobot = mOverlays.get(index);
         GeoPoint oldLocation = oldRobot.getLocation();
         int lat = (int) (oldLocation.getLatitudeE6() + 500 * rand.nextGaussian());
         int lon = (int) (oldLocation.getLongitudeE6() + 500 * rand.nextGaussian());
         mOverlays.set(index, new Robot(new GeoPoint(lat, lon)));
         populate();
         return true;
     }
 
     @Override
    protected OverlayItem createItem(int i) {
        Robot r = mOverlays.get(i);
        return new OverlayItem(r.getLocation(), "", "");
     }
 
     @Override
     public int size() {
         return mOverlays.size();
     }
 }
