 package awesome.tony.fastfind;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class ByteArrayAhoCorasick {
	public static class Node{
 		final private Node neighbors[] = new Node[256];
 		private Node failure = null;
 		private byte[] match = null;
 
		public Node(byte[] match) {
 			this.matches = true;
 			this.match = match; 
 		}
		public Node() {
 			this.matches = false;
 		}
 		boolean matches = false;
		public void setFail(Node fail) {
 			this.failure = fail;
 		}
		public Node getFail() {
 			return failure;
 		}
		public boolean isMatch(){
 			return matches;
 		}
		public void setMatch(byte[] match) {
 			matches = true;
 			this.match = match;
			
 		}
 	}
 
 	class SearchNugget{
 		private SearchNugget(Node node) {
 			this.current = node;
 		}
 		private Node current;
 	}
 	
 	final Node root = new Node();
 
 	public void addMatch(byte [] match){
 		addMatchRecursive(match, 0, root);
 	}
 	private void addMatchRecursive(byte match[], int offset, Node current){
 		int index = ((int)match[offset])&(0xff);
 		boolean atEnd = (offset == (match.length - 1));
 		if(current.neighbors[index] == null){
 			if(atEnd == true){
 				current.neighbors[index] = new Node(match);
 				return;
 			}
 			current.neighbors[index] = new Node();	
 		}
 		else if(atEnd == true){
 			current.neighbors[index].setMatch(match);
 			return;
 		}
 		addMatchRecursive(match, offset + 1 , current.neighbors[index]);
 	}
 
 	public void finalize(){
 		// perform bfs to build links
 		Queue<Node> queue = new LinkedList<Node>();
 		queue.add(root);
 		root.setFail(null);
 		while (!queue.isEmpty()) { 
 			Node current = queue.poll();
 			for(int i = 0; i < 256; i++) {
 				Node next = current.neighbors[i];
 				if (next != null) { 
 					// traverse failure to get state
 					Node fail = current.getFail();
 					while((fail != null) && fail.neighbors[i] == null){
 						fail = fail.getFail(); 
 					} 
 					if (fail != null) { 
 						next.setFail(fail.neighbors[i]); 
 					} else {
 						next.setFail(root); 
 					} 
 					queue.add(next); 
 				} 
 			} 
 		}
 
 	}
 
 	
 	public void evaluate(byte [] bytes){
 		evaluate(bytes, null);
 	}
 	
 	public SearchNugget startMultiCallSearch(){
 		return new SearchNugget(root);
 	}
 	
 	public void evaluate(SearchNugget nugget, byte[] bytes){
 		evaluate(bytes, nugget);
 	}
 	
 	private void evaluate(byte[] bytes, SearchNugget nugget) {
 		Node current = root;
 		if(nugget != null){
 			current = nugget.current;
 			if(current == null) throw new IllegalArgumentException("bad search nugget");
 		}
 		int index = 0;
 		while (index < bytes.length) { 
 			int currentChar = ((int)bytes[index++]) & 0xff; 
 			Node next = current.neighbors[currentChar]; 
 			if (next == null) { 
 				next = current.getFail();
 				while ((next != null) && next.neighbors[currentChar] == null) { 
 					next = next.getFail(); 
 				} 
 				if (next != null) { 
 					next = next.neighbors[currentChar]; 
 				} 
 				else { 
 					next = root; 
 				} 
 			} 
 			// Accept condition
 			if(next.isMatch()){
 				System.out.println("match: " + new String(next.match));
 			}
 			if(next.failure != null && next.failure.isMatch()){
 				System.out.println("match: " + new String(next.failure.match));
 
 			}
 			current = next; 
 		} 
 		if(nugget != null){
 			nugget.current = current;
 		}
 	}
 
 	public static void main(String args[]){
 		ByteArrayAhoCorasick b = new ByteArrayAhoCorasick();
 		b.addMatch("foo".getBytes());
 		b.addMatch("bar".getBytes());
 		b.addMatch("baz".getBytes());
 		b.addMatch("bufoon".getBytes());
 		b.addMatch("burden".getBytes());
 		b.addMatch("over".getBytes());
 		b.finalize();
 		SearchNugget n = b.startMultiCallSearch();
 		
 		b.evaluate("the brown foo fox something something bufo".getBytes(), n);
 		b.evaluate("ox".getBytes(), n);
 	}
 
 }
