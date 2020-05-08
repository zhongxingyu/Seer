 package net.retakethe.policyauction.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Nick Clarke
  */
 public final class CollectionUtils {
 
    public static <T>
            List<T> list(T e1) {
         List<T> l = new ArrayList<T>(1);
         l.add(e1);
         return l;
     }
 
    public static <T, T1 extends T, T2 extends T>
            List<T> list(T1 e1, T2 e2) {
         List<T> l = new ArrayList<T>(2);
         l.add(e1);
         l.add(e2);
         return l;
     }
 
    public static <T, T1 extends T, T2 extends T, T3 extends T>
            List<T> list(T1 e1, T2 e2, T3 e3) {
         List<T> l = new ArrayList<T>(3);
         l.add(e1);
         l.add(e2);
         l.add(e3);
         return l;
     }
 
    public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T>
            List<T> list(T1 e1, T2 e2, T3 e3, T4 e4) {
         List<T> l = new ArrayList<T>(4);
         l.add(e1);
         l.add(e2);
         l.add(e3);
         l.add(e4);
         return l;
     }
     
    public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T>
            List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5) {
         List<T> l = new ArrayList<T>(5);
         l.add(e1);
         l.add(e2);
         l.add(e3);
         l.add(e4);
         l.add(e5);
         return l;
     }
 
    public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T>
            List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6) {
         List<T> l = new ArrayList<T>(6);
         l.add(e1);
         l.add(e2);
         l.add(e3);
         l.add(e4);
         l.add(e5);
         l.add(e6);
         return l;
     }
 }
