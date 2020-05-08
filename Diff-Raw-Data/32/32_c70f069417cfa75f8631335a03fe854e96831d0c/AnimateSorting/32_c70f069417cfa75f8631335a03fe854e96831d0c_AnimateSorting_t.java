 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Vector;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 /* CSCE 314 - ASSIGNMENT 7
  * Team: Long Tran, Chuong Trinh
  * I- Contribution: 
  * 			1. Long Tran: Bubble Sort, Shell Sort, Quick Sort
  * 			2. Chuong Trinh: Insert Sort, Merge Sort, Heap Sort
  * 
  * II - Compiling: Compile the AnimateSorting with javac or any Java IDE.
  * III - Using the program
  * Upon the start of the program, users will be prompted to enter the size of the array to be sorted.
  * Theorically, the size of the array is only limited to the size of the computer memory. However, to limited size of the
  * computer's screen, we found the animations work best on an array with less than 15 items. 
  * 
  *IV - Implementation: 
  * In this program, we used 6 independent threads for 6 sorting algorithms. Meanwhile, the animations will be done in the
  * main thread using 6 different timers. The timers will also interrupt the sorting threads to pause/resume the thread after
  * each swap.
  * 
  *V - Known Bugs
  *	1. The pause/resume buttons have not been implemented.
  *  2. Animation for heap sort has a bug which we could not find upon to the submission of this assignment.
 *  
 *  UPDATE (12/02/2013): The bug for heap sort has been found. It was a tricky bug in the animation thread. 
  *VI- Reference: 
  * 		1. Oracle Java Document.
  * 		2. "Data Structures & Algorithms in Java" - Robert Lafore	
  */
 
 
 public class AnimateSorting extends JPanel  {
 
 	
 	
 	 private JButton[] bubble_list_button;
 	 private JButton[] quick_list_button;
 	 private JButton[] shell_list_button;
 	 private JButton[] merge_list_button;
 	 private JButton[] heap_list_button;
 	 private JButton[] insert_list_button;
 	
 	 private JPanel jBubble;
 	 private JPanel jQuick;
 	 private JPanel jShell;
 	 private JPanel jMerge;
 	 private JPanel jHeap;
 	 private JPanel jInsert;
 	 
 	 
 	 //The clock is used for timing
 	 private int bubble_clk = 0;
 	 private int quick_clk = 0;
 	 private int shell_clk =0;
 	 private int merge_clk = 0;
 	 private int heap_clk=0;
 	 private int insert_clk=0;
 	 
 	 private static int[] list_bubble;
 	 private static int[] list_quick;
 	 private static int[] list_shell;
 	 private static int[] list_merge;
 	 private static int[] list_heap;
 	 private List<Integer> list = new LinkedList<Integer>();
 
 	 
 	 
 	 static AnimateSorting.BubbleSort bubble;
 	 static AnimateSorting.QuickSort quick;
 	 static AnimateSorting.ShellSort shell;
 	 static AnimateSorting.MergeSort merge;
 	 static AnimateSorting.HeapSort heap;
 	 static AnimateSorting.InsertSort insert;
 	 
 	 
 	 private Timer bubble_timer;
 	 private Timer quick_timer;
 	 private Timer shell_timer;
 	 private Timer merge_timer;
 	 private Timer heap_timer;
 	 private Timer insert_timer;
 	 
 	 
 /*-----------------INSERT SORT------------------------------------------*/	 
 	 
 public class InsertSort implements Runnable {
 			
 		boolean suspendFlag = false;
 	
 		
 		public void run() {
 					
 				for (int i =0; i<list.size();i++) {
 						
 						int temp = list.get(i);
 						
 						for (int k = i;k>0;k--) {
 							
 								System.out.printf(" ready to swap");
 								
 								if (list.get(k)<list.get(k-1)) {
 								 	swap(k,k-1);
 									System.out.printf(" Yes swapped: %d vs %d", list.get(k),list.get(k-1));
 								}
 								else 
 						 			System.out.printf(" No swapped");
 								
 								
 								synchronized(this) {
 									
 										while(suspendFlag) {
 							              
 												try {
 								              	 	wait();
 								        }
 												catch (InterruptedException e) {
 																	
 													System.out.println(" thread interrupted.");
 												}
 										}
 								}
 					}
 					
 				}
 				System.out.printf("\n ------DONE--------");
 				
 			}
 
 	void suspend() {
 			 suspendFlag = true;
 	}
 						
 	synchronized void resume() {
 			 suspendFlag = false;
 			 notify();
 	}
 }
 
 
 public void buildAnimation(final int x,final int y) {
 	 
 	 final int xCord = insert_list_button[x].getLocation().x;
 	 
 	 final int yCord = insert_list_button[x].getLocation().y;
 	
 	System.out.printf("\n in process x,y,xCord,yCord: %d,%d,%d,%d",x,y,xCord,yCord);
 	
 	insert_timer = new Timer (50, new ActionListener() {
 	 		public void actionPerformed(ActionEvent e) {
 				if (list.get(x) > list.get(y)) {
 						if (insert_list_button[x].getLocation().y != yCord-40 && insert_list_button[x].getLocation().x == xCord) {
 							//moving up if y >= high-20 and x the same
 							System.out.printf("\n moving up");
 							insert_list_button[x].setBounds(insert_list_button[x].getLocation().x,insert_list_button[x].getLocation().y-2,40,40);
 							repaint();
 															}
 						else if(insert_list_button[y].getLocation().x != xCord ) {
 							//moving the chosen value to the left and compared value to the right
 							insert_list_button[y].setBounds(insert_list_button[y].getLocation().x +1,insert_list_button[y].getLocation().y,40,40);
 							insert_list_button[x].setBounds(insert_list_button[x].getLocation().x -1,insert_list_button[x].getLocation().y,40,40);
 							
 							repaint();
 							} 
 
 						else {
 							if (insert_list_button[x].getLocation().y != yCord) {
 							insert_list_button[x].setBounds(insert_list_button[x].getLocation().x,insert_list_button[x].getLocation().y+2,40,40);
 							repaint();
 							}
 							else {
 							System.out.println(" done animation");
 			 		 
 			 		 		if (insert_timer.isRunning()){
 			 			  			insert_timer.stop();
 			 				 }
 							JButton temp = insert_list_button[y];
 							insert_list_button[y] = insert_list_button[x];
 							insert_list_button[x] = temp;
 					 		 insert.resume();
 			 				 repaint();
 							}
 						}
 							
 							
 							
 					
 					}
 				}
 		});
 }
 	 
 public void swap(int i, int j){
 	 insert.suspend();
 	 int temp = list.get(i);
 	 list.add(i,list.get(j));
 	 list.remove(i+1);
 	 list.add(j,temp);
 	 list.remove(j+1);
 	 buildAnimation(i, j);
 
 	 if (!insert_timer.isRunning()){
 		 System.out.println("---- Start Timer: ");
 		 insert_timer.start();
 	 }
 }
 	 
 	 
 /*-----------------HEAP SORT------------------------------------------*/
 	 
 public class HeapSort implements Runnable{
 
 	
 	boolean suspendFlag = false;
	boolean suspendFlag2 = false;
 	int size = list_heap.length;
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub	
 		//gotta build a heap from the array list_heap
		System.out.println("---------- START HEAPIFYING -----------");
 		for (int i = size/2 -1; i>=0; i--){
 				trickleDown(i);
 		}
 		
		System.out.println("-------- NOW START SORTING --------");
 		//now when we have heap structure, start sorting by removing the head
 		//of the heap and move it to the back
 		for (int i = size -1; i>=0; i--){
 			 //always remove the root which is list_heap[0]
 			swapHeap(0, i);
			
 			
 			synchronized(this) {
 				
 				while(suspendFlag) {
 	              
 						try {
 		              	 	wait();
 		        }
 						catch (InterruptedException e) {
 											
 							System.out.println(" thread interrupted.");
 						}
 				}
 			}
 			
			list_heap[i] = remove();
			
 		}
 		
 		showResult();
 	}
 	
 	public int remove(){
 		
 		int root = list_heap[0];
 		list_heap[0] = list_heap[--size];
 		trickleDown(0);
 		
 		return root;
 
 	}
 	
 	public void trickleDown(int index){
 		
 		int largerChild;
 		int top = list_heap[index];//save the root
 		while (index < size/2){
 			
			int leftChild = index*2;
 			int rightChild = leftChild +1;
 			
 			if (rightChild < size && list_heap[leftChild] > list_heap[rightChild]){
 				
 					largerChild = leftChild;
 					
 			}
 			else{
 					largerChild = rightChild;
 			}
 			
 			if (top >= list_heap[largerChild]){
 				break; //escape from while loop since the top is already in the right place
 			}
 			
			swapHeap(index, largerChild);
 			list_heap[index] = list_heap[largerChild];
 			index = largerChild;
 			list_heap[index] = top;
 			
 			
 			synchronized(this) {
 				
 				while(suspendFlag) {
 	              
 						try {
 		              	 	wait();
						}
 						catch (InterruptedException e) {
 											
 							System.out.println(" thread interrupted.");
 						}
 				}
 			}
 			
 		}
 		
 		
 		
 	}
 	
 	
 	
 	private void showResult(){
 		
 		System.out.println("After Merge Sort: ");
 		for (int i=0; i< list_heap.length; i++){
 			System.out.print(" [ " + list_heap[i] + " ] " );
 		}
 	}
 	
 	void suspend() {
 		System.out.println("SUSPEND IS CALLED - suspend flag is now true ");
 	      suspendFlag = true;
 	   }
 	
 	synchronized void resume() {
 		
 		System.out.println("RESUME IS CALLED - suspend flag is now false ");
 	      suspendFlag = false;
 	      notify();
 	   }
 	
 	
 }
 
 public void swapHeap(int i, int j){
 	 
 	 heap.suspend();
 	 heapAnimation(i, j);
 	 if (!heap_timer.isRunning()){
 		 System.out.println("---- Start Timer: ");
 		 heap_timer.start();
 	 }
 }	
 
 public void heapAnimation(final int left, final int right){
 	
 	 heap_list_button[left].setEnabled(true);
 	 heap_list_button[right].setEnabled(true);
 		 
 		 int left_x = heap_list_button[left].getLocation().x;
 		 int right_x = heap_list_button[right].getLocation().x;
 		 final int delta = (right_x - left_x) / 10;
 		 	
 		 heap_timer = new Timer(200, new ActionListener() {
 			 
 							 public void actionPerformed(ActionEvent e){
 								 			
 								 	 if (heap_clk <5){
 									 		 	System.out.print("clk is: " + heap_clk);
 									 		 	
 									 			System.out.println("---- y is: " + (heap_list_button[left].getLocation().y -10));
 									 			heap_list_button[left].setLocation(heap_list_button[left].getLocation().x  , heap_list_button[left].getLocation().y -10);
 									 		
 									 	 		heap_list_button[right].setLocation(heap_list_button[right].getLocation().x , heap_list_button[right].getLocation().y +10);
 									 	 		repaint();
 									 	 		heap_clk++;
 								 	 		
 								 	 }
 								 	 
 								 	 if (heap_clk < (5+delta) && heap_clk >=5){
 											 		 
 											 		System.out.println("clk is: " + heap_clk);
 											 		
 											 		System.out.println("---- x is: " + (heap_list_button[left].getLocation().x +10));
 											 		 
 											 		heap_list_button[left].setLocation(heap_list_button[left].getLocation().x +10, heap_list_button[left].getLocation().y);
 											 		heap_list_button[right].setLocation(heap_list_button[right].getLocation().x -10, heap_list_button[right].getLocation().y);
 											 		repaint();
 											 		heap_clk++;
 								 		 
 								 	 }
 								 	 
 								 	 if (heap_clk < (10+delta) && heap_clk >= (5+delta)) {
 								 		 
 											 		System.out.println("clk is: " + heap_clk);
 											 		heap_list_button[left].setLocation(heap_list_button[left].getLocation().x, heap_list_button[left].getLocation().y +10);
 										 	 		heap_list_button[right].setLocation(heap_list_button[right].getLocation().x, heap_list_button[right].getLocation().y -10);
 										 	 		repaint();
 										 	 		heap_clk++;
 											 		 
 								 	 }
 								 	 
 								 	 else if (heap_clk >= 10+delta ){
 								 		 
 										 		System.out.println("clk is: " + heap_clk + " done animation");
 										 		 
 										 		 //done animation, reset the bubble_clk and resume sorting thread		 		 
 										 		 
 										 		  if (heap_timer.isRunning()){
 										 			System.out.println("!stop timer!");
 										 			  heap_timer.stop();
 										 			  
 										 			} 
 										 		
 										  		 heap_list_button[left].setEnabled(false);
 										  		 heap_list_button[right].setEnabled(false);
 										 		 	repaint();
 										 			heap_clk = 0;
 										 		
 										 			JButton temp;
 										 		 temp = heap_list_button[left];
 										 		 heap_list_button[left] = heap_list_button[right];
 										 		 heap_list_button[right] = temp;
 										 		 
 										 		
 										 		 heap.resume();
 										 		
 								 
 								 		 
 								 		 
 								 		 
 								 	 }
 								 		 
 								 		 
 							 }
 				
 						
 							 
 					 } );
 		 	
 	}
 	 
 /*-----------------------------------------------------------------*/	 
 public class MergeSort implements Runnable{
 	
 	boolean suspendFlag = false;
 
 	private void mergeSort(int[] a, int _left, int _right){
 		
 		int left = _left;
 		int right = _right;
 		
 			if (left < right){
 					
 					int mid  = (left+right) /2;
 					
 					mergeSort(a,left,mid);
 					mergeSort(a,mid+1,right);
 					
 					int end_left = mid;
 					int start_right = mid +1;
 					
 					while ((left <= end_left) && (start_right <= right)){
 						
 							if (a[left] < a[start_right]){
 								left++;
 							}
 							else{
 								
 								int tmp = a[start_right];
 								
 								for (int i = start_right - 1; i>= left; i--){
 									
 										swapMerge(i, i+1);
 										a[i+1] = a[i];
 										
 										synchronized(this) {
 					            while(suspendFlag) {
 					               try {
 					              	 System.out.println("suspend flag is now true -> wait");
 					              	 wait();
 												}
 												catch (InterruptedException e) {
 													// TODO Auto-generated catch block
 													System.out.println("Shell thread interrupted.");
 												}
 					            }
 					          }
 										
 								}
 								
 								a[left] = tmp;
 								left++;
 								end_left++;
 								start_right++;
 								
 							}
 						
 					}
 				
 			}
 		
 		
 	}
 	
 
 	
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		mergeSort(list_merge,0,list_merge.length -1);
 		
 		showResult();
 		
 		
 	}
 	
 	
 	
 	private void showResult(){
 		
 			System.out.println("After Merge Sort: ");
 			for (int i=0; i< list_merge.length; i++){
 				System.out.print(" [ " + list_merge[i] + " ] " );
 			}
 	}
 	
 	
 	void suspend() {
 		System.out.println("SUSPEND IS CALLED - suspend flag is now true ");
 	      suspendFlag = true;
 	   }
 	
 	synchronized void resume() {
 		
 		System.out.println("RESUME IS CALLED - suspend flag is now false ");
 	      suspendFlag = false;
 	      notify();
 	   }
 	
 	
 }
 
 public void mergeAnimation(final int left, final int right){
 	
 	 merge_list_button[left].setEnabled(true);
 	 merge_list_button[right].setEnabled(true);
 		 
 		 int left_x = merge_list_button[left].getLocation().x;
 		 int right_x = merge_list_button[right].getLocation().x;
 		 final int delta = (right_x - left_x) / 10;
 		 	
 		 merge_timer = new Timer(200, new ActionListener() {
 			 
 							 public void actionPerformed(ActionEvent e){
 								 			
 								 	 if (merge_clk <5){
 									 		 	System.out.print("clk is: " + merge_clk);
 									 		 	
 									 			System.out.println("---- y is: " + (merge_list_button[left].getLocation().y -10));
 									 			merge_list_button[left].setLocation(merge_list_button[left].getLocation().x  , merge_list_button[left].getLocation().y -10);
 									 		
 									 	 		merge_list_button[right].setLocation(merge_list_button[right].getLocation().x , merge_list_button[right].getLocation().y +10);
 									 	 		repaint();
 									 	 		merge_clk++;
 								 	 		
 								 	 }
 								 	 
 								 	 if (merge_clk < (5+delta) && merge_clk >=5){
 											 		 
 											 		System.out.println("clk is: " + merge_clk);
 											 		
 											 		System.out.println("---- x is: " + (merge_list_button[left].getLocation().x +10));
 											 		 
 											 		merge_list_button[left].setLocation(merge_list_button[left].getLocation().x +10, merge_list_button[left].getLocation().y);
 											 		merge_list_button[right].setLocation(merge_list_button[right].getLocation().x -10, merge_list_button[right].getLocation().y);
 											 		repaint();
 											 		merge_clk++;
 								 		 
 								 	 }
 								 	 
 								 	 if (merge_clk < (10+delta) && merge_clk >= (5+delta)) {
 								 		 
 											 		System.out.println("clk is: " + merge_clk);
 											 		merge_list_button[left].setLocation(merge_list_button[left].getLocation().x, merge_list_button[left].getLocation().y +10);
 										 	 		merge_list_button[right].setLocation(merge_list_button[right].getLocation().x, merge_list_button[right].getLocation().y -10);
 										 	 		repaint();
 										 	 		merge_clk++;
 											 		 
 								 	 }
 								 	 
 								 	 else if (merge_clk >= 10+delta ){
 								 		 
 										 		System.out.println("clk is: " + merge_clk + " done animation");
 										 		 
 										 		 //done animation, reset the bubble_clk and resume sorting thread		 		 
 										 		 
 										 		  if (merge_timer.isRunning()){
 										 			System.out.println("!stop timer!");
 										 			  merge_timer.stop();
 										 			  
 										 			} 
 										 		
 										  		 merge_list_button[left].setEnabled(false);
 										  		 merge_list_button[right].setEnabled(false);
 										 		 	repaint();
 										 			merge_clk = 0;
 										 		
 										 			JButton temp;
 										 		 temp = merge_list_button[left];
 										 		 merge_list_button[left] = merge_list_button[right];
 										 		 merge_list_button[right] = temp;
 										 		 
 										 		
 										 		 merge.resume();
 										 		
 								 
 								 		 
 								 		 
 								 		 
 								 	 }
 								 		 
 								 		 
 							 }
 				
 						
 							 
 					 } );
 		 	
 	}
 
 
 public void swapMerge(int i, int j){
 	 
 	 merge.suspend();
 	 mergeAnimation(i, j);
 	 if (!merge_timer.isRunning()){
 		 System.out.println("---- Start Timer: ");
 		 merge_timer.start();
 	 }
 }	 
 
 	 
 /*-------------------------------------------------------*/	 
 	 
 public class ShellSort implements Runnable{
 		 
 		boolean suspendFlag = false;
 
 		@Override
 		public void run() {
 				// TODO Auto-generated method stub
 				int increment = list_shell.length /2;
 				while (increment >0){
 						for (int i = increment; i< list_shell.length; i++){
 							
 								int j = i;
 								int temp = list_shell[i];
 								while (j >= increment && list_shell[j-increment] >temp){
 										
 										list_shell[j] = list_shell[j - increment];
 										swapShell(j - increment, j);
 										j = j - increment;
 										
 										synchronized(this) {
 					            while(suspendFlag) {
 					               try {
 					              	 System.out.println("suspend flag is now true -> wait");
 					              	 wait();
 												}
 												catch (InterruptedException e) {
 													// TODO Auto-generated catch block
 													System.out.println("Shell thread interrupted.");
 												}
 					            }
 					          }
 								}
 								list_shell[j] = temp;
 						}
 						
 						if (increment == 2){
 								increment = 1;						
 						}
 						else {
 								increment *= (5.0 /11);
 						}
 				}
 				
 				
 				
 		}
 		 
 		
 		void suspend() {
 			System.out.println("SUSPEND IS CALLED - suspend flag is now true ");
 		      suspendFlag = true;
 		   }
 		
 		synchronized void resume() {
 			
 			System.out.println("RESUME IS CALLED - suspend flag is now false ");
 		      suspendFlag = false;
 		      notify();
 		   }
 		 
 		 
 		 
 		 
 	 }
 	 
 public void shellAnimation(final int left, final int right){
 	
 	 shell_list_button[left].setEnabled(true);
 	 shell_list_button[right].setEnabled(true);
 		 
 		 int left_x = shell_list_button[left].getLocation().x;
 		 int right_x = shell_list_button[right].getLocation().x;
 		 final int delta = (right_x - left_x) / 10;
 		 	
 		 shell_timer = new Timer(200, new ActionListener() {
 			 
 							 public void actionPerformed(ActionEvent e){
 								 			
 								 	 if (shell_clk <5){
 									 		 	System.out.print("clk is: " + shell_clk);
 									 		 	
 									 			System.out.println("---- y is: " + (shell_list_button[left].getLocation().y -10));
 									 			shell_list_button[left].setLocation(shell_list_button[left].getLocation().x  , shell_list_button[left].getLocation().y -10);
 									 		
 									 	 		shell_list_button[right].setLocation(shell_list_button[right].getLocation().x , shell_list_button[right].getLocation().y +10);
 									 	 		repaint();
 									 	 		shell_clk++;
 								 	 		
 								 	 }
 								 	 
 								 	 if (shell_clk < (5+delta) && shell_clk >=5){
 											 		 
 											 		System.out.println("clk is: " + shell_clk);
 											 		
 											 		System.out.println("---- x is: " + (shell_list_button[left].getLocation().x +10));
 											 		 
 											 		shell_list_button[left].setLocation(shell_list_button[left].getLocation().x +10, shell_list_button[left].getLocation().y);
 											 		shell_list_button[right].setLocation(shell_list_button[right].getLocation().x -10, shell_list_button[right].getLocation().y);
 											 		repaint();
 											 		shell_clk++;
 								 		 
 								 	 }
 								 	 
 								 	 if (shell_clk < (10+delta) && shell_clk >= (5+delta)) {
 								 		 
 											 		System.out.println("clk is: " + shell_clk);
 											 		shell_list_button[left].setLocation(shell_list_button[left].getLocation().x, shell_list_button[left].getLocation().y +10);
 										 	 		shell_list_button[right].setLocation(shell_list_button[right].getLocation().x, shell_list_button[right].getLocation().y -10);
 										 	 		repaint();
 										 	 		shell_clk++;
 											 		 
 								 	 }
 								 	 
 								 	 else if (shell_clk >= 10+delta ){
 								 		 
 										 		System.out.println("clk is: " + shell_clk + " done animation");
 										 		 
 										 		 //done animation, reset the bubble_clk and resume sorting thread		 		 
 										 		 
 										 		  if (shell_timer.isRunning()){
 										 			System.out.println("!stop timer!");
 										 			  shell_timer.stop();
 										 			  
 										 			} 
 										 		
 										  		 shell_list_button[left].setEnabled(false);
 										  		 shell_list_button[right].setEnabled(false);
 										 		 	repaint();
 										 			shell_clk = 0;
 										 		
 										 			JButton temp;
 										 		 temp = shell_list_button[left];
 										 		 shell_list_button[left] = shell_list_button[right];
 										 		 shell_list_button[right] = temp;
 										 		 
 										 		
 										 		 shell.resume();
 										 		
 								 
 								 		 
 								 		 
 								 		 
 								 	 }
 								 		 
 								 		 
 							 }
 				
 						
 							 
 					 } );
 		 	
 	}
 		 
 	 
 	 
 	 
 	 public void swapShell(int i, int j){
 		 
 		 shell.suspend();
 		 shellAnimation(i, j);
 		 if (!shell_timer.isRunning()){
 			 System.out.println("---- Start Timer: ");
 			 shell_timer.start();
 		 }
 		 
 	 }
 	 
 	 
 	 
 	 
 	 
 	 
 	 
 /* --------------------------------------------------- */	
 	 public class QuickSort implements Runnable{
 		 
 		 boolean suspendFlag = false;
 
 		 //this function is used to partition the array into 2 sub arrays, the left sub array has numbers that is smaller than
 		 //the pivot and the right sub array has numbers that is bigger than the pivot
 			int partition(int[] array, int left, int right){
 					
 					int i=left;
 					int j=right;
 					int tmp;
 					int pivot = array[(left + right)/2];
 					
 					while (i<=j) {
 						
 							while (array[i] < pivot)	
 									i++;
 							while (array[j] > pivot)
 									j--;
 							
 							if (i <=j){
 								
 											tmp = array[i];
 											array[i] = array[j];
 											array[j] = tmp;
 											
 		
 											swapQuick(i, j);
 											
 											i++;
 											j--;
 											
 											synchronized(this) {
 						            while(suspendFlag) {
 						               try {
 						              	 System.out.println("suspend flag is now true -> wait");
 						              	 wait();
 													}
 													catch (InterruptedException e) {
 														// TODO Auto-generated catch block
 														System.out.println("Bubble thread interrupted.");
 													}
 						            }
 						          }
 
 									
 									}
 					}
 					
 					return i;
 					
 					
 				}
 				
 				void quickSort(int[] array, int left, int right) {
 					
 					int index = partition(array,left, right);
 					
 					if (left < index -1)
 							quickSort(array, left, index -1);
 					if (index < right)
 							quickSort(array, index, right);
 				}
 
 				@Override
 				public void run() {
 					// TODO Auto-generated method stub
 					quickSort(list_quick,0, list_quick.length -1);
 					
 				
 					
 				}
 				
 				void suspend() {
 					System.out.println("SUSPEND IS CALLED - suspend flag is now true ");
 				      suspendFlag = true;
 				   }
 				
 				synchronized void resume() {
 					
 					System.out.println("RESUME IS CALLED - suspend flag is now false ");
 				      suspendFlag = false;
 				      notify();
 				   }
 				 
 				 
 	 }
 		
 
 	 
 	 public void quickAnimation(final int left, final int right){
 		 
  		 quick_list_button[left].setEnabled(true);
 		 quick_list_button[right].setEnabled(true);
 		 
 		 int left_x = quick_list_button[left].getLocation().x;
 		 int right_x = quick_list_button[right].getLocation().x;
 		 final int delta = (right_x - left_x) / 10;
 		 	
 		 quick_timer = new Timer(200, new ActionListener() {
 			 
 							 public void actionPerformed(ActionEvent e){
 								 			
 								 	 if (quick_clk <5){
 									 		 	System.out.print("clk is: " + quick_clk);
 									 		 	
 									 			System.out.println("---- y is: " + (quick_list_button[left].getLocation().y -10));
 									 			quick_list_button[left].setLocation(quick_list_button[left].getLocation().x  , quick_list_button[left].getLocation().y -10);
 									 		
 									 	 		quick_list_button[right].setLocation(quick_list_button[right].getLocation().x , quick_list_button[right].getLocation().y +10);
 									 	 		repaint();
 									 	 		quick_clk++;
 								 	 		
 								 	 }
 								 	 
 								 	 if (quick_clk < (5+delta) && quick_clk >=5){
 											 		 
 											 		System.out.println("clk is: " + quick_clk);
 											 		
 											 		System.out.println("---- x is: " + (quick_list_button[left].getLocation().x +10));
 											 		 
 											 		quick_list_button[left].setLocation(quick_list_button[left].getLocation().x +10, quick_list_button[left].getLocation().y);
 											 		quick_list_button[right].setLocation(quick_list_button[right].getLocation().x -10, quick_list_button[right].getLocation().y);
 											 		repaint();
 											 		quick_clk++;
 								 		 
 								 	 }
 								 	 
 								 	 if (quick_clk < (10+delta) && quick_clk >= (5+delta)) {
 								 		 
 											 		System.out.println("clk is: " + quick_clk);
 											 		quick_list_button[left].setLocation(quick_list_button[left].getLocation().x, quick_list_button[left].getLocation().y +10);
 										 	 		quick_list_button[right].setLocation(quick_list_button[right].getLocation().x, quick_list_button[right].getLocation().y -10);
 										 	 		repaint();
 										 	 		quick_clk++;
 											 		 
 								 	 }
 								 	 
 								 	 else if (quick_clk >= 10+delta ){
 								 		 
 										 		System.out.println("clk is: " + quick_clk + " done animation");
 										 		 
 										 		 //done animation, reset the bubble_clk and resume sorting thread		 		 
 										 		 
 										 		  if (quick_timer.isRunning()){
 										 			System.out.println("!stop timer!");
 										 			  quick_timer.stop();
 										 			  
 										 			} 
 										 		  
 										 		  
 										 		 quick_list_button[left].setEnabled(false);
 												 quick_list_button[right].setEnabled(false);
 										 		
 										 		 	repaint();
 										 			quick_clk = 0;
 										 		
 										 			JButton temp;
 										 		 temp = quick_list_button[left];
 										 		 quick_list_button[left] = quick_list_button[right];
 										 		 quick_list_button[right] = temp;
 										 		 
 										 		
 										 		 quick.resume();
 										 		
 								 
 								 		 
 								 		 
 								 		 
 								 	 }
 								 		 
 								 		 
 							 }
 				
 						
 							 
 					 } );
 		 	
 	}
 		 
 	 
 	 
 	 
 	 public void swapQuick(int i, int j){
 		 
 		 quick.suspend();
 		 quickAnimation(i, j);
 		 if (!quick_timer.isRunning()){
 			 System.out.println("---- Start Timer: ");
 			 quick_timer.start();
 		 }
 		 
 	 }
 	 
 	 
 /* --------------------------------------------------- */	 
 	 /*Create a timer to do animation*/
 	 
 	 
 	 
 	 public class BubbleSort implements Runnable{
 			
 		 volatile	boolean suspendFlag = false;
 			
 						public void run(){
 						
 								for (int i=0; i<list_bubble.length -1; i++){
 									for (int j = 0; j< list_bubble.length -i -1; j++){
 											if (list_bubble[j] > list_bubble[j+1]){
 												
 								
 													int tmp = list_bubble[j];
 													list_bubble[j] = list_bubble[j+1];
 													list_bubble[j+1] = tmp;
 													swapBubble(j, j+1);
 													
 													
 													synchronized(this) {
 								            while(suspendFlag) {
 								               try {
 								              	 System.out.println("suspend flag is now true -> wait");
 								              	 wait();
 															}
 															catch (InterruptedException e) {
 																// TODO Auto-generated catch block
 																System.out.println("Bubble thread interrupted.");
 															}
 								            }
 								          }
 
 													
 													
 											}
 									}
 								
 								}
 							
 						}
 						//provide methods to suspend/resume the thread
 				void suspend() {
 					System.out.println("SUSPEND IS CALLED - suspend flag is now true ");
 				      suspendFlag = true;
 				   }
 				
 				synchronized void resume() {
 					
 					System.out.println("RESUME IS CALLED - suspend flag is now false ");
 				      suspendFlag = false;
 				      notify();
 				   }
 
 		
 		
 		}
 	 
 	 public void bubbleAnimation(final int left, final int right) {
 
 			
 
 		 bubble_list_button[left].setEnabled(true);
 		 bubble_list_button[right].setEnabled(true);
 		 
 			 bubble_timer = new Timer(200, new ActionListener() {
 			 
 			 public void actionPerformed(ActionEvent e){
 				 			
 				 	 if (bubble_clk <5){
 					 		 	System.out.print("clk is: " + bubble_clk);
 					 		 	
 					 			System.out.println("---- y is: " + (bubble_list_button[left].getLocation().y -10));
 					 			bubble_list_button[left].setLocation(bubble_list_button[left].getLocation().x  , bubble_list_button[left].getLocation().y -10);
 					 		
 					 	 		bubble_list_button[right].setLocation(bubble_list_button[right].getLocation().x , bubble_list_button[right].getLocation().y +10);
 					 	 		repaint();
 					 	 		bubble_clk++;
 				 	 		
 				 	 }
 				 	 
 				 	 if (bubble_clk < 9 && bubble_clk >=5){
 							 		 
 							 		System.out.println("clk is: " + bubble_clk);
 							 		
 							 		System.out.println("---- x is: " + (bubble_list_button[left].getLocation().x +10));
 							 		 
 							 		bubble_list_button[left].setLocation(bubble_list_button[left].getLocation().x +10, bubble_list_button[left].getLocation().y);
 							 		bubble_list_button[right].setLocation(bubble_list_button[right].getLocation().x -10, bubble_list_button[right].getLocation().y);
 							 		repaint();
 							 		bubble_clk++;
 				 		 
 				 	 }
 				 	 
 				 	 if (bubble_clk < 14 && bubble_clk >=9) {
 				 		 
 							 		System.out.println("clk is: " + bubble_clk);
 							 		bubble_list_button[left].setLocation(bubble_list_button[left].getLocation().x, bubble_list_button[left].getLocation().y +10);
 						 	 		bubble_list_button[right].setLocation(bubble_list_button[right].getLocation().x, bubble_list_button[right].getLocation().y -10);
 						 	 		repaint();
 						 	 		bubble_clk++;
 							 		 
 				 	 }
 				 	 
 				 	 else if (bubble_clk >= 14 ){
 				 		 
 						 		System.out.println("clk is: " + bubble_clk + " done animation");
 						 		 
 						 		 //done animation, reset the bubble_clk and resume sorting thread		 		 
 						 		 
 						 		  if (bubble_timer.isRunning()){
 						 			System.out.println("!stop timer!");
 						 			  bubble_timer.stop();
 						 			  
 						 			} 
 						 		  
 						 		 bubble_list_button[left].setEnabled(false);
 								 bubble_list_button[right].setEnabled(false);
 
 						 		 	repaint();
 						 			bubble_clk = 0;
 						 		 JButton temp;
 						 		 temp = bubble_list_button[left];
 						 		 bubble_list_button[left] = bubble_list_button[right];
 						 		 bubble_list_button[right] = temp;
 		
 						 		 bubble.resume();
 						 		
 				 
 				 		 
 				 		 
 				 		 
 				 	 }
 				 		 
 				 		 
 			 }
 
 		
 			 
 	 } ); }
 
 
 		public void swapBubble(int i, int j) {
 			
 			bubble.suspend();
 			
 			bubbleAnimation(i, j);
 			 
 			 if (!bubble_timer.isRunning()){
 				 System.out.println("---- Start Timer: ");
 				 bubble_timer.start();
 			
 			 }
 			 
 			 
 		}
 	 
 
 		 
 	/* --------------------------------------------------- */
 	 
 
 		
 	 public AnimateSorting(int[] a) {
 					
 		 			list_bubble = a.clone();
 		 			list_quick  = a.clone();
 		 			list_shell = a.clone();
 		 			list_merge = a.clone();
 					list_heap = a.clone();
 					
 					System.out.println("Size of a: " + a.length);
 					
 					for (int i=0; i< a.length; i++){
 						
 						list.add(a[i]);
 					}
 					
 					System.out.println("Size of list: " + list.size());
 					
 					
 					
 		 			jBubble = new JPanel();
 					jBubble.setBorder(BorderFactory.createTitledBorder("Bubble Sort"));
 					
 					
 		 			jQuick = new JPanel();
 					jQuick.setBorder(BorderFactory.createTitledBorder("Quick Sort"));
 					
 					jShell = new JPanel();
 					jShell.setBorder(BorderFactory.createTitledBorder("Shell Sort"));
 					
 					jMerge = new JPanel();
 					jMerge.setBorder(BorderFactory.createTitledBorder("Merge Sort"));
 					
 					jHeap = new JPanel();
 					jHeap.setBorder(BorderFactory.createTitledBorder("Heap Sort"));
 					
 					jInsert = new JPanel();
 					jInsert.setBorder(BorderFactory.createTitledBorder("Insert Sort"));
 					
 					
 					
 					bubble_list_button = new JButton[list_bubble.length];
 					quick_list_button = new JButton[list_quick.length];
 					shell_list_button = new JButton[list_shell.length];
 					merge_list_button = new JButton[list_merge.length];
 					heap_list_button = new JButton[list_heap.length];
 					insert_list_button = new JButton[list.size()];
 					
 					
 					
 		
 					GridLayout pane = new GridLayout(3,3); 
 					this.setLayout(pane);
 					jBubble.setLayout(null);
 					jQuick.setLayout(null);
 					jShell.setLayout(null);
 					jMerge.setLayout(null);
 					jHeap.setLayout(null);
 					jInsert.setLayout(null);
 					
 					
 					
 					for (int i =0; i<list_bubble.length;i++) {
 						bubble_list_button[i] = new JButton(String.valueOf(list_bubble[i]));
 						bubble_list_button[i].setSize(40,40);
 						bubble_list_button[i].setEnabled(false);
 						bubble_list_button[i].setLayout(null);
 						bubble_list_button[i].setOpaque(true);
 						bubble_list_button[i].setFocusPainted(false);
 						
 					}
 					
 					
 					for (int i =0; i<list_merge.length;i++) {
 						merge_list_button[i] = new JButton(String.valueOf(list_merge[i]));
 						merge_list_button[i].setSize(40,40);
 						merge_list_button[i].setEnabled(false);
 						merge_list_button[i].setLayout(null);
 						merge_list_button[i].setOpaque(true);
 						merge_list_button[i].setFocusPainted(false);
 						
 					}
 					
 					
 
 					
 					for (int i =0; i<list_quick.length;i++) {
 						quick_list_button[i] = new JButton(String.valueOf(list_quick[i]));
 						quick_list_button[i].setSize(40,40);
 						quick_list_button[i].setEnabled(false);
 						quick_list_button[i].setLayout(null);
 						quick_list_button[i].setOpaque(true);
 						quick_list_button[i].setFocusPainted(false);
 					}
 					
 					for (int i =0; i<list_shell.length;i++) {
 						shell_list_button[i] = new JButton(String.valueOf(list_shell[i]));
 						shell_list_button[i].setSize(40,40);
 						shell_list_button[i].setEnabled(false);
 						shell_list_button[i].setLayout(null);
 						shell_list_button[i].setOpaque(true);
 						shell_list_button[i].setFocusPainted(false);
 					}
 					System.out.println("CHECK POINT 1!");
 					
 					for (int i =0; i<list_heap.length;i++) {
 						heap_list_button[i] = new JButton(String.valueOf(list_heap[i]));
 						heap_list_button[i].setSize(40,40);
 						heap_list_button[i].setEnabled(false);
 						heap_list_button[i].setLayout(null);
 						heap_list_button[i].setOpaque(true);
 						heap_list_button[i].setFocusPainted(false);
 					}
 					
 					
 					for (int i =0; i<list.size();i++) {
 						insert_list_button[i] = new JButton(String.valueOf(list.get(i)));
 						insert_list_button[i].setSize(40,40);
 						insert_list_button[i].setEnabled(false);
 						insert_list_button[i].setLayout(null);
 						insert_list_button[i].setOpaque(true);
 						insert_list_button[i].setFocusPainted(false);
 					}
 					
 		
 
 					
 				for (int i =0; i<list_bubble.length;i++) {
 						jBubble.add(bubble_list_button[i]);
 						bubble_list_button[i].setLocation(i*40+40,50);
 						
 					}
 				
 				for (int i =0; i<list_quick.length;i++) {
 					jQuick.add(quick_list_button[i]);
 					quick_list_button[i].setLocation(i*40+40,50);
 					
 				}
 				
 				for (int i =0; i<list_shell.length;i++) {
 					jShell.add(shell_list_button[i]);
 					shell_list_button[i].setLocation(i*40+40,50);
 					
 				}
 				
 				for (int i =0; i<list_merge.length;i++) {
 					jMerge.add(merge_list_button[i]);
 					merge_list_button[i].setLocation(i*40+40,50);
 					
 				}
 				
 				for (int i =0; i<list_heap.length;i++) {
 					jHeap.add(heap_list_button[i]);
 					heap_list_button[i].setLocation(i*40+40,50);
 					
 				}
 				
 				for (int i =0; i<list.size();i++) {
 					jInsert.add(insert_list_button[i]);
 					insert_list_button[i].setLocation(i*40+40,50);
 					
 				}
 				
 				this.add(jBubble);
 				
 				this.add(jQuick);
 				
 				this.add(jShell);
 				
 				this.add(jMerge);
 				
 				this.add(jHeap);
 				
 				this.add(jInsert);
 
 					
 }
 	
 public static void main(String[] args){
 				
 
 	
 				
 							int[] a; 
 							
 							Scanner sc = new Scanner(System.in);
 							
 							System.out.print("Enter size of the array: "); 
 							
 							int n= sc.nextInt();
 							
 							Random coin = new Random();
 							
 							a = new int[n];
 							
 							if (n >1)
 								for (int i =0; i< n ; i++){
 									
 									if (coin.nextInt(2) ==0)
 										a[i] = coin.nextInt(100);
 									
 									else 
 										
 										a[i] = -1 * coin.nextInt(100);
 									
 								}
 							else{
 								System.out.println("Invalid size of array!");
 								System.exit(0);
 							}
 						
 	
 							AnimateSorting demo = new AnimateSorting(a);
 							
 							System.out.println("Before: ");
 							for (int i=0; i<a.length; i++){
 									System.out.print(" [ " + a[i] + " ] ");
 							}
 						
						System.out.println("------ BUBBLE SORTING ------");
 							
 							bubble= demo.new BubbleSort();
 							
 							(new Thread(bubble)).start(); //run buble sort in a new thread
 							
 							System.out.println("------ QUICK SORTING ------");
 							
 							quick = demo.new QuickSort();
 							
 							(new Thread(quick)).start();
 							
 							
 							System.out.println("------ SHELL SORTING ------");
 							
 							shell = demo.new ShellSort();
 							
 							(new Thread(shell)).start();
 		
 							
 							System.out.println("------ MERGE SORTING ------");
 							
 							merge = demo.new MergeSort();
 							
 							(new Thread(merge)).start();
 							
 							System.out.println("------ Insert SORTING ------");
 							
 							insert = demo.new InsertSort();
 							
							(new Thread(insert)).start();
 							
 							System.out.println("------ HEAP SORTING ------");
 							
 							heap = demo.new HeapSort();
 							
 							(new Thread(heap)).start();
 						
 		
 							
 							//draw a jframe
 							JFrame frame = new JFrame("AnimateSorting");
 							frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 							frame.add(demo);
 							frame.setSize(1024,800);
 							frame.setResizable(true);
 							frame.setVisible(true);
 							frame.setLayout(null); 
 					    
 					    
 }			
 			
 			public void paintComponent(Graphics g) {
 				
 				
 				super.paintComponent(g);			
 				
 			}		
 			
 
 
 
 
 }
 
 
 
 
 
