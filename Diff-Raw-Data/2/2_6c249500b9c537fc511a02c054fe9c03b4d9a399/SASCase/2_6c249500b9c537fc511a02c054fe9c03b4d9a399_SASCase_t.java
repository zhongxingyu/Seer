 package utils;
 
 import models.Triangle;
 import models.Triangle.Angle;
 import models.Triangle.Height;
 import models.Triangle.Side;
 
 public class SASCase implements TriangleCase {
 
   private Triangle _triangle;
 
   public SASCase() {
     _triangle = new Triangle();
   }
 
   public SASCase( double sideB, double angleA, double sideC ) {
     this();
     _triangle.set( Side.b, sideB );
     _triangle.set( Angle.A, angleA );
     _triangle.set( Side.c, sideC );
   }
 
   public void setCharacteristic1( double value ) {
     _triangle.set( Side.b, value );
   }
 
   public void setCharacteristic2( double value ) {
     _triangle.set( Angle.A, value );
   }
 
   public void setCharacteristic3( double value ) {
     _triangle.set( Side.c, value );
   }
 
   public Triangle calculateTriangle() {
     double a, b, c;
     double A, B, C;
     double hA, hB, hC;
 
     b = _triangle.get( Side.b );
     A = _triangle.get( Angle.A );
     c = _triangle.get( Side.c );
 
     a = Math.sqrt( b * b + c * c - 2 * b * c * Math.cos( Math.toRadians( A ) ) );
     _triangle.set( Side.a, a );
 
     B = Math.toDegrees( Math.asin( Math.sin( Math.toRadians( A ) ) * b / a ) );
     _triangle.set( Angle.B, B );
 
    C = 180 - A - B;
     _triangle.set( Angle.C, C );
 
     double area = _triangle.getArea();
 
     hA = 2 * area / a;
     _triangle.set( Height.hA, hA );
 
     hB = 2 * area / b;
     _triangle.set( Height.hB, hB );
 
     hC = 2 * area / c;
     _triangle.set( Height.hC, hC );
 
     return _triangle;
   }
 
 }
