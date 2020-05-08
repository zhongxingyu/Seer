package client;

 import java.util.*;
 
 class LargeList {
 	private static final int BATCH_SIZE = 100;
 	
 	public static void main(String[] args) {
 		List<Integer> list = new ArrayList<Integer>();
 		for (int i=0; i<1000; i++) {
 			list.add(i);
 		}
 		
 		//try to do the sublist thingy
 		
 		/*List<Integer> mysubList = list.subList(0, 10);
 		for (Integer a : mysubList) {
 			System.out.print(a + " ");
 		}*/
 		
 		int startIndex = 0, endIndex = 0;
 		int total = list.size();
 		
 		List<Integer> mysubList = new ArrayList<Integer>();
 		
 		while(endIndex < (total -1)){
 			startIndex = endIndex;
 			if((endIndex + BATCH_SIZE) > (total-1) ){
 				endIndex = total -1;
 			} else{
 				endIndex = startIndex + 100;
 			}
 			
 			mysubList = list.subList(startIndex, endIndex);
 			printList(mysubList);
 			
 		}
 		
 		mysubList.add(list.get(endIndex));
 		printList(mysubList);
 	}
 	
 	private static void printList(List<Integer> a){
 		for (Integer i : a) {
 			System.out.print(i + " ");
 		}
 		System.out.println("\n\n");
 	}
 }
