 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class IntCache {
     private static int a = 256;
 
     private static List b = new ArrayList();
     private static List c = new ArrayList();
 
     private static List d = new ArrayList();
     private static List e = new ArrayList();
 
     public static int[] a(int paramInt) {
         if (paramInt <= 256) {
             if (b.size() == 0) {
                 arrayOfInt = new int[256];
                 c.add(arrayOfInt);
                 return arrayOfInt;
             }
             arrayOfInt = (int[]) b.remove(b.size() - 1);
             c.add(arrayOfInt);
             return arrayOfInt;
         }
 
         if (paramInt > a) {
             a = paramInt;
 
             d.clear();
             e.clear();
 
             arrayOfInt = new int[a];
             e.add(arrayOfInt);
             return arrayOfInt;
         }
         if (d.size() == 0) {
             arrayOfInt = new int[a];
             e.add(arrayOfInt);
             return arrayOfInt;
         }
        int[] arrayOfInt = (int[]) d.remove(d.size() - 1);
         e.add(arrayOfInt);
         return arrayOfInt;
     }
 
     public static void a() {
         if (d.size() > 0)
             d.remove(d.size() - 1);
         if (b.size() > 0)
             b.remove(b.size() - 1);
 
         d.addAll(e);
         b.addAll(c);
 
         e.clear();
         c.clear();
     }
 }
