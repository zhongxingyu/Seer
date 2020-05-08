 class Main {
     public static void main(String[] s) { 
        System.out.println(new A().C().B().Br()[3]);
     }
 }
 
 class A {
     public C C() { return new C(); }
 }
 
 class B {
    int[] br;

    public int[] Br() { return br; }
 }
 
 class C {
     public B B() { return new B(); }
 }
