 package utils;
 
 import models.Triangle;
 import models.Triangle.Angle;
 import models.Triangle.Height;
 import models.Triangle.Side;
 
 public class SSSCase implements TriangleCase {
 
   private Triangle _triangle;
 
   public SSSCase() {
     _triangle = new Triangle();
   }
 
   public SSSCase( double sideA, double sideB, double sideC ) {
     this();
     _triangle.set( Side.a, sideA );
     _triangle.set( Side.b, sideB );
     _triangle.set( Side.c, sideC );
   }
 
   public void setCharacteristic1( double value ) {
     _triangle.set( Side.a, value );
   }
 
   public void setCharacteristic2( double value ) {
     _triangle.set( Side.b, value );
   }
 
   public void setCharacteristic3( double value ) {
     _triangle.set( Side.c, value );
   }
 
   public Triangle calculateTriangle() throws TriangleException {
     double a, b, c;
     double A, B, C;
     double hA, hB, hC;
 
     a = _triangle.get( Side.a );
     b = _triangle.get( Side.b );
     c = _triangle.get( Side.c );
 
     if ( !TriangleUtils.isValid( _triangle ) ) {
       throw new TriangleException( "Triangle not valid" );
     }
 
     A = Math.toDegrees( Math.acos( ( b * b + c * c - a * a ) / ( 2 * b * c ) ) );
     _triangle.set( Angle.A, A );
 
     B = Math.toDegrees( Math.acos( ( a * a + c * c - b * b ) / ( 2 * a * c ) ) );
     _triangle.set( Angle.B, B );
 
    C = Math.toDegrees( Math.acos( ( a * a + b * b - c * c ) / ( 2 * a * b ) ) );
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
