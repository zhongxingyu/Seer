 package org.life.sl.mapmatching;
 
 /*
 JMapMatcher
 
 Copyright (c) 2011 Bernhard Barkow, Hans Skov-Petersen, Bernhard Snizek and Contributors
 
 mail: bikeability@life.ku.dk
 web: http://www.bikeability.dk
 
 This program is free software; you can redistribute it and/or modify it under 
 the terms of the GNU General Public License as published by the Free Software 
 Foundation; either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT 
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with 
 this program; if not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.geotools.feature.SchemaException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.life.sl.graphs.Dijkstra;
 //import org.geotools.util.logging.Logging;
 import org.life.sl.graphs.GraphParams;
 import org.life.sl.graphs.PathSegmentGraph;
 import org.life.sl.orm.HibernateUtil;
 import org.life.sl.orm.Respondent;
 import org.life.sl.orm.ResultMetaData;
 import org.life.sl.orm.ResultRoute;
 import org.life.sl.orm.ResultNodeChoice;
 import org.life.sl.orm.SourceRoute;
 import org.life.sl.readers.shapefile.PointFileReader;
 import org.life.sl.routefinder.PathSizeSet;
 import org.life.sl.routefinder.RFParams;
 import org.life.sl.routefinder.Label;
 import org.life.sl.routefinder.RouteFinder;
 import org.life.sl.routefinder.MatchStats;
 import org.life.sl.routefinder.RouteFinder.LabelTraversal;
 import org.life.sl.utils.BestNAverageStat;
 import org.life.sl.utils.MathUtil;
 import org.life.sl.utils.Timer;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;
 import com.vividsolutions.jts.planargraph.DirectedEdge;
 import com.vividsolutions.jts.planargraph.Edge;
 import com.vividsolutions.jts.planargraph.Node;
 
 // JVM argument for max. heap size (required for large networks): -Xmx2048m
 
 /**
  * Main controller for the map matching algorithm
  * @author Bernhard Snizek
  * @author Bernhard Barkow
  */
 public class JMapMatcher {
 	public enum gpsLoader {
 		SHAPEFILE,
 		PGSQLDATABASE,
 		BULK_PGSQLDATABASE
 	}
 
 	private static String kCfgFileName = "JMM.cfg";
 	private static String kStatFileName = "JMM_stats.dat";
 	private static String kOutputDir = "results/";
 	public static double kCoordEps = 1.e-3;		///< tolerance for coordinate comparison (if (x1-x2 < kCoordEps) then x1==x2)
 
 	// input data:
 	private static gpsLoader kGPSLoader    = gpsLoader.BULK_PGSQLDATABASE;
 	private static gpsLoader kGraphLoader  = gpsLoader.PGSQLDATABASE;
 	private static boolean kUseReducedNetwork = true;	///< true: restrict network to an area enveloping the track
 	
 	private static String kGraphDataFileName = "tmp/04577_network.shp";
 	private static String kGPSPointFileName = "testdata/GPS_Points.shp";
 	private static int kGPSTrackID = 12158;		///< database ID of GPS track to match	
 
 	private PathSegmentGraph graph = null;	///< data basis (graph)
 	private GPSTrack gpsPoints;		///< the path to match (GPS points)
 	private RFParams rfParams = null;		///< parameters for the route finding algorithm
 	private static JMMConfig cfg = new JMMConfig(kCfgFileName);	///< parameters for the MapMatching controller
 	
 	private int sourcerouteID = 0;
 	
 	//private com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
 	private static Logger logger = Logger.getLogger("JMapMatcher");
 
 	/**
 	 * Default constructor: initialization with an empty graph (graph must then be be created later on)
 	 */
 	public JMapMatcher() {
 		this.graph = null;
 	}
 
 	/**
 	 * Initialization with an existing graph (right now, we only store the PathSegmentGraph locally)
 	 * @param g the PathSegmentGraph containing the path (if ==null, it can be created later on)
 	 */
 	public JMapMatcher(PathSegmentGraph g) {
 		this.graph = g;
 	}
 	
 	/**
 	 * Loads GPS data (points) from a file
 	 * @param fileName shapefile containing the GPS data points 
 	 * @throws IOException
 	 */
 	public void loadGPSPoints(String fileName) throws IOException {
 		File pointFile = new File(fileName);	// load data
 		PointFileReader pfr = new PointFileReader(pointFile);	// initialize data from file
 		gpsPoints = new GPSTrack(pfr.getPoints());	// the collection of GPS data points
 	}
 
 	/**
 	 * Deletes the currently loaded graph in order to free memory and start matching a new track (by setting it to null)
 	 */
 	public void clearGraph() {
 		this.graph = null;
 	}
 
 	/**
 	 * Loads a PathSegmentGraph containing the network to use (track + buffer)
 	 * @param track the GPS data
 	 * @return
 	 */
 	private PathSegmentGraph loadGraphFromDB(GPSTrack track) {
 		String dumpFile = "";
 		if (cfg.bDumpNetwork) {
 			File dir = new File(cfg.sDumpNetworkDir);
 			if (!dir.exists()) dir.mkdirs();
 			dumpFile = String.format("%s/%05d%s", cfg.sDumpNetworkDir, sourcerouteID, "_network.shp");	// path for network buffer dump
 		}
 		float bs = (float)rfParams.getDouble(RFParams.Type.NetworkBufferSize);
 		return new PathSegmentGraph(track, bs, dumpFile);
 	}
 	
 	/**
 	 * Loads GPS data from a file, then invokes the actual matching
 	 * @throws IOException
 	 * @return status code (0 = OK)
 	 */
 	public MatchStats match(String fileName) throws IOException {
 		loadGPSPoints(fileName);
 		return match();
 	}	
 
 	/**
 	 * Starts the map matching using GPS data from the database
 	 * @param sourceroute_id database ID of the sourcepoints (GPS route)
 	 * @return status code (0 = OK)
 	 */
 	public MatchStats match(int sourceroute_id)  {
 		System.out.println("Matching sourceroute (ID=" + sourceroute_id + ")");
 		gpsPoints = new GPSTrack(sourceroute_id);
 		if (gpsPoints.size() > 0) {	// check if the track contains points
 			sourcerouteID = sourceroute_id;				// store in class variable, for later use
 			return match();								// start the matching process with the loaded track
 		} else {
 			System.err.println("GPS track " + sourceroute_id + " contains no points!");
 			return new MatchStats(sourcerouteID, MatchStats.Status.EMPTYTRACK);
 		}
 	}
 
 	/**
 	 * Controls the map matching algorithm (assumes GPS data and graph data have been loaded before);
 	 * if no graph has been loaded yet, a new graph enveloping the GPS track is read from the database
 	 * @throws IOException
 	 * @return status code (0 = OK)
 	 */
 	public MatchStats match() {
 		Timer timer = new Timer();
 		timer.init();	// initialize timer
 		MatchStats stats;
 
 		rfParams = initConstraints();
 		double t_1_tot = 0;
 		double t_1_totMax = rfParams.getDouble(RFParams.Type.MaxRuntimeTotal);
 		boolean bTimeout = false; 
 		double bs = rfParams.getDouble(RFParams.Type.NetworkBufferSize);
 		double bs2 = rfParams.getDouble(RFParams.Type.NetworkBufferSize2);
 		double bsMax = rfParams.getDouble(RFParams.Type.NetworkBufferSizeMax);
 		double bsConnOK = -1.;
 		Node fromNode = null, toNode = null;
 		boolean useSecondStrategy = (bs2 > 0.);
 		ArrayList<Label> labels0 = new ArrayList<Label>();	// container for the final results
 		boolean repeat = false;
 		int nthRun = 1;
 		double gpsTrackLength = gpsPoints.getTrackLength();
 		do {	// loop: will be repeated if network buffer is resized
 			bs = Math.max(bs, gpsPoints.getMinDist());	// buffer size must be at least the minimum distance?
 			// load the graph, and check if it is connected, i.e. it there exists a connection from O to D at all:
 			boolean hasConnection = false;
 			double bsfNC = rfParams.getDouble(RFParams.Type.NoConnectionResizeNetwork);	// if > 1, buffer resizing is active
 			do {
 				rfParams.setDouble(RFParams.Type.NetworkBufferSize, bs);	// reinit buffer size
 				if (graph == null || repeat) {	// create a new graph enveloping the GPS track
 					graph = loadGraphFromDB(gpsPoints);
 				}
 				stats = null;
 				if (graph == null || graph.getSize_Edges() < 2) {
 					logger.error("Graph has no edges");
 					stats = new MatchStats(sourcerouteID, MatchStats.Status.NETERROR);
 					graph = null;
 				} else {
 					// Split the graph near the start and end point in order to create the start and end node: 
 					graph.splitGraphAtPoint(gpsPoints.getCoordinate(0), 0);
 					graph.splitGraphAtPoint(gpsPoints.getCoordinate(-1), 1);
 					
 					fromNode = graph.findClosestNode(gpsPoints.getCoordinate(0));	// first node (Origin)
 					toNode   = graph.findClosestNode(gpsPoints.getCoordinate(-1));	// last node in GPS route (Destination) 
 					
 					if (fromNode == null || toNode == null) {	// nodes are not in network - error and stop:
 						logger.error("Origin and destination not in network!?");
 						stats = new MatchStats(sourcerouteID, MatchStats.Status.NETERROR);
 					} else if (bsConnOK > 0. && bs > bsConnOK) {	// do the Dijkstra check below only for the smallest network
 						hasConnection = true;	// (if it was ok for bs, it must be ok for a larger buffer size)
 					} else {	// now check for connectedness by computing the shortest path:
 						logger.info("Checking graph for connection O-D");
 						if (bsfNC > 1.) {
 							Dijkstra.init(graph, fromNode);
 							if (Dijkstra.connects(fromNode, toNode)) {
 								hasConnection = true;
 								bsConnOK = bs;	// save this for later (buffer size where the connection check was ok)
 							} else {
 								bs *= bsfNC;
 								logger.warn("Resizing graph: new buffer size = " + bs);
 								stats = new MatchStats(sourcerouteID, MatchStats.Status.NETERROR);
 								if (bs <= bsMax) graph = null;	// important, to have a new one created in the next run
 							}
 						}
 					}
 				}
 			} while (!hasConnection && (bsfNC > 1.) && (bs <= bsMax));
 			if (graph == null) break;	// break the main loop if no graph could be constructed
 			
 			// log coordinates:
 			logger.info("Origin:      " + (fromNode != null ? fromNode.getCoordinate() : "null"));
 			logger.info("Destination: " + (toNode != null ? toNode.getCoordinate() : "null"));
 			double t_0 = timer.getRunTime(true, "++ graph loaded");
 			
 			// initialize the RouteFinder:
 			RouteFinder rf = new RouteFinder(graph, rfParams, labels0);	// perform the actual route finding procedure on the PathSegmentGraph
 			rf.calculateNearest();	// calculate nearest edges to all data points (needed for edges statistics)
 			// Prepare the evaluation (assigning score to labels):
 			EdgeStatistics eStat = new EdgeStatistics(rf, gpsPoints);
 			double t_2 = timer.getRunTime(true, "++ Edge statistics created");
 			
 			// do the actual route finding:
 			ArrayList<Label> labels = rf.findRoutes(fromNode, toNode, gpsTrackLength);	///< list containing all routes that were found (still unsorted!)
 			// rf has been created with labels0 as persistent container, so labels contains all routes found so far (?)
 			// repeat the route finding with swapped O and D, if required (BothDirections):
 			if ((nthRun == 1 && rfParams.getBool(RFParams.Type.BothDirections)) || (nthRun > 1 && rfParams.getBool(RFParams.Type.BothDirections2))) {
 				logger.info("switching direction: D>O (routes so far: " + labels.size() + ")");
 				rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, 2*rfParams.getInt(RFParams.Type.MaximumNumberOfRoutes));
 				labels = rf.findRoutes(toNode, fromNode, gpsTrackLength);	///< list containing all routes that were found (still unsorted!)
 			}
 			stats = rf.getStats();
 			
 			double t_1 = timer.getRunTime(true, "++ Routefinding finished");
 			t_1_tot += t_1;
 			double t_3 = 0;
 			
 			// check if we need to resize the buffer, or start a second run:
 			repeat = false;
 			double bsf = rfParams.getDouble(RFParams.Type.NoLabelsResizeNetwork);	// if > 1, buffer resizing is active
 			int minRoutes = rfParams.getInt(RFParams.Type.ShuffleResetExtraRoutes);	// buffer will be resized if less than this number of routes has been found
 			if ((bsf > 1. && minRoutes > 0 && labels.size() < minRoutes)			// not enough routes found yet 
 					|| (useSecondStrategy && nthRun < 2)) {							// second run is specified
 				repeat = true;
 			}
 			bTimeout = (t_1_totMax > 0 && t_1_tot > t_1_totMax);
 			if (bTimeout) repeat = false;	// if the maximum run time is consumed, break out in any case
 
 			// set sourceroute status: some routes were found after all, so we set status "OK"
 			if (!labels.isEmpty()) stats.srStatus = MatchStats.SourceRouteStatus.OK;
 			
 			if (labels.isEmpty()) {	// no routes at all were found
 				logger.warn("No labels found at all");
 				stats.status = MatchStats.Status.NOROUTES;
 				stats.srStatus = MatchStats.SourceRouteStatus.NOROUTES;
 			}
 
 			if ((!labels.isEmpty() && !repeat) || (labels.isEmpty() && nthRun > 1)) {	// we are finished
 				if (labels.isEmpty()) {	// we have a problem: no routes were found
 					logger.error("[" + sourcerouteID +"] - No labels found");
 				} else {
 					// first check if we have labels stored from a previous run:
 					//if (labels0.size() > 0) labels.addAll(labels0);
 					// if the shortest path is to be added, too, calculate it:
 					if (cfg.bWriteShortestPath) {
 						if (!Dijkstra.isInited(graph)) Dijkstra.init(graph, fromNode);
 						Label shortestPath = Dijkstra.getShortestPathTo_Label(toNode, eStat);
 						shortestPath.setShortest(true);
						if (!labels.contains(shortestPath)) labels.add(shortestPath);
 					}
 					// loop over all result routes, store them together with their score:
 					sortLabels(labels, cfg.sortRoutes);
 				}
 				int nOK = saveData(labels, fromNode, toNode, eStat);
 				
 				t_3 = timer.getRunTime(true, "++ " + nOK + " routes stored");
 				logger.info("++ load graph: " + t_0 + "s");
 				logger.info("++ findRoutes: " + t_1 + "s");
 				logger.info("++ saveRoutes: " + t_3 + "s");
 			} else {	// not yet - no or not enough labels found, or switch to the second (final) run (ShuffleReset)
 				// !!! THIS IS A REALLY UGLY WAY OF DOING THIS, SORRY !!!
 				// log message and status parameters:
 				if (!labels.isEmpty()) {	// no routes at all were found
 					if (repeat && nthRun < 2) { 	// not enough routes were found, so we repeat nevertheless
 						logger.warn("Not enough labels found yet ("+labels.size()+"/"+minRoutes+")");
 					} else {
 						logger.warn("Starting second run ("+rfParams.getString(RFParams.Type.LabelTraversal2)+")");
 					}
 				}
 				
 				if (!bTimeout) {
 					// resize buffer: check if resizing is allowed: (this happens in the first and the second run)
 					if (bsf > 1.) {	
 						bs *= bsf;
 						repeat = (bs <= bsMax);	// we can increase the buffer size
 					}
 					
 					if (useSecondStrategy && (labels.size() >= minRoutes || !repeat)) {	// either minRoutes have been found, or NetworkBufferSizeMax was reached
 						// switch to ShuffleReset strategy:
 						bs = bs2;	// additional run's buffer size - initialize for the second run
 						rfParams.set(RFParams.Type.LabelTraversal, rfParams.getString(RFParams.Type.LabelTraversal2));
 						double rt2 = rfParams.getDouble(RFParams.Type.MaxRuntime2);
 						if (rt2 > 0) rfParams.set(RFParams.Type.MaxRuntime, rt2);
 						rfParams.setInt(RFParams.Type.ShuffleResetExtraRoutes, 0);	// for the next run, we don't need the extra routes, instead we should start with ShuffleReset immediately
 						repeat = true;
 						nthRun = 2;	// we start the second run now
 						logger.info("Starting second run ("+rfParams.getString(RFParams.Type.LabelTraversal2)+")");
 	
 						// save labels for later:
 						labels0.addAll(labels);
 						sortLabels(labels0, JMMConfig.RouteSorting.MATCHSCORE);	// sort them, in order to get the best match
 						if (!labels0.isEmpty()) labels0.get(0).setChoice(true);	// mark the best match (chosen route)
 						else logger.error("No match found!");
 						// optionally, keep only a number of routes from the first run in the result list:
 						int nKeep = rfParams.getInt(RFParams.Type.RoutesUsedFromFirstRun);
 						if (nKeep > 0 && nKeep < labels0.size()) labels0.subList(nKeep, labels0.size()).clear();
 						// adjust length limit for next call of findRoutes:
 						double df = rfParams.getDouble(RFParams.Type.DistanceFactor2);
 						if (df <= 0.) df = rfParams.getDouble(RFParams.Type.DistanceFactor);
 						if (!labels0.isEmpty()) {
 							double trackLength2 = labels0.get(0).getLength();
 							df *= trackLength2 / gpsTrackLength;	// make df refer to the shortest route length, not the euclidean GPS track length
 						}
 						if (df > 0) {
 							rfParams.set(RFParams.Type.DistanceFactor, df);
 							logger.info("New DistanceFactor: " + df);
 						}
 						
 						// for the next run, we have to consider that routes are already saved:
 						int nMaxRoutes = rfParams.getInt(RFParams.Type.MaximumNumberOfRoutes2);
 						LabelTraversal itLabelOrder = LabelTraversal.valueOf(rfParams.getString(RFParams.Type.LabelTraversal2));
 						if (itLabelOrder == LabelTraversal.ShuffleReset || itLabelOrder == LabelTraversal.Shuffle) {
 							nMaxRoutes = cfg.nRoutesToWrite;	// if we randomize anyway, we can limit to the minimum required number of routes
 						} else {
 							if (nMaxRoutes <= 0) nMaxRoutes = rfParams.getInt(RFParams.Type.MaximumNumberOfRoutes);
 						}
 						//nMaxRoutes = nMaxRoutes - labels0.size();	// routes left for second run
 						System.out.println("+++ looking for "+nMaxRoutes+" routes in the second run");
 						if (rfParams.getBool(RFParams.Type.BothDirections2)) nMaxRoutes = (nMaxRoutes + 1) / 2;
 						rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, nMaxRoutes);
 						rfParams.setDouble(RFParams.Type.MaxPSOverlap, rfParams.getDouble(RFParams.Type.MaxPSOverlap2));
 					}
 				}
 
 				if (repeat) {
 					logger.info("Network buffer resized to " + bs + " - repeating task");
 				} else {	// network size limit reached
 					logger.warn("Network buffer size limit reached!");
 				}
 				// if repeat==true, the matching will now be repeated with a bigger network buffer or the second strategy
 			}
 			
 			// update/store sourceroute status:
 			if (kGPSLoader == gpsLoader.PGSQLDATABASE || kGPSLoader == gpsLoader.BULK_PGSQLDATABASE) {
 				SourceRoute sr = SourceRoute.getSourceRoute(sourcerouteID);
 				sr.setStatus((short)stats.srStatus.ordinal());
 				sr.save();
 			}
 
 			logger.info("++ Total time: " + (t_0 + t_1 + t_2 + t_3) + "s");
 			//if (t_1 > 60) stats.status = rf.getStatus();
 			stats.runTime = t_1;
 		} while (repeat);
 		
 		return stats;
 	}
 	
 	/**
 	 * Sorts the list of Labels according to the specified strategy
 	 * @param labels
 	 */
 	private void sortLabels(ArrayList<Label> labels, JMMConfig.RouteSorting sortCrit) {
 		if (sortCrit == JMMConfig.RouteSorting.LENGTH) {
 			Label.UniversalComparator lengthComp = new Label.UniversalComparator(cfg.sortRoutesLMSWeight);
 			Collections.sort(labels, lengthComp);	// sort labels (result routes) by their score: shortest comes first
 		} else {
 			Collections.sort(labels, Collections.reverseOrder());	// sort labels (result routes) by their score in reverse order, so that the best (highest score) comes first
 		}
 	}
 	
 	/**
 	 * Saves the results of the map matching, either to shapefiles (deprecated) or to a database (via Hibernate) 
 	 * @param labels list of result routes (all labels)
 	 * @param fromNode start node of the routes (origin)
 	 * @param toNode end node of the routes (destination)
 	 * @param eStat statistics object, required for writing track metadata
 	 * @return the number of results that have been saved 
 	 */
 	private int saveData(ArrayList<Label> labels, Node fromNode, Node toNode, EdgeStatistics eStat) {
 		String outFileName = "";
 		int respondentID = 0;
 		
 		if (cfg.bWriteToDatabase) {
 			// clear existing results from relevant database tables:
 			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			try {
 				session.beginTransaction();
 				session.createQuery("delete from ResultMetaData where sourcerouteid=" + sourcerouteID).executeUpdate();
 				session.createQuery("delete from ResultRoute where sourcerouteid=" + sourcerouteID).executeUpdate();
 				session.createQuery("delete from ResultNodeChoice where sourcerouteid=" + sourcerouteID).executeUpdate();
 				session.getTransaction().commit();
 			} catch (Exception e) {
 				logger.error("Error while deleting from database tables: maybe the tables have not been created yet? " + e.toString());
 			}
 		
 			Respondent resp = Respondent.getForSourceRouteID(sourcerouteID);
 			respondentID = resp.getId();
 		}
 
 		// write data for matched routes (1 record per matched route):
 		int nNonChoice = 0;
 		int nOK = 0;
 		boolean isFirst = true;
 		
 		// 1. select routes to write out (a part of all found routes)
 		// we want to have:
 		// - the chosen route
 		// - the shortest path (if required)
 		// - the NBest and NWorst according to our selected sort criteria
 		// - the remaining number of routes, selected randomly
 		int nLabels = labels.size();
 		int nRoutes = Math.min(cfg.nRoutesToWrite, nLabels);
 		ArrayList<Label> selRoutes = new ArrayList<Label>(nRoutes); 
 		int j;
 		boolean bAddShortest = cfg.bWriteShortestPath;
 		for (int i = 0; i < nRoutes; i++) {
 			if (i == 0) {
 				for (j = 0; j < nLabels; j++) if (labels.get(j).isChoice()) break;
 				if (j >= nLabels) j = 0;	// just for safety, if no chosen route was marked
 				//logger.info("Adding choice to output selection: " + j);
 			} else if (bAddShortest) {
 				bAddShortest = false;
 				for (j = 0; j < nLabels; j++) if (labels.get(j).isShortest()) break;
 				if (true || j >= nLabels) i--;	// if no shortest route was found, it has been selected before or was not marked
 				// -> continue with element i-1 (i=1) in the list
 				//logger.info("Adding shortest path to output selection: " + j);
 			} else if (i < cfg.iWriteNBest || i >= nRoutes - cfg.iWriteNWorst || nRoutes == nLabels) {	// write those without randomization
 				j = i;
 				while (j < nLabels && selRoutes.contains(labels.get(j))) j++;
 			} else {	// select random route
 				do {
 					j = Math.min((int)(Math.random() * nLabels + .5), nLabels - 1);
 				} while (selRoutes.contains(labels.get(j)));
 			}
 			if (j < nLabels) selRoutes.add(labels.get(j));
 		}
 		
 		// 2. prepare labels before output: compute Path Size Attribute
 		@SuppressWarnings("unused")
 		PathSizeSet myPSASet = new PathSizeSet(selRoutes);	// (the constructor also invokes the computation)
 
 		// 3. write out the selected routes
 		for (Label curLabel : selRoutes) {
 			
 			if (cfg.bWriteToDatabase) {
 				if (writeLabelToDatabase(curLabel, isFirst, respondentID, fromNode, toNode, eStat, selRoutes.size())) {
 					nOK++;
 				} else {
 					logger.error("ERROR storing route!!");
 				}
 			}
 			
 			if (cfg.bWriteToShapefiles) {
 				try {
 					String s = (sourcerouteID != 0 ? sourcerouteID + "_" : "");
 					if (isFirst) {	// the first route is the "choice" (best score) ...
 						isFirst = false;
 						outFileName = kOutputDir + s + "Best.shp";
 					} else {	// ... the other routes are "non-choices"+
 						outFileName = String.format("%s%03d%s", kOutputDir + s + "NonChoice", nNonChoice, ".shp");
 						nNonChoice++;
 					}
 					curLabel.dumpToShapeFile(outFileName);	// write result route to file
 				} catch (SchemaException e1) {
 					System.err.println("Error writing file " + outFileName + " (SchemaException)");
 				} catch (IOException e2) {
 					System.err.println("Error writing file " + outFileName + " (IOException)");
 				}
 			}
 			
 			isFirst = false;	// remaining routes are non-choices
 		}
 
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		// write metadata (1 record per matched GPS track):
 		ResultMetaData metaData = new ResultMetaData(sourcerouteID, respondentID);
 
 		metaData.setnAlternatives(labels.size());
 		metaData.setMaxDistanceFactor((float)rfParams.getDouble(RFParams.Type.DistanceFactor));
 		metaData.setAvgDistPt((float)gpsPoints.getAvgDist());
 		metaData.setMaxDistPt((float)gpsPoints.getMaxDist());
 		metaData.setMinDistPt((float)gpsPoints.getMinDist());
 		metaData.setnPoints(gpsPoints.size());
 		metaData.setTrackLength((float)gpsPoints.getTrackLength());
 		metaData.setDistPEavg((float)eStat.getDistPEAvg());
 		metaData.setDistPEavg05((float)eStat.getDistPE05());
 		metaData.setDistPEavg50((float)eStat.getDistPE50());
 		metaData.setDistPEavg95((float)eStat.getDistPE95());
 		// add scores for the best routes:
 		int[] nBest = { 1, 5, 10, 25, 50, 100 };
 		BestNAverageStat matchScoreAvgs = new BestNAverageStat(nBest);
 		BestNAverageStat matchLengthAvgs = new BestNAverageStat(nBest);
 		BestNAverageStat noMatchEdgeAvgs = new BestNAverageStat(nBest);
 		for (int i=0; i < Math.min(100, nLabels); i++) {
 			//matchScoreAvgs.add(labels.get(i).getScore());
 			// create a route, but only for the statistics, not for saving to the database:
 			ResultRoute route = new ResultRoute(sourcerouteID, respondentID, i==0, labels.get(i), gpsPoints, false);
 			matchScoreAvgs.add(route.getMatchScore());
 			matchLengthAvgs.add(route.getMatchLengthR());
 			noMatchEdgeAvgs.add((double)(route.getnEdgesWOPts()));
 		}
 		metaData.setScoreAvgs(matchScoreAvgs.getAverages());
 		metaData.setMatchLengthAvgs(matchLengthAvgs.getAverages());
 		metaData.setNoMatchEdgeAvgs(noMatchEdgeAvgs.getAverages());
 		
 		
 		session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.save(metaData);
 		session.getTransaction().commit();
 
 		return nOK;
 	}
 	
 	/**
 	 * Writes the data of one label to the database (route data, node choice data)
 	 * @param label
 	 * @param isChoice true if the label is the selected one
 	 * @param respondentID ID of the respondent
 	 * @param fromNode origin O
 	 * @param toNode destination D
 	 * @return true if no error occurred during writing the data
 	 */
 	private boolean writeLabelToDatabase(Label label, boolean isChoice, int respondentID, Node fromNode, Node toNode, EdgeStatistics eStat, long nAlternatives) {
 		boolean ok = false;
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		// set up entry for route table:
 		ResultRoute route = new ResultRoute(sourcerouteID, respondentID, isChoice, label, gpsPoints, cfg.bWriteTrafficLights);
 		route.setnAlternatives(nAlternatives);
 		// set remaining route parameters:
 		// get node list to create the lineString representing the route:
 		//Coordinate[] coordinates = label.getCoordinates();
 		//ok = (coordinates.length > 0);	// true if there were any points in the route
 		LineString lineString = label.getLineString();
 		ok = (lineString.getLength() > 0);
 		if (ok) try {
 			//LineString lineString = fact.createLineString(coordinates);
 			route.setGeometry(lineString);
 			
 			session.save(route);
 
 			// create output for the choice experiment:
 			if (cfg.bWriteChoices && isChoice) {	// (this is required only for the chosen route)
 				ResultNodeChoice choice = new ResultNodeChoice(route.getId(), sourcerouteID, respondentID);
 				
 				double dist = 0.;
 				int i = 0;	// counter
 				float[] edgeLengths = route.getEdgeLengths();
 				DirectedEdge lastEdge = null;
 				int[] nodeIDs = label.getNodeIDs();
 				List<DirectedEdge> edges = label.getRouteAsEdges();
 				for (DirectedEdge e : edges) {		// for each node along the route:
 					Node node = e.getFromNode();	// node at beginning of edge
 					Coordinate c_n = node.getCoordinate();
 
 					choice.setNodeID(nodeIDs[i]);
 					choice.setI(i);
 					choice.setDist((float)(dist / label.getLength()));	// distance along the route as fraction of the whole route
 					
 					// angle from node to destination:
 					Coordinate c_d = toNode.getCoordinate();
 					double angle_direct = Math.atan2(c_d.y - c_n.y, c_d.x - c_n.x);
 
 					@SuppressWarnings("unchecked")
 					List<DirectedEdge> outEdges = node.getOutEdges().getEdges();
 					if (lastEdge != null) outEdges.remove(lastEdge.getSym());
 					outEdges.remove(e);
 					Collections.shuffle(outEdges);	// use a random selection: the first noe are selected, the remaining ones are dropped
 					outEdges.add(0, e);				// put the chosen edge at the first position
 					int nOE = outEdges.size();
 					if (cfg.iFixedNodeChoices > 0) {
 						if (nOE < cfg.iFixedNodeChoices) {	// if there are not enough edges, fill up with null:
 							for (int j = nOE; j < cfg.iFixedNodeChoices; j++) outEdges.add(null);
 						} else if (nOE > cfg.iFixedNodeChoices) {	// too many: remove:
 							for (int j = nOE-1; j >= cfg.iFixedNodeChoices; j--) outEdges.remove(j);
 						}
 					}	// now, we have exactly cfg.iFixedChoices edges in the list
 					for (DirectedEdge oe : outEdges) {
 						if (lastEdge==null || (lastEdge != null && oe != lastEdge.getSym())) {	// don't use the backEdge (lastEdge.getSym())!
 							if (oe != null) {
 								Edge oee = oe.getEdge();
 								@SuppressWarnings("unchecked")
 								HashMap<String, Object> ed2 = (HashMap<String, Object>) oee.getData();
 								choice.setSelected(oe == e);
 								choice.setEdgeID((Integer)ed2.get("id"));
 								choice.setEnvType((Short)ed2.get("et"));
 								choice.setCykType((Short)ed2.get("ct"));
 								choice.setGroenM(((Double)ed2.get("gm")).floatValue());
 								double l = ((LineMergeEdge)oee).getLine().getLength();
 								choice.setGroenPct((float)((Double)ed2.get("gm") / l));
 								choice.setAngleToDest((float)(MathUtil.mapAngle_degrees(Math.toDegrees(oe.getAngle() - angle_direct))));	// store value in degrees
 								choice.setAngle(lastEdge != null ? (float)MathUtil.mapAngle_degrees(Math.toDegrees(oe.getAngle() - lastEdge.getAngle())) : 0);	// angle between edges at node
 								choice.setnPts(eStat.getCount(oee));
 								choice.setnChoices((short)nOE);
 							} else {	// oe==null: empty pseudo-edge
 								choice.setSelected(false);
 								choice.setEdgeID(0);
 								choice.setEnvType((short)0);
 								choice.setCykType((short)0);
 								choice.setGroenM(0f);
 								choice.setGroenPct(0f);
 								choice.setAngleToDest(0f);
 								choice.setAngle(0);
 								choice.setnPts((short)0);
 								choice.setnChoices((short)0);
 							}
 							session.save(choice.clone());	// save 1 choice/nonchoice for each outEdge!
 						}
 					}
 					dist += edgeLengths[i];
 					lastEdge = e;	// save for next comparison
 					i++;
 				}
 				
 			}
 			
 			session.getTransaction().commit();
 		} catch (Exception e) {
 			ok = false;
 			System.out.println(e);
 		}
 
 		return ok;
 	}
 
 	/**
 	 * Setup of default configuration parameters for the routefinding algorithm
 	 * @return
 	 */
 	private RFParams initConstraints() {
 		// initialize constraint fields:
 		rfParams = new RFParams();
 		rfParams.initDefaults();	// initialize with hardwired default values
 		
 		// read config file, eventually overwriting existing values:
 		int r = rfParams.readFromFile(kCfgFileName);
 		if (r < 0) logger.warn("Config file " + kCfgFileName + " not found!");
 		else logger.info("Read " + r + " values from config file " + kCfgFileName);
 		
 		return rfParams;
 	}
 
 	/**
 	 * main method: loads the data and invokes the matching algorithm
 	 * @param args command line arguments: 1: route ID; 2: route ID (end of range)
 	 * @throws IOException 
 	 */
 	public static void main(String... args) throws IOException {
 		//Logger logger = Logger.getRootLogger();
 		//BasicConfigurator.configure();	// set up logging
 		
 		/*if (args.length == 0) {	// command line arguments have the highest priority; consider cfg.sourcerouteIDs only if there are none
 			if (cfg.sourcerouteIDs.contains("-")) {	
 				String[] fromTo = cfg.sourcerouteIDs.trim().split("-");
 				query += " WHERE id>="+fromTo[0]+" AND id<="+fromTo[1];
 			} else {
 				if (args.length == 0 && !cfg.sourcerouteIDs.trim().isEmpty()) query += " WHERE id IN ("+cfg.sourcerouteIDs+")";
 			}
 		}
 		else if (args.length == 1) query += " WHERE id="+args[0];
 		else if (args.length == 2) query += " WHERE id>="+args[0]+" AND id<="+args[1];
 
 		cfg = new JMMConfig(cfgName);*/
 		
 		logger.setLevel(cfg.logLevel);
 		Logger.getRootLogger().setLevel(cfg.logLevel);
 		//logger.setLevel(Level.INFO);
 		GraphParams.getInstance().setSnap(cfg.graphSnapDistance > 0.);
 		GraphParams.getInstance().setSnapDistance(cfg.graphSnapDistance);
 		if (cfg.graphSnapDistance > 0.) kCoordEps = cfg.graphSnapDistance;
 		
 		PathSegmentGraph g = null;
 		// Let us load the graph ...
 		if (kGraphLoader == gpsLoader.PGSQLDATABASE) {
 			if (!kUseReducedNetwork) g = new PathSegmentGraph(1);	//... or delay the loading if kUseReducedNetwork==true
 		} else if (kGraphLoader == gpsLoader.SHAPEFILE) {
 			g = new PathSegmentGraph(kGraphDataFileName);	// get network from a file
 		}
 		// ... and invoke the matching algorithm:
 		if (kGPSLoader == gpsLoader.PGSQLDATABASE) {
 			new JMapMatcher(g).match(kGPSTrackID);			// get track from database; NOTE: g may be null here
 		} else if (kGPSLoader == gpsLoader.SHAPEFILE) {
 			new JMapMatcher(g).match(kGPSPointFileName);	// get track data from a file
 		} else if (kGPSLoader == gpsLoader.BULK_PGSQLDATABASE) {	// ... a sweet bulk loader
 			// 1. get a list over sourceroute ids
 			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			session.beginTransaction();
 
 			Query result;
 			String query = "from SourceRoute";
 			
 			System.out.println(cfg.sourcerouteIDs);
 			if (args.length == 0) {	// command line arguments have the highest priority; consider cfg.sourcerouteIDs only if there are none
 				if (cfg.sourcerouteIDs.contains("-")) {	
 					String[] fromTo = cfg.sourcerouteIDs.trim().split("-");
 					query += " WHERE id>="+fromTo[0]+" AND id<="+fromTo[1];
 				} else {
 					if (args.length == 0 && !cfg.sourcerouteIDs.trim().isEmpty()) query += " WHERE id IN ("+cfg.sourcerouteIDs+")";
 				}
 			}
 			else if (args.length == 1) query += " WHERE id="+args[0];
 			else if (args.length == 2) query += " WHERE id>="+args[0]+" AND id<="+args[1];
 
 			query += " ORDER BY id";
 			result = session.createQuery(query);
 			@SuppressWarnings("unchecked")
 			Iterator<SourceRoute> iterator = result.iterate();
 			logger.info(result.list().size() + " tracks to be matched");
 			ArrayList<Integer> sRoutes= new ArrayList<Integer>();
 			while (iterator.hasNext()) {
 				SourceRoute sR = iterator.next();
 				sRoutes.add(sR.getId());
 				//
 			}
 			session.getTransaction().commit();
 			
 			JMapMatcher jmm = new JMapMatcher(null);
 			HashMap<Integer, Integer> stat = new HashMap<Integer,Integer>(sRoutes.size());
 			for (int i=0; i<sRoutes.size(); i++) {
 				Integer route = sRoutes.get(i);
 				String sCount = "(" + (i+1) + "/" + sRoutes.size() + ")";
 				logger.info("--- Matching track " + route + "... " + sCount);
 				jmm.clearGraph();	// clear the graph, so that a new one enveloping the current track is loaded
 				MatchStats stats = jmm.match(route);
 				stats.save(kOutputDir+kStatFileName, i==0);
 				
 				logger.info("--- Track " + route + " matched " + sCount);
 			}
 			for (Integer r : stat.keySet()) System.out.println(r + "\t" + stat.get(r));
 		}
 	}
 }
