 public class ListUtilities {
 	
 	public static IntStack arrayToStack(int[] src) {
 		
 		IntStack dst = new IntStack();
 		IntNode currentInt;
 		
 		for (int i=src.length-1 ; i>=0 ; i--) {
 			currentInt = new IntNode(src[i]);
 			dst.push(currentInt);
 		}
 		
 		return dst;
 	}
 	
 	public static int[] stackToArray(IntStack src) {
 		int counter = 0;
 		IntNode current = src.getFirst();
 		while(current != null) {
 			counter++;
 			current = current.getNext();
 		}
 		int[] dst = new int[counter];
 		current = src.getFirst();
 		counter = 0;
 		while(current != null) {
 			dst[counter] = current.getValue();
 			current = current.getNext();
 		}
 		return dst;
 	}
 	
	public static void bubbleSort(IntStack src) {
 		int[] tmp = ListUtilities.stackToArray(src);
 		int temp;
 		boolean swap;
 		
 		for (int i=0 ; i<tmp.length-1 ; i++) {      			// each pass
 			swap = false;
 			for (int j=0 ; j<tmp.length-(i+1) ; j++) {		// each elt to be compared with the next
 				if(tmp[j]>tmp[j+1]) {
 					temp = tmp[j];
 					tmp[j] = tmp[j+1];
 					tmp[j+1] = temp;
 					swap = true;
 				}
 			}
 			if(!swap) {
				return;
 			}
 		}
		src = ListUtilities.arrayToStack(tmp);
	}
		
 }
