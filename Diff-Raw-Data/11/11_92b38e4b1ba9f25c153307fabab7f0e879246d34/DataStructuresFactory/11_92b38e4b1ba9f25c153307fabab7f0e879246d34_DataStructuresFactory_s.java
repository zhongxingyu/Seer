 package genericsWithTest;
 
 import genericsWithTest.DataStructures.DArrayList;
 import genericsWithTest.DataStructures.DHashSet;
 
 public class DataStructuresFactory {
	public Graph GraphALHS() {
		Graph g = new Graph(DArrayList.class, DHashSet.class, DHashSet.class);
 		return g;
 	}
 }
