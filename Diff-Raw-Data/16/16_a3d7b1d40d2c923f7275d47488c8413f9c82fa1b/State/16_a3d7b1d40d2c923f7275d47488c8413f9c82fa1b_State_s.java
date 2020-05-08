 package edu.itba.skbsolver;
 
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A scene stores the state of all objects in the map. It is related to a , that
  * stores the position of non-movable items.
  * 
  * @author eordano
  */
 public class State implements Comparable<State> {
 
 	public static final int[] dx = { 0, 1, 0, -1 };
 	public static final int[] dy = { 1, 0, -1, 0 };
 
 	public int[] boxes;
 	public int moves;
 	public int player;
 	public int hashCalculated = 0;
 	public State parent = null;
 	public Level map;
 
 	/**
 	 * Create a Snap from:
 	 * 
 	 * @param boxes
 	 *            a vector of positions where boxes are
 	 * @param player
 	 *            the position of the player
 	 * @param baseMap
 	 *            a Scenario where the boxes are
 	 */
 	State(int[] boxes, int player, Level map, int moves) {
 		this.boxes = boxes;
 		this.player = player;
 		this.map = map;
 		this.moves = moves;
 		this.hashCalculated = map.playerZobrist[player >> 16][player & 0xFFFF];
 		for (int i = 0; i < boxes.length; i++) {
 			hashCalculated ^= map.boxZobrist[boxes[i] >> 16][boxes[i] & 0xFFFF];
 		}
 	}
 
 	/**
 	 * Create a State from a previous state and a box movement
 	 * 
 	 * @param s
 	 * @param boxMoved
 	 * @param d
 	 */
 	public State(State s, int boxMoved, int direction, int playerMoves,
 			int newHash) {
 		this.parent = s;
 		this.boxes = s.boxes.clone();
 		this.moves = s.moves + playerMoves;
 		this.map = s.map;
 		this.player = s.boxes[boxMoved];
 
 		this.boxes[boxMoved] += (dx[direction] << 16) + dy[direction];
 
 		/*
 		 * Este código mantiene ordenado el arreglo
 		 * 
 		 * while (boxMoved > 0 && this.boxes[boxMoved-1] >
 		 * this.boxes[boxMoved]){ swap(boxMoved, boxMoved - 1); boxMoved--; }
 		 * while (boxMoved + 1 < this.boxes.length && this.boxes[boxMoved] >
 		 * this.boxes[boxMoved+1]){ swap(boxMoved, boxMoved + 1); boxMoved++; }
 		 */
 
 		this.hashCalculated = newHash;
 	}
 
 	@SuppressWarnings("unused")
 	private void swap(int i, int j) {
 		int c = this.boxes[i];
 		this.boxes[i] = this.boxes[j];
 		this.boxes[j] = c;
 	}
 
 	/**
 	 * @return player's X position
 	 */
 	public int px() {
 		return (player >> 16) & 0xFFFF;
 	}
 
 	/**
 	 * @return players' Y position
 	 */
 	public int py() {
 		return (player & 0xFFFF);
 	}
 
 	/**
 	 * @param i
 	 *            the index of the box
 	 * @return the x coordinate of the box
 	 */
 	public int bx(int i) {
 		return (boxes[i] >> 16) & 0xFFFF;
 	}
 
 	/**
 	 * @param i
 	 *            the index of the box
 	 * @return the y coordinate of the box
 	 */
 	public int by(int i) {
 		return (boxes[i] & 0xFFFF);
 	}
 
 	/**
 	 * Retrieve the player's position as a Point
 	 * 
 	 * @return
 	 */
 	public Point playerPoint() {
 		return new Point(px(), py());
 	}
 
 	/**
 	 * We are considering that two states are equal iff they have the same
 	 * Zobrist hash. This may not be true!!! but is very likely (if it's
 	 * completely random, the chances are 1/4 billion)
 	 * 
 	 * @param other
 	 * @return
 	 */
 	public boolean equals(State other) {
 		return this.hashCalculated == other.hashCalculated;
 	}
 
 	/**
 	 * The hash is calculated in the constructor
 	 */
 	public int hashCode() {
 		return this.hashCalculated;
 	}
 
 	/**
 	 * A comparison between states. This is required to push states into a
 	 * priority queue.
 	 */
 	public int compareTo(State b) {
 		if (moves == b.moves) {
 			return player - b.player;
 		}
 		return moves - b.moves;
 	}
 
 	/**
 	 * Calculate a new hash if the player would move
 	 * 
 	 * @param d
 	 *            the direction (0..3)
 	 * @param box
 	 *            the index of the box that will be moved
 	 * 
 	 * @return a new hash
 	 */
 	public int hashIfMove(int d, int box) {
		int ret = hashCalculated
				^ map.playerZobrist[player >> 16][player & 0xFFFF]
				^ map.boxZobrist[boxes[box] >> 16][boxes[box] & 0xFFFF];
 
		int np = player + (dx[d] << 16) + dy[d];
		int nb = boxes[box] + (dx[d] << 16) + dy[d];

		ret ^= map.playerZobrist[np >> 16][np & 0xFFFF]
				^ map.boxZobrist[nb >> 16][nb & 0xFFFF];
 
 		return ret;
 	}
 
 	public String toString() {
 		StringBuilder s = new StringBuilder();
 
 		Map<Point, Character> m = new HashMap<Point, Character>();
 
 		Point pl = new Point(player >> 16, player & 0xFFFF);
 		m.put(pl, map.get(pl.x, pl.y));
 		map.map[pl.x][pl.y] = '@';
 
 		for (int box : boxes) {
 			pl = new Point(box >> 16, box & 0xFFFF);
 			m.put(pl, map.get(pl.x, pl.y));
 			map.map[pl.x][pl.y] = '$';
 		}
 
 		for (char[] linea : map.map) {
 			s.append(linea);
 			s.append('\n');
 		}
 
 		for (Point p : m.keySet()) {
 			map.map[p.x][p.y] = m.get(p);
 		}
 
 		return s.toString();
 	}
 
 	public boolean triggersFreezeDeadlock(int boxMoved, int d) {
 		List<Integer> boxesAsWalls = new LinkedList<Integer>();
 		map.logger.info("Checking freeze...");
 
 		boxes[boxMoved] += ((dx[d] << 16) + dy[d]);
 		boolean result = _freezeCheck(boxesAsWalls, boxes[boxMoved], 0);
 		boxes[boxMoved] -= ((dx[d] << 16) + dy[d]);
 
 		return result;
 	}
 
 	private boolean _freezeCheck(List<Integer> boxesAsWalls, int box,
 			int targets) {
 
 		int bx = box >> 16;
 		int by = box & 0xFFFF;
 
 		map.logger.info("Checking freeze in " + bx + "," + by);
 
 		targets += map.get(bx, by) == '.' ? 1 : 0;
 
 		boolean verticalBlocked = map.get(bx + 1, by) == '#'
 				|| map.get(bx - 1, by) == '#'
 				|| (map.isBasicDeadlock(bx + 1, by) && map.isBasicDeadlock(
 						bx - 1, by));
 		if (!verticalBlocked) {
 			for (Integer wall : boxesAsWalls) {
 				if (wall == box + (1 << 16) || wall == box - (1 << 16)) {
 					verticalBlocked = true;
 				}
 			}
 		}
 
 		boolean horizontalBlocked = map.get(bx, by + 1) == '#'
 				|| map.get(bx, by - 1) == '#'
 				|| (map.isBasicDeadlock(bx, by + 1) && map.isBasicDeadlock(bx,
 						by - 1));
 		if (!horizontalBlocked) {
 			for (Integer wall : boxesAsWalls) {
 				if (wall == box + 1 || wall == box - 1) {
 					horizontalBlocked = true;
 				}
 			}
 		}
 
 		if (verticalBlocked && horizontalBlocked) {
 			if (boxesAsWalls.size() + 1 == targets) {
 				return false;
 			} else {
 				// Lottery :D
 				boxesAsWalls.add(box);
 				map.addNewCapacitor(boxesAsWalls, targets);
 				return true;
 			}
 		}
 
 		if (verticalBlocked) {
 			for (int abox : boxes) {
 				if (abox == box + 1 || abox == box - 1) {
 					boxesAsWalls.add(box);
 					if (_freezeCheck(boxesAsWalls, abox, targets)) {
 						return true;
 					}
 					boxesAsWalls.remove(boxesAsWalls.size() - 1);
 				}
 			}
 		}
 		if (horizontalBlocked) {
 			for (int abox : boxes) {
 				if (abox == box + (1 << 16) || abox == box - (1 << 16)) {
 					boxesAsWalls.add(box);
 					if (_freezeCheck(boxesAsWalls, abox, targets)) {
 						return true;
 					}
 					boxesAsWalls.remove(boxesAsWalls.size() - 1);
 				}
 			}
 		}
 
 		return false;
 	}
 }
