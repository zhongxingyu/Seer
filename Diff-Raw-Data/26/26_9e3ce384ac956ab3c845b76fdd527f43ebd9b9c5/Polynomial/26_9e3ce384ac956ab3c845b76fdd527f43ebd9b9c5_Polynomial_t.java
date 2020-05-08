 /**
  * Programmer: kyle
  * Date: 1/23/13
  * Time: 2:56 PM
  * Exercise:Lab01-1.26
  */
 public class Polynomial {
 
     //The attributes of the class
     private int degree;
    private int[] coefficients;
 
     //one argument constructor that sets the degree of the polynomial
     public Polynomial(int degree) {
         this.degree = degree;
        coefficients = new int[this.degree+1];

     }
 
     //sets the coefficient and degree using a counter
     public void setCoefficients(int degree, int coefficient) {
        coefficients[degree] = coefficient;
     }
 
     //does all the calculations on the polynomial
     public double evaluate(double xTerm) {
         //the total that is continuously added to
         double runningTotal = 0;
 
         //loops through the entire array
         for(int i = 0; i < coefficients.length; i++) {
            runningTotal += coefficients[i]*Math.pow(xTerm, i);
         }
         return runningTotal;
     }
 
     //gets the degree of the polynomial
     public int getDegree() {
         return degree;
     }
 }
