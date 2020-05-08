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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import org.life.sl.routefinder.RouteFinder;
 
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;
 import com.vividsolutions.jts.planargraph.Edge;
 
 /**
  * EdgeStatistics is a statistics of how many points are associated with each edge
  * @author Bernhard Barkow
  * @author Bernhard Snizek
  */
 public class EdgeStatistics {
 
 	double distPEAvg, distPE5, distPE95;	///> average distance between points an associated edges, plus 5% qantiles
 	
 	HashMap<Edge, Integer> edgePoints = new HashMap<Edge, Integer>();	///> container counting the number of points associated with each edge
 	private double pointEdgeDist[] = null;
 	
 	/**
 	 * default constructor
 	 */
 	public EdgeStatistics() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	/**
 	 * constructer which initializes the EdgeStatistics with a network and a set of GPS points
 	 * @param rf RouteFinder which contains the routes (edge network)
 	 * @param gpsPoints array of GPS points for which the statistics is created (measure how well the points fit to edges)
 	 */
 	public EdgeStatistics(RouteFinder rf, ArrayList<Point> gpsPoints) {
 		init(rf, gpsPoints);
 	}
 	
 	/**
 	 * initialize the EdgeStatistics with a network and a set of GPS points
 	 * @param rf RouteFinder which contains the routes (edge network)
 	 * @param gpsPoints array of GPS points for which the statistics is created (measure how well the points fit to edges)
 	 */
 	public void init(RouteFinder rf, ArrayList<Point> gpsPoints) {
 		pointEdgeDist = new double[gpsPoints.size()];
 		rf.setEdgeStatistics(this);
 		// first, clear the statistics:
 		edgePoints.clear();
 		// then, loop over all GPS points:
 		int i = 0;
 		for (Point p : gpsPoints) {
 			Edge e = rf.getNearestEdge(p);
 			addPoint(e);	// add the point to the associated edge (the edge nearest to each GPS data point)
			if (e!=null) pointEdgeDist[i++] = p.distance(((LineMergeEdge)e).getLine().getBoundary());	// distance between point and nearest edge
 		}
 		initPEDistStat();
 	}
 	
 	public void initPEDistStat() {
 		Arrays.sort(pointEdgeDist);
 		distPEAvg = 0;
 		for (double d : pointEdgeDist) {
 			distPEAvg += d;
 		}
 		double l = (double)pointEdgeDist.length;
 		distPEAvg /= l;
 		distPE5 = pointEdgeDist[(int)Math.round(l*0.05)];
 		distPE95 = pointEdgeDist[(int)Math.round(l*0.95)];
 	}
 	
 	public double getDistPEAvg() {
 		return distPEAvg;
 	}
 	public double getDistPE5() {
 		return distPE5;
 	}
 	public double getDistPE95() {
 		return distPE95;
 	}
 	
 	/**
 	 * add a new edge to the statistics table and initialize its counter with 0
 	 * @param e the edge to add
 	 */
 	public void addEdge(Edge e) {
 		edgePoints.put(e, 0);
 	}
 	
 	/**
 	 * add a point to an associated edge; 
 	 * if the edge is not contained in the statistics yet, it is initialized
 	 * @param e
 	 */
 	public void addPoint(Edge e) {
 		if (e != null) {
 			if (!edgePoints.containsKey(e)) edgePoints.put(e, 1);
 			else edgePoints.put(e, edgePoints.get(e) + 1);
 		}
 	}
 	
 	/**
 	 * @param e the edge whose number of associated points is requested
 	 * @return the edge/point-count (the number of points associated with an edge); if the edge does not yet exist in the statistics, 0 is returned
 	 */
 	public int getCount(Edge e) {
 		return (edgePoints.containsKey(e) ? edgePoints.get(e) : 0 );
 	}
 }
