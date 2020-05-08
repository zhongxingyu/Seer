 package jp.tkgktyk.wimaxhelperforaterm.my;
 
 import android.util.Log;
 
 public class MyLog {
 	private static String _getMethodName() {
 		String method = Thread.currentThread().getStackTrace()[4].getClassName();
 		method += "#" + Thread.currentThread().getStackTrace()[4].getMethodName();
 		method = method.substring(method.lastIndexOf(".")+1);
 		return method;
 	}
 	public static void d(String text) {
		Log.d(_getMethodName(), text);
 	}
 	public static void d() {
		Log.d("LogD", _getMethodName());
 	}
 	public static void e(String text) {
 		Log.e(_getMethodName(), text);
 	}
 	public static void e(Exception e) {
 		Log.e(_getMethodName(), e.toString());
 	}
 	public static void i(String text) {
 		Log.i(_getMethodName(), text);
 	}
 	public static void v(String text) {
 		Log.v(_getMethodName(), text);
 	}
 	public static void w(String text) {
 		Log.w(_getMethodName(), text);
 	}
 }
