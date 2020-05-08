 package org.mccaughey.pathGenerator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.geotools.data.DataUtilities;
 import org.geotools.data.Query;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.data.store.ReprojectingFeatureCollection;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geojson.feature.FeatureJSON;
 import org.geotools.graph.build.line.LineStringGraphGenerator;
 import org.geotools.graph.path.AStarShortestPathFinder;
 import org.geotools.graph.path.Path;
 import org.geotools.graph.structure.Edge;
 import org.geotools.graph.structure.Graph;
 import org.geotools.graph.structure.Node;
 import org.geotools.graph.traverse.standard.AStarIterator.AStarFunctions;
 import org.geotools.graph.traverse.standard.AStarIterator.AStarNode;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.mccaughey.pathGenerator.config.LayerMapping;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vividsolutions.jts.densify.Densifier;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import com.vividsolutions.jts.geom.TopologyException;
 import com.vividsolutions.jts.index.SpatialIndex;
 import com.vividsolutions.jts.index.strtree.STRtree;
 import com.vividsolutions.jts.linearref.LengthIndexedLine;
 import com.vividsolutions.jts.linearref.LinearLocation;
 import com.vividsolutions.jts.linearref.LocationIndexedLine;
 
 public class PathGenerator {
 
 	private static final double GEOMETRY_PRECISION = 100;
 	static final Logger LOGGER = LoggerFactory.getLogger(PathGenerator.class);
 	private static final Double MAX_SNAP_DISTANCE = 5000.0;
 	private static PrecisionModel precision = new PrecisionModel(
 			GEOMETRY_PRECISION);
 
 	private PathGenerator() {
 	}
 
 	public static List<Path> shortestPaths(SimpleFeatureSource networkSource,
 			Point start, List<Point> destinations) throws IOException,
 			NoSuchAuthorityCodeException, FactoryException {
 
//		Query query = new Query(LayerMapping.ROAD_SAMPLE_LAYER);
//		query.setCoordinateSystem(CRS.decode("EPSG:28355")); // FROM
 
 		SimpleFeatureCollection networkSimpleFeatureCollection = networkSource
				.getFeatures();
 		List<LineString> lines = nodeIntersections(networkSimpleFeatureCollection);
 
 		LOGGER.info("FEATURES: {}", networkSimpleFeatureCollection.size());
 		// Build a graph with all the destinations connected
 		LineStringGraphGenerator lineStringGen = createGraphWithAdditionalNodes(
 				lines, start, destinations);
 		Graph graph = lineStringGen.getGraph();
 		LOGGER.info("GRAPH: " + graph);
 
 		Node startNode = lineStringGen.getNode(start.getCoordinate());
 
 		List<Path> paths = new ArrayList<Path>();
 		for (Point end : destinations) {
			//LOGGER.info("calculating path for this point ...");	
 			try {
 				Node endNode = lineStringGen.getNode(end.getCoordinate());
 				if (endNode != null) {
 					LOGGER.info("Start Node: " + startNode.toString());
 					LOGGER.info("End Node: " + endNode.toString());
 
 					Path shortest = findAStarShortestPath(graph, startNode,
 							endNode);
 					paths.add(shortest);
 
 				}
 			} catch (Exception e) {
 				LOGGER.error("Something bad happened, ignoring"
 						+ e.getMessage());
 			}
 		}
 		return paths;
 	}
 
 	private static LineStringGraphGenerator createGraphWithAdditionalNodes(
 			List<LineString> lines, Point startingPoint,
 			List<Point> destinations) throws IOException {
 
 		LocationIndexedLine startConnectedLine = findNearestEdgeLine(lines,
 				MAX_SNAP_DISTANCE, startingPoint);
 
 		if (startConnectedLine != null) {
 			addConnectingLine(startingPoint, lines, startConnectedLine);
 		}
 
 		for (Point endPoint : destinations) {
 			LocationIndexedLine endConnectedLine = findNearestEdgeLine(lines,
 					MAX_SNAP_DISTANCE, endPoint);
 			if (endConnectedLine != null) {
 				addConnectingLine(endPoint, lines, endConnectedLine);
 			}
 		}
 		nodeIntersections(lines);
 
 		// create a linear graph generator
 		LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();
 		for (LineString line : lines) {
 			lineStringGen.add(line);
 		}
 
 		return lineStringGen;
 
 	}
 
 	private static List<LineString> addConnectingLine(Point newPoint,
 			List<LineString> lines, LocationIndexedLine connectedLine) {
 		Coordinate pt = newPoint.getCoordinate();
 		LinearLocation here = connectedLine.project(pt);
 		Coordinate minDistPoint = connectedLine.extractPoint(here);
 		LineString lineA = (LineString) connectedLine.extractLine(
 				connectedLine.getStartIndex(),
 				connectedLine.project(minDistPoint));
 		LineString lineB = (LineString) connectedLine.extractLine(
 				connectedLine.project(minDistPoint),
 				connectedLine.getEndIndex());
 		LineString originalLine = (LineString) connectedLine.extractLine(
 				connectedLine.getStartIndex(), connectedLine.getEndIndex());
 
 		GeometryFactory geometryFactory = new GeometryFactory(precision);
 		LineString newConnectingLine = geometryFactory
 				.createLineString(new Coordinate[] { pt, minDistPoint });
 
 		lines.add(newConnectingLine);
 		if (!((lineB.getLength() == 0.0) || (lineA.getLength() == 0.0))) {
 			removeLine(lines, originalLine);
 
 			lineA = geometryFactory.createLineString(lineA.getCoordinates());
 			lineB = geometryFactory.createLineString(lineB.getCoordinates());
 			lines.add(lineA);
 			lines.add(lineB);
 		}
 		return lines;
 	}
 
 	private static LocationIndexedLine findNearestEdgeLine(
 			List<LineString> lines, Double maxDistance, Point pointOfInterest)
 			throws IOException {
 		// Build network Graph - within bounds
 		SpatialIndex index = createLineStringIndex(lines);
 
 		Coordinate pt = pointOfInterest.getCoordinate();
 		Envelope search = new Envelope(pt);
 		search.expandBy(maxDistance);
 
 		/*
 		 * Query the spatial index for objects within the search envelope. Note
 		 * that this just compares the point envelope to the line envelopes so
 		 * it is possible that the point is actually more distant than
 		 * MAX_SEARCH_DISTANCE from a line.
 		 */
 		List<LocationIndexedLine> linesIndexed = index.query(search);
 
 		// Initialize the minimum distance found to our maximum acceptable
 		// distance plus a little bit
 		double minDist = maxDistance;
 		Coordinate minDistPoint = null;
 		LocationIndexedLine connectedLine = null;
 
 		for (LocationIndexedLine line : linesIndexed) {
 
 			LinearLocation here = line.project(pt);
 			Coordinate point = line.extractPoint(here);
 			double dist = point.distance(pt);
 			if (dist <= minDist) {
 				minDist = dist;
 				minDistPoint = point;
 				connectedLine = line;
 			}
 		}
 
 		if (minDistPoint != null) {
 			LOGGER.debug("{} - snapped by moving {}\n", pt.toString(), minDist);
 			return connectedLine;
 		}
 		return null;
 	}
 
 	private static void removeLine(List<LineString> lines, Geometry originalLine) {
 
 		for (LineString line : lines) {
 			if ((line.equals(originalLine))) {
 				lines.remove(line);
 				return;
 			}
 		}
 	}
 
 	public static SimpleFeatureCollection writePathNodes(List<Path> paths,
 			CoordinateReferenceSystem crs, File file)
 			throws GeneratedOutputEmptyException {
 		SimpleFeatureType featureType = createPathFeatureType(crs);
 		GeometryFactory geometryFactory = new GeometryFactory(precision);
 		List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
 		int pathID = 0;
 		for (Path path : paths) {
 			pathID++;
 			Node currentNode = path.getFirst();
 
 			long unixTime = 0; // The epoch
 
 			List<String> unvisited = new ArrayList<String>();
 			for (Edge edge : (List<Edge>) path.getEdges()) {
 				unvisited.add(String.valueOf(edge.getID()));
 			}
 			while (unvisited.size() > 0) {
 				if (currentNode.getEdges().size() > 0) {
 					featuresList = processEdges(unvisited, currentNode,
 							geometryFactory, featureType, unixTime, pathID,
 							featuresList);
 				}
 			}
 		}
 		// :todo when featuresList is empty ,
 		// ReprojectingFeatureCollection rfc instantiating generates
 		// nullpointerexception ,
 		// due to calling getSchema().getGeometryDescriptor() method on
 		// pathfeatures
 		SimpleFeatureCollection pathFeatures = DataUtilities
 				.collection(featuresList);
 		CoordinateReferenceSystem crs_target = DefaultGeographicCRS.WGS84;
 		LOGGER.info(crs.toWKT());
 
 		boolean outputisNotEmpty = pathFeatures.size() > 0;
 		if (outputisNotEmpty) {
 			ReprojectingFeatureCollection rfc = new ReprojectingFeatureCollection(
 					pathFeatures, crs_target);
 
 			writeFeatures(rfc, file);
 			LOGGER.info("GeoJSON writing complete");
 			return rfc;
 		} else {
 			LOGGER.info("Generated output is empty");
 			throw new GeneratedOutputEmptyException();
 
 		}
 	}
 
 	private static List<SimpleFeature> processEdges(List<String> unvisited,
 			Node currentNode, GeometryFactory geometryFactory,
 			SimpleFeatureType featureType, long unixTime, int pathID,
 			List<SimpleFeature> featuresList) {
 		
 		LineString line;
 		for (Edge edge : (List<Edge>) currentNode.getEdges()) {
 
 			if (unvisited.contains(String.valueOf(edge.getID()))) {
 				line = (LineString) edge.getObject();
 
 				Coordinate pt = ((Point) currentNode.getObject())
 						.getCoordinate();
 
 				LengthIndexedLine lil = new LengthIndexedLine(line);
 
 				if (lil.project(pt) == lil.getStartIndex()) {
 					// start coordinate is at start of line
 					for (int index = 0; index < lil.getEndIndex(); index += 25) {
 						Coordinate coordinate = lil.extractPoint(index);
 						Point point = geometryFactory.createPoint(coordinate);
 						SimpleFeature feature = buildTimeFeatureFromGeometry(
 								featureType, point, unixTime,
 								String.valueOf(pathID));
 						unixTime += 9000;
 						featuresList.add(feature);
 					}
 				} else if (lil.project(pt) == lil.getEndIndex()) {
 					// start coordinate is at the end of the line
 					for (int index = (int) lil.getEndIndex(); index >= 0; index -= 25) {
 						Coordinate coordinate = lil.extractPoint(index);
 						Point point = geometryFactory.createPoint(coordinate);
 						SimpleFeature feature = buildTimeFeatureFromGeometry(
 								featureType, point, unixTime,
 								String.valueOf(pathID));
 						unixTime += 9000;
 						featuresList.add(feature);
 					}
 				} else {
 					LOGGER.error("Start coordinate did not match with Index!");
 				}
 				unvisited.remove(String.valueOf(edge.getID()));
 
 				Node nextNode = edge.getOtherNode(currentNode);
 				if (nextNode != null)
 					currentNode = nextNode;
 				else {
 					break;
 				}
 
 			}
 		}
 		return featuresList;
 	}
 
 	private static SimpleFeature buildTimeFeatureFromGeometry(
 			SimpleFeatureType featureType, Geometry geom, long unix_time,
 			String path_id) {
 		SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
 		stb.init(featureType);
 		SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(featureType);
 		sfb.add(geom);
 		sfb.add(unix_time);
 		sfb.add(path_id);
 		return sfb.buildFeature(null);
 	}
 
 	private static SimpleFeatureType createPathFeatureType(
 			CoordinateReferenceSystem crs) {
 
 		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
 		builder.setName("Buffer");
 		if (crs != null) {
 			builder.setCRS(crs); // <- Coordinate reference system
 		}
 
 		// add attributes in order
 		builder.add("geometry", Point.class);
 		builder.add("when", Integer.class);
 		builder.add("path_id", String.class);
 
 		// build the type
 		SimpleFeatureType pathType = builder.buildFeatureType();
 
 		return pathType;
 	}
 
 	private static void writeFeatures(SimpleFeatureCollection features,
 			File file) {
 		FeatureJSON fjson = new FeatureJSON();
 		OutputStream os;
 		try {
 			os = new FileOutputStream(file);
 			try {
 				if (features.getSchema().getCoordinateReferenceSystem() != null) {
 					LOGGER.info("Encoding CRS?");
 					fjson.setEncodeFeatureCollectionBounds(true);
 					fjson.setEncodeFeatureCollectionCRS(true);
 				} else {
 					LOGGER.info("CRS is null");
 				}
 				fjson.writeFeatureCollection(features, os);
 			} finally {
 				os.close();
 			}
 		} catch (FileNotFoundException e1) {
 			LOGGER.error("Failed to write feature collection "
 					+ e1.getMessage());
 		} catch (IOException e) {
 			LOGGER.error("Failed to write feature collection " + e.getMessage());
 		}
 	}
 
 	private static SpatialIndex createLineStringIndex(List<LineString> lines)
 			throws IOException {
 		SpatialIndex index = new STRtree();
 
 		// Create line string index
 		// Just in case: check for null or empty geometry
 		for (LineString line : lines) {
 			if (line != null) {
 				Envelope env = line.getEnvelopeInternal();
 				if (!env.isNull()) {
 					index.insert(env, new LocationIndexedLine(line));
 				}
 			}
 		}
 
 		return index;
 	}
 
 	private static List<LineString> nodeIntersections(
 			SimpleFeatureCollection nonNodedNetwork) {
 		SimpleFeatureIterator networkIterator = nonNodedNetwork.features();
 
 		List<LineString> lines = new ArrayList<LineString>();
 
 		while (networkIterator.hasNext()) {
 			SimpleFeature edge = networkIterator.next();
 			Geometry line = (Geometry) edge.getDefaultGeometry();
 			for (int i = 0; i < line.getNumGeometries(); i++) {
 				lines.add((LineString) line.getGeometryN(i));
 			}
 		}
 
 		return nodeIntersections(lines);
 
 	}
 
 	private static List<LineString> nodeIntersections(List<LineString> rawLines) {
 		List<LineString> lines = new ArrayList<LineString>();
 
 		for (LineString line : rawLines) {
 			for (int i = 0; i < line.getNumGeometries(); i++) {
 				lines.add((LineString) line.getGeometryN(i));
 			}
 		}
 
 		GeometryFactory geomFactory = new GeometryFactory(precision);
 		Geometry grandMls = geomFactory.buildGeometry(lines);
 		Point mlsPt = geomFactory.createPoint(grandMls.getCoordinate());
 		try {
 			Geometry nodedLines = grandMls.union(mlsPt);
 
 			lines.clear();
 
 			for (int i = 0, n = nodedLines.getNumGeometries(); i < n; i++) {
 				Geometry g = nodedLines.getGeometryN(i);
 				if (g instanceof LineString) {
 					g = (LineString) Densifier.densify(g, 5.0);
 					lines.add((LineString) g);
 				}
 			}
 		} catch (TopologyException te) {
 			LOGGER.info("Failed to node non-noded intersection, ugh");
 		}
 		return lines;
 	}
 
 	private static Path findAStarShortestPath(Graph graph, Node start,
 			Node destination) throws Exception { // WrongPathException { <---
 													// FIXME: WrongPathException
 													// is not visible
 		// create a cost function and heuristic for A-Star
 		// in this case we are using geometry length
 		AStarFunctions asf = new AStarFunctions(destination) {
 
 			@Override
 			public double cost(AStarNode n1, AStarNode n2) {
 				Coordinate coordinate1 = ((Point) n1.getNode().getObject())
 						.getCoordinate();
 				Coordinate coordinate2 = ((Point) n2.getNode().getObject())
 						.getCoordinate();
 				return coordinate1.distance(coordinate2);
 			}
 
 			@Override
 			public double h(Node n) {
 				Coordinate coordinate1 = ((Point) n.getObject())
 						.getCoordinate();
 				Coordinate coordinate2 = ((Point) this.getDest().getObject())
 						.getCoordinate();
 				return coordinate1.distance(coordinate2);
 			}
 		};
 
 		AStarShortestPathFinder pf = new AStarShortestPathFinder(graph,
 				destination, start, asf);
 		pf.calculate();
 		pf.finish();
 		return pf.getPath();
 	}
 }
