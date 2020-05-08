 package vooga.towerdefense.gameeditor.gameloader.xmlloaders;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.w3c.dom.Element;
 
 import util.Location;
 import util.Pixmap;
 import util.XMLTool;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.Tile;
 import vooga.towerdefense.model.tiles.SpawnTile;
 import vooga.towerdefense.model.tiles.factories.DefaultTileFactory;
 import vooga.towerdefense.model.tiles.factories.DestinationTileFactory;
 import vooga.towerdefense.model.tiles.factories.GrassTileFactory;
 import vooga.towerdefense.model.tiles.factories.PathTileFactory;
 import vooga.towerdefense.model.tiles.factories.SpawnTileFactory;
 import vooga.towerdefense.model.tiles.factories.TileFactory;
 
 /**
  * This class is responsible for constructing GameMap objects
  * from an xml file describing different game maps.
  * 
  * An example of this xml file is shown below:
  * 
  * <map>
  *      <map1>
  *              <image></image>
  *              <dimension>
  *                      <width>800</width>
  *                      <height>600</height>            
  *              </dimension>
  *              <tile_size>50</tile_size>
  *              <grid>
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 1 1 1 1 1 0 0 0 0 0 0 0 0
  *                      0 0 0 1 0 0 0 1 0 0 0 1 1 1 0 0
  *                      0 0 0 1 0 0 0 1 0 0 0 1 0 1 0 0
  *                      1 1 1 1 0 0 0 1 1 1 1 1 0 1 1 1 
  *                      0 0 0 1 0 0 0 1 0 0 0 1 0 1 0 0
  *                      0 0 0 1 0 0 0 1 0 0 0 1 1 1 0 0
  *                      0 0 0 1 1 1 1 1 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *              </grid>
  *      </map1>
  *      <map2>
  *              <image></image>
  *              <dimension>
  *                      <width>800</width>
  *                      <height>600</height>            
  *              </dimension>
  *              <tile_size>50</tile_size>
  *              <grid>
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *                      0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
  *              </grid>
  *      </map2>
  * </map>
  * 
  * This file is read, and a List of GameMap objects can be returned.
  * 
  * @author Erick Gonzalez
  */
 public class MapXMLLoader {
     private static final String IMAGE_PATH = "/vooga/towerdefense/images/";
     
     // map_loadfile.xml tags
     private static final String MAP_TAG = "map";
     private static final String IMAGE_TAG = "image";
     private static final String DIMENSION_TAG = "dimension";
     private static final String WIDTH_TAG = "width";
     private static final String HEIGHT_TAG = "height";
     private static final String TILE_SIZE_TAG = "tile_size";
     private static final String GRID_TAG = "grid";
     
     private XMLTool myXMLTool;    
     private Map<String, TileFactory> myTileIdMap;
     
     /**
      * 
      * @param xmlTool an XMLTool with an xml file
      */
     public MapXMLLoader(XMLTool xmlTool) {
         myXMLTool = xmlTool;        
         initTileIdMap();
     }
     
     private void initTileIdMap() {
         myTileIdMap = new HashMap<String, TileFactory>();
        myTileIdMap.put("0", new DefaultTileFactory("0"));
        myTileIdMap.put("1", new GrassTileFactory("1"));
        myTileIdMap.put("2", new PathTileFactory("2"));
        myTileIdMap.put("s", new SpawnTileFactory("s"));
        myTileIdMap.put("d", new DestinationTileFactory("d"));
     }
     
     /**
      * Loads all the maps described in the xml file.
      * 
      * @return a list of GameMap objects
      */
     public List<GameMap> loadMaps() {
         List<GameMap> gameMaps = new ArrayList<GameMap>();
         //TODO: mapElement needs to be passed in as a parameter
         Element mapElement = myXMLTool.getElement(MAP_TAG);
         
         List<Element> subElements = myXMLTool.getChildrenList(mapElement);        
         for (Element subElement : subElements) {
             GameMap map = loadMap(subElement);
             gameMaps.add(map);
         }
         return gameMaps;
     }
     
     private GameMap loadMap(Element mapNameElement) {
         Map<String, Element> subElements = myXMLTool.getChildrenElementMap(mapNameElement);
         Pixmap mapImage = loadMapImage(subElements.get(IMAGE_TAG));
         Dimension mapDimensions = loadMapDimensions(subElements.get(DIMENSION_TAG));
         Dimension tileSize = loadMapTileSize(subElements.get(TILE_SIZE_TAG));
         
         
         GameMap map = new GameMap(mapImage, mapDimensions, tileSize);
         createAndSetGrid(subElements.get(GRID_TAG), map);
         return map;
     }
     
     private Pixmap loadMapImage(Element imageElement) {
         String imagePath = myXMLTool.getContent(imageElement);
         return new Pixmap(IMAGE_PATH + imagePath);
     }
     
     private Dimension loadMapDimensions(Element dimensionElement) {
         Map<String, Element> subElementMap = myXMLTool.getChildrenElementMap(dimensionElement);
         
         int width = getMapWidth(subElementMap.get(WIDTH_TAG));
         int height = getMapHeight(subElementMap.get(HEIGHT_TAG));
         return new Dimension(width, height);
     }
     
     private int getMapWidth(Element widthElement) {
         return Integer.parseInt(myXMLTool.getContent(widthElement));
     }
     
     private int getMapHeight(Element heightElement) {
         return Integer.parseInt(myXMLTool.getContent(heightElement));
     }
     
     private Dimension loadMapTileSize(Element tileSizeElement) {
         int tileSize = Integer.parseInt(myXMLTool.getContent(tileSizeElement));
         return new Dimension(tileSize, tileSize);
     } 
     
     private void createAndSetGrid(Element tilesElement, GameMap map) {
         Dimension mapDimensions = map.getSize();
         Dimension tileDimensions = map.getTileSize();
         
         String gridString = myXMLTool.getContent(tilesElement);
         Scanner reader = new Scanner(gridString);
         
         int horizontalTileCount = (int) (mapDimensions.getWidth() / tileDimensions.getWidth());
         int verticalTileCount = (int) (mapDimensions.getHeight() / tileDimensions.getHeight());
 
         Tile[][] grid = new Tile[horizontalTileCount][verticalTileCount];
 
         for (int i = 0; i < grid[0].length; i++) {
             for (int j = 0; j < grid.length; j++) {
                 int xCenter = (int) (j * tileDimensions.getWidth() + 
                         tileDimensions.getWidth() / 2);
                 int yCenter = (int) (i * tileDimensions.getHeight() + 
                         tileDimensions.getHeight() / 2);
                 String tileId = reader.next();                
                 Location location = new Location(xCenter, yCenter);
                 grid[j][i] = getTileFactory(tileId).createTile(location, map);
             }
         }
         map.setGrid(grid);
     }
     
     private TileFactory getTileFactory(String tileId) {
         return myTileIdMap.get(tileId);
     }
 }
