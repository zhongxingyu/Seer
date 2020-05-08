 /**
  * BinomialHeap
  *
  * An implementation of binomial heap over non-negative integers.
  */
 public class BinomialHeap
 {
 	
 	private static int EMPTY_HEAP_SIZE = -1; // TODO: check
 
 	private HeapNode min;
 	private HeapNode first;
 	private HeapNode last;
 	private int size; // Number of elements in the heap
 	private int treesNum;
 	private int[] treesSize;
 	
 	private void reset() {
 		min = null;
 		first = null;
 		last = null;
 		size = EMPTY_HEAP_SIZE;
 		treesNum = 0;
 		treesSize = null;
 	}
 	
 	public BinomialHeap() {
 		this(null);
 	}
 	
 	public BinomialHeap(HeapNode node) {
 		this.min = node;
 		this.first = node;
 		this.last = node;
 		if (node != null) {
			this.size = node.getTree().getSize();
 			treesNum = 1;
 			buildTreeSizes();
 		} else {
 			this.size = EMPTY_HEAP_SIZE;
 			treesNum = 0;
 			this.treesSize = null;
 		}
 	}
 	
    /**
     * public boolean empty()
     *
     * precondition: none
     * 
     * The method returns true if and only if the heap
     * is empty.
     *   
     */
     public boolean empty()
     {
     	return this.size == EMPTY_HEAP_SIZE;
     }
 		
    /**
     * public void insert(int value)
     *
     * Insert value into the heap 
     *
     */
     public void insert(int value) 
     {    
     	BinomialHeap heapToMeld;
     	HeapNode newHeapNode = new HeapNode(value);
     	if(empty()) {
     		this.first = newHeapNode;
     		this.last = newHeapNode;
     		this.min = newHeapNode;
     	} else {
     		heapToMeld = new BinomialHeap(newHeapNode);
     		this.meld(heapToMeld);
     	}
     	
     	this.size++;
     }
 
    /**
     * public void deleteMin()
     *
     * Delete the minimum value
     *
     */
     public void deleteMin()
     {
      	return; // should be replaced by student code
      	
     }
 
    /**
     * public int findMin()
     *
     * Return the minimum value
     *
     */
     public int findMin()
     {
     	return this.min.getValue();
     } 
     
    /**
     * public void meld (BinomialHeap heap2)
     *
     * Meld the heap with heap2
     *
     */
     public void meld (BinomialHeap heap2)
     {
     	HeapNode result = new HeapNode(-1);
     	HeapNode curr1 = this.getFirst();
     	HeapNode curr2 = heap2.getFirst();
     	HeapNode saved = null;
     	HeapNode tmp;
     	
     	while(curr2 != null) {
     		/*
     		if(curr1.getRank() == curr2.getRank()) {
     			if (saved != null) {
     				if (saved.getRank() == curr1.getRank()) {
     					result.setNext(saved);
     					saved = null;
     				}
     			}
     			curr1.linkWith(curr2);
     			saved = curr1;
     		} else if(curr1.getRank() > curr2.getRank()) {
     			if (saved != null && saved.getRank() == curr2.getRank()) {
     				curr2.linkWith(saved);
     				saved = curr2;
     			}
     			result.setNext(curr2);
     		} else {
     			
     		}
     		*/
     		
     		if (curr1.getRank() < curr2.getRank()) {
     			if (saved != null) {
     				if (saved.getRank() < curr1.getRank()) {
     					result.setNext(saved);
     					saved = null;
     				} else { // curr1.getRank() == saved.getRank()
     					//if (curr1.getValue() < saved)
     				}
     			}
     			
     			result.setNext(curr1);
     			curr1 = curr1.getNext();
     		} else if (curr2.getRank() < curr1.getRank()) {
     			if (saved != null) {
     				if (saved.getRank() < curr2.getRank()) {
     					result.setNext(saved);
     					saved = null;
     				}
     			}
     			
     			result.setNext(curr2);
     			curr2 = curr2.getNext();
     		} else { // ==
     			
     			if (curr1.getValue() < curr2.getValue()) {
     				curr1.linkWith(curr2);
     				saved = curr1;
     			} else {
     				curr2.linkWith(curr1);
     				saved = curr2;
     			}
     			
     			curr1 = curr1.getNext();
     			curr2 = curr2.getNext();
     		}
     	}
     }
 
    public HeapNode getFirst() {
 	return first;
 }
 
 public void setFirst(HeapNode first) {
 	this.first = first;
 }
 
 public HeapNode getLast() {
 	return last;
 }
 
 public void setLast(HeapNode last) {
 	this.last = last;
 }
 
 /**
     * public int size()
     *
     * Return the number of elements in the heap
     *   
     */
     public int size()
     {
     	return this.size;
     }
     
    /**
     * public int[] treesSize()
     *
     * Return an array containing the sizes of the trees that represent the heap
     * in ascending order.
     * 
     */
     public int[] treesSize()
     {
         return this.treesSize;
     }
 
    /**
     * public void arrayToHeap()
     *
     * Insert the array to the heap. Delete previous elemnts in the heap.
     * @author sakhbak1
     */
     public void arrayToHeap(int[] array)
     {
     	this.reset();
     	for(int i : array) {
     		this.insert(i);
     	}
     }
 	
    /**
     * public boolean isValid()
     *
     * Returns true if and only if the heap is valid.
     *   
     */
     public boolean isValid() 
     {
     	return false; // should be replaced by student code
     }
     
     private void buildTreeSizes() {
     	this.treesSize = new int[this.treesNum];
     	HeapNode current = first;
     	int i = 0;
     	while(current != null) {
     		this.treesSize[i++] = current.getSize();    		
     		current = current.getNext();
     	}
     }
     
    /**
     * public class HeapNode
     * 
     * If you wish to implement classes other than BinomialHeap
     * (for example HeapNode), do it in this file, not in 
     * another file 
     *  
     */
     public class HeapNode {
 
     	// Members 
     	
     	private int value;
     	private int rank;
     	private int size;
       	private HeapNode next;
     	private HeapNode prev;
     	private HeapNode leftMostChild;
     	
     	// C'tors
     	
 		public HeapNode(int value) {
     		this.value = value;
     	}
     	
     	// Getters and Setters
     	
 		public HeapNode getNext() {
 			return next;
 		}
 		public void setNext(HeapNode next) {
 			this.next = next;
 		}
 		public HeapNode getPrev() {
 			return prev;
 		}
 		public void setPrev(HeapNode prev) {
 			this.prev = prev;
 		}    	
     	public HeapNode getLeftMostChild() {
 			return leftMostChild;
 		}
 
 		public void setLeftMostChild(HeapNode leftMostChild) {
 			this.leftMostChild = leftMostChild;
 		}
     	public int getRank() {
     		return this.rank;
     	}
     	
     	public int getSize() {
     		return this.size;
     	}
     	
     	public int getValue() {
     		return this.value;
     	}
     	
     	public void setValue(int value) {
     		this.value = value;
     	}
     	
     	// Methods
     	
     	// TODO
     	public boolean isValid() {
     		return false;
     	}
     	
     	// TODO: whoever calls link needs to check the following assumptions:
     	// 1. other is a tree with a larger root than this
     	// 2. both trees must be of same rank
     	public void linkWith(HeapNode other) {
     		other.next = this.leftMostChild;
     		if(this.leftMostChild != null) {
     			this.leftMostChild.setPrev(other);
     			
     		}
     		this.leftMostChild = other;
     		
     		this.rank++;
     		this.size += other.size;
     	}
     }
 
 }
