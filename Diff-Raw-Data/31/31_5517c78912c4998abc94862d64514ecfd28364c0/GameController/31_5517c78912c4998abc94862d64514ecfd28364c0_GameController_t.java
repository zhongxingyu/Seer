 package vooga.rts.controller;
 
 import java.awt.AWTException;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Robot;
 import java.awt.geom.Rectangle2D;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import vooga.rts.gamedesign.Weapon;
 import vooga.rts.gamedesign.factories.Factory;
 import vooga.rts.gamedesign.sprite.rtsprite.Projectile;
 import vooga.rts.gamedesign.sprite.rtsprite.Resource;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.buildings.Barracks;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.buildings.Building;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.buildings.UpgradeBuilding;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.units.Soldier;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.units.Unit;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.units.Worker;
 import vooga.rts.gamedesign.strategy.attackstrategy.CanAttack;
 import vooga.rts.gamedesign.strategy.attackstrategy.CannotAttack;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 import vooga.rts.gui.Window;
 import vooga.rts.input.PositionObject;
 import vooga.rts.map.GameMap;
 import vooga.rts.player.HumanPlayer;
 import vooga.rts.player.Player;
 import vooga.rts.player.Team;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Camera;
 import vooga.rts.util.Location;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.PointTester;
 import vooga.rts.util.Sound;
 
 
 public class GameController extends AbstractController {
 
     private Map<Integer, Team> myTeams;
     private List<Player> myPlayers;
     private HumanPlayer myHuman;
 
     private GameMap myMap; // This needs a dimension that describes the total size of the map. Not
                            // made for now.
     private Resource r; 
 	private Building building;
 	private UpgradeBuilding upgradeBuilding;
     private Location myLeftMouse;
     private Location myLeftMouseWorld;
     
     private Rectangle2D myDrag;
 
     private PointTester pt;
 
     private Robot myMouseMover = null;
 
     public GameController () {
         myTeams = new HashMap<Integer, Team>();
         myPlayers = new ArrayList<Player>();
         myMap = new GameMap(8, new Dimension(512, 512));
         pt = new PointTester();
         try {
             myMouseMover = new Robot();
         }
         catch (AWTException e) {
             // Cannot move the camera
         }
     }
 
     public void addPlayer (Player player, int teamID) {
         myPlayers.add(player);
         if (myTeams.get(teamID) == null) {
             addTeam(teamID);
         }
         myTeams.get(teamID).addPlayer(player);
         if (player instanceof HumanPlayer) {
             myHuman = (HumanPlayer) player;
         }
     }
 
     public void addTeam (int teamID) {
         myTeams.put(teamID, new Team(teamID));
     }
 
     public void connect (/* NetworkGameInfo n */) {
 
     }
 
     @Override
     public void update (double elapsedTime) {
         for (Player f : myPlayers) {
             f.update(elapsedTime);
         }
         /*
          * Iterator<Team> it = myTeams.values().iterator();
          * while (it.hasNext()) {
          * 1Team t = it.next();
          * 
          * }
          */
         List<Unit> p1 = myTeams.get(1).getUnits();
         List<Unit> p2 = myTeams.get(2).getUnits();
         for (Unit u1 : p1) {
 			for (Unit u2 : p2) {
 				if (u1.inRange(u2)) {
 					u2.getAttacked(u1);
 				}
 				if (u2.inRange(u1)) {
 					u1.getAttacked(u2);
 					
 				}
 				if (u1 instanceof Worker)
 				{
 					((Worker)u1).gather(r);
 				}
 			}
 		}
 		building.update(elapsedTime);
 		//upgradeBuilding.update(elapsedTime);
         checkCameraMouse();
     }
 
     @Override
     public void paint (Graphics2D pen) {
         for (Player p : myPlayers) {
             p.paint(pen);
         }
         r.paint(pen);
         building.paint(pen);
         
         if (myDrag != null) {
            pen.draw(myDrag);      
         }
         pt.paint(pen);
     }
 
     @Override
     public void onLeftMouseDown (PositionObject o) {
         myLeftMouse = new Location(o.getPoint2D());
         myLeftMouseWorld = Camera.instance().viewtoWorld(o.getPoint2D()).to2D();
     }
 
     @Override
     public void onLeftMouseUp (PositionObject o) {
         // if it's not a gui thing
 
         if (myDrag == null) {
             Location3D worldClick = Camera.instance().viewtoWorld(o.getPoint2D());
             myHuman.handleLeftClick((int) worldClick.getX(), (int) worldClick.getY());
         }
         myLeftMouse = null;
         myDrag = null;
     }
 
     @Override
     public void onRightMouseDown (PositionObject o) {
 
     }
 
     @Override
     public void onRightMouseUp (PositionObject o) {
 
         // If it's not a GUI thing
         Location3D world = Camera.instance().viewtoWorld(o.getPoint2D()); 
         myHuman.handleRightClick((int) world.getX(), (int) world.getY());
     }
 
     @Override
     public void onMouseDrag (PositionObject o) {
         if (myLeftMouse != null) {
             Location3D world = Camera.instance().viewtoWorld(o.getPoint2D());
             
             double uX = world.getX() > myLeftMouseWorld.getX() ? myLeftMouseWorld.getX() : world.getX();
             double uY = world.getY() > myLeftMouseWorld.getY() ? myLeftMouseWorld.getY() : world.getY();
             double width = Math.abs(world.getX() - myLeftMouseWorld.getX());
             double height = Math.abs(world.getY() - myLeftMouseWorld.getY());
             Rectangle2D worldDrag = new Rectangle2D.Double(uX, uY, width, height);
             
             uX = o.getX() > myLeftMouse.getX() ? myLeftMouse.getX() : o.getX();
             uY = o.getY() > myLeftMouse.getY() ? myLeftMouse.getY() : o.getY();
             width = Math.abs(o.getX() - myLeftMouse.getX());
             height = Math.abs(o.getY() - myLeftMouse.getY());
             myDrag = new Rectangle2D.Double(uX, uY, width, height);
            System.err.println(worldDrag);
             myHuman.getUnits().select(worldDrag);
         }
     }
 
     public void activate () {
         setupGame();
     }
 
     private void setupGame () {
 		System.out.println("Game is starting setup");
 		
 		try {
 			//Factory factory = new Factory();
 
 			//UpgradeTree resultTree = factory.loadXMLFile("XML_Sample");
 			/*
 			upgradeBuilding = new UpgradeBuilding(new Pixmap(ResourceManager.instance().loadFile("images/barracks.jpeg")), 
 					new Location(700,700), new Dimension(150,150), null, 1,300);
 			*/
 			Player p1 = new HumanPlayer();
 			Pixmap p = new Pixmap(ResourceManager.instance().loadFile("images/sprites/soldier.png"));
 			Dimension s = new Dimension(90, 90);
 			r = new Resource(new Pixmap(ResourceManager.instance().loadFile("images/mineral.gif")), new Location3D (300,300,0), new Dimension(60, 60), 0, 80);
 			Sound soun = null;// new Sound("/vooga/rts/sounds/pikachu.wav");
 			Unit a = null;
 			a = new Soldier(p, new Location3D(200, 250 ,0), s, soun, 1, 400);
 			System.out.println("Player ID for a: " + a.getPlayerID());
 			//a.setUpgradeTree(resultTree,a.getPlayerID());
 			//upgradeBuilding.addUpgradeActions(resultTree);
 			Projectile proj =
 					new Projectile(new Pixmap(ResourceManager.instance()
 							.loadFile("images/bullet.png")), a.getWorldLocation(), new Dimension(30, 30),
 							2, 10, 1);
 			a.setAttackStrategy(new CanAttack());
 			a.getAttackStrategy().addWeapons(new Weapon(0, proj, 200, a.getWorldLocation(), 25));
 			Unit b = new Soldier(p, new Location3D(300, 150,0), s, soun, 1, 800);
 			System.out.println("Player ID for b: " + b.getPlayerID());
 
 			System.out.println("Game is setup 1");
 			Projectile proj2 =
 					new Projectile(
 							new Pixmap(ResourceManager.instance().loadFile("images/bullet.png")),
 							b.getWorldLocation(), new Dimension(30, 30), 1, 10, 1);
 			b.setAttackStrategy(new CannotAttack());
 			b.getAttackStrategy().addWeapons(new Weapon(0, proj2, 200, b.getWorldLocation(), 50));
 
 			Unit c = new Soldier(p, new Location3D(500, 300,0), s, soun, 2, 300);
 
 			Projectile proj3 =
 					new Projectile(
 							new Pixmap(ResourceManager.instance().loadFile("images/bullet.png")),
 							c.getWorldLocation(), new Dimension(30, 30), 1, 10, 1);
 			c.setAttackStrategy(new CannotAttack());
 			Unit w = new Worker(new Pixmap(ResourceManager.instance().loadFile("images/scv.gif")), new Location3D(500, 200,0), s, soun, 20, 40, 40);
 			c.getAttackStrategy().addWeapons(new Weapon(0, proj3, 200, c.getWorldLocation(), 50));
 			
 			p1.getUnits().addUnit(a);
 			p1.getUnits().addUnit(b);
 			p1.getUnits().addUnit(w);
 			Player p2 = new HumanPlayer();
 			p2.getUnits().addUnit(c);
 
 			addPlayer(p1, 1);
 			addPlayer(p2, 2);
 			
 			building = new Barracks(new Pixmap(ResourceManager.instance().loadFile("images/barracks.jpeg")), 
 					new Location3D(800,500,0), new Dimension(150,150), null, 1,300);
 			System.out.println("Setup Game");
 			myHuman = (HumanPlayer) p1;
 
 		}
 		catch (Exception e) {
 			// trollolol
 		}
 		
 		
     }
 
     private void checkCameraMouse () {
         Point p = MouseInfo.getPointerInfo().getLocation();
 
         double x = 0;
         double y = 0;
         double setX = p.getX();
         double setY = p.getY();
 
         if (p.getX() <= 0) {
             x = -1 * Camera.MOVE_SPEED;
             setX = 0;
         }
         if (p.getY() <= 0) {
             y = -1 * Camera.MOVE_SPEED;
             setY = 0;
         }
         if (p.getX() >= Window.SCREEN_SIZE.getWidth() - 1) {
             x = 1 * Camera.MOVE_SPEED;
             setX = Window.SCREEN_SIZE.getWidth() - 1;
         }
 
         if (p.getY() >= Window.SCREEN_SIZE.getHeight() - 1) {
             y = 1 * Camera.MOVE_SPEED;
             setY = Window.SCREEN_SIZE.getHeight() - 1;
         }
         if (x != 0 || y != 0) {
             Camera.instance().moveCamera(new Location(x, y));
             myMouseMover.mouseMove((int) setX, (int) setY);
         }
     }
 
     @Override
     public void onMouseMove (PositionObject o) {
 
     }
 
     @Override
     public MainState getGameState () {
         return MainState.Game;
     }
 
 }
