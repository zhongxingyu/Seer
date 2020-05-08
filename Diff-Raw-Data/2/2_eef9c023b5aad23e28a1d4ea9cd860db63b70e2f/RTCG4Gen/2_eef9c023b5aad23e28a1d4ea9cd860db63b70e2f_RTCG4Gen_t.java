 // File RTCG4.java --- the power example
 // sestoft@dina.kvl.dk * 2002-09
 
 // Using the gnu.bytecode package from http://www.gnu.org/software/kawa
 
 import gnu.bytecode.*;
 import java.io.*;
 
 public class RTCG4Generated {
 
   public static void main(String[] args) 
     throws IOException, NoSuchMethodException, IllegalAccessException, 
            java.lang.reflect.InvocationTargetException 
   {
     int count = Integer.parseInt(args[0]);
     int n = 16;
 
     ClassType co = GenPowerClass(n);
 
     // Output class file in human-readable format:
    //ClassTypeWriter.print(co, System.out, 0);
 
     // Output class file to array:
     byte[] classFile = co.writeToArray();
 
     // Load the class file from byte array into the JVM 
     Class ty = new ArrayClassLoader().loadClass("MyClass", classFile);
 
     // Get the MyMethod(int):
     java.lang.reflect.Method m = ty.getMethod("MyPower", new Class[] { int.class });
  
     // Call the method:    
     System.out.println(m.invoke(null, new Object[] { new Integer(3) }));
 
     // Call the method:    
     System.out.println(Power(n, 3));
 
   }
 
   public static ClassType GenPowerClass(int n)
   {  
     return genclass |[
       public class MyClass // extends java.lang.Object
       {
 	public static int MyPower(int x)
 	{
            #genbstms|[ PowerGen(thisCode, n, #var[x]); ]|;
 	}
       }
     ]|;
   }
 
   public static void PowerGen(CodeAttr thisCode, int n, Variable #var[x])
   {
     genbstms|[
       int p;
       p = 1;
       #genbstms|[
         while (n > 0) {
          if (n % 2 == 0) 
            { 
              genbstms|[ x = x * x; ]|;
              n = n / 2; 
            }
          else 
            { 
              genbstms|[ p = p * x; ]|;
              n = n - 1; 
            }
          }
       ]|;
       return p;
     ]|;
   }
 
   public static int Power(int n, int x) {
     int p;
     p = 1;
     while (n > 0) {
       if (n % 2 == 0) 
         { x = x * x; n = n / 2; }
       else 
         { p = p * x; n = n - 1; }
     }
     return p;
   }
 
 }
 
 // This is needed because defineClass is protected in java.lang.ClassLoader:
 
 class ArrayClassLoader extends ClassLoader {
   public Class loadClass(String name, byte[] classFile) {
     return defineClass(name, classFile, 0, classFile.length);
   }
 }
