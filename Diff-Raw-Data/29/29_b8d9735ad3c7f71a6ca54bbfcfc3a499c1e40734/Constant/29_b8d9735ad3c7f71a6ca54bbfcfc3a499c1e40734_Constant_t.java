 /**
  * The simplest class to implement Expression.
  * Defined by a single value.
  *
  */
 public class Constant implements Expression{
     private double num;
 
     /**
      * Constructs a new Constant using a default value of 0.
      */
     public Constant() {
         this.num = 0;
     }
 
     /**
      * Constructs a new Constant using a specified value. 
      * @param num the value of the Constant.
      */
     public Constant(double num) {
         this.num = num;
     }
 
     public double evaluate(Assignments assignments) {
         return num;
     }
 
     public Expression derivative(Variable var) {
        if (var == null) {
            throw new RuntimeException("Variable argument is null.");
        } else {
            return new Constant(0.0);
        }
     }
 
     public String toString() {
         return "" + num;
     }
 
     public boolean equals(Object o) {
         if (o instanceof Constant) {
             Constant other = (Constant)o;
             return num == other.num;
         }
         else
             return false;
     }
 }
