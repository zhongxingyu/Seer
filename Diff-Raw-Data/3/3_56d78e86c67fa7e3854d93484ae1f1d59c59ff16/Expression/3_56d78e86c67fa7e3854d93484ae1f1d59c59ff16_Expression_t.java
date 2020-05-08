 /**
  * Expression.java
  *
  * PolynomialEvaluator object for evaluating an
  * expression (e.g., 3x + 2x^3, 4 x ^4 + 3x + 2)
  */
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Expression
 {
     private List<Monomial> monomials;
     
     public Expression(List<Monomial> monomials)
     {
         this.monomials = monomials;
     }
     
     public List<Monomial> getMonomials()
     {
         return this.monomials;
     }
     
     /**
      * Evaluates this Expression with the given value for x:
      */
     public double evaluate(int x)
     {
         double sum = 0;
         
         //As long as there are more Monomials in this Expression,
         //keep summing:
         for (int i = 0; i < this.monomials.size(); i++)
         {
            mon = monomials.get(i);
            sum += mon.evaluate(x);
         }
         
         return sum;
     }
 }
