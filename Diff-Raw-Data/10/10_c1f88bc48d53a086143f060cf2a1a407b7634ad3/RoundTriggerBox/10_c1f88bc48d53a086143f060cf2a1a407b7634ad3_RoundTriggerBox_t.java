 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package info.jeppes.ZoneCore.TriggerBoxes;
 
 import java.awt.geom.Point2D;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 /**
  *
  * @author jeppe
  */
 public class RoundTriggerBox extends TriggerBox{
 
     private Point3D center;
     private double radius;
     
     private double minY;
     private double maxY;
     
     public RoundTriggerBox(Location center, double radius, String name, double height) {
         this(new Point3D(center),center.getWorld().getName(),radius,name,height);
     }
     public RoundTriggerBox(Point3D center, String worldName, double radius, String name, double height) {
         this(center,worldName,radius,name,center.getY() - height / 2,center.getY() + height / 2);
     }
     
     public RoundTriggerBox(Location center, double radius, String name, double minY, double maxY) {
         this(new Point3D(center),center.getWorld().getName(),radius,name,minY,maxY);
     }
     public RoundTriggerBox(Point3D center, String worldName, double radius, String name, double minY, double maxY) {
         this(center,worldName,radius,name,minY,maxY,false,true);
     }
     public RoundTriggerBox(Point3D center, String worldName, double radius, String name, double minY, double maxY, boolean useEvents) {
         this(center,worldName,radius,name,minY,maxY,useEvents,true);
     }
     public RoundTriggerBox(Point3D center, String worldName, double radius, String name, double minY, double maxY, boolean useEvents, boolean triggerByEveryone) {
         super(name,worldName,useEvents,triggerByEveryone);
         this.center = center;
         this.minY = minY;
         this.maxY = maxY;
     }
 
     @Override
     public boolean isInside(Location location) {
         return isInside(new Point3D(location), location.getWorld().getName());
     }
     @Override
     public boolean isInside(Point2D point, double y, String worldName){
         return isInside(new Point3D(point.getX(), y, point.getY()), worldName);
     }
     @Override
     public boolean isInside(Point3D point, String worldName){
         if(point.getY() < minY || maxY < point.getY() || !worldName.equals(getWorldName())){
             return false;
         }
         if(point.getY() >= getCircleBottomY() && point.getY() <= getCircleTopY()){
             double distance = PrecisePoint.distance(
                     point.getX(), point.getZ(), 
                     getCenter().getX(), getCenter().getZ()
                 );
             if(distance <= getRadius()){
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public Location getRandomLocationInsideBox() {
         double radius = Math.random() * getRadius();
         double theta = Math.random() * Math.toRadians(360);
         double x = Math.cos(theta) * radius;
         double z = Math.sin(theta) * radius;
         double y = getCircleBottomY() + (Math.random() * (getCircleTopY() - getCircleBottomY()));
         return new Location(getWorld(),x,y,z);
     }
     
     public Point3D getCenter() {
         return center;
     }
 
     public void setCenter(Location center, String worldName) {
         this.center = new Point3D(center);
         this.setWorld(worldName);
     }
     public void setCenter(Location center) {
         this.center = new Point3D(center);
         this.setWorld(center.getWorld());
     }
     public void setCenter(Point3D center, String worldName) {
         this.center = center;
         this.setWorld(worldName);
     }
 
     public double getRadius() {
         return radius;
     }
 
     public void setRadius(double radius) {
         this.radius = radius;
     }
 
     public double getCircleBottomY() {
         return minY;
     }
 
     public void setCircleBottomY(double startHeight) {
         this.minY = Math.min(startHeight, getCircleTopY());
     }
 
     public double getCircleTopY() {
         return maxY;
     }
 
     public void setCircleTopY(double endHeight) {
         this.minY = Math.min(endHeight, getCircleBottomY());
     }
     
     @Override
     public String toSaveString() {
         StringBuilder saveString = new StringBuilder();
         saveString.append("circle|");
         saveString.append(super.toSaveString()).append("|");
         saveString.append(center.toSaveString()).append("|");
         saveString.append(radius).append(",").append(minY).append(",").append(maxY);
         return saveString.toString();
     }
     
     public static RoundTriggerBox getRoundTriggerBox(String saveString) throws Exception{
         return getRoundTriggerBox(saveString,null);
     }
     public static RoundTriggerBox getRoundTriggerBox(String saveString, TriggerBoxEventHandler eventHandler) throws Exception{
         String[] split = saveString.split("|");
         
         if(!"circle".equals(split[0])){
             throw new Exception("TriggerBox is not a circle, it is a \""+split[0]+"\"");
         }
         //Base triggerbox parameters
         String[] baseTriggerBoxString = split[1].split(",");
         String name = baseTriggerBoxString[0];
         String worldName = baseTriggerBoxString[1];
         boolean useEvents = Boolean.parseBoolean(baseTriggerBoxString[2]);
         boolean triggerByEveryone = Boolean.parseBoolean(baseTriggerBoxString[3]);
         
         Point3D center = Point3D.toPoint3D(split[2]);
         
         String[] roundTriggerBoxString = split[3].split(",");
         double radius = Double.parseDouble(roundTriggerBoxString[0]);
         double minY = Double.parseDouble(roundTriggerBoxString[1]);
         double maxY = Double.parseDouble(roundTriggerBoxString[2]);
         
         RoundTriggerBox roundTriggerBox = new RoundTriggerBox(center, worldName, radius, name, minY, maxY, useEvents, triggerByEveryone);
         roundTriggerBox.setEventHandler(eventHandler);
         return roundTriggerBox;
     }
     
 }
