 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dota.pkg3;
 
 import image.ResourceTools;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Point;
 import java.util.ArrayList;
 
 /**
  *
  * @author kevin.lawrence
  */
 public class MapFactory {
 
     public static Map getMap(Image background, Dimension gridCellSize, Dimension gridSize,
             ArrayList<MapObstacle> obstacles, ArrayList<MapPortal> portals, ArrayList<Point> items) {
 
         Map map = new Map(background, gridCellSize, gridSize);
 
         map.setObstacles(obstacles);
         map.setItems(items);
         map.setPortals(portals);
 
         return map;
     }
     
     public static void addPortal(Map startMap, Point startLocation, Map destinationMap, Point destinationLocation){
         startMap.getPortals().add(new MapPortal(startLocation, destinationMap, destinationLocation));
     }
 
     public static Map getLevelOneMainMap() {
         Image background = ResourceTools.loadImageFromResource("Resources/Route (2).bmp");
         Dimension gridCellSize = new Dimension(16, 16);
         Dimension gridSize = new Dimension(20, 70);
 
         ArrayList<MapObstacle> obstacles = new ArrayList<MapObstacle>();
         obstacles.add(new MapObstacle(new Point(0, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(1, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(2, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(3, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(-1, 6), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(0, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(1, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(2, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(3, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(4, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(5, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(6, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(6, 8), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(6, 9), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(6, 10), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(6, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(5, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(4, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(3, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(2, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(1, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(0, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 1), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 2), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 3), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 5), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 6), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 8), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 9), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 10), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 12), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 13), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 14), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(58, 15), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(57, 1), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 15), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 14), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 13), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 12), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 11), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 10), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 9), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 8), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 6), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(62, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(63, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(64, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(65, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(66, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(67, 5), ObstacleType.BUSH));
         obstacles.add(new MapObstacle(new Point(68, 5), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(68, 6), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(68, 7), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(68, 8), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(69, 8), ObstacleType.WALL));
 
 
 
         obstacles.add(new MapObstacle(new Point(59, 15), ObstacleType.WATER));
         obstacles.add(new MapObstacle(new Point(60, 15), ObstacleType.WATER));
         obstacles.add(new MapObstacle(new Point(61, 15), ObstacleType.WATER));
 
         //add other obstacles here...
 
         ArrayList<MapPortal> portals = new ArrayList<MapPortal>();
         
         ArrayList<Point> items = new ArrayList<Point>();
         items.add(new Point(10, 10));
         items.add(new Point(12, 10));
        items.add(new Point(0,9));
         //add other items here
 
         Map levelOneMap = getMap(background, gridCellSize, gridSize, obstacles, portals, items);
 
         addPortal(levelOneMap, new Point(40, 4), getStoreMap(), new Point(0,0));
         //add other portals here...
 
         return levelOneMap;
     }
 
     public static Map getStoreMap() {
         //change this image to the store when you get one...
         Image background = ResourceTools.loadImageFromResource("Resources/Generic_Store(Grid).png");
         Dimension gridCellSize = new Dimension(16, 16);
         Dimension gridSize = new Dimension(20, 20);
 
         ArrayList<MapObstacle> obstacles = new ArrayList<MapObstacle>();
         obstacles.add(new MapObstacle(new Point(0, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(1, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(2, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(3, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(4, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(5, 5), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(7, 5), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(8, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(9, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(10, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(11, 4), ObstacleType.WALL));
         obstacles.add(new MapObstacle(new Point(12, 4), ObstacleType.WALL));
 
         //add other obstacles here...
 
 
         ArrayList<Point> items = new ArrayList<Point>();
         items.add(new Point(12, 5));
         items.add(new Point(11, 5));
         items.add(new Point(10, 5));
         //add other items here
 
         ArrayList<MapPortal> portals = new ArrayList<MapPortal>();
 
 
         return getMap(background, gridCellSize, gridSize, obstacles, portals, items);
     }
 }
