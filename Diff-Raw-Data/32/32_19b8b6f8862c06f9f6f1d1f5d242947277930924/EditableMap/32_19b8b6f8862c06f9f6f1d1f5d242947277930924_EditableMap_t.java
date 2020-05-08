 package vooga.rts.leveleditor.components;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.util.Map;
 import javax.xml.parsers.ParserConfigurationException;
 import vooga.rts.map.GameMap;
 import vooga.rts.state.GameState;
 import vooga.rts.util.Location;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 
 
 /**
  * a class of EditableMap, this editable map is used to generate a
  * map that can be used for game players. This class enables them to
  * generated their own map. This class extends GameMap
  * 
  * @author Richard Yang
  * 
  */
 
 public class EditableMap extends GameMap {
 
     /**
      * the default value of player upper limit
      */
     private static final int DEFAULT_PLAYER_UPPER_LIMIT = 8;
     
     /**
      * the default value of team upper limit
      */
     private static final int DEFAULT_TEAM_UPPER_LIMIT = 4;
     
     private String myMapName = "CIEMAS";
     private String myDescription = " our RTS is the best one !";
 
     private PlayerSet myPlayerSet;
 
     private MapSaver mySaver;
     private MapLoader myLoader;
     
     private EditableTileMap myTileMap;
     /**
      * constructor for editable map
      * @param name the name of this map
      * @param desc a brief description of this game map
      * @param xSize the x size of this map, in tiles
      * @param ySize the y size of this map, in tiles
      * @param tileWidth the width of the tiles in this map
      * @param tileHeight the height of the tiles in this map
      */
     public EditableMap (String name, String desc, int xSize, int ySize,
                              int tileWidth, int tileHeight) {
         super(new Dimension(tileWidth , tileHeight) , xSize , ySize);
         myTileMap = new EditableTileMap(new Dimension(tileWidth, tileHeight), xSize, ySize);
         myMapName = name;
         myDescription = desc;
         GameState.setMap(this);
         initializeMap();
         setTileMap(myTileMap);
     }
     /**
      * default constructor
      */
     public EditableMap () {
         this("", "", 0, 0, 0, 0);
     }
 
     /**
      * initialize the map tile by tile
      */
     public void initializeMap () {
         myTileMap.initialize();
         myPlayerSet = new PlayerSet(DEFAULT_PLAYER_UPPER_LIMIT, DEFAULT_TEAM_UPPER_LIMIT);
 
         try {
             mySaver = new MapSaver(this);
             myLoader = new MapLoader();
         }
         catch (ParserConfigurationException e) {
             e.printStackTrace();
         }
     }
     /**
      * reset the tile map to a certain size
      * @param xSize x size of this tile map
      * @param ySize y size of this tile map
      * @param tileWidth the width of a single tile
      * @param tileHeight the height of a single tile
      */
     public void resetTileMap(int xSize, int ySize ,int tileWidth, int tileHeight) {
         myTileMap = new EditableTileMap(new Dimension(tileWidth,tileHeight),ySize,xSize);
     }
 
     /**
      * clear all tiles
      */
 
     public void clearMap () {
         myTileMap.removeAllTiles();
         getTerrain().getMySprites().clear();
         getResources().getMySprites().clear();
         System.out.println("clear");
     }
     
     /**
      * return the map loaded by the loader
      * @return EditableMap
      */
     public EditableMap returnLoadedMap() {
         return myLoader.getMyMap();
     }
 
     /**
      * print the id of each tile, just for testing purpose
      */
     public void printMatrix () {
         System.out.println("printmatrix executed");
         System.out.println("X Size : " + getMyXsize());
         System.out.println("Y Size : " + getMyYsize());
         
         for (int i =0 ; i<getMyXsize() ; i++) {
             for (int j =0 ; j<getMyYsize() ; j++) {
                 System.out.print(myTileMap.getTile(i, j).getMyID());
                 System.out.print(" ");
             }
             System.out.print("\n");
         }
         System.out.print("\n");
     }
     
     /**
      * load the map from xml file
      * @param resourceFile resource xml file
      */
     public void load (File resourceFile) {
 
         try {
             myLoader.loadMapFile(resourceFile);
         }
        catch (Exception e) {
            // TODO Auto-generated catch block
             e.printStackTrace();
         }

        
         
     }
     /**
      * load the map from a certain path
      * @param filePath
      */
     public void load (String filePath) {
         File bufferFile = new File(filePath);
         load(bufferFile);
     }
     /**
      * save the file to a destination
      * @param objectiveFile the destination file
      */
     public void save (File objectiveFile) {
         try {
             mySaver.generateMapFile(objectiveFile);
         }
        catch (Exception e) {
            // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     /**
      * save the map to a certain path
      * @param filePath the desitination file path
      */
     public void save (String filePath) {
         File bufferFile = new File(filePath);
         save(bufferFile);
     }
     
     /**
      * add a player based on its location
      * @param location location of the player
      */
     public void addPlayer(Location location) {
         myPlayerSet.addPlayer(location);
     }
     /**
      * add a player based on x and y
      * @param x x position of player
      * @param y y position of player
      */
     public void addPlayer(int x , int y) {
         myPlayerSet.addPlayer(x, y);
     }
     
     /**
      * remove player 
      * @param start start location
      * @param end end location
      */
     public void removePlayer(Location start, Location end) {
         myPlayerSet.removePlayer(start, end);
     }
     
     /**
      * remove a player from the map
      * @param startX start x position 
      * @param startY start y position
      * @param endX end x position
      * @param endY end y position
      */
     public void removePlayer(int startX, int startY, int endX, int endY) {
         myPlayerSet.removePlayer(startX, startY, endX, endY);
     }
     
     /**
      * add a tile to this map
      * @param loc location of the terrain
      * @param id id of the terrain
      * @param name name of the terrain
      * @param imageName imageName of the terrain
      * @param image image of the terrain
      */
     public void addTile(Location3D loc, int id, String name, String imageName, Pixmap image) {
         myTileMap.addTile(loc, id, name, imageName, image);
     }
     /**
      * add tile to map    
      * @param i the ith column tile
      * @param j the jth row tile
      * @param id the id of the tile
      * @param name the name of the tile
      * @param imageName the image name of the tile
      * @param image the image of that tile
      */
     public void addTile(int i, int j, int id, String name , String imageName, Pixmap image) {
         myTileMap.addTile(i, j, id, name, imageName, image);
     }
     /**
      * add new terrains to the map
      * @param ter a certain terrain
      */
     public void addTerrain(EditableTerrain ter) {
         getTerrain().add(ter);
     } 
     
     /**
      * add new terrains based on some parameters
      * @param image the image of the terrain
      * @param center the center position of the terrain
      * @param id the id of the terrain
      * @param name
      * @param imageName
      * @param walkAbility
      */
     public void addTerrain(Pixmap image, Location3D center , int id , String name , String imageName, int walkAbility ) {
         addTerrain(new EditableTerrain(image, center , id , name ,imageName, walkAbility));
     }
     
     /**
      * add new terrains based on differnent parameters
      * @param image  image of that terrain
      * @param x x position
      * @param y y position
      * @param z z position
      * @param id id of the terrain
      * @param name name of the terrain
      * @param imageName imageName of the terrain
      * @param walkAbility walkability of the terrain
      */
     public void addTerrain(Pixmap image, int x, int y , int z, int id, String name , String imageName , int walkAbility) {
         addTerrain(image, new Location3D(x, y ,z), id, name , imageName ,walkAbility);
     }
     /**
      * add terrain based on different parameters 
      * @param image image of the terrain
      * @param x x position
      * @param y y position 
      * @param layerCount number of layers
      * @param layerHeight height of layer
      * @param id id of the terrain
      * @param name name of the terrain
      * @param imageName image name of the terrain
      * @param walkAbility walkability of the terrain
      */
     public void addTerrain(Pixmap image, int x, int y, int layerCount, int layerHeight, int id , String name , String imageName, int walkAbility) {
         addTerrain(image,x,y,layerCount*layerHeight,id,name,imageName,walkAbility);
     }
     
     /**
      * add a resource to the map
      * @param res certain resource
      */
     public void addResource(EditableResource res) {
         getResources().add(res);
     }
     /**
      * add resource based on different parameters
      * @param image image of the resource
      * @param center center position of the resource
      * @param id id of the resource
      * @param name of the resource
      * @param imageName of the resource
      * @param amount of the resource
      */
     public void addResource(Pixmap image, Location3D center , int id, String name , String imageName, int amount) {
         addResource(new EditableResource(image,center,id,name,imageName,amount));
     }
     
     /**
      * add a resource to the map based on different parameters
      * @param image image of the resource
      * @param x x position
      * @param y y position 
      * @param z z position
      * @param id id of the resource
      * @param name name of the resource
      * @param imageName of the resource
      * @param walkAbility of the resource
      */
     public void addResource(Pixmap image, int x, int y , int z, int id, String name , String imageName , int walkAbility) {
         addResource(image, new Location3D(x, y ,z), id, name , imageName ,walkAbility);
     }
     
     /**
      * add a resource based on differnet parameters
      * @param image image of resource
      * @param x x position
      * @param y y position
      * @param layerCount number of layers
      * @param layerHeight height of layer
      * @param id id of the resource
      * @param name name of the resource
      * @param imageName image name of the resource
      * @param amount amount of the resource
      */
     public void addResource(Pixmap image, int x, int y, int layerCount, int layerHeight, int id , String name , String imageName, int amount) {
         addResource(image,x,y,layerCount*layerHeight,id,name,imageName,amount);
     }
     
     /**
      *return the x size 
      * @return int
      */
     public int getMyXsize() {
         return myTileMap.getMyHeight();
     }
     /**
      * return the y size
      * @return int
      */
     public int getMyYsize() {
         return myTileMap.getMyWidth();
     }
     /**
      * get the width of tile
      * @return int
      */
     public int getMyTileWidth() {
         return (int)myTileMap.getMyTileSize().getWidth();
     }
     
     /**
      * get the height of tile 
      * @return int
      */
     public int getMyTileHeight() {
         return (int)myTileMap.getMyTileSize().getHeight();
     }
     
     /**
      * return the terrain size
      * @return int
      */
     public int getTerrainSize() {
         return getTerrain().getSize();
     }
     
     /**
      * return the resource number
      * @return int
      */
     public int getResourceSize() {
         return getResources().getSize();
     }
     
     /**
      * return the map name
      * @return String
      */
     public String getMyMapName () {
         return myMapName;
     }
     
     /**
      * get the number of all players
      * @return
      */
     public int getMyPlayerNumber() {
         return myPlayerSet.getMyPlayerNumber();
     }
     
     /**
      * get a certain tile based on position
      * @param i ith 
      * @param j jth
      * @return
      */
     public EditableTile getMyTile(int i , int j) {
         return myTileMap.getTile(i, j);
     }
     /**
      * return a certain terrain in this map
      * @param index the index of the terrain
      * @return EditableTerrain
      */
     public EditableTerrain getTerrain(int index) {
         return (EditableTerrain)getTerrain().getMySprites().get(index);
     }
     /**
      * return a certain resource in this map
      * @param index the index of the resource
      * @return EditableResource
      */
     public EditableResource getResource(int index) {
         return (EditableResource)getResources().getMySprites().get(index);
     }
 
     /**
      * set the map name 
      * @param myMapName the name of this map
      */
     public void setMyMapName (String myMapName) {
         this.myMapName = myMapName;
     }
     /**
      * get the description of that map
      * @return the description of current map
      */
     public String getMyDescription () {
         return myDescription;
     }
     /**
      * set the description of the map
      * @param myDescription the description
      */
     public void setMyDescription (String myDescription) {
         this.myDescription = myDescription;
     }
     /**
      * return all players
      * @return the whole player set
      */
     public Map<Integer,Location> getAllPlayers() {
         return myPlayerSet.getAllPlayers();
     }
 
 }
