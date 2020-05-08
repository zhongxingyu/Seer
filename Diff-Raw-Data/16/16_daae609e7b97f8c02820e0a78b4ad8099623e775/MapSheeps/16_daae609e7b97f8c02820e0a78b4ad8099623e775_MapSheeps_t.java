 package map;
 
 import main.Register;
 import model.Sheep;
 import model.SheepHistory;
 import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
 import util.GeneralUtil;
 import util.Vec2;
 
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.TreeMap;
 
 public class MapSheeps
 {
 	private Register register;
 	private ArrayList <Sheep> currentSheeps;
     private MapViewer map;
     private ArrayList<MapMarkerDot> mapMarkers = null;
     private TreeMap<Integer, MapMarkerDot> dotTreeMap = null;
 	
 	public MapSheeps(Register register, final MapViewer map)
 	{
         this.register = register;
 		this.currentSheeps = new ArrayList<Sheep>();
 
         this.map = map;
         //Sets the center of the map
         this.map.setMapCenter(getFarmerCenter());
 
         //Gets current sheep.
 		setSheeps();
 
         map.addListener(new MapSheepsListener());
 	}
 
     private class MapSheepsListener implements MapViewer.MapViewerListener{
         @Override
         public void nodeClicked(MapViewer.NodeInfo n) {
             mapMarkers = map.getMapMarkers();
             dotTreeMap = map.getDotId();
         }
 
         @Override
         public void mapClicked(double x, double y) {
             //Do nothing
         }
     }
 
     /**
      * Returns sheep from MapMarkerDot.
      * @param dot
      * @return
      */
     public Sheep getSheepByDot(MapMarkerDot dot){
         for(MapMarkerDot thisDot : mapMarkers){
             if(dot == thisDot){
                 return getSheepById(GeneralUtil.getKeyByValue(dotTreeMap, dot));
             }
         }
         return null;
     }
 
     /**
      * Returns sheep by sheepid.
      * @param id
      * @return
      */
     public Sheep getSheepById(int id){
         for(Sheep sheep : currentSheeps){
             if(sheep.getId() == id)
                 return sheep;
         }
         return null;
     }
 
     /**
      * Gets farmer center from register.
      * @return
      */
     public Vec2 getFarmerCenter(){
         return register.getFarmerLocation();
     }
 
 
     /**
      * Refreshes map.
      */
     public void refresh(){
         this.setSheeps();
         this.setCurrentSheepPositions();
     }
 
     /**
      * adds all of the farmers sheep to currentSheeps.
      */
 	public void setSheeps ()
 	{
 		currentSheeps = register.getAllSheeps();
 	}
 
 
     /**
      * Adds the current position of all the sheep to the map.
      *
      */
     public void setCurrentSheepPositions(){
         map.removeMarkers();
 
         double lat, lon;
         int counter = 0;
         ArrayList<Vec2> positions = register.getSheepPositions();
 
         for (Vec2 v : positions){
             lat = v.x;
             lon = v.y;
             map.addMarker(currentSheeps.get(counter).getName(), lat, lon, currentSheeps.get(counter).getId());
             counter += 1;
         }
     }
 
     /**
      * Finds the three last positions of sheep and adds them to map.
      * @param sheepid
      */
     public void setHistoricSheepPosition(int sheepid){
         map.removeMarkers();
 
         double lat, lon;
         Color color = null;
 
         SheepHistory history = register.getSheepHistory(sheepid);
         if(history != null){
             TreeMap<Long, Vec2> sheepHistory = history.getHistory();
             Collection<Vec2> pairs = sheepHistory.values();
             Vec2[] array = new Vec2[pairs.size()];
             pairs.toArray(array);
 
             //Iterates over Vec2[] array to find the three latest location of sheep
            for (int i = 3; i > 0 && i < array.length; i--){
                Vec2 position = array[array.length-i];
                 lat = position.x;
                 lon = position.y;
 
                //System.out.println("[Debug MapSheep History]: " + "Latitude: " + lat + ". Longitude:" + lon + ". Entry number: " + i);
 
                 switch(i){
                   case 1:
                        color = Color.GREEN;
                        break;
                   case 2:
                        color = Color.YELLOW;
                        break;
                   case 3:
                        color = Color.RED;
                        break;
                 }
                 map.addMarker(lat,lon,color);
 
             }
         }else{
             Vec2 pos = register.getSheepById(sheepid).getPos();
             map.addMarker(pos.x,pos.y,Color.GREEN);
         }
     }
 
 }
