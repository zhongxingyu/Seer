 /* Name: Alexandra (Sasha) Babayan & Brian Park
  * Date: 5/1/2013
  * Project 2
 */
 
 /**
  * FourHeap is an implementation of PriorityQueue that 
  * represents a heap structure with four branches per node.
  * The root of the heap will always be the minimum value in
  * the heap, making it easy to remove elements of a small priority.
  */
 import java.util.NoSuchElementException;
 
 public class FourHeap<E> implements PriorityQueue<E>{
 	private Comparator<? super E> comparator; 
 	private E[] arrayHeap;
 	private int size; 
 	
 	/**
 	 * Given a Comparator c, constructs a FourHeap object
 	 * Creates an empty FourHeap.
 	 * Takes a Comparator as an argument.
 	 */
 	public FourHeap(Comparator<? super E> c) {
 		comparator = c;
 		arrayHeap = (E[]) new Object[10];
 		size = 0;
 	}
 	
 	/**
 	 * Takes and inserts an element into the FourHeap.
 	 * Increases size of FourHeap.
 	 */
 	@Override
 	public void insert(E item) {
 		if (arrayHeap.length == size){
 			arrayHeap = doubleArray(arrayHeap);
 		}
 		size++;
 		arrayHeap[size-1]= item;
 		percolateUp(size-1);
 	}
 
 	/**
 	 * Returns the minimum value in the FourHeap.
 	 * Throws NoSuchElementException if FourHeap is empty.
 	 * Does not modify the FourHeap.
 	 */
 	@Override
 	public E findMin() {
 		if (isEmpty()) {
 			throw new NoSuchElementException();
 		}
 		return arrayHeap[0]; 
 	}
 
 	/**
 	 * Returns the minimum value in the FourHeap.
 	 * Throws NoSuchElementException if FourHeap is empty.
 	 * Modifies FourHeap by deleting the value.
 	 * Decreases Size of FourHeap.
 	 */
 	@Override
 	public E deleteMin() {
 		if (isEmpty()) {
 			throw new NoSuchElementException();
 		}
 		E min = arrayHeap[0];
		arrayHeap[0] = arrayHeap[size-1];
 		percolateDown(0);
 		size--;
 		return min;
 	}
 
 	/**
 	 * Returns a boolean on whether or not the FourHeap
 	 * is currently empty.
 	 */
 	@Override
 	public boolean isEmpty() {
 		return (size == 0);
 	}
 
 	/**
 	 * Private method that assists in doubling the size of the
 	 * structures that represent the FourHeap.
 	 * Takes the original array and returns a doubled array
 	 * with the same elements.
 	 */
 	private E[] doubleArray(E[] original) {
 		E[] newArray = (E[])new Object[original.length * 2];
 		for(int i = 0; i < original.length; i++) {
 			newArray[i] = original[i];
 		}
 		return newArray;
 	} 
 	
 	/**
 	 * Private method that moves an element up the FourHeap
 	 * if it is less that the value of its parent.
 	 * Modifies FourHeap structure.
 	 * Takes an index to be filled by percolating.
 	 */
 	private void percolateUp(int index) {
 		boolean done = false;
 		while(!done && index > 0){
 			int parent = (int) Math.floor((index-1)/4);
 			if(comparator.compare(arrayHeap[index], arrayHeap[parent]) < 0) {
 				E temp = arrayHeap[index];
 				arrayHeap[index] = arrayHeap[parent];
 				arrayHeap[parent] = temp;
 				index = parent;
 			} else {
 				done = true;
 			}
 		}
 	}
 
 	
 	/**
 	 * Private method that moves an element down the FourHeap
 	 * if it is greater that the value of its parent.
 	 * Modifies FourHeap structure.
 	 * Takes an index to be filled by percolating.
 	 */
 	private void percolateDown(int index) {
 		//children = (4*index) + 1, (4*index) +2, (4*index) +3, (4*index) +4
 		boolean done = false;
 		while(!done) {		
 			int i = (index*4)+1;
 			int min = index;
 			while(i <= size-1 && i <= (index*4) + 4 ) {
 				if(comparator.compare(arrayHeap[i], arrayHeap[min]) < 0){
 					min = i;
 				}
 				i++;
 			}
 			if(comparator.compare(arrayHeap[index], arrayHeap[min]) > 0) {
 				E minimumValue = arrayHeap[min];
 				arrayHeap[min] = arrayHeap[index];
 				arrayHeap[index] = minimumValue;
 				index = min;
 			} else {
 				done = true ;
 			}
 		}
 	}
 	
 }
