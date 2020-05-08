 package com.max.algs.graph;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeMap;
 
 import com.max.algs.it.SubsetIterator;
 
 
 
 /**
  * Undirected unweighted graph represented as an adjacency list
  *
  */
 public class Graph<T> {
 
 	
 	private static final Random RAND = new Random();
 	
 	
 	private final Map<T, Map<T, Edge<T>>> vertexes = new TreeMap<>();
 	
 	private int edgesCount;
 		
 	private boolean directed;
 	
 	public static <T> Graph<T> createGraph(){
 		return new Graph<T>( false );
 	}
 	
 	public static <T> Graph<T> createDirectedGraph(){
 		return new Graph<T>( true );
 	}
 	
 	
 	public List<Edge<T>> getAllEdges(){
 		List<Edge<T>> edges = new ArrayList<>();
 		
 		for( Map.Entry<T, Map<T, Edge<T>>> entry : vertexes.entrySet() ){
 			edges.addAll( entry.getValue().values() );
 		}
 		
 		return edges;
 	}
 	
 	
  	public static Graph<String> createChessboardGraph( int boardSize ){
 		
 		Graph<String> graph = Graph.createGraph();		
 		
 		for( int row = 0;  row < boardSize; row++ ){
 			
 			for( int col = 0; col < boardSize; col++ ){
 				
 				String baseVer = row + "_" + col;				
 				
 				graph.addVertex( baseVer );
 				
 				if( row-1 >=0 && col-1 >= 0 ){
 					graph.addEdge( baseVer, (row-1) + "_" + (col-1) );
 				}	
 				
 				if( row-1 >= 0 && col+1 < boardSize ){
 					graph.addEdge( baseVer, (row-1) + "_" + (col+1) );
 				}
 			}
 		}
 		
 		return graph;
 	}
 	
  	public boolean isEmpty(){
  		return vertexes.isEmpty();
  	}
  	
 
  	
  	public boolean hasCycle(){
  		
  		if( isEmpty() ){
  			return false;
  		}
  		
  		
  		T firstVertex = vertexes.entrySet().iterator().next().getKey();
  		
  		if( !directed ){
  			
  			// detect cycle in undirected graph using DFS 			
  			Map<T, T> parents = new HashMap<>();
  			Set<T> visited = new HashSet<>();
  			Set<T> marked = new HashSet<>();
  			
  			marked.add( firstVertex );
  			
  			Deque<T> queue = new ArrayDeque<>();
  			queue.add( firstVertex );
  			
  			while( ! queue.isEmpty() ){
  				T vertex = queue.poll();
  				
  				Set<T> neighbours = getAdjVertexes(vertex);
  				
  				for( T singleNeighbour : neighbours ){ 	
  					
  					if( visited.contains(singleNeighbour) ){ 
  						
  						T curParent = parents.get(vertex); 						
  						if( curParent != singleNeighbour ){
  							return true;
  						}
  					}					
  					
  					else if( ! marked.contains(singleNeighbour) ){
  						queue.add( singleNeighbour );
  						marked.add( singleNeighbour ); 						
  						parents.put( singleNeighbour, vertex );
  					}
  				}
  				
  				marked.remove( vertex );
  				visited.add( vertex ); 				
  			}  			
  			
  		}
  		
  		return false;
  	}
  	
  	
  	public List<T> vertexCoverageApproximation(){
  		
  		Set<Edge<T>> deletedEdges = new HashSet<>();
  		
  		List<T> coverage = new ArrayList<>();
  		
  		for( Map.Entry<T, Map<T, Edge<T>>> entry : vertexes.entrySet() ){
  			
  			for( Map.Entry<T, Edge<T>> edgeEntry : entry.getValue().entrySet() ){
  				
  				if( ! deletedEdges.contains(edgeEntry.getValue()) ){
  					
  					T vertex1 = entry.getKey(); 
  					T vertex2 = edgeEntry.getKey();
  					
  					coverage.add( vertex1 );
  					coverage.add( vertex2 );
  					
  					markAllIncidentEdgesAsDeleted( vertex1, deletedEdges );
  					markAllIncidentEdgesAsDeleted( vertex2, deletedEdges ); 					
  					break;					
  				}
  			}
  			
  			
  		}
  		
  		
  		return coverage;
  	}
  	
  	private void markAllIncidentEdgesAsDeleted(T vertex, Set<Edge<T>> deletedEdges){
  		
  		for( Map.Entry<T, Edge<T>> edgeEntry : vertexes.get(vertex).entrySet() ){
  			
  			deletedEdges.add( edgeEntry.getValue() );
  			
  			for(  Map.Entry<T, Edge<T>> otherSideEdgeEntry : vertexes.get(edgeEntry.getKey()).entrySet() ){
  				if( otherSideEdgeEntry.getKey().equals(vertex) ){
  					deletedEdges.add( otherSideEdgeEntry.getValue() );
  					break;
  				}
  			}
  			
  		}
  		
  	}
  	
  	
  	/**
  	 * Get all minimal vertex coverage using brute force algorithm.
  	 */
  	public List<T> vertexCoverageBruteforce(){
  		
  		Set<T> allVertexes = vertexes.keySet();
  		 		
  		SubsetIterator<T> subsetIt = new SubsetIterator<>(allVertexes);
  		
  		List<T> min = null;
  		
  		while( subsetIt.hasNext() ){
  			T[] subset = subsetIt.next();
  			
  			if( isVertexCoverage(subset) ){ 
  				
  				if( min == null || subset.length < min.size() ){
  					min = Arrays.asList( subset );
  				} 				
  			}
  		}
  		
  		
  		return min;
  	}
  	
  	private boolean isVertexCoverage(T[] set){
  		
  		Set<Edge<T>> coveredEdges = new HashSet<>(); 		
  		
  		for( T vertex : set ){
  			
  			Collection<Edge<T>> edges = vertexes.get(vertex).values();
  			
  			for( Edge<T> singleEdge : edges ){
  				
  				coveredEdges.add( singleEdge );
  				
  				T destVertex = singleEdge.dest;
  				
  				coveredEdges.add( vertexes.get(destVertex).get(vertex) );
  				
 // 				for( Edge<T> destEdge : vertexes.get(destVertex) ){
 // 					if( destEdge.vertex.equals(vertex) ){
 // 						coveredEdges.add( destEdge );
 // 						break;
 // 					}
 // 				} 				
  			} 			
  		}
  		
  		return coveredEdges.size() == edgesCount;
  	}
  	
  	
 
 
 	/**
 	 * 
 	 * @param vertexesCount
 	 * @param edgeProbability - possible values  0% - 100%
 	 * @return
 	 */
 	@SuppressWarnings("unused")
 	public static Graph<Integer> createRandomGraph( int vertexesCount, int edgeProbability ){
 				
 		if( edgeProbability < 0 || edgeProbability > 100 ){
 			throw new IllegalArgumentException("'edgeProbability' is incorrect: " + edgeProbability + ", should be in range [1, 100]");
 		}		
 		
 		Graph<Integer> graph =  Graph.createGraph();
 				
 		int skippedCount = 0;
 		int addedCount = 0;
 		int randValue;
 		
 		for( int ver1 = 0; ver1 < vertexesCount-1; ver1++ ){
 			
 			//graph.addVertex( ver1 );
 			
 			for( int ver2 = ver1+1; ver2 < vertexesCount; ver2++ ){
 				
 				randValue = RAND.nextInt(100);
 			
 				if( randValue < edgeProbability ){
 					graph.addEdge(ver1, ver2);
 					++addedCount;
 				}
 				else {
 					++skippedCount;
 				}				
 			}
 		}
 		
 //		System.out.println( "Actual probability: " + ((double)addedCount) / (double)(addedCount+skippedCount) * 100.0 );
 		
 		return graph;		
 	}
 	
 	
 	
 	public List<T> bfs(){
 		
 		if( isEmpty() ){
 			return new ArrayList<>(); 
 		}
 		
 		List<T> bfsPath = new ArrayList<>();
 		
 		T firstVertex = vertexes.keySet().iterator().next();
 		
 		Deque<T>deque = new ArrayDeque<>();
 		deque.add( firstVertex );
 		
 		Set<T> handled = new HashSet<>();
 		Set<T> marked = new HashSet<>();
 		marked.add( firstVertex );
 		
 		
 		while( ! deque.isEmpty() ){
 			T vertex = deque.poll();
 			
 			bfsPath.add( vertex );
 			
 			Collection<Edge<T>> edges = vertexes.get(vertex).values();
 			
 			for( Edge<T> edge : edges ){
 				if( ! marked.contains(edge.dest) && ! handled.contains(edge.dest) ){
 					deque.add( edge.dest );
 					marked.add( edge.dest );
 				}
 			}
 			
 			
 			marked.remove( firstVertex );
 			handled.add( firstVertex );			
 		}
 		
 		
 		return bfsPath;
 	}
 
 	
 	public Map<T, Integer> bellmanFordShortestPath(){
 		
 		Map<T, Integer> distance = new TreeMap<>();		
 		
 		Iterator<T> vertexIt = vertexes.keySet().iterator();
 		
 		T srcVertex = vertexIt.next();		
 		
 		distance.put(srcVertex, 0);
 		
 		while( vertexIt.hasNext() ){
 			distance.put(vertexIt.next(), Integer.MAX_VALUE);
 		}	
 		
 		// calculate relaxation for each vertex, edge times
 		
 		for( int i =0; i < vertexes.size()-1; i++ ){			
 			for( Map<T, Edge<T>> edgeEntry : vertexes.values() ){
 				
 				for( Edge<T> edge : edgeEntry.values() ){
 					int uDist = distance.get( edge.src );
 					int newDistance =  uDist + edge.weight;
 					
 					int vDist = distance.get( edge.dest );
 					
 					if( newDistance < vDist ){
 						distance.put(edge.dest, newDistance);
 					}				
 				}
 			}			
 		}
 		
 		// check for negative cycles		
 		for( Map<T, Edge<T>> edgeEntry : vertexes.values() ){			
 			for( Edge<T> edge : edgeEntry.values() ){
 				
 				int uDist = distance.get( edge.src );
 				int vDist = distance.get( edge.dest );
 				
 				if( vDist > uDist + edge.weight ){
 					throw new IllegalStateException("Negative cycle detected in graph");
 				}
 			}
 		}		
 
 		
 		return distance;
 	}
 	
 	
 	private Graph( boolean directed ){	
 		super();
 		this.directed = directed;
 	}
 	
 	
 	public List<T> getVertexes(){
 		return new ArrayList<>( vertexes.keySet() ); 
 	}
 	
 	/**
 	 * Do BFS and find path.
 	 */
 	public List<T> shortestPath(T src, T dest){		
 		
 		if( ! vertexes.containsKey(src) || ! vertexes.containsKey(dest) ){
 			return new ArrayList<>();
 		}	
 		
 		Set<T> visited = new HashSet<>();
 		Set<T> marked = new HashSet<>();
 		
 		Map<T, T> parent = new HashMap<>();
 		
 		Deque<T> queue = new ArrayDeque<>();		
 		queue.add( src );
 		marked.add( src );
 		
 		while( ! queue.isEmpty() ){
 			
 			T ver = queue.poll();
 			
 			if( ver.equals(dest) ){
 				return reconstructPath(src, dest, parent);
 			}
 			
 			visited.add( ver );
 			marked.remove( ver );
 			
 			Collection<Edge<T>> adjVertexes = vertexes.get(ver).values();
 			
 			for( Edge<T> adj : adjVertexes ){
 				if( ! visited.contains(adj) && ! marked.contains(adj) ){
 					queue.add( adj.dest );
 					marked.add( adj.dest );	
 					
 					parent.put( adj.dest, ver );
 				}
 			}
 			
 		}
 		
 		return new ArrayList<>();
 	}
 	
 	
 	private List<T> reconstructPath( T src, T dest, Map<T, T> parent ) {
 		
 		
 		T cur = dest;		
 		List<T> path = new ArrayList<>();
 		
 		while( ! cur.equals(src) ){			
 			path.add( cur );			
 			cur = parent.get(cur);			
 		}
 		
 		path.add( src );		
 		
 		Collections.reverse( path );
 		
 		return path;
 	}
 
 
 	public Set<T> getAdjVertexes( T ver ){
 		
 		Collection<Edge<T>> neighbourEdges = vertexes.get(ver).values();
 		
 		if( neighbourEdges == null ){
 			throw new IllegalArgumentException("Vertex not found: '" + String.valueOf(ver) + "'");
 		}
 		
 		Set<T> nighbour = new HashSet<>();
 		
 		for( Edge<T> edge : neighbourEdges ){
 			nighbour.add( edge.dest );
 		}		
 		
 		return nighbour;
 	}
 	
 	
 	public boolean addVertex( T ver ){		
 		return vertexes.put(ver, new TreeMap<T, Edge<T>>()) == null;		
 	}
 	
 	public boolean addEdge( T ver1, T ver2, int weight ){	
 		Map<T, Edge<T>> neighbour1 = vertexes.get(ver1);				
 		
 		if( neighbour1 == null ){
 			neighbour1 = new TreeMap<>();
 			vertexes.put( ver1, neighbour1 );		
 		}	
 		
 		Map<T, Edge<T>> neighbour2 = vertexes.get(ver2);
 		
 		if( neighbour2 == null ){
 			neighbour2  = new TreeMap<>(); 
 			vertexes.put( ver2, neighbour2 );
 		}
 		
 		if( ! directed ){
 			neighbour2.put( ver1, new Edge<>(ver2, ver1, weight) );
 			++edgesCount;
 		}
 		
 		
 		++edgesCount;
 		
 		neighbour1.put( ver2, new Edge<>(ver1, ver2, weight) );			
 		
 		return true;
 	}
 	
 	public boolean addEdge( T ver1, T ver2 ){		
 		return addEdge(ver1, ver2, 0);
 	}
 	
 	
 	public boolean hasEdge( T ver1, T ver2 ){	
 		
 		Map<T, Edge<T>> neighbour = vertexes.get(ver1);
 		
 		if( neighbour == null ){
 			return false;
 		}
 		
 		return neighbour.containsKey(ver2);
 	}
 
 	
 	public Map<T, Integer> colorGraph(){
 		
 		final int lastColorIndex = vertexes.size();
 		
 		Map<T, Integer> vertexColors = new HashMap<>();
 		
 		// handle each connected component
 		for( Map.Entry<T, Map<T, Edge<T>>> vertexEntry : vertexes.entrySet() ){
 			
 			T firstVertex = vertexEntry.getKey();
 			
 			// separate connected component found
 			if( ! vertexColors.containsKey(firstVertex) ){
 				
 				Set<T> marked = new HashSet<>();		
 				
 				Queue<T> queue = new ArrayDeque<>();
 				queue.add( firstVertex );
 				
 				BitSet colorsInUse = new BitSet( lastColorIndex);
 				
 				while( ! queue.isEmpty() ){
 					
 					T curVertex = queue.poll();
 					colorsInUse.clear( 0, lastColorIndex );
 					
 					for( T adjVer : getAdjVertexes(curVertex) ){
 						
 						Integer adjColor = vertexColors.get(adjVer);
 						
 						if( adjColor != null ){
 							colorsInUse.set( adjColor );
 						}		
 						else if( ! marked.contains(adjVer) ){
 							queue.add( adjVer );
 							marked.add( adjVer );
 						}				
 					}
 					
 					vertexColors.put(curVertex, colorsInUse.nextClearBit(0) );
 					marked.remove(curVertex);			
 				}		
			}			
		}	
 		
 		return vertexColors;
 	}
 	
 	
 
 	@Override
 	public String toString(){		
 		StringBuilder buf = new StringBuilder();
 		
 		
 		vertexes.keySet();
 		
 		for( T ver : vertexes.keySet() ){
 			
 			 buf.append( ver + " => " );
 			
 			 Map<T, Edge<T>> edges = vertexes.get(ver);		 
 			
 			 
 			 for( Map.Entry<T, Edge<T>> edge : edges.entrySet() ){
 				 
 				 if( edge.getValue().weight == 0 ){
 					 buf.append(edge.getValue().dest).append(", "); 
 				 }
 				 else {				 
 					 buf.append( "(").append( edge.getValue().dest).append(", ").append( edge.getValue().weight).append(") ");
 				 }
 			 }
 			 
 			
 			buf.append( System.getProperty("line.separator") );
 		}
 		
 		return buf.toString();
 	}
 	
 	
 	
 }
