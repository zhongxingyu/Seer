 package test.GraphAlgorithms.Trees;
 import java.util.*;
 import main.GraphAlgorithms.Trees.DFS;
 
 public class TestDFS
 {
 	public static boolean success = true;
 	public static void main(String[] args)
 	{
 		testOne(true);
 		if(!success)
 		{
 			System.out.println("FAILED: TestDFS");
 			System.exit(1);
 		}
 		else
 		{
 			System.exit(0);
 		}
 	}
 
 	public static void testOne(boolean printDebug)
 	{
 		DFS.Node[] tree = new DFS.Node[7];
 
 		for (int i = 0; i < 7; ++i) {
 			tree[i] = new DFS.Node();
 			char name = (char)('A' + i);
 			tree[i].name = Character.toString(name);
 		}
 
 		ArrayList<DFS.Node> graph = new ArrayList<DFS.Node>();
 		tree[0].children.add(tree[1]); //A childs B
 		tree[0].children.add(tree[2]); //A childs C
 
 		tree[1].children.add(tree[0]); //B looks at A
 		tree[1].children.add(tree[3]); //B childs D
 		tree[1].children.add(tree[4]); //B childs E
 
 		tree[2].children.add(tree[0]); //C childs F
 		tree[2].children.add(tree[5]); //C childs G
 		tree[2].children.add(tree[6]); //C looks at A
 
 		tree[3].children.add(tree[1]); //D looks at B
 
 		tree[4].children.add(tree[1]); //E looks at B
 
 		tree[5].children.add(tree[2]); //F looks at C
 
 		tree[6].children.add(tree[2]); //E looks at E
 
 		for(DFS.Node node : tree)
 			graph.add(node);
 
 		DFS dfs = new DFS();
 		ArrayList<DFS.Node> traversal = dfs.DFS(graph);
 		
 		String[] answer1 = { "A", "B", "D", "E", "C", "F", "G" };
 		String[] answer2 = { "A", "C", "G", "F", "B", "E", "D" };
 
		for(int i = 0; i < answer.length; ++i) {
 			if (!answer1[i].equals(traversal.get(i).name)) {
 				success = false;
 				return;
 			}
 		}
 		
		for(int i = 0; i < answer.length; ++i) {
 			if (!answer2[i].equals(traversal.get(i).name)) {
 				success = false;
 				return;
 			}
 		}
 	}
 
 	public static void testTwo(boolean printDebug)
 	{
 		DFS.Node[] tree = new DFS.Node[7];
 
 		for (int i = 0; i < 7; ++i) {
 		tree[i] = new DFS.Node();
 		char name = (char)('A' + i);
 		tree[i].name = Character.toString(name);
 		}
 
 		ArrayList<DFS.Node> graph = new ArrayList<DFS.Node>();
 		tree[0].children.add(tree[1]); //A childs B
 		tree[0].children.add(tree[2]); //A childs C
 
 		tree[1].children.add(tree[0]); //B looks at A
 		tree[1].children.add(tree[3]); //B childs D
 		tree[1].children.add(tree[4]); //B childs E
 
 		tree[2].children.add(tree[0]); //C childs F
 		tree[2].children.add(tree[5]); //C childs G
 		tree[2].children.add(tree[6]); //C looks at A
 
 		tree[3].children.add(tree[1]); //D looks at B
 
 		tree[4].children.add(tree[1]); //E looks at B
 
 		tree[5].children.add(tree[2]); //F looks at C
 
 		tree[6].children.add(tree[2]); //G looks at C
 
 		for(DFS.Node node : tree)
 			graph.add(node);
 
 		DFS dfs = new DFS();
 		ArrayList<DFS.Node> traversal = dfs.DFS(graph, 6);
 
 		String[] answer1 = { "G", "C", "F", "A", "B", "D", "E" };
 		String[] answer2 = { "G", "C", "A", "B", "E", "D", "F" };
 		
		for(int i = 0; i < answer.length; ++i) {
 			if (!answer1[i].equals(traversal.get(i).name)) {
 				success = false;
 				return;
 			}
 		}
 		
		for(int i = 0; i < answer.length; ++i) {
 			if (!answer2[i].equals(traversal.get(i).name)) {
 				success = false;
 				return;
 			}
 		}
 	}
 }
