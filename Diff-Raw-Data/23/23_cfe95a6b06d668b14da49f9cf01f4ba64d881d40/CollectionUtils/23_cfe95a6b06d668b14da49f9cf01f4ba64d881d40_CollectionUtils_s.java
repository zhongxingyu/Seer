 package net.retakethe.policyauction.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Nick Clarke
  */
 public final class CollectionUtils {
 
    public static <T> List<T> list(T e1) {
         List<T> l = new ArrayList<T>(1);
         l.add(e1);
         return l;
     }
 
    public static <T> List<T> list(T e1, T e2) {
         List<T> l = new ArrayList<T>(2);
         l.add(e1);
         l.add(e2);
         return l;
     }
 
    public static <T> List<T> list(T e1, T e2, T e3) {
         List<T> l = new ArrayList<T>(3);
         l.add(e1);
         l.add(e2);
         l.add(e3);
         return l;
     }
 
    public static <T> List<T> list(T e1, T e2, T e3, T e4) {
         List<T> l = new ArrayList<T>(4);
         l.add(e1);
         l.add(e2);
         l.add(e3);
         l.add(e4);
         return l;
     }
     
    public static <T> List<T> list(T e1, T e2, T e3, T e4, T e5) {
         List<T> l = new ArrayList<T>(5);
         l.add(e1);
         l.add(e2);
         l.add(e3);
         l.add(e4);
         l.add(e5);
         return l;
     }
 
    public static <T> List<T> list(T e1, T e2, T e3, T e4, T e5, T e6) {
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
