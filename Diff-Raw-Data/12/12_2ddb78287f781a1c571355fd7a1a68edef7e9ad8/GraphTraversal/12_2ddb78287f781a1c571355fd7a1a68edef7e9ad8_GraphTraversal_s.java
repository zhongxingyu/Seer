 package graph;
 
 import java.util.Arrays;
 
 public class GraphTraversal {
 
 	private static class Vertex{
 		char source;
 		char[] targets;
 		int[] edgeWeights;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		//Vertex[] graph = generateGraph(new char[][]{{'a', 'b', '4'}, {'b', 'd', '2'}, {'b', 'e', '2'}, {'d', 'c', '6'}, {'d', 'f', '1'}, {'f', 'c', '1', '2'}});
 		Vertex[] graph = generateGraph(new char[][]{{'a', 'b', '5'}, {'b', 'c', '2'}, {'c', 'd', '3'}, {'b', 'e', '4'}});
 		
 		char[] searchPath = new char[]{'a', 'c', 'e'};
 		
 		char[][] routes = null;
 		int[] totalWeights = null;
 		Vertex start = getVertexWithStartNode(graph, searchPath[0]);
 		routes = new char[1][];
 		routes[0] = new char[]{start.source};
 		routes = traverse(routes, totalWeights, start, graph, searchPath);
 		for(char[] route:routes){
 			boolean isCompletePath = true;
 			for(char point:searchPath){
 				if(!arrayContains(route, point))
 					isCompletePath = false;
 			}
 			if(isCompletePath){
 				System.out.println(Arrays.toString(route) + "--->" + getPathLength(route, graph));
 			}
 		}
 	}
 	
 	private static int getPathLength(char[] route, Vertex[] graph) {
 		// TODO Auto-generated method stub
 		int distance = 0;
 		for(int i=0;i<route.length;i++){
 			if(i+1<route.length){
 				Vertex startNode = getVertexWithStartNode(graph, route[i]);
 				for(int j=0;j<startNode.targets.length;j++){
 					if(startNode.targets[j] == route[i+1])
 						distance += startNode.edgeWeights[j];
 				}
 			}
 			
 		}
 		return distance;
 	}
 
 	private static boolean arrayContains(char[] route, char point) {
 		// TODO Auto-generated method stub
 		for(char pointInPathRoute:route)
 			if(pointInPathRoute == point)
 				return true;
 		return false;
 	}
 
 	private static char[][] traverse(char[][] routes, int[] totalWeights,
 			Vertex vertex, Vertex[] graph, char[] searchPath) {
 		// TODO Auto-generated method stub
 		int i=0;
 		boolean expanded = false;
 		while(i<routes.length){
 			char[] route = routes[i];
 			Vertex startNode = getVertexWithStartNode(graph, route[route.length - 1]);
 			if(startNode!=null && startNode.source!=searchPath[searchPath.length - 1]){
 				char[][] expandedRoutes = expandRoute(route, startNode);
 				routes = insertArrayAtPosition(routes, expandedRoutes, i);
 				i = i + expandedRoutes.length - 1;
 			}
 			i++;
 		}
 		
 		for(int j=0;j<routes.length;j++){
 			Vertex lastNodeVertex = getVertexWithStartNode(graph, routes[j][routes[j].length - 1]);
 			if(lastNodeVertex!=null && lastNodeVertex.source!=searchPath[searchPath.length - 1])
 				expanded = true;
 		}
 		if(expanded)
 			routes = traverse(routes, totalWeights, vertex, graph, searchPath);
 		return routes; 
 	}
 
 	private static char[][] insertArrayAtPosition(char[][] routes,
 			char[][] expandRoute, int position) {
 		char[][] newArray = new char[routes.length + expandRoute.length - 1][];
 		boolean inserted = false;
 		for(int counter = 0;counter<routes.length;counter++){
 			if(counter==position){
 				for(int addedCounter = 0;addedCounter<expandRoute.length;addedCounter++){
 					newArray[counter + addedCounter] = expandRoute[addedCounter];
 				}
 				inserted = true;
 			}else{
 				newArray[inserted?counter+expandRoute.length-1:counter] = routes[counter];
 			}
 		}
 		return newArray;
 	}
 
 	private static char[][] expandRoute(char[] route, Vertex startNode) {
 		// TODO Auto-generated method stub
 		char[][] expandedRoutes = null;
 		for(char target:startNode.targets){
 			char[] newArray = new char[route.length];
 			copyArray(route, newArray);
 			newArray = addElement(newArray, target);
 			expandedRoutes = insertElement(expandedRoutes, newArray);
 		}
 		return expandedRoutes;
 	}
 	
 	private static char[][] insertElement(char[][] collection, char[] element){
 		if(collection==null)
 			return new char[][]{element};
 		char[][] newCollection = new char[collection.length + 1][];
 		for(int counter = 0; counter<collection.length;counter++){
 			newCollection[counter] = collection[counter];
 		}
 		newCollection[collection.length] = element;
 		return newCollection;
 	}
 
 	private static Vertex getVertexWithStartNode(Vertex[] graph, char startNode){
 		for(int i=0;i<graph.length;i++){
 			if(graph[i]!=null && graph[i].source==startNode){
 				return copyVertex(graph[i]);
 			}
 		}
 		return null;
 	}
 
 	private static Vertex[] generateGraph(char[][] graph) {
 		Vertex[] convertedGraph = new Vertex[graph.length];
 		for(int i=0;i<graph.length;i++){
 			if(graph[i]!=null){
 				convertedGraph[i] = createVertex(new Vertex(), graph[i]);
 				graph[i] = null;
 				for(int j=i + 1;j<graph.length;j++){
 					if(graph[j]!=null && convertedGraph[i].source == graph[j][0]){
 						createVertex(convertedGraph[i], graph[j]);
 						graph[j] = null;
 					}
 				}
 			}
 		}
 		return convertedGraph;
 	}
 	
 	private static Vertex createVertex(Vertex v, char[] edge){
 		if(edge==null || edge.length<3){
			System.out.println("Invalid edge: " + edge);
 			return null;
 		}
 		v.source = edge[0];
 		v.targets = addElement(v.targets, edge[1]);
 		
 		int[] weight = null;
  		for(int i=2;i<edge.length;i++)
 			weight = addElement(weight, getInteger(edge[i]));
  		
 		v.edgeWeights = addElement(v.edgeWeights, getIntegerVal(weight));
 		return v;
 	}
 	
 	private static int getIntegerVal(int[] weight) {
 		int intWeight=0;
 		for(int j=0;j<weight.length; j++){
 			intWeight = intWeight + (weight[j] * (int)(Math.pow(10, (weight.length - 1 - j))));
 		}
 		return intWeight;
 	}
 
 	private static int getInteger(char c) {
 		switch(c){
 			case 48 : return 0;
 			case 49 : return 1;
 			case 50 : return 2;
 			case 51 : return 3;
 			case 52 : return 4;
 			case 53 : return 5;
 			case 54 : return 6;
 			case 55 : return 7;
 			case 56 : return 8;
 			case 57 : return 9;
 		}
 		return 0;
 	}
 
 	private static char[] addElement(char[] array, char element){
 		if(array==null)
 			return new char[]{element};
 		else{
 			char[] newArray = new char[array.length + 1];
 			for(int arrayCounter=0;arrayCounter<array.length;arrayCounter++)
 				newArray[arrayCounter] = array[arrayCounter];
 			newArray[array.length] = element;
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
 			newArray[array.length] = element;
 			return newArray;
 		}
 	}
 
 	private static Vertex copyVertex(Vertex source){
 		Vertex target = new Vertex();
 		target.source = source.source;
 		target.targets = new char[source.targets.length];
 		copyArray(source.targets, target.targets);
 		target.edgeWeights = new int[source.edgeWeights.length];
 		copyArray(source.edgeWeights, target.edgeWeights);
 		return target;
 	}
 
 	private static void copyArray(int[] source, int[] target) {
 		for(int i=0;i<source.length;i++){
 			target[i] = source[i];
 		}
 	}
 
 	private static void copyArray(char[] source, char[] target) {
 		for(int i=0;i<source.length;i++){
 			target[i] = source[i];
 		}
 	}
 }
