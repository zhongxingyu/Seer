 package info.jeppes.ZoneCore.TriggerBoxes;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 
 public class PolygonTriggerBox extends TriggerBox{
 
     private ArrayList<Point2D> polygon = new ArrayList();
     private double minY;
     private double maxY;
     private double radius = 0;
     private PrecisePoint simpleCentroid = null;
     
     public PolygonTriggerBox(ArrayList<Location> polygon, String name) throws Exception{
         super(name,polygon.get(0).getWorld().getName());
         if(!isLocationsInSameWorld(polygon)){
             throw new Exception("Some locations are not in the same world");
         }
         this.setPolygon(getListFromLocationArray(polygon), true);
     }
     public PolygonTriggerBox(ArrayList<Point2D> polygon, String name, String WorldName){
         super(name,WorldName);
         this.setPolygon(polygon, true);
     }
     public PolygonTriggerBox(ArrayList<Location> polygon, String name, double minY, double maxY) throws Exception{
         super(name,polygon.get(0).getWorld().getName());
         if(!isLocationsInSameWorld(polygon)){
             throw new Exception("Some locations are not in the same world");
         }
         this.setPolygon(getListFromLocationArray(polygon), false);
         this.minY = minY;
         this.maxY = maxY;
         this.recalculateRadiusAndCentroid();
     }
     public PolygonTriggerBox(ArrayList<Point2D> polygon, String name, String WorldName, double minY, double maxY){
         super(name,WorldName);
         this.setPolygon(polygon, false);
         this.minY = minY;
         this.maxY = maxY;
         this.recalculateRadiusAndCentroid();
     }
     public PolygonTriggerBox(ArrayList<Point2D> polygon, String name, String WorldName, double minY, double maxY, boolean useEvents){
         super(name,WorldName,useEvents);
         this.setPolygon(polygon, false);
         this.minY = minY;
         this.maxY = maxY;
         this.recalculateRadiusAndCentroid();
     }
     public PolygonTriggerBox(ArrayList<Point2D> polygon, String name, String WorldName, double minY, double maxY, boolean useEvents, boolean triggerByEveryone){
         super(name,WorldName,useEvents,triggerByEveryone);
         this.setPolygon(polygon, false);
         this.minY = minY;
         this.maxY = maxY;
         this.recalculateRadiusAndCentroid();
     }
     
     // return getArea of polygon
     public double getArea() { return Math.abs(getSignedArea()); }
 
     // return signed getArea of polygon
     public double getSignedArea() {
         double sum = 0.0;
         for (int i = 0; i < polygon.size()-1; i++) {
             sum = sum + (polygon.get(i).getX() * polygon.get(i+1).getY()) - (polygon.get(i).getY() * polygon.get(i+1).getX());
         }
         return 0.5 * sum;
     }
     
     private void recalculateMinAndMaxY(){
         boolean first = true;
         for(Point2D location : polygon){
             if(first){
                 first = false;
                 minY = location.getY();
                 maxY = location.getY();
                 continue;
             }
             minY = Math.min(minY, location.getY());
             maxY = Math.max(maxY, location.getY());
         }
     }
     
     private void recalculateRadiusAndCentroid(){
         double longestDiameter = 0;
         Point2D usedPoint1 = null;
         Point2D usedPoint2 = null;
         for(Point2D point : polygon){
             for(Point2D point2 : polygon){
                 double distance = point.distance(point2);
                 if(distance > longestDiameter){
                     longestDiameter = distance;
                     usedPoint1 = point;
                     usedPoint2 = point2;
                 }
             }
         }
         radius = longestDiameter/2;
         double angle = PrecisePoint.angleFrom(usedPoint1, usedPoint2);
         simpleCentroid = new PrecisePoint(usedPoint1.getX() + (Math.cos(angle) * radius),usedPoint1.getY() + (Math.sin(angle) * radius));
     }
     
     private boolean isLocationsInSameWorld(ArrayList<Location> polygon){
         for(Location location : polygon){
             if(!location.getWorld().getName().equals(getWorldName())){
                 return false;
             }
         }
         return true;
     }
     
     public static ArrayList<Point2D> getListFromLocationArray(ArrayList<Location> polygon){
         ArrayList<Point2D> tempPolygon = new ArrayList();
         for(Location location : polygon){
             tempPolygon.add(new PrecisePoint(location.getX(), location.getBlockZ()));
         }
         return tempPolygon;
     }
     
     public ArrayList<Point2D> getPolygon(){
         return (ArrayList<Point2D>)polygon.clone();
     }
     
     private void setPolygon(ArrayList<Point2D> polygon, boolean recalculate){
         this.polygon = polygon;
         if(recalculate){
             recalculateRadiusAndCentroid();
             recalculateMinAndMaxY();
         }
     }
     
     public void setPolygon(ArrayList<Point2D> polygon){
         setPolygon(polygon,true);
     }
     
     public void setPolygonWithLocation(ArrayList<Location> polygon) throws Exception{
         if(!isLocationsInSameWorld(polygon)){
             throw new Exception("Some locations are not in the same world");
         }
         setPolygonWithLocation(polygon,polygon.get(0).getWorld());
     }  
     public void setPolygonWithLocation(ArrayList<Location> polygon, World world){
         setPolygonWithLocation(polygon,world.getName());
     }  
     public void setPolygonWithLocation(ArrayList<Location> polygon, String worldName){
         setWorld(worldName);
         setPolygon(getListFromLocationArray(polygon),true);
     }
     
     public double getRadius() {
         return radius;
     }
     public Point2D getSimpleCentroid(){
         return simpleCentroid.clone();
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
         //fast and easy check to see if the location is not inside the box
         if(point.getY() < minY || maxY < point.getY() || !worldName.equals(getWorldName())){
             return false;
         }
         boolean result = false;
         for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
             if ((polygon.get(i).getY() > point.getZ()) != (polygon.get(j).getY() > point.getZ()) &&
                 (point.getX() < (polygon.get(j).getX() - polygon.get(i).getX()) * (point.getZ() - polygon.get(i).getY()) / (polygon.get(j).getY()-polygon.get(i).getY()) + polygon.get(i).getX())) {
                 result = !result;
             }
         }
         return result;
     }
 
     @Override
     public Location getRandomLocationInsideBox() {
         Point2D simpleCentroid1 = getSimpleCentroid();
         for(int i = 0; i < 100; i++){
             double randomAngle = Math.random() * Math.PI * 2;
             double randomRadius = Math.random() * getRadius();
             double x = Math.cos(randomAngle) * randomRadius + simpleCentroid1.getX();
             double z = Math.sin(randomAngle) * randomRadius + simpleCentroid1.getY();
             double y = minY + Math.random() * (maxY - minY);
             Location location = new Location(getWorld(),x,y,z);
             if(isInside(location)){
                 return location;
             }
         }
         //in case it does not find a random point inside the polygon within 
         //100 tries, it will return the simple centroid
         return new Location(
                 getWorld(),                             //world
                 simpleCentroid.getX(),                  //X
                 minY + Math.random() * (maxY - minY),   //Y
                 simpleCentroid.getY());                 //Z
     }
     public Location getRandomLocationInsideBoxOnGround(){
         Location location = null;
         for(int i = 0; i < 100; i++){
             location = getRandomLocationInsideBox();
             ArrayList<Integer> possibleY = new ArrayList();
 
             boolean lastBlockEmpty = false;
             for(int y = (int) minY; y < maxY; y++){
                 int blockTypeIdAt = getWorld().getBlockTypeIdAt(location.getBlockX(), y, location.getBlockZ());
                 if(blockTypeIdAt == 0){
                     if(!lastBlockEmpty){
                         possibleY.add(y);
                     }
                     lastBlockEmpty = true;
                 } else {
                     lastBlockEmpty = false;
                 }
             }
             if(!possibleY.isEmpty()){
                 location.setY(possibleY.get((int)(Math.random() * possibleY.size())));
                 return location;
             }
         }
         return location;
     }
     
     @Override
     public String toSaveString() {
         StringBuilder saveString = new StringBuilder();
         saveString.append("polygon|");
         saveString.append(super.toSaveString()).append("|");
         boolean first = true;
         for(Point2D point : this.polygon){
             if(!first){
                 saveString.append("_");
             } else {
                 first = false;
             }
             saveString.append(PrecisePoint.toSaveString(point));
         }
         saveString.append("|");
         saveString.append(minY).append(",").append(maxY);
         return saveString.toString();
     }
     
     public static PolygonTriggerBox getPolygonTriggerBox(String saveString) throws Exception{
         return getPolygonTriggerBox(saveString,null);
     }
     public static PolygonTriggerBox getPolygonTriggerBox(String saveString, TriggerBoxEventHandler eventHandler) throws Exception{
        String[] split = saveString.split("|");
         
         if(!"polygon".equals(split[0])){
             throw new Exception("TriggerBox is not a polygon, it is a \""+split[0]+"\"");
         }
         
         //Base triggerbox parameters
         String[] baseTriggerBoxString = split[1].split(",");
         String name = baseTriggerBoxString[0];
         String worldName = baseTriggerBoxString[1];
         boolean useEvents = Boolean.parseBoolean(baseTriggerBoxString[2]);
         boolean triggerByEveryone = Boolean.parseBoolean(baseTriggerBoxString[3]);
         
         ArrayList<Point2D> polygon = new ArrayList();
         for(String precisePointString : split[2].split("_")){
             polygon.add(PrecisePoint.toPrecisePoint(precisePointString));
         }
         
         String[] polygonTriggerBoxString = split[3].split(",");;
         double minY = Double.parseDouble(polygonTriggerBoxString[0]);
         double maxY = Double.parseDouble(polygonTriggerBoxString[1]);
         
         PolygonTriggerBox polygonTriggerBox = new PolygonTriggerBox(polygon, worldName, name, minY, maxY, useEvents, triggerByEveryone);
         polygonTriggerBox.setEventHandler(eventHandler);
         return polygonTriggerBox;
     }
 }
