 package de.uni_koblenz.jgstreetmaptest.nonjunit;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.GraphIOException;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
 import de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths.DijkstraAlgorithm;
 import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
 import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
 import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToBooleanFunctionAdapter;
 import de.uni_koblenz.jgralab.algolib.functions.adapters.MethodCallToDoubleFunctionAdapter;
 import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorAdapter;
 import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;
 import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
 import de.uni_koblenz.jgralabtest.algolib.nonjunit.Stopwatch;
 import de.uni_koblenz.jgstreetmap.osmschema.OsmGraph;
 import de.uni_koblenz.jgstreetmap.osmschema.OsmSchema;
 import de.uni_koblenz.jgstreetmap.osmschema.map.Node;
 import de.uni_koblenz.jgstreetmap.osmschema.routing.Segment;
 import de.uni_koblenz.jgstreetmap.osmschema.routing.SegmentType;
 import de.uni_koblenz.jgstreetmap.routing.DijkstraRouteCalculator;
 import de.uni_koblenz.jgstreetmap.routing.RouteCalculator.EdgeRating;
 import de.uni_koblenz.jgstreetmap.routing.RoutingResult;
 
 public class TryDijkstra {
 
 	private static final int RUNS = 150;
 	private static final int MAX_VISITORS = 15;
 	private static final int IGNORE_LEFT = Math.round(RUNS * 0.05f);
 	private static final int IGNORE_RIGHT = Math.round(RUNS / 5f);
 
 	public static void main(String[] args) throws GraphIOException {
 		OsmGraph graph = OsmSchema.instance().loadOsmGraph("OsmGraph.tg.gz",
 				new ConsoleProgressFunction());
 
 		int nodes = 0;
 		for (Vertex v : graph.vertices()) {
 			nodes += v instanceof Node ? 1 : 0;
 		}
 		System.out.println("Vertices: " + graph.getVCount());
 		System.out.println("Nodes: " + nodes);
 
 		int segments = 0;
 		int rsegments = 0;
 		BitSetVertexMarker relevantNodes = new BitSetVertexMarker(graph);
 		for (Edge e : graph.edges()) {
 			segments += e instanceof Segment ? 1 : 0;
 			if (subgraph.get(e) && navigable.get(e)) {
 				rsegments++;
 				relevantNodes.mark(e.getThis());
 				relevantNodes.mark(e.getThat());
 			}
 
 		}
 		System.out.println("Edges: " + graph.getECount());
 		System.out.println("Segments: " + segments);
 		System.out.println();
 		System.out.println("relevant Segments: " + rsegments);
 		System.out.println();
 		int rnodes = relevantNodes.size();
 		System.out.println("Relevant Nodes: " + rnodes);
 		System.out.println();
 		System.out.println("Nodes and segments: " + (nodes + segments));
 		System.out.println("Relevant graph elements: " + (rnodes + rsegments));
 		System.out.println();
 
 		Node start = (Node) graph.getVertex(24175);
 		Node target = (Node) graph.getVertex(46927);
 		System.out.println("Start node set to " + start.get_osmId());
 		System.out.println("Using internal Dijkstra implementation...");
 		Stopwatch sw = new Stopwatch();
 
 		long[] average1 = new long[RUNS];
 		for (int i = 0; i < RUNS; i++) {
 			average1[i] = runOnInternalImplementation(graph, start, target, sw,
 					i == RUNS - 1);
 		}
 		System.out.println();
 		System.out.println("Done.");
 
 		long[][] average2 = new long[MAX_VISITORS + 1][RUNS];
 		for (int visitors = 0; visitors <= MAX_VISITORS; visitors++) {
 			System.out.println("Using algolib implementation with " + visitors
 					+ " empty visitors...");
 			for (int i = 0; i < RUNS; i++) {
 				average2[visitors][i] = runOnAlgolibImplementation(graph,
 						start, target, sw, visitors, i == RUNS - 1);
 			}
 			System.out.println();
 			System.out.println("Done.");
 		}
 
 		System.out
 				.println("Using modified internal Dijkstra implementation...");
 		long[] average3 = new long[RUNS];
 		for (int i = 0; i < RUNS; i++) {
 			average3[i] = runOnModifiedInternalImplementation(graph, start,
 					target, sw, i == RUNS - 1);
 		}
 		System.out.println();
 		System.out.println("Done.");
 
 		System.out.println("Done.");
 		System.out.println();
 		System.out.println("Results for internal Dijkstra:");
 		printResult(average1);
 
 		for (int visitors = 0; visitors <= MAX_VISITORS; visitors++) {
 			System.out.println();
 			System.out.println("Results for algolib implementation with "
 					+ visitors + " empty visitors:");
 			printResult(average2[visitors]);
 		}
 
 		System.out.println();
 		System.out.println("Results for modified internal Dijkstra:");
 		printResult(average3);
 
 		System.out.println("Fini.");
 	}
 
 	private static long runOnInternalImplementation(OsmGraph graph, Node start,
 			Node target, Stopwatch sw, boolean printResult) {
 		sw.reset();
 		DijkstraRouteCalculator routing1 = new DijkstraRouteCalculator(graph);
 
 		routing1.setStart(start);
 		sw.start();
 		// RoutingResult result = routing1.getRoute(target, EdgeRating.LENGTH);
 		routing1.calculateShortestRoutes(EdgeRating.LENGTH);
 		sw.stop();
 		if (printResult) {
 			System.out.println();
 			System.out.println("Last run:");
 			RoutingResult result = routing1.getRoute(target, EdgeRating.LENGTH);
 			long duration = sw.getDuration();
 			List<Segment> route = result.getRoute();
 			double length = 0.0;
 			for (Segment current : route) {
 				length += current.get_length();
 				// System.out.println(current.getId());
 			}
 			System.out.println("Result length: " + length + " m.");
 			System.out.println("Duration: " + duration + " msecs.");
 		} else {
 			System.out.print(".");
 			System.out.flush();
 		}
 		return sw.getNanoDuration();
 	}
 
 	private static long runOnModifiedInternalImplementation(OsmGraph graph,
 			Node start, Node target, Stopwatch sw, boolean printResult) {
 		sw.reset();
 		ModifiedDijkstraRouteCalculator2 routing1 = new ModifiedDijkstraRouteCalculator2(
 				graph);
 
 		routing1.setStart(start);
 		sw.start();
 		// RoutingResult result = routing1.getRoute(target, EdgeRating.LENGTH);
 		routing1.calculateShortestRoutes(EdgeRating.LENGTH);
 		sw.stop();
 		if (printResult) {
 			System.out.println();
 			System.out.println("Last run:");
 			RoutingResult result = routing1.getRoute(target, EdgeRating.LENGTH);
 			long duration = sw.getDuration();
 			List<Segment> route = result.getRoute();
 			double length = 0.0;
 			for (Segment current : route) {
 				length += current.get_length();
 				// System.out.println(current.getId());
 			}
 			System.out.println("Result length: " + length + " m.");
 			System.out.println("Duration: " + duration + " msecs.");
 		} else {
 			System.out.print(".");
 			System.out.flush();
 		}
 		return sw.getNanoDuration();
 	}
 
 	private static BooleanFunction<GraphElement> subgraph = new MethodCallToBooleanFunctionAdapter<GraphElement>() {
 
 		@Override
 		public boolean get(GraphElement parameter) {
			return parameter.getSchemaClass() == Segment.class
					|| parameter.getSchemaClass() == Node.class;
 		}
 
 		@Override
 		public boolean isDefined(GraphElement parameter) {
 			return true;
 		}
 	};
 
 	private static BooleanFunction<Edge> navigable = new MethodCallToBooleanFunctionAdapter<Edge>() {
 
 		private Set<SegmentType> relevantTypes;
 		{
 			relevantTypes = new HashSet<SegmentType>();
 			relevantTypes.add(SegmentType.MOTORWAY);
 			relevantTypes.add(SegmentType.PRIMARY);
 			relevantTypes.add(SegmentType.SECONDARY);
 			relevantTypes.add(SegmentType.TERTIARY);
 			relevantTypes.add(SegmentType.RESIDENTIAL);
 			relevantTypes.add(SegmentType.WORMHOLE);
 			relevantTypes.add(SegmentType.SERVICE);
 			relevantTypes.add(SegmentType.UNSURFACED);
 		}
 
 		@Override
 		public boolean get(Edge parameter) {
 			Segment currentSegment = (Segment) parameter;
 			return relevantTypes.contains(currentSegment.get_wayType())
 					&& (currentSegment.isNormal() || !currentSegment
 							.is_oneway());
 		}
 
 		@Override
 		public boolean isDefined(Edge parameter) {
 			return true;
 		}
 	};
 
 	private static DoubleFunction<Edge> edgeWeight = new MethodCallToDoubleFunctionAdapter<Edge>() {
 
 		@Override
 		public double get(Edge parameter) {
 			return ((Segment) parameter).get_length();
 		}
 
 		@Override
 		public boolean isDefined(Edge parameter) {
 			return true;
 		}
 
 	};
 
 	private static long runOnAlgolibImplementation(OsmGraph graph, Node start,
 			Node target, Stopwatch sw, int visitors, boolean printResult) {
 		sw.reset();
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph, navigable,
				edgeWeight).undirected();
 		for (int i = 0; i < visitors; i++) {
 			// System.out.println("Adding visitor...");
 			dijkstra.addVisitor(new GraphVisitorAdapter());
 		}
 
 		sw.start();
 		try {
 			dijkstra.execute(start);
 		} catch (AlgorithmTerminatedException e) {
 			e.printStackTrace();
 		}
 		sw.stop();
 		if (printResult) {
 			System.out.println();
 			System.out.println("Last run:");
 			long duration = sw.getDuration();
 			double length = dijkstra.getDistance().get(target);
 			System.out.println("Result length: " + length + " m.");
 			System.out.println("Duration: " + duration + " msecs.");
 		} else {
 			System.out.print(".");
 			System.out.flush();
 		}
 		return sw.getNanoDuration();
 	}
 
 	private static void printResult(long[] average) {
 		Arrays.sort(average);
 		long sum = 0;
 		for (int i = 0 + IGNORE_LEFT; i < average.length - IGNORE_RIGHT; i++) {
 			sum += average[i];
 		}
 		System.out
 				.println("Average: "
 						+ (sum / ((RUNS - IGNORE_LEFT - IGNORE_RIGHT) * 1000.0 * 1000.0))
 						+ " msec");
 	}
 }
