 package com.xidstudios.pixelarena;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.math.Vector2;
 import com.xidstudios.pixelarena.arena.Arena;
 import com.xidstudios.pixelarena.entity.Player;
 
 /*
  * calc surrounding tiles f val
  * Check through closed list and make sure node being checked isnt on list
  * Check through openlist, if node in list is better than current one(new g< current g), if it is, replace parent with new one
  * Check surrounding tiles for lowest f val
  * Set lowest as current node
  * remove current node from open and add to closed list
  * repeat.
  * ------------------------------------------------------------>
  * 1. Add starting node to openlist
  * 2. Loop{
  * a) Look for the lowest F cost square on the open list. We refer to this as the current square.
  * b) Switch it to the closed list.
  * c) For each of the 8 squares adjacent to this current square 
  * - If it is not walkable or if it is on the closed list, ignore it. Otherwise do the following.
  * - If it isnt on the open list, add it to the open list. Make the current square the parent of this square. Record the F, G, and H costs of the square.
  * - If it is on the open list already, check to see if this path to that square is better, using G cost as the measure.
  * A lower G cost means that this is a better path.
  * If so, change the parent of the square to the current square, and recalculate the G and F scores of the square.
  * If you are keeping your open list sorted by F score, you may need to resort the list to account for the change.
  * d) Stop when you:
  * - Add the target square to the closed list, in which case the path has been found (see note below), or
  * - Fail to find the target square, and the open list is empty. In this case, there is no path.
  */
 
 public class Path {
 
 	private static Node cNode;
 
 	private static List<Node> oList = new ArrayList<Node>();
 
 	private static List<Node> cList = new ArrayList<Node>();
 
 	private static Vector2 origin;
 
 	private static Vector2 end;
 
 	private static Cell[][] cells;
 
 	private static Arena arena;
 
 	private static Player player;
 
 	private static int MIN_X;
 
 	private static int MIN_Y;
 
 	private static int MAX_X;
 
 	private static int MAX_Y;
 
 	private static int startPosX;
 
 	private static int startPosY;
 
 	private static int endPosX;
 
 	private static int endPosY;
 
 	public static void calculatePath(Vector2 from, Vector2 to,
 			Cell[][] mapCells, Arena a, Player p) {
 		initializeVariables(from, to, mapCells, a, p);
 
 		oList.clear();
 		cNode = new Node(null, origin, end);
 		// step 1
 		oList.add(cNode);
 		// step 2
 		for (int loop = 0; loop < 2; loop++) {
 			// Step a)
 			Node node = checkForLowestCost();
 			oList.add(node);
 			Gdx.app.log("Path", "" + node.getParentVector());
 			setCheckLimits(node);
 			for (int y = startPosY; y <= endPosY; y++) {
 				for (int x = startPosX; x <= endPosX; x++) {
 					// check if adjacent on closed list
 					if (!cList.contains(node)) {
 						// check if not blocked
 						if (!cells[x][y].getTile().getProperties()
 								.containsKey("blocked")) {
 							if (!oList.contains(node)) {
 								oList.add(new Node(node, new Vector2(x, y)));
 								Gdx.app.log("Node", "Added");
 							} else {
 								setMoveCost(x, y, node);
 								debugTotalValue(x, y, node);
 								oList.add(node);
 								boolean better = betterIn(node, oList);
 								if (better) {
 									cNode.setParentNode(node);
 								}
 							}
 
 						}
 					}
 				}
 				System.out.println();
 			}
 
 		}
 
 	}
 
 	private static void debugTotalValue(int x, int y, Node node) {
 		if ((y == MAX_Y - 1) && (x == MAX_X - 1))
 			System.out.print("SS|");
 		else
 			System.out.print(node.getTotalValue() + "|");
 	}
 
 	private static void setMoveCost(int x, int y, Node node) {
 
 		if (x != MAX_X - 1 && y != MAX_Y - 1) {
 			node.setMoveCost(14);
 		} else {
 			node.setMoveCost(10);
 		}
 	}
 
 	private static boolean betterIn(Node n, List<Node> l) {
 		for (Node no : l) {
 			if (no.nodeVector.x == n.nodeVector.x
 					&& no.nodeVector.y == n.nodeVector.y
 					&& (no.getTotalValue()) <= (n.getTotalValue()))
 				return true;
 		}
 		return false;
 	}
 
 	private static Node checkForLowestCost() {
 		int index = 0;
 		Gdx.app.log("Path", "oList size = " + oList.size());
 		int f = oList.get(index).getTotalValue();
 		for (Node temp : oList) {
			Gdx.app.log("", "" + temp.getTotalValue());
 			if (temp.getTotalValue() < f) {
 				f = temp.getTotalValue();
 				index = oList.lastIndexOf(temp);
 			}
 		}
 		return oList.get(index);
 	}
 
 	private static void setCheckLimits(Node node) {
 		int startPX = (int) node.nodeVector.x;
 		int startPY = (int) node.nodeVector.y;
 		MIN_X = startPX - 1;
 		MIN_Y = startPY - 1;
 		MAX_X = startPX + 1;
 		MAX_Y = startPY + 1;
 		Gdx.app.log(PArena.LOG, "MinX " + MIN_X + ", MinY " + MIN_Y);
 		Gdx.app.log(PArena.LOG, "MaxX " + MAX_X + ", MaxY " + MAX_Y);
 
 		startPosX = (startPX - 1 < MIN_X) ? startPX : startPX - 1;
 		startPosY = (startPY - 1 < MIN_Y) ? startPY : startPY - 1;
 		endPosX = (startPX + 1 > MAX_X) ? startPX : startPX + 1;
 		endPosY = (startPY + 1 > MAX_Y) ? startPY : startPY + 1;
 	}
 
 	private static void initializeVariables(Vector2 from, Vector2 to,
 			Cell[][] mapCells, Arena a, Player p) {
 		origin = from;
 		end = to;
 		cells = mapCells;
 		arena = a;
 		player = p;
 	}
 
 }
