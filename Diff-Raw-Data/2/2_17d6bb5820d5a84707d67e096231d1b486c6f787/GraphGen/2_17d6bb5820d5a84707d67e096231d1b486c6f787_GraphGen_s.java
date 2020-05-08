 package imo;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.apache.commons.collections15.Factory;
 
 import edu.uci.ics.jung.algorithms.generators.random.MixedRandomGraphGenerator;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SparseGraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.graph.util.Pair;
 
 /**
  * A class for generating Graphs, either randomly or from a file.
  *
  */
 public final class GraphGen
 {
 	private GraphGen()
 	{ //Cannot instantiate
 	}
 	
 	/**
 	 * Randomly generates a Graph within specified parameters.
 	 * @param numV Number of vertices.
 	 * @param minW Minimum edge weight, inclusive.
 	 * @param maxW Maximum edge weight, exclusive.
 	 * @return A random Graph with numV vertices and numE edges.
 	 */
 	public static Graph<Vertex, Edge> getGraph( final int numV, final int minW, final int maxW)
 	{
 		return getGraph( numV, minW, maxW, System.currentTimeMillis());
 	}
 	
 	/**
 	 * Randomly generates a Graph within specified parameters.
 	 * @param numV Number of vertices.
 	 * @param minW Minimum edge weight, inclusive.
 	 * @Param maxW Maximum edge weight, exclusive.
 	 * @param seed Seed for pseudo-random generation.
 	 * @return A random Graph with numV vertices and numE edges.
 	 */
 	public static Graph<Vertex, Edge> getGraph( final int numV, final int minW, final int maxW, final long seed)
 	{
 		final Random r = new Random(seed);
 		
 		Factory<Graph<Vertex,Edge>> gFact = new Factory<Graph<Vertex,Edge>>() {
 			@Override
 			public Graph<Vertex, Edge> create() {
 				return new SparseGraph<Vertex, Edge>();
 			}
 		};
 		
 		ArrayList<Vertex> verts = GraphGen.genVerts( numV);
 		
 		final Iterator<Vertex> vIter = verts.iterator();
 		
 		Factory<Vertex> vFact = new Factory<Vertex>() {
 			@Override
 			public Vertex create() {
 				return vIter.next();
 			}
 		};
 		Factory<Edge> eFact = new Factory<Edge>() {
 			@Override
 			public Edge create() {
 				return new Edge( r.nextInt(maxW - minW) + minW);
 			}
 		};
 		
 		Set<Vertex> vSeed = new HashSet(verts);
 		
 		Graph<Vertex, Edge> g = MixedRandomGraphGenerator.<Vertex,Edge>generateMixedRandomGraph(gFact, vFact, eFact, new HashMap<Edge,Number>(), numV, false, vSeed);
 		
 		for(Edge e : g.getEdges())
 		{
 			Pair<Vertex> pair = g.getEndpoints(e);
 			g.removeEdge(e);
 			g.addEdge(e, pair,EdgeType.DIRECTED);
 		}
 		
 		return g;
 	}
 	
 	/**
 	 * Loads a Graph from a .csv file representing an adjacency matrix with no labels.
 	 * @param file Relative filepath of the input Graph.
 	 * @return The Graph represented in the file.
 	 * @throws FileNotFoundException File Not Found
 	 */
 	public static Graph<Vertex, Edge> getGraph( String file) throws FileNotFoundException
 	{
		Scanner scan = new Scanner(new FileReader(file)).useDelimiter("\\s");
 		String first = scan.next().trim();
 		String[] fArray = first.split(",");
 		
 		int numV = fArray.length;
 		
 		int[][] mat = new int[numV][numV];
 		
 		for(int j = 0; j < numV; j++)
 		{
 			mat[0][j] = Integer.parseInt(fArray[j]);
 		}
 		
 		for(int i = 1; i < numV; i++)
 		{
 			String[] raw = null;
 			try{
 				raw = scan.next().trim().split(",");
 			} catch( NoSuchElementException e) {
 				System.out.println("File \"" + file + "\" malformed: row/column length mismatch");
 				return null;
 			}
 			for(int j = 0; j < numV; j++)
 			{
 				try{
 					mat[i][j] = Integer.parseInt(raw[j]);
 				} catch( NumberFormatException e) {
 					mat[i][j] = -1; //just ignore an invalid edge
 				}
 			}
 		}
 		
 		ArrayList<Vertex> verts = genVerts(numV);
 		
 		Graph<Vertex, Edge> g = new DirectedSparseGraph<Vertex, Edge>();
 		
 		for(Vertex v : verts)
 		{
 			g.addVertex(v);
 		}
 		
 		for(int i = 0; i < numV; i++)
 		{
 			for(int j = 0; j < numV; j++)
 			{
 				int weight = mat[i][j];
 				if( weight > 0 && i != j) //no costless, negative cost, or loop edges
 				{
 					g.addEdge(new Edge(weight), verts.get(i), verts.get(j));
 				}
 			}
 		}
 		return g;
 	}
 	
 	private static ArrayList<Vertex> genVerts( int numV)
 	{
 		ArrayList<Vertex> ret = new ArrayList<Vertex>(numV);
 		for(int i = 0; i < numV; i++)
 		{
 			String name = "";
 			for(int repeat = 0; repeat <= i / 26; repeat++)
 			{
 				name += String.valueOf((char) ((i % 26) + 97));
 			}
 			ret.add(new Vertex(name));
 		}
 		
 		return ret;
 	}
 }
