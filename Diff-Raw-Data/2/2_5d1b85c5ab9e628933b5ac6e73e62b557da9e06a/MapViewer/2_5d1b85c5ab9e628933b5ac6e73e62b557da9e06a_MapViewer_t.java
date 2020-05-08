 package map;
 
 import org.openstreetmap.gui.jmapviewer.*;
 import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
 import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
 import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
 import util.Vec2;
 
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 public class MapViewer extends MouseAdapter implements JMapViewerEventListener, MouseListener {
 
     private JMapViewerTree treeMap = null;
     private JMapViewer map = null;
 
     private int defaultZoom = 10;
    private double mapCenterX, mapCenterY;
 
     //ArrayList of all current dots.
     private ArrayList<MapMarkerDot> mapDots = null;
     private ArrayList<String> mouseDotName = null;
     private TreeMap<Integer, MapMarkerDot> dotId = null;
 
     //LinkedList to keep track of listeners
     private ArrayList<MapViewerListener> listeners = null;
 
     private DecimalFormat df = null;
 
     //variables to keep track of where the mouse is
     private double mouseX = 0;
     private double mouseY = 0;
 
     /**
      * Constructor
      */
     public MapViewer(){
 
         treeMap = new JMapViewerTree("Zones");
         map = this.getMap();
 
         mapDots = new ArrayList<MapMarkerDot>();
         mouseDotName = new ArrayList<String>();
         dotId = new TreeMap<Integer, MapMarkerDot>();
         listeners = new ArrayList<MapViewerListener>();
 
         //Sets the map type.
         map.setTileSource(new OsmTileSource.CycleMap());
 
         // Listen to the map viewer for user operations so components will
         // receive events and update
         map.addJMVListener(this);
 
         //Sets the movement mouse button to mouse1
         final DefaultMapController mapController = new DefaultMapController(map);
         mapController.setMovementMouseButton(MouseEvent.BUTTON1);
         mapController.setDoubleClickZoomEnabled(false);
 
         map.addMouseListener(new MapListener());
 
     }
 
     private class MapListener extends MouseAdapter{
         public void mouseClicked(MouseEvent e) {
             if (e.getButton() == MouseEvent.BUTTON1) {
                 Coordinate pos = map.getPosition(e.getX(), e.getY());
                 mouseX = pos.getLat();
                 mouseY = pos.getLon();
                 for(MapViewerListener listen : listeners){
                     listen.mapClicked(mouseX, mouseY);
                 }
                 setMouseDotName();
 
             }
         }
     }
 
     /**
      * Adds the dots from the (x,y) location of the mouse to an ArrayList.
      */
     public void setMouseDotName(){
 
         if(!mouseDotName.isEmpty()){
             mouseDotName.clear();
         }
 
         for (MapMarkerDot d : mapDots){
 
             //Sets decimalformat based on zoom.
             //min zoom = 0, max zoom = 18.
             int zoom = map.getZoom();
 
             if(zoom >= 0 && zoom < 5){
                 df = new DecimalFormat("#");
             }
             else if (zoom >= 5 && zoom < 10){
                 df = new DecimalFormat("#.0");
             }
             else if (zoom >= 10 && zoom < 15){
                 df = new DecimalFormat("#.00");
             }
             else if (zoom == 15){
                 df = new DecimalFormat("#.000");
             }
             else if (zoom >= 16 && zoom <= 18){
                 df = new DecimalFormat("#.0000");
             }
 
             String dotLat = df.format(d.getLat());
             String dotLon = df.format(d.getLon());
             String mouseLat = df.format(mouseX);
             String mouseLon = df.format(mouseY);
 
             if (dotLat.equals(mouseLat) && dotLon.equals(mouseLon)){
 
                 mouseDotName.add(d.getName());
                 System.out.println("Name: " + d.getName() + ". Zoom Level: " + map.getZoom());
 
                 NodeInfo nodeInfo = new NodeInfo(d.getName(), dotLat, dotLon, d);
 
                 for (MapViewerListener mvl : listeners){
                     mvl.nodeClicked(nodeInfo);
                 }
 
             }
         }
     }
 
     /**
      * NodeInfo class. Used to keep track of the node info.
      */
     public static class NodeInfo{
         String nodeName;
         String dotLat;
         String dotLon;
         MapMarkerDot dot;
 
         /**
          * Constructor
          * @param nodeName
          * @param dotLat
          * @param dotLon
          */
         public NodeInfo(String nodeName, String dotLat, String dotLon, MapMarkerDot dot){
             this.nodeName = nodeName;
             this.dotLat = dotLat;
             this.dotLon = dotLon;
             this.dot = dot;
         }
 
         /**
          * Helper function to get node name from NodeInfo
          * @return
          */
         public String getNodeName(){
             return this.nodeName;
         }
 
         /**
          * Helper function to get Latitude from NodeInfo
          * @return
          */
         public String getDotLat(){
             return this.dotLat;
         }
 
         /**
          * Helper function to get Longitude from NodeInfo
          * @return
          */
         public String getDotLon(){
             return this.dotLon;
         }
 
         public MapMarkerDot getDot(){
             return this.dot;
         }
     }
 
     /**
      *  Listener interface
      */
     public static interface MapViewerListener{
         public void nodeClicked(NodeInfo n);
         public void mapClicked(double x, double y);
     }
 
     /**
      * Add listener to "listeners"  LinkedList.
      * @param mvl
      */
     public void addListener(MapViewerListener mvl){
         listeners.add(mvl);
     }
 
     /**
      * Process commands from the JMapViewerTree
      * @param jmvCommandEvent
      */
     @Override
     public void processCommand(JMVCommandEvent jmvCommandEvent) {
     }
 
     /**
      * Get the viewer of the map
      *
      * @return
      */
     public JMapViewer getMap(){
         return treeMap.getViewer();
     }
 
     /**
      * Returns list of active Map Markers.
      * @return
      */
     public ArrayList<MapMarkerDot> getCurrentMapMarkers(){
         return mapDots;
     }
 
     /**
      * Helper function to set the center of the map
      * @param position
      */
     public void setMapCenter(Vec2 position){
        mapCenterX = position.x;
        mapCenterY = position.y;
        map.setDisplayPositionByLatLon(mapCenterX ,mapCenterY ,defaultZoom);
     }
 
     /**
      * Helper function for creating coordinates
      * @param lat
      * @param lon
      * @return
      */
     private static Coordinate c(double lat, double lon) {
         return new Coordinate(lat, lon);
     }
 
     /**
      * Helper function to add MapMarkers.
      * @param name
      * @param lat
      * @param lon
      * @param id
      */
     public void addMarker(String name, double lat, double lon, int id){
         MapMarkerDot dot = new MapMarkerDot(name,c(lat,lon));
         mapDots.add(dot);
         dotId.put(id,dot);
 
         map.addMapMarker(dot);
     }
 
     public TreeMap<Integer, MapMarkerDot> getDotId(){
         if (!dotId.isEmpty()){
             return dotId;
         }
             return null;
     }
 
     /**
      * Helper function to add MapMarker with different background color
      * @param lat
      * @param lon
      * @param color
      */
     public void addMarker(double lat, double lon, Color color){
         MapMarkerDot dot = new MapMarkerDot(c(lat,lon));
         dot.setBackColor(color);
         map.addMapMarker(dot);
     }
 
     /**
      * Removes all MapMarkers from the map
      */
     public void removeMarkers(){
        mapDots.clear();
        map.removeAllMapMarkers();
     }
 
 
     /**
      * Set MapMarker visibility
      * @param visibility
      */
     public void setMarkerVisiblity(boolean visibility){
         map.setMapMarkerVisible(visibility);
     }
 
     public ArrayList<MapMarkerDot> getMapMarkers(){
         return this.mapDots;
     }
 
 }
