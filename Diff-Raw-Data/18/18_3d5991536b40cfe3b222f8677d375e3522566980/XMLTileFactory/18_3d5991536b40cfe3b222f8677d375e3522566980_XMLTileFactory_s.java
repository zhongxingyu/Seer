 package vooga.towerdefense.model.tiles.factories;
 
 import java.util.HashMap;
 import org.w3c.dom.Element;
 import util.Location;
 import util.XMLTool;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.Tile;
 import vooga.towerdefense.model.tiles.VersaTile;
 import vooga.towerdefense.util.Pixmap;
 
 
 public class XMLTileFactory extends TileFactory {
     
     private static final String TILE_IMAGES_CLASS_PATH = "/vooga/towerdefense/images/map/";
     private static final String IMGFILE_TAG = "image";
     private static final String NAME_TAG = "name";
     private static final String WALKABLE_TAG = "walkable";
     private static final String BUILDABLE_TAG = "buildable";
     private static final String XML_TRUE = "yes";
     
     private String myName;
     private String myID;
     private Pixmap myImage;
     private XMLTool myXMLTool;
     private HashMap<String, String> myData;
     
     public XMLTileFactory(XMLTool tool, Element tileElement, String id) {
         myXMLTool = tool;
         myID = id;
         setData(tileElement);
     }
     
     private void setData (Element tileElement) {
         myData = (HashMap<String, String>) myXMLTool.getChildrenStringMap(tileElement);
         myName = myData.get(NAME_TAG);
         myImage = new Pixmap(TILE_IMAGES_CLASS_PATH + myData.get(IMGFILE_TAG));
     }
     
     @Override
     public String getName () {
         return myName;
     }
     
     @Override
     public Tile createTile (Location center, GameMap map) {
         VersaTile tile = new VersaTile(myImage, center, map.getTileSize());
        tile.setName(myName);        
         tile.setTileBuildable(myData.get(BUILDABLE_TAG).equals(XML_TRUE));
        tile.setTileWalkable(myData.get(WALKABLE_TAG).equals(XML_TRUE));       
         return tile;
     }
     
     @Override
     public String getTileId () {
         return myID;
     }
 }
