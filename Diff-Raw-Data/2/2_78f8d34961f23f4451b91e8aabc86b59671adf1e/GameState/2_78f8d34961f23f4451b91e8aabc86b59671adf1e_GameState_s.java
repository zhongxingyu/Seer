 package vooga.rts.state;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observer;
 import javax.xml.parsers.ParserConfigurationException;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.DragCommand;
 import vooga.rts.controller.Controller;
 import vooga.rts.gamedesign.sprite.gamesprites.Projectile;
 import vooga.rts.gamedesign.sprite.gamesprites.Resource;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Unit;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Building;
 import vooga.rts.gamedesign.state.UnitState;
 import vooga.rts.gamedesign.strategy.attackstrategy.CanAttack;
 import vooga.rts.gamedesign.strategy.gatherstrategy.CanGather;
 import vooga.rts.gamedesign.strategy.occupystrategy.CanBeOccupied;
 import vooga.rts.gamedesign.sprite.map.Terrain;
 import vooga.rts.gamedesign.strategy.production.CanProduce;
 import vooga.rts.gamedesign.weapon.Weapon;
 import vooga.rts.leveleditor.components.MapLoader;
 import vooga.rts.manager.PlayerManager;
 import vooga.rts.map.GameMap;
 import vooga.rts.player.HumanPlayer;
 import vooga.rts.player.Player;
 import vooga.rts.player.Team;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Camera;
 import vooga.rts.util.DelayedTask;
 import vooga.rts.util.FrameCounter;
 import vooga.rts.util.Information;
 import vooga.rts.util.Location;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.PointTester;
 
 
 /**
  * The main model of the game. This keeps track of all the players, the
  * humanplayer associated with the local game, plus the map.
  * 
  * @author Challen Herzberg-Brovold
  * 
  */
 
 public class GameState extends SubState implements Controller {
     
     private static GameMap myMap;
     private static PlayerManager myPlayers;
             
     private List<DelayedTask> myTasks;
     private FrameCounter myFrames;
 
     private Rectangle2D myDrag;
 
     public GameState (Observer observer) {
         super(observer);
         
         MapLoader ml = null;
         try {
             ml = new MapLoader();
            ml.loadMapFile("/vooga/rts/tests/maps/ciemas/ciemas.xml");
         }
         catch (ParserConfigurationException e) {
         }
         catch (Exception e1) {
         }       
         myMap = ml.getMyMap();
         
         // myMap = new GameMap(new Dimension(4000, 2000), true);
         myPlayers = new PlayerManager();
         // myMap = new GameMap(8, new Dimension(512, 512));
 
         myFrames = new FrameCounter(new Location(100, 20));
         myTasks = new ArrayList<DelayedTask>();
         setupGame();
     }
 
     @Override
     public void update (double elapsedTime) {
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
         pen.setBackground(Color.BLACK);
         myMap.paint(pen);
         getPlayers().getHuman().paint(pen);
 
         if (myDrag != null) {
             pen.draw(myDrag);
         }
         
         Camera.instance().paint(pen);
         myFrames.paint(pen);
     }
 
     @Override
     public void receiveCommand (Command command) {
         // If it's a drag, we need to do some extra checking.
         if (command instanceof DragCommand) {
             myDrag = ((DragCommand) command).getScreenRectangle();
             if (myDrag == null) {
                 return;
             }
         }
         sendCommand(command);
     }
 
     @Override
     public void sendCommand (Command command) {
         getPlayers().getHuman().sendCommand(command);
     }
 
     
     
     public void setupGame () {
         getPlayers().addPlayer(1);
 
         Unit worker =
                 new Unit(new Pixmap(ResourceManager.getInstance()
                         .<BufferedImage> getFile("images/scv.gif", BufferedImage.class)),
                          new Location3D(100, 100, 0), new Dimension(75, 75), null, 1, 200, 40, 150);
         worker.setGatherStrategy(new CanGather());
         Information i1 =
                 new Information("Worker",
                                 "I am a worker. I am sent down from Denethor, son of Ecthelion ",
                                 null, "images/scv.png");
         worker.setInfo(i1);
         getPlayers().getHuman().add(worker);
         Unit a = new Unit();
         a.setAttackStrategy(new CanAttack(a.getWorldLocation(), a.getPlayerID()));
         a.getEntityState().setUnitState(UnitState.ATTACK);
         Projectile proj =
                 new Projectile(new Pixmap(ResourceManager.getInstance()
                         .<BufferedImage> getFile("images/bullet.png", BufferedImage.class)),
                                a.getWorldLocation(), new Dimension(10, 10), 2, 10, 6, 800);
         a.getAttackStrategy().addWeapon(new Weapon(proj, 400, a.getWorldLocation(), 1));
         Information i2 =
                 new Information("Marine", "I am a soldier of Nunu.", null, "buttons/marine.png");
 
         a.setInfo(i2);
         getPlayers().getHuman().add(a);
         
         getPlayers().addPlayer(2);
 
         Unit c = new Unit();
         c.setWorldLocation(new Location3D(1200, 500, 0));
         c.move(c.getWorldLocation());
         c.setAttackStrategy(new CanAttack(c.getWorldLocation(), c.getPlayerID()));
         c.setHealth(150);
         // myHumanPlayer.add(c);
         getPlayers().getPlayer(1).add(c);
 
         Building b =
                 new Building(new Pixmap(ResourceManager.getInstance()
                         .<BufferedImage> getFile("images/factory.png", BufferedImage.class)),
                              new Location3D(500, 1000, 0), new Dimension(368, 224), null, 1, 300,
                              InteractiveEntity.DEFAULT_BUILD_TIME);
         b.setProductionStrategy(new CanProduce(b));
         ((CanProduce) b.getProductionStrategy()).addProducable(new Unit());
         ((CanProduce) b.getProductionStrategy()).createProductionActions(b);
         Information i =
                 new Information("Barracks", "This is a barracks that can make awesome pies", null,
                                 "buttons/marine.png");
         b.setInfo(i);
         getPlayers().getHuman().add(b);
 
         for (int j = 0; j < 10; j++) {
             getMap().getResources().add(new Resource(new Pixmap("images/mineral.gif"),
                                                      new Location3D(600 + j * 30, 600  - j * 20, 0),
                                                      new Dimension(50, 50), 0, 200, "mineral"));
         }
 
         for (int j = 0; j < 4; j++) {
             for (int k = 0; k < 8; k++) {
                 getMap().getTerrain().add(new Terrain(new Pixmap("images/gold.png"),
                                                       new Location3D(100 + k * 25, 100, j * 25),
                                                       new Dimension(50, 50)));
             }
             
         }
         Building garrison =
                 new Building(new Pixmap(ResourceManager.getInstance()
                         .<BufferedImage> getFile("images/home.png", BufferedImage.class)),
                              new Location3D(300, 450, 0), new Dimension(128, 128), null, 1, 300,
                              InteractiveEntity.DEFAULT_BUILD_TIME);
         
         Information garrisonInfo =
                 new Information("Garrison", "This is a garrison that soldiers can occupy", null,
                                 "buttons/marine.png");
         b.setInfo(garrisonInfo);
         garrison.setOccupyStrategy(new CanBeOccupied());
         garrison.getOccupyStrategy().createOccupyActions(garrison);
         getPlayers().getHuman().add(garrison);
         final Building f = b;
         myTasks.add(new DelayedTask(2, new Runnable() {
             @Override
             public void run () {
                 f.getAction((new Command("make Marine"))).apply();
             }
         }, true));
 
         final Building testGarrison = garrison;
         /*
         myTasks.add(new DelayedTask(10, new Runnable() {
             @Override
             public void run () {
                 if (testGarrison.getOccupyStrategy().getOccupiers().size() > 0) {
                     System.out.println("will puke!");
                     testGarrison.getAction(new Command("deoccupy")).apply();
                 }                
             }
         }));
         */
 
         b = new Building(new Pixmap(ResourceManager.getInstance()
                         .<BufferedImage> getFile("images/factory.png", BufferedImage.class)),
                              new Location3D(100, 500, 0), new Dimension(368, 224), null, 1, 300,
                              InteractiveEntity.DEFAULT_BUILD_TIME);
         b.setProductionStrategy(new CanProduce(b));
         ((CanProduce) b.getProductionStrategy()).addProducable(new Unit());
         ((CanProduce) b.getProductionStrategy()).createProductionActions(b);
         ((CanProduce) b.getProductionStrategy()).setRallyPoint(new Location3D(200, 600, 0));
         i =
                 new Information("Barracks", "This is a barracks that can make awesome pies", null,
                                 "buttons/marine.png");
         b.setInfo(i);
 
         final Building g = b;
         myTasks.add(new DelayedTask(2, new Runnable() {
             @Override
             public void run () {
                 g.getAction((new Command("make Marine"))).apply();
             }
         }, true));
 
         getPlayers().getPlayer(1).add(b);
     }
 
     private void yuckyUnitUpdate (double elapsedTime) {
 
         List<InteractiveEntity> p1 = getPlayers().getTeam(1).getUnits();
         List<InteractiveEntity> p2 = getPlayers().getTeam(2).getUnits();
         
         // now even yuckier
         for (int i = 0; i < p1.size(); ++i) {
             if (p1.get(i) instanceof Unit) {
                 for (int j = i + 1; j < p1.size(); ++j) {
                     ((Unit) p1.get(i)).occupy(p1.get(j));
                 }
             }
         }        
     }
     
     public static PlayerManager getPlayers() {
         return myPlayers;
     }
 
     public static GameMap getMap () {
         return myMap;
     }
 
     public static void setMap (GameMap map) {
         myMap = map;
     }
 
     @Override
     public void activate () {
         // TODO Auto-generated method stub
         
     }
 }
