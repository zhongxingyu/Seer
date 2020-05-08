 package city.math;
 
 import city.Pair;
 
 /**
  * An immutable equation of the form Ax+Bxy+Cy+D = 0
  * 
  * A first order (var^0 and var^1 only) bivariate (var from x, y)
  * equation.
  */
 public final class FirstOrderBivariate {
     private final double[] coeffs;
 
     public FirstOrderBivariate (double a, double b, double c, double d) {
         coeffs = new double[]{a, b, c, d};
     }
 
     public double getA () {
         return coeffs[0];
     }
 
     public double getB () {
         return coeffs[1];
     }
 
     public double getC () {
         return coeffs[2];
     }
 
     public double getD () {
         return coeffs[3];
     }
 
     private double getCoeff (int n)
             throws ArrayIndexOutOfBoundsException {
         return coeffs[n];
     }
 
     public FirstOrderBivariate scale (double factor) {
         return new FirstOrderBivariate(factor*getA(),
                 factor*getB(),
                 factor*getC(),
                 factor*getD());
     }
 
     public FirstOrderBivariate add (FirstOrderBivariate r) {
         return new FirstOrderBivariate(getA()+r.getA(),
                 getB()+r.getB(),
                 getC()+r.getC(),
                 getD()+r.getD());
     }
 
     public FirstOrderBivariate subtract (FirstOrderBivariate r) {
         return new FirstOrderBivariate(getA()-r.getA(),
                 getB()-r.getB(),
                 getC()-r.getC(),
                 getD()-r.getD());
     }
 
     private FirstOrderBivariate eliminateCoeff (FirstOrderBivariate r, int n)
             throws ArrayIndexOutOfBoundsException {
         if (getCoeff(n) == 0)
             return this;
 
         else if (r.getCoeff(n) == 0)
             return r;
 
         else
             return subtract(r.scale(getCoeff(n)/r.getCoeff(n)));
     }
 
     /**
      * Eliminate the Ax term by providing another FOB
      */
     public FirstOrderBivariate eliminateA (FirstOrderBivariate r) {
         return eliminateCoeff(r, 0);
     }
 
     /**
      * Eliminate the Bxy term by providing another FOB
      */
     public FirstOrderBivariate eliminateB (FirstOrderBivariate r) {
         return eliminateCoeff(r, 1);
     }
 
     /**
      * Eliminate the Cy term by providing another FOB
      */
     public FirstOrderBivariate eliminateC (FirstOrderBivariate r) {
         return eliminateCoeff(r, 2);
     }
 
     public Pair<Vector, Vector> solve (FirstOrderBivariate r) throws ComplexSolutionException {
         //Cancel Bxy term
         FirstOrderBivariate E0, E1 = eliminateB(r);
 
        //Is Cy term already cancelled?
        if (getC() == 0 || r.getC() == 0) {
             //Ax+Bxy+D = 0   (0)
             //A'x+C'y+D' = 0 (1)
             //Doesnt matter if one of Ax or Bxy is coincidentally gone in (0)
             E0 = eliminateC(r);
 
             //(0)  =>       x = -D/(A+By) (2)
 
             //(2)->(1)  =>  -DA'/(A+By)+C'y+D' = 0
             //              -DA'+C'y(A+By)+D'(A+By) = 0
             //              BC'y^2 + (AC'+BD')y + (AD'-DA') = 0
             Pair<Double, Double> ys = Quadratic.solve(
                     E0.getB()*E1.getC(),
                     E0.getA()*E1.getC() + E0.getB()*E1.getD(),
                     E0.getA()*E1.getD() - E0.getD()*E1.getA());
 
             //Using (2):
 
             double x0, x1;
 
             if (E0.getA() + E0.getB()*ys.getFirst() != 0)
                 x0 = -E0.getD()/(E0.getA() + E0.getB()*ys.getFirst());
 
             else
                 x0 = 0;
 
             if (E0.getA() + E0.getB()*ys.getSecond() != 0)
                 x1 = -E0.getD()/(E0.getA() + E0.getB()*ys.getSecond());
 
             else
                 x1 = 0;
 
             return new Pair<Vector, Vector>(
                     new Vector(x0, ys.getFirst()),
                     new Vector(x1, ys.getSecond()));
             //Else cancel Ax term and continue 
         } else {
             //Bxy+Cy+D = 0   (0)
             //A'x+C'y+D' = 0 (1)
             E0 = eliminateA(r);
 
             //(0)  =>       y = -D/(Bx+C) (2)
 
             //(2)->(1)  =>  A'x-DC'/(Bx+C)+D' = 0
             //              A'x(Bx+C)-DC'+D'(Bx+C) = 0
             //              BA'x^2 + (CA'+BD')x + (CD'-DC') = 0
             Pair<Double, Double> xs = Quadratic.solve(
                     E0.getB()*E1.getA(),
                     E0.getC()*E1.getA() + E0.getB()*E1.getD(),
                     E0.getC()*E1.getD() - E0.getD()*E1.getC());
 
             //Using (2):
 
             double y0, y1;
 
             if (E0.getC() + E0.getB()*xs.getFirst() != 0)
                 y0 = -E0.getD()/(E0.getC() + E0.getB()*xs.getFirst());
 
             else
                 y0 = 0;
 
             if (E0.getC() + E0.getB()*xs.getSecond() != 0)
                 y1 = -E0.getD()/(E0.getC() + E0.getB()*xs.getSecond());
 
             else
                 y1 = 0;
 
             return new Pair<Vector, Vector>(
                     new Vector(xs.getFirst(), y0),
                     new Vector(xs.getSecond(), y1));
         }
     }
 
     @Override
     public String toString () {
         return getA() + "x + " + getB() + "xy + " + getC() + "y + " + getD(); 
     }
 }
