 import java.util.*;
 
 class UniqueString {
     public static void main(String... args) {
 	String s = "abc";
 	String s1 = "abcsdesad";
 	System.out.println(isUnique(s));
 	System.out.println(isUnique(s1));
     }
 
     public static boolean isUnique(String s) {
 	return isUniqueInt(s);
     }
 
     public static boolean isUniqueHashSet(String s) {
 	HashSet<Character> set = new HashSet<Character>();
 	for (int i = 0; i < s.length(); i++) {
 	    if (!set.add(s.charAt(i))) return false;
 	}
 	return true;
     }
 
     public static boolean isUniqueInt(String s) {
 	int base = 0;
 	for (int i = 0; i < s.length(); i++) {
	    int mark = 1 << (int)s.charAt(i);
 	    if ((mark & base) != 0) return false;
 	    else base += mark;
 	}
 	return true;
     }
 }
