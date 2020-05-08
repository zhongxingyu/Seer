 package genericsWithTest;
 
 import genericsWithTest.DataStructures.DArrayList;
 import genericsWithTest.DataStructures.DHashSet;
 
 public class Tester {
 
 	public static void main(String[] args) {
		Graph g = new Graph("name", 0L, DArrayList.class, DHashSet.class, DHashSet.class, DirectedNode.class);
 	}
 }
