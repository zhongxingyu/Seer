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
         return kmp(Preconditions.checkNotNull(s.toCharArray()), Preconditions.checkNotNull(p.toCharArray()), 0, s.length() - 1);
     }
     
     public static int kmp(char[] s, char[] p, int start, int end) {
         int[] t = prekmp(p);
         int i = start;
         int m = 0;
 
         while(i + m <= end) {
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
                 t[i] = ++sub;
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
 
     public static String minimumContainingSubstring(String s, String p) {
         int[] balance = new int[256];
 
         for(int i = 0; i < p.length(); i++) {
             balance[(int) p.charAt(i)]--;
         }
 
         int count = 0;
         int[] window = new int[] {};
         int windowLength = Integer.MAX_VALUE;
 
         for(int left = 0, right = 0; right < s.length(); right++) {
             char current = s.charAt(right);
             balance[(int)current]++;
 
             if(balance[(int)current] == 0) {
                 count++;
 
                 if(count == p.length()) {
                     while(left <= right && balance[(int)s.charAt(left)] > 0) {
                         balance[(int)s.charAt(left)]--;
                         left++;
                     }
 
                     int newWindow = right - left + 1;
                     if(newWindow < windowLength) {
                         windowLength = newWindow;
                         window = new int[] { left, right };
                     }
 
                     balance[(int)s.charAt(left)]--;
                     left++;
                     count--;
                 }
             }
         }
 
         return window.length == 2 ? s.substring(window[0], window[1] + 1) : null;
     }
     
     public static String remove(String s, String p) {
     	return remove(s.toCharArray(), p.toCharArray());
     }
     
     private static String remove(char[] s, char[] p) {
     	int start = 0;
     	int end = s.length - 1;
     	while(start <= end) {
     		start = kmp(s,p, start, end);
     		
     		if(start == -1) {
     			break;
     		}
     		
     		for(int i = start; i <= end - p.length; i++) {
     			s[i] = s[i + p.length];
     		}
     		end -= p.length;
     	}
     	
     	return new String(s, 0, end + 1);
     }
 }
