 package vooga.rts.leveleditor.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import javax.swing.JComponent;
 import util.Location;
 import util.input.Input;
 import util.input.InputClassTarget;
 import util.input.InputMethodTarget;
 import util.input.PositionObject;
 import vooga.rts.leveleditor.components.EditableMap;
 import vooga.rts.leveleditor.components.EditableTerrain;
 import vooga.rts.leveleditor.components.EditableTile;
 import vooga.rts.leveleditor.components.EditableResource;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Camera;
 import vooga.rts.util.Location3D;
 
 /**
  * This panel is responsible for displaying the 
  * map editted by the user, every map component 
  * will be painted on this panel
  * 
  * @author Ziqiang Huang
  *
  */
 
 @SuppressWarnings("serial")
 @InputClassTarget
 public class MapPanel extends JComponent {
 
     public static final String INPUT_DIR = "vooga.rts.resources.properties.Input";
     public static final String PLAYER_IMAGE_PATH = "Player1.png";
     public static final Dimension DEFAULT_MAP_SIZE = new Dimension(600, 600);
     public static final int RESOURCEMODE = 1;
     public static final int PLAYERMODE = 2;
     public static final int TERRAINMODE = 3;
     public static final int TILEMODE = 4;
     public static final int FILLMODE = 5;
     public static final int REMOVEMODE = 6;
 
     private Canvas myCanvas;
     private EditableMap myMap;
     private Input myInput;
     private int myWidth;
     private int myHeight;
     private int myTileWidth;
     private int myTileHeight;
     private int myCurrentLayer;
     private int myMaxLayer;
     private int myMode;
     private BufferedImage myPlayerImage;
 
     /**
      * Constructor for the map
      * 
      * @param canvas : the canvas that holds the panel
      */
     public MapPanel (Canvas canvas) {
         myCanvas = canvas;
         myMap = new EditableMap();
         myInput = new Input(INPUT_DIR, this);
         myInput.addListenerTo(this);
         myWidth = 0;
         myHeight = 0;
         myCurrentLayer = 0;
         myMaxLayer = 0;
         myTileWidth = 0;
         myTileHeight = 0;
         myPlayerImage = ResourceManager.getInstance().<BufferedImage> getFile(PLAYER_IMAGE_PATH,
                                                               BufferedImage.class);
         setPanelSize();
 
     }
     /**
      * Set the panel and camera view according to the size user entered
      */
 
     private void setPanelSize () {
         Dimension d = new Dimension(myTileWidth * myWidth, myTileHeight * myHeight);
         setPreferredSize(d);
         Camera.instance().setMapSize(d);
         Camera.instance().setViewSize(d);
         Camera.instance().setWorldLocation(new Location3D((myTileWidth * myWidth)/2, (myTileHeight * myHeight)/2,0));
     }
 
     /**
      * Paint the map on this panel
      */
     @Override
     public void paintComponent (Graphics g) {
         g.setColor(Color.white);
         g.fillRect(0, 0, 2*myWidth * myTileWidth, myHeight * myTileHeight);
         Camera.instance().paint((Graphics2D)g);
         g.setColor(Color.gray);
         for (int i = 0; i < myWidth; i++) {
             drawLineTest(i * myTileWidth / 2, 0, i * myTileWidth / 2, myHeight * myTileHeight/2,g);
         }
 
         for (int j = 0; j < myHeight; j++) {
             drawLineTest(0, j * myTileHeight /2  , myWidth * myTileWidth / 2, j * myTileHeight /2,g);
         }
 
         ((Graphics2D) g).setStroke(new BasicStroke(2));
         g.setColor(Color.black);
 
         drawLineTest(0, 0, myWidth * myTileWidth/2, 0, g);
         drawLineTest(0, 0, 0, myHeight * myTileHeight/2,g);
         drawLineTest(myWidth * myTileWidth/2, 0, myWidth * myTileWidth/2, myHeight * myTileHeight/2,g);
         drawLineTest(0, myHeight * myTileHeight/2, myWidth * myTileWidth/2, myHeight * myTileHeight/2,g);
 
         myMap.paint((Graphics2D)g);
         
         for(Location loc : myMap.getAllPlayers().values()) {
            g.drawImage(myPlayerImage, (int)loc.getX(), (int)loc.getY(), null);
         }
     }
 
     /**
      * Helper method to draw a line between two point
      * 
      * @param x1 x value of the start point
      * @param y1 y value of the start point
      * @param x2 x value of the end point
      * @param y2 y value of the end point
      * @param g  Graphics to draw the line
      */
 
     public void drawLineTest(int x1, int y1, int x2, int y2, Graphics g) {
         Point p1 = new Point(x1,y1);
         Point2D loc1 = Camera.instance().worldToView(new Location3D(p1));
         Point p2 = new Point(x2,y2);
         Point2D loc2 = Camera.instance().worldToView(new Location3D(p2));
         g.drawLine((int)loc1.getX(), (int)loc1.getY(), (int)loc2.getX(), (int)loc2.getY());
     }
     
     /**
      * Set the width of the panel
      * @param w
      */
 
     public void setWidth (int w) {
         myWidth = w;
         setPanelSize();
         repaint();
     }
     
     /**
      * Set the height of the panel
      * @param h
      */
 
     public void setHeight (int h) {
         myHeight = h;
         setPanelSize();
         repaint();
     }
     
     /**
      * Set the tile width of the panel
      * @param w
      */
     public void setTileWidth (int w) {
         myTileWidth = w;
         setPanelSize();
         repaint();
     }
     /**
      * Set the tile height of the panel
      * @param h
      */
     public void setTileHeight (int h) {
         myTileHeight = h;
         setPanelSize();
         repaint();
     }
     
     /**
      *  Set the map for the panel
      * @param map
      */
     public void setMap(EditableMap map) {
         myMap = map;
     }
 
     /**
      * Initialize the map by user's customization
      * 
      * @param name
      * @param desc
      * @param width
      * @param height
      * @param tileWidth
      * @param tileHeight
      */
     public void initializeMap (String name, String desc, int width, int height, int tileWidth, int tileHeight) {
         myMap = new EditableMap(name,desc,width, height, tileWidth, tileHeight);
     }
     
     /**
      * place resource on the map according to the give location3D
      * @param loc
      */
     public void placeResource (Location3D loc) {
         EditableResource r = myCanvas.getCurrentSelectResource();
         myMap.addResource(r.getImage(), loc, r.getMyID(), r.getType(), r.getMyImageName(), r.getMyAmount());
         repaint();
     }
     
     /**
      * place terrain on the map according to the give location3D
      * @param loc
      */
     public void placeTerrain (Location3D loc) {
         EditableTerrain t = myCanvas.getCurrentSelectTerrain();
         myMap.addTerrain(t.getImage(), loc, t.getMyID(), t.getMyName(), t.getMyImageName(), t.getMyWalkAbility());
         repaint();
     }
 
     /**
      * place tile on the map according to the give location3D
      * @param loc
      */
     private void placeTile (Location3D loc) {
         EditableTile t = myCanvas.getCurrentSelectTile();
         myMap.addTile(loc, t.getMyID(), t.getMyName(), t.getMyImageName(), t.getImage());
         repaint();
     }
     
     /**
      * place player on the map according to the give location3D
      * @param loc
      */
    public void placePlayer (Location3D loc) {
        Point2D loc1 = Camera.instance().worldToView(loc);
        myMap.addPlayer(new Location(loc1.getX(),loc1.getY()));
         repaint();
     }
     
     /**
      * set the edit mode as fill
      */
     public void fill() {
         myMode = FILLMODE;       
     }
     
     /**
      * Set the edit mode as remove
      */
     public void setRemoveMode() {
         myMode = REMOVEMODE;
 
     }
     
     /**
      * remove terrains and resources from the current location
      * 
      * @param loc
      */
     public void remove(Location3D loc) {
         myMap.getTerrain().remove(myMap.getTerrain().getItem(loc));
         myMap.getResources().remove(myMap.getResources().getItem(loc));
         repaint();
     }
         
     /**
      * fill the map with current selected file
      */
     public void fillTiles() {
         EditableTile t = myCanvas.getCurrentSelectTile();
         for(int i = 0; i<myMap.getMyXsize(); ++i) {
             for(int j = 0; j<myMap.getMyYsize(); ++j) {
                 if (myMap.getMyTile(i, j).getMyID() == 0) {
                     myMap.addTile(i,j, t.getMyID(), t.getMyName(), t.getMyImageName(), t.getImage());
                 }
             }
         }
         repaint();        
     }
     
     /**
      * clear the map
      */
     public void clear () {
         myCurrentLayer = 0;
         myMaxLayer = 0;
         myMap.clearMap();
         repaint();
     }
     
     /**
      * 
      * @return the map of the panel
      */
     public EditableMap getMyMap () {
         return myMap;
     }
     
     /**
      * set the edit mode according to the given mode
      * @param mode
      */
     public void setMode (int mode) {
         myMode = mode;
     }
     
     /**
      * set the current layer according to the given number
      * @param n
      */
     public void setCurrentLayer (int n) {
         myCurrentLayer = n;
 
     }
     
     /**
      * 
      * @return the current layer user are editing       
      */
     public int getCurrentLayer () {
         return myCurrentLayer;
     }
     
     /**
      * 
      * @return the max layer for the map
      */
     public int getMaxLayer () {
         return myMaxLayer;
     }
     
     /**
      * add one layer to the map
      */
     public void addLayer () {
         myMaxLayer++;
 
     }
     
     /**
      * remove one layer from the map
      */
     public void removeLayer () {
         if(myMaxLayer>0)
             myMaxLayer--;
     }
     
     /**
      * edit the map according to the current mode 
      * @param p
      */
     @InputMethodTarget(name = "onLeftMouseDown")
     public void editMap (PositionObject p) {
         Location3D loc = Camera.instance().viewtoWorld(p.getPoint2D());
         switch (myMode) {
             case RESOURCEMODE:
                 placeResource(loc);
                 break;
             case PLAYERMODE:
                 placePlayer(loc);
                 break;
             case TERRAINMODE:
                 placeTerrain(loc);
                 break;
             case TILEMODE:
                 placeTile(loc);
                 break;
             case FILLMODE:
                 fillTiles();
                 break;
             case REMOVEMODE:
                 remove(loc);
             default:
                 break;
         }
     }
 
     /**
      * support drag for tile editing
      * @param p
      */
     @InputMethodTarget(name = "onMouseDrag")
     public void testDrag (PositionObject p) {
         Location3D loc = Camera.instance().viewtoWorld(p.getPoint2D());
         if (myMode == TILEMODE) {
             placeTile(loc);
         }
     }
 
 
 
 }
