 public class Floats {
   private static void expect(boolean v) {
     if (! v) throw new RuntimeException();
   }
 
   private static double multiply(double a, double b) {
     return a * b;
   }
 
   private static float multiply(float a, float b) {
     return a * b;
   }
 
   private static double divide(double a, double b) {
     return a / b;
   }
 
   private static double subtract(double a, double b) {
     return a - b;
   }
 
   public static void main(String[] args) {
     expect(multiply(0.5d, 0.5d) == 0.25d);
     expect(multiply(0.5f, 0.5f) == 0.25f);
 
     expect(multiply(0.5d, 0.5d) < 0.5d);
     expect(multiply(0.5f, 0.5f) < 0.5f);
 
     expect(multiply(0.5d, 0.5d) > 0.1d);
     expect(multiply(0.5f, 0.5f) > 0.1f);
 
     expect(divide(0.5d, 0.5d) == 1.0d);
 
     expect(subtract(0.5d, 0.5d) == 0.0d);
   }
 }
