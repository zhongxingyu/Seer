 package com.project4;
 
 import java.util.Stack;
 
 import processing.core.PApplet;
 import processing.core.PImage;
 
 import com.anotherbrick.inthewall.EventSubscriber;
 import com.anotherbrick.inthewall.TouchEnabled;
 import com.anotherbrick.inthewall.VizNotificationCenter.EventName;
 import com.anotherbrick.inthewall.VizPanel;
 
 public class Map extends VizPanel implements TouchEnabled, EventSubscriber {
 
   static final double MAX_LAT = 42.30146f;
   static final double MIN_LAT = 42.16198f;
   static final double MAX_LON = 93.56731f;
   static final double MIN_LON = 93.19066f;
 
   private double maxLat;
   private double minLat;
   private double maxLon;
   private double minLon;
 
   private PImage mapImage;
   private MapScatter mapScatter;
   private MapZoomButtons mapZoomButtons;
   private Stack<Double> zoomStackLon = new Stack<Double>();
   private Stack<Double> zoomStackLat = new Stack<Double>();
   private MapPanButtons mapPanButtons;
 
   public Map(float x0, float y0, float width, float height, VizPanel parent) {
     super(x0, y0, width, height, parent);
   }
 
   @Override
   public void eventReceived(EventName eventName, Object data) {
     if (eventName == EventName.BUTTON_TOUCHED) {
       manageButtons(data.toString());
     }
   }
 
   private void manageButtons(String buttonName) {
     if (buttonName.equals("zoomInButton")) {
       zoomIn();
     } else if (buttonName.equals("zoomOutButton")) {
       zoomOut();
     } else if (buttonName.equals("panUpButton")) {
       panUp();
     } else if (buttonName.equals("panDownButton")) {
       panDown();
     } else if (buttonName.equals("panRightButton")) {
       panRight();
     } else if (buttonName.equals("panLeftButton")) {
       panLeft();
     }
   }
 
 
 
   @Override
   public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
     return propagateTouch(x, y, down, touchType);
   }
 
   @Override
   public void setup() {
     maxLat = MAX_LAT;
     minLat = MIN_LAT;
     maxLon = MAX_LON;
     minLon = MIN_LON;
     mapImage = c.getImage("map", "png");
     setupMapZoomButtons();
     setupMapPanButtons();
     setupMapScatter();
     m.notificationCenter.registerToEvent(EventName.BUTTON_TOUCHED, this);
   }
 
   private void setupMapZoomButtons() {
     mapZoomButtons = new MapZoomButtons(getWidth() - 60, getHeight() - 60, this);
     addTouchSubscriber(mapZoomButtons);
     mapZoomButtons.setup();
   }
 
   private void setupMapPanButtons() {
     mapPanButtons = new MapPanButtons(getWidth() - 80, getHeight() - 140, this);
     addTouchSubscriber(mapPanButtons);
     mapPanButtons.setup();
   }
 
   private void setupMapScatter() {
     mapScatter = new MapScatter(0, 0, getWidth(), getHeight(), this);
     mapScatter.setup();
     addTouchSubscriber(mapScatter);
   }
 
   @Override
   public boolean draw() {
     drawImage();
     mapScatter.draw();
     mapZoomButtons.draw();
     mapPanButtons.draw();
     return false;
   }
 
   private void drawImage() {
     double zoom = (MAX_LON - MIN_LON) / (maxLon - minLon);
     double width = zoom * getWidth();
     double height = zoom * getHeight();
     double offsetLon = ((minLon - MIN_LON) / (MAX_LON - MIN_LON)) * width;
     double offsetLat = ((minLat - MIN_LAT) / (MAX_LAT - MIN_LAT)) * height;
     image(mapImage, (float) -offsetLon, (float) -offsetLat, (float) width, (float) height);
   }
 
   private void zoomIn() {
     double lon = (maxLon - minLon) / 7;
     double lat = (maxLat - minLat) / 7;
     zoomStackLon.add(lon);
     zoomStackLat.add(lat);
     minLon += lon;
     maxLon -= lon;
     minLat += lat;
     maxLat -= lat;
   }
 
   private double getPanOffset() {
     return (maxLat - minLat) / 10;
   }
 
   private void panUp() {
     double offset = Math.min(minLat - MIN_LAT, getPanOffset());
     minLat -= offset;
     maxLat -= offset;
   }
 
   private void panDown() {
     double offset = Math.min(MAX_LAT - maxLat, getPanOffset());
     minLat += offset;
     maxLat += offset;
   }
 
   private void panLeft() {
     double offset = Math.min(Math.abs(MIN_LON - minLon), getPanOffset());
     minLon -= offset;
     maxLon -= offset;
   }
 
   private void panRight() {
     double offset = Math.min(Math.abs(maxLon - MAX_LON), getPanOffset());
     minLon += offset;
     maxLon += offset;
   }
 
   private void zoomOut() {
     if (zoomStackLat.size() == 0) {
       minLon = MIN_LON;
       minLat = MIN_LAT;
       maxLon = MAX_LON;
       maxLat = MAX_LAT;
       return;
     }
     double lat = zoomStackLat.pop();
     double lon = zoomStackLon.pop();
     minLon -= lon;
     maxLon += lon;
     minLat -= lat;
     maxLat += lat;
    repositionMap();
  }

  private void repositionMap() {
     // fix errors
     if (minLon < MIN_LON) {
      double val = MIN_LON - minLon;
      minLon += val;
      maxLon += val;
     } else if (maxLon > MAX_LON) {
      double val = MAX_LON - maxLon;
      minLon -= val;
      maxLon -= val;
     }
     if (minLat < MIN_LAT) {
      double val = MIN_LAT - minLat;
      minLat += val;
      maxLat += val;
     } else if (maxLat > MAX_LAT) {
      double val = maxLat - MAX_LAT;
      minLat -= val;
      maxLat -= val;
     }
   }
 
   public double getMaxLat() {
     return maxLat;
   }
 
   public double getMinLat() {
     return minLat;
   }
 
   public double getMaxLon() {
     return maxLon;
   }
 
   public double getMinLon() {
     return minLon;
   }
 
 
 }
