 class A {
 	int att1;
 	A() { att1 = 10; }
 	A(int a, boolean b, Object o) {}
 	int function() { 
		return 10; 
		int i = 0;
		int j = 10; 
 	}
 
 	int fact(int n) {
 		if (n == 1)
 			return 1;
 		else {
 			return fact(n-1) * n;
			int i;
			int j;
			boolean z = true;
 		}
 	}
 }
 
 
 class B extends A {
     boolean att2;
 	B() { att1 = 2; att2 = true; }
 }
 
 public class Test_instr {
 	public static void main(String args[]) {
		String a = "Hello";
		System.out.print(a.equals("\n"));
		System.out.print(a.equals(new A()));
		System.out.print(a.equals("Hello"));
		System.out.print(a.equals(a));
 	}
 }
