 package de.uni_koblenz.jgstreetmap.model.kdtree;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import de.uni_koblenz.jgstreetmap.model.AnnotatedOsmGraph;
 import de.uni_koblenz.jgstreetmap.model.AnnotatedOsmGraph.Neighbour;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.HasElement;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.HasXChild;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.HasYChild;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.Key;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.NodeSet;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.XKey;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.YKey;
 import de.uni_koblenz.jgstreetmap.osmschema.map.Node;
 import de.uni_koblenz.jgstreetmap.routing.GpsTools;
 import de.uni_koblenz.jgstreetmap.routing.Segmentator;
 
 public class KDTreeQueries {
 
 	public static List<Neighbour> neighboursKD(AnnotatedOsmGraph g, double lat,
 			double lon, double maxrange) {
 		if (!g.hasKDTree()) {
 			return null;
 		}
 		double height = maxrange / (GpsTools.MINUTEMETER * 60);
 		double width = maxrange
 				/ (GpsTools.MINUTEMETER * 60 * Math.cos(Math.toRadians(lat)));
 		double tlon = lon - width;
 		double tlat = lat - height;
 		double blon = lon + width;
 		double blat = lat + height;
 		List<Node> l = new ArrayList<Node>();
 		rangeQuery(g, l, tlon, tlat, blon, blat, (Key) g.getKDTree()
 				.getFirstHasRootIncidence().getThat());
 		List<Neighbour> result = new ArrayList<Neighbour>(l.size());
 		for (Node n : l) {
 			result.add(new Neighbour(n, Segmentator.distance(lat, lon, n)));
 		}
 		Collections.sort(result, Neighbour.getComparator());
 		return result;
 	}
 
 	public static void rangeQuery(AnnotatedOsmGraph g, List<Node> l,
 			double topLeftLong, double topLeftLat, double bottomRightLong,
 			double bottomRightLat, Key key) {
 		// System.out.println("rangeQuery");
 		// System.out.println("\ttopLeftLat = "
 		// + MapPanel.formatLatitude(topLeftLat) + " " + topLeftLat);
 		// System.out.println("\ttopLeftLon = "
 		// + MapPanel.formatLongitude(topLeftLong) + " " + topLeftLong);
 		// System.out.println("\tbottomRightLat= "
 		// + MapPanel.formatLatitude(bottomRightLat) + " "
 		// + bottomRightLat);
 		// System.out.println("\tbottomRightLon= "
 		// + MapPanel.formatLongitude(bottomRightLong) + " "
 		// + bottomRightLong);
 		if (key instanceof XKey) {
 			rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 					bottomRightLat, (XKey) key);
 		} else if (key instanceof YKey) {
 			rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 					bottomRightLat, (YKey) key);
 		} else {
 			throw new RuntimeException("Unexpected KD root type "
					+ key.getM1Class());
 		}
 	}
 
 	public static void rangeQuery(AnnotatedOsmGraph g, List<Node> l,
 			double topLeftLong, double topLeftLat, double bottomRightLong,
 			double bottomRightLat, YKey key) {
 		double keyVal = key.get_keyValue();
 
 		// System.out.println("Query Y " + keyVal);
 
 		HasXChild leftChild = key.getFirstHasXChildIncidence();
 		if (leftChild == null) {
 			// the only child of key is a NodeSet
 			rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 					bottomRightLat, (NodeSet) key.getFirstHasSetIncidence()
 							.getThat());
 		} else {
 			HasXChild rightChild = leftChild.getNextHasXChildIncidence();
 			assert rightChild != null;
 			if (keyVal < topLeftLat) {
 				// only the right subtree has to be examined further
 				// System.out.print("R ");
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (XKey) rightChild.getThat());
 			} else if (keyVal >= bottomRightLat) {
 				// System.out.print("L ");
 				// only the left subtree has to be examined further
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (XKey) leftChild.getThat());
 			} else {
 				// the right and the left subtree have to be examined
 				// System.out.print("L ");
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (XKey) leftChild.getThat());
 				// System.out.print("R ");
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (XKey) rightChild.getThat());
 			}
 		}
 	}
 
 	public static void rangeQuery(AnnotatedOsmGraph g, List<Node> l,
 			double topLeftLong, double topLeftLat, double bottomRightLong,
 			double bottomRightLat, XKey key) {
 		double keyVal = key.get_keyValue();
 
 		// System.out.println("Query X " + keyVal);
 
 		HasYChild leftChild = key.getFirstHasYChildIncidence();
 		if (leftChild == null) {
 			rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 					bottomRightLat, (NodeSet) key.getFirstHasSetIncidence()
 							.getThat());
 		} else {
 			HasYChild rightChild = leftChild.getNextHasYChildIncidence();
 			assert rightChild != null;
 			if (keyVal < topLeftLong) {
 				// examine only the right subtree
 				// System.out.print("R ");
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (YKey) rightChild.getThat());
 			} else if (keyVal >= bottomRightLong) {
 				// examine only the left subtree
 				// System.out.print("L ");
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (YKey) leftChild.getThat());
 			} else {
 				// examine both subtrees
 				// System.out.print("L ");
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (YKey) leftChild.getThat());
 				// System.out.print("R ");
 				rangeQuery(g, l, topLeftLong, topLeftLat, bottomRightLong,
 						bottomRightLat, (YKey) rightChild.getThat());
 			}
 		}
 	}
 
 	/**
 	 * examines for every Node of the given NodeSet set if the Node is contained
 	 * by the rectangle defined by its vertex at the upper left corner and its
 	 * vertex at the lower right corner
	 *
 	 * @param g
 	 * @param topLeftLong
 	 *            longitude of the vertex at the upper left corner of the
 	 *            rectangle
 	 * @param topLeftLat
 	 *            latitude of the vertex at the upper left corner of the
 	 *            rectangle
 	 * @param bottomRightLong
 	 *            longitude of the vertex at the lower right corner of the
 	 *            rectangle
 	 * @param bottomRightLat
 	 *            latitude of the vertex at the lower right corner of the
 	 *            rectangle
 	 * @param set
 	 * @return a List of all Nodes which are included in the rectangle
 	 */
 	public static void rangeQuery(AnnotatedOsmGraph g, List<Node> l,
 			double topLeftLong, double topLeftLat, double bottomRightLong,
 			double bottomRightLat, NodeSet set) {
 		for (HasElement e : set.getHasElementIncidences()) {
 			Node n = (Node) e.getThat();
 			if ((n.get_latitude() >= topLeftLat)
 					&& (n.get_latitude() <= bottomRightLat)
 					&& (n.get_longitude() <= bottomRightLong)
 					&& (n.get_longitude() >= topLeftLong)
 					&& (n.getFirstSegmentIncidence() != null)) {
 				l.add(n);
 			}
 		}
 	}
 
 	public static Node nearestNodeStart(AnnotatedOsmGraph g, double lat,
 			double lon) {
 		Key key = (Key) g.getKDTree().getFirstHasRootIncidence().getThat();
 		if ((key instanceof XKey) || (key instanceof YKey)) {
 			return nearestNode(g, key, lat, lon);
 		} else {
 			throw new RuntimeException("Unexpected KD root type "
					+ key.getM1Class());
 		}
 	}
 
 	public static Node nearestNode(AnnotatedOsmGraph g, Key key, double lat,
 			double lon) {
 		double keyVal = key.get_keyValue();
 		if (key instanceof XKey) {
 			HasXChild leftChild = ((XKey) key).getFirstHasXChildIncidence();
 			if (lon <= keyVal) {
 				return nearestNode(g, (XKey) leftChild.getThat(), lat, lon);
 			} else {
 				return nearestNode(g, (XKey) leftChild
 						.getNextHasXChildIncidence().getThat(), lat, lon);
 			}
 		} else {
 			HasYChild leftChild = ((YKey) key).getFirstHasYChildIncidence();
 			if (lat <= keyVal) {
 				return nearestNode(g, (YKey) leftChild.getThat(), lat, lon);
 			} else {
 				return nearestNode(g, (YKey) leftChild
 						.getNextHasYChildIncidence().getThat(), lat, lon);
 			}
 		}
 	}
 
 	public static double getDistance(Node n, double lat, double lon) {
 		double helpX = (n.get_longitude() - lon) * (n.get_longitude() - lon);
 		double helpY = (n.get_latitude() - lat) * (n.get_latitude() - lat);
 		return Math.sqrt(helpX + helpY);
 	}
 
 	public static Node nearestNode(AnnotatedOsmGraph g, NodeSet set,
 			double lat, double lon) {
 		double dist = 0.0;
 		Node nearest = null;
 		for (Node n : set.get_elements()) {
 			double curdist = getDistance(n, lat, lon);
 			if ((nearest == null) || (curdist < dist)) {
 				dist = curdist;
 				nearest = n;
 			}
 		}
 		return nearest;
 	}
 }
