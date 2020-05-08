 class Main {
     public static void main(String[] s) { 
        System.out.println(new A().C().B().D());
     }
 }
 
 class A {
     public C C() { return new C(); }
 }
 
 class B {
    public int D() { return 5; }
 }
 
 class C {
     public B B() { return new B(); }
 }
