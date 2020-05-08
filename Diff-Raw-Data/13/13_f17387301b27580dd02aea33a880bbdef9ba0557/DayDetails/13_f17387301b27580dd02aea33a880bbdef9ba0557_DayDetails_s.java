 package com.project4.dayview;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.TreeMap;
 
 import processing.core.PApplet;
 import processing.core.PImage;
 import processing.core.PShape;
 
 import com.anotherbrick.inthewall.EventSubscriber;
 import com.anotherbrick.inthewall.TouchEnabled;
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 import com.anotherbrick.inthewall.VizNotificationCenter.EventName;
 import com.anotherbrick.inthewall.VizPanel;
 import com.project4.datasource.Day;
 import com.project4.datasource.Filter;
 import com.project4.datasource.Day.WeatherEnum;
 
 public class DayDetails extends VizPanel implements TouchEnabled, EventSubscriber {
 
   public final static float width = 100;
   public final static float height = 120;
   private int dayNumber;
   public float startX;
   public float endX;
   private Day day;
   private ArrayList<Day> days;
   private HashMap<String, PImage> weatherImages;
   private PImage windImage;
 
   public DayDetails(float x0, float y0, VizPanel parent) {
     super(x0, y0, width, height, parent);
   }
 
   @Override
   public void setup() {
     m.notificationCenter.registerToEvent(EventName.DAY_DESELECTED, this);
     m.notificationCenter.registerToEvent(EventName.DAY_SELECTED, this);
     m.notificationCenter.registerToEvent(EventName.DAYS_UPDATED, this);
     weatherImages = new HashMap<String, PImage>();
     for (String name : new String[] {"clear", "cloudy", "rain", "showers"}) {
       weatherImages.put(name, c.getImage("weather/" + name, "png"));
     }
     windImage = c.getImage("weather/wind", "png");
     setVisible(false);
   }
 
   @Override
   public boolean draw() {
     if (!isVisible()) return false;
     drawBackground();
     drawWeather();
     drawCounts();
     text(day.getDay() + " - " + day.getWindSpeed(), getWidth() / 2, getHeight() / 2);
     return false;
   }
 
   private void drawCounts() {
     pushStyle();
     TreeMap<Filter, Integer> counts = day.getCounts();
     textAlign(PApplet.LEFT, PApplet.TOP);
     int i = 0;
     float x = 10;
     float y = 40;
     for (Filter filter : day.getFilters()) {
       Integer count = counts.get(filter);
       fill(filter.getColor());
       rect(x, y + i, 15, 15);
       fill(MyColorEnum.WHITE);
       text(count + "", x + 30, y + i);
       i += 20;
     }
     popStyle();
   }
 
   private void drawWeather() {
     pushStyle();
     image(weatherImages.get(day.getWeather().toString().toLowerCase()), 10, 10);
     pushMatrix();
     imageMode(PApplet.CENTER);
    translate(getX0Absolute() + 80 + windImage.width / 2.0f, getY0Absolute() + 7 + windImage.height
        / 2.0f);
     rotate(radians(45 - day.getWindDirection()));
     m.p.image(windImage, 0, 0);
     popMatrix();
     text(day.getWindSpeed() + "mph", 40, 10);
     popStyle();
   }
 
   private void drawBackground() {
     noStroke();
     fill(MyColorEnum.LIGHT_BLUE, 200f);
     beginShape();
     vertex(getWidth() / 2 - 10, getHeight() - 20);
     vertex(getWidth() / 2 + 10, getHeight() - 20);
     vertex(getWidth() / 2, getHeight());
     endShape(PApplet.CLOSE);
     rect(0, 0, getWidth(), getHeight() - 20, 10, 10, 10, 10);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public void eventReceived(EventName eventName, Object data) {
     if (eventName == EventName.DAY_SELECTED) {
       dayNumber = (Integer) data;
       modifyPosition(map(dayNumber, 0, 20, startX, endX), getY0());
       day = days.get(dayNumber);
       this.setVisible(true);
     }
     if (eventName == EventName.DAY_DESELECTED) {
       this.setVisible(false);
     }
     if (eventName == EventName.DAYS_UPDATED) {
       days = (ArrayList<Day>) data;
     }
   }
 
   @Override
   public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
     return propagateTouch(x, y, down, touchType);
   }
 
 }
