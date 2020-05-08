 class Main {
     public static void main(String[] args) {
        System.out.println(new B().C().array()[0][1]); // should equal 5 
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
         int[] array;
         array = new int[2];
         array[0] = 5;
         array[1] = 4;
         return array;
    }
 }
