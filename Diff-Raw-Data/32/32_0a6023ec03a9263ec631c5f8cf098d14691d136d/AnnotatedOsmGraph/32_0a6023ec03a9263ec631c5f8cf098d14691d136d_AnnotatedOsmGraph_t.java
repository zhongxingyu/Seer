 package de.uni_koblenz.jgstreetmap.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.Stack;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
 import de.uni_koblenz.jgstreetmap.osmschema.impl.std.OsmGraphImpl;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.HasRoot;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.KDTree;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.Key;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.NodeSet;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.XKey;
 import de.uni_koblenz.jgstreetmap.osmschema.kdtree.YKey;
 import de.uni_koblenz.jgstreetmap.osmschema.map.Node;
 import de.uni_koblenz.jgstreetmap.osmschema.map.OsmPrimitive;
 import de.uni_koblenz.jgstreetmap.osmschema.map.Way;
 import de.uni_koblenz.jgstreetmap.routing.Segmentator;
 
 public class AnnotatedOsmGraph extends OsmGraphImpl {
 	private Map<Long, OsmPrimitive> osmIdMap;
 	private GraphMarker<LayoutInfo> layoutInfo;
 	private SortedMap<Integer, List<Way>> orderedWays;
 	private KDTree kdTree;
 
 	public AnnotatedOsmGraph(String id, int vmax, int emax) {
 		super(id, vmax, emax);
 		osmIdMap = new HashMap<Long, OsmPrimitive>();
 		layoutInfo = new GraphMarker<LayoutInfo>(this);
 		orderedWays = new TreeMap<Integer, List<Way>>();
 	}
 
 	@Override
 	public void loadingCompleted() {
 		for (OsmPrimitive o : getOsmPrimitiveVertices()) {
 			osmIdMap.put(o.get_osmId(), o);
 		}
 		for (Way way : getWayVertices()) {
 			LayoutInfo l = LayoutInfo.computeInfo(way);
 			layoutInfo.mark(way, l);
 			List<Way> lst = orderedWays.get(l.zOrder);
 			if (lst == null) {
 				lst = new LinkedList<Way>();
 				orderedWays.put(l.zOrder, lst);
 			}
 			lst.add(way);
 		}
 
 		// KDTreeBuilder.buildTree(this, 10);
 
 		// Mengengerüst berechnen
 		Map<String, Integer> m = new HashMap<String, Integer>();
 		for (Vertex v = getFirstVertex(); v != null; v = v.getNextVertex()) {
			String n = "V " + v.getSchemaClass().getSimpleName();
 			if (m.containsKey(n)) {
 				m.put(n, m.get(n) + 1);
 			} else {
 				m.put(n, 1);
 			}
 		}
 		for (de.uni_koblenz.jgralab.Edge e = getFirstEdge(); e != null; e = e
 				.getNextEdge()) {
			String n = "E " + e.getSchemaClass().getSimpleName();
 			if (m.containsKey(n)) {
 				m.put(n, m.get(n) + 1);
 			} else {
 				m.put(n, 1);
 			}
 		}
 		for (String n : new TreeSet<String>(m.keySet())) {
 			System.err.println(m.get(n) + "\t" + n);
 		}
 
 		kdTree = getFirstKDTree();
 	}
 
 	public SortedMap<Integer, List<Way>> getOrderedWayVertices() {
 		return orderedWays;
 	}
 
 	public LayoutInfo getLayoutInfo(OsmPrimitive o) {
 		return layoutInfo.getMark(o);
 	}
 
 	public OsmPrimitive getOsmPrimitiveById(long osmId) {
 		return osmIdMap.get(osmId);
 	}
 
 	public static String getTag(OsmPrimitive o, String key) {
 		Map<String, String> tags = o.get_tags();
 		if (tags != null) {
 			return tags.get(key);
 		}
 		return null;
 	}
 
 	public boolean hasKDTree() {
 		return kdTree != null;
 	}
 
 	public void deleteKDTree() {
 		if (kdTree == null) {
 			return;
 		}
 		Stack<Key> s = new Stack<Key>();
 		HasRoot e = kdTree.getFirstHasRootIncidence();
 		if (e != null) {
 			s.push((Key) e.getThat());
 			while (!s.empty()) {
 				Key current = s.pop();
 				NodeSet ns = (NodeSet) current.getFirstHasSetIncidence()
 						.getThat();
 				if (ns != null) {
 					ns.delete();
 				} else {
 					if (current instanceof XKey) {
 						for (Key k : ((XKey) current).get_children()) {
 							s.push(k);
 						}
 					} else {
 						for (Key k : ((YKey) current).get_children()) {
 							s.push(k);
 						}
 					}
 					current.delete();
 				}
 			}
 		}
 		kdTree.delete();
 		kdTree = null;
 	}
 
 	public KDTree getKDTree() {
 		return kdTree;
 	}
 
 	@Override
 	public KDTree createKDTree() {
 		if (kdTree == null) {
 			kdTree = super.createKDTree();
 		}
 		return kdTree;
 	}
 
 	public static class Neighbour {
 		private Node node;
 		private double distance;
 
 		public Neighbour(Node n, double d) {
 			node = n;
 			distance = d;
 		}
 
 		public Node getNode() {
 			return node;
 		}
 
 		public double getDistance() {
 			return distance;
 		}
 
 		public static Comparator<Neighbour> getComparator() {
 			return new Comparator<Neighbour>() {
 				@Override
 				public int compare(Neighbour o1, Neighbour o2) {
 					return (int) Math.signum(o1.distance - o2.distance);
 				}
 			};
 		}
 	}
 
 	public List<Neighbour> neighbours(double lat, double lon, double maxDistance) {
 		List<Neighbour> l = new ArrayList<Neighbour>();
 		for (Node n : getNodeVertices()) {
 			double dist = Segmentator.distance(lat, lon, n);
 			if ((dist < maxDistance) && (n.getFirstSegmentIncidence() != null)) {
 				l.add(new Neighbour(n, dist));
 			}
 		}
 		Collections.sort(l, Neighbour.getComparator());
 		return l;
 	}
 }
