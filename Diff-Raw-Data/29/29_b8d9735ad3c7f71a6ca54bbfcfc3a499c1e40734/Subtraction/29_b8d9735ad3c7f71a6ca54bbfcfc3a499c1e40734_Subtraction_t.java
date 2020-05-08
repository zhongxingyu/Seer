 /**
  * Represents a substraction expression (a-b) between 2 complex expressions.
  *
  * @author Ory Band
  * @version 1.0
  */
 public class Subtraction implements Expression {
     private Expression a, b;
 
     /**
      * @param a First expression.
      * @param b Second expression.
      *
      * @return a new Subtraction object with two assigned expressions.
      */
     public Subtraction(Expression a, Expression b) {
         if (a == null || b == null) {
             throw new RuntimeException("Expression argument is null.");
         }
 
         this.a = a;
         this.b = b;
     }
 
     public double evaluate(Assignments s) {
         if (s == null) {
             throw new RuntimeException("Assignment argument is null.");
         }
 
         return this.a.evaluate(s) - this.b.evaluate(s);
     }
 
     public Expression derivative(Variable v){
         if (v == null) {
             throw new RuntimeException("Variable argument is null");
         }
 
         return new Subtraction(this.a.derivative(v), this.b.derivative(v));
     }
 
     public boolean equals(Subtraction s) {
         return s != null &&
               this.a.equals(s.getA()) &&
               this.b.equals(s.getB());
     }
 
     public String toString() {
         return "(" + this.a + "-" + this.b + ")";
     }
 
     /** @return this.a . */
     public Expression getA() {
         return this.a;
     }
 
     /** @return this.b . */
     public Expression getB() {
         return this.b;
     }
 }
 
