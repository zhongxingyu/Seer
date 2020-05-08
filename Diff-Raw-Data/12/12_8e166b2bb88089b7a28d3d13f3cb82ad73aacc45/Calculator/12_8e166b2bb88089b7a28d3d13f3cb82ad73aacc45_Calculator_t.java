 package pl.mmatuszak.ift;
 
 
 public class Calculator 
 {
     public int add(int x, int y) {
         return x + y;
     }
 
     public int subtract(int x, int y) {
         return x + neg(y);
     }
 
     public int multiply(int x, int y) {
         return x * y;
     }
 
     public int divide(int x, int y) {
         throw new UnsupportedOperationException("to do divide");
     }
 
     public int neg(int x) {
        return -x;
     }
 
     public int abs(int x) {
         throw new UnsupportedOperationException("to do abs");
     }
 
     public int min(int x, int y) {
         throw new UnsupportedOperationException("to do min");
     }
 
     public int max(int x, int y) {
         throw new UnsupportedOperationException("to do max");
     }
 
 
     public int min(int x, int y, int z) {
         throw new UnsupportedOperationException("to do min");
     }
 
     public int max(int x, int y, int z) {
         throw new UnsupportedOperationException("to do max");
     }
 
     public int min(int x, int y, int z, int t) {
         throw new UnsupportedOperationException("to do min");
     }
 
     public int max(int x, int y, int z, int t) {
         throw new UnsupportedOperationException("to do max");
     }
 
 }
