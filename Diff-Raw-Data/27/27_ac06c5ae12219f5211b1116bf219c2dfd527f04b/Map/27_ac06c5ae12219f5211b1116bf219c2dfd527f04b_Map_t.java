 package server;
 
 import java.util.ArrayList;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 public class Map {
 	private TileContainer[][] tileMatrix;
 	private ArrayList<TileContainer> tileList;
 	private ArrayList<Player> players;
 	private ArrayList<Projectile> allProjectiles;
 	private TreeMap<Integer, Entity> entityMap;
 	// private TreeSet<Entity> changeBuffer;
 	private char[] button;
 	private boolean[] wasButton;
 	private int[] mouseId;
 	private boolean[] wasMouse;
 	private static Map instance;
 
 	public static Map getInstance() {
 		return instance;
 	}
 
 	public static void initialize(int n, int m) {
 		if (instance == null) {
 			instance = new Map();
 			instance.generateMap(n, m);
 		}
 	}
 
 	private Map() {
 		entityMap = new TreeMap<Integer, Entity>();
 		players = new ArrayList<Player>();
 		allProjectiles = new ArrayList<Projectile>();
 		// changeBuffer = new TreeSet<Entity>();
 		button = new char[Global.NUM_PLAYERS];
 		wasButton = new boolean[Global.NUM_PLAYERS];
 		mouseId = new int[Global.NUM_PLAYERS];
 		wasMouse = new boolean[Global.NUM_PLAYERS];
 	}
 
 	private void generateMap(int n, int m) {
 		tileMatrix = MapGenerator.generateNormalMap(n, m);
 		tileList = new ArrayList<TileContainer>();
 		for (TileContainer[] u : tileMatrix) {
 			for (TileContainer v : u) {
 				tileList.add(v);
 			}
 		}
 	}
 
 	public TileContainer[][] getTileMatrix() {
 		return tileMatrix;
 	}
 
 	public ArrayList<TileContainer> getTileList() {
 		return tileList;
 	}
 
 	public void addWarrior(int i) {
 		int x = 3, y = 3 + i;
 		Warrior c = new Warrior(i, x, y);
 
 		players.add(i, (Player) c);
 	}
 	
 	public void addArcher(int i) {
 		int x = 3, y = 3 + i;
 		Archer c = new Archer(i, x, y);
 
 		players.add(i, (Player) c);
 	}
 
 	public Player getPlayer(int i) {
 		return players.get(i);
 	}
 
 	public void putEntityInMap(Entity e) {
 		entityMap.put(e.getGlobalId(), e);
 	}
 
 	public Entity getEntityFromId(int globalId) {
 		return entityMap.get(globalId);
 	}
 
 	public void addProjectile(Projectile pr) {
 		tileMatrix[pr.getX()][pr.getY()].addProjectile(pr);
 	}
 
 	public void removeProjectile(Projectile pr) {
 		tileMatrix[pr.getX()][pr.getY()].removeProjectile(pr);
 	}
 
 	public ArrayList<Projectile> getAllProjectiles() {
 		return allProjectiles;
 	}
 
 	public void refreshProjectiles() {
 		ArrayList<Projectile> tmp = new ArrayList<Projectile>();
 		for (Projectile p : allProjectiles) {
 			if (!p.getDeathSent())
 				tmp.add(p);
 		}
 		allProjectiles = tmp;
 	}
 
 	public void moveProjectiles() {
 		// обрабатываем все летящие хрени
 		for (Projectile p : Map.getInstance().getAllProjectiles()) {
 			p.moveToTarget();
 		}
 		if (Global.time % Global.REFRESH_PROJECTILE_TIME == 0) {
 			Map.getInstance().refreshProjectiles();
 		}
 	}
 
 	public void makePlayersTurns() {
 
 		// обрабатываем мышь
 		for (int i = 0; i < Global.NUM_PLAYERS; i++) {
 			if (!wasMouse[i])
 				continue;
 			Player p = Map.getInstance().getPlayer(i);
 			Weapon w = p.getWeapon();
 			if (w == null)
 				continue; // если нет оружия, то щито поделать десу
 			Entity e = Map.getInstance().getEntityFromId(mouseId[i]);
 
 			int playerX = p.getX(), playerY = p.getY();
 			int targetX = e.getX(), targetY = e.getY();
 
 			if (playerX == targetX && playerY == targetY)
 				continue; // не хотет стрелять в себя
 
 			Projectile pr = new Projectile(playerX, playerY, targetX, targetY,
 					w.getDamage(), w.getRadius(), p);
 
 			synchronized (wasMouse) {
 				wasMouse[i] = false;
 			}
 		}
 		// обрабатываем клавиатуру
 		for (int i = 0; i < Global.NUM_PLAYERS; i++) {
 			if (!wasButton[i])
 				continue;
 			boolean success = false;
 			switch (button[i]) {
 			case 'w':
 				success = Map.getInstance().getPlayer(i)
 						.tryToChangeCoordBy(0, -1);
 				break;
 			case 'a':
 				success = Map.getInstance().getPlayer(i)
 						.tryToChangeCoordBy(-1, 0);
 				break;
 			case 's':
 				success = Map.getInstance().getPlayer(i)
 						.tryToChangeCoordBy(0, 1);
 				break;
 			case 'd':
 				success = Map.getInstance().getPlayer(i)
 						.tryToChangeCoordBy(1, 0);
 				break;
 			default:
 				success = true;
 				break;
 			}
 			if (success) {
 				synchronized (wasButton) {
 					wasButton[i] = false;
 				}
 			}
 		}
 	}
 
 	public void putButton(int clientId, char c) {
 		button[clientId] = c;
 		synchronized (wasButton) {
 			wasButton[clientId] = true;
 		}
 	}
 
 	public void putMouse(int clientId, int clickedId) {
 		mouseId[clientId] = clickedId;
 		synchronized (wasMouse) {
 			wasMouse[clientId] = true;
 		}
 	}
 
 	// public TreeSet<Entity> getChangeBuffer() {
 	// return changeBuffer;
 	// }
 }
