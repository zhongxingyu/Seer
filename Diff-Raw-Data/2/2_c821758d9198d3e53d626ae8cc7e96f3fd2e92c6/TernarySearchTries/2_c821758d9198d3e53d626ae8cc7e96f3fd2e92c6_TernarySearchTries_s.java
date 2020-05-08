 package dataStructure;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 
 public class TernarySearchTries<Value> {
 
 	private Node root;
 	private class Node
 	{
 		char c;
 
 
 		Node left, mid, right, post;
 		int postal;
 
 		HashSet<Value> val = new HashSet<Value>();
 	}
 	public HashSet<Value> get(String key)
 	{
 		Node x = get(root, key, 0);
 		if (x == null) return null;
 		System.out.println(x.val);
 		return  x.val;
 	}
 	
 	private Node get(Node x, String key, int d)
 	{
		if( x == null) return null;
 		char c = key.charAt(d);
 		if 		(c < x.c) 			 return get(x.left, key, d);
 		else if (c > x.c) 			 return get(x.right, key, d);
 		else if (d < key.length()-1) return get(x.mid, key, d+1);
 		else return x;
 	}
 	
 	public Node get(String key, int postal){
 		Node x = get(root, key, 0);
 		if(x.postal == postal) return x;
 		else return x.post;
 	}
 	public void put(String key, Value val, int postal){
 		key = key.toLowerCase();
 	 root = put(root, key, val, 0, postal);}
 	
 	public Node put(Node x, String key, Value val, int d, int postal)
 	{
 		char c = key.charAt(d);
 		if (key == "" || key == "''") return null;
 
 		if( x == null) { x = new Node(); x.c = c; x.postal = postal;}
 		if (c < x.c) x.left = put(x.left, key, val, d, postal);
 		else if (c > x.c) x.right = put(x.right, key, val , d, postal);
 		else if (d < key.length()-1) x.mid = put(x.mid, key, val, d+1, postal);
 		else if (postal != x.postal) x.post = put(x.post, key, val, d, postal);
 		else x.val.add(val);
 		return x;
 	
 	}
 
 	public Iterable<Integer> keysWithPrefix(String pre){
 		pre = pre.toLowerCase();
 		LinkedList<Integer> q = new LinkedList<Integer>();
 		collect(get(root, pre, 0), q);
 		return q;
 	}
 	
 	private void collect(Node x, LinkedList<Integer> q){
 		if( x == null) return;
 		if( q.isEmpty()){
 			if( x.val != null && x.val.size() > 0) q.add((Integer) x.val.iterator().next());
 			collect(x.mid,q);
 			collect(x.post,q);
 		}else{
 		if( x.val != null && x.val.size() > 0) q.add((Integer) x.val.iterator().next());
 			collect(x.left, q);
 			collect(x.right, q);
 			collect(x.mid, q);
 			collect(x.post,q);
 		}
 	}
 }
