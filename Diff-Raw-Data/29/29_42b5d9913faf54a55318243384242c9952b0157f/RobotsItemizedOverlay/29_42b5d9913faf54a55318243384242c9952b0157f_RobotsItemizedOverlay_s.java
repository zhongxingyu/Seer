 package com.example.android.apis.overlays;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.graphics.drawable.Drawable;
 
 import com.example.android.apis.model.Robot;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
public class RobotsItemizedOverlay extends ItemizedOverlay {
     private ArrayList<Robot> mOverlays = new ArrayList<Robot>();
 
     public RobotsItemizedOverlay(Drawable defaultMarker) {
         super(boundCenterBottom(defaultMarker));
        // TODO Auto-generated constructor stub
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
    protected RobotOverlayItem createItem(int i) {
        return new RobotOverlayItem(mOverlays.get(i));
     }
 
     @Override
     public int size() {
         return mOverlays.size();
     }
    
    private class RobotOverlayItem extends OverlayItem {
        private Robot robot;

        public RobotOverlayItem(Robot robot) {
            super(robot.getLocation(), "", "");
            this.robot = robot;
        }
        
    }

 }
