 
 public class MaxHeap {
 
 	private long[] heap;
 	private int last_elt;
 	
 	public MaxHeap(int size){
 		heap = new long[size];
 		last_elt = 0;
 	}
 	
 	public MaxHeap(long[] elts){
 		heap = new long[elts.length];
 		last_elt = 0;
 		for (int i = 0; i < elts.length; i++){
 			insert(elts[i]);
 		}
 	}
 	
 	public void insert(long elt){
 		int i = last_elt;
 		heap[last_elt] = elt;
 		last_elt++;
 
 		int parent = (i-1)/2;
 		while (i > 0 && heap[parent] < heap[i]){
 			//switch nodes
 			long temp = heap[parent];
 			heap[parent] = heap[i];
 			heap[i] = temp;
 			
 			i = parent; 
 			parent = (i-1)/2;
 		}
 		
 	}
 	
 	public long extractMax(){
 		if (last_elt > 0){
 			long max = heap[0];
 			heap[0] = heap[last_elt-1]; 
 		
 			last_elt--;
 			maxHeapify(0);
 		
 			return max;
		} else if(last_elt == 0) {
			last_elt--;
			return heap[0];
 		}
 		else
 			return -1;
 	}
 	
 	public int getSize(){
 		return last_elt;
 	}
 	
 	private void maxHeapify (int n){
 		int ind_l = n*2+1;
 		int ind_r = n*2+2;
 		int largest;
 		
 		if (ind_l < last_elt && heap[ind_l] > heap[n])
 			largest = ind_l;
 		else
 			largest = n;
 		
 		if (ind_r < last_elt && heap[ind_r] > heap[largest])
 			largest = ind_r;
 			
 		if (largest != n){
 			long temp = heap[n];
 			heap[n] = heap[largest];
 			heap[largest] = temp;
 			
 
 			maxHeapify(largest);
 		}
 			
 				
 	}
 	
 	public void printHeap(){
 		System.out.printf("Frontier index: %d\n", last_elt);
 		System.out.println("Heap: ");
 		for (int i = 0; i < last_elt; i++)
 			System.out.printf("index: %d, value: %d\n",i,heap[i]);
 		System.out.println();
 		
 	}
 }
 
 
