 package SigmaEC.util;
 
 import java.util.Collection;
 import java.util.Random;
 
 /**
  *
  * @author Eric 'Siggy' Scott
  */
 public class Misc
 {
     /**
      * Returns false if c contains an object of any other type, true otherwise.
      * This uses getClass() to compare, not instanceof, so subtypes of type will
      * cause a false result.
      */
     public static boolean containsOnlyClass(Collection c, Class type)
     {
         assert(c != null);
         assert(type != null);
         for (Object o : c)
             if (o.getClass() != type)
                 return false;
         return true;
     }
     
     /**
      * Irwin-Hall approximation of a standard Gaussian distribution.
      */
     public static double gaussianSample(Random random)
     {
         double sum = 0;
         for (int i = 0; i < 12; i++)
             sum += random.nextDouble();
        return sum/6;
     }
     
 }
