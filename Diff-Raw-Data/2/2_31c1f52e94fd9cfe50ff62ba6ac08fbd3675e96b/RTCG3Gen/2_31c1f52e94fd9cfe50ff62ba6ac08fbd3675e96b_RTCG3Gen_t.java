 // File RTCG3.java --- calling generated methods that contain loops
 // sestoft@dina.kvl.dk * 2002
 
 // Using the gnu.bytecode package from http://www.gnu.org/software/kawa
 
 // All three generated methods are precisely as fast as the compiled
 // Java method.
 
 import gnu.bytecode.*;
 import java.io.*;			// IOException
 
public class RTCG3Generated {
   public static void main(String[] args) 
     throws IOException, NoSuchMethodException, IllegalAccessException, 
            java.lang.reflect.InvocationTargetException {
 
     int count = Integer.parseInt(args[0]);
 
     ClassType co = genclass |[
         public class MyClass {
           public static void MyMethod1(int x) {
             do {
               x = x - 1;
             } while(x > 0);
             return;
           }
           public static void MyMethod2(int x) {
             do { 
             } while( (x = x - 1) > 0 );
             return;
           }
           public static void MyMethod3(int x) {
             do {
               x--;
             } while(x > 0);
             return;
           }
         }
     ]|;
     
     // Output class file to array:
     byte[] classFile = co.writeToArray();
     // Load the class file into the JVM 
 
     Class ty = new ArrayClassLoader().loadClass("MyClass", classFile);
     {
       java.lang.reflect.Method m = 
 	ty.getMethod("MyMethod1", new Class[] { int.class }); 
       Timer t = new Timer();
       m.invoke(null, new Object[] { new Integer(count) });
       System.out.println("Generated method 1: " + t.Check() + " sec");
     }
     {
       java.lang.reflect.Method m = 
 	ty.getMethod("MyMethod2", new Class[] { int.class }); 
       Timer t = new Timer();
       m.invoke(null, new Object[] { new Integer(count) });
       System.out.println("Generated method 2: " + t.Check() + " sec");
     }
     {
       java.lang.reflect.Method m = 
 	ty.getMethod("MyMethod3", new Class[] { int.class }); 
       Timer t = new Timer();
       m.invoke(null, new Object[] { new Integer(count) });
       System.out.println("Generated method 3: " + t.Check() + " sec");
     }
     {
       Timer t = new Timer();
       YourMethod(count);
       System.out.println("Compiled Java method: " + t.Check() + " sec");
     }
   }
 
   public static void YourMethod(int n) {
     do { 
       n--;
     } while (n != 0);
   }
 }
 
 // This is needed because defineClass is protected in java.lang.ClassLoader:
 
 class ArrayClassLoader extends ClassLoader {
   public Class loadClass(String name, byte[] classFile) {
     return defineClass(name, classFile, 0, classFile.length);
   }
 }
 
 // Crude timing utility ----------------------------------------
    
 class Timer {
   private long start;
 
   public Timer() {
     start = System.currentTimeMillis();
   }
 
   public double Check() {
     return (System.currentTimeMillis()-start)/1000.0;
   }
 }
