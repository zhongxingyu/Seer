 /**
  * @author Ory Band
  * @version 1.0
  */
 public class ArrayAssignments implements Assignments {
     private Assignment[] assignments;
 
     /** @return new ArrayAssignment object with no assignments. */
     public ArrayAssignments() {
         this.assignments = new Assignment[0];
     }
 
     /**
      * @param assignments Assignment array.
      *
      * @return new ArrayAssignment object with initial assignments given as argument.
      */
     public ArrayAssignments(Assignment[] s) {
         if (s == null) {
             throw new RuntimeException("Assignment[] argument is null.");
         }
 
        this.assignments = s;  // Shallow copy according to FAQ page.

        // Deep copy assignments.
        /*this.assignments = new Assignment[s.length];
 
         for (int i=0; i < s.length; i++) {
             this.assignments[i] = s[i];
        }*/
     }
 
     public double valueOf(Variable v) {
         // Validate argument.
         if (v == null) {
             throw new RuntimeException("Variable argument is null.");
         }
 
         // Search for v in the assignments list.
         for (int i=0; i < this.assignments.length; i++) {
             if (this.assignments[i].getVar().equals(v)) {
                 return this.assignments[i].getValue();
             }
         }
 
         // If v wasn't found, throw exception.
         throw new RuntimeException("Variable " + v.getName() + " has no valid assignment!");
     }
 
     public void addAssignment(Assignment a) {
         if (a == null) {
             throw new RuntimeException("Assignment argument is null.");
         }
 
         // Update assignment if it's already in the assignments' list.
         int i;
         boolean stop=false;
         for (i=0; i < this.assignments.length && !stop; i++) {
             if (this.assignments[i].equals(a)) {
                 this.assignments[i].setValue(a.getValue());
                 stop=true;
             }
         }
 
         // Creates a new list, and add a single new assignment to it.
         Assignment[] copy = new Assignment[assignments.length +1];
 
         // Copies the old assignments.
         for (i=0; i < this.assignments.length; i++) {
             copy[i] = this.assignments[i];
         }
 
         // Add the new assignment to the end of the list.
         copy[copy.length -1] = a;
 
         // Replace the old assignments list with new one.
         this.assignments = copy;
     }
 }
 
