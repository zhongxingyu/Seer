 package net.retakethe.policyauction.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Very utility class for making lists. 
  * 
  * @author Nick Clarke
  * @author Mathew Hartley
  */
 public final class CollectionUtils {
 
     public static <T, T1 extends T>
            List<T> list(T1 e1) {
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
     
     public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T>
     		List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7) {
 		List<T> l = new ArrayList<T>(7);
 		l.add(e1);
 		l.add(e2);
 		l.add(e3);
 		l.add(e4);
 		l.add(e5);
 		l.add(e6);
 		l.add(e7);
 		return l;
     }
     
     public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T>
     		List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8) {
 		List<T> l = new ArrayList<T>(8);
 		l.add(e1);
 		l.add(e2);
 		l.add(e3);
 		l.add(e4);
 		l.add(e5);
 		l.add(e6);
 		l.add(e7);
 		l.add(e8);
 		return l;
     }
     
     public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T>
     		List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9) {
     	List<T> l = new ArrayList<T>(9);
     	l.add(e1);
     	l.add(e2);
     	l.add(e3);
     	l.add(e4);
     	l.add(e5);
     	l.add(e6);
     	l.add(e7);
     	l.add(e8);
     	l.add(e9);
     	return l;
     }
     
     public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T>
     		List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10) {
     	List<T> l = new ArrayList<T>(10);
     	l.add(e1);
     	l.add(e2);
     	l.add(e3);
     	l.add(e4);
     	l.add(e5);
     	l.add(e6);
     	l.add(e7);
     	l.add(e8);
     	l.add(e9);
     	l.add(e10);
     	return l;
     }
     
     public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T>
 			List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11) {
 		List<T> l = new ArrayList<T>(11);
 		l.add(e1);
 		l.add(e2);
 		l.add(e3);
 		l.add(e4);
 		l.add(e5);
 		l.add(e6);
 		l.add(e7);
 		l.add(e8);
 		l.add(e9);
 		l.add(e10);
 		l.add(e11);
 		return l;
 		}
     
     public static <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T>
     List<T> list(T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12) {
 		List<T> l = new ArrayList<T>(12);
 		l.add(e1);
 		l.add(e2);
 		l.add(e3);
 		l.add(e4);
 		l.add(e5);
 		l.add(e6);
 		l.add(e7);
 		l.add(e8);
 		l.add(e9);
 		l.add(e10);
 		l.add(e11);
 		l.add(e12);
 		return l;
     }
 }
