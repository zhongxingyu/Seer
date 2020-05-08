 package vooga.rts.leveleditor.components;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import vooga.rts.leveleditor.gui.MapPanel;
 import vooga.rts.util.Location;
 
 
 /**
  * a class of EditableMap, this editable map is used to generate a
  * map that can be used for game players. This class enables them to
  * generated their own map
  * 
  * @author Richard Yang
  * 
  */
 
 public class EditableMap implements Serializable {
 
 
     /**
      * serial version UID
      */
     private static final long serialVersionUID = 5848819056578981375L;
     private int myXSize;
     private int myYSize; 
     
     private Map<Integer , MapLayer> myLayers;
     
     private List<Resource> myResource;
     
     private String myMapName = "CIEMAS";
     private String myDescription = " our RTS is the best one !";
 
     private EditableNode[][] myNodeMatrix;
 
     private Map<Integer, Location> myPlayerLocations;
     private int myPlayerNumber;
 
     private MapSaver mySaver;
     private MapLoader myLoader;
 
     public EditableMap (int x, int y, int nodeX, int nodeY) {
 
         myXSize = x;
         myYSize = y;
 
         initializeMap(nodeX, nodeY);
     }
 
     public EditableMap (int x, int y) {
 
         this(x, y, MapPanel.DEFAULT_TILE_WIDTH, MapPanel.DEFAULT_TILE_HEIGHT);
     }
 
     public EditableMap () {
         this(0, 0);
     }
 
     public void initializeMap (int width, int height) {
         myNodeMatrix = new EditableNode[myXSize][myYSize];
         for (int i = 0; i < myXSize; i++) {
             for (int j = 0; j < myYSize; j++) {
                 myNodeMatrix[i][j] = new EditableNode(i, j, width, height, false);
             }
         }
         myPlayerLocations = new HashMap<Integer, Location>();
         myPlayerNumber = 0;
         myLayers = new HashMap<Integer , MapLayer>();
         myResource = new ArrayList<Resource>();
 
 
         try {
             mySaver = new MapSaver(this);
         }
         catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     public void initializeMap () {
         initializeMap(MapPanel.DEFAULT_TILE_WIDTH, MapPanel.DEFAULT_TILE_HEIGHT);
     }
 
     public void addPlayer (int x, int y) {
 
         addPlayer(new Location(x, y));
     }
 
     public void addPlayer (Location loc) {
         myPlayerNumber++;
         myPlayerLocations.put(myPlayerNumber, loc);
     }
 
     public void removePlayer (int index) {
         myPlayerLocations.remove(index);
         myPlayerNumber--;
     }
 
 
     public HashMap<Integer, Location> getLocationMap () {
         return (HashMap<Integer, Location>) myPlayerLocations;
     }
 
     public void clearMap () {
         for (int i = 0; i < myXSize; i++) {
             for (int j = 0; j < myYSize; j++) {
                 myNodeMatrix[i][j].reset();
             }
         }
         myPlayerNumber = 0;
         myPlayerLocations.clear();
     }
 
     public void printMatrix () {
         System.out.println("printmatrix executed");
         System.out.println("X Size : " + myXSize);
         System.out.println("Y Size : " + myYSize);
 
         
         for(int i =0 ; i<myXSize ; i++) {
             for(int j =0 ; j<myYSize ; j++) {
                System.out.println(myNodeMatrix[i][j].getMyTile().getMyID());

             }
             System.out.print("\n");
         }
         for (int i = 0; i < 10; i++) {
             System.out.print("**");
         }
         System.out.print("\n");
     }
 
     public void load (File resourceFile) {
         try {
             myLoader.loadMapFile(resourceFile);
         }
         catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void load (String filePath) {
         File bufferFile = new File(filePath);
         load(bufferFile);
     }
 
     public void save (File objectiveFile) {
         try {
             mySaver.generateMapFile(objectiveFile);
         }
         catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void save (String filePath) {
         File bufferFile = new File(filePath);
         save(bufferFile);
     }
 
     public void addTerrain (int layerIndex, Terrain ter) {
         if (myLayers.containsKey(layerIndex)) {
             myLayers.get(layerIndex).addTerrain(ter);
         }
         else {
             MapLayer bufferLayer = new MapLayer();
             bufferLayer.addTerrain(ter);
             myLayers.put(layerIndex, bufferLayer);
         }
     }
     
     public void addTile(int x , int y , int id) {
         myNodeMatrix[x][y].getMyTile().setType(id);
     }
     
     
     
     public void addResource(int x, int y , int resourceID) {
         Resource buffer = new Resource(x,y,resourceID);
         myResource.add(buffer);
     }
     
     public int getLayerNumber() {
         return myLayers.size();
     }
     
     public Map<Integer , MapLayer> getLayerMap() {
         return myLayers;
     }
     
     public MapLayer getLayer(int index) {
         return myLayers.get(index);
     } 
     
     public List<Resource> getResourceSet() {
         return myResource;
     }
         public String getMyMapName () {
         return myMapName;
     }
 
     public void setMyMapName (String myMapName) {
         this.myMapName = myMapName;
     }
 
     public String getMyDescription () {
         return myDescription;
     }
 
     public void setMyDescription (String myDescription) {
         this.myDescription = myDescription;
     }
 
     public EditableNode getMapNode (int x, int y) {
         return myNodeMatrix[x][y];
     }
 
     public EditableNode[][] getMap () {
         return myNodeMatrix;
     }
 
     public int getMyPlayerNumber () {
         return myPlayerNumber;
     }
 
     public int getMyXSize () {
         return myXSize;
     }
 
     public int getMyYSize () {
         return myYSize;
     }
 
     public void setMyXSize (int myXSize) {
         this.myXSize = myXSize;
     }
 
     public void setMyYSize (int myYSize) {
         this.myYSize = myYSize;
     }
 
     public Location getPlayer (int index) {
         return myPlayerLocations.get(index);
     }
 
     public Map<Integer, Location> getAllPlayers () {
         return myPlayerLocations;
     }
 
     public int getWidth () {
         return myXSize;
     }
 
     public int getHeight () {
         return myYSize;
     }
 
     public void setWidth (int w) {
         myXSize = w;
 
     }
 
     public void setHeight (int h) {
         myXSize = h;
 
     }
 
     public void zoomIn () {
         for (int i = 0; i < myXSize; i++) {
             for (int j = 0; j < myYSize; j++) {
                 myNodeMatrix[i][j].ZoomIn();
             }
         }
 
     }
 
 
     public void zoomOut () {
         for (int i = 0; i < myXSize; i++) {
             for (int j = 0; j < myYSize; j++) {
                 myNodeMatrix[i][j].ZoomOut();
             }
         }
 
     }
 
     
     public static void main(String[] args) {
         EditableMap test = new EditableMap(10,10);
         test.addPlayer(1,2);
         test.addPlayer(2,3);
         test.addPlayer(3,4);
         test.addPlayer(5,7);
         test.addTile(1, 1, 1);
         test.addTile(2, 2, 2);
         test.addTile(3, 3, 3);
         test.addTerrain(3, new Terrain(2,2,1));
         test.addTerrain(2, new Terrain(3,3,2));
         test.addTerrain(1, new Terrain(4,4,3));
         test.addResource(7, 7, 1);
         test.addResource(8, 8, 2);
         test.addResource(9, 9, 3);
         test.save(System.getProperty("user.dir")+"./src/test.xml");
     }
 }
