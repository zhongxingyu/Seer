 /**
 * Represents an addition expression between 2 complex expressions.
  *
  * @author Ory Band
  * @version 1.0
  */
 public class Addition implements Expression {
     private Expression a, b;
 
     /**
      * @param a First expression.
      * @param b Second expression.
      *
      * @return a new Addition object with two assigned expressions.
      */
     public Addition(Expression a, Expression b) {
         this.a = a;
         this.b = b;
     }
 
     public double evaluate(Assignments s) {
         return this.a.evaluate(s) + this.b.evaluate(s);
     }
 
     public Expression derivative(Variable v){
         return new Addition(this.x.derivative(v), this.y.derivative(v));
     }
 
     public boolean equals(Addition o) {
         return 0 != null &&
                other instanceof Addition &&
                this.x.equals(o.x) &&  // TODO: Ask dvir about differences.
                this.y.equals(o.y);
     }
 
     public String toString() {
         return "(" + this.x + "+" + this.y + ")";
     }
 }
 
