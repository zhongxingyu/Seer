 package pintosim;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.io.File;
 import java.io.IOException;
 import java.awt.Point;
 
 public class GUISim {
 	private static final String IMAGE_MAP_FILENAME = "maps/30x30-lots-of-items.png";	
 	
 	public static void main(String[] args) {
 		showGUIInterface();
 	}
 	
 	public static void showGUIInterface() {
 		MapFeatures mf;
 		try {
 			mf = new ImageMapAnalyzer(new File(IMAGE_MAP_FILENAME));
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return;
 		}
 		
 		EnviornmentMap map = new EnviornmentMap(mf);
 		PintoManager pintoManager = new PintoManager(map);
 		PathFinder pathFinder = new DijkstraPathFinder(map);
 		
		// Add pintos
         List<Pinto> pintos = new ArrayList<Pinto>();
         for (Point pintoLocation : mf.getPintoLocations()) {
             Pinto pinto = new Pinto(pintoLocation, map, pintoManager, pathFinder);
             map.trackObject(pinto);
             pintoManager.addPinto(pinto);
         }
         
         //add items, naming them a1, a2, a3....
         List<Item> items = new ArrayList<Item>();
         int itemSerial = 1;
         for (Point itemLocation : mf.getItemLocations()) {
             String itemName = "a" + itemSerial++;
             Item item = new Item(itemName, itemLocation.x, itemLocation.y);
             map.trackItem(item);
         }
         map.trackObject(new Person(mf.getPersonLocation()));
 		
        new GUI(pintoManager, map);
 	}
 }
