 import java.util.*; 
 class CoxeterElement {
     private int[] expression;
     private int rank = 0;
     private int length;
 
     public CoxeterElement(int[] input, int rank) {
 	boolean validElement = true;
 	
 	// Check all generators are legal
 	for(int i = 0; i < input.length; i++) {
 	    if ((input[i] > rank) | (input[i] < 1)) {
 		validElement = false;
 	    }
 	}
 	
 	if (validElement) {
 	    this.rank = rank;
 	    this.length = input.length;
 	    this.expression = new int[length];
 	    for(int i = 0; i < input.length; i++) {
 		expression[i] = input[i];
 	    }
 	}
 	else {
 	    System.out.println("Invalid element");
 	}
     }
 
     public Element toPermutation() {
 	int[] permutation = new int[rank];
 	int temp;
 	int generator;
 	Element answer;
 
 	for (int i = 0; i < rank; i++) {
 	    permutation[i] = i + 1;
 	}
 	for (int i = 0; i < length; i++) {
 	    generator = expression[i];
 	    if (generator > 1) {
 		temp = permutation[generator - 2];
 		permutation[generator - 2] = permutation[generator - 1];
 		permutation[generator - 1] = temp;
 	    }
 	    else {
 		temp = permutation[0];
 		permutation[0] = -1*permutation[1];
 		permutation[1] = -1*temp;
 	    }
 	}
 	answer = new Element(permutation);
 	return answer;
     }
   
 }
