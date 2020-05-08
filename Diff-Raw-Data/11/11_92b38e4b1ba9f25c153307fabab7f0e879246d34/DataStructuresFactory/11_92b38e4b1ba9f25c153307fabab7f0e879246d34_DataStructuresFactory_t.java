 package genericsWithTest;
 
 import genericsWithTest.DataStructures.DArrayList;
 import genericsWithTest.DataStructures.DHashSet;
 
 public class DataStructuresFactory {
	public Graph GraphALHS(String name, long timestamp, Class<? extends Node> nodeType) {
		Graph g = new Graph(name, timestamp, DArrayList.class, DHashSet.class, DHashSet.class, nodeType);
 		return g;
 	}
 }
