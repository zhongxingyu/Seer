 public class ForIt implements SeqIt {
 
 	int currentVal, last, step;
 
 	public ForIt(For obj) {
 		currentVal=obj.first;
 		last=obj.last;
 		step=obj.step;
 	}
 
 	public boolean hasNext() { //checks if anymore elements
 		if(step > 0) {               //if for increment value is > 0
 			if(currentVal <= last)     //if for loop i is <= end condition, it keeps going and has next element
 				return true;
 			else 
 				return false;
 		}
 		else if(step < 0) {
 			if(currentVal >= last) 
 				return true;
 			else 
 				return false;
 		}
 		else 		// for loop incrementer is 0
 			return true;
 	}
 
 	public int next() { //returns next element and advances itr
 		if(hasNext()) {
 			int nextElement=currentVal;
 			currentVal+=step;  //add for incrementer to currentVal to get next element
 			return	nextElement;
 		}
 		else {
			System.err.println("ForIt called past end");
 			System.exit(1);
 			return currentVal;
 		}
 	}
 }
 		
