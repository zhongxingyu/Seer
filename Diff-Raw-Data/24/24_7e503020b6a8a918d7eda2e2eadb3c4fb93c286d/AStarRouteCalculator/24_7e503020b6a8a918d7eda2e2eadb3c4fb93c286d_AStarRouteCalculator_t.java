 /**
  * 
  */
 package de.uni_koblenz.jgstreetmap.routing;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import de.uni_koblenz.jgralab.GraphMarker;
 import de.uni_koblenz.jgstreetmap.model.AnnotatedOsmGraph;
 import de.uni_koblenz.jgstreetmap.osmschema.Node;
 import de.uni_koblenz.jgstreetmap.osmschema.OsmGraph;
 import de.uni_koblenz.jgstreetmap.osmschema.Way;
 import de.uni_koblenz.jgstreetmap.osmschema.routing.Segment;
 import de.uni_koblenz.jgstreetmap.osmschema.routing.SegmentType;
 
 /**
  * @author tassilo
  * 
  */
 public class AStarRouteCalculator extends RouteCalculator {
 	GraphMarker<AStarMark> marker;
 	PriorityQueue<Node> queue;
 
 	private class AStarMark {
 		/** indicates that AStar is done with the vertex */
 		boolean done;
 
 		/** records the distance to the start vertex + the heuristic value */
 		double fvalue;
 
 		/** records the distance to the start vertex */
 		double distance;
 
 		/** stores the predecessor in the path from the start vertex */
 		Segment parentSegment;
 
 		AStarMark(double f, double d, Segment s) {
 			fvalue = f;
 			distance = d;
 			parentSegment = s;
 		}
 	}
 
 	public AStarRouteCalculator(OsmGraph graph) {
 		this.graph = graph;
 		marker = null;
 		relevantTypes = new TreeSet<SegmentType>();
 		setRestriction(RoutingRestriction.CAR);
 		speeds = new Speed();
 	}
 
 	@Override
 	public RoutingResult getRoute(Node target, EdgeRating r) {
 		long startTime = System.currentTimeMillis();
 
 		if (marker != null) {
 			marker.clear();
 		} else {
 			marker = new GraphMarker<AStarMark>(graph);
 		}
 
 		if (queue == null) {
 			queue = new PriorityQueue<Node>(128, new Comparator<Node>() {
 				public int compare(Node a, Node b) {
 					return Double.compare(marker.getMark(a).fvalue, marker
 							.getMark(b).fvalue);
 				}
 			});
 		} else {
 			queue.clear();
 		}
 
 		marker.mark(start, new AStarMark(calculateHeuristic(start, target, r),
 				0, null));
 		queue.offer(start);
 		System.out.println("Target = " + target);
 
 		while (!queue.isEmpty()) {
 			Node currentVertex = queue.poll();
 			AStarMark mark = marker.getMark(currentVertex);
 			mark.done = true;
 			System.out.println("v = " + currentVertex);
 
 			if (currentVertex == target) {
 				System.out.println("Target found!");
 				Stack<Segment> routesegments = new Stack<Segment>();
 				while (mark != null && mark.parentSegment != null) {
 					routesegments.push(mark.parentSegment);
 					mark = marker.getMark(mark.parentSegment.getThis());
 				}
 
 				List<Segment> out = new ArrayList<Segment>(routesegments.size());
 				while (!routesegments.empty()) {
 					out.add(routesegments.pop());
 				}
 				// printRoute(out, r);
 				return new RoutingResult(out, System.currentTimeMillis()
 						- startTime);
 			}
 
 			for (Segment currentSegment : currentVertex.getSegmentIncidences()) {
 				if (relevantTypes.contains(currentSegment.getWayType())
 						&& (currentSegment.isNormal() || !currentSegment
 								.isOneway())) {
 
 					Node nextVertex = (Node) currentSegment.getThat();
 					double newDist = mark.distance
 							+ rate(currentSegment, r, mark.parentSegment);
 					double heuristic = calculateHeuristic(nextVertex, target, r);
 					double newFValue = newDist + heuristic;
 					AStarMark n = marker.getMark(nextVertex);
 					if (n == null) {
 						marker.mark(nextVertex, new AStarMark(newFValue,
 								newDist, currentSegment));
 						queue.offer(nextVertex);
 					} else if (n.distance > newDist) {
 						n.fvalue = newFValue;
 						n.distance = newDist;
 						n.parentSegment = currentSegment;
 						if (!n.done) {
 							queue.remove(nextVertex);
 							queue.offer(nextVertex);
 						}
 					}
 				} else {
 					System.out.println("Way is not relevant.");
 				}
 			}
 		}
 		// System.out.println("Returning null route!");
 		return new RoutingResult(null, System.currentTimeMillis() - startTime);
 	}
 
 	private void printRoute(List<Segment> route, EdgeRating rating) {
 		if (route == null) {
 			System.out.println("Route is null!");
 		}
 		String label = null;
 		double length = calculateCompleteWeight(route, EdgeRating.LENGTH) / 1000.0;
 		double time = calculateCompleteWeight(route, EdgeRating.TIME) / 3600;
 		// double degree = calculator.calculateCompleteWeight(route,
 		// EdgeRating.CONVENIENCE);
 		switch (rating) {
 		case TIME:
 			label = "Fastest route";
 			break;
 		case LENGTH:
 			label = "Shortest route";
 			break;
 		case CONVENIENCE:
 			label = "Most convenient route";
 			break;
 		}
 
 		System.out.println(label);
 		if (route.size() < 1) {
 			System.out.println("not found :-(");
 			return;
 		}
 		System.out.println("  Length: " + Math.round(length * 100.0) / 100.0
 				+ "km");
 
 		long hrs = (long) Math.floor(time);
 		long min = Math.round((time - hrs) * 60.0);
 		System.out.println("  Time  : " + hrs + " h " + min + " min");
 		// resultPanel.println(" Degree : " + degree);
 		Way lastWay = null;
 		String lastName = null;
 		for (Segment s : route) {
 			Way way = getWay(s);
 			if (way != null && way != lastWay) {
 				String name = AnnotatedOsmGraph.getTag(way, "name");
 				if (name != null) {
 					name = name.trim();
 				}
 				if (name == null) {
 					name = AnnotatedOsmGraph.getTag(way, "ref");
 					if (name != null) {
 						name = name.trim();
 					}
 				}
 				if (name != null && !name.equals(lastName)) {
 					System.out.println("  " + name);
 					lastName = name;
 				}
 				lastWay = way;
 			}
 
 		}
 	}
 
 	private Way getWay(Segment s) {
 		List<? extends Way> alphaWays = ((Node) s.getAlpha()).getWayList();
 		List<? extends Way> omegaWays = ((Node) s.getOmega()).getWayList();
 		for (Way w : alphaWays) {
 			for (Way v : omegaWays) {
 				if (v == w) {
 					return w;
 				}
 			}
 		}
 		return null;
 	}
 
 	private double calculateHeuristic(Node start, Node target, EdgeRating r) {
 		if (start == target)
 			return 0;
 
		double sLat = start.getLatitude() * Math.PI / 180;
		double sLong = start.getLongitude() * Math.PI / 180;
		double tLat = target.getLatitude() * Math.PI / 180;
		double tLong = target.getLongitude() * Math.PI / 180;
		double earthRadius = 6378.3 * 1000;
 
 		double dist = Math.acos(Math.sin(sLat) * Math.sin(tLat)
				+ Math.cos(sLat) * Math.cos(tLat) * Math.cos(sLong - tLong))
				* earthRadius;
 
		// System.out.println("dist between " + start + " and target " + target
		// + " = " + dist + " meters.");
 
 		switch (r) {
 		case LENGTH:
 			return dist;
 		case TIME:
 			return dist * 3.6 / speeds.motorway;
 		case CONVENIENCE:
 			return 0;
 		default:
 			return 0;
 		}
 	}
 }
