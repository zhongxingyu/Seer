 package org.eclipse.stem.internal.data.utility;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong;
 import org.eclipse.stem.definitions.adapters.spatial.geo.PlatformLatLongDataProvider;
 import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong.Segment;
 
 /**
  * NeighborUtility generates the CommonBorder property file.
  * 
  * <pre>
  * NeighborUtility has to run as an Eclipse Plugin application 
  * because it uses Platform.LatLongProvider to process the 
  * LatLong xml files and it will only run in a plugin environment.
  * 
  * *** NOTE ***
  *   The path for the Eclipse workspace is hardcoded below.
  *   Change the value for &quot;baseFolder&quot; to reflect your environment.
  *   Also the output is generated in whereever your system defines 
  *   as the &quot;Temporary&quot; folder.  You would then copy the files to 
  *     org.eclipse.stem.internal.data 
  *        resources/data/relationship/commonborder 
  *   and commit them to CVS.
  *   
  * To generate the required launch script:
  *   
  *  - From PackageExplorer, RightClick src for NeighborUtilityTest
  *  - Select RunAs-&gt;JUnit Plug-in Test
  *    It should startup a new image of Eclipse, run NeighborUtility 
  *    and terminate.  The console output should indicate any errors.
  *    
  * 
  * To make it easier to invoke, we use the JUnit plugin environment
  * to setup the plugin environment and invoke the following 
  * NeighborUtilityTest. 
  * NeighborUtilityTest will invoke the following methods in MeighborUtility
  * &lt;pre&gt;
  *   doAllInternal  - creates neighbor list for the admin 1 or 2 areas in 
  *                    each country. creates CCC_2_CCC_2.properties where 
  *                    CCC is the country code. 
  *                    
  *   doWorld        - creates a list of neighboring countries from the 
  *                    list of all countries. creates ZZZ_0_ZZZ_0.properties
  * 
  *   doAllBorders    - uses ZZZ_0_000.properties to identify neighboring 
  *                     countries and builds bordering neighbor lists 
  *                     from the admin 2 (or admin 1) areas in each country.
  *                     
  *                     
  * The methods setBBox and ProcessBBox do the actual processing. 
  * 
  * 
  *  API for setBBox and processBBox
  *   //Interate through the nodes.
  *    for (final Iterator nodeIter = canonicalGraph.getNodes().values()
  *        .iterator(); nodeIter.hasNext();) {
  *        final Node node = (Node) nodeIter.next();
  *        final LatLongProvider latLongProvider = (LatLongProvider) LatLongProviderAdapterFactory.INSTANCE
  *            .adapt(node, LatLongProvider.class);
  *  //The node may or may not have lat/long data associated with
  *  //it, if it doesn't the list that's returned will be empty.
  *      final LatLong latLong = latLongProvider.getLatLong();
  *  //Now for each node we create a BoundingBox for each polygon 
  *  //  and call setBBox to save the BoundingBox in a map.				
  *      String name = node.getDublinCore().getTitle();
  *      NeighborUtility.setBBox(map,bbox);
  *     } // for
  *  // We now have a map of AdminArea for one country and another map for 
  *  // for the neighboring country    
  *  //Now we call the processBBox method to do the processing
  *  // BBox1 is adjacent to BBox2 if any of the corners of BBox1 is contained
  *  // in BBox2 or any corner of BBox2 is contained in BBox1
  *  // if that is true they they might be neighbors and we check further
  *  // We compare all of the points of Polygon1 to match against polygon2
  *  // If we find a match then they are neighbors.
  *  //
  *  // If no match try again this time looking for points that match within 
  *  // a small delta. 
  *   
  *  // ifstill not match we make one more try, 
  *  // check if any of the points for 
  *  // polygon1 are contained inside polygon2 or visaversa. 
  *  
  *      List&lt;String[]&gt; list = NeighborUtility.processBBox(bbox1,bbox2);
  *      -&gt; testAdjacency(bbox1,bbox2)  test AdminArea containment
  *      -&gt; testAdjacencyMatch(bbox1,box2) Test if points match  
  *      
  *  // The resulting ArrayList contains an array of 2 Strings , each
  *  // representing 2 neighboring areas.  
  *  // We then call the writeProperties method to write the 
  *  // properties file.
  *      writeProperties(list,admin0a,admin0b);
  *  // The properties files will be written to the temporary directory
  *  // On Windosw:  c:/tmp/commonborders/CCC_2_XXX_2.properties    
  *  //
  *     It needs to be copied to org.eclipse.stem.internal.data
  *          /resources.data/relationship/comonborder 
  *     and then checked into CVS/SVN     
  *  &lt;/pre&gt;
  * 
  */
 public class NeighborUtility {
 
 	/**
 	 * User 's unique folder where they keep all of the STEM code. ******* Must
 	 * be changed *****
 	 */
 	public static String workspaceFolder = "c:/stem/";
 
 	/**
 	 * if true then debug info can be written
 	 */
 	public static boolean DEBUG = true;
 
 	static int MAX = 50;
 	static int counter = 0;
 
 	/**
 	 * URI reference to the LatLong data baseURI/country/CCC/CCC_0_MAP.xml
 	 */
 	public static String baseURI = "platform:/plugin/org.eclipse.stem.geography/"
 			+ "resources/data/geo/country/";
 	/**
 	 * Hard coded File reference to the above URI Needed because we don;t know
 	 * how to convert a URI to a file path
 	 */
 	public static String baseFolder = workspaceFolder
 			+ "org.eclipse.stem.geography/" + "resources/data/geo/country/";
 	/**
 	 * Hard coded File reference to the common border property files. Needed
 	 * because we don;t know how to convert a URI to a file path
 	 */
 	public static String commonborderFolder = workspaceFolder
 			+ "org.eclipse.stem.internal.data/"
 			+ "resources/data/relationship/commonborder/";
 	/**
 	 * Used to build an error message for neighbors with no common border
 	 */
 	public static StringBuffer problems = new StringBuffer(2000);
 
 	/**
 	 * statistics
 	 */
 	private static int c_bboxNeighbors = 0;
 	private static int c_pointNeighbors = 0;
 	private static int c_closeNeighbors = 0;
 	private static int c_segmentNeighbors = 0;
 	private static int c_Neighbors = 0;
 	private static long c_start = new Date().getTime();
 	private static final long start = new Date().getTime();
 
 	/**
 	 * if true then only check for neighbors using BBox check This is used for
 	 * generating the ZZZ world list.
 	 */
 	public static boolean bboxCheckOnly = false;
 
 	// ****************************************************************
 	// static methods
 	// ****************************************************************
 	/**
 	 * Build neighbor list for neighbors within a country We will call
 	 * setBBoxArea for each Admin area in the country then we will call
 	 * processBBox to prepare the list of neighbors.
 	 * 
 	 * @param fixture
 	 *            instance of PlatformLatLongDataProvider
 	 * @param admin0
 	 *            Admin0 name for the country (i.e USA)
 	 * @param adminFile
 	 *            Admin file (i.e. USA_2_MAP.xml)
 	 * 
 	 * #return list of String[2] where [0] is an Admin Area (1 or 2) and [1] is
 	 * a neighboring Admin area (1 or 2)
 	 */
 	static List<String[]> neighborCheck(
 			final PlatformLatLongDataProvider fixture, final String admin0,
 			final String adminFile) {
 
 		final String uriStr = baseURI + admin0 + "/" + adminFile;
 
 		// final URI uri = URI.createURI(uriString);
 		// final Collection<String> ids = fixture.getIds(uri);
 		/**
 		 * Map of areas and Boundaryboxes used for the process.
 		 */
 		final Map<String, AdminArea[]> map = buildMap(fixture, uriStr);
 		// new HashMap<String,AdminArea[]>(4000);
 		// for (String id : ids) {
 
 		// final LatLong latLong = fixture.getLatLong(URI.createURI(uriString
 		// + "#" + id));
 		// NeighborUtility.setArea(map,id, latLong);
 
 		// } // for each id
 		final List<String[]> list = NeighborUtility.processAreas(map);
 
 		return list;
 
 	}
 
 	/**
 	 * Build neighbor list for neighbors in bordering countries
 	 * 
 	 * @param fixture
 	 * @param admin0
 	 *            Admin0A name for the country (i.e USA)
 	 * @param adminFile
 	 *            AdminFileA file (i.e. USA_2_MAP.xml)
 	 * @param admin0
 	 *            Admin0B name for the country (i.e MEX)
 	 * @param adminFile
 	 *            AdminFileB file (i.e. MEX_2_MAP.xml)
 	 * @return List containing array of String
 	 * 
 	 */
 	static List<String[]> neighborCheck(
 			final PlatformLatLongDataProvider fixture, final String admin0A,
 			final String adminFileA, final String admin0B,
 			final String adminFileB) {
 
 		final String uriString1 = baseURI + admin0A + "/" + adminFileA;
 		final String uriString2 = baseURI + admin0B + "/" + adminFileB;
 		final Map<String, AdminArea[]> map1 = buildMap(fixture, uriString1);
 		final Map<String, AdminArea[]> map2 = buildMap(fixture, uriString2);
 
 		final List<String[]> list = NeighborUtility.matchArea(map1, map2);
 
 		return list;
 	} // neighborCheck
 
 	/**
 	 * Build Map of areas and Boundaryboxes used for the process.
 	 * 
 	 * @param fixture
 	 * @param uriStr
 	 *            URI to obtain lat/long data
 	 * 
 	 */
 
 	private static Map<String, AdminArea[]> buildMap(
 			final PlatformLatLongDataProvider fixture, final String uriStr) {
 		final Map<String, AdminArea[]> map = new HashMap<String, AdminArea[]>(
 				4000);
 		final URI uri = URI.createURI(uriStr);
 		final Collection<String> ids = fixture.getIds(uri);
 
 		for (final String id : ids) {
 			final LatLong latLong = fixture.getLatLong(URI.createURI(uriStr
 					+ "#" + id));
 			NeighborUtility.setArea(map, id, latLong);
 
 		} // for each id
 		return map;
 	}
 
 	/**
 	 * Build neighbor list for neighbors within a country for all of the
 	 * countries in the world.
 	 * 
 	 * @param fixture
 	 * 
 	 */
 	static void doAllInternal(final PlatformLatLongDataProvider fixture) {
 
 		final List<String> world = getAdmin0List();
 		for (final String admin0 : world) {
 
 			final String admin = getAdmin(admin0);
 			if (admin.contains("_0_")) {
 				// do not make xxx_0_xxx_0 file
 				continue;
 			}
 			if (admin != null) {
 				// debug(admin0+": "+admin);
 				output("** " + admin0);
 				final List<String[]> list = neighborCheck(fixture, admin0,
 						admin);
 				writeProperties(list, admin0, admin, admin0, admin);
 				statistics();
 				if (list.size() == 0) {
 					problems.append(admin0 + ": " + admin + "\n");
 				}
 			}
 		}
 		if (problems.length() > 0) {
 			debug("***");
 			debug("*** Admin areas with no internal neighbors:");
 			debug("***");
 			debug(problems.toString());
 			problems = new StringBuffer(2000);
 		}
 
 	}
 
 	/**
 	 * This will generate a list of neighboring countries.
 	 * ZZZ_0_ZZZ_0.properties
 	 * 
 	 * @param fixture
 	 *            needed to get the LatLong data
 	 * @param doAll
 	 *            true if doAllBorders will be run next using the returned list.
 	 */
 	static List<String[]> doWorld(final PlatformLatLongDataProvider fixture,
 			final boolean doAll) {
 		/**
 		 * Map of areas and Boundaryboxes used for the process.
 		 */
 		final Map<String, AdminArea[]> map = new HashMap<String, AdminArea[]>(
 				4000);
 
 		final List<String> listZZZ = getAdmin0List();
 		if (doAll) {
 			// we will get more world matches this way
 			// and remove them from the ZZZ list as we go
 			bboxCheckOnly = true;
 
 		}
 		for (final String admin0 : listZZZ) {
 			// debug("Do: "+admin0);
 			if (admin0.equals("ALB") || admin0.equals("GRC")) {
 				debug(admin0);
 			}
 
 			final String adminFile = getAdmin0(admin0);
 			if (!adminFile.contains("_0_")) {
 				output(admin0 + " admin 0 file  does not exist");
 				// continue;
 			}
 			final String uriString = baseURI + admin0 + "/" + adminFile;
 			final URI uri = URI.createURI(uriString);
 			final Collection<String> ids = fixture.getIds(uri);
 			if (ids == null || ids.size() == 0) {
 				output(admin0 + ": " + adminFile + " does not exist");
 			} else {
 				for (final String id : ids) {
 
 					final LatLong latLong = fixture.getLatLong(URI
 							.createURI(uriString + "#" + id));
 					if (adminFile.contains("_0_")) {
 						// debug("call setArea for id: "+id);
 						NeighborUtility.setArea(map, id, latLong);
 					} else {
 						// Since we are using the admin1 file instead of
 						// the admin 9 file , we have to lie about the
 						// id for the polygons.
 						// debug("call setArea for admin0: "+admin0);
 						NeighborUtility.setArea(map, admin0, latLong);
 					}
 				} // for each id
 			}
 
 		}
 		debug("doWorld: area setup complete.");
 		// find the neighboring countries based on bbox
 		final List<String[]> list = processAreas(map);
 		debug("ZZZ: ");
 		statistics();
 		// list now has neighbors for each country in world
 		// Now process each set of bordering countries
 
 		writeProperties(list, "ZZZ", "ZZZ_0", "ZZZ", "ZZZ_0");
 		// statistics();
 		return list;
 	}
 
 	/**
 	 * This will generate a list of neighbors for all neighboring countries. It
 	 * uses an array list (from DoWorld) as the source of neighbor countries.
 	 * 
 	 * @param fixture
 	 *            needed to get the LatLong data
 	 * @param worldList
 	 *            List of neighboring admin0 areas
 	 * @return true
 	 */
 	public static boolean doAllBorders(
 			final PlatformLatLongDataProvider fixture,
 			final List<String[]> worldList) {
 		output("doAllBorders: " + worldList.size());
 		bboxCheckOnly = false;
 		final ListIterator<String[]> it = worldList.listIterator();
 		while (it.hasNext()) {
 			final String[] b = it.next();
 			final String admin0A = b[0];
 			final String admin0B = b[1];
 			final String adminA = getAdmin(admin0A);
 			final String adminB = getAdmin(admin0B);
 			if (adminA == null || adminB == null) {
 				continue;
 			}
 			output(admin0A + ":" + adminA + " : " + admin0B + ":" + adminB);
 			final List<String[]> list = neighborCheck(fixture, admin0A, adminA,
 					admin0B, adminB);
 
 			if (list.size() == 0) {
 				output("*** Remove from ZZZ: " + admin0A + ":" + admin0B);
 				it.remove();
 			} else {
 				writeProperties(list, admin0A, adminA, admin0B, adminB);
 			}
 
 			statistics();
 		}
 		writeProperties(worldList, "ZZZ", "ZZZ_0", "ZZZ", "ZZZ_0");
 		return true;
 	}
 
 	/**
 	 * This will generate a list of neighbors for all neighboring countries. It
 	 * uses ZZZ_0_ZZZ_0.properties as the source of neighbor countries.
 	 * 
 	 * @param fixture
 	 *            needed to get the LatLong data
 	 */
 	static void doAllBorders(final PlatformLatLongDataProvider fixture) {
 		// get the version of ZZZ that we just built
 		// String folder = NeighborUtility.getPropertyFolder();
 		// Get the checked in version of ZZZ
 		final String folder = commonborderFolder;
 		final List<String[]> worldList = new ArrayList<String[]>();
 		Properties world = null;
 		File f = null;
		FileInputStream is = null;
 		try {
 			f = new File(folder, "ZZZ_0_ZZZ_0.properties");
			is = new FileInputStream(f);
 			world = new Properties();
 			world.load(is);
 
 			String key = null;
 			final Enumeration<Object> e = world.keys();
 			while (e.hasMoreElements()) {
 				key = (String) e.nextElement();
 				try {
 					@SuppressWarnings("unused")
 					final int n = Integer.parseInt(key);
 				} catch (final NumberFormatException e1) {
 					continue;
 				}
 
 				final String value = world.getProperty(key);
 				if (value != null) {
 					final StringTokenizer st = new StringTokenizer(value, ",");
 					if (st.countTokens() >= 2) {
 						final String v1 = st.nextToken().trim();
 						final String v2 = st.nextToken().trim();
 
 						final String[] b = { v1, v2 };
 						worldList.add(b);
 					}
 				}
 			}
 			output("worldList size: " + worldList.size());
 			doAllBorders(fixture, worldList);
 		} catch (final FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (final IOException e1) {
 			e1.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				// Ignore exceptions on close
			}
 		}
 	}
 
 	/**
 	 * Get the file name for the most detailed admin area for the specified
 	 * admin 0
 	 * 
 	 * @param admin0
 	 * @return file name for the admin area (i.e. USA_2_MAP.xml)
 	 */
 	public static String getAdmin(final String admin0) {
 		String adminfile2 = null;
 		String adminfile0 = null;
 		final File parent = new File(baseFolder + "/" + admin0);
 		if (parent.exists()) {
 			final String[] files = parent.list();
 			for (final String file : files) {
 				if (file.contains("_2_")) {
 					adminfile2 = file;
 					break;
 				} else if (file.contains("_1_")) {
 					// use _1_ if _2_ does not exist
 					adminfile2 = file;
 				} else if (file.contains("_0_")) {
 					adminfile0 = file;
 				}
 			}
 		}
 		if (adminfile2 == null) {
 			return adminfile0;
 		}
 		return adminfile2;
 	}
 
 	/**
 	 * Get the file name for the admin0 file (or admin1 if admin0 does not
 	 * exist)
 	 * 
 	 * @param admin0
 	 * @return file name for the admin area (i.e. USA_0_MAP.xml)
 	 */
 	public static String getAdmin0(final String admin0) {
 		String admin = null;
 		final File parent = new File(baseFolder + "/" + admin0);
 		if (parent.exists()) {
 			final String[] files = parent.list();
 			for (final String file : files) {
 				if (file.contains("_0_")) {
 					admin = file;
 					break;
 				} else if (file.contains("_1_")) {
 					admin = file;
 					break;
 				} else if (file.contains("_2_")) {
 					admin = file;
 					debug("getAdmin0: file: " + file);
 				}
 			}
 		}
 		return admin;
 	}
 
 	/**
 	 * Get the list of admin 0 areas
 	 * 
 	 * @return List of admin0 areas ( 3 character country codes)
 	 */
 	static List<String> getAdmin0List() {
 		final String worldFolder = baseFolder;
 		final String[] admin0 = new File(worldFolder).list();
 
 		final List<String> world = new ArrayList<String>();
 		for (final String e : admin0) {
 			if (e.equals(".svn")) {
 				continue;
 			}
 			if (e.equals("CVS")) {
 				continue;
 			}
 			world.add(e);
 		}
 		return world;
 	}
 
 	/**
 	 * Write the property file based on the List of neighbors if called with
 	 * (list,"USA","USA_2_MAP", "MEX","MEX_2_MAP" then it will write a file
 	 * USA_2_MEX_2.properties that contains entries like 1 = US-AZ-35023, SON, 0
 	 * 
 	 * @param list
 	 *            List of String[2]
 	 * @param admin0A
 	 *            Admin0 code for area 1
 	 * @param adminA
 	 *            Admin file
 	 * @param admin0B
 	 *            Admin0 code for area 2
 	 * @param adminB
 	 *            Admin file
 	 */
 	static void writeProperties(final List<String[]> list,
 			final String admin0A, final String adminA, final String admin0B,
 			final String adminB) {
 		try {
 			OutputStream os = null;
 			try {
 
 				final File folder = new File(NeighborUtility
 						.getPropertyFolder());
				if (!folder.mkdir()) {
					throw new IOException("Unable to create property folder: "+ folder.getAbsolutePath());
				}
 				final File fn = new File(folder, adminA.substring(0, 5) + "_"
 						+ adminB.substring(0, 5) + ".properties");
 				debug("Output File: " + fn);
 
 				os = new FileOutputStream(fn, false);
 
 				String title = "Common Border relationship between country "
 						+ admin0A + " and " + admin0B;
 				if (admin0A.equals(admin0B)) {
 					title = "Common Border relationship within country "
 							+ admin0A;
 				}
 				if (admin0A.equals("ZZZ")) {
 					title = "Common Border relationship between all countries in the world";
 				}
 				String comments = "# " + "\n" + "#  File generated from:"
 						+ "\n" + "#   " + baseFolder + admin0A + "/" + adminA
 						+ "\n" + "#   " + baseFolder + admin0B + "/" + adminB
 						+ "\n" + "#\n" + "#  Generated by: "
 						+ NeighborUtility.class + "\n" + "# " + "\n";
 				if (admin0A.equals(admin0B)) {
 					comments = "# " + "\n" + "#  File generated from:" + "\n"
 							+ "#   " + baseFolder + admin0A + "/" + adminA
 							+ "\n" + "#\n" + "#  Generated by: "
 							+ NeighborUtility.class + "\n" + "# " + "\n";
 				}
 				os.write(comments.getBytes());
 				String property = "RECORD_CLASSNAME = org.eclipse.stem.internal.data.CommonBorderRelationshipGraphRecord"
 						+ "\n";
 				os.write(property.getBytes());
 				property = "TITLE = " + title + "\n";
 				os.write(property.getBytes());
 				property = "VALID = start=2006-01-01;" + "\n";
 				os.write(property.getBytes());
 				final String levelA = adminA.substring(4, 5);
 				final String levelB = adminB.substring(4, 5);
 				String keyInfo = "ISOKEY_0 = " + admin0A + "\n";
 				keyInfo += "ISOKEY_0_LEVEL = " + levelA + "\n";
 				keyInfo += "ISOKEY_1 = " + admin0B + "\n";
 				keyInfo += "ISOKEY_1_LEVEL = " + levelB + "\n";
 				os.write(keyInfo.getBytes());
 				final int n = list.size();
 				for (int i = 0; i < n; i++) {
 					final String[] entry = list.get(i);
 					final StringBuffer sb = new StringBuffer(80);
 					sb.append("" + (i + 1));
 					sb.append(" = ");
 					sb.append(entry[0]);
 					sb.append(", ");
 					sb.append(entry[1]);
 					sb.append(", 0");
 					sb.append("\n");
 					os.write(sb.toString().getBytes());
 				}
 
 				os.flush();
 				os.close();
 			} catch (final FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Called to input another LatLong object This builds a Map of lat/long
 	 * areas for an admin area
 	 * 
 	 * @param map
 	 *            HashMap of ID to BoundaryBox for polygon
 	 * @param id
 	 *            Admin area id
 	 * @param latLong
 	 *            Latitude/longitude info
 	 */
 	public static void setArea(final Map<String, AdminArea[]> map,
 			final String id, final LatLong latLong) {
 		final List<Segment> list = latLong.getSegments();
 		final int s = list.size();
 		final Segment[] segments = new Segment[s];
 		for (int i = 0; i < s; i++) {
 			segments[i] = list.get(i);
 		}
 		final Polygon[] polygons = getPolygons(latLong);
 		final int n = polygons.length;
 		final AdminArea[] areas = new AdminArea[n];
 		for (int i = 0; i < n; i++) {
 			final Rectangle rectangle = polygons[i].getBounds();
 			areas[i] = new AdminArea(segments[i], rectangle, id);
 		}
 		// debug("create: "+name+": ");
 		final AdminArea[] orig = map.get(id);
 		if (orig == null) {
 			map.put(id, areas);
 		} else {
 			final int l = areas.length;
 			int o = orig.length;
 			final AdminArea[] newAreas = new AdminArea[l + o];
 			for (int i = 0; i < o; i++) {
 				newAreas[i] = orig[i];
 			}
 			final int a = areas.length;
 			for (int i = 0; i < a; i++, o++) {
 				newAreas[o] = areas[i];
 			}
 			map.put(id, newAreas);
 		}
 		// }
 	}
 
 	/**
 	 * 
 	 * After all the latLong data has been submitted this method is called to
 	 * generate the list of internal neighbors from this Map of AdminAreas.
 	 * 
 	 * BBox1 is adjacent to BBox2 if any of the corners of BBox1 is contained in
 	 * BBox2 or any corner of BBox2 is contained in BBox1
 	 * 
 	 * @param map
 	 *            Map of AdminArea rectangles keyed by admin area
 	 * 
 	 * @return List of neighbors.
 	 */
 	public static List<String[]> processAreas(final Map<String, AdminArea[]> map) {
 		/**
 		 * List of neighboring areas. This is the result and needs to be turned
 		 * into a properties file.
 		 */
 		final List<String[]> outlist = new ArrayList<String[]>(4000);
 		int counter = 0;
 		String[] entry; // = new String[2];
 
 		final Set<String> set = map.keySet();
 		final int n = set.size();
 		final String[] keys = set.toArray(new String[n]);
 		Arrays.sort(keys);
 		for (int i = 0; i < n; i++) {
 			final String key = keys[i];
 
 			// Rectangle[] bboxes = map.get(key);
 			// debug(key);
 			for (int j = i + 1; j < n; j++) {
 				if (key.contains("ALB") && keys[j].contains("GRC")) {
 					debug(key);
 				}
 				final boolean neighbor = isNeighbor(map, map, key, keys[j]);
 				if (neighbor) {
 					c_Neighbors++;
 					counter++;
 					debug(counter + " = " + key + ", " + keys[j] + ", 0");
 					entry = new String[2];
 					entry[0] = key;
 					entry[1] = keys[j];
 					outlist.add(entry);
 				}
 			}
 		}
 		// debug("Number of Neighbors: "+counter);
 		return outlist;
 	}
 
 	/**
 	 * 
 	 * After all the latLong data has been submitted this method is called to
 	 * generate the list of neighbors between the first set of Admin areas and
 	 * the 2nd
 	 * 
 	 * 
 	 * 
 	 * @param map1
 	 *            Map of AdminArea rectangles keyed by admin area
 	 * @param map2
 	 *            Map of AdminArea rectangles keyed by admin area
 	 * @return List of neighbors.
 	 */
 	public static List<String[]> matchArea(final Map<String, AdminArea[]> map1,
 			final Map<String, AdminArea[]> map2) {
 		/**
 		 * List of neighboring areas. This is the result and needs to be turned
 		 * into a properties file.
 		 */
 		final List<String[]> outlist = new ArrayList<String[]>(4000);
 		int counter = 0;
 		String[] entry = null;
 
 		final Set<String> set1 = map1.keySet();
 		final int n1 = set1.size();
 		final String[] keys1 = set1.toArray(new String[n1]);
 		Arrays.sort(keys1);
 		final Set<String> set2 = map2.keySet();
 		final int n2 = set2.size();
 		final String[] keys2 = set2.toArray(new String[n2]);
 		Arrays.sort(keys2);
 		for (int i = 0; i < n1; i++) {
 			final String key = keys1[i];
 			for (int j = 0; j < n2; j++) {
 				final String key2 = keys2[j];
 				// if ( key.contains("SON") && key2.contains("AZ")){
 				// debug("** "+key+" : "+key2);
 				// }
 
 				final boolean neighbor = isNeighbor(map1, map2, key, key2);
 
 				if (neighbor) {
 					c_Neighbors++;
 					counter++;
 					debug("isNeighbor match: " + key + " " + key2);
 					entry = new String[2];
 					entry[0] = key;
 					entry[1] = key2;
 					outlist.add(entry);
 
 				}
 			}
 		}
 		// if (counter == 0) {
 		// debug("Number of Neighbors: "+counter);
 		// }
 		return outlist;
 	}
 
 	/**
 	 * Given a LatLong, generate an array of polygons.
 	 * 
 	 * @param latLong
 	 * @return array of Polygons
 	 */
 	private static Polygon[] getPolygons(final LatLong latLong) {
 
 		final List<Segment> list = latLong.getSegments();
 		final int numareas = list.size();
 		final Polygon[] polygons = new Polygon[numareas];
 		for (int i = 0; i < numareas; i++) {
 
 			final Segment segment = list.get(i);
 			final Polygon polygon = getPolygon(segment);
 
 			// debug("Polygon: "+polygon);
 			polygons[i] = polygon;
 		}
 
 		return polygons;
 	}
 
 	/**
 	 * Given a LatLong Segment generate a polygon.
 	 * 
 	 * @param segment
 	 * @return Polygon
 	 */
 	private static Polygon getPolygon(final Segment segment) {
 
 		final Polygon polygon = new Polygon();
 		final int size = segment.size();
 		for (int p = 0; p < size; p++) {
 			final double x = segment.longitude(p);
 			final double y = segment.latitude(p);
 			final int xint = (int) ((x + 180) * 1000);
 			final int yint = (int) ((y + 90) * 1000);
 			polygon.addPoint(xint, yint);
 		}
 		return polygon;
 	} // getPolygon
 
 	/**
 	 * Given the names of 2 administrative areas, determine if they are
 	 * neighbors. For each admin area we have one or more bounding boxes. They
 	 * may be neighbors if any bounding box is adjacent to any bounding box for
 	 * the other admin area. If the bounding box test is true then it is worth
 	 * checking if they are really neighbors by checking first if any points on
 	 * the borders match. If so they are really neighbors and it returns true.
 	 * if not it checks if any point on the border of one area is contained in
 	 * the polygon for the other area.
 	 * 
 	 * @param map1
 	 * @param map2
 	 * @param key1
 	 *            area name
 	 * @param key2
 	 *            area name
 	 * 
 	 * @return true if neighboring areas
 	 */
 
 	private static boolean isNeighbor(final Map<String, AdminArea[]> map1,
 			final Map<String, AdminArea[]> map2, final String key1,
 			final String key2) {
 		boolean neighbor = false;
 
 		final AdminArea[] areas1 = map1.get(key1);
 		final AdminArea[] areas2 = map2.get(key2);
 
 		for (final AdminArea area1 : areas1) {
 			for (final AdminArea area2 : areas2) {
 				// provide a place to set breakpoint
 				if (key1.contains("ALB") && key2.contains("GRC")) {
 					debug(key1 + " : " + key2);
 				}
 				neighbor = testAdjacency(area1, area2); // bbox ck
 				if (neighbor) {
 					c_bboxNeighbors++;
 					// debug("*BBox Match: "+key1+" : "+key2);
 					if (bboxCheckOnly) {
 						// debug("*AdminArea Match:"+key1+" : "+key2);
 						// potential neighbor
 						return true;
 					}
 					// Now we know that they are potential neighbors
 					// now check if at least one border point matches
 					neighbor = testAdjacencyMatch(area1, area2);
 					if (!neighbor) {
 						// not found by matching points try containment
 						neighbor = testAdjacencyContains(area1, area2);
 						if (neighbor) {
 							c_segmentNeighbors++;
 							debug("*Border containment Match: " + key1 + " : "
 									+ key2);
 							return true;
 						}
 						// keep checking areas
 					} else {
 						// We have neighbors
 						// counting was done in above method
 						return true;
 					}
 				}
 			}
 		}
 		return neighbor;
 	}
 
 	/**
 	 * Test adjacency by containment of any corner of two bounding boxes If any
 	 * corner of AdminArea 1 is contained in AdminArea 2 or any corner of
 	 * AdminArea 2 is contained in AdminArea 1 then they are adjacent.
 	 * 
 	 * @param bbox1
 	 *            bounding box 1
 	 * @param bbox2
 	 *            bounding box 2
 	 * @return true if adjacent or containment true
 	 */
 	public static boolean testAdjacency(final AdminArea bbox1,
 			final AdminArea bbox2) {
 		final Rectangle r1 = bbox1.bbox;
 		final Rectangle r2 = bbox2.bbox;
 		// coords of corners of first bounding box
 		final double xMin1 = r1.getMinX();
 		final double xMax1 = r1.getMaxX();
 		final double yMin1 = r1.getMinY();
 		final double yMax1 = r1.getMaxY();
 		// test overlap of bbox 1 to bbox2
 		boolean adjacent = true;
 		while (true) {
 			if (r2.contains(xMin1, yMin1)) {
 				break;
 			}
 			if (r2.contains(xMin1, yMax1)) {
 				break;
 			}
 			if (r2.contains(xMax1, yMax1)) {
 				break;
 			}
 			if (r2.contains(xMax1, yMin1)) {
 				break;
 			}
 			adjacent = false;
 			break;
 		}
 		if (!adjacent) {
 			// coords of corners of second bounding box
 			final double xMin2 = r2.getMinX();
 			final double xMax2 = r2.getMaxX();
 			final double yMin2 = r2.getMinY();
 			final double yMax2 = r2.getMaxY();
 			// test overlap of bbox2 to bbox1
 			adjacent = true;
 			while (true) {
 				if (r1.contains(xMin2, yMin2)) {
 					break;
 				}
 				if (r1.contains(xMin2, yMax2)) {
 					break;
 				}
 				if (r1.contains(xMax2, yMax2)) {
 					break;
 				}
 				if (r1.contains(xMax2, yMin2)) {
 					break;
 				}
 				adjacent = false;
 				break;
 			}
 		}
 		// if (adjacent) {
 		// debug(" AdminArea match: "+
 		// bbox1.name+" : "+bbox2.name);
 		// }
 		return adjacent;
 
 	}
 
 	/**
 	 * Test adjacency by matching the points on the border of each area for a
 	 * match
 	 * 
 	 * @param area1
 	 *            AdminArea class containing polygon1
 	 * @param area2
 	 *            AdminArea class containing polygon2
 	 * @return true if adjacent or containment true
 	 */
 	@SuppressWarnings("boxing")
 	public static boolean testAdjacencyMatch(final AdminArea area1,
 			final AdminArea area2) {
 		boolean adjacent = false;
 		final Segment s1 = area1.segment;
 		final Segment s2 = area2.segment;
 		final String key1 = area1.name;
 		final String key2 = area2.name;
 		final int size1 = s1.size();
 		final int size2 = s2.size();
 		// For every point is segment 1
 		// see if there is a matching point in segment2
 		for (int i = 0; i < size1; i++) {
 			final double lt = s1.latitude(i);
 			final double ln = s1.longitude(i);
 			for (int j = 0; j < size2; j++) {
 				if (lt == s2.latitude(j) && ln == s2.longitude(j)) {
 					// found a match
 					c_pointNeighbors++;
 					debug("*Border point Match: " + key1 + " : " + key2);
 
 					adjacent = true;
 					return true;
 				}
 			}
 		}
 
 		for (int i = 0; i < size1; i++) {
 			final double lt = Math.abs(s1.latitude(i));
 			final double ln = Math.abs(s1.longitude(i));
 			for (int j = 0; j < size2; j++) {
 				if (closeTo(lt, ln, Math.abs(s2.latitude(j)), Math.abs(s2
 						.longitude(j)))) {
 					debug(" closeTo match point: " + key1 + " : " + key2);
 					c_closeNeighbors++;
 					debug(String.format("%f %f %f %f", s1.latitude(i), s1
 							.longitude(i), s2.latitude(j), s2.longitude(j)));
 					return true;
 				}
 			}
 		}
 		return adjacent;
 
 	}
 
 	/**
 	 * Check if point1 is close to point 2 by the amount of the delta. A delta
 	 * of 0.01 is approx 1.11 km
 	 * 
 	 * @param lt1
 	 *            Point 1 latitude
 	 * @param ln1
 	 *            Point 1 longitude
 	 * @param lt2
 	 *            point 2 latitude
 	 * @param ln2
 	 *            point 2 longitude
 	 * @return true if within delta.
 	 */
 
 	public static boolean closeTo(final double lt1, final double ln1,
 			final double lt2, final double ln2) {
 		final double delta = 0.01;
 
 		if ((lt1 <= lt2 + delta && lt1 >= lt2 - delta)
 				&& (ln1 <= ln2 + delta && ln1 >= ln2 - delta)) {
 
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Test adjacency by checking if any point on the border of one area is
 	 * contained in the polygon for the other area
 	 * 
 	 * @param bbox1
 	 *            AdminArea class containing polygon1
 	 * @param bbox2
 	 *            AdminArea class containing polygon2
 	 * @return true if adjacent or containment true
 	 */
 	public static boolean testAdjacencyContains(final AdminArea bbox1,
 			final AdminArea bbox2) {
 		boolean adjacent = false;
 		final Segment s1 = bbox1.segment;
 		final Segment s2 = bbox2.segment;
 		final String key1 = bbox1.name;
 		final String key2 = bbox2.name;
 		final int size1 = s1.size();
 		final int size2 = s2.size();
 
 		// try bbox2 polygon contains BBox1 points
 		final Polygon polygon2 = getPolygon(s2);
 		for (int i = 0; i < size1; i++) {
 			final double lt = s1.latitude(i);
 			final double ln = s1.longitude(i);
 			final double x = ((ln + 180) * 1000);
 			final double y = ((lt + 90) * 1000);
 			adjacent = polygon2.contains(x, y);
 			if (adjacent) {
 				debug(" segment2 contains point " + i + " " + key1 + " : "
 						+ key2);
 				adjacent = true;
 				return true;
 			}
 		}
 
 		// still not found adjacency,
 		// try bbox1 polygon contains BBox2 points
 		for (int i = 0; i < size2; i++) {
 			final double lt = s2.latitude(i);
 			final double ln = s2.longitude(i);
 			final Polygon polygon1 = getPolygon(s1);
 			final double x = ((ln + 180) * 1000);
 			final double y = ((lt + 90) * 1000);
 			adjacent = polygon1.contains(x, y);
 
 			if (adjacent) {
 				debug(" segment1 contains point " + i + " " + key1 + " : "
 						+ key2);
 				adjacent = true;
 				return true;
 			}
 		}
 
 		return adjacent;
 
 	}
 
 	/**
 	 * get the folder to use for output.
 	 * 
 	 * @return Fully qualified path (e.g. c:/tmp/commonborder)
 	 */
 	public static String getPropertyFolder() {
 
 		String tmpFolder = null;
 		try {
 			final File tmpFile = File.createTempFile("test", ".txt");
 			tmpFolder = tmpFile.getParent();
			if (!tmpFile.delete()) {
				debug("Warning:  Unable to delete temporary file: "+ tmpFile.getAbsolutePath());
			}
 
 			tmpFolder = tmpFolder + "/commonborder/";
 		} catch (final IOException e) {
 			debug("Unable to determine tmp folder for property files.");
 			debug(e.getMessage());
 		}
 		return tmpFolder;
 	}
 
 	/**
 	 * print the statistics
 	 * 
 	 */
 	public static void statistics() {
 		final long doneAt = new Date().getTime() - c_start;
 		System.out.println("* Neighbor Areas based on BoundingBox:     "
 				+ c_bboxNeighbors);
 		System.out.println("* Neighbor Areas based on Matching points: "
 				+ c_pointNeighbors);
 		System.out
 				.println("* Neighbor Areas based on very close Matching points: "
 						+ c_closeNeighbors);
 		System.out.println("* Neighbor Areas based on containment:     "
 				+ c_segmentNeighbors);
 		System.out.println("* Final count of Neighbor Areas:            "
 				+ c_Neighbors);
 		System.out.println("* Elapsed time: " + doneAt);
 		c_bboxNeighbors = 0;
 		c_pointNeighbors = 0;
 		c_closeNeighbors = 0;
 		c_segmentNeighbors = 0;
 		c_Neighbors = 0;
 		c_start = new Date().getTime();
 	}
 
 	/**
 	 * print debug messages to the r log.
 	 * 
 	 * @param msg
 	 */
 	public static void debug(final String msg) {
 		if (DEBUG) {
 			System.out.println(getClock() + " " + msg);
 		}
 	}
 
 	/**
 	 * print messages to the log.
 	 * 
 	 * @param msg
 	 */
 	public static void output(final String msg) {
 
 		System.out.println(getClock() + " " + msg);
 
 	}
 
 	/**
 	 * debug output for exceptions
 	 * 
 	 * @param msg
 	 * @param e
 	 */
 	public static void debug(final String msg, final Throwable e) {
 		// log(msg,e);
 		debug(msg);
 		e.printStackTrace();
 	}
 
 	private static String getClock() {
 		final long now = new Date().getTime() - start;
 		return Long.toString(now);
 	}
 
 	// *************************************************************
 	// *************************************************************
 	/**
 	 * Class used to hold both the polygon and bounding box so it can be stored
 	 * in a HashMap and passed around to methods that need both.
 	 * 
 	 * 
 	 * 
 	 */
 	public static class AdminArea {
 		String name = null;
 		Rectangle bbox = null;
 		Segment segment = null;
 
 		/**
 		 * constructor
 		 * 
 		 * @param segment
 		 *            Contains the latLong info for polygon
 		 * @param bbox
 		 *            Bounding box for polygon
 		 * @param name
 		 *            Name associated with polygon
 		 */
 		public AdminArea(final Segment segment, final Rectangle bbox,
 				final String name) {
 			this.name = name;
 			this.bbox = bbox;
 			this.segment = segment;
 
 		}
 	}
 
 }
