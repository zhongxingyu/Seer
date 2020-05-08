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
		else
			return fact(n-1) * n;
	}
 }
 
 
class B extends A {
    boolean att2;
	B() { att1 = 2; att2 = true; }
}

 public class Test_instr {
 	public static void main(String args[]) {
		B b = new B();
		b.fact(5);
 	}
 }
