 package org.rsbot.script.methods;
 
 import org.rsbot.script.internal.wrappers.TileFlags;
 import org.rsbot.script.web.Route;
 import org.rsbot.script.web.RouteStep;
 import org.rsbot.script.web.Teleport;
 import org.rsbot.script.web.TransportationHandler;
 import org.rsbot.script.wrappers.RSTile;
 import org.rsbot.script.wrappers.RSWeb;
 
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * The web class.
  *
  * @author Timer
  */
 public class Web extends MethodProvider {
 	public static final HashMap<RSTile, TileFlags> map = new HashMap<RSTile, TileFlags>();
 	public static boolean loaded = false;
 	private final Logger log = Logger.getLogger("Web");
 
 	Web(final MethodContext ctx) {
 		super(ctx);
 	}
 
 	public RSTile getNearestBank(final RSTile tile) {
 		double dist = -1.0D;
 		RSTile finalTile = null;
 		final RSTile[] BANKS = {new RSTile(3093, 3243, 0), new RSTile(3209, 3219, 2), new RSTile(3270, 3167, 0),
 				new RSTile(3253, 3421, 0), new RSTile(3188, 3437, 0), new RSTile(3094, 3491, 0),
 				new RSTile(3097, 3496, 0), new RSTile(2946, 3369, 0), new RSTile(3012, 3356, 0)};
 		for (RSTile bank : BANKS) {
 			double cdist = methods.calc.distanceBetween(tile, bank);
			if ((dist < cdist || dist == -1.0D) && (tile.getZ() == bank.getZ())) {
 				dist = cdist;
 				finalTile = bank;
 			}
 		}
 		return finalTile;
 	}
 
 	/**
 	 * Generates a path between two nodes.
 	 *
 	 * @param start The starting tile.
 	 * @param end   The ending tile.
 	 * @return The path.
 	 */
 	public RSTile[] generateNodePath(final RSTile start, final RSTile end) {
 		if (start.getZ() != end.getZ()) {
 			log.info("Different planes.");
 			return null;
 		}
 		if (start.equals(end)) {
 			return new RSTile[]{};
 			//return new RSWeb(methods, new WebTile[]{});
 		}
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
 		log.info("We did not find a path, how is that possible?");
 		return null;
 	}
 
 	public Route generateRoute(RSTile start, final RSTile end) {
 		TransportationHandler transportationHandler = new TransportationHandler(methods);
 		List<RouteStep> routeSteps = new ArrayList<RouteStep>();
 		if (transportationHandler.canTeleport(end)) {
 			Teleport teleport = transportationHandler.getTeleport(end);
 			if (teleport.teleportationLocation().getZ() == end.getZ()) {
 				RouteStep teleportStep = new RouteStep(methods, transportationHandler.getTeleport(end));
 				start = teleport.teleportationLocation();
 				routeSteps.add(teleportStep);
 			}
 		}
 		RSTile[] nodePath = generateNodePath(start, end);
 		if (nodePath != null) {
 			RouteStep walkingStep = new RouteStep(methods, nodePath);
 			routeSteps.add(walkingStep);
 			return new Route(routeSteps.toArray(new RouteStep[routeSteps.size()]));
 		}
 		return null;
 	}
 
 	public RSWeb getWeb(RSTile start, final RSTile end) {
 		Route onlyRoute = generateRoute(start, end);
 		return new RSWeb(new Route[]{onlyRoute});
 	}
 
 	/**
 	 * Node class.
 	 *
 	 * @author Jacmob
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
 				return x == n.x && y == n.y;
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
 		double dx = start.x - end.x;
 		double dy = start.y - end.y;
 		if (dx < 0) {
 			dx = -dx;
 		}
 		if (dy < 0) {
 			dy = -dy;
 		}
 		return dx < dy ? dy : dx;
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
 		final LinkedList<Node> tiles = new LinkedList<Node>();
 		final int x = t.x, y = t.y;
 		final RSTile here = t.toRSTile();
 		if (!Flag(here, TileFlags.Keys.WALL_SOUTH) &&
 				!Flag(new RSTile(here.getX(), here.getY() - 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER)) {
 			tiles.add(new Node(x, y - 1, t.toRSTile().getZ()));
 		}
 		if (!Flag(here, TileFlags.Keys.WALL_WEST) &&
 				!Flag(new RSTile(here.getX() - 1, here.getY()), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER)) {
 			tiles.add(new Node(x - 1, y, t.toRSTile().getZ()));
 		}
 		if (!Flag(here, TileFlags.Keys.WALL_NORTH) &&
 				!Flag(new RSTile(here.getX(), here.getY() + 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER)) {
 			tiles.add(new Node(x, y + 1, t.toRSTile().getZ()));
 		}
 		if (!Flag(here, TileFlags.Keys.WALL_EAST) &&
 				!Flag(new RSTile(here.getX() + 1, here.getY()), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER)) {
 			tiles.add(new Node(x + 1, y, t.toRSTile().getZ()));
 		}
 		if (!Flag(here, TileFlags.Keys.WALL_SOUTH_WEST, TileFlags.Keys.WALL_SOUTH, TileFlags.Keys.WALL_WEST) &&
 				!Flag(new RSTile(here.getX() - 1, here.getY() - 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER) &&
 				!Flag(new RSTile(here.getX(), here.getY() - 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_WEST) &&
 				!Flag(new RSTile(here.getX() - 1, here.getY()), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_SOUTH)) {
 			tiles.add(new Node(x - 1, y - 1, t.toRSTile().getZ()));
 		}
 		if (!Flag(here, TileFlags.Keys.WALL_NORTH_WEST, TileFlags.Keys.WALL_NORTH, TileFlags.Keys.WALL_WEST) &&
 				!Flag(new RSTile(here.getX() - 1, here.getY() + 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER) &&
 				!Flag(new RSTile(here.getX(), here.getY() + 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_WEST) &&
 				!Flag(new RSTile(here.getX() - 1, here.getY()), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_NORTH)) {
 			tiles.add(new Node(x - 1, y + 1, t.toRSTile().getZ()));
 		}
 		if (!Flag(here, TileFlags.Keys.WALL_SOUTH_EAST, TileFlags.Keys.WALL_SOUTH, TileFlags.Keys.WALL_EAST) &&
 				!Flag(new RSTile(here.getX() + 1, here.getY() - 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER) &&
 				!Flag(new RSTile(here.getX(), here.getY() - 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_EAST) &&
 				!Flag(new RSTile(here.getX() + 1, here.getY()), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_SOUTH)) {
 			tiles.add(new Node(x + 1, y - 1, t.toRSTile().getZ()));
 		}
 		if (!Flag(here, TileFlags.Keys.WALL_NORTH_EAST, TileFlags.Keys.WALL_NORTH, TileFlags.Keys.WALL_EAST) &&
 				!Flag(new RSTile(here.getX() + 1, here.getY() + 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER) &&
 				!Flag(new RSTile(here.getX(), here.getY() + 1), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_EAST) &&
 				!Flag(new RSTile(here.getX() + 1, here.getY()), TileFlags.Keys.BLOCKED, TileFlags.Keys.TILE_WATER, TileFlags.Keys.WALL_NORTH)) {
 			tiles.add(new Node(x + 1, y + 1, t.toRSTile().getZ()));
 		}
 		return tiles;
 	}
 
 	/**
 	 * Gets the TileFlags of a tile.
 	 *
 	 * @param tile The tile.
 	 * @return The <code>TileFlags</code>.
 	 */
 	public static TileFlags getTileFlags(final RSTile tile) {
 		if (Web.map.containsKey(tile)) {
 			return Web.map.get(tile);
 		}
 		return null;
 	}
 
 	/**
 	 * Checks the flags of a tile.
 	 *
 	 * @param tile The tile to check.
 	 * @param key  Keys to look for.
 	 * @return <tt>true</tt> if the tile contains flags.
 	 */
 	public static boolean Flag(final RSTile tile, final int... key) {
 		if (Web.map.containsKey(tile)) {
 			final TileFlags theTile = Web.map.get(tile);
 			return theTile.containsKey(key);
 		}
 		return false;
 	}
 }
