 package game_world;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import map.Location;
 import map.Map;
 import map.MapThread;
 import map.RandomMazeMap;
 import mobiles.AbstractMobile;
 import mobiles.DummyMobile;
 import player.Player;
 import display.Graphics;
 import event_handler.EventManager;
 import event_handler.InteractionEvent;
 
 /**
  * Manages the map, catches all input events, bundles them off to to
  * the event manager.
  * 
  * Also instantiates the portal manager - is the gameplay manager.
  * @author Nathaiel, Snean
  *
  */
 public class GameWorld implements KeyListener, MouseListener {
 	Calendar cal;
 	long keyTimer = 300;
 	long timer=0;
 	java.util.Date date;
 	Player player;
 	EventManager eventManager;
 	PortalManager portalManager;
 	static GameWorld instance;
 	MapThread r;
 	InteractionEvent ie;
 	ArrayList<AbstractMobile> mobiles;
 	AbstractMobile[] mobs;
 	Graphics graphics;
 	Map map;
 	int maxMobs =100;
 	// sean insisted.
 	/**
 	 * GameWorld - this guy is in charge of everything.
 	 * Catches events (key, mouse), Manages the map, and layers, all listeners.
 	 * Orders them.
 	 * @return
 	 */
 	public static GameWorld getInstance() {
 		if (instance == null) {
 			instance = new GameWorld();
 		}
 		return instance;
 	}
 
 	private GameWorld() {
 		cal = Calendar.getInstance();
 		timer = cal.getTimeInMillis();
 		eventManager = new EventManager();
 		portalManager = new PortalManager();
 		mobiles = new ArrayList<AbstractMobile>();
 
 		map = new RandomMazeMap(25, 25, 10, 10);
 		Graphics graphics = new Graphics(map.getMap());
 
 		for(int i = 0; i<maxMobs; i++) {
 			Location loc = map.getPlaceableLocation();
 			if(loc!=null) {
 				mobiles.add(new DummyMobile(loc.getX(),loc.getY(), map));
 			}
 		}
 		Location pS = map.getPlayerStart();
 		player = new Player(pS.getX(), pS.getY(), map);
 		r = new MapThread();
 		r.setMap(map);
 		r.setPlayer(player);
 		r.setMobiles(mobiles);
 		r.setGraphics(graphics);
 		graphics.setKeyListener(this);
 		graphics.setMouseListener(this);
 
 		Thread t= new Thread(r);
 		t.start();
 		eventManager.setPlayer(player);
 
 		//map.printMap();
 	}
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		//	handleEvent( e);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		//	handleEvent( e);
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		//	handleEvent( e);
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		//	handleEvent( e);
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		//	handleEvent( e);
 
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		handleEvent( e);
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		handleEvent( e);
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		handleEvent( e);
 
 	}
 	private void handleEvent(MouseEvent toHandle) {
 		// TODO Auto-generated method stub
 		ie = new InteractionEvent(0, toHandle, null);
 		eventManager.handleEvent(ie);
 	}
 	private void handleEvent(KeyEvent toHandle) {
 		// TODO Auto-generated method stub
 
		ie = new InteractionEvent(InteractionEvent.KYB, null, toHandle);
		//player.acceptInteraction(ie);KYB
 		eventManager.handleEvent(ie);
 	}
 	public void refresh() {
 		//		r.setMap(map.addPortals(new Portal[]{player}, map.getCopyMap()));
 	}
 }
