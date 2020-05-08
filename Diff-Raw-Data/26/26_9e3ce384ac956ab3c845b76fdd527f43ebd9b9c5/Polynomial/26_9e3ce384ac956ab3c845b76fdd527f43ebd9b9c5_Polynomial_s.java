 /**
  * Programmer: kyle
  * Date: 1/23/13
  * Time: 2:56 PM
  * Exercise:Lab01-1.26
  */
 public class Polynomial {
 
     //The attributes of the class
     private int degree;
    private int[] degrees = new int[100];
    private int[] coefficients = new int[100];
    private int count = 0;
 
     //one argument constructor that sets the degree of the polynomial
     public Polynomial(int degree) {
         this.degree = degree;
     }
 
     //sets the coefficient and degree using a counter
     public void setCoefficients(int degree, int coefficient) {
        coefficients[count] = coefficient;
        degrees[count] = degree;
        count++;
     }
 
     //does all the calculations on the polynomial
     public double evaluate(double xTerm) {
         //the total that is continuously added to
         double runningTotal = 0;
 
         //loops through the entire array
         for(int i = 0; i < coefficients.length; i++) {

            //this if statement only works if the degree is > 0 (ex nX^b  b must be > 0)
            if(degrees[i] > 0) {
                double total = coefficients[i] * Math.pow(xTerm, degrees[i]);
                runningTotal += total;
            }

            //this if statement handles cases where it would be xTerm^0 which would be coefficients[i] * 1
            if(degrees[i] == 0) {
                double total = coefficients[i];
                runningTotal += total;
            }
         }
         return runningTotal;
     }
 
     //gets the degree of the polynomial
     public int getDegree() {
         return degree;
     }
 }
