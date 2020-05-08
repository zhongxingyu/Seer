 package com.hceris.strings;
 
import java.util.Arrays;

 import com.google.common.base.Preconditions;
 
 public class Matching {
     private Matching() {}
 
     public static int simple(String s, String p) {
         return simple(Preconditions.checkNotNull(s.toCharArray()), Preconditions.checkNotNull(p.toCharArray()));
     }
     
     public static int simple(char[] s, char[] p) {
         int n = s.length;
         int m = p.length;
 
         if (n < m) {
             return -1;
         }
 
         for(int i = 0; i < n - m + 1; i++) {
             if(isMatch(s, i, p)) {
                 return i;
             }
         }
         return -1;
     }
 
     private static boolean isMatch(char[] s, int i, char[] p) {
         for(int j = 0; j < p.length; j++) {
             if(p[j] != s[i + j]) {
                 return false;
             }
         }
         return true;
     }
 
     public static int kmp(String s, String p) {
         return kmp(Preconditions.checkNotNull(s.toCharArray()), Preconditions.checkNotNull(p.toCharArray()));
     }
     
     public static int kmp(char[] s, char[] p) {
         int[] t = prekmp(p);
         int i = 0;
         int m = 0;
 
         while(i + m < s.length) {
             if(s[i + m] == p[m]) {
                 if(m == p.length - 1) {
                     return i;
                 }
 
                 m++;
             } else {
                 i = i + m - t[m];
                 m = t[m] == -1 ? 0 : t[m];
             }
         }
         return -1;
     }
 
     private static int[] prekmp(char[] p) {
         if(p.length == 1) { return new int[] { -1 }; }
 
         int[] t = new int[p.length];
         t[0] = -1;
         t[1] = 0;
 
         int sub = 0;
         int i = 2;
 
         while(i < t.length) {
             if(p[i-1] == p[sub]) {
                 sub++;
                 t[i] = sub;
                 i++;
             } else if(sub > 0) {
                 sub = t[sub];
             } else {
                 t[i] = 0;
                 i++;
             }
         }
 
         return t;
     }
 }
