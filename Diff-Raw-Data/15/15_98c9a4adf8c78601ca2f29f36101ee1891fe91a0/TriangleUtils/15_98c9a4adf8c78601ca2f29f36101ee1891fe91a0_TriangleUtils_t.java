 package utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Triangle;
 import models.Triangle.Side;
 
 public class TriangleUtils {
 
   private static final double precision = 0.1;
 
   private static boolean areEqual( double first, double second ) {
     return Math.abs( first - second ) < precision;
   }
 
   public static double random( double lowerLimit, double upperLimit ) {
     double randomNumber = 0;
     double temp;
 
     if ( lowerLimit > upperLimit ) {
       temp = lowerLimit;
       lowerLimit = upperLimit;
       upperLimit = temp;
     }
 
     randomNumber = Math.random() * ( upperLimit - lowerLimit ) + lowerLimit;
 
     return randomNumber;
   }
 
   public static boolean isValid( Triangle triangle ) {
     double sideA, sideB, sideC;
 
     sideA = triangle.get( Side.a );
     sideB = triangle.get( Side.b );
     sideC = triangle.get( Side.c );
 
     if ( ( sideA + sideB ) <= sideC ) return false;
     if ( ( sideA + sideC ) <= sideB ) return false;
     if ( ( sideB + sideC ) <= sideA ) return false;
 
     return true;
   }
 
  public static boolean isEquilateral( Triangle triangle ) {
     double sideA, sideB, sideC;
 
     sideA = triangle.get( Side.a );
     sideB = triangle.get( Side.b );
     sideC = triangle.get( Side.c );
 
    if ( areEqual( sideA, sideB ) && areEqual( sideA, sideC ) ) return true;
 
     return false;
   }
 
  public static boolean isIsosceles( Triangle triangle ) {
     double sideA, sideB, sideC;
 
     sideA = triangle.get( Side.a );
     sideB = triangle.get( Side.b );
     sideC = triangle.get( Side.c );
 
    if ( areEqual( sideA, sideB ) ) return true;
    if ( areEqual( sideA, sideC ) ) return true;
    if ( areEqual( sideB, sideC ) ) return true;
 
     return false;
   }
 
   public static boolean isRight( Triangle triangle ) {
     double sideA, sideB, sideC;
 
     sideA = triangle.get( Side.a );
     sideB = triangle.get( Side.b );
     sideC = triangle.get( Side.c );
 
     if ( areEqual( sideA * sideA, sideB * sideB + sideC * sideC ) ) return true;
     if ( areEqual( sideB * sideB, sideA * sideA + sideC * sideC ) ) return true;
     if ( areEqual( sideC * sideC, sideA * sideA + sideB * sideB ) ) return true;
 
     return false;
   }
 
   public static List<TriangleTypes> typeOfTriangle( Triangle triangle ) {
     List<TriangleTypes> types = new ArrayList<TriangleTypes>();
 
     if ( isEquilateral( triangle ) ) {
       types.add( TriangleTypes.EQUILATERAL );
       return types;
     }
 
     if ( isIsosceles( triangle ) ) {
       types.add( TriangleTypes.ISOSCELES );
     }
 
     if ( isRight( triangle ) ) {
       types.add( TriangleTypes.RIGHT );
     }
 
     if ( types.isEmpty() ) {
       types.add( TriangleTypes.SCALENE );
     }
 
     return types;
   }
 }
