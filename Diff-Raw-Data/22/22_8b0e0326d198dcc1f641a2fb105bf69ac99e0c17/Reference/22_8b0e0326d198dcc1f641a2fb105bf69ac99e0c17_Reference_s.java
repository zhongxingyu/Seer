 package suite.node;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class Reference extends Node {
 
 	private Node node = this;
 	private final int id = counter.getAndIncrement();
 
 	private static final AtomicInteger counter = new AtomicInteger();
 
 	public void bound(Node node) {
 		this.node = node;
 	}
 
 	public void unbound() { // Happens during backtracking
 		node = this;
 	}
 
 	@Override
 	public Node finalNode() {
 		return node != this ? node.finalNode() : node;
 	}
 
 	@Override
 	public int hashCode() {
 		return node != this ? node.hashCode() : super.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object object) {
		return node != this ? node.equals(object) : super.equals(object);
 	}
 
 	public int getId() {
 		return id;
 	}
 
 }
