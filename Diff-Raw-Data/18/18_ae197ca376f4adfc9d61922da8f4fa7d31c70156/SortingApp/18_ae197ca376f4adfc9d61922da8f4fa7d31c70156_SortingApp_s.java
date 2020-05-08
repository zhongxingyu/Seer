 package Sorting;
 
 // fill in your information in the line below, and comment it out in order to compile
// Name: ???   Student ID: ???;
 
 import java.text.DecimalFormat;
 
 public class SortingApp {
 	
 	protected static boolean buggy = true;
 
 	
 	public static void nonrecMergeSort(long[] a) {
 
 		long[] workSpace = new long[a.length];
 		for (int k=1; k<a.length; k *= 2) {
 			for (int i=0; i<a.length; i += (k*2)) {
 				merge(a, workSpace, i, i+k, i+k+k-1);
 				swap(a, 0, a.length-1);
 			}
 		}
 	}//nonrecMergeSort()
 	
 	
 	//-------------------------------------------------------------
 	//---------- Below is an implementation of Bubble Sort ----------
 	//-------------------------------------------------------------		
 	public static void bubbleSort(long[] a) {
 		int out, in;
 		for(out = a.length-1; out > 0; out--) {
 			for(in = 0; in < out; in++) {
 				if(a[in] > a[in+1]) {
 					swap(a, in, in+1);
 				}//if
 			}//for
 		}//for
 	}//bubbleSort()
 	
 	
 	//-----------------------------------------------------------------------
 	//---------- Below is an implementation of recursive Mergesort ----------
 	//-----------------------------------------------------------------------
 	public static void recMergeSort(long[] a) {
 		recMergeSort(a, new long[a.length], 0, a.length-1);
 	}//recMergeSort()
 	
 	protected static void recMergeSort(long[] a, long[] workSpace, int lowerBound, int upperBound) {
 		if(lowerBound == upperBound)            //if range is 1,
 			return;                              //no use sorting
 		else {                                  //find midpoint
 			int mid = (lowerBound + upperBound) / 2;
 			recMergeSort(a, workSpace, lowerBound, mid);         //sort low half
 			recMergeSort(a, workSpace, mid+1, upperBound);       //sort high half
 			merge(a, workSpace, lowerBound, mid+1, upperBound);  //merge them
 		}//else
 	}//recMergeSort()
 	
 	protected static void merge(long[] a, long[] workSpace, int lowPtr, int highPtr, int upperBound) {
 		int j = 0;                             // workspace index
 		int lowerBound = lowPtr;
 		int mid = highPtr-1;
 		int n = upperBound-lowerBound+1;       // # of items
 		
 		while(lowPtr <= mid && highPtr <= upperBound)
 			if(a[lowPtr] < a[highPtr] )
 				workSpace[j++] = a[lowPtr++];
 			else
 				workSpace[j++] = a[highPtr++];
 		
 		while(lowPtr <= mid)
 			workSpace[j++] = a[lowPtr++];
 		
 		while(highPtr <= upperBound)
 			workSpace[j++] = a[highPtr++];
 		
 		for(j=0; j<n; j++)
 			a[lowerBound+j] = workSpace[j];
 	}  // end merge()
 	
 	
 	//-------------------------------------------------------------
 	//---------- Below is an implementation of Quicksort ----------
 	//-------------------------------------------------------------	
 	public static void quicksort(long[] a) {
 		quicksort(a, 0, a.length-1);
 	}//quicksort()
 	
 	protected static void quicksort(long[] a, int left, int right) {
 		if(right - left <= 0)
 			return;
 		int pIdx = partition(a, left, right, right);	// always uses the right end element as pivot 
 		quicksort(a, left, pIdx-1);
 		quicksort(a, pIdx+1, right);
 	}//quicksort()
 	
 	// partition method
 	protected static int partition(long[] a, int left, int right, int pIdx) { 
 		long pivot = a[pIdx];
 		swap(a, pIdx, right);
 		int storeIndex = left;
 		for(int i=left; i<right; i++) {
 			if(a[i] <= pivot)
 				swap(a, i, storeIndex);
 
 		}//for
 		swap(a, right, storeIndex);
 		return storeIndex;
 	}//partition
 	
 	
 	//------------------------------------------------------
 	//---------- below are several helper methods ----------
 	//------------------------------------------------------
 		
 	// This creates an array with randomly generated elements
 	public static long[] randArray(int n) {
 		long[] rand = new long[n];
 		for(int i=0; i<n; i++)
 			rand[i] = (int) (Math.random() * n * 10);
 		return rand;
 	}//randArray()
 	
 	public static void startTimer() { 
 		timestamp = System.nanoTime();
 	}//startTimer()
 	
 	public static double endTimer() {
 		return (System.nanoTime() - timestamp)/1000000.0;
 	}//endTimer()
 	
 	public static void swap(long[] a, int i, int j) {
 		long temp = a[i];
 		a[j] = a[i];
		a[j] = temp;
 	}//swap
 	
 	private static long timestamp;
 	
 	//---------------------------------------------
 	//---------- This is the main method ----------
 	//---------------------------------------------		
 	public static void main(String[] args) {
 		
		
 		// After you have tested your non-recursive merge sort above,
 		// uncomment the following section (by removing the the two lines marked 'remove this line').
 		///* <---- remove this line --->
 		//run experiments
 		final int BUBBLE = 0, REC_MERGE = 1, NONREC_MERGE = 2, QUICK = 3;
 		
 		// max defines the maximum number of elements to be tested, which is 2^max
 		// runs defines the number rounds to be performed per test, in order to get an average running time.
 		int max = 14, runs = 5;
 		double[][] stats = new double[4][max];
 		for(int i=0; i<4; i++) {             //loop through each sorting algorithm
 			switch(i) {
 				case BUBBLE: System.out.print("Running Bubble Sort..."); break;
 				case REC_MERGE: System.out.print("Running Recursive Merge Sort..."); break;
 				case NONREC_MERGE: System.out.print("Running Non-Recursive Merge Sort..."); break;
 				case QUICK: System.out.print("Running Quicksort..."); break;
 			}//switch
 			for(int j=0; j<max; j++) {        //loop through size of data set
 				double avg = 0;
 				for(int k=0; k<runs; k++) {    //compute time for a number of runs
 					long[] a = randArray((int) Math.pow(2, j+1));
 					startTimer();
 					switch(i) {
 						case BUBBLE: bubbleSort(a); break;
 						case REC_MERGE: recMergeSort(a); break;
 						case NONREC_MERGE: nonrecMergeSort(a); break;
 						case QUICK: quicksort(a); break;
 					}//switch
 					avg += endTimer();
 				}//for
 				avg /= runs;
 				stats[i][j] = avg;
 			}//for
 			System.out.println("done.");
 		}//for
 		
 		DecimalFormat format = new DecimalFormat("0.0000");
 		System.out.println();
 		System.out.println("Average running time:");
 		System.out.println("    N\t |Bubble Sort\t |Rec Mergesort  |nonRec Merge   |Quicksort\t |");
 		System.out.println("---------|---------------|---------------|---------------|---------------|");
 		for(int i=0; i<stats[0].length; i++) {
 			System.out.print((int) Math.pow(2, i+1) + "\t  ");
 			for(int j=0; j<stats.length; j++) {
 				System.out.print(format.format(stats[j][i]) + "\t  ");
 			}//for
 			System.out.println();
 		}//for
 		System.out.println("---------|---------------|---------------|---------------|---------------|");
 		//<--- remove this line ---> */
 		
 	}//main()
 	
 }//Assignment5
