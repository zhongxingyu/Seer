 /*
  * Stores the variables contained in each Clause of the Formula
  * To be Stored in HashObjects two ArrayLists
  */
 
 
 /**
  *
  * @author Trevor Stevens
  */
 
 public class Clause {
     private int variables[];
     private int size,i;
     final private int length;
 
     public Clause(final int size,final int a[]) {
         variables = a;
         length = size;
         this.size = size;
     }
 
     public int size() {
         return size;
     }
 
     public int actualSize() {
         return length;
     }
 
     public void removeVar(final int var) {
         //remove and place a 0 in place of the variable in the array to hold position
         //also update clause size with the update size of the array
         for (i=0; i<length; i++) {
             if (variables[i] == var) {
                 variables[i] = 0;
                 size--;
             }
         }
     }
 
     public void addVar(final int var) {
        for (i=0; i<length; i++) {
             if (variables[i] == 0) {
                 variables[i] = var;
                 size++;
                 break;
             }
         }
     }
 
     public int get(final int index) {
         //*ensure index is formated for array*
         return variables[index];
     }
 
     public int lengthOne() {
         return (size == 1) ? findOne() : 0;
     }
 
     private int findOne() {
         for(i = 0; i<length ; i++) {
             if(variables[i] != 0) {
                 return variables[i];
             }
         }
        return 0; //base case should ever happen.
     }
 
     @Override
     public String toString() {
         final String delimiter = " ";
         final int varLen = variables.length;
         final StringBuilder buf = new StringBuilder();
         for (int curVar = 0; curVar < varLen; ++curVar) {
             buf.append(variables[curVar]);
             buf.append(delimiter);
         }
         return buf.toString();
     }
 }
