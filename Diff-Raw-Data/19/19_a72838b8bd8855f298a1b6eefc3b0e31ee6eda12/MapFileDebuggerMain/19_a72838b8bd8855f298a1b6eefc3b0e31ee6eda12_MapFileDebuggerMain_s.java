 /*
  * Copyright 2010, 2011 mapsforge.org
  *
  * This program is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mapsforge.applications.debug;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import org.mapsforge.applications.debug.db.TileSQLiteWriter;
 
 /**
  * 
  * @author Karsten Groll
  * 
  *         Main class for executing various tests helping understanding the map file format.
  */
 
 public class MapFileDebuggerMain {
 	private static String getBaseName(String path) {
 		return new File(path).getName().split("\\.(?=[^\\.]+$)")[0];
 	}
 
 	private static void checkIdexes(String path) {
 		SimpleTileExtractor ste = null;
 		byte[][] tiles;
 		tiles = new byte[200000][];
 
 		// Test if tile coordinates are correct (NEEDS DEBUG FILE!)
 		try {
 			ste = new SimpleTileExtractor(path);
 			// Expected and given signature
 			String expected;
 			String given;
 			int i = 0;
 			for (byte zoomInterval = 0; zoomInterval < ste.getMapFile().getAmountOfZoomIntervals(); zoomInterval++) {
 				for (int y = ste.getMinY(zoomInterval); y <= ste.getMaxY(zoomInterval); y++) {
 					for (int x = ste.getMinX(zoomInterval); x <= ste.getMaxX(zoomInterval); x++) {
 						tiles[i] = ste.getTile(x, y, zoomInterval);
 						if (tiles[i] == null)
 							continue;
 
 						expected = "###TileStart" + x + "," + y + "###";
 						given = new String(tiles[i], 0, 32);
 						if (!given.startsWith(expected)) {
 							System.out.println("ERROR @ (" + x + ", " + y + ") given: " + given
 									+ " expected: " + expected);
 						}
 					}
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (TileIndexOutOfBoundsException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 
 	private static void mapToSQLite(String mapsforeMapFilePath, String outputFilePath, boolean useCompression) {
 		SimpleTileExtractor ste = null;
 		MapFile mf = null;
 		TileSQLiteWriter writer = null;
 		byte[] zoomLevelConfiguration;
 		byte[] tile;
 
 		try {
 			ste = new SimpleTileExtractor(mapsforeMapFilePath);
 			mf = ste.getMapFile();
 
 			// Get zoom level configuration
 			zoomLevelConfiguration = mf.getBaseZoomLevel();
 
 			// Write tile to DB
 			writer = new TileSQLiteWriter(outputFilePath, zoomLevelConfiguration, useCompression);
 			int added = 0;
 
 			// Read tile
 			for (byte zoomInterval = 0; zoomInterval < ste.getMapFile().getAmountOfZoomIntervals(); zoomInterval++) {
 				for (int y = ste.getMinY(zoomInterval); y <= ste.getMaxY(zoomInterval); y++) {
 					for (int x = ste.getMinX(zoomInterval); x <= ste.getMaxX(zoomInterval); x++) {
 						tile = ste.getTile(x, y, zoomInterval);
 
 						if (tile != null) {
 							writer.insert(tile, x, y, zoomInterval);
 							++added;
 						}
 
 						if (added % 100 == 0) {
 							System.out.printf("Added %7d tiles\r", added);
 						}
 
 					}
 				}
 			}
 
 			// Write batches
 			writer.finish();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (TileIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void countAndPrintNumberOfStreetEntries(String path) {
 		SimpleTileExtractor ste = null;
 		byte[] rawTile;
 		Tile tile = null;
 		List<Way> ways = null;
 		HashMap<String, Integer> globalCount = new HashMap<String, Integer>();
 
 		Integer count = null;
 
 		// Total amount of street names
 		int totalStreetNameCount = 0;
 		// Street names that are in more than 1 tile
 		int multipleOccurenceStreetNameCount = 0;
 		// Exception values (#tiles > 100 for a given street name)
 		List<Integer> exceptionalValues = new ArrayList<Integer>();
 
 		// Test if tile coordinates are correct (NEEDS DEBUG FILE!)
 		try {
 			ste = new SimpleTileExtractor(path);
 
 			for (byte zoomInterval = 1; zoomInterval < ste.getMapFile().getAmountOfZoomIntervals(); zoomInterval++) {
 				for (int y = ste.getMinY(zoomInterval); y <= ste.getMaxY(zoomInterval); y++) {
 					for (int x = ste.getMinX(zoomInterval); x <= ste.getMaxX(zoomInterval); x++) {
 						rawTile = ste.getTile(x, y, zoomInterval);
 						if (rawTile == null)
 							continue;
 
 						tile = TileFactory.getTileFromRawData(rawTile, zoomInterval, ste.getMapFile());
 
 						// Get all way
 						ways = tile.getWays();
 
 						if (ways == null)
 							continue;
 
 						HashMap<String, Integer> localCount = new HashMap<String, Integer>();
 
 						for (Way w : ways) {
 							if (w.getName() == null)
 								continue;
 
 							if (localCount.containsKey(w.getName())) {
 								count = Integer.valueOf(localCount.get(w.getName()).intValue() + 1);
 								localCount.put(w.getName(), count);
 
 							} else {
 								localCount.put(w.getName(), new Integer(1));
 							}
 
 						}
 
 						// System.out.println("This tile has " + localCount.size() + " street names.");
 						for (String key : localCount.keySet()) {
 							// if (localCount.get(key).intValue() > 1) {
 							// System.out.println("  " + key + ": " + localCount.get(key));
 							// }
 
 							// Has the street name been discovered before?
 							if (globalCount.containsKey(key)) {
 								count = Integer.valueOf(globalCount.get(key).intValue() + 1);
 								globalCount.put(key, count);
 							} else {
 								++totalStreetNameCount;
 								globalCount.put(key, new Integer(1));
 							}
 
 						}
 
 						tile = null;
 
 					}
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (TileIndexOutOfBoundsException e) {
 			System.err.println(e.getMessage());
 		}
 
 		int[] streetNamesPerTile = new int[100];
 
 		for (String key : globalCount.keySet()) {
 			try {
 				++streetNamesPerTile[globalCount.get(key).intValue()];
 			} catch (ArrayIndexOutOfBoundsException e) {
 				System.err.println(e.getMessage());
 
 				try {
 					exceptionalValues.add(Integer.parseInt(e.getMessage()));
 				} catch (NumberFormatException e1) {
 					System.err.println(e.getMessage());
 				}
 
 			}
 
 			if (globalCount.get(key).intValue() > 1) {
 				++multipleOccurenceStreetNameCount;
 			}
 		}
 
 		System.out.println("----------");
 		System.out.println("TileCount | # Street names");
 		for (int i = 1; i < streetNamesPerTile.length; i++) {
 			System.out.printf("    %3d   |      %4d      \r\n", i, streetNamesPerTile[i]);
 		}
 
 		System.out.println("----------");
 		System.out.println("Outsiders: ");
 		Collections.sort(exceptionalValues);
 		for (Integer i : exceptionalValues) {
 			System.out.print(i + ", ");
 		}
 		System.out.println();
 
 		System.out.println("----------");
 		System.out.println("Total street names: " + totalStreetNameCount);
 		System.out.println("Streetnames in > 1 tiles: " + multipleOccurenceStreetNameCount);
 		System.out.printf("Total: %2.2f%%\r\n", (100.0f * multipleOccurenceStreetNameCount / totalStreetNameCount));
 
 	}
 
 	/**
 	 * @param args
 	 *            not used command line parameters.
 	 */
 	public static void main(String[] args) throws Exception {
		countAndPrintNumberOfStreetEntries("/home/moep/maps/berlin.map");
 	}
 }
