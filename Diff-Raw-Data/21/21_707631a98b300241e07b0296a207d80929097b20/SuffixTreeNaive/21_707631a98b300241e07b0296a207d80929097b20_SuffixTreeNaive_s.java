 package stralg13;
 
 import java.util.Map;
 
 public class SuffixTreeNaive {
 	public static final String STRING_END = "$";
 	Node root;
 	String string;
 	int iteration = 0;
 	
 	public SuffixTreeNaive(String string) {
 		this.string = string + STRING_END;
 
 		makeSuffixTree();
 	}
 
 	public SuffixTreeNaive() {
 		root = new Node();
 	}
 
 	public void makeSuffixTree() {
 		root = new Node();
 		root.addEdgeAndNewNode(0, string.length());
 
 		for (iteration = 0; iteration < string.length() - 2; ++iteration) {
 			 slowscan2(root, iteration + 1, string.length());
 		}
 		root.addEdgeAndNewNode(string.length() - 1, string.length());
 
 	}
 
 	Node slowscan2(Node startNode, int startIndex, int endIndex) {
 		if (startIndex < endIndex) {
 			for (Map.Entry<Tuple, Node> edge : startNode.edges.entrySet()) {
 				if (shouldContinueDown(startIndex, endIndex, edge.getKey())) {
 					return slowscan2(startNode.edges.get(edge.getKey()),
 							startIndex + edge.getKey().second
 									- edge.getKey().first, endIndex);
 				}
 				if (shouldSplitEdge(startIndex, edge.getKey())) {
 					int headIndex = getSplitIndex(
 							startIndex, endIndex, edge.getKey());
 					Node head = startNode.splitEdgeAndReturnNewNode(
 							edge.getKey(), headIndex);
 					
					int tailIndex = getLeafIndex(startIndex, endIndex,
							edge.getKey());
					head.addEdgeAndNewNode(iteration + 1 + headIndex, string.length());
 					
 					return head;
 				}
 			}
 		}
 		startNode.addEdgeAndNewNode(startIndex, string.length());
 		return startNode;
 	}
 	
 	public boolean equals(SuffixTreeNaive otherTree) {
 		return string.equals(otherTree.string) && root.equals(otherTree.root);
 	}
 	
 	public boolean equals(SuffixTree otherTree) {
 		return string.equals(otherTree.string) && root.equals(otherTree.root);
 	}
 
 	private boolean shouldContinueDown(int startIndex, int endIndex, Tuple key) {
 		if ((startIndex + (key.second - key.first)) > string.length() - 1)
 			return false;
 
 		return string
 				.substring(startIndex, endIndex)
 				.startsWith(string.substring(key.first, key.second));
 	}
 
 	boolean shouldSplitEdge(int startIndex, Tuple tuple) {
 		return string.charAt(tuple.first) == string.charAt(startIndex);
 	}
 
 	int getSplitIndex(int startIndex, int endIndex,
 			Tuple tuple) {
 		int i = 0;
 		for (; i < tuple.second - tuple.first && i < endIndex - startIndex; ++i) {
 			if (string.charAt(startIndex + i) != string.charAt(tuple.first + i))
 				break;
 		}
 		return tuple.first + i;
 		 
 	}
 
 	int getLeafIndex(int startIndex, int endIndex, Tuple tuple) {
 		int i = 0;
 		for (; i < tuple.second - tuple.first && i < endIndex - startIndex; ++i) {
 			if (string.charAt(startIndex + i) != string.charAt(tuple.first + i))
 				break;
 		}
 		return startIndex + i;
 	}
 
 	@Override
 	public String toString() {
 		return string + "\n" + root.toString();
 	}
 }
