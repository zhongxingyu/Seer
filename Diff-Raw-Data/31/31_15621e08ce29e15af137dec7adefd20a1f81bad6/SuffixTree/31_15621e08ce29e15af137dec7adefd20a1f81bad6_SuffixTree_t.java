 package utils;
 
 import java.lang.String;
 import java.util.ArrayList;
 
 public class SuffixTree {
 
 	public SuffixTreeNode root;
 	public static String word;
 	public int startPos;
 	public int endPos;
 	
 	public SuffixTree(String treeWord, int start, int end)
 	{
 		word = new String(treeWord+"$");
 		this.startPos = start;
 		this.endPos = end+1;
 		this.constructTree();
 	}
 	
 	public static void main(String[] args)
 	{
		String str = new String("TCTGAATTTT");
		SuffixTree tree = new SuffixTree(str, 1, str.length());
 		System.out.println("Done!");
 		printTree(tree.root, 0);
 		System.out.println();
 		
		ArrayList<String> repeats = findRepeats(tree, "TTT");
 
 		ArrayList<String> allStrings = new ArrayList<String>();
 
 		allStrings = findAllRepeatsOfLength(tree, 3, 4);
 		for(String repeat : repeats)
 		{
 			System.out.println(repeat);
 		}
 
 		System.out.println("All repeats:");
 		for(String s : allStrings)
 		{
 			System.out.println(s);
 		}
 		
 	}
 	
 	public void constructTree()
 	{
 		this.root = new SuffixTreeNode();
 		int i;
 		
 		for(i = 0; i < word.length(); i++)
 		{
 			this.addSuffix(this.startPos+i, this.endPos, word.substring(i), i+1);
 		}
 	}
 	
 	public void addSuffix(int start, int end, String suffix, int position)
 	{
 		StringBuilder tempSuffix = new StringBuilder(suffix);
 		SuffixTreeNode node = findPosition(this.root, tempSuffix);
 		int charsRemoved = suffix.length() - tempSuffix.length();
 		this.insertSuffix(start+charsRemoved, end, node, new String(tempSuffix), position);
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
 	
 	public void insertSuffix(int start, int end, SuffixTreeNode node, String suffix, int position)
 	{
		if(node.incomingEdge == null || suffix.startsWith(node.incomingEdge.getLabel(word)) || suffix.charAt(0) != node.incomingEdge.getLabel(word).charAt(0))
 		{
 			//Case 1: Only add leaf node
 			SuffixTreeNode leafNode = new SuffixTreeNode(new SuffixTreeEdge(start, end), node);
 			leafNode.position = position;
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
 			suffixNode.position = position;
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
 		SuffixTreeNode node = findPosition(tree.root, new StringBuilder(target));
 		
 		if(node.incomingEdge != null)
 		{
 			//Not Root Node
 			getAllStrings(node, repeats, target);
 		}
 		return repeats;
 		
 	}
 
      public static ArrayList<String> findAllRepeatsOfLength(SuffixTree tree, int min, int max)
      {
      	ArrayList<String> repeats = new ArrayList<String>();
 
      	ArrayList<String> allStrings = new ArrayList<String>();
 		getAllStrings(tree.root, allStrings, "");
 	 	
 	 	// iterate through every node - check its length - process and add to results
 		for(String s : allStrings) 
 		{
 			if(s.length() >= min && s.length() <= max)
 				repeats.addAll( findRepeats(tree, s) );
 		}
 
 		return repeats;
      }			     
 	
 	public static void getAllStrings(SuffixTreeNode start, ArrayList<String> repeats, String current)
 	{
 		if(start.children.size() == 0)
 		{
 			//Leaf Node
 			repeats.add(current);
 		}
 		for(SuffixTreeNode child : start.children)
 		{
 			
 			getAllStrings(child, repeats, current+child.incomingEdge.getLabel(word));
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
 				System.out.println(" (Leaf Node) - Start: " + start.incomingEdge.labelStartPos + ", End: " + start.incomingEdge.labelEndPos + " - Iteration Position: " + start.position);
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
