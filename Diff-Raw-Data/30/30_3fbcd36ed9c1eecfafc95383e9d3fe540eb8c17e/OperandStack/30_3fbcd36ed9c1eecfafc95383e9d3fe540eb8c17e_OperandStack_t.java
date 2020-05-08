 package hash.simplevm;
 
 import hash.runtime.Context;
 
 import java.util.EmptyStackException;
 
 public class OperandStack {
 
 	private int size = 0;
 	private Node top;
 	private Context locals;
 
 	public OperandStack(Context locals) {
 		this.locals = locals;
 	}
 
	public void push(Object obj) {
 		top = new Node(obj, top);
		size++;		
 	}
 
 	public Object peek() {
 		if (top != null)
 			return top.value;
 		return null;
 	}
 
 	public Object pop() {
 		if (top == null)
 			throw new EmptyStackException();
 		Node rv = top;
 		top = rv.previous;
 		size--;
		locals.setLastEvaluationResult(rv.value);
 		return rv.value;
 	}
 
 	public int size() {
 		return size;
 	}
 
 	private static class Node {
 		public Node(Object v, Node p) {
 			value = v;
 			previous = p;
 		}
 
 		public Object value;
 		public Node previous;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("[");
 		Node n = top;
 		while (n != null) {
 			if (n.value != null)
 				sb.append(n.value.toString());
 			else
 				sb.append("null");
 			sb.append(", ");
 			n = n.previous;
 		}
 		sb.delete(sb.length() - 2, sb.length());
 		sb.append("]");
 		return sb.toString();
 	}
 }
