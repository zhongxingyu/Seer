 class Main {
     public static void main(String[] args) {
        System.out.println(new B().C().array().length); // should equal 5 
     }
 }
 
 class B {
     C c;
     public C C() {
         C c;
         c = new C();
         return c;
     }
 }
 
 class C {
    public int[] array() {
        return new int[5];
    }
 }
