 package pintosim;
 
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.File;
 import java.io.IOException;
 import java.awt.Point;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.imageio.ImageIO;
 
 public class GUISim {
 	private static final String IMAGE_MAP_FILENAME = "maps/30x30-lots-of-items.png";	
         private static final String GRAPHICS_PACKAGE_DIR = "graphics/standard/";
         private static final int    CANVAS_RENDERING_FREQUENCY_MS = 10;
         private static final int    GAME_UPDATE_FREQUENCY_MS = 80;
         
 	public static void main(String[] args) {
 		showGUIInterface();
 	}
 	
 	public static void showGUIInterface() {
         MapFeatures mapFeatures;
         try {
             mapFeatures = new ImageMapAnalyzer(new File(IMAGE_MAP_FILENAME));
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
         
         EnviornmentMap map = new EnviornmentMap(mapFeatures);
         PintoManager pintoManager = new PintoManager(map);
         PathFinder pathFinder = new DijkstraPathFinder(map);
 
         Image pintoGraphic = null;
         Image floorGraphic = null;
         Image wallGraphic = null;
         Image itemGraphic = null;
         Image dockingGraphic = null;
         Image personGraphic = null;
         try {
             pintoGraphic = ImageIO.read(new File(GRAPHICS_PACKAGE_DIR + "pinto.png"));
             floorGraphic = ImageIO.read(new File(GRAPHICS_PACKAGE_DIR + "floor.png"));
             wallGraphic = ImageIO.read(new File(GRAPHICS_PACKAGE_DIR + "wall.png"));
             itemGraphic = ImageIO.read(new File(GRAPHICS_PACKAGE_DIR + "item.png"));
             dockingGraphic = ImageIO.read(new File(GRAPHICS_PACKAGE_DIR + "docking.png"));
             personGraphic = ImageIO.read(new File(GRAPHICS_PACKAGE_DIR + "person.png"));
             
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         final AnimPanel animPanel = new AnimPanel(map.getWidth() *20, map.getHeight()*20, CANVAS_RENDERING_FREQUENCY_MS);
 
         int tileSize = 20;
         // create sprites for the walls and floor
         boolean[][] matrix = mapFeatures.getFreeSpaceMatrix();
         for (int i=0; i < matrix.length; i++) {
             for (int j=0; j<matrix[i].length; j++) {
                 animPanel.addPaintable(new Sprite(
                     matrix[i][j] ? floorGraphic : wallGraphic
                   , new Point(i * tileSize, j * tileSize)
                   , 0
                 ));
             }
         }
 
         Tween tween = new EaseLinear();
 
         //add pintos
         List<Pinto> pintos = new ArrayList<Pinto>();
         for (Point pintoLocation : mapFeatures.getPintoLocations()) {
             Pinto pinto = new Pinto(pintoLocation, map, pintoManager, pathFinder);
             map.trackObject(pinto);
             pintoManager.addPinto(pinto);
             
             Point initialLocation = new Point(pintoLocation.x * 20, pintoLocation.y * 20);
             
             MovingSprite sprite = new MovingSprite(pintoGraphic, initialLocation, 10);
             pinto.addLocationChangeListeners(new TransitionCreator(GAME_UPDATE_FREQUENCY_MS, tileSize, sprite, tween));
             animPanel.addPaintable(sprite);
         }
         
         //add items, naming them a1, a2, a3....
         List<Item> items = new ArrayList<Item>();
         int itemSerial = 1;
         for (Point itemLocation : mapFeatures.getItemLocations()) {
             String itemName = "a" + itemSerial++;
             Item item = new Item(itemName, itemLocation.x, itemLocation.y);
             map.trackItem(item);
 
             Point initialLocation = new Point(itemLocation.x * 20, itemLocation.y * 20);
             
             MovingSprite sprite = new MovingSprite(itemGraphic, initialLocation, 5);
            item.addLocationChangeListeners(new TransitionCreator(
                    GAME_UPDATE_FREQUENCY_MS, tileSize, sprite, tween));
             animPanel.addPaintable(sprite);
         }
         
         Point initialLocation = mapFeatures.getPintoDockingStationLocation();
        animPanel.addPaintable(new Sprite(dockingGraphic,
                new Point(initialLocation.x * 20, initialLocation.y * 20), 5));
         
         initialLocation = mapFeatures.getPersonLocation();
        animPanel.addPaintable(new Sprite(personGraphic,
                new Point(initialLocation.x * 20, initialLocation.y * 20), 5));
 
         animPanel.addPaintable(new FramesPerSecond());
         map.trackObject(new Person(mapFeatures.getPersonLocation()));
         final PintoManager pm = pintoManager;
         Timer t = new Timer(true);
         TimerTask task = new TimerTask() {
             public void run() {
                 pm.work();
             }
         };
         t.scheduleAtFixedRate(task, 0, GAME_UPDATE_FREQUENCY_MS);
 
         new GUI(pintoManager, map, animPanel);
 	}
 }
