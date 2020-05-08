 import java.util.ArrayList;
 
 
 public class DaryHeap implements Heap {
 
 	private int size;
 	private ArrayList<AdjListNode> contents;
 	private int d;
 	
 	public DaryHeap(int d) {
 		this.size = 0;
 		this.contents = new ArrayList<AdjListNode>();
 		this.d = d;
 	}
 	
 	/**
 	 * Gets the index of the node's first child using array arithmetic
 	 * 
 	 * @param parent
 	 * @return
 	 */
 	private int getFirstChildIndex(int parent) {
 		return this.d*parent + 1;
 	}
 	
 	/**
 	 * Gets the index of the node's parent using array arithmetic
 	 * 
 	 * @param child
 	 * @return
 	 */
 	private static int getParent(int child) {
 		return (child - 1) / 2;
 	}
 	
 	@Override
 	public boolean isEmpty() {
 		return (this.size == 0);
 	}
 
 	@Override
 	/**
 	 * Inserts a value into the heap.
 	 */
 	public void insert(AdjListNode value) {
 		if (this.isEmpty()) {
 			this.contents.add(value);
 		} //                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
 		else {
 			int position = this.size;
 			this.contents.add(null); //ensure no out of bounds
 			int p = getParent(position);
 			System.out.println("Parent: " + p);
 			AdjListNode parent = this.contents.get(getParent(position));
 			System.out.println("Array size: " + this.contents.size());
 			System.out.println("Position: " + position);
 			while (position > 0 && value.compareTo(parent) < 0) {
 				this.contents.set(position, parent);
 				parent = contents.get(getParent(position));
 				position = getParent(position);
 				this.expand(position);
 			}
 			this.expand(position);
 			this.contents.set(position, value);
 		}
 		this.size++;
 	}
 	
 	private void expand(int n) {
 		int current_size = this.contents.size();
 		for (int i = current_size; i <= n; i++) {
 			this.contents.add(null);
 		}
 	}
 
 	@Override
 	public AdjListNode deleteMin() {
 		if (this.isEmpty())
 			return null;
 		AdjListNode min = contents.get(0);
 			
 		AdjListNode last = contents.get(this.size - 1);
 		contents.set(0, last);
 		// the node that will "trickle down" to the bottom
 		// of the heap
 		AdjListNode shiftingNode = last;
 		int position = 0;
 		int childIndex = getFirstChildIndex(position);
 		
 		while (childIndex <= this.size) {
 			childIndex = getFirstChildIndex(position);
 			int tempIndex = getFirstChildIndex(position);
 			
 			// iterate this node's children to
 			// find its minimum child
 			for (int i = 0; i < d; i++) {
 				if (tempIndex >= this.size)
 					break;
 				tempIndex++;
 				if (contents.get(tempIndex).compareTo(contents.get(childIndex)) < 0)
 					childIndex = tempIndex;
 			}
 			
 			// if shifting node is greater than the minimum child,
 			// make the swap
 			if (contents.get(childIndex).compareTo(shiftingNode) < 0)
 				contents.set(position, contents.get(childIndex));
 			else // we have already found the right place for the node
 				break;
 			
 			position = childIndex;
 		}
 		
 		// place shifting node into the correct position
 		contents.set(position, shiftingNode);
 		return min;
 	}
 	
 	public void printHeap() {
 		String str = "";
 		for (AdjListNode node : this.contents) {
 			if (node != null)
 				str += node.getWeight() + "\t";
 		}
 		System.out.println(str);
 	}
 	
 	public static void main(String[] args) {
 		DaryHeap heap = new DaryHeap(2);
 		assert(heap.isEmpty());
 		double[] values = {3.0, 4.0, 1.0, 5.0, 2.0, 6.0};
 		int i = 0;
 		for (double d : values) {
 			AdjListNode node = new AdjListNode(i, d);
 			heap.insert(node);
 			System.out.println("Adding " + d);
 			heap.printHeap();
 			i++;
 		}
 		heap.printHeap();
 		assert(!heap.isEmpty());
 		AdjListNode min = heap.deleteMin();
 		assert(min.getWeight() < 2.0);
 		assert(min.getVertex() == 2);
 		for (int j = 0; j < 5; j++) {
 			heap.deleteMin();
 		}
 		assert(heap.isEmpty());
 	}
 
 }
