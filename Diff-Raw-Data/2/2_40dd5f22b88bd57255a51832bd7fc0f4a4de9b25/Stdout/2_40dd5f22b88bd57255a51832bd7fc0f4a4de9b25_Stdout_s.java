 package javascool;
 
 public class Stdout {
   // @factory
   private Stdout() {}
 
   @Deprecated public static void clear() {
     // do nothing
   }
 
   public static void println(String string) {
     System.out.println(string);
   }
 
   public static void println() {
    System.out.println("\n");
   }
 
   public static void println(int i) {
     println("" + i);
   }
 
   public static void println(double d) {
     println("" + d);
   }
 
   public static void println(float f) {
     println("" + f);
   }
 
   public static void println(boolean b) {
     println("" + b);
   }
 
   public static void println(char c) {
     println("" + c);
   }  
 
   public static void println(Object o) {
     println("" + o);
   }
 
   public static void print(String string) {
     System.out.print(string);
   }
 
   public static void print(int i) {
     print("" + i);
   }
 
   public static void print(double d) {
     print("" + d);
   }
 
   public static void print(float f) {
     print("" + f);
   }
 
   public static void print(boolean b) {
     print("" + b);
   }
   
   public static void print(char c) {
     print("" + c);
   }  
 
   public static void print(Object o) {
     print("" + o);
   }
 }
