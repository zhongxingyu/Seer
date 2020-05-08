 package com.xidstudios.pixelarena.pathfinding;
 
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.math.Vector2;
 import com.xidstudios.pixelarena.arena.Arena;
 
 public class PathCreation {
 
 	static Node[][] superList;
 
 	static PriorityQueue<Node> openList;
 
 	static ArrayList<Node> closedList;
 
 	static Vector2 sV;
 
 	static Vector2 dV;
 
 	static Cell[][] cells;
 
 	static Arena arena;
 
 	private static boolean pathFound;
 
 	static MapChecker map;
 
 	public static void findPath(Vector2 startVector,
 			Vector2 destinationVector, Cell[][] cell) {
 		pathFound = false;
 
 		sV = startVector;
 		dV = destinationVector;
 
 		map = new MapChecker(cell);
 
 		// System.out.println("" + MapChecker.m.passable.length);
 
 		superList = new Node[map.passable.length][map.passable[0].length];
 		openList = new PriorityQueue<Node>(superList.length
 				* superList[0].length, new ScoreComparator());
 
 		closedList = new ArrayList<Node>();
 
		superList[(int) startVector.x][(int) startVector.y] = new Node(
 				startVector, null, 0, getHeuristicScore(startVector,
 						destinationVector));
 
		openList.add(superList[(int) startVector.x][(int) startVector.y]);
 
 		while (openList.size() > 0) {
 
 			// Takes the tile from the TileInfo PriorityQueue. Once it's polled, the Queue no longer has it. It is in our hands now.
 			Node currentTile = openList.poll();
 
 			// Calculating all the nice tiles around the open tile we found
 			calcSurroundingTile(currentTile, -1, -1);
 			calcSurroundingTile(currentTile, 0, -1);
 			calcSurroundingTile(currentTile, +1, -1);
 			calcSurroundingTile(currentTile, +1, 0);
 			calcSurroundingTile(currentTile, +1, +1);
 			calcSurroundingTile(currentTile, 0, +1);
 			calcSurroundingTile(currentTile, -1, +1);
 			calcSurroundingTile(currentTile, -1, 0);
 
 			if (currentTile.tileVector.x == destinationVector.x
 					&& currentTile.tileVector.y == destinationVector.y) {
 				System.out.println("Path Found and Created");
 				while (currentTile != null) {
 					arena.colorSquare(new Vector2(
 							(int) currentTile.tileVector.x,
 							(int) currentTile.tileVector.y));
 					// should move player here.....maybe we'll see but gonna have to use the
 					currentTile = currentTile.parentNode;
 				}
 				pathFound = true;
 				break;
 			}
 
 		}
 		if (!pathFound) {
 			System.out.println("Path Not Found");
 		}
 
 	}
 
 	private static void calcSurroundingTile(Node node, int cX, int cY) {
 		int newX = (int) (node.tileVector.x + cX);
 		int newY = (int) (node.tileVector.y + cY);
 
 		// out of bounds check
 		if (newX < 0 || newY < 0 || newX >= map.passable.length
 				|| newY >= map.passable[0].length) {
 			return;
 		}
 
 		Node surroundingTile = superList[newX][newY];
 
 		int toTileCost = (cX != 0 && cY != 0 ? 14 : 10)
 				* map.passCost[newX][newY];
 
 		if (surroundingTile == null) {
 			Vector2 newV = new Vector2(newX, newY);
 			surroundingTile = new Node(newV, node, node.cost
 					+ toTileCost, getHeuristicScore(newV, dV));
 			superList[newX][newY] = surroundingTile;
 			if (Map.map.passable[newX][newY]) {
 				openList.add(surroundingTile);
 			}
 		} else if (surroundingTile.cost > node.cost + toTileCost) {
 			surroundingTile.parentNode = node;
 		}
 	}
 
 	private static int getHeuristicScore(Vector2 startV, Vector2 destV) {
 
 		int xDif = (int) (destV.x > startV.x ? destV.x - startV.x
 				: startV.x - destV.x);
 		int yDif = (int) (destV.y > startV.y ? destV.y - startV.y
 				: startV.y - destV.y);
 
 		return (xDif + yDif) * 1;
 	}
 }
