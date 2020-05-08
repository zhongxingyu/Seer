class A {
	void b () {
		System.out.print("Creation de a\n");
	}
}
class Main {
	public static void main(String args[]) {
		A a = new A() ;
		a.b();
	}
}
