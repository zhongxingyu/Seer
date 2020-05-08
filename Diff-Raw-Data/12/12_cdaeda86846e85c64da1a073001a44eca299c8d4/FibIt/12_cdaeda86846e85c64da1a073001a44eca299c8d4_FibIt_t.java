 // Fib iterator
 
 public class FibIt implements SeqIt {
 
     // members
     protected int first1, first2, last;
     protected int f1, f2, sum;
     protected int index;
 
     // Constructor
     public FibIt(Fib FibObj) {
         this.first1 = FibObj.first1;
         this.first2 = FibObj.first2;
         this.last = FibObj.last;
 
         this.f1 = this.first1;
         this.f2 = this.first2;
         this.sum = this.first1;
         this.index = 0;
     }
 
     // ---------
     // Methods
     // ---------
 
     public boolean hasNext() {
         if (sum <= last) {
             return true;
         }
 
         return false;
     }
 
     public int next() {
         int retVal = 0;
         if (index == 0 && first1 <= last) {
             retVal = sum;
             sum = first2;
         } else if (index == 1 && first2 <= last) {
             retVal = sum;
             sum = first1 + first2;
         } else if (sum <= last) {
             retVal = sum;
             f1 = f2;
             f2 = sum;
             sum = f1 + f2;
         } else {
            System.err.println("FibIt called past end");
             System.exit(1);
         }
         index++;
         return retVal;
     }
 }
