/**
 * this is believed to be a compiler bug in Java 6, it was fixed in Java 7
 */
 public class VarargTest {
     public void v(int... vals) {
 	System.out.println("Super");
     }
 
     public static void main(String... args) {
 	new SubVararg().v(1, 2, 3);
     }
 }
 
 class SubVararg extends VarargTest {
     @Override
     public void v(int[] vals) {
 	System.out.println("Sub");
     }
 }
 
