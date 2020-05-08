 package org.rsbot.script.methods;
 
 import org.rsbot.Configuration;
 import org.rsbot.script.background.BankMonitor;
 import org.rsbot.script.background.WebData;
 import org.rsbot.script.internal.BackgroundScriptHandler;
 import org.rsbot.script.web.PlaneHandler;
 import org.rsbot.script.web.PlaneTraverse;
 import org.rsbot.script.web.Route;
 import org.rsbot.script.web.RouteStep;
 import org.rsbot.script.wrappers.RSTile;
 import org.rsbot.script.wrappers.RSWeb;
 import org.rsbot.service.WebQueue;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.*;
 
 /**
  * The web class.
  *
  * @author Timer
  */
 public class Web extends MethodProvider {
 	public static final HashMap<RSTile, Integer> rs_map = new HashMap<RSTile, Integer>();
 	public static boolean loaded = false;
 	public static boolean webScriptsLoaded = false;
 	private static final Object lock = new Object();
 	private static long lastAccess = 0;
 	private long lastLocalAccess = 0;
 	private int webDataId = 0, bankCacheId = 0;
 
 	Web(final MethodContext ctx) {
 		super(ctx);
 	}
 
 	/**
 	 * Gets the closest supported bank that is usable.
 	 *
 	 * @param tile The tile to look off of.
 	 * @return The closest bank's tile.
 	 */
 	public RSTile getNearestBank(final RSTile tile) {
 		double dist = -1.0D;
 		RSTile finalTile = null;
 		final RSTile[] BANKS = {new RSTile(3093, 3243, 0), new RSTile(3209, 3219, 2), new RSTile(3270, 3167, 0),
 				new RSTile(3253, 3421, 0), new RSTile(3188, 3437, 0), new RSTile(3094, 3491, 0),
 				new RSTile(3097, 3496, 0), new RSTile(2946, 3369, 0), new RSTile(3012, 3356, 0)};
 		for (RSTile bank : BANKS) {
 			double cdist = methods.calc.distanceBetween(tile, bank);
 			if ((dist > cdist || dist == -1.0D) && (tile.getZ() == bank.getZ())) {
 				dist = cdist;
 				finalTile = bank;
 			}
 		}
 		return finalTile;
 	}
 
 	/**
 	 * Gets the closest supported bank that is usable.
 	 *
 	 * @return The closest bank's tile.
 	 */
 	public RSTile getNearestBank() {
 		return getNearestBank(methods.players.getMyPlayer().getLocation());
 	}
 
 	/**
 	 * Generates a path between two nodes.
 	 *
 	 * @param start The starting tile.
 	 * @param end   The ending tile.
 	 * @return The path.
 	 */
 	public RSTile[] generateTilePath(final RSTile start, final RSTile end) {
 		if (start.getZ() != end.getZ()) {
 			return null;
 		}
 		if (start.equals(end)) {
 			return new RSTile[]{};
 		}
 		if (!areScriptsLoaded()) {
 			loadWebScripts();
 		}
 		lastLocalAccess = System.currentTimeMillis();
 		final HashSet<Node> open = new HashSet<Node>();
 		final HashSet<Node> closed = new HashSet<Node>();
 		Node curr = new Node(start.getX(), start.getY(), start.getZ());
 		final Node dest = new Node(end.getX(), end.getY(), end.getZ());
 		curr.f = Heuristic(curr, dest);
 		open.add(curr);
 		while (!open.isEmpty()) {
 			curr = Lowest_f(open);
 			if (curr.equals(dest)) {
 				return Path(curr);
 			}
 			open.remove(curr);
 			closed.add(curr);
 			for (final Node next : Web.Successors(curr)) {
 				if (!closed.contains(next)) {
 					final double t = curr.g + Dist(curr, next);
 					boolean use_t = false;
 					if (!open.contains(next)) {
 						open.add(next);
 						use_t = true;
 					} else if (t < next.g) {
 						use_t = true;
 					}
 					if (use_t) {
 						next.prev = curr;
 						next.g = t;
 						next.f = t + Heuristic(next, dest);
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Generates routes between two tiles.
 	 *
 	 * @param start The start tile.
 	 * @param end   The ending tile.
 	 * @return The generated route.
 	 */
 	private Route[] generateRoutes(final RSTile start, final RSTile end, final Route lastRoute) {
 		if (start.getZ() == end.getZ()) {
 			Route route = planeRoute(start, end, null);
 			if (route == null) {
 				return null;
 			} else {
 				route.parent = lastRoute;
 			}
 			if (route.parent == null) {
 				return new Route[]{route};
 			}
 			LinkedList<Route> finalRouting = new LinkedList<Route>();
 			while (route.parent != null) {
 				finalRouting.addLast(route);
 				route = route.parent;
 			}
 			return finalRouting.toArray(new Route[finalRouting.size()]);
 		}
 		PlaneHandler planeHandler = new PlaneHandler(methods);
 		PlaneTraverse[] traverses = planeHandler.get(methods.game.getPlane());
 		for (PlaneTraverse traverse : traverses) {
 			if (traverse.destPlane() == end.getZ()) {//TODO more complex method--prevent infinite loops once made.
 				final Route route = planeRoute(start, end, traverse);
 				route.parent = lastRoute;
 				return generateRoutes(traverse.dest(), end, route);
 			}
 		}
 		return null;//No applicable plane transfers.
 	}
 
 	public Route planeRoute(final RSTile start, final RSTile end, final PlaneTraverse transfer) {
 		if (!areScriptsLoaded()) {
 			loadWebScripts();
 		}
 		lastLocalAccess = System.currentTimeMillis();
 		if (transfer != null) {
 			final Route walkRoute = planeRoute(start, transfer.walkTo(), null);
 			if (walkRoute == null) {
 				return null;
 			}
 			//TODO START
 			/* code interaction with plane transfer to add to web route */
 			return walkRoute;
 			//TODO END
 		}
 		//TODO Path generation.
 		RSTile[] path = generateTilePath(start, end);    //TODO add teleports object etc
 		if (path == null) {
 			return null;
 		}
 		return new Route(new RouteStep[]{new RouteStep(methods, path)});
 	}
 
 	/**
 	 * Returns a web instance to traverse.
 	 *
 	 * @param start The starting tile.
 	 * @param end   The end tile.
 	 * @return The web constructed.  <code>null</code> if it cannot be done.
 	 */
 	public RSWeb getWeb(RSTile start, final RSTile end) {
 		Route[] routes = generateRoutes(start, end, null);
		if (routes == null) {
			return null;
		}
 		return new RSWeb(routes, start, end);
 	}
 
 	/**
 	 * Returns a web instance to traverse.
 	 *
 	 * @param end The end tile.
 	 * @return The web constructed.  <code>null</code> if it cannot be done.
 	 */
 	public RSWeb getWeb(final RSTile end) {
 		return getWeb(methods.players.getMyPlayer().getLocation(), end);
 	}
 
 	/**
 	 * Node class.
 	 */
 	private static class Node {
 		public int x, y, z;
 		public Node prev;
 		public double g, f;
 
 		public Node(final int x, final int y, final int z) {
 			this.x = x;
 			this.y = y;
 			this.z = z;
 			g = f = 0;
 		}
 
 		@Override
 		public int hashCode() {
 			return x << 4 | y;
 		}
 
 		@Override
 		public boolean equals(final Object o) {
 			if (o instanceof Node) {
 				final Node n = (Node) o;
 				return x == n.x && y == n.y && z == n.z;
 			}
 			return false;
 		}
 
 		@Override
 		public String toString() {
 			return "(" + x + "," + y + "," + z + ")";
 		}
 
 		public RSTile toRSTile() {
 			return new RSTile(x, y, z);
 		}
 	}
 
 	/**
 	 * Gets the heuristic distance.
 	 *
 	 * @param start Start node.
 	 * @param end   End node.
 	 * @return The distance.
 	 */
 	private static double Heuristic(final Node start, final Node end) {
 		/*double dx = start.x - end.x;
 		double dy = start.y - end.y;
 		if (dx < 0) {
 			dx = -dx;
 		}
 		if (dy < 0) {
 			dy = -dy;
 		}
 		return dx < dy ? dy : dx;*/
 		return Dist(start, end) * Math.sqrt(Math.pow(start.x - end.x, 2) + Math.pow(start.y - end.y, 2));
 	}
 
 	/**
 	 * The distance between two tiles.
 	 *
 	 * @param start The start tile.
 	 * @param end   The end tile.
 	 * @return The distance.
 	 */
 	private static double Dist(final Node start, final Node end) {
 		if (start.x != end.x && start.y != end.y) {
 			return 1.41421356;
 		} else {
 			return 1.0;
 		}
 	}
 
 	/**
 	 * Gets the lowest f score of a set.
 	 *
 	 * @param open The set.
 	 * @return The node that has the lowest f score.
 	 */
 	private static Node Lowest_f(final Set<Node> open) {
 		Node best = null;
 		for (final Node t : open) {
 			if (best == null || t.f < best.f) {
 				best = t;
 			}
 		}
 		return best;
 	}
 
 	/**
 	 * Constructs a path from a node.
 	 *
 	 * @param end The end node.
 	 * @return The constructed path.
 	 */
 	private static RSTile[] Path(final Node end) {
 		final LinkedList<RSTile> path = new LinkedList<RSTile>();
 		Node p = end;
 		while (p != null) {
 			path.addFirst(p.toRSTile());
 			p = p.prev;
 		}
 		return path.toArray(new RSTile[path.size()]);
 	}
 
 	/**
 	 * Gets successors of a tile.
 	 *
 	 * @param t The node.
 	 * @return The nodes.
 	 */
 	private static List<Node> Successors(final Node t) {
 		if (!Web.isLoaded() && !Web.loadWeb()) {
 			return null;
 		}
 		Web.lastAccess = System.currentTimeMillis();
 		final LinkedList<Node> tiles = new LinkedList<Node>();
 		final int x = t.x, y = t.y;
 		final RSTile here = t.toRSTile();
 		if (!Flag(here, RSTile.Flag.W_S) &&
 				!Flag(here.getX(), here.getY() - 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER)) {
 			tiles.add(new Node(x, y - 1, here.getZ()));
 		}
 		if (!Flag(here, RSTile.Flag.W_W) &&
 				!Flag(here.getX() - 1, here.getY(), here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER)) {
 			tiles.add(new Node(x - 1, y, here.getZ()));
 		}
 		if (!Flag(here, RSTile.Flag.W_N) &&
 				!Flag(here.getX(), here.getY() + 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER)) {
 			tiles.add(new Node(x, y + 1, here.getZ()));
 		}
 		if (!Flag(here, RSTile.Flag.W_E) &&
 				!Flag(here.getX() + 1, here.getY(), here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER)) {
 			tiles.add(new Node(x + 1, y, here.getZ()));
 		}
 		if (!Flag(here, RSTile.Flag.W_SW | RSTile.Flag.W_S | RSTile.Flag.W_W) &&
 				!Flag(here.getX() - 1, here.getY() - 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER) &&
 				!Flag(here.getX(), here.getY() - 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_W) &&
 				!Flag(here.getX() - 1, here.getY(), here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_S)) {
 			tiles.add(new Node(x - 1, y - 1, here.getZ()));
 		}
 		if (!Flag(here, RSTile.Flag.W_NW | RSTile.Flag.W_N | RSTile.Flag.W_W) &&
 				!Flag(here.getX() - 1, here.getY() + 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER) &&
 				!Flag(here.getX(), here.getY() + 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_W) &&
 				!Flag(here.getX() - 1, here.getY(), here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_N)) {
 			tiles.add(new Node(x - 1, y + 1, here.getZ()));
 		}
 		if (!Flag(here, RSTile.Flag.W_SE | RSTile.Flag.W_S | RSTile.Flag.W_E) &&
 				!Flag(here.getX() + 1, here.getY() - 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER) &&
 				!Flag(here.getX(), here.getY() - 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_E) &&
 				!Flag(here.getX() + 1, here.getY(), here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_S)) {
 			tiles.add(new Node(x + 1, y - 1, here.getZ()));
 		}
 		if (!Flag(here, RSTile.Flag.W_NE | RSTile.Flag.W_N | RSTile.Flag.W_E) &&
 				!Flag(here.getX() + 1, here.getY() + 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER) &&
 				!Flag(here.getX(), here.getY() + 1, here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_E) &&
 				!Flag(here.getX() + 1, here.getY(), here.getZ(), RSTile.Flag.BLOCKED | RSTile.Flag.WATER | RSTile.Flag.W_N)) {
 			tiles.add(new Node(x + 1, y + 1, here.getZ()));
 		}
 		return tiles;
 	}
 
 	/**
 	 * Gets the TileFlags of a tile.
 	 *
 	 * @param tile The tile.
 	 * @return The <code>TileFlags</code>.
 	 */
 	public static int GetTileFlag(final RSTile tile) {
 		final Short[] theTile = {(short) tile.getX(), (short) tile.getY(), (short) tile.getZ()};
 		return Web.rs_map.get(theTile);
 	}
 
 	/**
 	 * Checks the flags of a tile.
 	 *
 	 * @param tile The tile to check.
 	 * @param key  Keys to look for.
 	 * @return <tt>true</tt> if the tile contains flags.
 	 */
 	public static boolean Flag(final RSTile tile, final int key) {
 		if (Web.rs_map.containsKey(tile)) {
 			final int theFlag = Web.rs_map.get(tile);
 			return (theFlag & key) != 0;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks the flags of a tile.
 	 *
 	 * @param x   The tile to check (x).
 	 * @param y   The tile to check (y).
 	 * @param z   The tile to check (z).
 	 * @param key Keys to look for.
 	 * @return <tt>true</tt> if the tile contains flags.
 	 */
 	public static boolean Flag(final int x, final int y, final int z, final int key) {
 		return Flag(new RSTile(x, y, z), key);
 	}
 
 	/**
 	 * Checks if the web is loaded.
 	 *
 	 * @return <tt>true</tt> if the web is loaded; otherwise <tt>false</tt>.
 	 */
 	public static boolean isLoaded() {
 		return Web.loaded;
 	}
 
 	/**
 	 * Checks if the web is in use or not.
 	 *
 	 * @return <tt>true</tt> if in use; otherwise <tt>false</tt>.
 	 */
 	public static boolean isInActive() {
 		return isLoaded() && System.currentTimeMillis() - Web.lastAccess > (1000 * 60 * 5);
 	}
 
 	/**
 	 * Frees the web from memory.
 	 */
 	public static void free() {
 		if (isInActive()) {
 			Web.rs_map.clear();
 			Web.loaded = false;
 			System.gc();
 		}
 	}
 
 	/**
 	 * Loads the web into memory.
 	 *
 	 * @return <tt>true</tt> if the web was loaded; otherwise <tt>false</tt>.
 	 */
 	public static boolean loadWeb() {
 		if (!Web.loaded) {
 			Web.rs_map.clear();//Remove residue.
 			lastAccess = System.currentTimeMillis();
 			try {
 				if (!new File(Configuration.Paths.getWebDatabase()).exists()) {
 					Web.loaded = true;
 					return true;
 				}
 				final BufferedReader bufferedReader = new BufferedReader(new FileReader(Configuration.Paths.getWebDatabase()));
 				String dataLine;
 				final HashMap<RSTile, Integer> mapData = new HashMap<RSTile, Integer>();
 				while ((dataLine = bufferedReader.readLine()) != null) {
 					final String[] storeData = dataLine.split("k");
 					if (storeData.length == 2) {
 						final String[] tileData = storeData[0].split(",");
 						if (tileData.length == 3) {
 							try {
 								final RSTile tile = new RSTile(Integer.parseInt(tileData[0]), Integer.parseInt(tileData[1]), Integer.parseInt(tileData[2]));
 								final int tileFlag = Integer.parseInt(storeData[1]);
 								synchronized (lock) {
 									if (mapData.containsKey(tile)) {
 										WebQueue.Remove(dataLine);//Line is double, remove from file--bad collection!
 									} else {
 										mapData.put(tile, tileFlag);
 									}
 								}
 							} catch (final Exception e) {
 							}
 						} else {
 							synchronized (lock) {
 								WebQueue.Remove(dataLine);//Line is bad, remove from file.
 							}
 						}
 					} else {
 						synchronized (lock) {
 							WebQueue.Remove(dataLine);//Line is bad, remove from file.
 						}
 					}
 				}
 				synchronized (lock) {
 					Web.rs_map.putAll(mapData);
 					Web.loaded = true;
 				}
 			} catch (final Exception e) {
 				Web.loaded = false;
 				return false;
 			}
 		}
 		Web.loaded = true;
 		return true;
 	}
 
 	public boolean areScriptsLoaded() {
 		return webScriptsLoaded;
 	}
 
 	public boolean areScriptsInActive() {
 		return webScriptsLoaded && System.currentTimeMillis() - lastLocalAccess > (1000 * 60 * 5);
 	}
 
 	public void loadWebScripts() {
 		final BackgroundScriptHandler bsh = methods.bot.getBackgroundScriptHandler();
 		webDataId = bsh.runScript(new WebData());
 		bankCacheId = bsh.runScript(new BankMonitor());
 		webScriptsLoaded = true;
 	}
 
 	public void unloadWebScripts() {
 		if (webScriptsLoaded) {
 			final BackgroundScriptHandler bsh = methods.bot.getBackgroundScriptHandler();
 			bsh.stopScript(webDataId);
 			bsh.stopScript(bankCacheId);
 			webScriptsLoaded = false;
 		}
 	}
 }
