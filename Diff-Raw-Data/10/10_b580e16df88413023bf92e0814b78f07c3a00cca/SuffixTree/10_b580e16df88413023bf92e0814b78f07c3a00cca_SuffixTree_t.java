package utils;
 
 import java.lang.String;
 
 public class SuffixTree {
 
 	public SuffixTree(String word)
 	{
 		_word = word;
 		_root = new Node(this);
 	}
 
 	// TODO - Ukkonen specific stuff:
 	// public Node _active_node;
 	// public Edge _active_edge;
 
 	public Node _root;
 	public String _word;
 
 	private class Node {
 		public Node(SuffixTree tree) {
 			_tree = tree;
//			_edges = new ArrayList<Edge>();
//			_id; // TODO - generate unique node ID
 		}
 
 		private SuffixTree _tree;
 		public int _id;
 	}
 
 	private class Edge {
 
 		public Edge(SuffixTree tree, Node head) {
 			_tree = tree;
 			_head = head;
 			_start_index = 0; // _start_index = tree's current suffix index
//			_id; // TODO - generate unique edge number
 		}
 
 		private SuffixTree _tree;
 		private Node _head;
 		private Node _tail;
 		public int _start_index;
 		public int _end_index;
 		public int _id;
 		public int _length;
 	}
 }
