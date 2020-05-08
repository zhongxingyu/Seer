 public class FibIt implements SeqIt {   //implements SeqIt means it contains functinos in SeqIt class
 	
 	int first, second, last, currentVal, nextVal, index;
 	
 	public FibIt(Fib obj) {
 		currentVal=obj.first1;
 		nextVal=currentVal;
 		first=obj.first1;
 		second=obj.first2;
 		last=obj.last;
 		index=1;
 	}
 	
 	public boolean hasNext() { 
 		if(nextVal <= last) 
 			return true;
 		else 
 			return false;
 	}
 	
 	public int next() {
 		if(hasNext()) {
 			if(index == 1) { 
 				index++;
 				currentVal=first;
 				nextVal=second;
 			}
 			else if(index == 2) {
  				index++;
 				currentVal=second;
 				nextVal=first+second;
 			}
 			else {	//if past 2nd index  then must add previous two values to get next value
 
 				currentVal=nextVal; //nextVal=first+second
 				first=second;
 				second=currentVal;
 				nextVal=first+second;
 			}
 		}
 		else {
			System.err.println("FibIt called past end");
 			System.exit(1);
 		}
 		return currentVal;
 	}
 }
