 package stralg13;
 
 import java.util.Map;
 
 public class SuffixTree {
 	public static final String STRING_END = "$";
 	Node root;
 	String string;
 	int[] tailStart;
 	int iteration = 0;
 	
 	Node headOfIplus1 = null;
 	Node headOfI = null;
 
 	public SuffixTree(String string) {
 		this.string = string + STRING_END;
 
 		makeSuffixTree();
 	}
 
 	public SuffixTree() {
 		root = new Node();
 	}
 
 	public void makeSuffixTree() {
 		root = new Node();
 		root.addEdgeAndNewNode(0, string.length());
 		root.suffixLink = root;
 		headOfIplus1 = null;
 		headOfI = root;
 		tailStart = new int[string.length()];
 		tailStart[0] = 0;
 
 		for (iteration = 0; iteration < string.length() - 2; ++iteration) {
 			if (headOfI.equals(root)) {
 				headOfIplus1 = slowscan2(root, iteration + 1, string.length());
 			} else {
 				Tuple parentToHeadEdge = findParentEdge(headOfI);
 
 				int startIndex = parentToHeadEdge.first;
 				int endIndex = parentToHeadEdge.second;
 				Node startNode = headOfI.parent;
 				if (headOfI.parent == root) {
 					startIndex++;
 				} 
 				if (headOfI.parent == root && startIndex >= endIndex) {
 					headOfIplus1 = slowscan2(
 							root,
 							tailStart[iteration], string.length());
 					headOfI.suffixLink = root;
 				} else  {
 					if (startNode.suffixLink != null) {
 						startNode = startNode.suffixLink;
 					}
 					slowscan3(startNode,
 							startIndex, endIndex);
 				}
 			}
 
 			headOfI = headOfIplus1;
 		}
 		root.addEdgeAndNewNode(string.length() - 1, string.length());
 
 	}
 
 	protected Tuple findParentEdge(Node headOfI) {
 		for (Map.Entry<Tuple, Node> edge : headOfI.parent.edges.entrySet()) {
 			if (edge.getValue() == headOfI) {
 				return edge.getKey();
 			}
 		}
 		return null;
 	}
 
 	public boolean equals(SuffixTree otherTree) {
 		return string.equals(otherTree.string) && root.equals(otherTree.root);
 	}
 
 
 	void slowscan3(Node startNode, int startIndex, int endIndex) {
 
 		if (startIndex < endIndex) {
 			for (Map.Entry<Tuple, Node> edge : startNode.edges.entrySet()) {
 				if (shouldContinueDown(startIndex, endIndex, edge.getKey())) {
 					slowscan3(startNode.edges.get(edge.getKey()),
 							startIndex + edge.getKey().second
 									- edge.getKey().first, endIndex);
 					return;
 				}
 				if (shouldSplitEdge(startIndex, edge.getKey())) {
 					int index = getSplitIndex(
 							startIndex, endIndex, edge.getKey());
 					Node head = startNode.splitEdgeAndReturnNewNode(
 							edge.getKey(), index);
 					
 					int tailIndex = getLeafIndex(startIndex, endIndex,
							new Tuple(edge.getKey().first, index));
 					head.addEdgeAndNewNode(iteration + tailIndex,
 							string.length());
 
 					headOfIplus1 = startNode;
 					headOfI.suffixLink = headOfIplus1;
 
 					tailStart[iteration + 1] = iteration + tailIndex;
 					return;
 				}
 			}
 		}
 		
 		headOfIplus1 = slowscan2(
 				startNode,
 				tailStart[iteration], string.length());
 		headOfI.suffixLink = startNode;
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
 					head.addEdgeAndNewNode(tailIndex, string.length());
 					
 					tailStart[iteration + 1] = tailIndex;
 					return head;
 				}
 			}
 		}
 		tailStart[iteration + 1] = startIndex;
 		startNode.addEdgeAndNewNode(startIndex, string.length());
 		return startNode;
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
	
	int getLeafIndex3(int startIndex, int endIndex, Tuple tuple) {
		int i = 0;
		for (; i < tuple.second - tuple.first && i < endIndex - startIndex; ++i) {
			if (string.charAt(startIndex + i) != string.charAt(tuple.first + i))
				break;
		}
		return tuple.second;
	}
 
 	@Override
 	public String toString() {
 		return string + "\n" + root.toString();
 	}
 
 }
