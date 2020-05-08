 /**
  * @author Joe Cumbo & Pratik Sampat
  * @since 10/2/2013
  * 
  *        This program will read the input.txt file and run calculations on the
  *        data.
  */
 
 public class Quadratic {
     private double coefA;
     private double coefB;
     private double coefC;
 
     public Quadratic(double coefA, double coefB, double coefC) {
         this.coefA = coefA;
         this.coefB = coefB;
         this.coefC = coefC;
     }
 
     /**
      * Gets the first coefficient.
      * 
      * @return The first coefficient of the equation.
      */
     public double getCoefA() {
         return coefA;
     }
 
     /**
      * Gets the second coefficient.
      * 
      * @return The second coefficient of the equation.
      */
     public double getCoefB() {
         return coefB;
     }
 
     /**
      * Gets the third coefficient.
      * 
      * @return The third coefficient of the equation.
      */
     public double getCoefC() {
         return coefC;
     }
 
     /**
      * Gets the discriminant.
      * 
      * @return Calculates and returns the discriminant of the equation.
      */
     public double getDiscriminant() {
         return Math.pow(coefB, 2) - 4 * coefA * coefC;
     }
 
     /**
      * Evaluates the answer given x.
      * 
      * @param x
      *            The value to evaluated.
      * @return The evaluated answer.
      */
     public double evalExpression(double x) {
         return (coefA * Math.pow(x, 2)) + (coefB * x) + coefC;
     }
 
     /**
      * Adds two quadratics together.
      * 
      * @param quad1
      *            The first quadratic to be added.
      * @param quad2
      *            The second quadratic to be added.
      * @return A new quadratic whos coefficients are sum of the given
      *         quadratics.
      */
     public static Quadratic sum(Quadratic quad1, Quadratic quad2) {
         return new Quadratic(quad1.getCoefA() + quad2.getCoefA(), quad1.getCoefB() + quad2.getCoefB(), quad1.getCoefC() + quad2.getCoefC());
     }
 
     /**
      * Scales the given quadratic by a given amount.
      * 
      * @param quad1
      *            The quadratic to be scaled.
      * @param amount
      *            The amount to scale the quadratic by.
      * @return A new quadratic that is scaled by the amount.
      */
     public static Quadratic scale(Quadratic quad1, double amount) {
         return new Quadratic(quad1.getCoefA() * amount, quad1.getCoefB() * amount, quad1.getCoefC() * amount);
     }
 
     /**
      * Gets the number of real roots in the equation, returning 3 for infinity.
      * 
      * @return The number of real roots in the equation.
      */
     public int getRootNum() {
         if(coefA != 0) {
             if(Math.pow(2, coefB) < 4 * coefA * coefC) {
                 return 0;
             } else if(Math.pow(2, coefB) > 4 * coefA * coefC) {
                 return 2;
             } else {
                 return 1;
             }
         } else {
             if(coefB != 0) {
                 return 1;
             } else if(coefC != 0) {
                 return 0;
             } else {
                 return 3;
             }
         }
     }
 
     /**
      * Gets the first root.
      * 
      * @return The first root of the equation if there are 1 or 2 real roots.
      *         Else returns 0.
      */
     public double getRootOne() {
         int totalRoots = getRootNum();
         if(totalRoots > 0 && totalRoots < 3) {
            if(coefA == 0 && coefB != 0) {
                return -coefC / coefB;
            } else {
                 return (-coefB + Math.sqrt(getDiscriminant())) / (2 * coefA);
             }
         }
         return 0;
     }
 
     /**
      * Gets the second root.
      * 
      * @return The second root of the equation if there are 2 real roots. Else
      *         returns 0.
      */
     public double getRootTwo() {
         int totalRoots = getRootNum();
         if(totalRoots == 2) {
             return (-coefB - Math.sqrt(getDiscriminant())) / (2 * coefA);
         } else {
             return 0;
         }
     }
 
     /**
      * ToString method
      */
     @Override
     public String toString() {
         return coefA + "x² + " + coefB + "x + " + coefC;
     }
 
     /**
      * Checks to see if the given quadratic is an alias of the current
      * quadratic.
      * 
      * @param quad
      *            The Quadratic that will be compared against.
      * @return True if the given Quadratic is an alias of current Quadratic.
      */
     public boolean isAlias(Quadratic quad) {
         return this == quad;
     }
 
     /**
      * Equals method
      */
     @Override
     public boolean equals(Object obj) {
         if(this == obj) {
             return true;
         }
         if(obj == null) {
             return false;
         }
         if(getClass() != obj.getClass()) {
             return false;
         }
         Quadratic other = (Quadratic) obj;
         if(Double.doubleToLongBits(coefA) != Double.doubleToLongBits(other.coefA)) {
             return false;
         }
         if(Double.doubleToLongBits(coefB) != Double.doubleToLongBits(other.coefB)) {
             return false;
         }
         if(Double.doubleToLongBits(coefC) != Double.doubleToLongBits(other.coefC)) {
             return false;
         }
         return true;
     }
 
     /**
      * Clone Method
      */
     @Override
     public Quadratic clone() {
         return new Quadratic(coefA, coefB, coefC);
     }
 }
