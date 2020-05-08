 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package map.factory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 import map.component.Cavern;
 import map.component.Corridor;
 import map.component.MapComponent;
 import map.component.Maze;
 import map.component.OvalRoom;
 import map.component.RectangleRoom;
 import model.Coordinate;
 import objects.ViewablePixel;
 import objects.Wall;
 import objects.Way;
 
 /**
  *
  * @author hoanggia
  */
 public class MapGenerator {
 
     private static final int MAX_SIDE_OF_COMPONENT = 52;
     private static final int MIN_SIDE_OF_COMPONENT = 7;
     private static final int MAX_DISTANCE_BETWEEN_MAP_COMPONENT = 5;
     private Map<Coordinate, ViewablePixel> pixelsMap;
     private List<MapComponent> mapComponents;
 
     public MapGenerator() {
         pixelsMap = new TreeMap<Coordinate, ViewablePixel>();
         mapComponents = new ArrayList<MapComponent>();
     }
 
     public Map<Coordinate, ViewablePixel> generateMap() {
         Random r = new Random();
        int numberOfComponent = r.nextInt(5);
         for (int i = 0; i < numberOfComponent; i++) {
             MapComponent component = getRandomMapComponent();
             System.out.println(component.getClass().getName());
             Coordinate c;
             if (i != 0) {
                 System.out.println(component.getViewablePixels().keySet());
                 c = generateRandomCoordinateNextToComponent(mapComponents.get(i-1));
                 shiftAllCoordinateTo(component, c);
                 System.out.println(component.getViewablePixels().keySet());
                 System.out.println(pixelsMap.keySet());
 
                 Coordinate[] points = getNearestWayPoints(pixelsMap, component.getViewablePixels());
                 MapComponent corridor = new Corridor(points[0], points[1]);
                 System.out.println(points[0]+" " +points[1]);
                 pixelsMap.putAll(component.getViewablePixels());
                 pixelsMap.putAll(corridor.getViewablePixels());
 
 
             } else {
                 pixelsMap.putAll(component.getViewablePixels());
 
             }
             mapComponents.add(component);
 
 
         }
         //generateWalls();
         return pixelsMap;
     }
 
     private Coordinate generateRandomCoordinateNextToComponent(MapComponent component) {
         Random r = new Random();
         Coordinate maxCoordinate = getMaxCoordinateOfMapComponent(component);
         Coordinate minCoordinate = getMinCoordinateOfMapComponent(component);
         Coordinate c;
         switch (r.nextInt(3)) {
             case 0:
                 c = new Coordinate(maxCoordinate.getX() + r.nextInt(MAX_DISTANCE_BETWEEN_MAP_COMPONENT) + 2, minCoordinate.getY());
                 System.out.println("0");
                 break;
             case 1:
                 c = new Coordinate(minCoordinate.getX(), maxCoordinate.getY() + r.nextInt(MAX_DISTANCE_BETWEEN_MAP_COMPONENT) + 2);
                 System.out.println("1");
                 break;
             default:
                 c = new Coordinate(maxCoordinate.getX() + r.nextInt(MAX_DISTANCE_BETWEEN_MAP_COMPONENT) + 2, maxCoordinate.getY() + r.nextInt(MAX_DISTANCE_BETWEEN_MAP_COMPONENT) + 2);
                 System.out.println("2");
                 break;
 
         }
         System.out.println("random coordinate " + c);
         return c;
     }
 
     private boolean isOverlap(Map<Coordinate, ViewablePixel> componetA, Map<Coordinate, ViewablePixel> componetB) {
         for (Coordinate cA : componetA.keySet()) {
             for (Coordinate cB : componetB.keySet()) {
                 if (cA.equals(cB)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private Coordinate[] getNearestWayPoints(Map<Coordinate, ViewablePixel> componentA, Map<Coordinate, ViewablePixel> componentB) {
         int minDistance = Integer.MAX_VALUE;
         List<Coordinate[]> l = new ArrayList<Coordinate[]>();
         for (Coordinate cA : componentA.keySet()) {
             for (Coordinate cB : componentB.keySet()) {
                 if (componentA.get(cA) instanceof Way && componentB.get(cB) instanceof Way && minDistance > cA.distanceTo(cB)) {
                     minDistance = cA.distanceTo(cB);
                     Coordinate[] points = new Coordinate[2];
                     points[0] = cA;
                     points[1] = cB;
                     l.clear();
                     l.add(points);
 
                 } else if (componentA.get(cA) instanceof Way && componentB.get(cB) instanceof Way && minDistance == cA.distanceTo(cB)) {
                     Coordinate[] points = new Coordinate[2];
                     points[0] = cA;
                     points[1] = cB;
                     l.add(points);
                 }
             }
         }
         Random r=new Random();
 
         return l.get(r.nextInt(l.size()));
     }
 
     private void generateWalls() {
         Map<Coordinate, ViewablePixel> waysNow = new HashMap<Coordinate, ViewablePixel>(getWays());
         for (ViewablePixel p : waysNow.values()) {
 
             if (p instanceof Way) {
                 if (!pixelsMap.containsKey(new Coordinate(p.getCoordinate().getX() + 1, p.getCoordinate().getY()))) {
                     ViewablePixel wall = new Wall(new Coordinate(p.getCoordinate().getX() + 1, p.getCoordinate().getY()));
                     pixelsMap.put(wall.getCoordinate(), wall);
 
                 }
                 if (!pixelsMap.containsKey(new Coordinate(p.getCoordinate().getX(), p.getCoordinate().getY() + 1))) {
                     ViewablePixel wall = new Wall(new Coordinate(p.getCoordinate().getX(), p.getCoordinate().getY() + 1));
                     pixelsMap.put(wall.getCoordinate(), wall);
                 }
                 if (!pixelsMap.containsKey(new Coordinate(p.getCoordinate().getX() - 1, p.getCoordinate().getY()))) {
                     ViewablePixel wall = new Wall(new Coordinate(p.getCoordinate().getX() - 1, p.getCoordinate().getY()));
                     pixelsMap.put(wall.getCoordinate(), wall);
                 }
                 if (!pixelsMap.containsKey(new Coordinate(p.getCoordinate().getX(), p.getCoordinate().getY() - 1))) {
                     ViewablePixel wall = new Wall(new Coordinate(p.getCoordinate().getX(), p.getCoordinate().getY() - 1));
                     pixelsMap.put(wall.getCoordinate(), wall);
                 }
 
             }
         }
     }
 
     private Coordinate getMaxCoordinateOfMapComponent(MapComponent component) {
         int maxX = 0;
         int maxY = 0;
         for (Coordinate c : component.getViewablePixels().keySet()) {
             if (c.getX() > maxX) {
                 maxX = c.getX();
             }
             if (c.getY() > maxY) {
                 maxY = c.getY();
             }
         }
         return new Coordinate(maxX, maxY);
     }
 
     private Coordinate getMinCoordinateOfMapComponent(MapComponent component) {
         int minX = Integer.MAX_VALUE;
         int minY = Integer.MAX_VALUE;
         for (Coordinate c : component.getViewablePixels().keySet()) {
             if (c.getX() < minX) {
                 minX = c.getX();
             }
             if (c.getY() < minY) {
                 minY = c.getY();
             }
         }
         return new Coordinate(minX, minY);
     }
 
     private MapComponent getRandomMapComponent() {
         Random r = new Random();
         int randomIntForType = r.nextInt(4);
         int randomIntForRadius = limitRangeForValue(MAX_SIDE_OF_COMPONENT / 2, MIN_SIDE_OF_COMPONENT, r.nextInt());
         int randomIntForWidth = limitRangeForValue(MAX_SIDE_OF_COMPONENT, MIN_SIDE_OF_COMPONENT, r.nextInt());
         int randomIntForHeight = limitRangeForValue(MAX_SIDE_OF_COMPONENT, MIN_SIDE_OF_COMPONENT, r.nextInt());
         MapComponent component;
         switch (randomIntForType) {
             case 0:
                 component = new Cavern(randomIntForRadius);
                 break;
             case 1:
                 component = new Maze(randomIntForWidth, randomIntForHeight);
                 break;
             case 2:
                 component = new OvalRoom(randomIntForRadius);
                 break;
             default:
                 component = new RectangleRoom(randomIntForWidth, randomIntForHeight);
 
                 break;
         }
         return component;
     }
 
     private int limitRangeForValue(int max, int min, int value) {
         if (max < value) {
             return max;
         }
         if (min > value) {
             return min;
         }
         return value;
     }
 
     private Map<Coordinate, ViewablePixel> getWalls() {
         Map<Coordinate, ViewablePixel> walls = new HashMap<Coordinate, ViewablePixel>();
         for (ViewablePixel p : pixelsMap.values()) {
             if (p instanceof Wall) {
                 walls.put(p.getCoordinate(), p);
             }
         }
         return walls;
     }
 
     private Map<Coordinate, ViewablePixel> getOuterWalls() {
         Map<Coordinate, ViewablePixel> outerWalls = new HashMap<Coordinate, ViewablePixel>();
 
         for (ViewablePixel p : getWalls().values()) {
             Coordinate n = new Coordinate(p.getCoordinate().getX(), p.getCoordinate().getY() - 1);
             Coordinate e = new Coordinate(p.getCoordinate().getX() + 1, p.getCoordinate().getY());
             Coordinate w = new Coordinate(p.getCoordinate().getX() - 1, p.getCoordinate().getY());
             Coordinate s = new Coordinate(p.getCoordinate().getX(), p.getCoordinate().getY() + 1);
             if (!pixelsMap.containsKey(n) || !pixelsMap.containsKey(e) || !pixelsMap.containsKey(w) || !pixelsMap.containsKey(s)) {
                 outerWalls.put(p.getCoordinate(), p);
             }
         }
         return outerWalls;
     }
 
     private Map<Coordinate, ViewablePixel> getWays() {
 
         Map<Coordinate, ViewablePixel> ways = new HashMap<Coordinate, ViewablePixel>();
         for (ViewablePixel p : pixelsMap.values()) {
             if (p instanceof Way) {
                 ways.put(p.getCoordinate(), p);
             }
         }
 
         return ways;
     }
 
     private void shiftAllCoordinateTo(MapComponent component, Coordinate coordinate) {
         Map<Coordinate,ViewablePixel> newViewablePixels=new HashMap<Coordinate, ViewablePixel>();
         for (ViewablePixel p : component.getViewablePixels().values()) {
             p.getCoordinate().shiftCoordinate(coordinate.getX(), coordinate.getY());
             newViewablePixels.put(p.getCoordinate(), p);
         }
         component.getViewablePixels().clear();
         component.getViewablePixels().putAll(newViewablePixels);
 
     }
 }
