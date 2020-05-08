 /**
  * @author Ory Band
  * @version 1.0
  */
 public class ValueAssignment implements Assignment {
     private Variable var;
     private double   value;
 
     /**
      * Assign initial values.
      *
      * @param var Variable object.
      * @param value Variable's value.
      *
      * @return New ValueAssignment object.
      */
    public ValueAssignment(Variable var, double value) {
         this.var   = var;
        this.value = value;
     }
 
     /** @return True if var instances and their values are equal. */
    public boolean equals(ValueAssignment other) {
        return this.var.equals(other.getVar()) &&
               this.value == other.getValue();
     }
 
     /** @return string in format "name=value" . */
     public String toString() {
        return var.getName + "=" + value;
     }
 
     public Variable getVar() {
         return var;
     }
 
     public double getValue() {
         return value;
     }
 
     public void setVaule(double value) {
         this.value = value;
     }
 }
 
