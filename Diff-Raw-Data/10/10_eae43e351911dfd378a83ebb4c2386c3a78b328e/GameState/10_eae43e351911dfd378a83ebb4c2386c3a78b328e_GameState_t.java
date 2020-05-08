 package vooga.rts.state;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import javax.xml.parsers.ParserConfigurationException;
 import util.Location;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.DragCommand;
 import vooga.rts.controller.Controller;
 import vooga.rts.gamedesign.factories.Factory;
 import vooga.rts.gamedesign.sprite.gamesprites.Resource;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Building;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Unit;
 import vooga.rts.gamedesign.sprite.map.Terrain;
 import vooga.rts.leveleditor.components.MapLoader;
 import vooga.rts.manager.PlayerManager;
 import vooga.rts.map.GameMap;
 import vooga.rts.map.MiniMap;
 import vooga.rts.util.Camera;
 import vooga.rts.util.DelayedTask;
 import vooga.rts.util.FrameCounter;
 import vooga.rts.util.Information;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Scale;
 import vooga.rts.util.TimeIt;
 
 
 /**
  * The main model of the game. This keeps track of all the players, the
  * humanplayer associated with the local game, plus the map.
  * 
  * @author Challen Herzberg-Brovold
  * 
  */
 
 public class GameState extends SubState implements Controller, Observer {
     private static final Location3D DEFAULT_SOLDIER_ONE_RELATIVE_LOCATION = new Location3D(300,
                                                                                            300, 0);
     private static final Location3D DEFAULT_SOLDIER_TWO_RELATIVE_LOCATION = new Location3D(0, 500,
                                                                                            0);
     private static final Location3D DEFAULT_SOLDIER_THREE_RELATIVE_LOCATION = new Location3D(300,
                                                                                              0, 0);
     private static final Information DEFAULT_SOLDIER_INFO =
             new Information("Marine", "I am a soldier of Nunu.", "buttons/marine.png");
     private static final Location3D DEFAULT_WORKER_RELATIVE_LOCATION = new Location3D(200, 200, 0);
     private static final Information DEFAULT_WORKER_INFO =
             new Information("Worker",
                             "I am a worker. I am sent down from Denethor, son of Ecthelion ",
                             "images/scv.png");
     private static final Location3D DEFAULT_PRODUCTION_RELATIVE_LOCATION = new Location3D(000, 500,
                                                                                           0);
     private static final Information DEFAULT_PRODUCTION_INFO =
             new Information("Barracks", "This is a barracks that can make awesome pies",
                             "buttons/marine.png");
     private static final Location3D DEFAULT_OCCUPY_RELATIVE_LOCATION = new Location3D(300, 100, 0);
     private static final Information DEFAULT_OCCUPY_INFO =
             new Information("Garrison", "This is a garrison that soldiers can occupy",
                             "buttons/marine.png");
 
     private static GameMap myMap;
     private static PlayerManager myPlayers;
 
     private List<DelayedTask> myTasks;
     private FrameCounter myFrames;
 
     private Rectangle2D myDrag;
     private Factory myFactory;
 
     private MiniMap myMiniMap;
 
     private boolean isGameOver;
 
     public GameState (Observer observer) {
         super(observer);
         myFactory = new Factory();
         myFactory.loadXMLFile("Factory.xml");
 
         MapLoader ml = null;
         try {
             ml = new MapLoader();
             ml.loadMapFile("/vooga/rts/tests/maps/testmap/testmap.xml");
         }
         catch (ParserConfigurationException e) {
         }
         catch (Exception e1) {
         }
         setMap(ml.getMyMap());
         myMiniMap = new MiniMap(getMap(), new Location(50, 500), new Dimension(150, 200));
 
         // myMap = new GameMap(new Dimension(4000, 2000), true);
         myPlayers = new PlayerManager();
         // myMap = new GameMap(8, new Dimension(512, 512));
 
         myFrames = new FrameCounter(new Location(100, 20));
         myTasks = new ArrayList<DelayedTask>();
         isGameOver = false;
         setupGame();
     }
 
     @Override
     public void update (double elapsedTime) {
         if (isGameOver) {
             System.out.println("gamestate update game over");
             setChanged();
             notifyObservers();
         }
         myMap.update(elapsedTime);
         getPlayers().update(elapsedTime);
 
         for (DelayedTask dt : myTasks) {
             dt.update(elapsedTime);
         }
 
         yuckyUnitUpdate(elapsedTime);
         myFrames.update(elapsedTime);
     }
 
     @Override
     public void paint (Graphics2D pen) {
         Scale.unscalePen(pen);
         pen.setBackground(Color.BLACK);
         myMap.paint(pen);                
         myMiniMap.paint(pen);        
 
         if (myDrag != null) {
             pen.draw(myDrag);
         }
 
         Camera.instance().paint(pen);
         myFrames.paint(pen);
         Scale.scalePen(pen);
         myPlayers.getHuman().paint(pen);
     }
 
     @Override
     public void receiveCommand (Command command) {
         // If it's a drag, we need to do some extra checking.
         if (command instanceof DragCommand) {
             myDrag = ((DragCommand) command).getScreenRectangle();
             if (myDrag == null) { return; }
         }
         sendCommand(command);
     }
 
     @Override
     public void sendCommand (Command command) {
         getPlayers().getHuman().sendCommand(command);
     }
 
     public void setupGame () {
         getPlayers().addPlayer(1);
         Location3D playerOneBase = getPlayers().getHuman().getBase();
         generateInitialSprites(0, playerOneBase);
 
         getPlayers().addPlayer(2);
         Location3D playerEnemyBase = getPlayers().getPlayer(1).getEnemyBase();
         generateInitialSprites(1, playerEnemyBase);
 
         generateResources();
     }
 
     private void generateInitialSprites (int playerID, Location3D baseLocation) {
         Unit worker = (Unit) myFactory.getEntitiesMap().get("worker").copy();
         worker =
 
                 (Unit) setLocation(worker, baseLocation, DEFAULT_WORKER_RELATIVE_LOCATION);
 
         getPlayers().getPlayer(playerID).add(worker);
 
         Unit soldierOne = (Unit) myFactory.getEntitiesMap().get("Marine").copy();
         soldierOne =
 
                 (Unit) setLocation(soldierOne, baseLocation,
                                    DEFAULT_SOLDIER_ONE_RELATIVE_LOCATION);
 
         getPlayers().getPlayer(playerID).add(soldierOne);
 
         Building startProduction = (Building) myFactory.getEntitiesMap().get("home").copy();
         startProduction =
                 (Building) setLocation(startProduction, baseLocation,
                                        DEFAULT_PRODUCTION_RELATIVE_LOCATION);
         getPlayers().getPlayer(playerID).add(startProduction);
 
         Building startOccupy = (Building) myFactory.getEntitiesMap().get("garrison").copy();
         startOccupy =
                 (Building) setLocation(startOccupy, baseLocation,
                                        DEFAULT_OCCUPY_RELATIVE_LOCATION);
         getPlayers().getPlayer(playerID).add(startOccupy);
 
         // This is for testing
         final Building testGarrison = startOccupy;
 
         myTasks.add(new DelayedTask(10, new Runnable() {
             @Override
             public void run () {
                 if (testGarrison.getOccupyStrategy().getOccupiers().size() > 0) {
                     System.out.println("will puke!");
                     testGarrison.getAction(new Command("deoccupy")).apply();
                 }
             }
         }));
 
     }
 
     private InteractiveEntity setLocation (InteractiveEntity subject,
                                            Location3D base,
                                            Location3D reference) {
         subject.setWorldLocation(new Location3D(base.getX() + reference.getX(), base.getY() +
                                                                                 reference.getY(),
                                                 base.getZ() + reference.getZ()));
         subject.move(subject.getWorldLocation());
         return subject;
     }
 
     private void generateResources () {
         for (int j = 0; j < 10; j++) {
             getMap().getResources().add(new Resource(new Pixmap("images/mineral.gif"),
                                                      new Location3D(600 + j * 30, 600 - j * 20, 0),
                                                      new Dimension(50, 50), 0, 200, "mineral"));
         }
 
         for (int j = 0; j < 4; j++) {
             for (int k = 0; k < 8; k++) {
                 getMap().getTerrain().add(new Terrain(new Pixmap("images/gold.png"),
                                                       new Location3D(100 + k * 25, 100, j * 25),
                                                       new Dimension(50, 50)));
             }
         }
     }
 
     public void initializeGameOver () {
         isGameOver = true;
     }
 
     private void yuckyUnitUpdate (double elapsedTime) {
 
         List<InteractiveEntity> p1 = getPlayers().getTeam(1).getUnits();
         List<InteractiveEntity> p2 = getPlayers().getTeam(2).getUnits();
 
     }
 
     public static PlayerManager getPlayers () {
         return myPlayers;
     }
 
     public static GameMap getMap () {
         return myMap;
     }
 
     @Override
     public void update (Observable arg0, Object arg1) {
         initializeGameOver();
     }
 
     public static void setMap (GameMap map) {
         myMap = map;
     }
 
     @Override
     public void activate () {
         // TODO Auto-generated method stub
 
     }
 }
