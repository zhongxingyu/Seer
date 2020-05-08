 package LJava;
 
 public class Utils {
 	
 	static public enum Result {
 		SUCCESS, FAIL, FAIL_ELSEWHERE 
 	}
 	
 	
 	
 	public static boolean variable(Object x) {
 		return (x instanceof Variable);
 	}		
 }
