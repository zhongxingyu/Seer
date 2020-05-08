 package dna.graph.tests;
 
 import com.google.common.collect.ObjectArrays;
 
 import dna.graph.datastructures.DArray;
 import dna.graph.datastructures.DArrayList;
 import dna.graph.datastructures.DHashMap;
 import dna.graph.datastructures.DHashSet;
 import dna.graph.datastructures.DLinkedList;
 import dna.graph.edges.DirectedDoubleWeightedEdge;
 import dna.graph.edges.DirectedEdge;
 import dna.graph.edges.UndirectedDoubleWeightedEdge;
 import dna.graph.edges.UndirectedEdge;
 import dna.graph.generators.CliqueGenerator;
 import dna.graph.generators.RandomGraphGenerator;
 import dna.graph.generators.RingGenerator;
 import dna.graph.nodes.DirectedDoubleWeightedNode;
 import dna.graph.nodes.DirectedNode;
 import dna.graph.nodes.UndirectedDoubleWeightedNode;
 import dna.graph.nodes.UndirectedNode;
 import dna.updates.directed.RandomDirectedBatch;
 import dna.updates.undirected.RandomUndirectedBatch;
 
 public class GlobalTestParameters {
 	public static final Class[] nodeTypes = { UndirectedNode.class,
 			UndirectedDoubleWeightedNode.class, DirectedNode.class,
 			DirectedDoubleWeightedNode.class };
 
 	public static final Class[] edgeTypes = { UndirectedEdge.class,
 			UndirectedDoubleWeightedEdge.class, DirectedEdge.class,
 			DirectedDoubleWeightedEdge.class };
 
 	public static final Class[] elementClasses = ObjectArrays.concat(nodeTypes,
 			edgeTypes, Class.class);
 
 	public static final Class[] dataStructures = { DArray.class,
 			DArrayList.class, DHashMap.class, DHashSet.class, DLinkedList.class };
 
 	public static final Class[] graphGenerators = { RandomGraphGenerator.class,
 			CliqueGenerator.class, RingGenerator.class };
 
 	public static final Class[] batchGenerators = { RandomDirectedBatch.class,
 			RandomUndirectedBatch.class };
 }
