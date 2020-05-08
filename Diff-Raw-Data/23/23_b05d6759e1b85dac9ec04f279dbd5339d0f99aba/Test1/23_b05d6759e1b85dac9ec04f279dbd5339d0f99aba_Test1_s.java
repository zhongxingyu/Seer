 class A extends B {
 	int att1;
 	boolean att2;
 	B att3;
 	A() {}
 	A(int a) {}
 	A(int b, boolean a) {}
 	A(B b) {}
     void f() {}
	int f(int a) { return a; }
 	void f(boolean a, B cl) {}
 	B f(B a, int b, boolean c, Object d) { return a; }
 	B f(Object d, boolean c, int b, B a) { return a; }	
 	boolean test_surcharge() {}
 }
 
 class B {
 	A att4;
 	boolean test_surcharge() {}
 	B() {}
 }
 
 
 class C extends A {
 	int test_surcharge() {}
 }
 
 public class Test1 {
 	public static void main(String args[]) {}
 }
