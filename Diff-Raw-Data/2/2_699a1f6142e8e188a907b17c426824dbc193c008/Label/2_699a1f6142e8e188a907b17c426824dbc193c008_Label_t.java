 package org.life.sl.routefinder;
 
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
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.geotools.data.DataUtilities;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.Transaction;
 import org.geotools.data.collection.ListFeatureCollection;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.data.shapefile.ShapefileDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.data.simple.SimpleFeatureStore;
 //import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.SchemaException;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.life.sl.mapmatching.EdgeStatistics;
 import org.life.sl.orm.HibernateUtil;
 import org.life.sl.orm.OSMNode;
 import org.life.sl.routefinder.RouteFinder.LabelTraversal;
 import org.life.sl.utils.MathUtil;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;
 import com.vividsolutions.jts.planargraph.DirectedEdge;
 import com.vividsolutions.jts.planargraph.Edge;
 import com.vividsolutions.jts.planargraph.Node;
 /**
  * This class is used by the RouteGenerator to search for routes, and can also be used
  * to represent a route. Normally you would use a List of edges for that.
  * @author Pimin Konstantin Kefaloukos
  *
  */
 public class Label implements Comparable<Label> {
 	
 	/**
 	 * Comparator comparing only the last edge of two labels
 	 * @author bb
 	 */
 	public static class LastEdgeComparator implements Comparator<Label> {
 		RouteFinder.LabelTraversal sortOrder;
 		
 		public LastEdgeComparator(RouteFinder.LabelTraversal so) {
 			sortOrder = so;
 		}
 		public int compare(Label arg0, Label arg1) {
 			int r = arg0.compareTo_LE(arg1, sortOrder==LabelTraversal.BestFirstDR);
 			if (sortOrder == LabelTraversal.WorstFirst) r = -r;
 			return r;
 		}	
 	}
 
 	/**
 	 * Comparator comparing only the direction of the last edge of two labels
 	 * @author bb
 	 */
 	public static class LastEdgeDirectionComparator implements Comparator<Label> {
 		double ODAngle;
 		
 		public LastEdgeDirectionComparator(Node startNode, Node endNode) {
 			DirectedEdge e = new DirectedEdge(startNode, endNode, endNode.getCoordinate(), true);
 			ODAngle = e.getAngle();
 		}
 		public int compare(Label arg0, Label arg1) {
 			int r = arg0.compareDir_LE(arg1, ODAngle);
 			return r;
 		}	
 	}
 
 	/**
 	 * Comparator comparing the length of two labels
 	 * @author bb
 	 */
 	public static class LengthComparator implements Comparator<Label> {
 		public int compare(Label arg0, Label arg1) {
 			return arg0.compareTo_length(arg1);
 		}	
 	}
 
 	/**
 	 * A more universal Comparator considering the length AND the match score of two labels
 	 * @author bb
 	 */
 	public static class UniversalComparator implements Comparator<Label> {
 		private double l_ms_weight = 0;	// weighting factor
 		
 		public UniversalComparator(double l_ms_weight) {
 			this.l_ms_weight = l_ms_weight;
 		}
 		public int compare(Label arg0, Label arg1) {
 			return arg0.compareTo_lengthMS(arg1, l_ms_weight);
 		}	
 	}
 
 	private final double kCoordEps = 1.e0;		///< tolerance for coordinate comparison (if (x1-x2 < kCoordEps) then x1==x2)
 
 	private Label parent;			///> The parent of the Label
 	private Node node;				///> The node associated with this Label
 	private DirectedEdge backEdge;	///> The GeoEdge leading back to the node associated with the parent Label
 	private double length = 0.;		///> if the label represents a route, this is the length of the route (sum of all backEdges)
 	private double lastEdgeLength = 0.;	///> length of last backEdge
 	private double score = -1.;		///> the score of the label (evaluated according to edge statistics)
 	private short scoreCount = -1;	///> the unweighted score of the label (nearest points-count)
 	//private double lastScore = 0.;	///> the same but considering only the last edge	
 	private short lastScoreCount = 0;
 	private double pathSizeAttr = 0;	///> the Path Size Attribute (in a global context)
 	private double ODDirection = 0.;	///> the direction relative to the OD connection (+1 = parallel, 0 = normal, -1 = antiparallel)
 	
 	private List<Node> nodeList = null;			///< list of Nodes (not OSM database nodes!)
 	private List<DirectedEdge> edgeList = null;	///< list of Edges (not OSM database edges!)
 	private int[] nodeIDs = null;				///< list of OSMNode IDs
 	private int[] edgeIDs = null;				///< list of OSMEdge IDs
 
 	private static com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
 	
 	/**
 	 * Create a new Label as descendant of a parent label
 	 * @param parent The Label that this Label was expanded from
 	 * @param node The node associated with this Label.
 	 * @param backEdge The edge leading back to the parent of the Label.
 	 * @param length The new accumulative length of the route represented by this Label.
 	 */
 	public Label(Label parent, Node node, DirectedEdge backEdge, double length, double lastEdgeLength) {
 		//		System.out.println("Label(+) " + node.getCoordinate());
 		this.parent = parent;
 		this.node = node;
 		this.backEdge = backEdge;
 		this.lastEdgeLength = lastEdgeLength;
 		this.length = length;
 	}
 
 	/**
 	 * Create a new "empty" Label at a node;
 	 * parent and backEdge are set to null, length is set to 0.0 and expand is set to true.
 	 * @param node The node associated with this Label.
 	 */
 	public Label(Node node) {
 		this.parent = null;
 		this.node = node;
 		this.backEdge = null;
 		this.length = 0.;
 		this.lastEdgeLength = 0.;
 	}
 
 	/**
 	 * string representation
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return node.toString();
 	}
 	
 	public boolean equals(Label l) {
 		boolean b = false;
 		if (l.score == this.score) {	// just to avoid having to create and compare the whole node lists
 			List<Node> nodes0 = this.getNodes();
 			List<Node> nodes1 = l.getNodes();
 			b = nodes0.equals(nodes1);
 		}
 		return b;
 	}
 
 	/**
 	 * Comparison method
 	 * @param arg0 the other object to compare this to
 	 * @return 1 if this object is larger than the other, -1 if smaller, 0 if equal
 	 */
 	public int compareTo(Label arg0) {
 		int r = 0;
 		double ov = arg0.getScore();
 		if (this.score > ov) r = 1;
 		else if (this.score < ov) r = -1;
 //		int ov = arg0.getScoreCount();
 //		if (this.scoreCount > ov) r = 1;
 //		else if (this.scoreCount < ov) r = -1;
 		return r;
 	}
 	
 	/**
 	 * Comparison method using only the last edge;
 	 * in addition to the score, the direction relative to the last edge is evaluated if necessary
 	 * @param arg0 the other object to compare this to
 	 * @return 1 if this object is larger than the other, -1 if smaller, 0 if equal
 	 */
 	public int compareTo_LE(Label arg0, boolean useDR) {
 		int r = 0;
 		int ov = arg0.getLastScoreCount();
 		if (this.lastScoreCount > ov) r = 1;
 		else if (this.lastScoreCount < ov) r = -1;
 		else {
 			r = compareTo(arg0);	// default fallback: if both are equal, sort them according to their total score
 			if (ov==0 && useDR) {	// both edges have no points associated: compare directions (dead reckoning)
 				if (parent != null && parent.backEdge != null) {
 					/* Some notes:
 					 * - both edges should have the same parent!
 					 * - at the root node (parent=null), this comparison should not be invoked anyway
 					 * - at the first node, we don't have a reference direction yet;
 					     but if we are at the first node after root and don't have GPS points, we have a problem anyway */
 					if (Math.abs(this.getAngleDiff()) < Math.abs(arg0.getAngleDiff())) r = 1;
 					else r = -1;	// (smaller deviation is better)
 				}
 			}
 		}
 		return r;
 	}
 	
 	/**
 	 * Comparison method using the OD-direction of the last edge;
 	 * @param arg0 the other object to compare this to
 	 * @return 1 if this object is larger than the other, -1 if smaller, 0 if equal
 	 */
 	public int compareDir_LE(Label arg0, double ODAngle) {
 		int r = 0;
 		if (parent != null) {// && parent.backEdge != null) {
 			/* Some notes:
 			 * - both edges should have the same parent!
 			 * - at the root node (parent=null), this comparison should not be invoked anyway
 			 */
 			if (Math.abs(this.getODDirection(ODAngle)) > Math.abs(arg0.getODDirection(ODAngle))) r = 1;
 			else r = -1;	// (larger value (more parallel to OD) is better)
 		}
 		return r;
 	}
 	
 	/**
 	 * Comparison method using the route length
 	 * @param arg0 the other object to compare this to
 	 * @return 1 if this object is larger (longer) than the other, -1 if smaller (shorter), 0 if equal
 	 */
 	public int compareTo_length(Label arg0) {
 		int r = 0;
 		double ov = arg0.getLength();
 		if (this.length > ov) r = 1;
 		else if (this.length < ov) r = -1;
 		return r;
 	}
 	
 	/**
 	 * Comparison method using both the route length and the match score;
 	 * shorter length and higher match score mean "smaller" (meaning "better" in natural order sorting)
 	 * @param arg0 the other object to compare this to
 	 * @param l_ms_weight weighting factor: 0 = consider only the match score, 1 = consider only the length
 	 * @return 1 if this object is larger (longer) than the other, -1 if smaller (shorter), 0 if equal
 	 */
 	public int compareTo_lengthMS(Label arg0, double l_ms_weight) {
 		double r_ms = Math.abs(arg0.score / this.score);			// > 1 ... this > arg0
 		double r_l = Math.abs(this.length / arg0.length);			// > 1 ... this > arg0
 		double f = r_ms * (1. - l_ms_weight) + r_l * l_ms_weight;	// the weighted combination of both factors
 		int r = 0;
 		if (f > 1) r = 1;
 		else if (f < 1) r = -1;
 		return r;
 	}
 
 	// The following are getter methods:
 	
 	/**
 	 * @return Returns the parent of the Label
 	 */
 	public Label getParent() {
 		return this.parent;
 	}
 
 	/**
 	 * @return Returns the GeoNode associated with this label. GeoNodes can be associated with any number
 	 * of Labels, but Labels can only be associated with one GeoNode.
 	 */
 	public Node getNode() {
 		return this.node;
 	}
 
 	/**
 	 * @return Returns the edge leading back to the parent of this Label.
 	 */
 	public DirectedEdge getBackEdge() {
 		return this.backEdge;
 	}
 
 	/**
 	 * @return Returns the length of the route represented by this Label.
 	 */
 	public double getLength() {
 		return this.length;
 	}
 	
 	/**
 	 * Convenience function: returns distance from the current to another node 
 	 * @param node1 the other node
 	 * @return Cartesian distance between current node and node1 
 	 */
 	public double getDistanceTo(Node node1) {
 		return node.getCoordinate().distance(node1.getCoordinate());
 	}
 	
 	/**
 	 * Computes the angular difference between the two previous edges
 	 * @return
 	 */
 	public double getAngleDiff() {
 		if (parent != null && parent.backEdge != null) {
 			return MathUtil.mapAngle_radians(backEdge.getAngle() - (parent.backEdge.getSym().getAngle() - Math.PI));
 		} else return 0;	// at the first node, we don't have a reference direction yet
 	}
 
 	public double getODDirection() {
 		return ODDirection;
 	}
 	/**
 	 * compute and return ODDirection, the angle between the last edge and the connection from Origin to Destination 
 	 * @param ODAngle
 	 * @return ODDirection
 	 */
 	public double getODDirection(double ODAngle) {
 		if (parent != null) {// && parent.backEdge != null) {
			ODDirection = 1. - 2. * Math.abs(MathUtil.mapAngle_radians(Math.abs(backEdge.getAngle() - ODAngle)) / Math.PI);	// should be +1 for parallel, 0 for normal, -1 for antiparallel
 		} else ODDirection = 0;	// at the first node, we don't have a reference direction yet
 		return ODDirection;
 	}
 
 	/**
 	 * calculate the weighted and unweighted score of this label from the edge statistics;
 	 * 	this is a measure of the quality of the route: number of fitting data points, normalized to route length
 	 *  (the recursive variant is about 3 times faster than doing it explicitely for all edges)
 	 * @param eStat edge statistics
 	 */
 	public void calcScore(EdgeStatistics eStat) {
 		// calculate the score recursively:
 		score = 0.;
 		scoreCount = 0;
 		if (parent != null) {
 			lastScoreCount = eStat.getCount(backEdge.getEdge());
 			scoreCount = (short) (parent.getScoreCount(eStat) + lastScoreCount);
 //			lastScore = (double)lastScoreCount / lastEdgeLength;
 			//score = Math.round(parent.getScore(eStat) * parent.getLength()) + eStat.getCount(backEdge.getEdge());	// backEdge should be the last edge in the label
 			if (length > 0.) score = scoreCount / length;
 		}
 	}
 	
 	/**
 	 * @return the score of this label, freshly calculated from the edge statistics, if necessary
 	 * @param eStat edge statistics
 	 */
 	public double getScore(EdgeStatistics eStat) {
 		if (score < 0.) calcScore(eStat);
 		return score;
 	}
 	/**
 	 * @return the score of this label (getter method)
 	 */
 	public double getScore() {
 		return score;
 	}
 	/**
 	 * @return the unweighted score of this label (nearest points-count), freshly calculated from the edge statistics, if necessary
 	 * @param eStat edge statistics
 	 */
 	public short getScoreCount(EdgeStatistics eStat) {
 		if (scoreCount < 0) calcScore(eStat);
 		return scoreCount;
 	}
 	public short getScoreCount() {
 		return scoreCount;
 	}
 //	public double getLastScore() {
 //		return lastScore;
 //	}
 	public short getLastScoreCount() {
 		return lastScoreCount;
 	}
 	
 	public double getLastEdgeLength() {
 		return this.lastEdgeLength;
 	}
 	
 	/**
 	 * Return a parent label n generations back
 	 * @param n how far back in the history
 	 * @return label n generations back
 	 */
 	public Label getNthParent(int n) {
 		int i = n;
 		Label label = this;
 		while (label.parent != null && i > 0) {
 			label = label.parent;
 			i--;
 		}
 		return label;
 	}
 
 	/**
 	 * Given a directed edge, this method calculates how many times the undirected parent edge has been visited by the route
 	 * represented by this Label.
 	 * @param dirEdge The directed edge we are querying about.
 	 * @return An int value indicating the degree of overlap for the edge. Counting occurrences in both directions.
 	 */
 	public int getOccurrencesOfEdge(DirectedEdge dirEdge) {
 		Label label = this;
 		int n = 0;
 		while (label.getBackEdge() != null) {
 			Edge undirectedBackEdge = label.getBackEdge().getEdge();
 			if (dirEdge.getEdge() == undirectedBackEdge) n++;	// important: compare the undirected edges!
 			label = label.getParent();
 		}
 		return n;
 	}
 
 	/**
 	 * Given a directed edge, this method calculates how many times the undirected parent edge has been visited by the route
 	 * represented by this Label - alternative to getOccurrencesOfEdge().
 	 * @param dirEdge The directed edge we are querying about.
 	 * @return An int value indicating the degree of overlap for the edge. Counting occurrences in both directions (direction independent).
 	 */
 	public int getOccurrencesOfEdge_2(DirectedEdge dirEdge, boolean useDir) {
 		int n = 0;
 		edgeList = getRouteAsEdges();
 		for (DirectedEdge e : edgeList) {
 			if (useDir) {	// consider direction
 				if (dirEdge == e) n++;
 			} else {	// direction independent
 				if (dirEdge.getEdge() == e.getEdge()) n++;	// important: compare the undirected edges!
 			}
 		}
 		return n;
 	}
 
 	/**
 	 * Given a node, this method calculates how many times that node has been visited by the route
 	 * represented by this Label.
 	 * @param node The node we are querying about.
 	 * @return An int value indicating the degree of overlap for the node.
 	 */
 	public int getOccurrencesOfNode(Node node) {
 		Label label = this;
 		int n = 0;
 		while (label != null) {
 			if (node == label.getNode()) n++;
 			label = label.getParent();
 		}
 		return n;
 	}
 	
 	/**
 	 * Calculate the overlap factor, a value in [0...1]: 0 means no overlap, 1 means the whole route is contained in lbl0.
 	 * See also: E. Frejinger, M. Bierlaire, M. Ben-Akiva: Expanded Path Size Attribute, March 2009 
 	 * @param lbl0 the other label (route) to compare this one to
 	 * @param useDir if true, the edge direction is considered, if not, the overlap is computed independent of the edge direction 
 	 * @return the overlap factor [0...1]
 	 */
 	public double getOverlap(Label lbl0, boolean useDir) {
 		double ps = 0;
 		for (DirectedEdge e : this.getRouteAsEdges()) {
 			if (lbl0.getOccurrencesOfEdge_2(e, useDir) != 0) {	// direction independent
 				ps += ((LineMergeEdge)e.getEdge()).getLine().getLength();	// add length of this edge
 			}
 		}
 		ps /= getLength();
 		return ps;
 	}
 	
 	/**
 	 * Calculate the overlap factor of this label and all routes in a given set of labels. 
 	 * @param labels a list of labels
 	 * @param useDir if true, the edge direction is considered, if not, the overlap is computed independent of the edge direction
 	 * @param maxOverlap threshold: if > 0, the comparison is only performed until an overlap factor above this threshold is found (to improve performance) 
 	 * @return the maximum overlap factor [0...1] between this route and all those in the given set of labels (unless maxOverlap is given)
 	 */
 	public double getOverlapWithSet(List<Label> labels, boolean useDir, double maxOverlap) {
 		double ps = 0;
 		for (Label l : labels) {
 			ps = Math.max(ps, getOverlap(l, useDir));
 			if (maxOverlap > 0 && ps > maxOverlap) break;	// speed things up if we only check for a threshold
 			// TODO: extend that by doing a two-way comparison?
 			ps = Math.max(ps, l.getOverlap(this, useDir));	// ??
 			if (maxOverlap > 0 && ps > maxOverlap) break;
 		}
 //		System.out.print(".");
 		return ps;
 	}
 	
 	/**
 	 * Calculate the Path Size in a global context.
 	 * See also: E. Frejinger, M. Bierlaire, M. Ben-Akiva: Expanded Path Size Attribute, March 2009 
 	 * @param weights a HashMap containing the number of occurrences of all Edges 
 	 * @return the Path Size Attribute
 	 */
 	public double getPathSize_global(HashMap<Integer, Integer> weights) {
 		double ps = 0;
 		for (DirectedEdge de : this.getRouteAsEdges()) {
 			Edge e = de.getEdge();
 			// the edge ID is used as key for the HashMap 
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> userdata = (HashMap<String, Object>) e.getData();	// the user data object of the Edge
 			Integer edgeID = (Integer)userdata.get("id");
 			Integer ne = (weights.containsKey(edgeID) ? weights.get(edgeID) : 1);
 			//System.out.print(ne + "\t");
 			ps += ((LineMergeEdge)e).getLine().getLength() / (double)ne;	// add length of this edge, divided by number of uses
 		}
 		pathSizeAttr = ps / getLength();	// store in class variable
 		//System.out.println("\n" + getLength() + " ps: " + ps);
 		return pathSizeAttr;
 	}
 	
 	public double getPathSizeAttr() { return pathSizeAttr; } 
 
 	/**
 	 * Create a list of all the directed edges along this route, starting at the origin;
 	 * for efficiency, this list is only compiled once and cached in edgeList.
 	 * @return the route represented by this label as a list of directed edges
 	 */
 	public List<DirectedEdge> getRouteAsEdges() {
 		if (edgeList == null || edgeList.size() == 0) {
 			edgeList = new ArrayList<DirectedEdge>();
 			Label label = this;
 			while(label.getParent() != null) {
 				edgeList.add(label.getBackEdge());
 				label = label.getParent();
 			}
 			Collections.reverse(edgeList);	// now, the topmost label represents the first edge in the list
 		}
 		return edgeList;
 	}
 
 	/**
 	 * Create a list of all the Nodes along this route, starting with the origin;
 	 * for efficiency, this list is only compiled once and cached in nodeList.
 	 * @return a list of all the Nodes of this label starting with the origin 
 	 */
 	public List<Node> getNodes() {
 		if (nodeList == null || nodeList.size() == 0) {
 			nodeList = new ArrayList<Node>();
 			Label label = this;
 			while(label != null) {
 				nodeList.add(label.getNode());
 				label = label.getParent();
 			}
 			Collections.reverse(nodeList);	// now, the origin node is first in the list
 		}
 		return nodeList;
 	}
 
 	/**
 	 * Retrieve the database IDs of all edges
 	 * @return array with edge IDs
 	 */
 	public int[] getEdgeIDs() {
 		if (edgeIDs == null) {
 			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			session.beginTransaction();
 			List<DirectedEdge> edges = getRouteAsEdges();
 			edgeIDs = new int[edges.size()];
 			int n = 0;
 			for (DirectedEdge e : edges) {		// for each node along the route:
 				@SuppressWarnings("unchecked")
 				HashMap<String, Object> ed = (HashMap<String, Object>) e.getEdge().getData();
 				edgeIDs[n++] = (Integer)ed.get("id");
 			}
 			//session.getTransaction().commit();
 		}
 		return edgeIDs;
 	}
 
 	/**
 	 * Retrieve the database IDs of nodes and edges
 	 * @return array with node IDs
 	 */
 	public int[] getNodeIDs() {
 		if (nodeIDs == null) {
 			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 			session.beginTransaction();
 			List<Node> nodes = getNodes();
 			nodeIDs = new int[nodes.size()];
 			List<DirectedEdge> edges = getRouteAsEdges();
 			int n = 0;
 			for (DirectedEdge e : edges) {		// for each node along the route:
 				@SuppressWarnings("unchecked")
 				HashMap<String, Object> ed = (HashMap<String, Object>) e.getEdge().getData();
 				Integer edgeID = (Integer)ed.get("id");
 	
 				Node node = e.getFromNode();	// node at beginning of edge
 				Coordinate c_n = node.getCoordinate();
 	
 				// get node ID from database:
 				int nodeID = 0;
 				String s = " from OSMEdge where id=" + edgeID;
 				s = "from OSMNode where (id = (select fromnode"+s+") or id = (select tonode"+s+"))";
 				Query nodeRes = session.createQuery(s);
 				// TODO: make this more efficient using a PostGIS spatial query with indexing?
 				// match coordinates:
 				@SuppressWarnings("unchecked")
 				Iterator<OSMNode> it = nodeRes.iterate();
 				while (it.hasNext()) {
 					OSMNode on = it.next();
 					Coordinate onc = on.getGeometry().getCoordinate();
 					if (Math.abs(c_n.x - onc.x) < kCoordEps && Math.abs(c_n.y - onc.y) < kCoordEps) {
 						nodeID = on.getId();
 						break;
 					}								
 				}	// now, nodeID is either 0 or the database ID of the corresponding node
 				nodeIDs[n++] = nodeID;
 			}
 			//session.getTransaction().commit();
 		}
 		return nodeIDs;
 	}
 
 	/**
 	 * Create a list of all the Nodes along this route, starting with the origin;
 	 * for efficiency, this list is only compiled once and cached in nodeList.
 	 * @return a list of all the Nodes of this label starting with the origin 
 	 */
 	public List<Label> getLabels() {
 		ArrayList<Label> lblList = new ArrayList<Label>();
 		Label label = this;
 		while(label != null) {
 			lblList.add(label);
 			label = label.getParent();
 		}
 		Collections.reverse(lblList);	// now, the first label is first in the list
 		return lblList;
 	}
 	
 	/**
 	 * @return an array of Coordinates of all nodes (sorted from start to end)
 	 */
 	public Coordinate[] getCoordinates() {
 		List<Node> nodes = getNodes();
 		Coordinate[] coordinates = new Coordinate[nodes.size()];
 		int i = 0;
 		for (Node curNode : nodes) {
 			coordinates[i++] = curNode.getCoordinate();
 		}
 		return coordinates;
 	}
 	
 	/**
 	 * @return an array of Coordinates of all vertices (sorted from start to end) (not just nodes!)
 	 */
 	public LineString getLineString() {
 		ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
 		List<DirectedEdge> edges = getRouteAsEdges();
 
 		// check edge 0 versus edge 1
 		@SuppressWarnings("unchecked")
 		HashMap<String, Object> data0 = (HashMap<String, Object>) edges.get(0).getEdge().getData();
 		LineString ls0 = (LineString)data0.get("geom");
 		if (edges.size() > 1 && ls0 != null) {
 			double kSnapDistance = 0.5;
 
 			Coordinate[] cs0 = ls0.getCoordinates();
 
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> data1 = (HashMap<String, Object>) edges.get(1).getEdge().getData();
 			LineString ls1 = (LineString)data1.get("geom");
 			Coordinate[] cs1 = ls1.getCoordinates();
 
 			// let's isolate vertex number 0:
 			Coordinate cs0_vertex_0 = cs0[0];
 			Coordinate cs1_vertex_0 = cs1[0];
 			Coordinate cs1_vertex_last = cs1[cs1.length-1];
 
 			if ((cs0_vertex_0.distance(cs1_vertex_0) < kSnapDistance) || (cs0_vertex_0.distance(cs1_vertex_last) < kSnapDistance)) {
 				// reverse direction:
 				for (int c=cs0.length-1;c>-1; c--) coordinates.add(cs0[c]);
 			} else {
 				// we already have the correct direction:
 				for (int c=0; c<cs0.length; c++) coordinates.add(cs0[c]);
 			}
 
 			int i = 0;
 			for (DirectedEdge e : edges) {
 				if (i > 0) {	// only from the second edge on
 					@SuppressWarnings("unchecked")
 					HashMap<String, Object> data = (HashMap<String, Object>) e.getEdge().getData();
 					LineString ls = (LineString)data.get("geom");
 
 					Coordinate[] cc = ls.getCoordinates();
 					int ccl = cc.length;
 					if (ccl > 1) {	// cc.length must be >= 2
 						// check linestring direction:
 						if (cc[0].distance(coordinates.get(coordinates.size()-1)) < kSnapDistance) {
 							for (int c = 1; c < ccl; c++) coordinates.add(cc[c]);	// first to last
 						} else {
 							for (int c = cc.length-2; c >= 0; c--) coordinates.add(cc[c]);	// reverse direction
 						}
 					}
 				}
 				i++;
 			}
 
 			Coordinate[] coords = new Coordinate[coordinates.size()];
 			coordinates.toArray(coords);
 			return fact.createLineString(coords);
 		} else {	// we have only 1 edge
 			return ls0;
 		}
 	}
 
 	/**
 	 * export the label data to a shape file
 	 * @param filename the name of the shape file
 	 * @throws SchemaException
 	 * @throws IOException
 	 */
 	public void dumpToShapeFile(String filename) throws SchemaException, IOException {
 
 		final SimpleFeatureType TYPE = DataUtilities.createType("route",
 				"location:LineString:srid=4326," + // <- the geometry attribute: Polyline type
 						"name:String," + // <- a String attribute
 						"number:Integer" // a number attribute
 				);
 
 		// 1. build a feature
 		ArrayList<SimpleFeature> features = new ArrayList<SimpleFeature>();
 		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
 		for (DirectedEdge l : this.getRouteAsEdges()) {
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> data = (HashMap<String, Object>) l.getEdge().getData();
 			LineString ls = (LineString) data.get("geometry");
 			SimpleFeature feature = featureBuilder.buildFeature(null);	
 			feature.setDefaultGeometry(ls);
 			features.add(feature);
 		}
 		SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);//FeatureCollections.newCollection();
 		
 		// 2. write to a shapefile
         System.out.println("Writing to shapefile " + filename);
 		File newFile = new File(filename);
 		// File newFile = getNewShapeFile(file);
 
         ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
 
         Map<String, Serializable> params = new HashMap<String, Serializable>();
         params.put("url", newFile.toURI().toURL());
         params.put("create spatial index", Boolean.TRUE);
 
         ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
         newDataStore.createSchema(TYPE);
 
         // You can comment out this line if you are using the createFeatureType method (at end of
         // class file) rather than DataUtilities.createType
         newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
 		
         Transaction transaction = new DefaultTransaction("create");
 
         String typeName = newDataStore.getTypeNames()[0];
         SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
         
         if (featureSource instanceof SimpleFeatureStore) {
             SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
 
             featureStore.setTransaction(transaction);
             try {
                 featureStore.addFeatures(collection);
                 transaction.commit();
             } catch (Exception problem) {
                 problem.printStackTrace();
                 transaction.rollback();
             } finally {
                 transaction.close();
             }
             // System.exit(0); // success!
         } else {
             System.out.println(typeName + " does not support read/write access");
             System.exit(1);	// exit program with status 1 (error)
         }
     }
 		
 	/**
 	 * print the edge data of the current route to the console (mostly  for debugging purposes)
 	 */
 	public void printRoute() {
 		List<DirectedEdge> edges = this.getRouteAsEdges();
 		String resultString = "** ";
 		for (DirectedEdge e : edges) {
 			resultString += e.getData() + " - ";
 		}
 		System.out.println(resultString);
 	}
 }
