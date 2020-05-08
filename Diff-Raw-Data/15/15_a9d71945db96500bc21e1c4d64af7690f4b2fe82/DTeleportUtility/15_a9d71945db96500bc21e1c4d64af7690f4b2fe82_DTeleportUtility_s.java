 package com.golddigger.utils;
 
 import static com.golddigger.model.Direction.EAST;
 import static com.golddigger.model.Direction.NORTH;
 import static com.golddigger.model.Direction.NORTH_EAST;
 import static com.golddigger.model.Direction.NORTH_WEST;
 import static com.golddigger.model.Direction.SOUTH;
 import static com.golddigger.model.Direction.SOUTH_EAST;
 import static com.golddigger.model.Direction.SOUTH_WEST;
 import static com.golddigger.model.Direction.WEST;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import com.golddigger.model.Map;
 import com.golddigger.model.Coordinate;
 import com.golddigger.model.Tile;
 import com.golddigger.model.tiles.BaseTile;
 
 /**
  * This class contains some miscellaneous utility methods associated with the
  * teleportation. Originally for disadvantageous teleportation. Think about
  * creating a constructor with parameter map, since it is used often in the
  * methods.
  * 
  */
 
 public class DTeleportUtility {
 
 	/**
 	 * Links up the source tiles in the game map so they teleport to the
 	 * destinations - set teleportDestination attributes. Note that the
 	 * teleportation pairs are assumed to be validated before this function
 	 * (i.e. all coordinates are in the map and do not teleport to walls, etc.)
 	 * and so it does not print warnings.
 	 * 
 	 * @param teleportPairs
 	 *            The ArrayList of source and destination Point2D coordinates,
 	 *            the source teleports the digger to destination once the digger
 	 *            steps on the tile at the source coordinate
 	 * @param map
 	 *            The game map which contains the tiles to be linked for
 	 *            teleportation
 	 */
 	public void assignDTeleports(ArrayList<Coordinate[]> dTeleportPairs, Map map) {
 
 		for (Coordinate[] pair : dTeleportPairs) {
 			Coordinate src = pair[0];
 			Coordinate dst = pair[1];
 			boolean srcIsValid, dstIsValid;
 
 			srcIsValid = tileIsValid(src, map)
 					&& !(map.get(src) instanceof BaseTile);
 
 			dstIsValid = tileIsValid(dst, map);
 
 			if (srcIsValid && dstIsValid) {
 				map.get(src).setTeleportDestination(dst);
 			}
 		}
 
 	}
 
 	/**
 	 * This method finds tiles that a digger can reach by normal movement
 	 * (north, south, ...) starting from parameter location. The initial call to
 	 * this method should have the digger's base as the location.
 	 * 
 	 * @param location
 	 *            The starting location to see which tiles the digger can reach
 	 *            from here by normal movement
 	 * @param reachableTiles
 	 *            The set that holds tiles that are reachable from location (so
 	 *            this method can be called recursively) - note that all "tiles"
 	 *            in this Set have already been visited
 	 * @param map
 	 *            The game map that is to be traversed
 	 * @param teleportPairs
 	 *            The ArrayList of all disadvantageous teleports in this map, if
 	 *            there are no teleports give an empty ArrayList
 	 * @param numberOfSides
 	 *            Hex or square map
 	 * 
 	 */
 	public void findReachableTiles(Coordinate location,
 			HashSet<String> reachableTiles, Map map,
 			ArrayList<Coordinate[]> teleportPairs, int numberOfSides) {
 
 		String squareIndex = location.toString();
 
 		if (tileIsValid(location, map) && !reachableTiles.contains(squareIndex)) {
 
 			reachableTiles.add(squareIndex);
 
 			if (!(map.get(location) instanceof BaseTile)
 					&& hasDestination(location, teleportPairs) != null) {
 
 				/*
 				 * This tile will teleport somewhere so adjacent tiles cannot be
 				 * reached from here by normal movement. If location is a base
 				 * tile and has a destination then the pair is invalid, so
 				 * ignore the fact that it has a destination and continue moving
 				 * onto adjacent tiles
 				 */
 				return;
 			} else {
 
 				/*
 				 * Move onto adjacent tiles until all reachable tiles are
 				 * visited
 				 */
 				if (numberOfSides == 4) {
 					findReachableTiles(NORTH.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(EAST.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(WEST.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(SOUTH.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 				}
 				if (numberOfSides == 6) {
 					findReachableTiles(NORTH.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(NORTH_EAST.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(NORTH_WEST.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(SOUTH.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(SOUTH_EAST.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 					findReachableTiles(SOUTH_WEST.getOffset(location),
 							reachableTiles, map, teleportPairs, numberOfSides);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get the bases (tiles) from the map, similar function in class Game
 	 * 
 	 * @param map
 	 *            The map to get the bases from
 	 * @return An ArrayList of the base tiles
 	 */
 	public ArrayList<BaseTile> getBases(Map map) {
 
 		ArrayList<BaseTile> bases = new ArrayList<BaseTile>();
 
 		for (Tile[] row : map.getTiles()) {
 			for (Tile tile : row) {
 				if (tile instanceof BaseTile) {
 					bases.add((BaseTile) tile);
 				}
 			}
 		}
 
 		return bases;
 	}
 
 	/**
 	 * If there exists a pair in parameter teleportPairs such that its source is
 	 * identical to parameter source, return its destination Point2D coordinate.
 	 * Return the last one if there are more than one.
 	 * 
 	 * @param source
 	 *            The source Point2D coordinate to look for
 	 * @param teleportPairs
 	 *            The ArrayList to look in
 	 * @return The destination coordinate if there exists a pair in
 	 *         teleportPairs with a matching source or null otherwise
 	 */
 	public Coordinate hasDestination(Coordinate source,
 			ArrayList<Coordinate[]> teleportPairs) {
 
 		Coordinate destination = null;
 
 		for (Coordinate[] pair : teleportPairs) {
 			if (pair.length == 2 && pair[0].equals(source)) {
 				destination = pair[1];
 			}
 		}
 
 		return destination;
 
 	}
 
 	/**
 	 * Checks that a tile in a map is valid
 	 * 
 	 * @param tile
 	 *            The tile to validate
 	 * @param map
 	 *            The game map that the tile should be validated against
 	 * @return true if parameter tile is not null, is in the map and is
 	 *         treadable, false otherwise
 	 */
 	public boolean tileIsValid(Coordinate tile, Map map) {
 
 		boolean tileIsValid;
 
 		if (tile != null && map.get(tile) != null
 				&& map.get(tile).isTreadable()) {
 			tileIsValid = true;
 		} else {
 			tileIsValid = false;
 		}
 
 		return tileIsValid;
 
 	}
 
 	/**
 	 * Filters an ArrayList of disadvantageous teleport pairs based on whether
 	 * they are invalid in the sense that they are not walls, in the map, do not
 	 * teleport to trapped areas
 	 * 
 	 * @param dTeleportPairs
 	 *            The teleport source and destination Point2D coordinate pairs
 	 * @param map
 	 *            The game map to check the teleport mappings against
 	 * @param numberOfSides
 	 *            Hex or square map
 	 * @return The ArrayList of valid teleports
 	 */
 	public ArrayList<Coordinate[]> validDTeleports(
 			ArrayList<Coordinate[]> dTeleportPairs, Map map, int numberOfSides) {
 
 		ArrayList<Coordinate[]> validTeleports = new ArrayList<Coordinate[]>();
 		ArrayList<BaseTile> bases = getBases(map);
 
 		for (BaseTile base : bases) {
 
 			for (Coordinate[] pair : dTeleportPairs) {
 				
 				/*
 				 * Add the pair, test that the teleport works with previous
 				 * ones/is valid and not trap, otherwise remove it
 				 */
 				validTeleports.add(pair);
 
 				HashSet<String> reachableTiles = new HashSet<String>();
 				Coordinate baseCoordinate = map.getPosition(base);
 
 				/* Check that the pair itself is valid */
 				if (pair.length != 2) {
 					validTeleports.remove(pair);
 					continue;
 				}
 
 				Coordinate src = pair[0];
 				Coordinate dst = pair[1];
 				boolean srcIsValid = tileIsValid(src, map);
 				boolean dstIsValid = tileIsValid(dst, map);
 
 				if ((srcIsValid && src.equals(baseCoordinate)) || !srcIsValid
 						|| !dstIsValid) {
 					System.out
 							.println("Warning: disadvantageous teleport pair "
 									+ src.toString()
 									+ "->"
 									+ dst.toString()
 									+ " is invalid, dropped/ignored."
 									+ " Check that its source is not a base tile,"
 									+ " both source and destination are treadable"
 									+ " and are in the map");
 					validTeleports.remove(pair);
 					continue;
 				}
 
 				/* Now check for traps */
 
 				/*
 				 * If the source is not reachable from this base then do not
 				 * worry about this teleport pair, since a digger will not be
 				 * able to reach this source tile the digger cannot be
 				 * teleported. However, if the source can be reached but the
 				 * digger cannot get back to base from the destination by normal
 				 * movement then it is trapped so remove the pair.
 				 */
 				findReachableTiles(baseCoordinate, reachableTiles, map,
 						validTeleports, numberOfSides);
 
 				if (reachableTiles.contains(src.toString())
 						&& !(reachableTiles.contains(dst.toString()))) {
 					System.out
 							.println("Warning: disadvantageous teleport pair "
 									+ src.toString() + "->" + dst.toString()
 									+ " is invalid, dropped/ignored."
 									+ " Check that is not a trap");
 					validTeleports.remove(pair);
 					continue;
 				}
 			}
 		}
 
 		return validTeleports;
 	}
 
 	/**
 	 * Converts an array of strings of the form "xSrc,ySrc->xDst,yDst", where
 	 * xSrc... are integers into Point2D source and destination pairs.
 	 */
 	public ArrayList<Coordinate[]> formatDTeleports(String[] teleports) {
 
 		ArrayList<Coordinate[]> map = new ArrayList<Coordinate[]>();
 		String xSrc, xDst, ySrc, yDst;
 		Coordinate src, dst;
 
 		if (teleports == null) {
 			return map;
 		}
 		for (String pair : teleports) {
 			String[] coords = pair.split("->");
 			if (coords.length != 2) {
 				System.out.println("Warning: teleportation pair: \"" + pair
 						+ "\" is invalid");
 				continue;
 			}
 			try {
				xSrc = coords[0].split(",")[0];
				ySrc = coords[0].split(",")[1];
				xDst = coords[1].split(",")[0];
				yDst = coords[1].split(",")[1];
 			} catch (Exception e) {
 				continue;
 			}
 			try {
 				src = new Coordinate(Integer.parseInt(xSrc),
 						Integer.parseInt(ySrc));
 				dst = new Coordinate(Integer.parseInt(xDst),
 						Integer.parseInt(yDst));
 			} catch (NumberFormatException e) {
 				System.out.println("Warning: teleportation pair: \"" + pair
 						+ "\" is invalid");
 				continue;
 			}
 			Coordinate[] coordPair = { src, dst };
 			map.add(coordPair);
 		}
 		return map;
 	}
 }
