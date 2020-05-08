 // For iterator
 
 public class ForIt implements SeqIt {
 
     // members
     protected int first, last, step;
     protected int current;
     protected boolean validNext;
 
     // Constructor
     public ForIt(For ForObj) {
         this.first = ForObj.first;
         this.last = ForObj.last;
         this.step = ForObj.step;
         this.current = this.first;
     }
 
     // ----------
     // Methods
     // ----------
 
     public boolean hasNext() {
         if (step > 0 && current <= last) {
             return true;
         } else if (step < 0 && current >= last) {
             return true;
         } else if (step == 0) {
             return true;
         } else {
             return false;
         }
     }
 
     public int next() {
         if (step > 0 && current > last) {
            System.err.println("ForIt called past end");
             System.exit(1);
         } else if (step < 0 && current < last) {
            System.err.println("ForIt called past end");
             System.exit(1);
         }
         int retVal = current;
         current += step;
 
         return retVal;
     }
 }
