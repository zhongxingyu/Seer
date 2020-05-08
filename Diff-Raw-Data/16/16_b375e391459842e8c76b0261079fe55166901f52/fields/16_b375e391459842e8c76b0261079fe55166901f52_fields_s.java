 public class fields {
 	public static void main(String[] args) {
 		f1.f.j.method();
 		
 		f1 f = new f1();
 		f.f.j.method();
 		
 		/**
 		//This is not legal Java:
 		//the variable "java" was declared, so we can't access anything in java.* anymore
 		Object java = new Object();
 		Class c = java.lang.Integer.TYPE;
 		//*/
 	}
 }
 
 class f1 {
 	public static f2 f;
 	public f2 m = new f2();
 }
 
 class f2 {
 	public static f3 j;
 }
 
 class f3 {
 	public static void method() {
 		System.out.println("f3.method() called");
 	}
 }
