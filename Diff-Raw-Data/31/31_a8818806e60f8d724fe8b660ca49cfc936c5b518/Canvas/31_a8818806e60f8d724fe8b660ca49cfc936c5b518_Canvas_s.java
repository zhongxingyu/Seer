 package vooga.rts.leveleditor.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import vooga.rts.leveleditor.components.Resource;
 import vooga.rts.leveleditor.components.Terrain;
 import vooga.rts.leveleditor.components.Tile;
 import vooga.rts.resourcemanager.ImageLoader;
 import vooga.rts.resourcemanager.ResourceManager;
 
 /**
  * 
  * This class holds all the visible components. 
  * It will be roughly divided into two parts - the left being the EditableMap, 
  * which visualizes the map the designer is creating, 
  * the right being all the resources (e.g. Trees) available to be put on the map. 
  * It will also contains a menu bar that allows user to save and load map
  * 
  * @author Ziqiang Huang
  *
  */
 public class Canvas extends JFrame {
 
     public static final Dimension DEFAULT_CANVAS_SIZE  = new Dimension (800,600);
     public static final String USER_DIR = "user.dir";
 
     private MapPanel myMapPanel;
     private ResourcePanel myResourcePanel;
     private TilePanel myTilePanel;
     private TerrainPanel myTerrainPanel;
     private LayerPanel myLayerPanel;
     private ButtonPanel myButtonPanel;
     private MenuManager myMenuManager;
     private Resource myCurrentSelectResource;
     private Terrain myCurrentSelectTerrain;
     private Tile myCurrentSelectTile;
     private JScrollPane  myMapScroll;
 
     public Canvas() {
         setTitle("Level Editor");
         myMapPanel = new MapPanel(this);
         myResourcePanel = new ResourcePanel(this);
         myTerrainPanel = new TerrainPanel(this);
         myTilePanel = new TilePanel(this);
         myLayerPanel = new LayerPanel(this);
         myButtonPanel = new ButtonPanel(this);
         myMenuManager = new MenuManager(this);
 
         JPanel ChooserPanel = new JPanel(new BorderLayout());        
         JScrollPane resourceScroll = new JScrollPane(myResourcePanel);
         JScrollPane terrainScroll = new JScrollPane(myTerrainPanel);
         JScrollPane tileScroll = new JScrollPane(myTilePanel);
         JTabbedPane MapComponentTabPane = new JTabbedPane();
         MapComponentTabPane.add("Tiles", tileScroll);
         MapComponentTabPane.add("Terrains", terrainScroll);
         MapComponentTabPane.add("Resources", resourceScroll);
         JTabbedPane LayerTabPane = new JTabbedPane();
         JScrollPane layerScroll = new JScrollPane(myLayerPanel);
         LayerTabPane.add("Layers",layerScroll);
         ChooserPanel.add(MapComponentTabPane, BorderLayout.NORTH);
         ChooserPanel.add(LayerTabPane, BorderLayout.CENTER);
         ChooserPanel.add(myButtonPanel, BorderLayout.SOUTH);
 
         myMapScroll = new JScrollPane(myMapPanel);
         getContentPane().add(myMapScroll, BorderLayout.CENTER);
         getContentPane().add(ChooserPanel, BorderLayout.EAST);
 
         setJMenuBar(myMenuManager);
 
         setPreferredSize(DEFAULT_CANVAS_SIZE);
         pack();
         setVisible(true);
 
     }
 
 
     public void setCurrentSelectResource(Resource r) {
         myCurrentSelectResource = r;
     }
 
     public Resource getCurrentSelectResource() {
         return myCurrentSelectResource;
     }
     
     public void setCurrentSelectTerrain(Terrain t) {
         myCurrentSelectTerrain = t;        
     }
     
     public Terrain getCurrentSelectTerrain() {
         return myCurrentSelectTerrain;
     }
     
     public void setCurrentSelectTile(Tile t) {
         myCurrentSelectTile = t;       
     }
     
     public Tile getCurrentSelectTile() {
         return myCurrentSelectTile;
     }
 
     public void ZoomIn() {
         myMapPanel.ZoomIn();      
     }
 
     public void ZoomOut() {
         myMapPanel.ZoomOut();      
     }
 
     public void clear() {
         myMapPanel.clear();
         myLayerPanel.clear();
 
     }
 
     public void remove(boolean b) {
         myMapPanel.setRemoveFlag(b);       
     }
 
     public MapPanel getMapPanel() {
         return myMapPanel;
     }
     
     public void setMode(int mode) {
        myMapPanel.setMode(mode);       
     }
     
     public void addLayer() {
        myMapPanel.addLayer();
       myLayerPanel.addLayer(myMapPanel.getCurrentLayer());
        
     }
     
     public void removeLayer() {
       myMapPanel.removeLayer();
       myLayerPanel.removeLayer(myMapPanel.getCurrentLayer()); 
     }
 
     
     public static void main(String[] argv) {
         ResourceManager.getInstance().registerResourceLoader(new ImageLoader());
         ResourceManager.getInstance().setResourceBase("/vooga/rts/leveleditor/resource/");
         
         new Canvas();
     }
 
 
 
 
 }
