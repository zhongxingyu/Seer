 package dna.tests;
 
 import static org.junit.Assert.*;
 import static org.junit.Assume.*;
 import static org.mockito.Mockito.*;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.rules.TemporaryFolder;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import dna.datastructures.*;
 import dna.factories.GraphGenerator;
 import dna.factories.IGraphGenerator;
 import dna.factories.IRandomGenerator;
 import dna.factories.RandomDirectedGraphGenerator;
 import dna.factories.RandomUndirectedDoubleWeightedGraphGenerator;
 import dna.graph.Graph;
 import dna.graph.edges.Edge;
 import dna.graph.nodes.DirectedDoubleWeightedNode;
 import dna.graph.nodes.DirectedNode;
 import dna.graph.nodes.Node;
 import dna.graph.nodes.UndirectedDoubleWeightedNode;
 import dna.graph.nodes.UndirectedNode;
 import dna.io.GraphReader;
 import dna.io.GraphWriter;
 import dna.util.parameters.Parameter;
 
 @RunWith(Parameterized.class)
 public class GeneratorsTest {
 	private Class<? extends Node> nodeType;
 	private Class<? extends IGraphGenerator> generator;
 	private Constructor<? extends GraphGenerator> generatorConstructor;
 	private GraphDataStructure gds;
 	private GraphGenerator gg;
 
 	private final int nodeSize = 200;
 	private final int edgeSize = 250;
 
 	@Rule
 	public TemporaryFolder folder = new TemporaryFolder();
 	
 	@Rule
 	public ExpectedException exception = ExpectedException.none();
 
 	public GeneratorsTest(Class<? extends INodeListDatastructure> nodeListType,
 			Class<? extends IEdgeListDatastructure> graphEdgeListType,
 			Class<? extends IEdgeListDatastructure> nodeEdgeListType, Class<? extends Node> nodeType,
 			Class<? extends GraphGenerator> generator) throws InstantiationException, IllegalAccessException,
 			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
 		this.nodeType = nodeType;
 		this.generator = generator;
 		this.generatorConstructor = generator.getConstructor(String.class, Parameter[].class, GraphDataStructure.class,
 				long.class, int.class, int.class);
 
 		this.gds = new GraphDataStructure(nodeListType, graphEdgeListType, nodeEdgeListType, nodeType);
 		this.gg = this.generatorConstructor.newInstance("ABC", new Parameter[] {}, gds, 0, nodeSize, edgeSize);
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Parameterized.Parameters(name = "{0} {1} {2} {3} {4}")
 	public static Collection testPairs() {
 		Class[] dataStructures = { DArrayList.class, DArray.class, DHashMap.class, DHashSet.class, DLinkedList.class };
 		Class[] graphGenerators = { RandomDirectedGraphGenerator.class,
 				RandomUndirectedDoubleWeightedGraphGenerator.class };
 		Class[] nodeTypes = { UndirectedNode.class, UndirectedDoubleWeightedNode.class, DirectedNode.class,
 				DirectedDoubleWeightedNode.class };
 
 		ArrayList<Object> result = new ArrayList<>();
 		for (Class nodeListType : dataStructures) {
 			for (Class edgeListType : dataStructures) {
 				for (Class nodeEdgeListType : dataStructures) {
 					for (Class generator : graphGenerators) {
 						for (Class nodeType : nodeTypes) {
 							if (!(INodeListDatastructureReadable.class.isAssignableFrom(nodeListType)))
 								continue;
 							if (!(IEdgeListDatastructureReadable.class.isAssignableFrom(edgeListType)))
 								continue;
 							if (!(IEdgeListDatastructureReadable.class.isAssignableFrom(nodeEdgeListType)))
 								continue;
 
 							result.add(new Object[] { nodeListType, edgeListType, nodeEdgeListType, nodeType, generator });
 						}
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	@Test
 	public void testWrongNodeType() {
 		assumeFalse(gg.canGenerateNodeType(nodeType));
 		exception.expect(RuntimeException.class);
 		Graph g = gg.generate();
 	}
 
 	@Test
 	public void testGraphGeneration() {
 		assumeTrue(gg.canGenerateNodeType(nodeType));
 		Graph g = gg.generate();
 
 		assertEquals(nodeSize, g.getNodeCount());
 		assertEquals(edgeSize, g.getEdgeCount());
 	}
 
 	@Test
 	public void testWriteRead() throws ClassNotFoundException, IOException {
 		assumeTrue(gg.canGenerateNodeType(nodeType));
 		Graph g = gg.generate();
 
 		String graphName = gds.getDataStructures();
 
 		String tempFolder = folder.getRoot().getAbsolutePath();
 
 		GraphWriter gw = new GraphWriter();
 		gw.write(g, tempFolder, graphName);
 
 		GraphReader gr = new GraphReader();
 		Graph g2 = gr.read(tempFolder, graphName, null);
 
 		assertEquals(gds, g2.getGraphDatastructures());
 		assertEquals(g, g2);
 	}
 
 	@Test
 	public void testRandomGraphsAreRandom() {
 		assumeTrue(IRandomGenerator.class.isAssignableFrom(generator));
 		assumeTrue(gg.canGenerateNodeType(nodeType));
 		Graph g = gg.generate();
 
 		for (int i = 0; i < 20; i++) {
 			Graph g2 = gg.generate();
 			assertNotEquals(g, g2);
 		}
 	}
 
 	@Test
 	public void testWriteReadWithErrorInEdge() throws ClassNotFoundException, IOException {
 		assumeTrue(gg.canGenerateNodeType(nodeType));
 		Graph g = gg.generate();
 
 		String graphName = gds.getDataStructures();
 
 		String tempFolder = folder.getRoot().getAbsolutePath();
 
 		GraphWriter gw = new GraphWriter();
 		gw.write(g, tempFolder, graphName);
 
 		GraphReader gr = new GraphReader();
 		Graph g2 = gr.read(tempFolder, graphName, null);
 
 		assertEquals(g, g2);
 
 		// Change getStringRepresentation now to see that it is used for
 		// equality checks
 		for (int i = 0; i < Math.floor(edgeSize / 5); i++) {
 			Edge edgeReal = g.getRandomEdge();
 			assertNotNull(edgeReal);
 			g.removeEdge(edgeReal);
 			Edge edgeMocked = mock(this.gds.getEdgeType());
 			when(edgeMocked.getStringRepresentation()).thenReturn("");
 			g.addEdge(edgeMocked);
 			assertNotEquals(g, g2);
 		}
 	}
 
 	@Test
 	public void testWriteReadWithErrorInNode() throws ClassNotFoundException, IOException {
 		assumeTrue(gg.canGenerateNodeType(nodeType));
 		Graph g = gg.generate();
 
 		String graphName = gds.getDataStructures();
 
 		String tempFolder = folder.getRoot().getAbsolutePath();
 
 		GraphWriter gw = new GraphWriter();
 		gw.write(g, tempFolder, graphName);
 
 		GraphReader gr = new GraphReader();
 		Graph g2 = gr.read(tempFolder, graphName, null);
 
 		assertEquals(g, g2);
 
 		// Change getStringRepresentation now to see that it is used for
 		// equality checks
 		Node nodeReal = g.getNode(g.getNodeCount() - 1);
 		assertNotNull(nodeReal);
 		g.removeNode(nodeReal);
 		Node nodeMocked = mock(this.nodeType);
 		when(nodeMocked.getStringRepresentation()).thenReturn("");
 		g.addNode(nodeMocked);
 		assertNotEquals(g, g2);
 	}
 
 }
