 package com.grosser;
 
 import com.grosser.array.IntArray;
 import com.grosser.array.UnsortedUniqueArray;
 import com.grosser.sort.BubbleSort;
 import com.grosser.sort.InsertionSort;
 import com.grosser.sort.SelectionSort;
 
 /**
  * Class for testing various data structures and sorting/searching algorithms.
  * @author joshuagrosser
  */
 public class Client extends Thread implements Runnable {
 
 	/**
 	 * Array size constant.
 	 */
	private static final int ARRAY_SIZE = 100;
 	
 	/**
 	 * Instance.
 	 */
 	private static Client client;
 	
 	/**
 	 * Generic unsorted unique array of size n.
 	 */
 	private IntArray genericUnsortedArray;
 	
 	/**
 	 * Main method. Starting point for the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		client = new Client();
 		client.start();
 	}
 
 	/**
 	 * Main thread.
 	 */
 	@Override
 	public void run() {
 		System.out.println("Performing bubble sort.");
 		doBubbleSort();
 		
 		System.out.println("Performing selection sort.");
 		doSelectionSort();
 		
 		System.out.println("Performing insertion sort.");
 		doInsertionSort();
 	}
 	
 	private void doBubbleSort(){
 		genericUnsortedArray = new UnsortedUniqueArray(ARRAY_SIZE);
		genericUnsortedArray.populate(100);
 		
 		genericUnsortedArray.print();
 		genericUnsortedArray = BubbleSort.sort(genericUnsortedArray);
 		genericUnsortedArray.print();
 	}
 	
 	private void doSelectionSort(){
 		genericUnsortedArray = new UnsortedUniqueArray(ARRAY_SIZE);
		genericUnsortedArray.populate(100);
 		
 		genericUnsortedArray.print();
 		genericUnsortedArray = SelectionSort.sort(genericUnsortedArray);
 		genericUnsortedArray.print();
 	}
 	
 	private void doInsertionSort(){
 		genericUnsortedArray = new UnsortedUniqueArray(ARRAY_SIZE);
		genericUnsortedArray.populate(100);
 		
 		genericUnsortedArray.print();
 		genericUnsortedArray = InsertionSort.sort(genericUnsortedArray);
 		genericUnsortedArray.print();
 	}
 }
