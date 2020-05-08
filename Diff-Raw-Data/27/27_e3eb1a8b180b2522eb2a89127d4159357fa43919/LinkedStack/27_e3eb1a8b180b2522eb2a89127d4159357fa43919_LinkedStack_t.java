 package spacegamecraft;
 
 
 
 public class LinkedStack<T> {
 	public Node<T> root;
 	public int height;
 	public LinkedStack(T start) {
 		this.root = new Node<T>(start, null);
 		height = 0;
 	}
 	
 	public void push(T data) {
 		root = new Node<T>(data, root);
 	}
 	
 	public T pop() {
 		T result = root.d;
 		root = root.n;
 		
 		return result;
 	}
 	
 	public T peek() {
 		return root.d;
 	}
 }
 
class Node<T> {
	public T d;
	public Node<T> n;
	public Node(T d, Node<T> n){ 
		this.d = d;
		this.n = n;
	}
}
