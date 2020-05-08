 package JVM;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 
 public class StaticMethods {
 
	public static void println(String o) {
 		System.out.println(o);
 	}
 
 	public static void assert_(boolean b) {
 		assert b;
 	}
 
 	//-----------------------------------------------------
 
 	public static int unary_plus(int n) {
 		return +n;
 	}
 
 	public static int unary_minus(int n) {
 		return -n;
 	}
 
 	public static int unary_not(int n) {
 		return ~n;
 	}
 
 	public static boolean unary_not(boolean b) {
 		return !b;
 	}
 
 	//-----------------------------------------------------
 
 	public static boolean binary_eq(boolean x, boolean y) {
 		return x == y;
 	}
 
 	public static boolean binary_ne(boolean x, boolean y) {
 		return x != y;
 	}
 
 	//-----------------------------------------------------
 
 	public static int binary_add(int x, int y) {
 		return x + y;
 	}
 
 	public static int binary_sub(int x, int y) {
 		return x - y;
 	}
 
 	public static int binary_mul(int x, int y) {
 		return x * y;
 	}
 
 	public static int binary_div(int x, int y) {
 		return x / y;
 	}
 
 	public static int binary_mod(int x, int y) {
 		return x % y;
 	}
 
 	public static int binary_shl(int x, int y) {
 		return x << y;
 	}
 
 	public static int binary_shr(int x, int y) {
 		return x >> y;
 	}
 
 	public static boolean binary_lt(int x, int y) {
 		return x < y;
 	}
 
 	public static boolean binary_le(int x, int y) {
 		return x <= y;
 	}
 
 	public static boolean binary_gt(int x, int y) {
 		return x > y;
 	}
 
 	public static boolean binary_ge(int x, int y) {
 		return x >= y;
 	}
 
 	public static boolean binary_eq(int x, int y) {
 		return x == y;
 	}
 
 	public static boolean binary_ne(int x, int y) {
 		return x != y;
 	}
 
 	//-----------------------------------------------------
 
 	public static boolean binary_eq(String x, String y) {
 		return x.equals(y);
 	}
 
 	public static boolean binary_ne(String x, String y) {
 		return !x.equals(y);
 	}
 
 	//-----------------------------------------------------
 
 	public static String binary_add(String x, String y) {
 		return x + y;
 	}
 
 	public static String binary_add(String x, int y) {
 		return x + y;
 	}
 
 	public static String binary_add(String x, boolean y) {
 		return x + y;
 	}
 
 	public static String binary_add(int x, String y) {
 		return x + y;
 	}
 
 	public static String binary_add(boolean x, String y) {
 		return x + y;
 	}
 
 	//-----------------------------------------------------
 
 	public static HashMap<String, Method> getAllStaticMethods() {
 		HashMap<String, Method> map = new HashMap<String, Method>();
 		try {
 			Class<?> self = StaticMethods.class;
 			map.put("unary_+_I", self.getMethod("unary_plus", int.class));
 			map.put("unary_-_I", self.getMethod("unary_minus", int.class));
 			map.put("unary_~_I", self.getMethod("unary_not", int.class));
 			map.put("unary_!_Z", self.getMethod("unary_not", boolean.class));
 			map.put("binary_==_ZZ", self.getMethod("binary_eq", boolean.class, boolean.class));
 			map.put("binary_!=_ZZ", self.getMethod("binary_ne", boolean.class, boolean.class));
 			map.put("binary_+_II", self.getMethod("binary_add", int.class, int.class));
 			map.put("binary_-_II", self.getMethod("binary_sub", int.class, int.class));
 			map.put("binary_*_II", self.getMethod("binary_mul", int.class, int.class));
 			map.put("binary_/_II", self.getMethod("binary_div", int.class, int.class));
 			map.put("binary_%_II", self.getMethod("binary_mod", int.class, int.class));
 			map.put("binary_<<_II", self.getMethod("binary_shl", int.class, int.class));
 			map.put("binary_>>_II", self.getMethod("binary_shr", int.class, int.class));
 			map.put("binary_<_II", self.getMethod("binary_lt", int.class, int.class));
 			map.put("binary_<=_II", self.getMethod("binary_le", int.class, int.class));
 			map.put("binary_>_II", self.getMethod("binary_gt", int.class, int.class));
 			map.put("binary_>=_II", self.getMethod("binary_ge", int.class, int.class));
 			map.put("binary_==_II", self.getMethod("binary_eq", int.class, int.class));
 			map.put("binary_!=_II", self.getMethod("binary_ne", int.class, int.class));
 			map.put("binary_+_LL", self.getMethod("binary_add", String.class, String.class));
 			map.put("binary_+_LI", self.getMethod("binary_add", String.class, int.class));
 			map.put("binary_+_IL", self.getMethod("binary_add", int.class, String.class));
 			map.put("binary_+_LZ", self.getMethod("binary_add", String.class, boolean.class));
 			map.put("binary_+_ZL", self.getMethod("binary_add", boolean.class, String.class));
 			map.put("binary_==_LL", self.getMethod("binary_eq", String.class, String.class));
 			map.put("binary_!=_LL", self.getMethod("binary_ne", String.class, String.class));
 			map.put("assert",  self.getMethod("assert_", boolean.class));
			map.put("println", self.getMethod("println", String.class));
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return map;
 	}
 }
