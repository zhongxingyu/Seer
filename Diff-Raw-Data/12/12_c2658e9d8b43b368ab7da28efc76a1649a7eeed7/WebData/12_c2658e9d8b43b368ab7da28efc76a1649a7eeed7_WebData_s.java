 package org.rsbot.script.background;
 
 import org.rsbot.script.BackgroundScript;
 import org.rsbot.script.ScriptManifest;
 import org.rsbot.script.internal.wrappers.TileFlags;
 import org.rsbot.script.methods.Web;
 import org.rsbot.script.wrappers.RSTile;
 import org.rsbot.service.WebQueue;
 
 import java.util.HashMap;
 
 @ScriptManifest(name = "Web Data Collector", authors = {"Timer"})
 public class WebData extends BackgroundScript {
 	private RSTile lb = null;
 	private int lp = -1;
 	public final HashMap<RSTile, TileFlags> rs_map = new HashMap<RSTile, TileFlags>();
 
 	@Override
 	public boolean activateCondition() {
 		final RSTile curr_base = game.getMapBase();
 		final int curr_plane = game.getPlane();
		return game.isLoggedIn() && (lb == null || !lb.equals(curr_base)) && (lp == -1 || !(lp == curr_plane));
 	}
 
 	@Override
 	public int loop() {
 		try {
 			final RSTile curr_base = game.getMapBase();
 			final int curr_plane = game.getPlane();
			if (lb != null && lb.equals(curr_base) && (lp != -1 && lp == curr_plane)) {
				return -1;
			}
 			rs_map.clear();
 			sleep(5000);
 			if (!curr_base.equals(game.getMapBase())) {
 				return -1;
 			}
 			lb = curr_base;
 			lp = curr_plane;
 			Node t;
 			final int flags[][] = walking.getCollisionFlags(curr_plane);
 			for (int i = 3; i < 102; i++) {
 				for (int j = 3; j < 102; j++) {
 					final RSTile start = new RSTile(curr_base.getX() + i, curr_base.getY() + j, curr_plane);
 					final int base_x = game.getBaseX(), base_y = game.getBaseY();
 					final int curr_x = start.getX() - base_x, curr_y = start.getY() - base_y;
 					t = new Node(curr_x, curr_y);
 					final RSTile offset = walking.getCollisionOffset(curr_plane);
 					final int off_x = offset.getX();
 					final int off_y = offset.getY();
 					final int x = t.x, y = t.y;
 					final int f_x = x - off_x, f_y = y - off_y;
 					final int here = flags[f_x][f_y];
 					final TileFlags tI = new TileFlags(start, null);
 					if ((here & TileFlags.Flags.WALL_EAST) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_EAST);
 					}
 					if ((here & TileFlags.Flags.WALL_WEST) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_WEST);
 					}
 					if ((here & TileFlags.Flags.WALL_NORTH) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_NORTH);
 					}
 					if ((here & TileFlags.Flags.WALL_SOUTH) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_SOUTH);
 					}
 					if ((here & TileFlags.Flags.WALL_NORTH_EAST) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_NORTH_EAST);
 					}
 					if ((here & TileFlags.Flags.WALL_NORTH_WEST) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_NORTH_WEST);
 					}
 					if ((here & TileFlags.Flags.WALL_SOUTH_EAST) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_SOUTH_EAST);
 					}
 					if ((here & TileFlags.Flags.WALL_SOUTH_WEST) != 0) {
 						tI.addKey(TileFlags.Keys.WALL_SOUTH_WEST);
 					}
 					if ((here & TileFlags.Flags.BLOCKED) != 0) {
 						tI.addKey(TileFlags.Keys.BLOCKED);
 					} else {
 						if ((here & TileFlags.Flags.WATER) != 0) {
 							tI.addKey(TileFlags.Keys.TILE_WATER);
 						}
 					}
 					if (!Web.map.containsKey(start) && !tI.isWalkable()) {
 						rs_map.put(start, tI);
 					} else {
 						try {
 							if (!Web.map.get(start).equals(tI)) {
 								WebQueue.Remove(start);
 							}
 						} catch (final NullPointerException ignored) {
 						}
 					}
 				}
 			}
 			WebQueue.Add(rs_map);
 			return -1;
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	@Override
 	public int iterationSleep() {
 		return 1000;
 	}
 
 	private class Node {
 		public int x, y;
 
 		public Node(final int x, final int y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 }
