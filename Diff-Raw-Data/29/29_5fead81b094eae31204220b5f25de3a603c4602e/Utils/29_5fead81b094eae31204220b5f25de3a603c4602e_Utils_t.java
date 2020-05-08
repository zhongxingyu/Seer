 package utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Utility class.
  * 
  * @author Ben Ruijl
  * 
  */
 public class Utils {
     /**
      * A fast exp approximation. The validity range is from -700 to 700.
      * 
      * @param x
      *            Value
      * @return Return e^x
      */
     public static double exp(final double x) {
 	final long tmp = (long) (1512775 * x + (1072693248 - 60801));
 	return Double.longBitsToDouble(tmp << 32);
     }
 
     /**
      * Inverts a map.
      * 
      * @param <A>
      *            Type of the value
      * @param <B>
      *            Type of the key
      * @param map
      *            Map to invert
      * @return Inverted map
      */
     public static <A, B> Map<A, B> invert(final Map<B, A> map) {
 	final Map<A, B> result = new HashMap<A, B>();
 
 	for (final Entry<B, A> entry : map.entrySet()) {
 	    result.put(entry.getValue(), entry.getKey());
 	}
 
 	return result;
     }
 
     /**
      * Returns all combinations of k out of N.
      * 
      * @param <T>
      * @param list
      *            List of length N
      * @param k
      *            k to pick out of N
      * @return
      */
     public static <T> List<List<T>> combinations(List<T> list, int k) {
 	ArrayList<List<T>> result = new ArrayList<List<T>>();
 	for (int i = 0; i < list.size(); i++) {
 	    if (k == 1) {
 		List<T> one = new ArrayList<T>();
 		one.add(list.get(i));
 		result.add(one);
 	    } else {
 		for (List<T> next : combinations(
 			list.subList(i + 1, list.size()), k - 1)) {
 		    next.add(list.get(i));
 		    result.add(next);
 		}
 	    }
 	}
 
 	return result;
     }
 
     public static <T> List<T> complementary(List<T> total, List<T> sub) {
 	List<T> comp = new ArrayList<T>();
 	Map<T, Boolean> cont = new HashMap<T, Boolean>();
 
 	for (T a : sub) {
 	    cont.put(a, Boolean.TRUE);
 	}
 
 	for (T a : total) {
 	    if (!cont.containsKey(a)) {
 		comp.add(a);
 	    }
 	}
 
 	return comp;
     }
 
     public static <T> void permutate(List<T> a, int n, List<List<T>> result) {
 	if (n == 1) {
 	    result.add(a);
 	    return;
 	}

 	for (int i = 0; i < n; i++) {
	    List<T> b = new ArrayList<T>(a);
	    Collections.swap(b, i, n - 1);
	    permutate(b, n - 1, result);
 	}
     }
 
 }
