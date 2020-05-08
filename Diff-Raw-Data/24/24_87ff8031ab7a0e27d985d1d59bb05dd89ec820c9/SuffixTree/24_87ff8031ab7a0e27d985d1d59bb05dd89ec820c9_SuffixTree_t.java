 package utils;
 
 import java.lang.String;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.PalindromeResult;
 
 public class SuffixTree {
 
 	public SuffixTreeNode root;
 	public static String word;
 	public int startPos;
 	public int endPos;
 	
 	public SuffixTree(String treeWord)
 	{
 		word = new String(treeWord+"$");
 		this.startPos = 1;
 		this.endPos = word.length();
 		this.constructTree();
 	}
 	
 	public SuffixTree(String original, String rCompliment) 
 	{
 		//Construct Tree from Original String
 		word = new String(original+"$");
 		this.startPos = 1;
 		this.endPos = word.length();
 		this.constructTree();
 		//Insert reverse compliment into existing tree
 		this.insertNewSuffix(rCompliment);
 	}
 	
 	//NOTE: Assumes that generalized suffix tree has already been created
 	public List<PalindromeResult> findPalindromes(boolean allowUGPairing, int minPalindromeSize, int maxPalindromeSize, int minLoopSize, int maxLoopSize) {
 		//TODO find palindrome locations
 		//minLoopSize and maxLoopSize are what we know as gap size
 		
 		List<PalindromeResult> locations = new ArrayList<PalindromeResult>();
 		String original = word.substring(0, word.indexOf("$"));
 		String rCompliment = word.substring(word.indexOf("$")+1, word.length()-1);
 		int n = original.length();
 		int q;
 		String extension = new String();
 		int loopSize;
 		
 		for(q = 1; q < n; q++)
 		{
 			for(loopSize = minLoopSize; loopSize <= maxLoopSize; loopSize++)
 			{
 				if(q+loopSize < original.length())
 				{
 					extension = getLongestCommonExtension(this.root, original.substring(q+loopSize), rCompliment.substring(n-q));
 					if(extension != null && extension.length() >= minPalindromeSize && extension.length() <= maxPalindromeSize)
 					{
						int rightWingEnd = (q+loopSize+extension.length() < original.length()) ? q+loopSize+extension.length() : original.length();
						locations.add(new PalindromeResult(q-extension.length()+1, q, q+loopSize+1, rightWingEnd));
 					}
 				}
 			}
 		}
 		
 		//PSEUDOCODE
 		/*
 		 * int n = word.length();
 		 * 
 		 * for(int q = 0; q < word.length(); q++) {
 		 * 		
 		 * 		SuffixTreeNode s1Node = findOriginalLeafForPosition(q+1);
 		 * 		for(int loopSize = maxLoopSize; loopSize >= minLoopSize; loopSize--) {
 		 * 			SuffixTreeNode s2Node = findPalindromeLeafForPosition(n-q+1+maxLoopSize);
 		 * 
 		 * 			SuffixTreeNode lowestCommonAncestor = findLowestCommonAncestor(s1Node, s2Node);
 		 * 
 		 * 			if(edgeSize(lowestCommonAncestor) >= minPalindromeSize && edgeSize(lowestCommonAncestor) <= maxPalindromeSize) {
 		 * 				//It works! Yay! Save only the left wing of the palindrome. I can get what I need from that I think...
 		 * 				locations.add(new PalindromeResult(q - edgeSize, q, q + loopSize, q + loopsize + edgeSize)) //I think... there may be off by 1 errors :/  This will also not work with those weird gaps -_-
 		 * 
 		 * 			}
 		 * 
 		 * 		}
 		 * 
 		 * }
 		 * 
 		 */
 		
 		//locations.add(new PalindromeResult(1, 4, 8, 11));
 		
 		return locations;
 	}
 	
 	public static void main(String[] args)
 	{
 		//***  TESTING STUFF FOR FINDING REPEATS AND GENERALIZED SUFFIX TREE CONSTRUCTION ***
 		//String str = new String("AATATTTATTTGTTACAATGAGTGAGTGTCTTTACTTTACAATATACCCTAGAGAAATGGGTTACAGGCTATATAGACTCAATACTCAGGTAAAGAGAGCGTGAGTGAGATAATGATATGAAATTAGCAATGCTATTATTTCTACACTAGCGCCACTTAGCGGCACTTACTTTTATTGCTCACGAACGACATCCTGTGGTACGCCTTGAAATCTTATTTTATTAAAAACCTTTACGGTAAAATAAGTGTGATTTTCAAATTTCATATTCTTTAAAATCGAAGTAATTTTCCATAACGTTTGTGTGCCCGTGCTGGAGGTTTGGGGGCGTGGTAACCTTCTAAAACAAACTTGTGCTGCACAAGTGTCACTAGAGTTTCCATGTTTTATGTAGCAACATGTTAAATGTTACACATACACGGCCTTAAAAACATTACTGAACAATAATCTGTTTGGTGGAATTCAAAAACAGCACATTATTAACATTGAGTTCCTATCAAAAACAGCACATTATTAACATTGAGTTCCTATTTCTCGCATCGTAATCAAAAAAACGTTGTCATGATTCATTAATTATTTACTAAAGATGTCATTTTGTAACACAGTGCTATTAATAAAGCGTTCTTACATATTTGTTCGAACATGTGAAATCCACAAATCTTACCATATTTATATTTTGGTTATCATATTCGTTTGTTTGTGACCTTTTTGATTTGCTCTTTTTTGAGGGTAATTTATTTCCAGAAGATATTTTTGAACTATTTGGATTCTGAGTGTCCATGTTAATTTGTTCGTCTTCCTGCTAAATTTTAAAGTGATGACTTAAATAGGATTAAGTCAGATTAAATAAATTAAATAAGATTAAATACATATAATTATTTTATTTTAATTAAAAAGAATTTAATACGCTCTATCCTATAGTGCAAATTTAAAATTTTAAGGTTTTGACTTAAATAAGTTAGCATTACTAAAATAAAGTGCTTAACAAAAACACTTGACAAGGTAAAAGTCAAGGGTCAATTGCACCCTCAACGAATAAAACTTTCTAATATAATCTTTTGTGACGAAAATTAAATTGCACTTGATTTTATAAATACTTGAGTGATACATGAATGTAAAGATAGCGCTAAGAATGCAGGGATTGCATACCAAAACAAAATAAGTACATGGCTAACGTGAAATAACATTTAAATAATAAAAAAAAATGTAGTTTTACCACTCTTAAAAATCAATAGTTTAGCAGAAAAACAGATTAAACATACCATATACCAACGCTCAAGACTTTTGAACACTGGGGTTAGAAAGTTTTGCTATATGCCATTAAAGGCATTTTGACATGTATTAGGAGAAAAACAGATTAAAGAAACCATATTCAAACGATAATAACTTCTGAACACTTGAGGTTAGAAACTTTTGCTATATGCCATTGAAGGCATTTTGACATATATTTTGTCTGAGTACAATACAAAAAAAAACTAATACAAAATACGTAATTTTATTTGTTTAATGTAATATTTAAATATACTACCCGAAATAAAAATACAATCCACATTAGGGGGATACTATCAAAAAGTGTTTGTATTTGCTTAACAGGGGTTCAAAAAACTATACTCCCATCTTTTGTTCAAATTTTTATTATTTAGAAGAACACGTAGCACTTAAGGAATATACACTTTTGGGAAAAATTGAAACTTTATTTATAATACCTATGTCTGAACTCTAGCTAGGATATTTATATAATAAAAAAGCTTTTCTACGAGTTCATGCTTTTTAGTGTATATATGAATACACACTGAACAATAGCACACTCTTTGGCATCGTATTGTACCTTATACATAATTTTAAATGATCCACAAAATATATAAGAGTTTGCATCCAAGTATGTCATCGATGATGGTTTCACAAATTAATAATGAGTATATTAACTAATTATACTGATGCTTAAAATATAGGTTGTTAAAAGCAGACCTATCTGGATGTTTTATCATTAGTAATTAACGTTCGGAAGTTGAAGTCCGACTGTTTCTTAGCGTTGGCAGATTCGGTACATGTAGATACAATGTTCAAAATTGAGTTAATTTTAAATGAGTAAAAGCTTTCTTCCAAAAATATTTAAAAGCTTTTAAAGCAAGTAGAATGCACATTTGGAATTCAGACCTGTATAAAAAGAAATAAACCTGCAAGACATTCTATTTCTAAGTAAAGCAAGAAAGAATGATATAGTCAGTTCCTCTACTATCAGATACTGGGTTCTTAGCTAGAGTAAGTGCGACGGAGATAGAATATATAAAAACAAATAGGAAAAGACATTTCGAGCGCTAACCAGCACAACCTGCTGCCGGCTACATTATTTTAATTTGTAGTCGTTAGGTATAGAACAAATAATAAAGTAAGATATCTTCTGCGAACCAAAGTTGATATACTTTTTAAGTATTTATATATTTGGAAAATTATGTATACTGAATAATAATACTTTATGATTAAAACAACGTAACTAAGACTAATTTTCTATTATTTTTAAATAATTTGTATACTGCTTTAATAGCAACAATAGGATTTCGTCTCCGATTTTTAAATTAAAACCGAGTAACCTCATCAGGCAGATCTCCATAATGAGCTTTAAAAATGGGAGGCCGCAATGCTGCCGGCTATTCTAGCCCCACATTTATCAAAATAAATAGTGGGAGGAAGTACTCGTTATCTAACGCATAAGTTGGTGTAAATGTTTTTTTACATAATAATTTAGCCATTAAGTCCCATTAATCAATATCAAAAGCCGTTAAAATGTCGAATATGTTTTACCTAACAAAGATGCAAAACTGGATACAAGTGATCAGTACTGGTATCAATAACATGTTCGAGGAAGCCAATACTTTGCGCAAATTGTTAAATGTCGTTTCAAAGGAAGGCGATAACTTCAAATTCGGCCAGATAAAATGTTACGGATGTTAAGGGTGTCGCAGAAATAAAAGTATCCGAGACAATGAGACGGGTAGTTTTCTAGACCTAATTCCACGCTAAGTCCGAGAAATGGCCAGCTCAAAAGTGCATAAACGTGAGCTGTGTACATGGCCTCCCAAAATTCAGCAAATCTGATTGCAGAAAACCAATATTTGGACTGCGTAAAAAAGTGATAACGTAATATATTTTTTTGCTATTGTATTGTGTTGTGAGTCGTAGATACTTCGTGGCTACTTTTTATTTTTGGTTTTTATTTATCATTACTTTATTTACAATATTTATACTTAGTCATGTTAATTTAATTATACGATCATTTTCTTTTCATTTGATTTCTTCCTTTCCGAAATTTCCTTGATCGGAAAGGTTTTCAGAATTATTGCAAAATTATTATTCTTTAGAGAATAGGCAGCGTCTCATTAGGCAGGGATTTAGGCTTGGAAGCTATCGATGGTCTAACGAGATATCCAATTTTTCCTCTCTAAGCCAACATCGGTTTTATTTGTCGCTAATGCTACATTTAGCATTTTTCACATCTCTCAAATTAGTACAAACCAAGGCTTTGTTGCAGATTTTTGGGTAGGAAAAATTCAA");
		//String str = new String("TCTGAATTTT");
		//String str = new String("AAAAAACTTGAAACCCAAGTCGCGCTCTTGAAACCCAAGTGAACGGTTGAAACCCAAGTTACGCCAAGATTGAAACCCAAGTAA");
 //		String str = new String("ATAT");
 //		SuffixTree tree = new SuffixTree(str);
 //		tree.insertNewSuffix("TATT");
 //		System.out.println("Done!");
 //		printTree(tree.root, 0);
 //		System.out.println();
 //		
 //		ArrayList<String> repeats = findRepeats(tree, "TTT");
//		//ArrayList<String> repeats = findRepeats(tree, "TTGAAACCCAAGT");
 //
 //		ArrayList<String> allStrings = new ArrayList<String>();
 //
 //		allStrings = findAllRepeatsOfLength(tree, 2, 4);
 //		
 //		System.out.println("All repeats of target sequence:");
 //		for(String repeat : repeats)
 //		{
 //			System.out.println(repeat);
 //		}
 //
 //		System.out.println("All repeats of certain length:");
 //		for(String s : allStrings)
 //		{
 //			System.out.println(s);
 //		}
 		
 		//*** TESTING STUFF FOR FINDING PALINDROMES ***
 		String s1 = new String("ATCATACTGAT");
 		String s2 = new String("TAGTCATACTA");
 		SuffixTree tree = new SuffixTree(s1, s2);
 		printTree(tree.root, 0);
 		System.out.println();
 		
 		List<PalindromeResult> palindromes = tree.findPalindromes(false, 3, 3, 1, 1);
 		
 		System.out.println("Starting Palindrome Testing: ");
 		for(PalindromeResult pr : palindromes)
 		{
 			System.out.println("Left Wing (Start,End): " + pr.leftWingStart + ", " + pr.leftWingEnd + ", Right Wing (Start, End): " + pr.rightWingStart + ", " + pr.rightWingEnd);
 		}
 		
 	}
 	
 	public void constructTree()
 	{
 		
 		//Constructing tree from first string
 		this.root = new SuffixTreeNode();
 		
 		int i;
 		for(i = this.startPos; i <= this.endPos; i++)
 		{
 			this.addSuffix(i, this.endPos, word.substring(i-1));
 		}
 	}
 	
 	//NOTE: Assumes that tree has been initialized already
 	public void insertNewSuffix(String newSuffix)
 	{
 		int oldStrLength = word.length();
 		word = new String(word + newSuffix + "$");
 		this.startPos = oldStrLength+1;
 		this.endPos = word.length();
 		
 		int i;
 		for(i = this.startPos; i <= this.endPos; i++)
 		{
 			this.addSuffix(i, this.endPos, word.substring(i-1));
 		}
 	}
 	
 	public void addSuffix(int start, int end, String suffix)
 	{
 		StringBuilder tempSuffix = new StringBuilder(suffix);
 		SuffixTreeNode node = findPosition(this.root, tempSuffix);
 		if(tempSuffix.length() > 0)
 		{
 			int charsRemoved = suffix.length() - tempSuffix.length();
			boolean completeIncomingEdgeMatch = false;
			if(node.incomingEdge != null && charsRemoved >= (node.incomingEdge.labelEndPos - node.incomingEdge.labelStartPos + 1))
			{
				completeIncomingEdgeMatch = true;
			}
			this.insertSuffix(start+charsRemoved, end, node, new String(tempSuffix), start, completeIncomingEdgeMatch);
 		}
 		else
 		{
 			//Length of tempSuffix is 0 it means that a complete match in tree was found
 			//so there is nothing to insert
 			node.positions.add(start);
 		}
 	}
 	
 	//NOTE: Method modifies suffix parameter
 	public static SuffixTreeNode findPosition(SuffixTreeNode startNode, StringBuilder suffix)
 	{
 		int i;
 		int charsMatched = -1;
 		String label;
 		for(i = 0; i < startNode.children.size(); i++)
 		{
 			label = startNode.children.get(i).incomingEdge.getLabel(word);
 			charsMatched = matchSuffixWithLabel(new String(suffix), label);
 			if(charsMatched == label.length())
 			{
 				//Child Node label is a prefix of the target suffix so continue 
 				//traversing tree along this child node path
 				return findPosition(startNode.children.get(i), suffix.delete(0, charsMatched));
 			}
 			else if(charsMatched > 0)
 			{
 				//Child Node label is a partial prefix of the target suffix so continue 
 				//traversing tree along this child node path
 				return startNode.children.get(i);
 			}
 		}
 		
 		//None of the child nodes has labels matching the target suffix so return current node
 		return startNode;
 	}
 	
 	public static int matchSuffixWithLabel(String suffix, String label)
 	{
 		int i;
 		for(i = 0; i < suffix.length() && i < label.length(); i++)
 		{
 			if(suffix.charAt(i) != label.charAt(i))
 			{
 				break;
 			}
 		}
 		return i;
 	}
 	
	public void insertSuffix(int start, int end, SuffixTreeNode node, String suffix, int position, boolean completeIncomingEdgeMatch)
 	{
		if(node.incomingEdge == null || completeIncomingEdgeMatch || suffix.charAt(0) != node.incomingEdge.getLabel(word).charAt(0)) 
 		{
 			//Case 1: Only add leaf node
 			SuffixTreeNode leafNode = new SuffixTreeNode(new SuffixTreeEdge(start, end), node);
 			leafNode.positions.add(position);
 			node.children.add(leafNode);
 		}
 		else
 		{
 			//Case 2: Split Edge, Create Internal Node
 			
 			//Construct new label
 			int charsMatched = matchSuffixWithLabel(suffix, node.incomingEdge.getLabel(word));
 			
 			//Remove node from parent's child list
 			SuffixTreeNode parent = node.parent;
 			this.remove(parent.children, node);
 			
 			//Create new internal node and Add internal node to parent's child list
 			int internalNodeStart = node.incomingEdge.labelStartPos;
 			SuffixTreeNode internalNode = new SuffixTreeNode(new SuffixTreeEdge(internalNodeStart, internalNodeStart+charsMatched-1), parent);
 			parent.children.add(internalNode);
 			//Add old node (was removed from parent's child list) to internal node's child list
 			node.incomingEdge.labelStartPos = internalNodeStart+charsMatched;
 			node.parent = internalNode;
 			internalNode.children.add(node);
 			//Add new leaf node to finish off rest of suffix
 			SuffixTreeNode suffixNode = new SuffixTreeNode(new SuffixTreeEdge(start+charsMatched, end), internalNode);
 			suffixNode.positions.add(position);
 			internalNode.children.add(suffixNode);
 		}
 	}
 	
 	public void remove(ArrayList<SuffixTreeNode> list, SuffixTreeNode target)
 	{
 		int i;
 		for(i = 0; i < list.size(); i++)
 		{
 			if(list.get(i).compareTo(target, word) == 0)
 			{
 				list.remove(i);
 				break;
 			}
 		}
 	}
 	
 	public static ArrayList<String> findRepeats(SuffixTree tree, String target)
 	{
 		ArrayList<String> repeats = new ArrayList<String>();
 		getRepeatsUsingTarget(tree.root, repeats, target, "");
 		return repeats;
 		
 	}
 
      public static ArrayList<String> findAllRepeatsOfLength(SuffixTree tree, int min, int max)
      {
      	ArrayList<String> repeats = new ArrayList<String>();
      	ArrayList<SuffixTreeNode> internalNodes = new ArrayList<SuffixTreeNode>();
      	ArrayList<String> fullInternalNodeLabels = new ArrayList<String>();
      	
      	getAllInternalNodesOfLength(tree.root, internalNodes, fullInternalNodeLabels, "", min, max);
      	
      	int i;
      	for(i = 0; i < internalNodes.size() && i < fullInternalNodeLabels.size(); i++)
      	{
      		getLeafStringsFromNode(internalNodes.get(i), fullInternalNodeLabels.get(i), repeats);
      	}
 
 		return repeats;
      }			     
 	
 	public static void getRepeatsUsingTarget(SuffixTreeNode start, ArrayList<String> repeats, String target, String current)
 	{
 		if(start.children.size() == 0 && current.startsWith(target))
 		{
 			//Leaf Node
 			repeats.add(current);
 		}
 		else if(current.length() == 0 || target.startsWith(current))
 		{
 			//Root Node or Path label is prefix of target string
 			for(SuffixTreeNode child : start.children)
 			{
 				
 				getRepeatsUsingTarget(child, repeats, target, current+child.incomingEdge.getLabel(word));
 			}
 		}
 		
 	}
 	
 	public static void getAllInternalNodesOfLength(SuffixTreeNode start, ArrayList<SuffixTreeNode> nodes, ArrayList<String> nodeLabels, String current, int min, int max)
 	{
 		if(current.length() >= min && current.length() <= max && start.children.size() > 0)
 		{
 			nodes.add(start);
 			nodeLabels.add(current);
 		}
 		else if(current.length() <= max)
 		{
 			for(SuffixTreeNode child : start.children)
 			{
 				getAllInternalNodesOfLength(child, nodes, nodeLabels, current+child.incomingEdge.getLabel(word), min, max);
 			}
 		}
 	}
 	
 	public static void getLeafStringsFromNode(SuffixTreeNode start, String currentLabel, ArrayList<String> repeats)
 	{
 		if(start.children.size() == 0)
 		{
 			repeats.add(currentLabel);
 		}
 		else
 		{
 			for(SuffixTreeNode child : start.children)
 			{
 				getLeafStringsFromNode(child, currentLabel+child.incomingEdge.getLabel(word), repeats);
 			}
 		}
 	}
 	
 	//NOTE: This is naive implementation of lowest common ancestor algorithm
 	//Traces both nodes up the tree until a common node is reached
 	public static SuffixTreeNode findLowestCommonAncestor(SuffixTreeNode node1, SuffixTreeNode node2)
 	{
 		SuffixTreeNode t1 = node1;
 		SuffixTreeNode t2 = node2;
 		
 		while(t1.incomingEdge != null && t2.incomingEdge != null && t1.compareTo(t2, word) != 0)
 		{
 			t1 = t1.parent;
 			t2 = t2.parent;
 		}
 		
 		if(t1.incomingEdge == null)
 		{
 			//Node 1 hit the root so that is lca
 			return t1;
 		}
 		else
 		{
 			//Either Node 2 hit the root or the nodes are equal so it is lca
 			return t2;
 		}
 	}
 	
 	//NOTE: Returns null if no prefix of s1 is found
 	public static SuffixTreeNode findNode(SuffixTreeNode start, String s1)
 	{
 		
 		SuffixTreeNode node = start;
 		String current = new String();
 		int prevCurrentLength = 0;
 		while(node.children.size() > 0)
 		{
 			prevCurrentLength = current.length();
 			for(SuffixTreeNode child : node.children)
 			{
 				String temp = current+child.incomingEdge.getLabel(word);
 				if(temp.equals(s1))
 				{
 					return child;
 				}
 				else if(s1.startsWith(temp))
 				{
 					current += child.incomingEdge.getLabel(word);
 					node = child;
 					break;
 				}
 			}
 			if(current.length() == prevCurrentLength)
 			{
 				//Iterated through all children and did not update node
 				break;
 			}
 		}
 		
 		if(current.length() == 0)
 		{
 			return null;
 		}
 		else
 		{
 			return node;
 		}
 	}
 	
 	//NOTE: returns null if strings s1 or s2 were not found in tree
 	public static String getLongestCommonExtension(SuffixTreeNode start, String s1, String s2)
 	{
 		SuffixTreeNode n1 = SuffixTree.findNode(start, s1);
 		SuffixTreeNode n2 = SuffixTree.findNode(start, s2);
 		
 		if(n1 != null && n2 != null)
 		{
 			SuffixTreeNode lca = SuffixTree.findLowestCommonAncestor(n1, n2);
 			String extension = new String();
 			while(lca.incomingEdge != null)
 			{
 				extension = lca.incomingEdge.getLabel(word) + extension;
 				lca = lca.parent;
 			}
 			return extension;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	public static void printTree(SuffixTreeNode start, int depth)
 	{
 		int i;
 		if(start.incomingEdge != null)
 		{
 			System.out.print("Depth " + depth + ": " + start.incomingEdge.getLabel(word));
 			if(start.children.size() == 0)
 			{
 				System.out.print(" (Leaf Node) - Start: " + start.incomingEdge.labelStartPos + ", End: " + start.incomingEdge.labelEndPos + " - Iteration Positions: ");
 				int j;
 				for(j = 0; j < start.positions.size() - 1; j++)
 				{
 					System.out.print(start.positions.get(j) + ", ");
 				}
 				System.out.println(start.positions.get(j));
 			}
 			else
 			{
 				System.out.println(" (Internal Node) Start: " + start.incomingEdge.labelStartPos + ", End: " + start.incomingEdge.labelEndPos);
 			}
 		}
 		else
 		{
 			System.out.println("Depth " + depth + ": ROOT");
 		}
 		for(i = 0; i < start.children.size(); i++)
 		{
 			printTree(start.children.get(i), depth+1);
 		}
 	}
 
     
 }
