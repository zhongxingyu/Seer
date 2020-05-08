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
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 /**
  * 
  * @author Karsten Groll
  * 
  *         Main class for executing various tests helping understanding the map file format.
  */
 
 public class MapFileDebuggerMain {
 
 	private static void createFileDataStructure(int numXDimensions, int numYDimensions, int dataSize) {
 		try {
 			System.out.print("Creating file data structure...");
 			FileWriter.createDirStructureWithFiles("files/", numXDimensions, numYDimensions, dataSize);
 			System.out.print("done.\n");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void createSQLiteDataStructure(int numXDimensions, int numYDimensions, int dataSize) {
 		try {
 			System.out.print("Creating SQLite data structure...");
 			FileWriter.createSQLiteDB("test.db", numXDimensions, numYDimensions, dataSize);
 			System.out.print("done.\n");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// private static List<Tuple<Integer, Integer>> createRandomLookups(long seed, int count) {
 	// LinkedList<Tuple<Integer, Integer>> ret = new LinkedList<Tuple<Integer, Integer>>();
 	// Random r = new Random(seed);
 	//
 	// for (int i = 0; i < count; i++) {
 	// ret.add(new Tuple<Integer, Integer>(r.nextInt(300), r.nextInt(300)));
 	// }
 	//
 	// return ret;
 	// }
 	//
 	// private static void performQueries(final List<Tuple<Integer, Integer>> queries) {
 	// for (Tuple<Integer, Integer> t : queries) {
 	// try {
 	// // FileReader.readFile("files/" + t.e1 + "/" + t.e2 + ".y");
 	// FileReader.readFile("files/bzs7/" + t.e1 + "/" + t.e2 + ".y");
 	// } catch (IOException e) {
 	// System.err.println(e.getMessage());
 	// }
 	//
 	// }
 	// }
 
 	/**
 	 * @param args
 	 *            not used command line parameters.
 	 */
 	public static void main(String[] args) {
 		MapFormatReader mfr = null;
 		MapFile mf = null;
 		try {
			mfr = new MapFormatReader("/home/moep/berlin.map");
 			mf = mfr.parseFile();
 			mfr.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		if (mf != null) {
 			// System.out.println(mf);
 			for (SubFile sf : mf.getSubFiles()) {
 				// System.out.println(sf);
 			}
 		}
 
 		// createFileDataStructure(300, 300, 5);
 		// createSQLiteDataStructure(300, 300, 5);
 
 		// List<Tuple<Integer, Integer>> queries = createRandomLookups(42, 1000);
 		// performQueries(queries);
 
 		// try {
 		// FileReader fr = new FileReader(512 * 1024, "files/bla.bin");
 		// System.out.println("Reading chunks");
 		// byte[] chunk;
 		// while ((chunk = fr.nextChunk()) != null) {
 		//
 		// }
 		//
 		// fr.close();
 		// } catch (IOException e) {
 		// e.printStackTrace();
 		// }
 		//
 		// System.out.println("Done");
 
 		// try {
 		// FileWriter.createFile("files/bla.bin", 1024 * 300);
 		// } catch (IOException e) {
 		// e.printStackTrace();
 		// }
 
 	}
 }
