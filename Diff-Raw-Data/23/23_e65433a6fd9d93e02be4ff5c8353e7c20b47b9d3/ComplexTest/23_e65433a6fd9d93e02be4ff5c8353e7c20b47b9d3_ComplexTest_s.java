 public class ComplexTest
 {
  public static void main(String[] args)
  {
   Complex c1 = new Complex(5, 2);
   Complex c2 = new Complex(2, 13);
   Complex c3;
 
   c3 = ComplexMath.conjugate(c1);
  System.out.println(c3.strRep());
 
   c3 = ComplexMath.reciprocal(c1);
  System.out.println(c3.strRep());
 
   c3 = ComplexMath.add(c1, c2);
  System.out.println(c3.strRep());
 
   c3 = ComplexMath.subtract(c1, c2);
  System.out.println(c3.strRep());
 
   c3 = ComplexMath.multiply(c1, c2);
  System.out.println(c3.strRep());
 
   c3 = ComplexMath.divide(c1, c2);
  System.out.println(c3.strRep());
 
   c3 = ComplexMath.pow(c1, 3);
  System.out.println(c3.strRep());
 
   double mag = ComplexMath.mag(c1);
  System.out.println(mag);
  }
 }
