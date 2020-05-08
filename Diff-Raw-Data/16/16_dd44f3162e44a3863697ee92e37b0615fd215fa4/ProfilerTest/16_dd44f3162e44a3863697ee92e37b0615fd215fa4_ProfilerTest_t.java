 package dna.graph.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assume.assumeTrue;
 import static org.mockito.Mockito.mock;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumMap;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import dna.graph.Graph;
 import dna.graph.datastructures.DEmpty;
 import dna.graph.datastructures.DataStructure.AccessType;
 import dna.graph.datastructures.DataStructure.ListType;
 import dna.graph.datastructures.GraphDataStructure;
 import dna.graph.datastructures.IDataStructure;
 import dna.graph.datastructures.IEdgeListDatastructure;
 import dna.graph.datastructures.INodeListDatastructure;
 import dna.graph.edges.DirectedEdge;
 import dna.graph.edges.Edge;
 import dna.graph.edges.UndirectedEdge;
 import dna.graph.nodes.DirectedNode;
 import dna.graph.nodes.Node;
 import dna.graph.nodes.UndirectedNode;
 import dna.metrics.Metric;
 import dna.metrics.Metric.ApplicationType;
 import dna.metrics.Metric.MetricType;
 import dna.profiler.Profiler;
 import dna.series.data.Distribution;
 import dna.series.data.NodeNodeValueList;
 import dna.series.data.NodeValueList;
 import dna.series.data.Value;
 import dna.updates.batch.Batch;
 import dna.updates.update.Update;
 import dna.util.Config;
 
 @RunWith(Parameterized.class)
 public class ProfilerTest {
 
 	private GraphDataStructure gds;
 	private Graph graph;
 	private ApplicationType applicationType;
 	private Metric metric;
 	private String metricKey;
 
 	public ProfilerTest(
 			EnumMap<ListType, Class<? extends IDataStructure>> listTypes,
 			Class<? extends Node> nodeType, Class<? extends Edge> edgeType,
 			ApplicationType applicationType) throws InstantiationException,
 			IllegalAccessException, IllegalArgumentException,
 			InvocationTargetException, NoSuchMethodException, SecurityException {
 		this.gds = new GraphDataStructure(listTypes, nodeType, edgeType);
 		this.gds.setEdgeType(edgeType);
 		this.graph = gds.newGraphInstance("ABC", 1L, 10, 10);
 		this.applicationType = applicationType;
 		Profiler.activate();
 
 		this.graph = generateGraph();
 		metric = new TestMetric("test", this.applicationType,
 				MetricType.unknown);
 		metric.setGraph(graph);
 		this.metricKey = metric.getName();
 		if (applicationType != ApplicationType.Recomputation)
 			metricKey += Config.get("PROFILER_INITIALBATCH_KEYADDITION");
 	}
 
 	private Graph generateGraph() {
 		Graph g = new Graph("test", 1, this.gds);
 
 		Node n1 = gds.newNodeInstance(1);
 		Node n2 = gds.newNodeInstance(2);
 		g.addNode(n1);
 		g.addNode(n2);
 
 		Edge e = gds.newEdgeInstance(n1, n2);
 		e.connectToNodes();
 		g.addEdge(e);
 
		if (g.isDirected()) {
			e = gds.newEdgeInstance(n2, n1);
			e.connectToNodes();
			g.addEdge(e);
		}

 		return g;
 	}
 
 	@Before
 	public void resetProfiler() {
 		Profiler.startBatch();
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Parameterized.Parameters(name = "{0} {1} {2} {3}")
 	public static Collection<Object> testPairs() {
 		ArrayList<Object> result = new ArrayList<>();
 		Class nodeListType = null;
 		for (Class loopNodeListType : GlobalTestParameters.dataStructures) {
 			if (!(INodeListDatastructure.class
 					.isAssignableFrom(loopNodeListType)))
 				continue;
 			nodeListType = loopNodeListType;
 		}
 
 		Class edgeListType = null;
 		for (Class loopEdgeListType : GlobalTestParameters.dataStructures) {
 			if (!(IEdgeListDatastructure.class
 					.isAssignableFrom(loopEdgeListType)))
 				continue;
 			if (loopEdgeListType == DEmpty.class)
 				continue;
 			edgeListType = loopEdgeListType;
 		}
 
 		Class nodeEdgeListType = null;
 		for (Class loopNodeEdgeListType : GlobalTestParameters.dataStructures) {
 			if (!(IEdgeListDatastructure.class
 					.isAssignableFrom(loopNodeEdgeListType)))
 				continue;
 			if (loopNodeEdgeListType == DEmpty.class)
 				continue;
 			nodeEdgeListType = loopNodeEdgeListType;
 		}
 
 		for (Class nodeType : GlobalTestParameters.nodeTypes) {
 			for (Class edgeType : GlobalTestParameters.edgeTypes) {
 				if ((UndirectedEdge.class.isAssignableFrom(edgeType) && DirectedNode.class
 						.isAssignableFrom(nodeType))
 						|| (DirectedEdge.class.isAssignableFrom(edgeType) && UndirectedNode.class
 								.isAssignableFrom(nodeType)))
 					continue;
 
 				EnumMap<ListType, Class<? extends IDataStructure>> listTypes = new EnumMap<ListType, Class<? extends IDataStructure>>(
 						ListType.class);
 				listTypes.put(ListType.GlobalNodeList, nodeListType);
 				listTypes.put(ListType.GlobalEdgeList, edgeListType);
 				listTypes.put(ListType.LocalEdgeList, nodeEdgeListType);
 
 				for (ApplicationType a : ApplicationType.values()) {
 					result.add(new Object[] { listTypes, nodeType, edgeType, a });
 				}
 			}
 		}
 
 		return result;
 	}
 
 	@Test
 	public void testContainsNodeGlobalIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Contains));
 		metric.compute();
 		assertEquals(2, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Contains));
 	}
 
 	@Test
 	public void testContainsNodeLocalIsCountedInMetric() {
 		assumeTrue(graph.isDirected());
 
 		assertEquals(0, Profiler.getCount(metricKey, ListType.LocalNodeList,
 				AccessType.Contains));
 		metric.compute();
		assertEquals(2, Profiler.getCount(metricKey, ListType.LocalNodeList,
 				AccessType.Contains));
 	}
 
 	@Test
 	public void testContainsEdgeGlobalIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Contains));
 		metric.compute();
 		assertEquals(2, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Contains));
 	}
 
 	@Test
 	public void testContainsEdgeLocalIsCountedInMetric() {
 		assertEquals(
 				0,
 				Profiler.getCount(metricKey, new ListType[] {
 						ListType.LocalEdgeList, ListType.LocalInEdgeList,
 						ListType.LocalOutEdgeList }, AccessType.Contains));
 		metric.compute();
 		assertEquals(
 				1,
 				Profiler.getCount(metricKey, new ListType[] {
 						ListType.LocalEdgeList, ListType.LocalInEdgeList,
 						ListType.LocalOutEdgeList }, AccessType.Contains));
 	}
 
 	@Test
 	public void testSizeNodeGlobalIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Size));
 		metric.compute();
 		// Node size is called *three* times in the metric: once directly, once
 		// through printAll(), and once through printE()
 		assertEquals(3, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Size));
 	}
 
 	@Test
 	public void testGlobalGetRandomEdgeIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Random));
 		metric.compute();
 		assertEquals(1, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Random));
 	}
 
 	@Test
 	public void testGlobalGetSpecifiedNodeIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Get));
 		metric.compute();
 		assertEquals(1, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Get));
 	}
 
 	@Test
 	public void testSizeNodeLocalIsCountedInMetric() {
 		assumeTrue(graph.isDirected());
 
 		assertEquals(0, Profiler.getCount(metricKey, ListType.LocalNodeList,
 				AccessType.Size));
 		metric.compute();
 		assertEquals(1, Profiler.getCount(metricKey, ListType.LocalNodeList,
 				AccessType.Size));
 	}
 
 	@Test
 	public void testSizeEdgeGlobalIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Size));
 		metric.compute();
 		// Edge size is called *three* time in the metric: once directly, once
 		// through printAll(), once through printE()
 		assertEquals(3, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Size));
 	}
 
 	@Test
 	public void testSizeEdgeLocalIsCountedInMetric() {
 		assertEquals(
 				0,
 				Profiler.getCount(metricKey, new ListType[] {
 						ListType.LocalEdgeList, ListType.LocalInEdgeList,
 						ListType.LocalOutEdgeList }, AccessType.Size));
 		metric.compute();
 		// Local edge size is called *multiple* times in the metric: once
 		// directly, additional times for all nodes through printAll()
 
 		int additionalCounter = graph.getNodeCount();
 		// If the graph is directed, both in- and out-degrees are printed, so
 		// double that number
 		if (graph.isDirected())
 			additionalCounter *= 2;
 
 		assertEquals(
 				1 + additionalCounter,
 				Profiler.getCount(metricKey, new ListType[] {
 						ListType.LocalEdgeList, ListType.LocalInEdgeList,
 						ListType.LocalOutEdgeList }, AccessType.Size));
 	}
 
 	@Test
 	public void testIteratorNodeGlobalIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Iterator));
 		metric.compute();
 		assertEquals(1, Profiler.getCount(metricKey, ListType.GlobalNodeList,
 				AccessType.Iterator));
 	}
 
 	@Test
 	public void testIteratorEdgeGlobalIsCountedInMetric() {
 		assertEquals(0, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Iterator));
 		metric.compute();
 		assertEquals(2, Profiler.getCount(metricKey, ListType.GlobalEdgeList,
 				AccessType.Iterator));
 	}
 
 	private class TestMetric extends Metric {
 		public TestMetric(String name, ApplicationType type,
 				MetricType metricType) {
 			super(name, type, metricType);
 		}
 
 		@Override
 		public boolean applyBeforeBatch(Batch b) {
 			return false;
 		}
 
 		@Override
 		public boolean applyAfterBatch(Batch b) {
 			return false;
 		}
 
 		@Override
 		public boolean applyBeforeUpdate(Update u) {
 			return false;
 		}
 
 		@Override
 		public boolean applyAfterUpdate(Update u) {
 			return false;
 		}
 
 		@Override
 		public boolean compute() {
 			// This will yield a count for Contains, even if a mocked node
 			// cannot be found in the list
 			g.containsNode(mock(Node.class));
 
 			// This will yield a count for Contains, as the node was surely
 			// added
 			Node n = gds.newNodeInstance(1);
 			g.containsNode(n);
 
 			Edge eNotInList = gds.newEdgeInstance(n, n);
 			g.containsEdge(eNotInList);
 
 			Node n1 = g.getNode(1);
 			Edge e = g.getRandomEdge();
 
 			if (n1 instanceof DirectedNode) {
 				DirectedNode dn1 = (DirectedNode) n1;
 				dn1.hasNeighbor(dn1);
 
 				DirectedNode dn2 = (DirectedNode) gds.newNodeInstance(2);
 				dn1.hasNeighbor(dn2);
 			}
 
 			g.containsEdge(e);
 			n1.hasEdge(e);
 
 			g.getNodeCount();
 			g.getEdgeCount();
 
 			// Mute System.out here!
 			PrintStream former = System.out;
 			System.setOut(new PrintStream(new OutputStream() {
 				public void write(int b) throws IOException {
 				}
 			}));
 
 			// Run the interesting stuff
 			g.printAll();
 
 			// Another usecase for the edge iterator
 			g.printE();
 
 			System.setOut(former);
 
 			if (n1 instanceof DirectedNode) {
 				DirectedNode dn1 = (DirectedNode) n1;
 				dn1.getInDegree();
 				dn1.getNeighborCount();
 			}
 			if (n1 instanceof UndirectedNode) {
 				UndirectedNode un1 = (UndirectedNode) n1;
 				un1.getDegree();
 			}
 
 			return false;
 		}
 
 		@Override
 		public void init_() {
 		}
 
 		@Override
 		public void reset_() {
 		}
 
 		@Override
 		public Value[] getValues() {
 			return null;
 		}
 
 		@Override
 		public Distribution[] getDistributions() {
 			return null;
 		}
 
 		@Override
 		public NodeValueList[] getNodeValueLists() {
 			return null;
 		}
 
 		@Override
 		public boolean equals(Metric m) {
 			return false;
 		}
 
 		@Override
 		public boolean isApplicable(Graph g) {
 			return false;
 		}
 
 		@Override
 		public boolean isApplicable(Batch b) {
 			return false;
 		}
 
 		@Override
 		public boolean isComparableTo(Metric m) {
 			return false;
 		}
 
 		@Override
 		public NodeNodeValueList[] getNodeNodeValueLists() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 	}
 
 }
