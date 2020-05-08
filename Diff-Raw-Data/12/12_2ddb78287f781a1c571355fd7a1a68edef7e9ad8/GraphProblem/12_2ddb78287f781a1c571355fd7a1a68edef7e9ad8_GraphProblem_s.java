 package graph;
 
 public class GraphProblem {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		char[][] edges = new char[4][];
 		edges[0] = new char[]{'a', 'b'};
 		edges[1] = new char[]{'b', 'c'};
 		edges[2] = new char[]{'c', 'd'};
 		edges[3] = new char[]{'b', 'e'};
 		int[] weights = new int[]{5, 2, 3, 4};
 		
 		char[] vertices = new char[]{'a', 'b', 'c', 'd', 'e'};
 		
 		char[] route = new char[]{'a', 'b', 'e'};
 		
 		char[][] paths = new char[10][];
 		int[] usedEdges = new int[4];
 		
 		getPathsLength(edges, weights, vertices, route, paths,usedEdges, 0, 0);
 		
 
 	}
 
 	private static void getPathsLength(char[][] edges, int[] weights,
 			char[] vertices, char[] route, char[][] paths, int[] usedEdges,
 			int pathCounter, int routeCounter) {
 		
 		for(int pointsCount=0;pointsCount<vertices.length;pointsCount++){
 			int[] edgesFromVertexIndices = getEdgesFrom(vertices[pointsCount], edges);
 			
 			//addVerticesToPath(edgesFromVertexIndices, paths);
 			
 			if(edgesFromVertexIndices.length==0){
 				
 			}
 		}
 		
 		//paths[pathCounter] = addElement(paths[pathCounter], route[routeCounter]);
 		//char[] edge = getNextEdge(usedEdges, route[routeCounter], edges);
 	}
 	
 	private static int[] getEdgesFrom(char c, char[][] edges) {
 		int[] edgesFromVertexIndices = null;
 		for(int i=0;i<edges.length;i++){
 			if(edges[i][0]==c){
 				edgesFromVertexIndices = addElement(edgesFromVertexIndices,i);
 			}
 		}
 		return edgesFromVertexIndices;
 	}
 
	private static char[] getNextEdge(int[] usedEdges, char routeVertex, char[][] edges) {
 		for(int edgeCounter=0;edgeCounter<edges.length;edgeCounter++){
 			if(edges[edgeCounter][0]==routeVertex && !containsElement(usedEdges, edgeCounter))
 				return edges[edgeCounter];
 		}
 		return null;
 	}
 
	private static char[] addElement(char[] array, char element){
 		if(array==null)
 			return new char[]{element};
 		else{
 			char[] newArray = new char[array.length + 1];
 			for(int arrayCounter=0;arrayCounter<array.length;arrayCounter++)
 				newArray[arrayCounter] = array[arrayCounter];
 			newArray[array.length - 1] = element;
 			return newArray;
 		}
 	}
 	
 	private static int[] addElement(int[] array, int element){
 		if(array==null)
 			return new int[]{element};
 		else{
 			int[] newArray = new int[array.length + 1];
 			for(int arrayCounter=0;arrayCounter<array.length;arrayCounter++)
 				newArray[arrayCounter] = array[arrayCounter];
 			newArray[array.length - 1] = element;
 			return newArray;
 		}
 	}
 	
 	private static boolean containsElement(int[] array, int element){
 		return false;
 	}
 
 }
