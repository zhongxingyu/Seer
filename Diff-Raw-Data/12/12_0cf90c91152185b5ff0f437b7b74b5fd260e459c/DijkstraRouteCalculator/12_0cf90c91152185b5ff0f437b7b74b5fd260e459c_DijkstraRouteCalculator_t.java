 package de.uni_koblenz.jgstreetmap.routing;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import de.uni_koblenz.jgralab.GraphMarker;
 import de.uni_koblenz.jgstreetmap.osmschema.Node;
 import de.uni_koblenz.jgstreetmap.osmschema.OsmGraph;
 import de.uni_koblenz.jgstreetmap.osmschema.routing.Segment;
 import de.uni_koblenz.jgstreetmap.osmschema.routing.SegmentType;
 
 public class DijkstraRouteCalculator {
 
 	private class DijkstraMarker {
 
 		/** indicates that Dijkstra is done with the vertex */
 		boolean done;
 
 		/** * records the distance to the start vertex */
 		double distance;
 
 		/** stores the predecessor in the path from the start vertex */
 		Segment parentSegment;
 
 		DijkstraMarker(double d, Segment s) {
 			distance = d;
 			parentSegment = s;
 		}
 	}
 
 	public enum Direction {
 		NORMAL, REVERSED;
 	}
 
 	public enum EdgeRating {
 		LENGTH, TIME, CONVENIENCE;
 	}
 
 	public enum RoutingRestriction {
 		CAR, BIKE, FOOT
 	}
 
 	public class Speed {
 		public double cycle = 15;
 		public double motorway = 100;
 		public double countryroad = 60;
 		public double residential = 35;
 		public double footway = 5;
 		public double unsurfaced = 20;
 		public double service = 10;
 	}
 
	private static final double INCONVENIENCEVALUE = 50;
 
 	protected OsmGraph graph;
 	protected GraphMarker<DijkstraMarker> dijkstraMarker;
 	PriorityQueue<Node> queue;
 
 	// protected RoutingRestriction rest;
 	protected Set<SegmentType> relevantTypes;
 
 	protected Node start;
 
 	protected boolean routesCalculated;
 
 	protected boolean startChanged;
 
 	protected Speed speeds;
 
 	public DijkstraRouteCalculator(OsmGraph g) {
 		dijkstraMarker = null;
 		graph = g;
 		relevantTypes = new TreeSet<SegmentType>();
 		setRestriction(RoutingRestriction.CAR);
 		startChanged = false;
 		routesCalculated = false;
 		speeds = new Speed();
 	}
 
 	public double calculateCompleteWeight(List<Segment> list, EdgeRating r) {
 		double out = 0;
 		for (Segment currentSegment : list) {
 			switch (r) {
 			case LENGTH:
 				out += currentSegment.getLength();
 				break;
 			case TIME:
 				out += currentSegment.getLength()
 						* computeFactor(currentSegment);
 				break;
 			case CONVENIENCE:
 				out += currentSegment.getLength();
 			}
 
 		}
 		return out;
 	}
 
 	public void calculateShortestRoutes(EdgeRating r) {
 		if (start == null) {
 			throw new IllegalStateException(
 					"setStart() must be called before invoking this method!");
 		}
 		if (dijkstraMarker != null) {
 			dijkstraMarker.clear();
 		} else {
 			dijkstraMarker = new GraphMarker<DijkstraMarker>(graph);
 		}
 
 		if (queue == null) {
 			queue = new PriorityQueue<Node>(128, new Comparator<Node>() {
 				public int compare(Node a, Node b) {
 					return Double.compare(dijkstraMarker.getMark(a).distance,
 							dijkstraMarker.getMark(b).distance);
 				}
 			});
 		} else {
 			queue.clear();
 		}
 
 		dijkstraMarker.mark(start, new DijkstraMarker(0, null));
 		queue.offer(start);
 		// dijkstra algorithm main loop
 		while (!queue.isEmpty()) {
 			// retrieve vertex with smallest distance and mark as "done"
 			Node currentVertex = queue.poll();
 
 			DijkstraMarker m = dijkstraMarker.getMark(currentVertex);
 			m.done = true;
 
 			// follow each traverseable edge
 			for (Segment currentSegment : currentVertex.getSegmentIncidences()) {
 				if (relevantTypes.contains(currentSegment.getWayType())
 						&& (currentSegment.isNormal() || !currentSegment
 								.isOneway())) {
 
 					Node nextVertex = (Node) currentSegment.getThat();
 
 					double newDistance = m.distance
 							+ rate(currentSegment, r, m.parentSegment);
 					// if the new path is shorter than the distance stored
 					// at the other end, this new value is stored
 					DijkstraMarker n = dijkstraMarker.getMark(nextVertex);
 					if (n == null) {
 						dijkstraMarker.mark(nextVertex, new DijkstraMarker(
 								newDistance, currentSegment));
 						queue.offer(nextVertex); // position in the queue
 					} else if (n.distance > newDistance) {
 						n.distance = newDistance;
 						n.parentSegment = currentSegment;
 						if (!n.done) {
 							queue.remove(nextVertex);
 							queue.offer(nextVertex);
 						}
 					}
 				}
 			}
 		}
 		startChanged = false;
 		routesCalculated = true;
 	}
 
 	private double computeFactor(Segment s) {
 		switch (s.getWayType()) {
 		case CYCLEWAY:
 			return 3.6 / speeds.cycle;
 		case MOTORWAY:
 			return 3.6 / speeds.motorway;
 		case PRIMARY:
 			return 3.6 / speeds.countryroad;
 		case SECONDARY:
 			return 3.6 / speeds.countryroad;
 		case TERTIARY:
 			return 3.6 / speeds.residential;
 		case RESIDENTIAL:
 			return 3.6 / speeds.residential;
 		case FOOTWAY:
 			return 3.6 / speeds.footway;
 		case UNSURFACED:
 			return 3.6 / speeds.unsurfaced;
 		case SERVICE:
 			return 3.6 / speeds.service;
 		case WORMHOLE:
 			return 0;
 		default:
 			return Double.MAX_VALUE;
 		}
 	}
 
 	public List<Segment> getRoute(Node target) {
 		if (!routesCalculated) {
 			throw new IllegalStateException(
 					"Routes must be calculated before invoking this method.");
 		}
 		if (startChanged) {
 			throw new IllegalStateException(
 					"start must not be changed after calculating the routes.");
 		}
 		DijkstraMarker m = dijkstraMarker.getMark(target);
 		if (m == null || m.parentSegment == null) {
 			return null;
 		}
 		Stack<Segment> routesegments = new Stack<Segment>();
 		while (m != null && m.parentSegment != null) {
 			routesegments.push(m.parentSegment);
 			m = dijkstraMarker.getMark(m.parentSegment.getThis());
 		}
 
 		List<Segment> out = new ArrayList<Segment>(routesegments.size());
 		while (!routesegments.empty()) {
 			out.add(routesegments.pop());
 		}
 		return out;
 	}
 
 	public Node getStart() {
 		return start;
 	}
 
 	protected double rate(Segment s, EdgeRating r, Segment previous) {
 		switch (r) {
 		case LENGTH:
 			return s.getLength();
 		case TIME:
 			return s.getLength() * computeFactor(s);
 		case CONVENIENCE:
 			if (previous != null)
 				return (previous.getWayId() == s.getWayId()) ? s.getLength()
						: s.getLength() + INCONVENIENCEVALUE;
 			else
 				return s.getLength();
 		default:
 			return Double.MAX_VALUE;
 		}
 	}
 
 	public void setRestriction(RoutingRestriction rest) {
 
 		if (rest == RoutingRestriction.CAR) {
 			relevantTypes.add(SegmentType.MOTORWAY);
 			relevantTypes.add(SegmentType.PRIMARY);
 			relevantTypes.add(SegmentType.SECONDARY);
 			relevantTypes.add(SegmentType.TERTIARY);
 			relevantTypes.add(SegmentType.RESIDENTIAL);
 			relevantTypes.add(SegmentType.WORMHOLE);
 			relevantTypes.add(SegmentType.SERVICE);
 			relevantTypes.add(SegmentType.UNSURFACED);
 		} else if (rest == RoutingRestriction.BIKE) {
 			relevantTypes.add(SegmentType.CYCLEWAY);
 			relevantTypes.add(SegmentType.UNSURFACED);
 			relevantTypes.add(SegmentType.RESIDENTIAL);
 			relevantTypes.add(SegmentType.TERTIARY);
 			relevantTypes.add(SegmentType.SECONDARY);
 			relevantTypes.add(SegmentType.WORMHOLE);
 			relevantTypes.add(SegmentType.SERVICE);
 		} else if (rest == RoutingRestriction.FOOT) {
 			relevantTypes.add(SegmentType.CYCLEWAY);
 			relevantTypes.add(SegmentType.UNSURFACED);
 			relevantTypes.add(SegmentType.RESIDENTIAL);
 			relevantTypes.add(SegmentType.TERTIARY);
 			relevantTypes.add(SegmentType.SERVICE);
 			relevantTypes.add(SegmentType.WORMHOLE);
 			relevantTypes.add(SegmentType.FOOTWAY);
 		}
 
 	}
 
 	public void setStart(Node start) {
 		this.start = start;
 		this.startChanged = true;
 	}
 
 }
