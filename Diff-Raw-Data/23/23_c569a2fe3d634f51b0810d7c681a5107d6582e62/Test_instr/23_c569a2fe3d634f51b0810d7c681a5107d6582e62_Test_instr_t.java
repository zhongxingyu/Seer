 class A {
 	int att1;
 	A() { att1 = 10; }
 	A(int a, boolean b, Object o) {}
 	int function() { 
		return 10;
 	}
 
 	int fact(int n) {
 		if (n == 1)
 			return 1;
 		else {
 			return fact(n-1) * n;
 		}
 	}

	void newline() {
		System.out.print("\n");
	}
 }
 
 
 class B extends A {
     boolean att2;
 	B() { att1 = 2; att2 = true; }
 }
 
 public class Test_instr {
 	public static void main(String args[]) {
		A a1 = new A();
		A a2 = a1;
	    a2.att1 = 20;
		System.out.print(a1.att1);
 	}
 }
