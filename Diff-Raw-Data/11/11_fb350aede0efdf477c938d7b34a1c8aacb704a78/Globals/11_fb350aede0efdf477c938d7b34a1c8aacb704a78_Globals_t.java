 package lure.lang;
 
 import java.util.ArrayList;
 /**
  * <h1>Globals</h1>
  * <p>The Lure functions that are defined everywhere.</p>
  * <p>Copyright (c) 2012 Steven! Ragnarök</p>
  */
 public class Globals {
 
   public static Object equals(Object arg1, Object arg2) {
     return arg1.equals(arg2);
   }
 
  public static Object nequals(Object arg1, Object arg2) {
     return !arg1.equals(arg2);
   }
 
   /* not() in Lure. Negates the truthiness of a value. */
   public static Object not(Object arg1) {
     Boolean b = ((Boolean)arg1);
     if (b == false || b == null) {
       return true;
     } else {
       return false;
     }
   }
 
   /* The plus function. In Lure +(). */
 
   /* Lure's +(listOfNumbers) */
   public static Object plus(ArrayList args) {
     int total = 0;
     for (Object i : args) {
       total += ((Integer)i);
     }
     return total;
   }
 
   /* +(i, j) in Lure. */
   public static Object plus(Object arg1, Object arg2) {
     return ((Integer) arg1) + ((Integer) arg2);
   }
 
   /* +(i, j, k) in Lure. */
   public static Object plus(Object arg1, Object arg2, Object arg3) {
     return ((Integer) arg1) + ((Integer) arg2) + ((Integer) arg3);
   }
 
   /* you get the idea... */
   public static Object plus(Object arg1, Object arg2, Object arg3,
       Object arg4)
   {
     return ((Integer) arg1) + ((Integer) arg2) + ((Integer) arg3) +
       ((Integer) arg4);
   }
   public static Object plus(Object arg1, Object arg2, Object arg3,
       Object arg4, Object arg5)
   {
     return ((Integer) arg1) + ((Integer) arg2) + ((Integer) arg3) +
       ((Integer) arg4) + ((Integer) arg5);
   }
 
   /* The minus function. In Lure -() */
 
   /* Lure's -(listOfNumbers) */
   public static Object minus(ArrayList args) {
     int total = ((Integer)args.remove(0));
     for (Object i : args) {
       total -= ((Integer)i);
     }
     return total;
   }
 
   /* -(i, j) in Lure. */
   public static Object minus(Object arg1, Object arg2) {
     return ((Integer) arg1) - ((Integer) arg2);
   }
 
   /* -(i, j, k) in Lure. */
   public static Object minus(Object arg1, Object arg2, Object arg3) {
     return ((Integer) arg1) - ((Integer) arg2) - ((Integer) arg3);
   }
 
   /* you get the idea... */
   public static Object minus(Object arg1, Object arg2, Object arg3,
       Object arg4)
   {
     return ((Integer) arg1) - ((Integer) arg2) - ((Integer) arg3) -
       ((Integer) arg4);
   }
   public static Object minus(Object arg1, Object arg2, Object arg3,
       Object arg4, Object arg5)
   {
     return ((Integer) arg1) - ((Integer) arg2) - ((Integer) arg3) -
       ((Integer) arg4) - ((Integer) arg5);
   }
 
   /* The star function. In Lure *() */
 
   /* Lure's *(listOfNumbers) */
   public static Object star(ArrayList args) {
     int total = 1;
     for (Object i : args) {
       total *= ((Integer)i);
     }
     return total;
   }
 
   /* *(i, j) in Lure. */
   public static Object star(Object arg1, Object arg2) {
     return ((Integer) arg1) * ((Integer) arg2);
   }
 
   /* *(i, j, k) in Lure. */
   public static Object star(Object arg1, Object arg2, Object arg3) {
     return ((Integer) arg1) * ((Integer) arg2) * ((Integer) arg3);
   }
 
   /* you get the idea... */
   public static Object star(Object arg1, Object arg2, Object arg3,
       Object arg4)
   {
     return ((Integer) arg1) * ((Integer) arg2) * ((Integer) arg3) *
       ((Integer) arg4);
   }
   public static Object star(Object arg1, Object arg2, Object arg3,
       Object arg4, Object arg5)
   {
     return ((Integer) arg1) * ((Integer) arg2) * ((Integer) arg3) *
       ((Integer) arg4) * ((Integer) arg5);
   }
 
   /* The slash function. In Lure *() */
 
   /* Lure's /(listOfNumbers) */
   public static Object slash(ArrayList args) {
     int total = ((Integer)args.remove(0));
     for (Object i : args) {
       total /= ((Integer)i);
     }
     return total;
   }
 
   /* /(i, j) in Lure. */
   public static Object slash(Object arg1, Object arg2) {
     return ((Integer) arg1) / ((Integer) arg2);
   }
 
   /* /(i, j, k) in Lure. */
   public static Object slash(Object arg1, Object arg2, Object arg3) {
     return ((Integer) arg1) / ((Integer) arg2) / ((Integer) arg3);
   }
 
   /* you get the idea... */
   public static Object slash(Object arg1, Object arg2, Object arg3,
       Object arg4)
   {
     return ((Integer) arg1) / ((Integer) arg2) / ((Integer) arg3) /
       ((Integer) arg4);
   }
   public static Object slash(Object arg1, Object arg2, Object arg3,
       Object arg4, Object arg5)
   {
     return ((Integer) arg1) / ((Integer) arg2) / ((Integer) arg3) /
       ((Integer) arg4) / ((Integer) arg5);
   }
 
   public static Object puts(Object arg1) {
     System.out.println(arg1);
     return null;
   }
 
   public static Object puts(Object arg1, Object arg2) {
     System.out.println(arg1.toString() + " " + arg2.toString());
     return null;
   }
 
   public static Object puts(Object arg1, Object arg2, Object arg3) {
     System.out.println(arg1.toString() + " " + arg2.toString() + " " +
         arg3.toString());
     return null;
   }
 
   public static Object puts(Object arg1, Object arg2, Object arg3, Object arg4) {
     System.out.println(arg1.toString() + " " + arg2.toString() + " " +
         arg3.toString() + " " + arg4.toString());
     return null;
   }
 
   public static Object puts(Object arg1, Object arg2, Object arg3, Object arg4,
       Object arg5)
   {
     System.out.println(arg1.toString() + " " + arg2.toString() + " " +
         arg3.toString() + " " + arg4.toString() + " " + arg5.toString());
     return null;
   }
 }
